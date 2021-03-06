/**
 * {@link MediumAccessException}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:18:22 (December 9, 2010)
 */

package com.github.jmeta.library.media.api.exceptions;

import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.utility.errors.api.services.JMetaRuntimeException;

/**
 * This exception is thrown whenever an {@link Medium} could not be accessed.
 */
public class MediumAccessException extends JMetaRuntimeException {

	private static final long serialVersionUID = -1281570771668926850L;

	/**
	 * Creates a new {@link MediumAccessException}.
	 * 
	 * @param message The message.
	 * @param cause   The cause.
	 */
	public MediumAccessException(String message, Throwable cause) {
		super(message, cause);
	}
}
