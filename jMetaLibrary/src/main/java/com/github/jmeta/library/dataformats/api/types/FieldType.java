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
 * @param <F>
 */
public class FieldType<F> {

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((id == null) ? 0 : id.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      FieldType<?> other = (FieldType<?>) obj;
      if (id == null) {
         if (other.id != null)
            return false;
      } else if (!id.equals(other.id))
         return false;
      return true;
   }

   /**
    *
    */
   public static final FieldType<byte[]> BINARY = new FieldType<>("BINARY");

   @Override
   public String toString() {
      return "FieldType [id=" + id + "]";
   }

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

   public String getId() {
      return id;
   }

   private final String id;

   /**
    * Creates a new {@link FieldType}.
    *
    * @param id
    */
   private FieldType(String id) {
      Reject.ifNull(id, "id");
      this.id = id;
   }

}
