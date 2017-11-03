/**
 *
 * {@link AbstractWritableRandomAccessMediumStoreTest}.java
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

import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * {@link AbstractWritableRandomAccessMediumStoreTest} tests the {@link MediumStore} interface for writable
 * random-access media. Thus it contains tests for all writing methods.
 *
 * @param <T>
 *           The concrete type of {@link Medium} to use
 */
public abstract class AbstractWritableRandomAccessMediumStoreTest<T extends Medium<?>>
   extends AbstractMediumStoreTest<T> {

   private static class ExpectedMediumContentBuilder {

      private final String originalContent;
      private String expectedContent = "";

      public ExpectedMediumContentBuilder(String originalContent) {
         this.originalContent = originalContent;
      }

      public void appendFromOriginal(int offset, int size) {
         expectedContent += originalContent.substring(offset, offset + size);
      }

      public void appendLiteralString(String literalString) {
         expectedContent += literalString;
      }

      public String buildExpectedContent() {
         return expectedContent;
      }
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
