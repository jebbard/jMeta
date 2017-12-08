/**
 *
 * {@link MediaTestUtility}.java
 *
 * @author Jens Ebert
 *
 * @date 17.04.2017
 */
package com.github.jmeta.library.media.api.helper;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediaTestUtility} offers general helper methods for testing the media components.
 */
public class MediaTestUtility {

   /**
    * A test {@link Medium} to be used whenever it does not matter which exact type and properties the {@link Medium}
    * used has. This is a read-only {@link FileMedium} with default caching and read-write block size, pointing to an
    * existing file {@link MediaTestFiles#FIRST_TEST_FILE_PATH}.
    */
   public static final FileMedium DEFAULT_TEST_MEDIUM = new FileMedium(MediaTestFiles.FIRST_TEST_FILE_PATH, true);

   /**
    * A second test {@link Medium} to be used whenever checks require another {@link Medium} than the
    * {@link #DEFAULT_TEST_MEDIUM}.
    */
   public static final InMemoryMedium OTHER_MEDIUM = new InMemoryMedium(new byte[] {}, "Fake", false);

   /**
    * Reads the content of a file and returns it.
    * 
    * @param filePath
    *           the file path, must exist
    * @return contents of the file as byte array.
    */
   public static byte[] readFileContent(Path filePath) {
      Reject.ifNull(filePath, "filePath");
      Reject.ifFalse(Files.isRegularFile(filePath), "Files.isRegularFile(filePath)");

      try (RandomAccessFile raf = new RandomAccessFile(filePath.toFile(), "r")) {
         int size = (int) Files.size(filePath);
         byte[] bytesReadBuffer = new byte[size];

         int bytesRead = 0;
         while (bytesRead < size) {
            int readReturn = raf.read(bytesReadBuffer, bytesRead, size - bytesRead);

            if (readReturn == -1)
               throw new RuntimeException("Unexpected EOF");

            bytesRead += readReturn;
         }

         return bytesReadBuffer;
      } catch (Exception e) {
         throw new RuntimeException("Unexpected exception during reading of file <" + filePath + ">", e);
      }
   }

   /**
    * Convenience method returning a new {@link MediumOffset} pointing to the specified offset on the given
    * {@link Medium}. Also encapsulates the use of the {@link MediumOffset} implementation class in test cases to just a
    * few places.
    * 
    * @param medium
    *           The {@link Medium} to use
    * @param offset
    *           The offset to use
    * @return a new {@link MediumOffset} pointing to the specified offset on the specified {@link Medium}
    */
   public static MediumOffset at(Medium<?> medium, long offset) {
      return new StandardMediumOffset(medium, offset);
   }

   /**
    * Convenience method returning a new {@link MediumOffset} pointing to the specified offset on the
    * {@link #DEFAULT_TEST_MEDIUM}. Also encapsulates the use of the {@link MediumOffset} implementation class in test
    * cases to just a few places.
    * 
    * @param offset
    *           The offset to use
    * @return a new {@link MediumOffset} pointing to the specified offset on the default {@link #DEFAULT_TEST_MEDIUM}
    */
   public static MediumOffset at(long offset) {
      return at(DEFAULT_TEST_MEDIUM, offset);
   }

   private MediaTestUtility() {

   }
}
