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
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ForwardDataBlockReader}
 *
 */
public class ForwardDataBlockReader extends AbstractDataBlockReader {

   /**
    * Creates a new {@link ForwardDataBlockReader}.
    *
    * @param spec
    */
   public ForwardDataBlockReader(DataFormatSpecification spec) {
      super(spec);
   }

   /**
    * Returns the next {@link Container} with the given {@link DataBlockId} assumed to be stored starting at the given
    * {@link MediumOffset} or null. If the {@link Container}s presence is optional, its actual presence is determined
    *
    * @param parent
    */
   @Override
   public Container readContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
      long remainingDirectParentByteCount, ContainerContext containerContext, int sequenceNumber) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(id, "id");

      getMediumDataProvider().bufferBeforeRead(reference, remainingDirectParentByteCount);

      ContainerContext newContainerContext = new ContainerContext(getSpecification(), containerContext,
         getCustomSizeProvider(), getCustomCountProvider());

      DataBlockId actualId = determineActualContainerId(reference, id, remainingDirectParentByteCount, 0,
         newContainerContext);

      Container createdContainer = getDataBlockFactory().createContainer(actualId, parent, reference, this,
         newContainerContext, sequenceNumber);

      newContainerContext.initContainer(createdContainer);

      // Read headers
      MediumOffset nextReference = reference;

      List<Header> headers = new ArrayList<>();

      DataBlockDescription actualDesc = getSpecification().getDataBlockDescription(actualId);

      List<DataBlockDescription> headerDescs = actualDesc.getChildDescriptionsOfType(PhysicalDataBlockType.HEADER);

      long overallHeaderSize = 0;

      for (int i = 0; i < headerDescs.size(); ++i) {
         DataBlockDescription headerDesc = headerDescs.get(i);

         List<Header> nextHeaders = readHeadersOrFootersWithId(nextReference, headerDesc.getId(), actualId, headers,
            true, newContainerContext);

         long totalHeaderSize = 0;

         for (int j = 0; j < nextHeaders.size(); ++j) {
            Header nextHeader = nextHeaders.get(j);

            totalHeaderSize += nextHeader.getTotalSize();
            createdContainer.addHeader(createdContainer.getHeaders().size(), nextHeader);
         }

         overallHeaderSize += totalHeaderSize;

         nextReference = nextReference.advance(totalHeaderSize);

         headers.addAll(nextHeaders);
      }

      // Read payload
      DataBlockDescription payloadDesc = getPayloadDescription(actualDesc);

      long remainingPayloadByteCount = DataBlockDescription.UNDEFINED;

      if (remainingDirectParentByteCount != DataBlockDescription.UNDEFINED) {
         remainingPayloadByteCount = remainingDirectParentByteCount - overallHeaderSize;
      }

      Payload payload = readPayload(nextReference, payloadDesc.getId(), actualId, headers, remainingPayloadByteCount,
         newContainerContext);

      createdContainer.setPayload(payload);

      // Read footers
      nextReference = nextReference.advance(payload.getTotalSize());

      List<Header> footers = new ArrayList<>();
      List<DataBlockDescription> footerDescs = actualDesc.getChildDescriptionsOfType(PhysicalDataBlockType.FOOTER);

      for (int i = 0; i < footerDescs.size(); ++i) {
         DataBlockDescription footerDesc = footerDescs.get(i);

         List<Header> nextFooters = readHeadersOrFootersWithId(nextReference, footerDesc.getId(), actualId, footers,
            true, newContainerContext);

         long totalFooterSize = 0;

         for (int j = 0; j < nextFooters.size(); ++j) {
            Header nextFooter = nextFooters.get(j);

            totalFooterSize += nextFooter.getTotalSize();
            createdContainer.addFooter(j, nextFooter);
         }

         nextReference = nextReference.advance(totalFooterSize);

         footers.addAll(nextFooters);
      }

      return createdContainer;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readPayload(MediumOffset,
    *      com.github.jmeta.library.dataformats.api.types.DataBlockId, DataBlockId, java.util.List, long,
    *      ContainerContext)
    */
   @Override
   public Payload readPayload(MediumOffset reference, DataBlockId id, DataBlockId parentId, List<Header> headers,
      long remainingDirectParentByteCount, ContainerContext containerContext) {

      Reject.ifNull(headers, "headers");
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

      return getDataBlockFactory().createPayloadAfterRead(payloadDesc.getId(), reference, totalPayloadSize, this,
         containerContext);
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

   @Override
   protected List<MagicKey> getMagicKeys(DataBlockDescription containerDesc) {
      return containerDesc.getHeaderMagicKeys();
   }
}
