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

package jp.mintjams.tools.adapter;

import java.nio.charset.Charset;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

public abstract class AbstractValueAdapter<ValueType> implements ValueAdapter<ValueType> {

	protected final Map<String, Object> fEnv;

	protected AbstractValueAdapter() {
		this(new HashMap<>());
	}

	protected AbstractValueAdapter(Map<String, Object> env) {
		fEnv = env;
	}

	protected String getEncoding() {
		String encoding = null;
		Object o = fEnv.get(ENV_ENCODING);
		if (o instanceof Charset) {
			encoding = ((Charset) o).name();
		} else if (o instanceof String) {
			encoding = (String) o;
		}
		if (encoding == null) {
			return Charset.defaultCharset().name();
		}
		return encoding;
	}

	protected ZoneId getZoneId() {
		ZoneId zoneId = null;
		Object o = fEnv.get(ENV_ZONEID);
		if (o instanceof ZoneId) {
			zoneId = (ZoneId) o;
		} else if (o instanceof String) {
			zoneId = ZoneId.of((String) o);
		}
		if (zoneId == null) {
			return ZoneId.systemDefault();
		}
		return zoneId;
	}

	@Override
	public AdaptableValue<ValueType> getAdaptableValue(Object value) {
		return new AdaptableValueImpl(value);
	}

	private class AdaptableValueImpl implements AdaptableValue<ValueType> {
		private final Object fValue;

		private AdaptableValueImpl(Object value) {
			fValue = value;
		}

		@Override
		public AdaptableValue<ValueType> setProperty(String key, Object value) {
			fEnv.put(key, value);
			return this;
		}

		@Override
		public AdaptableValue<ValueType> setEncoding(String encoding) {
			if (!Charset.isSupported(encoding)) {
				throw new IllegalArgumentException(encoding);
			}

			setProperty(ENV_ENCODING, encoding);
			return this;
		}

		@Override
		public AdaptableValue<ValueType> setEncoding(Charset encoding) {
			setProperty(ENV_ENCODING, encoding);
			return null;
		}

		@Override
		public AdaptableValue<ValueType> setZoneId(String zoneId) {
			if (!ZoneId.getAvailableZoneIds().contains(zoneId)) {
				throw new IllegalArgumentException(zoneId);
			}

			setProperty(ENV_ZONEID, zoneId);
			return this;
		}

		@Override
		public AdaptableValue<ValueType> setZoneId(ZoneId zoneId) {
			setProperty(ENV_ZONEID, zoneId);
			return this;
		}

		@Override
		public ValueType getValue() {
			return adapt(fValue);
		}
	}

}
