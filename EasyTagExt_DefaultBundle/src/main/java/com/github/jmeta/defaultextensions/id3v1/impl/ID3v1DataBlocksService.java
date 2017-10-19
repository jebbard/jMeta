/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.defaultextensions.id3v1.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractDataService;

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
