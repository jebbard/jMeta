/**
 *
 * {@link MediumRegionTest}.java
 *
 * @author Jens Ebert
 *
 * @date 17.04.2011
 */
package de.je.jmeta.media.impl;

import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.PreconditionException;
import junit.framework.Assert;

/**
 * {@link MediumRegionTest} tests the {@link MediumRegion} class.
 */
public class MediumRegionTest {

   private static final int START_OFFSET = 15;

   private static final FileMedium MEDIUM = new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, true);

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

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = getCachedMediumRegions();
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

      MediumRegion uncachedRegion = getUncachedMediumRegion();

      Assert.assertEquals(null, uncachedRegion.getBytes());
   }

   /**
    * Tests {@link MediumRegion#getSize()}.
    */
   @Test
   public void getSize_forCachedRegion_returnsByteBufferSize() {

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = getCachedMediumRegions();
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

      MediumRegion uncachedRegion = getUncachedMediumRegion();

      Assert.assertEquals(EXPECTED_SIZE, uncachedRegion.getSize());
   }

   /**
    * Tests {@link MediumRegion#isCached()}.
    */
   @Test
   public void isCached_forUncachedRegion_returnsFalse() {

      MediumRegion uncachedRegion = getUncachedMediumRegion();

      Assert.assertFalse(uncachedRegion.isCached());
   }

   /**
    * Tests {@link MediumRegion#isCached()}.
    */
   @Test
   public void isCached_forCachedRegion_returnsTrue() {

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = getCachedMediumRegions();
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

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = getCachedMediumRegions();
      for (Iterator<IMediumReference> iterator = cacheRegionsToTest.keySet().iterator(); iterator.hasNext();) {
         MediumRegion region = cacheRegionsToTest.get(iterator.next());

         checkReferencesAndSize(region);
      }
   }

   /**
    * Tests {@link MediumRegion#getSize()}, {@link MediumRegion#getStartReference()} and
    * {@link MediumRegion#calculateEndReference()}.
    */
   @Test
   public void getStartAndEndReference_forUncachedRegion_matchSize() {

      MediumRegion region = getUncachedMediumRegion();

      checkReferencesAndSize(region);
   }

   /**
    * Tests {@link MediumRegion#contains(IMediumReference)}.
    */
   @Test
   public void isContained_forStartReferenceAndContainedByteOffsets_returnsTrue() {

      // For both cached and uncached regions
      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = getCachedMediumRegions();

      cacheRegionsToTest.put(getUncachedMediumRegion().getStartReference(), getUncachedMediumRegion());

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
      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = getCachedMediumRegions();

      cacheRegionsToTest.put(getUncachedMediumRegion().getStartReference(), getUncachedMediumRegion());

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

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = getCachedMediumRegions();
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

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = getCachedMediumRegions();
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
   @Test(expected = PreconditionException.class)
   public void discardBytesAtEnd_forUncachedRegion_throwsException() {

      MediumRegion region = getUncachedMediumRegion();
      region.discardBytesAtEnd(new StandardMediumReference(MEDIUM, START_OFFSET));
   }

   /**
    * Tests {@link MediumRegion#discardBytesAtFront(IMediumReference)}.
    */
   @Test(expected = PreconditionException.class)
   public void discardBytesAtFront_forUncachedRegion_throwsException() {

      MediumRegion region = getUncachedMediumRegion();
      region.discardBytesAtFront(new StandardMediumReference(MEDIUM, START_OFFSET));
   }

   /**
    * Tests {@link MediumRegion#discardBytesAtEnd(IMediumReference)}.
    */
   @Test(expected = PreconditionException.class)
   public void discardBytesAtEnd_endReference_throwsException() {

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = getCachedMediumRegions();

      MediumRegion region = cacheRegionsToTest.values().iterator().next();

      region.discardBytesAtEnd(region.calculateEndReference());
   }

   /**
    * Tests {@link MediumRegion#discardBytesAtFront(IMediumReference)}.
    */
   @Test
   public void discardBytesAtFront_startReference_changesNothing() {

      final Map<IMediumReference, MediumRegion> cacheRegionsToTest = getCachedMediumRegions();
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
    * Checks {@link MediumRegion#getSize()}, {@link MediumRegion#getStartReference()} and
    * {@link MediumRegion#calculateEndReference()} for being consistent.
    * 
    * @param region
    *           The {@link MediumRegion} to check.
    */
   private void checkReferencesAndSize(MediumRegion region) {

      IMediumReference startReference = region.getStartReference();
      IMediumReference endReference = region.calculateEndReference();

      Assert.assertNotNull(startReference);
      Assert.assertNotNull(endReference);

      Assert.assertTrue(endReference.behindOrEqual(startReference));
      Assert.assertTrue(startReference.before(endReference));

      Assert.assertTrue(region.getSize() > 0);

      long difference = endReference.distanceTo(startReference);

      Assert.assertEquals(difference, region.getSize());
   }

   private static MediumRegion getUncachedMediumRegion() {

      return new MediumRegion(new StandardMediumReference(MEDIUM, START_OFFSET / 2), EXPECTED_SIZE);
   }

   /**
    * Returns the cached {@link MediumRegion}s to test.
    * 
    * @return The cached {@link MediumRegion}s to test.
    */
   private static Map<IMediumReference, MediumRegion> getCachedMediumRegions() {

      final Map<IMediumReference, MediumRegion> theCacheRegions = new LinkedHashMap<>();
      for (int i = 0; i < THE_BUFFERS.length; i++) {
         final IMediumReference nextStartReference = new StandardMediumReference(MEDIUM, i * 10 + START_OFFSET);
         theCacheRegions.put(nextStartReference, new MediumRegion(nextStartReference, ByteBuffer.wrap(THE_BUFFERS[i])));
      }

      return theCacheRegions;
   }
}
