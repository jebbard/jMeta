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
import com.github.jmeta.library.datablocks.impl.StandardDataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;

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
   public DataBlockReader getDataBlockReader(DataFormatSpecification spec, int lazyFieldSize) {

      DataBlockReader oggDataBlockReader = new StandardDataBlockReader(spec, lazyFieldSize);

      OggPacketSizeAndCountProvider countAndSizeProvider = new OggPacketSizeAndCountProvider();
      oggDataBlockReader.setCustomCountProvider(countAndSizeProvider);
      oggDataBlockReader.setCustomSizeProvider(countAndSizeProvider);

      return oggDataBlockReader;
   }
}
