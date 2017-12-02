/**
 *
 * ShiftedMediumBlockComparator.java
 *
 * @author Jens
 *
 * @date 28.08.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.util.Comparator;

import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;

/**
 * {@link ShiftedMediumBlockComparator} compares two {@link ShiftedMediumBlock}s to ensure a proper execution order of
 * the associated write operations without any overwrites of existing data.
 *
 * Two {@link ShiftedMediumBlock}s are only equal if being the same object.
 *
 * The left {@link ShiftedMediumBlock} is smaller than the right one - which means it must be executed first - in the
 * following cases:
 * <ul>
 * <li>If both have causing type {@link MediumActionType#INSERT} and are located at the same offset, and the schedule
 * sequence number of the left is smaller than the right one</li>
 * <li>Otherwise if the source region of the right {@link ShiftedMediumBlock} overlaps the target region of the left
 * {@link ShiftedMediumBlock}</li>
 * <li>Otherwise if the source region start offset of the left {@link ShiftedMediumBlock} is smaller than the target
 * region start offset of the right {@link ShiftedMediumBlock}</li>
 * </ul>
 * 
 * In all other cases, the left {@link ShiftedMediumBlock} is considered bigger than the right one.
 */
public class ShiftedMediumBlockComparator implements Comparator<ShiftedMediumBlock> {

   /**
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    * 
    * @return negative int if left < right; positive int if left > right; zero if left.equals(right)
    */
   @Override
   public int compare(ShiftedMediumBlock left, ShiftedMediumBlock right) {

      if (left.equals(right)) {
         return 0;
      }

      MediumAction causingAction1 = left.getCausingAction();
      MediumAction causingAction2 = right.getCausingAction();

      // Preserve schedule order for INSERT actions with same offset
      if (causingAction1.getActionType() == MediumActionType.INSERT
         && causingAction2.getActionType() == MediumActionType.INSERT
         && causingAction1.getRegion().getStartReference().equals(causingAction2.getRegion().getStartReference())) {

         // Important note: INSERTs at the same offset must be sorted DESCENDING by sequence number to ensure
         // the last insert (with the biggest sequence number) is first in the resulting flush plan.
         return Long.compare(causingAction2.getScheduleSequenceNumber(), causingAction1.getScheduleSequenceNumber());
      }

      if (left.getSourceRegion().getOverlappingByteCount(right.getTargetRegion()) != 0
         || left.getSourceRegion().getStartReference().before(right.getTargetRegion().getStartReference())) {
         return -1;
      }

      return 1;
   }

}
