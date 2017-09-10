/**
 *
 * {@link MediumRegion}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package de.je.jmeta.media.api.datatype;

import java.nio.ByteBuffer;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link MediumRegion} represents a part of an external medium. This part can either be cached or not. If it is cached,
 * it additionally contains a sequence of bytes stored in the region, i.e. bytes e.g. previously read from an
 * {@link IMedium}. This class does not support cases where the number of cached bytes does not equal the size of the
 * region. Saying this, if it contains cached data, the number of bytes always exactly equals the size of the region.
 * Either all or nothing is cached.
 * 
 * A {@link MediumRegion} may at maximum have a size of {@link Integer#MAX_VALUE}. However, the minimum size is 0, so
 * the idea of the "empty" region is supported.
 */
public class MediumRegion {

   private IMediumReference startReference;

   private ByteBuffer buffer;

   private int size;

   /**
    * Creates a new {@link MediumRegion} backed with cached bytes.
    * 
    * @param startReference
    *           The start {@link IMediumReference} of the {@link MediumRegion}.
    * @param buffer
    *           The bytes buffered in this {@link MediumRegion}. May be empty. The remaining bytes are considered as
    *           being cached.
    */
   public MediumRegion(IMediumReference startReference, ByteBuffer buffer) {

      Reject.ifNull(buffer, "buffer");
      Reject.ifNull(startReference, "startReference");

      setBuffer(buffer);
      this.startReference = startReference;
   }

   /**
    * Creates a new {@link MediumRegion} not backed by cached bytes, but simply representing a region on the external
    * medium.
    * 
    * @param startReference
    *           The start {@link IMediumReference} of the {@link MediumRegion}.
    * @param size
    *           The size of the region in bytes. May be 0 to represent the empty region.
    */
   public MediumRegion(IMediumReference startReference, int size) {

      Reject.ifTrue(size < 0, "size < 0");
      Reject.ifNull(startReference, "startReference");

      this.buffer = null;
      this.size = size;
      this.startReference = startReference;
   }

   /**
    * Returns the bytes cached in this {@link MediumRegion}, if any. If there are cached bytes, only the bytes remaining
    * in the returned read-only {@link ByteBuffer} must be considered to be cached in this {@link MediumRegion}, other
    * bytes, if any, should be ignored. If there are no cached bytes, this method returns null.
    * 
    * @return the bytes cached in this {@link MediumRegion} or null if there are no cached bytes.
    */
   public ByteBuffer getBytes() {

      if (buffer == null) {
         return null;
      }

      return buffer.asReadOnlyBuffer();
   }

   /**
    * Returns the number of bytes this {@link MediumRegion} represents. Equals the number of cached bytes, if there are
    * any.
    * 
    * @return the number of bytes this {@link MediumRegion} represents.
    */
   public int getSize() {

      return size;
   }

   /**
    * Returns the start {@link IMediumReference} of this {@link MediumRegion}.
    * 
    * @return The start {@link IMediumReference} of this {@link MediumRegion}.
    */
   public IMediumReference getStartReference() {

      return startReference;
   }

   /**
    * Calculates and returns the {@link IMediumReference} pointing to the first byte after this {@link MediumRegion}.
    * 
    * @return the {@link IMediumReference} pointing to the first byte after this {@link MediumRegion}.
    */
   public IMediumReference calculateEndReference() {

      return startReference.advance(getSize());
   }

