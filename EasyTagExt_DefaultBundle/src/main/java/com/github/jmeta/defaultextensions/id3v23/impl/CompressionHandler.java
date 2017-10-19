/**
 *
 * {@link CompressionHandler}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.io.ByteArrayOutputStream;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.exception.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.IDataBlockFactory;
import com.github.jmeta.library.datablocks.api.type.IContainer;
import com.github.jmeta.library.datablocks.api.type.IField;
import com.github.jmeta.library.datablocks.api.type.IHeader;
import com.github.jmeta.library.dataformats.api.type.DataTransformationType;

import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.common.flags.Flags;

/**
 * {@link CompressionHandler}
 *
 */
public class CompressionHandler extends AbstractID3v2TransformationHandler {

   private static final Logger LOGGER = LoggerFactory.getLogger(CompressionHandler.class);

   private static final int BLOCK_SIZE = 1024;

   private static final int COMPRESSION_TRANSFORMATION_ID = 258;

   private static final String COMPRESSION_FLAG_NAME = "COMPRESSION";

   private static final String ID3V23_HEADER_FLAGS_ID = "id3v23.header.flags";

   /**
    * Creates a new {@link CompressionHandler}.
    * 
    * @param dtt
    * @param dbFactory
    * @param logging
    */
   public CompressionHandler(DataTransformationType dtt, IDataBlockFactory dbFactory) {
      super(dtt, COMPRESSION_TRANSFORMATION_ID, dbFactory);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ITransformationHandler#requiresTransform(com.github.jmeta.library.datablocks.api.type.IContainer)
    */
   @Override
   public boolean requiresTransform(IContainer container) {

      Reject.ifNull(container, "container");

      if (container.getHeaders().size() == 0)
         return false;

      IHeader firstHeader = container.getHeaders().get(0);

      for (int i = 0; i < firstHeader.getFields().size(); ++i) {
         IField<?> field = firstHeader.getFields().get(i);

         if (field.getId().equals(ID3V23_HEADER_FLAGS_ID)) {
            try {
               Flags flags = (Flags) field.getInterpretedValue();

               return flags.getFlag(COMPRESSION_FLAG_NAME);
            } catch (BinaryValueConversionException e) {
               LOGGER.warn(
                  "Field conversion from binary to interpreted value failed for field id <%1$s>. Exception see below.",
                  field.getId());
               LOGGER.error("requiresTransform", e);
               return false;
            }
         }
      }

      return false;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ITransformationHandler#requiresUntransform(com.github.jmeta.library.datablocks.api.type.IContainer)
    */
   @Override
   public boolean requiresUntransform(IContainer container) {

      Reject.ifNull(container, "container");

      return requiresTransform(container);
   }

   /**
    * @see AbstractID3v2TransformationHandler#transformRawBytes(byte[])
    */
   @Override
   protected byte[][] transformRawBytes(byte[] payloadBytes) {

      final Deflater compressor = new Deflater();
      compressor.setInput(payloadBytes);

      final ByteArrayOutputStream bos = new ByteArrayOutputStream(payloadBytes.length);
      byte[] buf = new byte[BLOCK_SIZE];

      try {
         int count = 0;

         while ((count = compressor.deflate(buf)) > 0)
            bos.write(buf, 0, count);

         if (!compressor.finished())
            throw new RuntimeException("Bad zip data, size:" + payloadBytes.length);
      } finally {
         compressor.end();
      }

      return new byte[][] { bos.toByteArray() };
   }

   /**
    * @see AbstractID3v2TransformationHandler#untransformRawBytes(byte[])
    */
   @Override
   protected byte[][] untransformRawBytes(byte[] payloadBytes) {

      // The case where the output gets longer than Integer.MAX is not handled anywhere
      // An OutOfMemoryError is expected whenever this happens during writing to the
      // ByteArrayOutputStream. However, this should not happen as ID3v2 also has a
      // decompressed size field of int size
      final Inflater decompressor = new Inflater();
      decompressor.setInput(payloadBytes);

      // This output stream will grow, if necessary
      final ByteArrayOutputStream bos = new ByteArrayOutputStream(payloadBytes.length);
      byte[] buf = new byte[BLOCK_SIZE];

      try {
         int count = 0;

         while ((count = decompressor.inflate(buf)) > 0)
            bos.write(buf, 0, count);

         if (!decompressor.finished())
            throw new RuntimeException("Bad zip data, size:" + payloadBytes.length);
      } catch (DataFormatException t) {
         throw new RuntimeException(t);
      } finally {
         decompressor.end();
      }

      return new byte[][] { bos.toByteArray() };
   }
}