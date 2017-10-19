/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.defaultextensions.lyrics3v2.impl;

import com.github.jmeta.defaultextensions.id3v1.impl.ID3v1DataBlocksService;
import com.github.jmeta.library.datablocks.api.services.AbstractDataService;
import com.github.jmeta.library.datablocks.api.services.IExtendedDataBlockFactory;

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
