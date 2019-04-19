/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.defaultextensions.id3v1.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.impl.ForwardDataBlockReader;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.media.api.services.MediumStore;

/**
 * {@link ID3v1DataBlocksService}
 *
 */
public class ID3v1DataBlocksService extends AbstractDataBlockService {

   /**
    * Creates a new {@link ID3v1DataBlocksService}.
    */
   public ID3v1DataBlocksService() {
      super(ID3v1Extension.ID3v1);
   }

   @Override
   public DataBlockReader createBackwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore,
      DataBlockEventBus eventBus) {
      return new ID3v1BackwardDataBlockReader(spec, new ForwardDataBlockReader(spec, mediumStore, eventBus),
         mediumStore, eventBus);
   }

}
