/**
 * {@link DataBlockId}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class DataBlockId {

   /**
    *
    */
   public static final String SEGMENT_SEPARATOR = ".";

   /**
    * Creates a new {@link DataBlockId}.
    * 
    * @param dataFormat
    * @param parent
    * @param localId
    */
   public DataBlockId(ContainerDataFormat dataFormat, DataBlockId parent,
      String localId) {
      Reject.ifNull(localId, "localId");
      Reject.ifNull(parent, "parent");

      m_idSegments.addAll(parent.getIdSegments());
      m_idSegments.add(localId);

      m_globalId = computeGlobalId(m_idSegments);
      m_dataFormat = dataFormat;
   }

   /**
    * Creates a new {@link DataBlockId}.
    * 
    * @param dataFormat
    * @param segments
    */
   public DataBlockId(ContainerDataFormat dataFormat, List<String> segments) {
      Reject.ifNull(segments, "segments");
      Reject.ifTrue(segments.isEmpty(),
         "segments.isEmpty()");

      m_idSegments.addAll(segments);
      m_globalId = computeGlobalId(m_idSegments);
      m_dataFormat = dataFormat;
   }

   /**
    * Creates a new {@link DataBlockId}.
    * 
    * @param dataFormat
    * @param globalId
    */
   public DataBlockId(ContainerDataFormat dataFormat, String globalId) {
      Reject.ifNull(globalId, "globalId");

      m_globalId = globalId;
      m_idSegments
         .addAll(Arrays.asList(m_globalId.split("\\" + SEGMENT_SEPARATOR)));
      m_dataFormat = dataFormat;
   }

   /**
    * @return the global id
    */
   public String getGlobalId() {

      return m_globalId;
   }

   /**
    * @return the local id
    */
   public String getLocalId() {

      return m_idSegments.get(m_idSegments.size() - 1);
   }

   /**
    * @return the id segments
    */
   public List<String> getIdSegments() {

      return Collections.unmodifiableList(m_idSegments);
   }

   /**
    * Returns dataFormat
    *
    * @return dataFormat
    */
   public ContainerDataFormat getDataFormat() {

      return m_dataFormat;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getSimpleName() + "[" + "globalId=" + m_globalId + "]";
   }

   /**
    * Computes the global id from a list of segments.
    *
    * @param segmentList
    *           The list of segments.
    * @return The global id.
    */
   private static String computeGlobalId(List<String> segmentList) {

      StringBuffer buf = new StringBuffer();

      for (int i = 0; i < segmentList.size(); ++i) {
         String segment = segmentList.get(i);

         buf.append(segment);

         if (i != segmentList.size() - 1)
            buf.append(SEGMENT_SEPARATOR);
      }

      return buf.toString();
   }

   private final String m_globalId;

   private final List<String> m_idSegments = new ArrayList<>();

   private final ContainerDataFormat m_dataFormat;

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result
         + ((m_dataFormat == null) ? 0 : m_dataFormat.hashCode());
      result = prime * result
         + ((m_globalId == null) ? 0 : m_globalId.hashCode());
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
      DataBlockId other = (DataBlockId) obj;
      if (m_dataFormat == null) {
         if (other.m_dataFormat != null)
            return false;
      } else if (!m_dataFormat.equals(other.m_dataFormat))
         return false;
      if (m_globalId == null) {
         if (other.m_globalId != null)
            return false;
      } else if (!m_globalId.equals(other.m_globalId))
         return false;
      return true;
   }
}
