/**
 *
 * {@link TestExtensionsOne}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.impl.testextensions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link TestExtensionsOne} is just an example extension.
 */
public class TestExtensionsOne implements Extension {

	/**
	 * @see com.github.jmeta.utility.extmanager.api.services.Extension#getAllServiceProviders(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getAllServiceProviders(Class<T> serviceInterface) {

		ArrayList<T> returnedProviders = new ArrayList<>();

		if (serviceInterface == TestExtensionServiceOne.class) {
			returnedProviders.add((T) new ExtensionOneServiceProviderOne());
			returnedProviders.add((T) new ExtensionOneServiceProviderTwo());
		}

		return returnedProviders;
	}

	/**
	 * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionDescription()
	 */
	@Override
	public ExtensionDescription getExtensionDescription() {
		return new ExtensionDescription("Test", "Test", "version", LocalDateTime.now(), "My ext", "Copy", "License");
	}

	/**
	 * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionId()
	 */
	@Override
	public String getExtensionId() {
		return "a";
	}
}
