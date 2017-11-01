/**
 *
 * {@link AbstractMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.api.services;

import java.io.IOException;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractMediumStoreTest} is the base class for testing the {@link MediumStore} interface. Each subclass
 * corresponds to a specific {@link Medium} type, and all {@link Medium} instances returned by a subclass is either all
 * read-only or all read-write. Thus, this class only contains the common reading tests. Furthermore, there are some
 * media which in principle do not allow caching. For those, {@link #getFilledMediumWithCacheBiggerThanMedium()} and
 * {@link #getFilledMediumWithCacheSmallerThanMedium()} must return null.
 * 
 * The filled media used for testing all must contain {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT} at the beginning, a
 * String fully containing only human-readable standard ASCII characters. This guarantees that 1 bytes = 1 character.
 * Furthermore, all bytes inserted must also be standard human-readable ASCII characters with this property.
 *
 * @param <T>
 *           The type of {@link Medium} to use
 */
public abstract class AbstractMediumStoreTest<T extends Medium<?>> {

   private MediumStore mediumStoreUnderTest;

   /**
    * For getting the current test case's name, must be public
    */
   @Rule
   public TestName testName = new TestName();

   /**
    * Validates all test files needed in this test class
    */
   @BeforeClass
   public static void validateTestFiles() {
      MediaTestFiles.validateTestFiles();
   }

   /**
    * Closes the {@link MediumStore} under test, if necessary.
    */
   @After
   public void tearDown() {
      if (mediumStoreUnderTest != null && mediumStoreUnderTest.isOpened()) {
         mediumStoreUnderTest.close();
      }
   }

   /**
    * Tests {@link MediumStore#close}.
    */
   @Test
   public void close_onOpenedMediumStore_closesStore() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.close();

      Assert.assertFalse(mediumStoreUnderTest.isOpened());
   }

   /**
    * Tests {@link MediumStore#close}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void close_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.close();
      mediumStoreUnderTest.close();
   }

   /**
    * Tests {@link MediumStore#isOpened()}.
    */
   @Test
   public void isOpened_onNewMediumStore_returnsTrue() {
      mediumStoreUnderTest = createEmptyMediumStore();

      Assert.assertTrue(mediumStoreUnderTest.isOpened());
   }

   /**
    * Creates an empty {@link Medium} for testing, i.e. with zero bytes.
    * 
    * For writable media, be sure to only return a copy of the original medium such that the original medium is not
    * modified by writing tests and all tests remain repeatable.
    * 
    * @param testMethodName
    *           the name of the current test method, can be used to create a copy of the original medium with the given
    *           name
    * 
    * @return an empty {@link Medium} for testing
    * 
    * @throws IOException
    *            In case of any errors creating the {@link Medium}
    */
   protected abstract T createEmptyMedium(String testMethodName) throws IOException;

   /**
    * Creates a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT} as content, backed or not
    * backed by a cache with the given maximum cache and cache region size as well as the given maximum read write block
    * size. Implementing test classes who's medium type does not support caching must return null.
    * 
    * For writable media, be sure to only return a copy of the original medium such that the original medium is not
    * modified by writing tests and all tests remain repeatable.
    * 
    * @param testMethodName
    *           the name of the current test method, can be used to create a copy of the original medium with the given
    *           name
    * @param enableCaching
    *           true to enable, false to disable caching
    * @param maxCacheSize
    *           the maximum cache size in bytes
    * @param maxCacheRegionSize
    *           the maximum cache region size in bytes
    * @param maxReadWriteBlockSize
    *           the maximum read write block size in bytes
    * 
    * @return a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT} as content with the given
    *         configuration parameters
    * 
    * @throws IOException
    *            In case of any errors creating the {@link Medium}
    */
   protected abstract T createFilledMedium(String testMethodName, boolean enableCaching, long maxCacheSize,
      int maxCacheRegionSize, int maxReadWriteBlockSize) throws IOException;

   /**
    * Returns the current content of the given {@link Medium} as string representation. Implementations should implement
    * a strategy to get this content that is independent of the classes under test. They can distinguish the media by
    * medium name to identify them. This method is used to check the current medium content against the expected medium
    * content.
    * 
    * @return the current content of the filled {@link Medium}
    */
   protected abstract String getCurrentMediumContentAsString(T medium);

   /**
    * Creates a {@link MediumStore} to test based on a given {@link Medium}.
    * 
    * @param mediumToUse
    *           The {@link Medium} to use for the {@link MediumStore}.
    * @return a {@link MediumStore} to test based on a given {@link Medium}.
    */
   protected abstract MediumStore createMediumStoreToTest(T mediumToUse);

   /**
    * Creates a {@link MediumStore} based on an empty {@link Medium}.
    * 
    * @return a {@link MediumStore} based on an empty {@link Medium}
    */
   protected MediumStore createEmptyMediumStore() {
      try {
         return createMediumStoreToTest(createEmptyMedium(testName.getMethodName()));
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }

   /**
    * Creates a {@link MediumStore} based on a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}
    * as content, backed by a cache, where the cache is smaller than the overall {@link Medium} size. This method must
    * be called at the beginning of a test case to create the {@link MediumStore} to test and its return value must be
    * assigned to {@link #mediumStoreUnderTest}. It is used for testing cases where data is read into the cache but then
    * automatically purged due to the limited cache size.
    * 
    * @return a {@link MediumStore} based on a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}
    *         as content, backed by a small cache, or null if the current implementation does not support this
    */
   protected MediumStore createFilledMediumStoreWithSmallCache() {
      T filledMedium;
      try {
         filledMedium = createFilledMedium(testName.getMethodName(), true,
            MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() - 20, MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() - 20,
            10);

         if (filledMedium == null) {
            return null;
         }

         return createMediumStoreToTest(filledMedium);
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }

   /**
    * Creates a {@link MediumStore} based on a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}
    * as content, backed by a cache, where the cache is (much) bigger than the overall {@link Medium} size. This method
    * must be called at the beginning of a test case to create the {@link MediumStore} to test and its return value must
    * be assigned to {@link #mediumStoreUnderTest}. It is used for testing cases where data read is expected to always
    * end up in the cache due to its big size.
    * 
    * @return a {@link MediumStore} based on a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}
    *         as content, backed by a big cache, or null if the current implementation does not support this
    */
   protected MediumStore createFilledMediumStoreWithBigCache() {
      T filledMedium;
      try {
         filledMedium = createFilledMedium(testName.getMethodName(), true,
            MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() + 1000,
            MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() - 20, 10);

         if (filledMedium == null) {
            return null;
         }

         return createMediumStoreToTest(filledMedium);
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }

   /**
    * Creates a {@link MediumStore} based on a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}
    * as content, with disabled caching. This method must be called at the beginning of a test case to create the
    * {@link MediumStore} to test and its return value must be assigned to {@link #mediumStoreUnderTest}. It is used by
    * tests that check that disabled caching really works as expected.
    * 
    * @return a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT} as content, with disabled
    *         caching
    */
   protected MediumStore createFilledUncachedMediumStore() {
      try {
         return createMediumStoreToTest(createFilledMedium(testName.getMethodName(), false, 1, 1, 10));
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }
}
