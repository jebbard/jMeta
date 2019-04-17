/**
 *
 * {@link ID3v1DataBlocksService}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package com.github.jmeta.defaultextensions.lyrics3v2.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockService;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.media.api.services.MediumStore;

/**
 * {@link Lyrics3v2DataBlocksService}
 *
 */
public class Lyrics3v2DataBlocksService extends AbstractDataBlockService {

   /**
    * Creates a new {@link Lyrics3v2DataBlocksService}.
    */
   public Lyrics3v2DataBlocksService() {
      super(Lyrics3v2Extension.LYRICS3v2);
   }

   @Override
   public DataBlockReader createForwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore) {
      return new Lyrics3v2ForwardDataBlockReader(spec, mediumStore);
   }
}
