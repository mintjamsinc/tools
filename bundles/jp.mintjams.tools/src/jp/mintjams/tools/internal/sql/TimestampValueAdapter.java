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

package jp.mintjams.tools.internal.sql;

import java.sql.Timestamp;
import java.util.Map;

import jp.mintjams.tools.adapter.Adaptables;
import jp.mintjams.tools.internal.adapter.AbstractValueAdapter;

public class TimestampValueAdapter extends AbstractValueAdapter<java.sql.Timestamp> {

	public TimestampValueAdapter() {
		super();
	}

	public TimestampValueAdapter(Map<String, Object> env) {
		super(env);
	}

	@Override
	public java.sql.Timestamp adapt(Object value) {
		if (value == null) {
			return null;
		}

		Timestamp timestampValue = Adaptables.getAdapter(value, Timestamp.class);
		if (timestampValue != null) {
			return timestampValue;
		}

		java.util.Date dateValue = new jp.mintjams.tools.internal.util.DateValueAdapter(fEnv).adapt(value);
		if (dateValue != null) {
			return new java.sql.Timestamp(dateValue.getTime());
		}

		return null;
	}

}
