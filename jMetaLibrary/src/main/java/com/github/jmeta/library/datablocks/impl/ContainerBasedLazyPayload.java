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
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link ContainerBasedLazyPayload} is the default implementation of {@link ContainerBasedPayload}. It lazily reads
 * child containers by providing a corresponding iterator when first requested.
 */
public class ContainerBasedLazyPayload extends AbstractDataBlock implements ContainerBasedPayload {

   private long totalSize;
   private DataBlockReader reader;

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Payload#initSize(long)
    */
   @Override
   public void initSize(long totalSize) {
      this.totalSize = totalSize;

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
    * Creates a new {@link ContainerBasedLazyPayload}.
    *
    * @param id
    * @param spec
    */
   public ContainerBasedLazyPayload(DataBlockId id, DataFormatSpecification spec) {
      super(id, spec);
   }

   public ContainerBasedLazyPayload(DataBlockId id, DataFormatSpecification spec, DataBlockReader reader) {
      super(id, spec);
      this.reader = reader;
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
