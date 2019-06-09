/**
 *
 * {@link FlagSpecificationBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 21.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;

/**
 * {@link FlagSpecificationBuilder} allows to build a {@link FlagSpecification}
 * to be set for a field.
 *
 * @param <P> The parent builder interface of the {@link FlagsFieldBuilder}
 */
public interface FlagSpecificationBuilder<P> {

	/**
	 * Adds a {@link FlagDescription} to the current flag specification
	 * 
	 * @param flagDescription The {@link FlagDescription} to add, must not be null
	 * @return This builder
	 */
	FlagSpecificationBuilder<P> addFlagDescription(FlagDescription flagDescription);

	/**
	 * Finishes this builder
	 * 
	 * @return The parent builder
	 */
	FlagsFieldBuilder<P> finishFlagSpecification();

	/**
	 * Adds default flag bytes to the current flag specification
	 * 
	 * @param defaultFlagBytes The default flag bytes, must not be null
	 * @return This builder
	 */
	FlagSpecificationBuilder<P> withDefaultFlagBytes(byte[] defaultFlagBytes);
}
