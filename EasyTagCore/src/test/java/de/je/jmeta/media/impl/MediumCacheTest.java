package de.je.jmeta.media.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.datatype.InMemoryMedium;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.PreconditionUnfullfilledException;
import de.je.util.javautil.common.err.Reject;

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

   public List<MediumRegion> getExpectedCacheRegionsWithGapRegions() {
      return buildRegionList(true);
   }

   public List<MediumRegion> getExpectedCacheRegions() {
      return buildRegionList(false);
   }
   
   public List<MediumRegion> getGapRegions() {
	   List<MediumRegion> regionListWithGaps = getExpectedCacheRegionsWithGapRegions();
	   List<MediumRegion> gapRegions = new ArrayList<MediumRegion>();
	      
	      if (startOffset > 0) {
	    	  gapRegions.add(createUnCachedMediumRegion(0L, (int) getStartOffset()));
	      }
	   
	   for (MediumRegion mediumRegion : regionListWithGaps) {
		if (!mediumRegion.isCached()) {
			gapRegions.add(mediumRegion);
		}
	}
	   
	   return gapRegions;
   }
   
   public List<MediumRegion> getConsecutiveRegions() {
	   List<MediumRegion> regionListWithGaps = getExpectedCacheRegionsWithGapRegions();
	   List<MediumRegion> consecutiveRegions = new ArrayList<MediumRegion>();
	   
	   boolean consecutiveRegionStarted = false;
	   int currentConsecutiveRegionSize = 0;
	   long currentConsecutiveRegionStartOffset = 0L;
	   
	   for (MediumRegion mediumRegion : regionListWithGaps) {
		if (mediumRegion.isCached()) {
			if (!consecutiveRegionStarted) {
				currentConsecutiveRegionStartOffset = mediumRegion.getStartReference().getAbsoluteMediumOffset();
				consecutiveRegionStarted = true;
			}
			currentConsecutiveRegionSize += mediumRegion.getSize();
		} else {
			if (consecutiveRegionStarted) {
				consecutiveRegionStarted = false;
				currentConsecutiveRegionSize = 0;
				consecutiveRegions.add(createUnCachedMediumRegion(currentConsecutiveRegionStartOffset, currentConsecutiveRegionSize));
			}
			
		}
	}
	   
	   return consecutiveRegions;
   }
   
//   public Long getOffsetOfFirstGapBiggerThan(int minSize) {
//	   List<MediumRegion> regionListWithGaps = getExpectedCacheRegionListWithGapRegions();
//	   for (MediumRegion mediumRegion : regionListWithGaps) {
//		if (mediumRegion.isCached() && mediumRegion.getSize() > minSize) {
//			return mediumRegion.getStartReference().getAbsoluteMediumOffset();
//		}
//	}
//	   return null;
//   }
//   
//   public List<MediumRegion> getFirstConsecutiveRegions() {
//	   List<MediumRegion> regionListWithGaps = getExpectedCacheRegionListWithGapRegions();
//	   List<MediumRegion> firstConsecutiveRegions = new ArrayList<MediumRegion>();
//	   
//	   boolean cacheRegionsStarted = false;
//	   for (MediumRegion mediumRegion : regionListWithGaps) {
//		if (mediumRegion.isCached()) {
//			if (cacheRegionsStarted) {
//				firstConsecutiveRegions.add(mediumRegion);
//			} else {
//				cacheRegionsStarted = true;
//			}
//		} else {
//			if (cacheRegionsStarted) {
//				break;
//			}
//		}
//	}
//	   return firstConsecutiveRegions;
//   }

   private List<MediumRegion> buildRegionList(boolean withGaps) {
      List<MediumRegion> resultList = new ArrayList<>(regionSizes.size());

      long currentOffset = startOffset;

      for (int i = 0; i < regionSizes.size(); ++i) {
         Integer currentRegionSize = regionSizes.get(i);
         Integer currentGapAfterRegion = gapsAfterRegion.get(i);

         resultList.add(createCachedMediumRegion(currentOffset, currentRegionSize));
         
         if (withGaps && currentGapAfterRegion > 0) {
        	 // Also build a non-cached gap region
             resultList.add(createUnCachedMediumRegion(currentOffset + currentRegionSize,
                     currentGapAfterRegion));
         }

         currentOffset += currentRegionSize + currentGapAfterRegion;
      }

      return resultList;
   }

private MediumRegion createCachedMediumRegion(long offset, Integer size) {
	return new MediumRegion(new StandardMediumReference(regionMedium, offset),
	    createRegionContent(offset, size));
}

