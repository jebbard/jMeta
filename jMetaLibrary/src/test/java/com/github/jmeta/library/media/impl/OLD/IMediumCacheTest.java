/**
 *
 * {@link IMediumCacheTest}.java
 *
 * @author Jens Ebert
 *
 * @date 08.04.2011
 */
package com.github.jmeta.library.media.impl.OLD;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.TreeMap;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.impl.reference.StandardMediumReference;

/**
 * {@link IMediumCacheTest} tests the {@link MediumCache} interface.
 */
public abstract class IMediumCacheTest {

   private static final String UNEXPECTED_END_OF_MEDIUM = "Unexpected end of medium! Exception: ";

   private static final int FREE_REGION_1_SIZE = 20;

   private static final int FREE_REGION_2_SIZE = 100;

   private static final int FREE_REGION_2A_SIZE = 3;

   private static final int FREE_REGION_3_SIZE = 20;

   private static final int FREE_REGION_1_START = 105;

   private static final int FREE_REGION_2_START = FREE_REGION_1_START + FREE_REGION_1_SIZE;

   private static final int FREE_REGION_2A_START = FREE_REGION_2_START + FREE_REGION_2_SIZE;

   private static final int FREE_REGION_3_START = FREE_REGION_2A_START + FREE_REGION_2A_SIZE + 10;

   /**
    * Adds another cache region starting at medium offset 100 plus arbitrary offset and size.
    * 
    * @param cache
    *           The {@link MediumCache}.
    * @param offset
    *           The offset, added to the fixed offset of 100.
    * @param size
    *           The size of the new region.
    * @throws EndOfMediumException
    *            If end of medium has been reached.
    */
   private void addCacheRegion(MediumCache cache, int offset, int size) throws EndOfMediumException {

      StandardMediumReference anotherExistingRegionReference = new StandardMediumReference(getExpectedMedium(), offset);
      final int anotherExistingRegionSize = size;
      cache.buffer(anotherExistingRegionReference, anotherExistingRegionSize);
      Assert.assertTrue(cache.getBufferedByteCountAt(anotherExistingRegionReference) >= size);
   }

   /**
    * Checks the {@link MediumCache#buffer} method.
    * 
    * @param testCacheSizes
    *           The regions to cache.
    * @param testling
    *           The {@link MediumCache} instance to test.
    * @throws EndOfMediumException
    *            if read goes beyond end of medium.
    */
   private void checkCache(final Map<MediumReference, Integer> testCacheSizes, MediumCache testling)
      throws EndOfMediumException {

      for (Iterator<MediumReference> iterator = testCacheSizes.keySet().iterator(); iterator.hasNext();) {
         MediumReference reference = iterator.next();
         long size = testCacheSizes.get(reference);

         testling.buffer(reference, (int) size);

         Assert.assertTrue(testling.getBufferedByteCountAt(reference) >= size);

         // Of course, fewer bytes than the cached size are also cached
         for (int i = 0; i < 5 && size > 0; i++, size--) {
            Assert.assertTrue(testling.getBufferedByteCountAt(reference) >= size);
         }
      }

      Map<MediumReference, Integer> cacheRegions = testling.getBufferedRegions();

      // Cache regions are smaller than the allowed maximum size
      for (Iterator<MediumReference> iterator = cacheRegions.keySet().iterator(); iterator.hasNext();) {
         MediumReference nextKey = iterator.next();
         Integer nextValue = cacheRegions.get(nextKey);

         Assert.assertTrue(nextValue <= getMaximumCacheSize());
      }
   }

   /**
    * Checks that there must be no overlapping regions within the current cache contents.
    * 
    * @param testling
    *           The {@link MediumCache}.
    */
   private void checkNoOverlappingRegions(MediumCache testling) {

      Comparator<? super MediumReference> mediumReferenceComparator = (leftRef,
         rightRef) -> new Long(leftRef.getAbsoluteMediumOffset()).compareTo(rightRef.getAbsoluteMediumOffset());

      Map<MediumReference, Integer> sortedCachedRegions = new TreeMap<>(mediumReferenceComparator);

      sortedCachedRegions.putAll(testling.getBufferedRegions());

      MediumReference previousEndReference = null;

      for (Iterator<MediumReference> iterator = sortedCachedRegions.keySet().iterator(); iterator.hasNext();) {
         MediumReference currentReference = iterator.next();
         MediumReference currentEndReference = currentReference
            .advance(testling.getBufferedRegions().get(currentReference));

         if (previousEndReference != null) {
            Assert.assertTrue(currentReference.behindOrEqual(previousEndReference));
         }

         previousEndReference = currentEndReference;
      }
   }

   /**
    * Checks whether expected data is read from the {@link MediumCache}.
    * 
    * @param bytesRead
    *           The bytes read from the {@link MediumCache}, relevant are those bytes between position and limit.
    * @param expectedBytes
    *           The bytes expected to be read.
    */
   private void checkReadData(ByteBuffer bytesRead, byte[] expectedBytes) {

      byte[] dummyBytes = new byte[bytesRead.remaining()];

      bytesRead.mark();

      bytesRead.get(dummyBytes);

      Assert.assertTrue(Arrays.equals(expectedBytes, dummyBytes));

      bytesRead.reset();
   }

   /**
    * Checks whether expected data for boundary cases is read from the {@link MediumCache}.
    * 
    * @param bytesRead
    *           The bytes read from the {@link MediumCache}, relevant are those bytes between position and limit.
    * @param reference
    *           The {@link StandardMediumReference} where the data has been read.
    */
   private void checkReadDataBoundaryCases(ByteBuffer bytesRead, MediumReference reference) {

      byte[] expectedBytes = getExpectedDataBoundaryCases();

      long offset = reference.getAbsoluteMediumOffset();

      if (offset + bytesRead.remaining() >= expectedBytes.length)
         throw new RuntimeException("Read data length must be smaller than expected data length");

      int theOffset = (int) offset;

      while (bytesRead.hasRemaining())
         Assert.assertEquals(expectedBytes[theOffset++], bytesRead.get());
   }

   /**
    * Checks the test data to avoid illegal values.
    */
   private void checkTestData() {

      if (getExpectedMedium() == null || getTestling() == null || getTestCacheSizes() == null
         || getExpectedData() == null)
         throw new RuntimeException("Test data must not be null");

      if (!getExpectedData().keySet().equals(getTestCacheSizes().keySet()))
         throw new RuntimeException("KeySets of getExpectedData() and getTestCacheSizes() must match");

      for (Iterator<MediumReference> iterator = getExpectedData().keySet().iterator(); iterator.hasNext();) {
         MediumReference nextKey = iterator.next();
         byte[] nextValue = getExpectedData().get(nextKey);

         Integer nextSize = getTestCacheSizes().get(nextKey);

         if (nextValue.length != nextSize)
            throw new RuntimeException(
               "Length of the getExpectedData() must match the size returned by getTestCacheSizes()");
      }
   }

