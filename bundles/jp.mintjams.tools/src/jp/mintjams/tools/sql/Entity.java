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

	private ParameterHandler fParameterHandler;
	private ResultHandler fResultHandler;
	private final String fTableName;
	private final List<ColumnInfo> fColumnList = new ArrayList<>();
	private final List<ColumnInfo> fPrimaryKeyList = new ArrayList<>();
	private final Map<String, Integer> fPrimaryKeyMap = new HashMap<>();

	public Entity(Builder builder) throws SQLException {
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
		Map<String, Object> variables = toLowerCaseKey(conditions);

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM ").append(fTableName);
		StringBuffer where = new StringBuffer();
		for (ColumnInfo info : fPrimaryKeyList) {
			if (where.length() == 0) {
				where.append(" WHERE ");
			} else {
				where.append(" AND ");
			}
			where.append(info.getName()).append(" = {{").append(info.getName().toLowerCase()).append("}}");
		}
		sql.append(where);

		return Query.newBuilder()
				.setStatement(sql.toString())
				.setVariables(variables)
				.setParameterHandler(fParameterHandler)
				.setResultHandler(fResultHandler)
				.build();
	}

	public Query find(Map<String, Object> conditions) throws SQLException {
		Map<String, Object> variables = toLowerCaseKey(conditions);

		StringBuffer sql = new StringBuffer();
		sql.append("SELECT * FROM ").append(fTableName);
		StringBuffer where = new StringBuffer();
		for (ColumnInfo info : fColumnList) {
			if (!variables.containsKey(info.getName().toLowerCase())) {
				continue;
			}

			if (where.length() == 0) {
				where.append(" WHERE ");
			} else {
				where.append(" AND ");
			}
			where.append(info.getName()).append(" = {{").append(info.getName().toLowerCase()).append("}}");
		}
		sql.append(where);

		return Query.newBuilder()
				.setStatement(sql.toString())
				.setVariables(toLowerCaseKey(variables))
				.setParameterHandler(fParameterHandler)
				.setResultHandler(fResultHandler)
				.build();
	}

	public Update create(Map<String, Object> values) throws SQLException {
		Map<String, Object> variables = toLowerCaseKey(values);

		List<String> insertNameList = new ArrayList<>();
		for (ColumnInfo info : fColumnList) {
			if (variables.containsKey(info.getName().toLowerCase())) {
				insertNameList.add(info.getName());
			}
		}

		StringBuffer sql = new StringBuffer();
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

		return Update.newBuilder()
				.setStatement(sql.toString())
				.setVariables(toLowerCaseKey(variables))
				.setParameterHandler(fParameterHandler)
				.build();
	}

	public Update updateByPrimaryKey(Map<String, Object> valuesAndConditions) throws SQLException {
		Map<String, Object> variables = toLowerCaseKey(valuesAndConditions);

		List<String> updateNameList = new ArrayList<>();
		for (ColumnInfo info : fColumnList) {
			if (fPrimaryKeyMap.containsKey(info.getName().toLowerCase())) {
				continue;
			}
			if (variables.containsKey(info.getName().toLowerCase())) {
				updateNameList.add(info.getName());
			}
		}

		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE ").append(fTableName);
		for (int i = 0; i < updateNameList.size(); i++) {
			if (i == 0) {
				sql.append(" SET ");
			} else {
				sql.append(", ");
			}
			sql.append(updateNameList.get(i)).append(" = ");
			sql.append("{{").append(updateNameList.get(i).toLowerCase()).append("}}");
		}
		for (int i = 0; i < fPrimaryKeyList.size(); i++) {
			ColumnInfo info = fPrimaryKeyList.get(i);
			if (i == 0) {
				sql.append(" WHERE ");
			} else {
				sql.append(" AND ");
			}
			sql.append(info.getName()).append(" = ");
			sql.append("{{").append(info.getName().toLowerCase()).append("}}");
		}

		return Update.newBuilder()
				.setStatement(sql.toString())
				.setVariables(toLowerCaseKey(variables))
				.setParameterHandler(fParameterHandler)
				.build();
	}

	public Update update(Map<String, Object> values, Map<String, Object> conditions) throws SQLException {
		Map<String, Object> variables = toLowerCaseKey(values);
		Map<String, Object> cnds = toLowerCaseKey(conditions);

		List<String> updateNameList = new ArrayList<>();
		for (ColumnInfo info : fColumnList) {
			if (variables.containsKey(info.getName().toLowerCase())) {
				updateNameList.add(info.getName());
			}
		}

		StringBuffer sql = new StringBuffer();
		sql.append("UPDATE ").append(fTableName);
		for (int i = 0; i < updateNameList.size(); i++) {
			if (i == 0) {
				sql.append(" SET ");
			} else {
				sql.append(", ");
			}
			sql.append(updateNameList.get(i)).append(" = ");
			sql.append("{{").append(updateNameList.get(i).toLowerCase()).append("}}");
		}
		int i = 0;
		for (ColumnInfo info : fColumnList) {
			if (!cnds.containsKey(info.getName().toLowerCase())) {
				continue;
			}

			if (i == 0) {
				sql.append(" WHERE ");
			} else {
				sql.append(" AND ");
			}
			sql.append(info.getName()).append(" = ");
			sql.append("{{__condition__").append(info.getName().toLowerCase()).append("}}");
			variables.put("__condition__" + info.getName().toLowerCase(), cnds.get(info.getName().toLowerCase()));
			i++;
		}

		return Update.newBuilder()
				.setStatement(sql.toString())
				.setVariables(toLowerCaseKey(variables))
				.setParameterHandler(fParameterHandler)
				.build();
	}

	public Update deleteByPrimaryKey(Map<String, Object> conditions) throws SQLException {
		Map<String, Object> variables = toLowerCaseKey(conditions);

		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM ").append(fTableName);
		for (int i = 0; i < fPrimaryKeyList.size(); i++) {
			ColumnInfo info = fPrimaryKeyList.get(i);
			if (i == 0) {
				sql.append(" WHERE ");
			} else {
				sql.append(" AND ");
			}
			sql.append(info.getName()).append(" = ");
			sql.append("{{").append(info.getName().toLowerCase()).append("}}");
		}

		return Update.newBuilder()
				.setStatement(sql.toString())
				.setVariables(toLowerCaseKey(variables))
				.setParameterHandler(fParameterHandler)
				.build();
	}

	public Update delete(Map<String, Object> conditions) throws SQLException {
		Map<String, Object> variables = toLowerCaseKey(conditions);

		StringBuffer sql = new StringBuffer();
		sql.append("DELETE FROM ").append(fTableName);
		int i = 0;
		for (ColumnInfo info : fColumnList) {
			if (!variables.containsKey(info.getName().toLowerCase())) {
				continue;
			}

			if (i == 0) {
				sql.append(" WHERE ");
			} else {
				sql.append(" AND ");
			}
			sql.append(info.getName()).append(" = ");
			sql.append("{{").append(info.getName().toLowerCase()).append("}}");
			i++;
		}

		return Update.newBuilder()
				.setStatement(sql.toString())
				.setVariables(toLowerCaseKey(variables))
				.setParameterHandler(fParameterHandler)
				.build();
	}

	private Map<String, Object> toLowerCaseKey(Map<String, Object> map) {
		Map<String, Object> result = new HashMap<>();
		for (Map.Entry<String, Object> e : map.entrySet()) {
			result.put(e.getKey().toLowerCase(), e.getValue());
		}
		return result;
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {}

		private String fName;
		public Builder setStatement(String name) {
			fName = name;
			return this;
		}

		private final Map<String, Object> fVariables = new HashMap<>();
		public Builder setVariables(Map<String, Object> variables) {
			fVariables.putAll(variables);
			return this;
		}
		public Builder setVariable(String key, Object value) {
			fVariables.put(key, value);
			return this;
		}

		private Connection fConnection;
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
