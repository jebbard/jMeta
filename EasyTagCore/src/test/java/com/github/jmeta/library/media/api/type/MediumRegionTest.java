/**
 *
 * {@link MediumRegionTest}.java
 *
 * @author Jens Ebert
 *
 * @date 17.04.2011
 */
package com.github.jmeta.library.media.api.type;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import com.github.jmeta.library.media.api.helper.MediaTestCaseConstants;
import com.github.jmeta.library.media.api.type.FileMedium;
import com.github.jmeta.library.media.api.type.IMediumReference;
import com.github.jmeta.library.media.api.type.InMemoryMedium;
import com.github.jmeta.library.media.api.type.MediumRegion;
import com.github.jmeta.library.media.impl.reference.StandardMediumReference;
import com.github.jmeta.utility.dbc.api.exception.PreconditionUnfullfilledException;

/**
 * {@link MediumRegionTest} tests the {@link MediumRegion} class.
 */
public class MediumRegionTest {

   private static final int START_OFFSET = 15;

   private static final FileMedium MEDIUM = new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, true);

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

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      for (Iterator<IMediumReference> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
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

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      for (Iterator<IMediumReference> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
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

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      for (Iterator<IMediumReference> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         Assert.assertTrue(region.isCached());
      }
   }

   /**
    * Tests {@link MediumRegion#getSize()}, {@link MediumRegion#getStartReference()} and
    * {@link MediumRegion#calculateEndReference()}.
    */
   @Test
   public void getStartAndEndReference_forCachedRegion_matchSize() {

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      for (Iterator<IMediumReference> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         assertRegionIsConsistent(region);
      }
   }

   /**
    * Tests {@link MediumRegion#getSize()}, {@link MediumRegion#getStartReference()} and
    * {@link MediumRegion#calculateEndReference()}.
    */
   @Test
   public void getStartAndEndReference_forUncachedRegion_matchSize() {

      MediumRegion region = createUncachedMediumRegion();

      assertRegionIsConsistent(region);
   }

   /**
    * Tests {@link MediumRegion#contains(IMediumReference)}.
    */
   @Test
   public void isContained_forStartReferenceAndContainedByteOffsets_returnsTrue() {

      // For both cached and uncached regions
      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();

      cacheRegionsToTest.put(createUncachedMediumRegion().getStartReference(), createUncachedMediumRegion());

      for (Iterator<IMediumReference> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         // By definition, the start reference of the region is contained
         Assert.assertTrue(region.contains(region.getStartReference()));

         int difference = (int) (region.calculateEndReference().distanceTo(region.getStartReference()));

         for (int i = 1; i < difference; i++) {
            IMediumReference containedReference = region.getStartReference().advance(i);

            Assert.assertTrue(region.contains(containedReference));
         }
      }
   }

   /**
    * Tests {@link MediumRegion#contains(IMediumReference)}.
    */
   @Test
   public void isContained_forEndReferenceAndOutsideByteOffsets_returnsFalse() {

      // For both cached and uncached regions
      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();

      cacheRegionsToTest.put(createUncachedMediumRegion().getStartReference(), createUncachedMediumRegion());

      for (Iterator<IMediumReference> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         // By definition, the end reference of the region is NOT contained
         Assert.assertFalse(region.contains(region.calculateEndReference()));
         Assert.assertFalse(region.contains(region.calculateEndReference().advance(10)));
         Assert.assertFalse(region.contains(region.getStartReference().advance(-START_OFFSET / 3)));
      }
   }

   /**
    * Tests {@link MediumRegion#discardBytesAtEnd(IMediumReference)}.
    */
   @Test
   public void discardBytesAtEnd_oneHalfOfBytesForCachedRegion_areDiscarded() {

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      int buffIndex = 0;
      for (Iterator<IMediumReference> iterator = cacheRegionsToTest.keySet().iterator(); iterator
         .hasNext(); buffIndex++) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         int halfSize = region.getSize() / 2;

         IMediumReference sharedReference = region.getStartReference().advance(halfSize);

         region.discardBytesAtEnd(sharedReference);

         Assert.assertEquals(region.getSize(), halfSize);
         Assert.assertEquals(sharedReference, region.calculateEndReference());
         Assert.assertEquals(region.getSize(), region.getBytes().remaining());

         if (halfSize > 0) {
            Assert.assertEquals(THE_BUFFERS[buffIndex][halfSize], region.getBytes().get());
         } else {
            Assert.assertEquals(0, region.getBytes().remaining());
         }
      }
   }

   /**
    * Tests {@link MediumRegion#discardBytesAtFront(IMediumReference)}.
    */
   @Test
   public void discardBytesAtFront_firstBytesForCachedRegion_areDiscarded() {

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      int buffIndex = 0;
      for (Iterator<IMediumReference> iterator = cacheRegionsToTest.keySet().iterator(); iterator
         .hasNext(); buffIndex++) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         int initialSize = region.getSize();
         int sizeDiff = initialSize / 2;

         IMediumReference sharedReference = region.getStartReference().advance(sizeDiff);

         region.discardBytesAtFront(sharedReference);

         Assert.assertEquals(region.getSize(), initialSize - sizeDiff);
         Assert.assertEquals(sharedReference, region.getStartReference());
         Assert.assertEquals(region.getSize(), region.getBytes().remaining());
         byte[] bytes = new byte[initialSize - sizeDiff];
         region.getBytes().get(bytes);

         Assert.assertEquals(THE_BUFFERS[buffIndex][sizeDiff], bytes[0]);

         Assert.assertEquals(THE_BUFFERS[buffIndex][THE_BUFFERS[buffIndex].length - 1], bytes[bytes.length - 1]);
      }
   }

   /**
    * Tests {@link MediumRegion#discardBytesAtEnd(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void discardBytesAtEnd_forUncachedRegion_throwsException() {

      MediumRegion region = createUncachedMediumRegion();
      region.discardBytesAtEnd(new StandardMediumReference(MEDIUM, START_OFFSET));
   }

   /**
    * Tests {@link MediumRegion#discardBytesAtFront(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void discardBytesAtFront_forUncachedRegion_throwsException() {

      MediumRegion region = createUncachedMediumRegion();
      region.discardBytesAtFront(new StandardMediumReference(MEDIUM, START_OFFSET));
   }

   /**
    * Tests {@link MediumRegion#discardBytesAtEnd(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void discardBytesAtEnd_endReference_throwsException() {

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();

      MediumRegion region = cacheRegionsToTest.values().iterator().next();

      region.discardBytesAtEnd(region.calculateEndReference());
   }

   /**
    * Tests {@link MediumRegion#discardBytesAtFront(IMediumReference)}.
    */
   @Test
   public void discardBytesAtFront_startReference_changesNothing() {

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = createCachedMediumRegions();
      for (Iterator<IMediumReference> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         ByteBuffer bytesBeforeChange = region.getBytes();
         int sizeBeforeChange = region.getSize();
         IMediumReference startReferenceBeforeChange = region.getStartReference();
         IMediumReference endReferenceBeforeChange = region.calculateEndReference();

         region.discardBytesAtFront(region.getStartReference());

         Assert.assertEquals(bytesBeforeChange, region.getBytes());
         Assert.assertEquals(sizeBeforeChange, region.getSize());
         Assert.assertEquals(startReferenceBeforeChange, region.getStartReference());
         Assert.assertEquals(endReferenceBeforeChange, region.calculateEndReference());
      }
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase1_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 10);

      Assert.assertTrue(region1.overlapsOtherRegionAtFront(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase2_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 5);

      Assert.assertTrue(region1.overlapsOtherRegionAtFront(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase3_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);

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

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);

      Assert.assertFalse(region1.overlapsOtherRegionAtFront(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase5_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 5);

      Assert.assertFalse(region2.overlapsOtherRegionAtFront(region1));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase6_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 2);

      Assert.assertFalse(region1.overlapsOtherRegionAtFront(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase7_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 2);

      Assert.assertFalse(region2.overlapsOtherRegionAtFront(region1));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)}.
    */
   @Test
   public void overlapsAtFront_forCase8_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 20), 10);

      Assert.assertFalse(region1.overlapsOtherRegionAtFront(region2));
      Assert.assertFalse(region2.overlapsOtherRegionAtFront(region1));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase1_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);

      Assert.assertTrue(region1.overlapsOtherRegionAtBack(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase2_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 5);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);

      Assert.assertTrue(region1.overlapsOtherRegionAtBack(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase3_returnsTrue() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);

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

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 10);

      Assert.assertFalse(region1.overlapsOtherRegionAtBack(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase5_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 5);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);

      Assert.assertFalse(region2.overlapsOtherRegionAtBack(region1));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase6_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 2);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);

      Assert.assertFalse(region1.overlapsOtherRegionAtBack(region2));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase7_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 2);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);

      Assert.assertFalse(region2.overlapsOtherRegionAtBack(region1));
   }

   /**
    * Tests {@link MediumRegion#overlapsOtherRegionAtBack(MediumRegion)}.
    */
   @Test
   public void overlapsAtBack_forCase8_returnsFalse() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 20), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);

      Assert.assertFalse(region1.overlapsOtherRegionAtBack(region2));
      Assert.assertFalse(region2.overlapsOtherRegionAtBack(region1));
   }

   /**
    * Tests {@link MediumRegion#getOverlappingByteCount(MediumRegion)}.
    */
   @Test
   public void getOverlappingByteCount_forNoSharedBytes_returnsZero() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 20), 10);

      Assert.assertEquals(0, region1.getOverlappingByteCount(region2));
      Assert.assertEquals(0, region2.getOverlappingByteCount(region1));
   }

   /**
    * Tests {@link MediumRegion#getOverlappingByteCount(MediumRegion)}.
    */
   @Test
   public void getOverlappingByteCount_forSharedBytes_returnsTheirExpectedCount() {

      MediumRegion region1 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 10);
      MediumRegion region2 = new MediumRegion(new StandardMediumReference(MEDIUM, 0), 10);
      MediumRegion region3 = new MediumRegion(new StandardMediumReference(MEDIUM, 5), 5);
      MediumRegion region4 = new MediumRegion(new StandardMediumReference(MEDIUM, 2), 4);

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
    * Tests {@link MediumRegion#split(IMediumReference)}.
    */
   @Test
   public void split_atValidOffsetForUncachedRegion_returnsTwoCorrectSizedRegions() {
      MediumRegion uncachedRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 10L), 20);
      MediumRegion expectedFirstUncachedRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 10L), 5);
      MediumRegion expectedSecondUncachedRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 15L), 15);

      assertSplitSplitsRegionCorrectly(uncachedRegion, 15L, expectedFirstUncachedRegion, expectedSecondUncachedRegion);
   }

   /**
    * Tests {@link MediumRegion#split(IMediumReference)}.
    */
   @Test
   public void split_atValidOffsetForCachedRegion_returnsTwoCorrectSizedRegions() {
      MediumRegion cachedRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 10L),
         ByteBuffer.wrap(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, }));
      MediumRegion expectedFirstCachedRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 10L),
         ByteBuffer.wrap(new byte[] { 1, 2, 3, }));
      MediumRegion expectedSecondCachedRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 13L),
         ByteBuffer.wrap(new byte[] { 4, 5, 6, 7, 8, 9, 10, }));

      assertSplitSplitsRegionCorrectly(cachedRegion, 13L, expectedFirstCachedRegion, expectedSecondCachedRegion);
   }

   /**
    * Tests {@link MediumRegion#split(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void split_atInvalidOffsetBefore_throwsException() {
      MediumRegion uncachedRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 10L), 20);

      IMediumReference invalidOffsetBefore = new StandardMediumReference(MEDIUM, 4L);

      uncachedRegion.split(invalidOffsetBefore);
   }

   /**
    * Tests {@link MediumRegion#split(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void split_atInvalidOffsetBehind_throwsException() {
      MediumRegion uncachedRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 10L), 20);

      IMediumReference invalidOffsetBehind = new StandardMediumReference(MEDIUM, 30L);

      uncachedRegion.split(invalidOffsetBehind);
   }

   /**
    * Tests {@link MediumRegion#split(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void split_atStartOffsetOfRegion_throwsException() {
      MediumRegion uncachedRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 10L), 20);

      IMediumReference invalidStartOffset = new StandardMediumReference(MEDIUM, 10L);

      uncachedRegion.split(invalidStartOffset);
   }

   /**
    * Tests {@link MediumRegion#split(IMediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void split_forInvalidMediumReference_throwsException() {
      MediumRegion uncachedRegion = new MediumRegion(new StandardMediumReference(MEDIUM, 10L), 20);

      IMediumReference unrelatedMediumOffset = new StandardMediumReference(UNRELATED_MEDIUM, 15L);

      uncachedRegion.split(unrelatedMediumOffset);
   }

   /**
    * Checks {@link MediumRegion#split(IMediumReference)}.
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

      long initialOffset = regionToSplit.getStartReference().getAbsoluteMediumOffset();
      ByteBuffer initialBytesCopy = null;
      int initialSize = regionToSplit.getSize();

      if (regionToSplit.getBytes() != null) {
         byte[] byteCopy = new byte[regionToSplit.getBytes().remaining()];

         regionToSplit.getBytes().get(byteCopy);

         initialBytesCopy = ByteBuffer.wrap(byteCopy);
      }

      MediumRegion[] splitRegions = regionToSplit.split(new StandardMediumReference(MEDIUM, splitOffset));

      Assert.assertNotNull(splitRegions);
      Assert.assertEquals(2, splitRegions.length);

      MediumRegion firstSplitRegion = splitRegions[0];

      Assert.assertNotNull(firstSplitRegion);
      Assert.assertEquals(expectedFirstRegion, firstSplitRegion);

      MediumRegion secondSplitRegion = splitRegions[1];

      Assert.assertNotNull(secondSplitRegion);
      Assert.assertEquals(expectedSecondRegion, secondSplitRegion);

      // Ensure that the original MediumRegion is still unchanged
      Assert.assertEquals(initialOffset, regionToSplit.getStartReference().getAbsoluteMediumOffset());
      Assert.assertEquals(initialSize, regionToSplit.getSize());
      Assert.assertEquals(initialBytesCopy, regionToSplit.getBytes());
   }

   /**
    * Checks {@link MediumRegion#getSize()}, {@link MediumRegion#getStartReference()} and
    * {@link MediumRegion#calculateEndReference()} for being consistent.
    * 
    * @param region
    *           The {@link MediumRegion} to check.
    */
   private void assertRegionIsConsistent(MediumRegion region) {

      IMediumReference startReference = region.getStartReference();
      IMediumReference endReference = region.calculateEndReference();

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

      return new MediumRegion(new StandardMediumReference(MEDIUM, START_OFFSET / 2), EXPECTED_SIZE);
   }

   /**
    * Creates several cached {@link MediumRegion}s to test.
    * 
    * @return The cached {@link MediumRegion}s to test.
    */
   private static Map<IMediumReference, MediumRegion> createCachedMediumRegions() {

      final Map<IMediumReference, MediumRegion> theCacheRegions = new LinkedHashMap<>();
      for (int i = 0; i < THE_BUFFERS.length; i++) {
         final IMediumReference nextStartReference = new StandardMediumReference(MEDIUM, i * 10 + START_OFFSET);
         theCacheRegions.put(nextStartReference, new MediumRegion(nextStartReference, ByteBuffer.wrap(THE_BUFFERS[i])));
      }

      return theCacheRegions;
   }
}
