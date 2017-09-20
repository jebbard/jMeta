/**
 *
 * {@link MediumReferenceComparator}.java
 *
 * @author Jens Ebert
 *
 * @date 04.04.2011
 */
package de.je.jmeta.media.impl;

import java.util.Comparator;

import de.je.jmeta.media.api.IMediumReference;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link MediumReferenceComparator} is used for comparing and sorting {@link IMediumReference}s in test cases.
 */
public class MediumReferenceComparator implements Comparator<IMediumReference> {

   /**
    * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
    */
   @Override
   public int compare(IMediumReference left, IMediumReference right) {

      Reject.ifNull(left, "left");
      Reject.ifNull(right, "right");

      if (left == right)
         return 0;

      if (left.equals(right))
         return 0;

      if (left.before(right))
         return -1;

      return 1;
   }
}
