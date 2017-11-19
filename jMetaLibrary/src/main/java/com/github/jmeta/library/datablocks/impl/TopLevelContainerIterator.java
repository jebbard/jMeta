/**
 * {@link TopLevelContainerIterator}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.exceptions.UnknownDataFormatException;
import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataFormat;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.OLD.IMediumStore_OLD;
import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.services.MediaAPI;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.utility.dbc.api.services.Reject;

// TODO stage2_002: Implement timeout when reading from stream-based medium

/**
 *
 */
public class TopLevelContainerIterator extends AbstractDataBlockIterator<Container> {

   private static final Logger LOGGER = LoggerFactory.getLogger(TopLevelContainerIterator.class);

   /**
    * Creates a new {@link TopLevelContainerIterator}.
    * 
    * @param medium
    * @param dataFormatHints
    * @param forceMediumReadOnly
    * @param readers
    * @param mediumFactory
    */
   public TopLevelContainerIterator(Medium<?> medium, List<DataFormat> dataFormatHints, boolean forceMediumReadOnly,
      Map<DataFormat, DataBlockReader> readers, MediaAPI mediumFactory) {
      Reject.ifNull(dataFormatHints, "dataFormatHints");
      Reject.ifNull(medium, "medium");
      Reject.ifNull(readers, "readers");
      Reject.ifNull(mediumFactory, "mediumFactory");

      m_readerMap.putAll(readers);
      m_mediumFactory = mediumFactory;

      setDataFormatHints(dataFormatHints);
      setMedium(medium, forceMediumReadOnly);
      m_currentReference = m_mediumStore.createMediumReference(0);
   }

   /**
    * @see java.util.Iterator#hasNext()
    */
   @Override
   public boolean hasNext() {

      // TODO stage2_004: Heavy problem with stream based media: When caching, the
      // end of medium is reached SOONER, which is NOT an indication that no more
      // containers are left, but that the whole data of the stream has been cached, before
      // being really accessed...
      return !m_mediumStore.isAtEndOfMedium(m_currentReference);
   }

   /**
    * @see java.util.Iterator#next()
    */
   @Override
   public Container next() {

	   Reject.ifFalse(hasNext(), "hasNext()");

      DataFormat dataFormat = identifyDataFormat(m_currentReference);

      if (dataFormat == null)
         throw new UnknownDataFormatException(m_currentReference,
            "Could not identify data format of next top-level block at " + m_currentReference);

      DataBlockReader reader = m_readerMap.get(dataFormat);

      List<DataBlockDescription> containerDescs = DataBlockDescription
         .getChildDescriptionsOfType(reader.getSpecification(), null, PhysicalDataBlockType.CONTAINER);

      for (int i = 0; i < containerDescs.size(); ++i) {
         DataBlockDescription containerDesc = containerDescs.get(i);

         Container container = reader.readContainerWithId(m_previousIdentificationReference, containerDesc.getId(),
            null, null, DataBlockDescription.UNKNOWN_SIZE);

         if (container != null) {
            m_currentReference = m_currentReference.advance(container.getTotalSize());

            return container;
         }
      }

      return null;
   }

   /**
    * @param dataFormatHints
    * @param allFormats
    * @return the list of {@link DataFormat}s
    */
   private List<DataFormat> determineDataFormatPrecedence(List<DataFormat> dataFormatHints,
      Set<DataFormat> allFormats) {

      // TODO stage2_009: Smarter implementation of determineDataFormatPrecedence using the hints
      return new ArrayList<>(allFormats);
   }

   private DataFormat identifyDataFormat(MediumReference reference) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(m_precedenceList, "setDataFormatHints() must have been called before");

      if (m_precedenceList.isEmpty())
         return null;

      // The whole header size is cached intentionally due to the premise of small headers
      long bytesToRead = m_longestHeaderSize;

      m_previousIdentificationReference = reference;

      try {
         m_readerMap.get(m_precedenceList.iterator().next()).cache(reference, bytesToRead);
      }

      catch (EndOfMediumException e) {
         // bytes really read are ignored as the read ByteBuffers.remaining() contains
         // the read byte count
         LOGGER.info("End of medium exception occurred during data format identification (see below).");
         LOGGER.info("Read " + e.getByteCountActuallyRead() + " of " + e.getByteCountTriedToRead() + " bytes tried to read.");
         LOGGER.error("identifyDataFormat", e);
      }

      for (Iterator<DataFormat> iterator = m_precedenceList.iterator(); iterator.hasNext();) {
         DataFormat dataFormat = iterator.next();
         DataBlockReader reader = m_readerMap.get(dataFormat);

         if (reader.identifiesDataFormat(m_previousIdentificationReference))
            return dataFormat;
      }

      return null;
   }

   private void setDataFormatHints(List<DataFormat> dataFormatHints) {

      Reject.ifNull(dataFormatHints, "dataFormatHints");

      m_precedenceList.clear();
      m_precedenceList.addAll(determineDataFormatPrecedence(dataFormatHints, m_readerMap.keySet()));

      for (Iterator<DataFormat> iterator = m_precedenceList.iterator(); iterator.hasNext();) {
         DataFormat dataFormat = iterator.next();

         final long longestHeaderMinimumSize = m_readerMap.get(dataFormat).getLongestMinimumContainerHeaderSize(null);

         if (m_longestHeaderSize < longestHeaderMinimumSize)
            m_longestHeaderSize = longestHeaderMinimumSize;
      }
   }

   /**
    * @param medium
    * @param forceMediumReadOnly
    */
   private void setMedium(Medium<?> medium, boolean forceMediumReadOnly) {

      for (Iterator<DataFormat> iterator = m_readerMap.keySet().iterator(); iterator.hasNext();) {
         DataFormat dataFormat = iterator.next();
         DataBlockReader reader = m_readerMap.get(dataFormat);
         m_mediumStore = m_mediumFactory.getMediumStore(medium);

         reader.setMediumCache(m_mediumStore);
      }
   }

   private MediumReference m_currentReference;

   private long m_longestHeaderSize = 0;

   private final MediaAPI m_mediumFactory;

   private IMediumStore_OLD m_mediumStore;

   private final List<DataFormat> m_precedenceList = new ArrayList<>();

   private MediumReference m_previousIdentificationReference;

   private final Map<DataFormat, DataBlockReader> m_readerMap = new LinkedHashMap<>();
}
