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

import org.junit.Assert;
import org.junit.Test;

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
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithBigCache_inMiddle_doesNotReadOrCacheDataBefore() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 20), 10);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 20 - 1)));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithBigCache_multipleOverlappingAndDisconnectedRegions_updatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 20), 10);
      cacheNoEOMExpected(at(currentMedium, 25), 100);
      cacheNoEOMExpected(at(currentMedium, 200), 35);

      Assert.assertEquals(103, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 20 + 2)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 20 - 1)));
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 25 + 100)));
      Assert.assertEquals(24, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 200 + 11)));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithSmallCache_overlappingAlreadyFreedRangeAtFront_updatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      mediumStoreUnderTest.open();

      int firstCacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;
	cacheNoEOMExpected(at(currentMedium, 10), firstCacheSize);
    
    verifyExactlyNReads(firstCacheSize / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1);
	
    long secondCacheStartOffset = 5;
    int secondCacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE;
    
	MediumReference secondCacheStartReference = at(currentMedium, secondCacheStartOffset);
	
	cacheNoEOMExpected(secondCacheStartReference, secondCacheSize);
    
    Assert.assertEquals(secondCacheSize, mediumStoreUnderTest.getCachedByteCountAt(secondCacheStartReference));
      
      verifyExactlyNReads((firstCacheSize + 35) / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 1);
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
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithSmallCache_withinAlreadyFreedRange_returnsExpectedDataAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 10;
      int getDataSize = 20;
      
      int cacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;
	cacheNoEOMExpected(at(currentMedium, 0), cacheSize);
	
	MediumReference getDataStartReference = at(currentMedium, getDataStartOffset);
	testGetData_returnsExpectedData(getDataStartReference, getDataSize, currentMediumContent);
    
    Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataStartReference));
    Assert.assertEquals(MAX_CACHE_SIZE_FOR_SMALL_CACHE - 25, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 55)));
      
      verifyExactlyNReads((MAX_CACHE_SIZE_FOR_SMALL_CACHE + getDataSize) / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithSmallCache_overlappingAlreadyFreedRangeAtFront_returnsExpectedDataAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      int cacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;
	cacheNoEOMExpected(at(currentMedium, 10), cacheSize);
	
    long getDataStartOffset = 5;
    int getDataSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE;
    
	MediumReference getDataStartReference = at(currentMedium, getDataStartOffset);
	testGetData_returnsExpectedData(getDataStartReference, getDataSize, currentMediumContent);
    
    Assert.assertEquals(getDataSize, mediumStoreUnderTest.getCachedByteCountAt(getDataStartReference));
      
      verifyExactlyNReads((MAX_CACHE_SIZE_FOR_SMALL_CACHE + getDataSize) / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithSmallCache_overlappingAlreadyCachedRangeAtFront_returnsExpectedDataAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithSmallCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 15 + 7 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;
      int getDataSize = 10;
      
      int cacheSize = MAX_CACHE_SIZE_FOR_SMALL_CACHE + 6 * MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE;
	cacheNoEOMExpected(at(currentMedium, 10), cacheSize);
	
	MediumReference getDataStartReference = at(currentMedium, getDataStartOffset);
	testGetData_returnsExpectedData(getDataStartReference, getDataSize, currentMediumContent);
    
    Assert.assertEquals(583, mediumStoreUnderTest.getCachedByteCountAt(getDataStartReference));
      
      verifyExactlyNReads(MAX_CACHE_SIZE_FOR_SMALL_CACHE / MAX_READ_WRITE_BLOCK_SIZE_FOR_SMALL_CACHE + 6 + 1);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithBigCache_inMiddle_doesNotReadOrCacheDataBefore() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      mediumStoreUnderTest.open();

      long getDataStartOffset = 20;
      int getDataSize = 100;

      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);
      
      verifyExactlyNReads(1);
      
      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 5)));
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
   }
}
