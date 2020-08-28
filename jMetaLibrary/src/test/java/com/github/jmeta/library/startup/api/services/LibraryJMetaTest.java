/**
 *
 * {@link LibraryJMetaTest}.java
 *
 * @author Jens
 *
 * @date 25.06.2016
 *
 */
package com.github.jmeta.library.startup.api.services;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.utility.testsetup.api.services.JMetaTestBasics;

/**
 * {@link LibraryJMetaTest} tests the {@link LibraryJMeta} interface and its
 * default implementation.
 */
public class LibraryJMetaTest {

	/**
	 * Tests {@link LibraryJMeta#getLowLevelAPI()}.
	 */
	@Test
	public void getDataBlockAccessor_returnsNonNullObject() {
		JMetaTestBasics.emptyLogFile(JMetaTestBasics.DEFAULT_UNITTEST_LOG_FILE);

		LibraryJMeta library = LibraryJMeta.getLibrary();

		Assert.assertNotNull(library.getLowLevelAPI());

		JMetaTestBasics.performGeneralLogCheck(JMetaTestBasics.DEFAULT_UNITTEST_LOG_FILE);
	}

	/**
	 * Tests {@link LibraryJMeta#getDataFormatRepository()}.
	 */
	@Test
	public void getDataFormatRepository_returnsNonNullObject() {
		JMetaTestBasics.emptyLogFile(JMetaTestBasics.DEFAULT_UNITTEST_LOG_FILE);

		LibraryJMeta library = LibraryJMeta.getLibrary();

		Assert.assertNotNull(library.getDataFormatRepository());

		JMetaTestBasics.performGeneralLogCheck(JMetaTestBasics.DEFAULT_UNITTEST_LOG_FILE);
	}

	/**
	 * Tests {@link LibraryJMeta#getLibrary()}.
	 */
	@Test
	public void getLibrary_startupWithoutErrors() {
		JMetaTestBasics.emptyLogFile(JMetaTestBasics.DEFAULT_UNITTEST_LOG_FILE);

		LibraryJMeta library = LibraryJMeta.getLibrary();

		Assert.assertNotNull(library);

		JMetaTestBasics.performGeneralLogCheck(JMetaTestBasics.DEFAULT_UNITTEST_LOG_FILE);
	}
}
