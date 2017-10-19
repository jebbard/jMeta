/**
 * {@link PhysicalDataBlockType}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.type;

import de.je.util.javautil.common.extenum.AbstractExtensibleEnum;

/**
 *
 */
public class PhysicalDataBlockType
   extends AbstractExtensibleEnum<PhysicalDataBlockType> {

   /**
   		 *
   		 */
   public static final PhysicalDataBlockType PAYLOAD = new PhysicalDataBlockType(
      "PAYLOAD");

   /**
    *
    */
   public static final PhysicalDataBlockType FIELD = new PhysicalDataBlockType(
      "FIELD");

   /**
   *
   */
   public static final PhysicalDataBlockType HEADER = new PhysicalDataBlockType(
      "HEADER");

   /**
   *
   */
   public static final PhysicalDataBlockType FOOTER = new PhysicalDataBlockType(
      "FOOTER");

   /**
   *
   */
   public static final PhysicalDataBlockType CONTAINER = new PhysicalDataBlockType(
      "CONTAINER");

   /**
    * Creates an instance of {@link PhysicalDataBlockType}.
    * 
    * @param id
    *           The id of the enumeration literal.
    */
   protected PhysicalDataBlockType(String id) {
      super(id);
   }

}
