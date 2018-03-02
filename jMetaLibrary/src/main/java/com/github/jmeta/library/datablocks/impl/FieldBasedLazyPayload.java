/**
 *
 * {@link FieldBasedLazyPayload}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2018
 *
 */
package com.github.jmeta.library.datablocks.impl;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldBasedPayload;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FieldBasedLazyPayload}
 *
 */
public class FieldBasedLazyPayload extends AbstractDataBlock implements FieldBasedPayload {

   private long totalSize;

   private List<Field<?>> fields;

   private final FieldFunctionStack context;

   /**
    * Creates a new {@link FieldBasedLazyPayload}.
    * 
    * @param id
    * @param parent
    * @param reference
    * @param dataBlockReader
    */
   public FieldBasedLazyPayload(DataBlockId id, MediumOffset reference, long totalSize, DataBlockReader dataBlockReader,
      FieldFunctionStack context) {
      super(id, null, reference, dataBlockReader);

      Reject.ifNull(context, "context");

      this.context = context;

      // The size of the payload is still unknown - There is no other way than to read
      // its children and sum up their sizes
      if (totalSize == DataBlockDescription.UNKNOWN_SIZE) {
         long summedUpTotalSize = 0;
         List<Field<?>> fields = getFields();

         for (int i = 0; i < fields.size(); ++i) {
            Field<?> field = fields.get(i);

            summedUpTotalSize += field.getTotalSize();
         }

         this.totalSize = summedUpTotalSize;
      }

      else {
         this.totalSize = totalSize;
      }
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Payload#getContainerIterator()
    */
   @Override
   public AbstractDataBlockIterator<Container> getContainerIterator() {
      return null;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getTotalSize()
    */
   @Override
   public long getTotalSize() {
      return totalSize;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.FieldSequence#getFields()
    */
   @Override
   public List<Field<?>> getFields() {
      if (this.fields == null) {
         this.fields = new ArrayList<>();

         MediumOffset fieldReference = getMediumReference();

         if (this.totalSize > 0) {
            List<Field<?>> readFields = getDataBlockReader().readFields(fieldReference, getId(), this.context,
               this.totalSize);

            for (int i = 0; i < readFields.size(); ++i) {
               Field<?> field = readFields.get(i);

               addField(field);
            }
         }
      }

      return this.fields;
   }

   private void addField(Field<?> field) {

      Reject.ifNull(field, "field");

      field.initParent(this);

      this.fields.add(field);
   }
}
