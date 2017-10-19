/**
 *
 * MediumChangeManagerCreateFlushPlanTest.java
 *
 * @author Jens
 *
 * @date 16.10.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import static com.github.jmeta.library.media.api.helper.MediaTestHelper.at;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.github.jmeta.library.media.api.helper.MediaTestHelper;
import com.github.jmeta.library.media.api.type.MediumAction;
import com.github.jmeta.library.media.api.type.MediumActionType;
import com.github.jmeta.library.media.api.type.MediumRegion;
import com.github.jmeta.library.media.impl.changeManager.ReadWriteActionSequence.ActionOrder;
import com.github.jmeta.library.media.impl.reference.MediumReferenceFactory;

import de.je.jmeta.testHelpers.basics.JMetaTestBasics;
import de.je.util.javautil.testUtil.setup.TestDataException;

/**
 * {@link MediumChangeManagerCreateFlushPlanTest} checks the quite complex method
 * {@link MediumChangeManager#createFlushPlan(int, long)} in all its variations. Note that the test case methods refer
 * to test case ids in their currentTestName (as prefix) which are listed in the design concept.
 *
 * This test class uses a concept to somehow simplify the specification of the expectation of {@link MediumAction}s that
 * are contained in the flush plan - without re-programming the create flush plan algorithm in this test class:
 * {@link ExpectedActionSequence}s. They encapsulate sequences of {@link MediumAction}s that form a logical block. See
 * javadocs of {@link ReadWriteActionSequence}, {@link WriteActionSequence} and {@link SingleActionSequence} for
 * details.
 */
public class MediumChangeManagerCreateFlushPlanTest {

   /**
    * The currently executing test currentTestName (must be public)
    */
   @Rule
   public TestName currentTestName = new TestName();

   private PrintStream currentDumpStreamExpected;
   private PrintStream currentDumpStreamActual;

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {
      File mediaFlushTestPath = new File(JMetaTestBasics.DEFAULT_LOG_PATH, "mediaCreateFlushPlan");

      currentDumpStreamExpected = setupTestFile(currentTestName.getMethodName() + "__EXPECTED", mediaFlushTestPath);
      currentDumpStreamActual = setupTestFile(currentTestName.getMethodName() + "__ACTUAL", mediaFlushTestPath);
   }

