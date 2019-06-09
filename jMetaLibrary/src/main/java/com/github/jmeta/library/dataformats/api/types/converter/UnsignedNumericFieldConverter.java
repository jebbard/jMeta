/**
 *
 * {@link BnaryConverter}.java
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
import com.github.jmeta.utility.numericutils.api.services.NumericDataTypeUtil;

/**
 * {@link UnsignedNumericFieldConverter}
 *
 */
public class UnsignedNumericFieldConverter extends AbstractBaseFieldConverter<Long> {

	private static final int MAX_LONG_BYTE_SIZE = Long.SIZE / Byte.SIZE;

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.converter.AbstractBaseFieldConverter#convertBinaryToInterpreted(java.nio.ByteBuffer,
	 *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription,
	 *      java.nio.ByteOrder, java.nio.charset.Charset)
	 */
	@Override
	protected Long convertBinaryToInterpreted(ByteBuffer binaryValue, DataBlockDescription desc, ByteOrder byteOrder,
		Charset characterEncoding) throws BinaryValueConversionException {

		long fieldByteCount = binaryValue.remaining();

		if (fieldByteCount > UnsignedNumericFieldConverter.MAX_LONG_BYTE_SIZE) {
			throw new BinaryValueConversionException(
				"Numeric fields may not be longer than " + UnsignedNumericFieldConverter.MAX_LONG_BYTE_SIZE + " bytes.",
				null, desc, binaryValue, byteOrder, characterEncoding);
		}

		ByteBuffer copiedBuffer = binaryValue.asReadOnlyBuffer();

		copiedBuffer.order(byteOrder);

		if (fieldByteCount == 1) {
			return (long) NumericDataTypeUtil.unsignedValue(copiedBuffer.get());
		} else if (fieldByteCount == 2) {
			return (long) NumericDataTypeUtil.unsignedValue(copiedBuffer.getShort());
		} else if (fieldByteCount <= 4) {
			return (long) NumericDataTypeUtil.unsignedValue(copiedBuffer.getInt());
		} else if (fieldByteCount <= UnsignedNumericFieldConverter.MAX_LONG_BYTE_SIZE) {
			final long longValue = copiedBuffer.getLong();

			if (longValue < 0) {
				throw new BinaryValueConversionException(
					"Negative long values currently cannot be represented as unsigned. Value: " + longValue + ".", null,
					desc, copiedBuffer, byteOrder, characterEncoding);
			}

			return longValue;
		}

		return null;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.converter.AbstractBaseFieldConverter#convertInterpretedToBinary(java.lang.Object,
	 *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription,
	 *      java.nio.ByteOrder, java.nio.charset.Charset)
	 */
	@Override
	protected ByteBuffer convertInterpretedToBinary(Long interpretedValue, DataBlockDescription desc,
		ByteOrder byteOrder, Charset characterEncoding) throws InterpretedValueConversionException {

		long fieldByteCount = desc.getMaximumByteLength();

		if (fieldByteCount > UnsignedNumericFieldConverter.MAX_LONG_BYTE_SIZE) {
			throw new InterpretedValueConversionException(
				"Numeric fields may not be longer than " + UnsignedNumericFieldConverter.MAX_LONG_BYTE_SIZE + " bytes.",
				null, desc, interpretedValue, byteOrder, characterEncoding);
		}

		ByteBuffer buffer = ByteBuffer.wrap(new byte[(int) fieldByteCount]);

		if (fieldByteCount == 1) {
			buffer.put(interpretedValue.byteValue());
		} else if (fieldByteCount == 2) {
			buffer.putShort(interpretedValue.shortValue());
		} else if (fieldByteCount <= 4) {
			buffer.putInt(interpretedValue.intValue());
		} else if (fieldByteCount <= UnsignedNumericFieldConverter.MAX_LONG_BYTE_SIZE) {
			buffer.putLong(interpretedValue);
		}

		buffer.rewind();

		return buffer;
	}
}
