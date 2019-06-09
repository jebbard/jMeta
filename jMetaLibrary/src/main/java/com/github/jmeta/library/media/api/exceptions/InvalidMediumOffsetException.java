/**
 *
 * {@link InvalidMediumOffsetException}.java
 *
 * @author Jens Ebert
 *
 * @date 18.10.2017
 *
 */
package com.github.jmeta.library.media.api.exceptions;

import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.errors.api.services.JMetaRuntimeException;

/**
 * {@link InvalidMediumOffsetException} is thrown whenever an
 * {@link MediumOffset} used with an {@link MediumStore} is considered invalid
 * due to various reasons. Note that this exception is not thrown if an
 * {@link MediumOffset} passed does not refer to the same {@link Medium} as the
 * {@link MediumStore}. Instead, a {@link PreconditionUnfullfilledException} is
 * thrown in that case.
 *
 */
public class InvalidMediumOffsetException extends JMetaRuntimeException {

	private static final long serialVersionUID = 1517944582617717936L;

	/**
	 * Creates a new {@link InvalidMediumOffsetException}.
	 * 
	 * @param offset the invalid {@link MediumOffset}
	 * @param reason the reason for it being invalid
	 */
	public InvalidMediumOffsetException(MediumOffset offset, String reason) {
		super("The given medium offset <" + offset + "> is invalid, because: " + reason, null);
	}
}
