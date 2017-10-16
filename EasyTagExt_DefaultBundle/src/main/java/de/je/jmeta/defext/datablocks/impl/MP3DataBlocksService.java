/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package de.je.jmeta.defext.datablocks.impl;

import java.util.HashMap;

import de.je.jmeta.datablocks.ITransformationHandler;
import de.je.jmeta.datablocks.export.AbstractDataService;
import de.je.jmeta.datablocks.export.IDataBlockReader;
import de.je.jmeta.dataformats.DataTransformationType;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.defext.datablocks.impl.mp3.MP3DataBlockReader;
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
