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

package org.mintjams.tools.io;

import java.io.Closeable;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;
import java.nio.file.Path;

public class LockFile implements Closeable {

	private RandomAccessFile fRandom;
	private FileLock fLock;

	private LockFile(Path path) throws IOException {
		try {
			fRandom = new RandomAccessFile(path.toFile(), "rw");
			fLock = fRandom.getChannel().tryLock();
		} catch (Throwable ex) {
			close();
			if (ex instanceof IOException) {
				throw ex;
			}
			if (ex instanceof RuntimeException) {
				throw ex;
			}
			throw (IOException) new IOException(ex.getMessage()).initCause(ex);
		}

		if (fLock == null) {
			close();
			throw new IOException("Could not obtain the lock.");
		}
	}

	public static LockFile create(Path path) throws IOException {
		return new LockFile(path);
	}

	public static LockFile create(String path) throws IOException {
		return new LockFile(Path.of(path));
	}

	@Override
	public synchronized void close() throws IOException {
		if (fLock != null) {
			try {
				fLock.release();
			} catch (Throwable ignore) {}
			fLock = null;
		}

		if (fRandom != null) {
			try {
				fRandom.close();
			} catch (Throwable ignore) {}
			fRandom = null;
		}
	}

}
