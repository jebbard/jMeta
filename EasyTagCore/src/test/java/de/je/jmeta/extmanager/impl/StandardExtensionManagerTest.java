/**
 *
 * {@link StandardExtensionManagerTest}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package de.je.jmeta.extmanager.impl;

import java.util.List;

import org.junit.Test;

import de.je.jmeta.extmanager.api.IExtension;
import de.je.jmeta.extmanager.api.IExtensionManager;
import de.je.jmeta.extmanager.impl.testextensions.ITestExtensionServiceOne;
import de.je.jmeta.extmanager.impl.testextensions.ITestExtensionServiceTwo;
import junit.framework.Assert;

/**
 * {@link StandardExtensionManagerTest} tests the {@link IExtensionManager} and its standard implementation as well as
 * the {@link IExtension} interface for a dummy implementation.
 */
public class StandardExtensionManagerTest {

   /**
    * Tests {@link IExtensionManager#getAllExtensions()}.
    */
   @Test
   public void getAllExtensions_returnsAllRegisteredExtensionsOnClassPath() {
      IExtensionManager extManager = new StandardExtensionManager();

      List<IExtension> extensions = extManager.getAllExtensions();

      Assert.assertEquals(2, extensions.size());
      IExtension firstExtension = extensions.get(0);
      IExtension secondExtension = extensions.get(1);

      Assert.assertNotNull(firstExtension);
      Assert.assertNotNull(secondExtension);

      Assert.assertNotNull(firstExtension.getExtensionDescription());
      Assert.assertNotNull(secondExtension.getExtensionDescription());

      Assert.assertEquals(2, firstExtension.getAllServiceProviders(ITestExtensionServiceOne.class).size());
      Assert.assertEquals(0, firstExtension.getAllServiceProviders(ITestExtensionServiceTwo.class).size());

      Assert.assertEquals(1, secondExtension.getAllServiceProviders(ITestExtensionServiceOne.class).size());
      Assert.assertEquals(1, secondExtension.getAllServiceProviders(ITestExtensionServiceTwo.class).size());
   }

}
