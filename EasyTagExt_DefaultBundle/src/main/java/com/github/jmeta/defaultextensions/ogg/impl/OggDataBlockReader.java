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

import com.github.jmeta.defaultextensions.mp3.impl.MP3DataBlockReader;
import com.github.jmeta.library.datablocks.api.exception.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.ITransformationHandler;
import com.github.jmeta.library.datablocks.api.type.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.type.IField;
import com.github.jmeta.library.datablocks.api.type.IHeader;
import com.github.jmeta.library.datablocks.impl.StandardDataBlockReader;
import com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.type.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.type.DataBlockId;
import com.github.jmeta.library.dataformats.api.type.DataTransformationType;
import com.github.jmeta.library.dataformats.api.type.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.type.PhysicalDataBlockType;

import de.je.jmeta.defext.dataformats.DefaultExtensionsDataFormat;

// TODO primeRefactor010: cleanup and document OggDataBlockReader
/**
 * {@link OggDataBlockReader}
 *
 */
public class OggDataBlockReader extends StandardDataBlockReader {

   private static final DataBlockId PACKET_CONTAINER_ID = new DataBlockId(DefaultExtensionsDataFormat.OGG,
      "ogg.payload.packetPartContainer");

   @Override
   protected void afterHeaderReading(DataBlockId containerId, FieldFunctionStack context, List<IHeader> headers) {

      if (containerId.getGlobalId().equals("ogg")) {
         final DataBlockId packetPayloadId = DataBlockDescription
            .getChildDescriptionsOfType(getSpecification(), PACKET_CONTAINER_ID, PhysicalDataBlockType.PAYLOAD).get(0)
            .getId();
         final DataBlockId oggPagePayloadId = DataBlockDescription
            .getChildDescriptionsOfType(getSpecification(), containerId, PhysicalDataBlockType.PAYLOAD).get(0).getId();
         final DataBlockId segmentFieldId = DataBlockDescription
            .getChildDescriptionsOfType(getSpecification(), packetPayloadId, PhysicalDataBlockType.FIELD).get(0)
            .getId();

         IHeader header = headers.get(0);

         long totalPayloadSize = 0;
         long sizeOfCurrentPacket = 0;
         long segmentCountOfCurrentPacket = 0;

         for (int i = 8; i < header.getFields().size(); ++i) {
            IField<?> segmentTableEntry = header.getFields().get(i);

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
    * @param logging
    */
   public OggDataBlockReader(IDataFormatSpecification spec,
      Map<DataTransformationType, ITransformationHandler> transformationHandlers, int maxFieldBlockSize) {
      super(spec, transformationHandlers, maxFieldBlockSize);
   }
}
