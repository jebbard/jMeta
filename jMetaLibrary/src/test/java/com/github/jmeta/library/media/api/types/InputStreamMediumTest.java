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
public class InputStreamMediumTest extends AbstractIMediumTest<InputStream> {

   private static final InputStream WRAPPED_MEDIUM = new ByteArrayInputStream(
      new byte[] { 1, 2, 3, 4, 5, 6 });

   private static final String EXTERNAL_NAME = "your name";

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
   protected IMedium<InputStream> getMediumToTest() {

      return new InputStreamMedium(WRAPPED_MEDIUM, EXTERNAL_NAME);
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#isExpectedAsReadOnly()
    */
   @Override
   protected boolean isExpectedAsReadOnly() {

      return true;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#isExpectedAsRandomAccess()
    */
   @Override
   protected boolean isExpectedAsRandomAccess() {

      return false;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#getExpectedWrappedMedium()
    */
   @Override
   protected InputStream getExpectedWrappedMedium() {

      return WRAPPED_MEDIUM;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#getExpectedExternalName()
    */
   @Override
   protected String getExpectedExternalName() {

      return EXTERNAL_NAME;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#isExpectedAsExisting()
    */
   @Override
   protected boolean isExpectedAsExisting() {

      return true;
   }

}
