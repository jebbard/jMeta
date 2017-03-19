/**
 *
 * {@link ID3v1DataBlocksExtension}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package de.je.jmeta.defext.datablocks.impl;

import java.util.HashMap;

import de.je.jmeta.datablocks.ITransformationHandler;
import de.je.jmeta.datablocks.export.AbstractDataBlocksExtension;
import de.je.jmeta.datablocks.export.IDataBlockReader;
import de.je.jmeta.dataformats.DataTransformationType;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.defext.datablocks.impl.ogg.OggDataBlockReader;
import de.je.jmeta.defext.dataformats.DefaultExtensionsDataFormat;

/**
 * {@link OggDataBlocksExtension}
 *
 */
public class OggDataBlocksExtension extends AbstractDataBlocksExtension {

   /**
    * Creates a new {@link OggDataBlocksExtension}.
    */
   public OggDataBlocksExtension() {
      super(DefaultExtensionsDataFormat.OGG);
   }

   @Override
   public IDataBlockReader getDataBlockReader(IDataFormatSpecification spec,
      int lazyFieldSize) {

      return new OggDataBlockReader(spec,
         new HashMap<DataTransformationType, ITransformationHandler>(),
         lazyFieldSize);
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.OggDataBlocksExtension";
   }
}
