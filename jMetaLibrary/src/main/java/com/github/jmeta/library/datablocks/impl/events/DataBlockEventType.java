/**
 *
 * {@link DataBlockEventType}.java
 *
 * @author Jens Ebert
 *
 * @date 19.04.2019
 *
 */
package com.github.jmeta.library.datablocks.impl.events;

import com.github.jmeta.library.datablocks.api.types.DataBlock;

/**
 * {@link DataBlockEventType} represents the actual kind of event that occurred.
 */
public enum DataBlockEventType {
	/**
	 * {@link DataBlock} has been inserted
	 */
	INSERTED,
	/**
	 * {@link DataBlock} has been removed
	 */
	REMOVED,
	/**
	 * {@link DataBlock} has been modified
	 */
	MODIFIED,
	/**
	 * All {@link DataBlock} changes have been flushed; causing {@link DataBlock} is
	 * null here
	 */
	FLUSHED,
	/**
	 * All {@link DataBlock} changes have been reset; causing {@link DataBlock} is
	 * null here
	 */
	RESET_ALL,
	/**
	 * {@link DataBlock} has been persisted
	 */
	PERSISTED,
}
