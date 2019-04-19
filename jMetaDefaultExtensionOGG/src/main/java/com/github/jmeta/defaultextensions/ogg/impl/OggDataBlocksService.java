/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.defaultextensions.ogg.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.impl.ForwardDataBlockReader;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.media.api.services.MediumStore;

/**
 * {@link OggDataBlocksService}
 *
 */
public class OggDataBlocksService extends AbstractDataBlockService {

   /**
    * Creates a new {@link OggDataBlocksService}.
    */
   public OggDataBlocksService() {
      super(OggExtension.OGG);
   }

   @Override
   public DataBlockReader createForwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore,
      DataBlockEventBus eventBus) {

      DataBlockReader oggDataBlockReader = new ForwardDataBlockReader(spec, mediumStore, eventBus);

      OggPacketSizeAndCountProvider countAndSizeProvider = new OggPacketSizeAndCountProvider();
      oggDataBlockReader.setCustomCountProvider(countAndSizeProvider);
      oggDataBlockReader.setCustomSizeProvider(countAndSizeProvider);

      return oggDataBlockReader;
   }
}
