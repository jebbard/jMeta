/**
 * {@link UnknownDataFormatException}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.exceptions;

import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * Thrown whenever the {@link ContainerDataFormat} of a top-level
 * {@link DataBlock} within a {@link AbstractMedium} is unknown. I.e. whenever a
 * {@link AbstractMedium} contains at least one top-level data block whose
 * {@link ContainerDataFormat} could not be identified, this exception is
 * thrown.
 */
public class UnknownDataFormatException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	private final MediumOffset m_mediumReference;

	/**
	 * Creates a new {@link UnknownDataFormatException}.
	 * 
	 * @param reference
	 * @param message
	 */
	public UnknownDataFormatException(MediumOffset reference, String message) {
		super(message);

		m_mediumReference = reference;
	}

	/**
	 * Returns medium
	 *
	 * @return medium
	 */
	public MediumOffset getMediumReference() {

		return m_mediumReference;
	}
}
