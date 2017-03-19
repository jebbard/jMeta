/**
 *
 * {@link AbstractExtensionManagementTest}.java
 *
 * @author Jens
 *
 * @date 24.03.2016
 *
 */
package de.je.jmeta.extmanager.export;

import org.junit.Before;

import de.je.jmeta.extmanager.impl.StandardExtensionManager;
import de.je.jmeta.testHelpers.basics.JMetaTestBasics;
import de.je.util.javautil.simpleregistry.ISimpleComponentRegistry;
import de.je.util.javautil.simpleregistry.SimpleComponentRegistry;

/**
 * {@link AbstractExtensionManagementTest} sets up the environment for extension management testing.
 */
public abstract class AbstractExtensionManagementTest {

   private IExtensionManager testling;

   private ISimpleComponentRegistry registry;

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {

      JMetaTestBasics.emptyLogFile(JMetaTestBasics.DEFAULT_LOG_FILE);
      registry = createExtensionManager();
      testling = registry.getComponentImplementation(IExtensionManager.class);
   }

   /**
    * Returns the {@link IExtensionManager} for testing.
    * 
    * @return the {@link IExtensionManager} for testing.
    */
   protected IExtensionManager getTestling() {

      return testling;
   }

   /**
    * Returns the {@link ISimpleComponentRegistry} to use.
    * 
    * @return the {@link ISimpleComponentRegistry} to use.
    */
   protected ISimpleComponentRegistry getComponentRegistry() {

      return registry;
   }

   /**
    * Creates the {@link ISimpleComponentRegistry} instance for testing {@link IExtensionManager}.
    */
   private static ISimpleComponentRegistry createExtensionManager() {

      ISimpleComponentRegistry newRegistry = new SimpleComponentRegistry();
      IExtensionManager extensionManager = new StandardExtensionManager(newRegistry);

      // To avoid unused warnings
      assert(extensionManager != null);

      return newRegistry;
   }

}
