package de.je.jmeta.media.impl.mediumAccessor;

import static de.je.jmeta.media.impl.TestMediumUtility.createReference;

import java.nio.ByteBuffer;

import org.junit.Test;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.exception.ReadOnlyMediumException;
import de.je.jmeta.media.impl.IMediumAccessor;
import de.je.util.javautil.testUtil.setup.TestDataException;

public abstract class AbstractReadOnlyMediumAccessorTest extends AbstractIMediumAccessorTest {

   @Override
   protected void validateTestMedium(IMedium<?> theMedium) {
      if (!theMedium.isReadOnly()) {
         throw new TestDataException("The test IMedium must be a read-only medium", null);
      }
   }

   /**
    * {@link IMediumAccessor#write(IMediumReference, java.nio.ByteBuffer)}
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void write_onReadOnlyMedium_throwsException() {

      IMediumAccessor<?> mediumAccessor = getImplementationToTest();

      final IMediumReference startReference = createReference(mediumAccessor.getMedium(), 0);
      mediumAccessor.write(startReference, ByteBuffer.wrap(new byte[1]));
   }

}