private MediumRegion createUnCachedMediumRegion(long offset, Integer size) {
	return new MediumRegion(new StandardMediumReference(regionMedium, offset),
	    size);
}

   public MediumCache buildCache() {
      List<MediumRegion> regionList = getExpectedCacheRegions();

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
 * This is a list index into the list of {@link MediumRegion}s built by the {@link TestCacheLayout} which
 * is created from {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions()}. It refers to the 
 * first {@link MediumRegion} in the default {@link TestCacheLayout} having direct consecutive follow-up {@link MediumRegion}s in the cache.
 */
private final static int DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS = 0;
private final static int DEFAULT_CACHE_INDEX_FIRST_CONSECUTIVE_REGIONS_SIZE = 160;

/**
 * This is a medium offset before the first cached {@link MediumRegion} in the default {@link TestCacheLayout}, which
 * is created from {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions()}.
 */
private final static long DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION = 2L;
/**
 * This is a medium offset within the first gap in the default {@link TestCacheLayout}, which
 * is created from {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions()}.
 */
private final static long DEFAULT_CACHE_LAYOUT_GAP_OFFSET = 200L;


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
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

      MediumCache cache = cacheLayout.buildCache();

      cache.clear();

      assertCacheIsEmpty(cache);
   }

   /**
    * Tests {@link MediumCache#clear()}.
    */
   @Test
   public void clearTwice_forFilledCache_cacheIsStillEmpty() {
      TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

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
		TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache();
		
		assertCacheInvariantsAreFulfilled(cache);
		
		Assert.assertEquals(cacheLayout.getExpectedCacheRegions().size(), cache.getAllCachedRegions().size());
		Assert.assertEquals(cacheLayout.getExpectedCacheRegions(), cache.getAllCachedRegions());
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
		TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

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
      
      Assert.assertEquals(0, emptyCache.getCachedByteCountAt(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION)));
      Assert.assertEquals(0, emptyCache.getCachedByteCountAt(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET)));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_forFilledCacheButWithOffsetOutsideRegions_returnsZero() {
		TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache();

	      Assert.assertEquals(0, cache.getCachedByteCountAt(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION)));
	      Assert.assertEquals(0, cache.getCachedByteCountAt(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET)));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_endOfLastConsecutiveRegion_returnsZero() {
		TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

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
		TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache();
		
		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);
		
		MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);
		Assert.assertEquals(DEFAULT_CACHE_INDEX_FIRST_CONSECUTIVE_REGIONS_SIZE, cache.getCachedByteCountAt(firstConsecutiveRegion.getStartReference()));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_endOfRegionWithConsecutiveRegions_returnsTotalSizeOfSubsequentRegions() {
		TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache();
		
		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);
		
		MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);
		Assert.assertEquals(DEFAULT_CACHE_INDEX_FIRST_CONSECUTIVE_REGIONS_SIZE - firstConsecutiveRegion.getSize(), cache.getCachedByteCountAt(firstConsecutiveRegion.calculateEndReference()));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_middleOfRegionWithConsecutiveRegions_returnsCorrectTotalConsecutiveByteCount() {
		TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache();
		
		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);
		
		MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);
		int middleOffset = firstConsecutiveRegion.getSize() / 2;
		Assert.assertEquals(DEFAULT_CACHE_INDEX_FIRST_CONSECUTIVE_REGIONS_SIZE - middleOffset, cache.getCachedByteCountAt(firstConsecutiveRegion.getStartReference().advance(middleOffset)));
   }

   /**
    * Tests {@link MediumCache#getCachedByteCountAt(IMediumReference)()}.
    */
   @Test
   public void getCachedByteCountAt_middleOfLastConsecutiveRegion_returnsRemainingLastRegionSize() {
		TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache();
		
		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);
		
		MediumRegion lastConsecutiveRegion = firstConsecutiveRegions.get(firstConsecutiveRegions.size() - 1);
		int middleOffset = lastConsecutiveRegion.getSize() / 2;
		Assert.assertEquals(lastConsecutiveRegion.getSize() - middleOffset, cache.getCachedByteCountAt(lastConsecutiveRegion.getStartReference().advance(middleOffset)));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_forEmptyCache_returnsEmptyList() {
      MediumCache emptyCache = new MediumCache(MEDIUM);
      
      Assert.assertEquals(0, emptyCache.getRegionsInRange(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_OFFSET_BEFORE_FIRST_REGION), 10).size());
      Assert.assertEquals(0, emptyCache.getRegionsInRange(new StandardMediumReference(MEDIUM, DEFAULT_CACHE_LAYOUT_GAP_OFFSET), 10).size());
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeCoveringFullCachedRegion_returnsSingleRegion() {
		TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache();
		
		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);
		
		MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);

		List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(firstConsecutiveRegion.getStartReference(), firstConsecutiveRegion.getSize());
		Assert.assertEquals(1, actualRegionsInRange.size());
		Assert.assertEquals(firstConsecutiveRegion, actualRegionsInRange.get(0));
   }

   /**
    * Tests {@link MediumCache#getRegionsInRange(IMediumReference, int)}.
    */
   @Test
   public void getRegionsInRange_rangeWithinCachedRegion_returnsEnclosingRegion() {
		TestCacheLayout cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache();
		
		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);
		
		MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);

		List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(firstConsecutiveRegion.getStartReference().advance(1L), firstConsecutiveRegion.getSize() - 1);
		Assert.assertEquals(1, actualRegionsInRange.size());
		Assert.assertEquals(firstConsecutiveRegion, actualRegionsInRange.get(0));
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

