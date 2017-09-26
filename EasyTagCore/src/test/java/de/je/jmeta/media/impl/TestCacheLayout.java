package de.je.jmeta.media.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.Reject;

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
      Reject.ifNegative(startOffset, "startOffset");

      this.startOffset = startOffset;
      this.regionMedium = regionMedium;
      this.maxCacheSize = maxCacheSize;
      this.maxCacheRegionSize = maxCacheRegionSize;
   }

   public long getStartOffset() {
      return startOffset;
   }

   public long getTotalRegionSizeInBytes() {
      return totalRegionSizeInBytes;
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

   public MediumCache buildCache() {
      List<MediumRegion> regionList = getAllCachedRegions();
   
      MediumCache resultingCache = new MediumCache(regionMedium, maxCacheSize, maxCacheRegionSize);
   
      for (MediumRegion mediumRegion : regionList) {
         resultingCache.addRegion(mediumRegion);
      }
   
      return resultingCache;
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

   private static ByteBuffer createRegionContent(long offset, int size) {
      byte[] content = new byte[size];

      for (int i = 0; i < content.length; i++) {
         content[i] = (byte) ((offset + i) % Byte.MAX_VALUE);
      }

      return ByteBuffer.wrap(content);
   }
}