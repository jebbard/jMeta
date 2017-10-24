/**
 *
 * {@link MediumReference}.java
 *
 * @author Jens
 *
 * @date 17.05.2015
 *
 */

package com.github.jmeta.library.media.api.types;

/**
 * {@link MediumReference} refers to a specific offset on a {@link Medium}.
 * 
 * It refers to a position of byte N on the {@link Medium}, N >= 0, and therefore gives a globally unique address of
 * each medium byte. {@link MediumReference} referring to the same {@link Medium} can be compared as to which position
 * is before, equal or behind another one.
 */
public interface MediumReference {

   /**
    * Creates a new {@link MediumReference} from this {@link MediumReference} that points to N bytes behind the
    * position of this. This {@link MediumReference} remains unchanged.
    * 
    * @param count
    *           The number of bytes to advance this {@link MediumReference} to. May be a positive or a negative amount
    *           of bytes. The method will throw an exception if in total the absolute offset of the advanced
    *           {@link MediumReference} gets negative.
    * 
    * @return A new {@link MediumReference} advanced to have medium position this.{@link #getAbsoluteMediumOffset()} +
    *         count.
    * 
    * @pre {@link #getAbsoluteMediumOffset()} >= -count
    */
   public MediumReference advance(long count);

   /**
    * Checks whether this {@link MediumReference} is referring to a position before the given other
    * {@link MediumReference}. This only works if both {@link MediumReference}s refer to the same {@link Medium}.
    * 
    * @param other
    *           The other {@link MediumReference} to compare with. Must refer to the same {@link Medium}.
    * @return true if this {@link MediumReference} refers to a position before the given other {@link MediumReference}
    *         , false otherwise.
    * 
    * @pre other.{@link #getMedium()}.equals({@link #getMedium()})
    */
   public boolean before(MediumReference other);

   /**
    * Checks whether this {@link MediumReference} is referring to a position behind the given other
    * {@link MediumReference} or to the same position. This only works if both {@link MediumReference}s refer to the
    * same {@link Medium}.
    * 
    * It is guaranteed that this method returns true iff {@link #equals(Object)} returns true.
    * 
    * @param other
    *           The other {@link MediumReference} to compare with. Must refer to the same {@link Medium}.
    * @return true if this {@link MediumReference} refers to a position on the same {@link Medium} that is behind or
    *         equal to the position referred to by the other {@link MediumReference}, false otherwise.
    * 
    * @pre other.{@link #getMedium()}.equals({@link #getMedium()})
    */
   public boolean behindOrEqual(MediumReference other);

   /**
    * Calculates the distance from this {@link MediumReference} to the given other {@link MediumReference}, by
    * subtracting the absolute medium offset of this from the other {@link MediumReference}'s offset, i.e. if
    * other.before(this), then the calculated difference will be positive, while in case of this.before(other), the
    * calculated difference will be negative.
    * 
    * This only works if both {@link MediumReference}s refer to the same {@link Medium}.
    * 
    * @param other
    *           The other {@link MediumReference} to measure the distance to. Must refer to the same {@link Medium}.
    * @return the number of bytes between this {@link MediumReference} and the other {@link MediumReference}.
    * 
    * @pre other.{@link #getMedium()}.equals({@link #getMedium()})
    */
   public long distanceTo(MediumReference other);

   /**
    * Returns the absolute offset in the the {@link Medium} this {@link MediumReference} refers to.
    * 
    * @return the absolute offset in the the {@link Medium} this {@link MediumReference} refers to.
    */
   public long getAbsoluteMediumOffset();

   /**
    * Returns the {@link Medium} this {@link MediumReference} refers to.
    * 
    * @return the {@link Medium} this {@link MediumReference} refers to.
    */
   public Medium<?> getMedium();
}