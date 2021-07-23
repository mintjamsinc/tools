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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Map;

import jp.mintjams.tools.lang.AbstractValueAdapter;
import jp.mintjams.tools.lang.Adaptables;

public class ReaderValueAdapter extends AbstractValueAdapter<Reader> {

	public ReaderValueAdapter() {
		super();
	}

	public ReaderValueAdapter(Map<String, Object> env) {
		super(env);
	}

	@Override
	public Reader adapt(Object value) {
		if (value == null) {
			return null;
		}

		Reader readerValue = Adaptables.getAdapter(value, Reader.class);
		if (readerValue != null) {
			return readerValue;
		}

		Clob clobValue = Adaptables.getAdapter(value, Clob.class);
		if (clobValue != null) {
			try {
				return clobValue.getCharacterStream();
			} catch (SQLException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		}

		InputStream streamValue = Adaptables.getAdapter(value, InputStream.class);
		if (streamValue != null) {
			try {
				return new InputStreamReader(streamValue, getEncoding());
			} catch (IOException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		}

		byte[] byteArrayValue = Adaptables.getAdapter(value, byte[].class);
		if (byteArrayValue != null) {
			try {
				return new InputStreamReader(new ByteArrayInputStream(byteArrayValue), getEncoding());
			} catch (IOException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		}

		File fileValue = Adaptables.getAdapter(value, File.class);
		if (fileValue != null) {
			try {
				return new InputStreamReader(new FileInputStream(fileValue), getEncoding());
			} catch (IOException ex) {
				throw (IllegalArgumentException) new IllegalArgumentException(ex.getMessage()).initCause(ex);
			}
		}

		String stringValue = Adaptables.getAdapter(value, String.class);
		if (stringValue != null) {
			return new StringReader(stringValue);
		}

		return null;
	}

}
