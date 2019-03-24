package com.github.jmeta.defaultextensions.id3v23.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;

/**
 * {@link ID3v23DataBlocksService}
 *
 */
public class ID3v23DataBlocksService extends AbstractDataBlockService {

   /**
    * @see com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService#getForwardDataBlockReader(com.github.jmeta.library.dataformats.api.services.DataFormatSpecification)
    */
   @Override
   public DataBlockReader getForwardDataBlockReader(DataFormatSpecification spec) {
      return new ID3v23DataBlockReader(spec);
   }

   /**
    * Creates a new {@link ID3v23DataBlocksService}.
    */
   public ID3v23DataBlocksService() {
      super(ID3v23Extension.ID3v23);
   }
}
