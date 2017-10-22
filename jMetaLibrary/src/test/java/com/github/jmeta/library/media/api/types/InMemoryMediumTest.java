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

/**
 * {@link InMemoryMediumTest} tests the {@link InMemoryMedium} class.
 */
public class InMemoryMediumTest extends AbstractIMediumTest<byte[]> {

   private static final byte[] WRAPPED_MEDIUM = new byte[] { 1, 2, 3, 4, 5, 6 };

   private static final String EXTERNAL_NAME = "my name";

   private static final boolean READ_ONLY = true;

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#getExpectedLength()
    */
   @Override
   protected long getExpectedLength() {

      return WRAPPED_MEDIUM.length;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.AbstractIMediumTest#getMediumToTest()
    */
   @Override
   protected IMedium<byte[]> getMediumToTest() {

      return new InMemoryMedium(WRAPPED_MEDIUM, EXTERNAL_NAME, READ_ONLY);
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
   protected byte[] getExpectedWrappedMedium() {

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