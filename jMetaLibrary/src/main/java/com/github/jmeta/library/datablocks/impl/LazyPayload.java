/**
 * {@link LazyPayload}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Field;
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
public class LazyPayload extends AbstractDataBlock implements Payload {

   /**
    * Creates a new {@link LazyPayload}.
    * 
    * @param id
    * @param reference
    * @param totalSize
    * @param dataBlockReader
    * @param context
    */
   public LazyPayload(DataBlockId id, MediumOffset reference,
      long totalSize, DataBlockReader dataBlockReader,
      FieldFunctionStack context) {
      super(id, null, reference, dataBlockReader);

      Reject.ifNull(context, "context");

      m_context = context;
      determineFieldPosition(id);

      m_totalSize = totalSize;

      // TODO primeRefactor007: review this new code and document...
      // The size of the payload is still unknown - There is no other way than to read
      // its children and sum up their sizes...
      if (m_totalSize == DataBlockDescription.UNKNOWN_SIZE) {
         long summedUpTotalSize = 0;

         if (m_hasFields) {
            List<Field<?>> fields = getFields();

            for (int i = 0; i < fields.size(); ++i) {
               Field<?> field = fields.get(i);

               summedUpTotalSize += field.getTotalSize();
            }
         }

         if (m_hasContainers) {
            AbstractDataBlockIterator<Container> containerIter = getContainerIterator();

            while (containerIter.hasNext()) {
               Container container = containerIter.next();

               summedUpTotalSize += container.getTotalSize();
            }
         }

         m_totalSize = summedUpTotalSize;
      }

      else
         m_totalSize = totalSize;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Payload#getFields()
    */
   @Override
   public List<Field<?>> getFields() {
      // TODO: Big issue using fieldFunctionStack here:
      // When reading a Payload that has both fields and containers as children, and
      // when the containers are located before the fields, then calling getFields()
      // WILL EMPTY the field function stack (pop). The consequence is that a subsequent
      // call to getContainerIterator().next() will cause exceptions due to no (longer)
      // present field functions required.

      // Fields start first
      if (m_fields == null)
         if (!m_hasFields)
            m_fields = new ArrayList<>();

         else {
            MediumOffset fieldReference = getMediumReference();

            m_fields = new ArrayList<>();

            long totalContainerSize = 0;

            // First read the containers to know where exactly the fields start
            if (!m_fieldsFirst) {
               AbstractDataBlockIterator<Container> iter = getContainerIterator();

               while (iter.hasNext()) {
                  Container container = iter.next();

                  totalContainerSize += container.getTotalSize();
               }

               fieldReference = fieldReference.advance(totalContainerSize);
            }

            if (m_hasFields || m_totalSize - totalContainerSize > 0) {
               List<Field<?>> readFields = getDataBlockReader().readFields(
                  fieldReference, getId(), m_context,
                  m_totalSize - totalContainerSize);

               for (int i = 0; i < readFields.size(); ++i) {
                  Field<?> field = readFields.get(i);

                  addField(field);
               }
            }
         }

      return m_fields;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Payload#getContainerIterator()
    */
   @Override
   public AbstractDataBlockIterator<Container> getContainerIterator() {

      MediumOffset containerReference = getMediumReference();

      long totalFieldSize = 0;

      if (m_hasFields && m_fieldsFirst) {
         Iterator<Field<?>> fieldIterator = getFields().iterator();

         while (fieldIterator.hasNext()) {
            Field<?> field = fieldIterator.next();

            totalFieldSize += field.getTotalSize();
         }

         containerReference = containerReference.advance(totalFieldSize);
      }

      return new PayloadContainerIterator(this, getDataBlockReader(),
         containerReference, m_context, totalFieldSize);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getTotalSize()
    */
   @Override
   public long getTotalSize() {

      return m_totalSize;
   }

   /**
    * @param id
    */
   private void determineFieldPosition(DataBlockId id) {

      DataFormatSpecification spec = getDataBlockReader().getSpecification();

      int fieldCount = DataBlockDescription
         .getChildDescriptionsOfType(spec, id, PhysicalDataBlockType.FIELD)
         .size();
      int containerCount = DataBlockDescription
         .getChildDescriptionsOfType(spec, id, PhysicalDataBlockType.CONTAINER)
         .size();

      final List<DataBlockId> orderedChildIds = spec.getDataBlockDescription(id)
         .getOrderedChildIds();

      if (fieldCount + containerCount != orderedChildIds.size())
         throw new IllegalStateException("For payload block " + id
            + ", only children with physical block type CONTAINER and FIELD are allowed to be specified.");

      m_hasFields = fieldCount > 0;
      m_hasContainers = containerCount > 0;

      PhysicalDataBlockType previousPhysicalType = null;
      PhysicalDataBlockType currentPhysicalType = null;

      int typeChanges = 0;

      for (int i = 0; i < orderedChildIds.size(); ++i) {
         DataBlockDescription desc = spec
            .getDataBlockDescription(orderedChildIds.get(i));

         currentPhysicalType = desc.getPhysicalType();

         if (currentPhysicalType.equals(PhysicalDataBlockType.FIELD))
            m_fieldsFirst = true;

         else
            m_fieldsFirst = false;

         if (previousPhysicalType != null
            && previousPhysicalType != currentPhysicalType)
            typeChanges++;

         previousPhysicalType = currentPhysicalType;
      }

      if (typeChanges > 1)
         throw new IllegalStateException("For payload data block " + id
            + ", there child containers and child fields must each form exactly one contiguous sequence, either fields first or containers first, no mixture possible.");
   }

   private void addField(Field<?> field) {

      Reject.ifNull(field, "field");

      field.initParent(this);

      m_fields.add(field);
   }

   private final FieldFunctionStack m_context;

   private long m_totalSize;

   private List<Field<?>> m_fields;

   private boolean m_fieldsFirst;

   private boolean m_hasFields;

   private boolean m_hasContainers;
}
