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
   public void cache_forFilledRandomAccessMediumWithBigCache_multipleOverlappingAndDisconnectedRegions_cachesExpectedBytes() {
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
   public void getData_forFilledMediumWithBigCache_unCachedRange_returnsExpectedDataAccessesMediumOnceAndUpdatesCache() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 15;
      int getDataSize = 80;

      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      // One read call only due to big read-write block size and random-access medium
      verifyExactlyNReads(1);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 0)));
      Assert.assertEquals(getDataSize,
         mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, getDataStartOffset)));
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMediumWithBigCache_rangeBeforeCachedRange_returnsExpectedDataAndAccessesMedium() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 100), 100);

      verifyExactlyNReads(1);

      long getDataStartOffset = 15;
      int getDataSize = 80;

      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);
      testGetData_returnsExpectedData(getDataOffset, getDataSize, currentMediumContent);

      verifyExactlyNReads(2);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getData_forFilledRandomAccessMediumWithBigCache_referenceBehindEOM_throwsException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = currentMediumContent.length() + 15;
      int getDataSize = 10;

      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);
   }
}
