/**
 *
 * {@link StreamMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.impl.store;

import java.io.FileInputStream;
import java.io.IOException;

import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.services.AbstractReadOnlyMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.impl.mediumAccessor.InputStreamMediumAccessor;

/**
 * {@link StreamMediumStoreTest} tests a {@link MediumStore} backed by {@link InputStreamMedium} instances.
 */
public class StreamMediumStoreTest extends AbstractReadOnlyMediumStoreTest<InputStreamMedium> {

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
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createMediumStoreToTest(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected MediumStore createMediumStoreToTest(InputStreamMedium mediumToUse) {
      return new StandardMediumStore<>(new InputStreamMediumAccessor(mediumToUse));
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#getCurrentMediumContentAsString(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected String getCurrentMediumContentAsString(InputStreamMedium medium) {
      if (medium.getName().equals(STREAM_BASED_EMPTY_MEDIUM_NAME)) {
         return MediaTestFiles.EMPTY_TEST_FILE_CONTENT;
      } else if (medium.getName().equals(STREAM_BASED_FILLED_MEDIUM_NAME)) {
         return MediaTestFiles.FIRST_TEST_FILE_CONTENT;
      }
      return "";
   }

}
