/**
 *
 * {@link DummyMediumCreator}.java
 *
 * @author Jens
 *
 * @date 27.05.2015
 *
 */
package com.github.jmeta.library.media.api.helper;

import java.nio.file.Path;

import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;

// TODO dissolve this class, use TestMediumHelper
/**
 * {@link DummyMediumCreator}
 *
 */
public class DummyMediumCreator {

   /**
    * Creates an in-memory medium with the given content
    * 
    * @param bytes
    * @param name
    * @param readOnly
    * @return The {@link Medium}
    */
   public static Medium<?> createDummyInMemoryMedium(byte[] bytes, String name, boolean readOnly) {

      return new InMemoryMedium(bytes, name, readOnly);
   }

   /**
    * Creates a default in-memory medium with default content
    * 
    * @return The {@link Medium}
    */
   public static Medium<?> createDefaultDummyInMemoryMedium() {

      return new InMemoryMedium(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 }, "the dummy",
         false);
   }

   /**
    * Creates a file dummy {@link Medium} based on the given file
    * 
    * @param file
    * @param readOnly
    * @return The {@link Medium}
    */
   public static Medium<Path> createDummyFileMedium(Path file, boolean readOnly) {

      return new FileMedium(file, readOnly);
   }
}
