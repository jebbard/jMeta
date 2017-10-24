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
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.reference.StandardMediumReference;
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

   private Map<MediumReference, MediumRegion> m_cache = new HashMap<>();

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
      Reject.ifFalse(accessor.isOpened(), "accessor.isOpened()");

      m_accessor = accessor;
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
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#createMediumReference(long)
    */
   @Override
   public MediumReference createMediumReference(long absoluteMediumOffset) {

      // TODO implement for writing
      return new StandardMediumReference(getMedium(), absoluteMediumOffset);
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#discard(MediumReference, long)
    */
   @Override
   public void discard(MediumReference startReference, long size) {

      Reject.ifNull(startReference, "startReference");
      Reject.ifNegative(size, "size");

      MediumRegion firstRegion = findContainingRegion(startReference);

      if (firstRegion == null || size == 0)
         return;

      long freedSize = 0;

      MediumReference currentReference = firstRegion.getStartReference();

      // The portion to free overlaps the end of an existing region
      if (firstRegion.getStartReference().before(startReference)) {
         long originalSize = firstRegion.getSize();
         long distance = startReference.distanceTo(firstRegion.getStartReference());

         // Set to end reference berfore trimming
         currentReference = firstRegion.calculateEndReference();

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

            m_cache.put(currentRegion.getStartReference(), currentRegion);

            freedSize += remainingSize;
         }

         else {
            freedSize += originalSize;
         }

         currentReference = currentRegion.calculateEndReference();
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
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#getData(com.github.jmeta.library.media.api.types.MediumReference, int)
    */
   @Override
   public synchronized ByteBuffer getData(MediumReference reference, int byteCount) {

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

         int startPosition = (int) (reference.distanceTo(nextRegion.getStartReference()));

         ByteBuffer bytes = nextRegion.getBytes();

         bytes.position(bytes.position() + startPosition);

         if (bytes.remaining() > byteCount)
            bytes.limit(bytes.position() + byteCount);

         int summedUpByteCount = bytes.remaining();

         returnedBuffer.put(bytes);

         MediumReference nextReference = nextRegion.calculateEndReference();

         while (summedUpByteCount < byteCount) {
            nextRegion = m_cache.get(nextReference);

            bytes = nextRegion.getBytes();

            if (nextRegion.getSize() > byteCount - summedUpByteCount)
               bytes.limit(bytes.position() + (byteCount - summedUpByteCount));

            returnedBuffer.put(bytes);

            summedUpByteCount += nextRegion.getSize();

            nextReference = nextRegion.calculateEndReference();
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
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#getBufferedByteCountAt(MediumReference)
    */
   @Override
   public synchronized long getBufferedByteCountAt(MediumReference reference) {

      Reject.ifNull(reference, "reference");
      Reject.ifFalse(reference.getMedium().equals(getMedium()), "reference.getMedium().equals(getMedium())");

      MediumRegion region = findContainingRegion(reference);

      if (region == null)
         return 0;

      long summedUpSize = region.calculateEndReference().distanceTo(reference);

      MediumReference nextReference = region.calculateEndReference();

      // Read all continuous regions and sum up their size
      while (m_cache.containsKey(nextReference)) {
         MediumRegion nextRegion = m_cache.get(nextReference);

         summedUpSize += nextRegion.getSize();

         nextReference = nextRegion.calculateEndReference();
      }

      return summedUpSize;
   }

   /**
    * @see com.github.jmeta.library.media.impl.OLD.MediumCache#getBufferedRegions()
    */
   @Override
   public Map<MediumReference, Integer> getBufferedRegions() {

      Map<MediumReference, Integer> returnedMap = new HashMap<>();

      for (Iterator<MediumReference> iterator = m_cache.keySet().iterator(); iterator.hasNext();) {
         MediumReference nextReference = iterator.next();
         MediumRegion nextRegion = m_cache.get(nextReference);

         returnedMap.put(nextReference, nextRegion.getSize());
      }

      return returnedMap;
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#insertData(com.github.jmeta.library.media.api.types.MediumReference,
    *      java.nio.ByteBuffer)
    */
   @Override
   public MediumAction insertData(MediumReference reference, ByteBuffer bytes) {

      // TODO implement for writing

      return null;
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#isAtEndOfMedium(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   public boolean isAtEndOfMedium(MediumReference reference) {

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
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#buffer(MediumReference, long)
    */
   @Override
   public synchronized void buffer(MediumReference cacheReference, int size) throws EndOfMediumException {

      Reject.ifNull(cacheReference, "reference");
      Reject.ifNegativeOrZero(size, "size");

      // Return without action if the region is already cached as such
      if (getBufferedByteCountAt(cacheReference) >= size)
         return;

      MediumReference newStartReference = cacheReference;
      // Reference to the last byte before end of region
      MediumReference newEndReference = cacheReference.advance(size);

      for (Iterator<MediumReference> iterator = m_cache.keySet().iterator(); iterator.hasNext();) {
         final MediumReference existingStartReference = iterator.next();
         final MediumRegion existingCacheRegion = m_cache.get(existingStartReference);

         final MediumReference existingEndReference = existingCacheRegion.calculateEndReference();

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
         if (newStartReference.equals(new StandardMediumReference(getMedium(), getMedium().getCurrentLength()))
            && newStartReference.getAbsoluteMediumOffset() != 0) {
            newStartReference = new StandardMediumReference(getMedium(),
               newStartReference.getAbsoluteMediumOffset() - 1);
            actualSize++;
         }

      MediumReference nextReference = newStartReference;

      try {
         for (int i = 0; i < actualSize / m_maxCacheRegionSize; i++) {
            readIntoCache(nextReference, m_maxCacheRegionSize);

            nextReference = nextReference.advance(m_maxCacheRegionSize);
         }

         if (actualSize % m_maxCacheRegionSize > 0)
            readIntoCache(nextReference, (int) actualSize % m_maxCacheRegionSize);
      } catch (EndOfMediumException e) {
         if (getMedium().isRandomAccess())
            if (!e.getMediumReference().equals(cacheReference) || e.getByteCountTriedToRead() != size)
               throw new EndOfMediumException(
                  (int) (getMedium().getCurrentLength() - cacheReference.getAbsoluteMediumOffset()), cacheReference,
                  size);

         throw e;
      }
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#removeData(com.github.jmeta.library.media.api.types.MediumReference, int)
    */
   @Override
   public MediumAction removeData(MediumReference reference, int byteCountToRemove) {

      // TODO implement for writing
      return null;
   }

   /**
    * @see com.github.jmeta.library.media.api.OLD.IMediumStore_OLD#replaceData(com.github.jmeta.library.media.api.types.MediumReference, int,
    *      java.nio.ByteBuffer)
    */
   @Override
   public MediumAction replaceData(MediumReference reference, int byteCountToReplace, ByteBuffer bytes) {

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
    * Finds the {@link MediumRegion} that contains the given {@link MediumReference}. If there is no such
    * {@link MediumRegion}, then null is returned.
    * 
    * @param reference
    *           The {@link MediumReference}.
    * @return The {@link MediumRegion} containing the given {@link MediumReference} or null if there is no such
    *         {@link MediumRegion}.
    */
   private MediumRegion findContainingRegion(MediumReference reference) {

      if (m_cache.containsKey(reference))
         return m_cache.get(reference);

      Iterator<MediumReference> iterator = m_cache.keySet().iterator();

      while (iterator.hasNext()) {
         MediumReference nextReference = iterator.next();
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
    *           The {@link MediumReference} pointing to the offset where the data is read from.
    * @param size
    *           The number of byte to read.
    * @throws EndOfMediumException
    *            If end of medium occurs during read.
    */
   private void readIntoCache(MediumReference reference, int size) throws EndOfMediumException {

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
    *           The {@link MediumReference} at which to start reading.
    * @param returnedBuffer
    *           The {@link ByteBuffer} to read bytes into, its remaining size is attempted to be read.
    */
   private void uncheckedReadFromMedium(MediumReference reference, ByteBuffer returnedBuffer) {

      m_accessor.setCurrentPosition(reference);

      try {
         m_accessor.read(returnedBuffer);
      } catch (EndOfMediumException e) {
         throw new MediumAccessException("Unexpected end of medium occurred during read", e);
      }
   }
}
