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

package jp.mintjams.tools.internal.mail;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.mail.Session;
import javax.net.ssl.SSLContext;

import jp.mintjams.tools.internal.net.SSLContextBuilder;

public class SessionBuilder {
	private URI fURI;

	private SessionBuilder(URI uri) {
		fURI = uri;
	}

	public static SessionBuilder create() {
		return new SessionBuilder(null);
	}

	public static SessionBuilder create(URI uri) {
		return new SessionBuilder(uri);
	}

	private SSLContext fSSLContext;
	public SessionBuilder setSSLContext(SSLContext sslContext) {
		fSSLContext = sslContext;
		return this;
	}

	public Session build() throws URISyntaxException, KeyManagementException, NoSuchAlgorithmException {
		Properties properties = new Properties(System.getProperties());

		if (fURI != null) {
			String protocol = fURI.getScheme().toLowerCase();
			properties.put("mail.host", fURI.getHost());
			properties.put("mail." + protocol + ".host", fURI.getHost());
			int port = fURI.getPort();
			if (port != -1) {
				properties.put("mail." + protocol + ".port", "" + port);
			}
			boolean sslEnabled = "imaps".equals(protocol) || "pop3s".equals(protocol) || "smtps".equals(protocol);
			if ("smtps".equals(protocol)) {
				properties.put("mail.smtp.starttls.enable", "true");
				properties.put("mail.transport.protocol", "smtp");
			} else {
				properties.put("mail." + protocol + ".ssl.enable", "" + sslEnabled);
			}
			if (sslEnabled) {
				SSLContext ctx = fSSLContext;
				if (ctx == null) {
					ctx = SSLContextBuilder.create("TLS").build();
				}
				properties.put("mail." + protocol + ".ssl.socketFactory", ctx.getSocketFactory());
			}
		}

		return Session.getInstance(properties);
	}

}
