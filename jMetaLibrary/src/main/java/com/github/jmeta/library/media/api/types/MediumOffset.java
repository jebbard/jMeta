/**
 *
 * {@link MediumOffset}.java
 *
 * @author Jens
 *
 * @date 17.05.2015
 *
 */

package com.github.jmeta.library.media.api.types;

/**
 * {@link MediumOffset} refers to a specific offset on a {@link Medium}.
 *
 * It refers to a position of byte N on the {@link Medium}, N &gt;= 0, and
 * therefore gives a globally unique address of each medium byte.
 * {@link MediumOffset} referring to the same {@link Medium} can be compared as
 * to which position is before, equal or behind another one.
 */
public interface MediumOffset {

	/**
	 * Creates a new {@link MediumOffset} from this {@link MediumOffset} that points
	 * to N bytes behind the position of this. This {@link MediumOffset} remains
	 * unchanged.
	 * 
	 * @param count The number of bytes to advance this {@link MediumOffset} to. May
	 *              be a positive or a negative amount of bytes. The method will
	 *              throw an exception if in total the absolute offset of the
	 *              advanced {@link MediumOffset} gets negative.
	 * 
	 * @return A new {@link MediumOffset} advanced to have medium position
	 *         this.{@link #getAbsoluteMediumOffset()} + count.
	 */
	MediumOffset advance(long count);

	/**
	 * Checks whether this {@link MediumOffset} is referring to a position before
	 * the given other {@link MediumOffset}. This only works if both
	 * {@link MediumOffset}s refer to the same {@link Medium}.
	 * 
	 * @param other The other {@link MediumOffset} to compare with. Must refer to
	 *              the same {@link Medium}.
	 * @return true if this {@link MediumOffset} refers to a position before the
	 *         given other {@link MediumOffset} , false otherwise.
	 */
	boolean before(MediumOffset other);

	/**
	 * Checks whether this {@link MediumOffset} is referring to a position behind
	 * the given other {@link MediumOffset} or to the same position. This only works
	 * if both {@link MediumOffset}s refer to the same {@link Medium}.
	 * 
	 * Returns true if this is equal to other (must be ensured by any
	 * implementation).
	 * 
	 * @param other The other {@link MediumOffset} to compare with. Must refer to
	 *              the same {@link Medium}.
	 * @return true if this {@link MediumOffset} refers to a position on the same
	 *         {@link Medium} that is behind or equal to the position referred to by
	 *         the other {@link MediumOffset}, false otherwise.
	 */
	boolean behindOrEqual(MediumOffset other);

	/**
	 * Calculates the distance from this {@link MediumOffset} to the given other
	 * {@link MediumOffset}, by subtracting the absolute medium offset of this from
	 * the other {@link MediumOffset}'s offset, i.e. if other.before(this), then the
	 * calculated difference will be positive, while in case of this.before(other),
	 * the calculated difference will be negative.
	 * 
	 * This only works if both {@link MediumOffset}s refer to the same
	 * {@link Medium}.
	 * 
	 * @param other The other {@link MediumOffset} to measure the distance to. Must
	 *              refer to the same {@link Medium}.
	 * @return the number of bytes between this {@link MediumOffset} and the other
	 *         {@link MediumOffset}.
	 */
	long distanceTo(MediumOffset other);

	/**
	 * Returns the absolute offset in the the {@link Medium} this
	 * {@link MediumOffset} refers to.
	 * 
	 * @return the absolute offset in the the {@link Medium} this
	 *         {@link MediumOffset} refers to.
	 */
	long getAbsoluteMediumOffset();

	/**
	 * Returns the {@link Medium} this {@link MediumOffset} refers to.
	 * 
	 * @return the {@link Medium} this {@link MediumOffset} refers to.
	 */
	Medium<?> getMedium();
}