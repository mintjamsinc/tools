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

package org.mintjams.tools.internal.sql;

import java.io.Closeable;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.JDBCType;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.mintjams.tools.adapter.Adaptable;
import org.mintjams.tools.io.Closer;
import org.mintjams.tools.sql.ParameterHandler;
import org.mintjams.tools.sql.ParameterHandler.ParameterContext;

public class SQLStatement implements Closeable, Adaptable {

	private final String fSource;
	private final Map<String, Object> fVariableMap = new HashMap<>();
	private final boolean fCallable;
	private final Connection fConnection;
	private StringBuilder fSQL = new StringBuilder();
	private List<SQLVariable> fSQLVariableList = new ArrayList<>();
	private ParameterHandler fParameterHandler;
	private final Closer fCloser = Closer.create();
	private PreparedStatement fPreparedStatement;
	private final List<ParameterContext> fOutParameterList = new ArrayList<>();

	private SQLStatement(Builder builder) {
		fSource = builder.fSource;
		fVariableMap.putAll(builder.fVariableMap);
		fCallable = builder.fCallable;
		fConnection = builder.fConnection;
		fParameterHandler = builder.fParameterHandler;
		if (fParameterHandler == null) {
			fParameterHandler = new DefaultParameterHandler();
		}
	}

	private void compile() {
		int parameterIndex = 0;
		Matcher m = Pattern.compile("\\{{2}[^{}]+?\\}{2}").matcher(fSource);
		int p = 0;
		while (m.find()) {
			fSQL.append(fSource.substring(p, m.start()));
			p = m.end();

			String variableString = m.group();
			variableString = variableString.substring(2, variableString.length() - 2);
			String[] nameAndOptions = variableString.trim().split(";");
			Object value = fVariableMap.get(nameAndOptions[0]);
			boolean isList = false;
			for (int i = 1; i < nameAndOptions.length; i++) {
				if (nameAndOptions[i].equalsIgnoreCase("list")) {
					isList = true;
					break;
				}
			}
			if (isList && value != null && (value instanceof Collection || value.getClass().isArray())) {
				Object[] values;
				if (value instanceof Collection) {
					values = ((Collection<?>) value).toArray();
				} else {
					values = (Object[]) value;
				}

				for (int i = 0; i < values.length; i++) {
					if (i > 0) {
						fSQL.append(",");
					}
					fSQL.append("?");
					fSQLVariableList.add(new SQLVariable(++parameterIndex, nameAndOptions, values[i]));
				}
			} else {
				fSQL.append("?");
				fSQLVariableList.add(new SQLVariable(++parameterIndex, nameAndOptions, value));
			}
		}
		fSQL.append(fSource.substring(p));
	}

	public PreparedStatement prepare() throws SQLException {
		if (fPreparedStatement != null) {
			throw new IllegalStateException("SQLStatement already open.");
		}

		compile();
		if (fCallable) {
			fPreparedStatement = fCloser.register(fConnection.prepareCall(fSQL.toString()));
		} else {
			fPreparedStatement = fCloser.register(fConnection.prepareStatement(fSQL.toString()));
		}
		bind(fPreparedStatement);
		return fPreparedStatement;
	}

	public PreparedStatement prepare(int resultSetType, int resultSetConcurrency) throws SQLException {
		if (fPreparedStatement != null) {
			throw new IllegalStateException("SQLStatement already open.");
		}

		compile();
		if (fCallable) {
			fPreparedStatement = fCloser.register(fConnection.prepareCall(fSQL.toString(), resultSetType, resultSetConcurrency));
		} else {
			fPreparedStatement = fCloser.register(fConnection.prepareStatement(fSQL.toString(), resultSetType, resultSetConcurrency));
		}
		bind(fPreparedStatement);
		return fPreparedStatement;
	}

	public PreparedStatement prepare(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
		if (fPreparedStatement != null) {
			throw new IllegalStateException("SQLStatement already open.");
		}

		compile();
		if (fCallable) {
			fPreparedStatement = fCloser.register(fConnection.prepareCall(fSQL.toString(), resultSetType, resultSetConcurrency, resultSetHoldability));
		} else {
			fPreparedStatement = fCloser.register(fConnection.prepareStatement(fSQL.toString(), resultSetType, resultSetConcurrency, resultSetHoldability));
		}
		bind(fPreparedStatement);
		return fPreparedStatement;
	}

	private void bind(PreparedStatement preparedStatement) throws SQLException {
		ParameterMetaData metadata = preparedStatement.getParameterMetaData();
		for (SQLVariable variable : fSQLVariableList) {
			ParameterContextImpl pc = new ParameterContextImpl(variable, preparedStatement, metadata);
			if (fCallable) {
				int mode = pc.getParameterMode();

				if (mode == ParameterMetaData.parameterModeOut || mode == ParameterMetaData.parameterModeInOut) {
					fOutParameterList.add(pc);
				}

				if (!(mode == ParameterMetaData.parameterModeIn || mode == ParameterMetaData.parameterModeInOut)) {
					continue;
				}
			}

			fParameterHandler.setParameter(pc);
		}
	}

