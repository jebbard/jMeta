/**
 *
 * {@link AbstractMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.api.services;

import static com.github.jmeta.library.media.api.helper.TestMedia.at;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.cache.MediumCache;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManager;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.offset.MediumOffsetFactory;
import com.github.jmeta.library.media.impl.store.StandardMediumStore;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractMediumStoreTest} is the base class for testing the {@link MediumStore} interface. In contrast to
 * {@link AbstractReadOnlyMediumStoreTest}, it uses also writable media, and it does not contain the default test cases
 * for {@link MediumStore#open()}, {@link MediumStore#close()}, {@link MediumStore#getMedium()} and
 * {@link MediumStore#createMediumOffset(long)}.
 * 
 * Each subclass corresponds to a specific {@link Medium} type, a read-only or writable {@link Medium} instance, as well
 * as a cached or un-cached medium. The sub-class hierarchy is non-trivial, so here is the explanation of the purpose of
 * each abstract subclass:
 * <ul>
 * <li>{@link AbstractMediumStoreTest}: Contains all tests that should run for any medium type and caching combinations,
 * thus e.g. all general negative tests (closed media and wrong reference)</li>
 * <li>{@link AbstractCachedMediumStoreTest}: Tests based on a cached medium; most of the "normal" read test cases go
 * here, no write test cases and no parameter negative tests</li>
 * <li>{@link AbstractCachedAndWritableRandomAccessMediumStoreTest}: Tests based on a cached, writable and thus
 * random-access medium; additional "normal" write test cases go here and no parameter negative tests</li>
 * <li>{@link AbstractUnCachedMediumStoreTest}: Tests based on an un-cached medium; only special read test cases
 * specifically designed for an un-cached medium go here, no write test cases</li>
 * <li>{@link AbstractUnCachedAndWritableRandomAccessMediumStoreTest}: Tests based on an un-cached, writable and thus
 * random-access medium; only special write test cases specifically designed for an un-cached medium go here</li>
 * </ul>
 * 
 * The filled media used for testing all must contain {@link TestMedia#FIRST_TEST_FILE_CONTENT} a String fully
 * containing only human-readable standard ASCII characters, and must be UTF-8 encoded. This guarantees that 1 bytes = 1
 * character. Furthermore, all bytes inserted must also be standard human-readable ASCII characters with this property.
 * 
 * There are specific naming conventions for testing {@link MediumStore#getData(MediumOffset, int)} and
 * {@link MediumStore#cache(MediumOffset, int)}: [method name]_[medium type]_[parameter values, esp. offset
 * range]_[expected behaviour].
 *
 * @param <T>
 *           The type of {@link Medium} to use
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractMediumStoreTest<T extends Medium<?>> {

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
    * For getting the current test case's name, must be public
    */
   @Rule
   public TestName testName = new TestName();

   protected MediumStore mediumStoreUnderTest;

   protected T currentMedium;

   protected MediumAccessor<T> mediumAccessorSpy;

   protected MediumCache mediumCacheSpy;

   protected MediumOffsetFactory mediumReferenceFactorySpy;

   protected MediumChangeManager mediumChangeManagerSpy;

   /**
    * Validates all test files needed in this test class
    */
   @BeforeClass
   public static void validateTestFiles() {
      TestMedia.validateTestFiles();
   }

   /**
    * Closes the {@link MediumStore} under test, if necessary.
    */
   @After
   public void tearDown() {
      if (mediumStoreUnderTest != null && mediumStoreUnderTest.isOpened()) {
         mediumStoreUnderTest.close();
      }
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void isAtEndOfMedium_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, 10));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void isAtEndOfMedium_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.isAtEndOfMedium(at(TestMedia.OTHER_MEDIUM, 10));
   }

   /**
    * Tests {@link MediumStore#createMediumOffset(long)}.
    */
   @Test
   public void isAtEndOfMedium_forReferencePreviouslyAtEOM_stillReturnsTrueAfterFlushingChanges() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
    * Tests {@link MediumStore#getCachedByteCountAt(MediumOffset)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void getCachedByteCountAt_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 0)));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getCachedByteCountAt_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.getCachedByteCountAt(at(TestMedia.OTHER_MEDIUM, 10));
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void cache_forClosedMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void cache_forReferencePointingToWrongMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(TestMedia.OTHER_MEDIUM, 10), 10);
   }

   /**
    * Tests {@link MediumStore#createMediumOffset(long)}.
    */
   @Test
   public void createMediumOffset_forFilledMedium_offsetBeforePendingChanges_referencesRemainUnchanged() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMedium_noCacheBefore_returnsExpectedData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 5;
      int getDataSize = 400;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMedium_partlyCacheBeforeWithGaps_returnsExpectedData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
      cacheNoEOMExpected(at(currentMedium, 30), 100);
      cacheNoEOMExpected(at(currentMedium, 135), 200);

      long getDataStartOffset = 5;
      int getDataSize = 400;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMedium_fullMediumCacheBefore_returnsExpectedData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 0), currentMediumContent.length());

      long getDataStartOffset = 5;
      int getDataSize = 400;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMedium_withPendingChanges_stillReturnsOldData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forEmptyMedium_fromStart_throwsEOMException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      testGetData_throwsEndOfMediumException(at(currentMedium, 0), 1, currentMedium.getMaxReadWriteBlockSizeInBytes(),
         currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test
   public void getData_forFilledMedium_fromMiddleToBeyondEOMAndNoCacheBefore_throwsEOMException() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      testGetData_throwsEndOfMediumException(at(currentMedium, 15), currentMediumContent.length(),
         currentMedium.getMaxReadWriteBlockSizeInBytes(), currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void getData_forClosedMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      getDataNoEOMExpected(at(currentMedium, 10), 10);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getData_forReferencePointingToWrongMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      getDataNoEOMExpected(at(TestMedia.OTHER_MEDIUM, 10), 10);
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
    * Tests {@link MediumStore#flush()} CF 1 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF1aSingleInsertAtStart_writesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createInsertAction(at(currentMedium, 0), "___CF1aSingleInsertAtStart___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 1 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF1bSingleInsertInTheMiddle_writesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createInsertAction(at(currentMedium, 200), "___CF1aSingleInsertInMiddle___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 1 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF1cSingleInsertAtTheEnd_writesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createRemoveAction(at(currentMedium, 0), 35), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 2 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF2bSingleRemoveInTheMiddle_writesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
         new MediumAction[] { createRemoveAction(at(currentMedium, 100), 142), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 2 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF2cSingleRemoveUntilEOM_writesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createReplaceAction(at(currentMedium, 0), 5, "___CF3aSingleInsertingReplacementAtStart___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 3 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF3bSingleInsertingReplaceInTheMiddle_writesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createReplaceAction(at(currentMedium, 900), 21, "___CF3bSingleInsertingReplacementInTheMiddle___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 3 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF3cSingleRemovingReplaceInTheMiddle_writesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      Assume.assumeTrue(!mediumStoreUnderTest.getMedium().isReadOnly());

      testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(new MediumAction[] {
         createReplaceAction(at(currentMedium, 600), 400, "___CF3cSingleRemovingReplacementInTheMiddle___"), });
   }

   /**
    * Tests {@link MediumStore#flush()} CF 3 (see Design Concept).
    */
   @Test
   public void flush_forFilledWritableMedium_CF3dSingleRemovingReplaceUntilTheEnd_writesCorrectData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
      mediumStoreUnderTest = createDefaultFilledMediumStore();

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
    * Creates an empty {@link Medium} for testing, i.e. with zero bytes.
    * 
    * For writable media, be sure to only return a copy of the original medium such that the original medium is not
    * modified by writing tests and all tests remain repeatable.
    * 
    * @param testMethodName
    *           the name of the current test method, can be used to create a copy of the original medium with the given
    *           name
    * 
    * @return an empty {@link Medium} for testing
    * 
    * @throws IOException
    *            In case of any errors creating the {@link Medium}
    */
   protected abstract T createEmptyMedium(String testMethodName) throws IOException;

   /**
    * Creates a {@link Medium} containing {@link TestMedia#FIRST_TEST_FILE_CONTENT} as content, backed or not backed by
    * a cache with the given maximum cache and cache region size as well as the given maximum read write block size.
    * Implementing test classes who's medium type does not support caching must return null.
    * 
    * For writable media, be sure to only return a copy of the original medium such that the original medium is not
    * modified by writing tests and all tests remain repeatable.
    * 
    * @param testMethodName
    *           the name of the current test method, can be used to create a copy of the original medium with the given
    *           name
    * @param maxCacheSize
    *           the maximum cache size in bytes
    * @param maxReadWriteBlockSize
    *           the maximum read write block size in bytes
    * @return a {@link Medium} containing {@link TestMedia#FIRST_TEST_FILE_CONTENT} as content with the given
    *         configuration parameters
    * 
    * @throws IOException
    *            In case of any errors creating the {@link Medium}
    */
   protected abstract T createFilledMedium(String testMethodName, long maxCacheSize, int maxReadWriteBlockSize)
      throws IOException;

   /**
    * Returns the current content of the given {@link Medium} as string representation. Implementations should implement
    * a strategy to get this content that is independent of the classes under test. They can distinguish the media by
    * medium name to identify them. This method is used to check the current medium content against the expected medium
    * content.
    * 
    * In test cases, you should only call this method either BEFORE opening the medium (to get the initial content) or
    * AFTER the closing of the medium (to get the changed content), otherwise you might run into exception because the
    * medium is still locked by the {@link MediumStore} and it cannot be accessed.
    * 
    * @return the current content of the filled {@link Medium}
    */
   protected abstract String getMediumContentAsString(T medium);

   /**
    * Creates a test class implementation specific {@link MediumAccessor} to use for testing.
    * 
    * @param mediumToUse
    *           The {@link Medium} to use for the {@link MediumStore}.
    * @return a {@link MediumAccessor} to use based on a given {@link Medium}.
    */
   protected abstract MediumAccessor<T> createMediumAccessor(T mediumToUse);

   /**
    * Creates a {@link MediumStore} to test based on a given {@link Medium}.
    * 
    * @param mediumToUse
    *           The {@link Medium} to use for the {@link MediumStore}.
    * @return a {@link MediumStore} to test based on a given {@link Medium}.
    */
   protected MediumStore createMediumStoreToTest(T mediumToUse) {
      Reject.ifNull(mediumToUse, "mediumToUse");

      mediumAccessorSpy = Mockito.spy(createMediumAccessor(mediumToUse));

      int maxCacheRegionSize = 0;

      if (mediumToUse.getMaxCacheSizeInBytes() > 0) {
         maxCacheRegionSize = mediumToUse.getMaxReadWriteBlockSizeInBytes();
      }

      mediumCacheSpy = Mockito
         .spy(new MediumCache(mediumToUse, mediumToUse.getMaxCacheSizeInBytes(), maxCacheRegionSize));
      mediumReferenceFactorySpy = Mockito.spy(new MediumOffsetFactory(mediumToUse));
      mediumChangeManagerSpy = Mockito.spy(new MediumChangeManager(mediumReferenceFactorySpy));

      return new StandardMediumStore<>(mediumAccessorSpy, mediumCacheSpy, mediumReferenceFactorySpy,
         mediumChangeManagerSpy);
   }

   /**
    * Creates a {@link MediumStore} based on an empty {@link Medium}.
    * 
    * @return a {@link MediumStore} based on an empty {@link Medium}
    */
   protected MediumStore createEmptyMediumStore() {
      try {
         currentMedium = createEmptyMedium(testName.getMethodName());

         return createMediumStoreToTest(currentMedium);
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }

   /**
    * Creates a default {@link MediumStore} based on a filled {@link Medium}.
    * 
    * @return a {@link MediumStore} based on a filled {@link Medium}
    */
   protected abstract MediumStore createDefaultFilledMediumStore();

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)} by comparing its result with the expected medium content.
    * 
    * @param getDataOffset
    *           The offset to use for the method call
    * @param getDataSize
    *           The size to use for the method call
    * @param currentMediumContent
    *           The current medium content used to get the expected data
    */
   protected void testGetData_returnsExpectedData(MediumOffset getDataOffset, int getDataSize,
      String currentMediumContent) {
      Reject.ifNull(currentMediumContent, "currentMediumContent");
      Reject.ifNull(getDataOffset, "getDataOffset");

      ByteBuffer returnedData = getDataNoEOMExpected(getDataOffset, getDataSize);

      assertByteBufferMatchesMediumRange(returnedData, getDataOffset, getDataSize, currentMediumContent);
   }

   /**
    * Checks whether the given {@link ByteBuffer} matches the medium content in the specified range
    * 
    * @param returnedData
    *           The {@link ByteBuffer} to check
    * @param rangeStartOffset
    *           The start offset of the compared range
    * @param rangeSize
    *           The size of the compared range
    * @param currentMediumContent
    *           The current content of the {@link Medium}
    */
   protected void assertByteBufferMatchesMediumRange(ByteBuffer returnedData, MediumOffset rangeStartOffset,
      int rangeSize, String currentMediumContent) {

      Reject.ifNull(currentMediumContent, "currentMediumContent");
      Reject.ifNull(rangeStartOffset, "rangeStartOffset");
      Reject.ifNull(returnedData, "returnedData");

      Assert.assertEquals(rangeSize, returnedData.remaining());
      byte[] byteBufferData = new byte[rangeSize];
      returnedData.asReadOnlyBuffer().get(byteBufferData);

      String returnedDataAsString = new String(byteBufferData, Charsets.CHARSET_UTF8);

      String expectedReturnedData = rangeStartOffset.getAbsoluteMediumOffset() > currentMediumContent.length() ? ""
         : currentMediumContent.substring((int) rangeStartOffset.getAbsoluteMediumOffset(),
            (int) (rangeStartOffset.getAbsoluteMediumOffset() + rangeSize));
      Assert.assertEquals(expectedReturnedData, returnedDataAsString);
   }

   /**
    * Tests {@link MediumStore#getData(MediumOffset, int)} to throw an end of medium exception when reaching it.
    * 
    * @param getDataSize
    *           The size to use for the method call
    * @param chunkSizeToUse
    *           The size of the read-write block chunks
    * @param currentMediumContent
    *           The current content of the medium
    * @param getDataStartOffset
    *           The offset to use for the method call
    */
   protected void testGetData_throwsEndOfMediumException(MediumOffset getDataOffset, int getDataSize,
      int chunkSizeToUse, String currentMediumContent) {

      Reject.ifNull(currentMediumContent, "currentMediumContent");
      Reject.ifNull(getDataOffset, "getDataOffset");

      try {
         mediumStoreUnderTest.getData(getDataOffset, getDataSize);
         Assert.fail(EndOfMediumException.class + " expected, but was not thrown");
      } catch (EndOfMediumException e) {
         long getDataStartOffset = getDataOffset.getAbsoluteMediumOffset();

         MediumOffset expectedReadOffset = at(currentMedium, getDataStartOffset
            + chunkSizeToUse * (int) ((currentMediumContent.length() - getDataStartOffset) / chunkSizeToUse));

         long expectedByteCountActuallyRead = (currentMediumContent.length() - getDataStartOffset) % chunkSizeToUse;

         // For cases where the cache offset is beyond the medium end
         if (expectedByteCountActuallyRead < 0) {
            expectedByteCountActuallyRead = 0;
         }

         Assert.assertEquals(expectedReadOffset, e.getReadStartReference());
         Assert.assertEquals(getDataSize < chunkSizeToUse ? getDataSize : chunkSizeToUse, e.getByteCountTriedToRead());
         Assert.assertEquals(expectedByteCountActuallyRead, e.getByteCountActuallyRead());
         assertByteBufferMatchesMediumRange(e.getBytesReadSoFar(), expectedReadOffset, e.getByteCountActuallyRead(),
            currentMediumContent);
      }
   }

   /**
    * Calls {@link MediumStore#getData(MediumOffset, int)} and expects no end of medium.
    * 
    * @param offset
    *           The offset to use
    * @param byteCount
    *           The number of bytes to cache
    */
   protected ByteBuffer getDataNoEOMExpected(MediumOffset offset, int byteCount) {
      try {
         return mediumStoreUnderTest.getData(offset, byteCount);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Calls {@link MediumStore#cache(MediumOffset, int)} and expects no end of medium.
    * 
    * @param offset
    *           The offset to use
    * @param byteCount
    *           The number of bytes to cache
    */
   protected void cacheNoEOMExpected(MediumOffset offset, int byteCount) {
      try {
         mediumStoreUnderTest.cache(offset, byteCount);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Verifies that there were exactly N calls to {@link MediumAccessor#read(ByteBuffer)} without
    * {@link EndOfMediumException}, no matter which parameters used.
    * 
    * @param N
    *           The number of expected calls
    */
   protected void verifyExactlyNReads(int N) {
      try {
         Mockito.verify(mediumAccessorSpy, Mockito.times(N)).read(Mockito.any());
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Verifies that there were exactly N calls to {@link MediumAccessor#write(ByteBuffer)} no matter which parameters
    * used.
    * 
    * @param N
    *           The number of expected calls
    */
   protected void verifyExactlyNWrites(int N) {
      Mockito.verify(mediumAccessorSpy, Mockito.times(N)).write(Mockito.any());
   }

   /**
    * Verifies that the medium cache is currently empty.
    */
   protected void assertCacheIsEmpty() {
      Assert.assertEquals(0, mediumCacheSpy.getAllCachedRegions().size());
      Assert.assertEquals(0, mediumCacheSpy.calculateCurrentCacheSizeInBytes());
   }

   /**
    * Tests {@link MediumStore#cache(MediumOffset, int)} for throwing an {@link EndOfMediumException} as expected.
    * 
    * @param cacheOffset
    *           The offset to start caching
    * @param cacheSize
    *           The size to cache
    * @param currentMediumContent
    *           The current content of the medium
    */
   protected void testCache_throwsEndOfMediumException(MediumOffset cacheOffset, int cacheSize,
      String currentMediumContent) {

      Reject.ifNull(currentMediumContent, "currentMediumContent");
      Reject.ifNull(cacheOffset, "cacheOffset");

      try {
         mediumStoreUnderTest.cache(cacheOffset, cacheSize);

         Assert.fail("Expected end of medium exception, but it did not occur!");
      }

      catch (EndOfMediumException e) {
         Assert.assertEquals(cacheOffset, e.getReadStartReference());
         Assert.assertEquals(cacheSize, e.getByteCountTriedToRead());
         long expectedByteCountActuallyRead = currentMediumContent.length() - cacheOffset.getAbsoluteMediumOffset();

         // For cases where the cache offset is beyond the medium end
         if (expectedByteCountActuallyRead < 0) {
            expectedByteCountActuallyRead = 0;
         }

         Assert.assertEquals(expectedByteCountActuallyRead, e.getByteCountActuallyRead());

         assertByteBufferMatchesMediumRange(e.getBytesReadSoFar(), cacheOffset, e.getByteCountActuallyRead(),
            currentMediumContent);
      }
   }

   /**
    * Creates a copy of the indicated file in a temporary folder with the given name parts.
    * 
    * @param pathToFile
    *           The path to the file to copy
    * @param mediumType
    *           The type of medium as string, concatenated to the target file name
    * @param testMethodName
    *           The name of the test method currently executed, concatenated to the target file name
    * @return a Path to a copied file
    * @throws IOException
    *            if anything bad happens during I/O
    */
   protected Path getCopiedFile(Path pathToFile, String mediumType, String testMethodName) throws IOException {
      Reject.ifNull(testMethodName, "testMethodName");
      Reject.ifNull(mediumType, "mediumType");
      Reject.ifNull(pathToFile, "pathToFile");

      Reject.ifFalse(Files.isRegularFile(pathToFile), "Files.isRegularFile(pathToFile)");

      Path copiedFile = Files.copy(pathToFile,
         TestMedia.TEST_FILE_TEMP_OUTPUT_DIRECTORY_PATH
            .resolve(getClass().getSimpleName() + "_" + mediumType + testMethodName + ".txt"),
         StandardCopyOption.REPLACE_EXISTING);
      return copiedFile;
   }

   protected MediumAction createManagedInsert(long offset, String insertText) {

      MediumOffset insertOffset = mediumStoreUnderTest.createMediumOffset(offset);

      return createInsertAction(insertOffset, insertText);
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
    * the specified array must have equal or bigger start offset than actions with offset j, if j > i. I.e. all actions
    * in the specified array must be sorted in offset order, and they are also executed this way. If you test cases
    * where the execution order differs from the offset order (which is allowed), this method MUST NOT be used. Use
    * {@link #testFlushWithComplexChangeSet_writesExpectedDataAndUndosActions(MediumAction...)} instead.
    * 
    * @param actionsInOffsetAndExecutionOrder
    *           The {@link MediumAction}s to apply in the given order; they are also used to derive the expectations for
    *           the written data in the end
    */
   protected void testFlushWithOrderedOffsets_writesExpectedDataAndUndosActions(
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

            byte[] actionByteArray = new byte[actionBytes.remaining()];
            actionBytes.mark();
            actionBytes.get(actionByteArray);
            actionBytes.reset();
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
   protected void testFlushWithComplexChangeSet_writesExpectedDataAndUndosActions(Path expectedFinalMediumContentFile,
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
}
