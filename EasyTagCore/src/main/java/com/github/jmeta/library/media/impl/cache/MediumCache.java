package com.github.jmeta.library.media.impl.cache;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import com.github.jmeta.library.media.api.type.IMedium;
import com.github.jmeta.library.media.api.type.IMediumReference;
import com.github.jmeta.library.media.api.type.MediumRegion;
import com.github.jmeta.library.media.api.type.MediumRegion.MediumRegionOverlapType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * Represents a permanent in-memory cache for an {@link IMedium}. It provides methods for adding and retrieving cache
 * data. Cache data is represented using {@link MediumRegion} instances. The cache itself and its {@link MediumRegion}s
 * adhere to a maximum size that can be passed to the constructor of this class.
 * 
 * This class has the following invariants, i.e. conditions that are guaranteed to be met at any time for outside users:
 * <ul>
 * <li>The {@link MediumRegion}s stored in this {@link MediumCache} do never overlap</li>
 * <li>The maximum cache size is never exceeded</li>
 * <li>The maximum size of a cache region is never exceeded by any region</li>
 * </ul>
 */
public class MediumCache {

   /**
    * The default maximum cache size can be interpreted as virtually unlimited size
    */
   public static final long UNLIMITED_CACHE_SIZE = Long.MAX_VALUE;
   /**
    * The default maximum cache region size can be interpreted as virtually unlimited size
    */
   public static final int UNLIMITED_CACHE_REGION_SIZE = Integer.MAX_VALUE;

   private final long maximumCacheSizeInBytes;
   private final int maximumCacheRegionSizeInBytes;
   private final IMedium<?> medium;

   private static final Comparator<IMediumReference> OFFSET_ORDER_ASCENDING_COMPARATOR = (leftRef,
      rightRef) -> new Long(leftRef.getAbsoluteMediumOffset()).compareTo(rightRef.getAbsoluteMediumOffset());

   private final TreeMap<IMediumReference, MediumRegion> cachedRegionsInOffsetOrder = new TreeMap<>(
      OFFSET_ORDER_ASCENDING_COMPARATOR);
   private final List<MediumRegion> cachedRegionsInInsertOrder = new LinkedList<>();

   /**
    * This constructor initializes the cache with {@link #UNLIMITED_CACHE_SIZE} as maximum cache size and
    * {@link #UNLIMITED_CACHE_REGION_SIZE} as maximum cache region size. Note that these sizes remain unchanged
    * throughout the lifetime of a {@link MediumCache} instance.
    * 
    * @param medium
    *           The {@link IMedium} instance this cache is based on.
    */
   public MediumCache(IMedium<?> medium) {
      this(medium, UNLIMITED_CACHE_SIZE, UNLIMITED_CACHE_REGION_SIZE);
   }

   /**
    * Creates a new {@link MediumCache} instance with the given maximum cache size and maximum cache region size. Note
    * that these sizes remain unchanged throughout the lifetime of a {@link MediumCache} instance.
    * 
    * @param medium
    *           The {@link IMedium} instance this cache is based on.
    * @param maximumCacheSizeInBytes
    *           The maximum cache size in bytes, must be bigger than zero.
    * @param maximumCacheRegionSizeInBytes
    *           The maximum cache size in bytes, must be bigger than zero. Must be smaller than the maximum cache size
    *           given in the other parameter.
    */
   public MediumCache(IMedium<?> medium, long maximumCacheSizeInBytes, int maximumCacheRegionSizeInBytes) {

      Reject.ifNull(medium, "medium");

      Reject.ifFalse(maximumCacheSizeInBytes >= maximumCacheRegionSizeInBytes,
         "maximumCacheSizeInBytes >= maximumCacheRegionSizeInBytes");

      this.medium = medium;
      this.maximumCacheSizeInBytes = maximumCacheSizeInBytes;
      this.maximumCacheRegionSizeInBytes = maximumCacheRegionSizeInBytes;
   }

   /**
    * @return The {@link IMedium} instance this cache is used for.
    */
   public IMedium<?> getMedium() {
      return medium;
   }

   /**
    * @return the maximum cache size in bytes
    */
   public long getMaximumCacheSizeInBytes() {
      return maximumCacheSizeInBytes;
   }

