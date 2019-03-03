/**
 *
 * {@link OggPacketSizeAndCountProvider}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2019
 *
 */
package com.github.jmeta.defaultextensions.ogg.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.CountProvider;
import com.github.jmeta.library.datablocks.api.services.SizeProvider;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link OggPacketSizeAndCountProvider}
 *
 */
public class OggPacketSizeAndCountProvider implements SizeProvider, CountProvider {

   /**
    * @see com.github.jmeta.library.datablocks.api.services.CountProvider#getCountOf(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      int, com.github.jmeta.library.datablocks.api.types.ContainerContext)
    */
   @Override
   public long getCountOf(DataBlockId id, int sequenceNumber, ContainerContext containerContext) {
      if (id.equals(OggExtension.OGG_PACKET_PAYLOAD_FIELD_ID)) {
         Header oggPageHeader = containerContext.getParentContainerContext().getContainer().getHeaders().get(0);

         List<Long> segmentSizesFromHeader = getSegmentSizesFromHeader(oggPageHeader);
         List<Long> packetSegmentCounts = new ArrayList<>();

         long segmentCountOfCurrentPacket = 0;

         for (int segmentIndex = 0; segmentIndex < segmentSizesFromHeader.size(); segmentIndex++) {

            long segmentSize = segmentSizesFromHeader.get(segmentIndex);

            if (segmentSize != 0) {
               segmentCountOfCurrentPacket++;
            }

            if (segmentSize < 0xFF || segmentSize == 0xFF && segmentIndex == segmentSizesFromHeader.size() - 1) {
               packetSegmentCounts.add(segmentCountOfCurrentPacket);
               segmentCountOfCurrentPacket = 0;
            }
         }

         int containerSequenceNumber = containerContext.getContainer().getSequenceNumber();

         return packetSegmentCounts.get(containerSequenceNumber);
      }

      return DataBlockDescription.UNDEFINED;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.SizeProvider#getSizeOf(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      int, com.github.jmeta.library.datablocks.api.types.ContainerContext)
    */
   @Override
   public long getSizeOf(DataBlockId id, int sequenceNumber, ContainerContext containerContext) {
      if (id.equals(OggExtension.OGG_PAYLOAD_ID)) {
         Header oggPageHeader = containerContext.getContainer().getHeaders().get(0);

         List<Long> segmentSizesFromHeader = getSegmentSizesFromHeader(oggPageHeader);
         return segmentSizesFromHeader.stream().collect(Collectors.summingLong(size -> size));
      }

      if (id.equals(OggExtension.OGG_PACKET_PAYLOAD_ID)) {
         Header oggPageHeader = containerContext.getParentContainerContext().getContainer().getHeaders().get(0);

         List<Long> segmentSizesFromHeader = getSegmentSizesFromHeader(oggPageHeader);
         List<Long> packetSizes = new ArrayList<>();

         long sizeOfCurrentPacket = 0;

         for (int segmentIndex = 0; segmentIndex < segmentSizesFromHeader.size(); segmentIndex++) {

            long segmentSize = segmentSizesFromHeader.get(segmentIndex);

            sizeOfCurrentPacket += segmentSize;

            if (segmentSize < 0xFF || segmentSize == 0xFF && segmentIndex == segmentSizesFromHeader.size() - 1) {
               packetSizes.add(sizeOfCurrentPacket);
               sizeOfCurrentPacket = 0;
            }
         }

         int containerSequenceNumber = containerContext.getContainer().getSequenceNumber();

         return packetSizes.get(containerSequenceNumber);
      }

      return DataBlockDescription.UNDEFINED;
   }

   private List<Long> getSegmentSizesFromHeader(Header oggPageHeader) {
      List<Long> segmentSizes = new ArrayList<>();

      // Ogg segment sizes start with ogg page header field with index 8
      for (int fieldIndex = 8; fieldIndex < oggPageHeader.getFields().size(); ++fieldIndex) {
         Field<?> segmentTableEntry = oggPageHeader.getFields().get(fieldIndex);

         try {
            long segmentSize = (Long) segmentTableEntry.getInterpretedValue();
            segmentSizes.add(segmentSize);
         } catch (BinaryValueConversionException e) {
            throw new RuntimeException("Unexpected field conversion exception", e);
         }
      }

      return segmentSizes;
   }
}
