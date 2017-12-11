/**
 *
 * {@link InMemoryMediumTest}.java
 *
 * @author Jens
 *
 * @date 27.05.2015
 *
 */
package com.github.jmeta.library.media.api.types;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.jmeta.library.media.api.helper.TestMedia;

/**
 * {@link FileMediumTest} tests the {@link FileMedium} class with an existing file.
 */
public class FileMediumTest extends AbstractMediumTest<Path> {

   private static final Path WRAPPED_MEDIUM = TestMedia.FIRST_TEST_FILE_PATH;

   private static final boolean READ_ONLY = false;

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getMediumToTest()
    */
   @Override
   protected Medium<Path> getMediumToTest() {

      return new FileMedium(WRAPPED_MEDIUM, READ_ONLY);
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#isExpectedAsReadOnly()
    */
   @Override
   protected boolean isExpectedAsReadOnly() {

      return READ_ONLY;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#isExpectedAsRandomAccess()
    */
   @Override
   protected boolean isExpectedAsRandomAccess() {

      return true;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getExpectedWrappedMedium()
    */
   @Override
   protected Path getExpectedWrappedMedium() {

      return WRAPPED_MEDIUM;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getExpectedExternalName()
    */
   @Override
   protected String getExpectedExternalName() {

      return WRAPPED_MEDIUM.toAbsolutePath().toString();
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#isExpectedAsExisting()
    */
   @Override
   protected boolean isExpectedAsExisting() {

      return true;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getExpectedLength()
    */
   @Override
   protected long getExpectedLength() {

      try {
         return Files.size(WRAPPED_MEDIUM);
      } catch (IOException e) {
         throw new RuntimeException(e);
      }
   }

}
