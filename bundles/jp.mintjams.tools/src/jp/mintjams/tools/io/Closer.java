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

import java.io.Closeable;
import java.io.IOException;
import java.util.Vector;

public class Closer extends Vector<Closeable> implements Closeable {

	private static final long serialVersionUID = 1L;

	private Closer() {}

	public static Closer create() {
		return new Closer();
	}

	@Deprecated
	public static Closer newCloser() {
		return create();
	}

	public synchronized boolean add(AutoCloseable e) {
		return add(new Closeable() {
			@Override
			public void close() throws IOException {
				try {
					e.close();
				} catch (Throwable ex) {
					throw (IOException) new IOException(ex.getMessage()).initCause(ex);
				}
			}
		});
	}

	public synchronized <C extends AutoCloseable> C register(C e) {
		add(e);
		return e;
	}

	public synchronized <C extends AutoCloseable> C unregister(C e) {
		remove(e);
		return e;
	}

	@Override
	public synchronized void close() throws IOException {
		close(true);
	}

	public synchronized void close(boolean quietly) throws IOException {
		while (!isEmpty()) {
			try {
				remove(size() - 1).close();
			} catch (Throwable ex) {
				if (quietly) {
					continue;
				}

				if (ex instanceof IOException) {
					throw ex;
				}
				throw (IOException) new IOException(ex.getMessage()).initCause(ex);
			}
		}
	}

}
