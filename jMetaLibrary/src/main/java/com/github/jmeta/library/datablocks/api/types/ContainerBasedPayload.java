/**
 *
 * {@link ContainerBasedPayload}.java
 *
 * @author Jens Ebert
 *
 * @date 02.03.2018
 *
 */
package com.github.jmeta.library.datablocks.api.types;

import java.util.Iterator;

import com.github.jmeta.library.datablocks.api.services.ContainerIterator;

/**
 * {@link ContainerBasedPayload} represents a {@link Payload} that solely
 * consists of {@link Container}s only.
 */
public interface ContainerBasedPayload extends Payload {

	/**
	 * Returns the child {@link Container}s contained in this {@link Payload}. If
	 * there are no {@link Container} children, the returned {@link Iterator} will
	 * not return any {@link Container}s.
	 *
	 * @return an {@link Iterator} for retrieving the child {@link Container}s of
	 *         this {@link Payload}. Might return an {@link Iterator} that does not
	 *         return any children if this {@link Payload} has none.
	 */
	ContainerIterator getContainerIterator();

}
