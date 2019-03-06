/**
 * {@link StandardDataBlockFactory}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class StandardDataBlockFactory implements DataBlockFactory {

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createContainer(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.DataBlock, MediumOffset, java.util.List,
    *      com.github.jmeta.library.datablocks.api.types.Payload, java.util.List, DataBlockReader, ContainerContext,
    *      int)
    */
   @Override
   public Container createContainer(DataBlockId id, DataBlock parent, MediumOffset reference, List<Header> headers,
      Payload payload, List<Header> footers, DataBlockReader reader, ContainerContext containerContext,
      int sequenceNumber) {

      return new StandardContainer(id, parent, reference, headers, payload, footers, reader, containerContext,
         sequenceNumber);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockFactory#createContainer(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.DataBlock,
    *      com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.datablocks.api.services.DataBlockReader,
    *      com.github.jmeta.library.datablocks.api.types.ContainerContext, int)
    */
   @Override
   public Container createContainer(DataBlockId id, DataBlock parent, MediumOffset reference, DataBlockReader reader,
      ContainerContext containerContext, int sequenceNumber) {
      return new StandardContainer(id, parent, reference, reader, containerContext, sequenceNumber);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createFieldFromBytes(DataBlockId,
    *      DataFormatSpecification, MediumOffset, BinaryValue, ByteOrder, Charset, int, ContainerContext)
    */
   @Override
   public <T> Field<T> createFieldFromBytes(DataBlockId id, DataFormatSpecification spec, MediumOffset reference,
      ByteBuffer fieldBytes, ByteOrder byteOrder, Charset characterEncoding, int sequenceNumber,
      ContainerContext containerContext) {

      Reject.ifNull(id, "fieldDesc");
      Reject.ifNull(reference, "reference");
      Reject.ifNegative(fieldBytes.remaining(), "fieldBytes.remaining()");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      StandardField<T> field = new StandardField<>(desc, fieldBytes, reference, sequenceNumber, containerContext);

      field.initByteOrder(byteOrder);
      field.initCharacterEncoding(characterEncoding);

      return field;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPayloadAfterRead(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      MediumOffset, long, com.github.jmeta.library.datablocks.api.services.DataBlockReader, ContainerContext)
    */
   @Override
   public Payload createPayloadAfterRead(DataBlockId id, MediumOffset reference, long totalSize, DataBlockReader reader,
      ContainerContext containerContext) {

      Reject.ifNull(id, "id");
      Reject.ifNull(reference, "reference");
      Reject.ifNull(reader, "reader");

      DataFormatSpecification spec = reader.getSpecification();

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      if (desc.getPhysicalType() == PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD) {
         return new ContainerBasedLazyPayload(id, reference, totalSize, reader, containerContext);
      } else {
         return new FieldBasedLazyPayload(id, reference, totalSize, reader, containerContext);
      }
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createHeaderOrFooter(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      MediumOffset, java.util.List, boolean, DataBlockReader, int, ContainerContext)
    */
   @Override
   public Header createHeaderOrFooter(DataBlockId id, MediumOffset reference, List<Field<?>> fields, boolean isFooter,
      DataBlockReader reader, int sequenceNumber, ContainerContext containerContext) {

      Reject.ifNull(id, "headerRef");
      Reject.ifNull(reference, "parent");
      Reject.ifNull(fields, "fields");

      return new StandardHeader(id, reference, fields, isFooter, reader, sequenceNumber, containerContext);
   }
}
