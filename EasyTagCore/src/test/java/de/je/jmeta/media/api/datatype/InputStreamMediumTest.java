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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.datatype.InputStreamMedium;

/**
 * {@link InputStreamMediumTest} tests the {@link InputStreamMedium} class.
 */
public class InputStreamMediumTest extends AbstractIMediumTest<InputStream> {

   private static final InputStream WRAPPED_MEDIUM = new ByteArrayInputStream(
      new byte[] { 1, 2, 3, 4, 5, 6 });

   private static final String EXTERNAL_NAME = "your name";

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
   protected IMedium<InputStream> getMediumToTest() {

      return new InputStreamMedium(WRAPPED_MEDIUM, EXTERNAL_NAME);
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#isExpectedAsReadOnly()
    */
   @Override
   protected boolean isExpectedAsReadOnly() {

      return true;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#isExpectedAsRandomAccess()
    */
   @Override
   protected boolean isExpectedAsRandomAccess() {

      return false;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getExpectedWrappedMedium()
    */
   @Override
   protected InputStream getExpectedWrappedMedium() {

      return WRAPPED_MEDIUM;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getExpectedExternalName()
    */
   @Override
   protected String getExpectedExternalName() {

      return EXTERNAL_NAME;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#isExpectedAsExisting()
    */
   @Override
   protected boolean isExpectedAsExisting() {

      return true;
   }

}
