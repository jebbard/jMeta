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

/**
 * {@link AbstractUnCachedAndWritableRandomAccessMediumStoreTest} tests the {@link MediumStore} interface for writable
 * random-access and uncached media. It contains tests specializing on writing to uncached media. In addition, it
 * contains all negative tests for writing methods.
 *
 * @param <T>
 *           The concrete type of {@link Medium} to use
 */
public abstract class AbstractUnCachedAndWritableRandomAccessMediumStoreTest<T extends Medium<?>>
   extends AbstractUnCachedMediumStoreTest<T> {

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forRandomAccessMediumAtEOM_returnsTrue() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      int endOfMediumOffset = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      Assert.assertTrue(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, endOfMediumOffset)));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forRandomAccessMediumBeforeEOM_returnsFalse() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      int endOfMediumOffset = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      Assert.assertFalse(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, endOfMediumOffset / 2)));
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledUncachedMedium_twiceInEnclosingRegion_reReadsData() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      int getDataSize = 200;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
      // Read again in range fully enclosed by first read
      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset).advance(5), getDataSize - 3,
         currentMediumContent);

      // Data as read block-wise, twice
      verifyExactlyNReads(2 * getDataSize / MAX_READ_WRITE_BLOCK_SIZE_FOR_UNCACHED_MEDIUM);

      assertCacheIsEmpty();
   }
}
