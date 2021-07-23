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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Map;

import jp.mintjams.tools.adapter.AbstractValueAdapter;
import jp.mintjams.tools.adapter.Adaptables;

public class BigIntegerValueAdapter extends AbstractValueAdapter<BigInteger> {

	public BigIntegerValueAdapter() {
		super();
	}

	public BigIntegerValueAdapter(Map<String, Object> env) {
		super(env);
	}

	@Override
	public BigInteger adapt(Object value) {
		if (value == null) {
			return null;
		}

		BigInteger bigIntegerValue = Adaptables.getAdapter(value, BigInteger.class);
		if (bigIntegerValue != null) {
			return bigIntegerValue;
		}

		Number numberValue = Adaptables.getAdapter(value, Number.class);
		if (numberValue != null) {
			return BigInteger.valueOf(numberValue.longValue());
		}

		String stringValue = new StringValueAdapter(fEnv).adapt(value);
		if (stringValue != null) {
			return BigInteger.valueOf(new BigDecimal(stringValue).longValue());
		}

		return null;
	}

}
