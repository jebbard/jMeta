/**
 *
 * {@link MediumRegion}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package com.github.jmeta.library.media.api.types;

import java.nio.ByteBuffer;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediumRegion} represents a part of an external medium. This part can either be cached or not. If it is cached,
 * it additionally contains a sequence of bytes stored in the region, i.e. bytes e.g. previously read from an
 * {@link Medium}. This class does not support cases where the number of cached bytes does not equal the size of the
 * region. Saying this, if it contains cached data, the number of bytes always exactly equals the size of the region.
 * Either all or nothing is cached.
 * 
 * A {@link MediumRegion} may at maximum have a size of {@link Integer#MAX_VALUE}. However, the minimum size is 0, so
 * the idea of the "empty" region is supported.
 */
public class MediumRegion {

   /**
    * Summarizes all relevant cases for types of overlaps between two {@link MediumRegion}s located on the same
    * {@link Medium}.
    */
   public enum MediumRegionOverlapType {
      /**
       * The regions do not overlap with each other, e.g. the left region starts and ends before the right region.
       */
      NO_OVERLAP,
      /**
       * The regions exactly cover the same offset range.
       */
      SAME_RANGE,
      /**
       * The left region is contained inside the right region. This does not include regions having
       * {@link MediumRegionOverlapType#SAME_RANGE}, but it covers also the cases that both regions start or end at the
       * same offset and the right region is bigger than the left on, i.e. cases where also
       * {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)} would return true. In the case that the left
       * {@link MediumRegion} starts or ends at the same offset as the right {@link MediumRegion},
       * {@link MediumRegion#determineOverlapWithOtherRegion(MediumRegion, MediumRegion)} returns
       * {@link #LEFT_FULLY_INSIDE_RIGHT}, and not {@link #LEFT_OVERLAPS_RIGHT_AT_FRONT} or
       * {@link #LEFT_OVERLAPS_RIGHT_AT_BACK}.
       */
      LEFT_FULLY_INSIDE_RIGHT,
      /**
       * The right region is contained inside the left region. This does not include regions having
       * {@link MediumRegionOverlapType#SAME_RANGE}, but it covers also the cases that both regions start or end at the
       * same offset and the left region is bigger than the right on, i.e. cases where also
       * {@link MediumRegion#overlapsOtherRegionAtFront(MediumRegion)} would return true.
       */
      RIGHT_FULLY_INSIDE_LEFT,
      /**
       * Represents the case that the left region overlaps the right region at its front, but it neither has the same
       * range, nor is it fully inside the right region, nor does it enclose the right region.
       * {@link #LEFT_OVERLAPS_RIGHT_AT_FRONT} represents a real overlap at front, i.e. the left region starts early as
       * the right region, overlaps it at its front by at least one byte and ends earlier as the right region.
       */
      LEFT_OVERLAPS_RIGHT_AT_FRONT,
      /**
       * Represents the case that the left region overlaps the right region at its back, but it neither has the same
       * range, nor is it fully inside the right region, nor does it enclose the right region.
       * {@link #LEFT_OVERLAPS_RIGHT_AT_BACK} represents a real overlap at back, i.e. the left region starts after the
       * right region, overlaps it at its back by at least one byte and ends after the right region.
       */
      LEFT_OVERLAPS_RIGHT_AT_BACK,
   }

   /**
    * {@link MediumRegionClipResult} represents the result of clipping two overlapping {@link MediumRegion}s against
    * each using {@link MediumRegion#clipOverlappingRegions(MediumRegion, MediumRegion)}.
    */
   public static class MediumRegionClipResult {

      /**
       * Creates a new {@link MediumRegionClipResult}.
       * 
       * @param overlappedPartOfLeftRegion
       *           See getter for details, must not be null
       * @param nonOverlappedPartOfLeftRegionAtFront
       *           See getter for details
       * @param nonOverlappedPartOfLeftRegionAtBack
       *           See getter for details
       */
      public MediumRegionClipResult(MediumRegion overlappedPartOfLeftRegion,
         MediumRegion nonOverlappedPartOfLeftRegionAtFront, MediumRegion nonOverlappedPartOfLeftRegionAtBack) {
         Reject.ifNull(overlappedPartOfLeftRegion, "overlappedPartOfLeftRegion");

         this.overlappedPartOfLeftRegion = overlappedPartOfLeftRegion;
         this.nonOverlappedPartOfLeftRegionAtFront = nonOverlappedPartOfLeftRegionAtFront;
         this.nonOverlappedPartOfLeftRegionAtBack = nonOverlappedPartOfLeftRegionAtBack;
      }

