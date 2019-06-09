/**
 * {@link ContainerIterator}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.api.services;

import java.util.Iterator;

import com.github.jmeta.library.datablocks.api.exceptions.UnknownDataFormatException;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.DataBlock;

/**
 * An {@link Iterator} used for iterating over {@link DataBlock}s. This
 * {@link Iterator} will throw an {@link UnsupportedOperationException} when the
 * #remove() method is called.
 *
 * The #next() method might throw an {@link UnknownDataFormatException} which is
 * a {@link RuntimeException}.
 */
public interface ContainerIterator extends Iterator<Container> {

	@Override
	void remove();
}
