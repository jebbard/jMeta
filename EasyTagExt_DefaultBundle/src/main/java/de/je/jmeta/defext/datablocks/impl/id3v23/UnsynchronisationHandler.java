/**
 *
 * {@link UnsynchronisationHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 08.03.2011
 */
package de.je.jmeta.defext.datablocks.impl.id3v23;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.je.jmeta.datablocks.BinaryValueConversionException;
import de.je.jmeta.datablocks.IContainer;
import de.je.jmeta.datablocks.IDataBlockFactory;
import de.je.jmeta.datablocks.IField;
import de.je.jmeta.datablocks.IHeader;
import de.je.jmeta.dataformats.DataTransformationType;
import de.je.util.javautil.common.array.EnhancedArrays;
import de.je.util.javautil.common.flags.Flags;

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
    * @param logging
    */
   public UnsynchronisationHandler(DataTransformationType dtt, IDataBlockFactory dbFactory) {
      super(dtt, UNSYNCHRONISATION_TRANSFORMATION_ID, dbFactory);
   }

   /**
    * @see de.je.jmeta.datablocks.export.AbstractTransformationHandler#requiresTransform(de.je.jmeta.datablocks.IContainer)
    */
   @Override
   public boolean requiresTransform(IContainer container) {

      if (super.requiresTransform(container)) {
         if (container.getHeaders().size() == 0)
            return false;

         IHeader id3v2Header = container.getHeaders().get(0);

         for (int i = 0; i < id3v2Header.getFields().size(); ++i) {
            IField<?> field = id3v2Header.getFields().get(i);

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
    * @see de.je.jmeta.datablocks.export.AbstractTransformationHandler#requiresUntransform(de.je.jmeta.datablocks.IContainer)
    */
   @Override
   public boolean requiresUntransform(IContainer container) {

      return requiresTransform(container);
   }

   /**
    * @see AbstractID3v2TransformationHandler#untransformRawBytes(byte[])
    */
   @Override
   // Synchronise
   protected byte[][] untransformRawBytes(byte[] payloadBytes) {

      if (payloadBytes.length < 1)
         return new byte[][] { payloadBytes };

      List<Byte> synchronisedByteList = new ArrayList<>(payloadBytes.length);

      for (int i = payloadBytes.length - 1; i > 0; i--) {
         byte previousByte = payloadBytes[i - 1];
         byte nextByte = payloadBytes[i];

         if (previousByte == 0xFF && nextByte == 0x00)
            synchronisedByteList.add(previousByte);

         else
            synchronisedByteList.add(nextByte);
      }

      synchronisedByteList.add(payloadBytes[payloadBytes.length - 1]);

      Collections.reverse(synchronisedByteList);

      return new byte[][] { EnhancedArrays.toArray(synchronisedByteList) };
   }

   /**
    * @see AbstractID3v2TransformationHandler#transformRawBytes(byte[])
    */
   @Override
   protected byte[][] transformRawBytes(byte[] payloadBytes) {

      if (payloadBytes.length < 1)
         return new byte[][] { payloadBytes };

      List<Byte> unsynchronisedByteList = new ArrayList<>(payloadBytes.length);

      for (int i = 0; i < payloadBytes.length - 1; i++) {
         byte firstByte = payloadBytes[i];
         byte secondByte = payloadBytes[i + 1];

         unsynchronisedByteList.add(firstByte);

         if (firstByte == 0xFF && (secondByte >= 0xE0 || secondByte == 0))
            unsynchronisedByteList.add((byte) 0);
      }

      unsynchronisedByteList.add(payloadBytes[payloadBytes.length - 1]);

      return new byte[][] { EnhancedArrays.toArray(unsynchronisedByteList) };
   }
}
