/**
 * {@link StandardDataBlockAccessor}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;
import com.github.jmeta.library.datablocks.api.services.IDataBlockAccessor;
import com.github.jmeta.library.datablocks.api.services.IDataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.IDataBlockReader;
import com.github.jmeta.library.datablocks.api.services.IDataBlockService;
import com.github.jmeta.library.datablocks.api.services.IExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.ITransformationHandler;
import com.github.jmeta.library.datablocks.api.type.IContainer;
import com.github.jmeta.library.dataformats.api.service.IDataFormatRepository;
import com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.type.DataFormat;
import com.github.jmeta.library.dataformats.api.type.DataTransformationType;
import com.github.jmeta.library.media.api.services.IMediaAPI;
import com.github.jmeta.library.media.api.type.IMedium;
import com.github.jmeta.utility.extmanager.api.IExtension;
import com.github.jmeta.utility.extmanager.api.IExtensionManager;
import com.github.jmeta.utility.extmanager.api.InvalidExtensionException;

import de.je.jmeta.common.ILoggingMessageConstants;
import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.common.registry.ComponentRegistry;

/**
 *
 */
public class StandardDataBlockAccessor implements IDataBlockAccessor {
   // TODO stage2_013: Provide means to set timeouts (read block timeout + identify timeout)

   private static final Logger LOGGER = LoggerFactory.getLogger(StandardDataBlockAccessor.class);

   private static final int DEFAULT_LAZY_FIELD_SIZE = 8192;

   @Override
   public void setLazyFieldSize(int lazyFieldSize) {

      m_lazyFieldSize = lazyFieldSize;

      for (Iterator<DataFormat> iterator = m_readers.keySet().iterator(); iterator.hasNext();) {
         DataFormat nextKey = iterator.next();
         IDataBlockReader nextValue = m_readers.get(nextKey);

         nextValue.setMaxFieldBlockSize(lazyFieldSize);
      }
   }

   /**
    * Creates a new {@link StandardDataBlockAccessor}.
    * 
    * @param registry
    */
   public StandardDataBlockAccessor() {

      extManager = ComponentRegistry.lookupService(IExtensionManager.class);

      m_repository = ComponentRegistry.lookupService(IDataFormatRepository.class);
      m_mediumFactory = ComponentRegistry.lookupService(IMediaAPI.class);

      List<IExtension> extBundles = extManager.getAllExtensions();

      String validatingExtensions = "Validating registered data blocks extensions"
         + ILoggingMessageConstants.SUFFIX_TASK;

      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_STARTING + validatingExtensions);

