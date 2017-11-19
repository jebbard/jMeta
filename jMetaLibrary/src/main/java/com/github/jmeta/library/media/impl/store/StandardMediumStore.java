/**
 *
 * {@link StandardMediumStore}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.impl.store;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.InvalidMediumReferenceException;
import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.exceptions.ReadOnlyMediumException;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.cache.MediumCache;
import com.github.jmeta.library.media.impl.cache.MediumRangeChunkAction;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManager;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.reference.MediumReferenceFactory;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardMediumStore} is the default implementation of the {@link MediumStore} interface.
 */
public class StandardMediumStore<T extends Medium<?>> implements MediumStore {

   private final MediumAccessor<T> mediumAccessor;

   private final MediumCache cache;

   private final MediumReferenceFactory referenceFactory;

   private boolean isOpened;

   private final MediumChangeManager changeManager;

   public StandardMediumStore(MediumAccessor<T> mediumAccessor, MediumCache cache,
      MediumReferenceFactory referenceFactory, MediumChangeManager changeManager) {
      Reject.ifNull(mediumAccessor, "mediumAccessor");
      Reject.ifNull(referenceFactory, "referenceFactory");
      Reject.ifNull(cache, "cache");

      this.mediumAccessor = mediumAccessor;
      this.cache = cache;
      this.referenceFactory = referenceFactory;
      this.changeManager = changeManager;

      isOpened = false;
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#isOpened()
    */
   @Override
   public boolean isOpened() {
      return isOpened;
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#close()
    */
   @Override
   public void close() {
      ensureOpened();

      cache.clear();
      mediumAccessor.close();
      referenceFactory.clear();

      isOpened = false;
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#getMedium()
    */
   @Override
   public Medium<?> getMedium() {
      return mediumAccessor.getMedium();
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#isAtEndOfMedium(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   public boolean isAtEndOfMedium(MediumReference offset) {
      Reject.ifNull(offset, "offset");
      ensureOpened();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");

      mediumAccessor.setCurrentPosition(offset);

      return mediumAccessor.isAtEndOfMedium();
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#open()
    */
   @Override
   public void open() {
      mediumAccessor.open();

      isOpened = true;
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#createMediumReference(long)
    */
   @Override
   public MediumReference createMediumReference(long offset) {
      ensureOpened();

      return referenceFactory.createMediumReference(offset);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#cache(com.github.jmeta.library.media.api.types.MediumReference,
    *      int)
    */
   @Override
   public void cache(MediumReference offset, int numberOfBytes) throws EndOfMediumException {
      Reject.ifNull(offset, "offset");
      ensureOpened();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");

      if (getMedium().isCachingEnabled()) {
         if (getMedium().isRandomAccess()) {
            long initialCacheSize = cache.calculateCurrentCacheSizeInBytes();

            List<MediumRegion> cacheRegionsInRange = cache.getRegionsInRange(offset, numberOfBytes);

            for (MediumRegion cacheRegion : cacheRegionsInRange) {
               cacheRegion = clipRegionAgainstRange(cacheRegion, offset, numberOfBytes);

               if (!cacheRegion.isCached()) {
                  MediumRegion regionWithBytes = readRegion(cacheRegion.getStartReference(), cacheRegion.getSize());
                  cache.addRegion(regionWithBytes);
               } else if (isPreviouslyCachedRegionNowUncached(cacheRegion, initialCacheSize, numberOfBytes)) {
                  cache.addRegion(cacheRegion);
               }
            }
         } else {
            List<MediumRegion> regionsToAdd = readDataFromCurrentPositionUntilOffsetForNonRandomAccessMedia(offset);

            MediumReference offsetToUse = mediumAccessor.getCurrentPosition();
            int numberOfBytesToUse = numberOfBytes - (int) offsetToUse.distanceTo(offset);

            if (numberOfBytesToUse > getCachedByteCountAt(offsetToUse)) {
               regionsToAdd.addAll(readRegionWise(offsetToUse, numberOfBytesToUse));
            }

            regionsToAdd.forEach(cache::addRegion);
         }
      }
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#getCachedByteCountAt(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   public long getCachedByteCountAt(MediumReference offset) {
      Reject.ifNull(offset, "offset");
      ensureOpened();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");

      return cache.getCachedByteCountAt(offset);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#getData(com.github.jmeta.library.media.api.types.MediumReference,
    *      int)
    */
   @Override
   public ByteBuffer getData(MediumReference offset, int numberOfBytes) throws EndOfMediumException {
      Reject.ifNull(offset, "offset");
      ensureOpened();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");

      if (getMedium().isCachingEnabled()) {
         long initialCacheSize = cache.calculateCurrentCacheSizeInBytes();

         List<MediumRegion> cacheRegionsInRange = cache.getRegionsInRange(offset, numberOfBytes);

         MediumRegion firstRegion = cacheRegionsInRange.get(0);

         // This "if" is just an optimization for the 80% case in which we just take the original ByteBuffer as view,
         // tailored to the requested range. This safes us new memory allocation and copying of bytes.
         if (cacheRegionsInRange.size() == 1 && firstRegion.isCached()
            && firstRegion.getOverlappingByteCount(new MediumRegion(offset, numberOfBytes)) == numberOfBytes) {

            ByteBuffer firstRegionCachedBytes = firstRegion.getBytes();
            firstRegionCachedBytes
               .position(firstRegionCachedBytes.position() + (int) offset.distanceTo(firstRegion.getStartReference()));
            firstRegionCachedBytes.limit(firstRegionCachedBytes.position() + numberOfBytes);

            return firstRegionCachedBytes;
         } else {
            ByteBuffer cachedBytes = ByteBuffer.allocate(numberOfBytes);

            for (MediumRegion cacheRegion : cacheRegionsInRange) {
               cacheRegion = clipRegionAgainstRange(cacheRegion, offset, numberOfBytes);

               if (cacheRegion.isCached()
                  && !isPreviouslyCachedRegionNowUncached(cacheRegion, initialCacheSize, numberOfBytes)) {
                  cachedBytes.put(cacheRegion.getBytes());
               } else {
                  List<MediumRegion> regionsRead = readDataFromCurrentPositionUntilOffsetForNonRandomAccessMedia(
                     cacheRegion.getStartReference());

                  MediumRegion regionToAddWithBytes = readRegion(cacheRegion.getStartReference(),
                     cacheRegion.getSize());

                  cachedBytes.put(regionToAddWithBytes.getBytes());

                  regionsRead.add(regionToAddWithBytes);

                  regionsRead.forEach((region) -> cache.addRegion(region));
               }
            }

            cachedBytes.rewind();

            return cachedBytes;
         }
      } else {
         readDataFromCurrentPositionUntilOffsetForNonRandomAccessMedia(offset);

         ByteBuffer readBytes = ByteBuffer.allocate(numberOfBytes);

         List<MediumRegion> regionsRead = readRegionWise(offset, numberOfBytes);

         regionsRead.forEach((region) -> readBytes.put(region.getBytes()));

         readBytes.rewind();

         return readBytes;
      }
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#insertData(com.github.jmeta.library.media.api.types.MediumReference,
    *      java.nio.ByteBuffer)
    */
   @Override
   public MediumAction insertData(MediumReference offset, ByteBuffer dataToInsert) {
      Reject.ifNull(offset, "offset");
      Reject.ifNull(dataToInsert, "dataToInsert");
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");
      ensureOpened();
      ensureWritable();

      return changeManager.scheduleInsert(new MediumRegion(offset, dataToInsert.capacity()), dataToInsert);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#removeData(com.github.jmeta.library.media.api.types.MediumReference,
    *      int)
    */
   @Override
   public MediumAction removeData(MediumReference offset, int numberOfBytesToRemove) {
      Reject.ifNull(offset, "offset");
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");
      ensureOpened();
      ensureWritable();

      return changeManager.scheduleRemove(new MediumRegion(offset, numberOfBytesToRemove));
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#replaceData(com.github.jmeta.library.media.api.types.MediumReference,
    *      int, java.nio.ByteBuffer)
    */
   @Override
   public MediumAction replaceData(MediumReference offset, int numberOfBytesToReplace, ByteBuffer replacementData) {
      Reject.ifNull(offset, "offset");
      Reject.ifNull(replacementData, "replacementData");
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");
      ensureOpened();
      ensureWritable();

      return changeManager.scheduleReplace(new MediumRegion(offset, numberOfBytesToReplace), replacementData);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)
    */
   @Override
   public void undo(MediumAction mediumAction) {
      Reject.ifNull(mediumAction, "mediumAction");
      Reject.ifFalse(mediumAction.getRegion().getStartReference().getMedium().equals(getMedium()),
         "mediumAction.getRegion().getStartReference().getMedium().equals(getMedium())");
      Reject.ifFalse(mediumAction.isPending(), "mediumAction.isPending()");
      ensureOpened();
      ensureWritable();

      changeManager.undo(mediumAction);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#flush()
    */
   @Override
   public void flush() {
      ensureOpened();
      ensureWritable();

   }

   /**
    * In case of {@link MediumRegion}s overlapping just front or back of a given range, they need to be clipped, which
    * is done by this method. It returns a clipped region that is guaranteed to start at or behind the range start
    * offset and end at or before the range end offset.
    * 
    * @param region
    *           The {@link MediumRegion} to clip
    * @param rangeOffset
    *           The start offset of the range
    * @param rangeSize
    *           The size of the range in bytes
    * @return a {@link MediumRegion} clipped against the given range
    */
   private MediumRegion clipRegionAgainstRange(MediumRegion region, MediumReference rangeOffset, int rangeSize) {
      MediumReference regionStartReference = region.getStartReference();
      MediumReference regionEndReference = region.calculateEndReference();
      MediumReference rangeEndReference = rangeOffset.advance(rangeSize);

      MediumRegion regionToUse = region;

      if (regionStartReference.before(rangeOffset)) {
         regionToUse = regionToUse.split(rangeOffset)[1];
      }

      if (rangeEndReference.before(regionEndReference)) {
         regionToUse = regionToUse.split(rangeEndReference)[0];
      }
      return regionToUse;
   }

   /**
    * Determines if a given cached {@link MediumRegion}, that was still cached at point in time
    * {@link MediumCache#getRegionsInRange(MediumReference, int)} was meanwhile has become uncached as new regions have
    * been added after this initial call which forced the cache to remove previously cached {@link MediumRegion}s to
    * keep its size below the maximum allowed cache size.
    * 
    * If the region is now uncached, it can nevertheless be used for caching again or retrieving data. This way we also
    * avoid unneeded medium accesses even in this corner case.
    * 
    * @param cachedRegion
    *           The cached {@link MediumRegion} to check
    * @param initialCacheSizeInBytes
    *           The cache size at point in time when {@link MediumCache#getRegionsInRange(MediumReference, int)} was
    *           called
    * @param maxNumberOfBytesToAdd
    *           The maximum number of bytes that might get added to the cache in total
    * 
    * @return true if the given {@link MediumRegion} has now become uncached, false otherwise
    */
   private boolean isPreviouslyCachedRegionNowUncached(MediumRegion cachedRegion, long initialCacheSizeInBytes,
      int maxNumberOfBytesToAdd) {
      return initialCacheSizeInBytes + maxNumberOfBytesToAdd > cache.getMaximumCacheSizeInBytes()
         && cache.getCachedByteCountAt(cachedRegion.getStartReference()) < cachedRegion.getSize();
   }

   /**
    * Only for non-random access media: If the medium's current position is smaller than the given offset, it reads all
    * bytes between these offsets chunk-wise and returns the corresponding {@link MediumRegion}s. If the medium's
    * current position is bigger than the given offset, it throws an {@link InvalidMediumReferenceException}. If the
    * offsets are equal, it returns an empty list. For random-access media it also returns an empty list.
    * 
    * @param offset
    *           The target offset
    * @return The {@link MediumRegion}s of maximum size {@link Medium#getMaxReadWriteBlockSizeInBytes()} between the
    *         current position and the given offset, if any
    * @throws EndOfMediumException
    *            if the end of the medium is reached during reading
    */
   private List<MediumRegion> readDataFromCurrentPositionUntilOffsetForNonRandomAccessMedia(MediumReference offset)
      throws EndOfMediumException {
      List<MediumRegion> regionsRead = new ArrayList<>();

      if (!getMedium().isRandomAccess()) {
         MediumReference currentPosition = mediumAccessor.getCurrentPosition();

         if (offset.before(currentPosition)) {
            long cachedByteCountAtOffset = cache.getCachedByteCountAt(offset);

            if (cachedByteCountAtOffset < currentPosition.distanceTo(offset)) {
               throw new InvalidMediumReferenceException(offset,
                  "Cannot re-read data of  non-random-access media for already passed ranges that are not fully cached");
            }
         }

         if (currentPosition.before(offset)) {
            regionsRead = readRegionWise(currentPosition, offset.distanceTo(currentPosition));
         }
      }
      return regionsRead;
   }

   /**
    * Determines chunked {@link MediumRegion}s filled with data read from the medium in the given range based on the
    * maximum read-write block size configured for the medium.
    * 
    * @param rangeOffset
    *           The range start offset
    * @param rangeSize
    *           The range's size in bytes
    * @return The list of all {@link MediumRegion}s of maximum size {@link Medium#getMaxReadWriteBlockSizeInBytes()}
    *         covering the whole range.
    * @throws EndOfMediumException
    *            if the end of the medium is reached during reading
    */
   private List<MediumRegion> readRegionWise(MediumReference rangeOffset, long rangeSize) throws EndOfMediumException {
      List<MediumRegion> regionsRead;
      regionsRead = MediumRangeChunkAction.performActionOnChunksInRange(MediumRegion.class, EndOfMediumException.class,
         rangeOffset, rangeSize, getMedium().getMaxReadWriteBlockSizeInBytes(), this::readRegion);
      return regionsRead;
   }

   /**
    * Reads a region of bytes from the external medium.
    * 
    * @param regionOffset
    *           The offset of the region
    * @param regionSize
    *           The size of the region
    * @throws EndOfMediumException
    *            in case of EOM was reached during reading
    */
   private MediumRegion readRegion(MediumReference regionOffset, int regionSize) throws EndOfMediumException {
      ByteBuffer dataRead = ByteBuffer.allocate(regionSize);

      mediumAccessor.setCurrentPosition(regionOffset);

      try {
         mediumAccessor.read(dataRead);
      } catch (EndOfMediumException e) {
         if (getMedium().isCachingEnabled()) {
            if (e.getByteCountActuallyRead() > 0) {
               cache.addRegion(new MediumRegion(e.getReadStartReference(), e.getBytesReadSoFar()));
            }
         }

         throw e;
      }

      return new MediumRegion(regionOffset, dataRead);
   }

   /**
    * Ensures that this {@link MediumStore} is opened for most of the operations.
    */
   private void ensureOpened() {
      if (!isOpened()) {
         throw new MediumStoreClosedException();
      }
   }

   /**
    * Ensures that the underlying {@link Medium} is opened for writing and writable.
    */
   private void ensureWritable() {
      if (getMedium().isReadOnly()) {
         throw new ReadOnlyMediumException(getMedium(), null);
      }
   }

}
