package com.github.jmeta.utility.compregistry.api.services;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import com.github.jmeta.utility.compregistry.api.exceptions.NoRegisteredServiceFoundException;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * This class allows to load a single service provider implementation for a
 * given service interface. This facilitates decoupling of components. A
 * component is a collection of Java classes with an API (public) part and an
 * implementation part. It can expose several Java service interfaces, for which
 * other components can request implementations by using the
 * {@link ComponentRegistry} class. This makes them unaware of the concrete
 * implementations and ensures there are no direct dependencies between
 * component implementations.
 *
 * Note that this class uses the {@link ServiceLoader} utility. In order to use
 * it, you must configure your services correspondingly in META-INF
 * configuration files, see the javadocs for {@link ServiceLoader}. THus, its
 * purpose is to ensure that you do not need to load a new {@link ServiceLoader}
 * any time you require a provider, which would decrease performance because
 * during loading, also file I/O happens in {@link ServiceLoader}.
 *
 * Note that this class is NOT thread-safe.
 */
public class ComponentRegistry {

	private static final Map<Class<?>, ServiceLoader<?>> SERVICE_LOADERS = new HashMap<>();

	/**
	 * Clears the complete {@link ServiceLoader} cache, thus enforcing
	 * re-instantiation of any {@link ServiceLoader} and the corresponding services
	 * it loads whenever it is next requested by {@link #lookupService(Class)}.
	 */
	public static void clearServiceCache() {
		ComponentRegistry.SERVICE_LOADERS.clear();
	}

	/**
	 * Looks up a service provider (i.e. implementation) for a given service
	 * interface. If there are multiple providers (which should be avoided), it just
	 * returns the first provider found, without giving any details about which one
	 * will be chosen. If there is no service provider registered, a
	 * {@link NoRegisteredServiceFoundException} is thrown.
	 * 
	 * @param service The service to query, should be a Java interface. Must not be
	 *                null.
	 * @param <S>     The concrete service interface type
	 * @return The first service provider found to be registered for the given
	 *         service interface.
	 */
	@SuppressWarnings("unchecked")
	public static <S> S lookupService(Class<S> service) {
		Reject.ifNull(service, "service");

		ServiceLoader<S> serviceLoader = null;

		if (ComponentRegistry.SERVICE_LOADERS.containsKey(service)) {
			serviceLoader = (ServiceLoader<S>) ComponentRegistry.SERVICE_LOADERS.get(service);
		} else {
			serviceLoader = ServiceLoader.load(service);
		}

		Iterator<S> serviceProviderIterator = serviceLoader.iterator();

		if (!serviceProviderIterator.hasNext()) {
			throw new NoRegisteredServiceFoundException("No registered service found for " + service);
		}

		ComponentRegistry.SERVICE_LOADERS.put(service, serviceLoader);

		return serviceProviderIterator.next();
	}
}
