/**
 *
 * {@link MediumChangeManagerCreateFlushPlanTest}.java
 *
 * @author Jens
 *
 * @date 16.10.2016
 */
package com.github.jmeta.library.media.impl.changeManager;

import static com.github.jmeta.library.media.api.helper.MediaTestUtility.at;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.changeManager.ReadWriteActionSequence.ActionOrder;
import com.github.jmeta.library.media.impl.offset.MediumOffsetFactory;
import com.github.jmeta.utility.byteutils.api.services.ByteArrayUtils;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

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
 * 
 * All test cases start with "CF" = "create flush plan" and a number, which is the test case number from the design
 * concept.
 * 
 * Note that this test class dumps expected and actual flush plan created in files for easier comparison, the files are
 * stored in a sub-folder of {@link MediaTestFiles#TEST_FILE_DIRECTORY_PATH}.
 * 
 * Here, we only have positive tests for creation of flush plans, all negative tests for invalid scheduling sequences
 * can be found in {@link MediumChangeManagerTest}.
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
      Path mediaFlushTestPath = MediaTestFiles.TEST_FILE_DIRECTORY_PATH.resolve("MediumChangeManagerTests");

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
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF1a_singleInsert_atStart_multipleFullBlocksBehind_oneFullInsertedBlock() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 800;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 200;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 0;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);
      // Bytes behind first insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - insertOffset1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount1, writeBlockSizeInBytes,
            insertSize1, ActionOrder.BACKWARD),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF1b_singleInsert_inMiddle_oneSmallBlockBehind_oneSmallInsertedBlock() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 20;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 12;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);
      // Bytes behind first insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - insertOffset1;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), totalRWSizeInBytes1, insertSize1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF1c_singleInsert_inMiddle_multipleFullAndRemainderBlockBehind_multipleFullAndRemainderInsertedBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1000;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 450;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
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

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount1, writeBlockSizeInBytes,
            insertSize1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), readWriteRemainderSizeInBytes1, insertSize1),
         new WriteActionSequence(at(insertOffset1), writeBlockCount1, writeBlockSizeInBytes, insertBuffer1, 0),
         new WriteActionSequence(at(insertOffset1 + writeBlockCount1 * writeBlockSizeInBytes), 1,
            writeRemainderSizeInBytes1, insertBuffer1, writeBlockCount1 * writeBlockSizeInBytes),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF1d_singleInsert_atEnd_noBlocksBehind_oneSmallInsertedBlock() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 20;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = totalMediumSizeInBytes;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF2a_singleRemove_atStart_oneFullAndRemainderBlockBehind() {

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
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF2b_singleRemove_coveringWholeMedium_noBlocksBehind() {

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
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF2c_singleRemove_inMiddle_oneSmallBlockBehind() {

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
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF2d_singleRemove_atEnd_noBlocksBehind() {

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
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF3a_singleInsertingReplace_atStart_oneSmallBlockBehind_multipleFullAndRemainderReplacementBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 150;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 350;
      int replacedByteCount1 = 35;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      int remainingWriteByteCount1 = replaceSize1 % writeBlockSizeInBytes;

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1),
            totalMediumSizeInBytes - replaceOffset1 - replacedByteCount1, replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, writeBlockSizeInBytes, replacementBuffer1, 0),
         new WriteActionSequence(at(replaceOffset1 + writeBlockSizeInBytes), 1, remainingWriteByteCount1,
            replacementBuffer1, writeBlockSizeInBytes),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF3b_singleRemovingReplace_atStart_multipleFullBlocksBehind_oneSmallReplacementBlock() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 900;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 100;
      int replacedByteCount1 = 300;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE, new MediumRegion(
         at(totalMediumSizeInBytes + replaceSize1 - replacedByteCount1), replacedByteCount1 - replaceSize1), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(replaceOffset1 + replacedByteCount1), readWriteBlockCount1,
            writeBlockSizeInBytes, replaceSize1 - replacedByteCount1, ActionOrder.FORWARD),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF3c_singleInsertingReplace_inMiddle_multipleFullAndRemainderBlockBehind_oneSmallReplacementBlock() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 500;
      int replaceSize1 = 50;
      int replacedByteCount1 = 35;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
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
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF3d_singleRemovingReplace_inMiddle_oneSmallBlockBehind_oneSmallReplacementBlock() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 300;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 150;
      int replaceSize1 = 80;
      int replacedByteCount1 = 100;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - replaceOffset1 - replacedByteCount1;

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE, new MediumRegion(
         at(totalMediumSizeInBytes + replaceSize1 - replacedByteCount1), replacedByteCount1 - replaceSize1), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1), totalRWSizeInBytes1,
            replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF3e_singleInsertingReplace_atEnd_noBlocksBehind_multipleFullReplacementBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 1450;
      int replaceSize1 = 350;
      int replacedByteCount1 = 50;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);

      int remainingWriteByteCount1 = replaceSize1 % writeBlockSizeInBytes;

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 1, writeBlockSizeInBytes, replacementBuffer1, 0),
         new WriteActionSequence(at(replaceOffset1 + writeBlockSizeInBytes), 1, remainingWriteByteCount1,
            replacementBuffer1, writeBlockSizeInBytes),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF3f_singleRemovingReplace_atEnd_noBlocksBehind_oneSmallReplacementBlock() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 300;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 280;
      int replaceSize1 = 10;
      int replacedByteCount1 = 20;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE, new MediumRegion(
         at(totalMediumSizeInBytes + replaceSize1 - replacedByteCount1), replacedByteCount1 - replaceSize1), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF3g_singleOverwritingReplace_inMiddle_oneSmallBlockBehind_oneSmallReplacementBlock() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 300;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 150;
      int replaceSize1 = 80;
      int replacedByteCount1 = replaceSize1;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF3h_singleInsertingReplace_coveringWholeMedium_mutipleFullReplacementBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 300;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 400;
      int replacedByteCount1 = totalMediumSizeInBytes;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);

      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 2, writeBlockSizeInBytes, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF4a_multipleInserts_allSameOffsetAtStart_multipleSmallInsertedBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 450;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 0;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertSize2 = 20;
      ByteBuffer insertBuffer2 = createTestByteBufferOfSize(insertSize2);
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
         new WriteActionSequence(at(insertOffset2 + insertSize1), 1, insertSize2, insertBuffer2, 0),
         new SingleActionSequence(insertAction2),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF4b_multipleInserts_allSameOffsetAtStart_multipleSmallAndFullInsertedBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1000;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 0;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertSize2 = 450;
      ByteBuffer insertBuffer2 = createTestByteBufferOfSize(insertSize2);
      int insertOffset2 = 0;
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);
      // Write block for second insert
      int writeBlockCount2 = insertSize2 / writeBlockSizeInBytes;
      int writeRemainderSizeInBytes2 = insertSize2 % writeBlockSizeInBytes;

      // Third insert
      int insertSize3 = 20;
      ByteBuffer insertBuffer3 = createTestByteBufferOfSize(insertSize3);
      int insertOffset3 = 0;
      MediumAction insertAction3 = testling.scheduleInsert(new MediumRegion(at(insertOffset3), insertSize3),
         insertBuffer3);
      // Bytes behind third insert
      int totalRWSizeInBytes3 = totalMediumSizeInBytes - insertOffset3;
      int readWriteBlockCount3 = totalRWSizeInBytes3 / writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount3, writeBlockSizeInBytes,
            insertSize1 + insertSize2 + insertSize3, ActionOrder.BACKWARD),
         new WriteActionSequence(at(insertOffset3 + insertSize1 + insertSize2), 1, insertSize3, insertBuffer3, 0),
         new SingleActionSequence(insertAction3),
         new WriteActionSequence(at(insertOffset2 + insertSize1), writeBlockCount2, writeBlockSizeInBytes,
            insertBuffer2, 0),
         new WriteActionSequence(at(insertOffset2 + writeBlockCount2 * writeBlockSizeInBytes + insertSize1), 1,
            writeRemainderSizeInBytes2, insertBuffer2, writeBlockCount2 * writeBlockSizeInBytes),
         new SingleActionSequence(insertAction2),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF4c_multipleInserts_atDifferentOffsetsInMiddle_smallInsertedBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 450;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 200;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertSize2 = 20;
      ByteBuffer insertBuffer2 = createTestByteBufferOfSize(insertSize2);
      int insertOffset2 = 250;
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(insertOffset2), writeBlockSizeInBytes, insertSize1 + insertSize2),
         new WriteActionSequence(at(insertOffset2 + insertSize1), 1, insertSize2, insertBuffer2, 0),
         new SingleActionSequence(insertAction2),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), insertOffset2 - insertOffset1, insertSize1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF4d_multipleInserts_atDifferentOffsetsAtEnd_smallInsertedBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 450;
      int writeBlockSizeInBytes = 200;

      // First insert
      int insertOffset1 = 449;
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertOffset2 = 450;
      int insertSize2 = 20;
      ByteBuffer insertBuffer2 = createTestByteBufferOfSize(insertSize2);
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(insertOffset2 + insertSize1), 1, insertSize2, insertBuffer2, 0),
         new SingleActionSequence(insertAction2),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), insertOffset2 - insertOffset1, insertSize1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF4e_multipleInserts_atDifferentOffsetsInMiddle_multipleSmallAndFullInsertedBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1000;
      int writeBlockSizeInBytes = 150;

      // First insert
      int insertSize1 = 40;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      int insertOffset1 = 200;
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertSize2 = 20;
      ByteBuffer insertBuffer2 = createTestByteBufferOfSize(insertSize2);
      int insertOffset2 = 250;
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);

      // Third insert
      int insertSize3 = 333;
      ByteBuffer insertBuffer3 = createTestByteBufferOfSize(insertSize3);
      int insertOffset3 = 500;
      MediumAction insertAction3 = testling.scheduleInsert(new MediumRegion(at(insertOffset3), insertSize3),
         insertBuffer3);

      // Fourth insert
      int insertSize4 = 250;
      ByteBuffer insertBuffer4 = createTestByteBufferOfSize(insertSize4);
      int insertOffset4 = 520;
      MediumAction insertAction4 = testling.scheduleInsert(new MediumRegion(at(insertOffset4), insertSize4),
         insertBuffer4);

      // Fifth insert
      int insertSize5 = 10;
      ByteBuffer insertBuffer5 = createTestByteBufferOfSize(insertSize5);
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
            insertSize5, insertBuffer5, 0),
         new SingleActionSequence(insertAction5),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset4), insertOffset5 - insertOffset4,
            insertSize1 + insertSize2 + insertSize3 + insertSize4),
         new WriteActionSequence(at(insertOffset4 + insertSize1 + insertSize2 + insertSize3), 1, writeBlockSizeInBytes,
            insertBuffer4, 0),
         new WriteActionSequence(at(insertOffset4 + writeBlockSizeInBytes + insertSize1 + insertSize2 + insertSize3), 1,
            insertSize4 % writeBlockSizeInBytes, insertBuffer4, writeBlockSizeInBytes),
         new SingleActionSequence(insertAction4),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset3), insertOffset4 - insertOffset3,
            insertSize1 + insertSize2 + insertSize3),
         new WriteActionSequence(at(insertOffset3 + insertSize1 + insertSize2), 2, writeBlockSizeInBytes, insertBuffer3,
            0),
         new WriteActionSequence(at(insertOffset3 + 2 * writeBlockSizeInBytes + insertSize1 + insertSize2), 1,
            insertSize3 % writeBlockSizeInBytes, insertBuffer3, 2 * writeBlockSizeInBytes),
         new SingleActionSequence(insertAction3),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset3 - writeBlockSizeInBytes), writeBlockSizeInBytes,
            insertSize1 + insertSize2),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset2),
            (insertOffset3 - insertOffset2) % writeBlockSizeInBytes, insertSize1 + insertSize2),
         new WriteActionSequence(at(insertOffset2 + insertSize1), 1, insertSize2, insertBuffer2, 0),
         new SingleActionSequence(insertAction2),
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), insertOffset2 - insertOffset1, insertSize1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF5a_multipleRemoves_allConsecutiveAtStart_multipleFullAndRemainderBlocksBehind() {

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
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF5b_multipleRemoves_nonConsecutiveInMiddle_multipleFullAndRemainderBlocksBetween() {

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
         ReadWriteActionSequence.createSingleBlock(
            at(removeOffset2 + removeSize2), removeOffset1 - removeOffset2 - removeSize2, -removeSize2),
         new SingleActionSequence(removeAction2), new ReadWriteActionSequence(at(removeOffset1 + removeSize1),
            readWriteBlockCount1, writeBlockSizeInBytes, -(removeSize1 + removeSize2), ActionOrder.FORWARD),
         new SingleActionSequence(removeAction1),
         new ReadWriteActionSequence(at(removeOffset3 + removeSize3), readWriteBlockCount3, writeBlockSizeInBytes,
            -(removeSize1 + removeSize2 + removeSize3), ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(
            at(removeOffset3 + removeSize3 + readWriteBlockCount3 * writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes3, -(removeSize1 + removeSize2 + removeSize3)),
         new SingleActionSequence(removeAction3), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF5c_multipleRemoves_partlyConsecutiveOneUntilEnd_multipleFullAndRemainderBlocksBetween() {

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
         ReadWriteActionSequence.createSingleBlock(
            at(removeOffset2 + removeSize2), removeOffset3 - removeOffset2 - removeSize2, -removeSize2),
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
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF5d_multipleRemoves_allConsecutiveCoveringWholeMedium_noBlocksBehind() {

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
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF6a_multipleInsertingReplaces_allConsecutiveAtStart_multipleSmallAndFullReplacementBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 50;
      int replacedByteCount1 = 35;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Second replace
      int replaceOffset2 = 35;
      int replaceSize2 = 250;
      int replacedByteCount2 = 150;
      ByteBuffer replacementBuffer2 = createTestByteBufferOfSize(replaceSize2);
      MediumAction replaceAction2 = testling.scheduleReplace(new MediumRegion(at(replaceOffset2), replacedByteCount2),
         replacementBuffer2);
      int totalRWSizeInBytes2 = totalMediumSizeInBytes - replaceOffset2 - replacedByteCount2;
      int readWriteBlockCount2 = totalRWSizeInBytes2 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes2 = totalRWSizeInBytes2 % writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1),
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount2, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1 + replaceSize2 - replacedByteCount2, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset2 + replacedByteCount2),
            readWriteRemainderSizeInBytes2, replaceSize1 - replacedByteCount1 + replaceSize2 - replacedByteCount2),
         new WriteActionSequence(at(replaceOffset2 + replaceSize1 - replacedByteCount1), 1, writeBlockSizeInBytes,
            replacementBuffer2, 0),
         new WriteActionSequence(at(replaceOffset2 + replaceSize1 - replacedByteCount1 + writeBlockSizeInBytes), 1,
            replaceSize2 % writeBlockSizeInBytes, replacementBuffer2, writeBlockSizeInBytes),
         new SingleActionSequence(replaceAction2));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF6b_multipleReplaces_nonConsecutiveInMiddle_multipleSmallAndFullReplacementBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First (inserting) replace
      int replaceOffset1 = 100;
      int replaceSize1 = 50;
      int replacedByteCount1 = 35;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Second (removing) replace
      int replaceOffset2 = 950;
      int replaceSize2 = 100;
      int replacedByteCount2 = 150;
      ByteBuffer replacementBuffer2 = createTestByteBufferOfSize(replaceSize2);
      MediumAction replaceAction2 = testling.scheduleReplace(new MediumRegion(at(replaceOffset2), replacedByteCount2),
         replacementBuffer2);

      // Third (overwriting) replace
      int replaceOffset3 = 600;
      int replaceSize3 = 100;
      int replacedByteCount3 = replaceSize3;
      ByteBuffer replacementBuffer3 = createTestByteBufferOfSize(replaceSize3);
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
         new ReadWriteActionSequence(at(replaceOffset3), readWriteBlockCount1, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1),
            readWriteRemainderSizeInBytes1, replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1),
         new ReadWriteActionSequence(at(replaceOffset2), readWriteBlockCount3, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset3 + replaceSize3), readWriteRemainderSizeInBytes3,
            replaceSize1 - replacedByteCount1),
         new WriteActionSequence(
            at(replaceOffset3 + replaceSize1 - replacedByteCount1), 1, replaceSize3, replacementBuffer3, 0),
         new SingleActionSequence(replaceAction3),
         new ReadWriteActionSequence(at(replaceOffset2 + replacedByteCount2), readWriteBlockCount2,
            writeBlockSizeInBytes, replaceSize2 - replacedByteCount2 + replaceSize1 - replacedByteCount1,
            ActionOrder.FORWARD),
         new WriteActionSequence(at(replaceOffset2 + replaceSize1 - replacedByteCount1), 1, replaceSize2,
            replacementBuffer2, 0),
         new SingleActionSequence(replaceAction2), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF6c_multipleMutuallyCompensatingReplaces_nonConsecutiveInMiddle_oneSmallAndOneFullBlockBetween() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 50;
      int replacedByteCount1 = 35;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Second replace
      int replaceOffset2 = 350;
      int replaceSize2 = 35;
      int replacedByteCount2 = 50;
      ByteBuffer replacementBuffer2 = createTestByteBufferOfSize(replaceSize2);
      MediumAction replaceAction2 = testling.scheduleReplace(new MediumRegion(at(replaceOffset2), replacedByteCount2),
         replacementBuffer2);

      int totalRWSizeInBytes1 = replaceOffset2 - replaceOffset1 - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(replaceOffset2), readWriteBlockCount1, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1),
            readWriteRemainderSizeInBytes1, replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1),
         new WriteActionSequence(at(replaceOffset2 + replaceSize1 - replacedByteCount1), 1, replaceSize2,
            replacementBuffer2, 0),
         new SingleActionSequence(replaceAction2));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF6d_multipleReplaces_partlyConsecutiveInMiddle_multipleSmallAndFullReplacementBlocks() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1500;
      int writeBlockSizeInBytes = 200;

      // First replace
      int replaceOffset1 = 0;
      int replaceSize1 = 35;
      int replacedByteCount1 = 50;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Second replace
      int replaceOffset2 = 350;
      int replaceSize2 = 50;
      int replacedByteCount2 = 35;
      ByteBuffer replacementBuffer2 = createTestByteBufferOfSize(replaceSize2);
      MediumAction replaceAction2 = testling.scheduleReplace(new MediumRegion(at(replaceOffset2), replacedByteCount2),
         replacementBuffer2);

      int totalRWSizeInBytes1 = replaceOffset2 - replaceOffset1 - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      // Third replace
      int replaceOffset3 = 700;
      int replaceSize3 = 150;
      int replacedByteCount3 = 100;
      ByteBuffer replacementBuffer3 = createTestByteBufferOfSize(replaceSize3);
      MediumAction replaceAction3 = testling.scheduleReplace(new MediumRegion(at(replaceOffset3), replacedByteCount3),
         replacementBuffer3);
      // Fourth replace
      int replaceOffset4 = 800;
      int replaceSize4 = 20;
      int replacedByteCount4 = replaceSize4;
      ByteBuffer replacementBuffer4 = createTestByteBufferOfSize(replaceSize4);
      MediumAction replaceAction4 = testling.scheduleReplace(new MediumRegion(at(replaceOffset4), replacedByteCount4),
         replacementBuffer4);
      // Fifth replace
      int replaceOffset5 = 820;
      int replaceSize5 = 80;
      int replacedByteCount5 = 100;
      ByteBuffer replacementBuffer5 = createTestByteBufferOfSize(replaceSize5);
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
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1),
         new WriteActionSequence(
            at(replaceOffset2 + replaceSize1 - replacedByteCount1), 1, replaceSize2, replacementBuffer2, 0),
         new SingleActionSequence(replaceAction2),
         new WriteActionSequence(at(replaceOffset4 + replaceSize3 - replacedByteCount3), 1, replaceSize4,
            replacementBuffer4, 0),
         new SingleActionSequence(replaceAction4),
         new WriteActionSequence(at(replaceOffset3), 1, replaceSize3, replacementBuffer3, 0),
         new SingleActionSequence(replaceAction3),
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount5, writeBlockSizeInBytes,
            replaceSize3 - replacedByteCount3 + replaceSize5 - replacedByteCount5, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset5 + replacedByteCount5),
            readWriteRemainderSizeInBytes5, replaceSize3 - replacedByteCount3 + replaceSize5 - replacedByteCount5),
         new WriteActionSequence(at(replaceOffset5 + replaceSize3 - replacedByteCount3), 1, replaceSize5,
            replacementBuffer5, 0),
         new SingleActionSequence(replaceAction5));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF7a_removeThenInsert_nonConsectuiveAtDifferentOffsets_mutuallyCompensating() {

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
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);
      // Bytes behind insert
      int totalRWSizeInBytes1 = removeOffset1 - insertOffset1;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), totalRWSizeInBytes1, insertSize1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1), new SingleActionSequence(removeAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF7b_removeThenInsert_allSameOffset_mutuallyCompensating() {

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
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1), new SingleActionSequence(removeAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF7c_insertThenRemove_allSameOffset_mutuallyCompensating() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Insert
      int insertOffset1 = 300;
      int insertSize1 = 160;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Remove
      int removeOffset1 = insertOffset1;
      int removeSize1 = 160;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1), new SingleActionSequence(removeAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF7d_removeThenInsert_nonConsecutiveAtDifferentOffsets_notMutuallyCompensating() {

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
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Second insert
      int insertOffset2 = 99;
      int insertSize2 = 130;
      ByteBuffer insertBuffer2 = createTestByteBufferOfSize(insertSize2);
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

      // Bytes between remove and second insert
      int totalRWSizeInBytes3 = removeOffset1 - insertOffset2;
      int readWriteRemainderSizeInBytes3 = totalRWSizeInBytes3 % writeBlockSizeInBytes;

      int truncatedBytes = insertSize1 + insertSize2 - removeSize1;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes + truncatedBytes), -truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(insertOffset2), readWriteRemainderSizeInBytes3, insertSize2),
         new WriteActionSequence(at(insertOffset2), 1, insertSize2, insertBuffer2, 0),
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
         new WriteActionSequence(at(insertOffset1 + insertSize2 - removeSize1), 1, writeBlockSizeInBytes, insertBuffer1,
            0),
         new WriteActionSequence(at(insertOffset1 + insertSize2 - removeSize1 + writeBlockSizeInBytes), 1,
            insertSize1 % writeBlockSizeInBytes, insertBuffer1, writeBlockSizeInBytes),
         new SingleActionSequence(insertAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF7e_removeWholeMediumThenInsert_atSameOffset() {

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
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      int truncatedBytes = insertSize1 - removeSize1;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes + truncatedBytes), -truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(0), 1, insertSize1, insertBuffer1, 0), new SingleActionSequence(insertAction1),
         new SingleActionSequence(removeAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF7f_removeThenInsert_consecutiveAtDifferentOffsets_notMutuallyCompensating() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Remove
      int removeOffset1 = 300;
      int removeSize1 = 160;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      // Insert
      int insertOffset1 = removeOffset1 + removeSize1;
      int insertSize1 = 100;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);
      // Bytes behind insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - insertOffset1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      int truncatedBytes = insertSize1 - removeSize1;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes + truncatedBytes), -truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new SingleActionSequence(removeAction1),
         new ReadWriteActionSequence(at(removeOffset1 + removeSize1), readWriteBlockCount1, writeBlockSizeInBytes,
            insertSize1 - removeSize1, ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(
            at(removeOffset1 + removeSize1 + readWriteBlockCount1 * writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes1, insertSize1 - removeSize1),
         new WriteActionSequence(at(removeOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF8a_replaceThenInsert_consecutiveAtDifferentOffsets_mutuallyCompensating() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Replace
      int replaceOffset1 = 0;
      int replaceSize1 = 100;
      int replacedByteCount1 = 300;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Insert
      int insertOffset1 = replaceOffset1 + replacedByteCount1;
      int insertSize1 = 200;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1),
         new WriteActionSequence(at(replaceOffset1 + replaceSize1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF8b_insertThenRemovingReplace_nonConsecutiveAtDifferentOffsets_mutuallyCompensating() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Insert
      int insertOffset1 = 320;
      int insertSize1 = 200;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Replace
      int replaceOffset1 = 0;
      int replaceSize1 = 100;
      int replacedByteCount1 = 300;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Bytes between end of replace and insert
      int totalRWSizeInBytes1 = insertOffset1 - replaceOffset1 - replacedByteCount1;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1), totalRWSizeInBytes1,
            replaceSize1 - replacedByteCount1),
         new WriteActionSequence(at(replaceOffset1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1),
         new WriteActionSequence(at(insertOffset1 - replacedByteCount1 + replaceSize1), 1, insertSize1, insertBuffer1,
            0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF8c_removingReplaceThenInsert_allSameOffset_notMutuallyCompensating() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Replace 300 existing by 100 new bytes
      int replaceOffset1 = 15;
      int replaceSize1 = 100;
      int replacedByteCount1 = 300;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Insert
      int insertOffset1 = replaceOffset1;
      int insertSize1 = 140;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Bytes after replace and insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - replaceOffset1 - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      int truncatedBytes = replacedByteCount1 - replaceSize1 - insertSize1;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes - truncatedBytes), truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1),
         new ReadWriteActionSequence(at(replaceOffset1 + replacedByteCount1), readWriteBlockCount1,
            writeBlockSizeInBytes, replaceSize1 - replacedByteCount1 + insertSize1, ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(
            at(replaceOffset1 + replacedByteCount1 + readWriteBlockCount1 * writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes1, replaceSize1 - replacedByteCount1 + insertSize1),
         new WriteActionSequence(at(replaceOffset1 + insertSize1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF8d_insertingReplaceThenInsert_allSameOffset_notMutuallyCompensating() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Replace 70 existing by 100 new bytes
      int replaceOffset1 = 15;
      int replaceSize1 = 100;
      int replacedByteCount1 = 70;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Insert
      int insertOffset1 = replaceOffset1;
      int insertSize1 = 140;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Bytes after replace and insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - replaceOffset1 - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount1, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1 + insertSize1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1),
            readWriteRemainderSizeInBytes1, replaceSize1 - replacedByteCount1 + insertSize1),
         new WriteActionSequence(at(replaceOffset1 + insertSize1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF8e_insertThenRemovingReplace_allSameOffset_notMutuallyCompensating() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Insert
      int insertOffset1 = 15;
      int insertSize1 = 140;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Replace 300 existing by 100 new bytes
      int replaceOffset1 = insertOffset1;
      int replaceSize1 = 100;
      int replacedByteCount1 = 300;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Bytes after replace and insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - replaceOffset1 - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      int truncatedBytes = replacedByteCount1 - replaceSize1 - insertSize1;

      MediumAction expectedTruncateAction = new MediumAction(MediumActionType.TRUNCATE,
         new MediumRegion(at(totalMediumSizeInBytes - truncatedBytes), truncatedBytes), 0, null);

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1),
         new ReadWriteActionSequence(at(replaceOffset1 + replacedByteCount1), readWriteBlockCount1,
            writeBlockSizeInBytes, replaceSize1 - replacedByteCount1 + insertSize1, ActionOrder.FORWARD),
         ReadWriteActionSequence.createSingleBlock(
            at(replaceOffset1 + replacedByteCount1 + readWriteBlockCount1 * writeBlockSizeInBytes),
            readWriteRemainderSizeInBytes1, replaceSize1 - replacedByteCount1 + insertSize1),
         new WriteActionSequence(at(replaceOffset1 + insertSize1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1), new SingleActionSequence(expectedTruncateAction));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   @Test
   public void CF8f_insertThenInsertingReplace_allSameOffset_notMutuallyCompensating() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // Insert
      int insertOffset1 = 15;
      int insertSize1 = 140;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // Replace 70 existing by 100 new bytes
      int replaceOffset1 = insertOffset1;
      int replaceSize1 = 100;
      int replacedByteCount1 = 70;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Bytes after replace and insert
      int totalRWSizeInBytes1 = totalMediumSizeInBytes - replaceOffset1 - replacedByteCount1;
      int readWriteBlockCount1 = totalRWSizeInBytes1 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new ReadWriteActionSequence(at(totalMediumSizeInBytes), readWriteBlockCount1, writeBlockSizeInBytes,
            replaceSize1 - replacedByteCount1 + insertSize1, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1),
            readWriteRemainderSizeInBytes1, replaceSize1 - replacedByteCount1 + insertSize1),
         new WriteActionSequence(at(replaceOffset1 + insertSize1), 1, replaceSize1, replacementBuffer1, 0),
         new SingleActionSequence(replaceAction1),
         new WriteActionSequence(at(insertOffset1), 1, insertSize1, insertBuffer1, 0),
         new SingleActionSequence(insertAction1));
   }

   /**
    * Tests {@link MediumChangeManager#createFlushPlan(int, long)}.
    */
   // TODO kleinen Bug bei BB Position in Remainder WriteSequence raus so dass DIFFs auch immer übereinstimmen!
   // (9)
   @Test
   public void CF9_insertRemoveAndReplace_notMutuallyCompensating() {

      MediumChangeManager testling = getTestling();

      int totalMediumSizeInBytes = 1140;
      int writeBlockSizeInBytes = 200;

      // First Insert
      int insertOffset1 = 22;
      int insertSize1 = 140;
      ByteBuffer insertBuffer1 = createTestByteBufferOfSize(insertSize1);
      MediumAction insertAction1 = testling.scheduleInsert(new MediumRegion(at(insertOffset1), insertSize1),
         insertBuffer1);

      // First replace
      int replaceOffset1 = 301;
      int replaceSize1 = 90;
      int replacedByteCount1 = replaceSize1;
      ByteBuffer replacementBuffer1 = createTestByteBufferOfSize(replaceSize1);
      MediumAction replaceAction1 = testling.scheduleReplace(new MediumRegion(at(replaceOffset1), replacedByteCount1),
         replacementBuffer1);

      // Second replace
      int replaceOffset2 = 0;
      int replaceSize2 = 200;
      int replacedByteCount2 = 10;
      ByteBuffer replacementBuffer2 = createTestByteBufferOfSize(replaceSize2);
      MediumAction replaceAction2 = testling.scheduleReplace(new MediumRegion(at(replaceOffset2), replacedByteCount2),
         replacementBuffer2);

      // First remove
      int removeOffset1 = 23;
      int removeSize1 = 250;
      MediumAction removeAction1 = testling.scheduleRemove(new MediumRegion(at(removeOffset1), removeSize1));

      // Second Insert
      int insertOffset2 = 300;
      int insertSize2 = 456;
      ByteBuffer insertBuffer2 = createTestByteBufferOfSize(insertSize2);
      MediumAction insertAction2 = testling.scheduleInsert(new MediumRegion(at(insertOffset2), insertSize2),
         insertBuffer2);

      // Third Insert
      int insertOffset3 = insertOffset2;
      int insertSize3 = 3;
      ByteBuffer insertBuffer3 = createTestByteBufferOfSize(insertSize3);
      MediumAction insertAction3 = testling.scheduleInsert(new MediumRegion(at(insertOffset3), insertSize3),
         insertBuffer3);

      // Second remove
      int removeOffset2 = 890;
      int removeSize2 = 250;
      MediumAction removeAction2 = testling.scheduleRemove(new MediumRegion(at(removeOffset2), removeSize2));

      // Bytes between second replace and first insert
      int totalRWSizeInBytes0 = insertOffset1 - replaceOffset2 - replacedByteCount2;
      int readWriteRemainderSizeInBytes0 = totalRWSizeInBytes0 % writeBlockSizeInBytes;
      int relativeWriteShiftInBytes0 = replaceSize2 - replacedByteCount2;

      // Bytes between first insert and first remove
      int totalRWSizeInBytes1 = removeOffset1 - insertOffset1;
      int readWriteRemainderSizeInBytes1 = totalRWSizeInBytes1 % writeBlockSizeInBytes;
      int relativeWriteShiftInBytes1 = insertSize1 + replaceSize2 - replacedByteCount2;

      // Bytes between first remove and second insert
      int totalRWSizeInBytes2 = insertOffset2 - removeOffset1 - removeSize1;
      int readWriteRemainderSizeInBytes2 = totalRWSizeInBytes2 % writeBlockSizeInBytes;
      int relativeWriteShiftInBytes2 = insertSize1 - replacedByteCount2 + replaceSize2 - removeSize1;

      // Bytes between second insert and first replace
      int totalRWSizeInBytes3 = replaceOffset1 - insertOffset2;
      int readWriteRemainderSizeInBytes3 = totalRWSizeInBytes3 % writeBlockSizeInBytes;
      int relativeWriteShiftInBytes3 = insertSize1 - replacedByteCount2 + replaceSize2 - removeSize1 + insertSize2
         + insertSize3;

      // Bytes between first replace and second remove
      int totalRWSizeInBytes4 = removeOffset2 - replaceOffset1 - replaceSize1;
      int readWriteBlockCount4 = totalRWSizeInBytes4 / writeBlockSizeInBytes;
      int readWriteRemainderSizeInBytes4 = totalRWSizeInBytes4 % writeBlockSizeInBytes;
      int relativeWriteShiftInBytes4 = insertSize1 - replacedByteCount1 + replaceSize1 - replacedByteCount2
         + replaceSize2 - removeSize1 + insertSize2 + insertSize3;

      checkCreatedFlushPlan(testling, writeBlockSizeInBytes, totalMediumSizeInBytes,
         new SingleActionSequence(removeAction2),
         // Bytes between first replace and second remove
         new ReadWriteActionSequence(at(removeOffset2), readWriteBlockCount4, writeBlockSizeInBytes,
            relativeWriteShiftInBytes4, ActionOrder.BACKWARD),
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset1 + replacedByteCount1),
            readWriteRemainderSizeInBytes4, relativeWriteShiftInBytes4),
         new WriteActionSequence(at(replaceOffset1 + relativeWriteShiftInBytes4), 1, replaceSize1, replacementBuffer1,
            0),
         new SingleActionSequence(replaceAction1),
         // Bytes between second insert and first replace
         ReadWriteActionSequence.createSingleBlock(at(insertOffset2), readWriteRemainderSizeInBytes3,
            relativeWriteShiftInBytes3),
         // Third insert
         new WriteActionSequence(
            at(insertOffset3 + insertSize1 - replacedByteCount2 + replaceSize2 - removeSize1 + insertSize2), 1,
            insertSize3, insertBuffer3, 0),
         new SingleActionSequence(insertAction3),
         // Second insert
         new WriteActionSequence(at(insertOffset2 + insertSize1 - replacedByteCount2 + replaceSize2 - removeSize1), 2,
            writeBlockSizeInBytes, insertBuffer2, 0),
         new WriteActionSequence(
            at(insertOffset2 + insertSize1 - replacedByteCount2 + replaceSize2 - removeSize1
               + 2 * writeBlockSizeInBytes),
            1, insertSize2 % writeBlockSizeInBytes, insertBuffer2, 2 * writeBlockSizeInBytes),
         new SingleActionSequence(insertAction2),
         // Bytes between first remove and second insert
         ReadWriteActionSequence.createSingleBlock(at(removeOffset1 + removeSize1), readWriteRemainderSizeInBytes2,
            relativeWriteShiftInBytes2),
         new SingleActionSequence(removeAction1),
         // Bytes between first insert and second replace
         ReadWriteActionSequence.createSingleBlock(at(insertOffset1), readWriteRemainderSizeInBytes1,
            relativeWriteShiftInBytes1),
         new WriteActionSequence(at(insertOffset1 + replaceSize2 - replacedByteCount2), 1, insertSize1, insertBuffer1,
            0),
         new SingleActionSequence(insertAction1),
         // Bytes between second replace and first insert
         ReadWriteActionSequence.createSingleBlock(at(replaceOffset2 + replacedByteCount2),
            readWriteRemainderSizeInBytes0, relativeWriteShiftInBytes0),
         new WriteActionSequence(at(replaceOffset2), 1, replaceSize2, replacementBuffer2, 0),
         new SingleActionSequence(replaceAction2));
   }

   /**
    * Convenience version of {@link #createTestByteBufferOfSize(int, byte)}, starting at byte offset 0.
    * 
    * @param size
    *           The total size of the buffer to create. Must be positive. If this parameter is 0, the empty
    *           {@link ByteBuffer} is returned.
    * @return see {@link #createTestByteBufferOfSize(int, byte)}.
    */
   private static ByteBuffer createTestByteBufferOfSize(int size) {
      return createTestByteBufferOfSize(size, (byte) 0);
   }

   /**
    * Creates a test byte buffer of the given size, filled with increasing byte values starting from the given start
    * offset. The first goal of this method is to provide {@link ByteBuffer}s with non-uniform content (e.g. not only
    * filled with zeroes). This ensures that during testing the correct portions of a ByteBuffer are checked against, so
    * it avoids bugs. If you would instead always use {@link ByteBuffer} filled uniformly with the same byte, you would
    * e.g. not find bugs related to wrongly copying contents of the buffer. The second goal of this method is to yet
    * provide reproducible data. Thus it does not simply return a random content buffer, but you always know what you
    * get, and subsequent calls with the same parameters return always the same {@link ByteBuffer}.
    * 
    * @param size
    *           The total size of the buffer to create. Must be positive. If this parameter is 0, the empty
    *           {@link ByteBuffer} is returned.
    * @param startByteOffset
    *           The start offset at which each byte sequence starts. Must be smaller than {@link Byte#MAX_VALUE}.
    * @return A test {@link ByteBuffer} of the given size, filled with size / (Byte.MAX_VALUE - startByteOffset)
    *         identical blocks of Bytes at front, each a sequence of length Byte.MAX_VALUE - startByteOffset, that
    *         starts with startByteOffset as the first byte, and continues with startByteOffset+1 as second byte and so
    *         on. At the end, there are size % (Byte.MAX_VALUE - startByteOffset) bytes that follow the same sequence.
    */
   private static ByteBuffer createTestByteBufferOfSize(int size, byte startByteOffset) {
      Reject.ifTrue(size < 0, "size < 0");
      Reject.ifNotInInterval(startByteOffset, 0, Byte.MAX_VALUE - 1, "startByteOffset");

      if (size == 0) {
         return ByteBuffer.allocate(0);
      }

      int blockSize = Byte.MAX_VALUE - startByteOffset;
      int blockCount = size / blockSize;
      int remainder = size % blockSize;

      List<Byte> totalByteSequence = new ArrayList<>();

      for (int j = 0; j < blockCount; j++) {
         List<Byte> bytes = IntStream.rangeClosed(startByteOffset, blockSize - 1).mapToObj(i -> (byte) i)
            .collect(Collectors.toList());

         totalByteSequence.addAll(bytes);
      }

      totalByteSequence.addAll(
         IntStream.rangeClosed(startByteOffset, remainder - 1).mapToObj(i -> (byte) i).collect(Collectors.toList()));

      return ByteBuffer.wrap(ByteArrayUtils.toArray(totalByteSequence));
   }

   /**
    * @return an instance of a new {@link MediumChangeManager} for testing
    */
   private MediumChangeManager getTestling() {
      return new MediumChangeManager(new MediumOffsetFactory(MediaTestUtility.DEFAULT_TEST_MEDIUM));
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
    * {@link InvalidTestDataException} in case of any I/O problems during the operation.
    * 
    * @param name
    *           The name of the test file, will be newly created, if it already exists it will be deleted first.
    * @param parentPath
    *           The parent folder of the file, must exist.
    */
   private static PrintStream setupTestFile(String name, Path parentPath) {
      try {
         Path testFile = parentPath.resolve(name);

         Files.deleteIfExists(testFile);

         return new PrintStream(new FileOutputStream(testFile.toFile()), false);
      } catch (IOException e) {
         throw new InvalidTestDataException("IO error during setup: " + e, e);
      }
   }

}
