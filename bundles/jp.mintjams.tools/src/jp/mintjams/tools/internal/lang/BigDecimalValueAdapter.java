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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.util.Map;

import jp.mintjams.tools.adapter.Adaptables;
import jp.mintjams.tools.internal.adapter.AbstractValueAdapter;
import jp.mintjams.tools.internal.util.Dates;

public class BigDecimalValueAdapter extends AbstractValueAdapter<BigDecimal> {

	public BigDecimalValueAdapter() {
		super();
	}

	public BigDecimalValueAdapter(Map<String, Object> env) {
		super(env);
	}

	@Override
	public BigDecimal adapt(Object value) {
		if (value == null) {
			return null;
		}

		BigDecimal bigDecimalValue = Adaptables.getAdapter(value, BigDecimal.class);
		if (bigDecimalValue != null) {
			return bigDecimalValue;
		}

		Number numberValue = Adaptables.getAdapter(value, Number.class);
		if (numberValue != null) {
			return new BigDecimal("" + numberValue.doubleValue());
		}

		java.util.Date dateValue = Adaptables.getAdapter(value, java.util.Date.class);
		if (dateValue != null) {
			return BigDecimal.valueOf(dateValue.getTime());
		}

		if (value instanceof Temporal) {
			return BigDecimal.valueOf(asDate((Temporal) value).getTime());
		}

		String stringValue = new StringValueAdapter(fEnv).adapt(value);
		if (stringValue != null) {
			return new BigDecimal(stringValue);
		}

		return null;
	}

	private java.util.Date asDate(Temporal value) {
		if (value instanceof OffsetDateTime) {
			return Dates.asDate((OffsetDateTime) value);
		}

		if (value instanceof LocalDateTime) {
			return Dates.asDate((LocalDateTime) value, getZoneId());
		}

		if (value instanceof LocalDate) {
			return Dates.asDate((LocalDate) value);
		}

		if (value instanceof OffsetTime) {
			return Dates.asDate((OffsetTime) value);
		}

		if (value instanceof LocalTime) {
			return Dates.asDate((LocalTime) value, getZoneId());
		}

		throw new IllegalArgumentException(value.getClass().getName());
	}

}
