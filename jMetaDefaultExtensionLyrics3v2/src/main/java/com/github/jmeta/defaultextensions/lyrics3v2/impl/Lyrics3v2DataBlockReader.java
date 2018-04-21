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
import com.github.jmeta.library.datablocks.impl.StandardDataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link Lyrics3v2DataBlockReader}
 *
 */
public class Lyrics3v2DataBlockReader extends StandardDataBlockReader {

   /**
    * Creates a new {@link Lyrics3v2DataBlockReader}.
    * 
    * @param spec
    * @param maxFieldBlockSize
    */
   public Lyrics3v2DataBlockReader(DataFormatSpecification spec, int maxFieldBlockSize) {
      super(spec, maxFieldBlockSize);
   }

   @Override
   public boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, boolean forwardRead) {

      if (parent == null) {
         return super.hasContainerWithId(reference, id, parent, remainingDirectParentByteCount, forwardRead);
      }

      final ByteBuffer readBytes = readBytes(reference.advance(Lyrics3v2Extension.FOOTER_SIZE_FIELD_LENGTH),
         Lyrics3v2Extension.lyrics3v2FooterMagicKey.getByteLength());

      if (Lyrics3v2Extension.lyrics3v2FooterMagicKey.isContainerPresent(readBytes)) {
         return false;
      }

      return true;
   }

}
