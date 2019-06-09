
package com.github.jmeta.library.datablocks.api.types;

import java.nio.ByteBuffer;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;

/**
 * {@link Field}
 *
 * @param <T>
 */
public interface Field<T> extends DataBlock {

	/**
	 * @return the binary value
	 * @throws InterpretedValueConversionException
	 */
	ByteBuffer getBinaryValue() throws InterpretedValueConversionException;

	/**
	 * Returns interpretedValue
	 *
	 * @return interpretedValue
	 * @throws BinaryValueConversionException
	 */
	T getInterpretedValue() throws BinaryValueConversionException;

	void setBinaryValue(ByteBuffer binaryValue);

	void setInterpretedValue(T interpretedBalue);
}