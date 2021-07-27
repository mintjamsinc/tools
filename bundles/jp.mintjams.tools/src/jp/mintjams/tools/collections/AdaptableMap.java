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

package jp.mintjams.tools.collections;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import jp.mintjams.tools.adapter.AdaptableValue;
import jp.mintjams.tools.adapter.ValueAdapter;
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
import jp.mintjams.tools.internal.sql.TimeValueAdapter;
import jp.mintjams.tools.internal.sql.TimestampValueAdapter;
import jp.mintjams.tools.internal.time.LocalDateTimeValueAdapter;
import jp.mintjams.tools.internal.time.LocalDateValueAdapter;
import jp.mintjams.tools.internal.time.LocalTimeValueAdapter;
import jp.mintjams.tools.internal.time.OffsetDateTimeValueAdapter;
import jp.mintjams.tools.internal.time.OffsetTimeValueAdapter;

public class AdaptableMap<K, V> implements Map<K, V> {

	private final Map<K, V> fMap;
	private final Map<Class<?>, Class<? extends ValueAdapter<?>>> fValueAdapterMap;
	private final Map<String, Object> fEnv;

	private AdaptableMap(Builder<K, V> builder) {
		fMap = builder.fMap;
		fValueAdapterMap = builder.fValueAdapterMap;
		fEnv = builder.fEnv;
	}

	@Override
	public int size() {
		return fMap.size();
	}

	@Override
	public boolean isEmpty() {
		return fMap.isEmpty();
	}

	@Override
	public boolean containsKey(Object key) {
		return fMap.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value) {
		return fMap.containsValue(value);
	}

	@Override
	public V get(Object key) {
		return fMap.get(key);
	}

	@Override
	public V put(K key, V value) {
		return fMap.put(key, value);
	}

