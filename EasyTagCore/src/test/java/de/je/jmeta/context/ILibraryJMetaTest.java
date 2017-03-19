/**
 *
 * ILibraryJMetaTest.java
 *
 * @author Jens
 *
 * @date 25.06.2016
 *
 */
package de.je.jmeta.context;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import de.je.jmeta.testHelpers.basics.JMetaTestBasics;

/**
 * {@link ILibraryJMetaTest}
 *
 */
public class ILibraryJMetaTest {

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {
      JMetaTestBasics.emptyLogFile(JMetaTestBasics.DEFAULT_LOG_FILE);
   }

   /**
    * Tears down the test fixtures.
    */
   @After
   public void tearDown() {
      JMetaTestBasics.performGeneralLogCheck(JMetaTestBasics.DEFAULT_LOG_FILE);
   }

   /**
    * Tests {@link ILibraryJMeta#getLibrary()}.
    */
   @Test
   public void getLibrary_startupWithoutErrors() {
      ILibraryJMeta library = ILibraryJMeta.getLibrary();

      Assert.assertNotNull(library);
   }

}
