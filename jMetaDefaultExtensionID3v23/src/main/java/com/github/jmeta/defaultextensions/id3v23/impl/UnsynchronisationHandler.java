/**
 *
 * {@link UnsynchronisationHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 08.03.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.utility.byteutils.api.services.ByteArrayUtils;

/**
 * {@link UnsynchronisationHandler}
 *
 */
public class UnsynchronisationHandler extends AbstractID3v2TransformationHandler {

   private static final Logger LOGGER = LoggerFactory.getLogger(UnsynchronisationHandler.class);

   private static final int UNSYNCHRONISATION_TRANSFORMATION_ID = 257;

   private static final String ID3V2_HEADER_FLAGS_ID = "id3v23.header.flags";

   private static final String UNSYNCHRONISATION_FLAG_NAME = "UNSYNCHRONISATION";

   /**
    * Creates a new {@link UnsynchronisationHandler}.
    * 
    * @param dtt
    * @param dbFactory
    */
   public UnsynchronisationHandler(DataTransformationType dtt, DataBlockFactory dbFactory) {
      super(dtt, UNSYNCHRONISATION_TRANSFORMATION_ID, dbFactory);
   }

   /**
    * @see com.github.jmeta.defaultextensions.id3v23.impl.AbstractTransformationHandler#requiresTransform(com.github.jmeta.library.datablocks.api.types.Container)
    */
   @Override
   public boolean requiresTransform(Container container) {

      if (super.requiresTransform(container)) {
         if (container.getHeaders().size() == 0)
            return false;

         Header id3v2Header = container.getHeaders().get(0);

         for (int i = 0; i < id3v2Header.getFields().size(); ++i) {
            Field<?> field = id3v2Header.getFields().get(i);

            if (field.getId().equals(ID3V2_HEADER_FLAGS_ID)) {
               try {
                  Flags flags = (Flags) field.getInterpretedValue();

                  return flags.getFlag(UNSYNCHRONISATION_FLAG_NAME);
               } catch (BinaryValueConversionException e) {
                  LOGGER.warn(
                     "Field conversion from binary to interpreted value failed for field id <%1$s>. Exception see below.",
                     field.getId());
                  LOGGER.error("requiresTransform", e);
                  return false;
               }
            }
         }
      }

      return false;
   }

   /**
    * @see com.github.jmeta.defaultextensions.id3v23.impl.AbstractTransformationHandler#requiresUntransform(com.github.jmeta.library.datablocks.api.types.Container)
    */
   @Override
   public boolean requiresUntransform(Container container) {

      return requiresTransform(container);
   }

   /**
    * @see AbstractID3v2TransformationHandler#untransformRawBytes(byte[])
    */
   @Override
   // Synchronise
   protected byte[][] untransformRawBytes(ByteBuffer payloadBytes) {

      if (payloadBytes.remaining() < 1)
         return new byte[][] { payloadBytes.array() };

      List<Byte> synchronisedByteList = new ArrayList<>(payloadBytes.remaining());

      for (int i = payloadBytes.remaining() - 1; i > 0; i--) {
         byte previousByte = payloadBytes.get(i - 1);
         byte nextByte = payloadBytes.get(i);

         if (previousByte == 0xFF && nextByte == 0x00)
            synchronisedByteList.add(previousByte);

         else
            synchronisedByteList.add(nextByte);
      }

      synchronisedByteList.add(payloadBytes.get(payloadBytes.remaining() - 1));

      Collections.reverse(synchronisedByteList);

      return new byte[][] { ByteArrayUtils.toArray(synchronisedByteList) };
   }

   /**
    * @see AbstractID3v2TransformationHandler#transformRawBytes(byte[])
    */
   @Override
   protected byte[][] transformRawBytes(ByteBuffer payloadBytes) {

      if (payloadBytes.remaining() < 1)
         return new byte[][] { payloadBytes.array() };

      List<Byte> unsynchronisedByteList = new ArrayList<>(payloadBytes.remaining());

      for (int i = 0; i < payloadBytes.remaining() - 1; i++) {
         byte firstByte = payloadBytes.get(i);
         byte secondByte = payloadBytes.get(i + 1);

         unsynchronisedByteList.add(firstByte);

         if (firstByte == 0xFF && (secondByte >= 0xE0 || secondByte == 0))
            unsynchronisedByteList.add((byte) 0);
      }

      unsynchronisedByteList.add(payloadBytes.get(payloadBytes.remaining() - 1));

      return new byte[][] { ByteArrayUtils.toArray(unsynchronisedByteList) };
   }
}
