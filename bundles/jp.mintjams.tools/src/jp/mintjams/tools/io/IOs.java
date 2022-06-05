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

package jp.mintjams.tools.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;

public class IOs {

	private IOs() {}

	public static void copy(InputStream in, OutputStream out) throws IOException {
		byte[] buffer = new byte[8192];
		for (;;) {
			int length = in.read(buffer);
			if (length == -1) {
				break;
			}
			out.write(buffer, 0, length);
		}
	}

	public static String toString(Reader in) throws IOException {
		try (in) {
			StringWriter out = new StringWriter();
			char[] buffer = new char[8192];
			for (;;) {
				int length = in.read(buffer);
				if (length == -1) {
					break;
				}
				out.write(buffer, 0, length);
			}
			return out.toString();
		}
	}

	public static String toString(InputStream in, String charsetName) throws IOException {
		return toString(new InputStreamReader(in, charsetName));
	}

}
