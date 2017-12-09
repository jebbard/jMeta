/**
 *
 * {@link AbstractCachedAndWritableRandomAccessMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.api.services;

import static com.github.jmeta.library.media.api.helper.MediaTestUtility.at;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.InvalidOverlappingWriteException;
import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link AbstractCachedAndWritableRandomAccessMediumStoreTest} tests the {@link MediumStore} interface for writable
 * random-access media. Thus it contains tests for all writing methods.
 *
 * @param <T>
 *           The concrete type of {@link Medium} to use
 */
public abstract class AbstractCachedAndWritableRandomAccessMediumStoreTest<T extends Medium<?>>
   extends AbstractCachedMediumStoreTest<T> {

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumOffset)} and {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void getCachedByteCountAt_forFilledRandomAccessMediumWithBigCache_priorCacheAndOffsetOutsideCachedRegion_returnsZero() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      int byteCountToCache = 10;

      MediumOffset cacheOffset = at(currentMedium, 20);

      cacheNoEOMExpected(cacheOffset, byteCountToCache);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(-5)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(byteCountToCache)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(byteCountToCache + 10)));
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithSmallCache_forAlreadyFreedRangeBeforeCurrentPosition_updatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, 10);
      int cacheSize = 20;

      cacheNoEOMExpected(cacheOffset, currentMediumContent.length() - 10);
      cacheNoEOMExpected(cacheOffset, cacheSize);

      Assert.assertEquals(cacheSize, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));

      int remainingCacheStartOffset = 595 + cacheSize;

      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize),
         remainingCacheStartOffset - (int) cacheOffset.getAbsoluteMediumOffset() - cacheSize);
      assertRangeIsCachedFromExternalMedium(at(currentMedium, remainingCacheStartOffset),
         currentMediumContent.length() - remainingCacheStartOffset, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithBigCache_inMiddle_doesNotReadOrCacheDataBefore() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, 20);
      int cacheSize = 10;
      cacheNoEOMExpected(cacheOffset, cacheSize);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 20 - 1)));

      assertRangeIsNotCached(at(currentMedium, 0), (int) cacheOffset.getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithBigCache_multipleOverlappingAndDisconnectedRegions_updatesCache() {
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

      Assert.assertEquals(103, mediumStoreUnderTest.getCachedByteCountAt(firstCacheOffset.advance(2)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(firstCacheOffset.advance(-1)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(secondCacheOffset.advance(secondCacheSize)));
      Assert.assertEquals(24, mediumStoreUnderTest.getCachedByteCountAt(thirdCacheOffset.advance(11)));

      assertRangeIsNotCached(at(currentMedium, 0), (int) firstCacheOffset.getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(firstCacheOffset, secondCacheSize + 5, currentMediumContent);
      assertRangeIsNotCached(secondCacheOffset.advance(secondCacheSize),
         (int) (thirdCacheOffset.getAbsoluteMediumOffset() - secondCacheOffset.getAbsoluteMediumOffset()
            - secondCacheSize));
      assertRangeIsCachedFromExternalMedium(thirdCacheOffset, thirdCacheSize, currentMediumContent);
      assertRangeIsNotCached(thirdCacheOffset.advance(thirdCacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithSmallCache_overlappingAlreadyFreedRangeAtFront_updatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset firstCacheOffset = at(currentMedium, 10);
      int firstCacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      MediumOffset secondCacheOffset = at(currentMedium, 5);
      int secondCacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(firstCacheOffset, firstCacheSize);

      verifyExactlyNReads(firstCacheSize / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1);

      cacheNoEOMExpected(secondCacheOffset, secondCacheSize);

      verifyExactlyNReads((firstCacheSize + 35) / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1);

      Assert.assertEquals(secondCacheSize, mediumStoreUnderTest.getCachedByteCountAt(secondCacheOffset));

      assertRangeIsNotCached(at(currentMedium, 0), (int) secondCacheOffset.getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(secondCacheOffset, secondCacheSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithBigCache_forOffsetBeyondEOM_throwsEOMException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, currentMediumContent.length() + 15);
      int cacheSize = 10;

      testCache_throwsEndOfMediumException(cacheOffset, cacheSize, currentMediumContent);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithSmallCache_withinAlreadyFreedRange_returnsExpectedDataAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      int cacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(at(currentMedium, 0), cacheSize);

      long getDataStartOffset = 10;
      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 20;

      MediumOffset expectedActualCacheStartOffset = at(currentMedium, 50);
      int expectedActualCacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE - getDataSize;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      Assert.assertEquals(expectedActualCacheSize,
         mediumStoreUnderTest.getCachedByteCountAt(expectedActualCacheStartOffset));

      verifyExactlyNReads(
         (MAX_CACHE_SIZE_FOR_SMALL_CACHE + getDataSize) / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);

      assertRangeIsNotCached(at(currentMedium, 0), (int) getDataOffset.getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize),
         (int) (expectedActualCacheStartOffset.getAbsoluteMediumOffset() - getDataOffset.getAbsoluteMediumOffset()
            - getDataSize));
      assertRangeIsCachedFromExternalMedium(expectedActualCacheStartOffset, expectedActualCacheSize,
         currentMediumContent);
      assertRangeIsNotCached(expectedActualCacheStartOffset.advance(expectedActualCacheSize),
         currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithSmallCache_overlappingAlreadyFreedRangeAtFront_returnsExpectedDataAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, 10);
      int cacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      MediumOffset getDataOffset = at(currentMedium, 5);
      int getDataSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(cacheOffset, cacheSize);

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      verifyExactlyNReads(
         (MAX_CACHE_SIZE_FOR_SMALL_CACHE + getDataSize) / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);

      assertRangeIsNotCached(at(currentMedium, 0), (int) getDataOffset.getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithSmallCache_overlappingAlreadyCachedRangeAtFront_returnsExpectedDataAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset getDataOffset = at(currentMedium, 15 + 7 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE);
      int getDataSize = 10;

      int cacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(at(currentMedium, 10), cacheSize);

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(583, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      verifyExactlyNReads(MAX_CACHE_SIZE_FOR_SMALL_CACHE / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);

      assertRangeIsNotCached(at(currentMedium, 0), (int) getDataOffset.advance(-10).getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(getDataOffset.advance(-10), MAX_CACHE_SIZE_FOR_SMALL_CACHE,
         currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(-10 + MAX_CACHE_SIZE_FOR_SMALL_CACHE),
         currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithBigCache_inMiddle_doesNotReadOrCacheDataBefore() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 20;
      MediumOffset getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 100;

      getDataNoEOMExpected(getDataOffset, getDataSize);

      verifyExactlyNReads(1);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 5)));

      assertRangeIsNotCached(at(currentMedium, 0), (int) getDataOffset.getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithBigCache_forOffsetBeyondEOM_throwsEOMException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset getDataOffset = at(currentMedium, (long) (currentMediumContent.length() + 15));
      int getDataSize = 10;

      testGetData_throwsEndOfMediumException(getDataOffset, getDataSize, currentMediumContent.length(),
         currentMediumContent);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#flush()} in terms of caching
    */
   @Test
   public void flush_forFilledRandomAccessMediumWithBigCache_variousChanges_nothingCachedBefore_cachesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      int startOffsetOfChanges = 4;

      mediumStoreUnderTest.open();

      int mediumSizeDelta = performAndCheckFlushForCachingTests(startOffsetOfChanges);

      assertRangeIsNotCached(at(currentMedium, 0), startOffsetOfChanges);

      String cacheContentAfterFlush = getCacheContentInRangeAsString(at(currentMedium, startOffsetOfChanges),
         currentMediumContent.length() - startOffsetOfChanges + mediumSizeDelta);

      mediumStoreUnderTest.close();

      String expectedMediumContent = new String(
         MediaTestUtility.readFileContent(createFlushExpectationPath("expectation_flush_caching.txt")),
         Charsets.CHARSET_UTF8);

      String actualMediumContent = getMediumContentAsString(currentMedium);

      Assert.assertEquals(expectedMediumContent, actualMediumContent);

      String mediumContentAfterFlush = getMediumContentAsString(currentMedium);

      String expectedCacheContent = mediumContentAfterFlush.substring(startOffsetOfChanges,
         mediumContentAfterFlush.length());

      Assert.assertEquals(expectedCacheContent, cacheContentAfterFlush);

      assertRangeIsNotCached(at(currentMedium, mediumContentAfterFlush.length()), 2000);
   }

   /**
    * Tests {@link MediumStore#flush()} in terms of caching
    */
   @Test
   public void flush_forFilledRandomAccessMediumWithBigCache_variousChanges_partlyCachedBefore_cachesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      int startOffsetOfChanges = 4;

      mediumStoreUnderTest.open();

      // Caching in some later removed and replaced regions, as well as some later "re-read" regions
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(5), 200);
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(255), 114);
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(600), 344);

      int mediumSizeDelta = performAndCheckFlushForCachingTests(startOffsetOfChanges);

      assertRangeIsNotCached(at(currentMedium, 0), startOffsetOfChanges);

      String cacheContentAfterFlush = getCacheContentInRangeAsString(at(currentMedium, startOffsetOfChanges),
         currentMediumContent.length() - startOffsetOfChanges + mediumSizeDelta);

      mediumStoreUnderTest.close();

      String expectedMediumContent = new String(
         MediaTestUtility.readFileContent(createFlushExpectationPath("expectation_flush_caching.txt")),
         Charsets.CHARSET_UTF8);

      String actualMediumContent = getMediumContentAsString(currentMedium);

      Assert.assertEquals(expectedMediumContent, actualMediumContent);

      String mediumContentAfterFlush = getMediumContentAsString(currentMedium);

      String expectedCacheContent = mediumContentAfterFlush.substring(startOffsetOfChanges,
         mediumContentAfterFlush.length());

      Assert.assertEquals(expectedCacheContent, cacheContentAfterFlush);

      assertRangeIsNotCached(at(currentMedium, mediumContentAfterFlush.length()), 2000);
   }

   /**
    * Tests {@link MediumStore#flush()} in terms of caching
    */
   @Test
   public void flush_forFilledRandomAccessMediumWithBigCache_variousChanges_allCachedBefore_cachesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      int startOffsetOfChanges = 4;

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(0), currentMediumContent.length());

      int mediumSizeDelta = performAndCheckFlushForCachingTests(startOffsetOfChanges);

      assertRangeIsCachedFromExternalMedium(at(currentMedium, 0), startOffsetOfChanges, currentMediumContent);

      String cacheContentAfterFlush = getCacheContentInRangeAsString(at(currentMedium, startOffsetOfChanges),
         currentMediumContent.length() - startOffsetOfChanges + mediumSizeDelta);

      mediumStoreUnderTest.close();

      String expectedMediumContent = new String(
         MediaTestUtility.readFileContent(createFlushExpectationPath("expectation_flush_caching.txt")),
         Charsets.CHARSET_UTF8);

      String actualMediumContent = getMediumContentAsString(currentMedium);

      Assert.assertEquals(expectedMediumContent, actualMediumContent);

      String mediumContentAfterFlush = getMediumContentAsString(currentMedium);

      String expectedCacheContent = mediumContentAfterFlush.substring(startOffsetOfChanges,
         mediumContentAfterFlush.length());

      Assert.assertEquals(expectedCacheContent, cacheContentAfterFlush);

      assertRangeIsNotCached(at(currentMedium, mediumContentAfterFlush.length()), 2000);
   }

   /**
    * Tests {@link MediumStore#flush()} in terms of caching
    */
   @Test
   public void flush_forFilledRandomAccessMediumWithBigCache_variousChanges_onlyGapsWithNoChangesCachedBefore_cachesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      int startOffsetOfChanges = 4;

      mediumStoreUnderTest.open();

      // Caching in some later removed and replaced regions, as well as some later "re-read" regions
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(0), 2);
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(400), 80);
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(1000), 186);

      int mediumSizeDelta = performAndCheckFlushForCachingTests(startOffsetOfChanges);

      assertRangeIsCachedFromExternalMedium(at(currentMedium, 0), 2, currentMediumContent);
      assertRangeIsNotCached(at(currentMedium, 2), startOffsetOfChanges - 2);

      String cacheContentAfterFlush = getCacheContentInRangeAsString(at(currentMedium, startOffsetOfChanges),
         currentMediumContent.length() - startOffsetOfChanges + mediumSizeDelta);

      mediumStoreUnderTest.close();

      String expectedMediumContent = new String(
         MediaTestUtility.readFileContent(createFlushExpectationPath("expectation_flush_caching.txt")),
         Charsets.CHARSET_UTF8);

      String actualMediumContent = getMediumContentAsString(currentMedium);

      Assert.assertEquals(expectedMediumContent, actualMediumContent);

      String mediumContentAfterFlush = getMediumContentAsString(currentMedium);

      String expectedCacheContent = mediumContentAfterFlush.substring(startOffsetOfChanges,
         mediumContentAfterFlush.length());

      Assert.assertEquals(expectedCacheContent, cacheContentAfterFlush);

      assertRangeIsNotCached(at(currentMedium, mediumContentAfterFlush.length()), 2000);
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void replaceData_overlappingWithPriorReplace_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(at(currentMedium, 10), 20, ByteBuffer.allocate(10));
      mediumStoreUnderTest.replaceData(at(currentMedium, 11), 21, ByteBuffer.allocate(8));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void replaceData_overlappingWithPriorRemove_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(at(currentMedium, 10), 20, ByteBuffer.allocate(10));
      mediumStoreUnderTest.removeData(at(currentMedium, 11), 22);
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void removeData_overlappingWithPriorReplace_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(at(currentMedium, 11), 100, ByteBuffer.allocate(100));
      mediumStoreUnderTest.removeData(at(currentMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void removeData_overlappingWithPriorRemove_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.removeData(at(currentMedium, 10), 20);
      mediumStoreUnderTest.removeData(at(currentMedium, 11), 100);
   }

   /**
    * Tests {@link MediumStore#removeData(MediumOffset, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void removeData_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.removeData(at(currentMedium, 10), 20);
   }

   /**
    * Schedules some hard-coded {@link MediumAction}s, flushes them and compares with the expectation. This is done as
    * preparation for any caching tests in connection to flush.
    * 
    * @param startOffsetOfChanges
    *           The offset to perform the first action
    * @return The change of the size of the medium after the flush
    */
   private int performAndCheckFlushForCachingTests(long startOffsetOfChanges) {
      MediumOffset firstReplaceOffset = at(currentMedium, startOffsetOfChanges);
      MediumOffset insertOffset = at(currentMedium, 56);
      MediumOffset secondReplaceOffset = at(currentMedium, 304);
      MediumOffset thirdReplaceOffset = at(currentMedium, 215);
      MediumOffset firstRemoveOffset = at(currentMedium, 500);
      MediumOffset secondRemoveOffset = at(currentMedium, 100);
      int firstRemoveSize = 486;
      int secondRemoveSize = 30;
      int firstReplaceSize = 50;
      int secondReplaceSize = 2;
      int thirdReplaceSize = 5;

      String firstReplacementText = "===CF9[000000000000]===";
      String insertionText = "===CF9[1]=== TEST TEST";
      String secondReplacementText = ">>>>uuuuuuuuuuuuuu<<<<";
      String thirdReplacementText = "REPLA";

      List<MediumAction> scheduledActions = scheduleAndFlush(
         new MediumAction[] { createReplaceAction(firstReplaceOffset, firstReplaceSize, firstReplacementText),
            createRemoveAction(firstRemoveOffset, firstRemoveSize), createInsertAction(insertOffset, insertionText),
            createRemoveAction(secondRemoveOffset, secondRemoveSize),
            createReplaceAction(secondReplaceOffset, secondReplaceSize, secondReplacementText),
            createReplaceAction(thirdReplaceOffset, thirdReplaceSize, thirdReplacementText), });

      scheduledActions.forEach(action -> Assert.assertFalse(action.isPending()));

      return -firstRemoveSize - secondRemoveSize + insertionText.length() + firstReplacementText.length()
         - firstReplaceSize + secondReplacementText.length() - secondReplaceSize + thirdReplacementText.length()
         - thirdReplaceSize;
   }

}
