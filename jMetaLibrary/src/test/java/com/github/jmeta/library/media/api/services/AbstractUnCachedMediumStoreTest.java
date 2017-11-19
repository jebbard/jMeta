/**
 *
 * {@link AbstractUnCachedMediumStoreTest}.java
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

import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractUnCachedMediumStoreTest} contains all test methods that need to operate on a {@link Medium} not backed
 * by a cache. In addition, it contains all negative tests for reading methods.
 * 
 * In addition, it contains all negative tests for the read-only access methods.
 */
public abstract class AbstractUnCachedMediumStoreTest<T extends Medium<?>> extends AbstractMediumStoreTest<T> {

   protected static final int MAX_READ_WRITE_BLOCK_SIZE_FOR_UNCACHED_MEDIUM = 20;

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forFilledMediumAndOffsetBeforeEnd_returnsFalse() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      mediumStoreUnderTest.open();

      Assert.assertFalse(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, 0)));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forEmptyMediumAtStartOffset_returnsTrue() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      Assert.assertTrue(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, 0)));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forFilledMediumAndGetDataUntilEOM_returnsTrue() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      int mediumSizeInBytes = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      // Read all bytes until EOM such that even for streams, we are at end of medium
      getDataNoEOMExpected(at(currentMedium, 0), mediumSizeInBytes);

      Assert.assertTrue(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, mediumSizeInBytes)));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumReference)} and
    * {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void getCachedByteCountAt_forFilledMediumWithDisabledCache_priorCache_returnsZero() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      mediumStoreUnderTest.open();

      MediumReference cacheOffset = at(currentMedium, 10);

      cacheNoEOMExpected(cacheOffset, 20);

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(cacheOffset.advance(2)));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumReference)}.
    */
   @Test
   public void getCachedByteCountAt_forReferenceBehindMedium_returnsZero() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      int mediumSizeInBytes = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, mediumSizeInBytes + 5)));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test
   public void cache_forFilledUncachedMedium_doesNotCacheAnything() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 20), 5);

      verifyExactlyNReads(0);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledUncachedMedium_doesNotAddAnythingToCache() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      mediumStoreUnderTest.open();

      long getDataStartOffset = 15;
      int getDataSize = 200;

      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);

      Mockito.verifyNoMoreInteractions(mediumCacheSpy);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledUncachedMedium_fromStartAndMoreBytesThanMaxRWBlockSize_readsBlockWise() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      int getDataSize = 3 * MAX_READ_WRITE_BLOCK_SIZE_FOR_UNCACHED_MEDIUM + 4;

      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);

      verifyExactlyNReads(4);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledUncachedMedium_fromStartAndLessBytesThanMaxRWBlockSize_readsOnce() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      int getDataSize = MAX_READ_WRITE_BLOCK_SIZE_FOR_UNCACHED_MEDIUM / 2;

      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);

      verifyExactlyNReads(1);

      assertCacheIsEmpty();
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createDefaultFilledMediumStore()
    */
   @Override
   protected MediumStore createDefaultFilledMediumStore() {
      return createFilledUncachedMediumStore();
   }

   /**
    * Creates a {@link MediumStore} based on a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}
    * as content, with disabled caching. This method must be called at the beginning of a test case to create the
    * {@link MediumStore} to test and its return value must be assigned to {@link #mediumStoreUnderTest}. It is used by
    * tests that check that disabled caching really works as expected.
    * 
    * @return a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT} as content, with disabled
    *         caching
    */
   protected MediumStore createFilledUncachedMediumStore() {
      try {
         currentMedium = createFilledMedium(testName.getMethodName(), 0, MAX_READ_WRITE_BLOCK_SIZE_FOR_UNCACHED_MEDIUM);
         return createMediumStoreToTest(currentMedium);
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }

}
