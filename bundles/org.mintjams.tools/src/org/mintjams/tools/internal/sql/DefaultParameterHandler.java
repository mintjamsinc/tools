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

		getHandler(context.getType()).setParameter(context);
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
		},
		REAL(Types.REAL) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.FLOAT.setParameter(context);
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
		},
		NUMERIC(Types.NUMERIC) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.DECIMAL.setParameter(context);
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
		},
		VARCHAR(Types.VARCHAR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.CHAR.setParameter(context);
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
		},
		VARBINARY(Types.VARBINARY) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.BINARY.setParameter(context);
			}
		},
		LONGVARBINARY(Types.LONGVARBINARY) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.BINARY.setParameter(context);
			}
		},
		NULL(Types.NULL) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				context.getStatement().setNull(context.getIndex(), context.getType());
			}
		},
		OTHER(Types.OTHER) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.JAVA_OBJECT.setParameter(context);
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
		},
		DISTINCT(Types.DISTINCT) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		STRUCT(Types.STRUCT) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
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
				context.getStatement().setObject(context.getIndex(), value);
			}
		},
		BLOB(Types.BLOB) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.BINARY.setParameter(context);
			}
		},
		CLOB(Types.CLOB) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.LONGVARCHAR.setParameter(context);
			}
		},
		REF(Types.REF) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		DATALINK(Types.DATALINK) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
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
		},

		/* JDBC 4.0 Types */

		ROWID(Types.ROWID) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		NCHAR(Types.NCHAR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.CHAR.setParameter(context);
			}
		},
		NVARCHAR(Types.NVARCHAR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.CHAR.setParameter(context);
			}
		},
		LONGNVARCHAR(Types.LONGNVARCHAR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.LONGVARCHAR.setParameter(context);
			}
		},
		NCLOB(Types.NCLOB) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.LONGVARCHAR.setParameter(context);
			}
		},
		SQLXML(Types.SQLXML) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},

		/* JDBC 4.2 Types */

		REF_CURSOR(Types.REF_CURSOR) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				throw new UnsupportedOperationException("Not yet implemented.");
			}
		},
		TIME_WITH_TIMEZONE(Types.TIME_WITH_TIMEZONE) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.TIME.setParameter(context);
			}
		},
		TIMESTAMP_WITH_TIMEZONE(Types.TIMESTAMP_WITH_TIMEZONE) {
			@Override
			public void setParameter(ParameterContext context) throws SQLException {
				Handler.TIMESTAMP.setParameter(context);
			}
		};

		private Integer fType;

		Handler(Integer type) {
			fType = type;
		}

		protected abstract void setParameter(ParameterContext context) throws SQLException;
	}

}
