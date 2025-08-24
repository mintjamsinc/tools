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

package org.mintjams.tools.internal.mail;

import java.nio.charset.Charset;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.internet.ContentType;

public class Charsets {

	public static final String UTF8 = "UTF-8";
	public static final String ISO2022JP = "ISO-2022-JP";

	public static String from(Part part) throws MessagingException {
		Objects.requireNonNull(part);

		if (part.getContentType() != null) {
			String charset = from(new ContentType(part.getContentType()), null);
			if (charset != null) {
				if (charset.equalsIgnoreCase("CP932")) {
					charset = "Windows-31J";
				}
				return charset;
			}
		}

		String encoding = null;
		try {
			encoding = part.getHeader("Content-Transfer-Encoding")[0];
		} catch (Throwable ignore) {}

		if (encoding != null) {
			if ("7bit".equalsIgnoreCase(encoding)) {
				return ISO2022JP;
			}
			if ("8bit".equalsIgnoreCase(encoding)) {
				return UTF8;
			}
		}

		return UTF8;
	}

	public static String from(ContentType contentType, String defaultCharset) {
		Objects.requireNonNull(contentType);

		String charset = contentType.getParameter("charset");
		if (charset == null || !Charset.isSupported(charset)) {
			return defaultCharset;
		}
		return charset;
	}

}
