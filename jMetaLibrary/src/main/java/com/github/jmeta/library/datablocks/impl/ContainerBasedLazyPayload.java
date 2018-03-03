/**
 *
 * {@link ContainerBasedLazyPayload}.java
 *
 * @author Jens Ebert
 *
 * @date 03.03.2018
 *
 */
package com.github.jmeta.library.datablocks.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerBasedPayload;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ContainerBasedLazyPayload} is the default implementation of {@link ContainerBasedPayload}. It lazily reads
 * child containers by providing a corresponding iterator when first requested.
 */
public class ContainerBasedLazyPayload extends AbstractDataBlock implements ContainerBasedPayload {

   private long totalSize;

   private final FieldFunctionStack context;

   /**
    * Creates a new {@link FieldBasedLazyPayload}.
    * 
    * @param id
    *           The {@link DataBlockId} of the payload
    * @param offset
    *           The start {@link MediumOffset} of the payload
    * @param totalSize
    *           The total payload size, if already known, or {@link DataBlockDescription#UNKNOWN_SIZE} if not yet known
    * @param dataBlockReader
    *           The {@link DataBlockReader} to be used parsing the content of the payload
    * @param context
    *           The current {@link FieldFunctionStack} context needed for parsing
    */
   public ContainerBasedLazyPayload(DataBlockId id, MediumOffset offset, long totalSize,
      DataBlockReader dataBlockReader, FieldFunctionStack context) {
      super(id, null, offset, dataBlockReader);

      Reject.ifNull(context, "context");

      this.context = context;

      this.totalSize = totalSize;

      // The size of the payload is still unknown - There is no other way than to read
      // its children and sum up their sizes...
      if (totalSize == DataBlockDescription.UNKNOWN_SIZE) {
         long summedUpTotalSize = 0;

         AbstractDataBlockIterator<Container> containerIter = getContainerIterator();

         while (containerIter.hasNext()) {
            Container container = containerIter.next();

            summedUpTotalSize += container.getTotalSize();
         }

         this.totalSize = summedUpTotalSize;
      }
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getTotalSize()
    */
   @Override
   public long getTotalSize() {
      return this.totalSize;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.ContainerBasedPayload#getContainerIterator()
    */
   @Override
   public AbstractDataBlockIterator<Container> getContainerIterator() {
      return new PayloadContainerIterator(this, getDataBlockReader(), getMediumReference(), this.context);
   }
}