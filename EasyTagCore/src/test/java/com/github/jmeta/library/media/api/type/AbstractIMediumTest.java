/**
 *
 * {@link IMediumEqualityTest}.java
 *
 * @author Jens Ebert
 *
 * @date 10.04.2011
 */
package com.github.jmeta.library.media.api.type;

import junit.framework.Assert;

import org.junit.Test;

import com.github.jmeta.library.media.api.type.IMedium;

/**
 * {@link AbstractIMediumTest} tests the {@link IMedium} interface. It basically only checks that the constructors are
 * passing input parameters properly.
 * 
 * @param <T>
 *           The type of wrapped medium.
 */
public abstract class AbstractIMediumTest<T> {

   /**
    * Tests {@link IMedium#toString}.
    */
   @Test
   public void toString_doesNotReturnNull_forMedium() {

      IMedium<T> medium = getMediumToTest();

      Assert.assertNotNull(medium.toString());
   }

   /**
    * Tests {@link IMedium#exists}.
    */
   @Test
   public void exists_returnsExpectedValue_forMedium() {

      IMedium<T> medium = getMediumToTest();
      boolean expectedToExist = isExpectedAsExisting();

      Assert.assertEquals(expectedToExist, medium.exists());
   }

   /**
    * Tests {@link IMedium#isReadOnly}.
    */
   @Test
   public void isReadOnly_returnsExpectedValue_forMedium() {

      IMedium<T> medium = getMediumToTest();
      boolean expectedReadOnly = isExpectedAsReadOnly();

      Assert.assertEquals(expectedReadOnly, medium.isReadOnly());
   }

   /**
    * Tests {@link IMedium#isRandomAccess}.
    */
   @Test
   public void isRandomAccess_returnsExpectedValue_forMedium() {

      IMedium<T> medium = getMediumToTest();
      boolean expectedRandomAccess = isExpectedAsRandomAccess();

      Assert.assertEquals(expectedRandomAccess, medium.isRandomAccess());
   }

   /**
    * Tests {@link IMedium#getName()}.
    */
   @Test
   public void getExternalName_returnsExpectedValue_forMedium() {

      IMedium<T> medium = getMediumToTest();
      String expectedExternalName = getExpectedExternalName();

      Assert.assertEquals(expectedExternalName, medium.getName());
   }

   /**
    * Tests {@link IMedium#getCurrentLength}.
    */
   @Test
   public void getCurrentLength_returnsExpectedValue_forMedium() {

      IMedium<T> medium = getMediumToTest();
      long expectedLength = getExpectedLength();

      Assert.assertEquals(expectedLength, medium.getCurrentLength());
   }

   /**
    * Tests {@link IMedium#getWrappedMedium}.
    */
   @Test
   public void getWrappedMedium_returnsExpectedValue_forMedium() {

      IMedium<T> medium = getMediumToTest();
      T expectedWrappedMedium = getExpectedWrappedMedium();

      Assert.assertEquals(expectedWrappedMedium, medium.getWrappedMedium());
   }

   /**
    * Returns the {@link IMedium} to test.
    * 
    * @return the {@link IMedium} to test.
    */
   protected abstract IMedium<T> getMediumToTest();

   /**
    * Returns whether the {@link IMedium} is expected to be read-only or not.
    * 
    * @return whether the {@link IMedium} is expected to be read-only or not.
    */
   protected abstract boolean isExpectedAsReadOnly();

   /**
    * Returns whether the {@link IMedium} is expected to be random-access or not.
    * 
    * @return whether the {@link IMedium} is expected to be random-access or not.
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
    * Returns whether the {@link IMedium} is expected to be existing or not.
    * 
    * @return whether the {@link IMedium} is expected to be existing or not.
    */
   protected abstract boolean isExpectedAsExisting();

   /**
    * Returns the expected {@link IMedium} length.
    * 
    * @return the expected {@link IMedium} length.
    */
   protected abstract long getExpectedLength();
}