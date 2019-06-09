/**
 *
 * {@link DataBlockCrossReference}.java
 *
 * @author Jens Ebert
 *
 * @date 14.06.2018
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link DataBlockCrossReference} allows to assign a symbolic name to a data
 * block and cross-reference this block from another one using this symbolic
 * name, e.g. for {@link AbstractFieldFunction}s. This eliminates the need to
 * reference the complete global id (and thus, duplicating it) while it is
 * actually built up from individual segments only. Furthermore references can
 * already be created and used before the referenced block is built and created
 * and its concrete id is yet unknown.
 */
public class DataBlockCrossReference {

	private final String refId;
	private DataBlockId referencedId;

	/**
	 * Creates a new {@link DataBlockCrossReference}.
	 *
	 * @param refId The reference's id, must be unique in the current build process
	 */
	public DataBlockCrossReference(String refId) {
		Reject.ifNull(refId, "refId");

		this.refId = refId;
	}

	/**
	 * @return The referenced {@link DataBlockId} if already known or null
	 */
	public DataBlockId getId() {
		return referencedId;
	}

	/**
	 * @return The reference's id
	 */
	public String getRefId() {
		return refId;
	}

	/**
	 * @return true if the referenced {@link DataBlockId} is already resolved, false
	 *         otherwise
	 */
	public boolean isResolved() {
		return referencedId != null;
	}

	/**
	 * Resolves the referenced target {@link DataBlockId} once known.
	 *
	 * @param referencedId The referenced target {@link DataBlockId}, must not be
	 *                     null.
	 */
	public void resolve(DataBlockId referencedId) {
		Reject.ifNull(referencedId, "referencedId");

		this.referencedId = referencedId;
	}
}
