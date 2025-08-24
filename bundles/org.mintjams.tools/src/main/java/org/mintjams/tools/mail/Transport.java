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

package org.mintjams.tools.mail;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

import javax.mail.MessagingException;
import javax.mail.Session;

import org.mintjams.tools.internal.mail.SessionBuilder;
import org.mintjams.tools.io.Closer;

public class Transport implements Closeable {

	private final Closer fCloser = Closer.create();
	private final URI fURI;
	private final String fUsername;
	private final String fPassword;
	private final Session fSession;
	private final javax.mail.Transport fTransport;

	private Transport(Builder builder) throws URISyntaxException, MessagingException {
		fURI = new URI(builder.fURI);
		fUsername = builder.fUsername;
		fPassword = builder.fPassword;

		String protocol = fURI.getScheme().toLowerCase();
		if ("smtps".equals(protocol)) {
			protocol = "smtp";
		}
		fSession = newSession();
		fTransport = fSession.getTransport(protocol);
		fCloser.add(asCloseable(fTransport));
		String username = fURI.getUserInfo();
		if (fUsername != null) {
			username = fUsername;
		}
		fTransport.connect(
				fURI.getHost(),
				fURI.getPort(),
				username,
				fPassword);
	}

	public void send(javax.mail.Message message) throws MessagingException {
		fTransport.sendMessage(message, message.getAllRecipients());
	}

	@Override
	public void close() throws IOException {
		fCloser.close();
	}

	private Session newSession() {
		try {
			return SessionBuilder.create(fURI).build();
		} catch (Throwable ex) {
			throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
		}
	}

	private Closeable asCloseable(javax.mail.Transport transport) {
		return new Closeable() {
			@Override
			public void close() throws IOException {
				try {
					transport.close();
				} catch (MessagingException ex) {
					throw (IOException) new IOException(ex.getMessage()).initCause(ex);
				}
			}
		};
	}

	public static class Builder {
		private final String fURI;

		private Builder(String uri) {
			fURI = uri;
		}

		public static Builder create(String uri) {
			return new Builder(uri);
		}

		private String fUsername;
		public Builder setUsername(String username) {
			fUsername = username;
			return this;
		}

		private String fPassword;
		public Builder setPassword(String password) {
			fPassword = password;
			return this;
		}

		public Transport build() throws URISyntaxException, MessagingException {
			Objects.requireNonNull(fURI);
			return new Transport(this);
		}
	}

}
