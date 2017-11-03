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

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.exceptions.ReadOnlyMediumException;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.cache.MediumCache;
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

   public StandardMediumStore(MediumAccessor<T> mediumAccessor) {
      Reject.ifNull(mediumAccessor, "mediumAccessor");

      this.mediumAccessor = mediumAccessor;
      T medium = mediumAccessor.getMedium();
      cache = new MediumCache(medium, medium.getMaxCacheSizeInBytes(), medium.getMaxCacheRegionSizeInBytes());
      referenceFactory = new MediumReferenceFactory(medium);

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

      cache.addRegion(new MediumRegion(offset, ByteBuffer.allocate(numberOfBytes)));
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#getCachedByteCountAt(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   public long getCachedByteCountAt(MediumReference offset) {
      Reject.ifNull(offset, "offset");
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");
      return cache.getCachedByteCountAt(offset);
   }

   /**
    * @see com.github.jmeta.library.media.api.services.MediumStore#getData(com.github.jmeta.library.media.api.types.MediumReference,
    *      int)
    */
   @Override
   public ByteBuffer getData(MediumReference offset, int numberOfBytes) {
      Reject.ifNull(offset, "offset");
      ensureOpened();
      Reject.ifFalse(offset.getMedium().equals(getMedium()), "offset.getMedium().equals(getMedium())");

      return null;
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
