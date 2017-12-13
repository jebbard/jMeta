/**
 *
 * {@link MP3DataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 09.10.2011
 */
package com.github.jmeta.defaultextensions.ogg.impl;

import java.util.List;
import java.util.Map;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.TransformationHandler;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.impl.StandardDataBlockReader;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

// TODO primeRefactor010: cleanup and document OggDataBlockReader
/**
 * {@link OggDataBlockReader}
 *
 */
public class OggDataBlockReader extends StandardDataBlockReader {

   private static final DataBlockId PACKET_CONTAINER_ID = new DataBlockId(OggExtension.OGG,
      "ogg.payload.packetPartContainer");

   @Override
   protected void afterHeaderReading(DataBlockId containerId, FieldFunctionStack context, List<Header> headers) {

      if (containerId.getGlobalId().equals("ogg")) {
         final DataBlockId packetPayloadId = DataBlockDescription
            .getChildDescriptionsOfType(getSpecification(), PACKET_CONTAINER_ID, PhysicalDataBlockType.PAYLOAD).get(0)
            .getId();
         final DataBlockId oggPagePayloadId = DataBlockDescription
            .getChildDescriptionsOfType(getSpecification(), containerId, PhysicalDataBlockType.PAYLOAD).get(0).getId();
         final DataBlockId segmentFieldId = DataBlockDescription
            .getChildDescriptionsOfType(getSpecification(), packetPayloadId, PhysicalDataBlockType.FIELD).get(0)
            .getId();

         Header header = headers.get(0);

         long totalPayloadSize = 0;
         long sizeOfCurrentPacket = 0;
         long segmentCountOfCurrentPacket = 0;

         for (int i = 8; i < header.getFields().size(); ++i) {
            Field<?> segmentTableEntry = header.getFields().get(i);

            try {
               long segmentSize = (Long) segmentTableEntry.getInterpretedValue();

               totalPayloadSize += segmentSize;
               sizeOfCurrentPacket += segmentSize;

               if (segmentSize != 0)
                  segmentCountOfCurrentPacket++;

               if (segmentSize < 0xFF || (segmentSize == 0xFF && i == header.getFields().size() - 1)) {
                  context.pushFieldFunction(packetPayloadId, FieldFunctionType.SIZE_OF, sizeOfCurrentPacket);
                  context.pushFieldFunction(segmentFieldId, FieldFunctionType.COUNT_OF, segmentCountOfCurrentPacket);
                  sizeOfCurrentPacket = 0;
                  segmentCountOfCurrentPacket = 0;
               }
            } catch (BinaryValueConversionException e) {
               // TODO doItFirst003: do not Silently ignore but log
               throw new RuntimeException(e);
            }
         }

         context.pushFieldFunction(oggPagePayloadId, FieldFunctionType.SIZE_OF, totalPayloadSize);
      }
   }

   /**
    * Creates a new {@link OggDataBlockReader}.
    * 
    * @param spec
    * @param transformationHandlers
    * @param maxFieldBlockSize
    */
   public OggDataBlockReader(DataFormatSpecification spec,
      Map<DataTransformationType, TransformationHandler> transformationHandlers, int maxFieldBlockSize) {
      super(spec, transformationHandlers, maxFieldBlockSize);
   }
}
