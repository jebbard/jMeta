/**
 *
 * {@link AbstractWritableRandomAccessMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 08.11.2017
 *
 */
package com.github.jmeta.library.media.api.services;

import static com.github.jmeta.library.media.api.helper.TestMedia.at;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Assume;
import org.junit.Test;
import org.mockito.Mockito;

import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractWritableRandomAccessMediumStoreTest} contains all test methods that need to operate on a random-access
 * {@link Medium}.
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
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test
   public void cache_forFilledRandomAccessMediumWithBigCache_forOffsetBeyondEOM_throwsEOMException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset cacheOffset = at(currentMedium, currentMediumContent.length() + 15);
      int cacheSize = 10;

      testCache_throwsEndOfMediumException(cacheOffset, cacheSize, currentMediumContent);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#createMediumOffset(long)}.
    */
   @Test
   public void createMediumOffset_forFilledMedium_offsetBeforePendingChanges_referencesRemainUnchanged() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String insertedString = "ABCDEF";
      int insertOffset = 10;
      int removeOffset = 100;
      int removeSize = 200;

      mediumStoreUnderTest.open();

      MediumOffset firstReferenceBeforeChanges = mediumStoreUnderTest.createMediumOffset(0);
      MediumOffset secondReferenceBeforeChanges = mediumStoreUnderTest.createMediumOffset(5);
      MediumOffset thirdReferenceBeforeChanges = mediumStoreUnderTest.createMediumOffset(insertOffset - 1);

      MediumOffset theInsertOffset = mediumStoreUnderTest.createMediumOffset(insertOffset);
      mediumStoreUnderTest.insertData(theInsertOffset,
         ByteBuffer.wrap(insertedString.getBytes(Charsets.CHARSET_ASCII)));

      MediumOffset theRemoveOffset = mediumStoreUnderTest.createMediumOffset(removeOffset);
      MediumAction actionToUndo = mediumStoreUnderTest.removeData(theRemoveOffset, removeSize);

      mediumStoreUnderTest.undo(actionToUndo);

      Assert.assertEquals(at(currentMedium, 0), firstReferenceBeforeChanges);
      Assert.assertEquals(at(currentMedium, 5), secondReferenceBeforeChanges);
      Assert.assertEquals(at(currentMedium, insertOffset - 1), thirdReferenceBeforeChanges);
   }

   /**
    * Tests {@link MediumStore#createMediumOffset(long)}.
    */
   @Test
   public void createMediumOffset_forFilledMedium_offsetBehindPendingChanges_referencesRemainUnchanged() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String insertedString = "ABCDEF";
      int insertOffset = 10;
      int removeOffset = 100;
      int removeSize = 200;

      mediumStoreUnderTest.open();

      MediumOffset firstReferenceBeforeChanges = mediumStoreUnderTest.createMediumOffset(100);
      MediumOffset secondReferenceBeforeChanges = mediumStoreUnderTest.createMediumOffset(55);
      MediumOffset thirdReferenceBeforeChanges = mediumStoreUnderTest.createMediumOffset(insertOffset + 20);

      MediumOffset theInsertOffset = mediumStoreUnderTest.createMediumOffset(insertOffset);

      mediumStoreUnderTest.insertData(theInsertOffset,
         ByteBuffer.wrap(insertedString.getBytes(Charsets.CHARSET_ASCII)));

      MediumOffset theRemoveOffset = mediumStoreUnderTest.createMediumOffset(removeOffset);
      MediumAction actionToUndo = mediumStoreUnderTest.removeData(theRemoveOffset, removeSize);

      mediumStoreUnderTest.undo(actionToUndo);

      Assert.assertEquals(at(currentMedium, 100), firstReferenceBeforeChanges);
      Assert.assertEquals(at(currentMedium, 55), secondReferenceBeforeChanges);
      Assert.assertEquals(at(currentMedium, insertOffset + 20), thirdReferenceBeforeChanges);
   }

   /**
    * Tests {@link MediumStore#createMediumOffset(long)}.
    */
   @Test
   public void createMediumOffset_forFilledMedium_offsetBeforeChanges_referencesRemainUnchangedAfterFlush() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String insertedString = "ABCDEF";
      int insertOffset = 10;
      int removeOffset = 100;
      int removeSize = 200;

      mediumStoreUnderTest.open();

      MediumOffset firstReferenceBeforeChanges = mediumStoreUnderTest.createMediumOffset(0);
      MediumOffset secondReferenceBeforeChanges = mediumStoreUnderTest.createMediumOffset(5);
      MediumOffset thirdReferenceBeforeChanges = mediumStoreUnderTest.createMediumOffset(insertOffset - 1);

      scheduleAndFlush(createInsertAction(at(currentMedium, insertOffset), insertedString),
         createRemoveAction(at(currentMedium, removeOffset), removeSize));

      Assert.assertEquals(at(currentMedium, 0), firstReferenceBeforeChanges);
      Assert.assertEquals(at(currentMedium, 5), secondReferenceBeforeChanges);
      Assert.assertEquals(at(currentMedium, insertOffset - 1), thirdReferenceBeforeChanges);
   }

   /**
    * Tests {@link MediumStore#createMediumOffset(long)}.
    */
   @Test
   public void createMediumOffset_forFilledMedium_offsetBehindChanges_referencesAreCorrectlyChangedAfterFlush() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String insertedString1 = "SecondString";
      String insertedString2 = "ABCDEF";
      int insertOffset = 10;
      int removeOffset1 = 100;
      int removeSize1 = 200;
      int removeOffset2 = 70;
      int removeSize2 = 13;
      int replaceOffset1 = removeOffset1 + removeSize1;
      int replaceSize1 = 18;
      String replacementString1 = "REPL #1";
      int replaceOffset2 = 1000;
      int replaceSize2 = 10;
      String replacementString2 = "1234567890";

      String mediumContentBefore = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset firstReferenceBehindTwoInserts = mediumStoreUnderTest.createMediumOffset(insertOffset);
      MediumOffset secondReferenceBehindTwoInserts = mediumStoreUnderTest.createMediumOffset(insertOffset + 1);
      MediumOffset thirdReferenceBehindFirstRemoveOffset = mediumStoreUnderTest.createMediumOffset(removeOffset2 + 1);
      MediumOffset fourthReferenceBehindSecondRemove = mediumStoreUnderTest
         .createMediumOffset(removeOffset1 + removeSize1 + 10);
      MediumOffset fifthReferenceWithinFirstReplace = mediumStoreUnderTest.createMediumOffset(replaceOffset1 + 5);
      MediumOffset sixthReferenceAtEndOfMedium = mediumStoreUnderTest.createMediumOffset(mediumContentBefore.length());
      MediumOffset seventhReferenceBeforeLastReplace = mediumStoreUnderTest.createMediumOffset(replaceOffset2 - 2);
      MediumOffset eigthReferenceAtLastReplace = mediumStoreUnderTest.createMediumOffset(replaceOffset2);
      MediumOffset ninthReferenceBehindLastReplace = mediumStoreUnderTest
         .createMediumOffset(replaceOffset2 + replacementString2.length());

      scheduleAndFlush(new MediumAction[] { createInsertAction(at(currentMedium, insertOffset), insertedString1),
         createRemoveAction(at(currentMedium, removeOffset1), removeSize1),
         createReplaceAction(at(currentMedium, replaceOffset2), replaceSize2, replacementString2),
         createInsertAction(at(currentMedium, insertOffset), insertedString2),
         createRemoveAction(at(currentMedium, removeOffset2), removeSize2),
         createReplaceAction(at(currentMedium, replaceOffset1), replaceSize1, replacementString1), });

      mediumStoreUnderTest.close();

      String mediumContentAfter = getMediumContentAsString(currentMedium);

      int behindFirstOffsetShift = insertedString1.length() + insertedString2.length();
      int behindFourthOffsetShift = behindFirstOffsetShift - removeSize2 - removeSize1;
      int behindSeventhOffsetShift = behindFourthOffsetShift - replaceSize1 + replacementString1.length();
      int behindEigthOffsetShift = behindFourthOffsetShift - replaceSize1 + replacementString1.length() - replaceSize2
         + replacementString2.length();

      Assert.assertEquals(at(currentMedium, insertOffset + behindFirstOffsetShift), firstReferenceBehindTwoInserts);
      Assert.assertEquals(at(currentMedium, insertOffset + 1 + behindFirstOffsetShift),
         secondReferenceBehindTwoInserts);
      Assert.assertEquals(at(currentMedium, removeOffset2 + behindFirstOffsetShift),
         thirdReferenceBehindFirstRemoveOffset);
      // Minus 3 here because of this offset is inside the first replace
      Assert.assertEquals(at(currentMedium, removeOffset1 + removeSize1 + 10 - 3 + behindFourthOffsetShift),
         fourthReferenceBehindSecondRemove);
      Assert.assertEquals(at(currentMedium, replaceOffset1 + 5 + behindFourthOffsetShift),
         fifthReferenceWithinFirstReplace);
      Assert.assertEquals(at(currentMedium, mediumContentAfter.length()), sixthReferenceAtEndOfMedium);
      Assert.assertEquals(at(currentMedium, replaceOffset2 - 2 + behindSeventhOffsetShift),
         seventhReferenceBeforeLastReplace);
      Assert.assertEquals(at(currentMedium, replaceOffset2 + behindSeventhOffsetShift), eigthReferenceAtLastReplace);
      Assert.assertEquals(at(currentMedium, replaceOffset2 + replacementString2.length() + behindEigthOffsetShift),
         ninthReferenceBehindLastReplace);
   }

   /**
    * Tests {@link MediumStore#flush()} CF 1 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF1aSingleInsertAtStart_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createInsertAction(at(currentMedium, 0), "___CF1aSingleInsertAtStart___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 1 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF1bSingleInsertInTheMiddle_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createInsertAction(at(currentMedium, 200), "___CF1aSingleInsertInMiddle___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 1 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF1cSingleInsertAtTheEnd_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createInsertAction(at(currentMedium, currentMediumContent.length()), "___CF1aSingleInsertAtTheEnd___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 2 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF2aSingleRemoveAtStart_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createRemoveAction(at(currentMedium, 0), 35), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 2 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF2bSingleRemoveInTheMiddle_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createRemoveAction(at(currentMedium, 100), 142), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 2 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF2cSingleRemoveUntilEOM_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createRemoveAction(at(currentMedium, currentMediumContent.length() - 17), 17), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 2 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF2dSingleRemoveWholeMedium_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createRemoveAction(at(currentMedium, 0), currentMediumContent.length()), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 3 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF3aSingleInsertingReplaceAtStart_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createReplaceAction(at(currentMedium, 0), 5, "___CF3aSingleInsertingReplacementAtStart___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 3 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF3bSingleInsertingReplaceInTheMiddle_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createReplaceAction(at(currentMedium, 900), 21, "___CF3bSingleInsertingReplacementInTheMiddle___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 3 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF3cSingleRemovingReplaceInTheMiddle_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createReplaceAction(at(currentMedium, 600), 400, "___CF3cSingleRemovingReplacementInTheMiddle___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 3 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF3dSingleRemovingReplaceUntilTheEnd_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createReplaceAction(at(currentMedium, 600), currentMediumContent.length() - 600,
            "___CF3dSingleRemovingReplacementUntilTheEnd___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 3 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF3eSingleOverwritingReplaceAtStart_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String replacementText = "___CF3eSingleOverwritingReplacementAtStart___";
      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createReplaceAction(at(currentMedium, 0), replacementText.length(), replacementText), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 3 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF3fSingleInsertingReplaceWholeMedium_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createReplaceAction(at(currentMedium, 0), currentMediumContent.length(),
            "___CF3fSingleInsertingReplacementWholeMedium___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 4 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF4aMultipleInsertsAtSameOffset_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createInsertAction(at(currentMedium, 10), "___CF4aMultipleInsertsAtSameOffset[1]___"),
            createInsertAction(at(currentMedium, 10), "____CF4aMultipleInsertsAtSameOffset[2]____"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 4 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF4bMultipleInsertsAtDifferentOffsets_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createInsertAction(at(currentMedium, 0), "___CF4bMultipleInsertsAtDifferentOffset[1]___"),
         createInsertAction(at(currentMedium, 23),
            "____CF4bMultipleInsertsAtDifferentOffset[2.1]____############################################################"),
         createInsertAction(at(currentMedium, 23), "_CF4b[2.2]_"), createInsertAction(
            at(currentMedium, currentMediumContent.length()), "CF4bMultipleInsertsAtDifferentOffset[3]"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 4 (see Design Concept).
    */
   @Test
   public void flush_forEmptyWritableMedium_CF4cMultipleInsertsAtSameOffset_writesCorrectData() {
      mediumStoreUnderTest = createEmptyMediumStore();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createInsertAction(at(currentMedium, 0), "___CF4cMultipleInsertsAtDifferentOffset[1]___"),
         createInsertAction(at(currentMedium, 0),
            "__CF4cMultipleInsertsAtDifferentOffset--------------------------------------------------------------------"),
         createInsertAction(at(currentMedium, 0), "_CF4c[2.2]_"),
         createInsertAction(at(currentMedium, 0), "CF4cMultipleInsertsAtDifferentOffset[3]"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 5 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF5aMultipleRemovesAtDifferentOffsets_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createRemoveAction(at(currentMedium, 0), 5),
            createRemoveAction(at(currentMedium, 23), 11), createRemoveAction(at(currentMedium, 34), 450),
            createRemoveAction(at(currentMedium, 1000), currentMediumContent.length() - 1000), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 5 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF5bMultipleRemovesWholeMedium_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createRemoveAction(at(currentMedium, 0), 999),
            createRemoveAction(at(currentMedium, 999), currentMediumContent.length() - 999), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 6 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF6aMultipleReplacesAtDifferentOffsets_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createReplaceAction(at(currentMedium, 0), 5, "___CF6aMultipleReplacesAtDifferentOffset[1]___"),
         createReplaceAction(at(currentMedium, 23), 11, "CF6a[2]"),
         createReplaceAction(at(currentMedium, 34), 10, "__CF6a[3]_"),
         createReplaceAction(at(currentMedium, 800), currentMediumContent.length() - 800,
            "#############################################################################CF6a[4]#############################################################################"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 6 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF6bMultipleMutuallyEliminatingReplacesAtDifferentOffsets_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String firstReplacementText = "___CF6bMultipleMutuallyEliminatingReplacesAtDifferentOffset[1]___";
      String secondReplacementText = "CF6b[2]";

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createReplaceAction(at(currentMedium, 0), secondReplacementText.length(), firstReplacementText),
         createReplaceAction(at(currentMedium, 666), firstReplacementText.length(), secondReplacementText), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 6 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF6cMultipleReplacesAtDifferentOffsets_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithComplexChangeSet_writesExpectedDataAndUndosActions(
         createFlushExpectationPath("expectation_flush_CF6c.txt"),
         new MediumAction[] { createReplaceAction(at(currentMedium, 700), 100,
            "eeeeeeeeeeeeeerrrrrrrrrrrrrrrrrttttttttttttttttt123456_________::::::::::::::::::::::::::_====7890zzzzzzzzzzzzzzuuuuuuuuuuuuiiiiiiiiiiiiiiiiiooooooooo"),
            createReplaceAction(at(currentMedium, 820), 100,
               "skdlforlaksldlopoerthutzhotzhjvmcklaewqopertntmvhsncmdjrhktmantenadfmatenmatswrt"),
            createReplaceAction(at(currentMedium, 0), 50, "=___abcdefghijkl_mnopqrstuvwxyz___="),
            createReplaceAction(at(currentMedium, 350), 35, "12345678901234567890123456789012345678901234567890"),
            createReplaceAction(at(currentMedium, 800), 20, "klklklklklklklklopqe"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 7 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF7aMutuallyEliminatingInsertsAndRemoves_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String insertText = "___CF7aMultipleMutuallyEliminatingInsertsAndRemoves[1]___";
      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createInsertAction(at(currentMedium, 0), insertText),
            createRemoveAction(at(currentMedium, 700), insertText.length()), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 7 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF7bMultipleInsertsAndRemovesAtDifferentOffsets_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithComplexChangeSet_writesExpectedDataAndUndosActions(
         createFlushExpectationPath("expectation_flush_CF7b.txt"),
         new MediumAction[] {
            createInsertAction(at(currentMedium, 4), "___CF7bMultipleInsertsAndRemovesAtDifferentOffsets[1]___"),
            createRemoveAction(at(currentMedium, 4), 200), createInsertAction(at(currentMedium, 4), "===CF7b[2]==="),
            createRemoveAction(at(currentMedium, 304), currentMediumContent.length() - 304),
            createInsertAction(at(currentMedium, currentMediumContent.length()), "###CF7b[3]###"),
            createInsertAction(at(currentMedium, currentMediumContent.length()), "x"),
            createRemoveAction(at(currentMedium, 204), 10), createInsertAction(at(currentMedium, 204), "TEST"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 8 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF8aMutuallyEliminatingInsertsAndReplaces_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String insertText = "___CF8aMultipleMutuallyEliminatingInsertsAndReplaces[1]___";
      String replacementText = "___CF8aMultipleMutuallyEliminatingInsertsAndReplaces[2]_______";

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createInsertAction(at(currentMedium, 11), insertText), createReplaceAction(
            at(currentMedium, 11), replacementText.length() + insertText.length(), replacementText), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 8b (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF8bReplaceThenInsertAtSameOffset_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithComplexChangeSet_writesExpectedDataAndUndosActions(
         createFlushExpectationPath("expectation_flush_CF8b.txt"), new MediumAction[] {

            createReplaceAction(at(currentMedium, 4), 50, "===CF9[000000000000]==="), createInsertAction(
               at(currentMedium, 4), "___CF9MultipleInsertsRemovesAndReplacesAtDifferentOffsets[2]___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 9 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF9aMultipleInsertsRemovesAndReplacesAtDifferentOffsets_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      testFlushWithComplexChangeSet_writesExpectedDataAndUndosActions(
         createFlushExpectationPath("expectation_flush_CF9a.txt"),
         new MediumAction[] { createReplaceAction(at(currentMedium, 4), 50, "===CF9[000000000000]==="),
            createInsertAction(at(currentMedium, 56), "===CF9[1]==="),
            createReplaceAction(at(currentMedium, 304), currentMediumContent.length() - 304, ">>>>uuuuuuuuuuuuuu<<<<"),
            createInsertAction(at(currentMedium, 4), "___CF9MultipleInsertsRemovesAndReplacesAtDifferentOffsets[2]___"),
            createInsertAction(at(currentMedium, currentMediumContent.length()), "###CF9[3]###"),
            createReplaceAction(at(currentMedium, 215), 1, "REPLACEMENT #2"),
            createRemoveAction(at(currentMedium, 204), 10), createInsertAction(at(currentMedium, 204), "TEST"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 9 (see Design Concept).
    *
    * A quite complex case with a brutal count of changes
    */
   @Test
   public void flush_forFilledWritableMedium_CF9bQuiteComplexCase_writesCorrectData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String currentMediumContent = getMediumContentAsString(currentMedium);

      // @formatter:off
      testFlushWithComplexChangeSet_writesExpectedDataAndUndosActions(
         createFlushExpectationPath("expectation_flush_CF9b.txt"), new MediumAction[] {
            createInsertAction(at(currentMedium, 1),
               "===CF9b[11111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111]==="),
            createReplaceAction(at(currentMedium, 2), 320, "=="),
            createRemoveAction(at(currentMedium, 1), 1),
            createInsertAction(at(currentMedium, 1), "a"),
            createInsertAction(at(currentMedium, 1), "b"),
            createInsertAction(at(currentMedium, 1), "c"),
            createInsertAction(at(currentMedium, 1), "d"),
            createInsertAction(at(currentMedium, 1), "e"),
            createInsertAction(at(currentMedium, 1), "f"),
            createInsertAction(at(currentMedium, 1), "g"),
            createInsertAction(at(currentMedium, 1), "h"),
            createInsertAction(at(currentMedium, 1), "i"),
            createInsertAction(at(currentMedium, 1), "j"),
            createInsertAction(at(currentMedium, 323), "___CF9b___"),
            createReplaceAction(at(currentMedium, 323), 10, "REPLACEMENT #2"),
            createRemoveAction(at(currentMedium, 347), 1),
            createInsertAction(at(currentMedium, 347), "/"),
            createRemoveAction(at(currentMedium, 351), 621),
            createInsertAction(at(currentMedium, 348), "\\"),
            createRemoveAction(at(currentMedium, 983), 11),
            createInsertAction(at(currentMedium, 1000),
               "sadasdfrfgjkhksdghssodghlshglskhgpahnjskehjgpiwerhgasdasdafdasfadfgegsdgsdgsgsdgjhaegsasahjephgehgsghd"
                  + "lkshgoilshglhsdgioligolishgehksoegsgelslgeseguhesueghsjeghseieisezegzseigzseigzskzgusgheskejghskeghs"
                  + "eungsenhegsughsgekndvshjgskhgdskgsvhnsdkgsdkghskd"),
            createReplaceAction(at(currentMedium, 333), 10, "1234567890"),
            createReplaceAction(at(currentMedium, 350), 1,
               "asdeafdfergbfgtekjkerjtertklkldksasksfgkasdfgafgasdfwergwkjgiohjbowejbvwobhji"
                  + "whhnakjshcjkhiuehveuhvakevjukiehvuihkhsbjkfbhikfubhiehflhsdhguiehglaeghaghk"
                  + "dlahgiuobhvkehlnblehlhaxsjkssdjashjasdfedfwvacnjavncjudvhuzwhuiherlkjhlkays"
                  + "hcoilvoihdvaihfvalkhaldvhid.,ksancaljkhnvkajdhvkajdhvkadhvalhfslakhdfaslfha"
                  + "jafaslfkhjasfkahsfslahfa"),
            createInsertAction(at(currentMedium, 1000), "TEST"),
            createReplaceAction(at(currentMedium, 1000), 100, "###REPLACEMENT###"),
            createRemoveAction(at(currentMedium, 1100), 50),
            createReplaceAction(at(currentMedium, 1151), 2, "Near the end"),
            createInsertAction(at(currentMedium, 995), "tESt1"),
            createInsertAction(at(currentMedium, 996), "tESt2"),
            createInsertAction(at(currentMedium, 997), "tESt3"),
            createInsertAction(at(currentMedium, 998), "tESt4"),
            createInsertAction(at(currentMedium, 1100), "Before"),
            createInsertAction(at(currentMedium, 1151), "Before2"),
            createRemoveAction(at(currentMedium, 1100), 51),
            createRemoveAction(at(currentMedium, 1153), currentMediumContent.length() - 1153),
         });
      // @formatter:on
   }

   /**
    * Tests {@link MediumStore#flush()}.
    */
   @Test
   public void flush_forFilledWritableMedium_withoutPendingChanges_doesNotPerformAnyChanges() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String mediumContentBefore = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.flush();

      mediumStoreUnderTest.close();

      String mediumContentAfter = getMediumContentAsString(currentMedium);

      Assert.assertEquals(mediumContentBefore, mediumContentAfter);

      Mockito.verify(mediumChangeManagerSpy).createFlushPlan(currentMedium.getMaxReadWriteBlockSizeInBytes(),
         currentMedium.getCurrentLength());
      Mockito.verify(mediumChangeManagerSpy).iterator();
      Mockito.verify(mediumChangeManagerSpy, Mockito.atMost(1)).getScheduledActionCount();
      Mockito.verify(mediumCacheSpy).clear();

      Mockito.verifyZeroInteractions(mediumCacheSpy);
      Mockito.verifyZeroInteractions(mediumChangeManagerSpy);

      verifyExactlyNReads(0);
      verifyExactlyNWrites(0);
   }

   /**
    * Tests {@link MediumStore#flush()}.
    */
   @Test
   public void flush_forFilledWritableMedium_twiceSecondTimeNoPendingChanges_doesNotPerformChangesTheSecondTime() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String mediumContentBefore = getMediumContentAsString(currentMedium);

      String insertedString = "ABCDEF";
      int insertOffset = 10;
      int removeOffset = 100;
      int removeSize = 200;

      mediumStoreUnderTest.open();

      scheduleAndFlush(createInsertAction(at(currentMedium, insertOffset), insertedString),
         createRemoveAction(at(currentMedium, removeOffset), removeSize));

      // Second flush
      mediumStoreUnderTest.flush();

      mediumStoreUnderTest.close();

      ExpectedMediumContentBuilder expectationBuilder = new ExpectedMediumContentBuilder(mediumContentBefore);

      expectationBuilder.appendFromOriginal(0, insertOffset);
      expectationBuilder.appendLiteralString(insertedString);
      expectationBuilder.appendFromOriginal(insertOffset, removeOffset - insertOffset);
      expectationBuilder.appendFromOriginal(removeOffset + removeSize,
         mediumContentBefore.length() - removeOffset - removeSize);

      String mediumContentAfter = getMediumContentAsString(currentMedium);

      Assert.assertEquals(expectationBuilder.buildExpectedContent(), mediumContentAfter);

      Mockito.verify(mediumChangeManagerSpy).createFlushPlan(currentMedium.getMaxReadWriteBlockSizeInBytes(),
         mediumContentBefore.length());
      Mockito.verify(mediumChangeManagerSpy).createFlushPlan(currentMedium.getMaxReadWriteBlockSizeInBytes(),
         mediumContentAfter.length());
      Mockito.verify(mediumChangeManagerSpy, Mockito.times(2)).iterator();
   }

   /**
    * Tests {@link MediumStore#flush()}.
    */
   @Test
   public void flush_forFilledWritableMedium_withPartlyUndoneAndPendingChanges_onlyPerformsPendingChanges() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String mediumContentBefore = getMediumContentAsString(currentMedium);

      String insertedString = "ABCDEF";
      byte[] insertionBytes = insertedString.getBytes(Charsets.CHARSET_ASCII);
      int insertOffset = 10;
      int removeOffset = 100;
      int removeSize = 200;

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.insertData(at(currentMedium, insertOffset), ByteBuffer.wrap(insertionBytes));

      MediumAction actionToUndo = mediumStoreUnderTest.removeData(at(currentMedium, removeOffset), removeSize);

      mediumStoreUnderTest.undo(actionToUndo);

      mediumStoreUnderTest.flush();

      mediumStoreUnderTest.close();

      String mediumContentAfter = getMediumContentAsString(currentMedium);

      ExpectedMediumContentBuilder expectationBuilder = new ExpectedMediumContentBuilder(mediumContentBefore);

      expectationBuilder.appendFromOriginal(0, insertOffset);
      expectationBuilder.appendLiteralString(insertedString);
      expectationBuilder.appendFromOriginal(insertOffset, mediumContentBefore.length() - insertOffset);

      Assert.assertEquals(expectationBuilder.buildExpectedContent(), mediumContentAfter);
   }

   /**
    * Tests {@link MediumStore#flush()}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void flush_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.flush();
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledRandomAccessMediumWithBigCache_forOffsetBeyondEOM_throwsEOMException() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset getDataOffset = at(currentMedium, currentMediumContent.length() + 15);
      int getDataSize = 10;

      testGetData_throwsEndOfMediumException(getDataOffset, getDataSize, currentMediumContent.length(),
         currentMediumContent);

      assertCacheIsEmpty();
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMedium_withPendingChanges_stillReturnsOldData() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String mediumContentBefore = getMediumContentAsString(currentMedium);

      String insertedString = "ABCDEF";
      byte[] insertionBytes = insertedString.getBytes(Charsets.CHARSET_ASCII);
      int insertOffset = 10;
      int removeOffset = 100;
      int removeSize = 200;

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.insertData(at(currentMedium, insertOffset), ByteBuffer.wrap(insertionBytes));

      mediumStoreUnderTest.removeData(at(currentMedium, removeOffset), removeSize);

      testGetData_returnsExpectedData(at(currentMedium, 5), 999, mediumContentBefore);
   }

   /**
    * Tests {@link MediumStore#insertData(MediumOffset, ByteBuffer)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void insertData_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.insertData(at(currentMedium, 10), ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#insertData(MediumOffset, ByteBuffer)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void insertData_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.insertData(at(TestMedia.OTHER_MEDIUM, 10), ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test
   public void isAtEndOfMedium_forFilledRandomAccessMediumAtEOM_returnsTrue() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      int endOfMediumOffset = getMediumContentAsString(currentMedium).length();

      mediumStoreUnderTest.open();

      Assert.assertTrue(mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, endOfMediumOffset)));
   }

   /**
    * Tests {@link MediumStore#createMediumOffset(long)}.
    */
   @Test
   public void isAtEndOfMedium_forReferencePreviouslyAtEOM_stillReturnsTrueAfterFlushingChanges() {
      mediumStoreUnderTest = createFilledMediumStoreWithBigCache();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      String insertedString1 = "SecondString";
      int insertOffset = 10;
      int removeOffset1 = 100;
      int removeSize1 = 200;

      String mediumContentBefore = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      MediumOffset referenceAtEndOfMedium = mediumStoreUnderTest.createMediumOffset(mediumContentBefore.length());

      scheduleAndFlush(new MediumAction[] { createInsertAction(at(currentMedium, insertOffset), insertedString1),
         createRemoveAction(at(currentMedium, removeOffset1), removeSize1), });

      mediumStoreUnderTest.close();

      String mediumContentAfter = getMediumContentAsString(currentMedium);

      Assert.assertEquals(at(currentMedium, mediumContentAfter.length()), referenceAtEndOfMedium);
   }

   /**
    * Tests {@link MediumStore#removeData(MediumOffset, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void removeData_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.removeData(at(currentMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#removeData(MediumOffset, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void removeData_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.removeData(at(TestMedia.OTHER_MEDIUM, 10), 20);
   }

   /**
    * Tests {@link MediumStore#replaceData(MediumOffset, int, java.nio.ByteBuffer)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void replaceData_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.replaceData(at(currentMedium, 10), 20, ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void replaceData_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(at(TestMedia.OTHER_MEDIUM, 10), 20, ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void undo_forNonPendingAction_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      MediumAction mediumAction = new MediumAction(MediumActionType.REMOVE, new MediumRegion(at(currentMedium, 10), 20),
         0, null);

      mediumAction.setDone();

      mediumStoreUnderTest.undo(mediumAction);
   }

   /**
    * Tests {@link MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void undo_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest
         .undo(new MediumAction(MediumActionType.REMOVE, new MediumRegion(at(currentMedium, 10), 20), 0, null));
   }

   /**
    * Tests {@link MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void undo_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.undo(
         new MediumAction(MediumActionType.REMOVE, new MediumRegion(at(TestMedia.OTHER_MEDIUM, 10), 20), 0, null));
   }

   /**
    * Tests flush with scheduling the given actions before
    *
    * @param actionsInExecutionOrder
    *           The {@link MediumAction} to be executed in the given order
    * @return The scheduled {@link MediumAction}s
    */
   protected List<MediumAction> scheduleAndFlush(MediumAction... actionsInExecutionOrder) {
      Reject.ifNull(actionsInExecutionOrder, "actionsInExecutionOrder");

      List<MediumAction> scheduledActions = new ArrayList<>();

      for (int i = 0; i < actionsInExecutionOrder.length; i++) {
         MediumAction currentAction = actionsInExecutionOrder[i];

         MediumOffset startOffset = mediumStoreUnderTest
            .createMediumOffset(currentAction.getRegion().getStartOffset().getAbsoluteMediumOffset());

         ByteBuffer actionBytes = currentAction.getActionBytes();

         switch (currentAction.getActionType()) {
            case INSERT:
               scheduledActions.add(mediumStoreUnderTest.insertData(startOffset, actionBytes));
            break;

            case REMOVE:
               scheduledActions.add(mediumStoreUnderTest.removeData(startOffset, currentAction.getRegion().getSize()));
            break;

            case REPLACE:
               scheduledActions
                  .add(mediumStoreUnderTest.replaceData(startOffset, currentAction.getRegion().getSize(), actionBytes));
            break;

            default:
               throw new RuntimeException("Only types INSERT, REMOVE and REPLACE can be used");
         }
      }

      mediumStoreUnderTest.flush();

      return scheduledActions;
   }

   /**
    * Creates a {@link Path} pointing to the given file containing expectations for flush testing.
    *
    * @param fileName
    *           The file name
    * @return a {@link Path} pointing to the given file containing expectations for flush testing.
    */
   protected Path createFlushExpectationPath(String fileName) {
      return TestMedia.TEST_FILE_DIRECTORY_PATH.resolve("MediumStoreTests").resolve(fileName);
   }

   /**
    * Creates an insert {@link MediumAction}
    *
    * @param offset
    *           The offset to use
    * @param insertText
    *           The text to insert
    * @return The corresponding insert {@link MediumAction}
    */
   protected static MediumAction createInsertAction(MediumOffset offset, String insertText) {
      return new MediumAction(MediumActionType.INSERT, new MediumRegion(offset, insertText.length()), 0,
         ByteBuffer.wrap(insertText.getBytes(Charsets.CHARSET_UTF8)));
   }

   /**
    * Creates a remove {@link MediumAction}
    *
    * @param offset
    *           The offset to use
    * @param removedByteCount
    *           The number of bytes to remove
    * @return The corresponding remove {@link MediumAction}
    */
   protected static MediumAction createRemoveAction(MediumOffset offset, int removedByteCount) {
      return new MediumAction(MediumActionType.REMOVE, new MediumRegion(offset, removedByteCount), 0, null);
   }

   /**
    * Creates an insert {@link MediumAction}
    *
    * @param offset
    *           The offset to use
    * @param replacedByteCount
    *           The number of bytes to replace
    * @param replacementText
    *           The replacement text
    * @return The corresponding replace {@link MediumAction}
    */
   protected static MediumAction createReplaceAction(MediumOffset offset, int replacedByteCount,
      String replacementText) {
      return new MediumAction(MediumActionType.REPLACE, new MediumRegion(offset, replacedByteCount), 0,
         ByteBuffer.wrap(replacementText.getBytes(Charsets.CHARSET_UTF8)));
   }

   /**
    * Tests {@link MediumStore#flush()} to write changes as expected and to undo all given actions
    *
    * IMPORTANT NOTE: This method must only be used for action offsets that are increasing, i.e. actions with index i in
    * the specified array must have equal or bigger start offset than actions with offset j, if j &gt; i. I.e. all
    * actions in the specified array must be sorted in offset order, and they are also executed this way. If you test
    * cases where the execution order differs from the offset order (which is allowed), this method MUST NOT be used.
    * Use {@link #testFlushWithComplexChangeSet_writesExpectedDataAndUndosActions(Path, MediumAction...)} instead.
    *
    * @param actionsInOffsetAndExecutionOrder
    *           The {@link MediumAction}s to apply in the given order; they are also used to derive the expectations for
    *           the written data in the end
    */
   private void testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
      MediumAction... actionsInOffsetAndExecutionOrder) {

      Reject.ifNull(actionsInOffsetAndExecutionOrder, "actionsInOffsetAndExecutionOrder");

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      ExpectedMediumContentBuilder expectationBuilder = new ExpectedMediumContentBuilder(currentMediumContent);

      int lastStringIndex = 0;

      List<MediumAction> scheduledActions = new ArrayList<>();

      MediumOffset previousStartOffset = null;

      for (int i = 0; i < actionsInOffsetAndExecutionOrder.length; i++) {
         MediumAction currentAction = actionsInOffsetAndExecutionOrder[i];

         MediumOffset startOffset = mediumStoreUnderTest
            .createMediumOffset(currentAction.getRegion().getStartOffset().getAbsoluteMediumOffset());
         int startOffsetAsInt = (int) startOffset.getAbsoluteMediumOffset();

         if (previousStartOffset != null) {
            if (startOffset.before(previousStartOffset)) {
               throw new InvalidTestDataException(
                  "This method must only be used with MediumAction arrays whose entries are sorted by offset, ascending",
                  null);
            }
         }

         if (startOffsetAsInt > lastStringIndex) {
            expectationBuilder.appendFromOriginal(lastStringIndex, startOffsetAsInt - lastStringIndex);
         }

         ByteBuffer actionBytes = currentAction.getActionBytes();

         if (actionBytes != null) {

            byte[] actionByteArray = ByteBufferUtils.asByteArrayCopy(actionBytes);
            String actionBytesAsString = new String(actionByteArray, Charsets.CHARSET_UTF8);

            expectationBuilder.appendLiteralString(actionBytesAsString);
         }

         switch (currentAction.getActionType()) {
            case INSERT:
               scheduledActions.add(mediumStoreUnderTest.insertData(startOffset, actionBytes));
               lastStringIndex = startOffsetAsInt;
            break;

            case REMOVE:
               scheduledActions.add(mediumStoreUnderTest.removeData(startOffset, currentAction.getRegion().getSize()));
               lastStringIndex = startOffsetAsInt + currentAction.getRegion().getSize();
            break;

            case REPLACE:
               scheduledActions
                  .add(mediumStoreUnderTest.replaceData(startOffset, currentAction.getRegion().getSize(), actionBytes));
               lastStringIndex = startOffsetAsInt + currentAction.getRegion().getSize();
            break;

            default:
               throw new RuntimeException("Only types INSERT, REMOVE and REPLACE can be used");
         }
      }

      if (lastStringIndex < currentMediumContent.length()) {
         expectationBuilder.appendFromOriginal(lastStringIndex, currentMediumContent.length() - lastStringIndex);
      }

      mediumStoreUnderTest.flush();

      scheduledActions.forEach(action -> Assert.assertFalse(action.isPending()));

      mediumStoreUnderTest.close();

      String expectedMediumContent = expectationBuilder.buildExpectedContent();

      String actualMediumContent = getMediumContentAsString(currentMedium);

      Assert.assertEquals(expectedMediumContent, actualMediumContent);
   }

   /**
    * Tests {@link MediumStore#flush()} to write changes as expected and to undo all given actions. This method is for
    * complex change sets where offset order does not need to be equal to execution order.
    *
    * For easier cases, you can also consider using
    * {@link #testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(MediumAction...)} where you do not need to
    * use an expectation file.
    *
    * @param expectedFinalMediumContentFile
    *           A {@link Path} pointing to an existing UTF8-encoded file only containing standard (ideally only
    *           printable) ASCII characters; it contains the expected final state of the medium after all changes have
    *           been applied using {@link MediumStore#flush()}.
    * @param actionsInExecutionOrder
    *           The {@link MediumAction}s to execute in the given order; they are not used to derive the expectations
    *           for the written data
    */
   private void testFlushWithComplexChangeSet_writesExpectedDataAndUndosActions(Path expectedFinalMediumContentFile,
      MediumAction... actionsInExecutionOrder) {
      Reject.ifNull(expectedFinalMediumContentFile, "expectedFinalMediumContentFile");

      Reject.ifFalse(Files.isRegularFile(expectedFinalMediumContentFile),
         "Files.isRegularFile(expectedFinalMediumContentFile)");

      mediumStoreUnderTest.open();

      List<MediumAction> scheduledActions = scheduleAndFlush(actionsInExecutionOrder);

      scheduledActions.forEach(action -> Assert.assertFalse(action.isPending()));

      mediumStoreUnderTest.close();

      String expectedMediumContent = new String(MediaTestUtility.readFileContent(expectedFinalMediumContentFile),
         Charsets.CHARSET_UTF8);

      String actualMediumContent = getMediumContentAsString(currentMedium);

      Assert.assertEquals(expectedMediumContent, actualMediumContent);
   }
}
