/**
 *
 * {@link MediumEqualityTest}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package com.github.jmeta.library.media.api.types;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link AbstractMediumTest} tests the {@link Medium} interface. It basically only checks that the constructors are
 * passing input parameters properly.
 * 
 * @param <T>
 *           The type of wrapped medium.
 */
public abstract class AbstractMediumTest<T> {

   /**
    * Tests {@link Medium#toString}.
    */
   @Test
   public void toString_doesNotReturnNull_forMedium() {

      Medium<T> medium = getMediumToTest();

      Assert.assertNotNull(medium.toString());
   }

   /**
    * Tests {@link Medium#isReadOnly}.
    */
   @Test
   public void isReadOnly_returnsExpectedValue_forMedium() {

      Medium<T> medium = getMediumToTest();
      boolean expectedReadOnly = isExpectedAsReadOnly();

      Assert.assertEquals(expectedReadOnly, medium.isReadOnly());
   }

   /**
    * Tests {@link Medium#isRandomAccess}.
    */
   @Test
   public void isRandomAccess_returnsExpectedValue_forMedium() {

      Medium<T> medium = getMediumToTest();
      boolean expectedRandomAccess = isExpectedAsRandomAccess();

      Assert.assertEquals(expectedRandomAccess, medium.isRandomAccess());
   }

   /**
    * Tests {@link Medium#getName()}.
    */
   @Test
   public void getExternalName_returnsExpectedValue_forMedium() {

      Medium<T> medium = getMediumToTest();
      String expectedExternalName = getExpectedExternalName();

      Assert.assertEquals(expectedExternalName, medium.getName());
   }

   /**
    * Tests {@link Medium#getCurrentLength}.
    */
   @Test
   public void getCurrentLength_returnsExpectedValue_forMedium() {

      Medium<T> medium = getMediumToTest();
      long expectedLength = getExpectedLength();

      Assert.assertEquals(expectedLength, medium.getCurrentLength());
   }

   /**
    * Tests {@link Medium#getWrappedMedium}.
    */
   @Test
   public void getWrappedMedium_returnsExpectedValue_forMedium() {

      Medium<T> medium = getMediumToTest();
      T expectedWrappedMedium = getExpectedWrappedMedium();

      Assert.assertEquals(expectedWrappedMedium, medium.getWrappedMedium());
   }

   /**
    * Returns the {@link Medium} to test.
    * 
    * @return the {@link Medium} to test.
    */
   protected abstract Medium<T> getMediumToTest();

   /**
    * Returns whether the {@link Medium} is expected to be read-only or not.
    * 
    * @return whether the {@link Medium} is expected to be read-only or not.
    */
   protected abstract boolean isExpectedAsReadOnly();

   /**
    * Returns whether the {@link Medium} is expected to be random-access or not.
    * 
    * @return whether the {@link Medium} is expected to be random-access or not.
    */
   protected abstract boolean isExpectedAsRandomAccess();

   /**
    * Returns the expected wrapped medium.
    * 
    * @return the expected wrapped medium.
    */
   protected abstract T getExpectedWrappedMedium();

   /**
    * Returns the expected external name.
    * 
    * @return the expected external name.
    */
   protected abstract String getExpectedExternalName();

   /**
    * Returns whether the {@link Medium} is expected to be existing or not.
    * 
    * @return whether the {@link Medium} is expected to be existing or not.
    */
   protected abstract boolean isExpectedAsExisting();

   /**
    * Returns the expected {@link Medium} length.
    * 
    * @return the expected {@link Medium} length.
    */
   protected abstract long getExpectedLength();
}