   /**
    * Splits this {@link MediumRegion} at the given {@link IMediumReference} and returns two new {@link MediumRegion}
    * instances in any case. This existing {@link MediumRegion} instance is kept unchanged.
    * 
    * @param at
    *           The {@link IMediumReference} to split this {@link MediumRegion}. Must be behind the start reference of
    *           this region, and must be smaller than the end reference of this {@link MediumRegion}.
    */
   public MediumRegion[] split(IMediumReference at) {
      Reject.ifNull(at, "at");
      IMediumReference.validateSameMedium(at, getStartReference().getMedium());
      Contract.checkPrecondition(contains(at), "split reference must be contained in this region");
      Contract.checkPrecondition(getStartReference().before(at),
         "split reference must not be equal to the start reference of this region");

      MediumRegion[] returnedSplitRegions = new MediumRegion[2];

      int firstRegionSize = (int) at.distanceTo(getStartReference());
      int secondRegionSize = (int) calculateEndReference().distanceTo(at);

      if (isCached()) {
         // NOTE: It is important here to NOT call getBytes() multiple times for each region, but just once,
         // because it always returns a new read only ByteBuffer copy with its own new position and limit
         // This has, however, the advantage, that we do not change the original ByteBuffer of this region
         // unintentionally.
         ByteBuffer originalBytes = getBytes();

         byte[] firstRegionBytes = new byte[firstRegionSize];
         byte[] secondRegionBytes = new byte[secondRegionSize];

         originalBytes.get(firstRegionBytes);
         originalBytes.get(secondRegionBytes);

         ByteBuffer firstRegionBuffer = ByteBuffer.wrap(firstRegionBytes);
         ByteBuffer secondRegionBuffer = ByteBuffer.wrap(secondRegionBytes);

         returnedSplitRegions[0] = new MediumRegion(getStartReference(), firstRegionBuffer);
         returnedSplitRegions[1] = new MediumRegion(at, secondRegionBuffer);
      } else {
         returnedSplitRegions[0] = new MediumRegion(getStartReference(), firstRegionSize);
         returnedSplitRegions[1] = new MediumRegion(at, secondRegionSize);
      }

      return returnedSplitRegions;
   }

   /**
    * Returns whether the given {@link IMediumReference} is contained in the {@link MediumRegion}, i.e. is between
    * {@link #getStartReference()} inclusive and {@link #calculateEndReference()} exclusive.
    * 
    * @param reference
    *           The {@link IMediumReference} to test for being contained.
    * @return true if it is contained, false otherwise.
    */
   public boolean contains(IMediumReference reference) {

      Reject.ifNull(reference, "reference");
      IMediumReference.validateSameMedium(this.getStartReference(), reference.getMedium());

      return reference.behindOrEqual(getStartReference()) && reference.before(calculateEndReference());
   }

   /**
    * Discards the back bytes of this {@link MediumRegion}, shortening it to a new end {@link IMediumReference}. Only
    * allowed to be called for cached {@link MediumRegion}s.
    * 
    * @param newEndReference
    *           The new end {@link IMediumReference}. Must be contained in the region. That said, it is not allowed to
    *           pass in the end reference of this {@link MediumRegion}, because it is not contained in it.
    */
   public void discardBytesAtEnd(IMediumReference newEndReference) {

      Reject.ifNull(newEndReference, "newEndReference");
      Contract.checkPrecondition(contains(newEndReference), "new end reference must be contained in this region");
      Contract.checkPrecondition(isCached(), "the medium region must be a cached region");

      if (newEndReference.equals(calculateEndReference()))
         return;

      int trimSize = (int) newEndReference.distanceTo(getStartReference());

      buffer.position(getSize() - trimSize);
      ByteBuffer newBuffer = ByteBuffer.allocate(trimSize);

      newBuffer.put(buffer);
      newBuffer.rewind();

      setBuffer(newBuffer);
   }

   /**
    * Discards the front bytes of this {@link MediumRegion}, shortening it to begin at a new start
    * {@link IMediumReference}. Only allowed to be called for cached {@link MediumRegion}s.
    * 
    * @param newStartReference
    *           The new start {@link IMediumReference}. Must be contained in the region. You can pass in the start
    *           reference of the {@link MediumRegion}, which changes nothing on this {@link MediumRegion}.
    */
   public void discardBytesAtFront(IMediumReference newStartReference) {

      Reject.ifNull(newStartReference, "newStartReference");
      Contract.checkPrecondition(contains(newStartReference), "new start reference must be contained in region");
      Contract.checkPrecondition(isCached(), "the medium region must be a cached region");

      if (newStartReference.equals(getStartReference()))
         return;

      int trimSize = (int) newStartReference.distanceTo(getStartReference());
      int newSize = (int) calculateEndReference().distanceTo(newStartReference);

      buffer.position(trimSize);
      ByteBuffer newBuffer = ByteBuffer.allocate(newSize);

      newBuffer.put(buffer);
      newBuffer.rewind();

      setBuffer(newBuffer);

      startReference = newStartReference;
   }

