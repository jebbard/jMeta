/**
 *
 * {@link StandardMediumReference}.java
 *
 * @author Jens Ebert
 *
 * @date 08.03.2011
 */
package com.github.jmeta.library.media.impl.reference;

import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardMediumReference} refers to a specific offset on a {@link Medium}.
 * 
 * It refers to a position of byte N on {@link Medium} X, N >= 0, and therefore gives a globally unique address of each
 * medium byte. {@link StandardMediumReference} referring to the same {@link Medium} can be compared as to which
 * position is before, equal or behind another one.
 */
public class StandardMediumReference implements MediumReference {

   private final Medium<?> medium;

   private long absoluteMediumOffset;

   private MediumReferenceFactory factory;

   /**
    * Creates a new {@link StandardMediumReference}.
    * 
    * @param medium
    *           The {@link Medium} to refer to.
    * @param absoluteMediumOffset
    *           The absolute offset in the {@link Medium}, relative to its starting point which is offset 0. Must not
    *           be smaller than 0.
    */
   public StandardMediumReference(Medium<?> medium, long absoluteMediumOffset) {

      Reject.ifNull(medium, "medium");

      this.medium = medium;
      setAbsoluteMediumOffset(absoluteMediumOffset);
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumReference#advance(long)
    */
   @Override
   public MediumReference advance(long count) {

      Reject.ifFalse(getAbsoluteMediumOffset() >= -count, "getAbsoluteMediumOffset() >= -count");

      long advancedMediumOffset = getAbsoluteMediumOffset() + count;

      if (this.factory != null) {
         return factory.createMediumReference(advancedMediumOffset);
      }

      return new StandardMediumReference(getMedium(), advancedMediumOffset);
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumReference#before(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   public boolean before(MediumReference other) {

      Reject.ifNull(other, "other");
      Reject.ifFalse(getMedium().equals(other.getMedium()),
	   	         "getMedium().equals(other.getMedium())");

      return getAbsoluteMediumOffset() < other.getAbsoluteMediumOffset();
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumReference#behindOrEqual(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   public boolean behindOrEqual(MediumReference other) {

      Reject.ifNull(other, "other");
      Reject.ifFalse(getMedium().equals(other.getMedium()),
	   	         "getMedium().equals(other.getMedium())");

      return getAbsoluteMediumOffset() >= other.getAbsoluteMediumOffset();
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumReference#distanceTo(com.github.jmeta.library.media.api.types.MediumReference)
    */
   @Override
   public long distanceTo(MediumReference other) {

      Reject.ifNull(other, "other");
      Reject.ifFalse(getMedium().equals(other.getMedium()),
	   	         "getMedium().equals(other.getMedium())");

      return getAbsoluteMediumOffset() - other.getAbsoluteMediumOffset();
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumReference#getAbsoluteMediumOffset()
    */
   @Override
   public long getAbsoluteMediumOffset() {

      return absoluteMediumOffset;
   }

   /**
    * @see com.github.jmeta.library.media.api.types.MediumReference#getMedium()
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
      MediumReference other = (MediumReference) obj;
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
    * Sets the {@link MediumReferenceFactory} this {@link MediumReference} belongs to. Whenever the
    * {@link #advance(long)} method is called, the advanced {@link MediumReference} instance is added to this
    * {@link MediumReferenceFactory}.
    * 
    * @param factory
    *           The {@link MediumReferenceFactory} to add this {@link MediumReference} to.
    */
   void setMediumReferenceRepository(MediumReferenceFactory factory) {

      Reject.ifNull(factory, "factory");

      this.factory = factory;
   }

   /**
    * Sets a new absolute medium offset for this {@link StandardMediumReference}.
    * 
    * @param absoluteMediumOffset
    *           The absolute offset in the {@link Medium}, relative to its starting point which is offset 0. Must not
    *           be smaller than 0.
    */
   void setAbsoluteMediumOffset(long absoluteMediumOffset) {

	   Reject.ifNegative(absoluteMediumOffset, "absoluteMediumOffset");

      this.absoluteMediumOffset = absoluteMediumOffset;
   }
}
