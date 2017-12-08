/**
 *
 * {@link MediumRegionTest}.java
 *
 * @author Jens Ebert
 *
 * @date 17.04.2011
 */
package com.github.jmeta.library.media.api.types;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffset;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * {@link MediumRegionTest} tests the {@link MediumRegion} class.
 */
public class MediumRegionTest {

   private static final int START_OFFSET = 15;

   private static final FileMedium MEDIUM = new FileMedium(MediaTestFiles.FIRST_TEST_FILE_PATH, true);

   private static final InMemoryMedium UNRELATED_MEDIUM = new InMemoryMedium(new byte[] {}, "Fake", false);

   private final static byte[][] THE_BUFFERS = new byte[][] { new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, },
      new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3,
         4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 1, 2, 3, 4, 5, 6, 7, 8, 9,
         10, },
      new byte[] { 1, }, new byte[] { 1, 2, 5, 6, 7, 8, 9, 'a', 'b', ' ' } };

   private static int EXPECTED_SIZE = 200;

   /**
    * Tests {@link MediumRegion#getBytes()}.
    */
   @Test
   public void getBytes_forCachedRegion_returnsInitializedBytes() {

      final Map<MediumOffset, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      for (Iterator<MediumOffset> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         ByteBuffer buffer = region.getBytes();

         Assert.assertEquals(buffer, region.getBytes());
      }
   }

   /**
    * Tests {@link MediumRegion#getBytes()}.
    */
   @Test
   public void getBytes_forUncachedRegion_returnsNoBytes() {

      MediumRegion uncachedRegion = createUncachedMediumRegion();

      Assert.assertEquals(null, uncachedRegion.getBytes());
   }

   /**
    * Tests {@link MediumRegion#getSize()}.
    */
   @Test
   public void getSize_forCachedRegion_returnsByteBufferSize() {

      final Map<MediumOffset, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      for (Iterator<MediumOffset> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         ByteBuffer buffer = region.getBytes();

         Assert.assertEquals(buffer.remaining(), region.getSize());
      }
   }

   /**
    * Tests {@link MediumRegion#getSize()}.
    */
   @Test
   public void getSize_forUncachedRegion_returnsInitialisedSize() {

      MediumRegion uncachedRegion = createUncachedMediumRegion();

      Assert.assertEquals(EXPECTED_SIZE, uncachedRegion.getSize());
   }

   /**
    * Tests {@link MediumRegion#isCached()}.
    */
   @Test
   public void isCached_forUncachedRegion_returnsFalse() {

      MediumRegion uncachedRegion = createUncachedMediumRegion();

      Assert.assertFalse(uncachedRegion.isCached());
   }

   /**
    * Tests {@link MediumRegion#isCached()}.
    */
   @Test
   public void isCached_forCachedRegion_returnsTrue() {

      final Map<MediumOffset, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      for (Iterator<MediumOffset> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         Assert.assertTrue(region.isCached());
      }
   }

   /**
    * Tests {@link MediumRegion#getSize()}, {@link MediumRegion#getStartOffset()} and
    * {@link MediumRegion#calculateEndOffset()}.
    */
   @Test
   public void getStartAndEndReference_forCachedRegion_matchSize() {

      final Map<MediumOffset, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      for (Iterator<MediumOffset> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         assertRegionIsConsistent(region);
      }
   }

   /**
    * Tests {@link MediumRegion#getSize()}, {@link MediumRegion#getStartOffset()} and
    * {@link MediumRegion#calculateEndOffset()}.
    */
   @Test
   public void getStartAndEndReference_forUncachedRegion_matchSize() {

      MediumRegion region = createUncachedMediumRegion();

      assertRegionIsConsistent(region);
   }

   /**
    * Tests {@link MediumRegion#contains(MediumOffset)}.
    */
   @Test
   public void isContained_forStartReferenceAndContainedByteOffsets_returnsTrue() {

      // For both cached and uncached regions
      final Map<MediumOffset, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();

      cacheRegionsToTest.put(createUncachedMediumRegion().getStartOffset(), createUncachedMediumRegion());

      for (Iterator<MediumOffset> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         // By definition, the start reference of the region is contained
         Assert.assertTrue(region.contains(region.getStartOffset()));

         int difference = (int) (region.calculateEndOffset().distanceTo(region.getStartOffset()));

         for (int i = 1; i < difference; i++) {
            MediumOffset containedReference = region.getStartOffset().advance(i);

            Assert.assertTrue(region.contains(containedReference));
         }
      }
   }

   /**
    * Tests {@link MediumRegion#contains(MediumOffset)}.
    */
   @Test
   public void isContained_forEndReferenceAndOutsideByteOffsets_returnsFalse() {

      // For both cached and uncached regions
      final Map<MediumOffset, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();

      cacheRegionsToTest.put(createUncachedMediumRegion().getStartOffset(), createUncachedMediumRegion());

      for (Iterator<MediumOffset> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         // By definition, the end reference of the region is NOT contained
         Assert.assertFalse(region.contains(region.calculateEndOffset()));
         Assert.assertFalse(region.contains(region.calculateEndOffset().advance(10)));
         Assert.assertFalse(region.contains(region.getStartOffset().advance(-START_OFFSET / 3)));
      }
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase1_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 10);

      Assert.assertTrue(region1.overlapsOtherRegionAtFront(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase2_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 5);

      Assert.assertTrue(region1.overlapsOtherRegionAtFront(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase3_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);

      Assert.assertTrue(region1.overlapsOtherRegionAtBack(region2));
      Assert.assertTrue(region2.overlapsOtherRegionAtBack(region1));
      Assert.assertTrue(region1.overlapsOtherRegionAtBack(region1));
      Assert.assertTrue(region2.overlapsOtherRegionAtBack(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase4_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);

      Assert.assertFalse(region1.overlapsOtherRegionAtFront(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase5_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 5);

      Assert.assertFalse(region2.overlapsOtherRegionAtFront(region1));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase6_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 2);

      Assert.assertFalse(region1.overlapsOtherRegionAtFront(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase7_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 2);

      Assert.assertFalse(region2.overlapsOtherRegionAtFront(region1));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase8_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 20), 10);

      Assert.assertFalse(region1.overlapsOtherRegionAtFront(region2));
      Assert.assertFalse(region2.overlapsOtherRegionAtFront(region1));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase1_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);

      Assert.assertTrue(region1.overlapsOtherRegionAtBack(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase2_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 5);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);

      Assert.assertTrue(region1.overlapsOtherRegionAtBack(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase3_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);

      Assert.assertTrue(region1.overlapsOtherRegionAtBack(region2));
      Assert.assertTrue(region2.overlapsOtherRegionAtBack(region1));
      Assert.assertTrue(region1.overlapsOtherRegionAtBack(region1));
      Assert.assertTrue(region2.overlapsOtherRegionAtBack(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase4_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 10);

      Assert.assertFalse(region1.overlapsOtherRegionAtBack(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase5_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 5);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);

      Assert.assertFalse(region2.overlapsOtherRegionAtBack(region1));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase6_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 2);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);

      Assert.assertFalse(region1.overlapsOtherRegionAtBack(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase7_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 2);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);

      Assert.assertFalse(region2.overlapsOtherRegionAtBack(region1));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase8_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 20), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);

      Assert.assertFalse(region1.overlapsOtherRegionAtBack(region2));
      Assert.assertFalse(region2.overlapsOtherRegionAtBack(region1));
   }

   /**
    * Tests {@link MediumRegion#getOverlappingByteCount(MediumRegion)}.
    */
   @Test
   public void getOverlappingByteCount_forNoSharedBytes_returnsZero() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 20), 10);

      Assert.assertEquals(0, region1.getOverlappingByteCount(region2));
      Assert.assertEquals(0, region2.getOverlappingByteCount(region1));
   }

   /**
    * Tests {@link MediumRegion#getOverlappingByteCount(MediumRegion)}.
    */
   @Test
   public void getOverlappingByteCount_forSharedBytes_returnsTheirExpectedCount() {

      MediumRegion region1 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumOffset(MEDIUM, 0), 10);
      MediumRegion region3 = new MediumRegion(new StandardMediumOffset(MEDIUM, 5), 5);
      MediumRegion region4 = new MediumRegion(new StandardMediumOffset(MEDIUM, 2), 4);

      Assert.assertEquals(5, region1.getOverlappingByteCount(region2));
      Assert.assertEquals(5, region2.getOverlappingByteCount(region1));
      Assert.assertEquals(5, region1.getOverlappingByteCount(region3));
      Assert.assertEquals(5, region3.getOverlappingByteCount(region1));
      Assert.assertEquals(5, region2.getOverlappingByteCount(region3));
      Assert.assertEquals(5, region3.getOverlappingByteCount(region2));
      Assert.assertEquals(1, region4.getOverlappingByteCount(region1));
      Assert.assertEquals(1, region1.getOverlappingByteCount(region4));
      Assert.assertEquals(4, region2.getOverlappingByteCount(region4));
      Assert.assertEquals(4, region4.getOverlappingByteCount(region2));
      Assert.assertEquals(1, region4.getOverlappingByteCount(region3));
      Assert.assertEquals(1, region3.getOverlappingByteCount(region4));
   }

   /**
    * Tests {@link MediumRegion#split(MediumOffset)}.
    */
   @Test
   public void split_atValidOffsetForUncachedRegion_returnsTwoCorrectSizedRegions() {
      MediumRegion uncachedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 10L), 20);
      MediumRegion expectedFirstUncachedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 10L), 5);
      MediumRegion expectedSecondUncachedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 15L), 15);

      assertSplitSplitsRegionCorrectly(uncachedRegion, 15L, expectedFirstUncachedRegion, expectedSecondUncachedRegion);
   }

   /**
    * Tests {@link MediumRegion#split(MediumOffset)}.
    */
   @Test
   public void split_atValidOffsetForCachedRegion_returnsTwoCorrectSizedRegions() {
      MediumRegion cachedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 10L),
         ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, }));
      MediumRegion expectedFirstCachedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 10L),
         ByteBuffer.wrap(new byte[] { 1, 2, 3, }));
      MediumRegion expectedSecondCachedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 13L),
         ByteBuffer.wrap(new byte[] { 4, 5, 6, 7, 8, 9, 10, }));

      assertSplitSplitsRegionCorrectly(cachedRegion, 13L, expectedFirstCachedRegion, expectedSecondCachedRegion);
   }

   /**
    * Tests {@link MediumRegion#split(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void split_atInvalidOffsetBefore_throwsException() {
      MediumRegion uncachedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 10L), 20);

      MediumOffset invalidOffsetBefore = new StandardMediumOffset(MEDIUM, 4L);

      uncachedRegion.split(invalidOffsetBefore);
   }

   /**
    * Tests {@link MediumRegion#split(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void split_atInvalidOffsetBehind_throwsException() {
      MediumRegion uncachedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 10L), 20);

      MediumOffset invalidOffsetBehind = new StandardMediumOffset(MEDIUM, 30L);

      uncachedRegion.split(invalidOffsetBehind);
   }

   /**
    * Tests {@link MediumRegion#split(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void split_atStartOffsetOfRegion_throwsException() {
      MediumRegion uncachedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 10L), 20);

      MediumOffset invalidStartOffset = new StandardMediumOffset(MEDIUM, 10L);

      uncachedRegion.split(invalidStartOffset);
   }

   /**
    * Tests {@link MediumRegion#split(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void split_forInvalidMediumReference_throwsException() {
      MediumRegion uncachedRegion = new MediumRegion(new StandardMediumOffset(MEDIUM, 10L), 20);

      MediumOffset unrelatedMediumOffset = new StandardMediumOffset(UNRELATED_MEDIUM, 15L);

      uncachedRegion.split(unrelatedMediumOffset);
   }

   /**
    * Checks {@link MediumRegion#split(MediumOffset)}.
    * 
    * @param regionToSplit
    *           The {@link MediumRegion} to call the method for
    * @param splitOffset
    *           The split offset to use
    * @param expectedFirstRegion
    *           The first expected split region
    * @param expectedSecondRegion
    *           The second expected split region
    */
   private void assertSplitSplitsRegionCorrectly(MediumRegion regionToSplit, long splitOffset,
      MediumRegion expectedFirstRegion, MediumRegion expectedSecondRegion) {

      long initialOffset = regionToSplit.getStartOffset().getAbsoluteMediumOffset();
      ByteBuffer initialBytesCopy = null;
      int initialSize = regionToSplit.getSize();

      if (regionToSplit.getBytes() != null) {
         byte[] byteCopy = new byte[regionToSplit.getBytes().remaining()];

         regionToSplit.getBytes().get(byteCopy);

         initialBytesCopy = ByteBuffer.wrap(byteCopy);
      }

      MediumRegion[] splitRegions = regionToSplit.split(new StandardMediumOffset(MEDIUM, splitOffset));

      Assert.assertNotNull(splitRegions);
      Assert.assertEquals(2, splitRegions.length);

      MediumRegion firstSplitRegion = splitRegions[0];

      Assert.assertNotNull(firstSplitRegion);
      Assert.assertEquals(expectedFirstRegion, firstSplitRegion);

      MediumRegion secondSplitRegion = splitRegions[1];

      Assert.assertNotNull(secondSplitRegion);
      Assert.assertEquals(expectedSecondRegion, secondSplitRegion);

      // Ensure that the original MediumRegion is still unchanged
      Assert.assertEquals(initialOffset, regionToSplit.getStartOffset().getAbsoluteMediumOffset());
      Assert.assertEquals(initialSize, regionToSplit.getSize());
      Assert.assertEquals(initialBytesCopy, regionToSplit.getBytes());
   }

   /**
    * Checks {@link MediumRegion#getSize()}, {@link MediumRegion#getStartOffset()} and
    * {@link MediumRegion#calculateEndOffset()} for being consistent.
    * 
    * @param region
    *           The {@link MediumRegion} to check.
    */
   private void assertRegionIsConsistent(MediumRegion region) {

      MediumOffset startReference = region.getStartOffset();
      MediumOffset endReference = region.calculateEndOffset();

      Assert.assertNotNull(startReference);
      Assert.assertNotNull(endReference);

      Assert.assertTrue(endReference.behindOrEqual(startReference));
      Assert.assertTrue(startReference.before(endReference));

      Assert.assertTrue(region.getSize() > 0);

      long distance = endReference.distanceTo(startReference);

      Assert.assertEquals(distance, region.getSize());
   }

   /**
    * Creates a single uncached medium region for testing.
    * 
    * @return A single uncached medium region for testing.
    */
   private static MediumRegion createUncachedMediumRegion() {

      return new MediumRegion(new StandardMediumOffset(MEDIUM, START_OFFSET / 2), EXPECTED_SIZE);
   }

   /**
    * Creates several cached {@link MediumRegion}s to test.
    * 
    * @return The cached {@link MediumRegion}s to test.
    */
   private static Map<MediumOffset, MediumRegion> createCachedMediumRegions() {

      final Map<MediumOffset, MediumRegion> theCacheRegions = new LinkedHashMap<>();
      for (int i = 0; i < THE_BUFFERS.length; i++) {
         final MediumOffset nextStartReference = new StandardMediumOffset(MEDIUM, i * 10 + START_OFFSET);
         theCacheRegions.put(nextStartReference, new MediumRegion(nextStartReference, ByteBuffer.wrap(THE_BUFFERS[i])));
      }

      return theCacheRegions;
   }
}
