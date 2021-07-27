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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
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

public class AdaptableList<E> implements List<E> {

	private final List<E> fList;
	private final Map<Class<?>, Class<? extends ValueAdapter<?>>> fValueAdapterMap;
	private final Map<String, Object> fEnv;

	public AdaptableList(Builder<E> builder) {
		fList = builder.fList;
		fValueAdapterMap = builder.fValueAdapterMap;
		fEnv = builder.fEnv;
	}

	@Override
	public int size() {
		return fList.size();
	}

	@Override
	public boolean isEmpty() {
		return fList.isEmpty();
	}

	@Override
	public boolean contains(Object o) {
		return fList.contains(o);
	}

	@Override
	public Iterator<E> iterator() {
		return fList.iterator();
	}

	@Override
	public Object[] toArray() {
		return fList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return fList.toArray(a);
	}

	@Override
	public boolean add(E e) {
		return fList.add(e);
	}

	@Override
	public boolean remove(Object o) {
		return fList.remove(o);
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		return fList.containsAll(c);
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		return fList.addAll(c);
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		return fList.addAll(index, c);
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		return fList.removeAll(c);
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		return fList.retainAll(c);
	}

	@Override
	public void clear() {
		fList.clear();
	}

	@Override
	public boolean equals(Object o) {
		return fList.equals(o);
	}

	@Override
	public int hashCode() {
		return fList.hashCode();
	}

	@Override
	public E get(int index) {
		return fList.get(index);
	}

	@Override
	public E set(int index, E element) {
		return fList.set(index, element);
	}

	@Override
	public void add(int index, E element) {
		fList.add(index, element);
	}

	@Override
	public E remove(int index) {
		return fList.remove(index);
	}

	@Override
	public int indexOf(Object o) {
		return fList.indexOf(o);
	}

	@Override
	public int lastIndexOf(Object o) {
		return fList.lastIndexOf(o);
	}

	@Override
	public ListIterator<E> listIterator() {
		return fList.listIterator();
	}

	@Override
	public ListIterator<E> listIterator(int index) {
		return fList.listIterator(index);
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		return fList.subList(fromIndex, toIndex);
	}

	public <ValueType> AdaptableValue<ValueType> adapt(int index, Class<ValueType> valueType) {
		HashMap<String, Object> env = new HashMap<>();
		return adapt(index, valueType, env);
	}

	@SuppressWarnings("unchecked")
	public <ValueType> AdaptableValue<ValueType> adapt(int index, Class<ValueType> valueType, HashMap<String, Object> env) {
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
				return (AdaptableValue<ValueType>) valueAdapterType.getConstructor(Map.class).newInstance(env).getAdaptableValue(get(index));
			} catch (Throwable ignore) {}
		}

		return null;
	}

	public BigDecimal getBigDecimal(int index) {
		return adapt(index, BigDecimal.class).getValue();
	}

	public BigInteger getBigInteger(int index) {
		return adapt(index, BigInteger.class).getValue();
	}

	public Boolean getBoolean(int index) {
		return adapt(index, Boolean.class).getValue();
	}

	public Byte getByte(int index) {
		return adapt(index, Byte.class).getValue();
	}

	public Character getCharacter(int index) {
		return adapt(index, Character.class).getValue();
	}

	public java.util.Date getDate(int index) {
		return adapt(index, java.util.Date.class).getValue();
	}

	public Double getDouble(int index) {
		return adapt(index, Double.class).getValue();
	}

	public Float getFloat(int index) {
		return adapt(index, Float.class).getValue();
	}

	public InputStream getInputStream(int index) {
		return adapt(index, InputStream.class).getValue();
	}

	public Integer getInteger(int index) {
		return adapt(index, Integer.class).getValue();
	}

	public Long getLong(int index) {
		return adapt(index, Long.class).getValue();
	}

	public Reader getReader(int index) {
		return adapt(index, Reader.class).getValue();
	}

	public Short getShort(int index) {
		return adapt(index, Short.class).getValue();
	}

	public String getString(int index) {
		return adapt(index, String.class).getValue();
	}

	public OffsetDateTime getOffsetDateTime(int index) {
		return adapt(index, OffsetDateTime.class).getValue();
	}

	public LocalDateTime getLocalDateTime(int index) {
		return adapt(index, LocalDateTime.class).getValue();
	}

	public LocalDate getLocalDate(int index) {
		return adapt(index, LocalDate.class).getValue();
	}

	public OffsetTime getOffsetTime(int index) {
		return adapt(index, OffsetTime.class).getValue();
	}

	public LocalTime getLocalTime(int index) {
		return adapt(index, LocalTime.class).getValue();
	}

	public static <E> Builder<E> newBuilder() {
		return new Builder<>();
	}

	public static class Builder<E> {
		private final List<E> fList = new ArrayList<>();
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

		public <ValueType> Builder<E> setValueAdapter(Class<ValueType> valueType, Class<ValueAdapter<ValueType>> valueAdapterType) {
			fValueAdapterMap.put(valueType, valueAdapterType);
			return this;
		}

		public Builder<E> addAll(Collection<? extends E> c) {
			fList.addAll(c);
			return this;
		}

		public Builder<E> add(E e) {
			fList.add(e);
			return this;
		}

		public Builder<E> setProperty(String key, Object value) {
			fEnv.put(key, value);
			return this;
		}

		public Builder<E> setEncoding(String encoding) {
			if (!Charset.isSupported(encoding)) {
				throw new IllegalArgumentException(encoding);
			}

			setProperty(ValueAdapter.ENV_ENCODING, encoding);
			return this;
		}

		public Builder<E> setEncoding(Charset encoding) {
			setProperty(ValueAdapter.ENV_ENCODING, encoding);
			return null;
		}

		public Builder<E> setZoneId(String zoneId) {
			if (!ZoneId.getAvailableZoneIds().contains(zoneId)) {
				throw new IllegalArgumentException(zoneId);
			}

			setProperty(ValueAdapter.ENV_ZONEID, zoneId);
			return this;
		}

		public Builder<E> setZoneId(ZoneId zoneId) {
			setProperty(ValueAdapter.ENV_ZONEID, zoneId);
			return this;
		}

		public Builder<E> setZoneId(TimeZone timeZone) {
			setProperty(ValueAdapter.ENV_ZONEID, timeZone.toZoneId());
			return this;
		}

		public Builder<E> setDisplayZoneId(String zoneId) {
			if (!ZoneId.getAvailableZoneIds().contains(zoneId)) {
				throw new IllegalArgumentException(zoneId);
			}

			setProperty(ValueAdapter.ENV_DISPLAYZONEID, zoneId);
			return this;
		}

		public Builder<E> setDisplayZoneId(ZoneId zoneId) {
			setProperty(ValueAdapter.ENV_DISPLAYZONEID, zoneId);
			return this;
		}

		public Builder<E> setDisplayZoneId(TimeZone timeZone) {
			setProperty(ValueAdapter.ENV_DISPLAYZONEID, timeZone.toZoneId());
			return this;
		}

		public AdaptableList<E> build() {
			return new AdaptableList<>(this);
		}
	}

}
