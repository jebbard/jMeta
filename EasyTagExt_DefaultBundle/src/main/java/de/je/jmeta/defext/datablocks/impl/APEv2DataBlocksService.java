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
 * {@link APEv2DataBlocksService}
 *
 */
public class APEv2DataBlocksService extends AbstractDataService {

   /**
    * Creates a new {@link APEv2DataBlocksService}.
    */
   public APEv2DataBlocksService() {
      super(DefaultExtensionsDataFormat.APEv2);
   }
}
