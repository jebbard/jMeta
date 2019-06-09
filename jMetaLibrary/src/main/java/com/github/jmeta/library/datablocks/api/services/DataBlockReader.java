
package com.github.jmeta.library.datablocks.api.services;

import java.nio.ByteBuffer;
import java.util.List;

import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldSequence;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.StandardField;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.MediumOffset;

/**
 * {@link DataBlockReader}
 *
 */
public interface DataBlockReader {

	/**
	 * @return the {@link DataFormatSpecification}
	 */
	DataFormatSpecification getSpecification();

	/**
	 * @param reference
	 * @param id
	 * @param parent
	 * @param remainingDirectParentByteCount
	 * @return true if it has, false otherwise
	 */
	boolean hasContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
		long remainingDirectParentByteCount);

	/**
	 * @param reference
	 * @return true if it identifies, false otherwise
	 */
	boolean identifiesDataFormat(MediumOffset reference);

	/**
	 * @param reference
	 * @param size
	 * @return the {@link ByteBuffer}
	 */
	ByteBuffer readBytes(MediumOffset reference, int size);

	/**
	 * Returns the next {@link Container} with the given {@link DataBlockId} assumed
	 * to be stored starting at the given {@link MediumOffset} or null. If the
	 * {@link Container}s presence is optional, its actual presence is determined
	 */
	Container readContainerWithId(MediumOffset reference, DataBlockId id, Payload parent,
		long remainingDirectParentByteCount, int sequenceNumber, ContainerContext containerContext);

	List<Field<?>> readFields(MediumOffset reference, DataBlockId parentId, long remainingDirectParentByteCount,
		DataBlock parent, ContainerContext containerContext);

	/**
	 * Returns the next {@link Header} instance with the given {@link DataBlockId}
	 * assumed to be stored starting at the given {@link MediumOffset} or null. If
	 * the {@link Header}s presence is optional, its actual presence is determined
	 * using the given previous {@link Header}s. The method returns null if no
	 * {@link Header} with the {@link DataBlockId} is present at the given
	 * {@link MediumOffset}. Note that this can either refer to headers or footers
	 * which is only determined by the isFooter parameter.
	 *
	 * @param startOffset      The {@link MediumOffset} pointing to the location of
	 *                         the assumed {@link Header} in the
	 *                         {@link AbstractMedium}.
	 * @param headerOrFooterId The {@link DataBlockId} of the assumed
	 *                         {@link Header}.
	 * @param containerContext TODO
	 * @return The {@link Header} with the given {@link DataBlockId} with its
	 *         {@link StandardField}s read from the given {@link MediumOffset}.
	 */
	<T extends FieldSequence> List<T> readHeadersOrFootersWithId(Class<T> fieldSequenceClass, MediumOffset startOffset,
		DataBlockId headerOrFooterId, ContainerContext containerContext);

	/**
	 * @param reference
	 * @param id
	 * @param parentId
	 * @param remainingDirectParentByteCount
	 * @param containerContext               TODO
	 * @param context
	 * @return the {@link Payload}
	 */
	Payload readPayload(MediumOffset reference, DataBlockId id, DataBlockId parentId,
		long remainingDirectParentByteCount, ContainerContext containerContext);

	void setCustomCountProvider(CountProvider countProvider);

	void setCustomSizeProvider(SizeProvider sizeProvider);
}