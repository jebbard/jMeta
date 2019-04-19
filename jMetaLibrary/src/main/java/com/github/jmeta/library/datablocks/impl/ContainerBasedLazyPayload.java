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

import com.github.jmeta.library.datablocks.api.services.ContainerIterator;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerBasedPayload;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlockState;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link ContainerBasedLazyPayload} is the default implementation of {@link ContainerBasedPayload}. It lazily reads
 * child containers by providing a corresponding iterator when first requested.
 */
public class ContainerBasedLazyPayload extends AbstractDataBlock implements ContainerBasedPayload {

   private long totalSize;
   private DataBlockReader reader;

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
    * @param mediumDataProvider
    *           TODO
    * @param context
    *           The current {@link FieldFunctionStack} context needed for parsing
    */
   public ContainerBasedLazyPayload(DataBlockId id, MediumOffset offset, long totalSize,
      DataBlockReader dataBlockReader, ContainerContext containerContext, MediumDataProvider mediumDataProvider,
      DataBlockEventBus eventBus) {
      super(id, 0, offset, null, mediumDataProvider, containerContext, DataBlockState.PERSISTED, eventBus);

      this.totalSize = totalSize;
      reader = dataBlockReader;

      // The size of the payload is still unknown - There is no other way than to read
      // its children and sum up their sizes...
      if (totalSize == DataBlockDescription.UNDEFINED) {
         long summedUpTotalSize = 0;

         ContainerIterator containerIter = getContainerIterator();

         while (containerIter.hasNext()) {
            Container container = containerIter.next();

            summedUpTotalSize += container.getSize();
         }

         this.totalSize = summedUpTotalSize;
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
    * @see com.github.jmeta.library.datablocks.api.types.ContainerBasedPayload#getContainerIterator()
    */
   @Override
   public ContainerIterator getContainerIterator() {
      return new PayloadContainerIterator(this, reader, getOffset());
   }
}
