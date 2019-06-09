/**
 *
 * {@link DynamicOccurrenceBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 10.06.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link DynamicOccurrenceBuilder} allows to specify the number of occurrences
 * of a data block
 */
public interface DynamicOccurrenceBuilder<C extends DataBlockDescriptionBuilder<C>> {

	/**
	 * Convenience method marking this data block as optional by setting its minimum
	 * occurrences to 0 and its maximum occurrences to 1.
	 * 
	 * @return The concrete builder instance
	 */
	C asOptional();

	/**
	 * Assigns a number of occurrences to the data block. If this method is not
	 * called, the default occurrences are
	 * {@link DataBlockDescription#getMinimumOccurrences()} = 1 and
	 * {@link DataBlockDescription#getMaximumOccurrences()} = 1.
	 * 
	 * @param minimumOccurrences The minimum number of occurrences, must be 0 or
	 *                           strictly positive and smaller than or equal to the
	 *                           maximum number of occurrences
	 * @param maximumOccurrences The maximum number of occurrences, must be strictly
	 *                           positive and bigger than or equal to the minimum
	 *                           number of occurrences
	 * @return The concrete builder instance
	 */
	C withOccurrences(long minimumOccurrences, long maximumOccurrences);

}
