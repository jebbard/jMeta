/**
 *
 * {@link AbstractEqualsTest}.java
 *
 * @author Jens Ebert
 *
 * @date 23.06.2008
 *
 */

package com.github.jmeta.utility.equalstest.api.services;

import static org.hamcrest.CoreMatchers.is;

import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractEqualsTest} tests for the correct implementation of the {@link #equals(Object)} method, which includes
 * reflexivity, transitivity and symmetry as well as expected behavior of {@link #hashCode()}.
 *
 * @param <T>
 *           The type of object to be equality-tested.
 */
public abstract class AbstractEqualsTest<T> {

   /**
    * Checks the test data to be valid.
    *
    * @throws IllegalArgumentException
    *            If data is not valid with a speaking message what is wrong in detail.
    */
   @Before
   public void checkTestData() {
      if (getObjects() == null || getEqualObjects() == null || getThirdEqualObjects() == null
         || getDifferentObjects() == null)
         throw new InvalidTestDataException("Do not return null when implementing getObjects(), "
            + "getEqualObjects(), getThirdEqualObjects() or getDifferentObjects()!", null);

      if (getObjects().size() != getEqualObjects().size() || getObjects().size() != getThirdEqualObjects().size()
         || getDifferentObjects().size() != getEqualObjects().size())
         throw new InvalidTestDataException("Tested vectors must be equal in size!", null);
   }

   /**
    * Tests {@link Object#equals(Object)}.
    */
   @Test
   public void equals_isReflexive_forTheIdenticalObjects() {

      List<T> objects = getObjects();

      for (int i = 0; i < objects.size(); ++i) {
         T object = objects.get(i);

         Assert.assertThat(object.equals(object), is(true));
      }
   }

   /**
    * Tests {@link Object#equals(Object)}.
    */
   @Test
   public void equals_returnsTrue_forEqualObjects() {

      List<T> objects = getObjects();
      List<T> equalObjects = getEqualObjects();
      List<T> thirdEqualObjects = getThirdEqualObjects();

      for (int i = 0; i < objects.size(); ++i) {
         T object = objects.get(i);
         T equalObject = equalObjects.get(i);
         T thirdEqualObject = thirdEqualObjects.get(i);

         Assert.assertTrue(object.equals(equalObject));
         // Not yet transitivity!
         Assert.assertTrue(equalObject.equals(thirdEqualObject));
      }
   }

   /**
    * Tests {@link Object#equals(Object)}.
    */
   @Test
   public void equals_isSymmetricallyReturningTrue_forEqualObjects() {

      List<T> objects = getObjects();
      List<T> equalObjects = getEqualObjects();
      List<T> thirdEqualObjects = getThirdEqualObjects();

      for (int i = 0; i < objects.size(); ++i) {
         T object = objects.get(i);
         T equalObject = equalObjects.get(i);
         T thirdEqualObject = thirdEqualObjects.get(i);

         Assert.assertTrue(equalObject.equals(object));
         // Not yet transitivity!
         Assert.assertTrue(thirdEqualObject.equals(equalObject));
      }
   }

   /**
    * Tests {@link Object#equals(Object)}.
    */
   @Test
   public void equals_isTransitivelyReturningTrue_forEqualObjects() {

      List<T> objects = getObjects();
      List<T> equalObjects = getEqualObjects();
      List<T> thirdEqualObjects = getThirdEqualObjects();

      for (int i = 0; i < objects.size(); ++i) {
         T object = objects.get(i);
         T equalObject = equalObjects.get(i);
         T thirdEqualObject = thirdEqualObjects.get(i);

         Assert.assertTrue(equalObject.equals(object));
         Assert.assertTrue(thirdEqualObject.equals(object));
         // And also symmetrically transitive!
         Assert.assertTrue(object.equals(thirdEqualObject));
      }
   }

   /**
    * Tests {@link Object#equals(Object)}.
    */
   @Test
   public void equals_returnsFalse_forDifferentObjects() {

      List<T> objects = getObjects();
      List<T> differentObjects = getDifferentObjects();

      for (int i = 0; i < objects.size(); ++i) {
         T object = objects.get(i);
         T differentObject = differentObjects.get(i);

         Assert.assertFalse(object.equals(differentObject));
      }
   }

   /**
    * Tests {@link Object#equals(Object)}.
    */
   @Test
   public void equals_isSymmetricallyReturningFalse_forDifferentObjects() {

      List<T> objects = getObjects();
      List<T> differentObjects = getDifferentObjects();

      for (int i = 0; i < objects.size(); ++i) {
         T object = objects.get(i);
         T differentObject = differentObjects.get(i);

         Assert.assertFalse(differentObject.equals(object));
      }
   }

   /**
    * Tests {@link Object#equals(Object)}.
    */
   @Test
   public void equals_returnsFalse_forCallingItWithNull() {

      List<T> objects = getObjects();

      for (int i = 0; i < objects.size(); ++i) {
         T object = objects.get(i);

         Assert.assertFalse(object.equals(null));
      }
   }

   /**
    * Tests {@link Object#equals(Object)}.
    */
   @Test
   public void hashCode_returnsSameInt_forEqualObjects() {

      List<T> objects = getObjects();
      List<T> equalObjects = getEqualObjects();
      List<T> thirdEqualObjects = getThirdEqualObjects();

      for (int i = 0; i < objects.size(); ++i) {
         T object = objects.get(i);
         T equalObject = equalObjects.get(i);
         T thirdEqualObject = thirdEqualObjects.get(i);

         Assert.assertEquals(object.hashCode(), equalObject.hashCode());
         Assert.assertEquals(object.hashCode(), thirdEqualObject.hashCode());
      }
   }

   /**
    * Returns left objects to be used in equals test. The right objects are tested to be unequal to null, equal to
    * themselves and equal to the right objects with the same index.
    *
    * @return The left objects to be used in equals test.
    */
   protected abstract List<T> getObjects();

   /**
    * Returns right objects to be used in equals test. The right objects are tested to be unequal to null, equal to
    * themselves and equal to the left objects with the same index.
    *
    * @return The right objects to be used in equals test.
    */
   protected abstract List<T> getEqualObjects();

   /**
    * Returns right objects that are known to be unequal to the corresponding left object with the same index.
    *
    * @return Right objects that are known to be unequal to the corresponding left object.
    */
   protected abstract List<T> getDifferentObjects();

   /**
    * Returns third objects that are known to be equal to the right objects with the same index.
    *
    * @return Third objects that are known to be equal to the right objects with the same index.
    */
   protected abstract List<T> getThirdEqualObjects();
}