   /**
    * @return the maximum cache region size in bytes
    */
   public int getMaximumCacheRegionSizeInBytes() {
      return maximumCacheRegionSizeInBytes;
   }

   /**
    * Calculates the current cache size in bytes.
    * 
    * @return the current cache size in bytes
    */
   public long calculateCurrentCacheSizeInBytes() {
      return cachedRegionsInInsertOrder.stream().collect(Collectors.summingLong(region -> region.getSize()));
   }

   /**
    * @return all {@link MediumRegion}s currently maintained in this {@link MediumCache}, ordered by their
    *         {@link IMediumReference} ascending. If there are none currently, returns an empty {@link List}.
    */
   public List<MediumRegion> getAllCachedRegions() {
      return new ArrayList<>(cachedRegionsInOffsetOrder.values());
   }

   /**
    * Returns {@link MediumRegion}s covering the whole range specified as input parameters. This includes
    * {@link MediumRegion}s actually contained in the cache, overlapping the given range, as well as all "gaps" in the
    * given range, i.e. non-cached regions. All returned {@link MediumRegion}s are guaranteed to have a maximum size of
    * {@link #getMaximumCacheRegionSizeInBytes()}.
    * 
    * Examples:
    * <ul>
    * <li>If the whole range is cached, returns all {@link MediumRegion}s that cover or overlap the given range. If a
    * {@link MediumRegion} starting before the given start reference and overlapping the range, it is returned as first
    * {@link MediumRegion}. If a {@link MediumRegion} starts before start reference + rangeSize and overlaps the range
    * at its back, it is returned as last {@link MediumRegion} in the result list.</li>
    * <li>If the cache does not contain any {@link MediumRegion}s overlapping the given range, nevertheless it returns
    * non-cached {@link MediumRegion} instances without data, covering the whole range. If rangeSize is smaller then the
    * configured {@link #getMaximumCacheRegionSizeInBytes()}, it returns as much {@link MediumRegion}s with the maximum
    * configured size as required to cover the range.</li>
    * <li>If the cache e.g. contains two {@link MediumRegion}s within the given range (starting behind the start
    * reference and ending before start reference + range size), but between both there is a gap, overall the methods
    * returns five {@link MediumRegion} instances. The first covering the region up to the start of the cached region,
    * second the first cached region, third a {@link MediumRegion} representing the gap between the two cached regions,
    * fourth the second cached {@link MediumRegion}, and fifth a {@link MediumRegion} representing the remaining region
    * of the range after the second cached {@link MediumRegion}.</li>
    * </ul>
    * 
    * @param startReference
    *           The starting {@link IMediumReference} of the range. Must refer to the same {@link IMedium} as returned
    *           by {@link #getMedium()}.
    * @param rangeSizeInBytes
    *           The size of the range in bytes. Must not be negative and must not be zero.
    * @return {@link MediumRegion}s covering the whole range specified as input parameters. For details see the method
    *         description above.
    */
   public List<MediumRegion> getRegionsInRange(IMediumReference startReference, int rangeSizeInBytes) {

      Reject.ifNull(startReference, "startReference");
      Reject.ifFalse(startReference.getMedium().equals(getMedium()), "startReference.getMedium().equals(getMedium())");
      Reject.ifTrue(rangeSizeInBytes <= 0, "The range size must be strictly bigger than zero");

      List<MediumRegion> regionsInRange = new ArrayList<>();

      if (cachedRegionsInOffsetOrder.isEmpty()) {
         return regionsInRange;
      }

      MediumRegion virtualRangeRegion = new MediumRegion(startReference, rangeSizeInBytes);

      IMediumReference endReference = virtualRangeRegion.calculateEndReference();

      IMediumReference firstCachedRegionReferenceNearToStartReference = cachedRegionsInOffsetOrder
         .floorKey(startReference);

      if (firstCachedRegionReferenceNearToStartReference == null) {
         firstCachedRegionReferenceNearToStartReference = cachedRegionsInOffsetOrder.ceilingKey(startReference);

         if (firstCachedRegionReferenceNearToStartReference == null) {
            throw new IllegalStateException("The cache is not empty, but no " + IMediumReference.class.getSimpleName()
               + " was found that is smaller, equal or bigger than the given start reference <" + startReference
               + ">. This must never happen.");
         }
      }

      MediumRegion firstCachedRegionNearToStartReference = cachedRegionsInOffsetOrder
         .get(firstCachedRegionReferenceNearToStartReference);

      MediumRegionOverlapType overlapWithFirstRegion = MediumRegion.determineOverlapWithOtherRegion(virtualRangeRegion,
         firstCachedRegionNearToStartReference);

      if (overlapWithFirstRegion == MediumRegionOverlapType.LEFT_FULLY_INSIDE_RIGHT) {
         regionsInRange.add(firstCachedRegionNearToStartReference);
         return regionsInRange;
      }

      Map<IMediumReference, MediumRegion> cachedRegionsStartingWithFirstNearRangeStart = cachedRegionsInOffsetOrder
         .tailMap(firstCachedRegionReferenceNearToStartReference);

      IMediumReference previousRegionEndReference = startReference;

      for (MediumRegion currentRegion : cachedRegionsStartingWithFirstNearRangeStart.values()) {
         IMediumReference currentRegionStartReference = currentRegion.getStartReference();
         IMediumReference currentRegionEndReference = currentRegion.calculateEndReference();

         MediumRegionOverlapType overlapWithRangeRegion = MediumRegion.determineOverlapWithOtherRegion(currentRegion,
            virtualRangeRegion);

         // Only process cached region if it overlaps with range
         if (overlapWithRangeRegion != MediumRegionOverlapType.NO_OVERLAP) {
            regionsInRange.addAll(
               getGapsBetweenCurrentAndPreviousCachedRegion(previousRegionEndReference, currentRegionStartReference));

            regionsInRange.add(currentRegion);

            previousRegionEndReference = currentRegionEndReference;
         }
      }

      regionsInRange.addAll(getGapsBetweenCurrentAndPreviousCachedRegion(previousRegionEndReference, endReference));

      return regionsInRange;
   }

