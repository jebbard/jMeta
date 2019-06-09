/**
 *
 * {@link TestDataCreationException}.java
 *
 * @author Jens Ebert
 *
 * @date 26.04.2009
 *
 */
package com.github.jmeta.utility.testsetup.api.exceptions;

/**
 * {@link InvalidTestDataException} is thrown whenever test data to create
 * before a test run could not be set up due to errors or is invalid.
 */
public class InvalidTestDataException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	/**
	 * Creates this exception.
	 *
	 * @param message The message to pass.
	 * @param cause   The causing exception, if any. May be null to indicate there
	 *                is no causing exception.
	 */
	public InvalidTestDataException(String message, Throwable cause) {
		super(message, cause);
	}
}
