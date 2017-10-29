/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.defaultextensions.mp3.impl;

import java.util.HashMap;

import com.github.jmeta.library.datablocks.api.services.AbstractDataService;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.TransformationHandler;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;

/**
 * {@link MP3DataBlocksService}
 *
 */
public class MP3DataBlocksService extends AbstractDataService {

   /**
    * Creates a new {@link MP3DataBlocksService}.
    */
   public MP3DataBlocksService() {
      super(MP3Extension.MP3);
   }

   @Override
   public DataBlockReader getDataBlockReader(DataFormatSpecification spec, int lazyFieldSize) {

      return new MP3DataBlockReader(spec, new HashMap<DataTransformationType, TransformationHandler>(), lazyFieldSize);
   }
}
