/**
 * {@link FieldFunctionType}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import com.github.jmeta.utility.dbc.api.services.Reject;

// TODO: SIZE_OF is problematic - See Vorbis user comment "length" - current implementation won't work

/**
 * {@link FieldFunctionType}
 *
 * @param <T>
 */
public class FieldFunctionType<T> {

   @Override
   public String toString() {
      return "FieldFunctionType [id=" + id + "]";
   }

   /**
    *
    */
   public static final FieldFunctionType<Long> SIZE_OF = new FieldFunctionType<>("SIZE_OF");

   /**
    *
    */
   public static final FieldFunctionType<Long> COUNT_OF = new FieldFunctionType<>("COUNT_OF");

   /**
    *
    */
   public static final FieldFunctionType<ByteOrder> BYTE_ORDER_OF = new FieldFunctionType<>("BYTE_ORDER_OF");

   /**
    *
    */
   public static final FieldFunctionType<Charset> CHARACTER_ENCODING_OF = new FieldFunctionType<>(
      "CHARACTER_ENCODING_OF");

   /**
    *
    */
   public static final FieldFunctionType<Boolean> PRESENCE_OF = new FieldFunctionType<>("PRESENCE_OF");

   /**
    *
    */
   public static final FieldFunctionType<String> ID_OF = new FieldFunctionType<>("ID_OF");

   public String getId() {
      return id;
   }

   private final String id;

   /**
    * Creates an instance of {@link FieldFunctionType}.
    *
    * @param id
    *           The id of the enumeration literal.
    */
   protected FieldFunctionType(String id) {
      Reject.ifNull(id, "id");
      this.id = id;
   }
}