   /**
    * Tears down the test fixtures.
    */
   @After
   public void tearDown() {
      if (currentDumpStreamExpected != null) {
         currentDumpStreamExpected.close();
      }

      if (currentDumpStreamActual != null) {
         currentDumpStreamActual.close();
      }
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF0_forEmptyChangeManager_returnsEmptyList() {

      MediumChangeManager testling = getTestling();

      List<MediumAction> flushPlan = testling.createFlushPlan(1, 1);

      Assert.assertTrue(flushPlan.isEmpty());
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF1a_singleInsert_middle_oneSmallBlockBehind_smallInsertBlock_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 20;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 12;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);
      // Bytes behind first insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - insertOffset1;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), totalRWSizeInBytes1, insertSize1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF1b_singleInsert_start_onlyFullBlocksBehind_fullBlockSizeInsertBlock_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 800;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 200;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 0;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);
      // Bytes behind first insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - insertOffset1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount1, writeBlockSizeInBytes,
            insertSize1, ActionOrder.BACKWARD),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF1c_singleInsert_middle_multiBlocksWithRemainder_multiInsertBlocksWithRemainder_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1000;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 450;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 12;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);
      // Bytes behind first insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - insertOffset1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;
      // Write block for first insert
      int writeBlockCount1 = insertSize1 / writeBlockSizeInBytes;
      int writeRemainderSizeInBytes1 = insertSize1 % writeBlockSizeInBytes;
      ByteBuffer remainingWriteBuffer1 = ByteBuffer.wrap(insertBuffer1.array(),
         writeBlockCount1 * writeBlockSizeInBytes, writeRemainderSizeInBytes1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount1, writeBlockSizeInBytes,
            insertSize1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), readWriteRemainderSizeInBytes1, insertSize1),
         new WriteActionSequence(at(insertOffset1), writeBlockCount1, writeBlockSizeInBytes, insertBuffer1),
         new WriteActionSequence(at(insertOffset1 + writeBlockCount1 * writeBlockSizeInBytes), 1,
            writeRemainderSizeInBytes1, remainingWriteBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF1d_singleInsert_end_smallInsertBlock_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 20;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = totalMediumSizeInBytes;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF2a_singleRemove_middle_oneSmallBlockBehind_smallRemoveSize_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 150;
      int writeBlockSizeInBytes = 200;

      // First remove
      int removeOffset1 = 12;
      int removeSize1 = 35;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes - removeSize1), removeSize1), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(removeOffset1 + removeSize1),
            totalMediumSizeInBytes - removeOffset1 - removeSize1, -removeSize1),
         new SingleActionSequence(removeAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF2b_singleRemove_start_largerBlockBehind_biggerRemoveSize_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // First remove
      int removeOffset1 = 0;
      int removeSize1 = 300;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));
      // Bytes behind first remove
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - removeOffset1 - removeSize1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes - removeSize1), removeSize1), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(removeOffset1 + removeSize1), readWriteBlockCount1, writeBlockSizeInBytes,
            -removeSize1, ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(
            at(removeOffset1 + removeSize1 + readWriteBlockCount1 * writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes1, -removeSize1),
         new SingleActionSequence(removeAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF2c_singleRemove_end_NoBytesBehind_biggerRemoveSize_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // First remove
      int removeSize1 = 400;
      int removeOffset1 = totalMediumSizeInBytes - removeSize1;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes - removeSize1), removeSize1), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new SingleActionSequence(removeAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF2d_singleRemove_removeWholeMedium_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // First remove
      int removeOffset1 = 0;
      int removeSize1 = totalMediumSizeInBytes;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes - removeSize1), removeSize1), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new SingleActionSequence(removeAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF3a_singleInsertingReplace_start_oneSmallBlockBehind_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 150;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 350;
      int replacedByteCount1 = 35;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);
      int remainingWriteByteCount1 = replaceSize1 % writeBlockSizeInBytes;
      ByteBuffer remainingWriteBuffer1 = ByteBuffer.wrap(replacementBuffer1.array(), writeBlockSizeInBytes,
         remainingWriteByteCount1);

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1),
            totalMediumSizeInBytes - replaceOffset1 - replacedByteCount1, replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, writeBlockSizeInBytes, replacementBuffer1),
         new WriteActionSequence(at(replaceOffset1 + writeBlockSizeInBytes), 1, remainingWriteByteCount1,
            remainingWriteBuffer1),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF3b_singleInsertingReplace_middle_biggerBlocksBehind_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 500;
      int replaceSize1 = 50;
      int replacedByteCount1 = 35;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - replaceOffset1 - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int remainingWriteBlockSize1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount1, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1), remainingWriteBlockSize1,
            replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF3c_singleInsertingReplace_end_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 1450;
      int replaceSize1 = 350;
      int replacedByteCount1 = 50;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);

      int remainingWriteByteCount1 = replaceSize1 % writeBlockSizeInBytes;
      ByteBuffer remainingWriteBuffer1 = ByteBuffer.wrap(replacementBuffer1.array(), writeBlockSizeInBytes,
         remainingWriteByteCount1);

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 1, writeBlockSizeInBytes, replacementBuffer1),
         new WriteActionSequence(at(replaceOffset1 + writeBlockSizeInBytes), 1, remainingWriteByteCount1,
            remainingWriteBuffer1),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF3d_singleRemovingReplace_start_multipleWholeBlocksBehind_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 900;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 100;
      int replacedByteCount1 = 300;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE, new MediumRegion(
         at(totalMediumSizeInBytes + replaceSize1 - replacedByteCount1), replacedByteCount1 - replaceSize1), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(replaceOffset1 + replacedByteCount1), readWriteBlockCount1,
            writeBlockSizeInBytes, replaceSize1 - replacedByteCount1, ActionOrder.FORWARD),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF3e_singleRemovingReplace_middle_smallBlockBehind_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 300;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 150;
      int replaceSize1 = 80;
      int replacedByteCount1 = 100;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - replaceOffset1 - replacedByteCount1;

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE, new MediumRegion(
         at(totalMediumSizeInBytes + replaceSize1 - replacedByteCount1), replacedByteCount1 - replaceSize1), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1), totalRWSizeInBytes1,
            replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF3f_singleRemovingReplace_end_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 300;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 280;
      int replaceSize1 = 10;
      int replacedByteCount1 = 20;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE, new MediumRegion(
         at(totalMediumSizeInBytes + replaceSize1 - replacedByteCount1), replacedByteCount1 - replaceSize1), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF3g_singleOverwritingReplace_middle_smallBlockBehind_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 300;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 150;
      int replaceSize1 = 80;
      int replacedByteCount1 = replaceSize1;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF3h_singleInsertingReplace_replaceWholeMedium_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 300;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 400;
      int replacedByteCount1 = totalMediumSizeInBytes;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 2, writeBlockSizeInBytes, replacementBuffer1),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF4a_multiInsert_start_allSameOffset_smallInsertBlockSizes_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 450;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 0;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertSize2 = 20;
      ByteBuffer insertBuffer2 = MediaTestHelper.createTestByteBufferOfSize(insertSize2);
      int insertOffset2 = 0;
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);
      // Bytes behind second insert
      int totalRWSizeInBytes2 = totalMediumSizeInBytes - insertOffset2;
      int readWriteBlockCount2 = totalRWSizeInBytes2 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes2 = totalRWSizeInBytes2 % writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount2, writeBlockSizeInBytes,
            insertSize1 + insertSize2, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), readWriteRemainderSizeInBytes2,
            insertSize1 + insertSize2),
         new WriteActionSequence(at(insertOffset2 + insertSize1), 1, insertSize2, insertBuffer2),
         new SingleActionSequence(insertAction2),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF4b_multiInsert_start_allSameOffset_differentInsertBlockSizes_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1000;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 0;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertSize2 = 450;
      ByteBuffer insertBuffer2 = MediaTestHelper.createTestByteBufferOfSize(insertSize2);
      int insertOffset2 = 0;
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);
      // Write block for second insert
      int writeBlockCount2 = insertSize2 / writeBlockSizeInBytes;
      int writeRemainderSizeInBytes2 = insertSize2 % writeBlockSizeInBytes;
      ByteBuffer remainingWriteBuffer2 = ByteBuffer.wrap(insertBuffer2.array(),
         writeBlockCount2 * writeBlockSizeInBytes, writeRemainderSizeInBytes2);

      // Third insert
      int insertSize3 = 20;
      ByteBuffer insertBuffer3 = MediaTestHelper.createTestByteBufferOfSize(insertSize3);
      int insertOffset3 = 0;
      MediumAction insertAction3 = testling.scheduleInsert(new MediumRegion(at(insertOffset3), insertSize3),
         insertBuffer3);
      // Bytes behind third insert
      int totalRWSizeInBytes3 = totalMediumSizeInBytes - insertOffset3;
      int readWriteBlockCount3 = totalRWSizeInBytes3 / writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount3, writeBlockSizeInBytes,
            insertSize1 + insertSize2 + insertSize3, ActionOrder.BACKWARD),
         new WriteActionSequence(at(insertOffset3 + insertSize1 + insertSize2), 1, insertSize3, insertBuffer3),
         new SingleActionSequence(insertAction3),
         new WriteActionSequence(at(insertOffset2 + insertSize1), writeBlockCount2, writeBlockSizeInBytes,
            insertBuffer2),
         new WriteActionSequence(at(insertOffset2 + writeBlockCount2 * writeBlockSizeInBytes + insertSize1), 1,
            writeRemainderSizeInBytes2, remainingWriteBuffer2),
         new SingleActionSequence(insertAction2),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF4c_multiInsert_middle_differentOffsets_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 450;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 200;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertSize2 = 20;
      ByteBuffer insertBuffer2 = MediaTestHelper.createTestByteBufferOfSize(insertSize2);
      int insertOffset2 = 250;
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(insertOffset2), writeBlockSizeInBytes, insertSize1 + insertSize2),
         new WriteActionSequence(at(insertOffset2 + insertSize1), 1, insertSize2, insertBuffer2),
         new SingleActionSequence(insertAction2),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), insertOffset2 - insertOffset1, insertSize1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF4d_multiInsert_untilEnd_differentOffsets_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 450;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 449;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertSize2 = 20;
      ByteBuffer insertBuffer2 = MediaTestHelper.createTestByteBufferOfSize(insertSize2);
      int insertOffset2 = 450;
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(insertOffset2 + insertSize1), 1, insertSize2, insertBuffer2),
         new SingleActionSequence(insertAction2),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), insertOffset2 - insertOffset1, insertSize1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF4e_multiInsert_middle_differentOffsets_variousInsertAndBehindSizes_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1000;
      int writeBlockSizeInBytes = 150;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 200;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertSize2 = 20;
      ByteBuffer insertBuffer2 = MediaTestHelper.createTestByteBufferOfSize(insertSize2);
      int insertOffset2 = 250;
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);

      // Third insert
      int insertSize3 = 333;
      ByteBuffer insertBuffer3 = MediaTestHelper.createTestByteBufferOfSize(insertSize3);
      int insertOffset3 = 500;
      MediumAction insertAction3 = testling.scheduleInsert(new MediumRegion(at(insertOffset3), insertSize3),
         insertBuffer3);

      // Fourth insert
      int insertSize4 = 250;
      ByteBuffer insertBuffer4 = MediaTestHelper.createTestByteBufferOfSize(insertSize4);
      int insertOffset4 = 520;
      MediumAction insertAction4 = testling.scheduleInsert(new MediumRegion(at(insertOffset4), insertSize4),
         insertBuffer4);

      // Fifth insert
      int insertSize5 = 10;
      ByteBuffer insertBuffer5 = MediaTestHelper.createTestByteBufferOfSize(insertSize5);
      int insertOffset5 = 521;
      MediumAction insertAction5 = testling.scheduleInsert(new MediumRegion(at(insertOffset5), insertSize5),
         insertBuffer5);

      // Bytes behind fifth insert
      int totalRWSizeInBytes5 = totalMediumSizeInBytes - insertOffset5;
      int readWriteBlockCount5 = totalRWSizeInBytes5 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes5 = totalRWSizeInBytes5 % writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount5, writeBlockSizeInBytes,
            insertSize1 + insertSize2 + insertSize3 + insertSize4 + insertSize5, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset5), readWriteRemainderSizeInBytes5,
            insertSize1 + insertSize2 + insertSize3 + insertSize4 + insertSize5),
         new WriteActionSequence(at(insertOffset5 + insertSize1 + insertSize2 + insertSize3 + insertSize4), 1,
            insertSize5, insertBuffer5),
         new SingleActionSequence(insertAction5),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset4), insertOffset5 - insertOffset4,
            insertSize1 + insertSize2 + insertSize3 + insertSize4),
         new WriteActionSequence(at(insertOffset4 + insertSize1 + insertSize2 + insertSize3), 1, writeBlockSizeInBytes,
            insertBuffer4),
         new WriteActionSequence(at(insertOffset4 + writeBlockSizeInBytes + insertSize1 + insertSize2 + insertSize3), 1,
            insertSize4 % writeBlockSizeInBytes, insertBuffer4),
         new SingleActionSequence(insertAction4),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset3), insertOffset4 - insertOffset3,
            insertSize1 + insertSize2 + insertSize3),
         new WriteActionSequence(at(insertOffset3 + insertSize1 + insertSize2), 2, writeBlockSizeInBytes,
            insertBuffer3),
         new WriteActionSequence(at(insertOffset3 + 2 * writeBlockSizeInBytes + insertSize1 + insertSize2), 1,
            insertSize3 % writeBlockSizeInBytes, insertBuffer3),
         new SingleActionSequence(insertAction3),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset3 - writeBlockSizeInBytes), writeBlockSizeInBytes,
            insertSize1 + insertSize2),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset2),
            (insertOffset3 - insertOffset2) % writeBlockSizeInBytes, insertSize1 + insertSize2),
         new WriteActionSequence(at(insertOffset2 + insertSize1), 1, insertSize2, insertBuffer2),
         new SingleActionSequence(insertAction2),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), insertOffset2 - insertOffset1, insertSize1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF5a_multiRemove_start_consecutive_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First remove
      int removeOffset1 = 0;
      int removeSize1 = 35;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));
      // Second remove
      int removeOffset2 = 35;
      int removeSize2 = 350;
      MediumAction removeAction2 = testling.scheduleRemove(new MediumRegion(at(removeOffset2), removeSize2));
      // Bytes behind second remove
      int totalRWSizeInBytes2 = totalMediumSizeInBytes - removeOffset1 - removeSize1 - removeSize2;
      int readWriteBlockCount2 = totalRWSizeInBytes2 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes2 = totalRWSizeInBytes2 % writeBlockSizeInBytes;

      int truncatedBytes = removeSize1 + removeSize2;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes - truncatedBytes), truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new SingleActionSequence(removeAction1),
         new ReadWriteActionSequence(at(removeOffset1 + removeSize1 + removeSize2), readWriteBlockCount2,
            writeBlockSizeInBytes, -(removeSize1 + removeSize2), ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(
            at(removeOffset1 + removeSize1 + removeSize2 + readWriteBlockCount2 * writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes2, -(removeSize1 + removeSize2)),
         new SingleActionSequence(removeAction2), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF5b_multiRemove_middle_nonConsecutive_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First remove
      int removeOffset1 = 47;
      int removeSize1 = 350;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));
      // Second remove
      int removeOffset2 = 10;
      int removeSize2 = 35;
      MediumAction removeAction2 = testling.scheduleRemove(new MediumRegion(at(removeOffset2), removeSize2));
      // Third remove
      int removeOffset3 = 797;
      int removeSize3 = 200;
      MediumAction removeAction3 = testling.scheduleRemove(new MediumRegion(at(removeOffset3), removeSize3));

      // Bytes between first and third remove
      int totalRWSizeInBytes1 = removeOffset3 - removeOffset1 - removeSize1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      // Bytes between third remove and EOF
      int totalRWSizeInBytes3 = totalMediumSizeInBytes - removeOffset3 - removeSize3;
      int readWriteBlockCount3 = totalRWSizeInBytes3 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes3 = totalRWSizeInBytes3 % writeBlockSizeInBytes;

      int truncatedBytes = removeSize1 + removeSize2 + removeSize3;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes - truncatedBytes), truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(removeOffset2 + removeSize2),
            removeOffset1 - removeOffset2 - removeSize2, -removeSize2),
         new SingleActionSequence(removeAction2),
         new ReadWriteActionSequence(at(removeOffset1 + removeSize1), readWriteBlockCount1, writeBlockSizeInBytes,
            -(removeSize1 + removeSize2), ActionOrder.FORWARD),
         new SingleActionSequence(removeAction1),
         new ReadWriteActionSequence(at(removeOffset3 + removeSize3), readWriteBlockCount3, writeBlockSizeInBytes,
            -(removeSize1 + removeSize2 + removeSize3), ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(
            at(removeOffset3 + removeSize3 + readWriteBlockCount3 * writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes3, -(removeSize1 + removeSize2 + removeSize3)),
         new SingleActionSequence(removeAction3), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF5c_multiRemove_untilEnd_partlyConsecutive_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First remove
      int removeOffset1 = 58;
      int removeSize1 = 400;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));
      // Second remove
      int removeOffset2 = 0;
      int removeSize2 = 50;
      MediumAction removeAction2 = testling.scheduleRemove(new MediumRegion(at(removeOffset2), removeSize2));
      // Third remove
      int removeOffset3 = 52;
      int removeSize3 = 1;
      MediumAction removeAction3 = testling.scheduleRemove(new MediumRegion(at(removeOffset3), removeSize3));
      // Fourth remove
      int removeOffset4 = 1000;
      int removeSize4 = 499;
      MediumAction removeAction4 = testling.scheduleRemove(new MediumRegion(at(removeOffset4), removeSize4));
      // Fifth remove
      int removeOffset5 = 1499;
      int removeSize5 = 1;
      MediumAction removeAction5 = testling.scheduleRemove(new MediumRegion(at(removeOffset5), removeSize5));

      // Bytes between first and fourth remove
      int totalRWSizeInBytes1 = removeOffset4 - removeOffset1 - removeSize1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      int truncatedBytes = removeSize1 + removeSize2 + removeSize3 + removeSize4 + removeSize5;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes - truncatedBytes), truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(removeOffset2 + removeSize2),
            removeOffset3 - removeOffset2 - removeSize2, -removeSize2),
         new SingleActionSequence(removeAction2),
         ReadWriteActionSequence.createSingleBlock(at(removeOffset3 + removeSize3),
            removeOffset1 - removeOffset3 - removeSize3, -(removeSize3 + removeSize2)),
         new SingleActionSequence(removeAction3),
         new ReadWriteActionSequence(at(removeOffset1 + removeSize1), readWriteBlockCount1, writeBlockSizeInBytes,
            -(removeSize1 + removeSize2 + removeSize3), ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(
            at(removeOffset1 + removeSize1 + readWriteBlockCount1 * writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes1, -(removeSize1 + removeSize2 + removeSize3)),
         new SingleActionSequence(removeAction1), new SingleActionSequence(removeAction4),
         new SingleActionSequence(removeAction5), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF5d_multiRemove_removeWholeMedium_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First remove
      int removeOffset1 = 58;
      int removeSize1 = 400;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));
      // Second remove
      int removeOffset2 = 0;
      int removeSize2 = 58;
      MediumAction removeAction2 = testling.scheduleRemove(new MediumRegion(at(removeOffset2), removeSize2));
      // Third remove
      int removeOffset3 = 458;
      int removeSize3 = 1042;
      MediumAction removeAction3 = testling.scheduleRemove(new MediumRegion(at(removeOffset3), removeSize3));

      int truncatedBytes = removeSize1 + removeSize2 + removeSize3;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes - truncatedBytes), truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new SingleActionSequence(removeAction2), new SingleActionSequence(removeAction1),
         new SingleActionSequence(removeAction3), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF6a_multiInsertingReplace_start_consecutive_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 50;
      int replacedByteCount1 = 35;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Second replace
      int replaceOffset2 = 35;
      int replaceSize2 = 250;
      int replacedByteCount2 = 150;
      ByteBuffer replacementBuffer2 = MediaTestHelper.createTestByteBufferOfSize(replaceSize2);
      MediumAction replaceAction2 = testling.scheduleReplace(new MediumRegion(at(replaceOffset2), replacedByteCount2),
         replacementBuffer2);
      int totalRWSizeInBytes2 = totalMediumSizeInBytes - replaceOffset2 - replacedByteCount2;
      int readWriteBlockCount2 = totalRWSizeInBytes2 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes2 = totalRWSizeInBytes2 % writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount2, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1 + replaceSize2 - replacedByteCount2, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset2 + replacedByteCount2),
            readWriteRemainderSizeInBytes2, replaceSize1 - replacedByteCount1 + replaceSize2 - replacedByteCount2),
         new WriteActionSequence(at(replaceOffset2 + replaceSize1 - replacedByteCount1), 1, writeBlockSizeInBytes,
            replacementBuffer2),
         new WriteActionSequence(at(replaceOffset2 + replaceSize1 - replacedByteCount1 + writeBlockSizeInBytes), 1,
            replaceSize2 % writeBlockSizeInBytes, replacementBuffer2),
         new SingleActionSequence(replaceAction2),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF6b_multiReplace_middle_nonconsecutive_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First (inserting) replace
      int replaceOffset1 = 100;
      int replaceSize1 = 50;
      int replacedByteCount1 = 35;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Second (removing) replace
      int replaceOffset2 = 950;
      int replaceSize2 = 100;
      int replacedByteCount2 = 150;
      ByteBuffer replacementBuffer2 = MediaTestHelper.createTestByteBufferOfSize(replaceSize2);
      MediumAction replaceAction2 = testling.scheduleReplace(new MediumRegion(at(replaceOffset2), replacedByteCount2),
         replacementBuffer2);

      // Third (overwriting) replace
      int replaceOffset3 = 600;
      int replaceSize3 = 100;
      int replacedByteCount3 = replaceSize3;
      ByteBuffer replacementBuffer3 = MediaTestHelper.createTestByteBufferOfSize(replaceSize3);
      MediumAction replaceAction3 = testling.scheduleReplace(new MediumRegion(at(replaceOffset3), replacedByteCount3),
         replacementBuffer3);

      // Bytes between replaceAction1 and replaceAction3
      int totalRWSizeInBytes1 = replaceOffset3 - replaceOffset1 - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      // Bytes behind replaceAction2
      int totalRWSizeInBytes2 = totalMediumSizeInBytes - replaceOffset2 - replacedByteCount2;
      int readWriteBlockCount2 = totalRWSizeInBytes2 / writeBlockSizeInBytes;

      // Bytes between replaceAction3 and replaceAction2
      int totalRWSizeInBytes3 = replaceOffset2 - replaceOffset3 - replaceSize3;
      int readWriteBlockCount3 = totalRWSizeInBytes3 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes3 = totalRWSizeInBytes3 % writeBlockSizeInBytes;

      int truncatedBytes = replaceSize1 - replacedByteCount1 + replaceSize2 - replacedByteCount2 + replaceSize3
         - replacedByteCount3;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes + truncatedBytes), -truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(replaceOffset2 + replacedByteCount2), readWriteBlockCount2,
            writeBlockSizeInBytes, replaceSize2 - replacedByteCount2 + replaceSize1 - replacedByteCount1,
            ActionOrder.FORWARD),
         new WriteActionSequence(at(replaceOffset2 + replaceSize1 - replacedByteCount1), 1, replaceSize2,
            replacementBuffer2),
         new SingleActionSequence(replaceAction2),
         new ReadWriteActionSequence(at(replaceOffset2), readWriteBlockCount3, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset3 + replaceSize3), readWriteRemainderSizeInBytes3,
            replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset3 + replaceSize1 - replacedByteCount1), 1, replaceSize3,
            replacementBuffer3),
         new SingleActionSequence(replaceAction3),
         new ReadWriteActionSequence(at(replaceOffset3), readWriteBlockCount1, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1),
            readWriteRemainderSizeInBytes1, replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF6c_multiMutuallyEliminatingReplace_middle_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 50;
      int replacedByteCount1 = 35;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Second replace
      int replaceOffset2 = 350;
      int replaceSize2 = 35;
      int replacedByteCount2 = 50;
      ByteBuffer replacementBuffer2 = MediaTestHelper.createTestByteBufferOfSize(replaceSize2);
      MediumAction replaceAction2 = testling.scheduleReplace(new MediumRegion(at(replaceOffset2), replacedByteCount2),
         replacementBuffer2);

      int totalRWSizeInBytes1 = replaceOffset2 - replaceOffset1 - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset2 + replaceSize1 - replacedByteCount1), 1, replaceSize2,
            replacementBuffer2),
         new SingleActionSequence(replaceAction2),
         new ReadWriteActionSequence(at(replaceOffset2), readWriteBlockCount1, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1),
            readWriteRemainderSizeInBytes1, replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF6d_multiComplexReplace_middle_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 35;
      int replacedByteCount1 = 50;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Second replace
      int replaceOffset2 = 350;
      int replaceSize2 = 50;
      int replacedByteCount2 = 35;
      ByteBuffer replacementBuffer2 = MediaTestHelper.createTestByteBufferOfSize(replaceSize2);
      MediumAction replaceAction2 = testling.scheduleReplace(new MediumRegion(at(replaceOffset2), replacedByteCount2),
         replacementBuffer2);

      int totalRWSizeInBytes1 = replaceOffset2 - replaceOffset1 - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      // Third replace
      int replaceOffset3 = 700;
      int replaceSize3 = 150;
      int replacedByteCount3 = 100;
      ByteBuffer replacementBuffer3 = MediaTestHelper.createTestByteBufferOfSize(replaceSize3);
      MediumAction replaceAction3 = testling.scheduleReplace(new MediumRegion(at(replaceOffset3), replacedByteCount3),
         replacementBuffer3);
      // Fourth replace
      int replaceOffset4 = 800;
      int replaceSize4 = 20;
      int replacedByteCount4 = replaceSize4;
      ByteBuffer replacementBuffer4 = MediaTestHelper.createTestByteBufferOfSize(replaceSize4);
      MediumAction replaceAction4 = testling.scheduleReplace(new MediumRegion(at(replaceOffset4), replacedByteCount4),
         replacementBuffer4);
      // Fifth replace
      int replaceOffset5 = 820;
      int replaceSize5 = 80;
      int replacedByteCount5 = 100;
      ByteBuffer replacementBuffer5 = MediaTestHelper.createTestByteBufferOfSize(replaceSize5);
      MediumAction replaceAction5 = testling.scheduleReplace(new MediumRegion(at(replaceOffset5), replacedByteCount5),
         replacementBuffer5);

      int totalRWSizeInBytes5 = totalMediumSizeInBytes - replaceOffset5 - replacedByteCount5;
      int readWriteBlockCount5 = totalRWSizeInBytes5 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes5 = totalRWSizeInBytes5 % writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(replaceOffset1 + replacedByteCount1), readWriteBlockCount1,
            writeBlockSizeInBytes, replaceSize1 - replacedByteCount1, ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1 + writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes1, replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1),
         new WriteActionSequence(at(replaceOffset2 + replaceSize1 - replacedByteCount1), 1, replaceSize2,
            replacementBuffer2),
         new SingleActionSequence(replaceAction2),
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount5, writeBlockSizeInBytes,
            replaceSize3 - replacedByteCount3 + replaceSize5 - replacedByteCount5, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset5 + replacedByteCount5),
            readWriteRemainderSizeInBytes5, replaceSize3 - replacedByteCount3 + replaceSize5 - replacedByteCount5),
         new WriteActionSequence(at(replaceOffset5 + replaceSize3 - replacedByteCount3), 1, replaceSize5,
            replacementBuffer5),
         new SingleActionSequence(replaceAction5),
         new WriteActionSequence(at(replaceOffset4 + replaceSize3 - replacedByteCount3), 1, replaceSize4,
            replacementBuffer4),
         new SingleActionSequence(replaceAction4),
         new WriteActionSequence(at(replaceOffset3), 1, replaceSize3, replacementBuffer3),
         new SingleActionSequence(replaceAction3));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF7a_removeAndInsert_mutuallyEliminatingDifferentOffsets_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Remove
      int removeOffset1 = 300;
      int removeSize1 = 160;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      // Insert
      int insertOffset1 = 140;
      int insertSize1 = 160;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);
      // Bytes behind insert
      int totalRWSizeInBytes1 = removeOffset1 - insertOffset1;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), totalRWSizeInBytes1, insertSize1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1), new SingleActionSequence(removeAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF7b_removeAndInsert_mutuallyEliminatingSameOffsetRemoveFirst_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Remove
      int removeOffset1 = 300;
      int removeSize1 = 160;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      // Insert
      int insertOffset1 = removeOffset1;
      int insertSize1 = 160;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1), new SingleActionSequence(removeAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF7c_removeAndInsert_mutuallyEliminatingSameOffsetInsertFirst_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Insert
      int insertOffset1 = 300;
      int insertSize1 = 160;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Remove
      int removeOffset1 = insertOffset1;
      int removeSize1 = 160;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1), new SingleActionSequence(removeAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF7d_removeAndInsert_insertsInRemovedArea_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertOffset1 = 140;
      int insertSize1 = 160;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Remove
      int removeOffset1 = 100;
      int removeSize1 = 500;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      // Second insert
      int insertOffset2 = 300;
      int insertSize2 = 360;
      ByteBuffer insertBuffer2 = MediaTestHelper.createTestByteBufferOfSize(insertSize2);
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);

      // Bytes behind
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - removeOffset1 - removeSize1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(removeOffset1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1),
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount1, writeBlockSizeInBytes,
            insertSize1 + insertSize2 - removeSize1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(removeOffset1 + removeSize1), readWriteRemainderSizeInBytes1,
            insertSize1 + insertSize2 - removeSize1),
         new SingleActionSequence(removeAction1),
         new WriteActionSequence(at(removeOffset1 + insertSize1), 1, writeBlockSizeInBytes, insertBuffer2),
         new WriteActionSequence(at(removeOffset1 + insertSize1 + writeBlockSizeInBytes), 1,
            insertSize2 % writeBlockSizeInBytes, insertBuffer2),
         new SingleActionSequence(insertAction2));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF7e_removeAndInsert_insertsInRemovedAreaAfterOtherNonRelatedActions_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Remove
      int removeOffset1 = 100;
      int removeSize1 = 500;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      // First insert
      int insertOffset1 = 900;
      int insertSize1 = 360;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertOffset2 = 140;
      int insertSize2 = 130;
      ByteBuffer insertBuffer2 = MediaTestHelper.createTestByteBufferOfSize(insertSize2);
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);

      // Bytes after first insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - insertOffset1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      // Bytes between remove and first insert
      int totalRWSizeInBytes2 = insertOffset1 - removeOffset1 - removeSize1;
      int readWriteBlockCount2 = totalRWSizeInBytes2 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes2 = totalRWSizeInBytes2 % writeBlockSizeInBytes;

      int truncatedBytes = insertSize1 + insertSize2 - removeSize1;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes + truncatedBytes), -truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(removeOffset1), 1, insertSize2, insertBuffer2),
         new SingleActionSequence(insertAction2),
         new ReadWriteActionSequence(at(removeOffset1 + removeSize1), readWriteBlockCount2, writeBlockSizeInBytes,
            insertSize2 - removeSize1, ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(
            at(removeOffset1 + removeSize1 + readWriteBlockCount2 * writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes2, insertSize2 - removeSize1),
         new SingleActionSequence(removeAction1),
         new ReadWriteActionSequence(at(insertOffset1), readWriteBlockCount1, writeBlockSizeInBytes,
            insertSize2 + insertSize1 - removeSize1, ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1 + readWriteBlockCount1 * writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes1, insertSize2 + insertSize1 - removeSize1),
         new WriteActionSequence(at(insertOffset1 + insertSize2 - removeSize1), 1, writeBlockSizeInBytes,
            insertBuffer1),
         new WriteActionSequence(at(insertOffset1 + insertSize2 - removeSize1 + writeBlockSizeInBytes), 1,
            insertSize1 % writeBlockSizeInBytes, insertBuffer1),
         new SingleActionSequence(insertAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF7f_removeAndInsert_removeWholeMedium_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Remove
      int removeOffset1 = 0;
      int removeSize1 = totalMediumSizeInBytes;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      // Insert
      int insertOffset1 = removeOffset1;
      int insertSize1 = 160;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      int truncatedBytes = insertSize1 - removeSize1;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes + truncatedBytes), -truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(0), 1, insertSize1, insertBuffer1), new SingleActionSequence(insertAction1),
         new SingleActionSequence(removeAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF8a_replaceAndInsert_mutuallyEliminatingConsecutive_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Replace
      int replaceOffset1 = 0;
      int replaceSize1 = 100;
      int replacedByteCount1 = 300;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Insert
      int insertOffset1 = replaceOffset1 + replacedByteCount1;
      int insertSize1 = 200;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1),
         new WriteActionSequence(at(replaceOffset1 + replaceSize1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}. See design concept for the testcase IDs starting
    * with CF.
    */
   @Test
   public void CF8b_replaceAndInsert_mutuallyEliminatingWithGap_returnsExpectedPlan() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Insert
      int insertOffset1 = 320;
      int insertSize1 = 200;
      ByteBuffer insertBuffer1 = MediaTestHelper.createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Replace
      int replaceOffset1 = 0;
      int replaceSize1 = 100;
      int replacedByteCount1 = 300;
      ByteBuffer replacementBuffer1 = MediaTestHelper.createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Bytes between end of replace and insert
      int totalRWSizeInBytes1 = insertOffset1 - replaceOffset1 - replacedByteCount1;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1), totalRWSizeInBytes1,
            replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1),
         new SingleActionSequence(replaceAction1),
         new WriteActionSequence(at(insertOffset1 - replacedByteCount1 + replaceSize1), 1, insertSize1, insertBuffer1),
         new SingleActionSequence(insertAction1));
   }

   /**
    * @return an instance of a new {@link MediumChangeManager} for testing
    */
   private MediumChangeManager getTestling() {
      return new MediumChangeManager(new MediumReferenceFactory(MediaTestHelper.getStandardMedium()));
   }

   /**
    * Calls the {@link MediumChangeManager#createFlushPlan(int, long)} method and checks its results against the
    * expected action sequence given by the {@link ExpectedActionSequence}s instances.
    * 
    * @param testling
    *           The {@link MediumChangeManager} under test
    * @param writeBlockSizeInBytes
    *           The write block size in bytes
    * @param totalMediumSizeInBytes
    *           The total size of the external medium in bytes
    * @param expectedActionSequence
    *           The {@link ExpectedActionSequence} instances
    */
   private void checkCreatedFlushPlan(MediumChangeManager testling, int writeBlockSizeInBytes,
      long totalMediumSizeInBytes, ExpectedActionSequence... expectedActionSequence) {

      List<MediumAction> createdFlushPlan = testling.createFlushPlan(writeBlockSizeInBytes, totalMediumSizeInBytes);

      dumpActualFlushPlan(createdFlushPlan);
      dumpExpectedFlushPlan(expectedActionSequence);

      Iterator<MediumAction> actionIterator = createdFlushPlan.iterator();

      for (ExpectedActionSequence actionSequence : expectedActionSequence) {
         actionSequence.assertFollowsSequence(actionIterator);
      }

      Assert.assertFalse(actionIterator.hasNext());
   }

   /**
    * Dumps all elements of the actually returned flush plan.
    * 
    * @param createdFlushPlan
    *           The flush plan to dump
    */
   private void dumpActualFlushPlan(List<MediumAction> createdFlushPlan) {
      for (int i = 0; i < createdFlushPlan.size(); ++i) {
         MediumAction element = createdFlushPlan.get(i);

         ExpectedActionSequence.dumpMediumAction(currentDumpStreamActual, element);
      }
   }

   /**
    * Dumps all elements of the actually returned flush plan.
    * 
    * @param expectedActionSequence
    *           The expected flush plan to dump
    */
   private void dumpExpectedFlushPlan(ExpectedActionSequence... expectedActionSequence) {
      for (ExpectedActionSequence actionSequence : expectedActionSequence) {
         actionSequence.dump(currentDumpStreamExpected);
      }
   }

   /**
    * Prepares a new test file, returning a {@link PrintStream} that can be used to write output to it. Throws a
    * {@link TestDataException} in case of any I/O problems during the operation.
    * 
    * @param name
    *           The name of the test file, will be newly created, if it already exists it will be deleted first.
    * @param parentFolder
    *           The parent folder of the file, must exist.
    */
   private static PrintStream setupTestFile(String name, File parentFolder) {
      try {
         File testFile = new File(parentFolder, name);

         if (testFile.exists()) {
            if (!testFile.delete()) {
               throw new TestDataException("IO error during setup: could not delete existing file", null);
            }
         } else {
            if (!testFile.createNewFile()) {
               throw new TestDataException("IO error during setup: could not create new test file", null);
            }
         }

         return new PrintStream(new FileOutputStream(testFile), false);
      } catch (IOException e) {
         throw new TestDataException("IO error during setup: " + e, e);
      }
   }

}
