/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.defaultextensions.ogg.impl;

import java.util.HashMap;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.TransformationHandler;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;

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

      return new OggDataBlockReader(spec, new HashMap<DataTransformationType, TransformationHandler>(), lazyFieldSize);
   }
}
