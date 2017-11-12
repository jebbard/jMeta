/**
 *
 * {@link AbstractCachedMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 08.11.2017
 *
 */
package com.github.jmeta.library.media.api.services;

import static com.github.jmeta.library.media.api.helper.MediaTestUtility.at;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractCachedMediumStoreTest} contains all test methods that need to operate on a cached {@link Medium}.
 */
public abstract class AbstractCachedMediumStoreTest<T extends Medium<?>> extends AbstractMediumStoreTest<T> {

   protected static final int MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE = 5;
   protected static final int MAX_CACHE_SIZE_FOR_SMALL_CACHE = MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() / 2;

   /**
    * Tests {@link MediumStore#close()}.
    */
   @Test
   public void close_onMediumStoreWithCachedContent_freesResources() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 0), 10);

      mediumStoreUnderTest.close();

      Mockito.verify(mediumCacheSpy).clear();
      Mockito.verify(mediumAccessorSpy).close();
      Mockito.verify(mediumReferenceFactorySpy).clear();
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forFilledMediumAndCacheUntilEOM_returnsTrue() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      int mediumSizeInBytes = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      // Read all bytes until EOM such that even for streams, we are at end of medium
      cacheNoEOMExpected(at(currentMedium, 0), mediumSizeInBytes);

      Assert.assertTrue(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, mediumSizeInBytes)));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumReference)}.
    */
   @Test
   public void getCachedByteCountAt_forFilledMediumWithBigCache_noPriorCache_returnsZero() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 10)));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumReference)} and
    * {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void getCachedByteCountAt_forFilledMediumWithBigCache_priorCacheAndOffsetInCachedRegion_returnsExpectedCount() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      int byteCountToCache = 10;

      MediumReference cacheOffset = at(currentMedium, 20);

      cacheNoEOMExpected(cacheOffset, byteCountToCache);

      Assert.assertEquals(byteCountToCache - 2, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(2)));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_untilEOM_throwsEndOfMediumException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      int mediumSizeInBytes = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      MediumReference cacheReference = at(currentMedium, 59);

      try {
         mediumStoreUnderTest.cache(cacheReference, mediumSizeInBytes + 100);

         Assert.fail("Expected end of medium exception, but it did not occur!");
      }

      catch (EndOfMediumException e) {
         Assert.assertEquals(cacheReference, e.getMediumReference());
         Assert.assertEquals(mediumSizeInBytes + 100, e.getByteCountTriedToRead());
         Assert.assertEquals(mediumSizeInBytes - cacheReference.getAbsoluteMediumOffset(), e.getBytesReallyRead());
      }
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledMediumWithBigCache_untilEOM_throwsEndOfMediumException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      int mediumSizeInBytes = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      MediumReference cacheReference = at(currentMedium, 0);

      try {
         mediumStoreUnderTest.cache(cacheReference, mediumSizeInBytes + 100);

         Assert.fail("Expected end of medium exception, but it did not occur!");
      }

      catch (EndOfMediumException e) {
         Assert.assertEquals(cacheReference, e.getMediumReference());
         Assert.assertEquals(mediumSizeInBytes + 100, e.getByteCountTriedToRead());
         Assert.assertEquals(mediumSizeInBytes, e.getBytesReallyRead());
      }
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledMediumWithSmallCache_moreThanMaxCacheSize_cachesOnlyUpToMaxCacheSize() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      int mediumSizeInBytes = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      MediumReference cacheOffset = at(currentMedium, 20);
      cacheNoEOMExpected(cacheOffset, mediumSizeInBytes - 21);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));
      Assert.assertEquals(
         MAX_CACHE_SIZE_FOR_SMALL_CACHE - MAX_CACHE_SIZE_FOR_SMALL_CACHE % MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE,
         mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 595)));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledMediumWithBigCache_cacheWithinPreviousCache_doesNotReadAgain() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 0), 30);
      cacheNoEOMExpected(at(currentMedium, 2), 10);

      verifyExactlyNReads(1);
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledMediumWithSmallCache_readsBlockWise() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      mediumStoreUnderTest.open();

      int expectedReadCount = 4;
      int byteCount = (expectedReadCount - 1) * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1;
      cacheNoEOMExpected(at(currentMedium, 0), byteCount);

      verifyExactlyNReads(expectedReadCount);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_alreadyCachedRangeWithGaps_returnsExpectedDataAndUpdatesCacheInGaps() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
      cacheNoEOMExpected(at(currentMedium, 30), 100);
      cacheNoEOMExpected(at(currentMedium, 135), 200);

      long getDataStartOffset = 5;
      int getDataSize = 400;

      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_returnsExpectedDataAndReadsBlockWise() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      int getDataSize = 3 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 4;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);

      verifyExactlyNReads(4);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_twiceInEnclosingRegion_doesNotReRead() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      int getDataSize = 200;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
      // Read again in range fully enclosed by first read
      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset).advance(5), getDataSize - 8,
         currentMediumContent);

      // Still only one read done
      verifyExactlyNReads(1);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_untilEOM_throwsEndOfMediumException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 15;
      int getDataSize = currentMediumContent.length();

      testGetData_forFullRangeRead_throwsEndOfMediumException(getDataStartOffset, getDataSize,
         currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_alreadyCachedRange_returnsExpectedDataAndDoesNotAccessMedium() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 0), 100);

      verifyExactlyNReads(1);

      long getDataStartOffset = 15;
      int getDataSize = 80;

      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      // No further read access after the initial cache happened!
      verifyExactlyNReads(1);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithSmallCache_moreDataThanMaxCacheSize_returnsExpectedDataButCachesOnlyLastBlocks() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 20;
      int getDataSize = currentMediumContent.length() - 21;

      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));
      Assert.assertEquals(
         MAX_CACHE_SIZE_FOR_SMALL_CACHE - MAX_CACHE_SIZE_FOR_SMALL_CACHE % MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE,
         mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 595)));
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createDafaulFilledMediumStore()
    */
   @Override
   protected MediumStore createDafaulFilledMediumStore() {
      return createFilledMediumStoreWithBigCache();
   }

   /**
    * Creates a {@link MediumStore} based on a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}
    * as content, backed by a cache, where the cache is smaller than the overall {@link Medium} size, in detail, it has
    * a size of {@link #MAX_CACHE_SIZE_FOR_SMALL_CACHE}. In line with that, the maximum read write block size is set to
    * {@link #MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE}. This method must be called at the beginning of a test case to
    * create the {@link MediumStore} to test and its return value must be assigned to {@link #mediumStoreUnderTest}. It
    * is used for testing cases where data is read into the cache but then automatically purged due to the limited cache
    * size.
    * 
    * @return a {@link MediumStore} based on a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}
    *         as content, backed by a small cache, or null if the current implementation does not support this
    */
   protected MediumStore createFilledMediumStoreWithSmallCache() {
      try {
         currentMedium = createFilledMedium(testName.getMethodName(), MAX_CACHE_SIZE_FOR_SMALL_CACHE,
            MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE);

         return createMediumStoreToTest(currentMedium);
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }

   /**
    * Creates a {@link MediumStore} based on a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}
    * as content, backed by a cache, where the cache is (much) bigger than the overall {@link Medium} size. The
    * read-write block size is set to same value (to ensure only single reads during the test cases when testing
    * {@link MediumStore#cache(MediumReference, int)}) and an also quite big max cache region size. This method must be
    * called at the beginning of a test case to create the {@link MediumStore} to test and its return value must be
    * assigned to {@link #mediumStoreUnderTest}. It is used for testing cases where data read is expected to always end
    * up in the cache due to its big size.
    * 
    * @return a {@link MediumStore} based on a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}
    *         as content, backed by a big cache, or null if the current implementation does not support this
    */
   protected MediumStore createFilledMediumStoreWithBigCache() {
      try {
         currentMedium = createFilledMedium(testName.getMethodName(),
            MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() + 1000,
            MediaTestFiles.FIRST_TEST_FILE_CONTENT.length() + 1000);

         return createMediumStoreToTest(currentMedium);
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }

}
