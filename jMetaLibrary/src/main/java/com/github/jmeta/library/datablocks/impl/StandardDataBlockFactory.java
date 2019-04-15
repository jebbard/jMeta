/**
 * {@link StandardDataBlockFactory}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldSequence;
import com.github.jmeta.library.datablocks.api.types.Footer;
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

   @Override
   public Container createContainer(DataBlockId id, DataBlock parent, MediumOffset reference, List<Header> headers,
      Payload payload, List<Footer> footers, DataBlockReader reader, ContainerContext containerContext,
      int sequenceNumber) {

      return new StandardContainer(id, parent, reference, headers, payload, footers, reader, containerContext,
         sequenceNumber);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockFactory#createContainer(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.datablocks.api.types.DataBlock,
    *      com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.datablocks.api.services.DataBlockReader,
    *      com.github.jmeta.library.datablocks.impl.StandardContainerContext, int)
    */
   @Override
   public Container createContainer(DataBlockId id, DataBlock parent, MediumOffset reference, DataBlockReader reader,
      ContainerContext containerContext, int sequenceNumber) {
      return new StandardContainer(id, parent, reference, reader, containerContext, sequenceNumber);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createFieldFromBytes(DataBlockId,
    *      DataFormatSpecification, MediumOffset, BinaryValue, int, DataBlockReader, DataBlock,
    *      StandardContainerContext)
    */
   @Override
   public <T> Field<T> createFieldFromBytes(DataBlockId id, DataFormatSpecification spec, MediumOffset reference,
      ByteBuffer fieldBytes, int sequenceNumber, DataBlockReader reader, DataBlock parent,
      ContainerContext containerContext) {

      Reject.ifNull(id, "fieldDesc");
      Reject.ifNull(reference, "reference");
      Reject.ifNegative(fieldBytes.remaining(), "fieldBytes.remaining()");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      StandardField<T> field = new StandardField<>(desc, fieldBytes, reference, sequenceNumber, containerContext,
         reader, parent);

      return field;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPayloadAfterRead(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      MediumOffset, long, com.github.jmeta.library.datablocks.api.services.DataBlockReader,
    *      StandardContainerContext)
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
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createHeader(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      MediumOffset, java.util.List, DataBlockReader, int, StandardContainerContext)
    */
   @Override
   public <T extends FieldSequence> T createHeaderOrFooter(Class<T> fieldSequenceClass, DataBlockId id,
      MediumOffset reference, List<Field<?>> fields, DataBlockReader reader, int sequenceNumber,
      ContainerContext containerContext) {

      Reject.ifNull(id, "headerRef");
      Reject.ifNull(reference, "parent");
      Reject.ifNull(fields, "fields");

      return (T) new StandardHeaderOrFooter(id, reference, fields, fieldSequenceClass == Footer.class, reader,
         sequenceNumber, containerContext);
   }
}
