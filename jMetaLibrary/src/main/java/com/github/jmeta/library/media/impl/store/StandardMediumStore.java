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

   public StandardMediumStore(MediumAccessor<T> mediumAccessor, MediumCache cache,
      MediumReferenceFactory referenceFactory) {
      Reject.ifNull(mediumAccessor, "mediumAccessor");
      Reject.ifNull(referenceFactory, "referenceFactory");
      Reject.ifNull(cache, "cache");

      this.mediumAccessor = mediumAccessor;
      this.cache = cache;
      this.referenceFactory = referenceFactory;

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

         MediumReference currentMediumPosition = mediumAccessor.getCurrentPosition();

         // Ensure to read any bytes necessary to be read before the given offset for stream-based media
         if (!getMedium().isRandomAccess()) {
            if (currentMediumPosition.before(offset)) {
               cacheChunkWise(currentMediumPosition, (int) offset.distanceTo(currentMediumPosition));
            }
         }

         long cachedByteCountAt = cache.getCachedByteCountAt(offset);

         if (cachedByteCountAt < numberOfBytes) {
            // Ensure to read any bytes necessary to be read before the given offset for stream-based media
            if (!getMedium().isRandomAccess() && offset.before(currentMediumPosition)) {
               // cacheChunkWise(currentMediumPosition, numberOfBytes - (int) currentMediumPosition.distanceTo(offset));
               throw new InvalidMediumReferenceException(offset,
                  "Cannot cache data for non-random-access media in front of highest read offset");
            } else {
               cacheChunkWise(offset, numberOfBytes);
            }
         }
      }
   }

   private void cacheChunkWise(MediumReference offset, int size) throws EndOfMediumException {
      List<MediumRegion> regionsToAdd = MediumRangeChunkAction.walkDividedRangeWithException(MediumRegion.class,
         EndOfMediumException.class, offset, size, getMedium().getMaxReadWriteBlockSizeInBytes(), this::readChunk);

      regionsToAdd.forEach(cache::addRegion);
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

      ByteBuffer readBytes = ByteBuffer.allocate(numberOfBytes);

      if (getMedium().isCachingEnabled()) {

         // Ensure to read any bytes necessary to be read before the given offset for stream-based media
         if (!getMedium().isRandomAccess()) {
            MediumReference currentMediumPosition = mediumAccessor.getCurrentPosition();

            if (currentMediumPosition.before(offset)) {
               cache(currentMediumPosition, (int) offset.distanceTo(currentMediumPosition));
            }
         }

         // Ensure cache is updated
         cache(offset, numberOfBytes);

         List<MediumRegion> cacheRegionsInRange = cache.getRegionsInRange(offset, numberOfBytes);

         for (MediumRegion mediumRegion : cacheRegionsInRange) {
            MediumReference regionStartReference = mediumRegion.getStartReference();
            MediumReference regionEndReference = mediumRegion.calculateEndReference();
            MediumReference rangeEndReference = offset.advance(numberOfBytes);

            MediumRegion regionToUse = mediumRegion;

            if (regionStartReference.before(offset)) {
               regionToUse = regionToUse.split(offset)[1];
            }

            if (rangeEndReference.before(regionEndReference)) {
               regionToUse = regionToUse.split(rangeEndReference)[0];
            }

            readBytes.put(regionToUse.getBytes());
         }
      } else {
         List<MediumRegion> regionsToAdd = MediumRangeChunkAction.walkDividedRangeWithException(MediumRegion.class,
            EndOfMediumException.class, offset, numberOfBytes, getMedium().getMaxReadWriteBlockSizeInBytes(),
            this::readChunk);

         regionsToAdd.forEach((region) -> readBytes.put(region.getBytes()));
      }

      readBytes.rewind();

      return readBytes;
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#insertData(com.github.jmeta.library.media.api.types.MediumReference,
    *      java.nio.ByteBuffer)
    */
   @Override
   public MediumAction insertData(MediumReference offset, ByteBuffer dataToInsert) {
      Reject.ifNull(offset, "offset");
      Reject.ifNull(dataToInsert, "dataToInsert");
      ensureOpened();
      ensureWritable();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");

      return null;
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#removeData(com.github.jmeta.library.media.api.types.MediumReference,
    *      int)
    */
   @Override
   public MediumAction removeData(MediumReference offset, int numberOfBytesToRemove) {
      Reject.ifNull(offset, "offset");
      ensureOpened();
      ensureWritable();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");

      return null;
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#replaceData(com.github.jmeta.library.media.api.types.MediumReference,
    *      int, java.nio.ByteBuffer)
    */
   @Override
   public MediumAction replaceData(MediumReference offset, int numberOfBytesToReplace, ByteBuffer replacementData) {
      Reject.ifNull(offset, "offset");
      Reject.ifNull(replacementData, "replacementData");
      ensureOpened();
      ensureWritable();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");

      return null;
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)
    */
   @Override
   public void undo(MediumAction mediumAction) {
      Reject.ifNull(mediumAction, "mediumAction");
      ensureOpened();
      ensureWritable();
      Reject.ifFalse(mediumAction.getRegion().getStartReference().getMedium().equals(getMedium()),
         "mediumAction.getRegion().getStartReference().getMedium().equals(getMedium())");

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
    * Reads a chunk of bytes from the external medium.
    * 
    * @param chunkOffset
    *           The offset of the chunk
    * @param chunkSizeInBytes
    *           The size of the chunk
    * @throws EndOfMediumException
    */
   private MediumRegion readChunk(MediumReference chunkOffset, int chunkSizeInBytes) throws EndOfMediumException {
      ByteBuffer dataRead = ByteBuffer.allocate(chunkSizeInBytes);

      mediumAccessor.setCurrentPosition(chunkOffset);

      mediumAccessor.read(dataRead);

      return new MediumRegion(chunkOffset, dataRead);
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
