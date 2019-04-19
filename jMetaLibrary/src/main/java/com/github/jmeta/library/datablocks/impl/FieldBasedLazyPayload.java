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

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlockState;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldBasedPayload;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FieldBasedLazyPayload} is the default implementation of {@link FieldBasedPayload}. It lazily reads fields when
 * first requested.
 */
public class FieldBasedLazyPayload extends AbstractDataBlock implements FieldBasedPayload {

   private long totalSize;
   private DataBlockReader reader;

   private List<Field<?>> fields;

   /**
    * Creates a new {@link FieldBasedLazyPayload}.
    *
    * @param id
    *           The {@link DataBlockId} of the payload
    * @param offset
    *           The start {@link MediumOffset} of the payload
    * @param totalSize
    *           The total payload size, if already known, or {@link DataBlockDescription#UNDEFINED} if not yet known
    * @param dataBlockReader
    *           The {@link DataBlockReader} to be used parsing the content of the payload
    * @param containerContext
    *           TODO
    * @param mediumDataProvider
    *           TODO
    * @param context
    *           The current {@link FieldFunctionStack} context needed for parsing
    */
   public FieldBasedLazyPayload(DataBlockId id, MediumOffset offset, long totalSize, DataBlockReader dataBlockReader,
      ContainerContext containerContext, MediumDataProvider mediumDataProvider, DataBlockEventBus eventBus) {
      super(id, 0, offset, null, mediumDataProvider, containerContext, DataBlockState.PERSISTED, eventBus);
      reader = dataBlockReader;

      // The size of the payload is still unknown - There is no other way than to read
      // its children and sum up their sizes
      if (totalSize == DataBlockDescription.UNDEFINED) {
         long summedUpTotalSize = 0;
         List<Field<?>> fields = getFields();

         for (int i = 0; i < fields.size(); ++i) {
            Field<?> field = fields.get(i);

            summedUpTotalSize += field.getSize();
         }

         this.totalSize = summedUpTotalSize;
      }

      else {
         this.totalSize = totalSize;
      }
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getSize()
    */
   @Override
   public long getSize() {
      return totalSize;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.FieldSequence#getFields()
    */
   @Override
   public List<Field<?>> getFields() {
      if (fields == null) {
         fields = new ArrayList<>();

         MediumOffset fieldReference = getOffset();

         if (totalSize > 0) {
            List<Field<?>> readFields = reader.readFields(fieldReference, getId(), totalSize, this,
               getContainerContext());

            for (int i = 0; i < readFields.size(); ++i) {
               Field<?> field = readFields.get(i);

               addField(field);
            }
         }
      }

      return fields;
   }

   private void addField(Field<?> field) {

      Reject.ifNull(field, "field");

      field.initParent(this);

      fields.add(field);
   }
}
