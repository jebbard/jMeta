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

import com.github.jmeta.defaultextensions.id3v1.impl.ID3v1DataBlocksService;
import com.github.jmeta.library.datablocks.api.services.AbstractDataService;
import com.github.jmeta.library.datablocks.api.services.IDataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ITransformationHandler;
import com.github.jmeta.library.dataformats.api.services.IDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;

/**
 * {@link OggDataBlocksService}
 *
 */
public class OggDataBlocksService extends AbstractDataService {

   /**
    * Creates a new {@link OggDataBlocksService}.
    */
   public OggDataBlocksService() {
      super(OggExtension.OGG);
   }

   @Override
   public IDataBlockReader getDataBlockReader(IDataFormatSpecification spec, int lazyFieldSize) {

      return new OggDataBlockReader(spec, new HashMap<DataTransformationType, ITransformationHandler>(), lazyFieldSize);
   }
}