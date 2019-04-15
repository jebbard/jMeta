/**
 *
 * {@link ForwardDataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 24.03.2019
 *
 */
package com.github.jmeta.library.datablocks.impl;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Footer;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link BackwardDataBlockReader} is used for backward-reading of data blocks. One important thing to know is that once
 * a top-level container has been backward-read, its contents is <i>forward read</i>. This is the reason why this class
 * has a reference to its brother forward {@link DataBlockReader} which might need to be passed to a child.
 */
public class BackwardDataBlockReader extends AbstractDataBlockReader {

   private final DataBlockReader forwardReader;

   /**
    * @return the forward {@link DataBlockReader}
    */
   protected DataBlockReader getForwardReader() {
      return forwardReader;
   }

   /**
    * Creates a new {@link BackwardDataBlockReader}.
    *
    * @param spec
    *           The {@link DataFormatSpecification}, must not be null
    * @param forwardReader
    *           The forward {@link DataBlockReader}, must not be null
    */
   public BackwardDataBlockReader(DataFormatSpecification spec, DataBlockReader forwardReader) {
      super(spec);
      Reject.ifNull(forwardReader, "forwardReader");

      this.forwardReader = forwardReader;
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractDataBlockReader#setMediumStore(com.github.jmeta.library.media.api.services.MediumStore)
    */
   @Override
   public void setMediumStore(MediumStore mediumStore) {
      super.setMediumStore(mediumStore);

      getForwardReader().setMediumStore(mediumStore);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readContainerWithId(com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.Payload, long, int,
    *      com.github.jmeta.library.datablocks.impl.ContainerContext)
    */
   @Override
   public Container readContainerWithId(MediumOffset currentOffset, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, int sequenceNumber, ContainerContext containerContext) {
      Reject.ifNull(id, "id");
      Reject.ifNull(currentOffset, "currentOffset");

      ContainerContext newContainerContext = new StandardContainerContext(getSpecification(), containerContext,
         getCustomSizeProvider(), getCustomCountProvider());

      DataBlockId concreteContainerId = determineConcreteContainerId(currentOffset, id, remainingDirectParentByteCount,
         0, newContainerContext);

      DataBlockDescription containerDesc = getSpecification().getDataBlockDescription(concreteContainerId);

      // Read footers
      MediumOffset nextReference = currentOffset;

      List<Footer> footers = new ArrayList<>();

      List<DataBlockDescription> footerDescs = containerDesc.getChildDescriptionsOfType(PhysicalDataBlockType.FOOTER);

      for (int i = 0; i < footerDescs.size(); ++i) {
         DataBlockDescription footerDesc = footerDescs.get(i);

         if (!footerDesc.hasFixedSize()) {
            throw new IllegalStateException("Cannot backward-read a footer with dynamic size");
         }

         nextReference = nextReference.advance(-footerDesc.getMaximumByteLength());

         List<Footer> nextFooters = readHeadersOrFootersWithId(Footer.class, nextReference, footerDesc.getId(),
            newContainerContext);

         footers.addAll(0, nextFooters);
      }

      // Read payload
      DataBlockDescription payloadDesc = getPayloadDescription(containerDesc);

      Payload payload = readPayload(nextReference, payloadDesc.getId(), concreteContainerId,
         remainingDirectParentByteCount, newContainerContext);

      // Read headers
      nextReference = nextReference.advance(-payload.getSize());

      List<Header> headers = new ArrayList<>();
      List<DataBlockDescription> headerDescs = containerDesc.getChildDescriptionsOfType(PhysicalDataBlockType.HEADER);

      for (int i = 0; i < headerDescs.size(); ++i) {
         DataBlockDescription headerDesc = headerDescs.get(i);

         if (!headerDesc.hasFixedSize()) {
            throw new IllegalStateException("Cannot backward-read a header with dynamic size");
         }

         nextReference = nextReference.advance(-headerDesc.getMaximumByteLength());

         List<Header> nextHeaders = readHeadersOrFootersWithId(Header.class, nextReference, headerDesc.getId(),
            newContainerContext);

         headers.addAll(0, nextHeaders);
      }

      // Create container
      // IMPORTANT: The containers StandardMediumReference MUST NOT be set to the original passed
      // StandardMediumReference because that one points to the containers back!
      final Container container = getDataBlockFactory().createContainer(concreteContainerId, parent, nextReference,
         headers, payload, footers, this, newContainerContext, sequenceNumber);
      newContainerContext.initContainer(container);

      return container;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readPayload(com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId, long,
    *      com.github.jmeta.library.datablocks.impl.ContainerContext)
    */
   @Override
   public Payload readPayload(MediumOffset reference, DataBlockId id, DataBlockId parentId,
      long remainingDirectParentByteCount, ContainerContext containerContext) {

      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");

      DataBlockDescription payloadDesc = getSpecification().getDataBlockDescription(id);

      long totalPayloadSize = containerContext.getSizeOf(payloadDesc.getId(), 0);

      if (totalPayloadSize == DataBlockDescription.UNDEFINED) {
         throw new IllegalStateException("Payload size could not be determined");
      }

      final Payload createPayloadAfterRead = getDataBlockFactory().createPayloadAfterRead(payloadDesc.getId(),
         reference.advance(-totalPayloadSize), totalPayloadSize, getForwardReader(), containerContext);

      return createPayloadAfterRead;
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractDataBlockReader#hasEnoughBytesForMagicKey(com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.MagicKey, long)
    */
   @Override
   protected boolean hasEnoughBytesForMagicKey(MediumOffset reference, MagicKey magicKey,
      long remainingDirectParentByteCount) {
      return reference.getAbsoluteMediumOffset() + magicKey.getDeltaOffset() >= 0;
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractDataBlockReader#getMagicKeys(com.github.jmeta.library.dataformats.api.types.DataBlockDescription)
    */
   @Override
   protected List<MagicKey> getMagicKeys(DataBlockDescription containerDesc) {
      return containerDesc.getFooterMagicKeys();
   }
}
