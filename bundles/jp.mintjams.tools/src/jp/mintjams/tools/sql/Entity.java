/*
 * Copyright (c) 2021 MintJams Inc.
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package jp.mintjams.tools.sql;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Entity {

	private final Connection fConnection;
	private final ParameterHandler fParameterHandler;
	private final ResultHandler fResultHandler;
	private final String fTableName;
	private final List<ColumnInfo> fColumnList = new ArrayList<>();
	private final List<ColumnInfo> fPrimaryKeyList = new ArrayList<>();
	private final Map<String, Integer> fPrimaryKeyMap = new HashMap<>();

	private Entity(Builder builder) throws SQLException {
		fConnection = builder.fConnection;
		fParameterHandler = builder.fParameterHandler;
		fResultHandler = builder.fResultHandler;

		DatabaseMetaData metadata = builder.fConnection.getMetaData();

		String[] args = builder.fName.split("\\.");
		List<String> argList = new ArrayList<>();
		for (String e : args) {
			e = e.trim();
			if (metadata.storesLowerCaseIdentifiers()) {
				e = e.toLowerCase();
			} else if (metadata.storesUpperCaseIdentifiers()) {
				e = e.toUpperCase();
			}
			argList.add(e);
		}
		while (argList.size() < 3) {
			argList.add(0, null);
		}

		String catalog = argList.get(0);
		String schema = argList.get(1);
		fTableName = argList.get(2);

		try (ResultSet results = metadata.getColumns(catalog, schema, fTableName, "%")) {
			while (results.next()) {
				do {
					if (!fTableName.equalsIgnoreCase(results.getString("TABLE_NAME"))) {
						break;
					}
					if (schema != null) {
						if (!schema.equalsIgnoreCase(results.getString("TABLE_SCHEM"))) {
							break;
						}
					}
					if (catalog != null) {
						if (!catalog.equalsIgnoreCase(results.getString("TABLE_CAT"))) {
							break;
						}
					}

					int type = results.getInt("DATA_TYPE");
					if (type == Types.OTHER) {
						String typeName = results.getString("TYPE_NAME").toUpperCase();
						if (typeName.startsWith("TIMESTAMP")) {
							type = Types.TIMESTAMP;
						}
					}
					String name = results.getString("COLUMN_NAME");
					fColumnList.add(new ColumnInfo(name, results.getInt("ORDINAL_POSITION")));
				} while (false);
			}
		}

		try (ResultSet results = metadata.getPrimaryKeys(catalog, schema, fTableName)) {
			while (results.next()) {
				do {
					if (!fTableName.equalsIgnoreCase(results.getString("TABLE_NAME"))) {
						break;
					}
					if (schema != null) {
						if (!schema.equalsIgnoreCase(results.getString("TABLE_SCHEM"))) {
							break;
						}
					}
					if (catalog != null) {
						if (!catalog.equalsIgnoreCase(results.getString("TABLE_CAT"))) {
							break;
						}
					}

					String name = results.getString("COLUMN_NAME");
					int seq = results.getInt("KEY_SEQ");
					fPrimaryKeyList.add(new ColumnInfo(name, seq));
					fPrimaryKeyMap.put(name.toLowerCase(), seq);
				} while (false);
			}
		}

		Collections.sort(fPrimaryKeyList, new Comparator<ColumnInfo>() {
			@Override
			public int compare(ColumnInfo o1, ColumnInfo o2) {
				if (o1.getPosition() < o2.getPosition()) {
					return -1;
				}
				if (o1.getPosition() > o2.getPosition()) {
					return 1;
				}
				return 0;
			}
		});

		Collections.sort(fColumnList, new Comparator<ColumnInfo>() {
			@Override
			public int compare(ColumnInfo o1, ColumnInfo o2) {
				int o1pk = fPrimaryKeyMap.containsKey(o1.getName().toLowerCase()) ? 0 : 1;
				int o2pk = fPrimaryKeyMap.containsKey(o2.getName().toLowerCase()) ? 0 : 1;
				if (o1pk < o2pk) {
					return -1;
				}
				if (o1pk > o2pk) {
					return 1;
				}
				if (o1pk == 0 && o2pk == 0) {
					int o1seq = fPrimaryKeyMap.get(o1.getName().toLowerCase());
					int o2seq = fPrimaryKeyMap.get(o2.getName().toLowerCase());
					if (o1seq < o2seq) {
						return -1;
					}
					if (o1seq > o2seq) {
						return 1;
					}
				}

				if (o1.getPosition() < o2.getPosition()) {
					return -1;
				}
				if (o1.getPosition() > o2.getPosition()) {
					return 1;
				}
				return 0;
			}
		});
	}

	public Query findByPrimaryKey(Map<String, Object> conditions) throws SQLException {
		Map<String, Object> variables = normalizeKey(conditions);

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(fTableName);
		sql.append(createWhereClause(variables, true));

		return Query.newBuilder(fConnection)
				.setStatement(sql.toString())
				.setVariables(variables)
				.setParameterHandler(fParameterHandler)
				.setResultHandler(fResultHandler)
				.build();
	}

	public Query find(Map<String, Object> conditions) throws SQLException {
		Map<String, Object> variables = normalizeKey(conditions);

		StringBuilder sql = new StringBuilder();
		sql.append("SELECT * FROM ").append(fTableName);
		sql.append(createWhereClause(variables, false));

		return Query.newBuilder(fConnection)
				.setStatement(sql.toString())
				.setVariables(variables)
				.setParameterHandler(fParameterHandler)
				.setResultHandler(fResultHandler)
				.build();
	}

	public Update create(Map<String, Object> values) throws SQLException {
		Map<String, Object> variables = normalizeKey(values);

		List<String> insertNameList = new ArrayList<>();
		for (ColumnInfo info : fColumnList) {
			String varName = info.getName().toLowerCase();
			if (variables.containsKey(varName)) {
				insertNameList.add(info.getName());
			}
		}

		StringBuilder sql = new StringBuilder();
		sql.append("INSERT INTO ").append(fTableName);
		for (int i = 0; i < insertNameList.size(); i++) {
			if (i == 0) {
				sql.append(" (");
			} else {
				sql.append(", ");
			}
			sql.append(insertNameList.get(i));
		}
		for (int i = 0; i < insertNameList.size(); i++) {
			if (i == 0) {
				sql.append(") VALUES (");
			} else {
				sql.append(", ");
			}
			sql.append("{{").append(insertNameList.get(i).toLowerCase()).append("}}");
		}
		sql.append(")");

		return Update.newBuilder(fConnection)
				.setStatement(sql.toString())
				.setVariables(variables)
				.setParameterHandler(fParameterHandler)
				.build();
	}

	public Update updateByPrimaryKey(Map<String, Object> values) throws SQLException {
		return updateByPrimaryKey(values, values);
	}

	public Update updateByPrimaryKey(Map<String, Object> updates, Map<String, Object> conditions) throws SQLException {
		Map<String, Object> variables = normalizeKey(updates);
		Map<String, Object> cnds = normalizeKey(conditions);

		for (ColumnInfo info : fPrimaryKeyList) {
			String varName = info.getName().toLowerCase();
			if (variables.containsKey(varName)) {
				variables.remove(varName);
			}
		}

		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(fTableName);
		sql.append(createSetCommand(variables));
		sql.append(createWhereClause(variables, cnds, true));

		return Update.newBuilder(fConnection)
				.setStatement(sql.toString())
				.setVariables(variables)
				.setParameterHandler(fParameterHandler)
				.build();
	}

	public Update update(Map<String, Object> updates, Map<String, Object> conditions) throws SQLException {
		Map<String, Object> variables = normalizeKey(updates);
		Map<String, Object> cnds = normalizeKey(conditions);

		StringBuilder sql = new StringBuilder();
		sql.append("UPDATE ").append(fTableName);
		sql.append(createSetCommand(variables));
		sql.append(createWhereClause(variables, cnds, false));

		return Update.newBuilder(fConnection)
				.setStatement(sql.toString())
				.setVariables(variables)
				.setParameterHandler(fParameterHandler)
				.build();
	}

	public Update deleteByPrimaryKey(Map<String, Object> conditions) throws SQLException {
		Map<String, Object> variables = normalizeKey(conditions);

		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append(fTableName);
		sql.append(createWhereClause(variables, true));

		return Update.newBuilder(fConnection)
				.setStatement(sql.toString())
				.setVariables(variables)
				.setParameterHandler(fParameterHandler)
				.build();
	}

	public Update delete(Map<String, Object> conditions) throws SQLException {
		Map<String, Object> variables = normalizeKey(conditions);

		StringBuilder sql = new StringBuilder();
		sql.append("DELETE FROM ").append(fTableName);
		sql.append(createWhereClause(variables, false));

		return Update.newBuilder(fConnection)
				.setStatement(sql.toString())
				.setVariables(variables)
				.setParameterHandler(fParameterHandler)
				.build();
	}

	private Map<String, Object> normalizeKey(Map<String, Object> map) {
		Map<String, Object> result = new HashMap<>();
		for (Map.Entry<String, Object> e : map.entrySet()) {
			result.put(e.getKey().toLowerCase(), e.getValue());
		}
		return result;
	}

	private String createSetCommand(Map<String, Object> variables) {
		StringBuilder sql = new StringBuilder();
		int i = 0;
		for (ColumnInfo info : fColumnList) {
			String varName = info.getName().toLowerCase();
			if (!variables.containsKey(varName)) {
				continue;
			}

			if (i == 0) {
				sql.append(" SET ");
			} else {
				sql.append(", ");
			}
			sql.append(info.getName()).append(" = ").append("{{").append(varName).append("}}");
			i++;
		}
		return sql.toString();
	}

	private String createWhereClause(Map<String, Object> variables, boolean primaryKeys) {
		return createWhereClause(variables, null, primaryKeys);
	}

	private String createWhereClause(Map<String, Object> variables, Map<String, Object> cnds, boolean primaryKeys) {
		StringBuilder sql = new StringBuilder();
		int i = 0;
		for (ColumnInfo info : (primaryKeys ? fPrimaryKeyList : fColumnList)) {
			String varName = info.getName().toLowerCase();

			if (!(Objects.isNull(cnds) ? variables : cnds).containsKey(varName)) {
				if (!primaryKeys) {
					continue;
				}

				throw new IllegalArgumentException(info.getName() + " must be specified.");
			}

			if (i == 0) {
				sql.append(" WHERE ");
			} else {
				sql.append(" AND ");
			}
			sql.append(info.getName());
			if ((Objects.isNull(cnds) ? variables : cnds).get(varName) == null) {
				sql.append(" IS NULL");
			} else {
				String cndName = !Objects.isNull(cnds) ? ("@cnd@" + varName) : varName;
				sql.append(" = ").append("{{").append(cndName).append("}}");
				if (!Objects.isNull(cnds)) {
					variables.put(cndName, cnds.get(varName));
				}
			}
			i++;
		}
		return sql.toString();
	}

	public static Builder newBuilder(Connection connection) {
		return Builder.create(connection);
	}

	@Deprecated
	public static Builder newBuilder() {
		return Builder.create(null);
	}

	public static class Builder {
		private Connection fConnection;

		private Builder(Connection connection) {
			fConnection = connection;
		}

		public static Builder create(Connection connection) {
			return new Builder(connection);
		}

		private String fName;
		public Builder setName(String name) {
			fName = name;
			return this;
		}

		@Deprecated
		public Builder setConnection(Connection connection) {
			fConnection = connection;
			return this;
		}

		private ParameterHandler fParameterHandler;
		public Builder setParameterHandler(ParameterHandler parameterHandler) {
			fParameterHandler = parameterHandler;
			return this;
		}

		private ResultHandler fResultHandler;
		public Builder setResultHandler(ResultHandler resultHandler) {
			fResultHandler = resultHandler;
			return this;
		}

		public Entity build() throws SQLException {
			Objects.requireNonNull(fName);
			Objects.requireNonNull(fConnection);
			return new Entity(this);
		}
	}

	private static class ColumnInfo {
		final String fName;
		final int fPosition;

		private ColumnInfo(String name, int position) {
			fName = name;
			fPosition = position;
		}

		private String getName() {
			return fName;
		}

		private int getPosition() {
			return fPosition;
		}
	}

}
