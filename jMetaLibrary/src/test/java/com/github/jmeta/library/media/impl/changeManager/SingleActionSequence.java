/**
 *
 * SingleActionSequence.java
 *
 * @author Jens
 *
 * @date 11.10.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.io.PrintStream;
import java.util.Iterator;

import org.junit.Assert;

import com.github.jmeta.library.media.api.types.MediumAction;

/**
 * {@link SingleActionSequence} represents a simple single {@link MediumAction}
 * of arbitrary type.
 */
public class SingleActionSequence extends ExpectedActionSequence {

	private final MediumAction expectedAction;

	/**
	 * Creates a new {@link SingleActionSequence}.
	 * 
	 * @param expectedAction the single {@link MediumAction} expected in the
	 *                       sequence.
	 */
	public SingleActionSequence(MediumAction expectedAction) {
		super(expectedAction.getRegion().getStartOffset(), 1, expectedAction.getRegion().getSize());

		this.expectedAction = expectedAction;
	}

	/**
	 * @see com.github.jmeta.library.media.impl.changeManager.ExpectedActionSequence#assertFollowsSequence(java.util.Iterator)
	 */
	@Override
	public void assertFollowsSequence(Iterator<MediumAction> actionIter) {
		Assert.assertTrue(actionIter.hasNext());
		MediumAction nextAction = actionIter.next();
		Assert.assertNotNull(nextAction);
		Assert.assertEquals(expectedAction, nextAction);
	}

	/**
	 * @see com.github.jmeta.library.media.impl.changeManager.ExpectedActionSequence#dump(java.io.PrintStream)
	 */
	@Override
	public void dump(PrintStream stream) {
		ExpectedActionSequence.dumpMediumAction(stream, expectedAction);
	}

}
