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
import java.time.ZonedDateTime;
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
			return from(sqlTimestampValue);
		}

		java.sql.Date sqlDateValue = Adaptables.getAdapter(value, java.sql.Date.class);
		if (sqlDateValue != null) {
			return from(sqlDateValue);
		}

		java.sql.Time sqlTimeValue = Adaptables.getAdapter(value, java.sql.Time.class);
		if (sqlTimeValue != null) {
			return from(sqlTimeValue);
		}

		java.util.Date dateValue = Adaptables.getAdapter(value, java.util.Date.class);
		if (dateValue != null) {
			return dateValue;
		}

		OffsetDateTime offsetDateTimeValue = Adaptables.getAdapter(value, OffsetDateTime.class);
		if (offsetDateTimeValue != null) {
			return from(offsetDateTimeValue);
		}

		LocalDateTime localDateTimeValue = Adaptables.getAdapter(value, LocalDateTime.class);
		if (localDateTimeValue != null) {
			return from(localDateTimeValue);
		}

		LocalDate localDateValue = Adaptables.getAdapter(value, LocalDate.class);
		if (localDateValue != null) {
			return from(localDateValue);
		}

		OffsetTime offsetTimeValue = Adaptables.getAdapter(value, OffsetTime.class);
		if (offsetTimeValue != null) {
			return from(offsetTimeValue);
		}

		LocalTime localTimeValue = Adaptables.getAdapter(value, LocalTime.class);
		if (localTimeValue != null) {
			return from(localTimeValue);
		}

		String stringValue = new StringValueAdapter(fEnv).adapt(value);
		if (stringValue != null) {
			try {
				return from(OffsetDateTime.parse(stringValue));
			} catch (Exception ignore) {}

			try {
				return from(LocalDateTime.parse(stringValue));
			} catch (Exception ignore) {}

			try {
				return from(LocalDate.parse(stringValue));
			} catch (Exception ignore) {}

			try {
				return from(OffsetTime.parse(stringValue));
			} catch (Exception ignore) {}

			try {
				return from(LocalTime.parse(stringValue));
			} catch (Exception ignore) {}

			try {
				return from(java.sql.Timestamp.valueOf(stringValue));
			} catch (Exception ignore) {}

			try {
				return from(java.sql.Date.valueOf(stringValue));
			} catch (Exception ignore) {}

			try {
				return from(java.sql.Time.valueOf(stringValue));
			} catch (Exception ignore) {}
		}

		return null;
	}

	private java.util.Date from(java.sql.Timestamp timestamp) {
		return new java.util.Date(timestamp.getTime());
	}

	private java.util.Date from(java.sql.Date date) {
		return new java.util.Date(date.getTime());
	}

	private java.util.Date from(java.sql.Time time) {
		return new java.util.Date(time.getTime());
	}

	private java.util.Date from(OffsetDateTime offsetDateTime) {
		return java.util.Date.from(offsetDateTime.toInstant());
	}

	private java.util.Date from(LocalDateTime localDateTime) {
		return java.util.Date.from(ZonedDateTime.of(localDateTime, getZoneId()).toInstant());
	}

	private java.util.Date from(LocalDate localDate) {
		return new java.util.Date(localDate.toEpochDay() * 86400000);
	}

	private java.util.Date from(OffsetTime offsetTime) {
		return java.util.Date.from(offsetTime.atDate(LocalDate.ofEpochDay(0)).toInstant());
	}

	private java.util.Date from(LocalTime localTime) {
		return java.util.Date.from(ZonedDateTime.of(localTime.atDate(LocalDate.ofEpochDay(0)), getZoneId()).toInstant());
	}

}
