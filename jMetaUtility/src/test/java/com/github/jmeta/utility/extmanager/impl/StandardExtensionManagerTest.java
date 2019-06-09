/**
 *
 * {@link StandardExtensionManagerTest}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.impl;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.services.ExtensionManager;
import com.github.jmeta.utility.extmanager.impl.testextensions.TestExtensionServiceOne;
import com.github.jmeta.utility.extmanager.impl.testextensions.TestExtensionServiceTwo;

/**
 * {@link StandardExtensionManagerTest} tests the {@link ExtensionManager} and
 * its standard implementation as well as the {@link Extension} interface for a
 * dummy implementation.
 */
public class StandardExtensionManagerTest {

	/**
	 * Tests {@link ExtensionManager#getAllExtensions()}.
	 */
	@Test
	public void getAllExtensions_returnsAllRegisteredExtensionsOnClassPath() {
		ExtensionManager extManager = new StandardExtensionManager();

		List<Extension> extensions = extManager.getAllExtensions();

		Assert.assertEquals(2, extensions.size());
		Extension firstExtension = extensions.get(0);
		Extension secondExtension = extensions.get(1);

		Assert.assertNotNull(firstExtension);
		Assert.assertNotNull(secondExtension);

		Assert.assertNotNull(firstExtension.getExtensionDescription());
		Assert.assertNotNull(secondExtension.getExtensionDescription());

		Assert.assertEquals(2, firstExtension.getAllServiceProviders(TestExtensionServiceOne.class).size());
		Assert.assertEquals(0, firstExtension.getAllServiceProviders(TestExtensionServiceTwo.class).size());

		Assert.assertEquals(1, secondExtension.getAllServiceProviders(TestExtensionServiceOne.class).size());
		Assert.assertEquals(1, secondExtension.getAllServiceProviders(TestExtensionServiceTwo.class).size());
	}

}
