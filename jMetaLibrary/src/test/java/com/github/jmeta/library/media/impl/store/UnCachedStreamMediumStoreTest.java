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

import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.InvalidMediumReferenceException;
import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.services.AbstractUnCachedMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.impl.mediumAccessor.InputStreamMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;

/**
 * {@link UnCachedStreamMediumStoreTest} tests a {@link MediumStore} backed by {@link InputStreamMedium} instances
 * without a cache.
 */
public class UnCachedStreamMediumStoreTest extends AbstractUnCachedMediumStoreTest<InputStreamMedium> {

   private static final String STREAM_BASED_FILLED_MEDIUM_NAME = "Stream based filled medium";
   private static final String STREAM_BASED_EMPTY_MEDIUM_NAME = "Stream based empty medium";

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledUncachedStreamMedium_fromMiddleAndMoreBytesThanMaxRWBlockSize_readsRangeAndBytesBeforeBlockWise() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      mediumStoreUnderTest.open();

      long getDataStartOffset = MAX_READ_WRITE_BLOCK_SIZE_FOR_UNCACHED_MEDIUM * 3 + 5;
      int getDataSize = 3 * MAX_READ_WRITE_BLOCK_SIZE_FOR_UNCACHED_MEDIUM;

      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);

      verifyExactlyNReads(4 + 3);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test(expected = InvalidMediumReferenceException.class)
   public void getData_forFilledUncachedStreamMedium_twiceInEnclosingRegion_throwsException() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      int getDataSize = 200;

      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);

      // Read again in range fully enclosed by first read
      // - Must throw exception here because the previous data read was returned and advanced the stream, but it
      // was not added to the cache. Accessing the same offset again for an uncached stream is not possible
      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test(expected = InvalidMediumReferenceException.class)
   public void getData_forFilledUncachedStreamMedium_forOffsetBeforeCurrentPosition_throwsException() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      mediumStoreUnderTest.open();

      getDataNoEOMExpected(at(currentMedium, 20), 100);

      long getDataStartOffset = 0;
      int getDataSize = 200;

      getDataNoEOMExpected(at(currentMedium, getDataStartOffset), getDataSize);

      assertCacheIsEmpty();
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
   protected InputStreamMedium createFilledMedium(String testMethodName, long maxCacheSize, int maxReadWriteBlockSize)
      throws IOException {
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