      /**
       * @return a {@link MediumRegion} that is the overlapping part of the two {@link MediumRegion}s, containing the
       *         bytes in the overlap area taken from the original left master {@link MediumRegion}.
       */
      public MediumRegion getOverlappingPartOfLeftRegion() {
         return overlappedPartOfLeftRegion;
      }

      /**
       * @return any part of the left master region that does not overlap at the front, or null if there is no part of
       *         the left master region starting before the right region.
       */
      public MediumRegion getNonOverlappedPartOfLeftRegionAtFront() {
         return nonOverlappedPartOfLeftRegionAtFront;
      }

      /**
       * @return any part of the left master region that does not overlap at the back, or null if there is no part of
       *         the left master region ending behind the right region.
       */
      public MediumRegion getNonOverlappedPartOfLeftRegionAtBack() {
         return nonOverlappedPartOfLeftRegionAtBack;
      }

      private final MediumRegion overlappedPartOfLeftRegion;
      private final MediumRegion nonOverlappedPartOfLeftRegionAtFront;
      private final MediumRegion nonOverlappedPartOfLeftRegionAtBack;
   }

   private MediumOffset startReference;

   private ByteBuffer buffer;

   private int size;

   /**
    * Creates a new {@link MediumRegion} backed with cached bytes.
    * 
    * @param startReference
    *           The start {@link MediumOffset} of the {@link MediumRegion}.
    * @param buffer
    *           The bytes buffered in this {@link MediumRegion}. May be empty. The remaining bytes are considered as
    *           being cached.
    */
   public MediumRegion(MediumOffset startReference, ByteBuffer buffer) {

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
    *           The start {@link MediumOffset} of the {@link MediumRegion}.
    * @param size
    *           The size of the region in bytes. May be 0 to represent the empty region.
    */
   public MediumRegion(MediumOffset startReference, int size) {

      Reject.ifNegative(size, "size");
      Reject.ifNull(startReference, "startReference");

      this.buffer = null;
      this.size = size;
      this.startReference = startReference;
   }

   /**
    * Allows for readable code that uses the overlapping relations between two {@link MediumRegion}s. Two regions can in
    * principle have all the overlap behaviors as described in the class {@link MediumRegionOverlapType}.
    * 
    * @param left
    *           The left {@link MediumRegion}, must refer to the same {@link Medium} as the right {@link MediumRegion}
    * @param right
    *           The right {@link MediumRegion}, must refer to the same {@link Medium} as the left {@link MediumRegion},
    *           must point to the same {@link Medium} as the left {@link MediumRegion}
    * @return The determined {@link MediumRegionOverlapType} of the two regions
    */
   public static MediumRegionOverlapType determineRegionOverlap(MediumRegion left, MediumRegion right) {
      Reject.ifNull(right, "right");
      Reject.ifNull(left, "left");
      Reject.ifFalse(left.getStartOffset().getMedium().equals(right.getStartOffset().getMedium()),
         "left.getStartReference().getMedium().equals(right.getStartReference().getMedium()");

      int overlappingByteCount = left.getOverlappingByteCount(right);

      if (overlappingByteCount == 0) {
         return MediumRegionOverlapType.NO_OVERLAP;
      } else if (overlappingByteCount == left.getSize() && overlappingByteCount == right.getSize()) {
         return MediumRegionOverlapType.SAME_RANGE;
      } else if (overlappingByteCount == left.getSize()) {
         return MediumRegionOverlapType.LEFT_FULLY_INSIDE_RIGHT;
      } else if (overlappingByteCount == right.getSize()) {
         return MediumRegionOverlapType.RIGHT_FULLY_INSIDE_LEFT;
      } else if (left.overlapsOtherRegionAtFront(right)) {
         return MediumRegionOverlapType.LEFT_OVERLAPS_RIGHT_AT_FRONT;
      } else if (left.overlapsOtherRegionAtBack(right)) {
         return MediumRegionOverlapType.LEFT_OVERLAPS_RIGHT_AT_BACK;
      } else {
         throw new IllegalStateException(
            "Impossible overlap of two regions detected: left=" + left + ", right=" + right);
      }
   }

   /**
    * Performs clipping of two {@link MediumRegion}s overlapping each other, where the left {@link MediumRegion} is
    * considered as "master region", which means that the overlapped portion in the returned
    * {@link MediumRegionClipResult} contains the bytes of the left {@link MediumRegion}. Only non-overlapping parts of
    * the left region are returned. That said, the left {@link MediumRegion} is what counts here: Parts of the right
    * {@link MediumRegion} never appear in the {@link MediumRegionClipResult}.
    * 
    * @param leftMasterRegion
    *           The left master {@link MediumRegion} to clip against the right region
    * @param rightRegion
    *           The right {@link MediumRegion} to clip against, must overlap the left region, must point to the same
    *           {@link Medium} as the left {@link MediumRegion}
    * @return A {@link MediumRegionClipResult} containing all clipped portions of the left master {@link MediumRegion},
    *         see {@link MediumRegionClipResult} for details.
    */
   public static MediumRegionClipResult clipOverlappingRegions(MediumRegion leftMasterRegion,
      MediumRegion rightRegion) {
      Reject.ifNull(rightRegion, "rightRegion");
      Reject.ifNull(leftMasterRegion, "leftMasterRegion");

      Reject.ifTrue(leftMasterRegion.getOverlappingByteCount(rightRegion) == 0,
         "leftMasterRegion.getOverlappingByteCount(rightRegion) == 0");

      MediumRegion overlappedPartOfLeftRegion = leftMasterRegion;
      MediumRegion nonOverlappedPartOfLeftRegionAtFront = null;
      MediumRegion nonOverlappedPartOfLeftRegionAtBack = null;

      if (leftMasterRegion.getStartOffset().before(rightRegion.getStartOffset())) {
         MediumRegion[] splitRegions = leftMasterRegion.split(rightRegion.getStartOffset());

         nonOverlappedPartOfLeftRegionAtFront = splitRegions[0];
         overlappedPartOfLeftRegion = splitRegions[1];
      }

      if (rightRegion.calculateEndOffset().before(leftMasterRegion.calculateEndOffset())) {
         MediumRegion[] splitRegions = overlappedPartOfLeftRegion.split(rightRegion.calculateEndOffset());

         overlappedPartOfLeftRegion = splitRegions[0];
         nonOverlappedPartOfLeftRegionAtBack = splitRegions[1];
      }

      return new MediumRegionClipResult(overlappedPartOfLeftRegion, nonOverlappedPartOfLeftRegionAtFront,
         nonOverlappedPartOfLeftRegionAtBack);
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
    * Returns the start {@link MediumOffset} of this {@link MediumRegion}.
    * 
    * @return The start {@link MediumOffset} of this {@link MediumRegion}.
    */
   public MediumOffset getStartOffset() {

      return startReference;
   }

   /**
    * Calculates and returns the {@link MediumOffset} pointing to the first byte after this {@link MediumRegion}.
    * 
    * @return the {@link MediumOffset} pointing to the first byte after this {@link MediumRegion}.
    */
   public MediumOffset calculateEndOffset() {

      return startReference.advance(getSize());
   }

   /**
    * Splits this {@link MediumRegion} at the given {@link MediumOffset} and returns two new {@link MediumRegion}
    * instances in any case. This existing {@link MediumRegion} instance is kept unchanged.
    * 
    * @param at
    *           The {@link MediumOffset} to split this {@link MediumRegion}. Must be behind the start offset of this
    *           region, and must be smaller than the end offset of this {@link MediumRegion}.
    * 
    * @return An array of size 2, containing the split region parts in offset order
    */
   public MediumRegion[] split(MediumOffset at) {
      Reject.ifNull(at, "at");
      Reject.ifFalse(at.getMedium().equals(getStartOffset().getMedium()),
         "at.getMedium().equals(getStartOffset().getMedium())");
      Reject.ifFalse(contains(at), "contains(at)");
      Reject.ifFalse(getStartOffset().before(at), "getStartOffset().before(at)");

      MediumRegion[] returnedSplitRegions = new MediumRegion[2];

      int firstRegionSize = (int) at.distanceTo(getStartOffset());
      int secondRegionSize = (int) calculateEndOffset().distanceTo(at);

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

         returnedSplitRegions[0] = new MediumRegion(getStartOffset(), firstRegionBuffer);
         returnedSplitRegions[1] = new MediumRegion(at, secondRegionBuffer);
      } else {
         returnedSplitRegions[0] = new MediumRegion(getStartOffset(), firstRegionSize);
         returnedSplitRegions[1] = new MediumRegion(at, secondRegionSize);
      }

      return returnedSplitRegions;
   }

   /**
    * Returns whether the given {@link MediumOffset} is contained in the {@link MediumRegion}, i.e. is between
    * {@link #getStartOffset()} inclusive and {@link #calculateEndOffset()} exclusive.
    * 
    * @param reference
    *           The {@link MediumOffset} to test for being contained.
    * @return true if it is contained, false otherwise.
    */
   public boolean contains(MediumOffset reference) {

      Reject.ifNull(reference, "reference");
      Reject.ifFalse(reference.getMedium().equals(getStartOffset().getMedium()),
         "reference.getMedium().equals(getStartReference().getMedium())");

      return reference.behindOrEqual(getStartOffset()) && reference.before(calculateEndOffset());
   }

   /**
    * Discards the back bytes of this {@link MediumRegion}, shortening it to a new end {@link MediumOffset}. Only
    * allowed to be called for cached {@link MediumRegion}s.
    * 
    * @param newEndReference
    *           The new end {@link MediumOffset}. Must be contained in the region. That said, it is not allowed to pass
    *           in the end reference of this {@link MediumRegion}, because it is not contained in it.
    */
   public void discardBytesAtEnd(MediumOffset newEndReference) {

      Reject.ifNull(newEndReference, "newEndReference");
      Reject.ifFalse(contains(newEndReference), "contains(newEndReference)");
      Reject.ifFalse(isCached(), "isCached()");

      if (newEndReference.equals(calculateEndOffset()))
         return;

      int trimSize = (int) newEndReference.distanceTo(getStartOffset());

      buffer.position(getSize() - trimSize);
      ByteBuffer newBuffer = ByteBuffer.allocate(trimSize);

      newBuffer.put(buffer);
      newBuffer.rewind();

      setBuffer(newBuffer);
   }

   /**
    * Discards the front bytes of this {@link MediumRegion}, shortening it to begin at a new start {@link MediumOffset}.
    * Only allowed to be called for cached {@link MediumRegion}s.
    * 
    * @param newStartReference
    *           The new start {@link MediumOffset}. Must be contained in the region. You can pass in the start reference
    *           of the {@link MediumRegion}, which changes nothing on this {@link MediumRegion}.
    */
   public void discardBytesAtFront(MediumOffset newStartReference) {

      Reject.ifNull(newStartReference, "newStartReference");
      Reject.ifFalse(contains(newStartReference), "contains(newStartReference)");
      Reject.ifFalse(isCached(), "isCached()");

      if (newStartReference.equals(getStartOffset()))
         return;

      int trimSize = (int) newStartReference.distanceTo(getStartOffset());
      int newSize = (int) calculateEndOffset().distanceTo(newStartReference);

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
    * <b>Case 6:</b> This region fully encloses other region
    * <p>
    * <code>other: __S______E__</code>
    * <p>
    * <code>this : S__________E</code>
    * <p>
    * <b>Case 7:</b> Other region fully encloses this region
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
    *           {@link Medium} as this {@link MediumRegion}.
    * @return true if an overlap at back is detected, false otherwise.
    */
   public boolean overlapsOtherRegionAtBack(MediumRegion other) {
      return other.overlapsOtherRegionAtFront(this);
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
    * <b>Case 6:</b> This region fully encloses other region
    * <p>
    * <code>other: __S______E__</code>
    * <p>
    * <code>this : S__________E</code>
    * <p>
    * <b>Case 7:</b> Other region fully encloses this region
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
    *           {@link Medium} as this {@link MediumRegion}.
    * @return true if an overlap at front is detected, false otherwise.
    */
   public boolean overlapsOtherRegionAtFront(MediumRegion other) {
      Reject.ifNull(other, "other");
      Reject.ifFalse(other.getStartOffset().getMedium().equals(getStartOffset().getMedium()),
         "other.getStartReference().getMedium().equals(getStartReference().getMedium())");

      MediumOffset startRef = getStartOffset();
      MediumOffset otherStartRef = other.getStartOffset();
      MediumOffset endRef = calculateEndOffset();
      MediumOffset otherEndRef = other.calculateEndOffset();

      return otherStartRef.behindOrEqual(startRef) && otherStartRef.before(endRef) && otherEndRef.behindOrEqual(endRef);
   }

   /**
    * Returns the number of bytes present both in this {@link MediumRegion} and in another {@link MediumRegion}.
    * 
    * @param other
    *           The other {@link MediumRegion} checked for shared bytes. Must refer to the same {@link Medium} as this
    *           {@link MediumRegion}.
    * @return The number of shared bytes between the two {@link MediumRegion}s or 0 if there are none.
    */
   public int getOverlappingByteCount(MediumRegion other) {
      Reject.ifNull(other, "other");
      Reject.ifFalse(other.getStartOffset().getMedium().equals(getStartOffset().getMedium()),
         "other.getStartReference().getMedium().equals(getStartReference().getMedium())");

      MediumOffset startRef = getStartOffset();
      MediumOffset otherStartRef = other.getStartOffset();
      MediumOffset endRef = calculateEndOffset();
      MediumOffset otherEndRef = other.calculateEndOffset();

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

      return "MediumRegion [[" + getStartOffset().getAbsoluteMediumOffset() + ", "
         + (getStartOffset().getAbsoluteMediumOffset() + getSize()) + "), size=" + getSize() + ", buffer=" + buffer
         + ", on " + getStartOffset().getMedium() + "]";
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
