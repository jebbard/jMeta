/**
 *
 * {@link ContainerContext}.java
 *
 * @author Jens Ebert
 *
 * @date 15.04.2019
 *
 */
package com.github.jmeta.library.datablocks.api.types;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.library.datablocks.api.services.CountProvider;
import com.github.jmeta.library.datablocks.api.services.SizeProvider;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction;
import com.github.jmeta.library.dataformats.api.types.CountOf;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.SizeOf;
import com.github.jmeta.library.dataformats.api.types.SummedSizeOf;

/**
 * {@link ContainerContext}
 *
 */
public interface ContainerContext {

	/**
	 * Adds all {@link AbstractFieldFunction}s of the given field to this
	 * {@link ContainerContext}. The concrete values can be later retrieved using
	 * the getters.
	 *
	 * @param field The {@link Field} to add, must not be null
	 */
	void addFieldFunctions(Field<?> field);

	/**
	 * Creates a new {@link ContainerContext} based on this {@link ContainerContext}
	 * and the provided child {@link Container}.
	 *
	 * @param childContainer The child {@link Container}, must not be null
	 * @return A child {@link ContainerContext} for the given child
	 *         {@link Container}
	 */
	ContainerContext createChildContainerContext(Container childContainer);

	/**
	 * Determines the {@link ByteOrder} of the given {@link DataBlockId} with the
	 * given sequence number within the current {@link Container}. The approach is
	 * as follows:
	 * <ul>
	 * <li>If the field has a fixed {@link ByteOrder} according to its
	 * specification, the fixed {@link ByteOrder} is taken</li>
	 * <li>Otherwise the field functions are searched for a field that contains the
	 * {@link ByteOrder} of the data block within this {@link ContainerContext}</li>
	 * <li>If there is none, the same is done hierarchically for the parent
	 * {@link DataBlockId}s of the requested id (sequence number 0)</li>
	 * <li>If there is none, the same steps are done hierarchically using the parent
	 * {@link ContainerContext}</li>
	 * <li>If there is none in the parent container context, the default
	 * {@link ByteOrder} of the {@link DataFormatSpecification} is returned</li>
	 * </ul>
	 *
	 * @param id             The {@link DataBlockId} of the data block, must not be
	 *                       null
	 * @param sequenceNumber The sequence number of the data block, must not be
	 *                       negative
	 * @return The {@link ByteOrder} of the data block or the default
	 *         {@link ByteOrder} of the {@link DataFormatSpecification} if none is
	 *         available
	 */
	ByteOrder getByteOrderOf(DataBlockId id, int sequenceNumber);

	/**
	 * Determines the {@link Charset} of the given {@link DataBlockId} with the
	 * given sequence number within the current {@link Container}. The approach is
	 * as follows:
	 * <ul>
	 * <li>If the field has a fixed {@link Charset} according to its specification,
	 * the fixed {@link Charset} is taken</li>
	 * <li>Otherwise the field functions are searched for a field that contains the
	 * {@link Charset} of the data block within this {@link ContainerContext}</li>
	 * <li>If there is none, the same is done hierarchically for the parent
	 * {@link DataBlockId}s of the requested id (sequence number 0)</li>
	 * <li>If there is none, the same is done hierarchically for the parent
	 * {@link ContainerContext}</li>
	 * <li>If there is none in the parent container context, the default
	 * {@link Charset} of the {@link DataFormatSpecification} is returned</li>
	 * </ul>
	 *
	 * @param id             The {@link DataBlockId} of the data block, must not be
	 *                       null
	 * @param sequenceNumber The sequence number of the data block, must not be
	 *                       negative
	 * @return The {@link Charset} of the data block or the default {@link Charset}
	 *         of the {@link DataFormatSpecification} if none is available
	 */
	Charset getCharacterEncodingOf(DataBlockId id, int sequenceNumber);

	/**
	 * @return the {@link Container} this {@link ContainerContext} belongs to
	 */
	Container getContainer();

	/**
	 * @return the {@link DataFormatSpecification} the container of this context
	 *         belongs to
	 */
	DataFormatSpecification getDataFormatSpecification();

	/**
	 * Determines the number of occurrences of the given {@link DataBlockId} within
	 * the current {@link Container}. The approach is as follows:
	 * <ul>
	 * <li>If there is a custom {@link CountProvider} returning a size that is not
	 * equal to {@link DataBlockDescription#UNDEFINED}, this size is returned</li>
	 * <li>Otherwise if the data block has fixed number of occurrences according to
	 * its specification, this number is returned</li>
	 * <li>Otherwise if the data block is optional, the field functions are searched
	 * for a field that contains the presence of the data block within this
	 * {@link ContainerContext}</li>
	 * <li>Otherwise the {@link CountOf} field functions are searched for a field
	 * that contains the count of the data block within this
	 * {@link ContainerContext}</li>
	 * <li>If there is none, the same is done hierarchically for the parent
	 * {@link ContainerContext}</li>
	 * <li>If there is none in the parent container context,
	 * {@link DataBlockDescription#UNDEFINED} is returned</li>
	 * </ul>
	 *
	 * @param id The {@link DataBlockId} of the data block, must not be null
	 * @return The count of the data block or {@link DataBlockDescription#UNDEFINED}
	 *         if none is available
	 */
	long getOccurrencesOf(DataBlockId id);

	/**
	 * @return the parent {@link ContainerContext} or null if this is a top-level
	 *         {@link ContainerContext}
	 */
	ContainerContext getParentContainerContext();

	/**
	 * Determines the size of the given {@link DataBlockId} with the given sequence
	 * number within the current {@link Container}. The approach is as follows:
	 * <ul>
	 * <li>If there is a custom {@link SizeProvider} returning a size that is not
	 * equal to {@link DataBlockDescription#UNDEFINED}, this size is returned</li>
	 * <li>Otherwise if the data block has fixed size according to its
	 * specification, this size is returned</li>
	 * <li>Otherwise the {@link SizeOf} field functions (single block size) are
	 * searched for a field that contains the size of the data block within this
	 * {@link ContainerContext}</li>
	 * <li>Otherwise the {@link SummedSizeOf} field functions (multiple block size)
	 * are searched for a field that contains the size of the data block within this
	 * {@link ContainerContext}</li>
	 * <li>If there is no single block size function found, it is checked if there
	 * is one for the matching generic id of the target data block</li>
	 * <li>If there is none, the same is done hierarchically for the parent
	 * {@link ContainerContext}</li>
	 * <li>If there is none in the parent container context,
	 * {@link DataBlockDescription#UNDEFINED} is returned</li>
	 * </ul>
	 *
	 * @param id             The {@link DataBlockId} of the data block, must not be
	 *                       null
	 * @param sequenceNumber The sequence number of the data block, must not be
	 *                       negative
	 * @return The size of the data block or {@link DataBlockDescription#UNDEFINED}
	 *         if none is available
	 */
	long getSizeOf(DataBlockId id, int sequenceNumber);

}