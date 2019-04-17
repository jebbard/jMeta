/**
 *
 * {@link Lyrics3v2BackwardDataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 15.04.2018
 *
 */
package com.github.jmeta.defaultextensions.lyrics3v2.impl;

import java.nio.ByteBuffer;

import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.ForwardDataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link Lyrics3v2ForwardDataBlockReader}
 *
 */
public class Lyrics3v2ForwardDataBlockReader extends ForwardDataBlockReader {

   /**
    * Creates a new {@link Lyrics3v2ForwardDataBlockReader}.
    *
    * @param spec
    * @param mediumStore TODO
    */
   public Lyrics3v2ForwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore) {
      super(spec, mediumStore);
   }

   @Override
   public boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount) {

      if (parent == null) {
         return super.hasContainerWithId(reference, id, parent, remainingDirectParentByteCount);
      }

      MagicKey lyrics3v2FooterMagicKey = getSpecification().getDataBlockDescription(Lyrics3v2Extension.REF_TAG.getId())
         .getFooterMagicKeys().get(0);

      final ByteBuffer readBytes = readBytes(reference.advance(6), lyrics3v2FooterMagicKey.getByteLength());

      if (lyrics3v2FooterMagicKey.isPresentIn(readBytes)) {
         return false;
      }

      return true;
   }

}
