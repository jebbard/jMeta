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
import java.nio.file.Paths;

import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.IMedium;
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
    * @return The {@link IMedium}
    */
   public static IMedium<?> createDummyInMemoryMedium(byte[] bytes, String name, boolean readOnly) {

      return new InMemoryMedium(bytes, name, readOnly);
   }

   /**
    * Creates an in-memory medium with the given content
    * 
    * @param bytes
    * @param name
    * @return The {@link IMedium}
    */
   public static IMedium<?> createDummyInMemoryMedium(byte[] bytes, String name) {

      return new InMemoryMedium(bytes, name, false);
   }

   /**
    * Creates an in-memory medium with the given content
    * 
    * @param bytes
    * @return The {@link IMedium}
    */
   public static IMedium<?> createDummyInMemoryMedium(byte[] bytes) {

      return new InMemoryMedium(bytes, "the dummy", false);
   }

   /**
    * Creates a default in-memory medium with default content
    * 
    * @return The {@link IMedium}
    */
   public static IMedium<?> createDefaultDummyInMemoryMedium() {

      return new InMemoryMedium(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 }, "the dummy",
         false);
   }

   /**
    * @return the default file dummy medium
    */
   public static IMedium<Path> createDefaultDummyFileMedium() {

      return createDummyFileMedium(Paths.get("."), false);
   }

   /**
    * Creates a file dummy {@link IMedium} based on the given file
    * 
    * @param file
    * @param readOnly
    * @return The {@link IMedium}
    */
   public static IMedium<Path> createDummyFileMedium(Path file, boolean readOnly) {

      return new FileMedium(file, readOnly);
   }
}
