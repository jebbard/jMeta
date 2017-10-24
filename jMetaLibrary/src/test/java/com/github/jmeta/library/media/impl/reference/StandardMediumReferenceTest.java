/**
 *
 * {@link StandardMediumReferenceTest}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package com.github.jmeta.library.media.impl.reference;

import static org.hamcrest.CoreMatchers.is;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.DummyMediumCreator;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * {@link StandardMediumReferenceTest} tests the {@link StandardMediumReference} class and its interface
 * {@link MediumReference}.
 */
public class StandardMediumReferenceTest {

   private Medium<?> dummyMedium = DummyMediumCreator.createDefaultDummyInMemoryMedium();

   /**
    * Tests {@link MediumReference#advance}.
    */
   @Test
   public void advance_byPositiveDistance_referenceCorrectlyAdvanced() {

      final MediumReference referenceToTest = new StandardMediumReference(dummyMedium, 0);

      long[] positiveAdvanceDistances = new long[] { 1, 99, 199, 1029674, 8, };

      long offsetBefore = referenceToTest.getAbsoluteMediumOffset();

      for (int i = 0; i < positiveAdvanceDistances.length; i++) {
         long advance = positiveAdvanceDistances[i];

         MediumReference advancedReference = referenceToTest.advance(advance);

         // Old reference is unchanged
         Assert.assertThat(referenceToTest.getAbsoluteMediumOffset(), is(offsetBefore));

         // New reference has been advanced by the given count
         long diffDistance = advancedReference.getAbsoluteMediumOffset() - referenceToTest.getAbsoluteMediumOffset();

         Assert.assertThat(advance, is(diffDistance));
      }
   }

   /**
    * Tests {@link MediumReference#advance}.
    */
   @Test
   public void advance_byNegativeCountSmallerOrEqualToOffset_referenceIsCorrectlyAdvanced() {

      final MediumReference referenceToTest = new StandardMediumReference(dummyMedium, 5);

      long[] negativeAdvanceDistances = new long[] { -1, -5, };

      long offsetBefore = referenceToTest.getAbsoluteMediumOffset();

      for (int i = 0; i < negativeAdvanceDistances.length; i++) {
         long advance = negativeAdvanceDistances[i];

         MediumReference advancedReference = referenceToTest.advance(advance);

         // Old reference is unchanged
         Assert.assertThat(referenceToTest.getAbsoluteMediumOffset(), is(offsetBefore));

         // New reference has been advanced by the given count
         long diffDistance = advancedReference.getAbsoluteMediumOffset() - referenceToTest.getAbsoluteMediumOffset();

         Assert.assertThat(advance, is(diffDistance));
      }
   }

   /**
    * Tests {@link MediumReference#advance}.
    */
   @Test
   public void advance_byZero_referenceNotAdvanced() {

      final MediumReference referenceToTest = new StandardMediumReference(dummyMedium, 5);

      long offsetBefore = referenceToTest.getAbsoluteMediumOffset();

      MediumReference advancedReference = referenceToTest.advance(0);

      // Old reference is unchanged
      Assert.assertThat(referenceToTest.getAbsoluteMediumOffset(), is(offsetBefore));

      // New reference has been advanced by the given count
      Assert.assertThat(advancedReference.getAbsoluteMediumOffset(), is(referenceToTest.getAbsoluteMediumOffset()));
   }

