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

package jp.mintjams.tools.adapter;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import jp.mintjams.tools.internal.io.InputStreamValueAdapter;
import jp.mintjams.tools.internal.io.ReaderValueAdapter;
import jp.mintjams.tools.internal.lang.BigDecimalValueAdapter;
import jp.mintjams.tools.internal.lang.BigIntegerValueAdapter;
import jp.mintjams.tools.internal.lang.BooleanValueAdapter;
import jp.mintjams.tools.internal.lang.ByteValueAdapter;
import jp.mintjams.tools.internal.lang.CharacterValueAdapter;
import jp.mintjams.tools.internal.lang.DoubleValueAdapter;
import jp.mintjams.tools.internal.lang.FloatValueAdapter;
import jp.mintjams.tools.internal.lang.IntegerValueAdapter;
import jp.mintjams.tools.internal.lang.LongValueAdapter;
import jp.mintjams.tools.internal.lang.ShortValueAdapter;
import jp.mintjams.tools.internal.lang.StringValueAdapter;
import jp.mintjams.tools.internal.net.URIValueAdapter;
import jp.mintjams.tools.internal.net.URLValueAdapter;
import jp.mintjams.tools.internal.sql.TimeValueAdapter;
import jp.mintjams.tools.internal.sql.TimestampValueAdapter;
import jp.mintjams.tools.internal.time.LocalDateTimeValueAdapter;
import jp.mintjams.tools.internal.time.LocalDateValueAdapter;
import jp.mintjams.tools.internal.time.LocalTimeValueAdapter;
import jp.mintjams.tools.internal.time.OffsetDateTimeValueAdapter;
import jp.mintjams.tools.internal.time.OffsetTimeValueAdapter;

public class ValueAdapters {

	private ValueAdapters() {}

	public static Map<Class<?>, Class<? extends ValueAdapter<?>>> createValueAdapterMap() {
		Map<Class<?>, Class<? extends ValueAdapter<?>>> m = new HashMap<>();
		m.put(BigDecimal.class, BigDecimalValueAdapter.class);
		m.put(BigInteger.class, BigIntegerValueAdapter.class);
		m.put(Boolean.class, BooleanValueAdapter.class);
		m.put(Byte.class, ByteValueAdapter.class);
		m.put(Character.class, CharacterValueAdapter.class);
		m.put(java.util.Calendar.class, jp.mintjams.tools.internal.util.CalendarValueAdapter.class);
		m.put(java.util.Date.class, jp.mintjams.tools.internal.util.DateValueAdapter.class);
		m.put(Double.class, DoubleValueAdapter.class);
		m.put(Float.class, FloatValueAdapter.class);
		m.put(InputStream.class, InputStreamValueAdapter.class);
		m.put(Integer.class, IntegerValueAdapter.class);
		m.put(Long.class, LongValueAdapter.class);
		m.put(Reader.class, ReaderValueAdapter.class);
		m.put(Short.class, ShortValueAdapter.class);
		m.put(String.class, StringValueAdapter.class);
		m.put(java.sql.Date.class, jp.mintjams.tools.internal.sql.DateValueAdapter.class);
		m.put(Timestamp.class, TimestampValueAdapter.class);
		m.put(Time.class, TimeValueAdapter.class);
		m.put(OffsetDateTime.class, OffsetDateTimeValueAdapter.class);
		m.put(LocalDateTime.class, LocalDateTimeValueAdapter.class);
		m.put(LocalDate.class, LocalDateValueAdapter.class);
		m.put(OffsetTime.class, OffsetTimeValueAdapter.class);
		m.put(LocalTime.class, LocalTimeValueAdapter.class);
		m.put(URI.class, URIValueAdapter.class);
		m.put(URL.class, URLValueAdapter.class);
		return m;
	}

	@SuppressWarnings("unchecked")
	public static <ValueType> ValueAdapter<ValueType> createValueAdapter(Map<String, Object> env, Class<ValueType> valueType) {
		Map<Class<?>, Class<? extends ValueAdapter<?>>> valueAdapters = (Map<Class<?>, Class<? extends ValueAdapter<?>>>) env.get(ValueAdapter.ENV_VALUEADAPTERS);
		Class<? extends ValueAdapter<?>> valueAdapterType = valueAdapters.get(valueType);
		try {
			return (ValueAdapter<ValueType>) valueAdapterType.getConstructor(Map.class).newInstance(env);
		} catch (Throwable ignore) {}
		return null;
	}

	public static String getEncoding(Map<String, Object> env) {
		String encoding = null;
		Object o = env.get(ValueAdapter.ENV_ENCODING);
		if (o instanceof Charset) {
			encoding = ((Charset) o).name();
		} else if (o instanceof String) {
			encoding = (String) o;
		}
		if (encoding == null) {
			return Charset.defaultCharset().name();
		}
		return encoding;
	}

	public static ZoneId getZoneId(Map<String, Object> env) {
		ZoneId zoneId = null;
		Object o = env.get(ValueAdapter.ENV_ZONEID);
		if (o instanceof ZoneId) {
			zoneId = (ZoneId) o;
		} else if (o instanceof String) {
			zoneId = ZoneId.of((String) o);
		}
		if (zoneId == null) {
			return ZoneId.systemDefault();
		}
		return zoneId;
	}

	public static ZoneId getDisplayZoneId(Map<String, Object> env) {
		ZoneId zoneId = null;
		Object o = env.get(ValueAdapter.ENV_DISPLAYZONEID);
		if (o instanceof ZoneId) {
			zoneId = (ZoneId) o;
		} else if (o instanceof String) {
			zoneId = ZoneId.of((String) o);
		}
		if (zoneId == null) {
			return ZoneId.systemDefault();
		}
		return zoneId;
	}

}