      for (IExtension iExtension2 : extBundles) {
         List<IDataBlockService> bundleDataBlocksExtensions = iExtension2
            .getAllServiceProviders(IDataBlockService.class);

         for (IDataBlockService dataBlocksExtension : bundleDataBlocksExtensions) {
            final DataFormat extensionDataFormat = dataBlocksExtension.getDataFormat();

            if (extensionDataFormat == null) {
               final String message = "The extension " + dataBlocksExtension
                  + " must not return null for its data format.";
               LOGGER.error(ILoggingMessageConstants.PREFIX_TASK_FAILED + ILoggingMessageConstants.PREFIX_CRITICAL_ERROR
                  + validatingExtensions);
               LOGGER.error(message);
               throw new InvalidExtensionException(message, iExtension2);
            }

            if (m_factories.containsKey(extensionDataFormat) || m_readers.containsKey(extensionDataFormat)) {
               LOGGER.warn(
                  "The custom data blocks extension <%1$s> is NOT REGISTERED and therefore ignored because it provides the data format <%2$s> that is already provided by another custom extension with id <%3$s>.",
                  iExtension2.getExtensionId(), extensionDataFormat, iExtension2.getExtensionId());
            }

            else {
               addDataBlockExtensions(iExtension2, dataBlocksExtension);
            }
         }
      }

      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_DONE_NEUTRAL + validatingExtensions);
   }

   /**
    * @param dataBlocksExtensions
    * @param bundleType
    * @param bundle
    * @param logging
    */
   private void addDataBlockExtensions(IExtension iExtension2, IDataBlockService dataBlocksExtensions) {

      DataFormat format = dataBlocksExtensions.getDataFormat();

      final IDataFormatSpecification spec = m_repository.getDataFormatSpecification(format);

      if (spec == null) {
         throw new InvalidExtensionException("The extension " + iExtension2.getExtensionId() + " for data format "
            + format + " must have a corresponding registered data format specification for the format.", iExtension2);
      }

      IDataBlockReader dataBlockReader = dataBlocksExtensions.getDataBlockReader(spec, m_lazyFieldSize);

      IExtendedDataBlockFactory dataBlockFactory = dataBlocksExtensions.getDataBlockFactory();

      // Set default data block factory
      if (dataBlockFactory == null)
         dataBlockFactory = new StandardDataBlockFactory();

      dataBlockFactory.setMediumFactory(m_mediumFactory);

      m_factories.put(format, dataBlockFactory);

      List<ITransformationHandler> transformationHandlers = dataBlocksExtensions.getTransformationHandlers(spec,
         m_factories.get(format));

      if (transformationHandlers == null) {
         throw new InvalidExtensionException(
            "Transformation handlers returned by " + iExtension2.getExtensionId() + " must not be null", iExtension2);
      }

      Map<DataTransformationType, ITransformationHandler> transformationHandlersMap = new HashMap<>();

      for (int i = 0; i < transformationHandlers.size(); ++i) {
         ITransformationHandler handler = transformationHandlers.get(i);

         transformationHandlersMap.put(handler.getTransformationType(), handler);
      }

      // Set default data block reader
      if (dataBlockReader == null) {
         dataBlockReader = new StandardDataBlockReader(spec, transformationHandlersMap, m_lazyFieldSize);
      }

      dataBlockReader.initDataBlockFactory(dataBlockFactory);
      m_readers.put(format, dataBlockReader);
   }

   /**
    * @see IDataBlockAccessor#getContainerIterator
    */
   @Override
   public AbstractDataBlockIterator<IContainer> getContainerIterator(IMedium<?> medium,
      List<DataFormat> dataFormatHints, boolean forceMediumReadOnly) {

      Reject.ifNull(dataFormatHints, "dataFormatHints");
      Reject.ifNull(medium, "medium");

      return new TopLevelContainerIterator(medium, dataFormatHints, forceMediumReadOnly, m_readers, m_mediumFactory);
   }

   @Override
   public AbstractDataBlockIterator<IContainer> getReverseContainerIterator(IMedium<?> medium,
      List<DataFormat> dataFormatHints, boolean forceMediumReadOnly) {

      Reject.ifNull(dataFormatHints, "dataFormatHints");
      Reject.ifNull(medium, "medium");

      if (!medium.isRandomAccess())
         throw new UnsupportedMediumException("Medium " + medium + " must be a random access medium.");

      return new TopLevelReverseContainerIterator(medium, dataFormatHints, forceMediumReadOnly, m_readers,
         m_mediumFactory);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.IDataBlockAccessor#getDataBlockFactory(DataFormat)
    */
   @Override
   public IDataBlockFactory getDataBlockFactory(DataFormat dataFormat) {

      return m_factories.get(dataFormat);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.IDataBlockAccessor#getTransformationHandlers(DataFormat)
    */
   @Override
   public Map<DataTransformationType, ITransformationHandler> getTransformationHandlers(DataFormat dataFormat) {

      Reject.ifNull(dataFormat, "dataFormat");

      return m_readers.get(dataFormat).getTransformationHandlers();
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.IDataBlockAccessor#setTransformationHandler(DataFormat, DataTransformationType,
    *      com.github.jmeta.library.datablocks.api.services.ITransformationHandler)
    */
   @Override
   public void setTransformationHandler(DataFormat dataFormat, DataTransformationType transformationType,
      ITransformationHandler handler) {

      Reject.ifNull(dataFormat, "dataFormat");

      m_readers.get(dataFormat).setTransformationHandler(transformationType, handler);
   }

   private int m_lazyFieldSize = DEFAULT_LAZY_FIELD_SIZE;

   private final IDataFormatRepository m_repository;

   private final IMediaAPI m_mediumFactory;

   private final Map<DataFormat, IDataBlockReader> m_readers = new HashMap<>();

   private final Map<DataFormat, IExtendedDataBlockFactory> m_factories = new HashMap<>();

   private final IExtensionManager extManager;

   /**
    * @see IDataBlockAccessor#closeMedium
    */
   @Override
   public void closeMedium(IMedium<?> medium) {

      Reject.ifNull(medium, "medium");

      // m_mediumFactory.closeMedium(medium);
   }
}
