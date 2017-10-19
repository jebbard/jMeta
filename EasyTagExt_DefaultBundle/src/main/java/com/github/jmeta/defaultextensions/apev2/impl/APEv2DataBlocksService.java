/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.defaultextensions.apev2.impl;

import com.github.jmeta.defaultextensions.id3v1.impl.ID3v1DataBlocksService;
import com.github.jmeta.library.datablocks.api.services.AbstractDataService;

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
