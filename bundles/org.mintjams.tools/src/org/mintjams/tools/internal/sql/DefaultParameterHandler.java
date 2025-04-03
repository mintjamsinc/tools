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

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.Array;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import org.mintjams.tools.adapter.ValueAdapter;
import org.mintjams.tools.adapter.ValueAdapters;
import org.mintjams.tools.internal.io.InputStreamValueAdapter;
import org.mintjams.tools.internal.io.ReaderValueAdapter;
import org.mintjams.tools.internal.lang.BigDecimalValueAdapter;
import org.mintjams.tools.internal.lang.BooleanValueAdapter;
import org.mintjams.tools.internal.lang.ByteValueAdapter;
import org.mintjams.tools.internal.lang.DoubleValueAdapter;
import org.mintjams.tools.internal.lang.FloatValueAdapter;
import org.mintjams.tools.internal.lang.IntegerValueAdapter;
import org.mintjams.tools.internal.lang.LongValueAdapter;
import org.mintjams.tools.internal.lang.ObjectArrayValueAdapter;
import org.mintjams.tools.internal.lang.ShortValueAdapter;
import org.mintjams.tools.internal.lang.StringValueAdapter;
import org.mintjams.tools.sql.ParameterHandler;

public class DefaultParameterHandler implements ParameterHandler {

	@Override
	public void setParameter(ParameterContext context) throws SQLException {
		if (context.getValue() == null) {
			context.getStatement().setNull(context.getIndex(), context.getType());
			return;
		}

		getHandler(getType(context)).setParameter(context);
	}

	@Override
	public Object getParameter(ParameterContext context) throws SQLException {
		return getHandler(getType(context)).getParameter(context);
	}

	private int getType(ParameterContext context) throws SQLException {
		int type = context.getType();
		if (type == Types.OTHER) {
			String typeName = context.getParameterMetaData().getParameterTypeName(context.getIndex()).toUpperCase();
			if (typeName.startsWith("TIMESTAMP")) {
				type = Types.TIMESTAMP;
			}
		}
		return type;
	}

	private Handler getHandler(int type) {
		for (Handler handler : Handler.class.getEnumConstants()) {
			if (type == handler.fType) {
				return handler;
			}
		}
		throw new IllegalArgumentException("Type:" + type + " is not a valid java.sql.Types value.");
	}

	private static Map<String, Object> createEnv(ParameterContext context) {
		Map<String, Object> env = new HashMap<>();

		String encoding = context.getOptions().get(ValueAdapter.ENV_ENCODING);
		if (encoding != null && !encoding.trim().isEmpty()) {
			env.put(ValueAdapter.ENV_ENCODING, encoding);
		}

		String zoneIdString = context.getOptions().get(ValueAdapter.ENV_ZONEID);
		if (zoneIdString != null && !zoneIdString.trim().isEmpty()) {
			env.put(ValueAdapter.ENV_ZONEID, zoneIdString);
		}

		String displayZoneIdString = context.getOptions().get(ValueAdapter.ENV_DISPLAYZONEID);
		if (displayZoneIdString != null && !displayZoneIdString.trim().isEmpty()) {
			env.put(ValueAdapter.ENV_DISPLAYZONEID, displayZoneIdString);
		}

		env.put(ValueAdapter.ENV_VALUEADAPTERS, ValueAdapters.createValueAdapterMap());

		return env;
	}

