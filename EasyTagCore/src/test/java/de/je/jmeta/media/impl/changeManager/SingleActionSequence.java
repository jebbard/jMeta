/**
 *
 * SingleActionSequence.java
 *
 * @author Jens
 *
 * @date 11.10.2016
 *
 */
package de.je.jmeta.media.impl.changeManager;

import java.io.PrintStream;
import java.util.Iterator;

import org.junit.Assert;

import de.je.jmeta.media.api.datatype.MediumAction;

/**
 * {@link SingleActionSequence} represents a simple single {@link MediumAction} of arbitrary type.
 */
public class SingleActionSequence extends ExpectedActionSequence {

   private final MediumAction expectedAction;

   /**
    * Creates a new {@link SingleActionSequence}.
    * 
    * @param expectedAction
    *           the single {@link MediumAction} expected in the sequence.
    */
   public SingleActionSequence(MediumAction expectedAction) {
      super(expectedAction.getRegion().getStartReference(), 1, expectedAction.getRegion().getSize());

      this.expectedAction = expectedAction;
   }

   /**
    * @see de.je.jmeta.media.impl.changeManager.ExpectedActionSequence#assertFollowsSequence(java.util.Iterator)
    */
   @Override
   public void assertFollowsSequence(Iterator<MediumAction> actionIter) {
      Assert.assertTrue(actionIter.hasNext());
      MediumAction nextAction = actionIter.next();
      Assert.assertNotNull(nextAction);
      Assert.assertEquals(this.expectedAction, nextAction);
   }

   /**
    * @see de.je.jmeta.media.impl.changeManager.ExpectedActionSequence#dump(java.io.PrintStream)
    */
   @Override
   public void dump(PrintStream stream) {
      dumpMediumAction(stream, expectedAction);
   }

}
