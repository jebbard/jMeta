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
import de.je.jmeta.datablocks.export.IExtendedDataBlockFactory;
import de.je.jmeta.defext.datablocks.impl.lyrics3v2.Lyrics3v2DataBlockFactory;
import de.je.jmeta.defext.dataformats.DefaultExtensionsDataFormat;

/**
 * {@link Lyrics3v2DataBlocksExtension}
 *
 */
public class Lyrics3v2DataBlocksExtension extends AbstractDataBlocksExtension {

   /**
    * Creates a new {@link Lyrics3v2DataBlocksExtension}.
    */
   public Lyrics3v2DataBlocksExtension() {
      super(DefaultExtensionsDataFormat.LYRICS3v2);
   }

   @Override
   public IExtendedDataBlockFactory getDataBlockFactory() {

      return new Lyrics3v2DataBlockFactory();
   }

   /**
    * @see de.je.jmeta.extmanager.export.IExtensionPoint#getExtensionId()
    */
   @Override
   public String getExtensionId() {

      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.Lyrics3v2DataBlocksExtension";
   }
}
