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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * {@link InputStreamMediumTest} tests the {@link InputStreamMedium} class.
 */
public class InputStreamMediumTest extends AbstractMediumTest<InputStream> {

   private static final InputStream WRAPPED_MEDIUM = new ByteArrayInputStream(new byte[] { 1, 2, 3, 4, 5, 6 });

   private static final String EXTERNAL_NAME = "your name";

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getExpectedAccessType()
    */
   @Override
   protected MediumAccessType getExpectedAccessType() {
      return MediumAccessType.READ_ONLY;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getExpectedExternalName()
    */
   @Override
   protected String getExpectedExternalName() {

      return InputStreamMediumTest.EXTERNAL_NAME;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getExpectedLength()
    */
   @Override
   protected long getExpectedLength() {

      return Medium.UNKNOWN_LENGTH;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getExpectedWrappedMedium()
    */
   @Override
   protected InputStream getExpectedWrappedMedium() {

      return InputStreamMediumTest.WRAPPED_MEDIUM;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getMediumToTest()
    */
   @Override
   protected Medium<InputStream> getMediumToTest() {

      return new InputStreamMedium(InputStreamMediumTest.WRAPPED_MEDIUM, InputStreamMediumTest.EXTERNAL_NAME);
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#isExpectedAsExisting()
    */
   @Override
   protected boolean isExpectedAsExisting() {

      return true;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#isExpectedAsRandomAccess()
    */
   @Override
   protected boolean isExpectedAsRandomAccess() {

      return false;
   }
}
