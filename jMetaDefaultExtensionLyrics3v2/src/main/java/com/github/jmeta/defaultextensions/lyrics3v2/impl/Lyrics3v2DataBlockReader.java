/**
 *
 * {@link Lyrics3v2DataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 15.04.2018
 *
 */
package com.github.jmeta.defaultextensions.lyrics3v2.impl;

import java.nio.ByteBuffer;

import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.AbstractDataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link Lyrics3v2DataBlockReader}
 *
 */
public class Lyrics3v2DataBlockReader extends AbstractDataBlockReader {

   /**
    * Creates a new {@link Lyrics3v2DataBlockReader}.
    *
    * @param spec
    */
   public Lyrics3v2DataBlockReader(DataFormatSpecification spec) {
      super(spec);
   }

   @Override
   public boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, boolean forwardRead) {

      if (parent == null) {
         return super.hasContainerWithId(reference, id, parent, remainingDirectParentByteCount, forwardRead);
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
