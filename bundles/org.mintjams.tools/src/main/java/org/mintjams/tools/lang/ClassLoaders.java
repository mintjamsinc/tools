/*
 * Copyright (c) 2022 MintJams Inc.
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

package org.mintjams.tools.lang;

import java.io.Closeable;

public class ClassLoaders {

	private ClassLoaders() {}

	public static Closeable withClassLoader(ClassLoader classLoader) {
		return new WithClassLoader(classLoader);
	}

	private static class WithClassLoader implements Closeable {
		private final ClassLoader fContextClassLoader;

		private WithClassLoader(ClassLoader classLoader) {
			fContextClassLoader = Thread.currentThread().getContextClassLoader();
			if (classLoader != null) {
				Thread.currentThread().setContextClassLoader(classLoader);
			}
		}

		@Override
		public void close() {
			Thread.currentThread().setContextClassLoader(fContextClassLoader);
		}
	}

}
