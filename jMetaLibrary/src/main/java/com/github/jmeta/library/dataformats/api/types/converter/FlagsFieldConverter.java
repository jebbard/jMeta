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
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils;

/**
 * {@link FlagsFieldConverter}
 *
 */
public class FlagsFieldConverter extends AbstractBaseFieldConverter<Flags> {

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.converter.AbstractBaseFieldConverter#convertBinaryToInterpreted(java.nio.ByteBuffer,
	 *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription,
	 *      java.nio.ByteOrder, java.nio.charset.Charset)
	 */
	@Override
	protected Flags convertBinaryToInterpreted(ByteBuffer binaryValue, DataBlockDescription desc, ByteOrder byteOrder,
		Charset characterEncoding) throws BinaryValueConversionException {

		int staticFlagLength = desc.getFieldProperties().getFlagSpecification().getByteLength();
		if (binaryValue.remaining() > staticFlagLength) {
			throw new BinaryValueConversionException(
				"Flags fields may not be longer than " + staticFlagLength + " bytes.", null, desc, binaryValue,
				byteOrder, characterEncoding);
		}

		FlagSpecification flagSpec = desc.getFieldProperties().getFlagSpecification();

		final Flags flags = new Flags(flagSpec);

		byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(binaryValue);

		flags.fromArray(copiedBytes);

		return flags;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.types.converter.AbstractBaseFieldConverter#convertInterpretedToBinary(java.lang.Object,
	 *      com.github.jmeta.library.dataformats.api.types.DataBlockDescription,
	 *      java.nio.ByteOrder, java.nio.charset.Charset)
	 */
	@Override
	protected ByteBuffer convertInterpretedToBinary(Flags interpretedValue, DataBlockDescription desc,
		ByteOrder byteOrder, Charset characterEncoding) throws InterpretedValueConversionException {

		return ByteBuffer.wrap(interpretedValue.asArray());
	}
}
