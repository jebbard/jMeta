/**
 *
 * {@link StandardStringFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link StandardBinaryFieldBuilder} allows to build binary field descriptions.
 *
 * @param <P> The parent type of this builder
 */
public class StandardBinaryFieldBuilder<P extends DataBlockDescriptionBuilder<P>>
	extends AbstractFieldBuilder<P, byte[], BinaryFieldBuilder<P>> implements BinaryFieldBuilder<P> {

	/**
	 * @see AbstractFieldBuilder#AbstractFieldBuilder(DataBlockDescriptionBuilder,
	 *      String, String, String, FieldType, boolean)
	 */
	public StandardBinaryFieldBuilder(P parentBuilder, String localId, String name, String description,
		boolean isGeneric) {
		super(parentBuilder, localId, name, description, FieldType.BINARY, isGeneric);
	}
}