   /**
    * Tells whether this whole {@link MediumRegion} is cached or not. If it is cached, {@link #getBytes()} returns a
    * non-null and non-empty {@link ByteBuffer}
    * 
    * @return true, if the whole {@link MediumRegion} is cached, false otherwise
    */
   public boolean isCached() {

      return this.buffer != null;
   }

   /**
    * Tells whether this {@link MediumRegion} overlaps another {@link MediumRegion} at its back or not. This method
    * returns true in the following cases (S stands for start byte, E for end byte, "_" stands for any other byte):
    * <p>
    * <b>Case 1:</b> Overlap at back
    * <p>
    * <code>other: S__________E___</code>
    * <p>
    * <code>this : ___S__________E</code>
    * <p>
    * <b>Case 2:</b> Overlap until end byte
    * <p>
    * <code>other: S__________E</code>
    * <p>
    * <code>this : ___S_______E</code>
    * <p>
    * <b>Case 3:</b> Regions are equal
    * <p>
    * <code>other: S__________E</code>
    * <p>
    * <code>this : S__________E</code>
    * <p>
    * 
    * Likewise, it returns false in the following cases:
    * 
    * <p>
    * <b>Case 4:</b> Overlap at front
    * <p>
    * <code>other: ___S__________E</code>
    * <p>
    * <code>this : S__________E___</code>
    * <p>
    * <b>Case 5:</b> Overlap starting with start byte
    * <p>
    * <code>other: S__________E</code>
    * <p>
    * <code>this : S_______E___</code>
    * <p>
    * <b>Case 6:</b> This region contains other region
    * <p>
    * <code>other: __S______E__</code>
    * <p>
    * <code>this : S__________E</code>
    * <p>
    * <b>Case 7:</b> Other region contains this region
    * <p>
    * <code>other: S__________E</code>
    * <p>
    * <code>this : __S______E__</code>
    * <p>
    * <b>Case 8:</b> The regions do not share any bytes at all.
    * <p>
    * 
    * That said, {@link #overlapsOtherRegionAtBack(MediumRegion)} and {@link #overlapsOtherRegionAtFront(MediumRegion)}
    * only both return true in the case that this and the other region are equal.
    * 
    * @param other
    *           The other {@link MediumRegion} checked for overlaps by this {@link MediumRegion}. Must refer to the same
    *           {@link IMedium} as this {@link MediumRegion}.
    * @return true if an overlap at back is detected, false otherwise.
    */
   public boolean overlapsOtherRegionAtBack(MediumRegion other) {
      Reject.ifNull(other, "other");
      IMediumReference.validateSameMedium(this.getStartReference(), other.getStartReference().getMedium());

      IMediumReference startRef = getStartReference();
      IMediumReference otherStartRef = other.getStartReference();
      IMediumReference endRef = calculateEndReference();
      IMediumReference otherEndRef = other.calculateEndReference();

      return startRef.behindOrEqual(otherStartRef) && startRef.before(otherEndRef) && endRef.behindOrEqual(otherEndRef);
   }

