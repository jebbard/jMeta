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
import com.github.jmeta.library.media.api.exceptions.InvalidMediumOffsetException;
import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.exceptions.ReadOnlyMediumException;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.api.types.MediumRegion.MediumRegionClipResult;
import com.github.jmeta.library.media.impl.cache.MediumCache;
import com.github.jmeta.library.media.impl.cache.MediumRangeChunkAction;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManager;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.offset.MediumOffsetFactory;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.errors.api.services.JMetaIllegalStateException;

/**
 * {@link StandardMediumStore} is the default implementation of the {@link MediumStore} interface.
 */
public class StandardMediumStore<T extends Medium<?>> implements MediumStore {

   private final MediumAccessor<T> mediumAccessor;

   private final MediumCache cache;

   private final MediumOffsetFactory offsetFactory;

   private boolean isOpened;

   private final MediumChangeManager changeManager;

   /**
    * Creates a new {@link StandardMediumStore}.
    * 
    * @param mediumAccessor
    *           The {@link MediumAccessor} instance to use, also contains the {@link Medium} this {@link MediumStore}
    *           works on
    * @param cache
    *           The {@link MediumCache} instance to use
    * @param offsetFactory
    *           The {@link MediumOffsetFactory} instance to use
    * @param changeManager
    *           The {@link MediumChangeManager} to use
    */
   public StandardMediumStore(MediumAccessor<T> mediumAccessor, MediumCache cache, MediumOffsetFactory offsetFactory,
      MediumChangeManager changeManager) {
      Reject.ifNull(mediumAccessor, "mediumAccessor");
      Reject.ifNull(offsetFactory, "offsetFactory");
      Reject.ifNull(changeManager, "changeManager");
      Reject.ifNull(cache, "cache");

      this.mediumAccessor = mediumAccessor;
      this.cache = cache;
      this.offsetFactory = offsetFactory;
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
      offsetFactory.clear();

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
    * @see com.github.jmeta.library.media.api.services.MediumStore#isAtEndOfMedium(com.github.jmeta.library.media.api.types.MediumOffset)
    */
   @Override
   public boolean isAtEndOfMedium(MediumOffset offset) {
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
    * @see com.github.jmeta.library.media.api.services.MediumStore#createMediumOffset(long)
    */
   @Override
   public MediumOffset createMediumOffset(long offset) {
      ensureOpened();

      return offsetFactory.createMediumOffset(offset);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#cache(com.github.jmeta.library.media.api.types.MediumOffset,
    *      int)
    */
   @Override
   public void cache(MediumOffset offset, int numberOfBytes) throws EndOfMediumException {
      Reject.ifNull(offset, "offset");
      ensureOpened();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");
      Reject.ifNegativeOrZero(numberOfBytes, "numberOfBytes");

      if (getMedium().getCurrentLength() != Medium.UNKNOWN_LENGTH) {
         if (getMedium().getCurrentLength() < offset.getAbsoluteMediumOffset()) {
            throw new EndOfMediumException(offset, numberOfBytes, 0, ByteBuffer.allocate(0));
         }
      }

      if (getMedium().isCachingEnabled()) {
         if (getMedium().isRandomAccess()) {
            long initialCacheSize = cache.calculateCurrentCacheSizeInBytes();

            List<MediumRegion> cacheRegionsInRange = cache.getRegionsInRange(offset, numberOfBytes);

            for (MediumRegion cacheRegion : cacheRegionsInRange) {
               cacheRegion = clipRegionAgainstRange(cacheRegion, offset, numberOfBytes);

               if (!cacheRegion.isCached()) {
                  MediumRegion regionWithBytes = readRegion(cacheRegion.getStartOffset(), cacheRegion.getSize());
                  cache.addRegion(regionWithBytes);
               } else if (isPreviouslyCachedRegionNowUncached(cacheRegion, initialCacheSize, numberOfBytes)) {
                  cache.addRegion(cacheRegion);
               }
            }
         } else {
            List<MediumRegion> regionsToAdd = readDataFromCurrentPositionUntilOffsetForNonRandomAccessMedia(offset);

            MediumOffset offsetToUse = mediumAccessor.getCurrentPosition();
            int numberOfBytesToUse = numberOfBytes - (int) offsetToUse.distanceTo(offset);

            if (numberOfBytesToUse > getCachedByteCountAt(offsetToUse)) {
               regionsToAdd.addAll(readRegionWise(offsetToUse, numberOfBytesToUse));
            }

            regionsToAdd.forEach(cache::addRegion);
         }
      }
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#getCachedByteCountAt(com.github.jmeta.library.media.api.types.MediumOffset)
    */
   @Override
   public long getCachedByteCountAt(MediumOffset offset) {
      Reject.ifNull(offset, "offset");
      ensureOpened();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");

      return cache.getCachedByteCountAt(offset);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#getData(com.github.jmeta.library.media.api.types.MediumOffset,
    *      int)
    */
   @Override
   public ByteBuffer getData(MediumOffset offset, int numberOfBytes) throws EndOfMediumException {
      Reject.ifNull(offset, "offset");
      ensureOpened();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");
      Reject.ifNegativeOrZero(numberOfBytes, "numberOfBytes");

      if (getMedium().getCurrentLength() != Medium.UNKNOWN_LENGTH) {
         if (getMedium().getCurrentLength() < offset.getAbsoluteMediumOffset()) {
            throw new EndOfMediumException(offset, numberOfBytes, 0, ByteBuffer.allocate(0));
         }
      }

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
               .position(firstRegionCachedBytes.position() + (int) offset.distanceTo(firstRegion.getStartOffset()));
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
                     cacheRegion.getStartOffset());

                  MediumRegion regionToAddWithBytes = readRegion(cacheRegion.getStartOffset(), cacheRegion.getSize());

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
    * @see com.github.jmeta.library.media.api.services.MediumStore#insertData(com.github.jmeta.library.media.api.types.MediumOffset,
    *      java.nio.ByteBuffer)
    */
   @Override
   public MediumAction insertData(MediumOffset offset, ByteBuffer dataToInsert) {
      Reject.ifNull(offset, "offset");
      Reject.ifNull(dataToInsert, "dataToInsert");
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");
      ensureOpened();
      ensureWritable();

      return changeManager.scheduleInsert(new MediumRegion(offset, dataToInsert.remaining()), dataToInsert);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#removeData(com.github.jmeta.library.media.api.types.MediumOffset,
    *      int)
    */
   @Override
   public MediumAction removeData(MediumOffset offset, int numberOfBytesToRemove) {
      Reject.ifNull(offset, "offset");
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");
      ensureOpened();
      ensureWritable();

      return changeManager.scheduleRemove(new MediumRegion(offset, numberOfBytesToRemove));
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#replaceData(com.github.jmeta.library.media.api.types.MediumOffset,
    *      int, java.nio.ByteBuffer)
    */
   @Override
   public MediumAction replaceData(MediumOffset offset, int numberOfBytesToReplace, ByteBuffer replacementData) {
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
      Reject.ifFalse(mediumAction.getRegion().getStartOffset().getMedium().equals(getMedium()),
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

      List<MediumAction> flushPlan = changeManager.createFlushPlan(getMedium().getMaxReadWriteBlockSizeInBytes(),
         getMedium().getCurrentLength());

      // Phase 1 - Medium access phase
      ByteBuffer lastReadBytes = null;

      MediumActionType previousActionType = null;

      List<MediumAction> scheduledActions = new ArrayList<>();

      for (MediumAction mediumAction : flushPlan) {
         switch (mediumAction.getActionType()) {
            case READ:
               try {
                  lastReadBytes = getData(mediumAction.getRegion().getStartOffset(),
                     mediumAction.getRegion().getSize());
               } catch (EndOfMediumException e) {
                  throw new JMetaIllegalStateException(
                     "Unexpected end of medium, maybe the external medium was changed by another process? Medium: "
                        + getMedium(),
                     e);
               }
               mediumAction.setDone();
            break;

            case WRITE:
               if (mediumAction.getActionBytes() != null) {
                  mediumAccessor.setCurrentPosition(mediumAction.getRegion().getStartOffset());
                  mediumAccessor.write(mediumAction.getActionBytes());
               } else {
                  if (previousActionType != MediumActionType.READ || lastReadBytes == null
                     || lastReadBytes.remaining() != mediumAction.getRegion().getSize()) {
                     throw new JMetaIllegalStateException(
                        "A WRITE action was given, but there was no READ action directly before reading the exact amount of bytes indicated by the current action: "
                           + mediumAction,
                        null);
                  }

                  mediumAccessor.setCurrentPosition(mediumAction.getRegion().getStartOffset());
                  mediumAccessor.write(lastReadBytes);
                  lastReadBytes = null;
               }
               mediumAction.setDone();
            break;

            case TRUNCATE:
               mediumAccessor.setCurrentPosition(mediumAction.getRegion().getStartOffset());
               mediumAccessor.truncate();
               mediumAction.setDone();
            break;

            default:
               scheduledActions.add(mediumAction);
         }

         previousActionType = mediumAction.getActionType();
      }

      // Phase 2 - Cache update phase
      for (MediumAction scheduledAction : scheduledActions) {
         ByteBuffer actionBytes = scheduledAction.getActionBytes();

         switch (scheduledAction.getActionType()) {
            case INSERT:
               changeManager.undo(scheduledAction);

               // If there is an existing cached region containing the insert offset, we must split it there,
               // to ensure the part of the region behind the insert offset is shifted correspondingly to leave room
               // for the inserts
               if (getMedium().isCachingEnabled()) {
                  MediumRegion existingRegionContainingInsertOffset = cache
                     .getRegionsInRange(scheduledAction.getRegion().getStartOffset(), 1).get(0);

                  if (existingRegionContainingInsertOffset.isCached() && existingRegionContainingInsertOffset
                     .getStartOffset().before(scheduledAction.getRegion().getStartOffset())) {
                     MediumRegion existingRegionSplitAtInsertOffset = existingRegionContainingInsertOffset
                        .split(scheduledAction.getRegion().getStartOffset())[0];

                     cache.addRegion(existingRegionSplitAtInsertOffset);
                  }
               }

               offsetFactory.updateOffsets(scheduledAction);

               if (getMedium().isCachingEnabled()) {
                  // Please note the comment in ShiftedMediumBlock.initStartReference()
                  cache.addRegion(new MediumRegion(scheduledAction.getRegion().getStartOffset(), actionBytes));
               }
            break;

            case REMOVE:
               if (getMedium().isCachingEnabled()) {
                  cache.removeRegionsInRange(scheduledAction.getRegion().getStartOffset(),
                     scheduledAction.getRegion().getSize());
               }
               changeManager.undo(scheduledAction);
               offsetFactory.updateOffsets(scheduledAction);
            break;

            case REPLACE:
               changeManager.undo(scheduledAction);
               if (getMedium().isCachingEnabled()) {
                  cache.removeRegionsInRange(scheduledAction.getRegion().getStartOffset(),
                     scheduledAction.getRegion().getSize());
               }
               offsetFactory.updateOffsets(scheduledAction);
               if (getMedium().isCachingEnabled()) {
                  cache.addRegion(new MediumRegion(scheduledAction.getRegion().getStartOffset(), actionBytes));
               }
            break;

            default:
               throw new JMetaIllegalStateException("Unexpected medium action type for action: " + scheduledAction,
                  null);
         }
      }

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
   private MediumRegion clipRegionAgainstRange(MediumRegion region, MediumOffset rangeOffset, int rangeSize) {
      MediumRegionClipResult clipResult = MediumRegion.clipOverlappingRegions(region,
         new MediumRegion(rangeOffset, rangeSize));

      return clipResult.getOverlappingPartOfLeftRegion();
   }

   /**
    * Determines if a given cached {@link MediumRegion}, that was still cached at point in time
    * {@link MediumCache#getRegionsInRange(MediumOffset, int)} was meanwhile has become uncached as new regions have
    * been added after this initial call which forced the cache to remove previously cached {@link MediumRegion}s to
    * keep its size below the maximum allowed cache size.
    * 
    * If the region is now uncached, it can nevertheless be used for caching again or retrieving data. This way we also
    * avoid unneeded medium accesses even in this corner case.
    * 
    * @param cachedRegion
    *           The cached {@link MediumRegion} to check
    * @param initialCacheSizeInBytes
    *           The cache size at point in time when {@link MediumCache#getRegionsInRange(MediumOffset, int)} was called
    * @param maxNumberOfBytesToAdd
    *           The maximum number of bytes that might get added to the cache in total
    * 
    * @return true if the given {@link MediumRegion} has now become uncached, false otherwise
    */
   private boolean isPreviouslyCachedRegionNowUncached(MediumRegion cachedRegion, long initialCacheSizeInBytes,
      int maxNumberOfBytesToAdd) {
      return initialCacheSizeInBytes + maxNumberOfBytesToAdd > cache.getMaximumCacheSizeInBytes()
         && cache.getCachedByteCountAt(cachedRegion.getStartOffset()) < cachedRegion.getSize();
   }

   /**
    * Only for non-random access media: If the medium's current position is smaller than the given offset, it reads all
    * bytes between these offsets chunk-wise and returns the corresponding {@link MediumRegion}s. If the medium's
    * current position is bigger than the given offset, it throws an {@link InvalidMediumOffsetException}. If the
    * offsets are equal, it returns an empty list. For random-access media it also returns an empty list.
    * 
    * @param offset
    *           The target offset
    * @return The {@link MediumRegion}s of maximum size {@link Medium#getMaxReadWriteBlockSizeInBytes()} between the
    *         current position and the given offset, if any
    * @throws EndOfMediumException
    *            if the end of the medium is reached during reading
    */
   private List<MediumRegion> readDataFromCurrentPositionUntilOffsetForNonRandomAccessMedia(MediumOffset offset)
      throws EndOfMediumException {
      List<MediumRegion> regionsRead = new ArrayList<>();

      if (!getMedium().isRandomAccess()) {
         MediumOffset currentPosition = mediumAccessor.getCurrentPosition();

         if (offset.before(currentPosition)) {
            long cachedByteCountAtOffset = cache.getCachedByteCountAt(offset);

            if (cachedByteCountAtOffset < currentPosition.distanceTo(offset)) {
               throw new InvalidMediumOffsetException(offset,
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
   private List<MediumRegion> readRegionWise(MediumOffset rangeOffset, long rangeSize) throws EndOfMediumException {
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
   private MediumRegion readRegion(MediumOffset regionOffset, int regionSize) throws EndOfMediumException {
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
