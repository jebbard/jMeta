/**
 *
 * {@link MediumOffsetFactoryTest}.java
 *
 * @author Jens
 *
 * @date 20.05.2016
 *
 */
package com.github.jmeta.library.media.impl.offset;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.MediumAccessType;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediumOffsetFactoryTest} tests the {@link MediumOffsetFactory} class.
 */
public class MediumOffsetFactoryTest {

   private static final long END_OFFSET = 1000L;
   private static final ByteBuffer DEFAULT_BYTES = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4 });
   private static final FileMedium MEDIUM = new FileMedium(TestMedia.FIRST_TEST_FILE_PATH, MediumAccessType.READ_ONLY);

   private final static long[] THE_REFERENCE_OFFSETS = new long[] { 0L, 2L, 3L, 0L, 2L, 20L, 50L, 50L, 500L,
      MediumOffsetFactoryTest.END_OFFSET, };

   /**
    * Tests {@link MediumOffsetFactory#clear()} and {@link MediumOffsetFactory#getAllOffsets()}.
    */
   @Test
   public void clear_forEmptyFactory_getAllOffsets_returnsEmptyList() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      testling.clear();

      assertMediumOffsetFactoryIsEmpty(testling);
   }

   /**
    * Tests {@link MediumOffsetFactory#clear()} and {@link MediumOffsetFactory#getAllOffsets()}.
    */
   @Test
   public void clear_getAllOffsets_returnsEmptyList() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      createAndAddDefaultOffsets(testling);

      testling.clear();

      assertMediumOffsetFactoryIsEmpty(testling);
   }

   /**
    * Tests {@link MediumOffsetFactory#getAllOffsets()} and {@link MediumOffset#advance(long)}.
    */
   @Test
   public void getAllOffsets_advancedReference_returnsAdvancedReference() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);
      List<MediumOffset> expectedReferences = createAndAddDefaultOffsets(testling);

      MediumOffset advancedRef = expectedReferences.get(0).advance(10);

      Assert.assertTrue(testling.getAllOffsets().contains(advancedRef));
      expectedReferences.add(advancedRef);

      Assert.assertEquals(expectedReferences, testling.getAllOffsets());
   }

   /**
    * Tests {@link MediumOffsetFactory#getAllOffsets()}.
    */
   @Test
   public void getAllOffsets_afterCreatingReferences_returnsAllReferences() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);
      List<MediumOffset> expectedReferences = createAndAddDefaultOffsets(testling);

      Assert.assertEquals(expectedReferences, testling.getAllOffsets());
   }

   /**
    * Tests {@link MediumOffsetFactory#getAllOffsets()}.
    */
   @Test
   public void getAllOffsets_noReferencesCreatedYet_returnsEmptyList() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      Assert.assertTrue(testling.getAllOffsets().isEmpty());
   }

   /**
    * Tests {@link MediumOffsetFactory#getAllOffsetsBehindOrEqual(MediumOffset)}.
    */
   @Test
   public void getAllOffsetsBehindOrEqual_afterCreatingReferences_returnsOnlyReferencesBehindOrEqual() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      List<MediumOffset> allReferences = createAndAddDefaultOffsets(testling);

      assertGetAllOffsetsBehindOrEqualReturnsExpectedOffsets(testling, 0, allReferences,
         new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });

      assertGetAllOffsetsBehindOrEqualReturnsExpectedOffsets(testling, 2, allReferences,
         new int[] { 1, 2, 4, 5, 6, 7, 8, 9 });

      assertGetAllOffsetsBehindOrEqualReturnsExpectedOffsets(testling, 501, allReferences, new int[] { 9 });

      assertGetAllOffsetsBehindOrEqualReturnsExpectedOffsets(testling, 3, allReferences,
         new int[] { 2, 5, 6, 7, 8, 9 });

      assertGetAllOffsetsBehindOrEqualReturnsExpectedOffsets(testling, 1000, allReferences, new int[] { 9 });
   }

   /**
    * Tests {@link MediumOffsetFactory#getAllOffsetsBehindOrEqual(MediumOffset)}.
    */
   @Test
   public void getAllOffsetsBehindOrEqual_forOffsetAfterLastReference_returnsEmptyList() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      List<MediumOffset> allReferences = createAndAddDefaultOffsets(testling);

      assertGetAllOffsetsBehindOrEqualReturnsExpectedOffsets(testling, 1001L, allReferences, new int[] {});

      assertGetAllOffsetsBehindOrEqualReturnsExpectedOffsets(testling, 1000002L, allReferences, new int[] {});
   }

   /**
    * Tests {@link MediumOffsetFactory#getAllOffsetsBehindOrEqual(MediumOffset)}.
    */
   @Test
   public void getAllOffsetsBehindOrEqual_noReferencesCreatedYet_returnsEmptyList() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      Assert.assertTrue(
         testling.getAllOffsetsBehindOrEqual(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 100)).isEmpty());
   }

   /**
    * Tests {@link MediumOffsetFactory#getAllOffsetsInRegion(MediumRegion)}.
    */
   @Test
   public void getAllOffsetsInRegion_afterCreatingReferences_returnsOnlyReferencesInRegion() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      List<MediumOffset> allReferences = createAndAddDefaultOffsets(testling);

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 0, 10, allReferences, new int[] { 0, 1, 2, 3, 4 });

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 0, 1, allReferences, new int[] { 0, 3 });

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 2, 3, allReferences, new int[] { 1, 2, 4 });

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 501, 1000, allReferences, new int[] { 9 });

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 3, 497, allReferences, new int[] { 2, 5, 6, 7 });

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 0, 111113, allReferences,
         new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 1000, 1209123912, allReferences, new int[] { 9 });
   }

   /**
    * Tests {@link MediumOffsetFactory#getAllOffsetsInRegion(MediumRegion)}.
    */
   @Test
   public void getAllOffsetsInRegion_forGapsBetweenCreatedReferences_returnsEmptyList() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      List<MediumOffset> allReferences = createAndAddDefaultOffsets(testling);

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 4, 15, allReferences, new int[] {});

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 21, 28, allReferences, new int[] {});

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 501, 499, allReferences, new int[] {});

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 1001, 1, allReferences, new int[] {});

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 1001, 2, allReferences, new int[] {});

      assertGetAllOffsetsInRegionReturnsExpectedOffsets(testling, 6888, 19999, allReferences, new int[] {});
   }

   /**
    * Tests {@link MediumOffsetFactory#getAllOffsetsInRegion(MediumRegion)}.
    */
   @Test
   public void getAllOffsetsInRegion_noReferencesCreatedYet_returnsEmptyList() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      MediumRegion mediumRegion = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 0), 10);
      Assert.assertTrue(testling.getAllOffsetsInRegion(mediumRegion).isEmpty());
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_forEmptyFactory_changesNothing() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      MediumRegion mediumRegion = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 0),
         MediumOffsetFactoryTest.DEFAULT_BYTES.remaining());

      testling.updateOffsets(
         new MediumAction(MediumActionType.INSERT, mediumRegion, 0, MediumOffsetFactoryTest.DEFAULT_BYTES));

      assertMediumOffsetFactoryIsEmpty(testling);
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_insertAtExistingOffset_increasesOffsetByRegionSize() {

      ByteBuffer bytesRegion1 = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
      ByteBuffer bytesRegion2 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

      int regionSize1 = bytesRegion1.remaining();
      int regionSize2 = bytesRegion2.remaining();

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 20L),
         regionSize1);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 50L),
         regionSize2);

      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion1,
         MediumActionType.INSERT, bytesRegion1,
         new long[] { 0, 0, 0, 0, 0, regionSize1, regionSize1, regionSize1, regionSize1, regionSize1 });
      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion2,
         MediumActionType.INSERT, bytesRegion2,
         new long[] { 0, 0, 0, 0, 0, 0, regionSize2, regionSize2, regionSize2, regionSize2 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_insertBeforeExistingOffset_increasesOffsetByRegionSize() {

      ByteBuffer bytesRegion1 = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
      ByteBuffer bytesRegion2 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

      int regionSize1 = bytesRegion1.remaining();
      int regionSize2 = bytesRegion2.remaining();

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 19L),
         regionSize1);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 47L),
         regionSize2);

      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion1,
         MediumActionType.INSERT, bytesRegion1,
         new long[] { 0, 0, 0, 0, 0, regionSize1, regionSize1, regionSize1, regionSize1, regionSize1 });
      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion2,
         MediumActionType.INSERT, bytesRegion2,
         new long[] { 0, 0, 0, 0, 0, 0, regionSize2, regionSize2, regionSize2, regionSize2 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_insertBehindEveryExistingOffset_changesNoOffsets() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      MediumRegion mediumRegion = new MediumRegion(
         new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, MediumOffsetFactoryTest.END_OFFSET + 1),
         MediumOffsetFactoryTest.DEFAULT_BYTES.remaining());

      assertUpdateOffsetsUpdatesOffsetsCorrectly(testling, mediumRegion, MediumActionType.INSERT,
         MediumOffsetFactoryTest.DEFAULT_BYTES, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_insertingReplaceAtExistingOffset_doesNotChangeReplacementOffsetAndIncreasesOffsetsBehindReplacedBytes() {

      ByteBuffer bytesRegion1 = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
      ByteBuffer bytesRegion2 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 20L), 10);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 50L), 2);

      int delta1 = bytesRegion1.remaining() - 10;
      int delta2 = bytesRegion2.remaining() - 2;

      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion1,
         MediumActionType.REPLACE, bytesRegion1, new long[] { 0, 0, 0, 0, 0, 0, delta1, delta1, delta1, delta1 });
      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion2,
         MediumActionType.REPLACE, bytesRegion2, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, delta2, delta2 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_overwritingReplace_changesNoOffsetsEvenIfOverlapping() {
      ByteBuffer bytesRegion1 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
      ByteBuffer bytesRegion2 = ByteBuffer.wrap(new byte[] { 1, 1 });

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 16L),
         bytesRegion1.remaining());
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 996L),
         bytesRegion2.remaining());

      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion1,
         MediumActionType.REPLACE, bytesRegion1, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion2,
         MediumActionType.REPLACE, bytesRegion2, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_removeAtEndOffset_changesNoOffsets() {

      MediumRegion mediumRegion1 = new MediumRegion(
         new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, MediumOffsetFactoryTest.END_OFFSET), 20000);

      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion1,
         MediumActionType.REMOVE, null, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_removeBeforeExistingOffsetNotOverlapping_decreasesOffsetsByRegionSize() {

      int regionSize1 = 5;
      int regionSize2 = 5;

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 15L),
         regionSize1);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 14L),
         regionSize2);

      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion1,
         MediumActionType.REMOVE, null,
         new long[] { 0, 0, 0, 0, 0, -regionSize1, -regionSize1, -regionSize1, -regionSize1, -regionSize1 });
      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion2,
         MediumActionType.REMOVE, null,
         new long[] { 0, 0, 0, 0, 0, -regionSize2, -regionSize2, -regionSize2, -regionSize2, -regionSize2 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_removeBeforeExistingOffsetOverlappingIt_overlappedOffsetFallsBackToRemoveOffset() {

      int regionSize1 = 50;
      int regionSize2 = 5;

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 16L),
         regionSize1);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 999L),
         regionSize2);

      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion1,
         MediumActionType.REMOVE, null, new long[] { 0, 0, 0, 0, 0, -4, -34, -34, -regionSize1, -regionSize1 });
      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion2,
         MediumActionType.REMOVE, null, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_removeBehindEveryExistingOffset_changesNoOffsets() {

      MediumOffsetFactory testling = new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM);

      MediumRegion mediumRegion = new MediumRegion(
         new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, MediumOffsetFactoryTest.END_OFFSET + 1), 50);

      assertUpdateOffsetsUpdatesOffsetsCorrectly(testling, mediumRegion, MediumActionType.REMOVE, null,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_removingReplaceAtExistingOffset_doesNotChangeReplacementOffsetAndDecreasesOffsetsBehindReplacedBytes() {

      ByteBuffer bytesRegion1 = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
      ByteBuffer bytesRegion2 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 20L), 100);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 50L), 200);

      int delta1 = 100 - bytesRegion1.remaining();
      int delta2 = 200 - bytesRegion2.remaining();

      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion1,
         MediumActionType.REPLACE, bytesRegion1, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, -delta1, -delta1 });
      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion2,
         MediumActionType.REPLACE, bytesRegion2, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, -delta2, -delta2 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_removingReplaceOverlappingExistingOffset_doesNotChangeExistingOffsetsInReplacedRegionAndDecreasesOffsetsBehind() {
      ByteBuffer bytesRegion1 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
      ByteBuffer bytesRegion2 = ByteBuffer.wrap(new byte[] { 1, 1 });

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 16L), 50);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 996L), 5);

      int delta1 = 50 - bytesRegion1.remaining();

      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion1,
         MediumActionType.REPLACE, bytesRegion1, new long[] { 0, 0, 0, 0, 0, 0, -4, -4, -delta1, -delta1 });
      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion2,
         MediumActionType.REPLACE, bytesRegion2, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, -2 });
   }

   /**
    * Tests {@link MediumOffsetFactory#updateOffsets(MediumAction)}.
    */
   @Test
   public void updateOffsets_replaceBehindEveryExistingOffset_changesNoOffsets() {

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 1001L),
         MediumOffsetFactoryTest.DEFAULT_BYTES.remaining());
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 1001L),
         987);
      MediumRegion mediumRegion3 = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 1001L), 1);

      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion1,
         MediumActionType.REPLACE, MediumOffsetFactoryTest.DEFAULT_BYTES, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion2,
         MediumActionType.REPLACE, MediumOffsetFactoryTest.DEFAULT_BYTES, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
      assertUpdateOffsetsUpdatesOffsetsCorrectly(new MediumOffsetFactory(MediumOffsetFactoryTest.MEDIUM), mediumRegion3,
         MediumActionType.REPLACE, MediumOffsetFactoryTest.DEFAULT_BYTES, new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   }

   /**
    * Checks {@link MediumOffsetFactory#getAllOffsetsBehindOrEqual(MediumOffset)}.
    * 
    * @param testling
    *           The {@link MediumOffsetFactory} under test
    * @param startOffset
    *           The start offset to pass.
    * @param allReferences
    *           All previously created {@link MediumOffset}s (return value of
    *           {@link #createAndAddDefaultOffsets(MediumOffsetFactory)}).
    * @param expectedReferenceIndices
    *           The indices of all {@link MediumOffset}s (relative to {@link #THE_REFERENCE_OFFSETS}) that are expected
    *           as a result of a call to {@link MediumOffsetFactory#getAllOffsetsInRegion(MediumRegion)}.
    */
   private void assertGetAllOffsetsBehindOrEqualReturnsExpectedOffsets(MediumOffsetFactory testling, long startOffset,
      List<MediumOffset> allReferences, int[] expectedReferenceIndices) {

      MediumOffset ref = new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, startOffset);

      List<MediumOffset> expectedReferencesBehindOrEqual = new ArrayList<>();

      for (int i = 0; i < expectedReferenceIndices.length; i++) {
         expectedReferencesBehindOrEqual.add(allReferences.get(expectedReferenceIndices[i]));
      }

      Assert.assertEquals(expectedReferencesBehindOrEqual, testling.getAllOffsetsBehindOrEqual(ref));
   }

   /**
    * Checks {@link MediumOffsetFactory#getAllOffsetsInRegion(MediumRegion)}.
    * 
    * @param testling
    *           The {@link MediumOffsetFactory} under test
    * @param regionStartOffset
    *           The start offset for the {@link MediumRegion} to pass.
    * @param regionSize
    *           The size of the {@link MediumRegion} to pass.
    * @param allReferences
    *           All previously created {@link MediumOffset}s (return value of
    *           {@link #createAndAddDefaultOffsets(MediumOffsetFactory)}).
    * @param expectedReferenceIndices
    *           The indices of all {@link MediumOffset}s (relative to {@link #THE_REFERENCE_OFFSETS}) that are expected
    *           as a result of a call to {@link MediumOffsetFactory#getAllOffsetsInRegion(MediumRegion)}.
    */
   private void assertGetAllOffsetsInRegionReturnsExpectedOffsets(MediumOffsetFactory testling, long regionStartOffset,
      int regionSize, List<MediumOffset> allReferences, int[] expectedReferenceIndices) {

      MediumRegion mediumRegion = new MediumRegion(
         new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, regionStartOffset), regionSize);

      List<MediumOffset> expectedReferencesInRegion = new ArrayList<>();

      for (int i = 0; i < expectedReferenceIndices.length; i++) {
         expectedReferencesInRegion.add(allReferences.get(expectedReferenceIndices[i]));
      }

      Assert.assertEquals(expectedReferencesInRegion, testling.getAllOffsetsInRegion(mediumRegion));
   }

   /**
    * Checks that all getter methods of {@link MediumOffsetFactory} return an empty list.
    * 
    * @param testling
    *           The {@link MediumOffsetFactory} under test
    */
   private void assertMediumOffsetFactoryIsEmpty(MediumOffsetFactory testling) {

      MediumRegion mediumRegion = new MediumRegion(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 0), 10);

      Assert.assertTrue(testling.getAllOffsets().isEmpty());
      Assert.assertTrue(testling.getAllOffsetsInRegion(mediumRegion).isEmpty());
      Assert.assertTrue(
         testling.getAllOffsetsBehindOrEqual(new StandardMediumOffset(MediumOffsetFactoryTest.MEDIUM, 100)).isEmpty());
   }

   /**
    * Checks the correct updating of references.
    * 
    * @param testling
    *           The {@link MediumOffsetFactory} under test
    * @param region
    *           The {@link MediumRegion} that is changed
    * @param type
    *           The type of change, either {@link MediumActionType#INSERT} or {@link MediumActionType#REMOVE}
    * @param actionBytes
    *           The bytes associated with the {@link MediumAction}.
    * @param expectedDeltasPerOffset
    *           The expected delta offsets for each of the offsets given by {@link #THE_REFERENCE_OFFSETS}.
    */
   private void assertUpdateOffsetsUpdatesOffsetsCorrectly(MediumOffsetFactory testling, MediumRegion region,
      MediumActionType type, ByteBuffer actionBytes, long[] expectedDeltasPerOffset) {

      Reject.ifTrue(expectedDeltasPerOffset.length != MediumOffsetFactoryTest.THE_REFERENCE_OFFSETS.length,
         "The deltas array must have the same size as the reference offset array");

      List<MediumOffset> defaultReferences = createAndAddDefaultOffsets(testling);

      testling.updateOffsets(new MediumAction(type, region, 0, actionBytes));

      List<MediumOffset> referencesAfterUpdate = testling.getAllOffsets();

      // Due to the nature of the factory, all initial references must have been
      // updated
      Assert.assertEquals(referencesAfterUpdate, defaultReferences);

      for (int i = 0; i < MediumOffsetFactoryTest.THE_REFERENCE_OFFSETS.length; i++) {
         Assert.assertEquals(MediumOffsetFactoryTest.THE_REFERENCE_OFFSETS[i] + expectedDeltasPerOffset[i],
            referencesAfterUpdate.get(i).getAbsoluteMediumOffset());
      }
   }

   /**
    * Creates and adds all the {@link MediumOffset}s given by {@link #THE_REFERENCE_OFFSETS}.
    * 
    * @param testling
    *           The {@link MediumOffsetFactory} under test.
    * @return All {@link MediumOffset}s.
    */
   private List<MediumOffset> createAndAddDefaultOffsets(MediumOffsetFactory testling) {

      List<MediumOffset> allReferences = new ArrayList<>();

      for (int i = 0; i < MediumOffsetFactoryTest.THE_REFERENCE_OFFSETS.length; i++) {
         MediumOffset reference = testling.createMediumOffset(MediumOffsetFactoryTest.THE_REFERENCE_OFFSETS[i]);
         allReferences.add(reference);
      }

      return allReferences;
   }

}
