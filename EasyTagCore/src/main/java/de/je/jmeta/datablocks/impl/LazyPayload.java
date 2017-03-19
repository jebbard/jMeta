/**
 * {@link LazyPayload}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package de.je.jmeta.datablocks.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.je.jmeta.datablocks.AbstractDataBlockIterator;
import de.je.jmeta.datablocks.IContainer;
import de.je.jmeta.datablocks.IField;
import de.je.jmeta.datablocks.IPayload;
import de.je.jmeta.datablocks.export.AbstractDataBlock;
import de.je.jmeta.datablocks.export.FieldFunctionStack;
import de.je.jmeta.datablocks.export.IDataBlockReader;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.jmeta.dataformats.DataBlockId;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.dataformats.PhysicalDataBlockType;
import de.je.jmeta.media.api.IMediumReference;
import de.je.util.javautil.common.err.Reject;

/**
 *
 */
public class LazyPayload extends AbstractDataBlock implements IPayload {

   /**
    * Creates a new {@link LazyPayload}.
    * 
    * @param id
    * @param reference
    * @param totalSize
    * @param dataBlockReader
    * @param context
    */
   public LazyPayload(DataBlockId id, IMediumReference reference,
      long totalSize, IDataBlockReader dataBlockReader,
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
            List<IField<?>> fields = getFields();

            for (int i = 0; i < fields.size(); ++i) {
               IField<?> field = fields.get(i);

               summedUpTotalSize += field.getTotalSize();
            }
         }

         if (m_hasContainers) {
            AbstractDataBlockIterator<IContainer> containerIter = getContainerIterator();

            while (containerIter.hasNext()) {
               IContainer container = containerIter.next();

               summedUpTotalSize += container.getTotalSize();
            }
         }

         m_totalSize = summedUpTotalSize;
      }

      else
         m_totalSize = totalSize;
   }

   /**
    * @see de.je.jmeta.datablocks.IPayload#getFields()
    */
   @Override
   public List<IField<?>> getFields() {
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
            IMediumReference fieldReference = getMediumReference();

            m_fields = new ArrayList<>();

            long totalContainerSize = 0;

            // First read the containers to know where exactly the fields start
            if (!m_fieldsFirst) {
               AbstractDataBlockIterator<IContainer> iter = getContainerIterator();

               while (iter.hasNext()) {
                  IContainer container = iter.next();

                  totalContainerSize += container.getTotalSize();
               }

               fieldReference = fieldReference.advance(totalContainerSize);
            }

            if (m_hasFields || m_totalSize - totalContainerSize > 0) {
               List<IField<?>> readFields = getDataBlockReader().readFields(
                  fieldReference, getId(), m_context,
                  m_totalSize - totalContainerSize);

               for (int i = 0; i < readFields.size(); ++i) {
                  IField<?> field = readFields.get(i);

                  addField(field);
               }
            }
         }

      return m_fields;
   }

   /**
    * @see de.je.jmeta.datablocks.IPayload#getContainerIterator()
    */
   @Override
   public AbstractDataBlockIterator<IContainer> getContainerIterator() {

      IMediumReference containerReference = getMediumReference();

      long totalFieldSize = 0;

      if (m_hasFields && m_fieldsFirst) {
         Iterator<IField<?>> fieldIterator = getFields().iterator();

         while (fieldIterator.hasNext()) {
            IField<?> field = fieldIterator.next();

            totalFieldSize += field.getTotalSize();
         }

         containerReference = containerReference.advance(totalFieldSize);
      }

      return new PayloadContainerIterator(this, getDataBlockReader(),
         containerReference, m_context, totalFieldSize);
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlock#getTotalSize()
    */
   @Override
   public long getTotalSize() {

      return m_totalSize;
   }

   /**
    * @param id
    */
   private void determineFieldPosition(DataBlockId id) {

      IDataFormatSpecification spec = getDataBlockReader().getSpecification();

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

   private void addField(IField<?> field) {

      Reject.ifNull(field, "field");

      field.initParent(this);

      m_fields.add(field);
   }

   private final FieldFunctionStack m_context;

   private long m_totalSize;

   private List<IField<?>> m_fields;

   private boolean m_fieldsFirst;

   private boolean m_hasFields;

   private boolean m_hasContainers;
}
