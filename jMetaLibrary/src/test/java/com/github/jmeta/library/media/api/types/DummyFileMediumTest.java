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

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * {@link DummyFileMediumTest} tests the {@link FileMedium} class with a non-existing dummy file.
 */
public class DummyFileMediumTest extends AbstractIMediumTest<Path> {

   private static final Path WRAPPED_MEDIUM = Paths.get("./testFile.tst");

   private static final boolean READ_ONLY = false;

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#getExpectedLength()
    */
   @Override
   protected long getExpectedLength() {

      return IMedium.UNKNOWN_LENGTH;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#getMediumToTest()
    */
   @Override
   protected IMedium<Path> getMediumToTest() {

      return new FileMedium(WRAPPED_MEDIUM, READ_ONLY);
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#isExpectedAsReadOnly()
    */
   @Override
   protected boolean isExpectedAsReadOnly() {

      return READ_ONLY;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#isExpectedAsRandomAccess()
    */
   @Override
   protected boolean isExpectedAsRandomAccess() {

      return true;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#getExpectedWrappedMedium()
    */
   @Override
   protected Path getExpectedWrappedMedium() {

      return WRAPPED_MEDIUM;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#getExpectedExternalName()
    */
   @Override
   protected String getExpectedExternalName() {

      return WRAPPED_MEDIUM.toAbsolutePath().toString();
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#isExpectedAsExisting()
    */
   @Override
   protected boolean isExpectedAsExisting() {

      return false;
   }

}
