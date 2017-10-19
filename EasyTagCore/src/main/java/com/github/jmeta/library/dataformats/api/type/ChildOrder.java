/**
 * {@link ChildOrder}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.type;

import de.je.util.javautil.common.extenum.AbstractExtensibleEnum;

/**
 *
 */
public class ChildOrder extends AbstractExtensibleEnum<ChildOrder> {

   /**
   		 *
   		 */
   public static final ChildOrder SEQUENTIAL = new ChildOrder("SEQUENTIAL");

   /**
   		 *
   		 */
   public static final ChildOrder UNORDERED = new ChildOrder("UNORDERED");

   /**
    * Creates an instance of {@link ChildOrder}.
    * 
    * @param id
    *           The id of the enumeration literal.
    */
   protected ChildOrder(String id) {
      super(id);
   }
}
