/**
 *
 * MediumActionComparatorTest.java
 *
 * @author Jens
 *
 * @date 23.05.2016
 *
 */
package com.github.jmeta.library.media.impl.changeManager;

import java.nio.ByteBuffer;
import java.nio.file.Paths;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.reference.StandardMediumReference;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;

/**
 * {@link MediumActionComparatorTest} tests the class {@link MediumActionComparator}.
 */
public class MediumActionComparatorTest {

   private static final ByteBuffer DEFAULT_BYTES = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });
   private static final FileMedium MEDIUM = new FileMedium(MediaTestFiles.FIRST_TEST_FILE_PATH, true);

   /**
    * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
    */
   @Test
   public void compare_forEqualMediumActions_returnsZero() {

      MediumActionComparator comparator = new MediumActionComparator();

      MediumAction actionALeft = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES);
      MediumAction actionARight = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES);
      MediumAction actionBLeft = new MediumAction(MediumActionType.WRITE,
         new MediumRegion(new StandardMediumReference(MEDIUM, 22), DEFAULT_BYTES.remaining()), 122, DEFAULT_BYTES);
      MediumAction actionBRight = new MediumAction(MediumActionType.WRITE,
         new MediumRegion(new StandardMediumReference(MEDIUM, 22), DEFAULT_BYTES.remaining()), 122, DEFAULT_BYTES);
      MediumAction actionCLeft = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 2, DEFAULT_BYTES);
      MediumAction actionCRight = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 2, DEFAULT_BYTES);

      Assert.assertEquals(0, comparator.compare(actionALeft, actionARight));
      Assert.assertEquals(0, comparator.compare(actionBLeft, actionBRight));
      Assert.assertEquals(0, comparator.compare(actionCLeft, actionCRight));
   }

   /**
    * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
    */
   @Test
   public void compare_forLeftMediumActionsGreater_returnsPositiveInt() {

      MediumActionComparator comparator = new MediumActionComparator();

      // Sequence number of left object is bigger than right one
      MediumAction actionALeft = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 2, DEFAULT_BYTES);
      MediumAction actionARight = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 1, DEFAULT_BYTES);
      // MediumReference offset of left object is bigger than right one, sequence number identical
      MediumAction actionBLeft = new MediumAction(MediumActionType.WRITE,
         new MediumRegion(new StandardMediumReference(MEDIUM, 9999), DEFAULT_BYTES.remaining()), 122, DEFAULT_BYTES);
      MediumAction actionBRight = new MediumAction(MediumActionType.WRITE,
         new MediumRegion(new StandardMediumReference(MEDIUM, 22), DEFAULT_BYTES.remaining()), 122, DEFAULT_BYTES);
      // MediumReference offset of left object is bigger than right one, and sequence number is smaller than right one
      MediumAction actionCLeft = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 100), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES);
      MediumAction actionCRight = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 1, DEFAULT_BYTES);
      // Both MediumReference offset and sequence number of left object is bigger than right one
      MediumAction actionDLeft = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 100), DEFAULT_BYTES.remaining()), 2, DEFAULT_BYTES);
      MediumAction actionDRight = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 1, DEFAULT_BYTES);
      // Both MediumReference offset and sequence number are equal, but Action type and region size differ
      MediumAction actionELeft = new MediumAction(MediumActionType.REPLACE,
         new MediumRegion(new StandardMediumReference(MEDIUM, 100), 11), 0, DEFAULT_BYTES);
      MediumAction actionERight = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 100), 1), 0, ByteBuffer.wrap(new byte[] { 1 }));

      Assert.assertTrue(comparator.compare(actionALeft, actionARight) > 0);
      Assert.assertTrue(comparator.compare(actionBLeft, actionBRight) > 0);
      Assert.assertTrue(comparator.compare(actionCLeft, actionCRight) > 0);
      Assert.assertTrue(comparator.compare(actionDLeft, actionDRight) > 0);
      Assert.assertTrue(comparator.compare(actionELeft, actionERight) > 0);
   }

   /**
    * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
    */
   @Test
   public void compare_forLeftMediumActionsSmaller_returnsNegativeInt() {

      MediumActionComparator comparator = new MediumActionComparator();

      // Sequence number of left object is smaller than right one
      MediumAction actionALeft = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 1, DEFAULT_BYTES);
      MediumAction actionARight = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 2, DEFAULT_BYTES);
      // MediumReference offset of left object is smaller than right one, sequence number identical
      MediumAction actionBLeft = new MediumAction(MediumActionType.WRITE,
         new MediumRegion(new StandardMediumReference(MEDIUM, 22), DEFAULT_BYTES.remaining()), 122, DEFAULT_BYTES);
      MediumAction actionBRight = new MediumAction(MediumActionType.WRITE,
         new MediumRegion(new StandardMediumReference(MEDIUM, 9999), DEFAULT_BYTES.remaining()), 122, DEFAULT_BYTES);
      // MediumReference offset of left object is smaller than right one, and sequence number is bigger than right one
      MediumAction actionCLeft = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 1, DEFAULT_BYTES);
      MediumAction actionCRight = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 100), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES);
      // Both MediumReference offset and sequence number of left object is smaller than right one
      MediumAction actionDLeft = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES);
      MediumAction actionDRight = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 100), DEFAULT_BYTES.remaining()), 1, DEFAULT_BYTES);

      Assert.assertTrue(comparator.compare(actionALeft, actionARight) < 0);
      Assert.assertTrue(comparator.compare(actionBLeft, actionBRight) < 0);
      Assert.assertTrue(comparator.compare(actionCLeft, actionCRight) < 0);
      Assert.assertTrue(comparator.compare(actionDLeft, actionDRight) < 0);
   }

   /**
    * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void compare_forMediumActionsWithDifferentMedium_throwsException() {

      MediumActionComparator comparator = new MediumActionComparator();

      FileMedium otherMedium = new FileMedium(Paths.get("."), true);

      MediumAction actionA1 = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES);
      MediumAction actionA2 = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(otherMedium, 0), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES);

      comparator.compare(actionA1, actionA2);
   }

   /**
    * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
    */
   @Test(expected = NullPointerException.class)
   public void compare_forLeftMediumActionNull_throwsNullPointerException() {

      MediumActionComparator comparator = new MediumActionComparator();

      MediumAction actionA1 = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES);

      comparator.compare(null, actionA1);
   }

   /**
    * Tests {@link MediumActionComparator#compare(MediumAction, MediumAction)}.
    */
   @Test(expected = NullPointerException.class)
   public void compare_forRightMediumActionNull_throwsNullPointerException() {

      MediumActionComparator comparator = new MediumActionComparator();

      MediumAction actionA1 = new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES);

      comparator.compare(actionA1, null);
   }

}
