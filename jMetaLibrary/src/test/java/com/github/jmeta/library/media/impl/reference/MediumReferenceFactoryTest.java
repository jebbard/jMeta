/**
 *
 * MediumReferenceFactoryTest.java
 *
 * @author Jens
 *
 * @date 20.05.2016
 *
 */
package com.github.jmeta.library.media.impl.reference;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.MediaTestCaseConstants;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediumReferenceFactoryTest} tests the {@link MediumReferenceFactory} class.
 */
public class MediumReferenceFactoryTest {

   private static final ByteBuffer DEFAULT_BYTES = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4 });
   private static final FileMedium MEDIUM = new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, true);

   private final static long[] THE_REFERENCE_OFFSETS = new long[] { 0L, 2L, 3L, 0L, 2L, 20L, 50L, 50L, 500L, 1000L, };

   /**
    * Tests {@link MediumReferenceFactory#getAllReferences()}.
    */
   @Test
   public void getAllReferences_noReferencesCreatedYet_returnsEmptyList() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      Assert.assertTrue(testling.getAllReferences().isEmpty());
   }

   /**
    * Tests {@link MediumReferenceFactory#getAllReferences()}.
    */
   @Test
   public void getAllReferences_afterCreatingReferences_returnsAllReferences() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);
      List<MediumReference> expectedReferences = createAndAddDefaultReferences(testling);

      Assert.assertEquals(expectedReferences, testling.getAllReferences());
   }

   /**
    * Tests {@link MediumReferenceFactory#getAllReferencesInRegion(MediumRegion)}.
    */
   @Test
   public void getAllReferencesInRegion_noReferencesCreatedYet_returnsEmptyList() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      MediumRegion mediumRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      Assert.assertTrue(testling.getAllReferencesInRegion(mediumRegion).isEmpty());
   }

   /**
    * Tests {@link MediumReferenceFactory#getAllReferencesInRegion(MediumRegion)}.
    */
   @Test
   public void getAllReferencesInRegion_afterCreatingReferences_returnsOnlyReferencesInRegion() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      List<MediumReference> allReferences = createAndAddDefaultReferences(testling);

      checkGetAllReferencesInRegion(testling, 0, 10, allReferences, new int[] { 0, 1, 2, 3, 4 });

      checkGetAllReferencesInRegion(testling, 0, 1, allReferences, new int[] { 0, 3 });

      checkGetAllReferencesInRegion(testling, 2, 3, allReferences, new int[] { 1, 2, 4 });

      checkGetAllReferencesInRegion(testling, 501, 1000, allReferences, new int[] { 9 });

      checkGetAllReferencesInRegion(testling, 3, 497, allReferences, new int[] { 2, 5, 6, 7 });

      checkGetAllReferencesInRegion(testling, 0, 111113, allReferences, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });

      checkGetAllReferencesInRegion(testling, 1000, 1209123912, allReferences, new int[] { 9 });
   }

   /**
    * Tests {@link MediumReferenceFactory#getAllReferencesInRegion(MediumRegion)}.
    */
   @Test
   public void getAllReferencesInRegion_forGapsBetweenCreatedReferences_returnsEmptyList() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      List<MediumReference> allReferences = createAndAddDefaultReferences(testling);

      checkGetAllReferencesInRegion(testling, 4, 15, allReferences, new int[] {});

      checkGetAllReferencesInRegion(testling, 21, 28, allReferences, new int[] {});

      checkGetAllReferencesInRegion(testling, 501, 499, allReferences, new int[] {});

      checkGetAllReferencesInRegion(testling, 1001, 1, allReferences, new int[] {});

      checkGetAllReferencesInRegion(testling, 1001, 2, allReferences, new int[] {});

      checkGetAllReferencesInRegion(testling, 6888, 19999, allReferences, new int[] {});
   }

   /**
    * Tests {@link MediumReferenceFactory#getAllReferencesBehindOrEqual(MediumReference)}.
    */
   @Test
   public void getAllReferencesBehindOrEqual_noReferencesCreatedYet_returnsEmptyList() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      Assert.assertTrue(testling.getAllReferencesBehindOrEqual(new StandardMediumReference(MEDIUM, 100)).isEmpty());
   }

   /**
    * Tests {@link MediumReferenceFactory#getAllReferencesBehindOrEqual(MediumReference)}.
    */
   @Test
   public void getAllReferencesBehindOrEqual_afterCreatingReferences_returnsOnlyReferencesBehindOrEqual() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      List<MediumReference> allReferences = createAndAddDefaultReferences(testling);

      checkGetAllReferencesBehindOrEqual(testling, 0, allReferences, new int[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9 });

      checkGetAllReferencesBehindOrEqual(testling, 2, allReferences, new int[] { 1, 2, 4, 5, 6, 7, 8, 9 });

      checkGetAllReferencesBehindOrEqual(testling, 501, allReferences, new int[] { 9 });

      checkGetAllReferencesBehindOrEqual(testling, 3, allReferences, new int[] { 2, 5, 6, 7, 8, 9 });

      checkGetAllReferencesBehindOrEqual(testling, 1000, allReferences, new int[] { 9 });
   }

   /**
    * Tests {@link MediumReferenceFactory#getAllReferencesBehindOrEqual(MediumReference)}.
    */
   @Test
   public void getAllReferencesBehindOrEqual_forOffsetAfterLastReference_returnsEmptyList() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      List<MediumReference> allReferences = createAndAddDefaultReferences(testling);

      checkGetAllReferencesBehindOrEqual(testling, 1001L, allReferences, new int[] {});

      checkGetAllReferencesBehindOrEqual(testling, 1000002L, allReferences, new int[] {});
   }

   /**
    * Tests {@link MediumReferenceFactory#clear()} and {@link MediumReferenceFactory#getAllReferences()}.
    */
   @Test
   public void clear_forEmptyFactory_getAllReferences_returnsEmptyList() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      testling.clear();

      checkAllEmpty(testling);
   }

   /**
    * Tests {@link MediumReferenceFactory#clear()} and {@link MediumReferenceFactory#getAllReferences()}.
    */
   @Test
   public void clear_getAllReferences_returnsEmptyList() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      createAndAddDefaultReferences(testling);

      testling.clear();

      checkAllEmpty(testling);
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_forEmptyFactory_changesNothing() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      MediumRegion mediumRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining());

      testling.updateReferences(new MediumAction(MediumActionType.INSERT, mediumRegion, 0, DEFAULT_BYTES));

      checkAllEmpty(testling);
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_insertAfterEveryReference_changesNothing() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      MediumRegion mediumRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 1001L),
         DEFAULT_BYTES.remaining());

      checkUpdateReferences(testling, mediumRegion, MediumActionType.INSERT, DEFAULT_BYTES,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_removeAfterEveryReference_changesNothing() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);

      MediumRegion mediumRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 1001L), 50);

      checkUpdateReferences(testling, mediumRegion, MediumActionType.REMOVE, null,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_replaceAfterEveryReference_changesNothing() {

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumReference(MEDIUM, 1001L),
         DEFAULT_BYTES.remaining());
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumReference(MEDIUM, 1001L), 987);
      MediumRegion mediumRegion3 = new MediumRegion(new StandardMediumReference(MEDIUM, 1001L), 1);

      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion1, MediumActionType.REPLACE, DEFAULT_BYTES,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion2, MediumActionType.REPLACE, DEFAULT_BYTES,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion3, MediumActionType.REPLACE, DEFAULT_BYTES,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_insertBefore_increasesOffsetsByRegionSize() {

      ByteBuffer bytesRegion1 = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
      ByteBuffer bytesRegion2 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

      int regionSize1 = bytesRegion1.remaining();
      int regionSize2 = bytesRegion2.remaining();

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumReference(MEDIUM, 20L), regionSize1);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumReference(MEDIUM, 50L), regionSize2);

      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion1, MediumActionType.INSERT, bytesRegion1,
         new long[] { 0, 0, 0, 0, 0, regionSize1, regionSize1, regionSize1, regionSize1, regionSize1 });
      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion2, MediumActionType.INSERT, bytesRegion2,
         new long[] { 0, 0, 0, 0, 0, 0, regionSize2, regionSize2, regionSize2, regionSize2 });
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_replaceBeforeWithMoreReplacementBytes_increasesOffsetsByReplacementDelta() {

      ByteBuffer bytesRegion1 = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
      ByteBuffer bytesRegion2 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumReference(MEDIUM, 20L), 10);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumReference(MEDIUM, 50L), 2);

      int delta1 = bytesRegion1.remaining() - 10;
      int delta2 = bytesRegion2.remaining() - 2;

      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion1, MediumActionType.REPLACE, bytesRegion1,
         new long[] { 0, 0, 0, 0, 0, 0, delta1, delta1, delta1, delta1 });
      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion2, MediumActionType.REPLACE, bytesRegion2,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, delta2, delta2 });
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_removeBeforeWithoutOverlap_decreasesOffsetsByRegionSize() {

      int regionSize1 = 5;
      int regionSize2 = 5;

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumReference(MEDIUM, 15L), regionSize1);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumReference(MEDIUM, 14L), regionSize2);

      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion1, MediumActionType.REMOVE, null,
         new long[] { 0, 0, 0, 0, 0, -regionSize1, -regionSize1, -regionSize1, -regionSize1, -regionSize1 });
      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion2, MediumActionType.REMOVE, null,
         new long[] { 0, 0, 0, 0, 0, -regionSize2, -regionSize2, -regionSize2, -regionSize2, -regionSize2 });
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_replaceBeforeWithLessReplacementBytesWithoutOverlap_decreasesOffsetsByReplacementDelta() {

      ByteBuffer bytesRegion1 = ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
         0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1 });
      ByteBuffer bytesRegion2 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumReference(MEDIUM, 20L), 100);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumReference(MEDIUM, 50L), 200);

      int delta1 = 100 - bytesRegion1.remaining();
      int delta2 = 200 - bytesRegion2.remaining();

      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion1, MediumActionType.REPLACE, bytesRegion1,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, -delta1, -delta1 });
      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion2, MediumActionType.REPLACE, bytesRegion2,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, -delta2, -delta2 });
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_removeBeforeWithOverlap_decreasesOffsetsCorrectly() {

      int regionSize1 = 50;
      int regionSize2 = 5;

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumReference(MEDIUM, 16L), regionSize1);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumReference(MEDIUM, 999L), regionSize2);

      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion1, MediumActionType.REMOVE, null,
         new long[] { 0, 0, 0, 0, 0, -4, -34, -34, -regionSize1, -regionSize1 });
      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion2, MediumActionType.REMOVE, null,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, -1 });
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_replaceBeforeWithLessReplacementBytesWithOverlap_decreasesOffsetsCorrectly() {
      ByteBuffer bytesRegion1 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
      ByteBuffer bytesRegion2 = ByteBuffer.wrap(new byte[] { 1, 1 });

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumReference(MEDIUM, 16L), 50);
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumReference(MEDIUM, 996L), 5);

      int delta1 = 50 - bytesRegion1.remaining();

      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion1, MediumActionType.REPLACE, bytesRegion1,
         new long[] { 0, 0, 0, 0, 0, 0, -4, -4, -delta1, -delta1 });
      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion2, MediumActionType.REPLACE, bytesRegion2,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, -2 });
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_replaceBeforeWithEqualReplacementBytes_changesNothing() {
      ByteBuffer bytesRegion1 = ByteBuffer
         .wrap(new byte[] { 1, 2, 3, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
      ByteBuffer bytesRegion2 = ByteBuffer.wrap(new byte[] { 1, 1 });

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumReference(MEDIUM, 16L), bytesRegion1.remaining());
      MediumRegion mediumRegion2 = new MediumRegion(new StandardMediumReference(MEDIUM, 996L),
         bytesRegion2.remaining());

      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion1, MediumActionType.REPLACE, bytesRegion1,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion2, MediumActionType.REPLACE, bytesRegion2,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   }

   /**
    * Tests {@link MediumReferenceFactory#updateReferences(MediumAction)}.
    */
   @Test
   public void updateReferences_removeAtStartOffset_changesNothing() {

      MediumRegion mediumRegion1 = new MediumRegion(new StandardMediumReference(MEDIUM, 1000L), 20000);

      checkUpdateReferences(new MediumReferenceFactory(MEDIUM), mediumRegion1, MediumActionType.REMOVE, null,
         new long[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 });
   }

   /**
    * Tests {@link MediumReferenceFactory#getAllReferences()} and {@link MediumReference#advance(long)}.
    */
   @Test
   public void getAllReferences_advancedReference_returnsAdvancedReference() {

      MediumReferenceFactory testling = new MediumReferenceFactory(MEDIUM);
      List<MediumReference> expectedReferences = createAndAddDefaultReferences(testling);

      MediumReference advancedRef = expectedReferences.get(0).advance(10);

      Assert.assertTrue(testling.getAllReferences().contains(advancedRef));
      expectedReferences.add(advancedRef);

      Assert.assertEquals(expectedReferences, testling.getAllReferences());
   }

   /**
    * Checks the correct updating of references.
    * 
    * @param testling
    *           The {@link MediumReferenceFactory} under test
    * @param region
    *           The {@link MediumRegion} that is changed
    * @param type
    *           The type of change, either {@link MediumActionType#INSERT} or {@link MediumActionType#REMOVE}
    * @param actionBytes
    *           The bytes associated with the {@link MediumAction}.
    * @param expectedDeltasPerReference
    *           The expected delta offsets for each of the references given by {@link #THE_REFERENCE_OFFSETS}.
    */
   private void checkUpdateReferences(MediumReferenceFactory testling, MediumRegion region, MediumActionType type,
      ByteBuffer actionBytes, long[] expectedDeltasPerReference) {

      Reject.ifTrue(expectedDeltasPerReference.length != THE_REFERENCE_OFFSETS.length,
         "The deltas array must have the same size as the reference offset array");

      List<MediumReference> defaultReferences = createAndAddDefaultReferences(testling);

      testling.updateReferences(new MediumAction(type, region, 0, actionBytes));

      List<MediumReference> referencesAfterUpdate = testling.getAllReferences();

      // Due to the nature of the factory, all initial references must have been updated
      Assert.assertEquals(referencesAfterUpdate, defaultReferences);

      for (int i = 0; i < THE_REFERENCE_OFFSETS.length; i++) {
         Assert.assertEquals(THE_REFERENCE_OFFSETS[i] + expectedDeltasPerReference[i],
            referencesAfterUpdate.get(i).getAbsoluteMediumOffset());
      }
   }

   /**
    * Checks that all getter methods of {@link MediumReferenceFactory} return an empty list.
    * 
    * @param testling
    *           The {@link MediumReferenceFactory} under test
    */
   private void checkAllEmpty(MediumReferenceFactory testling) {

      MediumRegion mediumRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);

      Assert.assertTrue(testling.getAllReferences().isEmpty());
      Assert.assertTrue(testling.getAllReferencesInRegion(mediumRegion).isEmpty());
      Assert.assertTrue(testling.getAllReferencesBehindOrEqual(new StandardMediumReference(MEDIUM, 100)).isEmpty());
   }

   /**
    * Creates and adds all the {@link MediumReference}s given by {@link #THE_REFERENCE_OFFSETS}.
    * 
    * @param testling
    *           The {@link MediumReferenceFactory} under test.
    * @return All {@link MediumReference}s.
    */
   private List<MediumReference> createAndAddDefaultReferences(MediumReferenceFactory testling) {

      List<MediumReference> allReferences = new ArrayList<>();

      for (int i = 0; i < THE_REFERENCE_OFFSETS.length; i++) {
         MediumReference reference = testling.createMediumReference(THE_REFERENCE_OFFSETS[i]);
         allReferences.add(reference);
      }

      return allReferences;
   }

   /**
    * Checks {@link MediumReferenceFactory#getAllReferencesInRegion(MediumRegion)}.
    * 
    * @param testling
    *           The {@link MediumReferenceFactory} under test
    * @param regionStartOffset
    *           The start offset for the {@link MediumRegion} to pass.
    * @param regionSize
    *           The size of the {@link MediumRegion} to pass.
    * @param allReferences
    *           All previously created {@link MediumReference}s (return value of
    *           {@link #createAndAddDefaultReferences(MediumReferenceFactory)}).
    * @param expectedReferenceIndices
    *           The indices of all {@link MediumReference}s (relative to {@link #THE_REFERENCE_OFFSETS}) that are
    *           expected as a result of a call to {@link MediumReferenceFactory#getAllReferencesInRegion(MediumRegion)}.
    */
   private void checkGetAllReferencesInRegion(MediumReferenceFactory testling, long regionStartOffset, int regionSize,
      List<MediumReference> allReferences, int[] expectedReferenceIndices) {

      MediumRegion mediumRegion = new MediumRegion(new StandardMediumReference(MEDIUM, regionStartOffset), regionSize);

      List<MediumReference> expectedReferencesInRegion = new ArrayList<>();

      for (int i = 0; i < expectedReferenceIndices.length; i++) {
         expectedReferencesInRegion.add(allReferences.get(expectedReferenceIndices[i]));
      }

      Assert.assertEquals(expectedReferencesInRegion, testling.getAllReferencesInRegion(mediumRegion));
   }

   /**
    * Checks {@link MediumReferenceFactory#getAllReferencesBehindOrEqual(MediumReference)}.
    * 
    * @param testling
    *           The {@link MediumReferenceFactory} under test
    * @param startOffset
    *           The start offset to pass.
    * @param allReferences
    *           All previously created {@link MediumReference}s (return value of
    *           {@link #createAndAddDefaultReferences(MediumReferenceFactory)}).
    * @param expectedReferenceIndices
    *           The indices of all {@link MediumReference}s (relative to {@link #THE_REFERENCE_OFFSETS}) that are
    *           expected as a result of a call to {@link MediumReferenceFactory#getAllReferencesInRegion(MediumRegion)}.
    */
   private void checkGetAllReferencesBehindOrEqual(MediumReferenceFactory testling, long startOffset,
      List<MediumReference> allReferences, int[] expectedReferenceIndices) {

      MediumReference ref = new StandardMediumReference(MEDIUM, startOffset);

      List<MediumReference> expectedReferencesBehindOrEqual = new ArrayList<>();

      for (int i = 0; i < expectedReferenceIndices.length; i++) {
         expectedReferencesBehindOrEqual.add(allReferences.get(expectedReferenceIndices[i]));
      }

      Assert.assertEquals(expectedReferencesBehindOrEqual, testling.getAllReferencesBehindOrEqual(ref));
   }

}
