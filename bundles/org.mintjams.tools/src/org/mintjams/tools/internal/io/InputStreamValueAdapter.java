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

package org.mintjams.tools.internal.io;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.ParameterizedType;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Map;

import org.mintjams.tools.adapter.AbstractValueAdapter;
import org.mintjams.tools.adapter.Adaptables;
import org.mintjams.tools.adapter.UnadaptableValueException;

public class InputStreamValueAdapter extends AbstractValueAdapter<InputStream> {

	public InputStreamValueAdapter() {
		super();
	}

	public InputStreamValueAdapter(Map<String, Object> env) {
		super(env);
	}

	@Override
	public InputStream adapt(Object value) {
		if (value == null) {
			return null;
		}

		InputStream streamValue = Adaptables.getAdapter(value, InputStream.class);
		if (streamValue != null) {
			return streamValue;
		}

		Blob blobValue = Adaptables.getAdapter(value, Blob.class);
		if (blobValue != null) {
			try {
				return blobValue.getBinaryStream();
			} catch (SQLException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		}

		Clob clobValue = Adaptables.getAdapter(value, Clob.class);
		if (clobValue != null) {
			try {
				return clobValue.getAsciiStream();
			} catch (SQLException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		}

		byte[] byteArrayValue = Adaptables.getAdapter(value, byte[].class);
		if (byteArrayValue != null) {
			return new ByteArrayInputStream(byteArrayValue);
		}

		File fileValue = Adaptables.getAdapter(value, File.class);
		if (fileValue != null) {
			try {
				return new FileInputStream(fileValue);
			} catch (IOException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		}

		String stringValue = Adaptables.getAdapter(value, String.class);
		if (stringValue != null) {
			try {
				return new ByteArrayInputStream(stringValue.getBytes(getEncoding()));
			} catch (IOException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		}

		throw new UnadaptableValueException("Value cannot adapt to type \""
				+ ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName()
				+ "\"");
	}

}
