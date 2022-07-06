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

package org.mintjams.tools.osgi;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.osgi.framework.BundleContext;

public class Configuration {

	private final BundleContext fBundleContext;
	private VariableProvider fVariableProvider;

	private Configuration(BundleContext context) {
		fBundleContext = context;
		fVariableProvider = new DefaultVariableProvider();
	}

	public static Configuration create(BundleContext context) {
		return new Configuration(context);
	}

	public Configuration with(VariableProvider variableProvider) {
		fVariableProvider = variableProvider;
		return this;
	}

	public String replaceVariables(String source) {
		StringBuilder result = new StringBuilder();

		Matcher m = Pattern.compile("\\$\\{.+?\\}").matcher(source);
		int p = 0;
		while (m.find()) {
			result.append(source.substring(p, m.start()));
			p = m.end();

			String variable = m.group();
			variable = variable.substring(2, variable.length() - 1).trim();

			Object value = fVariableProvider.getVariable(variable);
			if (value == null) {
				throw new IllegalArgumentException("Undefined variable: " + variable + " (Source: " + source + ")");
			}

			String replacement = value.toString();
			if (replacement.indexOf("${") != -1) {
				replacement = replaceVariables(replacement);
			}

			result.append(replacement);
		}
		result.append(source.substring(p));

		return result.toString();
	}

	private class DefaultVariableProvider implements VariableProvider {
		private final java.util.Properties fSystemProperties = System.getProperties();

		@Override
		public Object getVariable(String name) {
			String value = fBundleContext.getProperty(name);
			if (value != null) {
				return value;
			}
			return fSystemProperties.getProperty(name);
		}
	}

}