   /**
    * Tells whether this {@link MediumRegion} overlaps another {@link MediumRegion} at its front or not. This method
    * returns true in the following cases (S stands for start byte, E for end byte, "_" stands for any other byte):
    * <p>
    * <b>Case 1:</b> Overlap at front
    * <p>
    * <code>other: ___S__________E</code>
    * <p>
    * <code>this : S__________E___</code>
    * <p>
    * <b>Case 2:</b> Overlap starting with start byte
    * <p>
    * <code>other: S__________E</code>
    * <p>
    * <code>this : S_______E___</code>
    * <p>
    * <b>Case 3:</b> Regions are equal
    * <p>
    * <code>other: S__________E</code>
    * <p>
    * <code>this : S__________E</code>
    * <p>
    * 
    * Likewise, it returns false in the following cases:
    * 
    * <p>
    * <b>Case 4:</b> Overlap at back
    * <p>
    * <code>other: S__________E___</code>
    * <p>
    * <code>this : ___S__________E</code>
    * <p>
    * <b>Case 5:</b> Overlap until end byte
    * <p>
    * <code>other: S__________E</code>
    * <p>
    * <code>this : ___S_______E</code>
    * <p>
    * <b>Case 6:</b> This region contains other region
    * <p>
    * <code>other: __S______E__</code>
    * <p>
    * <code>this : S__________E</code>
    * <p>
    * <b>Case 7:</b> Other region contains this region
    * <p>
    * <code>other: S__________E</code>
    * <p>
    * <code>this : __S______E__</code>
    * <p>
    * <b>Case 8:</b> The regions do not share any bytes at all.
    * <p>
    * 
    * That said, {@link #overlapsOtherRegionAtBack(MediumRegion)} and {@link #overlapsOtherRegionAtFront(MediumRegion)}
    * only both return true in the case that this and the other region are equal.
    * 
    * @param other
    *           The other {@link MediumRegion} checked for overlaps by this {@link MediumRegion}. Must refer to the same
    *           {@link IMedium} as this {@link MediumRegion}.
    * @return true if an overlap at front is detected, false otherwise.
    */
   public boolean overlapsOtherRegionAtFront(MediumRegion other) {
      Reject.ifNull(other, "other");
      IMediumReference.validateSameMedium(this.getStartReference(), other.getStartReference().getMedium());

      IMediumReference startRef = getStartReference();
      IMediumReference otherStartRef = other.getStartReference();
      IMediumReference endRef = calculateEndReference();
      IMediumReference otherEndRef = other.calculateEndReference();

      return otherStartRef.behindOrEqual(startRef) && otherStartRef.before(endRef) && otherEndRef.behindOrEqual(endRef);
   }

   /**
    * Returns the number of bytes present both in this {@link MediumRegion} and in another {@link MediumRegion}.
    * 
    * @param other
    *           The other {@link MediumRegion} checked for shared bytes. Must refer to the same {@link IMedium} as this
    *           {@link MediumRegion}.
    * @return The number of shared bytes between the two {@link MediumRegion}s or 0 if there are none.
    */
   public int getOverlappingByteCount(MediumRegion other) {
      Reject.ifNull(other, "other");
      IMediumReference.validateSameMedium(this.getStartReference(), other.getStartReference().getMedium());

      IMediumReference startRef = getStartReference();
      IMediumReference otherStartRef = other.getStartReference();
      IMediumReference endRef = calculateEndReference();
      IMediumReference otherEndRef = other.calculateEndReference();

      if (overlapsOtherRegionAtFront(other)) {
         return (int) endRef.distanceTo(otherStartRef);
      } else if (overlapsOtherRegionAtBack(other)) {
         return (int) otherEndRef.distanceTo(startRef);
      } else if (startRef.before(otherStartRef) && otherEndRef.before(endRef)) {
         return other.getSize();
      } else if (otherStartRef.before(startRef) && endRef.before(otherEndRef)) {
         return getSize();
      }

      return 0;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return "MediumRegion [[" + getStartReference().getAbsoluteMediumOffset() + ", "
         + (getStartReference().getAbsoluteMediumOffset() + getSize()) + "), size=" + getSize() + ", buffer=" + buffer
         + ", on " + getStartReference().getMedium() + "]";
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + ((buffer == null) ? 0 : buffer.hashCode());
      result = prime * result + size;
      result = prime * result + ((startReference == null) ? 0 : startReference.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {

      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      MediumRegion other = (MediumRegion) obj;
      if (size != other.size)
         return false;
      if (startReference == null) {
         if (other.startReference != null)
            return false;
      } else if (!startReference.equals(other.startReference))
         return false;
      if (buffer == null) {
         if (other.buffer != null)
            return false;
      } else if (!buffer.equals(other.buffer))
         return false;
      return true;
   }

   /**
    * Resets the internal {@link ByteBuffer} and ensures that the size of the region gets updated correctly.
    * 
    * @param buffer
    *           The new {@link ByteBuffer} to set.
    */
   private void setBuffer(ByteBuffer buffer) {

      this.buffer = buffer;
      this.size = buffer.remaining();
   }
}
