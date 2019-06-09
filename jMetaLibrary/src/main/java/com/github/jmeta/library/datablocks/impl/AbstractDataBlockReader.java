/**
 *
 * {@link AbstractDataBlockReader}.java
 *
 * @author Jens Ebert
 *
 * @date 17.01.2011
 */

package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.CountProvider;
import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.services.ExtendedDataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.SizeProvider;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldSequence;
import com.github.jmeta.library.datablocks.api.types.Footer;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractDataBlockReader} contains the default implementation of
 * {@link DataBlockReader}, i.e. everything that is not depending on whether
 * forward or backward reading happens. Differentiation between forward and
 * backward reading is done by different implementations of
 * {@link #hasEnoughBytesForMagicKey(MediumOffset, MagicKey, long)},
 * {@link #getMagicKeys(DataBlockDescription)} as well as
 * {@link #readContainerWithId(MediumOffset, DataBlockId, Payload, long, int, ContainerContext)}
 * and
 * {@link #readPayload(MediumOffset, DataBlockId, DataBlockId, long, ContainerContext)}.
 */
public abstract class AbstractDataBlockReader implements DataBlockReader {

	private static final Logger LOGGER = LoggerFactory.getLogger(AbstractDataBlockReader.class);

	private static final String LOGGING_BINARY_TO_INTERPRETED_FAILED = "Field conversion from binary to interpreted value failed for field id <%1$s>. Exception see below.";

	private final DataFormatSpecification spec;

	private final ExtendedDataBlockFactory dataBlockFactory;

	private SizeProvider customSizeProvider;

	private CountProvider customCountProvider;

	private final MediumDataProvider mediumDataProvider;

	private final DataBlockEventBus eventBus;

	/**
	 * Creates a new {@link AbstractDataBlockReader}.
	 *
	 * @param spec        The {@link DataFormatSpecification}, must not be null
	 * @param mediumStore TODO
	 * @param eventBus    TODO
	 */
	public AbstractDataBlockReader(DataFormatSpecification spec, MediumStore mediumStore, DataBlockEventBus eventBus) {
		Reject.ifNull(spec, "spec");
		Reject.ifNull(mediumStore, "mediumStore");
		Reject.ifNull(eventBus, "eventBus");

		mediumDataProvider = new MediumDataProvider(mediumStore);

		this.spec = spec;
		this.eventBus = eventBus;
		dataBlockFactory = new StandardDataBlockFactory(mediumDataProvider, spec, eventBus);
	}

	private DataBlockId concreteBlockIdFromGenericId(DataBlockId genericBlockId, Field<?> headerField) {

		String concreteLocalId = "";
		try {
			concreteLocalId = headerField.getInterpretedValue().toString();
		} catch (BinaryValueConversionException e) {
			// Silently ignore: The local id remains by its previous value
			AbstractDataBlockReader.LOGGER.warn(AbstractDataBlockReader.LOGGING_BINARY_TO_INTERPRETED_FAILED,
				headerField.getId());
			AbstractDataBlockReader.LOGGER.error("concreteBlockIdFromGenericId", e);
		}

		String concreteGlobalId = genericBlockId.getGlobalId().replace(genericBlockId.getLocalId(), concreteLocalId);

		return new DataBlockId(genericBlockId.getDataFormat(), concreteGlobalId);
	}

	private DataBlockDescription createUnknownFieldDescription(DataBlockId parentId) {

		DataBlockId unknownBlockId = new DataBlockId(parentId, DataFormatSpecification.UNKNOWN_FIELD_ID);

		FieldProperties<byte[]> unknownFieldProperties = new FieldProperties<>(FieldType.BINARY, new byte[] { 0 }, null,
			null, null, null, null, null, false, DataBlockDescription.UNDEFINED, null);

		return new DataBlockDescription(unknownBlockId, DataFormatSpecification.UNKNOWN_FIELD_ID,
			DataFormatSpecification.UNKNOWN_FIELD_ID, PhysicalDataBlockType.FIELD, new ArrayList<>(),
			unknownFieldProperties, 1, 1, DataBlockDescription.UNDEFINED, DataBlockDescription.UNDEFINED, false, null);
	}

	private long determineActualFieldSize(DataBlockDescription fieldDesc, long remainingDirectParentByteCount,
		MediumOffset reference, int sequenceNumber, ContainerContext containerContext) {

		long actualBlockSize = DataBlockDescription.UNDEFINED;

		final Character terminationCharacter = fieldDesc.getFieldProperties().getTerminationCharacter();

		// Determine termination bytes from termination character - Note that e.g. for
		// ID3v1 there exists the case that
		// fields are terminated and have a fixed size at the same time
		if (!fieldDesc.hasFixedSize() && (terminationCharacter != null)) {
			Charset characterEncoding = containerContext.getCharacterEncodingOf(fieldDesc.getId(), sequenceNumber);

			actualBlockSize = getSizeUpToTerminationBytes(reference, characterEncoding, terminationCharacter,
				remainingDirectParentByteCount);
		}

		if (actualBlockSize == DataBlockDescription.UNDEFINED) {
			actualBlockSize = containerContext.getSizeOf(fieldDesc.getId(), sequenceNumber);
		}

		if (actualBlockSize == DataBlockDescription.UNDEFINED) {
			actualBlockSize = remainingDirectParentByteCount;
		}

		return actualBlockSize;
	}

	/**
	 * Determines the concrete container id for a generic container with the given
	 * id.
	 *
	 * @param currentOffset            The current read {@link MediumOffset}, must
	 *                                 not be null
	 * @param genericContainerId       The generic container's {@link DataBlockId},
	 *                                 must not be null
	 * @param remainingParentByteCount The number of remaining parent bytes at the
	 *                                 given offset or
	 *                                 {@link DataBlockDescription#UNDEFINED} if
	 *                                 unknown
	 * @param sequenceNumber           The sequence number of the container
	 * @param containerContext         The {@link ContainerContext} of the
	 *                                 container, must not be null
	 * @return the concrete container id for a generic container with the given id
	 */
	protected DataBlockId determineConcreteContainerId(MediumOffset currentOffset, DataBlockId genericContainerId,
		long remainingParentByteCount, int sequenceNumber, ContainerContext containerContext) {

		DataBlockDescription desc = spec.getDataBlockDescription(genericContainerId);

		if (desc.isGeneric()) {
			DataBlockId idFieldId = desc.getIdField();

			final DataBlockDescription idFieldDesc = spec.getDataBlockDescription(idFieldId);

			long byteOffset = idFieldDesc.getByteOffsetFromStartOfContainer();

			if (byteOffset == DataBlockDescription.UNDEFINED) {
				throw new IllegalStateException("For generic data block " + genericContainerId
					+ ", the exact offset of its id field in its container parent " + genericContainerId
					+ " must be specified.");
			}

			MediumOffset idFieldReference = currentOffset.advance(byteOffset);

			long actualFieldSize = determineActualFieldSize(idFieldDesc, remainingParentByteCount - byteOffset,
				idFieldReference, 0, containerContext);

			if (actualFieldSize == DataBlockDescription.UNDEFINED) {
				throw new IllegalStateException(
					"Could not determine size of field " + idFieldId + " which stores the id of a generic data block");
			}

			// Read the field that defines the actual id for the container
			Field<?> idField = readField(idFieldReference, idFieldDesc, actualFieldSize, sequenceNumber,
				containerContext);

			return concreteBlockIdFromGenericId(genericContainerId, idField);
		}

		return genericContainerId;
	}

	/**
	 * @return the custom {@link CountProvider} or null if none set
	 */
	protected CountProvider getCustomCountProvider() {
		return customCountProvider;
	}

	/**
	 * @return the custom {@link SizeProvider} or null if none set
	 */
	protected SizeProvider getCustomSizeProvider() {
		return customSizeProvider;
	}

	/**
	 * @return the {@link DataBlockFactory}
	 */
	protected ExtendedDataBlockFactory getDataBlockFactory() {
		return dataBlockFactory;
	}

	/**
	 * Returns the attribute {@link #eventBus}.
	 *
	 * @return the attribute {@link #eventBus}
	 */
	protected DataBlockEventBus getEventBus() {
		return eventBus;
	}

	/**
	 * Gets all magic keys relevant for reading a container with the given
	 * {@link DataBlockDescription}.
	 *
	 * @param containerDesc The container's {@link DataBlockDescription}, must not
	 *                      be null
	 * @return all magic keys relevant for reading a container with the given
	 *         {@link DataBlockDescription}
	 */
	protected abstract List<MagicKey> getMagicKeys(DataBlockDescription containerDesc);

	/**
	 * @return the {@link MediumDataProvider}
	 */
	public MediumDataProvider getMediumDataProvider() {
		return mediumDataProvider;
	}

	/**
	 * Validates and gets the payload's {@link DataBlockDescription} belonging to
	 * the given container's {@link DataBlockDescription}.
	 *
	 * @param containerDesc The container's {@link DataBlockDescription}, must not
	 *                      be null
	 * @return the payload's {@link DataBlockDescription} belonging to the given
	 *         container's {@link DataBlockDescription}
	 */
	protected DataBlockDescription getPayloadDescription(DataBlockDescription containerDesc) {
		List<DataBlockDescription> payloadDescs = containerDesc
			.getChildDescriptionsOfType(PhysicalDataBlockType.FIELD_BASED_PAYLOAD);

		payloadDescs.addAll(containerDesc.getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD));

		if (payloadDescs.size() != 1) {
			throw new IllegalStateException(
				"For container parents, there must be a single data block of type FIELD_BASED_PAYLOAD or CONTAINER_BASED_PAYLOAD");
		}

		return payloadDescs.get(0);
	}

	private long getSizeUpToTerminationBytes(MediumOffset reference, Charset charset, Character terminationCharacter,
		long remainingDirectParentByteCount) {
		FieldTerminationFinder finder = new FieldTerminationFinder();

		return finder.getSizeUntilTermination(charset, terminationCharacter,
			mediumDataProvider.createFieldDataProvider(reference), remainingDirectParentByteCount,
			reference.getMedium().getMaxReadWriteBlockSizeInBytes());
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#getSpecification()
	 */
	@Override
	public DataFormatSpecification getSpecification() {
		return spec;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#hasContainerWithId(MediumOffset,
	 *      com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      com.github.jmeta.library.datablocks.api.types.Payload, long)
	 */
	@Override
	public boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
		long remainingDirectParentByteCount) {
		Reject.ifNull(reference, "reference");
		Reject.ifNull(id, "id");

		mediumDataProvider.bufferBeforeRead(reference, remainingDirectParentByteCount);

		DataBlockDescription defaultNestedContainerDescription = getSpecification()
			.getDefaultNestedContainerDescription();

		if ((defaultNestedContainerDescription != null) && id.equals(defaultNestedContainerDescription.getId())) {
			return true;
		}

		DataBlockDescription containerDesc = spec.getDataBlockDescription(id);

		List<MagicKey> magicKeys = getMagicKeys(containerDesc);

		for (int i = 0; i < magicKeys.size(); ++i) {
			MagicKey magicKey = magicKeys.get(i);

			// Does the magic key equal the medium bytes at the given reference?
			int magicKeySizeInBytes = magicKey.getByteLength();

			// This container cannot be stored in the parent, as there are not enough bytes
			// in the parent
			// left for its magic key.
			if (!hasEnoughBytesForMagicKey(reference, magicKey, remainingDirectParentByteCount)) {
				return false;
			}

			MediumOffset magicKeyReference = reference.advance(magicKey.getDeltaOffset());

			final ByteBuffer readBytes = readBytes(magicKeyReference, magicKeySizeInBytes);

			if (magicKey.isPresentIn(readBytes)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * Tells whether there are sufficient bytes available for storing the given
	 * {@link MagicKey} at the given offset.
	 *
	 * @param reference                      The {@link MediumOffset}, must not be
	 *                                       null
	 * @param magicKey                       The {@link MagicKey}, must not be null
	 * @param remainingDirectParentByteCount The number of parent bytes remaining in
	 *                                       the medium at the given offset or
	 *                                       {@link DataBlockDescription#UNDEFINED}
	 *                                       if unknown
	 * @return
	 */
	protected abstract boolean hasEnoughBytesForMagicKey(MediumOffset reference, MagicKey magicKey,
		long remainingDirectParentByteCount);

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#identifiesDataFormat(MediumOffset)
	 */
	@Override
	public boolean identifiesDataFormat(MediumOffset reference) {
		Reject.ifNull(reference, "reference");

		List<DataBlockDescription> topLevelContainerDescs = spec.getTopLevelDataBlockDescriptions();

		for (int i = 0; i < topLevelContainerDescs.size(); ++i) {
			DataBlockDescription desc = topLevelContainerDescs.get(i);

			if (hasContainerWithId(reference, desc.getId(), null, DataBlockDescription.UNDEFINED)) {
				return true;
			}
		}

		return false;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readBytes(com.github.jmeta.library.media.api.types.MediumOffset,
	 *      int)
	 */
	@Override
	public ByteBuffer readBytes(MediumOffset reference, int size) {
		return mediumDataProvider.getData(reference, size);
	}

	private Field<?> readField(final MediumOffset reference, DataBlockDescription fieldDesc, long fieldSize,
		int sequenceNumber, ContainerContext containerContext) {

		if (fieldSize > FieldProperties.MAX_FIELD_SIZE) {
			throw new IllegalStateException("Field size must not exceed " + FieldProperties.MAX_FIELD_SIZE + " bytes");
		}

		// This cast is ok as we check for upper bound above
		ByteBuffer fieldBuffer = readBytes(reference, (int) fieldSize);

		return dataBlockFactory.createPersistedField(fieldDesc.getId(), sequenceNumber, null, reference, fieldBuffer,
			containerContext, this);
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readFields(MediumOffset,
	 *      com.github.jmeta.library.dataformats.api.types.DataBlockId, long,
	 *      DataBlock, ContainerContext)
	 */
	@Override
	public List<Field<?>> readFields(MediumOffset reference, DataBlockId parentId, long remainingDirectParentByteCount,
		DataBlock parent, ContainerContext containerContext) {

		DataBlockDescription parentDesc = spec.getDataBlockDescription(parentId);

		List<DataBlockDescription> fieldChildren = parentDesc.getChildDescriptionsOfType(PhysicalDataBlockType.FIELD);

		List<Field<?>> fields = new ArrayList<>();

		MediumOffset currentFieldReference = reference;

		long currentlyRemainingParentByteCount = remainingDirectParentByteCount;

		for (int i = 0; i < fieldChildren.size(); ++i) {
			mediumDataProvider.bufferBeforeRead(currentFieldReference, currentlyRemainingParentByteCount);

			DataBlockDescription fieldDesc = fieldChildren.get(i);

			long actualOccurrences = containerContext.getOccurrencesOf(fieldDesc.getId());

			for (int j = 0; j < actualOccurrences; j++) {
				long fieldSize = determineActualFieldSize(fieldDesc, currentlyRemainingParentByteCount,
					currentFieldReference, j, containerContext);

				Field<?> newField = readField(currentFieldReference, fieldDesc, fieldSize, j, containerContext);

				containerContext.addFieldFunctions(newField);

				fields.add(newField);

				if (currentlyRemainingParentByteCount != DataBlockDescription.UNDEFINED) {
					currentlyRemainingParentByteCount -= fieldSize;
				}

				currentFieldReference = currentFieldReference.advance(fieldSize);
			}
		}

		if (currentlyRemainingParentByteCount > 0) {
			DataBlockDescription unknownFieldDescription = createUnknownFieldDescription(parentId);

			Field<?> unknownField = readField(currentFieldReference, unknownFieldDescription,
				currentlyRemainingParentByteCount, 0, containerContext);

			fields.add(unknownField);
		}

		return fields;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#readHeadersOrFootersWithId(java.lang.Class,
	 *      com.github.jmeta.library.media.api.types.MediumOffset,
	 *      com.github.jmeta.library.dataformats.api.types.DataBlockId,
	 *      com.github.jmeta.library.datablocks.api.types.ContainerContext)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T extends FieldSequence> List<T> readHeadersOrFootersWithId(Class<T> fieldSequenceClass,
		MediumOffset startOffset, DataBlockId headerOrFooterId, ContainerContext containerContext) {
		Reject.ifNull(fieldSequenceClass, "fieldSequenceClass");
		Reject.ifNull(headerOrFooterId, "headerOrFooterId");
		Reject.ifNull(startOffset, "reference");
		Reject.ifFalse(spec.specifiesBlockWithId(headerOrFooterId), "m_spec.specifiesBlockWithId(headerOrFooterId)");

		DataBlockDescription desc = spec.getDataBlockDescription(headerOrFooterId);

		List<T> nextHeadersOrFooters = new ArrayList<>();

		// Get the actual occurrences of this headerId based on the fields of the
		// previous
		// headers
		long actualOccurrences = containerContext.getOccurrencesOf(desc.getId());

		// Read all header occurrences
		for (int i = 0; i < actualOccurrences; i++) {
			long headerOrFooterSize = containerContext.getSizeOf(headerOrFooterId, i);

			List<Field<?>> headerOrFooterFields = readFields(startOffset, headerOrFooterId, headerOrFooterSize, null,
				containerContext);

			if (fieldSequenceClass == Footer.class) {
				nextHeadersOrFooters.add((T) dataBlockFactory.createPersistedFooter(headerOrFooterId, startOffset,
					headerOrFooterFields, i, containerContext, this));
			} else {
				nextHeadersOrFooters.add((T) dataBlockFactory.createPersistedHeader(headerOrFooterId, startOffset,
					headerOrFooterFields, i, containerContext, this));
			}
		}

		return nextHeadersOrFooters;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#setCustomCountProvider(com.github.jmeta.library.datablocks.api.services.CountProvider)
	 */
	@Override
	public void setCustomCountProvider(CountProvider countProvider) {
		Reject.ifNull(countProvider, "countProvider");

		customCountProvider = countProvider;
	}

	/**
	 * @see com.github.jmeta.library.datablocks.api.services.DataBlockReader#setCustomSizeProvider(com.github.jmeta.library.datablocks.api.services.SizeProvider)
	 */
	@Override
	public void setCustomSizeProvider(SizeProvider sizeProvider) {
		Reject.ifNull(sizeProvider, "sizeProvider");

		customSizeProvider = sizeProvider;
	}

}
