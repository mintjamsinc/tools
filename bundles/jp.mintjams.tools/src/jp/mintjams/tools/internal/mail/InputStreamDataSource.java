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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.activation.DataSource;

public class InputStreamDataSource implements DataSource, Closeable {

	private final Cache fCache;
	private final String fFilename;
	private final String fContentType;

	public InputStreamDataSource(InputStream stream, String filename, String contentType) throws IOException {
		this.fCache = new Cache(stream);
		this.fFilename = filename;
		this.fContentType = contentType;
	}

	@Override
	public String getContentType() {
		return fContentType;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return fCache.getInputStream();
	}

	@Override
	public String getName() {
		return fFilename;
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return null;
	}

	@Override
	public void close() throws IOException {
		try {
			fCache.close();
		} catch (Throwable ignore) {}
	}

	private class Cache implements Closeable {
		private final Path fPath;

		private Cache(InputStream in) throws IOException {
			fPath = Files.createTempFile("data-", null);
			fPath.toFile().deleteOnExit();

			try (OutputStream out = new BufferedOutputStream(Files.newOutputStream(fPath))) {
				try (in) {
					byte[] buffer = new byte[8192];
					for (;;) {
						int length = in.read(buffer);
						if (length == -1) {
							break;
						}
						out.write(buffer, 0, length);
					}
				}
			}
		}

		public InputStream getInputStream() throws IOException {
			return new BufferedInputStream(Files.newInputStream(fPath));
		}

		@Override
		public void close() throws IOException {
			try {
				Files.deleteIfExists(fPath);
			} catch (Throwable ignore) {}
		}
	}

}
