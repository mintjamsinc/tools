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

package org.mintjams.tools.internal.lang;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.util.Calendar;
import java.util.Map;

import org.mintjams.tools.adapter.AbstractValueAdapter;
import org.mintjams.tools.adapter.Adaptables;
import org.mintjams.tools.adapter.UnadaptableValueException;
import org.mintjams.tools.adapter.ValueAdapters;
import org.mintjams.tools.internal.util.Dates;

public class StringValueAdapter extends AbstractValueAdapter<String> {

	public StringValueAdapter() {
		super();
	}

	public StringValueAdapter(Map<String, Object> env) {
		super(env);
	}

	@Override
	public String adapt(Object value) {
		if (value == null) {
			return null;
		}

		String stringValue = Adaptables.getAdapter(value, String.class);
		if (stringValue != null) {
			return stringValue;
		}

		try {
			Reader reader = ValueAdapters.createValueAdapter(fEnv, Reader.class).adapt(value);
			try {
				return asString(reader);
			} catch (IOException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		} catch (UnadaptableValueException ignore) {}

		if (value instanceof Calendar) {
			return formatDateTime(((Calendar) value).getTime());
		}

		if (value instanceof java.util.Date) {
			return formatDateTime((java.util.Date) value);
		}

		if (value instanceof Temporal) {
			return asString((Temporal) value);
		}

		if (value instanceof Number) {
			return asString((Number) value);
		}

		if (value instanceof URI) {
			return ((URI) value).toASCIIString();
		}

		if (value instanceof URL) {
			try {
				return ((URL) value).toURI().toASCIIString();
			} catch (URISyntaxException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		}

		return value.toString();
	}

	private String asString(Reader reader) throws IOException {
		try (Reader in = asBufferedReader(reader)) {
			StringWriter out = new StringWriter();
			char[] buffer = new char[8192];
			for (;;) {
				int n = in.read(buffer);
				if (n == -1) {
					break;
				}
				out.write(buffer, 0, n);
			}
			return out.toString();
		}
	}

	private BufferedReader asBufferedReader(Reader reader) {
		if (reader instanceof BufferedReader) {
			return (BufferedReader) reader;
		}

		return new BufferedReader(reader);
	}

	private String formatDateTime(java.util.Date value) {
		return Dates.asOffsetDateTime(value, getDisplayZoneId()).toString();
	}

	private String formatDate(java.util.Date value) {
		return Dates.asOffsetDateTime(value, getDisplayZoneId()).toLocalDate().toString();
	}

	private String formatTime(java.util.Date value) {
		return Dates.asOffsetDateTime(value, getDisplayZoneId()).toOffsetTime().toString();
	}

	private String asString(Temporal value) {
		if (value instanceof OffsetDateTime) {
			return formatDateTime(Dates.asDate((OffsetDateTime) value));
		}

		if (value instanceof LocalDateTime) {
			return formatDateTime(Dates.asDate((LocalDateTime) value, getZoneId()));
		}

		if (value instanceof LocalDate) {
			return formatDate(Dates.asDate((LocalDate) value));
		}

		if (value instanceof OffsetTime) {
			return formatTime(Dates.asDate((OffsetTime) value));
		}

		if (value instanceof LocalTime) {
			return formatTime(Dates.asDate((LocalTime) value, getZoneId()));
		}

		return value.toString();
	}

	private String asString(Number value) {
		if (value instanceof BigDecimal) {
			return ((BigDecimal) value).toPlainString();
		}

		if (value instanceof BigInteger) {
			return ((BigInteger) value).toString();
		}

		if (value instanceof Float) {
			return new BigDecimal("" + value.floatValue()).toPlainString();
		}

		if (value instanceof Double) {
			return new BigDecimal("" + value.doubleValue()).toPlainString();
		}

		return BigDecimal.valueOf(value.longValue()).toPlainString();
	}

}
