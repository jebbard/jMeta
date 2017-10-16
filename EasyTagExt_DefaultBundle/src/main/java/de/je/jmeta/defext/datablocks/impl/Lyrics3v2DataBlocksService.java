/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package de.je.jmeta.defext.datablocks.impl;

import de.je.jmeta.datablocks.export.AbstractDataService;
import de.je.jmeta.datablocks.export.IExtendedDataBlockFactory;
import de.je.jmeta.defext.datablocks.impl.lyrics3v2.Lyrics3v2DataBlockFactory;
import de.je.jmeta.defext.dataformats.DefaultExtensionsDataFormat;

/**
 * {@link Lyrics3v2DataBlocksService}
 *
 */
public class Lyrics3v2DataBlocksService extends AbstractDataService {

   /**
    * Creates a new {@link Lyrics3v2DataBlocksService}.
    */
   public Lyrics3v2DataBlocksService() {
      super(DefaultExtensionsDataFormat.LYRICS3v2);
   }

   @Override
   public IExtendedDataBlockFactory getDataBlockFactory() {

      return new Lyrics3v2DataBlockFactory();
   }
}
