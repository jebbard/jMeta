/**
 * {@link StandardDataBlockFactory}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.DataBlockState;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldSequence;
import com.github.jmeta.library.datablocks.api.types.Footer;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class StandardDataBlockFactory implements ExtendedDataBlockFactory {

   private final MediumDataProvider mediumDataProvider;
   private final DataFormatSpecification spec;
   private final DataBlockEventBus eventBus;

   /**
    * Creates a new {@link StandardDataBlockFactory}.
    *
    * @param mediumDataProvider
    * @param eventBus
    *           TODO
    */
   public StandardDataBlockFactory(MediumDataProvider mediumDataProvider, DataFormatSpecification spec,
      DataBlockEventBus eventBus) {
      super();
      this.mediumDataProvider = mediumDataProvider;
      this.spec = spec;
      this.eventBus = eventBus;
   }

   @Override
   public Container createPersistedContainer(DataBlockId id, int sequenceNumber, DataBlock parent, MediumOffset offset,
      List<Header> headers, Payload payload, List<Footer> footers, DataBlockReader reader,
      ContainerContext containerContext) {

      return new StandardContainer(id, parent, offset, headers, payload, footers, mediumDataProvider, containerContext,
         sequenceNumber, eventBus);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockFactory#createPersistedContainer(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      int, com.github.jmeta.library.datablocks.api.types.DataBlock,
    *      com.github.jmeta.library.media.api.types.MediumOffset,
    *      com.github.jmeta.library.datablocks.api.services.DataBlockReader,
    *      com.github.jmeta.library.datablocks.impl.StandardContainerContext)
    */
   @Override
   public Container createPersistedContainerWithoutChildren(DataBlockId id, int sequenceNumber, DataBlock parent,
      MediumOffset offset, DataBlockReader reader, ContainerContext containerContext) {
      return new StandardContainer(id, parent, offset, mediumDataProvider, containerContext, sequenceNumber, eventBus);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedHeader(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.media.api.types.MediumOffset, java.util.List, int,
    *      com.github.jmeta.library.datablocks.api.types.ContainerContext,
    *      com.github.jmeta.library.datablocks.api.services.DataBlockReader)
    */
   @Override
   public Header createPersistedHeader(DataBlockId id, MediumOffset reference, List<Field<?>> fields,
      int sequenceNumber, ContainerContext containerContext, DataBlockReader reader) {
      return createHeaderOrFooter(Header.class, id, reference, fields, sequenceNumber, containerContext);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedFooter(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.media.api.types.MediumOffset, java.util.List, int,
    *      com.github.jmeta.library.datablocks.api.types.ContainerContext,
    *      com.github.jmeta.library.datablocks.api.services.DataBlockReader)
    */
   @Override
   public Footer createPersistedFooter(DataBlockId id, MediumOffset reference, List<Field<?>> fields,
      int sequenceNumber, ContainerContext containerContext, DataBlockReader reader) {
      return createHeaderOrFooter(Footer.class, id, reference, fields, sequenceNumber, containerContext);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedField(DataBlockId,
    *      int, DataBlock, MediumOffset, BinaryValue, StandardContainerContext)
    */
   @Override
   public <T> Field<T> createPersistedField(DataBlockId id, int sequenceNumber, DataBlock parent, MediumOffset offset,
      ByteBuffer fieldBytes, ContainerContext containerContext, DataBlockReader reader) {

      Reject.ifNull(id, "fieldDesc");
      Reject.ifNull(offset, "reference");
      Reject.ifNegative(fieldBytes.remaining(), "fieldBytes.remaining()");

      StandardField<T> field = new StandardField<>(id, spec);

      if (parent != null) {
         field.initParent(parent);
      }

      field.attachToMedium(offset, sequenceNumber, mediumDataProvider, eventBus, DataBlockState.PERSISTED);
      field.initContainerContext(containerContext);
      field.setBinaryValue(fieldBytes);

      return field;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createPersistedPayload(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      MediumOffset, StandardContainerContext, long,
    *      com.github.jmeta.library.datablocks.api.services.DataBlockReader)
    */
   @Override
   public Payload createPersistedPayload(DataBlockId id, MediumOffset offset, ContainerContext containerContext,
      long totalSize, DataBlockReader reader) {

      Reject.ifNull(id, "id");
      Reject.ifNull(offset, "reference");
      Reject.ifNull(reader, "reader");

      DataFormatSpecification spec = reader.getSpecification();

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      if (desc.getPhysicalType() == PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD) {
         ContainerBasedLazyPayload containerBasedLazyPayload = new ContainerBasedLazyPayload(id, spec, reader);
         containerBasedLazyPayload.initContainerContext(containerContext);
         containerBasedLazyPayload.attachToMedium(offset, 0, mediumDataProvider, eventBus, DataBlockState.PERSISTED);
         containerBasedLazyPayload.initSize(totalSize);

         return containerBasedLazyPayload;
      } else {
         FieldBasedLazyPayload fieldBasedLazyPayload = new FieldBasedLazyPayload(id, spec, reader);
         fieldBasedLazyPayload.initContainerContext(containerContext);
         fieldBasedLazyPayload.attachToMedium(offset, 0, mediumDataProvider, eventBus, DataBlockState.PERSISTED);
         fieldBasedLazyPayload.initSize(totalSize);

         return fieldBasedLazyPayload;
      }
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory#createHeader(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      MediumOffset, java.util.List, DataBlockReader, int, StandardContainerContext)
    */
   private <T extends FieldSequence> T createHeaderOrFooter(Class<T> fieldSequenceClass, DataBlockId id,
      MediumOffset reference, List<Field<?>> fields, int sequenceNumber, ContainerContext containerContext) {

      Reject.ifNull(id, "headerRef");
      Reject.ifNull(reference, "parent");
      Reject.ifNull(fields, "fields");

      StandardHeaderOrFooter headerOrFooter = new StandardHeaderOrFooter(id, spec, fieldSequenceClass == Footer.class);

      headerOrFooter.initContainerContext(containerContext);
      headerOrFooter.attachToMedium(reference, sequenceNumber, mediumDataProvider, eventBus, DataBlockState.PERSISTED);
      headerOrFooter.setFields(fields);

      return (T) headerOrFooter;
   }
}
