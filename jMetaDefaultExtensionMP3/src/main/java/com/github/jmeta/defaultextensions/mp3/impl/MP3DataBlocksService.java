/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.defaultextensions.mp3.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.impl.AbstractDataBlockReader;
import com.github.jmeta.library.datablocks.impl.ForwardDataBlockReader;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.media.api.services.MediumStore;

/**
 * {@link MP3DataBlocksService}
 *
 */
public class MP3DataBlocksService extends AbstractDataBlockService {

	/**
	 * Creates a new {@link MP3DataBlocksService}.
	 */
	public MP3DataBlocksService() {
		super(MP3Extension.MP3);
	}

	@Override
	public DataBlockReader createForwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore,
		DataBlockEventBus eventBus) {
		AbstractDataBlockReader mp3DataBlockReader = new ForwardDataBlockReader(spec, mediumStore, eventBus);

		mp3DataBlockReader.setCustomSizeProvider(new MP3SizeProvider());

		return mp3DataBlockReader;
	}
}
