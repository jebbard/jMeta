/**
 * {@link StandardLowLevelAPI}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.services.LowLevelAPI;
import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.datablocks.api.services.MediumContainerIterator;
import com.github.jmeta.library.dataformats.api.services.DataFormatRepository;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
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
public class StandardLowLevelAPI implements LowLevelAPI {

	private static final Logger LOGGER = LoggerFactory.getLogger(StandardLowLevelAPI.class);

	private final DataFormatRepository m_repository;

	private final MediaAPI m_mediumFactory;

	private final Map<ContainerDataFormat, DataBlockService> dataBlockServices = new HashMap<>();

	private final ExtensionManager extManager;

	/**
	 * Creates a new {@link StandardLowLevelAPI}.
	 */
	public StandardLowLevelAPI() {

		extManager = ComponentRegistry.lookupService(ExtensionManager.class);

		m_repository = ComponentRegistry.lookupService(DataFormatRepository.class);

		m_mediumFactory = ComponentRegistry.lookupService(MediaAPI.class);

		List<Extension> extBundles = extManager.getAllExtensions();

		String validatingExtensions = "Validating registered data blocks extensions" + LoggingConstants.SUFFIX_TASK;

		StandardLowLevelAPI.LOGGER.info(LoggingConstants.PREFIX_TASK_STARTING + validatingExtensions);

		for (Extension iExtension2 : extBundles) {
			List<DataBlockService> bundleDataBlocksExtensions = iExtension2
				.getAllServiceProviders(DataBlockService.class);

			for (DataBlockService dataBlocksExtension : bundleDataBlocksExtensions) {
				final ContainerDataFormat extensionDataFormat = dataBlocksExtension.getDataFormat();

				if (extensionDataFormat == null) {
					final String message = "The extension " + dataBlocksExtension
						+ " must not return null for its data format.";
					StandardLowLevelAPI.LOGGER.error(LoggingConstants.PREFIX_TASK_FAILED
						+ LoggingConstants.PREFIX_CRITICAL_ERROR + validatingExtensions);
					StandardLowLevelAPI.LOGGER.error(message);
					throw new InvalidExtensionException(message, iExtension2);
				}

				final DataFormatSpecification spec = m_repository.getDataFormatSpecification(extensionDataFormat);

				if (spec == null) {
					throw new InvalidExtensionException(
						"The extension " + iExtension2.getExtensionId() + " for data format " + extensionDataFormat
							+ " must have a corresponding registered data format specification for the format.",
						iExtension2);
				}

				if (dataBlockServices.containsKey(extensionDataFormat)) {
					StandardLowLevelAPI.LOGGER.warn(
						"The custom data blocks extension <%1$s> is NOT REGISTERED and therefore ignored because it provides the data format <%2$s> that is already provided by another custom extension with id <%3$s>.",
						iExtension2.getExtensionId(), extensionDataFormat, iExtension2.getExtensionId());
				}

				else {
					dataBlockServices.put(extensionDataFormat, dataBlocksExtension);
				}
			}
		}

		StandardLowLevelAPI.LOGGER.info(LoggingConstants.PREFIX_TASK_DONE_NEUTRAL + validatingExtensions);
	}

	/**
	 * @see LowLevelAPI#getContainerIterator
	 */
	@Override
	public MediumContainerIterator getContainerIterator(Medium<?> medium) {
		Reject.ifNull(medium, "medium");

		MediumStore mediumStore = m_mediumFactory.createMediumStore(medium);
		mediumStore.open();

		return new StandardMediumContainerIterator(mediumStore, true, new HashSet<>(dataBlockServices.values()));
	}

	@Override
	public MediumContainerIterator getReverseContainerIterator(Medium<?> medium) {
		Reject.ifNull(medium, "medium");

		if (!medium.isRandomAccess()) {
			throw new UnsupportedMediumException("Medium " + medium + " must be a random access medium.");
		}

		MediumStore mediumStore = m_mediumFactory.createMediumStore(medium);
		mediumStore.open();

		return new StandardMediumContainerIterator(mediumStore, false, new HashSet<>(dataBlockServices.values()));
	}
}
