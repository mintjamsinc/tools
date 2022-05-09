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

package jp.mintjams.tools.internal.time;

import java.time.LocalDate;
import java.util.Map;

import jp.mintjams.tools.adapter.Adaptables;
import jp.mintjams.tools.adapter.ValueAdapters;
import jp.mintjams.tools.adapter.AbstractValueAdapter;

public class LocalDateValueAdapter extends AbstractValueAdapter<LocalDate> {

	public LocalDateValueAdapter() {
		super();
	}

	public LocalDateValueAdapter(Map<String, Object> env) {
		super(env);
	}

	@Override
	public LocalDate adapt(Object value) {
		if (value == null) {
			return null;
		}

		LocalDate localDateValue = Adaptables.getAdapter(value, LocalDate.class);
		if (localDateValue != null) {
			return localDateValue;
		}

		java.util.Date dateValue = ValueAdapters.createValueAdapter(fEnv, java.util.Date.class).adapt(value);
		if (dateValue != null) {
			return dateValue.toInstant().atZone(getZoneId()).toLocalDate();
		}

		return null;
	}

}