   /**
    * Tests {@link MediumReference#advance}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void advance_forNegativeCountBiggerThanOffset_throwsException() {

      final MediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);

      referenceToTest.advance(-5);
   }

   /**
    * Tests {@link MediumReference#before(MediumReference)}.
    */
   @Test
   public void before_forSameReference_returnsFalse() {

      long[] testOffsets = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < testOffsets.length; i++) {
         long offset = testOffsets[i];

         MediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.before(referenceToTest), is(false));
      }
   }

   /**
    * Tests {@link MediumReference#before(MediumReference)}.
    */
   @Test
   public void before_forReferenceWithLowerOffset_returnsTrue() {

      final MediumReference relativeReference = new StandardMediumReference(dummyMedium, 5);

      long[] offsetsBeforeRelativeReference = new long[] { 0, 2, 4 };

      for (int i = 0; i < offsetsBeforeRelativeReference.length; i++) {
         long offset = offsetsBeforeRelativeReference[i];

         MediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.before(relativeReference), is(true));
      }
   }

   /**
    * Tests {@link MediumReference#before(MediumReference)}.
    */
   @Test
   public void before_forReferenceWithHigherOffset_returnsFalse() {

      final MediumReference relativeReference = new StandardMediumReference(dummyMedium, 5);

      long[] offsetsBehindRelativeReference = new long[] { 6, 10, 99999 };

      for (int i = 0; i < offsetsBehindRelativeReference.length; i++) {
         long offset = offsetsBehindRelativeReference[i];

         MediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.before(relativeReference), is(false));
      }
   }

   /**
    * Tests {@link MediumReference#before(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void before_forDifferentMedium_throwsException() {

      final MediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);
      final MediumReference referenceForOtherMedium = new StandardMediumReference(
         new FileMedium(Paths.get("other"), false), 5);

      referenceToTest.before(referenceForOtherMedium);
   }

   /**
    * Tests {@link MediumReference#behindOrEqual(MediumReference)}.
    */
   @Test
   public void behindOrEqual_forSameReference_returnsTrue() {

      long[] testOffsets = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < testOffsets.length; i++) {
         long offset = testOffsets[i];

         MediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.behindOrEqual(referenceToTest), is(true));
      }
   }

   /**
    * Tests {@link MediumReference#behindOrEqual(MediumReference)}.
    */
   @Test
   public void behindOrEqual_forReferenceWithLowerOffset_returnsFalse() {

      final MediumReference relativeReference = new StandardMediumReference(dummyMedium, 5);

      long[] offsetsBeforeRelativeReference = new long[] { 0, 2, 4 };

      for (int i = 0; i < offsetsBeforeRelativeReference.length; i++) {
         long offset = offsetsBeforeRelativeReference[i];

         MediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.behindOrEqual(relativeReference), is(false));
      }
   }

   /**
    * Tests {@link MediumReference#behindOrEqual(MediumReference)}.
    */
   @Test
   public void behindOrEqual_forReferenceWithHigherOffset_returnsTrue() {

      final MediumReference relativeReference = new StandardMediumReference(dummyMedium, 5);

      long[] offsetsBehindRelativeReference = new long[] { 6, 10, 99999 };

      for (int i = 0; i < offsetsBehindRelativeReference.length; i++) {
         long offset = offsetsBehindRelativeReference[i];

         MediumReference referenceToTest = new StandardMediumReference(dummyMedium, offset);

         Assert.assertThat(referenceToTest.behindOrEqual(relativeReference), is(true));
      }
   }

   /**
    * Tests {@link MediumReference#behindOrEqual(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void behindOrEqual_forDifferentMedium_throwsException() {

      final MediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);
      final MediumReference referenceForOtherMedium = new StandardMediumReference(
         new FileMedium(Paths.get("other"), false), 5);

      referenceToTest.behindOrEqual(referenceForOtherMedium);
   }

   /**
    * Tests {@link MediumReference#before(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void constructor_forNegativeOffset_throwsException() {

      new StandardMediumReference(dummyMedium, -1);
   }

   /**
    * Tests {@link MediumReference#distanceTo(MediumReference)}.
    */
   @Test
   public void distanceTo_forSameReference_returnsZero() {

      final MediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);

      Assert.assertThat(referenceToTest.distanceTo(referenceToTest), is(0L));
   }

   /**
    * Tests {@link MediumReference#distanceTo(MediumReference)}.
    */
   @Test
   public void distanceTo_forReferencesBehind_returnsCorrectPositiveDistance() {

      final MediumReference referenceToTest = new StandardMediumReference(dummyMedium, 2);

      long[] positiveOffsetsBehind = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < positiveOffsetsBehind.length; i++) {
         long offset = positiveOffsetsBehind[i];

         MediumReference advancedReference = new StandardMediumReference(dummyMedium, offset);

         long differenceTo = referenceToTest.getAbsoluteMediumOffset() - offset;

         Assert.assertThat(referenceToTest.distanceTo(advancedReference), is(differenceTo));
      }
   }

   /**
    * Tests {@link MediumReference#distanceTo(MediumReference)}.
    */
   @Test
   public void distanceTo_forReferencesBefore_returnsCorrectNegativeDistance() {

      final MediumReference referenceToTest = new StandardMediumReference(dummyMedium, 2);

      long[] positiveOffsetsBehind = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < positiveOffsetsBehind.length; i++) {
         long offset = positiveOffsetsBehind[i];

         MediumReference advancedReference = new StandardMediumReference(dummyMedium, offset);

         long differenceTo = referenceToTest.getAbsoluteMediumOffset() - offset;

         Assert.assertThat(advancedReference.distanceTo(referenceToTest), is(-differenceTo));
      }
   }

   /**
    * Tests {@link MediumReference#distanceTo(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void distanceTo_forDifferentMedium_throwsException() {

      final MediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);
      final MediumReference referenceForOtherMedium = new StandardMediumReference(
         new FileMedium(Paths.get("other"), false), 5);

      referenceToTest.distanceTo(referenceForOtherMedium);
   }

   /**
    * Tests {@link MediumReference#distanceTo(MediumReference)}.
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
    * Tests {@link MediumReference#before(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void setAbsoluteMediumOffset_forNegativeOffset_throwsException() {

      StandardMediumReference referenceToTest = new StandardMediumReference(dummyMedium, 4);

      referenceToTest.setAbsoluteMediumOffset(-11);
   }
}