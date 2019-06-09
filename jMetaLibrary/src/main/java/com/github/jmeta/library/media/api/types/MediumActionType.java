/**
 *
 * {@link MediumActionType}.java
 *
 * @author Jens
 *
 * @date 17.05.2015
 *
 */
package com.github.jmeta.library.media.api.types;

/**
 * {@link MediumActionType} defines the type of {@link MediumAction}.
 */
public enum MediumActionType {
	/**
	 * The {@link MediumAction} is a remove action of n &gt; 0 bytes on the external
	 * medium
	 */
	REMOVE,
	/**
	 * The {@link MediumAction} is a replace action of n &gt; 0 bytes on the
	 * external medium by m &gt; 0 new bytes ( m != n is possible)
	 */
	REPLACE,
	/**
	 * The {@link MediumAction} is an insert action of n &gt; 0 bytes on the
	 * external medium
	 */
	INSERT,
	/**
	 * The {@link MediumAction} is a read action of n &gt; 0 bytes on the external
	 * medium
	 */
	READ,
	/**
	 * The {@link MediumAction} is a write action of n &gt; 0 bytes on the external
	 * medium
	 */
	WRITE,
	/**
	 * The {@link MediumAction} is a truncate action on the external medium starting
	 * at a given offset (= shortening of the medium). The size of the truncation is
	 * already completely defined by its offset, as it always reaches until the end
	 * of file.
	 */
	TRUNCATE
}
