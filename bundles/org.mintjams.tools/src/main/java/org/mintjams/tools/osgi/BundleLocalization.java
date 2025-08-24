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

import java.util.Locale;
import java.util.ResourceBundle;

import org.osgi.framework.Bundle;
import org.osgi.framework.Constants;
import org.osgi.framework.wiring.BundleWiring;

public class BundleLocalization {

	private final Bundle fBundle;
	private final Locale fLocale;

	private BundleLocalization(Bundle bundle, Locale locale) {
		fBundle = bundle;
		fLocale = locale;
	}

	public static BundleLocalization create(Bundle bundle) {
		return new BundleLocalization(bundle, Locale.getDefault());
	}

	public static BundleLocalization create(Bundle bundle, Locale locale) {
		return new BundleLocalization(bundle, locale);
	}

	private ResourceBundle getResourceBundle() {
		String baseName = fBundle.getHeaders().get(Constants.BUNDLE_LOCALIZATION);
		if (baseName == null || baseName.trim().isEmpty()) {
			baseName = Constants.BUNDLE_LOCALIZATION_DEFAULT_BASENAME;
		}
		return ResourceBundle.getBundle(baseName.replace('/', '.'), fLocale, fBundle.adapt(BundleWiring.class).getClassLoader());
	}

	public String getVendor() {
		String v = fBundle.getHeaders(fLocale.toString()).get(Constants.BUNDLE_VENDOR);
		return (v != null) ? v.toString().trim() : "";
	}

	public String getString(String key) {
		return getResourceBundle().getString(key);
	}

}
