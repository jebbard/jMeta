/**
 *
 * {@link ContainerBasedPayloadBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link ContainerSequenceBuilder} allows to build a sequence of container data
 * blocks, providing methods for adding concrete or generic containers with
 * field-based or container-based payload.
 *
 * @param <C> The concrete {@link ContainerSequenceBuilder} interface derived
 *            from this interface
 */
public interface ContainerSequenceBuilder<C extends ContainerSequenceBuilder<C>> {

	/**
	 * Adds a concrete container with container-based payload
	 * 
	 * @param localId     The local id of the new data block, must not be null, must
	 *                    not contain the character
	 *                    {@link DataBlockId#SEGMENT_SEPARATOR}
	 * @param name        The human-readable specification name of the data block or
	 *                    null
	 * @param description A description of the data block preferably taken from the
	 *                    data format specification or null
	 * @return The child builder for the new data block
	 */
	ContainerBuilder<C, ContainerBasedPayloadBuilder<C>> addContainerWithContainerBasedPayload(String localId,
		String name, String description);

	/**
	 * Adds a concrete container with field-based payload
	 * 
	 * @param localId     The local id of the new data block, must not be null, must
	 *                    not contain the character
	 *                    {@link DataBlockId#SEGMENT_SEPARATOR}
	 * @param name        The human-readable specification name of the data block or
	 *                    null
	 * @param description A description of the data block preferably taken from the
	 *                    data format specification or null
	 * @return The child builder for the new data block
	 */
	ContainerBuilder<C, FieldBasedPayloadBuilder<C>> addContainerWithFieldBasedPayload(String localId, String name,
		String description);

	/**
	 * Adds a generic container with container-based payload
	 * 
	 * @param localId     The local id of the new data block, must not be null, must
	 *                    not contain the character
	 *                    {@link DataBlockId#SEGMENT_SEPARATOR}
	 * @param name        The human-readable specification name of the data block or
	 *                    null
	 * @param description A description of the data block preferably taken from the
	 *                    data format specification or null
	 * @return The child builder for the new data block
	 */
	ContainerBuilder<C, ContainerBasedPayloadBuilder<C>> addGenericContainerWithContainerBasedPayload(String localId,
		String name, String description);

	/**
	 * Adds a generic container with field-based payload
	 * 
	 * @param localId     The local id of the new data block, must not be null, must
	 *                    not contain the character
	 *                    {@link DataBlockId#SEGMENT_SEPARATOR}
	 * @param name        The human-readable specification name of the data block or
	 *                    null
	 * @param description A description of the data block preferably taken from the
	 *                    data format specification or null
	 * @return The child builder for the new data block
	 */
	ContainerBuilder<C, FieldBasedPayloadBuilder<C>> addGenericContainerWithFieldBasedPayload(String localId,
		String name, String description);
}
