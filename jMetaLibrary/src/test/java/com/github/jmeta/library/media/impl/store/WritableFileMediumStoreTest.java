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
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.services.AbstractWritableRandomAccessMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.impl.mediumAccessor.FileMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link WritableFileMediumStoreTest} tests a {@link MediumStore} backed by {@link FileMedium} instances.
 */
public class WritableFileMediumStoreTest extends AbstractWritableRandomAccessMediumStoreTest<FileMedium> {

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
    */
   @Override
   protected FileMedium createEmptyMedium(String testMethodName) throws IOException {

      Path copiedFile = getCopiedFile(MediaTestFiles.EMPTY_TEST_FILE_PATH, "EMPTY_MEDIUM_", testMethodName);

      return new FileMedium(copiedFile, false);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createFilledMedium(java.lang.String,
    *      boolean, long, int, int)
    */
   @Override
   protected FileMedium createFilledMedium(String testMethodName, boolean enableCaching, long maxCacheSize,
      int maxCacheRegionSize, int maxReadWriteBlockSize) throws IOException {

      Path copiedFile = getCopiedFile(MediaTestFiles.FIRST_TEST_FILE_PATH, "FIRST_TEST_FILE_MEDIUM_", testMethodName);
      return new FileMedium(copiedFile, false, enableCaching, maxCacheSize, maxCacheRegionSize, maxReadWriteBlockSize);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createMediumAccessor(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected MediumAccessor<FileMedium> createMediumAccessor(FileMedium mediumToUse) {
      return new FileMediumAccessor(mediumToUse);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#getCurrentMediumContentAsString(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected String getCurrentMediumContentAsString(FileMedium medium) {
      return new String(MediaTestUtility.readFileContent(medium.getWrappedMedium()), Charsets.CHARSET_UTF8);
   }

   private Path getCopiedFile(Path pathToFile, String prefix, String testMethodName) throws IOException {
      Path copiedFile = Files.copy(pathToFile,
         MediaTestFiles.TEST_FILE_TEMP_OUTPUT_DIRECTORY_PATH.resolve(prefix + testMethodName + ".txt"),
         StandardCopyOption.REPLACE_EXISTING);
      return copiedFile;
   }
}
