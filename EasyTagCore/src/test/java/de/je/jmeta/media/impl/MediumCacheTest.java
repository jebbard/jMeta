package de.je.jmeta.media.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.datatype.InMemoryMedium;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.PreconditionUnfullfilledException;
import de.je.util.javautil.common.err.Reject;
import junit.framework.Assert;

/**
 * This class tests the {@link MediumCache} class.
 */
public class MediumCacheTest {

   private static final FileMedium MEDIUM = new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, true);

   private static final InMemoryMedium UNRELATED_MEDIUM = new InMemoryMedium(new byte[] {}, "Fake", false);

   /**
    * This is a list index into the list of {@link MediumRegion}s built by the {@link TestCacheLayout} which is created
    * from {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions(long, int)}. It refers to the first
    * {@link MediumRegion} in the default {@link TestCacheLayout} having direct consecutive follow-up
    * {@link MediumRegion}s in the cache.
    */
   private final static int DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS = 0;

   /**
    * Size in bytes of the first range with multiple consecutive cached regions in the default cache
    */
   private final static int DEFAULT_CACHE_FIRST_CONSECUTIVE_REGIONS_SIZE = 160;

   /**
    * Size in bytes of the first gap between cached regions in the default cache
    */
   private final static int DEFAULT_CACHE_FIRST_GAP_SIZE = 200;

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
    * Tests {@link MediumCache#calculateCurrentCacheSizeInBytes()}.
    */
   @Test
   public void getCurrentCacheSizeInBytes_forEmptyCache_returnsZero() {
      MediumCache emptyCache = new MediumCache(MEDIUM);

      Assert.assertEquals(0, emptyCache.calculateCurrentCacheSizeInBytes());
   }

   /**
    * Tests {@link MediumCache#calculateCurrentCacheSizeInBytes()}.
    */
   @Test
   public void getCurrentCacheSizeInBytes_forFilledCache_returnsExpectedTotalSize() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      Assert.assertEquals(cacheLayout.getTotalRegionSizeInBytes(), cache.calculateCurrentCacheSizeInBytes());
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
      Assert.assertEquals(DEFAULT_CACHE_FIRST_CONSECUTIVE_REGIONS_SIZE,
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
      Assert.assertEquals(DEFAULT_CACHE_FIRST_CONSECUTIVE_REGIONS_SIZE - firstConsecutiveRegion.getSize(),
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
      Assert.assertEquals(DEFAULT_CACHE_FIRST_CONSECUTIVE_REGIONS_SIZE - middleOffset,
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

      int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout,
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeWithinCachedRegion_returnsEnclosingRegion() {

      int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 1, 2,
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeFullyCoveringMultipleCachedRegions_returnsAllCoveredRegions() {

      int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout,
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS),
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 1),
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeStartingAndEndingWithinDifferentCachedRegionsWithoutGaps_returnsAllCoveredRegions() {

      int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 1, 2,
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS),
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 1),
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeOutsideCachedRegionAndSizeLessThanMaxRegionSize_returnsSingleUncachedRegion() {

      int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout,
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION),
            (int) (cacheLayout.getStartOffset() - DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION)));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeOutsideCachedRegionSizeBiggerThanButNoMultipleOfMaxRegionSize_returnsTwoUncachedRegions() {

      int maxCacheRegionSize = 180;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      int remainderRegionSize = 5;

      testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout,
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET), maxCacheRegionSize),
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET + maxCacheRegionSize),
            remainderRegionSize));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeOutsideCachedRegionExactlyTripleSizeOfMaxRegionSize_returnsThreeUncachedRegions() {

      int maxCacheRegionSize = 60;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout,
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET), maxCacheRegionSize),
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET + maxCacheRegionSize),
            maxCacheRegionSize),
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET + 2 * maxCacheRegionSize),
            maxCacheRegionSize));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeWithGapBetweenTwoCachedRegionsAndSizeLessThanMaxRegionSize_returnsCachedRegionsAndGap() {

      int maxCacheRegionSize = DEFAULT_CACHE_FIRST_GAP_SIZE + 33;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      MediumRegion firstRegion = cacheLayout.getAllCachedRegions()
         .get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2);
      MediumRegion lastRegion = cacheLayout.getAllCachedRegions()
         .get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 3);

      testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 5, 7, firstRegion,
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET),
            (int) lastRegion.getStartReference().distanceTo(firstRegion.calculateEndReference())),
         lastRegion);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeWithGapBetweenTwoCachedRegionsAndExactlyQuadSizeOfMaxRegionSize_returnsCachedRegionsAndGap() {

      int maxCacheRegionSize = DEFAULT_CACHE_FIRST_GAP_SIZE / 4;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 5, 7,
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2),
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET), maxCacheRegionSize),
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET + maxCacheRegionSize),
            maxCacheRegionSize),
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET + 2 * maxCacheRegionSize),
            maxCacheRegionSize),
         new MediumRegion(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET + 3 * maxCacheRegionSize),
            maxCacheRegionSize),
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 3));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeOverlappingCachedRegionsAtFront_returnsSingleGapAndSingleCachedRegion() {

      int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      int uncachedRegionSize = 3;

      testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 0, 1,
         new MediumRegion(new StandardMediumReference(MEDIUM, cacheLayout.getStartOffset() - uncachedRegionSize),
            uncachedRegionSize),
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeOverlappingCachedRegionsAtBack_returnsTwoCachedAndSingleGapRegion() {

      int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      int uncachedRegionSize = 10;

      MediumRegion firstRegion = cacheLayout.getAllCachedRegions()
         .get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2);
      testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout, firstRegion,
         new MediumRegion(firstRegion.calculateEndReference(), uncachedRegionSize));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeCoveringAllCachedRegions_returnsCachedRegionsAndGapsBetweenThem() {

      int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      int uncachedRegionBeforeSize = 3;
      int uncachedRegionAfterSize = 20;

      List<MediumRegion> allCachedRegionsWithGaps = cacheLayout.getAllCachedRegionsWithGaps();
      List<MediumRegion> expectedRegions = new ArrayList<>();

      MediumRegion lastRegion = allCachedRegionsWithGaps.get(allCachedRegionsWithGaps.size() - 1);

      expectedRegions.add(
         new MediumRegion(new StandardMediumReference(MEDIUM, cacheLayout.getStartOffset() - uncachedRegionBeforeSize),
            uncachedRegionBeforeSize));
      expectedRegions.addAll(allCachedRegionsWithGaps);
      expectedRegions.add(new MediumRegion(lastRegion.calculateEndReference(), uncachedRegionAfterSize));

      MediumRegion[] expectedRegionsArray = new MediumRegion[expectedRegions.size()];
      testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout, expectedRegions.toArray(expectedRegionsArray));
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

   private void testGetRegionsInRange_fullyCoveringExpectedRegions(TestCacheLayout cacheLayout,
      MediumRegion... expectedRegions) {
      testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 0, 0, expectedRegions);
   }

   private void testGetRegionsInRange_notFullyCoveringExpectedRegions(TestCacheLayout cacheLayout,
      int firstRegionAddSizeToStart, int lastRegionSubstractSizeFromEnd, MediumRegion... expectedRegions) {
      Reject.ifNegative(firstRegionAddSizeToStart, "firstRegionAddSizeToStart");
      Reject.ifNegative(lastRegionSubstractSizeFromEnd, "lastRegionSubstractSizeFromEnd");

      List<MediumRegion> expectedRegionList = Arrays.asList(expectedRegions);

      IMediumReference rangeStartReference = expectedRegionList.get(0).getStartReference();

      int totalRegionSize = getTotalRegionSize(expectedRegionList);

      MediumCache cache = cacheLayout.buildCache();

      List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(
         rangeStartReference.advance(firstRegionAddSizeToStart), totalRegionSize - lastRegionSubstractSizeFromEnd);

      Assert.assertEquals(expectedRegionList, actualRegionsInRange);

   }

   private static final int getTotalRegionSize(List<MediumRegion> regionList) {
      return regionList.stream().collect(Collectors.summingInt(region -> region.getSize()));
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
    * Creates a {@link TestCacheLayout} with unlimited sizes and some pre-existing content: (1) Three consecutive
    * regions of different sizes starting at an offset > 0, (2) A gap of {@value #DEFAULT_CACHE_FIRST_GAP_SIZE} bytes,
    * (3) Two consecutive regions, (4) A gap of 1 byte, (5) A last region
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
      TestCacheLayout cacheLayout = new TestCacheLayout(5L, MEDIUM, maxCacheSize, maxCacheRegionSize);

      cacheLayout.addNextRegionInfo(40, 0);        // Region 1: [5, 45)
      cacheLayout.addNextRegionInfo(20, 0);        // Region 2: [45, 65)
      cacheLayout.addNextRegionInfo(100, DEFAULT_CACHE_FIRST_GAP_SIZE); // Region 3: [65, 165), Gap Region 4: [165, 365)
      cacheLayout.addNextRegionInfo(5, 0);         // Region 5: [365, 370)
      cacheLayout.addNextRegionInfo(27, 1);        // Region 6: [370, 397), Gap Region 7: [397, 398)
      cacheLayout.addNextRegionInfo(150, 0);       // Region 5: [398, 548)

      return cacheLayout;
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

      Assert.assertEquals(0L, cache.calculateCurrentCacheSizeInBytes());
      Assert.assertEquals(0, cache.getAllCachedRegions().size());
      Assert.assertEquals(0, cache.getRegionsInRange(new StandardMediumReference(MEDIUM, 0L),
         (int) Math.min(cache.getMaximumCacheSizeInBytes(), Integer.MAX_VALUE)).size());
   }
}
