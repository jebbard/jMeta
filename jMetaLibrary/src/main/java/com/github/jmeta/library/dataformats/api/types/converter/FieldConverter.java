/**
 *
 * {@link IConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package com.github.jmeta.library.dataformats.api.types.converter;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link FieldConverter}
 *
 * @param <T>
 */
public interface FieldConverter<T> {

	/**
	 * @param interpretedValue
	 * @param desc
	 * @param byteOrder
	 * @param characterEncoding
	 * @return the binary value
	 * @throws InterpretedValueConversionException
	 */
	ByteBuffer toBinary(T interpretedValue, DataBlockDescription desc, ByteOrder byteOrder, Charset characterEncoding)
		throws InterpretedValueConversionException;

	/**
	 * @param binaryValue
	 * @param desc
	 * @param byteOrder
	 * @param characterEncoding
	 * @return the interpreted value
	 * @throws BinaryValueConversionException
	 */
	T toInterpreted(ByteBuffer binaryValue, DataBlockDescription desc, ByteOrder byteOrder, Charset characterEncoding)
		throws BinaryValueConversionException;
}
