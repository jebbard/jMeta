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

import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link BinaryFieldBuilder} allows to set properties of field data blocks with
 * {@link FieldType#BINARY}.
 *
 * @param <P> The concrete parent builder interface
 */
public interface BinaryFieldBuilder<P> extends FieldBuilder<P, byte[], BinaryFieldBuilder<P>> {
	// Intentionally empty
}
