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
import de.je.jmeta.defext.datablocks.impl.ogg.OggDataBlockReader;
import de.je.jmeta.defext.dataformats.DefaultExtensionsDataFormat;

/**
 * {@link OggDataBlocksService}
 *
 */
public class OggDataBlocksService extends AbstractDataService {

   /**
    * Creates a new {@link OggDataBlocksService}.
    */
   public OggDataBlocksService() {
      super(DefaultExtensionsDataFormat.OGG);
   }

   @Override
   public IDataBlockReader getDataBlockReader(IDataFormatSpecification spec, int lazyFieldSize) {

      return new OggDataBlockReader(spec, new HashMap<DataTransformationType, ITransformationHandler>(), lazyFieldSize);
   }
}
