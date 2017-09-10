/**
 *
 * {@link IMediumReferenceTest}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package de.je.jmeta.media.impl;

import static org.hamcrest.CoreMatchers.is;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.datatype.DummyMediumCreator;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.impl.StandardMediumReference;
import de.je.util.javautil.common.err.PreconditionException;

/**
 * {@link IMediumReferenceTest} tests the {@link StandardMediumReference} class and its interface
 * {@link IMediumReference}.
 */
public class IMediumReferenceTest {

   private IMedium<?> dummyMedium = DummyMediumCreator.createDefaultDummyFileMedium();

   /**
    * Tests {@link IMediumReference#advance}.
    */
   @Test
   public void advance_byPositiveDistance_referenceCorrectlyAdvanced() {

      final IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 0);

      long[] positiveAdvanceDistances = new long[] { 1, 99, 199, 1029674, 8, };

      long offsetBefore = referenceToTest.getAbsoluteMediumOffset();

      for (int i = 0; i < positiveAdvanceDistances.length; i++) {
         long advance = positiveAdvanceDistances[i];

         IMediumReference advancedReference = referenceToTest.advance(advance);

         // Old reference is unchanged
         Assert.assertThat(referenceToTest.getAbsoluteMediumOffset(), is(offsetBefore));

         // New reference has been advanced by the given count
         long diffDistance = advancedReference.getAbsoluteMediumOffset() - referenceToTest.getAbsoluteMediumOffset();

         Assert.assertThat(advance, is(diffDistance));
      }
   }

   /**
    * Tests {@link IMediumReference#advance}.
    */
   @Test
   public void advance_byNegativeCountSmallerOrEqualToOffset_referenceIsCorrectlyAdvanced() {

      final IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 5);

      long[] negativeAdvanceDistances = new long[] { -1, -5, };

      long offsetBefore = referenceToTest.getAbsoluteMediumOffset();

      for (int i = 0; i < negativeAdvanceDistances.length; i++) {
         long advance = negativeAdvanceDistances[i];

         IMediumReference advancedReference = referenceToTest.advance(advance);

         // Old reference is unchanged
         Assert.assertThat(referenceToTest.getAbsoluteMediumOffset(), is(offsetBefore));

         // New reference has been advanced by the given count
         long diffDistance = advancedReference.getAbsoluteMediumOffset() - referenceToTest.getAbsoluteMediumOffset();

         Assert.assertThat(advance, is(diffDistance));
      }
   }

   /**
    * Tests {@link IMediumReference#advance}.
    */
   @Test
   public void advance_byZero_referenceNotAdvanced() {

      final IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 5);

      long offsetBefore = referenceToTest.getAbsoluteMediumOffset();

      IMediumReference advancedReference = referenceToTest.advance(0);

      // Old reference is unchanged
      Assert.assertThat(referenceToTest.getAbsoluteMediumOffset(), is(offsetBefore));

      // New reference has been advanced by the given count
      Assert.assertThat(advancedReference.getAbsoluteMediumOffset(), is(referenceToTest.getAbsoluteMediumOffset()));
   }

   /**
    * Tests {@link IMediumReference#advance}.
    */
   @Test(expected = PreconditionException.class)
   public void advance_forNegativeCountBiggerThanOffset_throwsException() {

      final IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);

      referenceToTest.advance(-5);
   }

   /**
    * Tests {@link IMediumReference#before(IMediumReference)}.
    */
   @Test
   public void before_forSameReference_returnsFalse() {

      long[] testOffsets = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < testOffsets.length; i++) {
         long offset = testOffsets[i];

         IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.before(referenceToTest), is(false));
      }
   }

   /**
    * Tests {@link IMediumReference#before(IMediumReference)}.
    */
   @Test
   public void before_forReferenceWithLowerOffset_returnsTrue() {

      final IMediumReference relativeReference = new StandardMediumReference(dummyMedium, 5);

      long[] offsetsBeforeRelativeReference = new long[] { 0, 2, 4 };

      for (int i = 0; i < offsetsBeforeRelativeReference.length; i++) {
         long offset = offsetsBeforeRelativeReference[i];

         IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.before(relativeReference), is(true));
      }
   }

   /**
    * Tests {@link IMediumReference#before(IMediumReference)}.
    */
   @Test
   public void before_forReferenceWithHigherOffset_returnsFalse() {

      final IMediumReference relativeReference = new StandardMediumReference(dummyMedium, 5);

      long[] offsetsBehindRelativeReference = new long[] { 6, 10, 99999 };

      for (int i = 0; i < offsetsBehindRelativeReference.length; i++) {
         long offset = offsetsBehindRelativeReference[i];

         IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.before(relativeReference), is(false));
      }
   }

   /**
    * Tests {@link IMediumReference#before(IMediumReference)}.
    */
   @Test(expected = PreconditionException.class)
   public void before_forDifferentMedium_throwsException() {

      final IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);
      final IMediumReference referenceForOtherMedium = new StandardMediumReference(
         new FileMedium(new File("other"), false), 5);

      referenceToTest.before(referenceForOtherMedium);
   }

   /**
    * Tests {@link IMediumReference#behindOrEqual(IMediumReference)}.
    */
   @Test
   public void behindOrEqual_forSameReference_returnsTrue() {

      long[] testOffsets = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < testOffsets.length; i++) {
         long offset = testOffsets[i];

         IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.behindOrEqual(referenceToTest), is(true));
      }
   }

   /**
    * Tests {@link IMediumReference#behindOrEqual(IMediumReference)}.
    */
   @Test
   public void behindOrEqual_forReferenceWithLowerOffset_returnsFalse() {

      final IMediumReference relativeReference = new StandardMediumReference(dummyMedium, 5);

      long[] offsetsBeforeRelativeReference = new long[] { 0, 2, 4 };

      for (int i = 0; i < offsetsBeforeRelativeReference.length; i++) {
         long offset = offsetsBeforeRelativeReference[i];

         IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.behindOrEqual(relativeReference), is(false));
      }
   }

   /**
    * Tests {@link IMediumReference#behindOrEqual(IMediumReference)}.
    */
   @Test
   public void behindOrEqual_forReferenceWithHigherOffset_returnsTrue() {

      final IMediumReference relativeReference = new StandardMediumReference(dummyMedium, 5);

      long[] offsetsBehindRelativeReference = new long[] { 6, 10, 99999 };

      for (int i = 0; i < offsetsBehindRelativeReference.length; i++) {
         long offset = offsetsBehindRelativeReference[i];

         IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.behindOrEqual(relativeReference), is(true));
      }
   }

   /**
    * Tests {@link IMediumReference#behindOrEqual(IMediumReference)}.
    */
   @Test(expected = PreconditionException.class)
   public void behindOrEqual_forDifferentMedium_throwsException() {

      final IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);
      final IMediumReference referenceForOtherMedium = new StandardMediumReference(
         new FileMedium(new File("other"), false), 5);

      referenceToTest.behindOrEqual(referenceForOtherMedium);
   }

   /**
    * Tests {@link IMediumReference#before(IMediumReference)}.
    */
   @Test(expected = PreconditionException.class)
   public void constructor_forNegativeOffset_throwsException() {

      new StandardMediumReference(dummyMedium, -1);
   }

   /**
    * Tests {@link IMediumReference#distanceTo(IMediumReference)}.
    */
   @Test
   public void distanceTo_forSameReference_returnsZero() {

      final IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);

      Assert.assertThat(referenceToTest.distanceTo(referenceToTest), is(0L));
   }

   /**
    * Tests {@link IMediumReference#distanceTo(IMediumReference)}.
    */
   @Test
   public void distanceTo_forReferencesBehind_returnsCorrectPositiveDistance() {

      final IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 2);

      long[] positiveOffsetsBehind = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < positiveOffsetsBehind.length; i++) {
         long offset = positiveOffsetsBehind[i];

         IMediumReference advancedReference = new StandardMediumReference(dummyMedium, offset);

         long differenceTo = referenceToTest.getAbsoluteMediumOffset() - offset;

         Assert.assertThat(referenceToTest.distanceTo(advancedReference), is(differenceTo));
      }
   }

   /**
    * Tests {@link IMediumReference#distanceTo(IMediumReference)}.
    */
   @Test
   public void distanceTo_forReferencesBefore_returnsCorrectNegativeDistance() {

      final IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 2);

      long[] positiveOffsetsBehind = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < positiveOffsetsBehind.length; i++) {
         long offset = positiveOffsetsBehind[i];

         IMediumReference advancedReference = new StandardMediumReference(dummyMedium, offset);

         long differenceTo = referenceToTest.getAbsoluteMediumOffset() - offset;

         Assert.assertThat(advancedReference.distanceTo(referenceToTest), is(-differenceTo));
      }
   }

   /**
    * Tests {@link IMediumReference#distanceTo(IMediumReference)}.
    */
   @Test(expected = PreconditionException.class)
   public void distanceTo_forDifferentMedium_throwsException() {

      final IMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);
      final IMediumReference referenceForOtherMedium = new StandardMediumReference(
         new FileMedium(new File("other"), false), 5);

      referenceToTest.distanceTo(referenceForOtherMedium);
   }

   /**
    * Tests {@link IMediumReference#distanceTo(IMediumReference)}.
    */
   @Test
   public void setAbsoluteMediumOffset_forPositiveOffset_changesOffsetToNewValue() {

      StandardMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);

      referenceToTest.setAbsoluteMediumOffset(0);
      Assert.assertEquals(0, referenceToTest.getAbsoluteMediumOffset());

      referenceToTest.setAbsoluteMediumOffset(32);
      Assert.assertEquals(32, referenceToTest.getAbsoluteMediumOffset());
   }

   /**
    * Tests {@link IMediumReference#before(IMediumReference)}.
    */
   @Test(expected = PreconditionException.class)
   public void setAbsoluteMediumOffset_forNegativeOffset_throwsException() {

      StandardMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);

      referenceToTest.setAbsoluteMediumOffset(-11);
   }

   /**
    * Tests {@link IMediumReference#validateSameMedium(IMediumReference, IMediumReference)}.
    */
   @Test
   public void checkSameMedium_forSameMedium_doesNotThrowException() {

      StandardMediumReference firstReference = new StandardMediumReference(dummyMedium, 0);
      StandardMediumReference secondReference = new StandardMediumReference(dummyMedium, 0);

      IMediumReference.validateSameMedium(firstReference, dummyMedium);
      IMediumReference.validateSameMedium(firstReference, dummyMedium);
      IMediumReference.validateSameMedium(secondReference, dummyMedium);
   }

   /**
    * Tests {@link IMediumReference#validateSameMedium(IMediumReference, IMediumReference)}.
    */
   @Test(expected = PreconditionException.class)
   public void checkSameMedium_forDifferentMedia_throwsException() {

      StandardMediumReference firstReference = new StandardMediumReference(dummyMedium, 0);

      IMediumReference.validateSameMedium(firstReference, DummyMediumCreator.createDefaultDummyInMemoryMedium());
   }

}