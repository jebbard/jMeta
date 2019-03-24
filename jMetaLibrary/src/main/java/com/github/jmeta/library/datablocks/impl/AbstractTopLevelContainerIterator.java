/**
 * {@link AbstractTopLevelContainerIterator}.java
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
 * {@link AbstractTopLevelContainerIterator} is used to read all {@link Container}s present in a {@link Medium}. It can
 * be operated in two modes: Forward reading (the default mode) where the medium is iterated with increasing offsets and
 * backward reading for reading a medium (mainly: files or byte arrays) from back to front.
 */
public abstract class AbstractTopLevelContainerIterator extends AbstractDataBlockIterator<Container> {

   private MediumOffset currentOffset;

   /**
    * Returns the attribute {@link #mediumStore}.
    *
    * @return the attribute {@link #mediumStore}
    */
   protected MediumStore getMediumStore() {
      return mediumStore;
   }

   private final MediumStore mediumStore;

   private final List<ContainerDataFormat> precedenceList = new ArrayList<>();

   private final Map<ContainerDataFormat, DataBlockReader> readerMap = new LinkedHashMap<>();

   private final Map<DataBlockId, Integer> nextSequenceNumber = new HashMap<>();

   /**
    * Creates a new {@link AbstractTopLevelContainerIterator}.
    *
    * @param medium
    *           The {@link Medium} to iterate
    * @param readers
    *           All {@link DataBlockReader}s per supported {@link ContainerDataFormat}
    * @param mediumStore
    *           The {@link MediumStore} used to read from the {@link Medium}
    */
   public AbstractTopLevelContainerIterator(Medium<?> medium, Map<ContainerDataFormat, DataBlockReader> readers,
      MediumStore mediumStore) {
      Reject.ifNull(medium, "medium");
      Reject.ifNull(readers, "readers");
      Reject.ifNull(mediumStore, "mediumFactory");

      readerMap.putAll(readers);
      this.mediumStore = mediumStore;
      currentOffset = getReadStartOffset();

      precedenceList.addAll(new ArrayList<>(readerMap.keySet()));
      setMedium(medium);
   }

   public abstract long getBytesToAdvanceToNextContainer(Container currentContainer);

   public abstract MediumOffset getReadStartOffset();

   /**
    * @see java.io.Closeable#close()
    */
   @Override
   public void close() throws IOException {
      mediumStore.close();
   }

   /**
    * Returns the attribute {@link #currentOffset}.
    *
    * @return the attribute {@link #currentOffset}
    */
   protected MediumOffset getCurrentOffset() {
      return currentOffset;
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

      if (precedenceList.isEmpty()) {
         return null;
      }

      for (Iterator<ContainerDataFormat> iterator = precedenceList.iterator(); iterator.hasNext();) {
         ContainerDataFormat dataFormat = iterator.next();
         DataBlockReader reader = readerMap.get(dataFormat);

         if (reader.identifiesDataFormat(reference)) {
            return dataFormat;
         }
      }

      return null;
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

      DataBlockReader reader = readerMap.get(dataFormat);

      List<DataBlockDescription> containerDescs = reader.getSpecification().getTopLevelDataBlockDescriptions();

      for (int i = 0; i < containerDescs.size(); ++i) {
         DataBlockDescription containerDesc = containerDescs.get(i);

         DataBlockId containerId = containerDesc.getId();

         int sequenceNumber = 0;

         if (nextSequenceNumber.containsKey(containerId)) {
            sequenceNumber = nextSequenceNumber.get(containerId);
         }

         Container container = readContainerWithId(reader, currentOffset, containerId, sequenceNumber);

         nextSequenceNumber.put(containerId, sequenceNumber + 1);

         if (container != null) {
            currentOffset = currentOffset.advance(getBytesToAdvanceToNextContainer(container));

            return container;
         }
      }

      return null;
   }

   private Container readContainerWithId(DataBlockReader reader, MediumOffset currentMediumOffset,
      DataBlockId containerId, int sequenceNumber) {
      return reader.readContainerWithId(currentMediumOffset, containerId, null, DataBlockDescription.UNDEFINED, null,
         sequenceNumber);
   }

   private void setMedium(Medium<?> medium) {

      for (Iterator<ContainerDataFormat> iterator = readerMap.keySet().iterator(); iterator.hasNext();) {
         ContainerDataFormat dataFormat = iterator.next();
         DataBlockReader reader = readerMap.get(dataFormat);
         reader.setMediumCache(mediumStore);
      }
   }
}
