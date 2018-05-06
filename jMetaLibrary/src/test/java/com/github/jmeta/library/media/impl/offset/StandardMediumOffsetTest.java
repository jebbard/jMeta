/**
 *
 * {@link StandardMediumOffsetTest}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package com.github.jmeta.library.media.impl.offset;

import static org.hamcrest.CoreMatchers.is;

import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * {@link StandardMediumOffsetTest} tests the {@link StandardMediumOffset} class and its interface
 * {@link MediumOffset}.
 */
public class StandardMediumOffsetTest {

   private Medium<?> dummyMedium = (Medium<?>) new InMemoryMedium(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17 }, "the dummy",
   false);

   /**
    * Tests {@link MediumOffset#advance}.
    */
   @Test
   public void advance_byPositiveDistance_offsetCorrectlyAdvanced() {

      final MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 0);

      long[] positiveAdvanceDistances = new long[] { 1, 99, 199, 1029674, 8, };

      long offsetBefore = referenceToTest.getAbsoluteMediumOffset();

      for (int i = 0; i < positiveAdvanceDistances.length; i++) {
         long advance = positiveAdvanceDistances[i];

         MediumOffset advancedReference = referenceToTest.advance(advance);

         // Old reference is unchanged
         Assert.assertThat(referenceToTest.getAbsoluteMediumOffset(), is(offsetBefore));

         // New reference has been advanced by the given count
         long diffDistance = advancedReference.getAbsoluteMediumOffset() - referenceToTest.getAbsoluteMediumOffset();

         Assert.assertThat(advance, is(diffDistance));
      }
   }

   /**
    * Tests {@link MediumOffset#advance}.
    */
   @Test
   public void advance_byNegativeCountSmallerOrEqualToOffset_offsetIsCorrectlyAdvanced() {

      final MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 5);

      long[] negativeAdvanceDistances = new long[] { -1, -5, };

      long offsetBefore = referenceToTest.getAbsoluteMediumOffset();

      for (int i = 0; i < negativeAdvanceDistances.length; i++) {
         long advance = negativeAdvanceDistances[i];

         MediumOffset advancedReference = referenceToTest.advance(advance);

         // Old reference is unchanged
         Assert.assertThat(referenceToTest.getAbsoluteMediumOffset(), is(offsetBefore));

         // New reference has been advanced by the given count
         long diffDistance = advancedReference.getAbsoluteMediumOffset() - referenceToTest.getAbsoluteMediumOffset();

         Assert.assertThat(advance, is(diffDistance));
      }
   }

   /**
    * Tests {@link MediumOffset#advance}.
    */
   @Test
   public void advance_byZero_offsetNotAdvanced() {

      final MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 5);

      long offsetBefore = referenceToTest.getAbsoluteMediumOffset();

      MediumOffset advancedReference = referenceToTest.advance(0);

      // Old reference is unchanged
      Assert.assertThat(referenceToTest.getAbsoluteMediumOffset(), is(offsetBefore));

      // New reference has been advanced by the given count
      Assert.assertThat(advancedReference.getAbsoluteMediumOffset(), is(referenceToTest.getAbsoluteMediumOffset()));
   }

   /**
    * Tests {@link MediumOffset#advance}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void advance_forNegativeCountBiggerThanOffset_throwsException() {

      final MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 4);

      referenceToTest.advance(-5);
   }

   /**
    * Tests {@link MediumOffset#before(MediumOffset)}.
    */
   @Test
   public void before_forSameOffset_returnsFalse() {

      long[] testOffsets = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < testOffsets.length; i++) {
         long offset = testOffsets[i];

         MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, offset);

         Assert.assertThat(referenceToTest.before(referenceToTest), is(false));
      }
   }

   /**
    * Tests {@link MediumOffset#before(MediumOffset)}.
    */
   @Test
   public void before_forReferenceWithLowerOffset_returnsTrue() {

      final MediumOffset relativeReference = new StandardMediumOffset(dummyMedium, 5);

      long[] offsetsBeforeRelativeReference = new long[] { 0, 2, 4 };

      for (int i = 0; i < offsetsBeforeRelativeReference.length; i++) {
         long offset = offsetsBeforeRelativeReference[i];

         MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, offset);

         Assert.assertThat(referenceToTest.before(relativeReference), is(true));
      }
   }

   /**
    * Tests {@link MediumOffset#before(MediumOffset)}.
    */
   @Test
   public void before_forReferenceWithHigherOffset_returnsFalse() {

      final MediumOffset relativeReference = new StandardMediumOffset(dummyMedium, 5);

      long[] offsetsBehindRelativeReference = new long[] { 6, 10, 99999 };

      for (int i = 0; i < offsetsBehindRelativeReference.length; i++) {
         long offset = offsetsBehindRelativeReference[i];

         MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, offset);

         Assert.assertThat(referenceToTest.before(relativeReference), is(false));
      }
   }

   /**
    * Tests {@link MediumOffset#before(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void before_forDifferentMedium_throwsException() {

      final MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 4);
      final MediumOffset referenceForOtherMedium = new StandardMediumOffset(
         new FileMedium(Paths.get("other"), false), 5);

      referenceToTest.before(referenceForOtherMedium);
   }

   /**
    * Tests {@link MediumOffset#behindOrEqual(MediumOffset)}.
    */
   @Test
   public void behindOrEqual_forSameReference_returnsTrue() {

      long[] testOffsets = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < testOffsets.length; i++) {
         long offset = testOffsets[i];

         MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, offset);

         Assert.assertThat(referenceToTest.behindOrEqual(referenceToTest), is(true));
      }
   }

   /**
    * Tests {@link MediumOffset#behindOrEqual(MediumOffset)}.
    */
   @Test
   public void behindOrEqual_forReferenceWithLowerOffset_returnsFalse() {

      final MediumOffset relativeReference = new StandardMediumOffset(dummyMedium, 5);

      long[] offsetsBeforeRelativeReference = new long[] { 0, 2, 4 };

      for (int i = 0; i < offsetsBeforeRelativeReference.length; i++) {
         long offset = offsetsBeforeRelativeReference[i];

         MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, offset);

         Assert.assertThat(referenceToTest.behindOrEqual(relativeReference), is(false));
      }
   }

   /**
    * Tests {@link MediumOffset#behindOrEqual(MediumOffset)}.
    */
   @Test
   public void behindOrEqual_forReferenceWithHigherOffset_returnsTrue() {

      final MediumOffset relativeReference = new StandardMediumOffset(dummyMedium, 5);

      long[] offsetsBehindRelativeReference = new long[] { 6, 10, 99999 };

      for (int i = 0; i < offsetsBehindRelativeReference.length; i++) {
         long offset = offsetsBehindRelativeReference[i];

         MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, offset);

         Assert.assertThat(referenceToTest.behindOrEqual(relativeReference), is(true));
      }
   }

   /**
    * Tests {@link MediumOffset#behindOrEqual(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void behindOrEqual_forDifferentMedium_throwsException() {

      final MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 4);
      final MediumOffset referenceForOtherMedium = new StandardMediumOffset(
         new FileMedium(Paths.get("other"), false), 5);

      referenceToTest.behindOrEqual(referenceForOtherMedium);
   }

   /**
    * Tests {@link MediumOffset#before(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void constructor_forNegativeOffset_throwsException() {

      new StandardMediumOffset(dummyMedium, -1);
   }

   /**
    * Tests {@link MediumOffset#distanceTo(MediumOffset)}.
    */
   @Test
   public void distanceTo_forSameReference_returnsZero() {

      final MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 4);

      Assert.assertThat(referenceToTest.distanceTo(referenceToTest), is(0L));
   }

   /**
    * Tests {@link MediumOffset#distanceTo(MediumOffset)}.
    */
   @Test
   public void distanceTo_forReferencesBehind_returnsCorrectPositiveDistance() {

      final MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 2);

      long[] positiveOffsetsBehind = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < positiveOffsetsBehind.length; i++) {
         long offset = positiveOffsetsBehind[i];

         MediumOffset advancedReference = new StandardMediumOffset(dummyMedium, offset);

         long differenceTo = referenceToTest.getAbsoluteMediumOffset() - offset;

         Assert.assertThat(referenceToTest.distanceTo(advancedReference), is(differenceTo));
      }
   }

   /**
    * Tests {@link MediumOffset#distanceTo(MediumOffset)}.
    */
   @Test
   public void distanceTo_forReferencesBefore_returnsCorrectNegativeDistance() {

      final MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 2);

      long[] positiveOffsetsBehind = new long[] { 3, 99, 199, 1029674, 8, };

      for (int i = 0; i < positiveOffsetsBehind.length; i++) {
         long offset = positiveOffsetsBehind[i];

         MediumOffset advancedReference = new StandardMediumOffset(dummyMedium, offset);

         long differenceTo = referenceToTest.getAbsoluteMediumOffset() - offset;

         Assert.assertThat(advancedReference.distanceTo(referenceToTest), is(-differenceTo));
      }
   }

   /**
    * Tests {@link MediumOffset#distanceTo(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void distanceTo_forDifferentMedium_throwsException() {

      final MediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 4);
      final MediumOffset referenceForOtherMedium = new StandardMediumOffset(
         new FileMedium(Paths.get("other"), false), 5);

      referenceToTest.distanceTo(referenceForOtherMedium);
   }

   /**
    * Tests {@link MediumOffset#distanceTo(MediumOffset)}.
    */
   @Test
   public void setAbsoluteMediumOffset_forPositiveOffset_changesOffsetToNewValue() {

      StandardMediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 4);

      referenceToTest.setAbsoluteMediumOffset(0);
      Assert.assertEquals(0, referenceToTest.getAbsoluteMediumOffset());

      referenceToTest.setAbsoluteMediumOffset(32);
      Assert.assertEquals(32, referenceToTest.getAbsoluteMediumOffset());
   }

   /**
    * Tests {@link MediumOffset#before(MediumOffset)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void setAbsoluteMediumOffset_forNegativeOffset_throwsException() {

      StandardMediumOffset referenceToTest = new StandardMediumOffset(dummyMedium, 4);

      referenceToTest.setAbsoluteMediumOffset(-11);
   }
}