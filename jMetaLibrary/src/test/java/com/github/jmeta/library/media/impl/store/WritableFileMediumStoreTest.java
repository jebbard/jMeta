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

import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.services.AbstractWritableRandomAccessMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.impl.mediumAccessor.FileMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link WritableFileMediumStoreTest} tests a {@link MediumStore} backed by {@link FileMedium} instances.
 */
public class WritableFileMediumStoreTest extends AbstractWritableRandomAccessMediumStoreTest<FileMedium> {

   /**
    * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
    */
   @Override
   protected FileMedium createEmptyMedium(String testMethodName) throws IOException {

      Path copiedFile = getCopiedFile(TestMedia.EMPTY_TEST_FILE_PATH, "EMPTY_MEDIUM_", testMethodName);

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

   /**
    * Creates a copy of the indicated file in a temporary folder with the given name parts.
    * 
    * @param pathToFile
    *           The path to the file to copy
    * @param mediumType
    *           The type of medium as string, concatenated to the target file name
    * @param testMethodName
    *           The name of the test method currently executed, concatenated to the target file name
    * @return a Path to a copied file
    * @throws IOException
    *            if anything bad happens during I/O
    */
   private Path getCopiedFile(Path pathToFile, String mediumType, String testMethodName) throws IOException {
      Reject.ifNull(testMethodName, "testMethodName");
      Reject.ifNull(mediumType, "mediumType");
      Reject.ifNull(pathToFile, "pathToFile");

      Reject.ifFalse(Files.isRegularFile(pathToFile), "Files.isRegularFile(pathToFile)");

      Path copiedFile = Files.copy(pathToFile,
         TestMedia.TEST_FILE_TEMP_OUTPUT_DIRECTORY_PATH
            .resolve(getClass().getSimpleName() + "_" + mediumType + testMethodName + ".txt"),
         StandardCopyOption.REPLACE_EXISTING);
      return copiedFile;
   }
}
