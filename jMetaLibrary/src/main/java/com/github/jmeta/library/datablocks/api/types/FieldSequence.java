/**
 *
 * {@link FieldSequence}.java
 *
 * @author jebert
 *
 * @date 25.06.2011
 */
package com.github.jmeta.library.datablocks.api.types;

import java.util.List;

/**
 * {@link FieldSequence}
 *
 */
public interface FieldSequence extends DataBlock {

	/**
	 * Returns the {@link List} of {@link Field}s belonging to this {@link Payload}.
	 * The returned {@link List} might be empty if the {@link Payload} does not
	 * contain {@link Field}s as children.
	 *
	 * @return the {@link List} of {@link Field}s that build this {@link Payload}.
	 *         Might return an empty {@link List} if there are no child
	 *         {@link Field}s.
	 */
	List<Field<?>> getFields();
}
