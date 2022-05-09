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

package jp.mintjams.tools.mail;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Iterator;
import java.util.Objects;

import javax.mail.Folder;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.search.SearchTerm;

import jp.mintjams.tools.internal.mail.QueryFilter;
import jp.mintjams.tools.internal.mail.SessionBuilder;
import jp.mintjams.tools.io.Closer;

public class Query {

	private final URI fURI;
	private final String fUsername;
	private final String fPassword;
	private final SearchTerm fSearchTerm;

	private Query(Builder builder) throws URISyntaxException {
		fURI = new URI(builder.fURI);
		fUsername = builder.fUsername;
		fPassword = builder.fPassword;
		fSearchTerm = builder.fSearchTerm;
	}

	public Result execute() throws MessagingException {
		return new ResultImpl();
	}

	private Session newSession() {
		try {
			return SessionBuilder.create(fURI).build();
		} catch (Throwable ex) {
			throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
		}
	}

	private Store getStore(Session session) throws MessagingException {
		String protocol = fURI.getScheme().toLowerCase();
		Store store = session.getStore(protocol);
		String username = fURI.getUserInfo();
		if (fUsername != null) {
			username = fUsername;
		}
		if (username != null) {
			store.connect(username, fPassword);
		} else {
			store.connect();
		}
		return store;
	}

	private Closeable asCloseable(Store store) {
		return new Closeable() {
			@Override
			public void close() throws IOException {
				try {
					store.close();
				} catch (MessagingException ex) {
					throw (IOException) new IOException(ex.getMessage()).initCause(ex);
				}
			}
		};
	}

	private Closeable asCloseable(Folder folder) {
		return new Closeable() {
			@Override
			public void close() throws IOException {
				try {
					folder.close(true);
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

		private SearchTerm fSearchTerm;
		public Builder setSearchTerm(SearchTerm term) {
			fSearchTerm = term;
			return this;
		}
		public Builder setFilter(String filter) {
			fSearchTerm = new QueryFilter().parse(filter);
			return this;
		}

		public Query build() throws URISyntaxException {
			Objects.requireNonNull(fURI);
			return new Query(this);
		}
	}

	public interface Result extends Iterable<Message>, Closeable {
		int getSize();
	}

	private class ResultImpl implements Result {
		private final Closer fCloser = Closer.create();
		private final Session fSession;
		private final Folder fFolder;
		private final javax.mail.Message[] fMessages;
		private javax.mail.Message fNextMessage;
		private int fNumber = 0;

		private Iterator<Message> fIterator = new Iterator<Message>() {
			@Override
			public boolean hasNext() {
				return (fNextMessage != null);
			}

			@Override
			public Message next() {
				try {
					return Message.from(fNextMessage);
				} catch (MessagingException | IOException ex) {
					throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
				} finally {
					fNextMessage = getNextMessage();
				}
			}
		};

		private ResultImpl() throws MessagingException {
			fSession = newSession();
			Store store = getStore(fSession);
			fCloser.add(asCloseable(store));

			String path = fURI.getPath();
			if (path.startsWith("/")) {
				path = path.substring(1);
			}
			fFolder = store.getFolder(path);
			fCloser.add(asCloseable(fFolder));
			fFolder.open(Folder.READ_WRITE);

			if (fSearchTerm != null) {
				fMessages = fFolder.search(fSearchTerm);
			} else {
				fMessages = null;
			}
			fNextMessage = getNextMessage();
		}

		private javax.mail.Message getNextMessage() {
			if (fMessages == null) {
				try {
					return fFolder.getMessage(++fNumber);
				} catch (Throwable ignore) {}
			} else {
				if (fNumber < fMessages.length) {
					return fMessages[fNumber++];
				}
			}
			return null;
		}

		@Override
		public int getSize() {
			if (fMessages == null) {
				try {
					return fFolder.getMessageCount();
				} catch (Throwable ignore) {}
			}
			return fMessages.length;
		}

		@Override
		public Iterator<Message> iterator() {
			return fIterator;
		}

		@Override
		public void close() throws IOException {
			fCloser.close();
		}
	}

}
