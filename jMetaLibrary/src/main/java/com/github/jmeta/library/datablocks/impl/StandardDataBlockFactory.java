/**
 * {@link StandardDataBlockFactory}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.DataBlockState;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldSequence;
import com.github.jmeta.library.datablocks.api.types.Footer;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class StandardDataBlockFactory implements ExtendedDataBlockFactory {

	private final MediumDataProvider mediumDataProvider;
	private final DataFormatSpecification spec;
	private final DataBlockEventBus eventBus;

	/**
	 * Creates a new {@link StandardDataBlockFactory}.
	 *
	 * @param mediumDataProvider
	 * @param eventBus           TODO
	 */
	public StandardDataBlockFactory(MediumDataProvider mediumDataProvider, DataFormatSpecification spec,
		DataBlockEventBus eventBus) {
		super();
		this.mediumDataProvider = mediumDataProvider;
		this.spec = spec;
		this.eventBus = eventBus;
	}

	@SuppressWarnings("unchecked")
	private <T extends FieldSequence> T createHeaderOrFooter(DataBlockId id, MediumOffset reference,
		List<Field<?>> fields, int sequenceNumber, ContainerContext containerContext) {

		Reject.ifNull(id, "headerRef");
		Reject.ifNull(reference, "parent");
		Reject.ifNull(fields, "fields");

		StandardHeaderOrFooter headerOrFooter = new StandardHeaderOrFooter(id, spec);

		initDataBlock(headerOrFooter, reference, sequenceNumber, null, containerContext);
		headerOrFooter.setFields(fields);

		return (T) headerOrFooter;
	}

	@Override
	public Container createPersistedContainer(DataBlockId id, int sequenceNumber, DataBlock parent, MediumOffset offset,
		List<Header> headers, Payload payload, List<Footer> footers, DataBlockReader reader,
		ContainerContext containerContext) {

		StandardContainer container = new StandardContainer(id, spec);

		initDataBlock(container, offset, sequenceNumber, parent, containerContext);

		for (int i = 0; i < headers.size(); i++) {
			container.insertHeader(i, headers.get(i));
		}

		container.setPayload(payload);

		for (int i = 0; i < footers.size(); i++) {
			container.insertFooter(i, footers.get(i));
		}

		return container;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedContainerWithoutChildren(com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      int, com.github.jmeta.library.datablocks.api.types.DataBlock,
	 *      com.github.jmeta.library.media.api.types.MediumOffset,
	 *      com.github.jmeta.library.datablocks.api.services.DataBlockReader,
	 *      com.github.jmeta.library.datablocks.api.types.ContainerContext)
	 */
	@Override
	public Container createPersistedContainerWithoutChildren(DataBlockId id, int sequenceNumber, DataBlock parent,
		MediumOffset offset, DataBlockReader reader, ContainerContext containerContext) {

		StandardContainer container = new StandardContainer(id, spec);

		initDataBlock(container, offset, sequenceNumber, parent, containerContext);

		return container;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedField(com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      int, com.github.jmeta.library.datablocks.api.types.DataBlock,
	 *      com.github.jmeta.library.media.api.types.MediumOffset,
	 *      java.nio.ByteBuffer,
	 *      com.github.jmeta.library.datablocks.api.types.ContainerContext,
	 *      com.github.jmeta.library.datablocks.api.services.DataBlockReader)
	 */
	@Override
	public <T> Field<T> createPersistedField(DataBlockId id, int sequenceNumber, DataBlock parent, MediumOffset offset,
		ByteBuffer fieldBytes, ContainerContext containerContext, DataBlockReader reader) {

		Reject.ifNull(id, "fieldDesc");
		Reject.ifNull(offset, "reference");
		Reject.ifNegative(fieldBytes.remaining(), "fieldBytes.remaining()");

		StandardField<T> field = new StandardField<>(id, spec);

		initDataBlock(field, offset, sequenceNumber, parent, containerContext);

		field.setBinaryValue(fieldBytes);

		return field;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedFooter(com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      com.github.jmeta.library.media.api.types.MediumOffset, java.util.List,
	 *      int, com.github.jmeta.library.datablocks.api.types.ContainerContext,
	 *      com.github.jmeta.library.datablocks.api.services.DataBlockReader)
	 */
	@Override
	public Footer createPersistedFooter(DataBlockId id, MediumOffset reference, List<Field<?>> fields,
		int sequenceNumber, ContainerContext containerContext, DataBlockReader reader) {
		return createHeaderOrFooter(id, reference, fields, sequenceNumber, containerContext);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedHeader(com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      com.github.jmeta.library.media.api.types.MediumOffset, java.util.List,
	 *      int, com.github.jmeta.library.datablocks.api.types.ContainerContext,
	 *      com.github.jmeta.library.datablocks.api.services.DataBlockReader)
	 */
	@Override
	public Header createPersistedHeader(DataBlockId id, MediumOffset reference, List<Field<?>> fields,
		int sequenceNumber, ContainerContext containerContext, DataBlockReader reader) {
		return createHeaderOrFooter(id, reference, fields, sequenceNumber, containerContext);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedPayload(com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      com.github.jmeta.library.media.api.types.MediumOffset,
	 *      com.github.jmeta.library.datablocks.api.types.ContainerContext, long,
	 *      com.github.jmeta.library.datablocks.api.services.DataBlockReader)
	 */
	@Override
	public Payload createPersistedPayload(DataBlockId id, MediumOffset offset, ContainerContext containerContext,
		long totalSize, DataBlockReader reader) {

		Reject.ifNull(id, "id");
		Reject.ifNull(offset, "reference");
		Reject.ifNull(reader, "reader");

		DataFormatSpecification spec = reader.getSpecification();

		DataBlockDescription desc = spec.getDataBlockDescription(id);

		if (desc.getPhysicalType() == PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD) {
			ContainerBasedLazyPayload containerBasedLazyPayload = new ContainerBasedLazyPayload(id, spec, reader);
			initDataBlock(containerBasedLazyPayload, offset, 0, null, containerContext);
			containerBasedLazyPayload.initSize(totalSize);

			return containerBasedLazyPayload;
		} else {
			FieldBasedLazyPayload fieldBasedLazyPayload = new FieldBasedLazyPayload(id, spec, reader);
			initDataBlock(fieldBasedLazyPayload, offset, 0, null, containerContext);
			fieldBasedLazyPayload.initSize(totalSize);

			return fieldBasedLazyPayload;
		}
	}

	/**
	 * @param dataBlock
	 * @param offset
	 * @param sequenceNumber
	 * @param parent
	 * @param containerContext
	 */
	private void initDataBlock(AbstractDataBlock dataBlock, MediumOffset offset, int sequenceNumber, DataBlock parent,
		ContainerContext containerContext) {
		dataBlock.initContainerContext(containerContext);

		if (parent != null) {
			dataBlock.initParent(parent);
		}

		dataBlock.attachToMedium(offset, sequenceNumber, mediumDataProvider, eventBus, DataBlockState.PERSISTED);
	}
}
