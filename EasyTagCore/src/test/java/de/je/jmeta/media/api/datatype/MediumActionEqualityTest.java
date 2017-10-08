/**
 *
 * MediumActionEqualityTest.java
 *
 * @author Jens
 *
 * @date 20.05.2016
 *
 */
package de.je.jmeta.media.api.datatype;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import de.je.jmeta.media.api.helper.MediaTestCaseConstants;
import de.je.jmeta.media.impl.StandardMediumReference;
import de.je.util.javautil.testUtil.equa.AbstractEqualsTest;

/**
 * {@link MediumActionEqualityTest} tests the equals and hashCode implementation of {@link MediumAction}.
 */
public class MediumActionEqualityTest extends AbstractEqualsTest<MediumAction> {

   private static final ByteBuffer DEFAULT_BYTES = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });
   private static final FileMedium MEDIUM = new FileMedium(MediaTestCaseConstants.STANDARD_TEST_FILE, true);

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getObjects()
    */
   @Override
   protected List<MediumAction> getObjects() {

      List<MediumAction> objects = new ArrayList<>();

      objects.add(new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES));
      objects.add(new MediumAction(MediumActionType.WRITE, new MediumRegion(new StandardMediumReference(MEDIUM, 22), 4),
         122, null));
      objects.add(new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 12, DEFAULT_BYTES));
      objects.add(new MediumAction(MediumActionType.REPLACE,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), 9), 22, DEFAULT_BYTES));

      return objects;
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getEqualObjects()
    */
   @Override
   protected List<MediumAction> getEqualObjects() {

      return getObjects();
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getDifferentObjects()
    */
   @Override
   protected List<MediumAction> getDifferentObjects() {

      List<MediumAction> objects = new ArrayList<>();

      // Different in MediumRegion
      objects.add(new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 2), DEFAULT_BYTES.remaining()), 0, DEFAULT_BYTES));
      // Different in action type
      objects.add(new MediumAction(MediumActionType.READ, new MediumRegion(new StandardMediumReference(MEDIUM, 22), 20),
         122, null));
      // Different in sequence number
      objects.add(new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), DEFAULT_BYTES.remaining()), 22, DEFAULT_BYTES));
      // Different replacement byte count
      objects.add(new MediumAction(MediumActionType.REPLACE,
         new MediumRegion(new StandardMediumReference(MEDIUM, 0), 11), 22, DEFAULT_BYTES));

      return objects;
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getThirdEqualObjects()
    */
   @Override
   protected List<MediumAction> getThirdEqualObjects() {

      return getObjects();
   }
}