   /**
    * Returns the number of consecutively cached bytes starting the the given start {@link IMediumReference}. If there
    * is no cached byte at the given start reference, returns 0L. THe methods stops at the first non cached byte behind
    * start reference, so whenever the first gap is detected.
    * 
    * @param startReference
    *           The starting {@link IMediumReference}. Must refer to the same {@link IMedium} as returned by
    *           {@link #getMedium()}.
    * @return the number of consecutively cached bytes starting the the given start {@link IMediumReference}. For
    *         details see the method description above.
    */
   public long getCachedByteCountAt(IMediumReference startReference) {

      Reject.ifNull(startReference, "startReference");
      Reject.ifFalse(startReference.getMedium().equals(getMedium()), "startReference.getMedium().equals(getMedium())");

      long totalCachedByteCount = 0L;

      IMediumReference previousOrEqualReference = cachedRegionsInOffsetOrder.floorKey(startReference);

      if (previousOrEqualReference != null) {
         MediumRegion previousRegion = cachedRegionsInOffsetOrder.get(previousOrEqualReference);

         if (previousRegion.contains(startReference)) {
            totalCachedByteCount += previousRegion.getSize()
               - startReference.distanceTo(previousRegion.getStartReference());

            Map<IMediumReference, MediumRegion> tailRegions = cachedRegionsInOffsetOrder.tailMap(startReference, false);

            for (Iterator<IMediumReference> iterator = tailRegions.keySet().iterator(); iterator.hasNext();) {
               IMediumReference nextReference = iterator.next();

               MediumRegion nextRegion = tailRegions.get(nextReference);

               // Consecutive region
               if (nextRegion.getStartReference().equals(previousRegion.calculateEndReference())) {
                  totalCachedByteCount += nextRegion.getSize();
               } else {
                  break;
               }

               previousRegion = nextRegion;
            }
         }
      }

      return totalCachedByteCount;
   }

