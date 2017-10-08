/**
 *
 * {@link InMemoryMediumTest}.java
 *
 * @author Jens
 *
 * @date 27.05.2015
 *
 */
package de.je.jmeta.media.api.datatype;

import java.nio.file.Path;
import java.nio.file.Paths;

import de.je.jmeta.media.api.IMedium;

/**
 * {@link DummyFileMediumTest} tests the {@link FileMedium} class with a non-existing dummy file.
 */
public class DummyFileMediumTest extends AbstractIMediumTest<Path> {

   private static final Path WRAPPED_MEDIUM = Paths.get("./testFile.tst");

   private static final boolean READ_ONLY = false;

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getExpectedLength()
    */
   @Override
   protected long getExpectedLength() {

      return IMedium.UNKNOWN_LENGTH;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getMediumToTest()
    */
   @Override
   protected IMedium<Path> getMediumToTest() {

      return new FileMedium(WRAPPED_MEDIUM, READ_ONLY);
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#isExpectedAsReadOnly()
    */
   @Override
   protected boolean isExpectedAsReadOnly() {

      return READ_ONLY;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#isExpectedAsRandomAccess()
    */
   @Override
   protected boolean isExpectedAsRandomAccess() {

      return true;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getExpectedWrappedMedium()
    */
   @Override
   protected Path getExpectedWrappedMedium() {

      return WRAPPED_MEDIUM;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getExpectedExternalName()
    */
   @Override
   protected String getExpectedExternalName() {

      return WRAPPED_MEDIUM.toAbsolutePath().toString();
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#isExpectedAsExisting()
    */
   @Override
   protected boolean isExpectedAsExisting() {

      return false;
   }

}
