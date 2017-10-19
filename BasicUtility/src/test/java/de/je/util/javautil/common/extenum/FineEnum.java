/**
 *
 * {@link FineEnum}.java
 *
 * @author Jens Ebert
 *
 * @date 18.06.2009
 *
 */
package de.je.util.javautil.common.extenum;

import de.je.util.javautil.common.extenum.AbstractExtensibleEnum;

/**
 * {@link FineEnum} is just a dummy enumeration for testing {@link AbstractExtensibleEnum}
 */
public class FineEnum extends AbstractExtensibleEnum<FineEnum> {

   /**
    * Protected constructor
    *
    * @param id
    *           The id
    */
   protected FineEnum(String id) {
      super(id);
   }

   /**
    * One enum instance
    */
   public static final FineEnum ACTUAL_CONTENTS = new FineEnum("ACT_CONT");
   /**
    * Another enum instance
    */
   public static final FineEnum ACTUAL_CONTENTS2 = new FineEnum("ACT_CONT2");
}
