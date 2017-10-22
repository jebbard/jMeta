/**
 *
 * {@link BitAddressXXXTest}.java
 *
 * @author Jens Ebert
 *
 * @date 09.01.2009
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.utility.equalstest.api.services.AbstractEqualsTest;

/**
 * {@link BitAddressXXXTest}
 *
 */
public class BitAddressXXXTest extends AbstractEqualsTest<BitAddress> {

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getObjects()
    */
   @Override
   protected List<BitAddress> getObjects() {
      if (m_objects == null) {
         m_objects = new ArrayList<>();

         m_objects.add(new BitAddress(6, 0));
         m_objects.add(new BitAddress(7, 0));
         m_objects.add(new BitAddress(200, 0));
      }

      return m_objects;
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getEqualObjects()
    */
   @Override
   protected List<BitAddress> getEqualObjects() {
      if (m_equalObjects == null) {
         m_equalObjects = new ArrayList<>();

         m_equalObjects.add(new BitAddress(6, 0));
         m_equalObjects.add(new BitAddress(7, 0));
         m_equalObjects.add(new BitAddress(200, 0));
      }

      return m_equalObjects;
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getDifferentObjects()
    */
   @Override
   protected List<BitAddress> getDifferentObjects() {
      if (m_differentObjects == null) {
         m_differentObjects = new ArrayList<>();

         m_differentObjects.add(new BitAddress(0, 0));
         m_differentObjects.add(new BitAddress(6, 0));
         m_differentObjects.add(new BitAddress(200, 1));
      }

      return m_differentObjects;
   }

   /**
    * @see de.je.util.javautil.testUtil.equa.AbstractEqualsTest#getThirdEqualObjects()
    */
   @Override
   protected List<BitAddress> getThirdEqualObjects() {
      if (m_thirdEqualObjects == null) {
         m_thirdEqualObjects = new ArrayList<>();

         m_thirdEqualObjects.add(new BitAddress(6, 0));
         m_thirdEqualObjects.add(new BitAddress(7, 0));
         m_thirdEqualObjects.add(new BitAddress(200, 0));
      }

      return m_thirdEqualObjects;
   }

   private List<BitAddress> m_objects;
   private List<BitAddress> m_equalObjects;
   private List<BitAddress> m_thirdEqualObjects;
   private List<BitAddress> m_differentObjects;
}
