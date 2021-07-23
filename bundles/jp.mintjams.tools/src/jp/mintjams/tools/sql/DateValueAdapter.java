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

package jp.mintjams.tools.sql;

import java.util.Map;

import jp.mintjams.tools.lang.AbstractValueAdapter;
import jp.mintjams.tools.lang.Adaptables;

public class DateValueAdapter extends AbstractValueAdapter<java.sql.Date> {

	public DateValueAdapter() {
		super();
	}

	public DateValueAdapter(Map<String, Object> env) {
		super(env);
	}

	@Override
	public java.sql.Date adapt(Object value) {
		if (value == null) {
			return null;
		}

		java.sql.Date sqlDateValue = Adaptables.getAdapter(value, java.sql.Date.class);
		if (sqlDateValue != null) {
			return sqlDateValue;
		}

		java.util.Date dateValue = new jp.mintjams.tools.lang.DateValueAdapter(fEnv).adapt(value);
		if (dateValue != null) {
			return new java.sql.Date(dateValue.getTime());
		}

		return null;
	}

}
