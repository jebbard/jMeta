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
import com.github.jmeta.library.datablocks.impl.StandardDataBlockReader;
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
   public DataBlockReader getDataBlockReader(DataFormatSpecification spec) {
      StandardDataBlockReader mp3DataBlockReader = new StandardDataBlockReader(spec);

      mp3DataBlockReader.setCustomSizeProvider(new MP3SizeProvider());

      return mp3DataBlockReader;
   }
}
