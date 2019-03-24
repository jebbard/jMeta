/**
 *
 * {@link ForwardTopLevelContainerIterator}.java
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
 * {@link ForwardTopLevelContainerIterator}
 *
 */
public class ForwardTopLevelContainerIterator extends AbstractTopLevelContainerIterator {

   /**
    * Creates a new {@link ForwardTopLevelContainerIterator}.
    *
    * @param medium
    * @param readers
    * @param mediumStore
    * @param forwardRead
    */
   public ForwardTopLevelContainerIterator(Medium<?> medium, Map<ContainerDataFormat, DataBlockReader> readers,
      MediumStore mediumStore, boolean forwardRead) {
      super(medium, readers, mediumStore);
   }

   /**
    * @see java.util.Iterator#hasNext()
    */
   @Override
   public boolean hasNext() {
      // NOTE: For streaming media, the offset parameter for MediumStore.isAtEndOfMedium is actually ignored, but
      // the test is always done at the current stream position.
      // Thus, if pre-buffering occurs for a streaming medium, the actual stream position might be already at EOM,
      // but at this.currentOffset, there are still some cached bytes available waiting to be fetched.
      boolean cachedBytesExist = getMediumStore().getCachedByteCountAt(getCurrentOffset()) > 0;

      if (cachedBytesExist) {
         return true;
      }

      return !getMediumStore().isAtEndOfMedium(getCurrentOffset());
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractTopLevelContainerIterator#getBytesToAdvanceToNextContainer(com.github.jmeta.library.datablocks.api.types.Container)
    */
   @Override
   public long getBytesToAdvanceToNextContainer(Container currentContainer) {
      return currentContainer.getTotalSize();
   }

   /**
    * @see com.github.jmeta.library.datablocks.impl.AbstractTopLevelContainerIterator#getReadStartOffset()
    */
   @Override
   public MediumOffset getReadStartOffset() {
      return getMediumStore().createMediumOffset(0);
   }

}
