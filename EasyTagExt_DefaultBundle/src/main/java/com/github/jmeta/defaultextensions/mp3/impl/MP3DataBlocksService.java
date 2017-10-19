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

import com.github.jmeta.defaultextensions.id3v1.impl.ID3v1DataBlocksService;
import com.github.jmeta.library.datablocks.api.services.AbstractDataService;
import com.github.jmeta.library.datablocks.api.services.IDataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ITransformationHandler;
import com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.type.DataTransformationType;

import de.je.jmeta.defext.dataformats.DefaultExtensionsDataFormat;

/**
 * {@link MP3DataBlocksService}
 *
 */
public class MP3DataBlocksService extends AbstractDataService {

   /**
    * Creates a new {@link MP3DataBlocksService}.
    */
   public MP3DataBlocksService() {
      super(DefaultExtensionsDataFormat.MP3);
   }

   @Override
   public IDataBlockReader getDataBlockReader(IDataFormatSpecification spec, int lazyFieldSize) {

      return new MP3DataBlockReader(spec, new HashMap<DataTransformationType, ITransformationHandler>(), lazyFieldSize);
   }
}
