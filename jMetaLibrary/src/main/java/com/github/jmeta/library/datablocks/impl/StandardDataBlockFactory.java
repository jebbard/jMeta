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

   private final MediumDataProvider mediumDataProvider;
   private final DataFormatSpecification spec;

   /**
    * Creates a new {@link StandardDataBlockFactory}.
    *
    * @param mediumDataProvider
    */
   public StandardDataBlockFactory(MediumDataProvider mediumDataProvider, DataFormatSpecification spec) {
      super();
      this.mediumDataProvider = mediumDataProvider;
      this.spec = spec;
   }

   @Override
   public Container createPersistedContainer(DataBlockId id, int sequenceNumber, DataBlock parent, MediumOffset offset,
      List<Header> headers, Payload payload, List<Footer> footers, DataBlockReader reader,
      ContainerContext containerContext) {

      return new StandardContainer(id, parent, offset, headers, payload, footers, mediumDataProvider,
         containerContext, sequenceNumber);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockFactory#createPersistedContainer(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      int,
    *      com.github.jmeta.library.datablocks.api.types.DataBlock,
    *      com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.datablocks.api.services.DataBlockReader, com.github.jmeta.library.datablocks.impl.StandardContainerContext)
    */
   @Override
   public Container createPersistedContainer(DataBlockId id, int sequenceNumber, DataBlock parent, MediumOffset offset,
      DataBlockReader reader, ContainerContext containerContext) {
      return new StandardContainer(id, parent, offset, mediumDataProvider, containerContext, sequenceNumber);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedField(DataBlockId,
    *      int, DataBlock, MediumOffset, BinaryValue, StandardContainerContext)
    */
   @Override
   public <T> Field<T> createPersistedField(DataBlockId id, int sequenceNumber, DataBlock parent, MediumOffset offset,
      ByteBuffer fieldBytes, ContainerContext containerContext) {

      Reject.ifNull(id, "fieldDesc");
      Reject.ifNull(offset, "reference");
      Reject.ifNegative(fieldBytes.remaining(), "fieldBytes.remaining()");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      StandardField<T> field = new StandardField<>(desc, fieldBytes, offset, sequenceNumber, containerContext,
         mediumDataProvider, parent);

      return field;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedPayload(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      MediumOffset, StandardContainerContext, long,
    *      com.github.jmeta.library.datablocks.api.services.DataBlockReader)
    */
   @Override
   public Payload createPersistedPayload(DataBlockId id, MediumOffset offset, ContainerContext containerContext, long totalSize,
      DataBlockReader reader) {

      Reject.ifNull(id, "id");
      Reject.ifNull(offset, "reference");
      Reject.ifNull(reader, "reader");

      DataFormatSpecification spec = reader.getSpecification();

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      if (desc.getPhysicalType() == PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD) {
         return new ContainerBasedLazyPayload(id, offset, totalSize, reader, containerContext, mediumDataProvider);
      } else {
         return new FieldBasedLazyPayload(id, offset, totalSize, reader, containerContext, mediumDataProvider);
      }
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createHeader(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      MediumOffset, java.util.List, DataBlockReader, int, StandardContainerContext)
    */
   @Override
   public <T extends FieldSequence> T createHeaderOrFooter(Class<T> fieldSequenceClass, DataBlockId id,
      MediumOffset reference, List<Field<?>> fields, int sequenceNumber, ContainerContext containerContext) {

      Reject.ifNull(id, "headerRef");
      Reject.ifNull(reference, "parent");
      Reject.ifNull(fields, "fields");

      return (T) new StandardHeaderOrFooter(id, reference, fields, fieldSequenceClass == Footer.class,
         mediumDataProvider, sequenceNumber, containerContext);
   }
}
