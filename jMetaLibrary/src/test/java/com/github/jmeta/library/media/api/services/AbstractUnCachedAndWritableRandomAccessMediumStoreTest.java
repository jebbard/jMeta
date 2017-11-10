/**
 *
 * {@link AbstractCachedAndWritableRandomAccessMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.api.services;

import static com.github.jmeta.library.media.api.helper.MediaTestUtility.at;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * {@link AbstractUnCachedAndWritableRandomAccessMediumStoreTest} tests the {@link MediumStore} interface for writable
 * random-access and uncached media. It contains tests specializing on writing to uncached media. In addition, it
 * contains all negative tests for writing methods.
 *
 * @param <T>
 *           The concrete type of {@link Medium} to use
 */
public abstract class AbstractUnCachedAndWritableRandomAccessMediumStoreTest<T extends Medium<?>>
   extends AbstractUnCachedMediumStoreTest<T> {

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forRandomAccessMediumAtEOM_returnsTrue() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      int endOfMediumOffset = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      Assert.assertTrue(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, endOfMediumOffset)));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test
   public void isAtEndOfMedium_forRandomAccessMediumBeforeEOM_returnsFalse() {
      mediumStoreUnderTest = createFilledUncachedMediumStore();

      int endOfMediumOffset = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      Assert.assertFalse(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, endOfMediumOffset / 2)));
   }

   /**
    * Tests {@link MediumStore#replaceData(MediumReference, int, java.nio.ByteBuffer)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void replaceData_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.replaceData(at(currentMedium, 10), 20, ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void replaceData_forInvalidReference_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(at(MediaTestUtility.OTHER_MEDIUM, 10), 20, ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#removeData(MediumReference, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void removeData_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.removeData(at(currentMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#removeData(MediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void removeData_forInvalidReference_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.removeData(at(MediaTestUtility.OTHER_MEDIUM, 10), 20);
   }

   /**
    * Tests {@link MediumStore#insertData(MediumReference, ByteBuffer)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void insertData_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.insertData(at(currentMedium, 10), ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#insertData(MediumReference, ByteBuffer)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void insertData_forInvalidReference_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.insertData(at(MediaTestUtility.OTHER_MEDIUM, 10), ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void undo_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest
         .undo(new MediumAction(MediumActionType.REMOVE, new MediumRegion(at(currentMedium, 10), 20), 0, null));
   }

   /**
    * Tests {@link MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void undo_forInvalidReference_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.undo(new MediumAction(MediumActionType.REMOVE,
         new MediumRegion(at(MediaTestUtility.OTHER_MEDIUM, 10), 20), 0, null));
   }

   /**
    * Tests {@link MediumStore#flush()}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void flush_onClosedMediumStore_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.flush();
   }
}
