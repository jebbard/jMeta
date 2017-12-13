/**
 *
 * {@link StandardMediumOffset}.java
 *
 * @author Jens Ebert
 *
 * @date 08.03.2011
 */
package com.github.jmeta.library.media.impl.offset;

import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardMediumOffset} is the default implementation of {@link MediumOffset}.
 */
public class StandardMediumOffset implements MediumOffset {

   private final Medium<?> medium;

   private long absoluteMediumOffset;

   private MediumOffsetFactory factory;

   /**
    * Creates a new {@link StandardMediumOffset}.
    * 
    * @param medium
    *           The {@link Medium} to refer to.
    * @param absoluteMediumOffset
    *           The absolute offset in the {@link Medium}, relative to its starting point which is offset 0. Must not be
    *           smaller than 0.
    */
   public StandardMediumOffset(Medium<?> medium, long absoluteMediumOffset) {

      Reject.ifNull(medium, "medium");

      this.medium = medium;
      setAbsoluteMediumOffset(absoluteMediumOffset);
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumOffset#advance(long)
    */
   @Override
   public MediumOffset advance(long count) {

      Reject.ifFalse(getAbsoluteMediumOffset() >= -count, "getAbsoluteMediumOffset() >= -count");

      long advancedMediumOffset = getAbsoluteMediumOffset() + count;

      if (this.factory != null) {
         return factory.createMediumOffset(advancedMediumOffset);
      }

      return new StandardMediumOffset(getMedium(), advancedMediumOffset);
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumOffset#before(com.github.jmeta.library.media.api.types.MediumOffset)
    */
   @Override
   public boolean before(MediumOffset other) {

      Reject.ifNull(other, "other");
      Reject.ifFalse(getMedium().equals(other.getMedium()), "getMedium().equals(other.getMedium())");

      return getAbsoluteMediumOffset() < other.getAbsoluteMediumOffset();
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumOffset#behindOrEqual(com.github.jmeta.library.media.api.types.MediumOffset)
    */
   @Override
   public boolean behindOrEqual(MediumOffset other) {

      Reject.ifNull(other, "other");
      Reject.ifFalse(getMedium().equals(other.getMedium()), "getMedium().equals(other.getMedium())");

      return getAbsoluteMediumOffset() >= other.getAbsoluteMediumOffset();
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumOffset#distanceTo(com.github.jmeta.library.media.api.types.MediumOffset)
    */
   @Override
   public long distanceTo(MediumOffset other) {

      Reject.ifNull(other, "other");
      Reject.ifFalse(getMedium().equals(other.getMedium()), "getMedium().equals(other.getMedium())");

      return getAbsoluteMediumOffset() - other.getAbsoluteMediumOffset();
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumOffset#getAbsoluteMediumOffset()
    */
   @Override
   public long getAbsoluteMediumOffset() {

      return absoluteMediumOffset;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumOffset#getMedium()
    */
   @Override
   public Medium<?> getMedium() {

      return medium;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      MediumOffset other = (MediumOffset) obj;
      if (absoluteMediumOffset != other.getAbsoluteMediumOffset()) {
         return false;
      }
      if (medium == null) {
         if (other.getMedium() != null) {
            return false;
         }
      } else if (!medium.equals(other.getMedium())) {
         return false;
      }
      return true;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + (int) (absoluteMediumOffset ^ (absoluteMediumOffset >>> 32));
      result = prime * result + ((medium == null) ? 0 : medium.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return "StandardMediumReference [absoluteMediumOffset=" + absoluteMediumOffset + " ("
         + Long.toHexString(absoluteMediumOffset).toUpperCase() + " HEX), medium=" + medium + "]";
   }

   /**
    * Sets the {@link MediumOffsetFactory} this {@link MediumOffset} belongs to. Whenever the {@link #advance(long)}
    * method is called, the advanced {@link MediumOffset} instance is added to this {@link MediumOffsetFactory}.
    * 
    * @param factory
    *           The {@link MediumOffsetFactory} to add this {@link MediumOffset} to.
    */
   void setMediumReferenceRepository(MediumOffsetFactory factory) {

      Reject.ifNull(factory, "factory");

      this.factory = factory;
   }

   /**
    * Sets a new absolute medium offset for this {@link StandardMediumOffset}.
    * 
    * @param absoluteMediumOffset
    *           The absolute offset in the {@link Medium}, relative to its starting point which is offset 0. Must not be
    *           smaller than 0.
    */
   void setAbsoluteMediumOffset(long absoluteMediumOffset) {

      Reject.ifNegative(absoluteMediumOffset, "absoluteMediumOffset");

      this.absoluteMediumOffset = absoluteMediumOffset;
   }
}