   /**
    * Returns the data bytes expected at the cached {@link StandardMediumReference}s. The size of the {@link Map}
    * returned must match the size of the {@link Map} returned by {@link #getTestCacheSizes()}. Furthermore, the keySet
    * must be identical. Last but not least, the size of the bytes in this returned {@link Map} must match the cached
    * sizes returned by {@link #getTestCacheSizes()}.
    * 
    * @return the data bytes expected at the cached {@link StandardMediumReference}s.
    */
   protected abstract Map<MediumReference, byte[]> getExpectedData();

   /**
    * Returns the data bytes expected in the boundary test cases from offset 0 onwards. (About 50 bytes are sufficient)
    * 
    * @return the data bytes expected in the boundary test cases from offset 0 onwards.
    */
   protected abstract byte[] getExpectedDataBoundaryCases();

   /**
    * Returns the expected {@link Medium} that is cached by the {@link MediumCache} implementation.
    * 
    * @return the expected {@link Medium} that is cached by the {@link MediumCache} implementation.
    */
   protected abstract Medium<?> getExpectedMedium();

   /**
    * Returns whether the {@link MediumCache} is expected to be random-access or not.
    * 
    * @return whether the {@link MediumCache} is expected to be random-access or not.
    */
   protected abstract boolean getExpectedRandomAccess();

   /**
    * Returns the expected maximum cache region size.
    * 
    * @return the expected maximum cache region size.
    */
   protected abstract int getMaximumCacheSize();

   /**
    * Returns the cache regions to be used for testing {@link MediumCache#buffer} and
    * {@link MediumCache#getBufferedByteCountAt} with an end of medium situation. The map contains the
    * {@link StandardMediumReference}s to be used with {@link MediumCache#buffer}, mapped to the actual number bytes
    * left up to the end of the medium.
    * 
    * It should be regions included that overlap each other.
    * 
    * These regions must cause an {@link EndOfMediumException}. At least one of the {@link StandardMediumReference}s
    * should be chosen to be more than 1 byte before the end of medium, so that bytes can be cached regularly using half
    * of the distance to end of medium before finally testing the caching that will cause the end of medium situation.
    * 
    * @return the cache regions to be used for testing {@link MediumCache#buffer} and
    *         {@link MediumCache#getBufferedByteCountAt}.
    */
   protected abstract Map<MediumReference, Integer> getMediumReferencesAndDistToEOM();

   /**
    * Returns all the coherent overlapping multi-regions, i.e. those regions that consist of several other regions
    * returned by {@link #getTestCacheSizes()}. These regions might either overlap, contain each other or be the direct
    * neighbour to each other.
    * 
    * @return all the coherent overlapping multi-regions, i.e. those regions that consist of several other regions
    *         returned by {@link #getTestCacheSizes()}.
    */
   protected abstract Map<MediumReference, Integer> getOverlappingRegions();

   /**
    * Returns the cache regions to be used for testing {@link MediumCache#buffer} and
    * {@link MediumCache#getBufferedByteCountAt}. The {@link StandardMediumReference} keys must be stored in order from
    * lowest to highest offset in the map.
    * 
    * It should be regions included that overlap each other.
    * 
    * These regions must be cacheable without {@link EndOfMediumException}.
    * 
    * @return the cache regions to be used for testing {@link MediumCache#buffer} and
    *         {@link MediumCache#getBufferedByteCountAt}.
    */
   protected abstract LinkedHashMap<MediumReference, Integer> getTestCacheSizes();

   /**
    * Returns an {@link MediumCache} implementation for test.
    * 
    * @return an {@link MediumCache} implementation for test.
    */
   protected abstract MediumCache getTestling();

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {

      checkTestData();
   }

   /**
    * Tears down the test fixtures.
    */
   @After
   public void tearDown() {

      getTestling().close();
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} for following boundary case: A new region to be cached
    * overlaps an existing region at the beginning for about 1 byte. => EXPECTED: The existing region remains unchanged.
    * The new region is trimmed at its back by one byte, making it end one byte earlier.
    */
   @Test
   public void test_cacheBoundaryCase1_overlapStart() {

      MediumCache cache = getTestling();

      MediumReference existingRegionReference = null;
      MediumReference newRegionReference = null;

      try {
         // Several other, for asserts as such unimportant region to test
         // behavior with more than just one existing region
         addCacheRegion(cache, 100, 4);
         addCacheRegion(cache, 105, 4);
         addCacheRegion(cache, 110, 4);
         addCacheRegion(cache, 115, 4);

         // A new region to be cached overlaps an existing region at the
         // beginning for about 1 byte.
         existingRegionReference = new StandardMediumReference(getExpectedMedium(), 5);
         final int existingRegionSize = 20;
         cache.buffer(existingRegionReference, existingRegionSize);
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         newRegionReference = new StandardMediumReference(getExpectedMedium(), 2);
         final int newRegionSize = 4;
         cache.buffer(newRegionReference, newRegionSize);

         // Existing region is cached
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));
         // Both together are cached
         Assert.assertEquals(existingRegionSize + newRegionSize - 1, cache.getBufferedByteCountAt(newRegionReference));

         // Check that the correct data is contained in the cache
         checkReadDataBoundaryCases(cache.getData(existingRegionReference, existingRegionSize),
            existingRegionReference);
         checkReadDataBoundaryCases(cache.getData(newRegionReference, newRegionSize), newRegionReference);

