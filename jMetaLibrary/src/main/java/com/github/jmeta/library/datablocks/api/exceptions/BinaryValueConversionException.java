/**
 *
 * {@link BinaryValueConversionException}.java
 *
 * @author Jens Ebert
 *
 * @date 20.06.2011
 */
package com.github.jmeta.library.datablocks.api.exceptions;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link BinaryValueConversionException}
 *
 */
public class BinaryValueConversionException extends Exception {

	private static final long serialVersionUID = 1L;

	private final DataBlockDescription m_fieldDescription;

	private final ByteBuffer m_binaryValue;

	private final Charset m_characterEncoding;

	private final ByteOrder m_byteOrder;

	/**
	 * Creates a new {@link BinaryValueConversionException}.
	 *
	 * @param message
	 * @param cause
	 * @param fieldDesc
	 * @param binaryValue
	 * @param byteOrder
	 * @param characterEncoding
	 */
	public BinaryValueConversionException(String message, Throwable cause, DataBlockDescription fieldDesc,
		ByteBuffer binaryValue, ByteOrder byteOrder, Charset characterEncoding) {
		super(message, cause);

		m_binaryValue = binaryValue;
		m_characterEncoding = characterEncoding;
		m_byteOrder = byteOrder;
		m_fieldDescription = fieldDesc;
	}

	public ByteBuffer getBinaryValue() {

		return m_binaryValue;
	}

	/**
	 * @return the {@link ByteOrder}
	 */
	public ByteOrder getByteOrder() {

		return m_byteOrder;
	}

	/**
	 * @return the {@link Charset}
	 */
	public Charset getCharacterEncoding() {

		return m_characterEncoding;
	}

	/**
	 * @return the {@link DataBlockDescription}
	 */
	public DataBlockDescription getFieldDescription() {

		return m_fieldDescription;
	}
}
