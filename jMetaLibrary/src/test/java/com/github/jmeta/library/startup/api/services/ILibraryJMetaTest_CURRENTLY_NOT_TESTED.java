/**
 *
 * ILibraryJMetaTest.java
 *
 * @author Jens
 *
 * @date 25.06.2016
 *
 */
package com.github.jmeta.library.startup.api.services;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.jmeta.utility.testsetup.api.services.JMetaTestBasics;

/**
 * {@link ILibraryJMetaTest_CURRENTLY_NOT_TESTED}
 *
 */
public class ILibraryJMetaTest_CURRENTLY_NOT_TESTED {

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
