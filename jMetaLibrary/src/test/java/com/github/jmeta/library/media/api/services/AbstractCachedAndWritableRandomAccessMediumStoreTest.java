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

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.InvalidOverlappingWriteException;
import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * {@link AbstractCachedAndWritableRandomAccessMediumStoreTest} tests the {@link MediumStore} interface for writable
 * random-access media. Thus it contains tests for all writing methods.
 *
 * @param <T>
 *           The concrete type of {@link Medium} to use
 */
public abstract class AbstractCachedAndWritableRandomAccessMediumStoreTest<T extends Medium<?>>
   extends AbstractCachedMediumStoreTest<T> {

   private static class ExpectedMediumContentBuilder {

      private final String originalContent;
      private String expectedContent = "";

      public ExpectedMediumContentBuilder(String originalContent) {
         this.originalContent = originalContent;
      }

      public void appendFromOriginal(int offset, int size) {
         expectedContent += originalContent.substring(offset, offset + size);
      }

      public void appendLiteralString(String literalString) {
         expectedContent += literalString;
      }

      public String buildExpectedContent() {
         return expectedContent;
      }
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumReference)} and
    * {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void getCachedByteCountAt_forFilledRandomAccessMediumWithBigCache_priorCacheAndOffsetOutsideCachedRegion_returnsZero() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      int byteCountToCache = 10;

      MediumReference cacheOffset = at(currentMedium, 20);

      cacheNoEOMExpected(cacheOffset, byteCountToCache);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(-5)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(byteCountToCache)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(byteCountToCache + 10)));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithSmallCache_forAlreadyFreedRangeBeforeCurrentPosition_updatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumReference cacheOffset = at(currentMedium, 10);
      int cacheSize = 20;

      cacheNoEOMExpected(cacheOffset, currentMediumContent.length() - 10);
      cacheNoEOMExpected(cacheOffset, cacheSize);

      Assert.assertEquals(cacheSize, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset));

      int remainingCacheStartOffset = 595 + cacheSize;

      assertRangeIsCached(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize),
         remainingCacheStartOffset - (int) cacheOffset.getAbsoluteMediumOffset() - cacheSize);
      assertRangeIsCached(at(currentMedium, remainingCacheStartOffset),
         currentMediumContent.length() - remainingCacheStartOffset, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithBigCache_inMiddle_doesNotReadOrCacheDataBefore() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumReference cacheOffset = at(currentMedium, 20);
      int cacheSize = 10;
      cacheNoEOMExpected(cacheOffset, cacheSize);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 20 - 1)));

      assertRangeIsNotCached(at(currentMedium, 0), (int) cacheOffset.getAbsoluteMediumOffset());
      assertRangeIsCached(cacheOffset, cacheSize, currentMediumContent);
      assertRangeIsNotCached(cacheOffset.advance(cacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithBigCache_multipleOverlappingAndDisconnectedRegions_updatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumReference firstCacheOffset = at(currentMedium, 20);
      int firstCacheSize = 10;
      MediumReference secondCacheOffset = at(currentMedium, 25);
      int secondCacheSize = 100;
      MediumReference thirdCacheOffset = at(currentMedium, 200);
      int thirdCacheSize = 35;

      cacheNoEOMExpected(firstCacheOffset, firstCacheSize);
      cacheNoEOMExpected(secondCacheOffset, secondCacheSize);
      cacheNoEOMExpected(thirdCacheOffset, thirdCacheSize);

      Assert.assertEquals(103, mediumStoreUnderTest.getCachedByteCountAt(firstCacheOffset.advance(2)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(firstCacheOffset.advance(-1)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(secondCacheOffset.advance(secondCacheSize)));
      Assert.assertEquals(24, mediumStoreUnderTest.getCachedByteCountAt(thirdCacheOffset.advance(11)));

      assertRangeIsNotCached(at(currentMedium, 0), (int) firstCacheOffset.getAbsoluteMediumOffset());
      assertRangeIsCached(firstCacheOffset, secondCacheSize + 5, currentMediumContent);
      assertRangeIsNotCached(secondCacheOffset.advance(secondCacheSize),
         (int) (thirdCacheOffset.getAbsoluteMediumOffset() - secondCacheOffset.getAbsoluteMediumOffset()
            - secondCacheSize));
      assertRangeIsCached(thirdCacheOffset, thirdCacheSize, currentMediumContent);
      assertRangeIsNotCached(thirdCacheOffset.advance(thirdCacheSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithSmallCache_overlappingAlreadyFreedRangeAtFront_updatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumReference firstCacheOffset = at(currentMedium, 10);
      int firstCacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      MediumReference secondCacheOffset = at(currentMedium, 5);
      int secondCacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(firstCacheOffset, firstCacheSize);

      verifyExactlyNReads(firstCacheSize / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1);

      cacheNoEOMExpected(secondCacheOffset, secondCacheSize);

      verifyExactlyNReads((firstCacheSize + 35) / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1);

      Assert.assertEquals(secondCacheSize, mediumStoreUnderTest.getCachedByteCountAt(secondCacheOffset));

      assertRangeIsNotCached(at(currentMedium, 0), (int) secondCacheOffset.getAbsoluteMediumOffset());
      assertRangeIsCached(secondCacheOffset, secondCacheSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void cache_forFilledRandomAccessMediumWithBigCache_forOffsetBeyondEOM_throwsException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = currentMediumContent.length() + 15;
      int getDataSize = 10;

      cacheNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithSmallCache_withinAlreadyFreedRange_returnsExpectedDataAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      int cacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(at(currentMedium, 0), cacheSize);

      long getDataStartOffset = 10;
      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 20;

      MediumReference expectedActualCacheStartOffset = at(currentMedium, 50);
      int expectedActualCacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE - getDataSize;

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      Assert.assertEquals(expectedActualCacheSize,
         mediumStoreUnderTest.getCachedByteCountAt(expectedActualCacheStartOffset));

      verifyExactlyNReads(
         (MAX_CACHE_SIZE_FOR_SMALL_CACHE + getDataSize) / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);

      assertRangeIsNotCached(at(currentMedium, 0), (int) getDataOffset.getAbsoluteMediumOffset());
      assertRangeIsCached(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize),
         (int) (expectedActualCacheStartOffset.getAbsoluteMediumOffset() - getDataOffset.getAbsoluteMediumOffset()
            - getDataSize));
      assertRangeIsCached(expectedActualCacheStartOffset, expectedActualCacheSize, currentMediumContent);
      assertRangeIsNotCached(expectedActualCacheStartOffset.advance(expectedActualCacheSize),
         currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithSmallCache_overlappingAlreadyFreedRangeAtFront_returnsExpectedDataAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumReference cacheOffset = at(currentMedium, 10);
      int cacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      MediumReference getDataOffset = at(currentMedium, 5);
      int getDataSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(cacheOffset, cacheSize);

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      verifyExactlyNReads(
         (MAX_CACHE_SIZE_FOR_SMALL_CACHE + getDataSize) / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);

      assertRangeIsNotCached(at(currentMedium, 0), (int) getDataOffset.getAbsoluteMediumOffset());
      assertRangeIsCached(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithSmallCache_overlappingAlreadyCachedRangeAtFront_returnsExpectedDataAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumReference getDataOffset = at(currentMedium, 15 + 7 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE);
      int getDataSize = 10;

      int cacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;

      cacheNoEOMExpected(at(currentMedium, 10), cacheSize);

      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      Assert.assertEquals(583, mediumStoreUnderTest.getCachedByteCountAt(getDataOffset));

      verifyExactlyNReads(MAX_CACHE_SIZE_FOR_SMALL_CACHE / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);

      assertRangeIsNotCached(at(currentMedium, 0), (int) getDataOffset.advance(-10).getAbsoluteMediumOffset());
      assertRangeIsCached(getDataOffset.advance(-10), MAX_CACHE_SIZE_FOR_SMALL_CACHE, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(-10 + MAX_CACHE_SIZE_FOR_SMALL_CACHE),
         currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithBigCache_inMiddle_doesNotReadOrCacheDataBefore() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 20;
      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);
      int getDataSize = 100;

      getDataNoEOMExpected(getDataOffset, getDataSize);

      verifyExactlyNReads(1);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 5)));

      assertRangeIsNotCached(at(currentMedium, 0), (int) getDataOffset.getAbsoluteMediumOffset());
      assertRangeIsCached(getDataOffset, getDataSize, currentMediumContent);
      assertRangeIsNotCached(getDataOffset.advance(getDataSize), currentMediumContent.length());
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getData_forFilledRandomAccessMediumWithBigCache_forOffsetBeyondEOM_throwsException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = currentMediumContent.length() + 15;
      int getDataSize = 10;

      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void replaceData_overlappingWithPriorReplace_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(at(currentMedium, 10), 20, ByteBuffer.allocate(10));
      mediumStoreUnderTest.replaceData(at(currentMedium, 11), 21, ByteBuffer.allocate(8));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void replaceData_overlappingWithPriorRemove_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(at(currentMedium, 10), 20, ByteBuffer.allocate(10));
      mediumStoreUnderTest.removeData(at(currentMedium, 11), 22);
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void removeData_overlappingWithPriorReplace_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(at(currentMedium, 11), 100, ByteBuffer.allocate(100));
      mediumStoreUnderTest.removeData(at(currentMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test(expected = InvalidOverlappingWriteException.class)
   public void removeData_overlappingWithPriorRemove_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.removeData(at(currentMedium, 10), 20);
      mediumStoreUnderTest.removeData(at(currentMedium, 11), 100);
   }

   /**
    * Tests {@link MediumStore#removeData(MediumReference, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void removeData_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.removeData(at(currentMedium, 10), 20);
   }
}
