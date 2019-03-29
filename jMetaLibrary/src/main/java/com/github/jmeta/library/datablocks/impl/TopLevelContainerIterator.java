/**
 * {@link TopLevelContainerIterator}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.datablocks.api.exceptions.UnknownDataFormatException;
import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link TopLevelContainerIterator} is used to read all top-level {@link Container}s present in a {@link Medium}. It
 * can be operated in two modes: Forward reading (the default mode) where the medium is iterated with increasing offsets
 * and backward reading for reading a medium (mainly: files or byte arrays) from back to front.
 */
public class TopLevelContainerIterator extends AbstractDataBlockIterator<Container> {

   private MediumOffset currentOffset;

   private final boolean forwardRead;

   private final MediumStore mediumStore;

   private final List<ContainerDataFormat> dataFormatPrecedence = new ArrayList<>();

   private final Map<ContainerDataFormat, DataBlockReader> readers = new LinkedHashMap<>();

   private final Map<DataBlockId, Integer> nextSequenceNumber = new HashMap<>();

   /**
    * Creates a new {@link TopLevelContainerIterator}.
    *
    * @param medium
    *           The {@link Medium} to iterate, must not be null
    * @param readers
    *           All {@link DataBlockReader}s per supported {@link ContainerDataFormat}, must not be null
    * @param mediumStore
    *           The {@link MediumStore} used to read from the {@link Medium}, must not be null
    * @param forwardRead
    *           true for forward reading, false for backward reading
    */
   public TopLevelContainerIterator(Medium<?> medium, Map<ContainerDataFormat, DataBlockReader> readers,
      MediumStore mediumStore, boolean forwardRead) {
      Reject.ifNull(medium, "medium");
      Reject.ifNull(readers, "readers");
      Reject.ifNull(mediumStore, "mediumFactory");

      this.readers.putAll(readers);
      this.mediumStore = mediumStore;
      this.forwardRead = forwardRead;

      if (this.forwardRead) {
         currentOffset = mediumStore.createMediumOffset(0);
      } else {
         currentOffset = mediumStore.createMediumOffset(medium.getCurrentLength());
      }

      dataFormatPrecedence.addAll(new ArrayList<>(readers.keySet()));
      setMediumStore();
   }

   /**
    * @see java.util.Iterator#hasNext()
    */
   @Override
   public boolean hasNext() {
      if (forwardRead) {
         // NOTE: For streaming media, the offset parameter for MediumStore.isAtEndOfMedium is actually ignored, but
         // the test is always done at the current stream position.
         // Thus, if pre-buffering occurs for a streaming medium, the actual stream position might be already at EOM,
         // but at this.currentOffset, there are still some cached bytes available waiting to be fetched.
         boolean cachedBytesExist = mediumStore.getCachedByteCountAt(currentOffset) > 0;

         if (cachedBytesExist) {
            return true;
         }

         return !mediumStore.isAtEndOfMedium(currentOffset);
      } else {
         return currentOffset.getAbsoluteMediumOffset() != 0;
      }
   }

   /**
    * @see java.util.Iterator#next()
    */
   @Override
   public Container next() {

      Reject.ifFalse(hasNext(), "hasNext()");

      ContainerDataFormat dataFormat = identifyDataFormat(currentOffset);

      if (dataFormat == null) {
         throw new UnknownDataFormatException(currentOffset,
            "Could not identify data format of top-level block at " + currentOffset);
      }

      DataBlockReader reader = readers.get(dataFormat);

      List<DataBlockDescription> containerDescs = reader.getSpecification().getTopLevelDataBlockDescriptions();

      for (int i = 0; i < containerDescs.size(); ++i) {
         DataBlockDescription containerDesc = containerDescs.get(i);

         DataBlockId containerId = containerDesc.getId();

         int sequenceNumber = 0;

         if (nextSequenceNumber.containsKey(containerId)) {
            sequenceNumber = nextSequenceNumber.get(containerId);
         }

         Container container = reader.readContainerWithId(currentOffset, containerId, null,
            DataBlockDescription.UNDEFINED, sequenceNumber, null);

         nextSequenceNumber.put(containerId, sequenceNumber + 1);

         if (container != null) {
            currentOffset = currentOffset.advance(getBytesToAdvanceToNextContainer(container));

            return container;
         }
      }

      return null;
   }

   /**
    * @see java.io.Closeable#close()
    */
   @Override
   public void close() throws IOException {
      mediumStore.close();
   }

   /**
    * Subclasses need to provide the number of bytes to advance based on the given {@link Container} instance. This
    * number might be positive (for forward reading) or negative (for backward reading).
    *
    * @param currentContainer
    * @return
    */
   public long getBytesToAdvanceToNextContainer(Container currentContainer) {
      if (forwardRead) {
         return currentContainer.getTotalSize();
      } else {
         return -currentContainer.getTotalSize();
      }
   }

   /**
    * Identifies the {@link ContainerDataFormat} present at the given {@link MediumOffset}
    *
    * @param reference
    *           The {@link MediumOffset} to start scanning. For forward reading, it is the start offset of an assumed
    *           container, for backward reading, it is the end offset of an assumed container.
    * @return The {@link ContainerDataFormat} identified or null if none could be identified
    */
   private ContainerDataFormat identifyDataFormat(MediumOffset reference) {

      if (dataFormatPrecedence.isEmpty()) {
         return null;
      }

      for (Iterator<ContainerDataFormat> iterator = dataFormatPrecedence.iterator(); iterator.hasNext();) {
         ContainerDataFormat dataFormat = iterator.next();
         DataBlockReader reader = readers.get(dataFormat);

         if (reader.identifiesDataFormat(reference)) {
            return dataFormat;
         }
      }

      return null;
   }

   private void setMediumStore() {
      readers.values().stream().forEach(reader -> reader.setMediumStore(mediumStore));
   }
}
