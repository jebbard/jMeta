/**
 *
 * {@link FileMedium}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.api.types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FileMedium} represents a file that is both readable and writable.
 */
public class FileMedium extends AbstractMedium<Path> {

   /**
    * Creates a new {@link FileMedium} with default values for all properties that influence reading and writing.
    *
    * @param medium
    *           see {@link #FileMedium(Path, MediumAccessType, long, int)}
    * @param mediumAccessType
    *           see {@link #FileMedium(Path, MediumAccessType, long, int)}
    */
   public FileMedium(Path medium, MediumAccessType mediumAccessType) {
      this(medium, mediumAccessType, Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES,
         Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES);
   }

   /**
    * Creates a new {@link FileMedium} and allows to explicitly set all configuration properties that influence reading
    * and writing.
    *
    * @param medium
    *           The {@link Path} to use, must not be null, must be a file and must exist
    * @param mediumAccessType
    *           The {@link MediumAccessType} of the medium
    * @param maxCacheSizeInBytes
    *           see {@link Medium#getMaxCacheSizeInBytes()}, must be bigger than 0
    * @param maxReadWriteBlockSizeInBytes
    *           see {@link Medium#getMaxReadWriteBlockSizeInBytes()}
    */
   public FileMedium(Path medium, MediumAccessType mediumAccessType, long maxCacheSizeInBytes,
      int maxReadWriteBlockSizeInBytes) {
      super(medium, medium.toAbsolutePath().toString(), true, mediumAccessType, maxCacheSizeInBytes,
         maxReadWriteBlockSizeInBytes);

      Reject.ifFalse(Files.isRegularFile(medium), "Files.isRegularFile(medium)");
   }

   /**
    * @see com.github.jmeta.library.media.api.types.Medium#getCurrentLength()
    */
   @Override
   public long getCurrentLength() {

      try {
         return Files.size(getWrappedMedium());
      } catch (IOException e) {
         return Medium.UNKNOWN_LENGTH;
      }
   }
}
