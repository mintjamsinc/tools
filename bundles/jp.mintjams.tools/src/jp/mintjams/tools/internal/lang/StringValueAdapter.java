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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;
import java.time.OffsetDateTime;
import java.util.Map;

import jp.mintjams.tools.adapter.AbstractValueAdapter;
import jp.mintjams.tools.adapter.Adaptables;
import jp.mintjams.tools.internal.io.ReaderValueAdapter;

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

		Reader reader = new ReaderValueAdapter(fEnv).adapt(value);
		if (reader != null) {
			try {
				return asString(reader);
			} catch (IOException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		}

		if (value instanceof java.util.Date) {
			return OffsetDateTime.ofInstant(((java.util.Date) value).toInstant(), getZoneId()).toString();
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

}
