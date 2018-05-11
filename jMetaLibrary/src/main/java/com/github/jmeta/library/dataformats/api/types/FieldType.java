/**
 *
 * {@link FieldType}.java
 *
 * @author Jens Ebert
 *
 * @date 04.01.2011
 */

package com.github.jmeta.library.dataformats.api.types;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FieldType}
 *
 * @param <T>
 */
public class FieldType<T> {

   /**
    *
    */
   public static final FieldType<byte[]> BINARY = new FieldType<>("BINARY");

   /**
    *
    */
   public static final FieldType<Long> UNSIGNED_WHOLE_NUMBER = new FieldType<>("UNSIGNED WHOLE NUMBER");

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

   public String getId() {
      return id;
   }

   private final String id;

   /**
    * Creates a new {@link FieldType}.
    *
    * @param id
    */
   protected FieldType(String id) {
      Reject.ifNull(id, "id");
      this.id = id;
   }

}
