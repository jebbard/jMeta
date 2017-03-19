/**
 *
 * {@link ID3v1DataBlocksExtension}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package de.je.jmeta.defext.datablocks.impl;

import de.je.jmeta.datablocks.export.AbstractDataBlocksExtension;
import de.je.jmeta.defext.dataformats.DefaultExtensionsDataFormat;

/**
 * {@link ID3v1DataBlocksExtension}
 *
 */
public class ID3v1DataBlocksExtension extends AbstractDataBlocksExtension {

   /**
    * Creates a new {@link ID3v1DataBlocksExtension}.
    */
   public ID3v1DataBlocksExtension() {
      super(DefaultExtensionsDataFormat.ID3v1);
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.ID3v1DataBlocksExtension";
   }
}
