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
import com.github.jmeta.library.datablocks.impl.BackwardDataBlockReader;
import com.github.jmeta.library.datablocks.impl.ForwardDataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;

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
   public DataBlockReader getForwardDataBlockReader(DataFormatSpecification spec) {
      AbstractDataBlockReader mp3DataBlockReader = new ForwardDataBlockReader(spec);

      mp3DataBlockReader.setCustomSizeProvider(new MP3SizeProvider());

      return mp3DataBlockReader;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockService#getBackwardDataBlockReader(com.github.jmeta.library.dataformats.api.services.DataFormatSpecification)
    */
   @Override
   public DataBlockReader getBackwardDataBlockReader(DataFormatSpecification spec) {
      AbstractDataBlockReader mp3DataBlockReader = new BackwardDataBlockReader(spec);

      mp3DataBlockReader.setCustomSizeProvider(new MP3SizeProvider());

      return mp3DataBlockReader;
   }

}
