/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.defaultextensions.lyrics3v2.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;

/**
 * {@link Lyrics3v2DataBlocksService}
 *
 */
public class Lyrics3v2DataBlocksService extends AbstractDataBlockService {

   /**
    * Creates a new {@link Lyrics3v2DataBlocksService}.
    */
   public Lyrics3v2DataBlocksService() {
      super(Lyrics3v2Extension.LYRICS3v2);
   }

   @Override
   public DataBlockReader getDataBlockReader(DataFormatSpecification spec, int lazyFieldSize) {
      return new Lyrics3v2DataBlockReader(spec, lazyFieldSize);
   }

   @Override
   public ExtendedDataBlockFactory getDataBlockFactory() {

      return new Lyrics3v2DataBlockFactory();
   }
}
