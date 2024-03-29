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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

public class IOs {

	private IOs() {}

	public static void copy(InputStream in, OutputStream out) throws IOException {
		for (byte[] buffer = new byte[8192];;) {
			int length = in.read(buffer);
			if (length == -1) {
				break;
			}
			out.write(buffer, 0, length);
		}
	}

	public static void copy(InputStream in, OutputStream out, long offset, long length) throws IOException {
		if (offset < 0) {
			throw new IllegalArgumentException("Invalid offset: " + offset);
		}
		if (length < 0) {
			throw new IllegalArgumentException("Invalid length: " + length);
		}

		if (length == 0) {
			return;
		}

		if (offset > 0) {
			in.skip(offset);
		}

		for (byte[] buffer = new byte[8192]; length > 0;) {
			int readLength;
			if (length >= buffer.length) {
				readLength = in.read(buffer);
			} else {
				readLength = in.read(buffer, 0, Math.toIntExact(length));
			}
			if (readLength == -1) {
				break;
			}
			out.write(buffer, 0, readLength);
			length -= readLength;
		}
	}

	public static void copy(Reader in, Writer out) throws IOException {
		for (char[] buffer = new char[8192];;) {
			int length = in.read(buffer);
			if (length == -1) {
				break;
			}
			out.write(buffer, 0, length);
		}
	}

	public static byte[] toByteArray(InputStream in) throws IOException {
		try (in) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			copy(in, out);
			return out.toByteArray();
		}
	}

	public static void deleteIfExists(Path path) throws IOException {
		if (!Files.exists(path)) {
			return;
		}

		if (!Files.isDirectory(path)) {
			Files.delete(path);
			return;
		}

		try (Stream<Path> stream = Files.walk(path)) {
			stream.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
		}
	}

	public static void closeQuietly(AutoCloseable closeable) {
		try {
			closeable.close();
		} catch (Throwable ignore) {}
	}

}
