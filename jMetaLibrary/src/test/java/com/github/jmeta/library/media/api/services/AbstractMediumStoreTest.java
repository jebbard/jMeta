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

import static com.github.jmeta.library.media.api.helper.TestMedia.at;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.cache.MediumCache;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManager;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.offset.MediumOffsetFactory;
import com.github.jmeta.library.media.impl.store.StandardMediumStore;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractMediumStoreTest} is the base class for testing the {@link MediumStore} interface. In contrast to
 * {@link AbstractReadOnlyMediumStoreTest}, it uses also writable media, and it does not contain the default test cases
 * for {@link MediumStore#open()}, {@link MediumStore#close()}, {@link MediumStore#getMedium()} and
 * {@link MediumStore#createMediumOffset(long)}, these can be found in {@link AbstractReadOnlyMediumStoreTest}.
 *
 * Each subclass corresponds to a specific {@link Medium} type, a read-only or writable {@link Medium} instance, as well
 * as a random-access or non-random-access medium. The sub-class hierarchy is as follows:
 * <ul>
 * <li>{@link AbstractMediumStoreTest}: Contains all tests that should run for any medium type and access combinations,
 * thus e.g. all general negative tests (closed media and wrong reference)</li>
 * <li>{@link AbstractWritableRandomAccessMediumStoreTest}: Tests based on a writable random-access medium; so all write
 * test cases go here</li>
 * </ul>
 *
 * The filled media used for testing all must contain {@link TestMedia#FIRST_TEST_FILE_CONTENT}, a string fully
 * containing only human-readable US-ASCII characters, and must be UTF-8 encoded. This guarantees that 1 byte = 1
 * character. Furthermore, all bytes inserted must also be human-readable US-ASCII characters with this property.
 *
 * There are specific naming conventions for testing {@link MediumStore#getData(MediumOffset, int)} and
 * {@link MediumStore#cache(MediumOffset, int)}: [method name]_[medium type]_[parameter values, esp. offset
 * range]_[expected behaviour].
 *
 * @param <T>
 *           The type of {@link Medium} to use
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractMediumStoreTest<T extends Medium<?>> {

   /**
    * For getting the current test case's name, must be public
    */
   @Rule
   public TestName testName = new TestName();

   protected MediumStore mediumStoreUnderTest;

   protected T currentMedium;

   protected MediumAccessor<T> mediumAccessorSpy;

   protected MediumCache mediumCacheSpy;

   protected MediumOffsetFactory mediumReferenceFactorySpy;

   protected MediumChangeManager mediumChangeManagerSpy;

   /**
    * Validates all test files needed in this test class
    */
   @BeforeClass
   public static void validateTestFiles() {
      TestMedia.validateTestFiles();
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
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void cache_forClosedMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void cache_forReferencePointingToWrongMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(TestMedia.OTHER_MEDIUM, 10), 10);
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forEmptyMedium_fromStart_throwsEOMException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      testCache_throwsEndOfMediumException(at(currentMedium, 0), 1, currentMediumContent);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledMediumWithBigCache_cacheWithinPreviousCache_doesNotRead() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, 0);
      int cacheSize = 30;
      cacheNoEOMExpected(cacheOffset, cacheSize);
      cacheNoEOMExpected(at(currentMedium, 2), 10);

      verifyExactlyNReads(1);

      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledMediumWithBigCache_fromStartLessThanMaxRWSize_readsOnceAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      int cacheSize = 500;
      MediumOffset cacheOffset = at(currentMedium, 0);
      cacheNoEOMExpected(cacheOffset, cacheSize);

      verifyExactlyNReads(1);

      Assert.assertEquals(cacheSize, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));

      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledMediumWithBigCache_fromStartTillBeyondEOM_throwsEOMException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, 59);

      testCache_throwsEndOfMediumException(cacheOffset, currentMediumContent.length() + 100, currentMediumContent);

      assertRangeIsCachedFromExternalMedium(cacheOffset,
         currentMediumContent.length() - (int) cacheOffset.getAbsoluteMediumOffset(), currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledMediumWithBigCache_partlyCacheBeforeWithGaps_updatesCacheInGaps() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
      cacheNoEOMExpected(at(currentMedium, 30), 100);
      cacheNoEOMExpected(at(currentMedium, 135), 200);

      int cacheSize = 500;
      MediumOffset cacheOffset = at(currentMedium, 0);
      cacheNoEOMExpected(cacheOffset, cacheSize);

      verifyExactlyNReads(3 + 4);

      Assert.assertEquals(cacheSize, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));

      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledMediumWithSmallCache_cacheWithinPreviousCache_doesNotRead() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, 0);
      int cacheSize = 30;

      cacheNoEOMExpected(cacheOffset, cacheSize);
      cacheNoEOMExpected(at(currentMedium, 2), 10);

      verifyExactlyNReads(cacheSize / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE);

      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledMediumWithSmallCache_fromStartAndMoreThanMaxCSize_cachesOnlyUpToMaxCSize() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, 20);
      cacheNoEOMExpected(cacheOffset, currentMediumContent.length() - 21);

      MediumOffset expectedActualCacheStartOffset = at(currentMedium, 595);
      int expectedCachedByteCount = MAX_CACHE_SIZE_FOR_SMALL_CACHE
         - MAX_CACHE_SIZE_FOR_SMALL_CACHE % MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));
      Assert.assertEquals(expectedCachedByteCount,
         mediumStoreUnderTest.getCachedByteCountAt(expectedActualCacheStartOffset));

      assertRangeIsNotCached(at(currentMedium, 0), (int) expectedActualCacheStartOffset.getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(expectedActualCacheStartOffset, expectedCachedByteCount,
         currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledMediumWithSmallCache_fromStartMoreThanMaxRWSize_readsBlockWiseAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      int expectedReadCount = 4;
      int cacheSize = (expectedReadCount - 1) * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1;
      MediumOffset cacheOffset = at(currentMedium, 0);
      cacheNoEOMExpected(cacheOffset, cacheSize);

      verifyExactlyNReads(expectedReadCount);

      Assert.assertEquals(cacheSize, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));

      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

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
    * Tests {@link MediumStore#getCachedByteCountAt(MediumOffset)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void getCachedByteCountAt_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 0)));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getCachedByteCountAt_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.getCachedByteCountAt(at(TestMedia.OTHER_MEDIUM, 10));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumOffset)}.
    */
   @Test
   public void getCachedByteCountAt_forFilledMediumWithBigCache_noPriorCache_returnsZero() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 10)));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumOffset)} and {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void getCachedByteCountAt_forFilledMediumWithBigCache_priorCacheAndOffsetInCachedRegion_returnsExpectedCount() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      int byteCountToCache = 10;

      MediumOffset cacheOffset = at(currentMedium, 20);

      cacheNoEOMExpected(cacheOffset, byteCountToCache);

      Assert.assertEquals(byteCountToCache - 2, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(2)));
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMedium_noCacheBefore_returnsExpectedData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 5;
      int getDataSize = 400;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMedium_partlyCacheBeforeWithGaps_returnsExpectedData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
      cacheNoEOMExpected(at(currentMedium, 30), 100);
      cacheNoEOMExpected(at(currentMedium, 135), 200);

      long getDataStartOffset = 5;
      int getDataSize = 400;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMedium_fullMediumCacheBefore_returnsExpectedData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 0), currentMediumContent.length());

      long getDataStartOffset = 5;
      int getDataSize = 400;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forEmptyMedium_fromStart_throwsEOMException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      testGetData_throwsEndOfMediumException(at(currentMedium, 0), 1, currentMedium.getMaxReadWriteBlockSizeInBytes(),
         currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMedium_fromMiddleToBeyondEOMAndNoCacheBefore_throwsEOMException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      testGetData_throwsEndOfMediumException(at(currentMedium, 15), currentMediumContent.length(),
         currentMedium.getMaxReadWriteBlockSizeInBytes(), currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void getData_forClosedMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      getDataNoEOMExpected(at(currentMedium, 10), 10);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getData_forReferencePointingToWrongMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      getDataNoEOMExpected(at(TestMedia.OTHER_MEDIUM, 10), 10);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_cacheBeforeAndRangeInSingleCachedRegion_doesNotReReadAndReturnsExpectedData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, 0);
      int cacheSize = 100;

      cacheNoEOMExpected(cacheOffset, cacheSize);

      verifyExactlyNReads(1);

      long getDataStartOffset = 15;
      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 80;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      // No further read access after the initial cache happened!
      verifyExactlyNReads(1);

      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_fromStartAndNoCacheBefore_returnsExpectedDataAndReadsOnceAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 300;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      verifyExactlyNReads(1);

      assertRangeIsCachedFromExternalMedium(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_partlyCacheBeforeWithGaps_updatesCacheInGaps() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
      cacheNoEOMExpected(at(currentMedium, 30), 100);
      cacheNoEOMExpected(at(currentMedium, 135), 200);

      long getDataStartOffset = 0;
      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 400;

      getDataNoEOMExpected(getDataOffset, getDataSize);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      verifyExactlyNReads(3 + 4);

      assertRangeIsCachedFromExternalMedium(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_twiceInEnclosingRegion_doesNotReReadAndReturnsExpectedData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 200;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);
      // Read again in range fully enclosed by first read
      testGetData_returnsExpectedData(getDataOffset.advance(5), getDataSize - 8, currentMediumContent);

      // Still only one read done
      verifyExactlyNReads(1);

      assertRangeIsCachedFromExternalMedium(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMediumWithSmallCache_cacheWithMultipleRegionsBeforeAndStartBehindFirstRegionStart_doesNotReReadAndReturnsExpectedData() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      int blocksRead = 10;
      MediumOffset cacheOffset = at(currentMedium, 0);
      int cacheSize = blocksRead * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;
      cacheNoEOMExpected(cacheOffset, cacheSize);

      verifyExactlyNReads(blocksRead);

      long getDataStartOffset = MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE - 1;
      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 8 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 2;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      // No further read access after the initial cache happened!
      verifyExactlyNReads(blocksRead);

      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMediumWithSmallCache_fromStartAndNoCacheBefore_returnsExpectedDataAndReadsBlockWiseAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 3 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 4;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      verifyExactlyNReads(4);

      assertRangeIsCachedFromExternalMedium(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMediumWithSmallCache_noCacheBeforeAndMoreThanMaxCSize_returnsExpectedDataButCachesOnlyLastBlocks() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 20;
      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = currentMediumContent.length() - 21;

      MediumOffset expectedActualCacheStartOffset = at(currentMedium, 595);
      int expectedCachedByteCount = MAX_CACHE_SIZE_FOR_SMALL_CACHE
         - MAX_CACHE_SIZE_FOR_SMALL_CACHE % MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));
      Assert.assertEquals(expectedCachedByteCount,
         mediumStoreUnderTest.getCachedByteCountAt(expectedActualCacheStartOffset));

      assertRangeIsCachedFromExternalMedium(expectedActualCacheStartOffset, expectedCachedByteCount,
         currentMediumContent);
      assertRangeIsNotCached(expectedActualCacheStartOffset.advance(expectedCachedByteCount),
         currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMediumWithSmallCache_twiceInEnclosingRegion_doesNotReReadAndReturnsExpectedData() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 200;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);
      // Read again in range fully enclosed by first read
      testGetData_returnsExpectedData(getDataOffset.advance(5), getDataSize - 8, currentMediumContent);

      // Still only one read done
      verifyExactlyNReads(getDataSize / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE);

      assertRangeIsCachedFromExternalMedium(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void isAtEndOfMedium_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, 10));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void isAtEndOfMedium_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.isAtEndOfMedium(at(TestMedia.OTHER_MEDIUM, 10));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test
   public void isAtEndOfMedium_forFilledMediumBeforeEOM_returnsFalse() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      int endOfMediumOffset = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      Assert.assertFalse(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, endOfMediumOffset / 2)));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
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

   protected static final int MAX_CACHE_SIZE_FOR_SMALL_CACHE = TestMedia.FIRST_TEST_FILE_CONTENT.length() / 2;

   protected static final int MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE = 5;

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
    * Creates a {@link Medium} containing {@link TestMedia#FIRST_TEST_FILE_CONTENT} as content, backed or not backed by
    * a cache with the given maximum cache and cache region size as well as the given maximum read write block size.
    * Implementing test classes who's medium type does not support caching must return null.
    *
    * For writable media, be sure to only return a copy of the original medium such that the original medium is not
    * modified by writing tests and all tests remain repeatable.
    *
    * @param testMethodName
    *           the name of the current test method, can be used to create a copy of the original medium with the given
    *           name
    * @param maxCacheSize
    *           the maximum cache size in bytes
    * @param maxReadWriteBlockSize
    *           the maximum read write block size in bytes
    * @return a {@link Medium} containing {@link TestMedia#FIRST_TEST_FILE_CONTENT} as content with the given
    *         configuration parameters
    *
    * @throws IOException
    *            In case of any errors creating the {@link Medium}
    */
   protected abstract T createFilledMedium(String testMethodName, long maxCacheSize, int maxReadWriteBlockSize)
      throws IOException;

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
    * @param medium
    *           The medium to use
    *
    * @return the current content of the filled {@link Medium}
    */
   protected abstract String getMediumContentAsString(T medium);

   /**
    * Creates a test class implementation specific {@link MediumAccessor} to use for testing.
    *
    * @param mediumToUse
    *           The {@link Medium} to use for the {@link MediumStore}.
    * @return a {@link MediumAccessor} to use based on a given {@link Medium}.
    */
   protected abstract MediumAccessor<T> createMediumAccessor(T mediumToUse);

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
    * Creates a {@link MediumStore} based on a {@link Medium} containing {@link TestMedia#FIRST_TEST_FILE_CONTENT} as
    * content, backed by a cache, where the cache is (much) bigger than the overall {@link Medium} size. The read-write
    * block size is set to same value (to ensure only single reads during the test cases when testing
    * {@link MediumStore#cache(MediumOffset, int)}) and an also quite big max cache region size. This method must be
    * called at the beginning of a test case to create the {@link MediumStore} to test and its return value must be
    * assigned to {@link #mediumStoreUnderTest}. It is used for testing cases where data read is expected to always end
    * up in the cache due to its big size.
    *
    * @return a {@link MediumStore} based on a {@link Medium} containing {@link TestMedia#FIRST_TEST_FILE_CONTENT} as
    *         content, backed by a big cache, or null if the current implementation does not support this
    */
   protected MediumStore createFilledMediumStoreWithBigCache() {
      try {
         currentMedium = createFilledMedium(testName.getMethodName(), TestMedia.FIRST_TEST_FILE_CONTENT.length() + 1000,
            TestMedia.FIRST_TEST_FILE_CONTENT.length() + 1000);

         return createMediumStoreToTest(currentMedium);
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }

   /**
    * Creates a {@link MediumStore} based on a {@link Medium} containing {@link TestMedia#FIRST_TEST_FILE_CONTENT} as
    * content, backed by a cache, where the cache is smaller than the overall {@link Medium} size, in detail, it has a
    * size of {@link #MAX_CACHE_SIZE_FOR_SMALL_CACHE}. In line with that, the maximum read write block size is set to
    * {@link #MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE}. This method must be called at the beginning of a test case to
    * create the {@link MediumStore} to test and its return value must be assigned to {@link #mediumStoreUnderTest}. It
    * is used for testing cases where data is read into the cache but then automatically purged due to the limited cache
    * size.
    *
    * @return a {@link MediumStore} based on a {@link Medium} containing {@link TestMedia#FIRST_TEST_FILE_CONTENT} as
    *         content, backed by a small cache, or null if the current implementation does not support this
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
    * Tests {@link MediumStore#getData(MediumOffset, int)} by comparing its result with the expected medium content.
    *
    * @param getDataOffset
    *           The offset to use for the method call
    * @param getDataSize
    *           The size to use for the method call
    * @param currentMediumContent
    *           The current medium content used to get the expected data
    */
   protected void testGetData_returnsExpectedData(MediumOffset getDataOffset, int getDataSize,
      String currentMediumContent) {
      Reject.ifNull(currentMediumContent, "currentMediumContent");
      Reject.ifNull(getDataOffset, "getDataOffset");

      ByteBuffer returnedData = getDataNoEOMExpected(getDataOffset, getDataSize);

      assertByteBufferMatchesMediumRange(returnedData, getDataOffset, getDataSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)} to throw an end of medium exception when reaching it.
    *
    * @param getDataOffset
    *           The offset to use for the method call
    * @param getDataSize
    *           The size to use for the method call
    * @param chunkSizeToUse
    *           The size of the read-write block chunks
    * @param currentMediumContent
    *           The current content of the medium
    */
   protected void testGetData_throwsEndOfMediumException(MediumOffset getDataOffset, int getDataSize,
      int chunkSizeToUse, String currentMediumContent) {

      Reject.ifNull(currentMediumContent, "currentMediumContent");
      Reject.ifNull(getDataOffset, "getDataOffset");

      try {
         mediumStoreUnderTest.getData(getDataOffset, getDataSize);
         Assert.fail(EndOfMediumException.class + " expected, but was not thrown");
      } catch (EndOfMediumException e) {
         long getDataStartOffset = getDataOffset.getAbsoluteMediumOffset();

         MediumOffset expectedReadOffset = at(currentMedium, getDataStartOffset
            + chunkSizeToUse * (int) ((currentMediumContent.length() - getDataStartOffset) / chunkSizeToUse));

         long expectedByteCountActuallyRead = (currentMediumContent.length() - getDataStartOffset) % chunkSizeToUse;

         // For cases where the cache offset is beyond the medium end
         if (expectedByteCountActuallyRead < 0) {
            expectedByteCountActuallyRead = 0;
         }

         Assert.assertEquals(expectedReadOffset, e.getReadStartReference());
         Assert.assertEquals(getDataSize < chunkSizeToUse ? getDataSize : chunkSizeToUse, e.getByteCountTriedToRead());
         Assert.assertEquals(expectedByteCountActuallyRead, e.getByteCountActuallyRead());
         assertByteBufferMatchesMediumRange(e.getBytesReadSoFar(), expectedReadOffset, e.getByteCountActuallyRead(),
            currentMediumContent);
      }
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)} for throwing an {@link EndOfMediumException} as expected.
    *
    * @param cacheOffset
    *           The offset to start caching
    * @param cacheSize
    *           The size to cache
    * @param currentMediumContent
    *           The current content of the medium
    */
   protected void testCache_throwsEndOfMediumException(MediumOffset cacheOffset, int cacheSize,
      String currentMediumContent) {

      Reject.ifNull(currentMediumContent, "currentMediumContent");
      Reject.ifNull(cacheOffset, "cacheOffset");

      try {
         mediumStoreUnderTest.cache(cacheOffset, cacheSize);

         Assert.fail("Expected end of medium exception, but it did not occur!");
      }

      catch (EndOfMediumException e) {
         Assert.assertEquals(cacheOffset, e.getReadStartReference());
         Assert.assertEquals(cacheSize, e.getByteCountTriedToRead());
         long expectedByteCountActuallyRead = currentMediumContent.length() - cacheOffset.getAbsoluteMediumOffset();

         // For cases where the cache offset is beyond the medium end
         if (expectedByteCountActuallyRead < 0) {
            expectedByteCountActuallyRead = 0;
         }

         Assert.assertEquals(expectedByteCountActuallyRead, e.getByteCountActuallyRead());

         assertByteBufferMatchesMediumRange(e.getBytesReadSoFar(), cacheOffset, e.getByteCountActuallyRead(),
            currentMediumContent);
      }
   }

   /**
    * Calls {@link MediumStore#getData(MediumOffset, int)} and expects no end of medium.
    *
    * @param offset
    *           The offset to use
    * @param byteCount
    *           The number of bytes to cache
    *
    * @return The result data
    */
   protected ByteBuffer getDataNoEOMExpected(MediumOffset offset, int byteCount) {
      try {
         return mediumStoreUnderTest.getData(offset, byteCount);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Calls {@link MediumStore#cache(MediumOffset, int)} and expects no end of medium.
    *
    * @param offset
    *           The offset to use
    * @param byteCount
    *           The number of bytes to cache
    */
   protected void cacheNoEOMExpected(MediumOffset offset, int byteCount) {
      try {
         mediumStoreUnderTest.cache(offset, byteCount);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Verifies that there were exactly N calls to {@link MediumAccessor#read(ByteBuffer)} without
    * {@link EndOfMediumException}, no matter which parameters used.
    *
    * @param N
    *           The number of expected calls
    */
   protected void verifyExactlyNReads(int N) {
      try {
         Mockito.verify(mediumAccessorSpy, Mockito.times(N)).read(Mockito.anyInt());
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Verifies that there were exactly N calls to {@link MediumAccessor#write(ByteBuffer)} no matter which parameters
    * used.
    *
    * @param N
    *           The number of expected calls
    */
   protected void verifyExactlyNWrites(int N) {
      Mockito.verify(mediumAccessorSpy, Mockito.times(N)).write(Mockito.any());
   }

   /**
    * Returns the exact content of the cache in the given range as string, ensuring (by assertion) that the total range
    * is really actually cached.
    *
    * @param offset
    *           The start offset of the range
    * @param rangeSize
    *           The size of the range
    * @return A string representation of the total cache content in the range, with size rangeSize
    */
   protected String getCacheContentInRangeAsString(MediumOffset offset, int rangeSize) {
      List<MediumRegion> regions = mediumCacheSpy.getRegionsInRange(offset, rangeSize);

      ByteBuffer mergedBytes = ByteBuffer.allocate(rangeSize);

      for (MediumRegion mediumRegion : regions) {
         Assert.assertTrue(mediumRegion.isCached());

         mergedBytes.put(mediumRegion.getBytes());
      }

      mergedBytes.rewind();

      byte[] bufferBytes = new byte[rangeSize];

      mergedBytes.get(bufferBytes);

      String actualRangeString = new String(bufferBytes, Charsets.CHARSET_ASCII);
      return actualRangeString;
   }

   /**
    * Verifies that the medium cache is currently empty.
    */
   protected void assertCacheIsEmpty() {
      Assert.assertEquals(0, mediumCacheSpy.getAllCachedRegions().size());
      Assert.assertEquals(0, mediumCacheSpy.calculateCurrentCacheSizeInBytes());
   }

   /**
    * Checks whether the given {@link ByteBuffer} matches the medium content in the specified range
    *
    * @param returnedData
    *           The {@link ByteBuffer} to check
    * @param rangeStartOffset
    *           The start offset of the compared range
    * @param rangeSize
    *           The size of the compared range
    * @param currentMediumContent
    *           The current content of the {@link Medium}
    */
   protected void assertByteBufferMatchesMediumRange(ByteBuffer returnedData, MediumOffset rangeStartOffset,
      int rangeSize, String currentMediumContent) {

      Reject.ifNull(currentMediumContent, "currentMediumContent");
      Reject.ifNull(rangeStartOffset, "rangeStartOffset");
      Reject.ifNull(returnedData, "returnedData");

      Assert.assertEquals(rangeSize, returnedData.remaining());
      byte[] byteBufferData = new byte[rangeSize];
      returnedData.asReadOnlyBuffer().get(byteBufferData);

      String returnedDataAsString = new String(byteBufferData, Charsets.CHARSET_UTF8);

      String expectedReturnedData = rangeStartOffset.getAbsoluteMediumOffset() > currentMediumContent.length() ? ""
         : currentMediumContent.substring((int) rangeStartOffset.getAbsoluteMediumOffset(),
            (int) (rangeStartOffset.getAbsoluteMediumOffset() + rangeSize));
      Assert.assertEquals(expectedReturnedData, returnedDataAsString);
   }

   /**
    * Ensures the given range is actually fully cached, filled with content from the external medium in the given range.
    * It assumes that the first region in range returned exactly starts at the given range offset and the last region in
    * range returned from the cache exactly ends at the end of the range.
    *
    * @param rangeStartOffset
    *           The start {@link MediumOffset} of the range
    * @param rangeSize
    *           The size of the range
    * @param totalExpectedMediumContent
    *           The expected content of the whole medium as string
    */
   protected void assertRangeIsCachedFromExternalMedium(MediumOffset rangeStartOffset, int rangeSize,
      String totalExpectedMediumContent) {

      int startIndex = (int) rangeStartOffset.getAbsoluteMediumOffset();

      String expectedRangeString = totalExpectedMediumContent.substring(startIndex, startIndex + rangeSize);

      assertCacheContainsStringAt(rangeStartOffset, expectedRangeString);
   }

   /**
    * Ensures the given range is not cached.
    *
    * @param rangeStartOffset
    *           The start {@link MediumOffset} of the range
    * @param rangeSize
    *           The size of the range
    */
   protected void assertRangeIsNotCached(MediumOffset rangeStartOffset, int rangeSize) {
      List<MediumRegion> regions = mediumCacheSpy.getRegionsInRange(rangeStartOffset, rangeSize);

      for (MediumRegion mediumRegion : regions) {
         Assert.assertFalse(mediumRegion.isCached());
      }
   }

   /**
    * Ensures that the cache contains the given bytes (in string form) at the given offset.
    *
    * @param offset
    *           The start {@link MediumOffset} to check
    * @param expectedCacheContent
    *           The expected content of the cache at the given offset
    */
   private void assertCacheContainsStringAt(MediumOffset offset, String expectedCacheContent) {
      int rangeSize = expectedCacheContent.length();
      String actualRangeString = getCacheContentInRangeAsString(offset, rangeSize);

      Assert.assertEquals(expectedCacheContent, actualRangeString);
   }

   /**
    * Creates a {@link MediumStore} to test based on a given {@link Medium}.
    *
    * @param mediumToUse
    *           The {@link Medium} to use for the {@link MediumStore}.
    * @return a {@link MediumStore} to test based on a given {@link Medium}.
    */
   private MediumStore createMediumStoreToTest(T mediumToUse) {
      Reject.ifNull(mediumToUse, "mediumToUse");

      mediumAccessorSpy = Mockito.spy(createMediumAccessor(mediumToUse));

      int maxCacheRegionSize = 0;
      long maxCacheSize = 0;

      if (mediumToUse.requiresCaching()) {
         maxCacheSize = mediumToUse.getMaxCacheSizeInBytes();
         maxCacheRegionSize = mediumToUse.getMaxReadWriteBlockSizeInBytes();
      }

      mediumCacheSpy = Mockito.spy(new MediumCache(mediumToUse, maxCacheSize, maxCacheRegionSize));
      mediumReferenceFactorySpy = Mockito.spy(new MediumOffsetFactory(mediumToUse));
      mediumChangeManagerSpy = Mockito.spy(new MediumChangeManager(mediumReferenceFactorySpy));

      return new StandardMediumStore<>(mediumAccessorSpy, mediumCacheSpy, mediumReferenceFactorySpy,
         mediumChangeManagerSpy);
   }
}
