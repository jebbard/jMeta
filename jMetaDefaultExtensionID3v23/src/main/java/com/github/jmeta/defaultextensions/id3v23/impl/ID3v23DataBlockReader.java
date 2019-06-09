/**
 *
 * {@link ID3v23DataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 11.03.2018
 *
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.ForwardDataBlockReader;
import com.github.jmeta.library.datablocks.impl.MediumDataProvider;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ID3v23DataBlockReader}
 *
 */
public class ID3v23DataBlockReader extends ForwardDataBlockReader {

	private Map<ID3v2TransformationType, AbstractID3v2TransformationHandler> transformationsReadOrder = new LinkedHashMap<>();

	/**
	 * Creates a new {@link ID3v23DataBlockReader}.
	 *
	 * @param spec
	 * @param mediumStore TODO
	 */
	public ID3v23DataBlockReader(DataFormatSpecification spec, MediumStore mediumStore, DataBlockEventBus eventBus) {
		super(spec, mediumStore, eventBus);

		setCustomSizeProvider(new ID3v23ExtHeaderSizeProvider());

		transformationsReadOrder.put(ID3v2TransformationType.UNSYNCHRONIZATION,
			new UnsynchronisationHandler(getDataBlockFactory()));
		transformationsReadOrder.put(ID3v2TransformationType.COMPRESSION,
			new CompressionHandler(getDataBlockFactory()));
	}

	private Container applyTransformationsAfterRead(Container container, DataBlockReader reader) {
		Container transformedContainer = container;

		Iterator<AbstractID3v2TransformationHandler> handlerIterator = transformationsReadOrder.values().iterator();
		MediumDataProvider mediumDataProvider = getMediumDataProvider();

		while (handlerIterator.hasNext()) {
			AbstractID3v2TransformationHandler transformationHandler = handlerIterator.next();

			if (transformationHandler.requiresUntransform(transformedContainer)) {
				mediumDataProvider.bufferBeforeRead(transformedContainer.getOffset(), transformedContainer.getSize());
				transformedContainer = transformationHandler.untransform(transformedContainer, reader);
			}
		}

		return transformedContainer;
	}

	public Map<ID3v2TransformationType, AbstractID3v2TransformationHandler> getTransformationHandlers() {

		return Collections.unmodifiableMap(transformationsReadOrder);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.impl.ForwardDataBlockReader#readContainerWithId(com.github.jmeta.library.media.api.types.MediumOffset,
	 *      com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      com.github.jmeta.library.datablocks.api.types.Payload, long, int,
	 *      com.github.jmeta.library.datablocks.api.types.ContainerContext)
	 */
	@Override
	public Container readContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
		long remainingDirectParentByteCount, int sequenceNumber, ContainerContext containerContext) {
		Container container = super.readContainerWithId(reference, id, parent, remainingDirectParentByteCount,
			sequenceNumber, containerContext);

		return applyTransformationsAfterRead(container, this);
	}

	public void removeEncryptionHandler() {
		transformationsReadOrder.remove(ID3v2TransformationType.ENCRYPTION);
	}

	public void setEncryptionHandler(AbstractID3v2TransformationHandler handler) {
		Reject.ifNull(handler, "handler");

		transformationsReadOrder.put(ID3v2TransformationType.ENCRYPTION, handler);
	}
}
