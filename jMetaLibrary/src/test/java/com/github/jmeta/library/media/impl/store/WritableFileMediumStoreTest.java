/**
 *
 * {@link ReadOnlyInMemoryMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.impl.store;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.InvalidOverlappingWriteException;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.services.AbstractMediumStoreTest;
import com.github.jmeta.library.media.api.services.AbstractWritableRandomAccessMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.MediumAccessType;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.impl.mediumAccessor.FileMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link WritableFileMediumStoreTest} tests a {@link MediumStore} backed by {@link FileMedium} instances.
 */
public class WritableFileMediumStoreTest extends AbstractWritableRandomAccessMediumStoreTest<FileMedium> {

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithBigCache_inMiddle_doesNotReadOrCacheDataBefore() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = TestMedia.at(currentMedium, 20);
      int cacheSize = 10;
      cacheNoEOMExpected(cacheOffset, cacheSize);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(TestMedia.at(currentMedium, 20 - 1)));

      assertRangeIsNotCached(TestMedia.at(currentMedium, 0), (int) cacheOffset.getAbsoluteMediumOffset());
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

      MediumOffset firstCacheOffset = TestMedia.at(currentMedium, 20);
      int firstCacheSize = 10;
      MediumOffset secondCacheOffset = TestMedia.at(currentMedium, 25);
      int secondCacheSize = 100;
      MediumOffset thirdCacheOffset = TestMedia.at(currentMedium, 200);
      int thirdCacheSize = 35;

      cacheNoEOMExpected(firstCacheOffset, firstCacheSize);
      cacheNoEOMExpected(secondCacheOffset, secondCacheSize);
      cacheNoEOMExpected(thirdCacheOffset, thirdCacheSize);

      Assert.assertEquals(103, mediumStoreUnderTest.getCachedByteCountAt(firstCacheOffset.advance(2)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(firstCacheOffset.advance(-1)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(secondCacheOffset.advance(secondCacheSize)));
      Assert.assertEquals(24, mediumStoreUnderTest.getCachedByteCountAt(thirdCacheOffset.advance(11)));

      assertRangeIsNotCached(TestMedia.at(currentMedium, 0), (int) firstCacheOffset.getAbsoluteMediumOffset());
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
   public void cache_forFilledRandomAccessMediumWithSmallCache_forAlreadyFreedRangeBeforeCurrentPosition_updatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = TestMedia.at(currentMedium, 10);
      int cacheSize = 20;

      cacheNoEOMExpected(cacheOffset, currentMediumContent.length() - 10);
      cacheNoEOMExpected(cacheOffset, cacheSize);

      Assert.assertEquals(cacheSize, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));

      int remainingCacheStartOffset = 595 + cacheSize;

      assertRangeIsCachedFromExternalMedium(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize),
         remainingCacheStartOffset - (int) cacheOffset.getAbsoluteMediumOffset() - cacheSize);
      assertRangeIsCachedFromExternalMedium(TestMedia.at(currentMedium, remainingCacheStartOffset),
         currentMediumContent.length() - remainingCacheStartOffset, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithSmallCache_overlappingAlreadyFreedRangeAtFront_updatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset firstCacheOffset = TestMedia.at(currentMedium, 10);
      int firstCacheSize = AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE
         + 6 * AbstractMediumStoreTest.MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      MediumOffset secondCacheOffset = TestMedia.at(currentMedium, 5);
      int secondCacheSize = AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(firstCacheOffset, firstCacheSize);

      verifyExactlyNReads(firstCacheSize / AbstractMediumStoreTest.MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1);

      cacheNoEOMExpected(secondCacheOffset, secondCacheSize);

      verifyExactlyNReads(
         (firstCacheSize + 35) / AbstractMediumStoreTest.MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1);

      Assert.assertEquals(secondCacheSize, mediumStoreUnderTest.getCachedByteCountAt(secondCacheOffset));

      assertRangeIsNotCached(TestMedia.at(currentMedium, 0), (int) secondCacheOffset.getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(secondCacheOffset, secondCacheSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#flush()} in terms of caching
    */
   @Test
   public void flush_forFilledRandomAccessMediumWithBigCache_variousChanges_allCachedBefore_cachesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      int startOffsetOfChanges = 4;

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(0), currentMediumContent.length());

      int mediumSizeDelta = performAndCheckFlushForCachingTests(startOffsetOfChanges);

      assertRangeIsCachedFromExternalMedium(TestMedia.at(currentMedium, 0), startOffsetOfChanges, currentMediumContent);

      String cacheContentAfterFlush = getCacheContentInRangeAsString(TestMedia.at(currentMedium, startOffsetOfChanges),
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

      assertRangeIsNotCached(TestMedia.at(currentMedium, mediumContentAfterFlush.length()), 2000);
   }

   /**
    * Tests {@link MediumStore#flush()} in terms of caching
    */
   @Test
   public void flush_forFilledRandomAccessMediumWithBigCache_variousChanges_nothingCachedBefore_cachesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      int startOffsetOfChanges = 4;

      mediumStoreUnderTest.open();

      int mediumSizeDelta = performAndCheckFlushForCachingTests(startOffsetOfChanges);

      assertRangeIsNotCached(TestMedia.at(currentMedium, 0), startOffsetOfChanges);

      String cacheContentAfterFlush = getCacheContentInRangeAsString(TestMedia.at(currentMedium, startOffsetOfChanges),
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

      assertRangeIsNotCached(TestMedia.at(currentMedium, mediumContentAfterFlush.length()), 2000);
   }

   /**
    * Tests {@link MediumStore#flush()} in terms of caching
    */
   @Test
   public void flush_forFilledRandomAccessMediumWithBigCache_variousChanges_onlyGapsWithNoChangesCachedBefore_cachesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      int startOffsetOfChanges = 4;

      mediumStoreUnderTest.open();

      // Caching in some later removed and replaced regions, as well as some later
      // "re-read" regions
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(0), 2);
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(400), 80);
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(1000), 186);

      int mediumSizeDelta = performAndCheckFlushForCachingTests(startOffsetOfChanges);

      assertRangeIsCachedFromExternalMedium(TestMedia.at(currentMedium, 0), 2, currentMediumContent);
      assertRangeIsNotCached(TestMedia.at(currentMedium, 2), startOffsetOfChanges - 2);

      String cacheContentAfterFlush = getCacheContentInRangeAsString(TestMedia.at(currentMedium, startOffsetOfChanges),
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

      assertRangeIsNotCached(TestMedia.at(currentMedium, mediumContentAfterFlush.length()), 2000);
   }

   /**
    * Tests {@link MediumStore#flush()} in terms of caching
    */
   @Test
   public void flush_forFilledRandomAccessMediumWithBigCache_variousChanges_partlyCachedBefore_cachesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      int startOffsetOfChanges = 4;

      mediumStoreUnderTest.open();

      // Caching in some later removed and replaced regions, as well as some later
      // "re-read" regions
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(5), 200);
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(255), 114);
      cacheNoEOMExpected(mediumStoreUnderTest.createMediumOffset(600), 344);

      int mediumSizeDelta = performAndCheckFlushForCachingTests(startOffsetOfChanges);

      assertRangeIsNotCached(TestMedia.at(currentMedium, 0), startOffsetOfChanges);

      String cacheContentAfterFlush = getCacheContentInRangeAsString(TestMedia.at(currentMedium, startOffsetOfChanges),
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

      assertRangeIsNotCached(TestMedia.at(currentMedium, mediumContentAfterFlush.length()), 2000);
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumOffset)} and {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void getCachedByteCountAt_forFilledRandomAccessMediumWithBigCache_priorCacheAndOffsetOutsideCachedRegion_returnsZero() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      int byteCountToCache = 10;

      MediumOffset cacheOffset = TestMedia.at(currentMedium, 20);

      cacheNoEOMExpected(cacheOffset, byteCountToCache);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(-5)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(byteCountToCache)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(byteCountToCache + 10)));
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
      MediumOffset getDataOffset = TestMedia.at(currentMedium, getDataStartOffset);
      int getDataSize = 100;

      getDataNoEOMExpected(getDataOffset, getDataSize);

      verifyExactlyNReads(1);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(TestMedia.at(currentMedium, 5)));

      assertRangeIsNotCached(TestMedia.at(currentMedium, 0), (int) getDataOffset.getAbsoluteMediumOffset());
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

      MediumOffset getDataOffset = TestMedia.at(currentMedium,
         15 + 7 * AbstractMediumStoreTest.MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE);
      int getDataSize = 10;

      int cacheSize = AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE
         + 6 * AbstractMediumStoreTest.MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(TestMedia.at(currentMedium, 10), cacheSize);

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(583, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      verifyExactlyNReads(AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE
         / AbstractMediumStoreTest.MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);

      assertRangeIsNotCached(TestMedia.at(currentMedium, 0),
         (int) getDataOffset.advance(-10).getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(getDataOffset.advance(-10),
         AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(-10 + AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE),
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

      MediumOffset cacheOffset = TestMedia.at(currentMedium, 10);
      int cacheSize = AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE
         + 6 * AbstractMediumStoreTest.MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      MediumOffset getDataOffset = TestMedia.at(currentMedium, 5);
      int getDataSize = AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(cacheOffset, cacheSize);

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      verifyExactlyNReads((AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE + getDataSize)
         / AbstractMediumStoreTest.MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);

      assertRangeIsNotCached(TestMedia.at(currentMedium, 0), (int) getDataOffset.getAbsoluteMediumOffset());
      assertRangeIsCachedFromExternalMedium(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithSmallCache_withinAlreadyFreedRange_returnsExpectedDataAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      int cacheSize = AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE
         + 6 * AbstractMediumStoreTest.MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(TestMedia.at(currentMedium, 0), cacheSize);

      long getDataStartOffset = 10;
      MediumOffset getDataOffset = TestMedia.at(currentMedium, getDataStartOffset);
      int getDataSize = 20;

      MediumOffset expectedActualCacheStartOffset = TestMedia.at(currentMedium, 50);
      int expectedActualCacheSize = AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE - getDataSize;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      Assert.assertEquals(expectedActualCacheSize,
         mediumStoreUnderTest.getCachedByteCountAt(expectedActualCacheStartOffset));

      verifyExactlyNReads((AbstractMediumStoreTest.MAX_CACHE_SIZE_FOR_SMALL_CACHE + getDataSize)
         / AbstractMediumStoreTest.MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);

      assertRangeIsNotCached(TestMedia.at(currentMedium, 0), (int) getDataOffset.getAbsoluteMediumOffset());
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
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void removeData_overlappingWithPriorRemove_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.removeData(TestMedia.at(currentMedium, 10), 20);
      mediumStoreUnderTest.removeData(TestMedia.at(currentMedium, 11), 100);
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void removeData_overlappingWithPriorReplace_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(TestMedia.at(currentMedium, 11), 100, ByteBuffer.allocate(100));
      mediumStoreUnderTest.removeData(TestMedia.at(currentMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void replaceData_overlappingWithPriorRemove_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(TestMedia.at(currentMedium, 10), 20, ByteBuffer.allocate(10));
      mediumStoreUnderTest.removeData(TestMedia.at(currentMedium, 11), 22);
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void replaceData_overlappingWithPriorReplace_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(TestMedia.at(currentMedium, 10), 20, ByteBuffer.allocate(10));
      mediumStoreUnderTest.replaceData(TestMedia.at(currentMedium, 11), 21, ByteBuffer.allocate(8));
   }

   /**
    * Creates a copy of the indicated file in a temporary folder with the given name parts.
    *
    * @param pathToFile
    *           The path to the file to copy
    * @param mediumType
    *           The type of medium as string, concatenated to the target file name
    * @param testMethodName
    *           The name of the test method currently executed, concatenated to the target file name
    * @return a Path to a copied file
    * @throws IOException
    *            if anything bad happens during I/O
    */
   private Path getCopiedFile(Path pathToFile, String mediumType, String testMethodName) throws IOException {
      Reject.ifNull(testMethodName, "testMethodName");
      Reject.ifNull(mediumType, "mediumType");
      Reject.ifNull(pathToFile, "pathToFile");

      Reject.ifFalse(Files.isRegularFile(pathToFile), "Files.isRegularFile(pathToFile)");

      Path copiedFile = Files.copy(pathToFile,
         TestMedia.TEST_FILE_TEMP_OUTPUT_DIRECTORY_PATH
            .resolve(getClass().getSimpleName() + "_" + mediumType + testMethodName + ".txt"),
         StandardCopyOption.REPLACE_EXISTING);
      return copiedFile;
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
      MediumOffset firstReplaceOffset = TestMedia.at(currentMedium, startOffsetOfChanges);
      MediumOffset insertOffset = TestMedia.at(currentMedium, 56);
      MediumOffset secondReplaceOffset = TestMedia.at(currentMedium, 304);
      MediumOffset thirdReplaceOffset = TestMedia.at(currentMedium, 215);
      MediumOffset firstRemoveOffset = TestMedia.at(currentMedium, 500);
      MediumOffset secondRemoveOffset = TestMedia.at(currentMedium, 100);
      int firstRemoveSize = 486;
      int secondRemoveSize = 30;
      int firstReplaceSize = 50;
      int secondReplaceSize = 2;
      int thirdReplaceSize = 5;

      String firstReplacementText = "===CF9[000000000000]===";
      String insertionText = "===CF9[1]=== TEST TEST";
      String secondReplacementText = ">>>>uuuuuuuuuuuuuu<<<<";
      String thirdReplacementText = "REPLA";

      List<MediumAction> scheduledActions = scheduleAndFlush(new MediumAction[] {
         AbstractWritableRandomAccessMediumStoreTest.createReplaceAction(firstReplaceOffset, firstReplaceSize,
            firstReplacementText),
         AbstractWritableRandomAccessMediumStoreTest.createRemoveAction(firstRemoveOffset, firstRemoveSize),
         AbstractWritableRandomAccessMediumStoreTest.createInsertAction(insertOffset, insertionText),
         AbstractWritableRandomAccessMediumStoreTest.createRemoveAction(secondRemoveOffset, secondRemoveSize),
         AbstractWritableRandomAccessMediumStoreTest.createReplaceAction(secondReplaceOffset, secondReplaceSize,
            secondReplacementText),
         AbstractWritableRandomAccessMediumStoreTest.createReplaceAction(thirdReplaceOffset, thirdReplaceSize,
            thirdReplacementText), });

      scheduledActions.forEach(action -> Assert.assertFalse(action.isPending()));

      return -firstRemoveSize - secondRemoveSize + insertionText.length() + firstReplacementText.length()
         - firstReplaceSize + secondReplacementText.length() - secondReplaceSize + thirdReplacementText.length()
         - thirdReplaceSize;
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
    */
   @Override
   protected FileMedium createEmptyMedium(String testMethodName) throws IOException {

      Path copiedFile = getCopiedFile(TestMedia.EMPTY_TEST_FILE_PATH, "EMPTY_MEDIUM_", testMethodName);

      return new FileMedium(copiedFile, MediumAccessType.READ_WRITE);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createFilledMedium(java.lang.String,
    *      long, int)
    */
   @Override
   protected FileMedium createFilledMedium(String testMethodName, long maxCacheSize, int maxReadWriteBlockSize)
      throws IOException {

      Path copiedFile = getCopiedFile(TestMedia.FIRST_TEST_FILE_PATH, "FIRST_TEST_FILE_MEDIUM_", testMethodName);
      return new FileMedium(copiedFile, MediumAccessType.READ_WRITE, maxCacheSize, maxReadWriteBlockSize);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createMediumAccessor(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected MediumAccessor<FileMedium> createMediumAccessor(FileMedium mediumToUse) {
      return new FileMediumAccessor(mediumToUse);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#getMediumContentAsString(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected String getMediumContentAsString(FileMedium medium) {
      return new String(MediaTestUtility.readFileContent(medium.getWrappedMedium()), Charsets.CHARSET_UTF8);
   }
}
