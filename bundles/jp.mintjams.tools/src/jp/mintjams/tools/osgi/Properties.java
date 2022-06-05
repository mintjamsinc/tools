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

package jp.mintjams.tools.osgi;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import jp.mintjams.tools.adapter.Adaptable;

public class Properties implements Adaptable {

	private Dictionary<String, Object> fProperties = new Hashtable<>();

	private Properties(Dictionary<String, ?> properties) {
		if (properties != null) {
			for (Enumeration<String> e = properties.keys(); e.hasMoreElements();) {
				String k = e.nextElement();
				fProperties.put(k, properties.get(k));
			}
		}
	}

	private Properties(Map<String, ?> properties) {
		for (String k : properties.keySet()) {
			fProperties.put(k, properties.get(k));
		}
	}

	public static Properties create() {
		return new Properties((Map<String, ?>) null);
	}

	public static Properties create(Dictionary<String, ?> properties) {
		return new Properties(properties);
	}

	public static Properties create(Map<String, ?> properties) {
		return new Properties(properties);
	}

	public Object getObject(String key) {
		Object propValue = fProperties.get(key);
		if (propValue == null) {
			return null;
		}

		if (propValue.getClass().isArray()) {
			Object[] prop = (Object[]) propValue;
			return prop.length > 0 ? prop[0] : null;
		}

		if (propValue instanceof Collection<?>) {
			Collection<?> prop = (Collection<?>) propValue;
			return prop.isEmpty() ? null : prop.iterator().next();
		}

		return propValue;
	}

	public boolean getBoolean(String key) {
		return getBoolean(key, false);
	}

	public boolean getBoolean(String key, boolean defaultValue) {
		Object propValue = getObject(key);
		if (propValue instanceof Boolean) {
			return (Boolean) propValue;
		}

		if (propValue != null) {
			return Boolean.parseBoolean(String.valueOf(propValue));
		}

		return defaultValue;
	}

	public String getString(String key) {
		return getString(key, "");
	}

	public String getString(String key, String defaultValue) {
		Object propValue = getObject(key);
		return (propValue != null) ? propValue.toString() : defaultValue;
	}

	public long getLong(String key) {
		return getLong(key, 0);
	}

	public long getLong(String key, long defaultValue) {
		Object propValue = getObject(key);
		if (propValue instanceof Long) {
			return (Long) propValue;
		}

		if (propValue != null) {
			try {
				return Long.parseLong(String.valueOf(propValue));
			} catch (Throwable ignore) {}
		}

		return defaultValue;
	}

	public int getInteger(String key) {
		return getInteger(key, 0);
	}

	public int getInteger(String key, int defaultValue) {
		Object propValue = getObject(key);
		if (propValue instanceof Integer) {
			return (Integer) propValue;
		}

		if (propValue != null) {
			try {
				return Integer.parseInt(String.valueOf(propValue));
			} catch (Throwable ignore) {}
		}

		return defaultValue;
	}

	public double getDouble(String key) {
		return getDouble(key, 0);
	}

	public double getDouble(String key, double defaultValue) {
		Object propValue = getObject(key);
		if (propValue instanceof Double) {
			return (Double) propValue;
		}

		if (propValue != null) {
			try {
				return Double.parseDouble(String.valueOf(propValue));
			} catch (Throwable ignore) {}
		}

		return defaultValue;
	}

	public String[] getStringArray(String key) {
		return getStringArray(key, null);
	}

	public String[] getStringArray(String key, String[] defaultArray) {
		Object propValue = getObject(key);
		if (propValue == null) {
			return defaultArray;
		}

		if (propValue instanceof String) {
			return new String[] { (String) propValue };
		}

		if (propValue instanceof String[]) {
			return (String[]) propValue;
		}

		if (propValue.getClass().isArray()) {
			Object[] valueArray = (Object[]) propValue;
			List<String> values = new ArrayList<String>(valueArray.length);
			for (Object value : valueArray) {
				if (value != null) {
					values.add(value.toString());
				}
			}
			return values.toArray(new String[values.size()]);

		}

		if (propValue instanceof Collection<?>) {
			Collection<?> valueCollection = (Collection<?>) propValue;
			List<String> valueList = new ArrayList<String>(valueCollection.size());
			for (Object value : valueCollection) {
				if (value != null) {
					valueList.add(value.toString());
				}
			}
			return valueList.toArray(new String[valueList.size()]);
		}

		return defaultArray;
	}

	public Enumeration<String> keys() {
		return fProperties.keys();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <AdapterType> AdapterType adaptTo(Class<AdapterType> adapter) {
		if (adapter.equals(Dictionary.class)) {
			return (AdapterType) fProperties;
		}

		return null;
	}

}