         // Reference to new region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(newRegionReference));
         Assert.assertEquals(new Integer(newRegionSize - 1), cache.getBufferedRegions().get(newRegionReference));
         // Reference to existing region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(existingRegionReference));
         Assert.assertEquals(new Integer(existingRegionSize), cache.getBufferedRegions().get(existingRegionReference));
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} for following boundary case: A new region to be cached
    * exactly covers the same portion of the medium as an existing cache region (equal size). => EXPECTED: The existing
    * region remains unchanged, the new region is ignored. From the outside point-of-view, this cannot be distinguished
    * from the behavior that the new region is cached while the existing one is dropped.
    */
   @Test
   public void test_cacheBoundaryCase10_equalSize() {

      MediumCache cache = getTestling();

      StandardMediumReference existingRegionReference = null;
      StandardMediumReference newRegionReference = null;

      try {
         // Several other, for asserts as such unimportant region to test
         // behavior with more than just one existing region
         addCacheRegion(cache, 100, 4);
         addCacheRegion(cache, 105, 4);
         addCacheRegion(cache, 110, 4);
         addCacheRegion(cache, 115, 4);

         // A new region to be cached overlaps an existing region at the
         // beginning for about 1 byte.
         existingRegionReference = new StandardMediumReference(getExpectedMedium(), 5);
         final int existingRegionSize = 20;
         cache.buffer(existingRegionReference, existingRegionSize);
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         newRegionReference = new StandardMediumReference(getExpectedMedium(), 5);
         final int newRegionSize = existingRegionSize;
         cache.buffer(newRegionReference, newRegionSize);

         // Existing region is cached
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));
         // New region is cached
         Assert.assertEquals(newRegionSize, cache.getBufferedByteCountAt(newRegionReference));

         // No bytes are cached around the existing and new region
         StandardMediumReference refBeforeRegion = new StandardMediumReference(getExpectedMedium(),
            newRegionReference.getAbsoluteMediumOffset() - 1);
         Assert.assertEquals(0, cache.getBufferedByteCountAt(refBeforeRegion));
         Assert.assertEquals(0, cache.getBufferedByteCountAt(newRegionReference.advance(newRegionSize)));

         // Check that the correct data is contained in the cache
         checkReadDataBoundaryCases(cache.getData(existingRegionReference, existingRegionSize),
            existingRegionReference);
         checkReadDataBoundaryCases(cache.getData(newRegionReference, newRegionSize), newRegionReference);

         // Reference to new region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(newRegionReference));
         Assert.assertEquals(new Integer(newRegionSize), cache.getBufferedRegions().get(newRegionReference));
         // Reference to existing region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(existingRegionReference));
         Assert.assertEquals(new Integer(newRegionSize), cache.getBufferedRegions().get(newRegionReference));
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} for following boundary case: A new region to be cached
    * overlaps an existing region at the end for about 1 byte. => EXPECTED: The existing region remains unchanged. The
    * new region is trimmed at its start by one byte, making it start one byte later.
    */
   @Test
   public void test_cacheBoundaryCase2_overlapEnd() {

      MediumCache cache = getTestling();

      StandardMediumReference existingRegionReference = null;
      StandardMediumReference newRegionReference = null;

      try {
         // Several other, for asserts as such unimportant region to test
         // behavior with more than just one existing region
         addCacheRegion(cache, 100, 4);
         addCacheRegion(cache, 105, 4);
         addCacheRegion(cache, 110, 4);
         addCacheRegion(cache, 115, 4);

         existingRegionReference = new StandardMediumReference(getExpectedMedium(), 5);
         final int existingRegionSize = 20;
         cache.buffer(existingRegionReference, existingRegionSize);
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         newRegionReference = new StandardMediumReference(getExpectedMedium(),
            existingRegionReference.getAbsoluteMediumOffset() + existingRegionSize - 1);
         final int newRegionSize = 4;
         cache.buffer(newRegionReference, newRegionSize);

         // New region is cached
         Assert.assertEquals(newRegionSize, cache.getBufferedByteCountAt(newRegionReference));
         // Both together are cached
         Assert.assertEquals(existingRegionSize + newRegionSize - 1,
            cache.getBufferedByteCountAt(existingRegionReference));

         // Check that the correct data is contained in the cache
         checkReadDataBoundaryCases(cache.getData(existingRegionReference, existingRegionSize),
            existingRegionReference);
         checkReadDataBoundaryCases(cache.getData(newRegionReference, newRegionSize), newRegionReference);

         // Existing region is unchanged
         Assert.assertTrue(cache.getBufferedRegions().containsKey(existingRegionReference));
         Assert.assertEquals(new Integer(existingRegionSize), cache.getBufferedRegions().get(existingRegionReference));

         // The new region has been shifted to start 1 byte later
         Assert.assertFalse(cache.getBufferedRegions().containsKey(newRegionReference));
         final MediumReference shiftedNewRegionReference = newRegionReference.advance(1);
         Assert.assertTrue(cache.getBufferedRegions().containsKey(shiftedNewRegionReference));
         Assert.assertEquals(new Integer(newRegionSize - 1), cache.getBufferedRegions().get(shiftedNewRegionReference));
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} for following boundary case: A new region to be cached
    * ends exactly at the byte before another already cached region. => EXPECTED: The new region is completely cached,
    * the existing region remains unchanged.
    */
   @Test
   public void test_cacheBoundaryCase3_borderingBack() {

      MediumCache cache = getTestling();

      StandardMediumReference existingRegionReference = null;
      StandardMediumReference newRegionReference = null;

      try {
         // Several other, for asserts as such unimportant region to test
         // behavior with more than just one existing region
         addCacheRegion(cache, 100, 4);
         addCacheRegion(cache, 105, 4);
         addCacheRegion(cache, 110, 4);
         addCacheRegion(cache, 115, 4);

         // A new region to be cached overlaps an existing region at the
         // beginning for about 1 byte.
         existingRegionReference = new StandardMediumReference(getExpectedMedium(), 5);

         final int existingRegionSize = 20;
         cache.buffer(existingRegionReference, existingRegionSize);
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         newRegionReference = new StandardMediumReference(getExpectedMedium(), 2);
         final int newRegionSize = 3;
         cache.buffer(newRegionReference, newRegionSize);

         // Existing region is cached
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));
         // Both together are cached
         Assert.assertEquals(existingRegionSize + newRegionSize, cache.getBufferedByteCountAt(newRegionReference));

         // Check that the correct data is contained in the cache
         checkReadDataBoundaryCases(cache.getData(existingRegionReference, existingRegionSize),
            existingRegionReference);
         checkReadDataBoundaryCases(cache.getData(newRegionReference, newRegionSize), newRegionReference);

         // Reference to new and existing region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(newRegionReference));
         Assert.assertEquals(new Integer(newRegionSize), cache.getBufferedRegions().get(newRegionReference));
         Assert.assertTrue(cache.getBufferedRegions().containsKey(existingRegionReference));
         Assert.assertEquals(new Integer(existingRegionSize), cache.getBufferedRegions().get(existingRegionReference));
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} for following boundary case: A new region to be cached
    * ends exactly at the byte before another already cached region. => EXPECTED: The new region is completely cached,
    * the existing region remains unchanged.
    */
   @Test
   public void test_cacheBoundaryCase4_borderingFront() {

      MediumCache cache = getTestling();

      StandardMediumReference existingRegionReference = null;
      StandardMediumReference newRegionReference = null;

      try {
         // Several other, for asserts as such unimportant region to test
         // behavior with more than just one existing region
         addCacheRegion(cache, 100, 4);
         addCacheRegion(cache, 105, 4);
         addCacheRegion(cache, 110, 4);
         addCacheRegion(cache, 115, 4);

         // A new region to be cached overlaps an existing region at the
         // beginning for about 1 byte.
         existingRegionReference = new StandardMediumReference(getExpectedMedium(), 5);
         final int existingRegionSize = 20;
         cache.buffer(existingRegionReference, existingRegionSize);
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         newRegionReference = new StandardMediumReference(getExpectedMedium(),
            existingRegionReference.getAbsoluteMediumOffset() + existingRegionSize);
         final int newRegionSize = 4;
         cache.buffer(newRegionReference, newRegionSize);

         // Existing region is cached
         Assert.assertTrue(cache.getBufferedByteCountAt(existingRegionReference) > existingRegionSize);
         // Both together are cached
         Assert.assertEquals(existingRegionSize + newRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         // Check that the correct data is contained in the cache
         checkReadDataBoundaryCases(cache.getData(existingRegionReference, existingRegionSize),
            existingRegionReference);
         checkReadDataBoundaryCases(cache.getData(newRegionReference, newRegionSize), newRegionReference);

         // Reference to new and existing region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(newRegionReference));
         Assert.assertEquals(new Integer(newRegionSize), cache.getBufferedRegions().get(newRegionReference));
         Assert.assertTrue(cache.getBufferedRegions().containsKey(existingRegionReference));
         Assert.assertEquals(new Integer(existingRegionSize), cache.getBufferedRegions().get(existingRegionReference));
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} for following boundary case: A new region to be cached
    * is completely covered by an already cached region. => EXPECTED: The existing region remains unchanged, nothing at
    * all changes in the cache.
    */
   @Test
   public void test_cacheBoundaryCase5_alreadyCached() {

      MediumCache cache = getTestling();

      StandardMediumReference existingRegionReference = null;
      StandardMediumReference newRegionReference = null;

      try {
         // Several other, for asserts as such unimportant region to test
         // behavior with more than just one existing region
         addCacheRegion(cache, 100, 4);
         addCacheRegion(cache, 105, 4);
         addCacheRegion(cache, 110, 4);
         addCacheRegion(cache, 115, 4);

         // A new region to be cached overlaps an existing region at the
         // beginning for about 1 byte.
         existingRegionReference = new StandardMediumReference(getExpectedMedium(), 5);
         final int existingRegionSize = 20;
         cache.buffer(existingRegionReference, existingRegionSize);
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         newRegionReference = new StandardMediumReference(getExpectedMedium(),
            existingRegionReference.getAbsoluteMediumOffset() + 3);
         final int newRegionSize = 5;
         cache.buffer(newRegionReference, newRegionSize);

         // Existing region is cached
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));
         // New region is cached
         Assert.assertEquals(existingRegionSize - 3, cache.getBufferedByteCountAt(newRegionReference));

         // Check that the correct data is contained in the cache
         checkReadDataBoundaryCases(cache.getData(existingRegionReference, existingRegionSize),
            existingRegionReference);
         checkReadDataBoundaryCases(cache.getData(newRegionReference, newRegionSize), newRegionReference);

         // Existing region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(existingRegionReference));
         Assert.assertEquals(new Integer(existingRegionSize), cache.getBufferedRegions().get(existingRegionReference));

         // New region is not in returned cache regions
         Assert.assertFalse(cache.getBufferedRegions().containsKey(newRegionReference));
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} for following boundary case: A new region to be cached
    * contains an already cached region completely, the start byte of the already cached region is behind the new region
    * start byte AND its end byte is before the end byte of the new region. => EXPECTED: The new region is completely
    * cached, the existing region is dropped.
    */
   @Test
   public void test_cacheBoundaryCase6_dropExisting() {

      MediumCache cache = getTestling();

      StandardMediumReference existingRegionReference = null;
      StandardMediumReference newRegionReference = null;

      try {
         // Several other, for asserts as such unimportant region to test
         // behavior with more than just one existing region
         addCacheRegion(cache, 100, 4);
         addCacheRegion(cache, 105, 4);
         addCacheRegion(cache, 110, 4);
         addCacheRegion(cache, 115, 4);

         // A new region to be cached overlaps an existing region at the
         // beginning for about 1 byte.
         existingRegionReference = new StandardMediumReference(getExpectedMedium(), 5);
         final int existingRegionSize = 20;
         cache.buffer(existingRegionReference, existingRegionSize);
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         newRegionReference = new StandardMediumReference(getExpectedMedium(),
            existingRegionReference.getAbsoluteMediumOffset() - 2);
         final int newRegionSize = existingRegionSize + 4;
         cache.buffer(newRegionReference, newRegionSize);

         // New region is cached
         Assert.assertEquals(newRegionSize, cache.getBufferedByteCountAt(newRegionReference));
         // Existing region is cached
         Assert.assertTrue(cache.getBufferedByteCountAt(existingRegionReference) > existingRegionSize);

         // Check that the correct data is contained in the cache
         checkReadDataBoundaryCases(cache.getData(existingRegionReference, existingRegionSize),
            existingRegionReference);
         checkReadDataBoundaryCases(cache.getData(newRegionReference, newRegionSize), newRegionReference);

         // Reference to new region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(newRegionReference));

         if (getMaximumCacheSize() >= newRegionSize)
            Assert.assertEquals(new Integer(newRegionSize), cache.getBufferedRegions().get(newRegionReference));

         else
            Assert.assertEquals(new Integer(getMaximumCacheSize()), cache.getBufferedRegions().get(newRegionReference));

         // Reference to existing region is not in returned cache regions
         Assert.assertFalse(cache.getBufferedRegions().containsKey(existingRegionReference));
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} for following boundary case: A new region to be cached
    * contains an already cached region completely, the start byte of the already cached region is behind the new region
    * BUT its end byte is located at the same offset as the end byte of the existing region. => EXPECTED: The new region
    * is completely cached, the existing region is dropped.
    */
   @Test
   public void test_cacheBoundaryCase7_dropExistingStart() {

      MediumCache cache = getTestling();

      StandardMediumReference existingRegionReference = null;
      StandardMediumReference newRegionReference = null;

      try {
         // Several other, for asserts as such unimportant region to test
         // behavior with more than just one existing region
         addCacheRegion(cache, 100, 4);
         addCacheRegion(cache, 105, 4);
         addCacheRegion(cache, 110, 4);
         addCacheRegion(cache, 115, 4);

         // A new region to be cached overlaps an existing region at the
         // beginning for about 1 byte.
         existingRegionReference = new StandardMediumReference(getExpectedMedium(), 5);
         final int existingRegionSize = 20;
         cache.buffer(existingRegionReference, existingRegionSize);
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         newRegionReference = new StandardMediumReference(getExpectedMedium(),
            existingRegionReference.getAbsoluteMediumOffset() - 1);
         final int newRegionSize = existingRegionSize + 1;
         cache.buffer(newRegionReference, newRegionSize);

         // Existing region is cached
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));
         // New region is cached
         Assert.assertEquals(newRegionSize, cache.getBufferedByteCountAt(newRegionReference));

         // Check that the correct data is contained in the cache
         checkReadDataBoundaryCases(cache.getData(existingRegionReference, existingRegionSize),
            existingRegionReference);
         checkReadDataBoundaryCases(cache.getData(newRegionReference, newRegionSize), newRegionReference);

         // Reference to new region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(newRegionReference));

         if (getMaximumCacheSize() >= newRegionSize)
            Assert.assertEquals(new Integer(newRegionSize), cache.getBufferedRegions().get(newRegionReference));

         else
            Assert.assertEquals(new Integer(getMaximumCacheSize()), cache.getBufferedRegions().get(newRegionReference));

         // Reference to existing region is not in returned cache regions
         Assert.assertFalse(cache.getBufferedRegions().containsKey(existingRegionReference));
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} for following boundary case: A new region to be cached
    * contains an already cached region completely, the start byte of the already cached region is at the same offset as
    * the new region's start byte AND its end byte is located before the offset of the end byte of the new region. =>
    * EXPECTED: The new region is completely cached, the existing region is dropped.
    */
   @Test
   public void test_cacheBoundaryCase8_dropExistingEnd() {

      MediumCache cache = getTestling();

      StandardMediumReference existingRegionReference = null;
      StandardMediumReference newRegionReference = null;

      try {
         // Several other, for asserts as such unimportant region to test
         // behavior with more than just one existing region
         addCacheRegion(cache, 100, 4);
         addCacheRegion(cache, 105, 4);
         addCacheRegion(cache, 110, 4);
         addCacheRegion(cache, 115, 4);

         // A new region to be cached overlaps an existing region at the
         // beginning for about 1 byte.
         existingRegionReference = new StandardMediumReference(getExpectedMedium(), 5);
         final int existingRegionSize = 20;
         cache.buffer(existingRegionReference, existingRegionSize);
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         newRegionReference = existingRegionReference;
         final int newRegionSize = existingRegionSize + 1;
         cache.buffer(newRegionReference, newRegionSize);

         // Existing region is cached
         Assert.assertEquals(newRegionSize, cache.getBufferedByteCountAt(existingRegionReference));

         // Check that the correct data is contained in the cache
         checkReadDataBoundaryCases(cache.getData(existingRegionReference, existingRegionSize),
            existingRegionReference);
         checkReadDataBoundaryCases(cache.getData(newRegionReference, newRegionSize), newRegionReference);

         // Reference to new region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(newRegionReference));

         if (getMaximumCacheSize() >= newRegionSize)
            Assert.assertEquals(new Integer(newRegionSize), cache.getBufferedRegions().get(newRegionReference));

         else
            Assert.assertEquals(new Integer(getMaximumCacheSize()), cache.getBufferedRegions().get(newRegionReference));

         // Reference to existing region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(existingRegionReference));
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} for following boundary case: A new region to be cached
    * contains multiple already cached regions completely. => EXPECTED: The new region is completely cached, all of the
    * existing regions are dropped.
    */
   @Test
   public void test_cacheBoundaryCase9_dropExistingMultiple() {

      MediumCache cache = getTestling();

      StandardMediumReference existingRegionReference = null;
      StandardMediumReference newRegionReference = null;

      try {
         // Several other, for asserts as such unimportant region to test
         // behavior with more than just one existing region
         addCacheRegion(cache, 100, 4);
         addCacheRegion(cache, 105, 4);
         addCacheRegion(cache, 110, 4);
         addCacheRegion(cache, 115, 4);

         // A new region to be cached overlaps an existing region at the
         // beginning for about 1 byte.
         existingRegionReference = new StandardMediumReference(getExpectedMedium(), 5);
         final int existingRegionSize = 3;
         cache.buffer(existingRegionReference, existingRegionSize);
         Assert.assertEquals(existingRegionSize, cache.getBufferedByteCountAt(existingRegionReference));
         StandardMediumReference existingRegionReference2 = new StandardMediumReference(getExpectedMedium(), 9);
         final int existingRegionSize2 = 3;
         cache.buffer(existingRegionReference2, existingRegionSize2);
         Assert.assertEquals(existingRegionSize2, cache.getBufferedByteCountAt(existingRegionReference2));
         StandardMediumReference existingRegionReference3 = new StandardMediumReference(getExpectedMedium(), 12);
         final int existingRegionSize3 = 6;
         cache.buffer(existingRegionReference3, existingRegionSize3);
         Assert.assertEquals(existingRegionSize3, cache.getBufferedByteCountAt(existingRegionReference3));

         newRegionReference = new StandardMediumReference(getExpectedMedium(), 3);
         final int newRegionSize = 30;
         cache.buffer(newRegionReference, newRegionSize);

         // Existing region is cached
         Assert.assertEquals(newRegionSize, cache.getBufferedByteCountAt(newRegionReference));

         // Check that the correct data is contained in the cache
         checkReadDataBoundaryCases(cache.getData(existingRegionReference, existingRegionSize),
            existingRegionReference);
         checkReadDataBoundaryCases(cache.getData(existingRegionReference2, existingRegionSize2),
            existingRegionReference2);
         checkReadDataBoundaryCases(cache.getData(existingRegionReference3, existingRegionSize3),
            existingRegionReference3);
         checkReadDataBoundaryCases(cache.getData(newRegionReference, newRegionSize), newRegionReference);

         // Reference to new region is in returned cache regions
         Assert.assertTrue(cache.getBufferedRegions().containsKey(newRegionReference));

         // Reference to existing regions is not in returned cache regions
         Assert.assertFalse(cache.getBufferedRegions().containsKey(existingRegionReference));
         Assert.assertFalse(cache.getBufferedRegions().containsKey(existingRegionReference2));
         Assert.assertFalse(cache.getBufferedRegions().containsKey(existingRegionReference3));
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} and
    * {@link MediumCache#getBufferedByteCountAt(MediumReference)}.
    */
   @Test
   public void test_cacheGetCachedByteCountAt() {

      final Map<MediumReference, Integer> testCacheSizes = getTestCacheSizes();

      MediumCache testling = getTestling();

      // At first, the cached regions are not at all cached
      for (Iterator<MediumReference> iterator = testCacheSizes.keySet().iterator(); iterator.hasNext();) {
         MediumReference reference = iterator.next();

         Assert.assertEquals(0, testling.getBufferedByteCountAt(reference));
      }

      try {
         // Test cached regions
         checkCache(testCacheSizes, testling);
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }

      checkNoOverlappingRegions(testling);
   }

   /**
    * Tests {@link MediumCache#buffer} and {@link MediumCache#getBufferedByteCountAt} if the size to cache exceeds the
    * current medium length and therefore must cause an {@link EndOfMediumException}.
    */
   @Test
   public void test_cacheIsCachedEndOfMedium() {

      MediumCache cache = getTestling();

      final Map<MediumReference, Integer> distancesToEOM = getMediumReferencesAndDistToEOM();

      // At first, the cached regions are not at all cached
      for (Iterator<MediumReference> iterator = distancesToEOM.keySet().iterator(); iterator.hasNext();) {
         MediumReference reference = iterator.next();

         Assert.assertEquals(0, cache.getBufferedByteCountAt(reference));
      }

      // Caching creates an EndOfMediumExeption, but all bytes up to end of
      // medium are
      // cached nevertheless
      for (Iterator<MediumReference> iterator = distancesToEOM.keySet().iterator(); iterator.hasNext();) {
         MediumReference reference = iterator.next();
         long size = distancesToEOM.get(reference);

         try {
            cache.buffer(reference, (int) size * 2);
            Assert.fail("End of medium is expected here!");
         }

         catch (EndOfMediumException e) {
            Assert.assertEquals(size, e.getBytesReallyRead());
            Assert.assertEquals(size, cache.getBufferedByteCountAt(reference));
            Assert.assertEquals(size * 2, e.getByteCountTriedToRead());
            Assert.assertEquals(reference, e.getMediumReference());
         }
      }
   }

   /**
    * Tests {@link MediumCache#buffer(MediumReference, long)} and {@link MediumCache#getBufferedByteCountAt} in case
    * of overlapping, larger regions.
    */
   @Test
   public void test_cacheIsCachedOverlapping() {

      final Map<MediumReference, Integer> testCacheSizes = getTestCacheSizes();

      MediumCache testling = getTestling();

      try {
         // Cache the regions
         for (Iterator<MediumReference> iterator = testCacheSizes.keySet().iterator(); iterator.hasNext();) {
            MediumReference reference = iterator.next();
            long size = testCacheSizes.get(reference);

            testling.buffer(reference, (int) size);
         }

         final Map<MediumReference, Integer> overlappingRegions = getOverlappingRegions();

         // Check caching of larger composed regions
         for (Iterator<MediumReference> iterator = overlappingRegions.keySet().iterator(); iterator.hasNext();) {
            MediumReference composedReference = iterator.next();
            long composedSize = overlappingRegions.get(composedReference);

            Assert.assertEquals(composedSize, testling.getBufferedByteCountAt(composedReference));
            // Cache the composed region again
            testling.buffer(composedReference, (int) composedSize);
            Assert.assertEquals(composedSize, testling.getBufferedByteCountAt(composedReference));
         }

         // Previously cached regions must still be cached
         checkCache(testCacheSizes, testling);
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }

      checkNoOverlappingRegions(testling);
   }

   /**
    * Tests {@link MediumCache#discard(MediumReference, long)} with non-consecutive and non overlapping regions.
    */
   @Test
   public void test_free() {

      final Map<MediumReference, Integer> testCacheSizes = getTestCacheSizes();
      final Map<MediumReference, byte[]> expectedData = getExpectedData();

      MediumCache testling = getTestling();

      for (Iterator<MediumReference> iterator = testCacheSizes.keySet().iterator(); iterator.hasNext();) {
         MediumReference reference = iterator.next();
         int size = testCacheSizes.get(reference);

         // Calls to free with a size of 0 have no effect
         testling.discard(reference, 0);

         // Calls to free with a non-cached reference have no effect
         testling.discard(reference, size);

         // Cache the data
         try {
            testling.buffer(reference, size);
            checkCache(testCacheSizes, testling);
         } catch (EndOfMediumException e) {
            Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
         }

         try {
            // Read the data
            ByteBuffer bytesReadAfterCaching = testling.getData(reference, size);

            checkReadData(bytesReadAfterCaching, expectedData.get(reference));

            Assert.assertNotNull(bytesReadAfterCaching);
            Assert.assertEquals(size, bytesReadAfterCaching.remaining());

            // Free the data
            testling.discard(reference, size);

            Assert.assertEquals(0, testling.getBufferedByteCountAt(reference));

            // Read the data again, now without it being cached
            bytesReadAfterCaching = testling.getData(reference, size);

            checkReadData(bytesReadAfterCaching, expectedData.get(reference));

            Assert.assertNotNull(bytesReadAfterCaching);
            Assert.assertEquals(size, bytesReadAfterCaching.remaining());
         } catch (EndOfMediumException e) {
            Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
         }
      }
   }

   /**
    * Tests {@link MediumCache#discard(MediumReference, long)} with multiple consecutive regions freed at once. The
    * {@link StandardMediumReference} given to free is exactly the start reference of an existing region.
    */
   @Test
   public void test_freeBoundaryCase1_consecutiveRegions() {

      MediumCache testling = getTestling();

      try {
         // These two regions are consecutive (120 bytes)
         addCacheRegion(testling, FREE_REGION_1_START, FREE_REGION_1_SIZE);
         addCacheRegion(testling, FREE_REGION_2_START, FREE_REGION_2_SIZE);

         // This region starts 5 bytes behind the previous one
         addCacheRegion(testling, FREE_REGION_3_START, FREE_REGION_3_SIZE);
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }

      final StandardMediumReference FREE_REGION_1_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_1_START);
      final StandardMediumReference FREE_REGION_2_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_2_START);
      final StandardMediumReference FREE_REGION_3_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_3_START);

      testling.discard(FREE_REGION_1_START_REF, FREE_REGION_1_SIZE + FREE_REGION_2_SIZE);

      Assert.assertEquals(0, testling.getBufferedByteCountAt(FREE_REGION_1_START_REF));
      Assert.assertEquals(0, testling.getBufferedByteCountAt(FREE_REGION_2_START_REF));
      Assert.assertEquals(FREE_REGION_3_SIZE, testling.getBufferedByteCountAt(FREE_REGION_3_START_REF));
   }

   /**
    * Tests {@link MediumCache#discard(MediumReference, long)} with multiple consecutive regions freed at once, while
    * another consecutive regions follows it, but stays untouched due to the free size is only up to the second
    * consecutive region. The last consecutive region must NOT get freed. The {@link StandardMediumReference} given to
    * free is exactly the start reference of an existing region.
    */
   @Test
   public void test_freeBoundaryCase2_consecutiveRegionsOneUntouched() {

      MediumCache testling = getTestling();

      try {
         // These three regions are consecutive (123 bytes)
         addCacheRegion(testling, FREE_REGION_1_START, FREE_REGION_1_SIZE);
         addCacheRegion(testling, FREE_REGION_2_START, FREE_REGION_2_SIZE);
         addCacheRegion(testling, FREE_REGION_2A_START, FREE_REGION_2A_SIZE);

         // This region starts 5 bytes behind the previous one
         addCacheRegion(testling, FREE_REGION_3_START, FREE_REGION_3_SIZE);
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }

      final StandardMediumReference FREE_REGION_1_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_1_START);
      final StandardMediumReference FREE_REGION_2_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_2_START);
      final StandardMediumReference FREE_REGION_2A_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_2A_START);
      final StandardMediumReference FREE_REGION_3_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_3_START);

      testling.discard(FREE_REGION_1_START_REF, FREE_REGION_1_SIZE + FREE_REGION_2_SIZE);

      Assert.assertEquals(0, testling.getBufferedByteCountAt(FREE_REGION_1_START_REF));
      Assert.assertEquals(0, testling.getBufferedByteCountAt(FREE_REGION_2_START_REF));
      Assert.assertEquals(FREE_REGION_2A_SIZE, testling.getBufferedByteCountAt(FREE_REGION_2A_START_REF));
      Assert.assertEquals(FREE_REGION_3_SIZE, testling.getBufferedByteCountAt(FREE_REGION_3_START_REF));
   }

   /**
    * Tests {@link MediumCache#discard(MediumReference, long)} with multiple consecutive regions freed at once, while
    * a bigger free size is specified, exceeding the consecutive regions and reaching up to another, non-consecutive
    * region. Only the consecutive regions must get freed. The {@link StandardMediumReference} given to free is exactly
    * the start reference of an existing region.
    */
   @Test
   public void test_freeBoundaryCase3_consecutiveRegionsBiggerSize() {

      MediumCache testling = getTestling();

      try {
         // These two regions are consecutive (120 bytes)
         addCacheRegion(testling, FREE_REGION_1_START, FREE_REGION_1_SIZE);
         addCacheRegion(testling, FREE_REGION_2_START, FREE_REGION_2_SIZE);
         addCacheRegion(testling, FREE_REGION_2A_START, FREE_REGION_2A_SIZE);

         // This region starts 5 bytes behind the previous one
         addCacheRegion(testling, FREE_REGION_3_START, FREE_REGION_3_SIZE);
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }

      final StandardMediumReference FREE_REGION_1_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_1_START);
      final StandardMediumReference FREE_REGION_2_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_2_START);
      final StandardMediumReference FREE_REGION_2A_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_2A_START);
      final StandardMediumReference FREE_REGION_3_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_3_START);

      testling.discard(FREE_REGION_1_START_REF, FREE_REGION_1_SIZE + FREE_REGION_2_SIZE + FREE_REGION_2A_SIZE + 10);

      // Still, only the regions themselves are freed
      Assert.assertEquals(0, testling.getBufferedByteCountAt(FREE_REGION_1_START_REF));
      Assert.assertEquals(0, testling.getBufferedByteCountAt(FREE_REGION_2_START_REF));
      Assert.assertEquals(0, testling.getBufferedByteCountAt(FREE_REGION_2A_START_REF));
      Assert.assertEquals(FREE_REGION_3_SIZE, testling.getBufferedByteCountAt(FREE_REGION_3_START_REF));
   }

   /**
    * Tests {@link MediumCache#discard(MediumReference, long)} with multiple consecutive regions freed at once, while
    * the start {@link StandardMediumReference} overlaps the end of an existing region.
    */
   @Test
   public void test_freeBoundaryCase4_overlapEndOfRegion() {

      MediumCache testling = getTestling();

      try {
         // These two regions are consecutive (120 bytes)
         addCacheRegion(testling, FREE_REGION_1_START, FREE_REGION_1_SIZE);
         addCacheRegion(testling, FREE_REGION_2_START, FREE_REGION_2_SIZE);
         addCacheRegion(testling, FREE_REGION_2A_START, FREE_REGION_2A_SIZE);

         // This region starts 5 bytes behind the previous one
         addCacheRegion(testling, FREE_REGION_3_START, FREE_REGION_3_SIZE);
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }

      final StandardMediumReference FREE_REGION_1_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_1_START);
      final StandardMediumReference FREE_REGION_2_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_2_START);
      final StandardMediumReference FREE_REGION_2A_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_2A_START);
      final StandardMediumReference FREE_REGION_3_START_REF = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_3_START);

      final int freeStartOverlapSize = 10;
      final MediumReference freeStartReference = FREE_REGION_1_START_REF.advance(freeStartOverlapSize);
      testling.discard(freeStartReference, FREE_REGION_1_SIZE + FREE_REGION_2_SIZE + FREE_REGION_2A_SIZE + 10);

      // A region is still cached at the region 1 start offset
      Assert.assertEquals(freeStartOverlapSize, testling.getBufferedByteCountAt(FREE_REGION_1_START_REF));
      // Nothing anymore cached behind
      Assert.assertEquals(0, testling.getBufferedByteCountAt(freeStartReference));
      Assert.assertEquals(0, testling.getBufferedByteCountAt(FREE_REGION_2_START_REF));
      Assert.assertEquals(0, testling.getBufferedByteCountAt(FREE_REGION_2A_START_REF));
      Assert.assertEquals(FREE_REGION_3_SIZE, testling.getBufferedByteCountAt(FREE_REGION_3_START_REF));
   }

   /**
    * Tests {@link MediumCache#discard(MediumReference, long)} with multiple consecutive regions freed at once, while
    * the start {@link StandardMediumReference} overlaps the end of an existing region and the end of the portion to
    * three overlaps the start of an existing region.
    */
   @Test
   public void test_freeBoundaryCase5_overlapStartOfRegion() {

      MediumCache testling = getTestling();

      try {
         // These two regions are consecutive (120 bytes)
         addCacheRegion(testling, FREE_REGION_1_START, FREE_REGION_1_SIZE);
         addCacheRegion(testling, FREE_REGION_2_START, FREE_REGION_2_SIZE);
         addCacheRegion(testling, FREE_REGION_2A_START, FREE_REGION_2A_SIZE);

         // This region starts 5 bytes behind the previous one
         addCacheRegion(testling, FREE_REGION_3_START, FREE_REGION_3_SIZE);
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }

      final StandardMediumReference freeRegion1StartRef = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_1_START);
      final StandardMediumReference freeRegion2StartRef = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_2_START);
      final StandardMediumReference freeRegion2aStartRef = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_2A_START);
      final StandardMediumReference freeRegion3StartRef = new StandardMediumReference(getExpectedMedium(),
         FREE_REGION_3_START);

      final int freeStartOverlapSize = 10;
      final int freeEndOverlapSize = 5;
      final MediumReference freeStartReference = freeRegion1StartRef.advance(freeStartOverlapSize);
      testling.discard(freeStartReference,
         FREE_REGION_1_SIZE + FREE_REGION_2_SIZE - freeStartOverlapSize - freeEndOverlapSize);

      // A region is still cached at the region 1 start offset
      Assert.assertEquals(freeStartOverlapSize, testling.getBufferedByteCountAt(freeRegion1StartRef));
      // Nothing anymore cached behind
      Assert.assertEquals(0, testling.getBufferedByteCountAt(freeStartReference));
      Assert.assertEquals(0, testling.getBufferedByteCountAt(freeRegion2StartRef));
      // A part of region 2 is still cached
      Assert.assertEquals(freeEndOverlapSize + FREE_REGION_2A_SIZE,
         testling.getBufferedByteCountAt(freeRegion2StartRef.advance(FREE_REGION_2_SIZE - freeEndOverlapSize)));
      Assert.assertEquals(FREE_REGION_2A_SIZE, testling.getBufferedByteCountAt(freeRegion2aStartRef));
      Assert.assertEquals(FREE_REGION_3_SIZE, testling.getBufferedByteCountAt(freeRegion3StartRef));
   }

   /**
    * Tests {@link MediumCache#getData}.
    */
   @Test
   public void test_getData() {

      final Map<MediumReference, Integer> testCacheSizes = getTestCacheSizes();
      final Map<MediumReference, byte[]> expectedData = getExpectedData();

      MediumCache testling = getTestling();

      for (Iterator<MediumReference> iterator = testCacheSizes.keySet().iterator(); iterator.hasNext();) {
         MediumReference reference = iterator.next();
         int size = testCacheSizes.get(reference);

         // Read data without prior caching (except for totally overlapping
         // regions)
         ByteBuffer bytesReadBeforeCaching;
         try {
            bytesReadBeforeCaching = testling.getData(reference, size);

            Assert.assertNotNull(bytesReadBeforeCaching);
            Assert.assertEquals(size, bytesReadBeforeCaching.remaining());

            checkReadData(bytesReadBeforeCaching, expectedData.get(reference));

            try {
               testling.buffer(reference, size);
            } catch (EndOfMediumException e) {
               Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
            }

            try {
               ByteBuffer bytesReadAfterCaching = testling.getData(reference, size);

               checkReadData(bytesReadAfterCaching, expectedData.get(reference));

               Assert.assertNotNull(bytesReadAfterCaching);
               Assert.assertEquals(size, bytesReadAfterCaching.remaining());

               Assert.assertEquals(bytesReadBeforeCaching, bytesReadAfterCaching);
            } catch (EndOfMediumException e) {
               Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
            }
         } catch (EndOfMediumException e1) {
            Assert.fail(UNEXPECTED_END_OF_MEDIUM + e1);
         }
      }
   }

   /**
    * Tests {@link MediumCache#getData} when end of medium conditions occur.
    */
   @Test
   public void test_getDataEndOfMedium() {

      final Map<MediumReference, Integer> testCacheSizes = getMediumReferencesAndDistToEOM();

      MediumCache testling = getTestling();

      for (Iterator<MediumReference> iterator = testCacheSizes.keySet().iterator(); iterator.hasNext();) {
         MediumReference reference = iterator.next();
         int size = testCacheSizes.get(reference);

         // Read data without prior caching, in EOM case
         try {
            testling.getData(reference, size * 2);

            Assert.fail("End of medium is expected here!");
         }

         catch (Exception e) {
            Assert.assertNotNull("Exception as expected", e);
         }
      }
   }

   /**
    * Tests {@link MediumCache#getData} in case of reading across overlapping cached regions.
    */
   @Test
   public void test_getDataOverlapping() {

      final Map<MediumReference, Integer> testCacheSizes = getTestCacheSizes();

      MediumCache testling = getTestling();

      try {
         // Cache the regions
         for (Iterator<MediumReference> iterator = testCacheSizes.keySet().iterator(); iterator.hasNext();) {
            MediumReference reference = iterator.next();
            long size = testCacheSizes.get(reference);

            testling.buffer(reference, (int) size);
         }
      } catch (EndOfMediumException e) {
         Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
      }

      final Map<MediumReference, Integer> overlappingRegions = getOverlappingRegions();

      // Check retrieving the data from larger composed regions
      for (Iterator<MediumReference> iterator = overlappingRegions.keySet().iterator(); iterator.hasNext();) {
         MediumReference composedReference = iterator.next();
         int composedSize = overlappingRegions.get(composedReference);

         Assert.assertEquals(composedSize, testling.getBufferedByteCountAt(composedReference));

         // Get the data from the composed region
         ByteBuffer data;
         try {
            data = testling.getData(composedReference, composedSize);
            Assert.assertEquals(composedSize, data.remaining());
         } catch (EndOfMediumException e) {
            Assert.fail(UNEXPECTED_END_OF_MEDIUM + e);
         }
      }
   }

   /**
    * Tests {@link MediumCache#getMedium}.
    */
   @Test
   public void test_getMedium() {

      Assert.assertEquals(getTestling().getMedium(), getExpectedMedium());
   }
}
