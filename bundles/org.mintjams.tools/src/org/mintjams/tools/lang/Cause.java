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

package org.mintjams.tools.lang;

public class Cause {

	private final Throwable fThrowable;

	private Cause(Throwable throwable) {
		if (throwable == null) {
			throw new IllegalArgumentException("Throwable must not be null.");
		}

		fThrowable = throwable;
	}

	public static Cause create(Throwable throwable) {
		return new Cause(throwable);
	}

	public <ThrowType extends Throwable> ThrowType wrap(Class<ThrowType> throwType) {
		return wrap(throwType, null, true);
	}

	public <ThrowType extends Throwable> ThrowType wrap(Class<ThrowType> throwType, boolean throwIfRuntimeException) {
		return wrap(throwType, null, throwIfRuntimeException);
	}

	public <ThrowType extends Throwable> ThrowType wrap(Class<ThrowType> throwType, String message) {
		return wrap(throwType, message, true);
	}

	@SuppressWarnings("unchecked")
	public <ThrowType extends Throwable> ThrowType wrap(Class<ThrowType> throwType, String message, boolean throwIfRuntimeException) {
		if (throwIfRuntimeException) {
			if (fThrowable instanceof RuntimeException) {
				throw (RuntimeException) fThrowable;
			}
		}

		if (throwType.isInstance(fThrowable)) {
			return (ThrowType) fThrowable;
		}

		try {
			if (message == null) {
				return (ThrowType) throwType.getConstructor().newInstance().initCause(fThrowable);
			}

			return (ThrowType) throwType.getConstructor(String.class).newInstance(message).initCause(fThrowable);
		} catch (Throwable ex) {
			if (throwType.equals(ex.getClass())) {
				return (ThrowType) ex;
			}
			throw (IllegalStateException) new IllegalStateException(ex.getMessage()).initCause(ex);
		}
	}

}
