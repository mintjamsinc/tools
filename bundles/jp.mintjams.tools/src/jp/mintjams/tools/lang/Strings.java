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

package jp.mintjams.tools.lang;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.Charset;

public class Strings {

	private Strings() {}

	public static boolean isEmpty(String value) {
		return (value == null || value.isEmpty());
	}

	public static boolean isBlank(String value) {
		return (value == null || value.trim().isEmpty());
	}

	public static String trim(String value) {
		if (value == null) {
			return null;
		}

		return value.trim();
	}

	public static String trimToEmpty(String value) {
		if (value == null) {
			return "";
		}

		return value.trim();
	}

	public static String defaultString(String value) {
		return defaultString(value, "");
	}

	public static String defaultString(String value, String defaultValue) {
		if (value == null) {
			return defaultValue;
		}

		return value;
	}

	public static String defaultIfEmpty(String value, String defaultValue) {
		if (isEmpty(value)) {
			return defaultValue;
		}

		return value;
	}

	public static String defaultIfBlank(String value, String defaultValue) {
		if (isBlank(value)) {
			return defaultValue;
		}

		return value;
	}

	public static String readAll(InputStream stream, String charsetName) throws IOException {
		return readAll(stream, Charset.forName(charsetName));
	}

	public static String readAll(InputStream stream, Charset charset) throws IOException {
		return readAll(new InputStreamReader(stream, charset));
	}

	public static String readAll(Reader reader) throws IOException {
		try (reader) {
			StringWriter out = new StringWriter();
			for (char[] buf = new char[8192];;) {
				int length = reader.read(buf);
				if (length == -1) {
					break;
				}
				out.write(buf, 0, length);
			}
			return out.toString();
		}
	}

}
