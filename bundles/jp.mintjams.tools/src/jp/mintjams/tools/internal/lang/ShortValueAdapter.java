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

package jp.mintjams.tools.internal.lang;

import java.lang.reflect.ParameterizedType;
import java.math.BigDecimal;
import java.util.Map;

import jp.mintjams.tools.adapter.Adaptables;
import jp.mintjams.tools.adapter.UnadaptableValueException;
import jp.mintjams.tools.adapter.ValueAdapters;
import jp.mintjams.tools.adapter.AbstractValueAdapter;

public class ShortValueAdapter extends AbstractValueAdapter<Short> {

	public ShortValueAdapter() {
		super();
	}

	public ShortValueAdapter(Map<String, Object> env) {
		super(env);
	}

	@Override
	public Short adapt(Object value) {
		if (value == null) {
			return null;
		}

		Short shortValue = Adaptables.getAdapter(value, Short.class);
		if (shortValue != null) {
			return shortValue;
		}

		Number numberValue = Adaptables.getAdapter(value, Number.class);
		if (numberValue != null) {
			return numberValue.shortValue();
		}

		try {
			String stringValue = ValueAdapters.createValueAdapter(fEnv, String.class).adapt(value);
			return new BigDecimal(stringValue).shortValue();
		} catch (UnadaptableValueException ignore) {}

		throw new UnadaptableValueException("Value cannot adapt to type \""
				+ ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName()
				+ "\"");
	}

}
