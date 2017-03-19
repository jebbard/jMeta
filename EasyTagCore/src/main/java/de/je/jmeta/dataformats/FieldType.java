/**
 *
 * {@link FieldType}.java
 *
 * @author Jens Ebert
 *
 * @date 04.01.2011
 */

package de.je.jmeta.dataformats;

import de.je.util.javautil.common.extenum.AbstractExtensibleEnum;
import de.je.util.javautil.common.flags.Flags;

/**
 * {@link FieldType}
 *
 * @param <T>
 */
public class FieldType<T> extends AbstractExtensibleEnum<FieldType<T>> {

   /**
    *
    */
   public static final FieldType<BinaryValue> BINARY = new FieldType<>(
      "BINARY");

   /**
    *
    */
   public static final FieldType<Long> UNSIGNED_WHOLE_NUMBER = new FieldType<>(
      "UNSIGNED WHOLE NUMBER");

   /**
    *
    */
   public static final FieldType<String> STRING = new FieldType<>("STRING");

   /**
    *
    */
   public static final FieldType<Flags> FLAGS = new FieldType<>("FLAGS");

   /**
    *
    */
   public static final FieldType<?> ENUMERATED = new FieldType<>("ENUMERATED");

   /**
    *
    */
   public static final FieldType<?> ANY = new FieldType<>("ANY");

   /**
    * Creates a new {@link FieldType}.
    *
    * @param id
    */
   protected FieldType(String id) {
      super(id);
   }

}
