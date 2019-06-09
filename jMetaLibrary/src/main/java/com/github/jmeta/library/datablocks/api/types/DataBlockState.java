/**
 *
 * {@link DataBlockState}.java
 *
 * @author Jens Ebert
 *
 * @date 18.04.2019
 *
 */
package com.github.jmeta.library.datablocks.api.types;

/**
 * {@link DataBlockState} represents the current state of a {@link DataBlock}.
 */
public enum DataBlockState {

	/**
	 * The {@link DataBlock} has been newly created and is not associated to a
	 * medium yet, thus it also has no offset.
	 */
	NEW,
	/**
	 * The {@link DataBlock} is persisted on a medium and no changes were made by
	 * the user yet compared to the medium state, this includes the
	 * {@link DataBlock} itself as well as all of its descendants. The
	 * {@link DataBlock} also has an offset on the external medium.
	 */
	PERSISTED,
	/**
	 * The {@link DataBlock} is scheduled for insertion on the external medium. That
	 * means it is not yet present on the medium and thus does not have an offset
	 * yet, but it will be persisted with the next flush.
	 */
	INSERTED,
	/**
	 * The {@link DataBlock} is scheduled for removal on the external medium. That
	 * means it is already persisted on the medium and has an offset, but it will be
	 * removed with the next flush.
	 */
	REMOVED,
	/**
	 * The {@link DataBlock} is scheduled for modification on the external medium.
	 * That means it is already persisted on the medium and has an offset, but its
	 * value will be changed with the next flush.
	 */
	MODIFIED,
}
