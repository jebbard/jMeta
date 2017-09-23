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

/**
 * {@link AbstractExtensionManagementTest} sets up the environment for extension management testing.
 */
public abstract class AbstractExtensionManagementTest {

   private IExtensionManager testling;

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {

      JMetaTestBasics.emptyLogFile(JMetaTestBasics.DEFAULT_LOG_FILE);
      testling = new StandardExtensionManager();
   }

   /**
    * Returns the {@link IExtensionManager} for testing.
    * 
    * @return the {@link IExtensionManager} for testing.
    */
   protected IExtensionManager getTestling() {

      return testling;
   }
}
