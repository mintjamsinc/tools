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

import java.io.Closeable;
import java.io.IOException;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

public class Registration<ServiceType> implements Closeable {

	private final ServiceRegistration<ServiceType> fServiceRegistration;
	private final ServiceType fService;

	private Registration(ServiceRegistration<ServiceType> serviceRegistration, ServiceType service) {
		fServiceRegistration = serviceRegistration;
		fService = service;
	}

	public ServiceType getService() {
		return fService;
	}

	@Override
	public void close() throws IOException {
		if (fServiceRegistration != null) {
			fServiceRegistration.unregister();
		}
	}

	public static <ServiceType> Builder<ServiceType> newBuilder(Class<ServiceType> serviceType) {
		return Builder.create(serviceType);
	}

	public static class Builder<ServiceType> {
		private BundleContext fBundleContext;
		private Class<ServiceType> fServiceType;
		private ServiceType fService;
		private Dictionary<String, Object> fProperties = new Hashtable<>();

		private Builder(Class<ServiceType> serviceType) {
			fServiceType = serviceType;
		}

		public static <ServiceType> Builder<ServiceType> create(Class<ServiceType> serviceType) {
			return new Builder<>(serviceType);
		}

		public Builder<ServiceType> setBundleContext(BundleContext bundleContext) {
			fBundleContext = bundleContext;
			return this;
		}

		public Builder<ServiceType> setService(ServiceType service) {
			fService = service;
			return this;
		}

		public Builder<ServiceType> setProperties(Dictionary<String, ?> properties) {
			for (Enumeration<String> e = properties.keys(); e.hasMoreElements();) {
				String k = e.nextElement();
				fProperties.put(k, properties.get(k));
			}
			return this;
		}

		public Builder<ServiceType> setProperty(String key, Object value) {
			fProperties.put(key, value);
			return this;
		}

		public Registration<ServiceType> build() {
			return new Registration<ServiceType>(fBundleContext.registerService(fServiceType, fService, fProperties), fService);
		}
	}

}
