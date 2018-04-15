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
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

// TODO primeRefactor008: Review, refactor and document TopLevelReverseContainerIterator
/**
 *
 */
public class TopLevelReverseContainerIterator extends AbstractDataBlockIterator<Container> {

   private static final Logger LOGGER = LoggerFactory.getLogger(TopLevelReverseContainerIterator.class);

   /**
    * Creates a new {@link TopLevelReverseContainerIterator}.
    * 
    * @param medium
    * @param dataFormatHints
    * @param forceMediumReadOnly
    * @param readers
    * @param mediumStore
    */
   public TopLevelReverseContainerIterator(Medium<?> medium, List<ContainerDataFormat> dataFormatHints,
      boolean forceMediumReadOnly, Map<ContainerDataFormat, DataBlockReader> readers, MediumStore mediumStore) {
      Reject.ifNull(dataFormatHints, "dataFormatHints");
      Reject.ifNull(medium, "medium");
      Reject.ifNull(readers, "readers");
      Reject.ifNull(mediumStore, "mediumFactory");

      m_readerMap.putAll(readers);
      m_mediumStore = mediumStore;

      setDataFormatHints(dataFormatHints);
      setMedium(medium, forceMediumReadOnly);
      m_currentReference = m_mediumStore.createMediumOffset(medium.getCurrentLength());
   }

   /**
    * @see java.util.Iterator#hasNext()
    */
   @Override
   public boolean hasNext() {

      // Already read the first (=last) container
      return m_currentReference.getAbsoluteMediumOffset() != 0;
   }

   /**
    * @see java.util.Iterator#next()
    */
   @Override
   public Container next() {

      Reject.ifFalse(hasNext(), "hasNext()");

      ContainerDataFormat dataFormat = identifyDataFormat(m_currentReference);

      if (dataFormat == null)
         throw new UnknownDataFormatException(m_currentReference,
            "Could not identify data format of top-level block at " + m_currentReference);

      DataBlockReader reader = m_readerMap.get(dataFormat);

      List<DataBlockDescription> containerDescs = DataBlockDescription
         .getChildDescriptionsOfType(reader.getSpecification(), null, PhysicalDataBlockType.CONTAINER);

      for (int i = 0; i < containerDescs.size(); ++i) {
         DataBlockDescription containerDesc = containerDescs.get(i);

         DataBlockDescription headerOrFooterDesc = m_readerMap.get(dataFormat).getSpecification()
            .getDataBlockDescription(m_theMagicKey.getHeaderOrFooterId());

         Container container = null;

         // The magic key is contained in a footer and needs to be read "backward" (as is usually the case)
         if (headerOrFooterDesc.getPhysicalType().equals(PhysicalDataBlockType.FOOTER))
            container = reader.readContainerWithIdBackwards(m_previousIdentificationReference, containerDesc.getId(),
               null, null, DataBlockDescription.UNKNOWN_SIZE);

         // Otherwise if the backwards read magic key is contained in a header (especially for ID3v1...)
         else
            container = reader.readContainerWithId(m_previousIdentificationReference, containerDesc.getId(), null, null,
               DataBlockDescription.UNKNOWN_SIZE);

         if (container != null) {
            m_currentReference = m_currentReference.advance(-container.getTotalSize());

            return container;
         }
      }

      return null;
   }

   /**
    * @param dataFormatHints
    * @param allFormats
    * @return the list of {@link ContainerDataFormat}s
    */
   private List<ContainerDataFormat> determineDataFormatPrecedence(List<ContainerDataFormat> dataFormatHints,
      Set<ContainerDataFormat> allFormats) {

      return new ArrayList<>(allFormats);
   }

   private ContainerDataFormat identifyDataFormat(MediumOffset reference) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(m_precedenceList, "setDataFormatHints() must have been called before");

      if (m_precedenceList.isEmpty())
         return null;

      for (Iterator<ContainerDataFormat> iterator = m_precedenceList.iterator(); iterator.hasNext();) {
         ContainerDataFormat dataFormat = iterator.next();

         final DataBlockReader dataBlockReader = m_readerMap.get(dataFormat);
         List<DataBlockDescription> topLevelContainerDescs = DataBlockDescription
            .getChildDescriptionsOfType(dataBlockReader.getSpecification(), null, PhysicalDataBlockType.CONTAINER);

         for (int i = 0; i < topLevelContainerDescs.size(); ++i) {
            DataBlockDescription containerDesc = topLevelContainerDescs.get(i);

            if (!containerDesc.getMagicKeys().isEmpty()) {
               List<MagicKey> magicKeys = containerDesc.getMagicKeys();

               for (int j = 0; j < magicKeys.size(); ++j) {
                  MagicKey magicKey = magicKeys.get(j);

                  long offsetForBackwardReading = magicKey.getHeaderOrFooterOffsetForBackwardReading();

                  if (offsetForBackwardReading != MagicKey.NO_BACKWARD_READING
                     && reference.getAbsoluteMediumOffset() + offsetForBackwardReading >= 0) {
                     MediumOffset footerStartReference = reference.advance(offsetForBackwardReading);
                     try {
                        dataBlockReader.cache(footerStartReference, -offsetForBackwardReading);
                     }

                     catch (EndOfMediumException e) {
                        // bytes really read are ignored as the read ByteBuffers.remaining() contains
                        // the read byte count
                        LOGGER.info("End of medium exception occurred during data format identification (see below).");
                        LOGGER.info("Read " + e.getByteCountActuallyRead() + " of " + e.getByteCountTriedToRead()
                           + " bytes tried to read.");
                        LOGGER.error("identifyDataFormat", e);
                     }

                     if (dataBlockReader.identifiesDataFormat(footerStartReference)) {
                        m_theMagicKey = magicKey;
                        m_previousIdentificationReference = footerStartReference;
                        return dataFormat;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private void setDataFormatHints(List<ContainerDataFormat> dataFormatHints) {

      Reject.ifNull(dataFormatHints, "dataFormatHints");

      m_precedenceList.clear();
      m_precedenceList.addAll(determineDataFormatPrecedence(dataFormatHints, m_readerMap.keySet()));
   }

   /**
    * @param medium
    * @param forceMediumReadOnly
    */
   private void setMedium(Medium<?> medium, boolean forceMediumReadOnly) {

      for (Iterator<ContainerDataFormat> iterator = m_readerMap.keySet().iterator(); iterator.hasNext();) {
         ContainerDataFormat dataFormat = iterator.next();
         DataBlockReader reader = m_readerMap.get(dataFormat);
         reader.setMediumCache(m_mediumStore);
      }
   }

   private MediumOffset m_currentReference;

   private MagicKey m_theMagicKey;

   private MediumStore m_mediumStore;

   private final List<ContainerDataFormat> m_precedenceList = new ArrayList<>();

   private MediumOffset m_previousIdentificationReference;

   private final Map<ContainerDataFormat, DataBlockReader> m_readerMap = new LinkedHashMap<>();
}
