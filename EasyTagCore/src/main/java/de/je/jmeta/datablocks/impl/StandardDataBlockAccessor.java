/**
 * {@link StandardDataBlockAccessor}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package de.je.jmeta.datablocks.impl;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.je.jmeta.common.ILoggingMessageConstants;
import de.je.jmeta.datablocks.AbstractDataBlockIterator;
import de.je.jmeta.datablocks.IContainer;
import de.je.jmeta.datablocks.IDataBlockAccessor;
import de.je.jmeta.datablocks.IDataBlockFactory;
import de.je.jmeta.datablocks.ITransformationHandler;
import de.je.jmeta.datablocks.export.IDataBlockReader;
import de.je.jmeta.datablocks.export.IDataBlocksExtension;
import de.je.jmeta.datablocks.export.IExtendedDataBlockFactory;
import de.je.jmeta.dataformats.DataFormat;
import de.je.jmeta.dataformats.DataTransformationType;
import de.je.jmeta.dataformats.IDataFormatRepository;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.extmanager.export.BundleType;
import de.je.jmeta.extmanager.export.IExtensionBundle;
import de.je.jmeta.extmanager.export.IExtensionManager;
import de.je.jmeta.extmanager.export.InvalidExtensionException;
import de.je.jmeta.media.api.IMediaAPI;
import de.je.jmeta.media.api.IMedium;
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

      List<IExtensionBundle> extBundles = extManager.getRegisteredExtensionBundles();

      Map<DataFormat, IDataBlocksExtension> defaultDataBlocksExtensions = new HashMap<>();
      Map<DataFormat, IDataBlocksExtension> customDataBlocksExtensions = new HashMap<>();

      String validatingExtensions = "Validating registered data blocks extensions"
         + ILoggingMessageConstants.SUFFIX_TASK;

      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_STARTING + validatingExtensions);

      for (int i = 0; i < extBundles.size(); ++i) {
         IExtensionBundle bundle = extBundles.get(i);

         List<IDataBlocksExtension> bundleDataBlocksExtensions = bundle
            .getExtensionsForExtensionPoint(IDataBlocksExtension.class);

         for (int j = 0; j < bundleDataBlocksExtensions.size(); ++j) {
            IDataBlocksExtension dataBlocksExtension = bundleDataBlocksExtensions.get(j);

            final DataFormat extensionDataFormat = dataBlocksExtension.getDataFormat();

            if (extensionDataFormat == null) {
               final String message = "The extension " + dataBlocksExtension
                  + " must not return null for its data format.";
               LOGGER.error(ILoggingMessageConstants.PREFIX_TASK_FAILED + ILoggingMessageConstants.PREFIX_CRITICAL_ERROR
                  + validatingExtensions);
               LOGGER.error(message);
               throw new InvalidExtensionException(message, bundle, dataBlocksExtension);
            }

            // Default bundle data formats may not override existing extension data formats
            if (bundle.getDescription().getType().equals(BundleType.DEFAULT)) {
               if (defaultDataBlocksExtensions.containsKey(extensionDataFormat))
                  LOGGER.warn(
                     "The default data blocks extension <%1$s> is NOT REGISTERED and therefore ignored because it provides the data format <%2$s> that is already provided by another default extension with id <%3$s>.",
                     dataBlocksExtension.getExtensionId(), extensionDataFormat,
                     defaultDataBlocksExtensions.get(extensionDataFormat).getExtensionId());

               else if (customDataBlocksExtensions.containsKey(extensionDataFormat))
                  LOGGER.warn(
                     "The default data blocks extension <%1$s> is NOT REGISTERED and therefore ignored because it provides the data format <%2$s> that is already provided by another custom extension with id <%3$s>.",
                     dataBlocksExtension.getExtensionId(), extensionDataFormat,
                     customDataBlocksExtensions.get(extensionDataFormat).getExtensionId());

               else
                  defaultDataBlocksExtensions.put(extensionDataFormat, dataBlocksExtension);
            }

            // Custom bundle data formats may override existing default extension data formats, but no other custom ones
            else if (bundle.getDescription().getType().equals(BundleType.CUSTOM)) {
               if (customDataBlocksExtensions.containsKey(extensionDataFormat))
                  LOGGER.warn(
                     "The custom data blocks extension <%1$s> is NOT REGISTERED and therefore ignored because it provides the data format <%2$s> that is already provided by another custom extension with id <%3$s>.",
                     dataBlocksExtension.getExtensionId(), extensionDataFormat,
                     customDataBlocksExtensions.get(extensionDataFormat).getExtensionId());

               else
                  customDataBlocksExtensions.put(extensionDataFormat, dataBlocksExtension);
            }
         }

         addDataBlockExtensions(customDataBlocksExtensions, BundleType.CUSTOM, bundle);
         addDataBlockExtensions(defaultDataBlocksExtensions, BundleType.DEFAULT, bundle);
      }

      LOGGER.info(ILoggingMessageConstants.PREFIX_TASK_DONE_NEUTRAL + validatingExtensions);
   }

   /**
    * @param dataBlocksExtensions
    * @param bundleType
    * @param bundle
    * @param logging
    */
   private void addDataBlockExtensions(Map<DataFormat, IDataBlocksExtension> dataBlocksExtensions,
      BundleType bundleType, IExtensionBundle bundle) {

      for (Iterator<DataFormat> iterator = dataBlocksExtensions.keySet().iterator(); iterator.hasNext();) {
         DataFormat format = iterator.next();
         IDataBlocksExtension extension = dataBlocksExtensions.get(format);

         final IDataFormatSpecification spec = m_repository.getDataFormatSpecification(format);

         if (spec == null)
            throw new InvalidExtensionException(
               "The extension " + extension + " for data format " + format
                  + " must have a corresponding registered data format specification for the format.",
               bundle, extension);

         IDataBlockReader dataBlockReader = extension.getDataBlockReader(spec, m_lazyFieldSize);

         IExtendedDataBlockFactory dataBlockFactory = extension.getDataBlockFactory();

         // Set default data block factory
         if (dataBlockFactory == null)
            dataBlockFactory = new StandardDataBlockFactory();

         dataBlockFactory.setMediumFactory(m_mediumFactory);

         if (bundleType.equals(BundleType.DEFAULT) && m_factories.containsKey(format)) {
            LOGGER.warn("The default data format extension <%1$s> is OVERRIDDEN by the data formats extension <%2$s>.",
               extension.getExtensionId(), m_factories.get(format));
         }

         else
            m_factories.put(format, dataBlockFactory);

         List<ITransformationHandler> transformationHandlers = extension.getTransformationHandlers(spec,
            m_factories.get(format));

         if (transformationHandlers == null)
            throw new InvalidExtensionException(
               "Transformation handlers returned by " + extension + " must not be null", bundle, extension);

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

         if (bundleType.equals(BundleType.DEFAULT) && m_readers.containsKey(format)) {
            LOGGER.warn(
               "The default data format reader for <%1$s> is OVERRIDDEN by a reader of data formats extension <%2$s>.",
               extension.getExtensionId(), m_readers.get(format));
         }

         else
            m_readers.put(format, dataBlockReader);
      }
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
    * @see de.je.jmeta.datablocks.IDataBlockAccessor#getDataBlockFactory(DataFormat)
    */
   @Override
   public IDataBlockFactory getDataBlockFactory(DataFormat dataFormat) {

      return m_factories.get(dataFormat);
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlockAccessor#getTransformationHandlers(DataFormat)
    */
   @Override
   public Map<DataTransformationType, ITransformationHandler> getTransformationHandlers(DataFormat dataFormat) {

      Reject.ifNull(dataFormat, "dataFormat");

      return m_readers.get(dataFormat).getTransformationHandlers();
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlockAccessor#setTransformationHandler(DataFormat, DataTransformationType,
    *      de.je.jmeta.datablocks.ITransformationHandler)
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
