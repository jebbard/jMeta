/**
 *
 * InvalidOverlappingWriteException.java
 *
 * @author Jens
 *
 * @date 02.06.2016
 *
 */
package com.github.jmeta.library.media.api.exceptions;

import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.errors.api.services.JMetaRuntimeException;

/**
 * {@link InvalidOverlappingWriteException} is thrown whenever one of the
 * operations
 * {@link MediumStore#replaceData(com.github.jmeta.library.media.api.types.MediumOffset, int, java.nio.ByteBuffer)}
 * or
 * {@link MediumStore#removeData(com.github.jmeta.library.media.api.types.MediumOffset, int)}
 * is invoked before a {@link MediumStore#flush()}, and the operation overlaps
 * with a previous remove or replace operation.
 */
public class InvalidOverlappingWriteException extends JMetaRuntimeException {

	private static final long serialVersionUID = 2210334358957045512L;

	private final MediumAction overlappedExistingAction;
	private final MediumRegion region;
	private final MediumActionType actionType;

	/**
	 * Creates a new {@link InvalidOverlappingWriteException}.
	 * 
	 * @param overlappedExistingAction The existing {@link MediumAction} that was
	 *                                 overlapped
	 * @param actionType               The {@link MediumActionType} of the write
	 *                                 operation that lead to the exception
	 * @param region                   The {@link MediumRegion} of the write
	 *                                 operation that lead to the exception
	 */
	public InvalidOverlappingWriteException(MediumAction overlappedExistingAction, MediumActionType actionType,
		MediumRegion region) {
		super("Invalid overlap between existing action <" + overlappedExistingAction + "> and new region <" + region
			+ "> of action type <" + actionType + ">.", null);
		Reject.ifNull(overlappedExistingAction, "overlappedExistingAction");
		Reject.ifNull(actionType, "actionType");
		Reject.ifNull(region, "region");

		this.overlappedExistingAction = overlappedExistingAction;
		this.region = region;
		this.actionType = actionType;
	}

	/**
	 * Returns the {@link MediumActionType} of the write operation that lead to the
	 * exception
	 * 
	 * @return the {@link MediumActionType} of the write operation that lead to the
	 *         exception
	 */
	public MediumActionType getActionType() {
		return actionType;
	}

	/**
	 * Returns the existing {@link MediumAction} that was overlapped
	 * 
	 * @return The existing {@link MediumAction} that was overlapped
	 */
	public MediumAction getOverlappedExistingAction() {
		return overlappedExistingAction;
	}

	/**
	 * Returns the {@link MediumRegion} of the write operation that lead to the
	 * exception
	 * 
	 * @return the {@link MediumRegion} of the write operation that lead to the
	 *         exception
	 */
	public MediumRegion getRegion() {
		return region;
	}
}
