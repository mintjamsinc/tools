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

package org.mintjams.tools.sql;

import java.io.Closeable;
import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;

import org.mintjams.tools.collections.AdaptableMap;
import org.mintjams.tools.internal.sql.DefaultResultHandler;
import org.mintjams.tools.internal.sql.SQLStatement;
import org.mintjams.tools.io.Closer;
import org.mintjams.tools.sql.ParameterHandler.ParameterContext;
import org.mintjams.tools.sql.ResultHandler.ResultContext;

public class Call {

	private final String fStatement;
	private final Map<String, Object> fVariables = new HashMap<>();
	private final Connection fConnection;
	private final boolean fCloseConnection;
	private final ParameterHandler fParameterHandler;
	private ResultHandler fResultHandler;
	private Integer fOffset;
	private Integer fLimit;
	private String fCursorName;
	private Integer fFetchDirection;
	private Integer fFetchSize;

	private Call(Builder builder) {
		fStatement = builder.fStatement;
		fVariables.putAll(builder.fVariables);
		fConnection = builder.fConnection;
		fCloseConnection = builder.fCloseConnection;
		fParameterHandler = builder.fParameterHandler;
		fResultHandler = builder.fResultHandler;
	}

	private SQLStatement prepare() throws SQLException {
		return SQLStatement.newBuilder()
				.setSource(fStatement)
				.setVariables(fVariables)
				.setCallable(true)
				.setConnection(fConnection)
				.setParameterHandler(fParameterHandler)
				.build();
	}

	public Call setOffset(int offset) throws SQLException {
		fOffset = offset;
		return this;
	}

	public Call setLimit(int limit) throws SQLException {
		fLimit = limit;
		return this;
	}

	public Call setCursorName(String name) throws SQLException {
		fCursorName = name;
		return this;
	}

	public Call setFetchDirection(int direction) throws SQLException {
		fFetchDirection = direction;
		return this;
	}

	public Call setFetchSize(int rows) throws SQLException {
		fFetchSize = rows;
		return this;
	}

