/**
 *
 * MediumActionComparator.java
 *
 * @author Jens
 *
 * @date 23.05.2016
 *
 */
package de.je.jmeta.media.impl;

import java.util.Comparator;

import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.datatype.MediumAction;
import de.je.jmeta.media.api.datatype.MediumActionType;

/**
 * {@link MediumActionComparator} compares two {@link MediumAction} objects.
 * 
 * A {@link MediumAction} X is smaller than another {@link MediumAction} Y, if one of the following is true:
 * <ul>
 * <li>The {@link IMediumReference} of X is smaller than the {@link IMediumReference} of Y, or</li>
 * <li>The {@link IMediumReference} of X is equal to the {@link IMediumReference} of Y, but the sequence number of X is
 * smaller than the sequence number of Y</li>
 * </ul>
 * 
 * That said, it does not matter what the values of the other attributes of a {@link MediumAction}, especially the
 * {@link MediumActionType}, is to determine if it is smaller.
 * 
 * Two {@link MediumAction}s are equal if and only if all of their attributes are equal (see implementation of
 * {@link MediumAction#equals(Object)}.
 * 
 * A {@link MediumAction} X is bigger than another {@link MediumAction} Y, if it is neither smaller nor equal. That
 * means in case of equal {@link IMediumReference} and equal sequence number, X is always considered bigger than Y if
 * any other attribute differs.
 */
public class MediumActionComparator implements Comparator<MediumAction> {

   /**
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   @Override
   public int compare(MediumAction left, MediumAction right) {

      IMediumReference leftStartReference = left.getRegion().getStartReference();
      IMediumReference rightStartReference = right.getRegion().getStartReference();

      IMediumReference.validateSameMedium(leftStartReference, rightStartReference.getMedium());

      // Ensure contract of compare is fulfilled
      if (left.equals(right)) {
         return 0;
      }

      if (leftStartReference.equals(rightStartReference)) {

         // left is smaller than right
         if (left.getSequenceNumber() < right.getSequenceNumber()) {
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
