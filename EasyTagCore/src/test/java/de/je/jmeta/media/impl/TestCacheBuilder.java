package de.je.jmeta.media.impl;

import static de.je.jmeta.media.impl.TestMediumUtility.createCachedMediumRegion;
import static de.je.jmeta.media.impl.TestMediumUtility.createUnCachedMediumRegion;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.datatype.MediumRegion;
import de.je.util.javautil.common.err.Reject;

/**
 * This helper class helps to easily define the structure and contents of a {@link MediumCache} instance declaratively.
 * It removes the burden of creating {@link MediumRegion}s for the correct medium with the correct references from the
 * test automater by just working with plain offsets. It furthermore accumulates the (expected) total cache size and
 * provides the helper method {@link #getAllCachedRegionsWithGaps()} for testing
 * {@link MediumCache#getRegionsInRange(de.je.jmeta.media.api.IMediumReference, int)}. You can build the
 * {@link MediumCache} instance for testing from the {@link TestCacheBuilder} using the method
 * {@link #buildCache(long, int)}.
 * 
 * Instances of this class are also used to defined the <i>expected</i> structure of a {@link MediumCache} when testing
 * {@link MediumCache#addRegion(MediumRegion)}. For this, it provides copy mechanisms from an existing
 * {@link TestCacheBuilder} and incremental manipulation methods.
 *
 */
class TestCacheBuilder {

   /**
    * Represents all necessary about a potential cached {@link MediumRegion} to create by the {@link TestCacheBuilder}.
    */
   public static class TestCacheRegionInfo {

      private final long regionStartOffset;
      private final int regionSize;
      private final byte[] regionBytes;

      /**
       * Creates a new instance, thereby generating the region bytes using
       * {@link TestMediumUtility#createRegionContent(long, int)}.
       * 
       * @param regionStartOffset
       *           The start offset to use, must not be negative and must be smaller than the end offset.
       * @param regionEndOffset
       *           The end offset to use, must not be negative and must be bigger than the start offset
       */
      public TestCacheRegionInfo(long regionStartOffset, long regionEndOffset) {
         this(regionStartOffset,
            TestMediumUtility.createRegionContent(regionStartOffset, (int) (regionEndOffset - regionStartOffset)));
      }

      /**
       * Creates a new instance using the given region bytes.
       * 
       * @param regionStartOffset
       *           The start offset to use. Must be positive or zero.
       * @param regionBytes
       *           The region's bytes to use
       */
      public TestCacheRegionInfo(long regionStartOffset, byte[] regionBytes) {
         Reject.ifNegative(regionStartOffset, "regionStartOffset");
         Reject.ifNull(regionBytes, "regionBytes");

         this.regionStartOffset = regionStartOffset;
         this.regionSize = regionBytes.length;
         this.regionBytes = regionBytes;
      }

      /**
       * @return the region's start offset
       */
      public long getRegionStartOffset() {
         return regionStartOffset;
      }

      /**
       * @return the region's end offset
       */
      public long getRegionEndOffset() {
         return regionStartOffset + regionSize;
      }

      /**
       * @return the region's size
       */
      public int getRegionSize() {
         return regionSize;
      }

      /**
       * @return the region's bytes
       */
      public byte[] getRegionBytes() {
         return regionBytes;
      }
   }

   private final List<TestCacheRegionInfo> regionInfos;

   private final IMedium<?> regionMedium;

   /**
    * Creates a new empty {@link TestCacheBuilder} for the given {@link IMedium}. After this, it can be populated using
    * {@link #addNextRegionInfo(long, long)}.
    * 
    * @param regionMedium
    *           The {@link IMedium} to use.
    */
   public TestCacheBuilder(IMedium<?> regionMedium) {
      Reject.ifNull(regionMedium, "regionMedium");

      this.regionInfos = new ArrayList<>();
      this.regionMedium = regionMedium;
   }

