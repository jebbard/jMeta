package de.je.jmeta.media.impl;

import static de.je.jmeta.media.impl.TestMediumUtility.DUMMY_DEFAULT_TEST_MEDIUM;
import static de.je.jmeta.media.impl.TestMediumUtility.DUMMY_UNRELATED_MEDIUM;
import static de.je.jmeta.media.impl.TestMediumUtility.createCachedMediumRegionOnDefaultMedium;
import static de.je.jmeta.media.impl.TestMediumUtility.createReference;
import static de.je.jmeta.media.impl.TestMediumUtility.createReferenceToDefaultMedium;
import static de.je.jmeta.media.impl.TestMediumUtility.createUnCachedMediumRegionOnDefaultMedium;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Test;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.PreconditionUnfullfilledException;
import de.je.util.javautil.common.err.Reject;
import junit.framework.Assert;

/**
 * This class tests the {@link MediumCache} class.
 */
public class MediumCacheTest {

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
   private final static long DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION = 2L;
   /**
    * This is a medium offset within the first gap in the default {@link TestCacheLayout}, which is created from
    * {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions(long, int)}.
    */
   private final static long DEFAULT_CACHE_OFFSET_IN_FIRST_GAP = 165L;

   /**
    * Tests {@link MediumCache#MediumCache(de.je.jmeta.media.api.IMedium)}, {@link MediumCache#getMedium()},
    * {@link MediumCache#getMaximumCacheSizeInBytes()} and {@link MediumCache#getMaximumCacheRegionSizeInBytes()}.
    */
   @Test
   public void constructor_initializesMediumAndDefaultSizesCorrectly() {
      MediumCache newCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);

