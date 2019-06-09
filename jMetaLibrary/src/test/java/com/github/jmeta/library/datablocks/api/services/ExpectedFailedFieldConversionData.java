/**
 *
 * {@link FieldConversionData}.java
 *
 * @author Jens Ebert
 *
 * @date 28.06.2011
 */
package com.github.jmeta.library.datablocks.api.services;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

/**
 * {@link ExpectedFailedFieldConversionData} is used for storing expected
 * {@link Charset} and {@link ByteOrder} for a failed field conversion.
 */
public class ExpectedFailedFieldConversionData {

	private final Charset m_characterEncoding;

	private final ByteOrder m_byteOrder;

	/**
	 * Creates a new {@link ExpectedFailedFieldConversionData}.
	 * 
	 * @param characterEncoding The expected {@link Charset}.
	 * @param byteOrder         The expected {@link ByteOrder}.
	 */
	public ExpectedFailedFieldConversionData(Charset characterEncoding, ByteOrder byteOrder) {
		m_characterEncoding = characterEncoding;
		m_byteOrder = byteOrder;
	}

	/**
	 * Returns the expected {@link ByteOrder}.
	 *
	 * @return the expected {@link ByteOrder}.
	 */
	public ByteOrder getByteOrder() {

		return m_byteOrder;
	}

	/**
	 * Returns the expected {@link Charset}.
	 * 
	 * @return the expected {@link Charset}.
	 */
	public Charset getCharacterEncoding() {

		return m_characterEncoding;
	}
}