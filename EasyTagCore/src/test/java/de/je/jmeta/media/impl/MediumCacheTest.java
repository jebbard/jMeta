package de.je.jmeta.media.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.MediaTestCaseConstants;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.datatype.InMemoryMedium;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.PreconditionException;
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

   public TestCacheLayout(long startOffset, IMedium<?> regionMedium, long maxCacheSize, int maxCacheRegionSize) {
      Reject.ifNull(regionMedium, "regionMedium");
      Contract.checkPrecondition(startOffset >= 0, "startOffset >= 0 was false");

      this.startOffset = startOffset;
      this.regionMedium = regionMedium;
      this.maxCacheSize = maxCacheSize;
      this.maxCacheRegionSize = maxCacheRegionSize;
   }

   public void addNextRegionInfo(int regionSize, int gapAfterRegion) {
      Contract.checkPrecondition(regionSize > 0, "regionSize > 0 was false");
      Contract.checkPrecondition(gapAfterRegion >= 0, "gapAfterRegion >= 0 was false");

      regionSizes.add(regionSize);
      gapsAfterRegion.add(gapAfterRegion);
      totalRegionSizeInBytes += regionSize;
   }

   public List<MediumRegion> getExpectedRegionList() {
      return buildRegionList();
   }

   private List<MediumRegion> buildRegionList() {
      List<MediumRegion> resultList = new ArrayList<>(regionSizes.size());

      long currentOffset = startOffset;

      for (int i = 0; i < regionSizes.size(); ++i) {
         Integer currentRegionSize = regionSizes.get(i);
         Integer currentGapAfterRegion = gapsAfterRegion.get(i);

         resultList.add(new MediumRegion(new StandardMediumReference(regionMedium, currentOffset),
            createRegionContent(currentOffset, currentRegionSize)));

         currentOffset += currentGapAfterRegion;
      }

      return resultList;
   }

   public MediumCache buildCache() {
      List<MediumRegion> regionList = buildRegionList();

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
   @Test(expected = PreconditionException.class)
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
      TestCacheLayout cacheLayout = createCacheLayoutHavingContentWithGaps();

      MediumCache cache = cacheLayout.buildCache();

      cache.clear();

      assertCacheIsEmpty(cache);
   }

   /**
    * Tests {@link MediumCache#clear()}.
    */
   @Test
   public void clearTwice_forFilledCache_cacheIsStillEmpty() {
      TestCacheLayout cacheLayout = createCacheLayoutHavingContentWithGaps();

      MediumCache cache = cacheLayout.buildCache();

      cache.clear();

      assertCacheIsEmpty(cache);

      cache.clear();

      assertCacheIsEmpty(cache);
   }

   private void assertCacheIsEmpty(MediumCache cache) {

      Assert.assertEquals(0L, cache.getCurrentCacheSizeInBytes());
      Assert.assertEquals(0, cache.getAllCachedRegions().size());
      Assert.assertEquals(0, cache
         .getRegionsInRange(new StandardMediumReference(MEDIUM, 0L), (int) cache.getMaximumCacheSizeInBytes()).size());
   }

   /**
    * Creates a {@link TestCacheLayout} with unlimited sizes and some pre-existing content: (1) Three consecutive
    * regions of different sizes starting at an offset > 0, (2) A gap of 200 bytes, (3) Two consecutive regions, (4) A
    * gap of 1 byte, (5) A last region
    * 
    * @return The default {@link MediumCache} pre-filled and with unlimited sizes
    */
   private TestCacheLayout createCacheLayoutHavingContentWithGaps() {
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
