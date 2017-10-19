/**
 * {@link DataTransformationType}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.type;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 */
public class DataTransformationType {

   /**
    * Creates a new {@link DataTransformationType}.
    * 
    * @param id
    * @param affectedContainers
    * @param isBuiltIn
    * @param readOrder
    * @param writeOrder
    */
   public DataTransformationType(String id,
      List<DataBlockId> affectedContainers, boolean isBuiltIn, int readOrder,
      int writeOrder) {
      m_affectedContainers.addAll(affectedContainers);
      m_name = id;
      m_isBuiltIn = isBuiltIn;
      m_readOrder = readOrder;
      m_writeOrder = writeOrder;
   }

   /**
    * @return the name of the transformation
    */
   public String getName() {

      return m_name;
   }

   /**
    * @return true if built-in, false if user-provided
    */
   public boolean isBuiltIn() {

      return m_isBuiltIn;
   }

   /**
    * @return the read order
    */
   public int getReadOrder() {

      return m_readOrder;
   }

   /**
    * @return the write order
    */
   public int getWriteOrder() {

      return m_writeOrder;
   }

   /**
    * Returns affectedContainers
    *
    * @return affectedContainers
    */
   public Set<DataBlockId> getAffectedContainers() {

      return Collections.unmodifiableSet(m_affectedContainers);
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return "DataTransformationType [m_affectedContainers="
         + m_affectedContainers + ", m_isBuiltIn=" + m_isBuiltIn + ", m_name="
         + m_name + ", m_readOrder=" + m_readOrder + ", m_writeOrder="
         + m_writeOrder + "]";
   }

   private final Set<DataBlockId> m_affectedContainers = new HashSet<>();

   private final String m_name;

   private final boolean m_isBuiltIn;

   private final int m_readOrder;

   private final int m_writeOrder;

}