	@Override
	public V remove(Object key) {
		return fMap.remove(key);
	}

	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		fMap.putAll(m);
	}

	@Override
	public void clear() {
		fMap.clear();
	}

	@Override
	public Set<K> keySet() {
		return fMap.keySet();
	}

	@Override
	public Collection<V> values() {
		return fMap.values();
	}

	@Override
	public Set<Entry<K, V>> entrySet() {
		return fMap.entrySet();
	}

	public <ValueType> AdaptableValue<ValueType> adapt(Object key, Class<ValueType> valueType) {
		HashMap<String, Object> env = new HashMap<>();
		return adapt(key, valueType, env);
	}

	@SuppressWarnings("unchecked")
	public <ValueType> AdaptableValue<ValueType> adapt(Object key, Class<ValueType> valueType, HashMap<String, Object> env) {
		Class<? extends ValueAdapter<?>> valueAdapterType = fValueAdapterMap.get(valueType);
		if (valueAdapterType != null) {
			if (env == null) {
				env = new HashMap<>();
			}
			for (Map.Entry<String, Object> e : fEnv.entrySet()) {
				if (!env.containsKey(e.getKey())) {
					env.put(e.getKey(), e.getValue());
				}
			}
			try {
				return (AdaptableValue<ValueType>) valueAdapterType.getConstructor(Map.class).newInstance(env).getAdaptableValue(get(key));
			} catch (Throwable ignore) {}
		}

		return null;
	}

	public BigDecimal getBigDecimal(Object key) {
		return adapt(key, BigDecimal.class).getValue();
	}

	public BigInteger getBigInteger(Object key) {
		return adapt(key, BigInteger.class).getValue();
	}

	public Boolean getBoolean(Object key) {
		return adapt(key, Boolean.class).getValue();
	}

	public Byte getByte(Object key) {
		return adapt(key, Byte.class).getValue();
	}

	public Character getCharacter(Object key) {
		return adapt(key, Character.class).getValue();
	}

	public java.util.Date getDate(Object key) {
		return adapt(key, java.util.Date.class).getValue();
	}

	public Double getDouble(Object key) {
		return adapt(key, Double.class).getValue();
	}

	public Float getFloat(Object key) {
		return adapt(key, Float.class).getValue();
	}

	public InputStream getInputStream(Object key) {
		return adapt(key, InputStream.class).getValue();
	}

	public Integer getInteger(Object key) {
		return adapt(key, Integer.class).getValue();
	}

	public Long getLong(Object key) {
		return adapt(key, Long.class).getValue();
	}

	public Reader getReader(Object key) {
		return adapt(key, Reader.class).getValue();
	}

	public Short getShort(Object key) {
		return adapt(key, Short.class).getValue();
	}

	public String getString(Object key) {
		return adapt(key, String.class).getValue();
	}

	public OffsetDateTime getOffsetDateTime(Object key) {
		return adapt(key, OffsetDateTime.class).getValue();
	}

	public LocalDateTime getLocalDateTime(Object key) {
		return adapt(key, LocalDateTime.class).getValue();
	}

	public LocalDate getLocalDate(Object key) {
		return adapt(key, LocalDate.class).getValue();
	}

	public OffsetTime getOffsetTime(Object key) {
		return adapt(key, OffsetTime.class).getValue();
	}

	public LocalTime getLocalTime(Object key) {
		return adapt(key, LocalTime.class).getValue();
	}

	public static <K, V> Builder<K, V> newBuilder() {
		return new Builder<>();
	}

	public static class Builder<K, V> {
		private final Map<K, V> fMap = new HashMap<>();
		private final Map<Class<?>, Class<? extends ValueAdapter<?>>> fValueAdapterMap = new HashMap<>();
		private final Map<String, Object> fEnv = new HashMap<>();

		private Builder() {
			fValueAdapterMap.put(BigDecimal.class, BigDecimalValueAdapter.class);
			fValueAdapterMap.put(BigInteger.class, BigIntegerValueAdapter.class);
			fValueAdapterMap.put(Boolean.class, BooleanValueAdapter.class);
			fValueAdapterMap.put(Byte.class, ByteValueAdapter.class);
			fValueAdapterMap.put(Character.class, CharacterValueAdapter.class);
			fValueAdapterMap.put(java.util.Date.class, jp.mintjams.tools.internal.util.DateValueAdapter.class);
			fValueAdapterMap.put(Double.class, DoubleValueAdapter.class);
			fValueAdapterMap.put(Float.class, FloatValueAdapter.class);
			fValueAdapterMap.put(InputStream.class, InputStreamValueAdapter.class);
			fValueAdapterMap.put(Integer.class, IntegerValueAdapter.class);
			fValueAdapterMap.put(Long.class, LongValueAdapter.class);
			fValueAdapterMap.put(Reader.class, ReaderValueAdapter.class);
			fValueAdapterMap.put(Short.class, ShortValueAdapter.class);
			fValueAdapterMap.put(String.class, StringValueAdapter.class);
			fValueAdapterMap.put(java.sql.Date.class, jp.mintjams.tools.internal.sql.DateValueAdapter.class);
			fValueAdapterMap.put(Timestamp.class, TimestampValueAdapter.class);
			fValueAdapterMap.put(Time.class, TimeValueAdapter.class);
			fValueAdapterMap.put(OffsetDateTime.class, OffsetDateTimeValueAdapter.class);
			fValueAdapterMap.put(LocalDateTime.class, LocalDateTimeValueAdapter.class);
			fValueAdapterMap.put(LocalDate.class, LocalDateValueAdapter.class);
			fValueAdapterMap.put(OffsetTime.class, OffsetTimeValueAdapter.class);
			fValueAdapterMap.put(LocalTime.class, LocalTimeValueAdapter.class);
		}

		public <ValueType> Builder<K, V> setValueAdapter(Class<ValueType> valueType, Class<ValueAdapter<ValueType>> valueAdapterType) {
			fValueAdapterMap.put(valueType, valueAdapterType);
			return this;
		}

		public Builder<K, V> putAll(Map<? extends K, ? extends V> m) {
			fMap.putAll(m);
			return this;
		}

		public Builder<K, V> put(K key, V value) {
			fMap.put(key, value);
			return this;
		}

		public Builder<K, V> setProperty(String key, Object value) {
			fEnv.put(key, value);
			return this;
		}

		public Builder<K, V> setEncoding(String encoding) {
			if (!Charset.isSupported(encoding)) {
				throw new IllegalArgumentException(encoding);
			}

			setProperty(ValueAdapter.ENV_ENCODING, encoding);
			return this;
		}

		public Builder<K, V> setEncoding(Charset encoding) {
			setProperty(ValueAdapter.ENV_ENCODING, encoding);
			return null;
		}

		public Builder<K, V> setZoneId(String zoneId) {
			if (!ZoneId.getAvailableZoneIds().contains(zoneId)) {
				throw new IllegalArgumentException(zoneId);
			}

			setProperty(ValueAdapter.ENV_ZONEID, zoneId);
			return this;
		}

		public Builder<K, V> setZoneId(ZoneId zoneId) {
			setProperty(ValueAdapter.ENV_ZONEID, zoneId);
			return this;
		}

		public Builder<K, V> setZoneId(TimeZone timeZone) {
			setProperty(ValueAdapter.ENV_ZONEID, timeZone.toZoneId());
			return this;
		}

		public Builder<K, V> setDisplayZoneId(String zoneId) {
			if (!ZoneId.getAvailableZoneIds().contains(zoneId)) {
				throw new IllegalArgumentException(zoneId);
			}

			setProperty(ValueAdapter.ENV_DISPLAYZONEID, zoneId);
			return this;
		}

		public Builder<K, V> setDisplayZoneId(ZoneId zoneId) {
			setProperty(ValueAdapter.ENV_DISPLAYZONEID, zoneId);
			return this;
		}

		public Builder<K, V> setDisplayZoneId(TimeZone timeZone) {
			setProperty(ValueAdapter.ENV_DISPLAYZONEID, timeZone.toZoneId());
			return this;
		}

		public AdaptableMap<K, V> build() {
			return new AdaptableMap<>(this);
		}
	}

}