   /**
    * Creates a new {@link TestCacheBuilder} containing the copied region info data of the given other
    * {@link TestCacheBuilder}.
    * 
    * @param builderToCopy
    *           The {@link TestCacheBuilder} to copy.
    */
   public TestCacheBuilder(TestCacheBuilder builderToCopy) {
      Reject.ifNull(builderToCopy, "builderToCopy");

      this.regionMedium = builderToCopy.regionMedium;
      this.regionInfos = new ArrayList<>(builderToCopy.regionInfos);
   }

   /**
    * @return 0L if this {@link TestCacheBuilder} is still empty, otherwise the start offset of the first region in the
    *         layout.
    */
   public long getStartOffset() {
      return regionInfos.isEmpty() ? 0L : regionInfos.get(0).getRegionStartOffset();
   }

   /**
    * @return 0L if this {@link TestCacheBuilder} is still empty, otherwise the end offset of the last region in the
    *         layout.
    */
   public long getEndOffset() {
      return regionInfos.isEmpty() ? 0L : regionInfos.get(regionInfos.size() - 1).getRegionEndOffset();
   }

   /**
    * @return the accumulated total region size of this {@link TestCacheBuilder}.
    */
   public long getTotalRegionSizeInBytes() {
      return regionInfos.stream().collect(Collectors.summingLong(regionInfo -> regionInfo.getRegionSize()));
   }

   /**
    * @return all cached {@link MediumRegion}s currently defined in this {@link TestCacheBuilder} in offset order.
    */
   public List<MediumRegion> getAllCachedRegions() {
      return buildRegionList(false);
   }

   /**
    * @return all cached {@link MediumRegion}s currently defined in this {@link TestCacheBuilder} plus any uncached gap
    *         {@link MediumRegion}s between them, in offset order.
    */
   public List<MediumRegion> getAllCachedRegionsWithGaps() {
      return buildRegionList(true);
   }

   /**
    * Adds the necessary information for a new cached {@link MediumRegion}s to be contained in the {@link MediumCache}
    * to build. You specify the region based on its start and end offset. Where are the region bytes? You cannot pass
    * them here, but they are automatically generated in a reproducible way. The byte at offset x has the value
    * "x MOD Byte.MAX_VALUE". This way, on the one hand you can detect any problems with wrong bytes in the cache (as
    * most bytes should differ from each other), on the other hand, you are not surprised which byte is where.
    * 
    * This method is incremental in a sense that you have to build the cache step by step from lower offset regions to
    * higher offset regions. You cannot add a region with offset 10 after you have already added a region with offset
    * 1000.
    * 
    * @param regionStartOffset
    *           The start offset to use, must be smaller than the end offset, must be bigger or equal to the last added
    *           region end offset.
    * @param regionEndOffset
    *           The end offset to use, must be bigger than the start offset
    */
   public void addNextRegionInfo(long regionStartOffset, long regionEndOffset) {
      addNextRegionInfo(new TestCacheRegionInfo(regionStartOffset, regionEndOffset));
   }

   /**
    * Adds a a region information based on an existing {@link MediumRegion} to this {@link TestCacheBuilder}. Basically
    * it takes the offset, size and byte information from the existing {@link MediumRegion}. This will lead to the equal
    * {@link MediumRegion} created later by {@link #buildCache(long, int)}.
    * 
    * @param region
    *           The {@link MediumRegion} to for which to add a region info. The {@link MediumRegion} must not overlap
    *           any previously added region info and its offset must be behind any existing region infos.
    */
   public void addNextRegionInfoFromRegion(MediumRegion region) {
      Reject.ifNull(region, "region");

      addNextRegionInfo(regionInfoFromMediumRegion(region));
   }

   private TestCacheRegionInfo regionInfoFromMediumRegion(MediumRegion region) {
      ByteBuffer bytes = region.getBytes();
      byte[] regionBytes = new byte[bytes.remaining()];

      bytes.get(regionBytes);

      TestCacheRegionInfo testCacheRegionInfoToAdd = new TestCacheRegionInfo(
         region.getStartReference().getAbsoluteMediumOffset(), regionBytes);
      return testCacheRegionInfoToAdd;
   }