private List<MediumRegion> getFirstConsecutiveRegionsFromDefaultCache(
		TestCacheLayout cacheLayout) {
	List<MediumRegion> allRegions = cacheLayout.getExpectedCacheRegions();
	

	List<MediumRegion> firstConsecutiveRegions = new ArrayList<MediumRegion>();
	
	firstConsecutiveRegions.add(allRegions.get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS));
	firstConsecutiveRegions.add(allRegions.get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS+1));
	firstConsecutiveRegions.add(allRegions.get(DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS+2));
	return firstConsecutiveRegions;
}
   
   /**
    * Verifies that all invariants of the {@link MediumCache} class are fulfilled.	
 * @param cache The {@link MediumCache} instance to check.
 */
private void assertCacheInvariantsAreFulfilled(MediumCache cache) {
	   List<MediumRegion> actualRegions = cache.getAllCachedRegions();
	   
	   long actualCacheSize = 0;
	   IMediumReference previousEndReference = null;
	   for (MediumRegion mediumRegion : actualRegions) {
		actualCacheSize += mediumRegion.getSize();
		
		if (previousEndReference != null) {
			Assert.assertTrue("The current medium region <"+mediumRegion+"> overlaps the previous region which violates the class invariant of " + MediumCache.class.getSimpleName(), mediumRegion.getStartReference().behindOrEqual(previousEndReference));
		}
		
		Assert.assertTrue("The current medium regions size <"+mediumRegion.getSize()+"> must be smaller or equal to the configured maximum cache region size of <"+cache.getMaximumCacheRegionSizeInBytes()	+">", mediumRegion.getSize() <= cache.getMaximumCacheRegionSizeInBytes());
	}
	   
	   Assert.assertTrue("The current total medium region size of <"+actualCacheSize+"> must be smaller or equal to the configured maximum cache size of <"+cache.getMaximumCacheSizeInBytes()	+">", actualCacheSize <= cache.getMaximumCacheSizeInBytes());
   }

   /**
    * Verifies that the given {@link MediumCache} instance is indeed empty.	
 * @param cache The cache instance to check.
 */
private void assertCacheIsEmpty(MediumCache cache) {

      Assert.assertEquals(0L, cache.getCurrentCacheSizeInBytes());
      Assert.assertEquals(0, cache.getAllCachedRegions().size());
      Assert.assertEquals(0, cache
         .getRegionsInRange(new StandardMediumReference(MEDIUM, 0L), (int) Math.min(cache.getMaximumCacheSizeInBytes(), Integer.MAX_VALUE)).size());
   }

   /**
    * Creates a {@link TestCacheLayout} with unlimited sizes and some pre-existing content: (1) Three consecutive
    * regions of different sizes starting at an offset > 0, (2) A gap of 200 bytes, (3) Two consecutive regions, (4) A
    * gap of 1 byte, (5) A last region
    * 
    * @return The default {@link MediumCache} pre-filled and with unlimited sizes
    */
   private TestCacheLayout createDefaultLayoutHavingSubsequentAndScatteredRegions() {
      TestCacheLayout cacheLayout = new TestCacheLayout(5L, MEDIUM, MediumCache.UNLIMITED_CACHE_SIZE,
         MediumCache.UNLIMITED_CACHE_REGION_SIZE);

      cacheLayout.addNextRegionInfo(40, 0);
      cacheLayout.addNextRegionInfo(20, 0);
      cacheLayout.addNextRegionInfo(100, 200);
      cacheLayout.addNextRegionInfo(5, 0);
      cacheLayout.addNextRegionInfo(27, 1);
      cacheLayout.addNextRegionInfo(350, 0);

      return cacheLayout;
   }
}