   /**
    * Adds a region of fresh data to this {@link MediumCache}. If the region is bigger than the configured
    * {@link #getMaximumCacheRegionSizeInBytes()} it is split correspondingly. Any already existing cache regions are
    * split (if partly overlapped by the new {@link MediumRegion}) or removed (if entirely covered by the new
    * {@link MediumRegion}) to ensure there are no overlapping regions and the new data replaces existing old data in
    * the cache. If the cache size would grow beyond the configured {@link #getMaximumCacheSizeInBytes()}, previously
    * added {@link MediumRegion}s are automatically removed from the cache according to a FIFO approach: The
    * {@link MediumRegion}s added first to the {@link MediumCache} are removed first, until the cache is again smaller
    * than the maximum cache size, including the new {@link MediumRegion}. If the new region by itself is already bigger
    * than the configured {@link #getMaximumCacheSizeInBytes()}, it is split such that each resulting
    * {@link MediumRegion} is smaller than {@link #getMaximumCacheRegionSizeInBytes()}, and only those
    * {@link MediumRegion}s with highest offset are added to the cache such that its maximum size is not exceeded. I.e.
    * it might occur for {@link MediumRegion}s bigger than the maximum cache size that their starting bytes are not
    * added to the cache.
    * 
    * @param regionToAdd
    *           The new {@link MediumRegion} to add. Must contain data and must refer to the same {@link IMedium} as
    *           returned by {@link #getMedium()}.
    */
   public void addRegion(MediumRegion regionToAdd) {
      Reject.ifNull(regionToAdd, "regionToAdd");
      Reject.ifFalse(regionToAdd.getStartReference().getMedium().equals(getMedium()),
         "region.getStartReference().getMedium().equals(getMedium())");

      // First we existing regions overlapped by the new region
      List<MediumRegion> regionsInRange = getRegionsInRange(regionToAdd.getStartReference(), regionToAdd.getSize());

      regionsInRange.stream().filter(regionInRange -> regionInRange.isCached())
         .forEach(existingRegion -> clipExistingRegionAgainstRegionToAdd(existingRegion, regionToAdd));

      // Then we ensure the new region is divided into subregions with max region size, if necessary
      // and we add all divided subregions into the cache
      List<MediumRegion> regionsToAdd = MediumRangeChunkAction.walkDividedRange(MediumRegion.class,
         regionToAdd.getStartReference(), regionToAdd.getSize(), getMaximumCacheRegionSizeInBytes(),
         (chunkStartReference, chunkSize) -> splitRegionExceedingMaxCacheRegionSize(regionToAdd, chunkStartReference,
            chunkSize));

      regionsToAdd.stream().forEach(this::addRegionToCache);

      // Finally, we check if the maximum cache size is exceeded and remove any existing regions first to ensure the
      // size decreases below; even the first parts of the new region are removed, if necessary
      long newCacheSize = calculateCurrentCacheSizeInBytes();

      List<MediumRegion> regionsToRemove = new ArrayList<>();

      Iterator<MediumRegion> cachedRegionsInInsertOrderIterator = cachedRegionsInInsertOrder.iterator();

      while (cachedRegionsInInsertOrderIterator.hasNext() && newCacheSize > getMaximumCacheSizeInBytes()) {
         MediumRegion nextRegion = cachedRegionsInInsertOrderIterator.next();

         regionsToRemove.add(nextRegion);

         newCacheSize -= nextRegion.getSize();
      }

      regionsToRemove.stream().forEach(this::removeRegionFromCache);
   }

   /**
    * Removes up all {@link MediumRegion}s currently contained in this {@link MediumCache} instance, effectively
    * emptying the cache.
    */
   public void clear() {
      cachedRegionsInInsertOrder.clear();
      cachedRegionsInOffsetOrder.clear();
   }

   /**
    * Determines uncached gap {@link MediumRegion}s for the given range with a maximum size of
    * {@link #getMaximumCacheRegionSizeInBytes()} each.
    * 
    * @param previousRegionEndReference
    *           The start {@link IMediumReference} of the range
    * @param currentRegionStartReference
    *           The start {@link IMediumReference} of the range
    * @return all uncached gap {@link MediumRegion}s in the range.
    */
   private List<MediumRegion> getGapsBetweenCurrentAndPreviousCachedRegion(IMediumReference previousRegionEndReference,
      IMediumReference currentRegionStartReference) {

      List<MediumRegion> gapRegions = new ArrayList<>();

      // Gap detected! - Determine uncached region(s) for gap
      if (previousRegionEndReference.before(currentRegionStartReference)) {
         int gapSizeInBytes = (int) currentRegionStartReference.distanceTo(previousRegionEndReference);

         gapRegions.addAll(MediumRangeChunkAction.walkDividedRange(MediumRegion.class, previousRegionEndReference,
            gapSizeInBytes, getMaximumCacheRegionSizeInBytes(),
            (chunkStartReference, chunkSize) -> new MediumRegion(chunkStartReference, chunkSize)));
      }

      return gapRegions;
   }

