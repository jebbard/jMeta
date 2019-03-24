/**
 *
 * {@link BackwardTopLevelContainerIterator}.java
 *
 * @author Jens Ebert
 *
 * @date 24.03.2019
 *
 */
package com.github.jmeta.library.datablocks.impl;

import java.util.Map;

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link BackwardTopLevelContainerIterator}
 *
 */
public class BackwardTopLevelContainerIterator extends AbstractTopLevelContainerIterator {

   /**
    * Creates a new {@link BackwardTopLevelContainerIterator}.
    *
    * @param medium
    * @param readers
    * @param mediumStore
    * @param forwardRead
    */
   public BackwardTopLevelContainerIterator(Medium<?> medium, Map<ContainerDataFormat, DataBlockReader> readers,
      MediumStore mediumStore, boolean forwardRead) {
      super(medium, readers, mediumStore);
   }

   /**
    * @see java.util.Iterator#hasNext()
    */
   @Override
   public boolean hasNext() {
      return getCurrentOffset().getAbsoluteMediumOffset() != 0;
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractTopLevelContainerIterator#getBytesToAdvanceToNextContainer(com.github.jmeta.library.datablocks.api.types.Container)
    */
   @Override
   public long getBytesToAdvanceToNextContainer(Container currentContainer) {
      return -currentContainer.getTotalSize();
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractTopLevelContainerIterator#getReadStartOffset()
    */
   @Override
   public MediumOffset getReadStartOffset() {
      return getMediumStore().createMediumOffset(getMediumStore().getMedium().getCurrentLength());
   }

}