   /**
    * Adds a a region information based on an existing {@link MediumRegion} to this {@link TestCacheBuilder}. Basically
    * it takes the offset, size and byte information from the existing {@link MediumRegion}. This will lead to the equal
    * {@link MediumRegion} created later by {@link #buildCache(long, int)}.
    * 
    * @param regionIndex
    *           Identifies the region before which to insert the hew region information.
    * @param region
    *           The {@link MediumRegion} to for which to add a region info. The {@link MediumRegion} must not overlap
    *           any previously added region info and its offset must be behind any existing region infos.
    */
   public void insertRegionInfoFromRegion(int regionIndex, MediumRegion region) {
      Reject.ifNotInInterval(regionIndex, 0, regionInfos.size() - 1, "regionIndex");
      Reject.ifNull(region, "region");

      regionInfos.add(regionIndex, regionInfoFromMediumRegion(region));
   }

   /**
    * Actually adds the given {@link TestCacheRegionInfo} to the internal data structures after validating it.
    * 
    * @param testCacheRegionInfoToAdd
    *           the {@link TestCacheRegionInfo} to add.
    */
   private void addNextRegionInfo(TestCacheRegionInfo testCacheRegionInfoToAdd) {
      if (!regionInfos.isEmpty()) {
         long lastAddedRegionInfoEndOffset = regionInfos.get(regionInfos.size() - 1).getRegionEndOffset();

         Reject.ifFalse(testCacheRegionInfoToAdd.getRegionStartOffset() >= lastAddedRegionInfoEndOffset,
            "regionStartOffset >= lastAddedRegionEndOffset");
      }

      regionInfos.add(testCacheRegionInfoToAdd);
   }

   /**
    * Trims the region info identified by the given index at its front, effectively making it smaller and removing bytes
    * at its front, shifting also its start offset by the corresponding number of trimmed bytes.
    * 
    * @param regionIndex
    *           Identifies the region to be modified, must be a valid index in the existing region infos.
    * @param removeBytesFromStart
    *           Number of bytes to trim away at the start of the original existing region, must not be negative or zero.
    *           Must not be bigger than the original region info's size minus 1.
    */
   public void trimRegionInfoAtFront(int regionIndex, int removeBytesFromStart) {
      Reject.ifNotInInterval(regionIndex, 0, regionInfos.size() - 1, "regionIndex");

      TestCacheRegionInfo originalRegionInfo = regionInfos.get(regionIndex);

      Reject.ifNotInInterval(removeBytesFromStart, 1, originalRegionInfo.getRegionSize() - 1, "removeBytesFromStart");

      byte[] remainingRegionBytes = new byte[originalRegionInfo.getRegionSize() - removeBytesFromStart];

      System.arraycopy(originalRegionInfo.getRegionBytes(), removeBytesFromStart, remainingRegionBytes, 0,
         remainingRegionBytes.length);

      TestCacheRegionInfo newRegionInfo = new TestCacheRegionInfo(
         originalRegionInfo.getRegionStartOffset() + removeBytesFromStart, remainingRegionBytes);

      regionInfos.set(regionIndex, newRegionInfo);
   }

   /**
    * Trims the region info identified by the given index at its front, effectively making it smaller and removing bytes
    * at its front, shifting also its start offset by the corresponding number of trimmed bytes.
    * 
    * @param regionIndex
    *           Identifies the region to be modified, must be a valid index in the existing region infos.
    * @param removeBytesFromEnd
    *           Number of bytes to trim away at the end of the original existing region, must not be negative or zero.
    *           Must not be bigger than the original region info's size minus 1.
    */
   public void trimRegionInfoAtBack(int regionIndex, int removeBytesFromEnd) {
      Reject.ifNotInInterval(regionIndex, 0, regionInfos.size() - 1, "regionIndex");

      TestCacheRegionInfo originalRegionInfo = regionInfos.get(regionIndex);

      Reject.ifNotInInterval(removeBytesFromEnd, 1, originalRegionInfo.getRegionSize() - 1, "removeBytesFromEnd");

      byte[] remainingRegionBytes = new byte[originalRegionInfo.getRegionSize() - removeBytesFromEnd];

      System.arraycopy(originalRegionInfo.getRegionBytes(), 0, remainingRegionBytes, 0, remainingRegionBytes.length);

      TestCacheRegionInfo newRegionInfo = new TestCacheRegionInfo(originalRegionInfo.getRegionStartOffset(),
         remainingRegionBytes);

      regionInfos.set(regionIndex, newRegionInfo);
   }

