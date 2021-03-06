/**
 * {@link LowLevelAPI}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:11 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.services;

import java.util.Iterator;
import java.util.List;

import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.Medium;

/**
 * This interface is the starting point when accessing the {@link DataBlock}s of
 * a {@link AbstractMedium}. For any existing {@link AbstractMedium}, it returns
 * an {@link Iterator} that allows to iterate the top-level {@link DataBlock}s
 * of that {@link AbstractMedium}.
 */
public interface LowLevelAPI {

	/**
	 * Returns the {@link Iterator} for retrieving all the top-level
	 * {@link DataBlock}s in the given {@link AbstractMedium}. Optionally, a
	 * {@link List} of expected {@link ContainerDataFormat}s can be specified to
	 * support the library to identify the {@link ContainerDataFormat}s faster.
	 *
	 * @param medium              the {@link AbstractMedium} for which to get the
	 *                            top-level {@link DataBlock}s.
	 * @return the {@link Iterator} for iterating all the top-level
	 *         {@link DataBlock}s of the {@link AbstractMedium}.
	 */
	MediumContainerIterator getContainerIterator(Medium<?> medium);

	/**
	 * @param medium
	 * @return a reverse {@link ContainerIterator}
	 */
	MediumContainerIterator getReverseContainerIterator(Medium<?> medium);
}
