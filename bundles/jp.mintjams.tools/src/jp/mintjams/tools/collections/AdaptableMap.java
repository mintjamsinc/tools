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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeMap;

import jp.mintjams.tools.adapter.AdaptableValue;
import jp.mintjams.tools.adapter.ValueAdapter;
import jp.mintjams.tools.adapter.ValueAdapters;

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
		return adapt(key, valueType, null);
	}

	public <ValueType> AdaptableValue<ValueType> adapt(Object key, Class<ValueType> valueType, HashMap<String, Object> env) {
		Class<? extends ValueAdapter<?>> valueAdapterType = fValueAdapterMap.get(valueType);
		if (valueAdapterType != null) {
			if (env == null) {
				env = new HashMap<>();
			}
			if (!env.containsKey(ValueAdapter.ENV_VALUEADAPTERS)) {
				env.put(ValueAdapter.ENV_VALUEADAPTERS, Collections.unmodifiableMap(fValueAdapterMap));
			}
			for (Map.Entry<String, Object> e : fEnv.entrySet()) {
				if (!env.containsKey(e.getKey())) {
					env.put(e.getKey(), e.getValue());
				}
			}
			try {
				return ValueAdapters.createValueAdapter(env, valueType).getAdaptableValue(get(key));
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

	public Object[] getObjectArray(Object key) {
		return adapt(key, Object[].class).getValue();
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
		return Builder.<K, V>create();
	}

	public static <K, V> Builder<K, V> newBuilder(Comparator<? super K> comparator) {
		return Builder.<K, V>create(comparator);
	}

	public static class Builder<K, V> {
		private final Map<K, V> fMap;
		private final Map<Class<?>, Class<? extends ValueAdapter<?>>> fValueAdapterMap;
		private final Map<String, Object> fEnv = new HashMap<>();

		private Builder(Comparator<? super K> comparator) {
			if (comparator != null) {
				fMap = new TreeMap<>(comparator);
			} else {
				fMap = new TreeMap<>();
			}
			fValueAdapterMap = ValueAdapters.createValueAdapterMap();
		}

		public static <K, V> Builder<K, V> create() {
			return new Builder<>(null);
		}

		public static <K, V> Builder<K, V> create(Comparator<? super K> comparator) {
			return new Builder<>(comparator);
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
