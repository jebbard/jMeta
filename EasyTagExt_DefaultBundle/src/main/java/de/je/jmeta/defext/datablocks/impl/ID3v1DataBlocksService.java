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
import de.je.jmeta.defext.dataformats.DefaultExtensionsDataFormat;

/**
 * {@link ID3v1DataBlocksService}
 *
 */
public class ID3v1DataBlocksService extends AbstractDataService {

   /**
    * Creates a new {@link ID3v1DataBlocksService}.
    */
   public ID3v1DataBlocksService() {
      super(DefaultExtensionsDataFormat.ID3v1);
   }
}
