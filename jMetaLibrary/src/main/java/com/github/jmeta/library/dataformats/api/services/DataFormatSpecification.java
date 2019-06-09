/**
 * {@link DataFormatSpecification}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:10 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.services;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * Represents the complete specification of a single
 * {@link ContainerDataFormat}. The specification contains all
 * {@link DataBlockDescription}s for the {@link ContainerDataFormat}.
 */
public interface DataFormatSpecification {

	/**
	*
	*/
	String UNKNOWN_FIELD_ID = "<<++**UNKNOWN_FIELD**++>>";

	/**
	 * @return all {@link AbstractFieldFunction}s specified in this
	 *         {@link DataFormatSpecification}, mapped to the target block id they
	 *         reference
	 */
	Map<DataBlockId, List<AbstractFieldFunction<?>>> getAllFieldFunctionsByTargetId();

	/**
	 * @param id
	 * @return the {@link DataBlockDescription} for the given id
	 */
	DataBlockDescription getDataBlockDescription(DataBlockId id);

	/**
	 * @return the data format
	 */
	ContainerDataFormat getDataFormat();

	/**
	 * @return the default {@link ByteOrder}
	 */
	ByteOrder getDefaultByteOrder();

	/**
	 * @return the default {@link Charset}
	 */
	Charset getDefaultCharacterEncoding();

	DataBlockDescription getDefaultNestedContainerDescription();

	/**
	 * @param id
	 * @return the generic id
	 */
	DataBlockId getMatchingGenericId(DataBlockId id);

	/**
	 * @return the {@link ByteOrder}s supported
	 */
	List<ByteOrder> getSupportedByteOrders();

	/**
	 * @return the {@link Charset}s supported
	 */
	List<Charset> getSupportedCharacterEncodings();

	/**
	 * @return the top-level data blocks
	 */
	List<DataBlockDescription> getTopLevelDataBlockDescriptions();

	/**
	 * @param id
	 * @return true if the given {@link DataBlockId} is contained in the current
	 *         data format, false otherwise
	 */
	boolean specifiesBlockWithId(DataBlockId id);
}
