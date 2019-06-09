/**
 *
 * {@link StringFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import java.nio.ByteOrder;

import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link NumericFieldBuilder} allows to set properties of field data blocks
 * with {@link FieldType#UNSIGNED_WHOLE_NUMBER}.
 *
 * @param <P> The concrete parent builder interface
 */
public interface NumericFieldBuilder<P> extends FieldBuilder<P, Long, NumericFieldBuilder<P>> {

	/**
	 * Sets the fixed {@link ByteOrder} this field uses.
	 * 
	 * @param byteOrder The fixed {@link ByteOrder} or null
	 * @return This builder
	 */
	NumericFieldBuilder<P> withFixedByteOrder(ByteOrder byteOrder);
}