   /**
    * Replaces the content of the region identified by the given index with new region bytes. Essentially only a single
    * fill byte is used.
    * 
    * @param regionIndex
    *           Identifies the region to be modified, must be a valid index in the existing region infos.
    * @param newRegionFillByte
    *           This byte is used as the content of the replacement region, replacing the original region bytes.
    */
   public void replaceRegion(int regionIndex, byte newRegionFillByte) {
      Reject.ifNotInInterval(regionIndex, 0, regionInfos.size() - 1, "regionIndex");

      TestCacheRegionInfo originalRegion = regionInfos.get(regionIndex);

      byte[] replacementRegionBytes = new byte[originalRegion.getRegionSize()];

      Arrays.fill(replacementRegionBytes, newRegionFillByte);

      regionInfos.set(regionIndex,
         new TestCacheRegionInfo(originalRegion.getRegionStartOffset(), replacementRegionBytes));
   }

   /**
    * Removes the region identified by the given index.
    * 
    * @param regionIndex
    *           Identifies the region to be removed, must be a valid index in the existing region infos.
    */
   public void removeRegion(int regionIndex) {
      Reject.ifNotInInterval(regionIndex, 0, regionInfos.size() - 1, "regionIndex");

      regionInfos.remove(regionIndex);
   }

   /**
    * Builds a {@link MediumCache} instance from this {@link TestCacheBuilder} for testing, using all
    * {@link MediumRegion} information currently contained in it. It does not do anything special to ensure that the
    * regions are below the given maximum region or maximum cache size. This has to be ensured manually by the test
    * automater.
    * 
    * @param maxCacheSize
    *           The maximum cache size to use.
    * @param maxCacheRegionSize
    *           The maximum region size to use.
    * @return The built {@link MediumCache} for testing.
    */
   public MediumCache buildCache(long maxCacheSize, int maxCacheRegionSize) {
      List<MediumRegion> regionList = getAllCachedRegions();

      MediumCache resultingCache = new MediumCache(regionMedium, maxCacheSize, maxCacheRegionSize);

      for (MediumRegion mediumRegion : regionList) {
         resultingCache.addRegion(mediumRegion);
      }

      return resultingCache;
   }

   /**
    * Builds the list of all {@link MediumRegion}s in this {@link TestCacheBuilder} with or without uncached gap
    * {@link MediumRegion}s.
    * 
    * @param withGaps
    *           true if uncached gap {@link MediumRegion}s must also be contained, false for only cached
    *           {@link MediumRegion}s.
    * @return the list of all {@link MediumRegion}s in this {@link TestCacheBuilder} with or without uncached gap
    *         {@link MediumRegion}s, in offset order.
    */
   private List<MediumRegion> buildRegionList(boolean withGaps) {
      List<MediumRegion> resultList = new ArrayList<>(regionInfos.size());

      Long lastRegionEndOffset = null;

      for (TestCacheRegionInfo testCacheRegionInfo : regionInfos) {
         Long currentRegionStartOffset = testCacheRegionInfo.getRegionStartOffset();
         Long currentRegionEndOffset = testCacheRegionInfo.getRegionEndOffset();

         // Also build a non-cached gap region
         if (withGaps && lastRegionEndOffset != null && currentRegionStartOffset > lastRegionEndOffset) {
            resultList.add(createUnCachedMediumRegion(regionMedium, lastRegionEndOffset,
               (int) (currentRegionStartOffset - lastRegionEndOffset)));
         }

         resultList
            .add(createCachedMediumRegion(regionMedium, currentRegionStartOffset, testCacheRegionInfo.getRegionSize()));

         lastRegionEndOffset = currentRegionEndOffset;
      }

      return resultList;
   }
}