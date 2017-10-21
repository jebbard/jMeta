/**
 *
 * {@link FileMedium}.java
 *
 * @author Jens
 *
 * @date 16.05.2015
 *
 */
package com.github.jmeta.library.media.api.type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * {@link FileMedium} represents a file that is both readable and writable.
 */
public class FileMedium extends AbstractMedium<Path> {

   /**
    * Creates a new {@link FileMedium}.
    * 
    * @param medium
    *           The {@link Path} this {@link FileMedium} refers to, must be a file.
    * @param readOnly
    *           true to make this {@link FileMedium} read-only, false enables read and write.
    */
   public FileMedium(Path medium, boolean readOnly) {
      super(medium, medium.toAbsolutePath().toString(), true, readOnly, true);
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMedium#exists()
    */
   @Override
   public boolean exists() {

      return Files.exists(getWrappedMedium());
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMedium#getCurrentLength()
    */
   @Override
   public long getCurrentLength() {

      try {
         return Files.size(getWrappedMedium());
      } catch (IOException e) {
         return IMedium.UNKNOWN_LENGTH;
      }
   }
}