	private enum Handler {
		BIT(Types.BIT) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.BOOLEAN.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.BOOLEAN.getParameter(context);
			}
		},
		TINYINT(Types.TINYINT) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Byte value = new ByteValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setByte(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getByte(context.getIndex());
			}
		},
		SMALLINT(Types.SMALLINT) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Short value = new ShortValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setShort(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getShort(context.getIndex());
			}
		},
		INTEGER(Types.INTEGER) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Integer value = new IntegerValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setInt(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getInt(context.getIndex());
			}
		},
		BIGINT(Types.BIGINT) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Long value = new LongValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setLong(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getLong(context.getIndex());
			}
		},
		FLOAT(Types.FLOAT) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Float value = new FloatValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setFloat(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getFloat(context.getIndex());
			}
		},
		REAL(Types.REAL) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.FLOAT.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.FLOAT.getParameter(context);
			}
		},
		DOUBLE(Types.DOUBLE) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Double value = new DoubleValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setDouble(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getDouble(context.getIndex());
			}
		},
		NUMERIC(Types.NUMERIC) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.DECIMAL.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.DECIMAL.getParameter(context);
			}
		},
		DECIMAL(Types.DECIMAL) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				BigDecimal value = new BigDecimalValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setBigDecimal(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getBigDecimal(context.getIndex());
			}
		},
		CHAR(Types.CHAR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				String value = new StringValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setString(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getString(context.getIndex());
			}
		},
		VARCHAR(Types.VARCHAR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.CHAR.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.CHAR.getParameter(context);
			}
		},
		LONGVARCHAR(Types.LONGVARCHAR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Reader value = new ReaderValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.registerCloseable(value);
				context.getStatement().setCharacterStream(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				Reader value = ((CallableStatement) context.getStatement()).getCharacterStream(context.getIndex());
				if (value != null) {
					context.registerCloseable(value);
				}
				return value;
			}
		},
		DATE(Types.DATE) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				java.sql.Date value = new org.mintjams.tools.internal.sql.DateValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setDate(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getDate(context.getIndex());
			}
		},
		TIME(Types.TIME) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Time value = new TimeValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setTime(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getTime(context.getIndex());
			}
		},
		TIMESTAMP(Types.TIMESTAMP) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Timestamp value = new TimestampValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setTimestamp(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getTimestamp(context.getIndex());
			}
		},
		BINARY(Types.BINARY) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				InputStream value = new InputStreamValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.registerCloseable(value);
				context.getStatement().setBinaryStream(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getBytes(context.getIndex());
			}
		},
		VARBINARY(Types.VARBINARY) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.BINARY.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.BINARY.getParameter(context);
			}
		},
		LONGVARBINARY(Types.LONGVARBINARY) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.BINARY.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.BINARY.getParameter(context);
			}
		},
		NULL(Types.NULL) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				context.getStatement().setNull(context.getIndex(), context.getType());
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return null;
			}
		},
		OTHER(Types.OTHER) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.JAVA_OBJECT.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.JAVA_OBJECT.getParameter(context);
			}
		},
		JAVA_OBJECT(Types.JAVA_OBJECT) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Object value = context.getValue();
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setObject(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getObject(context.getIndex());
			}
		},
		DISTINCT(Types.DISTINCT) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		STRUCT(Types.STRUCT) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		ARRAY(Types.ARRAY) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Object[] value = new ObjectArrayValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				String typeName = context.getParameterMetaData().getParameterTypeName(context.getIndex());
				if (typeName.startsWith("_")) {
					typeName = typeName.substring(1);
				}
				context.getStatement().setArray(context.getIndex(), context.getStatement().getConnection().createArrayOf(typeName, value));
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				Object o = ((CallableStatement) context.getStatement()).getObject(context.getIndex());

				if (o instanceof Array) {
					try {
						return ((Array) o).getArray();
					} finally {
						try {
							((Array) o).free();
						} catch (Throwable ignore) {}
					}
				}

				return o;
			}
		},
		BLOB(Types.BLOB) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.BINARY.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				Blob value = ((CallableStatement) context.getStatement()).getBlob(context.getIndex());
				if (value == null) {
					return null;
				}
				return context.registerCloseable(value.getBinaryStream());
			}
		},
		CLOB(Types.CLOB) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.LONGVARCHAR.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				Clob value = ((CallableStatement) context.getStatement()).getClob(context.getIndex());
				if (value == null) {
					return null;
				}
				return context.registerCloseable(value.getCharacterStream());
			}
		},
		REF(Types.REF) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		DATALINK(Types.DATALINK) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		BOOLEAN(Types.BOOLEAN) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Boolean value = new BooleanValueAdapter(createEnv(context)).adapt(context.getValue());
				if (value == null) {
					context.getStatement().setNull(context.getIndex(), context.getType());
					return;
				}
				context.getStatement().setBoolean(context.getIndex(), value);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getBoolean(context.getIndex());
			}
		},

		/* JDBC 4.0 Types */

		ROWID(Types.ROWID) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		NCHAR(Types.NCHAR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.CHAR.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.CHAR.getParameter(context);
			}
		},
		NVARCHAR(Types.NVARCHAR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.CHAR.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.CHAR.getParameter(context);
			}
		},
		LONGNVARCHAR(Types.LONGNVARCHAR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.LONGVARCHAR.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.LONGVARCHAR.getParameter(context);
			}
		},
		NCLOB(Types.NCLOB) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.LONGVARCHAR.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.LONGVARCHAR.getParameter(context);
			}
		},
		SQLXML(Types.SQLXML) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},

		/* JDBC 4.2 Types */

		REF_CURSOR(Types.REF_CURSOR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return ((CallableStatement) context.getStatement()).getObject(context.getIndex());
			}
		},
		TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.TIME.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.TIME.getParameter(context);
			}
		},
		TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.TIMESTAMP.setParameter(context);
			}

			@Override
			public Object getParameter(ParameterContext context) throws SQLException {
				return Handler.TIMESTAMP.getParameter(context);
			}
		};

		private Integer fType;

		Handler(Integer type) {
			fType = type;
		}

		protected abstract void setParameter(ParameterContext context) throws SQLException;

		protected abstract Object getParameter(ParameterContext context) throws SQLException;
	}

}
