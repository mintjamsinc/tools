/*
 * Copyright (c) 2024 MintJams Inc.
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

package org.mintjams.tools.util;

import java.util.Calendar;

import org.mintjams.tools.internal.util.DateValueAdapter;

public class DateTime {

	private final Calendar fCalendar;

	private DateTime(long time) {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		fCalendar = c;
	}

	public static DateTime create() {
		return new DateTime(System.currentTimeMillis());
	}

	public static DateTime create(long value) {
		return new DateTime(value);
	}

	public static DateTime create(java.util.Date value) {
		return new DateTime(value.getTime());
	}

	public static DateTime create(Calendar value) {
		return new DateTime(value.getTimeInMillis());
	}

	public static DateTime create(String value) {
		return new DateTime(new DateValueAdapter().adapt(value).getTime());
	}

	public DateTime add(int field, int amount) {
		fCalendar.add(field, amount);
		return this;
	}

	public DateTime addYears(int amount) {
		return add(Calendar.YEAR, amount);
	}

	public DateTime addMonths(int amount) {
		return add(Calendar.MONTH, amount);
	}

	public DateTime addDays(int amount) {
		return add(Calendar.DAY_OF_MONTH, amount);
	}

	public DateTime addHours(int amount) {
		return add(Calendar.HOUR_OF_DAY, amount);
	}

	public DateTime addMinutes(int amount) {
		return add(Calendar.MINUTE, amount);
	}

	public DateTime addSeconds(int amount) {
		return add(Calendar.SECOND, amount);
	}

	public DateTime addMilliseconds(int amount) {
		return add(Calendar.MILLISECOND, amount);
	}

	public DateTime set(int field, int amount) {
		fCalendar.set(field, amount);
		return this;
	}

	public DateTime setYear(int amount) {
		return set(Calendar.YEAR, amount);
	}

	public DateTime setMonth(int amount) {
		return set(Calendar.MONTH, amount);
	}

	public DateTime setDay(int amount) {
		return set(Calendar.DAY_OF_MONTH, amount);
	}

	public DateTime setHour(int amount) {
		return set(Calendar.HOUR_OF_DAY, amount);
	}

	public DateTime setMinute(int amount) {
		return set(Calendar.MINUTE, amount);
	}

	public DateTime setSecond(int amount) {
		return set(Calendar.SECOND, amount);
	}

	public DateTime setMillisecond(int amount) {
		return set(Calendar.MILLISECOND, amount);
	}

	public int get(int field) {
		return fCalendar.get(field);
	}

	public int getYear() {
		return get(Calendar.YEAR);
	}

	public int getMonth() {
		return get(Calendar.MONTH);
	}

	public int getDay() {
		return get(Calendar.DAY_OF_MONTH);
	}

	public int getHour() {
		return get(Calendar.HOUR_OF_DAY);
	}

	public int getMinute() {
		return get(Calendar.MINUTE);
	}

	public int getSecond() {
		return get(Calendar.SECOND);
	}

	public int getMillisecond() {
		return get(Calendar.MILLISECOND);
	}

	public Calendar asCalendar() {
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(fCalendar.getTimeInMillis());
		c.setTimeZone(fCalendar.getTimeZone());
		return c;
	}

	public java.util.Date asDate() {
		return new java.util.Date(fCalendar.getTimeInMillis());
	}

}
