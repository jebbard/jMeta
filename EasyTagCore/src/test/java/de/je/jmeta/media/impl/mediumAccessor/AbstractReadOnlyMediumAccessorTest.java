/**
 * {@link AbstractReadOnlyMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 08.10.17 21:22:53 (October 8, 2017)
 */

package de.je.jmeta.media.impl.mediumAccessor;

import static de.je.jmeta.media.api.helper.TestMediumUtility.createReference;

import java.nio.ByteBuffer;

import org.junit.Test;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.exception.ReadOnlyMediumException;
import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.util.javautil.testUtil.setup.TestDataException;

/**
 * {@link AbstractReadOnlyMediumAccessorTest} is the base class for testing read-only {@link IMediumAccessor} instances,
 * no matter if random-access or not.
 * 
 * It thus has only some test cases to ensure write operations are not possible with the {@link IMediumAccessor}.
 */
public abstract class AbstractReadOnlyMediumAccessorTest extends AbstractIMediumAccessorTest {

   /**
    * {@link IMediumAccessor#write(IMediumReference, java.nio.ByteBuffer)}
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void write_onReadOnlyMedium_throwsException() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      final IMediumReference startReference = createReference(mediumAccessor.getMedium(), 0);
      mediumAccessor.write(startReference, ByteBuffer.wrap(new byte[1]));
   }

   /**
    * @see de.je.jmeta.media.impl.mediumAccessor.AbstractIMediumAccessorTest#validateTestMedium(de.je.jmeta.media.api.IMedium)
    */
   @Override
   protected void validateTestMedium(IMedium<?> theMedium) {
      if (!theMedium.isReadOnly()) {
         throw new TestDataException("The test IMedium must be a read-only medium", null);
      }
   }

}