      Assert.assertEquals(DUMMY_DEFAULT_TEST_MEDIUM, newCache.getMedium());
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
      MediumCache newCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM, maximumCacheSizeInBytes,
         maximumCacheRegionSizeInBytes);

      Assert.assertEquals(DUMMY_DEFAULT_TEST_MEDIUM, newCache.getMedium());
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
      new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM, maximumCacheSizeInBytes, maximumCacheRegionSizeInBytes);
   }

   /**
    * Tests {@link MediumCache#clear()}.
    */
   @Test
   public void clear_forEmptyCache_changesNothing() {
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);

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
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);

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
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);

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
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);
      emptyCache.getCachedByteCountAt(createReference(DUMMY_UNRELATED_MEDIUM, 0L));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_forEmptyCache_returnsZero() {
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);

      Assert.assertEquals(0,
         emptyCache.getCachedByteCountAt(createReferenceToDefaultMedium(DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION)));
      Assert.assertEquals(0,
         emptyCache.getCachedByteCountAt(createReferenceToDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP)));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_forFilledCacheButWithOffsetOutsideRegions_returnsZero() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      MediumCache cache = cacheLayout.buildCache();

      Assert.assertEquals(0,
         cache.getCachedByteCountAt(createReferenceToDefaultMedium(DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION)));
      Assert.assertEquals(0,
         cache.getCachedByteCountAt(createReferenceToDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP)));
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
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);

      Assert.assertEquals(0, emptyCache
         .getRegionsInRange(createReferenceToDefaultMedium(DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION), 10).size());
      Assert.assertEquals(0,
         emptyCache.getRegionsInRange(createReferenceToDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP), 10).size());
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
         createUnCachedMediumRegionOnDefaultMedium(DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION,
            (int) (cacheLayout.getStartOffset() - DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION)));
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
         createUnCachedMediumRegionOnDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP, maxCacheRegionSize),
         createUnCachedMediumRegionOnDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP + maxCacheRegionSize,
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
         createUnCachedMediumRegionOnDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP, maxCacheRegionSize),
         createUnCachedMediumRegionOnDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP + maxCacheRegionSize,
            maxCacheRegionSize),
         createUnCachedMediumRegionOnDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP + 2 * maxCacheRegionSize,
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
         createUnCachedMediumRegionOnDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP,
            (int) lastRegion.getStartReference().distanceTo(firstRegion.calculateEndReference())),
         lastRegion);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeWithGapBetweenTwoCachedRegionsAndExactlyDoubleSizeOfMaxRegionSize_returnsCachedRegionsAndGap() {

      int maxCacheRegionSize = DEFAULT_CACHE_FIRST_GAP_SIZE / 2;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

      testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 5, 7,
         cacheLayout.getAllCachedRegions().get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2),
         createUnCachedMediumRegionOnDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP, maxCacheRegionSize),
         createUnCachedMediumRegionOnDefaultMedium(DEFAULT_CACHE_OFFSET_IN_FIRST_GAP + maxCacheRegionSize,
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
         createUnCachedMediumRegionOnDefaultMedium(cacheLayout.getStartOffset() - uncachedRegionSize,
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

      expectedRegions.add(createUnCachedMediumRegionOnDefaultMedium(
         cacheLayout.getStartOffset() - uncachedRegionBeforeSize, uncachedRegionBeforeSize));
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
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);
      emptyCache.getRegionsInRange(createReference(DUMMY_UNRELATED_MEDIUM, 0L), 10);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getRegionsInRange_forInvalidRangeSize_throwsException() {
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);
      emptyCache.getRegionsInRange(createReferenceToDefaultMedium(0L), -10);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_withSizeSmallerThanMaxRegionSizeToEmptyCache_addsSingleRegionToCache() {
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(0L, 20, 15);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(regionToAdd);

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(emptyCache, regionToAdd,
         expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_withSizeBiggerThanMaxRegionSizeToEmptyCache_addsMultipleRegionsToCache() {
      int maxRegionSize = 50;
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM, Long.MAX_VALUE, maxRegionSize);

      long startOfs = 10L;
      int fillByte = -88;
      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(startOfs, 355, fillByte);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(createCachedMediumRegionOnDefaultMedium(startOfs, maxRegionSize, fillByte));
      expectedRegions.add(createCachedMediumRegionOnDefaultMedium(startOfs + maxRegionSize, maxRegionSize, fillByte));
      expectedRegions
         .add(createCachedMediumRegionOnDefaultMedium(startOfs + 2 * maxRegionSize, maxRegionSize, fillByte));
      expectedRegions
         .add(createCachedMediumRegionOnDefaultMedium(startOfs + 3 * maxRegionSize, maxRegionSize, fillByte));
      expectedRegions
         .add(createCachedMediumRegionOnDefaultMedium(startOfs + 4 * maxRegionSize, maxRegionSize, fillByte));
      expectedRegions
         .add(createCachedMediumRegionOnDefaultMedium(startOfs + 5 * maxRegionSize, maxRegionSize, fillByte));
      expectedRegions
         .add(createCachedMediumRegionOnDefaultMedium(startOfs + 6 * maxRegionSize, maxRegionSize, fillByte));
      expectedRegions.add(createCachedMediumRegionOnDefaultMedium(startOfs + 7 * maxRegionSize, 5, fillByte));

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(emptyCache, regionToAdd,
         expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_withSizeBiggerThanMaxCacheSizeToEmptyCache_addsLastSplitPartsUpToMaxCacheSizeToCache() {
      long maxCacheSize = 1000L;
      int maxRegionSize = 350;
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM, maxCacheSize, maxRegionSize);
      long startOfs = 10L;

      int fillByte = -77;
      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(startOfs, (int) (maxCacheSize + 100),
         fillByte);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(createCachedMediumRegionOnDefaultMedium(startOfs + maxRegionSize, maxRegionSize, fillByte));
      expectedRegions
         .add(createCachedMediumRegionOnDefaultMedium(startOfs + 2 * maxRegionSize, maxRegionSize, fillByte));
      expectedRegions.add(createCachedMediumRegionOnDefaultMedium(startOfs + 3 * maxRegionSize, 50, fillByte));

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(emptyCache, regionToAdd,
         expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheWithoutOverlapsBehindExistingRegions_addsSingleRegionToCache() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      List<MediumRegion> allCachedRegions = cacheLayout.getAllCachedRegions();

      long newRegionStartOffset = allCachedRegions.get(allCachedRegions.size() - 1).calculateEndReference()
         .getAbsoluteMediumOffset() + 5L;

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(newRegionStartOffset, 20, 15);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.addAll(allCachedRegions);
      expectedRegions.add(regionToAdd);

      MediumCache cache = cacheLayout.buildCache();

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheWithoutOverlapsBeforeExistingRegions_addsSingleRegionToCache() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      List<MediumRegion> allCachedRegions = cacheLayout.getAllCachedRegions();

      long newRegionStartOffset = DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION;

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(newRegionStartOffset,
         (int) (cacheLayout.getStartOffset() - newRegionStartOffset), 0);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(regionToAdd);
      expectedRegions.addAll(allCachedRegions);

      MediumCache cache = cacheLayout.buildCache();

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheOverlappingExistingRegionAtFront_shrinksExistingRegionAndAddsNewRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      List<MediumRegion> allCachedRegions = cacheLayout.getAllCachedRegions();

      MediumRegion originalRegion = allCachedRegions.get(0);

      int newRegionStartDistanceToOriginalRegionStart = 3;
      int regionToAddSize = originalRegion.getSize();

      int frontOverlap = regionToAddSize - newRegionStartDistanceToOriginalRegionStart;

      long regionToAddStartOffset = originalRegion.getStartReference().getAbsoluteMediumOffset()
         - newRegionStartDistanceToOriginalRegionStart;

      MediumRegion[] splitRegions = originalRegion.split(originalRegion.getStartReference().advance(frontOverlap));

      MediumRegion survivingPartOfOriginalRegion = splitRegions[1];

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(regionToAddStartOffset, regionToAddSize, -66);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(regionToAdd);
      expectedRegions.add(survivingPartOfOriginalRegion);
      for (int i = 1; i < allCachedRegions.size(); i++) {
         expectedRegions.add(allCachedRegions.get(i));
      }

      MediumCache cache = cacheLayout.buildCache();

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheOverlappingExistingRegionAtFrontSummedSizeAboveButNewSizeBelowMaxCacheSize_keepsOldAndAddsSplitRegion() {
      long maxCacheSize = 250L;
      int maxRegionSize = 200;
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM, maxCacheSize, maxRegionSize);

      MediumRegion firstRegion = createCachedMediumRegionOnDefaultMedium(0L, 200, 15);
      emptyCache.addRegion(firstRegion);

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(150L, 100, 66);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(firstRegion.split(regionToAdd.getStartReference())[0]);
      expectedRegions.add(regionToAdd);

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(emptyCache, regionToAdd,
         expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheOverlappingExistingRegionAtBack_shrinksExistingRegionAndAddsNewRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      List<MediumRegion> allCachedRegions = cacheLayout.getAllCachedRegions();

      MediumRegion originalRegion = allCachedRegions.get(2);

      int newRegionStartDistanceToOriginalRegionStart = 2;
      int regionToAddSize = originalRegion.getSize();

      long regionToAddStartOffset = originalRegion.getStartReference().getAbsoluteMediumOffset()
         + newRegionStartDistanceToOriginalRegionStart;

      MediumRegion[] splitRegions = originalRegion
         .split(originalRegion.getStartReference().advance(newRegionStartDistanceToOriginalRegionStart));

      MediumRegion survivingPartOfOriginalRegion = splitRegions[0];

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(regionToAddStartOffset, regionToAddSize, -66);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(allCachedRegions.get(0));
      expectedRegions.add(allCachedRegions.get(1));
      expectedRegions.add(survivingPartOfOriginalRegion);
      expectedRegions.add(regionToAdd);
      for (int i = 3; i < allCachedRegions.size(); i++) {
         expectedRegions.add(allCachedRegions.get(i));
      }

      MediumCache cache = cacheLayout.buildCache();

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheExactlyCoveringExistingRegion_replacesExistingWithNewRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      List<MediumRegion> allCachedRegions = cacheLayout.getAllCachedRegions();

      long newRegionStartOffset = allCachedRegions.get(0).getStartReference().getAbsoluteMediumOffset();

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(newRegionStartOffset,
         allCachedRegions.get(0).getSize(), -55);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.addAll(allCachedRegions);

      expectedRegions.set(0, regionToAdd);

      MediumCache cache = cacheLayout.buildCache();

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheOverlappingMultipleRegionsWithGaps_replacesAllRegionsWithNewRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      List<MediumRegion> allCachedRegions = cacheLayout.getAllCachedRegions();

      int frontOverlap = 4;
      int backOverlap = 3;

      MediumRegion firstOriginalRegionToDivide = allCachedRegions.get(2);
      MediumRegion lastOriginalRegionToDivide = allCachedRegions.get(3);

      MediumRegion[] splitRegions = firstOriginalRegionToDivide
         .split(firstOriginalRegionToDivide.getStartReference().advance(frontOverlap));

      MediumRegion survivingPartOfFirstOriginalRegion = splitRegions[0];

      splitRegions = lastOriginalRegionToDivide
         .split(lastOriginalRegionToDivide.calculateEndReference().advance(-backOverlap));

      MediumRegion survivingPartOfLastOriginalRegion = splitRegions[1];

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(
         lastOriginalRegionToDivide.getStartReference().getAbsoluteMediumOffset() + frontOverlap,
         DEFAULT_CACHE_FIRST_GAP_SIZE + frontOverlap + backOverlap, -44);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(allCachedRegions.get(0));
      expectedRegions.add(allCachedRegions.get(1));
      expectedRegions.add(survivingPartOfFirstOriginalRegion);
      expectedRegions.add(regionToAdd);
      expectedRegions.add(survivingPartOfLastOriginalRegion);

      for (int i = 4; i < allCachedRegions.size(); i++) {
         expectedRegions.add(allCachedRegions.get(i));
      }

      MediumCache cache = cacheLayout.buildCache();

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheOverlappingMultipleRegionsWithGapsStartAtExistingRegionStart_replacesAllRegionsWithNewRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      List<MediumRegion> allCachedRegions = cacheLayout.getAllCachedRegions();

      int backOverlap = 3;

      MediumRegion lastOriginalRegionToDivide = allCachedRegions.get(3);

      MediumRegion[] splitRegions = lastOriginalRegionToDivide
         .split(lastOriginalRegionToDivide.calculateEndReference().advance(-backOverlap));

      MediumRegion survivingPartOfLastOriginalRegion = splitRegions[1];

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(
         lastOriginalRegionToDivide.getStartReference().getAbsoluteMediumOffset(),
         DEFAULT_CACHE_FIRST_GAP_SIZE + backOverlap, -44);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(allCachedRegions.get(0));
      expectedRegions.add(allCachedRegions.get(1));
      expectedRegions.add(regionToAdd);
      expectedRegions.add(survivingPartOfLastOriginalRegion);

      for (int i = 4; i < allCachedRegions.size(); i++) {
         expectedRegions.add(allCachedRegions.get(i));
      }

      MediumCache cache = cacheLayout.buildCache();

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheEnclosedByExistingRegion_dividesExistingRegionWithNewRegion() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(
         MediumCache.UNLIMITED_CACHE_SIZE, MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      List<MediumRegion> allCachedRegions = cacheLayout.getAllCachedRegions();

      MediumRegion originalRegion = allCachedRegions.get(0);

      int frontOverlap = 2;
      int backOverlap = 3;

      MediumRegion[] splitRegions = originalRegion.split(originalRegion.getStartReference().advance(frontOverlap));

      MediumRegion survivingPartOneOfOriginalRegion = splitRegions[0];

      splitRegions = originalRegion.split(originalRegion.calculateEndReference().advance(-backOverlap));

      MediumRegion survivingPartTwoOfOriginalRegion = splitRegions[1];

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(
         originalRegion.getStartReference().getAbsoluteMediumOffset() + frontOverlap,
         originalRegion.getSize() - frontOverlap - backOverlap, -77);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(survivingPartOneOfOriginalRegion);
      expectedRegions.add(regionToAdd);
      expectedRegions.add(survivingPartTwoOfOriginalRegion);
      for (int i = 1; i < allCachedRegions.size(); i++) {
         expectedRegions.add(allCachedRegions.get(i));
      }

      MediumCache cache = cacheLayout.buildCache();

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheExceedingMaxCacheSizeWithLessBytesThanFirstAddedRegion_removesFirstAddedAndAddsNewRegion() {
      long maxCacheSize = 250L;
      int maxRegionSize = 200;
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM, maxCacheSize, maxRegionSize);

      MediumRegion firstRegion = createCachedMediumRegionOnDefaultMedium(0L, 200, 15);
      emptyCache.addRegion(firstRegion);

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(300L, 100, 66);

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(regionToAdd);

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(emptyCache, regionToAdd,
         expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheExceedingMaxCacheSizeNewRegionBiggerThanAllExisting_removesAllExistingAndAddsNewRegion() {
      long maxCacheSize = 1000L;
      int maxRegionSize = 1000;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(maxCacheSize, maxRegionSize);

      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(9999L, (int) (maxCacheSize - 1));

      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions.add(regionToAdd);

      MediumCache cache = cacheLayout.buildCache();

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#addRegion(MediumRegion)}.
    */
   @Test
   public void addRegion_toFilledCacheExceedingMaxCacheSizeNewRegionBiggerThanMaxCacheSizeAndCovereingSomeExisting_removesAllExistingAndAddsEndPartsOfNewRegion() {
      long maxCacheSize = 500L;
      int maxRegionSize = 150;

      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions(maxCacheSize, maxRegionSize);

      // Why choosing 46L as start offset?
      // - New region spans from [46, 548)
      // - It does not overlap the first default region, but the second, starting at the 2nd byte
      // - It covers all other regions and gaps in the default cache, and ends exactly where the last region in the
      // default cache ends
      long startOffset = 46L;
      int fillByte = -99;
      MediumRegion regionToAdd = createCachedMediumRegionOnDefaultMedium(startOffset, (int) (maxCacheSize + 2),
         fillByte);

      // Expectation: The total size of the cache after adding the new region would be
      // 500 + 2 (=size of new region) + 1 (=size of remainder of second region) + 40 (=size of first region) = 543
      // As this exceeds the max cache size by 43, expectation is:
      // (1) The first region spanning 40 bytes and the one byte remainder of the second region are removed
      // (2) The first part of the new region having a size of 150 bytes is removed
      List<MediumRegion> expectedRegions = new ArrayList<>();

      expectedRegions
         .add(createCachedMediumRegionOnDefaultMedium(startOffset + maxRegionSize, maxRegionSize, fillByte));
      expectedRegions
         .add(createCachedMediumRegionOnDefaultMedium(startOffset + 2 * maxRegionSize, maxRegionSize, fillByte));
      expectedRegions.add(createCachedMediumRegionOnDefaultMedium(startOffset + 3 * maxRegionSize, 52, fillByte));

      MediumCache cache = cacheLayout.buildCache();

      testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void addRegion_forInvalidReference_throwsException() {
      MediumCache emptyCache = new MediumCache(DUMMY_DEFAULT_TEST_MEDIUM);
      emptyCache.addRegion(new MediumRegion(createReference(DUMMY_UNRELATED_MEDIUM, 0L), 10));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)} for a range that exactly fully covers a list of
    * expected {@link MediumRegion}s.
    * 
    * @param cacheLayout
    *           The {@link TestCacheLayout} to use containing details about the cache structure
    * @param expectedRegions
    *           The {@link MediumRegion}s expected to be returned by
    *           {@link MediumCache#getRegionsInRange(IMediumReference, int)}. The input range when calling this method
    *           is derived from this list, its start reference is the {@link IMediumReference} of the first region in
    *           the list, and its size is the total size of all regions in the list.
    */
   private void testGetRegionsInRange_fullyCoveringExpectedRegions(TestCacheLayout cacheLayout,
      MediumRegion... expectedRegions) {
      testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 0, 0, expectedRegions);
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)} for a range that exactly fully covers a list of
    * expected {@link MediumRegion}s.
    * 
    * @param cacheLayout
    *           The {@link TestCacheLayout} to use containing details about the cache structure
    * @param firstRegionAddSizeToStart
    * @param lastRegionSubstractSizeFromEnd
    * @param expectedRegions
    *           The {@link MediumRegion}s expected to be returned by
    *           {@link MediumCache#getRegionsInRange(IMediumReference, int)}. The input range when calling this method
    *           is derived from this list, its start reference is the {@link IMediumReference} of the first region in
    *           the list advanced by "firstRegionAddSizeToStart" bytes, and its size is the total size of all regions in
    *           the list minus "firstRegionAddSizeToStart" minus "lastRegionSubstractSizeFromEnd".
    */
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

   /**
    * Calls {@link MediumCache#addRegion(MediumRegion)} and checks its outcomes. Firstly, after the call, the cache
    * invariants must still be in place. Second, the expected regions and nothing else must be present, and third, the
    * cache size must have changed correspondingly.
    * 
    * @param cache
    *           The test object
    * @param regionToAdd
    *           The region to add to the cache
    * @param expectedRegions
    *           The list of expected to be present in the cache after the call (and nothing else), in offset order.
    */
   private void testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(MediumCache cache,
      MediumRegion regionToAdd, List<MediumRegion> expectedRegions) {

      cache.addRegion(regionToAdd);

      assertCacheInvariantsAreFulfilled(cache);

      long actualNewSize = cache.calculateCurrentCacheSizeInBytes();

      Assert.assertEquals(getTotalRegionSize(expectedRegions), actualNewSize);

      List<MediumRegion> allRegions = cache.getAllCachedRegions();

      Assert.assertEquals(expectedRegions, allRegions);
   }

   /**
    * Returns the total size of a list of {@link MediumRegion}s.
    * 
    * @param regionList
    *           The region list
    * @return The summed size of all regions in the list.
    */
   private static final int getTotalRegionSize(List<MediumRegion> regionList) {
      return regionList.stream().collect(Collectors.summingInt(region -> region.getSize()));
   }

   /**
    * Returns the first consecutive sequence of {@link MediumRegion}s in the default {@link TestCacheLayout}.
    * 
    * @param cacheLayout
    *           The {@link TestCacheLayout} to use containing details about the cache structure
    * @return the first consecutive sequence of {@link MediumRegion}s in the default {@link TestCacheLayout}.
    */
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
      TestCacheLayout cacheLayout = new TestCacheLayout(5L, DUMMY_DEFAULT_TEST_MEDIUM, maxCacheSize,
         maxCacheRegionSize);

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
      Assert.assertEquals(0, cache.getRegionsInRange(createReferenceToDefaultMedium(0L),
         (int) Math.min(cache.getMaximumCacheSizeInBytes(), Integer.MAX_VALUE)).size());
   }
}
