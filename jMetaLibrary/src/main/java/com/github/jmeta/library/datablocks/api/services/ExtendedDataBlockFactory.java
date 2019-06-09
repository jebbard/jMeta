/**
 *
 * {@link ExtendedDataBlockFactory}.java
 *
 * @author Jens Ebert
 *
 * @date 22.04.2019
 *
 */
package com.github.jmeta.library.datablocks.api.services;

import java.nio.ByteBuffer;
import java.util.List;

import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Footer;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link ExtendedDataBlockFactory}
 *
 */
public interface ExtendedDataBlockFactory extends DataBlockFactory {

	/**
	 * Creates an {@link Container} instance.
	 *
	 * @param id               the {@link DataBlockId}.
	 * @param sequenceNumber   TODO
	 * @param parent           the parent {@link DataBlock} or null if there is not
	 *                         parent {@link DataBlock}.
	 * @param offset           the {@link AbstractMedium} of the {@link Container}.
	 * @param headers          the headers building this {@link Container}.
	 * @param payload          the {@link Payload} building this {@link Container}.
	 * @param footers          the footers building this {@link Container}.
	 * @param reader           TODO
	 * @param containerContext TODO
	 * @return the created {@link Container}.
	 */
	Container createPersistedContainer(DataBlockId id, int sequenceNumber, DataBlock parent, MediumOffset offset,
		List<Header> headers, Payload payload, List<Footer> footers, DataBlockReader reader,
		ContainerContext containerContext);

	Container createPersistedContainerWithoutChildren(DataBlockId id, int sequenceNumber, DataBlock parent,
		MediumOffset offset, DataBlockReader reader, ContainerContext containerContext);

	/**
	 * @param id
	 * @param sequenceNumber   TODO
	 * @param parent           TODO
	 * @param offset
	 * @param fieldBytes
	 * @param containerContext TODO
	 * @param reader           TODO
	 * @return the {@link Field}
	 */
	<T> Field<T> createPersistedField(DataBlockId id, int sequenceNumber, DataBlock parent, MediumOffset offset,
		ByteBuffer fieldBytes, ContainerContext containerContext, DataBlockReader reader);

	/**
	 * @param id
	 * @param reference
	 * @param fields
	 * @param sequenceNumber   TODO
	 * @param containerContext TODO
	 * @param reader           TODO
	 * @return the {@link Header}
	 */
	Footer createPersistedFooter(DataBlockId id, MediumOffset reference, List<Field<?>> fields, int sequenceNumber,
		ContainerContext containerContext, DataBlockReader reader);

	/**
	 * @param id
	 * @param reference
	 * @param fields
	 * @param sequenceNumber   TODO
	 * @param containerContext TODO
	 * @param reader           TODO
	 * @return the {@link Header}
	 */
	Header createPersistedHeader(DataBlockId id, MediumOffset reference, List<Field<?>> fields, int sequenceNumber,
		ContainerContext containerContext, DataBlockReader reader);

	/**
	 * @param id
	 * @param offset
	 * @param containerContext TODO
	 * @param totalSize
	 * @param reader
	 * @return the {@link Payload}
	 */
	Payload createPersistedPayload(DataBlockId id, MediumOffset offset, ContainerContext containerContext,
		long totalSize, DataBlockReader reader);

}
