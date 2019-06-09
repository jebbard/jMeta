package com.github.jmeta.defaultextensions.id3v23.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.media.api.services.MediumStore;

/**
 * {@link ID3v23DataBlocksService}
 *
 */
public class ID3v23DataBlocksService extends AbstractDataBlockService {

	/**
	 * Creates a new {@link ID3v23DataBlocksService}.
	 */
	public ID3v23DataBlocksService() {
		super(ID3v23Extension.ID3v23);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService#createForwardDataBlockReader(com.github.jmeta.library.dataformats.api.services.DataFormatSpecification,
	 *      MediumStore, DataBlockEventBus)
	 */
	@Override
	public DataBlockReader createForwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore,
		DataBlockEventBus eventBus) {
		return new ID3v23DataBlockReader(spec, mediumStore, eventBus);
	}
}
