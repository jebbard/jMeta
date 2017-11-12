/**
 *
 * {@link CachedStreamMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.impl.store;

import static com.github.jmeta.library.media.api.helper.MediaTestUtility.at;

import java.io.FileInputStream;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.InvalidMediumReferenceException;
import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.services.AbstractCachedMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.impl.mediumAccessor.InputStreamMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;

/**
 * {@link CachedStreamMediumStoreTest} tests a {@link MediumStore} backed by {@link InputStreamMedium} instances with a
 * cache.
 */
public class CachedStreamMediumStoreTest extends AbstractCachedMediumStoreTest<InputStreamMedium> {

   private static final String STREAM_BASED_FILLED_MEDIUM_NAME = "Stream based filled medium";
   private static final String STREAM_BASED_EMPTY_MEDIUM_NAME = "Stream based empty medium";

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledStreamMediumWithBigCache_offsetBiggerThanLastReadOffset_alsoCachesBytesUntilStartOffset() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeNotNull(mediumStoreUnderTest);

      mediumStoreUnderTest.open();

      int cacheStartOffsetPos = 20;
      MediumReference cacheStartOffset = at(currentMedium, cacheStartOffsetPos);
      int bytesToCache = 10;
      cacheNoEOMExpected(cacheStartOffset, bytesToCache);

      Assert.assertEquals(bytesToCache,
         mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, cacheStartOffsetPos)));
      Assert.assertEquals(bytesToCache + cacheStartOffsetPos,
         mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 0)));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledStreamMediumWithBigCache_multipleOverlappingAndDisconnectedRegions_cachesExpectedBytes() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 20), 10);
      cacheNoEOMExpected(at(currentMedium, 25), 100);
      cacheNoEOMExpected(at(currentMedium, 200), 35);

      Assert.assertEquals(213, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 20 + 2)));
      Assert.assertEquals(216, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 20 - 1)));
      Assert.assertEquals(110, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 25 + 100)));
      Assert.assertEquals(24, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 200 + 11)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 235)));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test(expected = InvalidMediumReferenceException.class)
   public void cache_forFilledStreamMediumWithSmallCache_offsetInPreviouslyFreedCacheRegion_throwsException() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      mediumStoreUnderTest.open();

      // Cache more bytes than max cache size such that regions cached at the beginning of the medium need to be freed
      // automatically
      cacheNoEOMExpected(at(currentMedium, 20), MAX_CACHE_SIZE_FOR_SMALL_CACHE + 100);

      cacheNoEOMExpected(at(currentMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumReference)} and
    * {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void getCachedByteCountAt_forFilledStreamMediumWithBigCache_priorCacheAndOffsetOutsideCachedRegion_returnsZero() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      int byteCountToCache = 10;

      MediumReference cacheOffset = at(currentMedium, 20);

      cacheNoEOMExpected(cacheOffset, byteCountToCache);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(byteCountToCache)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(byteCountToCache + 10)));
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_unCachedRange_returnsExpectedDataAccessesMediumTwiceAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 15;
      int getDataSize = 80;

      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      // Two read calls due to big read-write block size and stream medium
      verifyExactlyNReads(2);

      Assert.assertEquals(getDataSize + getDataStartOffset,
         mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 0)));
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_rangeBeforeCachedRange_returnsExpectedDataAndDoesNotAccessMediumAgain() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 100), 100);

      // One read to read up to start offset, second read for the explicit cache call
      verifyExactlyNReads(2);

      long getDataStartOffset = 15;
      int getDataSize = 80;

      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      // No additional reads, as everything was already cached
      verifyExactlyNReads(2);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test(expected = InvalidMediumReferenceException.class)
   public void getData_forFilledMediumWithSmallCache_offsetInPreviouslyFreedCacheRegion_throwsException() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      mediumStoreUnderTest.open();

      // Cache more bytes than max cache size such that regions cached at the beginning of the medium need to be freed
      // automatically
      cacheNoEOMExpected(at(currentMedium, 20), MAX_CACHE_SIZE_FOR_SMALL_CACHE + 100);

      getDataNoEOMExpected(at(currentMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledStreamMediumWithBigCache_referenceBehindEOM_throwsEndOfMediumException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = currentMediumContent.length() + 15;
      int getDataSize = 10;

      try {
         mediumStoreUnderTest.getData(at(currentMedium, getDataStartOffset), getDataSize);
         Assert.fail("Expected " + EndOfMediumException.class);
      } catch (EndOfMediumException e) {
         // as expected
      }
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
    */
   @Override
   protected InputStreamMedium createEmptyMedium(String testMethodName) throws IOException {
      return new InputStreamMedium(new FileInputStream(MediaTestFiles.EMPTY_TEST_FILE_PATH.toFile()),
         STREAM_BASED_EMPTY_MEDIUM_NAME);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createFilledMedium(java.lang.String,
    *      long, int)
    */
   @Override
   protected InputStreamMedium createFilledMedium(String testMethodName, long maxCacheSize, int maxReadWriteBlockSize) throws IOException {
      return new InputStreamMedium(new FileInputStream(MediaTestFiles.FIRST_TEST_FILE_PATH.toFile()),
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
         return MediaTestFiles.EMPTY_TEST_FILE_CONTENT;
      } else if (medium.getName().equals(STREAM_BASED_FILLED_MEDIUM_NAME)) {
         return MediaTestFiles.FIRST_TEST_FILE_CONTENT;
      }
      return "";
   }

}
