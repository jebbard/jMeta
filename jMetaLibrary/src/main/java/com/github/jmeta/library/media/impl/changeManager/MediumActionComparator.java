/**
 *
 * MediumActionComparator.java
 *
 * @author Jens
 *
 * @date 23.05.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.util.Comparator;

import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediumActionComparator} compares two {@link MediumAction} objects.
 *
 * First of all, this comparator returns 0 if and only if the compared
 * {@link MediumAction}s are equal according to
 * {@link MediumAction#equals(Object)}.
 *
 * A {@link MediumAction} X is smaller than another {@link MediumAction} Y, if
 * one of the following is true:
 * <ul>
 * <li>The {@link MediumOffset} of X is smaller than the {@link MediumOffset} of
 * Y, or</li>
 * <li>If the {@link MediumOffset} of X is equal to the {@link MediumOffset} of
 * Y, we have following cases:</li>
 * <li>Case 1: If the left {@link MediumAction} is an
 * {@link MediumActionType#INSERT} action and the right is not: X is smaller
 * than Y. That is: INSERTs at the same offset as Non-INSERTs are always
 * executed first</li>
 * <li>Case 2: If the left {@link MediumAction} is an
 * {@link MediumActionType#INSERT} action and the right also is: X is smaller
 * than Y iff the schedule sequence number of X is smaller than the schedule
 * sequence number of Y</li>
 * <li>Case 3: If the left {@link MediumAction} is not an
 * {@link MediumActionType#INSERT} action: X is smaller than Y iff the schedule
 * sequence number of X is smaller than the schedule sequence number of Y</li>
 * </ul>
 *
 * In any other case, X is bigger then Y.
 *
 * IMPORTANT: Do not change this {@link Comparator} unless you know what you are
 * doing! Especially beware of the following:
 * <ul>
 * <li>It must only return 0 if the two {@link MediumAction}s are equal</li>
 * <li>This especially means: After the initial equality check is false, you
 * MUST NOT return 0 in any case; E.g. do not use Long.compareTo() for e.g.
 * sequence numbers or offsets!</li>
 * <li>The comparator must by "antisymmetric", i.e. if a &lt; b, then also b
 * &gt; a and !(b &lt; a)</li>
 * <li>The comparator must by "transitive", i.e. if a &lt; b, and b &lt; c, then
 * also a &lt; c</li>
 * </ul>
 */
public class MediumActionComparator implements Comparator<MediumAction> {

	/**
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(MediumAction left, MediumAction right) {

		MediumOffset leftStartReference = left.getRegion().getStartOffset();
		MediumOffset rightStartReference = right.getRegion().getStartOffset();
		Reject.ifFalse(leftStartReference.getMedium().equals(rightStartReference.getMedium()),
			"leftStartReference.getMedium().equals(rightStartReference.getMedium())");

		// Ensure contract of compare is fulfilled
		if (left.equals(right)) {
			return 0;
		}

		// Important NOTE: You must never return 0 in the remaining lines at all! The
		// point is that TreeSet and co.
		// use compareTo to check for equality, i.e. if it returns 0 it considers two
		// elements equal. So only -1 or 1
		// must be returned in the remainder of this method!

		if (leftStartReference.equals(rightStartReference)) {

			// INSERTs always must be sorted before REMOVEs or REPLACEs, no matter what
			// their schedule sequence number is
			if ((left.getActionType() != MediumActionType.INSERT)
				&& (right.getActionType() == MediumActionType.INSERT)) {
				return 1;
			} else if ((left.getActionType() == MediumActionType.INSERT)
				&& (right.getActionType() != MediumActionType.INSERT)) {
				return -1;
			} else if (left.getScheduleSequenceNumber() < right.getScheduleSequenceNumber()) {
				return -1;
			} else {
				return 1;
			}
		} else {
			if (leftStartReference.before(rightStartReference)) {
				return -1;
			}
			return 1;
		}
	}
}
