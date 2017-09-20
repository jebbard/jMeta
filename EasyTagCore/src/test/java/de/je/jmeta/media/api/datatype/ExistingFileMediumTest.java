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

import java.io.File;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.MediaTestCaseConstants;

/**
 * {@link ExistingFileMediumTest} tests the {@link FileMedium} class with an existing file.
 */
public class ExistingFileMediumTest extends AbstractIMediumTest<File> {

   private static final File WRAPPED_MEDIUM = MediaTestCaseConstants.STANDARD_TEST_FILE;

   private static final boolean READ_ONLY = false;

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getMediumToTest()
    */
   @Override
   protected IMedium<File> getMediumToTest() {

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
   protected File getExpectedWrappedMedium() {

      return WRAPPED_MEDIUM;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getExpectedExternalName()
    */
   @Override
   protected String getExpectedExternalName() {

      return WRAPPED_MEDIUM.getAbsolutePath();
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#isExpectedAsExisting()
    */
   @Override
   protected boolean isExpectedAsExisting() {

      return true;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getExpectedLength()
    */
   @Override
   protected long getExpectedLength() {

      return WRAPPED_MEDIUM.length();
   }

}
