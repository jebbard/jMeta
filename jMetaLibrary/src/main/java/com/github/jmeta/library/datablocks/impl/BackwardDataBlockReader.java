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
 * {@link BackwardDataBlockReader}
 *
 */
public class BackwardDataBlockReader extends AbstractDataBlockReader {

   private final DataBlockReader forwardReader;

   /**
    * Returns the attribute {@link #forwardReader}.
    *
    * @return the attribute {@link #forwardReader}
    */
   protected DataBlockReader getForwardReader() {
      return forwardReader;
   }

   /**
    * Creates a new {@link BackwardDataBlockReader}.
    *
    * @param spec
    */
   public BackwardDataBlockReader(DataFormatSpecification spec, DataBlockReader forwardReader) {
      super(spec);

      this.forwardReader = forwardReader;
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractDataBlockReader#setMediumCache(com.github.jmeta.library.media.api.services.MediumStore)
    */
   @Override
   public void setMediumCache(MediumStore cache) {
      super.setMediumCache(cache);

      getForwardReader().setMediumCache(cache);
   }

   @Override
   public Container readContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, ContainerContext containerContext, int sequenceNumber) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(id, "id");

      ContainerContext newContainerContext = new ContainerContext(getSpecification(), containerContext,
         getCustomSizeProvider(), getCustomCountProvider());

      DataBlockId actualId = determineActualContainerId(reference, id, remainingDirectParentByteCount, 0,
         newContainerContext);

      // Read footers
      MediumOffset nextReference = reference;

      List<Header> footers = new ArrayList<>();

      DataBlockDescription actualDesc = getSpecification().getDataBlockDescription(actualId);

      List<DataBlockDescription> footerDescs = actualDesc.getChildDescriptionsOfType(PhysicalDataBlockType.FOOTER);

      for (int i = 0; i < footerDescs.size(); ++i) {
         DataBlockDescription footerDesc = footerDescs.get(i);

         long staticLength = footerDesc.hasFixedSize() ? footerDesc.getMaximumByteLength()
            : DataBlockDescription.UNDEFINED;

         nextReference = nextReference.advance(-staticLength);

         List<Header> nextFooters = readHeadersOrFootersWithId(nextReference, footerDesc.getId(), actualId, footers,
            true, newContainerContext);

         @SuppressWarnings("unused")
         long totalFooterSize = 0;

         for (int j = 0; j < nextFooters.size(); ++j) {
            Header nextFooter = nextFooters.get(j);

            totalFooterSize += nextFooter.getTotalSize();
         }

         // TODO primeRefactor003: handle multiple footers

         footers.addAll(nextFooters);
      }

      // Read payload
      List<DataBlockDescription> payloadDescs = actualDesc
         .getChildDescriptionsOfType(PhysicalDataBlockType.FIELD_BASED_PAYLOAD);

      payloadDescs.addAll(actualDesc.getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD));

      // CONFIG_CHECK: Any container must specify a single PAYLOAD block
      if (payloadDescs.size() != 1) {
         throw new IllegalStateException("For container parents, there must be a single data block of type PAYLOAD");
      }

      DataBlockDescription payloadDesc = payloadDescs.get(0);

      Payload payload = readPayload(nextReference, payloadDesc.getId(), actualId, footers,
         remainingDirectParentByteCount, newContainerContext);

      // Read headers
      nextReference = nextReference.advance(-payload.getTotalSize());

      List<Header> headers = new ArrayList<>();

      List<DataBlockDescription> headerDescs = actualDesc.getChildDescriptionsOfType(PhysicalDataBlockType.HEADER);

      for (int i = 0; i < headerDescs.size(); ++i) {
         DataBlockDescription headerDesc = headerDescs.get(i);

         // TODO primeRefactor003: handle multiple headers?

         // TODO primeRefactor003: expects static header size?
         nextReference = nextReference.advance(-headerDesc.getMaximumByteLength());

         List<Header> nextHeaders = readHeadersOrFootersWithId(nextReference, headerDesc.getId(), actualId, headers,
            false, newContainerContext);

         @SuppressWarnings("unused")
         long totalHeaderSize = 0;

         for (int j = 0; j < nextHeaders.size(); ++j) {
            Header nextHeader = nextHeaders.get(j);

            totalHeaderSize += nextHeader.getTotalSize();
         }

         headers.addAll(nextHeaders);
      }

      // Create container
      // IMPORTANT: The containers StandardMediumReference MUST NOT be set to the original passed
      // StandardMediumReference because that one points to the containers back!
      final Container container = getDataBlockFactory().createContainer(actualId, parent, nextReference, headers,
         payload, footers, this, newContainerContext, sequenceNumber);

      return container;
   }

   @Override
   public Payload readPayload(MediumOffset reference, DataBlockId id, DataBlockId parentId, List<Header> footers,
      long remainingDirectParentByteCount, ContainerContext containerContext) {

      Reject.ifNull(footers, "footers");
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

   @Override
   protected boolean hasEnoughBytesForMagicKey(MediumOffset reference, MagicKey magicKey,
      long remainingDirectParentByteCount) {
      return reference.getAbsoluteMediumOffset() + magicKey.getDeltaOffset() >= 0;
   }

   @Override
   protected List<MagicKey> getMagicKeys(DataBlockDescription containerDesc) {
      return containerDesc.getFooterMagicKeys();
   }
}
