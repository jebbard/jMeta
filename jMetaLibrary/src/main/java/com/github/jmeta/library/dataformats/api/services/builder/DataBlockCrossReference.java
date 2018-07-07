/**
 *
 * {@link DataBlockCrossReference}.java
 *
 * @author Jens Ebert
 *
 * @date 14.06.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import com.github.jmeta.library.dataformats.api.types.FieldFunction;

/**
 * {@link DataBlockCrossReference} allows to assign a symbolic name to a data block and cross-reference this block from
 * another one using this symbolic name, e.g. for {@link FieldFunction}s. This eliminates the need to reference the
 * complete global id (and thus, duplicating it) while it is actually built up from individual segments only.
 */
public class DataBlockCrossReference {

   private final String refId;

   /**
    * Creates a new {@link DataBlockCrossReference}.
    * 
    * @param refId
    *           The reference's id, must be unique in the current build process
    */
   public DataBlockCrossReference(String refId) {
      this.refId = refId;
   }

   public String getRefId() {
      return refId;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((refId == null) ? 0 : refId.hashCode());
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
      DataBlockCrossReference other = (DataBlockCrossReference) obj;
      if (refId == null) {
         if (other.refId != null)
            return false;
      } else if (!refId.equals(other.refId))
         return false;
      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "DataBlockCrossReference [refId=" + refId + "]";
   }
}
