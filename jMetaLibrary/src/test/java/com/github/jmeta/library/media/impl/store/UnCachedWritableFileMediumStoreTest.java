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
import java.nio.file.Path;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.services.AbstractUnCachedAndWritableRandomAccessMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.impl.mediumAccessor.FileMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link UnCachedWritableFileMediumStoreTest} tests a {@link MediumStore} backed by {@link FileMedium} instances and
 * without a cache.
 */
public class UnCachedWritableFileMediumStoreTest
   extends AbstractUnCachedAndWritableRandomAccessMediumStoreTest<FileMedium> {

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
    */
   @Override
   protected FileMedium createEmptyMedium(String testMethodName) throws IOException {

      Path copiedFile = getCopiedFile(TestMedia.EMPTY_TEST_FILE_PATH,
         "EMPTY_MEDIUM_" + getClass().getSimpleName() + "_", testMethodName);

      return new FileMedium(copiedFile, false);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createFilledMedium(java.lang.String,
    *      long, int)
    */
   @Override
   protected FileMedium createFilledMedium(String testMethodName, long maxCacheSize, int maxReadWriteBlockSize)
      throws IOException {

      Path copiedFile = getCopiedFile(TestMedia.FIRST_TEST_FILE_PATH, "FIRST_TEST_FILE_MEDIUM_", testMethodName);
      return new FileMedium(copiedFile, false, maxCacheSize, maxReadWriteBlockSize);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createMediumAccessor(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected MediumAccessor<FileMedium> createMediumAccessor(FileMedium mediumToUse) {
      return new FileMediumAccessor(mediumToUse);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#getMediumContentAsString(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected String getMediumContentAsString(FileMedium medium) {
      return new String(MediaTestUtility.readFileContent(medium.getWrappedMedium()), Charsets.CHARSET_UTF8);
   }
}
