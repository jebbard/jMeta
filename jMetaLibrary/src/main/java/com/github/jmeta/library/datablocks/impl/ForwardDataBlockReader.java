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

import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlockState;
import com.github.jmeta.library.datablocks.api.types.Footer;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ForwardDataBlockReader} is used for forward-reading of data blocks.
 */
public class ForwardDataBlockReader extends AbstractDataBlockReader {

   /**
    * Creates a new {@link ForwardDataBlockReader}.
    *
    * @param spec
    *           The {@link DataFormatSpecification}, must not be null
    * @param mediumStore
    *           TODO
    */
   public ForwardDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore, DataBlockEventBus eventBus) {
      super(spec, mediumStore, eventBus);
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

      getMediumDataProvider().bufferBeforeRead(currentOffset, remainingDirectParentByteCount);

      DataBlockId concreteContainerId = determineConcreteContainerId(currentOffset, id, remainingDirectParentByteCount,
         0, parent != null ? parent.getContainerContext() : null);

      StandardContainer createdContainer = new StandardContainer(concreteContainerId, getSpecification());

      createdContainer.initSequenceNumber(sequenceNumber);

      if (parent == null) {
         createdContainer.initTopLevelContainerContext(getCustomSizeProvider(), getCustomCountProvider());
      } else {
         createdContainer.initParent(parent);
      }

      ContainerContext newContainerContext = createdContainer.getContainerContext();

      DataBlockDescription containerDesc = getSpecification().getDataBlockDescription(concreteContainerId);

      // Read headers
      MediumOffset nextReference = currentOffset;

      List<Header> headers = new ArrayList<>();

      List<DataBlockDescription> headerDescs = containerDesc.getChildDescriptionsOfType(PhysicalDataBlockType.HEADER);

      long overallHeaderSize = 0;

      for (int i = 0; i < headerDescs.size(); ++i) {
         DataBlockDescription headerDesc = headerDescs.get(i);

         List<Header> nextHeaders = readHeadersOrFootersWithId(Header.class, nextReference, headerDesc.getId(),
            newContainerContext);

         long totalHeaderSize = 0;

         for (int j = 0; j < nextHeaders.size(); ++j) {
            Header nextHeader = nextHeaders.get(j);

            totalHeaderSize += nextHeader.getSize();
            createdContainer.insertHeader(createdContainer.getHeaders().size(), nextHeader);
         }

         overallHeaderSize += totalHeaderSize;

         nextReference = nextReference.advance(totalHeaderSize);

         headers.addAll(nextHeaders);
      }

      // Read payload
      DataBlockDescription payloadDesc = getPayloadDescription(containerDesc);

      long remainingPayloadByteCount = DataBlockDescription.UNDEFINED;

      if (remainingDirectParentByteCount != DataBlockDescription.UNDEFINED) {
         remainingPayloadByteCount = remainingDirectParentByteCount - overallHeaderSize;
      }

      Payload payload = readPayload(nextReference, payloadDesc.getId(), concreteContainerId, remainingPayloadByteCount,
         newContainerContext);

      createdContainer.setPayload(payload);

      // Read footers
      nextReference = nextReference.advance(payload.getSize());

      List<Footer> footers = new ArrayList<>();
      List<DataBlockDescription> footerDescs = containerDesc.getChildDescriptionsOfType(PhysicalDataBlockType.FOOTER);

      for (int i = 0; i < footerDescs.size(); ++i) {
         DataBlockDescription footerDesc = footerDescs.get(i);

         List<Footer> nextFooters = readHeadersOrFootersWithId(Footer.class, nextReference, footerDesc.getId(),
            newContainerContext);

         long totalFooterSize = 0;

         for (int j = 0; j < nextFooters.size(); ++j) {
            Footer nextFooter = nextFooters.get(j);

            totalFooterSize += nextFooter.getSize();
            createdContainer.insertFooter(createdContainer.getFooters().size(), nextFooter);
         }

         nextReference = nextReference.advance(totalFooterSize);

         footers.addAll(nextFooters);
      }

      createdContainer.attachToMedium(currentOffset, sequenceNumber, getMediumDataProvider(), getEventBus(),
         DataBlockState.PERSISTED);

      return createdContainer;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readPayload(MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId, DataBlockId, long, ContainerContext)
    */
   @Override
   public Payload readPayload(MediumOffset reference, DataBlockId id, DataBlockId parentId,
      long remainingDirectParentByteCount, ContainerContext containerContext) {
      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");

      DataBlockDescription payloadDesc = getSpecification().getDataBlockDescription(id);

      long totalPayloadSize = containerContext.getSizeOf(payloadDesc.getId(), 0);

      if (totalPayloadSize == DataBlockDescription.UNDEFINED) {
         totalPayloadSize = remainingDirectParentByteCount;
      }

      // If the medium is a stream-based medium, all the payload bytes must be cached first
      if (!reference.getMedium().isRandomAccess() && totalPayloadSize != DataBlockDescription.UNDEFINED
         && totalPayloadSize != 0) {
         getMediumDataProvider().bufferBeforeRead(reference, totalPayloadSize);
      }

      return getDataBlockFactory().createPersistedPayload(payloadDesc.getId(), reference, containerContext,
         totalPayloadSize, this);
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractDataBlockReader#hasEnoughBytesForMagicKey(com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.MagicKey, long)
    */
   @Override
   protected boolean hasEnoughBytesForMagicKey(MediumOffset reference, MagicKey magicKey,
      long remainingDirectParentByteCount) {
      return remainingDirectParentByteCount == DataBlockDescription.UNDEFINED
         || magicKey.getByteLength() <= remainingDirectParentByteCount;
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractDataBlockReader#getMagicKeys(com.github.jmeta.library.dataformats.api.types.DataBlockDescription)
    */
   @Override
   protected List<MagicKey> getMagicKeys(DataBlockDescription containerDesc) {
      return containerDesc.getHeaderMagicKeys();
   }
}
