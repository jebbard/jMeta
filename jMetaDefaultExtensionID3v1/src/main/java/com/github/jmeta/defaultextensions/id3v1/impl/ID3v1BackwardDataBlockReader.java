/**
 *
 * {@link ID3v1BackwardDataBlockReader}.java
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
import com.github.jmeta.library.datablocks.impl.BackwardDataBlockReader;
import com.github.jmeta.library.datablocks.impl.ForwardDataBlockReader;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link ID3v1BackwardDataBlockReader}
 *
 */
public class ID3v1BackwardDataBlockReader extends BackwardDataBlockReader {

   /**
    * Creates a new {@link ID3v1BackwardDataBlockReader}.
    *
    * @param spec
    * @param forwardReader
    *           TODO
    * @param mediumStore
    *           TODO
    */
   public ID3v1BackwardDataBlockReader(DataFormatSpecification spec, ForwardDataBlockReader forwardReader,
      MediumStore mediumStore, DataBlockEventBus eventBus) {
      super(spec, forwardReader, mediumStore, eventBus);
   }

   @Override
   public Container readContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, int sequenceNumber, ContainerContext containerContext) {
      return getForwardReader().readContainerWithId(reference.advance(-ID3v1Extension.ID3V1_TAG_LENGTH), id, parent,
         remainingDirectParentByteCount, sequenceNumber, containerContext);
   }

   @Override
   public boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount) {

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
