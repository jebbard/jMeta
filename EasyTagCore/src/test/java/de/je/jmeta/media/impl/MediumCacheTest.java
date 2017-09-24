package de.je.jmeta.media.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.datatype.InMemoryMedium;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.PreconditionUnfullfilledException;
import de.je.util.javautil.common.err.Reject;
import junit.framework.Assert;

class TestCacheLayout {

   private final long startOffset;
   private final List<Integer> regionSizes = new ArrayList<>();
   private final List<Integer> gapsAfterRegion = new ArrayList<>();
   private final IMedium<?> regionMedium;
   private long totalRegionSizeInBytes;
   private final long maxCacheSize;
   private final int maxCacheRegionSize;

   public long getStartOffset() {
      return startOffset;
   }

   public TestCacheLayout(long startOffset, IMedium<?> regionMedium, long maxCacheSize, int maxCacheRegionSize) {
      Reject.ifNull(regionMedium, "regionMedium");
      Reject.ifNegative(startOffset, "startOffset");

      this.startOffset = startOffset;
      this.regionMedium = regionMedium;
      this.maxCacheSize = maxCacheSize;
      this.maxCacheRegionSize = maxCacheRegionSize;
   }

   public void addNextRegionInfo(int regionSize, int gapAfterRegion) {
      Reject.ifNegativeOrZero(regionSize, "regionSize");
      Reject.ifNegative(gapAfterRegion, "gapAfterRegion");

      regionSizes.add(regionSize);
      gapsAfterRegion.add(gapAfterRegion);
      totalRegionSizeInBytes += regionSize;
   }

   public List<MediumRegion> getAllCachedRegionsWithGaps() {
      return buildRegionList(true);
   }

   public List<MediumRegion> getAllCachedRegions() {
      return buildRegionList(false);
   }

   private List<MediumRegion> buildRegionList(boolean withGaps) {
      List<MediumRegion> resultList = new ArrayList<>(regionSizes.size());

      long currentOffset = startOffset;

      for (int i = 0; i < regionSizes.size(); ++i) {
         Integer currentRegionSize = regionSizes.get(i);
         Integer currentGapAfterRegion = gapsAfterRegion.get(i);

         resultList.add(createCachedMediumRegion(currentOffset, currentRegionSize));

         if (withGaps && currentGapAfterRegion > 0) {
            // Also build a non-cached gap region
            resultList.add(createUnCachedMediumRegion(currentOffset + currentRegionSize, currentGapAfterRegion));
         }

         currentOffset += currentRegionSize + currentGapAfterRegion;
      }

      return resultList;
   }

   private MediumRegion createCachedMediumRegion(long offset, Integer size) {
      return new MediumRegion(new StandardMediumReference(regionMedium, offset), createRegionContent(offset, size));
   }

   private MediumRegion createUnCachedMediumRegion(long offset, Integer size) {
      return new MediumRegion(new StandardMediumReference(regionMedium, offset), size);
   }

   public MediumCache buildCache() {
      List<MediumRegion> regionList = getAllCachedRegions();

      MediumCache resultingCache = new MediumCache(regionMedium, maxCacheSize, maxCacheRegionSize);

      for (MediumRegion mediumRegion : regionList) {
         resultingCache.addRegion(mediumRegion);
      }

      return resultingCache;
   }

   public long getTotalRegionSizeInBytes() {
      return totalRegionSizeInBytes;
   }

   private static ByteBuffer createRegionContent(long offset, int size) {
      byte[] content = new byte[size];

      for (int i = 0; i < content.length; i++) {
         content[i] = (byte) ((offset + i) % Byte.MAX_VALUE);
      }

      return ByteBuffer.wrap(content);
   }
}

/**
 * This class tests the {@link MediumCache} class.
 */
public class MediumCacheTest {

