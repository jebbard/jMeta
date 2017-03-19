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
 * {@link APEv2DataBlocksExtension}
 *
 */
public class APEv2DataBlocksExtension extends AbstractDataBlocksExtension {

   /**
    * Creates a new {@link APEv2DataBlocksExtension}.
    */
   public APEv2DataBlocksExtension() {
      super(DefaultExtensionsDataFormat.APEv2);
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.APEv2DataBlocksExtension";
   }
}
