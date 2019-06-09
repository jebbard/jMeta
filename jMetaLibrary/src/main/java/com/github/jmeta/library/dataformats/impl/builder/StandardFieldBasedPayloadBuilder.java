/**
 *
 * {@link StandardFieldBasedPayloadContainerBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFieldBasedPayloadBuilder} allows to build field-based payload
 * descriptions.
 *
 * @param <P> The parent type of this builder
 */
public class StandardFieldBasedPayloadBuilder<P extends ContainerSequenceBuilder<P>>
	extends AbstractFieldSequenceBuilder<ContainerBuilder<P, FieldBasedPayloadBuilder<P>>, FieldBasedPayloadBuilder<P>>
	implements FieldBasedPayloadBuilder<P> {

	/**
	 * Creates a new {@link StandardFieldBasedPayloadBuilder}.
	 * 
	 * @param parentBuilder The parent {@link DataFormatBuilder}. Required for
	 *                      allowing a fluent API, as it is returned by the
	 *                      {@link #finish()} method. Must not be null.
	 * @param localId       The local id of the data block. Must not be null and
	 *                      must not contain the
	 *                      {@link DataBlockId#SEGMENT_SEPARATOR}.
	 * @param name          The human-readable name of the data block in its
	 *                      specification
	 * @param description   The description of the data block from its specification
	 * @param isGeneric     true if it is a generic data block, false otherwise
	 */
	public StandardFieldBasedPayloadBuilder(ContainerBuilder<P, FieldBasedPayloadBuilder<P>> parentBuilder,
		String localId, String name, String description, boolean isGeneric) {
		super(parentBuilder, localId, name, description, PhysicalDataBlockType.FIELD_BASED_PAYLOAD, isGeneric);
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder#finishFieldBasedPayload()
	 */
	@Override
	public ContainerBuilder<P, FieldBasedPayloadBuilder<P>> finishFieldBasedPayload() {
		return finish();
	}
}