   private static final FileMedium MEDIUM = new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, true);

   private static final InMemoryMedium UNRELATED_MEDIUM = new InMemoryMedium(new byte[] {}, "Fake", false);

   /**
    * This is the start offset of the first cached region in the default test cache layout.
    */
   private static final long DEFAULT_CACHE_START_OFFSET = 5L;

   /**
    * This is a list index into the list of {@link MediumRegion}s built by the {@link TestCacheLayout} which is created
    * from {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions(long, int)}. It refers to the first
    * {@link MediumRegion} in the default {@link TestCacheLayout} having direct consecutive follow-up
    * {@link MediumRegion}s in the cache.
    */
   private final static int DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS = 0;
   private final static int DEFAULT_CACHE_INDEX_FIRST_CONSECUTIVE_REGIONS_SIZE = 160;

   /**
    * This is a medium offset before the first cached {@link MediumRegion} in the default {@link TestCacheLayout}, which
    * is created from {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions(long, int)}.
    */
   private final static long DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION = 2L;
   /**
    * This is a medium offset within the first gap in the default {@link TestCacheLayout}, which is created from
    * {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions(long, int)}.
    */
   private final static long DEFAULT_CACHE_LAYOUT_GAP_OFFSET = 165L;

   /**
    * Tests {@link MediumCache#MediumCache(de.je.jmeta.media.api.IMedium)}, {@link MediumCache#getMedium()},
    * {@link MediumCache#getMaximumCacheSizeInBytes()} and {@link MediumCache#getMaximumCacheRegionSizeInBytes()}.
    */
   @Test
   public void constructor_initializesMediumAndDefaultSizesCorrectly() {
      MediumCache newCache = new MediumCache(MEDIUM);

      Assert.assertEquals(MEDIUM, newCache.getMedium());
      Assert.assertEquals(MediumCache.UNLIMITED_CACHE_SIZE, newCache.getMaximumCacheSizeInBytes());
      Assert.assertEquals(MediumCache.UNLIMITED_CACHE_REGION_SIZE, newCache.getMaximumCacheRegionSizeInBytes());
   }

   /**
    * Tests {@link MediumCache#MediumCache(de.je.jmeta.media.api.IMedium)}, {@link MediumCache#getMedium()},
    * {@link MediumCache#getMaximumCacheSizeInBytes()} and {@link MediumCache#getMaximumCacheRegionSizeInBytes()}.
    */
   @Test
   public void constructor_initializesMediumAndSizesCorrectly() {
      long maximumCacheSizeInBytes = 20L;
      int maximumCacheRegionSizeInBytes = 10;
      MediumCache newCache = new MediumCache(MEDIUM, maximumCacheSizeInBytes, maximumCacheRegionSizeInBytes);

      Assert.assertEquals(MEDIUM, newCache.getMedium());
      Assert.assertEquals(maximumCacheSizeInBytes, newCache.getMaximumCacheSizeInBytes());
      Assert.assertEquals(maximumCacheRegionSizeInBytes, newCache.getMaximumCacheRegionSizeInBytes());
   }

   /**
    * Tests {@link MediumCache#MediumCache(de.je.jmeta.media.api.IMedium)}, {@link MediumCache#getMedium()},
    * {@link MediumCache#getMaximumCacheSizeInBytes()} and {@link MediumCache#getMaximumCacheRegionSizeInBytes()}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void constructor_forMaxCacheSizeSmallerThanMaxRegionSize_throwsException() {
      long maximumCacheSizeInBytes = 10L;
      int maximumCacheRegionSizeInBytes = 20;
      new MediumCache(MEDIUM, maximumCacheSizeInBytes, maximumCacheRegionSizeInBytes);
   }

   /**
    * Tests {@link MediumCache#clear()}.
    */
   @Test
   public void clear_forEmptyCache_changesNothing() {
      MediumCache emptyCache = new MediumCache(MEDIUM);

      emptyCache.clear();

      assertCacheIsEmpty(emptyCache);
   }

   /**
    * Tests {@link MediumCache#clear()}.
    */
   @Test
   public void clear_forFilledCache_cacheIsEmpty() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      cache.clear();

      assertCacheIsEmpty(cache);
   }

   /**
    * Tests {@link MediumCache#clear()}.
    */
   @Test
   public void clearTwice_forFilledCache_cacheIsStillEmpty() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      cache.clear();

      assertCacheIsEmpty(cache);

      cache.clear();

      assertCacheIsEmpty(cache);
   }

   /**
    * Tests {@link MediumCache#getAllCachedRegions()}.
    */
   @Test
   public void getAllCachedRegions_forEmptyCache_returnsEmptyList() {
      MediumCache emptyCache = new MediumCache(MEDIUM);

      Assert.assertEquals(0, emptyCache.getAllCachedRegions().size());
   }

   /**
    * Tests {@link MediumCache#getAllCachedRegions()}.
    */
   @Test
   public void getAllCachedRegions_forFilledCache_returnsExpectedRegionsInCorrectOrder() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      assertCacheInvariantsAreFulfilled(cache);

      Assert.assertEquals(cacheLayout.getAllCachedRegions().size(), cache.getAllCachedRegions().size());
      Assert.assertEquals(cacheLayout.getAllCachedRegions(), cache.getAllCachedRegions());
   }

   /**
    * Tests {@link MediumCache#getCurrentCacheSizeInBytes()}.
    */
   @Test
   public void getCurrentCacheSizeInBytes_forEmptyCache_returnsZero() {
      MediumCache emptyCache = new MediumCache(MEDIUM);

      Assert.assertEquals(0, emptyCache.getCurrentCacheSizeInBytes());
   }

   /**
    * Tests {@link MediumCache#getCurrentCacheSizeInBytes()}.
    */
   @Test
   public void getCurrentCacheSizeInBytes_forFilledCache_returnsExpectedTotalSize() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      Assert.assertEquals(cacheLayout.getTotalRegionSizeInBytes(), cache.getCurrentCacheSizeInBytes());
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getCachedByteCountAt_forInvalidReference_throwsException() {
      MediumCache emptyCache = new MediumCache(MEDIUM);
      emptyCache.getCachedByteCountAt(new StandardMediumReference(UNRELATED_MEDIUM, 0L));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_forEmptyCache_returnsZero() {
      MediumCache emptyCache = new MediumCache(MEDIUM);

      Assert.assertEquals(0, emptyCache
         .getCachedByteCountAt(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION)));
      Assert.assertEquals(0,
         emptyCache.getCachedByteCountAt(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET)));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_forFilledCacheButWithOffsetOutsideRegions_returnsZero() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      Assert.assertEquals(0, cache
         .getCachedByteCountAt(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION)));
      Assert.assertEquals(0,
         cache.getCachedByteCountAt(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET)));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_endOfLastConsecutiveRegion_returnsZero() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      MediumRegion lastConsecutiveRegion = firstConsecutiveRegions.get(firstConsecutiveRegions.size() - 1);
      Assert.assertEquals(0, cache.getCachedByteCountAt(lastConsecutiveRegion.calculateEndReference()));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_startOfRegionWithConsecutiveRegions_returnsTotalSizeOfAllConsecutiveRegions() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);
      Assert.assertEquals(DEFAULT_CACHE_INDEX_FIRST_CONSECUTIVE_REGIONS_SIZE,
         cache.getCachedByteCountAt(firstConsecutiveRegion.getStartReference()));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_endOfRegionWithConsecutiveRegions_returnsTotalSizeOfSubsequentRegions() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);
      Assert.assertEquals(DEFAULT_CACHE_INDEX_FIRST_CONSECUTIVE_REGIONS_SIZE - firstConsecutiveRegion.getSize(),
         cache.getCachedByteCountAt(firstConsecutiveRegion.calculateEndReference()));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_middleOfRegionWithConsecutiveRegions_returnsCorrectTotalConsecutiveByteCount() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);
      int middleOffset = firstConsecutiveRegion.getSize() / 2;
      Assert.assertEquals(DEFAULT_CACHE_INDEX_FIRST_CONSECUTIVE_REGIONS_SIZE - middleOffset,
         cache.getCachedByteCountAt(firstConsecutiveRegion.getStartReference().advance(middleOffset)));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_middleOfLastConsecutiveRegion_returnsRemainingLastRegionSize() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      MediumRegion lastConsecutiveRegion = firstConsecutiveRegions.get(firstConsecutiveRegions.size() - 1);
      int middleOffset = lastConsecutiveRegion.getSize() / 2;
      Assert.assertEquals(lastConsecutiveRegion.getSize() - middleOffset,
         cache.getCachedByteCountAt(lastConsecutiveRegion.getStartReference().advance(middleOffset)));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_forEmptyCache_returnsEmptyList() {
      MediumCache emptyCache = new MediumCache(MEDIUM);

      Assert.assertEquals(0,
         emptyCache
            .getRegionsInRange(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION), 10)
            .size());
      Assert.assertEquals(0,
         emptyCache.getRegionsInRange(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET), 10).size());
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeCoveringFullCachedRegion_returnsSingleRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);

      List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(firstConsecutiveRegion.getStartReference(),
         firstConsecutiveRegion.getSize());
      Assert.assertEquals(1, actualRegionsInRange.size());
      Assert.assertEquals(firstConsecutiveRegion, actualRegionsInRange.get(0));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeWithinCachedRegion_returnsEnclosingRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);

      List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(
         firstConsecutiveRegion.getStartReference().advance(1L), firstConsecutiveRegion.getSize() - 1);
      Assert.assertEquals(1, actualRegionsInRange.size());
      Assert.assertEquals(firstConsecutiveRegion, actualRegionsInRange.get(0));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeFullyCoveringMultipleCachedRegions_returnsAllCoveredRegions() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      int totalSize = firstConsecutiveRegions.get(0).getSize() + firstConsecutiveRegions.get(1).getSize()
         + firstConsecutiveRegions.get(2).getSize();

      List<MediumRegion> actualRegionsInRange = cache
         .getRegionsInRange(firstConsecutiveRegions.get(0).getStartReference(), totalSize);
      Assert.assertEquals(firstConsecutiveRegions, actualRegionsInRange);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeStartingAndEndingWithinCachedRegion_returnsAllCoveredRegions() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);
      MediumRegion lastConsecutiveRegion = firstConsecutiveRegions.get(2);

      List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(
         firstConsecutiveRegion.getStartReference().advance(1L), lastConsecutiveRegion.getSize() - 1);
      Assert.assertEquals(firstConsecutiveRegions, actualRegionsInRange);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeOutsideCachedRegionAndSizeLessThanMaxRegionSize_returnsSingleUncachedRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      StandardMediumReference startReference = new StandardMediumReference(MEDIUM,
         DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION);
      int rangeSizeInBytes = (int) (DEFAULT_CACHE_START_OFFSET - DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION);

      MediumRegion expectedUncachedRegion = new MediumRegion(startReference, rangeSizeInBytes);

      List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(startReference, rangeSizeInBytes);

      Assert.assertEquals(1, actualRegionsInRange.size());
      Assert.assertEquals(expectedUncachedRegion, actualRegionsInRange.get(0));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeOutsideCachedRegionSizeBiggerThanButNoMultipleOfMaxRegionSize_returnsTwoUncachedRegions() {

      int maxCacheRegionSize = 180;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      MediumCache cache = cacheLayout.buildCache();

      // This start offset is 5 bytes within the gap behind the third consecutive region, which has size 200 bytes in
      // total
      long startOffset = DEFAULT_CACHE_LAYOUT_GAP_OFFSET;
      long endOffset = startOffset + maxCacheRegionSize + 5L;

      StandardMediumReference startReference = new StandardMediumReference(MEDIUM, startOffset);
      int rangeSizeInBytes = (int) (endOffset - startOffset);

      List<MediumRegion> expectedUncachedRegions = new ArrayList<>();
      expectedUncachedRegions.add(new MediumRegion(startReference, maxCacheRegionSize));
      expectedUncachedRegions
         .add(new MediumRegion(startReference.advance(maxCacheRegionSize), rangeSizeInBytes - maxCacheRegionSize));

      List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(startReference, rangeSizeInBytes);

      Assert.assertEquals(expectedUncachedRegions, actualRegionsInRange);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeOutsideCachedRegionExactlyTripleSizeOfMaxRegionSize_returnsThreeUncachedRegions() {

      int maxCacheRegionSize = 60;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      MediumCache cache = cacheLayout.buildCache();

      // This start offset is 5 bytes within the gap behind the third consecutive region, which has size 200 bytes in
      // total
      long startOffset = DEFAULT_CACHE_LAYOUT_GAP_OFFSET;
      long endOffset = startOffset + maxCacheRegionSize * 3;

      StandardMediumReference startReference = new StandardMediumReference(MEDIUM, startOffset);
      int rangeSizeInBytes = (int) (endOffset - startOffset);

      List<MediumRegion> expectedUncachedRegions = new ArrayList<>();
      expectedUncachedRegions.add(new MediumRegion(startReference, maxCacheRegionSize));
      expectedUncachedRegions.add(new MediumRegion(startReference.advance(maxCacheRegionSize), maxCacheRegionSize));
      expectedUncachedRegions.add(new MediumRegion(startReference.advance(2 * maxCacheRegionSize), maxCacheRegionSize));

      List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(startReference, rangeSizeInBytes);

      Assert.assertEquals(expectedUncachedRegions, actualRegionsInRange);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeOverlappingCachedRegionsAtFront_returnsSingleGapAndSingleCachedRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);

      int expectedFirstUncachedRegionSize = 3;
      long regionStartOffset = DEFAULT_CACHE_START_OFFSET - expectedFirstUncachedRegionSize;
      IMediumReference startReference = new StandardMediumReference(MEDIUM, regionStartOffset);

      List<MediumRegion> expectedRegions = new ArrayList<>();
      expectedRegions.add(new MediumRegion(startReference, expectedFirstUncachedRegionSize));
      expectedRegions.add(firstConsecutiveRegion);

      List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(startReference,
         firstConsecutiveRegion.getSize() - 1);

      Assert.assertEquals(expectedRegions, actualRegionsInRange);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeOverlappingCachedRegionsAtBack_returnsTwoCachedAndSingleGapRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

      MediumRegion lastConsecutiveRegion = firstConsecutiveRegions.get(2);

      int expectedLastUncachedRegionSize = 10;
      IMediumReference startReference = lastConsecutiveRegion.getStartReference();

      List<MediumRegion> expectedRegions = new ArrayList<>();
      expectedRegions.add(lastConsecutiveRegion);
      expectedRegions
         .add(new MediumRegion(lastConsecutiveRegion.calculateEndReference(), expectedLastUncachedRegionSize));

      List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(startReference,
         lastConsecutiveRegion.getSize() + expectedLastUncachedRegionSize);

      Assert.assertEquals(expectedRegions, actualRegionsInRange);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeCoveringAllCachedRegions_returnsCachedRegionsAndGapsBetweenThem() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> allCachedRegionsWithGaps = cacheLayout.getAllCachedRegionsWithGaps();

      MediumRegion firstRegion = allCachedRegionsWithGaps.get(0);
      MediumRegion lastRegion = allCachedRegionsWithGaps.get(allCachedRegionsWithGaps.size() - 1);

      long totalRangeSizeCoveredByCachedRegions = lastRegion.calculateEndReference()
         .distanceTo(firstRegion.getStartReference());

      int expectedFirstUncachedRegionSize = 3;
      long regionStartOffset = DEFAULT_CACHE_START_OFFSET - expectedFirstUncachedRegionSize;

      int expectedLastUncachedRegionSize = 20;

      IMediumReference startReference = new StandardMediumReference(MEDIUM, regionStartOffset);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      // Gap at front
      expectedRegions.add(new MediumRegion(startReference, expectedFirstUncachedRegionSize));

      expectedRegions.addAll(allCachedRegionsWithGaps);

      // Gap at back
      expectedRegions.add(new MediumRegion(lastRegion.calculateEndReference(), expectedLastUncachedRegionSize));

      int totalRangeSize = (int) (expectedFirstUncachedRegionSize + totalRangeSizeCoveredByCachedRegions
         + expectedLastUncachedRegionSize);

      List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(startReference, totalRangeSize);

      Assert.assertEquals(expectedRegions, actualRegionsInRange);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getRegionsInRange_forInvalidReference_throwsException() {
      MediumCache emptyCache = new MediumCache(MEDIUM);
      emptyCache.getRegionsInRange(new StandardMediumReference(UNRELATED_MEDIUM, 0L), 10);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getRegionsInRange_forInvalidRangeSize_throwsException() {
      MediumCache emptyCache = new MediumCache(MEDIUM);
      emptyCache.getRegionsInRange(new StandardMediumReference(MEDIUM, 0L), -10);
   }

   private List<MediumRegion> getFirstConsecutiveRegionsFromDefaultCache(TestCacheLayout cacheLayout) {
      List<MediumRegion> allRegions = cacheLayout.getAllCachedRegions();

      List<MediumRegion> firstConsecutiveRegions = new ArrayList<MediumRegion>();

      firstConsecutiveRegions.add(allRegions.get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS));
      firstConsecutiveRegions.add(allRegions.get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 1));
      firstConsecutiveRegions.add(allRegions.get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2));
      return firstConsecutiveRegions;
   }

   /**
    * Verifies that all invariants of the {@link MediumCache} class are fulfilled.
    * 
    * @param cache
    *           The {@link MediumCache} instance to check.
    */
   private void assertCacheInvariantsAreFulfilled(MediumCache cache) {
      List<MediumRegion> actualRegions = cache.getAllCachedRegions();

      long actualCacheSize = 0;
      IMediumReference previousEndReference = null;
      for (MediumRegion mediumRegion : actualRegions) {
         actualCacheSize += mediumRegion.getSize();

         if (previousEndReference != null) {
            Assert.assertTrue(
               "The current medium region <" + mediumRegion
                  + "> overlaps the previous region which violates the class invariant of "
                  + MediumCache.class.getSimpleName(),
               mediumRegion.getStartReference().behindOrEqual(previousEndReference));
         }

         Assert.assertTrue(
            "The current medium regions size <" + mediumRegion.getSize()
               + "> must be smaller or equal to the configured maximum cache region size of <"
               + cache.getMaximumCacheRegionSizeInBytes() + ">",
            mediumRegion.getSize() <= cache.getMaximumCacheRegionSizeInBytes());
      }

      Assert.assertTrue("The current total medium region size of <" + actualCacheSize
         + "> must be smaller or equal to the configured maximum cache size of <" + cache.getMaximumCacheSizeInBytes()
         + ">", actualCacheSize <= cache.getMaximumCacheSizeInBytes());
   }

   /**
    * Verifies that the given {@link MediumCache} instance is indeed empty.
    * 
    * @param cache
    *           The cache instance to check.
    */
   private void assertCacheIsEmpty(MediumCache cache) {

      Assert.assertEquals(0L, cache.getCurrentCacheSizeInBytes());
      Assert.assertEquals(0, cache.getAllCachedRegions().size());
      Assert.assertEquals(0, cache.getRegionsInRange(new StandardMediumReference(MEDIUM, 0L),
         (int) Math.min(cache.getMaximumCacheSizeInBytes(), Integer.MAX_VALUE)).size());
   }

   /**
    * Creates a {@link TestCacheLayout} with unlimited sizes and some pre-existing content: (1) Three consecutive
    * regions of different sizes starting at an offset > 0, (2) A gap of 200 bytes, (3) Two consecutive regions, (4) A
    * gap of 1 byte, (5) A last region
    * 
    * @param maxCacheSize
    *           The maximum cache size to set
    * @param maxCacheRegionSize
    *           The maximum cache region size to set
    * 
    * @return The default {@link MediumCache} pre-filled and with unlimited sizes
    */
   private TestCacheLayout createDefaultLayoutHavingSubsequentAndScatteredRegions(long maxCacheSize,
      int maxCacheRegionSize) {
      TestCacheLayout cacheLayout = new TestCacheLayout(DEFAULT_CACHE_START_OFFSET, MEDIUM, maxCacheSize,
         maxCacheRegionSize);

      cacheLayout.addNextRegionInfo(40, 0);        // Region 1: [5, 45)
      cacheLayout.addNextRegionInfo(20, 0);        // Region 2: [45, 65)
      cacheLayout.addNextRegionInfo(100, 200);     // Region 3: [65, 165), Gap Region 4: [165, 365)
      cacheLayout.addNextRegionInfo(5, 0);         // Region 5: [365, 370)
      cacheLayout.addNextRegionInfo(27, 1);        // Region 6: [370, 397), Gap Region 7: [397, 398)
      cacheLayout.addNextRegionInfo(150, 0);       // Region 5: [398, 548)

      return cacheLayout;
   }
}
