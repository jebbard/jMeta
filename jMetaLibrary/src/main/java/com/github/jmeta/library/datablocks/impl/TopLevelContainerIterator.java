/**
 * {@link TopLevelContainerIterator}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jmeta.library.datablocks.api.exceptions.UnknownDataFormatException;
import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link TopLevelContainerIterator} is used to read all {@link Container}s present in a {@link Medium}. It can be
 * operated in two modes: Forward reading (the default mode) where the medium is iterated with increasing offsets and
 * backward reading for reading a medium (mainly: files or byte arrays) from back to front.
 */
public class TopLevelContainerIterator extends AbstractDataBlockIterator<Container> {

   private MediumOffset currentOffset;

   private final boolean forwardRead;

   private final MediumStore mediumStore;

   private final List<ContainerDataFormat> precedenceList = new ArrayList<>();

   private final Map<ContainerDataFormat, DataBlockReader> readerMap = new LinkedHashMap<>();

   /**
    * Creates a new {@link TopLevelContainerIterator}.
    *
    * @param medium
    *           The {@link Medium} to iterate
    * @param readers
    *           All {@link DataBlockReader}s per supported {@link ContainerDataFormat}
    * @param mediumStore
    *           The {@link MediumStore} used to read from the {@link Medium}
    * @param forwardRead
    *           true for forward reading, false for backward reading
    */
   public TopLevelContainerIterator(Medium<?> medium, Map<ContainerDataFormat, DataBlockReader> readers,
      MediumStore mediumStore, boolean forwardRead) {
      Reject.ifNull(medium, "medium");
      Reject.ifNull(readers, "readers");
      Reject.ifNull(mediumStore, "mediumFactory");

      readerMap.putAll(readers);
      this.mediumStore = mediumStore;
      this.forwardRead = forwardRead;

      if (this.forwardRead) {
         currentOffset = mediumStore.createMediumOffset(0);
      } else {
         currentOffset = mediumStore.createMediumOffset(medium.getCurrentLength());
      }

      precedenceList.addAll(new ArrayList<>(readerMap.keySet()));
      setMedium(medium);
   }

   /**
    * @see java.io.Closeable#close()
    */
   @Override
   public void close() throws IOException {
      mediumStore.close();
   }

   private long determineMinimumHeaderSize() {
      List<DataBlockDescription> topLevelDescriptions = readerMap.values().stream()
         .flatMap(dr -> dr.getSpecification().getTopLevelDataBlockDescriptions().stream()).collect(Collectors.toList());

      List<DataBlockDescription> topLevelHeaders = topLevelDescriptions.stream()
         .flatMap(tld -> tld.getOrderedChildren().stream())
         .filter(ch -> ch.getPhysicalType() == PhysicalDataBlockType.HEADER).collect(Collectors.toList());

      return topLevelHeaders.stream().max(Comparator.comparing(DataBlockDescription::getMinimumByteLength)).get()
         .getMinimumByteLength();
   }

   private long getBytesToAdvance(Container container) {
      if (forwardRead) {
         return container.getTotalSize();
      } else {
         return -container.getTotalSize();
      }
   }

   /**
    * @see java.util.Iterator#hasNext()
    */
   @Override
   public boolean hasNext() {
      if (forwardRead) {
         return !mediumStore.isAtEndOfMedium(currentOffset);
      } else {
         return currentOffset.getAbsoluteMediumOffset() != 0;
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

      if (precedenceList.isEmpty()) {
         return null;
      }

      long minHeaderSize = determineMinimumHeaderSize();

      try {
         mediumStore.cache(reference, reference.getMedium().getMaxReadWriteBlockSizeInBytes());
      } catch (EndOfMediumException e) {
         // Silently ignore as this might well happen if a data format min header size is too big for the medium
      }

      for (Iterator<ContainerDataFormat> iterator = precedenceList.iterator(); iterator.hasNext();) {
         ContainerDataFormat dataFormat = iterator.next();
         DataBlockReader reader = readerMap.get(dataFormat);

         if (reader.identifiesDataFormat(reference, forwardRead)) {
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

         Container container = readContainerWithId(reader, currentOffset, containerDesc.getId());

         if (container != null) {
            currentOffset = currentOffset.advance(getBytesToAdvance(container));

            return container;
         }
      }

      return null;
   }

   private Container readContainerWithId(DataBlockReader reader, MediumOffset currentMediumOffset,
      DataBlockId containerId) {
      if (forwardRead) {
         return reader.readContainerWithId(currentMediumOffset, containerId, null, null,
            DataBlockDescription.UNDEFINED);
      } else {
         return reader.readContainerWithIdBackwards(currentMediumOffset, containerId, null, null,
            DataBlockDescription.UNDEFINED);
      }
   }

   private void setMedium(Medium<?> medium) {

      for (Iterator<ContainerDataFormat> iterator = readerMap.keySet().iterator(); iterator.hasNext();) {
         ContainerDataFormat dataFormat = iterator.next();
         DataBlockReader reader = readerMap.get(dataFormat);
         reader.setMediumCache(mediumStore);
      }
   }
}
