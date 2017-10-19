/**
 *
 * {@link FineEnum2}.java
 *
 * @author Jens Ebert
 *
 * @date 18.06.2009
 *
 */
package de.je.util.javautil.common.extenum;

/**
 * {@link FineEnum2} is just a dummy class for testing {@link AbstractExtensibleEnum}.
 */
public class FineEnum2 extends FineEnum {

   /**
    * Private constructor
    *
    * @param id
    *           The id
    */
   private FineEnum2(String id) {
      super(id);
   }

   /**
    * One enum instance, intentionally hiding field from superclass
    */
   @SuppressWarnings("hiding")
   public static final FineEnum2 ACTUAL_CONTENTS = new FineEnum2("XXX");
}
