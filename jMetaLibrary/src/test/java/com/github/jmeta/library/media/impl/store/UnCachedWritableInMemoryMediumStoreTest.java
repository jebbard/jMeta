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

import static com.github.jmeta.library.media.api.helper.TestMedia.at;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.services.AbstractUnCachedMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MemoryMediumAccessor;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link UnCachedWritableInMemoryMediumStoreTest} tests a {@link MediumStore} backed by {@link InMemoryMedium}
 * instances, which is - dy definition - without an additional cache.
 */
public class UnCachedWritableInMemoryMediumStoreTest extends AbstractUnCachedMediumStoreTest<InMemoryMedium> {

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test
   public void isAtEndOfMedium_forRandomAccessMediumAtEOM_returnsTrue() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      int endOfMediumOffset = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      Assert.assertTrue(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, endOfMediumOffset)));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test
   public void isAtEndOfMedium_forRandomAccessMediumBeforeEOM_returnsFalse() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      int endOfMediumOffset = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      Assert.assertFalse(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, endOfMediumOffset / 2)));
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
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

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithBigCache_forOffsetBeyondEOM_throwsEOMException() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

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
   public void getData_forFilledRandomAccessMediumWithBigCache_forOffsetBeyondEOM_throwsEOMException() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset getDataOffset = at(currentMedium, (long) (currentMediumContent.length() + 15));
      int getDataSize = 10;

      testGetData_throwsEndOfMediumException(getDataOffset, getDataSize, currentMediumContent.length(),
         currentMediumContent);

      assertCacheIsEmpty();
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
    */
   @Override
   protected InMemoryMedium createEmptyMedium(String testMethodName) throws IOException {
      return new InMemoryMedium(new byte[0], "Stream based empty medium", false);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createFilledMedium(java.lang.String,
    *      long, int)
    */
   @Override
   protected InMemoryMedium createFilledMedium(String testMethodName, long maxCacheSize, int maxReadWriteBlockSize)
      throws IOException {
      return new InMemoryMedium(MediaTestUtility.readFileContent(TestMedia.FIRST_TEST_FILE_PATH),
         "Stream based filled medium", false, maxReadWriteBlockSize);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createMediumAccessor(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected MediumAccessor<InMemoryMedium> createMediumAccessor(InMemoryMedium mediumToUse) {
      return new MemoryMediumAccessor(mediumToUse);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#getMediumContentAsString(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected String getMediumContentAsString(InMemoryMedium medium) {
      return new String(medium.getWrappedMedium().array(), Charsets.CHARSET_UTF8);
   }
}
