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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import jp.mintjams.tools.lang.AdaptableValue;
import jp.mintjams.tools.lang.BigDecimalValueAdapter;
import jp.mintjams.tools.lang.BigIntegerValueAdapter;
import jp.mintjams.tools.lang.BooleanValueAdapter;
import jp.mintjams.tools.lang.ByteValueAdapter;
import jp.mintjams.tools.lang.CharacterValueAdapter;
import jp.mintjams.tools.lang.DoubleValueAdapter;
import jp.mintjams.tools.lang.FloatValueAdapter;
import jp.mintjams.tools.lang.InputStreamValueAdapter;
import jp.mintjams.tools.lang.IntegerValueAdapter;
import jp.mintjams.tools.lang.LongValueAdapter;
import jp.mintjams.tools.lang.ReaderValueAdapter;
import jp.mintjams.tools.lang.ShortValueAdapter;
import jp.mintjams.tools.lang.StringValueAdapter;
import jp.mintjams.tools.sql.TimeValueAdapter;
import jp.mintjams.tools.sql.TimestampValueAdapter;

public class AdaptableMap<K, V> implements Map<K, V> {

	private final Map<K, V> fMap;

	public AdaptableMap() {
		this(new HashMap<K, V>());
	}

	public AdaptableMap(Map<K, V> map) {
		fMap = map;
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

	public <ValueType> AdaptableValue<ValueType> getAdaptableValue(Object key, Class<ValueType> valueType) {
		HashMap<String, Object> env = new HashMap<>();
		return getAdaptableValue(key, valueType, env);
	}

	@SuppressWarnings("unchecked")
	public <ValueType> AdaptableValue<ValueType> getAdaptableValue(Object key, Class<ValueType> valueType, HashMap<String, Object> env) {
		if (env == null) {
			env = new HashMap<>();
		}

		if (BigDecimal.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new BigDecimalValueAdapter(env).getAdaptableValue(get(key));
		}

		if (BigInteger.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new BigIntegerValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Boolean.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new BooleanValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Byte.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new ByteValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Character.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new CharacterValueAdapter(env).getAdaptableValue(get(key));
		}

		if (java.util.Date.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new jp.mintjams.tools.lang.DateValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Double.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new DoubleValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Float.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new FloatValueAdapter(env).getAdaptableValue(get(key));
		}

		if (InputStream.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new InputStreamValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Integer.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new IntegerValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Long.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new LongValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Reader.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new ReaderValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Short.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new ShortValueAdapter(env).getAdaptableValue(get(key));
		}

		if (String.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new StringValueAdapter(env).getAdaptableValue(get(key));
		}

		if (java.sql.Date.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new jp.mintjams.tools.sql.DateValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Timestamp.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new TimestampValueAdapter(env).getAdaptableValue(get(key));
		}

		if (Time.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new TimeValueAdapter(env).getAdaptableValue(get(key));
		}

		return null;
	}

}
