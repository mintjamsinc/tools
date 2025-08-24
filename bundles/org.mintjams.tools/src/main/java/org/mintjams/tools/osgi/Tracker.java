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

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.TreeMap;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

public class Tracker<ServiceType> implements Closeable {

	private final BundleContext fBundleContext;
	private final ServiceTracker<ServiceType, ServiceType> fServiceTracker;
	private final Listener<? super ServiceType> fListener;
	private final Map<ServiceReference<ServiceType>, ServiceType> fServices = new TreeMap<>(new ServiceReferenceComparator());
	private ServiceTrackerCustomizer<ServiceType, ServiceType> fServiceTrackerCustomizer = new ServiceTrackerCustomizer<ServiceType, ServiceType>() {
		@Override
		public ServiceType addingService(ServiceReference<ServiceType> reference) {
			ServiceType service = fBundleContext.getService(reference);
			fServices.put(reference, service);
			if (fListener != null) {
				fListener.on(new ServiceAddingEvent<>(service));
			}
			return service;
		}

		@Override
		public void modifiedService(ServiceReference<ServiceType> reference, ServiceType service) {
			if (fListener != null) {
				fListener.on(new ServiceModifiedEvent<>(service));
			}
		}

		@Override
		public void removedService(ServiceReference<ServiceType> reference, ServiceType service) {
			if (fListener != null) {
				fListener.on(new ServiceRemovedEvent<>(service));
			}
			fServices.remove(reference);
			try {
				fBundleContext.ungetService(reference);
			} catch (Throwable ignore) {}
		}
	};

	private Tracker(Builder<ServiceType> builder) throws InvalidSyntaxException {
		fBundleContext = builder.fBundleContext;
		fServiceTracker = builder.createServiceTracker(fServiceTrackerCustomizer);
		fListener = builder.fListener;
	}

	public int getTrackingCount() {
		return fServices.size();
	}

	public Map<ServiceReference<ServiceType>, ServiceType> getTracked() {
		return Collections.unmodifiableMap(fServices);
	}

	public ServiceType getService() {
		if (fServices.size() == 0) {
			return null;
		}
		return fServices.values().iterator().next();
	}

	public Collection<ServiceType> getServices() {
		return Collections.unmodifiableCollection(fServices.values());
	}

	public Tracker<ServiceType> open() {
		fServiceTracker.open();
		return this;
	}

	@Override
	public void close() throws IOException {
		if (fServiceTracker != null) {
			ServiceReference<ServiceType>[] references = fServiceTracker.getServiceReferences();
			if (references != null) {
				for (ServiceReference<ServiceType> reference : references) {
					try {
						fBundleContext.ungetService(reference);
					} catch (Throwable ignore) {}
				}
			}

			fServiceTracker.close();
		}
	}

	public static <ServiceType> Builder<ServiceType> newBuilder(Class<ServiceType> serviceType) {
		return Builder.create(serviceType);
	}

	public static class Builder<ServiceType> {
		private BundleContext fBundleContext;
		private Class<ServiceType> fServiceType;
		private Dictionary<String, Object> fProperties = new Hashtable<>();
		private Listener<? super ServiceType> fListener;

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

		public Builder<ServiceType> setProperty(String key, String value) {
			fProperties.put(key, value);
			return this;
		}

		public Builder<ServiceType> setListener(Listener<? super ServiceType> listener) {
			fListener = listener;
			return this;
		}

		private ServiceTracker<ServiceType, ServiceType> createServiceTracker(ServiceTrackerCustomizer<ServiceType, ServiceType> serviceTrackerCustomizer) throws InvalidSyntaxException {
			StringBuilder buf = new StringBuilder();
			buf.append("(&");
			buf.append("(").append(Constants.OBJECTCLASS).append("=").append(fServiceType.getName()).append(")");
			for (Enumeration<String> e = fProperties.keys(); e.hasMoreElements();) {
				String k = e.nextElement();
				Object v = fProperties.get(k);
				buf.append("(").append(k).append("=").append(v.toString()).append(")");
			}
			buf.append(")");
			return new ServiceTracker<>(fBundleContext, fBundleContext.createFilter(buf.toString()), serviceTrackerCustomizer);
		}

		public Tracker<ServiceType> build() throws InvalidSyntaxException {
			return new Tracker<>(this);
		}
	}

	private class ServiceReferenceComparator implements Comparator<ServiceReference<ServiceType>> {
		@Override
		public int compare(ServiceReference<ServiceType> reference1, ServiceReference<ServiceType> reference2) {
			int ranking1 = getRanking(reference1);
			int ranking2 = getRanking(reference2);
			if (ranking1 > ranking2) {
				return -1;
			}
			if (ranking1 < ranking2) {
				return 1;
			}

			long id1 = getID(reference1);
			long id2 = getID(reference2);
			if (id1 < id2) {
				return -1;
			}
			if (id1 > id2) {
				return 1;
			}

			return 0;
		}

		private int getRanking(ServiceReference<ServiceType> reference) {
			try {
				Object v = reference.getProperty(Constants.SERVICE_RANKING);
				return (v instanceof Integer) ? Integer.class.cast(v) : Integer.valueOf(0);
			} catch (Throwable ignore) {}
			return 0;
		}

		private long getID(ServiceReference<ServiceType> reference) {
			try {
				Object v = reference.getProperty(Constants.SERVICE_ID);
				return (v instanceof Long) ? Long.class.cast(v) : Long.valueOf(0);
			} catch (Throwable ignore) {}
			return 0;
		}
	}

	public interface Listener<ListenerType> {
		public void on(Event<ListenerType> event);
	}

	public interface Event<ListenerType> {
		public ListenerType getService();
	}

	public static abstract class AbstractEvent<ListenerType> implements Event<ListenerType> {
		private final ListenerType fService;

		protected AbstractEvent(ListenerType service) {
			fService = service;
		}

		public ListenerType getService() {
			return fService;
		}
	}

	public static class ServiceAddingEvent<ListenerType> extends AbstractEvent<ListenerType> {
		private ServiceAddingEvent(ListenerType service) {
			super(service);
		}
	}

	public static class ServiceModifiedEvent<ListenerType> extends AbstractEvent<ListenerType> {
		private ServiceModifiedEvent(ListenerType service) {
			super(service);
		}
	}

	public static class ServiceRemovedEvent<ListenerType> extends AbstractEvent<ListenerType> {
		private ServiceRemovedEvent(ListenerType service) {
			super(service);
		}
	}

}
