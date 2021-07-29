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

package jp.mintjams.tools.internal.sql;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import jp.mintjams.tools.sql.ResultHandler;

public class DefaultResultHandler implements ResultHandler {

	@Override
	public Map<String, Object> getResultAsMap(ResultContext context) throws SQLException {
		Map<String, Object> result = new HashMap<>();
		ResultSetMetaData metadata = context.getResultSetMetaData();
		for (int i = 0; i < metadata.getColumnCount(); i++) {
			int columnIndex = i + 1;
			int type = metadata.getColumnType(columnIndex);
			if (type == Types.OTHER) {
				String typeName = metadata.getColumnTypeName(columnIndex).toUpperCase();
				if (typeName.startsWith("TIMESTAMP")) {
					type = Types.TIMESTAMP;
				}
			}
			result.put(metadata.getColumnLabel(columnIndex), getHandler(type).getValue(context, columnIndex));
		}
		return result;
	}

	private Handler getHandler(int type) {
		for (Handler handler : Handler.class.getEnumConstants()) {
			if (type == handler.fType) {
				return handler;
			}
		}
		throw new IllegalArgumentException("Type:" + type + " is not a valid java.sql.Types value.");
	}

	private enum Handler {
		BIT(Types.BIT) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.BOOLEAN.getValue(context, columnIndex);
			}
		},
		TINYINT(Types.TINYINT) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Byte value = context.getResultSet().getByte(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		SMALLINT(Types.SMALLINT) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Short value = context.getResultSet().getShort(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		INTEGER(Types.INTEGER) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Integer value = context.getResultSet().getInt(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		BIGINT(Types.BIGINT) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Long value = context.getResultSet().getLong(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		FLOAT(Types.FLOAT) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Float value = context.getResultSet().getFloat(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		REAL(Types.REAL) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.FLOAT.getValue(context, columnIndex);
			}
		},
		DOUBLE(Types.DOUBLE) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Double value = context.getResultSet().getDouble(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		NUMERIC(Types.NUMERIC) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.DECIMAL.getValue(context, columnIndex);
			}
		},
		DECIMAL(Types.DECIMAL) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				BigDecimal value = context.getResultSet().getBigDecimal(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		CHAR(Types.CHAR) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				String value = context.getResultSet().getString(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		VARCHAR(Types.VARCHAR) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.CHAR.getValue(context, columnIndex);
			}
		},
		LONGVARCHAR(Types.LONGVARCHAR) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Reader value = context.getResultSet().getCharacterStream(columnIndex);
				if (value != null) {
					context.registerCloseable(value);
				}
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		DATE(Types.DATE) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				java.sql.Date value = context.getResultSet().getDate(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		TIME(Types.TIME) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Time value = context.getResultSet().getTime(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		TIMESTAMP(Types.TIMESTAMP) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Timestamp value = context.getResultSet().getTimestamp(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		BINARY(Types.BINARY) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				byte[] value = context.getResultSet().getBytes(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		VARBINARY(Types.VARBINARY) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.BINARY.getValue(context, columnIndex);
			}
		},
		LONGVARBINARY(Types.LONGVARBINARY) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				InputStream value = context.getResultSet().getBinaryStream(columnIndex);
				if (value != null) {
					context.registerCloseable(value);
				}
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		NULL(Types.NULL) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return null;
			}
		},
		OTHER(Types.OTHER) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.JAVA_OBJECT.getValue(context, columnIndex);
			}
		},
		JAVA_OBJECT(Types.JAVA_OBJECT) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Object value = context.getResultSet().getObject(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},
		DISTINCT(Types.DISTINCT) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		STRUCT(Types.STRUCT) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		ARRAY(Types.ARRAY) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		BLOB(Types.BLOB) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Blob blob = context.getResultSet().getBlob(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return context.registerCloseable(blob.getBinaryStream());
			}
		},
		CLOB(Types.CLOB) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Clob blob = context.getResultSet().getClob(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return context.registerCloseable(blob.getCharacterStream());
			}
		},
		REF(Types.REF) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		DATALINK(Types.DATALINK) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		BOOLEAN(Types.BOOLEAN) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				Boolean value = context.getResultSet().getBoolean(columnIndex);
				if (context.getResultSet().wasNull()) {
					return null;
				}

				return value;
			}
		},

		/* JDBC 4.0 Types */

		ROWID(Types.ROWID) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		NCHAR(Types.NCHAR) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.CHAR.getValue(context, columnIndex);
			}
		},
		NVARCHAR(Types.NVARCHAR) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.CHAR.getValue(context, columnIndex);
			}
		},
		LONGNVARCHAR(Types.LONGNVARCHAR) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.LONGVARCHAR.getValue(context, columnIndex);
			}
		},
		NCLOB(Types.NCLOB) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.LONGVARCHAR.getValue(context, columnIndex);
			}
		},
		SQLXML(Types.SQLXML) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},

		/* JDBC 4.2 Types */

		REF_CURSOR(Types.REF_CURSOR) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.TIME.getValue(context, columnIndex);
			}
		},
		TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE) {
			@Override
			public Object getValue(ResultContext context, int columnIndex) throws SQLException {
				return Handler.TIMESTAMP.getValue(context, columnIndex);
			}
		};

		private Integer fType;

		Handler(Integer type) {
			fType = type;
		}

		protected abstract Object getValue(ResultContext context, int columnIndex) throws SQLException;
	}

}
