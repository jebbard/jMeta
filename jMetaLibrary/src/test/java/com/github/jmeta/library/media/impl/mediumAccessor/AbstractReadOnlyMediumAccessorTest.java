/**
 * {@link AbstractReadOnlyMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 08.10.17 21:22:53 (October 8, 2017)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.ReadOnlyMediumException;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAccessType;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractReadOnlyMediumAccessorTest} is the base class for testing read-only {@link MediumAccessor} instances,
 * no matter if random-access or not.
 *
 * It thus has only some test cases to ensure write operations are not possible with the {@link MediumAccessor}.
 */
public abstract class AbstractReadOnlyMediumAccessorTest extends AbstractMediumAccessorTest {

   /**
    * Tests {@link MediumAccessor#truncate()}.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void truncate_onReadOnlyMedium_throwsException() {

      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      mediumAccessor.truncate();
   }

   /**
    * Tests {@link MediumAccessor#write(ByteBuffer)}.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void write_onReadOnlyMedium_throwsException() {

      MediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.open();

      mediumAccessor.write(ByteBuffer.wrap(new byte[1]));
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractMediumAccessorTest#validateTestMedium(com.github.jmeta.library.media.api.types.Medium)
    */
   @Override
   protected void validateTestMedium(Medium<?> theMedium) {
      if (theMedium.getMediumAccessType() != MediumAccessType.READ_ONLY) {
         throw new InvalidTestDataException("The test medium must be a read-only medium", null);
      }
   }

}
