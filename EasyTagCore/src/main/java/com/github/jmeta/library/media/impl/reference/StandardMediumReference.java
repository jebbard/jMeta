/**
 *
 * {@link StandardMediumReference}.java
 *
 * @author Jens Ebert
 *
 * @date 08.03.2011
 */
package com.github.jmeta.library.media.impl.reference;

import com.github.jmeta.library.media.api.type.IMedium;
import com.github.jmeta.library.media.api.type.IMediumReference;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardMediumReference} refers to a specific offset on a {@link IMedium}.
 * 
 * It refers to a position of byte N on {@link IMedium} X, N >= 0, and therefore gives a globally unique address of each
 * medium byte. {@link StandardMediumReference} referring to the same {@link IMedium} can be compared as to which
 * position is before, equal or behind another one.
 */
public class StandardMediumReference implements IMediumReference {

   private final IMedium<?> medium;

   private long absoluteMediumOffset;

   private MediumReferenceFactory factory;

   /**
    * Creates a new {@link StandardMediumReference}.
    * 
    * @param medium
    *           The {@link IMedium} to refer to.
    * @param absoluteMediumOffset
    *           The absolute offset in the {@link IMedium}, relative to its starting point which is offset 0. Must not
    *           be smaller than 0.
    */
   public StandardMediumReference(IMedium<?> medium, long absoluteMediumOffset) {

      Reject.ifNull(medium, "medium");

      this.medium = medium;
      setAbsoluteMediumOffset(absoluteMediumOffset);
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMediumReference#advance(long)
    */
   @Override
   public IMediumReference advance(long count) {

      Reject.ifFalse(getAbsoluteMediumOffset() >= -count, "getAbsoluteMediumOffset() >= -count");

      long advancedMediumOffset = getAbsoluteMediumOffset() + count;

      if (this.factory != null) {
         return factory.createMediumReference(advancedMediumOffset);
      }

      return new StandardMediumReference(getMedium(), advancedMediumOffset);
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMediumReference#before(com.github.jmeta.library.media.api.type.IMediumReference)
    */
   @Override
   public boolean before(IMediumReference other) {

      Reject.ifNull(other, "other");
      Reject.ifFalse(getMedium().equals(other.getMedium()),
	   	         "getMedium().equals(other.getMedium())");

      return getAbsoluteMediumOffset() < other.getAbsoluteMediumOffset();
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMediumReference#behindOrEqual(com.github.jmeta.library.media.api.type.IMediumReference)
    */
   @Override
   public boolean behindOrEqual(IMediumReference other) {

      Reject.ifNull(other, "other");
      Reject.ifFalse(getMedium().equals(other.getMedium()),
	   	         "getMedium().equals(other.getMedium())");

      return getAbsoluteMediumOffset() >= other.getAbsoluteMediumOffset();
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMediumReference#distanceTo(com.github.jmeta.library.media.api.type.IMediumReference)
    */
   @Override
   public long distanceTo(IMediumReference other) {

      Reject.ifNull(other, "other");
      Reject.ifFalse(getMedium().equals(other.getMedium()),
	   	         "getMedium().equals(other.getMedium())");

      return getAbsoluteMediumOffset() - other.getAbsoluteMediumOffset();
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMediumReference#getAbsoluteMediumOffset()
    */
   @Override
   public long getAbsoluteMediumOffset() {

      return absoluteMediumOffset;
   }

   /**
    * @see com.github.jmeta.library.media.api.type.IMediumReference#getMedium()
    */
   @Override
   public IMedium<?> getMedium() {

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
      IMediumReference other = (IMediumReference) obj;
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
    * Sets the {@link MediumReferenceFactory} this {@link IMediumReference} belongs to. Whenever the
    * {@link #advance(long)} method is called, the advanced {@link IMediumReference} instance is added to this
    * {@link MediumReferenceFactory}.
    * 
    * @param factory
    *           The {@link MediumReferenceFactory} to add this {@link IMediumReference} to.
    */
   void setMediumReferenceRepository(MediumReferenceFactory factory) {

      Reject.ifNull(factory, "factory");

      this.factory = factory;
   }

   /**
    * Sets a new absolute medium offset for this {@link StandardMediumReference}.
    * 
    * @param absoluteMediumOffset
    *           The absolute offset in the {@link IMedium}, relative to its starting point which is offset 0. Must not
    *           be smaller than 0.
    */
   void setAbsoluteMediumOffset(long absoluteMediumOffset) {

	   Reject.ifNegative(absoluteMediumOffset, "absoluteMediumOffset");

      this.absoluteMediumOffset = absoluteMediumOffset;
   }
}
