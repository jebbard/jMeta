/**
 *
 * {@link IMediumReference}.java
 *
 * @author Jens
 *
 * @date 17.05.2015
 *
 */

package com.github.jmeta.library.media.api.types;

/**
 * {@link IMediumReference} refers to a specific offset on a {@link IMedium}.
 * 
 * It refers to a position of byte N on the {@link IMedium}, N >= 0, and therefore gives a globally unique address of
 * each medium byte. {@link IMediumReference} referring to the same {@link IMedium} can be compared as to which position
 * is before, equal or behind another one.
 */
public interface IMediumReference {

   /**
    * Creates a new {@link IMediumReference} from this {@link IMediumReference} that points to N bytes behind the
    * position of this. This {@link IMediumReference} remains unchanged.
    * 
    * @param count
    *           The number of bytes to advance this {@link IMediumReference} to. May be a positive or a negative amount
    *           of bytes. The method will throw an exception if in total the absolute offset of the advanced
    *           {@link IMediumReference} gets negative.
    * 
    * @return A new {@link IMediumReference} advanced to have medium position this.{@link #getAbsoluteMediumOffset()} +
    *         count.
    * 
    * @pre {@link #getAbsoluteMediumOffset()} >= -count
    */
   public IMediumReference advance(long count);

   /**
    * Checks whether this {@link IMediumReference} is referring to a position before the given other
    * {@link IMediumReference}. This only works if both {@link IMediumReference}s refer to the same {@link IMedium}.
    * 
    * @param other
    *           The other {@link IMediumReference} to compare with. Must refer to the same {@link IMedium}.
    * @return true if this {@link IMediumReference} refers to a position before the given other {@link IMediumReference}
    *         , false otherwise.
    * 
    * @pre other.{@link #getMedium()}.equals({@link #getMedium()})
    */
   public boolean before(IMediumReference other);

   /**
    * Checks whether this {@link IMediumReference} is referring to a position behind the given other
    * {@link IMediumReference} or to the same position. This only works if both {@link IMediumReference}s refer to the
    * same {@link IMedium}.
    * 
    * It is guaranteed that this method returns true iff {@link #equals(Object)} returns true.
    * 
    * @param other
    *           The other {@link IMediumReference} to compare with. Must refer to the same {@link IMedium}.
    * @return true if this {@link IMediumReference} refers to a position on the same {@link IMedium} that is behind or
    *         equal to the position referred to by the other {@link IMediumReference}, false otherwise.
    * 
    * @pre other.{@link #getMedium()}.equals({@link #getMedium()})
    */
   public boolean behindOrEqual(IMediumReference other);

   /**
    * Calculates the distance from this {@link IMediumReference} to the given other {@link IMediumReference}, by
    * subtracting the absolute medium offset of this from the other {@link IMediumReference}'s offset, i.e. if
    * other.before(this), then the calculated difference will be positive, while in case of this.before(other), the
    * calculated difference will be negative.
    * 
    * This only works if both {@link IMediumReference}s refer to the same {@link IMedium}.
    * 
    * @param other
    *           The other {@link IMediumReference} to measure the distance to. Must refer to the same {@link IMedium}.
    * @return the number of bytes between this {@link IMediumReference} and the other {@link IMediumReference}.
    * 
    * @pre other.{@link #getMedium()}.equals({@link #getMedium()})
    */
   public long distanceTo(IMediumReference other);

   /**
    * Returns the absolute offset in the the {@link IMedium} this {@link IMediumReference} refers to.
    * 
    * @return the absolute offset in the the {@link IMedium} this {@link IMediumReference} refers to.
    */
   public long getAbsoluteMediumOffset();

   /**
    * Returns the {@link IMedium} this {@link IMediumReference} refers to.
    * 
    * @return the {@link IMedium} this {@link IMediumReference} refers to.
    */
   public IMedium<?> getMedium();
}