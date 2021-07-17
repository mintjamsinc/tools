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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

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

public class AdaptableList<E> implements List<E> {

	private final List<E> fList;

	public AdaptableList() {
		this(new ArrayList<>());
	}

	public AdaptableList(List<E> list) {
		fList = list;
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

	public <ValueType> AdaptableValue<ValueType> getAdaptableValue(int index, Class<ValueType> valueType) {
		HashMap<String, Object> env = new HashMap<>();
		return getAdaptableValue(index, valueType, env);
	}

	@SuppressWarnings("unchecked")
	public <ValueType> AdaptableValue<ValueType> getAdaptableValue(int index, Class<ValueType> valueType, HashMap<String, Object> env) {
		if (env == null) {
			env = new HashMap<>();
		}

		if (BigDecimal.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new BigDecimalValueAdapter(env).getAdaptableValue(get(index));
		}

		if (BigInteger.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new BigIntegerValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Boolean.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new BooleanValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Byte.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new ByteValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Character.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new CharacterValueAdapter(env).getAdaptableValue(get(index));
		}

		if (java.util.Date.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new jp.mintjams.tools.lang.DateValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Double.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new DoubleValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Float.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new FloatValueAdapter(env).getAdaptableValue(get(index));
		}

		if (InputStream.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new InputStreamValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Integer.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new IntegerValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Long.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new LongValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Reader.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new ReaderValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Short.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new ShortValueAdapter(env).getAdaptableValue(get(index));
		}

		if (String.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new StringValueAdapter(env).getAdaptableValue(get(index));
		}

		if (java.sql.Date.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new jp.mintjams.tools.sql.DateValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Timestamp.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new TimestampValueAdapter(env).getAdaptableValue(get(index));
		}

		if (Time.class.equals(valueType)) {
			return (AdaptableValue<ValueType>) new TimeValueAdapter(env).getAdaptableValue(get(index));
		}

		return null;
	}

}
