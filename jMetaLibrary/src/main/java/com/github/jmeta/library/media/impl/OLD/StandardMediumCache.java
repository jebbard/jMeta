/**
 *
 * {@link MediumAccessReference}.java
 *
 * @author Jens Ebert
 *
 * @date 17.01.2011
 */

package com.github.jmeta.library.media.impl.OLD;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumAccessException;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardMediumCache} provides a standard implementation of the {@link MediumCache} interface. To fulfill the
 * contract of the {@link MediumCache} interface, it uses a {@link HashMap} internally to implement an efficient
 * caching. It works with an {@link MediumAccessor} to read from an {@link AbstractMedium}.
 * 
 * @invariant A single cache region returned by {@link #getBufferedRegions()} must not be bigger than the maximum cache
 *            region size specified to the constructor.
 */
public class StandardMediumCache implements MediumCache {

   private final MediumAccessor<?> m_accessor;

   private Map<MediumOffset, MediumRegion> m_cache = new HashMap<>();

   private final int m_maxCacheRegionSize;

   /**
    * Creates a new {@link StandardMediumCache} working on the given {@link MediumAccessor}.
    * 
    * @param accessor
    *           The {@link MediumAccessor} used for accessing the {@link AbstractMedium} bytes. Must be opened.
    * @param maxCacheRegionSize
    *           The maximum allowed size for a cache region. Must be bigger than 0.
    * @pre maxCacheRegionSize > 0
    * @pre accessor.isOpened()
    */
   public StandardMediumCache(MediumAccessor<?> accessor, int maxCacheRegionSize) {

      Reject.ifNull(accessor, "accessor");
      Reject.ifNegativeOrZero(maxCacheRegionSize, "maxCacheRegionSize");
      Reject.ifTrue(accessor.isOpened(), "accessor.isOpened()");

      m_accessor = accessor;
      accessor.open();
      m_maxCacheRegionSize = maxCacheRegionSize;
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#close()
    */
   @Override
   public void close() {

      Reject.ifTrue(!isOpened(), "!isOpened()");

      m_accessor.close();
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#createMediumOffset(long)
    */
   @Override
   public MediumOffset createMediumReference(long absoluteMediumOffset) {

      // TODO implement for writing
      return new StandardMediumOffset(getMedium(), absoluteMediumOffset);
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#discard(MediumOffset, long)
    */
   @Override
   public void discard(MediumOffset startReference, long size) {

      Reject.ifNull(startReference, "startReference");
      Reject.ifNegative(size, "size");

      MediumRegion firstRegion = findContainingRegion(startReference);

      if (firstRegion == null || size == 0)
         return;

      long freedSize = 0;

      MediumOffset currentReference = firstRegion.getStartOffset();

      // The portion to free overlaps the end of an existing region
      if (firstRegion.getStartOffset().before(startReference)) {
         long originalSize = firstRegion.getSize();
         long distance = startReference.distanceTo(firstRegion.getStartOffset());

         // Set to end reference berfore trimming
         currentReference = firstRegion.calculateEndOffset();

         firstRegion.discardBytesAtEnd(startReference);

         freedSize += originalSize - distance;
      }

      // Free consecutive further regions up to the requested size
      while (freedSize < size && m_cache.containsKey(currentReference)) {
         final MediumRegion currentRegion = m_cache.get(currentReference);

         final int originalSize = currentRegion.getSize();

         final long remainingSize = size - freedSize;

         m_cache.remove(currentReference);

         if (originalSize > remainingSize) {
            currentRegion.discardBytesAtFront(currentReference.advance(remainingSize));

            m_cache.put(currentRegion.getStartOffset(), currentRegion);

            freedSize += remainingSize;
         }

         else {
            freedSize += originalSize;
         }

         currentReference = currentRegion.calculateEndOffset();
      }
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#flush()
    */
   @Override
   public void flush() {

      // TODO implement for writing
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#getData(com.github.jmeta.library.media.api.types.MediumOffset,
    *      int)
    */
   @Override
   public synchronized ByteBuffer getData(MediumOffset reference, int byteCount) {

      Reject.ifNull(reference, "reference");
      Reject.ifNegative(byteCount, "size");

      ByteBuffer returnedBuffer = ByteBuffer.allocate(byteCount);

      if (byteCount == 0)
         return returnedBuffer;

      // Not cached: Read directly from medium
      if (getBufferedByteCountAt(reference) < byteCount)
         uncheckedReadFromMedium(reference, returnedBuffer);

      // Otherwise take the data from the cache
      else {
         MediumRegion nextRegion = findContainingRegion(reference);

         int startPosition = (int) (reference.distanceTo(nextRegion.getStartOffset()));

         ByteBuffer bytes = nextRegion.getBytes();

         bytes.position(bytes.position() + startPosition);

         if (bytes.remaining() > byteCount)
            bytes.limit(bytes.position() + byteCount);

         int summedUpByteCount = bytes.remaining();

         returnedBuffer.put(bytes);

         MediumOffset nextReference = nextRegion.calculateEndOffset();

         while (summedUpByteCount < byteCount) {
            nextRegion = m_cache.get(nextReference);

            bytes = nextRegion.getBytes();

            if (nextRegion.getSize() > byteCount - summedUpByteCount)
               bytes.limit(bytes.position() + (byteCount - summedUpByteCount));

            returnedBuffer.put(bytes);

            summedUpByteCount += nextRegion.getSize();

            nextReference = nextRegion.calculateEndOffset();
         }

         returnedBuffer.rewind();
      }

      return returnedBuffer;
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#getMedium()
    */
   @Override
   public Medium<?> getMedium() {

      return m_accessor.getMedium();
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#getBufferedByteCountAt(MediumOffset)
    */
   @Override
   public synchronized long getBufferedByteCountAt(MediumOffset reference) {

      Reject.ifNull(reference, "reference");
      Reject.ifFalse(reference.getMedium().equals(getMedium()), "reference.getMedium().equals(getMedium())");

      MediumRegion region = findContainingRegion(reference);

      if (region == null)
         return 0;

      long summedUpSize = region.calculateEndOffset().distanceTo(reference);

      MediumOffset nextReference = region.calculateEndOffset();

      // Read all continuous regions and sum up their size
      while (m_cache.containsKey(nextReference)) {
         MediumRegion nextRegion = m_cache.get(nextReference);

         summedUpSize += nextRegion.getSize();

         nextReference = nextRegion.calculateEndOffset();
      }

      return summedUpSize;
   }

   /**
    * @see com.github.jmeta.library.media.impl.OLD.MediumCache#getBufferedRegions()
    */
   @Override
   public Map<MediumOffset, Integer> getBufferedRegions() {

      Map<MediumOffset, Integer> returnedMap = new HashMap<>();

      for (Iterator<MediumOffset> iterator = m_cache.keySet().iterator(); iterator.hasNext();) {
         MediumOffset nextReference = iterator.next();
         MediumRegion nextRegion = m_cache.get(nextReference);

         returnedMap.put(nextReference, nextRegion.getSize());
      }

      return returnedMap;
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#insertData(com.github.jmeta.library.media.api.types.MediumOffset,
    *      java.nio.ByteBuffer)
    */
   @Override
   public MediumAction insertData(MediumOffset reference, ByteBuffer bytes) {

      // TODO implement for writing

      return null;
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#isAtEndOfMedium(com.github.jmeta.library.media.api.types.MediumOffset)
    */
   @Override
   public boolean isAtEndOfMedium(MediumOffset reference) {

      m_accessor.setCurrentPosition(reference);

      return m_accessor.isAtEndOfMedium();
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#isOpened()
    */
   @Override
   public boolean isOpened() {

      return m_accessor.isOpened();
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#buffer(MediumOffset, long)
    */
   @Override
   public synchronized void buffer(MediumOffset cacheReference, int size) throws EndOfMediumException {

      Reject.ifNull(cacheReference, "reference");
      Reject.ifNegativeOrZero(size, "size");

      // Return without action if the region is already cached as such
      if (getBufferedByteCountAt(cacheReference) >= size)
         return;

      MediumOffset newStartReference = cacheReference;
      // Reference to the last byte before end of region
      MediumOffset newEndReference = cacheReference.advance(size);

      for (Iterator<MediumOffset> iterator = m_cache.keySet().iterator(); iterator.hasNext();) {
         final MediumOffset existingStartReference = iterator.next();
         final MediumRegion existingCacheRegion = m_cache.get(existingStartReference);

         final MediumOffset existingEndReference = existingCacheRegion.calculateEndOffset();

         /*
          * If the new region incorporates one or several complete existing regions, the existing regions are dropped
          * and the new region is then cached as such.
          */
         if ((newStartReference.before(existingStartReference) || newStartReference.equals(existingStartReference))
            && newEndReference.behindOrEqual(existingEndReference))
            iterator.remove();

         else {
            /*
             * If the new region overlaps the end of an existing region, the existing region remains unchanged, the new
             * region is trimmed to start at the first byte after the existing region and then cached as such.
             */
            if (existingCacheRegion.contains(newStartReference))
               newStartReference = existingEndReference;

            /*
             * If the new region overlaps the start of an existing region, the existing region remains unchanged, the
             * new region is trimmed to end at the last byte before the existing region and then cached as such.
             */
            if (existingStartReference.behindOrEqual(newStartReference)
               && existingStartReference.before(newEndReference))
               newEndReference = existingStartReference;
         }
      }

      long actualSize = newEndReference.distanceTo(newStartReference);

      // In case that the whole medium is already cached, the new cache request
      // is changed
      // to start one byte earlier to cause an EndOfMediumException properly
      // when reading
      // later
      if (getMedium().isRandomAccess())
         if (newStartReference.equals(new StandardMediumOffset(getMedium(), getMedium().getCurrentLength()))
            && newStartReference.getAbsoluteMediumOffset() != 0) {
            newStartReference = new StandardMediumOffset(getMedium(), newStartReference.getAbsoluteMediumOffset() - 1);
            actualSize++;
         }

      MediumOffset nextReference = newStartReference;

      try {
         for (int i = 0; i < actualSize / m_maxCacheRegionSize; i++) {
            readIntoCache(nextReference, m_maxCacheRegionSize);

            nextReference = nextReference.advance(m_maxCacheRegionSize);
         }

         if (actualSize % m_maxCacheRegionSize > 0)
            readIntoCache(nextReference, (int) actualSize % m_maxCacheRegionSize);
      } catch (EndOfMediumException e) {
         if (getMedium().isRandomAccess())
            if (!e.getReadStartReference().equals(cacheReference) || e.getByteCountTriedToRead() != size)
               throw new EndOfMediumException(cacheReference, size,
                  (int) (getMedium().getCurrentLength() - cacheReference.getAbsoluteMediumOffset()),
                  ByteBuffer.allocate(0));

         throw e;
      }
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#removeData(com.github.jmeta.library.media.api.types.MediumOffset,
    *      int)
    */
   @Override
   public MediumAction removeData(MediumOffset reference, int byteCountToRemove) {

      // TODO implement for writing
      return null;
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#replaceData(com.github.jmeta.library.media.api.types.MediumOffset,
    *      int, java.nio.ByteBuffer)
    */
   @Override
   public MediumAction replaceData(MediumOffset reference, int byteCountToReplace, ByteBuffer bytes) {

      // TODO implement for writing
      return null;
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#undo(com.github.jmeta.library.media.api.types.MediumAction)
    */
   @Override
   public void undo(MediumAction handle) {

      // TODO implement for writing
   }

   /**
    * Finds the {@link MediumRegion} that contains the given {@link MediumOffset}. If there is no such
    * {@link MediumRegion}, then null is returned.
    * 
    * @param reference
    *           The {@link MediumOffset}.
    * @return The {@link MediumRegion} containing the given {@link MediumOffset} or null if there is no such
    *         {@link MediumRegion}.
    */
   private MediumRegion findContainingRegion(MediumOffset reference) {

      if (m_cache.containsKey(reference))
         return m_cache.get(reference);

      Iterator<MediumOffset> iterator = m_cache.keySet().iterator();

      while (iterator.hasNext()) {
         MediumOffset nextReference = iterator.next();
         MediumRegion nextRegion = m_cache.get(nextReference);

         if (nextRegion.contains(reference))
            return nextRegion;
      }

      return null;
   }

   /**
    * Reads a new medium cache region into this {@link MediumCache}.
    * 
    * @param reference
    *           The {@link MediumOffset} pointing to the offset where the data is read from.
    * @param size
    *           The number of byte to read.
    * @throws EndOfMediumException
    *            If end of medium occurs during read.
    */
   private void readIntoCache(MediumOffset reference, int size) throws EndOfMediumException {

      ByteBuffer newBuffer = ByteBuffer.allocate(size);

      m_accessor.setCurrentPosition(reference);

      try {
         m_accessor.read(newBuffer);
      }
      // In every case put the read content - stored in the buffer by
      // IMediumAccessor.read() - to the cache, also in the case of end of
      // medium
      finally {
         m_cache.put(reference, new MediumRegion(reference, newBuffer));
      }
   }

   /**
    * Performs an unchecked read from the medium, i.e. any {@link EndOfMediumException} is converted to a runtime
    * {@link MediumAccessException}.
    * 
    * @param reference
    *           The {@link MediumOffset} at which to start reading.
    * @param returnedBuffer
    *           The {@link ByteBuffer} to read bytes into, its remaining size is attempted to be read.
    */
   private void uncheckedReadFromMedium(MediumOffset reference, ByteBuffer returnedBuffer) {

      m_accessor.setCurrentPosition(reference);

      try {
         m_accessor.read(returnedBuffer);
      } catch (EndOfMediumException e) {
         throw new MediumAccessException("Unexpected end of medium occurred during read", e);
      }
   }
}
