/**
 *
 * {@link AbstractReadOnlyMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.api.services;

import static com.github.jmeta.library.media.api.helper.MediaTestUtility.at;

import java.nio.ByteBuffer;

import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.ReadOnlyMediumException;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumRegion;

/**
 * {@link AbstractReadOnlyMediumStoreTest} contains negative tests for {@link MediumStore} write method on read-only
 * {@link Medium} instances.
 *
 * @param <T>
 *           The type of {@link Medium} to test
 */
public abstract class AbstractReadOnlyMediumStoreTest<T extends Medium<?>> extends AbstractMediumStoreTest<T> {

   /**
    * Tests {@link MediumStore#replaceData(MediumReference, int, java.nio.ByteBuffer)}.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void replaceData_onReadOnlyMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.replaceData(at(emptyMedium, 10), 20, ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#removeData(MediumReference, int)}.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void removeData_onReadOnlyMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.removeData(at(emptyMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#insertData(MediumReference, ByteBuffer)}.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void insertData_onReadOnlyMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.insertData(at(emptyMedium, 10), ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)}.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void undo_onReadOnlyMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest
         .undo(new MediumAction(MediumActionType.REMOVE, new MediumRegion(at(emptyMedium, 10), 20), 0, null));
   }

   /**
    * Tests {@link MediumStore#flush()}.
    */
   @Test(expected = ReadOnlyMediumException.class)
   public void flush_onReadOnlyMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.flush();
   }
}
