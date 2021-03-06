package com.github.jmeta.library.datablocks.api.services;

import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.media.api.services.MediumStore;

/**
 * {@link DataBlockService}
 *
 */
public interface DataBlockService {

	/**
	 * @param spec
	 * @param mediumStore TODO
	 * @param eventBus    TODO
	 * @return null to indicate usage of the default {@link DataBlockReader}
	 */
	DataBlockReader createBackwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore,
		DataBlockEventBus eventBus);

	/**
	 * @param spec
	 * @param mediumStore TODO
	 * @param eventBus    TODO
	 * @return null to indicate usage of the default {@link DataBlockReader}
	 */
	DataBlockReader createForwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore,
		DataBlockEventBus eventBus);

	/**
	 * @return the {@link ContainerDataFormat}
	 */
	ContainerDataFormat getDataFormat();
}
