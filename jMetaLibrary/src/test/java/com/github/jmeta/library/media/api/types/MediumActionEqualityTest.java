/**
 *
 * MediumActionEqualityTest.java
 *
 * @author Jens
 *
 * @date 20.05.2016
 *
 */
package com.github.jmeta.library.media.api.types;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.impl.offset.StandardMediumOffset;
import com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest;

/**
 * {@link MediumActionEqualityTest} tests the equals and hashCode implementation of {@link MediumAction}.
 */
public class MediumActionEqualityTest extends AbstractEqualsTest<MediumAction> {

   private static final ByteBuffer DEFAULT_BYTES = ByteBuffer.wrap(new byte[] { 0, 1, 2, 3, 4 });
   private static final FileMedium MEDIUM = new FileMedium(TestMedia.FIRST_TEST_FILE_PATH, MediumAccessType.READ_ONLY);

   /**
    * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getDifferentObjects()
    */
   @Override
   protected List<MediumAction> getDifferentObjects() {

      List<MediumAction> objects = new ArrayList<>();

      // Different in MediumRegion
      objects.add(new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumOffset(MediumActionEqualityTest.MEDIUM, 2),
            MediumActionEqualityTest.DEFAULT_BYTES.remaining()),
         0, MediumActionEqualityTest.DEFAULT_BYTES));
      // Different in action type
      objects.add(new MediumAction(MediumActionType.READ,
         new MediumRegion(new StandardMediumOffset(MediumActionEqualityTest.MEDIUM, 22), 20), 122, null));
      // Different in sequence number
      objects.add(new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumOffset(MediumActionEqualityTest.MEDIUM, 0),
            MediumActionEqualityTest.DEFAULT_BYTES.remaining()),
         22, MediumActionEqualityTest.DEFAULT_BYTES));
      // Different replacement byte count
      objects.add(new MediumAction(MediumActionType.REPLACE,
         new MediumRegion(new StandardMediumOffset(MediumActionEqualityTest.MEDIUM, 0), 11), 22,
         MediumActionEqualityTest.DEFAULT_BYTES));

      return objects;
   }

   /**
    * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getEqualObjects()
    */
   @Override
   protected List<MediumAction> getEqualObjects() {

      return getObjects();
   }

   /**
    * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getObjects()
    */
   @Override
   protected List<MediumAction> getObjects() {

      List<MediumAction> objects = new ArrayList<>();

      objects.add(new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumOffset(MediumActionEqualityTest.MEDIUM, 0),
            MediumActionEqualityTest.DEFAULT_BYTES.remaining()),
         0, MediumActionEqualityTest.DEFAULT_BYTES));
      objects.add(new MediumAction(MediumActionType.WRITE,
         new MediumRegion(new StandardMediumOffset(MediumActionEqualityTest.MEDIUM, 22), 4), 122, null));
      objects.add(new MediumAction(MediumActionType.INSERT,
         new MediumRegion(new StandardMediumOffset(MediumActionEqualityTest.MEDIUM, 0),
            MediumActionEqualityTest.DEFAULT_BYTES.remaining()),
         12, MediumActionEqualityTest.DEFAULT_BYTES));
      objects.add(new MediumAction(MediumActionType.REPLACE,
         new MediumRegion(new StandardMediumOffset(MediumActionEqualityTest.MEDIUM, 0), 9), 22,
         MediumActionEqualityTest.DEFAULT_BYTES));

      return objects;
   }

   /**
    * @see com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest#getThirdEqualObjects()
    */
   @Override
   protected List<MediumAction> getThirdEqualObjects() {

      return getObjects();
   }
}
