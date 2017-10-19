/**
 *
 * {@link InMemoryMediumTest}.java
 *
 * @author Jens
 *
 * @date 27.05.2015
 *
 */
package com.github.jmeta.library.media.api.type;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import com.github.jmeta.library.media.api.helper.MediaTestCaseConstants;
import com.github.jmeta.library.media.api.type.FileMedium;
import com.github.jmeta.library.media.api.type.IMedium;

/**
 * {@link ExistingFileMediumTest} tests the {@link FileMedium} class with an existing file.
 */
public class ExistingFileMediumTest extends AbstractIMediumTest<Path> {

   private static final Path WRAPPED_MEDIUM = MediaTestCaseConstants.STANDARD_TEST_FILE;

   private static final boolean READ_ONLY = false;

   /**
    * @see com.github.jmeta.library.media.api.type.AbstractIMediumTest#getMediumToTest()
    */
   @Override
   protected IMedium<Path> getMediumToTest() {

      return new FileMedium(WRAPPED_MEDIUM, READ_ONLY);
   }

   /**
    * @see com.github.jmeta.library.media.api.type.AbstractIMediumTest#isExpectedAsReadOnly()
    */
   @Override
   protected boolean isExpectedAsReadOnly() {

      return READ_ONLY;
   }

   /**
    * @see com.github.jmeta.library.media.api.type.AbstractIMediumTest#isExpectedAsRandomAccess()
    */
   @Override
   protected boolean isExpectedAsRandomAccess() {

      return true;
   }

   /**
    * @see com.github.jmeta.library.media.api.type.AbstractIMediumTest#getExpectedWrappedMedium()
    */
   @Override
   protected Path getExpectedWrappedMedium() {

      return WRAPPED_MEDIUM;
   }

   /**
    * @see com.github.jmeta.library.media.api.type.AbstractIMediumTest#getExpectedExternalName()
    */
   @Override
   protected String getExpectedExternalName() {

      return WRAPPED_MEDIUM.toAbsolutePath().toString();
   }

   /**
    * @see com.github.jmeta.library.media.api.type.AbstractIMediumTest#isExpectedAsExisting()
    */
   @Override
   protected boolean isExpectedAsExisting() {

      return true;
   }

   /**
    * @see com.github.jmeta.library.media.api.type.AbstractIMediumTest#getExpectedLength()
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
