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

import static com.github.jmeta.library.media.api.helper.MediaTestUtility.at;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
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

   protected MediumStore mediumStoreUnderTest;
   protected T currentMedium;

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
    * Tests {@link MediumStore#close()}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void open_onOpenedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();
      mediumStoreUnderTest.open();
   }

   /**
    * Tests {@link MediumStore#close()}.
    */
   @Test
   public void close_onOpenedMediumStore_closesStore() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.close();

      Assert.assertFalse(mediumStoreUnderTest.isOpened());
   }

   /**
    * Tests {@link MediumStore#close()}.
    */
   @Test
   public void close_onMediumStoreWithCachedContent_freesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeNotNull(mediumStoreUnderTest);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 0), 10);

      mediumStoreUnderTest.close();

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 0)));
   }

   /**
    * Tests {@link MediumStore#close()}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void close_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.close();
      mediumStoreUnderTest.close();
   }

   /**
    * Tests {@link MediumStore#isOpened()}.
    */
   @Test
   public void isOpened_onNewMediumStore_returnsFalse() {
      mediumStoreUnderTest = createEmptyMediumStore();

      Assert.assertFalse(mediumStoreUnderTest.isOpened());
   }

   /**
    * Tests {@link MediumStore#isOpened()} and {@link MediumStore#open()}.
    */
   @Test
   public void isOpened_onOpenedMediumStore_returnsTrue() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      Assert.assertTrue(mediumStoreUnderTest.isOpened());
   }

   /**
    * Tests {@link MediumStore#getMedium()}.
    */
   @Test
   public void getMedium_onOpenedMediumStore_returnsExpectedMedium() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      Assert.assertEquals(currentMedium, mediumStoreUnderTest.getMedium());
   }

   /**
    * Tests {@link MediumStore#getMedium()}.
    */
   @Test
   public void getMedium_onClosedMediumStore_returnsExpectedMedium() {
      mediumStoreUnderTest = createEmptyMediumStore();

      Assert.assertEquals(currentMedium, mediumStoreUnderTest.getMedium());
   }

   /**
    * Tests {@link MediumStore#createMediumReference(long)}.
    */
   @Test
   public void createMediumReference_onOpenedMediumStore_returnsExpectedReference() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      int offset = 10;
      MediumReference actualReference = mediumStoreUnderTest.createMediumReference(offset);

      Assert.assertEquals(at(currentMedium, offset), actualReference);
   }

   /**
    * Tests {@link MediumStore#createMediumReference(long)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void createMediumReference_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.createMediumReference(10);
   }

   /**
    * Tests {@link MediumStore#createMediumReference(long)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void createMediumReference_forInvalidOffset_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.createMediumReference(-10);
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forFilledMediumAndOffsetBeforeEnd_returnsFalse() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      mediumStoreUnderTest.open();

      Assert.assertFalse(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, 0)));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forEmptyMediumAtStartOffset_returnsTrue() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      Assert.assertTrue(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, 0)));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forFilledMediumAndCacheUntilEOM_returnsTrue() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      int mediumSizeInBytes = getCurrentMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      // Read all bytes until EOM such that even for streams, we are at end of medium
      cacheNoEOMExpected(at(currentMedium, 0), mediumSizeInBytes);

      Assert.assertTrue(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, mediumSizeInBytes)));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void isAtEndOfMedium_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, 10));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void isAtEndOfMedium_forInvalidReference_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.isAtEndOfMedium(at(MediaTestUtility.OTHER_MEDIUM, 10));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getCachedByteCountAt_forInvalidReference_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.getCachedByteCountAt(at(MediaTestUtility.OTHER_MEDIUM, 10));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void cache_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void cache_forInvalidReference_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(MediaTestUtility.OTHER_MEDIUM, 10), 10);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void getData_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      getDataNoEOMExpected(at(currentMedium, 10), 10);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getData_forInvalidReference_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      getDataNoEOMExpected(at(MediaTestUtility.OTHER_MEDIUM, 10), 10);
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
    * In test cases, you should only call this method either BEFORE opening the medium (to get the initial content) or
    * AFTER the closing of the medium (to get the changed content), otherwise you might run into exception because the
    * medium is still locked by the {@link MediumStore} and it cannot be accessed.
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
         currentMedium = createEmptyMedium(testName.getMethodName());

         return createMediumStoreToTest(currentMedium);
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
      try {
         currentMedium = createFilledMedium(testName.getMethodName(), true,
            MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() - 20, MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() - 20,
            10);

         if (currentMedium == null) {
            return null;
         }

         return createMediumStoreToTest(currentMedium);
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
      try {
         currentMedium = createFilledMedium(testName.getMethodName(), true,
            MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() + 1000,
            MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() - 20, 10);

         if (currentMedium == null) {
            return null;
         }

         return createMediumStoreToTest(currentMedium);
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
         currentMedium = createFilledMedium(testName.getMethodName(), false, 1, 1, 10);
         return createMediumStoreToTest(currentMedium);
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }

   /**
    * Calls {@link MediumStore#getData(MediumReference, int)} and expects no end of medium.
    * 
    * @param offset
    *           The offset to use
    * @param byteCount
    *           The number of bytes to cache
    */
   private ByteBuffer getDataNoEOMExpected(MediumReference offset, int byteCount) {
      try {
         return mediumStoreUnderTest.getData(offset, byteCount);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Calls {@link MediumStore#cache(MediumReference, int)} and expects no end of medium.
    * 
    * @param offset
    *           The offset to use
    * @param byteCount
    *           The number of bytes to cache
    */
   private void cacheNoEOMExpected(MediumReference offset, int byteCount) {
      try {
         mediumStoreUnderTest.cache(offset, byteCount);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }
}
