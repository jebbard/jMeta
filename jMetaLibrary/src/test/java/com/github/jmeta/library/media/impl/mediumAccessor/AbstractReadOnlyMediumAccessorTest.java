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
import com.github.jmeta.library.media.api.types.IMedium;
import com.github.jmeta.library.media.api.types.IMediumReference;
import com.github.jmeta.utility.testsetup.api.exceptions.TestDataException;

/**
 * {@link AbstractReadOnlyMediumAccessorTest} is the base class for testing read-only {@link IMediumAccessor} instances,
 * no matter if random-access or not.
 * 
 * It thus has only some test cases to ensure write operations are not possible with the {@link IMediumAccessor}.
 */
public abstract class AbstractReadOnlyMediumAccessorTest extends AbstractIMediumAccessorTest {

   /**
    * Tests {@link IMediumAccessor#write(ByteBuffer)}.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void write_onReadOnlyMedium_throwsException() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.write(ByteBuffer.wrap(new byte[1]));
   }

   /**
    * Tests {@link IMediumAccessor#truncate(IMediumReference)}.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void truncate_onReadOnlyMedium_throwsException() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      mediumAccessor.truncate();
   }

   /**
    * @see com.github.jmeta.library.media.impl.mediumAccessor.AbstractIMediumAccessorTest#validateTestMedium(com.github.jmeta.library.media.api.types.IMedium)
    */
   @Override
   protected void validateTestMedium(IMedium<?> theMedium) {
      if (!theMedium.isReadOnly()) {
         throw new TestDataException("The test IMedium must be a read-only medium", null);
      }
   }

}
