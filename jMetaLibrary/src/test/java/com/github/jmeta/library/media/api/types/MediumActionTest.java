/**
 *
 * MediumActionTest.java
 *
 * @author Jens
 *
 * @date 20.05.2016
 *
 */
package com.github.jmeta.library.media.api.types;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffset;

/**
 * {@link MediumActionTest} tests the {@link MediumAction} class.
 */
public class MediumActionTest {

   private static final ByteBuffer DEFAULT_BYTES = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });
   private static final FileMedium MEDIUM = new FileMedium(MediaTestFiles.FIRST_TEST_FILE_PATH, true);

   /**
    * Tests the constructor and several getters, for an insert action.
    */
   @Test
   public void constructor_forInsertAction_initializesFieldsCorrectly() {

      MediumActionType expectedActionType = MediumActionType.INSERT;
      int expectedSequenceNumber = 11;
      ByteBuffer expectedActionBytes = DEFAULT_BYTES;
      MediumRegion expectedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 11),
         expectedActionBytes.remaining());

      MediumAction newAction = new MediumAction(expectedActionType, expectedRegion, expectedSequenceNumber,
         expectedActionBytes);

      checkCreatedAction(newAction, expectedActionType, expectedRegion, expectedActionBytes, expectedSequenceNumber);
   }

   /**
    * Tests the constructor and several getters, for a replace action.
    */
   @Test
   public void constructor_forReplaceAction_initializesFieldsCorrectly() {

      MediumActionType expectedActionType = MediumActionType.REPLACE;
      int expectedSequenceNumber = 11;
      ByteBuffer expectedActionBytes = DEFAULT_BYTES;
      MediumRegion expectedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 11), 22);

      MediumAction newAction = new MediumAction(expectedActionType, expectedRegion, expectedSequenceNumber,
         expectedActionBytes);

      checkCreatedAction(newAction, expectedActionType, expectedRegion, expectedActionBytes, expectedSequenceNumber);
   }

   /**
    * Tests the constructor and several getters, for a any action with bytes that is not replace or insert action.
    */
   @Test
   public void constructor_forWriteAction_initializesFieldsCorrectly() {

      MediumActionType expectedActionType = MediumActionType.WRITE;
      int expectedSequenceNumber = 11;
      ByteBuffer expectedActionBytes = null;
      MediumRegion expectedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 11), 4);

      MediumAction newAction = new MediumAction(expectedActionType, expectedRegion, expectedSequenceNumber,
         expectedActionBytes);

      checkCreatedAction(newAction, expectedActionType, expectedRegion, expectedActionBytes, expectedSequenceNumber);
   }

   /**
    * Tests the constructor and several getters, for a any action without bytes.
    */
   @Test
   public void constructor_forActionsWithoutBytes_initializesFieldsCorrectly() {

      MediumActionType expectedActionType = MediumActionType.TRUNCATE;
      int expectedSequenceNumber = 11;
      ByteBuffer expectedActionBytes = null;
      MediumRegion expectedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 11), 1);

      MediumAction newAction = new MediumAction(expectedActionType, expectedRegion, expectedSequenceNumber,
         expectedActionBytes);

      checkCreatedAction(newAction, expectedActionType, expectedRegion, expectedActionBytes, expectedSequenceNumber);
   }

   /**
    * Tests {@link MediumAction#setDone()}.
    */
   @Test
   public void done_changesIsPending() {

      MediumActionType expectedActionType = MediumActionType.INSERT;
      int expectedSequenceNumber = 0;
      ByteBuffer expectedActionBytes = DEFAULT_BYTES;
      MediumRegion expectedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 0),
         expectedActionBytes.remaining());

      MediumAction newAction = new MediumAction(expectedActionType, expectedRegion, expectedSequenceNumber,
         expectedActionBytes);

      newAction.setDone();

      Assert.assertFalse(newAction.isPending());
   }

   /**
    * Tests {@link MediumAction#getSizeDelta()}.
    */
   @Test
   public void getSizeDelta_returnsCorrectValuesForSizeChangingTypes() {

      ByteBuffer expectedActionBytes = DEFAULT_BYTES;
      MediumAction insertAction = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumOffset(MEDIUM, 0), expectedActionBytes.remaining()), 0,
         expectedActionBytes);

      int expectedInsertDelta = expectedActionBytes.remaining();

      Assert.assertEquals(expectedInsertDelta, insertAction.getSizeDelta());

      int bytesToRemove = 99;
      MediumAction removeAction = new MediumAction(MediumActionType.REMOVE,
         new MediumRegion(new StandardMediumOffset(MEDIUM, 0), bytesToRemove), 0, null);

      int expectedRemoveDelta = -bytesToRemove;

      Assert.assertEquals(expectedRemoveDelta, removeAction.getSizeDelta());

      int bytesTruncated = 99999;
      MediumAction truncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(new StandardMediumOffset(MEDIUM, 0), bytesTruncated), 0, null);

      int expectedTruncateDelta = -bytesTruncated;

      Assert.assertEquals(expectedTruncateDelta, truncateAction.getSizeDelta());

      int bytesToReplace1 = DEFAULT_BYTES.remaining() - 1;
      MediumAction insertingReplaceAction = new MediumAction(MediumActionType.REPLACE,
         new MediumRegion(new StandardMediumOffset(MEDIUM, 0), bytesToReplace1), 0, DEFAULT_BYTES);

      int expectedReplaceDelta1 = 1;

      Assert.assertEquals(expectedReplaceDelta1, insertingReplaceAction.getSizeDelta());

      int bytesToReplace2 = DEFAULT_BYTES.remaining() + 10;
      MediumAction removingReplaceAction = new MediumAction(MediumActionType.REPLACE,
         new MediumRegion(new StandardMediumOffset(MEDIUM, 0), bytesToReplace2), 0, DEFAULT_BYTES);

      int expectedReplaceDelta2 = -10;

      Assert.assertEquals(expectedReplaceDelta2, removingReplaceAction.getSizeDelta());
   }

   /**
    * Tests {@link MediumAction#getSizeDelta()}.
    */
   @Test
   public void getSizeDelta_returnsZeroForNonSizeChangingTypes() {

      MediumAction readAction = new MediumAction(MediumActionType.READ,
         new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 20), 0, null);

      Assert.assertEquals(0, readAction.getSizeDelta());

      MediumAction writeAction = new MediumAction(MediumActionType.READ,
         new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 20), 0, null);

      Assert.assertEquals(0, writeAction.getSizeDelta());
   }

   /**
    * @param newAction
    * @param expectedActionType
    * @param expectedRegion
    * @param expectedActionBytes
    * @param expectedSequenceNumber
    */
   private void checkCreatedAction(MediumAction newAction, MediumActionType expectedActionType,
      MediumRegion expectedRegion, ByteBuffer expectedActionBytes, int expectedSequenceNumber) {
      Assert.assertEquals(expectedActionType, newAction.getActionType());
      Assert.assertEquals(expectedSequenceNumber, newAction.getScheduleSequenceNumber());
      Assert.assertEquals(expectedActionBytes, newAction.getActionBytes());
      Assert.assertEquals(expectedRegion, newAction.getRegion());
      Assert.assertTrue(newAction.isPending());
   }
}
