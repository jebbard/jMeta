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
   public void cache_forFilledMediumWithBigCache_fromStartTillBeyondEOM_throwsEOMException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);
      mediumStoreUnderTest.open();

      testCache_throwsEndOfMediumException(59, currentMediumContent.length() + 100, currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forEmptyMedium_fromStart_throwsEOMException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      testCache_throwsEndOfMediumException(0, 1, 0);
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledMediumWithSmallCache_fromStartAndMoreThanMaxCSize_cachesOnlyUpToMaxCSize() {
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
   public void cache_forFilledMediumWithBigCache_cacheWithinPreviousCache_doesNotRead() {
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
   public void cache_forFilledMediumWithSmallCache_cacheWithinPreviousCache_doesNotRead() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      mediumStoreUnderTest.open();

      int cacheSize = 30;
      cacheNoEOMExpected(at(currentMedium, 0), cacheSize);
      cacheNoEOMExpected(at(currentMedium, 2), 10);

      verifyExactlyNReads(cacheSize / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE);
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledMediumWithSmallCache_fromStartMoreThanMaxRWSize_readsBlockWiseAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      mediumStoreUnderTest.open();

      int expectedReadCount = 4;
      int cacheSize = (expectedReadCount - 1) * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1;
      MediumReference cacheOffset = at(currentMedium, 0);
      cacheNoEOMExpected(cacheOffset, cacheSize);

      verifyExactlyNReads(expectedReadCount);

      Assert.assertEquals(cacheSize, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledMediumWithBigCache_fromStartLessThanMaxRWSize_readsOnceAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      int cacheStartOffset = 500;
      MediumReference cacheOffset = at(currentMedium, 0);
      cacheNoEOMExpected(cacheOffset, cacheStartOffset);

      verifyExactlyNReads(1);

      Assert.assertEquals(cacheStartOffset, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledMediumWithBigCache_partlyCacheBeforeWithGaps_updatesCacheInGaps() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
      cacheNoEOMExpected(at(currentMedium, 30), 100);
      cacheNoEOMExpected(at(currentMedium, 135), 200);

      int cacheSize = 500;
      MediumReference cacheOffset = at(currentMedium, 0);
      cacheNoEOMExpected(cacheOffset, cacheSize);

      verifyExactlyNReads(3 + 4);

      Assert.assertEquals(cacheSize, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_partlyCacheBeforeWithGaps_updatesCacheInGaps() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
      cacheNoEOMExpected(at(currentMedium, 30), 100);
      cacheNoEOMExpected(at(currentMedium, 135), 200);

      long getDataStartOffset = 0;
      int getDataSize = 400;

      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);

      Assert.assertEquals(getDataSize,
         mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, getDataStartOffset)));

      verifyExactlyNReads(3 + 4);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_cacheBeforeAndRangeInSingleCachedRegion_doesNotReReadAndReturnsExpectedData() {
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
   public void getData_forFilledMediumWithSmallCache_cacheWithMultipleRegionsBeforeAndStartBehindFirstRegionStart_doesNotReReadAndReturnsExpectedData() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      int blocksRead = 10;
      cacheNoEOMExpected(at(currentMedium, 0), blocksRead * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE);

      verifyExactlyNReads(blocksRead);

      long getDataStartOffset = MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE - 1;
      int getDataSize = 8 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 2;

      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      // No further read access after the initial cache happened!
      verifyExactlyNReads(blocksRead);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_twiceInEnclosingRegion_doesNotReReadAndReturnsExpectedData() {
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
   public void getData_forFilledMediumWithSmallCache_twiceInEnclosingRegion_doesNotReReadAndReturnsExpectedData() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      int getDataSize = 200;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
      // Read again in range fully enclosed by first read
      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset).advance(5), getDataSize - 8,
         currentMediumContent);

      // Still only one read done
      verifyExactlyNReads(getDataSize / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_fromStartAndNoCacheBefore_returnsExpectedDataAndReadsOnceAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      int getDataSize = 300;

      MediumReference getDataStartReference = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataStartReference, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataStartReference));

      verifyExactlyNReads(1);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithSmallCache_fromStartAndNoCacheBefore_returnsExpectedDataAndReadsBlockWiseAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      int getDataSize = 3 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 4;

      MediumReference getDataStartReference = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataStartReference, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataStartReference));

      verifyExactlyNReads(4);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithSmallCache_noCacheBeforeAndMoreThanMaxCSize_returnsExpectedDataButCachesOnlyLastBlocks() {
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

   protected void testCache_throwsEndOfMediumException(long cacheOffset, int cacheSize, int mediumTotalSize) {
      MediumReference cacheReference = at(currentMedium, cacheOffset);
      try {
         mediumStoreUnderTest.cache(cacheReference, cacheSize);

         Assert.fail("Expected end of medium exception, but it did not occur!");
      }

      catch (EndOfMediumException e) {
         Assert.assertEquals(cacheReference, e.getMediumReference());
         Assert.assertEquals(cacheSize, e.getByteCountTriedToRead());
         Assert.assertEquals(mediumTotalSize - cacheReference.getAbsoluteMediumOffset(), e.getBytesReallyRead());
      }
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createDefaultFilledMediumStore()
    */
   @Override
   protected MediumStore createDefaultFilledMediumStore() {
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
