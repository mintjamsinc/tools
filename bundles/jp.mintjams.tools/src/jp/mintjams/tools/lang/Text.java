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

public class Text {

	private String fText;

	private Text(String text) {
		fText = text;
	}

	public static Text from(InputStream stream, String charsetName) throws IOException {
		return from(stream, Charset.forName(charsetName));
	}

	public static Text from(InputStream stream, Charset charset) throws IOException {
		return from(new InputStreamReader(stream, charset));
	}

	public static Text from(Reader reader) throws IOException {
		try (reader) {
			StringWriter out = new StringWriter();
			for (char[] buf = new char[8192];;) {
				int length = reader.read(buf);
				if (length == -1) {
					break;
				}
				out.write(buf, 0, length);
			}
			return new Text(out.toString());
		}
	}

	@Override
	public String toString() {
		return fText.toString();
	}

}
