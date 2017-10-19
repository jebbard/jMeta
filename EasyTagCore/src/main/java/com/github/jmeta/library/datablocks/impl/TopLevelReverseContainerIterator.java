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

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;
import com.github.jmeta.library.datablocks.api.services.IDataBlockReader;
import com.github.jmeta.library.datablocks.api.type.IContainer;
import com.github.jmeta.library.dataformats.api.type.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.type.DataFormat;
import com.github.jmeta.library.dataformats.api.type.MagicKey;
import com.github.jmeta.library.dataformats.api.type.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.OLD.IMediumStore_OLD;
import com.github.jmeta.library.media.api.exception.EndOfMediumException;
import com.github.jmeta.library.media.api.services.IMediaAPI;
import com.github.jmeta.library.media.api.type.IMedium;
import com.github.jmeta.library.media.api.type.IMediumReference;

import de.je.util.javautil.common.err.Reject;

// TODO primeRefactor008: Review, refactor and document TopLevelReverseContainerIterator
/**
 *
 */
public class TopLevelReverseContainerIterator extends AbstractDataBlockIterator<IContainer> {

   private static final Logger LOGGER = LoggerFactory.getLogger(TopLevelReverseContainerIterator.class);

   /**
    * Creates a new {@link TopLevelReverseContainerIterator}.
    * 
    * @param medium
    * @param dataFormatHints
    * @param forceMediumReadOnly
    * @param readers
    * @param mediumFactory
    * @param logging
    */
   public TopLevelReverseContainerIterator(IMedium<?> medium, List<DataFormat> dataFormatHints,
      boolean forceMediumReadOnly, Map<DataFormat, IDataBlockReader> readers, IMediaAPI mediumFactory) {
      Reject.ifNull(dataFormatHints, "dataFormatHints");
      Reject.ifNull(medium, "medium");
      Reject.ifNull(readers, "readers");
      Reject.ifNull(mediumFactory, "mediumFactory");

      m_readerMap.putAll(readers);
      m_mediumFactory = mediumFactory;

      setDataFormatHints(dataFormatHints);
      setMedium(medium, forceMediumReadOnly);
      m_currentReference = m_mediumStore.createMediumReference(medium.getCurrentLength());
   }

   /**
    * @see java.util.Iterator#hasNext()
    */
   @Override
   public boolean hasNext() {

      // Already read the first (=last) container
      if (m_currentReference.getAbsoluteMediumOffset() == 0)
         return false;

      DataFormat dataFormat = identifyDataFormat(m_currentReference);

      // Do NOT throw UnknownDataFormatException, instead, make the iteration stop "silently"
      return dataFormat != null;
   }

   /**
    * @see java.util.Iterator#next()
    */
   @Override
   public IContainer next() {

	   Reject.ifFalse(hasNext(), "hasNext()");

      DataFormat dataFormat = identifyDataFormat(m_currentReference);

      IDataBlockReader reader = m_readerMap.get(dataFormat);

      List<DataBlockDescription> containerDescs = DataBlockDescription
         .getChildDescriptionsOfType(reader.getSpecification(), null, PhysicalDataBlockType.CONTAINER);

      for (int i = 0; i < containerDescs.size(); ++i) {
         DataBlockDescription containerDesc = containerDescs.get(i);

         DataBlockDescription headerOrFooterDesc = m_readerMap.get(dataFormat).getSpecification()
            .getDataBlockDescription(m_theMagicKey.getHeaderOrFooterId());

         IContainer container = null;

         // The magic key is contained in a footer and needs to be read "backward" (as is usually the case)
         if (headerOrFooterDesc.getPhysicalType().equals(PhysicalDataBlockType.FOOTER))
            container = reader.readContainerWithIdBackwards(m_previousFooterStartReference, containerDesc.getId(), null,
               null, DataBlockDescription.UNKNOWN_SIZE);

         // Otherwise if the backwards read magic key is contained in a header (especially for ID3v1...)
         else
            container = reader.readContainerWithId(m_previousFooterStartReference, containerDesc.getId(), null, null,
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
    * @return the list of {@link DataFormat}s
    */
   private List<DataFormat> determineDataFormatPrecedence(List<DataFormat> dataFormatHints,
      Set<DataFormat> allFormats) {

      return new ArrayList<>(allFormats);
   }

   private DataFormat identifyDataFormat(IMediumReference reference) {

      Reject.ifNull(reference, "reference");
      Reject.ifNull(m_precedenceList, "setDataFormatHints() must have been called before");

      if (m_precedenceList.isEmpty())
         return null;

      for (Iterator<DataFormat> iterator = m_precedenceList.iterator(); iterator.hasNext();) {
         DataFormat dataFormat = iterator.next();

         final IDataBlockReader dataBlockReader = m_readerMap.get(dataFormat);
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
                     IMediumReference footerStartReference = reference.advance(offsetForBackwardReading);
                     try {
                        dataBlockReader.cache(footerStartReference, -offsetForBackwardReading);
                     }

                     catch (EndOfMediumException e) {
                        // bytes really read are ignored as the read ByteBuffers.remaining() contains
                        // the read byte count
                        LOGGER.info("End of medium exception occurred during data format identification (see below).");
                        LOGGER.info("Read " + e.getBytesReallyRead() + " of " + e.getByteCountTriedToRead()
                           + " bytes tried to read.");
                        LOGGER.error("identifyDataFormat", e);
                     }

                     if (dataBlockReader.identifiesDataFormat(footerStartReference)) {
                        m_theMagicKey = magicKey;
                        m_previousFooterStartReference = footerStartReference;
                        return dataFormat;
                     }
                  }
               }
            }
         }
      }

      return null;
   }

   private void setDataFormatHints(List<DataFormat> dataFormatHints) {

      Reject.ifNull(dataFormatHints, "dataFormatHints");

      m_precedenceList.clear();
      m_precedenceList.addAll(determineDataFormatPrecedence(dataFormatHints, m_readerMap.keySet()));
   }

   /**
    * @param medium
    * @param forceMediumReadOnly
    */
   private void setMedium(IMedium<?> medium, boolean forceMediumReadOnly) {

      for (Iterator<DataFormat> iterator = m_readerMap.keySet().iterator(); iterator.hasNext();) {
         DataFormat dataFormat = iterator.next();
         IDataBlockReader reader = m_readerMap.get(dataFormat);
         m_mediumStore = m_mediumFactory.getMediumStore(medium);
         reader.setMediumCache(m_mediumStore);
      }
   }

   private IMediumReference m_currentReference;

   private final IMediaAPI m_mediumFactory;

   private final List<DataFormat> m_precedenceList = new ArrayList<>();

   private IMediumReference m_previousFooterStartReference;

   private MagicKey m_theMagicKey;

   private IMediumStore_OLD m_mediumStore;

   private final Map<DataFormat, IDataBlockReader> m_readerMap = new LinkedHashMap<>();
}
