/**
 *
 * {@link ID3v1DataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 20.04.2018
 *
 */
package com.github.jmeta.defaultextensions.id3v1.impl;

import java.nio.ByteBuffer;

import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.AbstractDataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link ID3v1DataBlockReader}
 *
 */
public class ID3v1DataBlockReader extends AbstractDataBlockReader {

   /**
    * Creates a new {@link ID3v1DataBlockReader}.
    *
    * @param spec
    */
   public ID3v1DataBlockReader(DataFormatSpecification spec) {
      super(spec);
   }

   @Override
   public Container readContainerWithIdBackwards(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, ContainerContext containerContext, int sequenceNumber) {
      return readContainerWithId(reference.advance(-ID3v1Extension.ID3V1_TAG_LENGTH), id, parent,
         remainingDirectParentByteCount, containerContext, sequenceNumber);
   }

   @Override
   public boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, boolean forwardRead) {

      if (forwardRead) {
         return super.hasContainerWithId(reference, id, parent, remainingDirectParentByteCount, forwardRead);
      } else {
         if (reference.getAbsoluteMediumOffset() - ID3v1Extension.ID3V1_TAG_LENGTH < 0) {
            return false;
         }

         MagicKey id3v1TagMagicKey = getSpecification().getDataBlockDescription(id).getHeaderMagicKeys().get(0);

         int magicKeySizeInBytes = id3v1TagMagicKey.getByteLength();

         MediumOffset magicKeyReference = reference.advance(-ID3v1Extension.ID3V1_TAG_LENGTH);

         final ByteBuffer readBytes = readBytes(magicKeyReference, magicKeySizeInBytes);

         return id3v1TagMagicKey.isPresentIn(readBytes);
      }
   }

}
