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

import java.io.Closeable;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import jp.mintjams.tools.collections.AdaptableMap;
import jp.mintjams.tools.internal.sql.DefaultResultHandler;
import jp.mintjams.tools.sql.ResultHandler.ResultContext;
import jp.mintjams.tools.util.Closer;

public class Query implements Closeable {

	private ResultHandler fResultHandler;
	private PreparedStatement fPreparedStatement;
	private final Closer fCloser = Closer.newCloser();

	public Query(Builder builder) throws SQLException {
		fResultHandler = builder.fResultHandler;

		SQLStatement statement = SQLStatement.newBuilder()
				.setSource(builder.fStatement)
				.setVariables(builder.fVariables)
				.setConnection(builder.fConnection)
				.setParameterHandler(builder.fParameterHandler)
				.build();
		fCloser.add(statement);

		fPreparedStatement = statement.prepare(
				ResultSet.TYPE_FORWARD_ONLY,
				ResultSet.CONCUR_READ_ONLY,
				ResultSet.HOLD_CURSORS_OVER_COMMIT);
		fPreparedStatement.setFetchSize(1000);
	}

	@Override
	public void close() throws IOException {
		fCloser.close();
	}

	public Query closeOnCompletion() throws SQLException {
		fPreparedStatement.closeOnCompletion();
		return this;
	}

	public Query setCursorName(String name) throws SQLException {
		fPreparedStatement.setCursorName(name);
		return this;
	}

	public Query setMaxRows(int max) throws SQLException {
		fPreparedStatement.setMaxRows(max);
		return this;
	}

	public Query setFetchDirection(int direction) throws SQLException {
		fPreparedStatement.setFetchDirection(direction);
		return this;
	}

	public Query setFetchSize(int rows) throws SQLException {
		fPreparedStatement.setFetchSize(rows);
		return this;
	}

	public ResultSetIterator execute() throws SQLException {
		return new ResultSetIteratorImpl(fPreparedStatement.executeQuery());
	}

	public static Builder newBuilder() {
		return new Builder();
	}

	public static class Builder {
		private Builder() {}

		private String fStatement;
		public Builder setStatement(String statement) {
			fStatement = statement;
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

		public Query build() throws SQLException {
			Objects.requireNonNull(fStatement);
			Objects.requireNonNull(fConnection);
			return new Query(this);
		}
	}

	private class ResultSetIteratorImpl implements ResultSetIterator {
		private final ResultSet fResultSet;
		private final ResultSetMetaData fMetadata;
		private final Closer fCloser = Closer.newCloser();
		private boolean fHasNext;

		private ResultSetIteratorImpl(ResultSet resultSet) throws SQLException {
			fResultSet = resultSet;
			fCloser.add(toCloseable(fResultSet));
			fMetadata = resultSet.getMetaData();

			try {
				fHasNext = fResultSet.next();
			} catch (SQLException ex) {
				throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
			}
		}

		@Override
		public boolean hasNext() {
			return fHasNext;
		}

		@Override
		public AdaptableMap<String, Object> next() {
			if (!fHasNext) {
				throw new NoSuchElementException("No more query results available.");
			}

			ResultHandler handler = fResultHandler;
			if (handler == null) {
				handler = new DefaultResultHandler();
			}

			Map<String, Object> columns;
			try {
				columns = handler.getResultAsMap(new ResultContextImpl());
			} catch (SQLException ex) {
				throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
			}
			AdaptableMap<String, Object> result = AdaptableMap.<String, Object>newBuilder().setMap(columns).build();

			try {
				fHasNext = fResultSet.next();
			} catch (SQLException ex) {
				throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
			}

			return result;
		}

		private Closeable toCloseable(ResultSet resultSet) {
			return new Closeable() {
				@Override
				public void close() throws IOException {
					try {
						resultSet.close();
					} catch (SQLException ex) {
						throw (IOException) new IOException(ex.getMessage()).initCause(ex);
					}
				}
			};
		}

		@Override
		public void close() throws IOException {
			fCloser.close();
		}

		private class ResultContextImpl implements ResultContext {
			@Override
			public ResultSet getResultSet() {
				return fResultSet;
			}

			@Override
			public ResultSetMetaData getResultSetMetaData() {
				return fMetadata;
			}

			@Override
			public void addCloseable(Closeable closeable) {
				fCloser.add(closeable);
			}
		}
	}

}