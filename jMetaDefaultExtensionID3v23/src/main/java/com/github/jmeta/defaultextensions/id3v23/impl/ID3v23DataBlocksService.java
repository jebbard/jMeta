package com.github.jmeta.defaultextensions.id3v23.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;

/**
 * {@link ID3v23DataBlocksService}
 *
 */
public class ID3v23DataBlocksService extends AbstractDataBlockService {

   /**
    * @see com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService#getDataBlockReader(com.github.jmeta.library.dataformats.api.services.DataFormatSpecification,
    *      int)
    */
   @Override
   public DataBlockReader getDataBlockReader(DataFormatSpecification spec, int lazyFieldSize) {
      return new ID3v23DataBlockReader(spec, lazyFieldSize);
   }

   /**
    * Creates a new {@link ID3v23DataBlocksService}.
    */
   public ID3v23DataBlocksService() {
      super(ID3v23Extension.ID3v23);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService#getDataBlockFactory()
    */
   @Override
   public ExtendedDataBlockFactory getDataBlockFactory() {

      return new ID3v23DataBlockFactory();
   }
}
