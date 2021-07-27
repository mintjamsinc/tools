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
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class Dates {

	private Dates() {}

	public static java.util.Date asDate(java.sql.Timestamp timestamp) {
		return new java.util.Date(timestamp.getTime());
	}

	public static java.util.Date asDate(java.sql.Date date) {
		return new java.util.Date(date.getTime());
	}

	public static java.util.Date asDate(java.sql.Time time) {
		return new java.util.Date(time.getTime());
	}

	public static java.util.Date asDate(OffsetDateTime offsetDateTime) {
		return java.util.Date.from(offsetDateTime.toInstant());
	}

	public static java.util.Date asDate(LocalDateTime localDateTime, ZoneId zoneId) {
		return java.util.Date.from(ZonedDateTime.of(localDateTime, zoneId).toInstant());
	}

	public static java.util.Date asDate(LocalDate localDate) {
		return new java.util.Date(localDate.toEpochDay() * 86400000);
	}

	public static java.util.Date asDate(OffsetTime offsetTime) {
		return java.util.Date.from(offsetTime.atDate(LocalDate.ofEpochDay(0)).toInstant());
	}

	public static java.util.Date asDate(LocalTime localTime, ZoneId zoneId) {
		return java.util.Date.from(ZonedDateTime.of(localTime.atDate(LocalDate.ofEpochDay(0)), zoneId).toInstant());
	}

	public static java.util.Date asDate(long timeInMillis) {
		return new java.util.Date(timeInMillis);
	}

	public static OffsetDateTime asOffsetDateTime(java.util.Date value, ZoneId zoneId) {
		if (value instanceof java.sql.Date || value instanceof java.sql.Time || value instanceof java.sql.Timestamp) {
			value = new java.util.Date(value.getTime());
		}

		return OffsetDateTime.ofInstant(value.toInstant(), zoneId);
	}

}
