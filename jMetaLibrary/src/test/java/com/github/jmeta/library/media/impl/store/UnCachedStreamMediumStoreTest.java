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

import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.services.AbstractUnCachedMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.api.types.MediumReference;
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
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
    */
   @Override
   protected InputStreamMedium createEmptyMedium(String testMethodName) throws IOException {
      return new InputStreamMedium(new FileInputStream(MediaTestFiles.EMPTY_TEST_FILE_PATH.toFile()),
         STREAM_BASED_EMPTY_MEDIUM_NAME);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createFilledMedium(java.lang.String,
    *      boolean, long, int, int)
    */
   @Override
   protected InputStreamMedium createFilledMedium(String testMethodName, boolean enableCaching, long maxCacheSize,
      int maxCacheRegionSize, int maxReadWriteBlockSize) throws IOException {
      return new InputStreamMedium(new FileInputStream(MediaTestFiles.FIRST_TEST_FILE_PATH.toFile()),
         STREAM_BASED_FILLED_MEDIUM_NAME, enableCaching, maxCacheSize, maxCacheRegionSize, maxReadWriteBlockSize);
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
