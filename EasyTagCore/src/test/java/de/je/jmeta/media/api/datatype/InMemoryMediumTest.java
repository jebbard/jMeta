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

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.datatype.InMemoryMedium;

/**
 * {@link InMemoryMediumTest} tests the {@link InMemoryMedium} class.
 */
public class InMemoryMediumTest extends AbstractIMediumTest<byte[]> {

   private static final byte[] WRAPPED_MEDIUM = new byte[] { 1, 2, 3, 4, 5, 6 };

   private static final String EXTERNAL_NAME = "my name";

   private static final boolean READ_ONLY = true;

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getExpectedLength()
    */
   @Override
   protected long getExpectedLength() {

      return WRAPPED_MEDIUM.length;
   }

   /**
    * @see de.je.jmeta.media.api.datatype.AbstractIMediumTest#getMediumToTest()
    */
   @Override
   protected IMedium<byte[]> getMediumToTest() {

      return new InMemoryMedium(WRAPPED_MEDIUM, EXTERNAL_NAME, READ_ONLY);
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
   protected byte[] getExpectedWrappedMedium() {

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
