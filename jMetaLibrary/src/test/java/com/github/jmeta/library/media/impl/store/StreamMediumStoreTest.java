/**
 *
 * {@link StreamMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.impl.store;

import static com.github.jmeta.library.media.api.helper.TestMedia.at;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.InvalidMediumOffsetException;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.services.AbstractMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.impl.mediumAccessor.InputStreamMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;

/**
 * {@link StreamMediumStoreTest} tests a {@link MediumStore} backed by {@link InputStreamMedium} instances with a
 * cache.
 */
public class StreamMediumStoreTest extends AbstractMediumStoreTest<InputStreamMedium> {

   private static final String STREAM_BASED_FILLED_MEDIUM_NAME = "Stream based filled medium";
   private static final String STREAM_BASED_EMPTY_MEDIUM_NAME = "Stream based empty medium";

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumOffset)} and {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void getCachedByteCountAt_forFilledStreamMediumWithBigCache_priorCacheAndOffsetOutsideCachedRegion_returnsZero() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      int byteCountToCache = 10;

      MediumOffset cacheOffset = at(currentMedium, 20);

      cacheNoEOMExpected(cacheOffset, byteCountToCache);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(byteCountToCache)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(byteCountToCache + 10)));
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledStreamMediumWithBigCache_fromMiddle_cachesUpToStartOffset() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset mediumStartOffset = at(currentMedium, 0);
      MediumOffset cacheOffset = at(currentMedium, 20);
      int cacheSize = 10;

      cacheNoEOMExpected(cacheOffset, cacheSize);

      Assert.assertEquals(cacheSize, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));
      Assert.assertEquals(cacheSize + cacheOffset.getAbsoluteMediumOffset(),
         mediumStoreUnderTest.getCachedByteCountAt(mediumStartOffset));

      assertRangeIsCachedFromExternalMedium(mediumStartOffset, (int) cacheOffset.getAbsoluteMediumOffset(),
         currentMediumContent);
      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledStreamMediumWithBigCache_multipleOverlappingAndDisconnectedRegions_cachesExpectedBytes() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset firstCacheOffset = at(currentMedium, 20);
      int firstCacheSize = 10;
      MediumOffset secondCacheOffset = at(currentMedium, 25);
      int secondCacheSize = 100;
      MediumOffset thirdCacheOffset = at(currentMedium, 200);
      int thirdCacheSize = 35;

      cacheNoEOMExpected(firstCacheOffset, firstCacheSize);
      cacheNoEOMExpected(secondCacheOffset, secondCacheSize);
      cacheNoEOMExpected(thirdCacheOffset, thirdCacheSize);

      Assert.assertEquals(213, mediumStoreUnderTest.getCachedByteCountAt(firstCacheOffset.advance(2)));
      Assert.assertEquals(216, mediumStoreUnderTest.getCachedByteCountAt(firstCacheOffset.advance(-1)));
      Assert.assertEquals(110, mediumStoreUnderTest.getCachedByteCountAt(secondCacheOffset.advance(secondCacheSize)));
      Assert.assertEquals(24, mediumStoreUnderTest.getCachedByteCountAt(thirdCacheOffset.advance(11)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(thirdCacheOffset.advance(thirdCacheSize)));

      assertRangeIsCachedFromExternalMedium(at(currentMedium, 0),
         (int) thirdCacheOffset.getAbsoluteMediumOffset() + thirdCacheSize, currentMediumContent);
      assertRangeIsNotCached(thirdCacheOffset.advance(thirdCacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledStreamMediumWithSmallCache_middleOffset_readsBlockWiseUpToStartOffsetAndCachesOnlyLastRegions() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      // Cache more bytes than max cache size such that regions cached at the beginning of the medium need to be freed
      // automatically
      MediumOffset cacheOffset = at(currentMedium, 20);
      int expectedActualCacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE;
      int cacheSize = expectedActualCacheSize + 100;

      MediumOffset expectedActualCacheStartOffset = at(currentMedium, 120);

      cacheNoEOMExpected(cacheOffset, cacheSize);

      verifyExactlyNReads(
         (int) (cacheOffset.getAbsoluteMediumOffset() + cacheSize) / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 0)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 119)));
      Assert.assertEquals(expectedActualCacheSize,
         mediumStoreUnderTest.getCachedByteCountAt(expectedActualCacheStartOffset));

      assertRangeIsNotCached(at(currentMedium, 0), (int) expectedActualCacheStartOffset.getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(expectedActualCacheStartOffset, expectedActualCacheSize,
         currentMediumContent);
      assertRangeIsNotCached(expectedActualCacheStartOffset.advance(expectedActualCacheSize),
         currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test(expected = InvalidMediumOffsetException.class)
   public void cache_forFilledStreamMediumWithSmallCache_offsetInPreviouslyFreedCacheRegion_throwsException() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      mediumStoreUnderTest.open();

      // Cache more bytes than max cache size such that regions cached at the beginning of the medium need to be freed
      // automatically
      cacheNoEOMExpected(at(currentMedium, 20), MAX_CACHE_SIZE_FOR_SMALL_CACHE + 100);

      cacheNoEOMExpected(at(currentMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test(expected = InvalidMediumOffsetException.class)
   public void getData_forFilledStreamMediumWithSmallCache_offsetInPreviouslyFreedCacheRegion_throwsException() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      mediumStoreUnderTest.open();

      // Cache more bytes than max cache size such that regions cached at the beginning of the medium need to be freed
      // automatically
      cacheNoEOMExpected(at(currentMedium, 20), MAX_CACHE_SIZE_FOR_SMALL_CACHE + 100);

      getDataNoEOMExpected(at(currentMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledStreamMediumWithBigCache_forOffsetBeyondEOM_throwsEOMException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = currentMediumContent.length() + 15;
      int getDataSize = 10;

      try {
         mediumStoreUnderTest.getData(at(currentMedium, getDataStartOffset), getDataSize);
         Assert.fail("Expected " + EndOfMediumException.class);
      } catch (EndOfMediumException e) {
         Assert.assertEquals(at(currentMedium, 0), e.getReadStartReference());
         Assert.assertEquals(getDataStartOffset, e.getByteCountTriedToRead());
         Assert.assertEquals(currentMediumContent.length(), e.getByteCountActuallyRead());

         assertByteBufferMatchesMediumRange(e.getBytesReadSoFar(), e.getReadStartReference(),
            e.getByteCountActuallyRead(), currentMediumContent);
      }

      assertRangeIsCachedFromExternalMedium(at(currentMedium, 0), currentMediumContent.length(), currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_unCachedRange_returnsExpectedDataAccessesMediumTwiceAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset getDataOffset = at(currentMedium, (long) 15);
      int getDataSize = 80;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      // Two read calls due to big read-write block size and stream medium
      verifyExactlyNReads(2);

      Assert.assertEquals(getDataSize + getDataOffset.getAbsoluteMediumOffset(),
         mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 0)));

      assertRangeIsCachedFromExternalMedium(at(currentMedium, 0),
         (int) getDataOffset.getAbsoluteMediumOffset() + getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_rangeBeforeCachedRange_returnsExpectedDataAndDoesNotAccessMediumAgain() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, 100);
      int cacheSize = 100;

      cacheNoEOMExpected(cacheOffset, cacheSize);

      // One read to read up to start offset, second read for the explicit cache call
      verifyExactlyNReads(2);

      long getDataStartOffset = 15;
      int getDataSize = 80;

      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      // No additional reads, as everything was already cached
      verifyExactlyNReads(2);

      assertRangeIsCachedFromExternalMedium(at(currentMedium, 0),
         (int) cacheOffset.getAbsoluteMediumOffset() + cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
    */
   @Override
   protected InputStreamMedium createEmptyMedium(String testMethodName) throws IOException {
      return new InputStreamMedium(new FileInputStream(TestMedia.EMPTY_TEST_FILE_PATH.toFile()),
         STREAM_BASED_EMPTY_MEDIUM_NAME);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createFilledMedium(java.lang.String,
    *      long, int)
    */
   @Override
   protected InputStreamMedium createFilledMedium(String testMethodName, long maxCacheSize, int maxReadWriteBlockSize)
      throws IOException {
      return new InputStreamMedium(new FileInputStream(TestMedia.FIRST_TEST_FILE_PATH.toFile()),
         STREAM_BASED_FILLED_MEDIUM_NAME, maxCacheSize, maxReadWriteBlockSize);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createMediumAccessor(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected MediumAccessor<InputStreamMedium> createMediumAccessor(InputStreamMedium mediumToUse) {
      return new InputStreamMediumAccessor(mediumToUse);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#getMediumContentAsString(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected String getMediumContentAsString(InputStreamMedium medium) {
      if (medium.getName().equals(STREAM_BASED_EMPTY_MEDIUM_NAME)) {
         return TestMedia.EMPTY_TEST_FILE_CONTENT;
      } else if (medium.getName().equals(STREAM_BASED_FILLED_MEDIUM_NAME)) {
         return TestMedia.FIRST_TEST_FILE_CONTENT;
      }
      return "";
   }

}