   /**
    * This method performs the actual splitting of a {@link MediumRegion} according to the maximum allowed cache region
    * size. It is used with
    * {@link MediumRangeChunkAction#walkDividedRange(Class, IMediumReference, int, int, MediumRangeChunkAction)} and is
    * called for each chunk the original {@link MediumRegion} is split into. It returns the {@link MediumRegion}
    * corresponding to the current chunk.
    * 
    * @param regionToSplit
    *           The enclosing region to split
    * @param chunkStartReference
    *           The start {@link IMediumReference} of the current chunk
    * @param chunkSize
    *           The size of the current chunk
    * @return The split {@link MediumRegion} with given start {@link IMediumReference} and (chunk) size with the
    *         corresponding portion of the original region to split.
    */
   private MediumRegion splitRegionExceedingMaxCacheRegionSize(MediumRegion regionToSplit,
      IMediumReference chunkStartReference, int chunkSize) {

      IMediumReference chunkEndReference = chunkStartReference.advance(chunkSize);

      MediumRegion splitRegion = regionToSplit;

      if (regionToSplit.getStartReference().before(chunkStartReference)) {
         splitRegion = regionToSplit.split(chunkStartReference)[1];
      }

      if (chunkEndReference.before(regionToSplit.calculateEndReference())) {
         splitRegion = splitRegion.split(chunkEndReference)[0];
      }

      return splitRegion;
   }

   /**
    * Clips an existing region against a new region to add. Clipping means that the existing region is trimmed by the
    * portions overlapped by the new region to add.
    * 
    * @param leftExistingRegion
    *           The existing {@link MediumRegion} to be clipped
    * @param rightRegionToAdd
    *           The new {@link MediumRegion} to add
    */
   private void clipExistingRegionAgainstRegionToAdd(MediumRegion leftExistingRegion, MediumRegion rightRegionToAdd) {

      MediumRegionOverlapType overlapType = MediumRegion.determineOverlapWithOtherRegion(leftExistingRegion,
         rightRegionToAdd);

      if (overlapType == MediumRegionOverlapType.LEFT_FULLY_INSIDE_RIGHT
         || overlapType == MediumRegionOverlapType.SAME_RANGE) {
         removeRegionFromCache(leftExistingRegion);
      } else if (overlapType == MediumRegionOverlapType.RIGHT_FULLY_INSIDE_LEFT) {
         removeRegionFromCache(leftExistingRegion);
         MediumRegion survivingPartOne = leftExistingRegion.split(rightRegionToAdd.getStartReference())[0];
         addRegionToCache(survivingPartOne);
         MediumRegion survivingPartTwo = leftExistingRegion.split(rightRegionToAdd.calculateEndReference())[1];
         addRegionToCache(survivingPartTwo);
      } else if (overlapType == MediumRegionOverlapType.LEFT_OVERLAPS_RIGHT_AT_FRONT) {
         removeRegionFromCache(leftExistingRegion);
         MediumRegion survivingPart = leftExistingRegion.split(rightRegionToAdd.getStartReference())[0];
         addRegionToCache(survivingPart);
      } else if (overlapType == MediumRegionOverlapType.LEFT_OVERLAPS_RIGHT_AT_BACK) {
         removeRegionFromCache(leftExistingRegion);
         MediumRegion survivingPart = leftExistingRegion.split(rightRegionToAdd.calculateEndReference())[1];
         addRegionToCache(survivingPart);
      }
   }

   /**
    * Adds the given region to the internal data structures representing the cache.
    * 
    * @param region
    *           The {@link MediumRegion} to add.
    */
   private void addRegionToCache(MediumRegion region) {
      cachedRegionsInInsertOrder.add(region);
      cachedRegionsInOffsetOrder.put(region.getStartReference(), region);
   }

   /**
    * Removes the given region from internal data structures representing the cache. It assumes that the region is
    * really contained in the cache, so calling methods have to ensure they only pass such regions to this method.
    * 
    * @param region
    *           The {@link MediumRegion} to remove.
    */
   private void removeRegionFromCache(MediumRegion region) {
      cachedRegionsInInsertOrder.remove(region);
      cachedRegionsInOffsetOrder.remove(region.getStartReference());
   }
}
