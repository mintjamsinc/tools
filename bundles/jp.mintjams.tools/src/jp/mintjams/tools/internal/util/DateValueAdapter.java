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

package jp.mintjams.tools.internal.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.Map;

import jp.mintjams.tools.adapter.Adaptables;
import jp.mintjams.tools.internal.adapter.AbstractValueAdapter;
import jp.mintjams.tools.internal.lang.StringValueAdapter;

public class DateValueAdapter extends AbstractValueAdapter<java.util.Date> {

	public DateValueAdapter() {
		super();
	}

	public DateValueAdapter(Map<String, Object> env) {
		super(env);
	}

	@Override
	public java.util.Date adapt(Object value) {
		if (value == null) {
			return null;
		}

		java.sql.Timestamp sqlTimestampValue = Adaptables.getAdapter(value, java.sql.Timestamp.class);
		if (sqlTimestampValue != null) {
			return Dates.asDate(sqlTimestampValue);
		}

		java.sql.Date sqlDateValue = Adaptables.getAdapter(value, java.sql.Date.class);
		if (sqlDateValue != null) {
			return Dates.asDate(sqlDateValue);
		}

		java.sql.Time sqlTimeValue = Adaptables.getAdapter(value, java.sql.Time.class);
		if (sqlTimeValue != null) {
			return Dates.asDate(sqlTimeValue);
		}

		java.util.Date dateValue = Adaptables.getAdapter(value, java.util.Date.class);
		if (dateValue != null) {
			return dateValue;
		}

		OffsetDateTime offsetDateTimeValue = Adaptables.getAdapter(value, OffsetDateTime.class);
		if (offsetDateTimeValue != null) {
			return Dates.asDate(offsetDateTimeValue);
		}

		LocalDateTime localDateTimeValue = Adaptables.getAdapter(value, LocalDateTime.class);
		if (localDateTimeValue != null) {
			return Dates.asDate(localDateTimeValue, getZoneId());
		}

		LocalDate localDateValue = Adaptables.getAdapter(value, LocalDate.class);
		if (localDateValue != null) {
			return Dates.asDate(localDateValue);
		}

		OffsetTime offsetTimeValue = Adaptables.getAdapter(value, OffsetTime.class);
		if (offsetTimeValue != null) {
			return Dates.asDate(offsetTimeValue);
		}

		LocalTime localTimeValue = Adaptables.getAdapter(value, LocalTime.class);
		if (localTimeValue != null) {
			return Dates.asDate(localTimeValue, getZoneId());
		}

		String stringValue = new StringValueAdapter(fEnv).adapt(value);
		if (stringValue != null) {
			try {
				return Dates.asDate(OffsetDateTime.parse(stringValue));
			} catch (Exception ignore) {}

			try {
				return Dates.asDate(LocalDateTime.parse(stringValue), getZoneId());
			} catch (Exception ignore) {}

			try {
				return Dates.asDate(LocalDate.parse(stringValue));
			} catch (Exception ignore) {}

			try {
				return Dates.asDate(OffsetTime.parse(stringValue));
			} catch (Exception ignore) {}

			try {
				return Dates.asDate(LocalTime.parse(stringValue), getZoneId());
			} catch (Exception ignore) {}

			try {
				return Dates.asDate(java.sql.Timestamp.valueOf(stringValue));
			} catch (Exception ignore) {}

			try {
				return Dates.asDate(java.sql.Date.valueOf(stringValue));
			} catch (Exception ignore) {}

			try {
				return Dates.asDate(java.sql.Time.valueOf(stringValue));
			} catch (Exception ignore) {}
		}

		Number numberValue = Adaptables.getAdapter(value, Number.class);
		if (numberValue != null) {
			return Dates.asDate(numberValue.longValue());
		}

		return null;
	}

}
