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
import com.github.jmeta.library.datablocks.api.services.DataBlockAccessor;
import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.TransformationHandler;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.dataformats.api.services.DataFormatRepository;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;
import com.github.jmeta.library.media.api.services.MediaAPI;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.extmanager.api.exceptions.InvalidExtensionException;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.services.ExtensionManager;
import com.github.jmeta.utility.logging.api.services.LoggingConstants;

/**
 *
 */
public class StandardDataBlockAccessor implements DataBlockAccessor {
   // TODO stage2_013: Provide means to set timeouts (read block timeout + identify timeout)

   private static final Logger LOGGER = LoggerFactory.getLogger(StandardDataBlockAccessor.class);

   private static final int DEFAULT_LAZY_FIELD_SIZE = 8192;

   private final Map<Medium<?>, MediumStore> mediumStores = new HashMap<>();

   @Override
   public void setLazyFieldSize(int lazyFieldSize) {

      m_lazyFieldSize = lazyFieldSize;

      for (Iterator<ContainerDataFormat> iterator = m_readers.keySet().iterator(); iterator.hasNext();) {
         ContainerDataFormat nextKey = iterator.next();
         DataBlockReader nextValue = m_readers.get(nextKey);

         nextValue.setMaxFieldBlockSize(lazyFieldSize);
      }
   }

   /**
    * Creates a new {@link StandardDataBlockAccessor}.
    */
   public StandardDataBlockAccessor() {

      extManager = ComponentRegistry.lookupService(ExtensionManager.class);

      m_repository = ComponentRegistry.lookupService(DataFormatRepository.class);
      m_mediumFactory = ComponentRegistry.lookupService(MediaAPI.class);

      List<Extension> extBundles = extManager.getAllExtensions();

      String validatingExtensions = "Validating registered data blocks extensions"
         + LoggingConstants.SUFFIX_TASK;

      LOGGER.info(LoggingConstants.PREFIX_TASK_STARTING + validatingExtensions);

      for (Extension iExtension2 : extBundles) {
         List<DataBlockService> bundleDataBlocksExtensions = iExtension2.getAllServiceProviders(DataBlockService.class);

         for (DataBlockService dataBlocksExtension : bundleDataBlocksExtensions) {
            final ContainerDataFormat extensionDataFormat = dataBlocksExtension.getDataFormat();

            if (extensionDataFormat == null) {
               final String message = "The extension " + dataBlocksExtension
                  + " must not return null for its data format.";
               LOGGER.error(LoggingConstants.PREFIX_TASK_FAILED + LoggingConstants.PREFIX_CRITICAL_ERROR
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

      LOGGER.info(LoggingConstants.PREFIX_TASK_DONE_NEUTRAL + validatingExtensions);
   }

   private void addDataBlockExtensions(Extension iExtension2, DataBlockService dataBlocksExtensions) {

      ContainerDataFormat format = dataBlocksExtensions.getDataFormat();

      final DataFormatSpecification spec = m_repository.getDataFormatSpecification(format);

      if (spec == null) {
         throw new InvalidExtensionException("The extension " + iExtension2.getExtensionId() + " for data format "
            + format + " must have a corresponding registered data format specification for the format.", iExtension2);
      }

      DataBlockReader dataBlockReader = dataBlocksExtensions.getDataBlockReader(spec, m_lazyFieldSize);

      ExtendedDataBlockFactory dataBlockFactory = dataBlocksExtensions.getDataBlockFactory();

      // Set default data block factory
      if (dataBlockFactory == null)
         dataBlockFactory = new StandardDataBlockFactory();

      dataBlockFactory.setMediumFactory(m_mediumFactory);

      m_factories.put(format, dataBlockFactory);

      List<TransformationHandler> transformationHandlers = dataBlocksExtensions.getTransformationHandlers(spec,
         m_factories.get(format));

      if (transformationHandlers == null) {
         throw new InvalidExtensionException(
            "Transformation handlers returned by " + iExtension2.getExtensionId() + " must not be null", iExtension2);
      }

      Map<DataTransformationType, TransformationHandler> transformationHandlersMap = new HashMap<>();

      for (int i = 0; i < transformationHandlers.size(); ++i) {
         TransformationHandler handler = transformationHandlers.get(i);

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
    * @see DataBlockAccessor#getContainerIterator
    */
   @Override
   public AbstractDataBlockIterator<Container> getContainerIterator(Medium<?> medium, List<ContainerDataFormat> dataFormatHints,
      boolean forceMediumReadOnly) {

      Reject.ifNull(dataFormatHints, "dataFormatHints");
      Reject.ifNull(medium, "medium");

      MediumStore mediumStore = m_mediumFactory.createMediumStore(medium);
      mediumStore.open();

      mediumStores.put(medium, mediumStore);

      return new TopLevelContainerIterator(medium, dataFormatHints, forceMediumReadOnly, m_readers, mediumStore);
   }

   @Override
   public AbstractDataBlockIterator<Container> getReverseContainerIterator(Medium<?> medium,
      List<ContainerDataFormat> dataFormatHints, boolean forceMediumReadOnly) {

      Reject.ifNull(dataFormatHints, "dataFormatHints");
      Reject.ifNull(medium, "medium");

      if (!medium.isRandomAccess())
         throw new UnsupportedMediumException("Medium " + medium + " must be a random access medium.");

      MediumStore mediumStore = m_mediumFactory.createMediumStore(medium);
      mediumStore.open();

      mediumStores.put(medium, mediumStore);

      return new TopLevelReverseContainerIterator(medium, dataFormatHints, forceMediumReadOnly, m_readers, mediumStore);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockAccessor#getDataBlockFactory(ContainerDataFormat)
    */
   @Override
   public DataBlockFactory getDataBlockFactory(ContainerDataFormat dataFormat) {

      return m_factories.get(dataFormat);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockAccessor#getTransformationHandlers(ContainerDataFormat)
    */
   @Override
   public Map<DataTransformationType, TransformationHandler> getTransformationHandlers(ContainerDataFormat dataFormat) {

      Reject.ifNull(dataFormat, "dataFormat");

      return m_readers.get(dataFormat).getTransformationHandlers();
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.DataBlockAccessor#setTransformationHandler(ContainerDataFormat,
    *      DataTransformationType, com.github.jmeta.library.datablocks.api.services.TransformationHandler)
    */
   @Override
   public void setTransformationHandler(ContainerDataFormat dataFormat, DataTransformationType transformationType,
      TransformationHandler handler) {

      Reject.ifNull(dataFormat, "dataFormat");

      m_readers.get(dataFormat).setTransformationHandler(transformationType, handler);
   }

   private int m_lazyFieldSize = DEFAULT_LAZY_FIELD_SIZE;

   private final DataFormatRepository m_repository;

   private final MediaAPI m_mediumFactory;

   private final Map<ContainerDataFormat, DataBlockReader> m_readers = new HashMap<>();

   private final Map<ContainerDataFormat, ExtendedDataBlockFactory> m_factories = new HashMap<>();

   private final ExtensionManager extManager;

   /**
    * @see DataBlockAccessor#closeMedium
    */
   @Override
   public void closeMedium(Medium<?> medium) {

      Reject.ifNull(medium, "medium");

      mediumStores.get(medium).close();
   }
}
