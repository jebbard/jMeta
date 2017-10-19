/**
 * {@link LocationProperties}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.je.util.javautil.common.err.Reject;

/**
 *
 */
public class LocationProperties {

   /**
    * Creates a new {@link LocationProperties}.
    * 
    * @param byteOffset
    * @param minOccurrences
    * @param maxOccurrences
    * @param byteInterval
    * @param predecessors
    * @param successors
    */
   public LocationProperties(long byteOffset, int minOccurrences,
      int maxOccurrences, long byteInterval, List<DataBlockId> predecessors,
      List<DataBlockId> successors) {
      Reject.ifNull(successors, "successors");
      Reject.ifNull(predecessors, "predecessors");

      m_byteOffset = byteOffset;
      m_maxOccurrences = maxOccurrences;
      m_minOccurrences = minOccurrences;
      m_byteInterval = byteInterval;
      m_predecessors.addAll(predecessors);
      m_successors.addAll(successors);
   }

   /**
    * @return byte offset
    */
   public long getByteOffset() {

      return m_byteOffset;
   }

   /**
    * @return max occurrences
    */
   public int getMaxOccurrences() {

      return m_maxOccurrences;
   }

   /**
    * @return min occurrences
    */
   public int getMinOccurrences() {

      return m_minOccurrences;
   }

   /**
    * @return the byte interval
    */
   public long getByteInterval() {

      return m_byteInterval;
   }

   /**
    * @return the predecessors
    */
   public List<DataBlockId> getPredecessors() {

      return Collections.unmodifiableList(m_predecessors);
   }

   /**
    * @return the successors
    */
   public List<DataBlockId> getSuccessors() {

      return Collections.unmodifiableList(m_successors);
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[" + ", byteOffset=" + m_byteOffset
         + ", maxOccurrences=" + m_maxOccurrences + ", minOccurrences="
         + m_minOccurrences + ", byteInterval=" + m_byteInterval
         + ", predecessors=" + m_predecessors + ", successors=" + m_successors
         + "]";
   }

   private final long m_byteOffset;

   private final int m_maxOccurrences;

   private final int m_minOccurrences;

   private final long m_byteInterval;

   private final List<DataBlockId> m_predecessors = new ArrayList<>();

   private final List<DataBlockId> m_successors = new ArrayList<>();

}