	public Result execute() throws SQLException {
		SQLStatement stmt = null;
		boolean isResultSet;
		try {
			stmt = prepare();

			PreparedStatement p = stmt.prepare(
					java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY,
					java.sql.ResultSet.HOLD_CURSORS_OVER_COMMIT);
			if (fCursorName != null) {
				p.setCursorName(fCursorName);
			}
			if (fFetchDirection != null) {
				p.setFetchDirection(fFetchDirection);
			}
			p.setFetchSize((fFetchSize != null) ? fFetchSize : 1000);

			isResultSet = p.execute();
		} catch (Throwable ex) {
			try {
				stmt.close();
			} catch (Throwable ignore) {}
			if (fCloseConnection) {
				try {
					fConnection.close();
				} catch (Throwable ignore) {}
			}

			if (ex instanceof SQLException) {
				throw ex;
			}
			throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
		}
		return new ResultImpl(isResultSet, stmt);
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

		@Deprecated
		public Builder setConnection(Connection connection) {
			fConnection = connection;
			return this;
		}

		private boolean fCloseConnection;
		public Builder setCloseConnection(boolean closeConnection) {
			fCloseConnection = closeConnection;
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

		public Call build() throws SQLException {
			Objects.requireNonNull(fStatement);
			Objects.requireNonNull(fConnection);
			return new Call(this);
		}
	}

	public interface Result extends Iterable<ResultSet>, Closeable {
		AdaptableMap<String, Object> getOutParameters();
	}

	public interface ResultSet extends Iterable<AdaptableMap<String, Object>>, Closeable {
		int getRow();

		void skip(int skipNum);
	}

	private class ResultImpl implements Result {
		private final SQLStatement fSQLStatement;
		private boolean fIsResultSet;
		private boolean fNoMoreResults;
		private final Closer fCloser = Closer.create();

		private Iterator<ResultSet> fIterator = new Iterator<ResultSet>() {
			@Override
			public boolean hasNext() {
				return !fNoMoreResults;
			}

			@Override
			public ResultSet next() {
				if (fNoMoreResults) {
					throw new NoSuchElementException("No more query results available.");
				}

				try {
					return fCloser.register(
							new ResultSetImpl(fSQLStatement.adaptTo(Statement.class).getResultSet(), new Closeable() {
								@Override
								public void close() throws IOException {
									try {
										nextResult(false);
									} catch (SQLException ex) {
										throw (IOException) new IOException(ex.getMessage()).initCause(ex);
									}
								}
							}));
				} catch (SQLException ex) {
					throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
				}
			}
		};

		private ResultImpl(boolean isResultSet, SQLStatement stmt) throws SQLException {
			if (fCloseConnection) {
				fCloser.register(fConnection);
			}
			fSQLStatement = fCloser.register(stmt);
			fIsResultSet = isResultSet;
			nextResult(true);
		}

		private void nextResult(boolean skipMoreResults) throws SQLException {
			while (!fNoMoreResults) {
				if (!skipMoreResults) {
					fIsResultSet = fSQLStatement.adaptTo(Statement.class).getMoreResults();
				}

				if (fIsResultSet) {
					break;
				} else {
					if (fSQLStatement.adaptTo(CallableStatement.class).getUpdateCount() == -1) {
						fNoMoreResults = true;
						break;
					}
				}

				skipMoreResults = false;
			}
		}

		private Map<String, Object> fOutParameters;
		@Override
		public AdaptableMap<String, Object> getOutParameters() {
			if (fOutParameters == null) {
				fOutParameters = new HashMap<>();
				ParameterHandler handler = fSQLStatement.adaptTo(ParameterHandler.class);
				for (ParameterContext context : fSQLStatement.listOutParameters()) {
					try {
						Object value = handler.getParameter(context);
						if (value instanceof java.sql.ResultSet) {
							value = fCloser.register(new ResultSetImpl((java.sql.ResultSet) value));
						}
						fOutParameters.put(context.getName(), value);
					} catch (SQLException ex) {
						throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
					}
				}
			}
			return AdaptableMap.<String, Object>newBuilder(String.CASE_INSENSITIVE_ORDER).putAll(fOutParameters).build();
		}

		@Override
		public Iterator<ResultSet> iterator() {
			return fIterator;
		}

		@Override
		public void close() throws IOException {
			fCloser.close();
		}
	}

	private class ResultSetImpl implements ResultSet {
		private final java.sql.ResultSet fResultSet;
		private final ResultSetMetaData fMetadata;
		private final Closer fCloser = Closer.create();
		private boolean fHasNext;
		private int fRow = 0;

		private ResultContext fResultContext = new ResultContext() {
			@Override
			public java.sql.ResultSet getResultSet() {
				return fResultSet;
			}

			@Override
			public ResultSetMetaData getResultSetMetaData() {
				return fMetadata;
			}

			@Override
			public <C extends Closeable> C registerCloseable(C closeable) {
				return fCloser.register(closeable);
			}
		};

		private Iterator<AdaptableMap<String, Object>> fIterator = new Iterator<AdaptableMap<String, Object>>() {
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
					columns = handler.getResultAsMap(fResultContext);
				} catch (SQLException ex) {
					throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
				}
				AdaptableMap<String, Object> result = AdaptableMap.<String, Object>newBuilder(String.CASE_INSENSITIVE_ORDER).putAll(columns).build();

				try {
					fHasNext = fResultSet.next();
				} catch (SQLException ex) {
					throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
				}

				fRow++;
				if (fHasNext && (fLimit != null) && (fRow >= fLimit)) {
					fHasNext = false;
				}

				return result;
			}
		};

		private ResultSetImpl(java.sql.ResultSet rs) throws SQLException {
			this(rs, null);
		}

		private ResultSetImpl(java.sql.ResultSet rs, Closeable next) throws SQLException {
			if (next != null) {
				fCloser.register(next);
			}
			fResultSet = fCloser.register(rs);
			fMetadata = fResultSet.getMetaData();

			try {
				int offset = (fOffset == null) ? 0 : fOffset;
				for (int i = -1; i < offset; i++) {
					fHasNext = fResultSet.next();
					if (!fHasNext) {
						break;
					}
				}
			} catch (SQLException ex) {
				throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
			}
		}

		@Override
		public Iterator<AdaptableMap<String, Object>> iterator() {
			return fIterator;
		}

		@Override
		public int getRow() {
			return fRow;
		}

		@Override
		public void skip(int skipNum) {
			try {
				for (int i = 0; i < skipNum; i++) {
					fHasNext = fResultSet.next();
					if (!fHasNext) {
						break;
					}

					fRow++;
					if ((fLimit != null) && (fRow >= fLimit)) {
						fHasNext = false;
						break;
					}
				}
			} catch (SQLException ex) {
				throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
			}
		}

		@Override
		public void close() throws IOException {
			fCloser.close();
		}
	}

}
