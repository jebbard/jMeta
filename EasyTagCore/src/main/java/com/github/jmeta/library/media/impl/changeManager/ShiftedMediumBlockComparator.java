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
 * {@link ShiftedMediumBlockComparator}
 *
 */
public class ShiftedMediumBlockComparator implements Comparator<ShiftedMediumBlock> {

   /**
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    * 
    * @return negative int if o1 < o2; positive int if o1 > o2; zero if o1.equals(o2)
    */
   @Override
   public int compare(ShiftedMediumBlock o1, ShiftedMediumBlock o2) {

      if (o1.equals(o2)) {
         return 0;
      }

      MediumAction causingAction1 = o1.getCausingAction();
      MediumAction causingAction2 = o2.getCausingAction();

      // Preserve call order for INSERT actions with same offset
      if (causingAction1.getActionType() == MediumActionType.INSERT
         && causingAction2.getActionType() == MediumActionType.INSERT
         && causingAction1.getRegion().getStartReference().equals(causingAction2.getRegion().getStartReference())) {

         // Important note: INSERTs at the same offset must be sorted DESCENDING by sequence number to ensure
         // the last insert (with the biggest sequence number) is first in the resulting flush plan.

         return Integer.compare(causingAction2.getSequenceNumber(), causingAction1.getSequenceNumber());
      }

      if (o1.getSourceRegion().getOverlappingByteCount(o2.getTargetRegion()) != 0
         || o1.getSourceRegion().getStartReference().before(o2.getTargetRegion().getStartReference())) {
         return -1;
      }

      return 1;
   }

}