	public boolean hasOutParameters() {
		return !fOutParameterList.isEmpty();
	}

	public List<ParameterContext> listOutParameters() {
		return Collections.unmodifiableList(fOutParameterList);
	}

	@Override
	public void close() throws IOException {
		fCloser.close();
		fPreparedStatement = null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <AdapterType> AdapterType adaptTo(Class<AdapterType> adapterType) {
		Objects.requireNonNull(adapterType);

		if (adapterType.equals(Statement.class) || adapterType.equals(PreparedStatement.class) || adapterType.equals(CallableStatement.class)) {
			return (AdapterType) fPreparedStatement;
		}

		if (adapterType.equals(ParameterHandler.class)) {
			return (AdapterType) fParameterHandler;
		}

		return null;
	}

	public static Builder newBuilder() {
		return Builder.create();
	}

	public static class Builder {
		private Builder() {}

		public static Builder create() {
			return new Builder();
		}

		private String fSource;
		public Builder setSource(String source) {
			fSource = source;
			return this;
		}

		private final Map<String, Object> fVariableMap = new HashMap<>();
		public Builder setVariables(Map<String, Object> variables) {
			fVariableMap.putAll(variables);
			return this;
		}
		public Builder setVariable(String key, Object value) {
			fVariableMap.put(key, value);
			return this;
		}

		private boolean fCallable;
		public Builder setCallable(boolean callable) {
			fCallable = callable;
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

		public SQLStatement build() {
			Objects.requireNonNull(fSource);
			Objects.requireNonNull(fConnection);
			return new SQLStatement(this);
		}
	}

	private static class SQLVariable {
		private final int fParameterIndex;
		private final String fName;
		private final Object fValue;
		private final Map<String, String> fOptionMap;

		private SQLVariable(int parameterIndex, String[] nameAndOptions, Object value) {
			fParameterIndex = parameterIndex;
			fName = nameAndOptions[0];
			if (nameAndOptions.length > 1) {
				fOptionMap = new HashMap<>();
				for (int i = 1; i < nameAndOptions.length; i++) {
					String e = nameAndOptions[i].trim();
					if (e.isEmpty()) {
						continue;
					}

					String[] kv = e.split("=");
					fOptionMap.put(kv[0].trim().toLowerCase(), (kv.length > 1) ? kv[1].trim() : null);
				}
			} else {
				fOptionMap = null;
			}
			this.fValue = value;
		}

		private int getParameterIndex() {
			return fParameterIndex;
		}

		private String getName() {
			return fName;
		}

		private Object getValue() {
			return fValue;
		}

		private Map<String, String> getOptionsAsMap() {
			if (fOptionMap == null) {
				return Collections.emptyMap();
			}
			return fOptionMap;
		}

		private Options fOptions = new Options();
		private Options getOptions() {
			return fOptions;
		}

		private class Options {
			private Integer getType() {
				if (!has("type") || isEmpty("type")) {
					return null;
				}

				try {
					return JDBCType.valueOf(fOptionMap.get("type")).getVendorTypeNumber();
				} catch (Throwable ignore) {}
				return null;
			}

			private boolean has(String name) {
				if (fOptionMap == null) {
					return false;
				}
				return fOptionMap.containsKey(name);
			}

			private String get(String name) {
				if (fOptionMap == null) {
					return null;
				}
				return fOptionMap.get(name);
			}

			private boolean isEmpty(String name) {
				String v = get(name);
				return (v == null) || v.trim().isEmpty();
			}
		}
	}

	private class ParameterContextImpl implements ParameterContext {
		private final SQLVariable fVariable;
		private final PreparedStatement fStatement;
		private final ParameterMetaData fMetadata;

		private ParameterContextImpl(SQLVariable variable, PreparedStatement preparedStatement, ParameterMetaData metadata) {
			fVariable = variable;
			fStatement = preparedStatement;
			fMetadata = metadata;
		}

		@Override
		public int getIndex() {
			return fVariable.getParameterIndex();
		}

		@Override
		public String getName() {
			return fVariable.getName();
		}

		@Override
		public Object getValue() {
			return fVariable.getValue();
		}

		@Override
		public int getType() {
			Integer sqlType = fVariable.getOptions().getType();
			if (sqlType == null) {
				try {
					sqlType = fMetadata.getParameterType(fVariable.getParameterIndex());
				} catch (Throwable ex) {}
			}
			if (sqlType == null) {
				sqlType = Types.VARCHAR;
			}
			return sqlType;
		}

		@Override
		public PreparedStatement getStatement() {
			return fStatement;
		}

		@Override
		public ParameterMetaData getParameterMetaData() {
			return fMetadata;
		}

		@Override
		public Map<String, String> getOptions() {
			return fVariable.getOptionsAsMap();
		}

		@Override
		public int getParameterMode() {
			try {
				return fMetadata.getParameterMode(fVariable.getParameterIndex());
			} catch (Throwable ex) {}
			return ParameterMetaData.parameterModeUnknown;
		}

		@Override
		public <C extends Closeable> C registerCloseable(C closeable) {
			return fCloser.register(closeable);
		}
	}

}
