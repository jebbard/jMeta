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

import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.services.AbstractReadOnlyMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.impl.mediumAccessor.FileMediumAccessor;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link ReadOnlyFileMediumStoreTest} tests a {@link MediumStore} backed by {@link FileMedium} instances.
 */
public class ReadOnlyFileMediumStoreTest extends AbstractReadOnlyMediumStoreTest<FileMedium> {

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
    */
   @Override
   protected FileMedium createEmptyMedium(String testMethodName) throws IOException {
      return new FileMedium(MediaTestFiles.EMPTY_TEST_FILE_PATH, true);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createFilledMedium(java.lang.String,
    *      boolean, long, int, int)
    */
   @Override
   protected FileMedium createFilledMedium(String testMethodName, boolean enableCaching, long maxCacheSize,
      int maxCacheRegionSize, int maxReadWriteBlockSize) throws IOException {
      return new FileMedium(MediaTestFiles.FIRST_TEST_FILE_PATH, true, enableCaching, maxCacheSize, maxCacheRegionSize,
         maxReadWriteBlockSize);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createMediumStoreToTest(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected MediumStore createMediumStoreToTest(FileMedium mediumToUse) {
      return new StandardMediumStore<>(new FileMediumAccessor(mediumToUse));
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#getCurrentMediumContentAsString(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected String getCurrentMediumContentAsString(FileMedium medium) {
      return new String(MediaTestUtility.readFileContent(medium.getWrappedMedium()), Charsets.CHARSET_UTF8);
   }
}
