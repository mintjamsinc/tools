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
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.mintjams.tools.io.InputStreamValueAdapter;
import jp.mintjams.tools.io.ReaderValueAdapter;
import jp.mintjams.tools.lang.AdaptableValue;
import jp.mintjams.tools.lang.BigDecimalValueAdapter;
import jp.mintjams.tools.lang.BigIntegerValueAdapter;
import jp.mintjams.tools.lang.BooleanValueAdapter;
import jp.mintjams.tools.lang.ByteValueAdapter;
import jp.mintjams.tools.lang.CharacterValueAdapter;
import jp.mintjams.tools.lang.DoubleValueAdapter;
import jp.mintjams.tools.lang.FloatValueAdapter;
import jp.mintjams.tools.lang.IntegerValueAdapter;
import jp.mintjams.tools.lang.LongValueAdapter;
import jp.mintjams.tools.lang.ShortValueAdapter;
import jp.mintjams.tools.lang.StringValueAdapter;
import jp.mintjams.tools.lang.ValueAdapter;
import jp.mintjams.tools.sql.TimeValueAdapter;
import jp.mintjams.tools.sql.TimestampValueAdapter;
import jp.mintjams.tools.time.LocalDateTimeValueAdapter;
import jp.mintjams.tools.time.LocalDateValueAdapter;
import jp.mintjams.tools.time.LocalTimeValueAdapter;
import jp.mintjams.tools.time.OffsetDateTimeValueAdapter;
import jp.mintjams.tools.time.OffsetTimeValueAdapter;

public class AdaptableMap<K, V> implements Map<K, V> {

	private final Map<K, V> fMap;
	private final Map<Class<?>, Class<? extends ValueAdapter<?>>> fValueAdapterMap;

	private AdaptableMap(Builder<K, V> builder) {
		fMap = builder.fMap;
		fValueAdapterMap = builder.fValueAdapterMap;
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
			try {
				return (AdaptableValue<ValueType>) valueAdapterType.getConstructor(Map.class).newInstance(env).getAdaptableValue(get(key));
			} catch (Throwable ignore) {}
		}

		return null;
	}

	public static <K, V> Builder<K, V> newBuilder() {
		return new Builder<>();
	}

	public static class Builder<K, V> {
		private final Map<K, V> fMap = new HashMap<>();
		private final Map<Class<?>, Class<? extends ValueAdapter<?>>> fValueAdapterMap = new HashMap<>();

		private Builder() {
			fValueAdapterMap.put(BigDecimal.class, BigDecimalValueAdapter.class);
			fValueAdapterMap.put(BigInteger.class, BigIntegerValueAdapter.class);
			fValueAdapterMap.put(Boolean.class, BooleanValueAdapter.class);
			fValueAdapterMap.put(Byte.class, ByteValueAdapter.class);
			fValueAdapterMap.put(Character.class, CharacterValueAdapter.class);
			fValueAdapterMap.put(java.util.Date.class, jp.mintjams.tools.lang.DateValueAdapter.class);
			fValueAdapterMap.put(Double.class, DoubleValueAdapter.class);
			fValueAdapterMap.put(Float.class, FloatValueAdapter.class);
			fValueAdapterMap.put(InputStream.class, InputStreamValueAdapter.class);
			fValueAdapterMap.put(Integer.class, IntegerValueAdapter.class);
			fValueAdapterMap.put(Long.class, LongValueAdapter.class);
			fValueAdapterMap.put(Reader.class, ReaderValueAdapter.class);
			fValueAdapterMap.put(Short.class, ShortValueAdapter.class);
			fValueAdapterMap.put(String.class, StringValueAdapter.class);
			fValueAdapterMap.put(java.sql.Date.class, jp.mintjams.tools.sql.DateValueAdapter.class);
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

		public AdaptableMap<K, V> build() {
			return new AdaptableMap<>(this);
		}
	}

}
