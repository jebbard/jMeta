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
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link MediumActionComparator} compares two {@link MediumAction} objects.
 * 
 * A {@link MediumAction} X is smaller than another {@link MediumAction} Y, if one of the following is true:
 * <ul>
 * <li>The {@link MediumReference} of X is smaller than the {@link MediumReference} of Y, or</li>
 * <li>If the {@link MediumReference} of X is equal to the {@link MediumReference} of Y, we have following cases:</li>
 * <li>Case 1: If the left {@link MediumAction} is an {@link MediumActionType#INSERT} action and the right is not: X is
 * smaller than Y. That is: INSERTs at the same offset as Non-INSERTs are always executed first</li>
 * <li>Case 2: If the left {@link MediumAction} is an {@link MediumActionType#INSERT} action and the right also is: X is
 * smaller than Y iff the schedule sequence number of X is smaller than the schedule sequence number of Y</li>
 * <li>Case 3: If the left {@link MediumAction} is not an {@link MediumActionType#INSERT} action: X is smaller than Y
 * iff the schedule sequence number of X is smaller than the schedule sequence number of Y</li>
 * </ul>
 * 
 * That said, it does not matter what the values of the other attributes of a {@link MediumAction} are.
 * 
 * Two {@link MediumAction}s are equal if and only if all of their attributes are equal (see implementation of
 * {@link MediumAction#equals(Object)}. Thus a {@link MediumAction} X is bigger than another {@link MediumAction} Y, if
 * it is neither smaller nor equal. That means in case of equal {@link MediumReference}s and equal sequence numbers, X
 * is always considered bigger than Y if any other attribute differs.
 */
public class MediumActionComparator implements Comparator<MediumAction> {

   /**
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   @Override
   public int compare(MediumAction left, MediumAction right) {

      MediumReference leftStartReference = left.getRegion().getStartReference();
      MediumReference rightStartReference = right.getRegion().getStartReference();
      Reject.ifFalse(leftStartReference.getMedium().equals(rightStartReference.getMedium()),
         "leftStartReference.getMedium().equals(rightStartReference.getMedium())");

      // Ensure contract of compare is fulfilled
      if (left.equals(right)) {
         return 0;
      }

      if (leftStartReference.equals(rightStartReference)) {

         // INSERTs always must be sorted before REMOVEs or REPLACEs, no matter what their schedule sequence number is
         if (left.getActionType() == MediumActionType.INSERT && right.getActionType() != MediumActionType.INSERT) {
            return -1;
         }

         // left is smaller than right
         if (left.getScheduleSequenceNumber() < right.getScheduleSequenceNumber()) {
            return -1;
         }

         // In case of the same start offset and sequence numbers, also 1 is returned
         // no matter what value the action type and medium regions have
         // left is bigger than right
         return 1;
      } else if (leftStartReference.before(rightStartReference)) {
         // left is smaller than right
         return -1;
      } else if (leftStartReference.behindOrEqual(rightStartReference)) {
         // left is bigger than right
         return 1;
      }

      throw new IllegalStateException("Impossible to come here");
   }
}
