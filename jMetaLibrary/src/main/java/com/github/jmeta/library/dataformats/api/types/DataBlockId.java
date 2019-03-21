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
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * Identifies a data block within its {@link DataFormatSpecification}. Ids are hierarchical in a sense that they consist
 * of segments, and each segment is called a local id. It is a string that contains only alphanumerical characters. A
 * {@link DataBlockId} is called the child another one if it just appends one or several local id segments to it. The
 * other {@link DataBlockId} is then called its parent.
 */
public class DataBlockId {

   private final String globalId;

   private final List<String> idSegments = new ArrayList<>();

   private final ContainerDataFormat dataFormat;

   private final DataBlockId parentId;

   /**
    * A default local id for a {@link PhysicalDataBlockType#HEADER} data block.
    */
   public static final String DEFAULT_HEADER_ID = "header";

   /**
    * A default local id for a {@link PhysicalDataBlockType#FOOTER} data block.
    */
   public static final String DEFAULT_FOOTER_ID = "footer";

   /**
    * A default local id for a {@link PhysicalDataBlockType#FIELD_BASED_PAYLOAD} or
    * {@link PhysicalDataBlockType#CONTAINER_BASED_PAYLOAD} data block.
    */
   public static final String DEFAULT_PAYLOAD_ID = "payload";

   /**
    * A default local id for an id {@link PhysicalDataBlockType#FIELD} data block.
    */
   public static final String DEFAULT_FIELD_ID_ID = "id";

   /**
    * A default local id for a size {@link PhysicalDataBlockType#FIELD} data block.
    */
   public static final String DEFAULT_FIELD_SIZE_ID = "size";

   /**
    * A default local id for a flags {@link PhysicalDataBlockType#FIELD} data block.
    */
   public static final String DEFAULT_FIELD_FLAGS_ID = "flags";

   /**
    * Segment separator character.
    */
   public static final String SEGMENT_SEPARATOR = ".";

   /**
    * Creates a new {@link DataBlockId} starting from the parent {@link DataBlockId} and the local id.
    *
    * @param parentId
    *           The parent {@link DataBlockId}, must not be null
    * @param localId
    *           The local id, must not be null
    */
   public DataBlockId(DataBlockId parentId, String localId) {
      Reject.ifNull(localId, "localId");
      Reject.ifNull(parentId, "parent");

      idSegments.addAll(parentId.getIdSegments());
      idSegments.add(localId);
      globalId = computeGlobalId(idSegments);
      this.parentId = parentId;
      dataFormat = parentId.getDataFormat();
   }

   /**
    * Creates a new {@link DataBlockId} from a list of segments.
    *
    * @param dataFormat
    *           The {@link ContainerDataFormat} this {@link DataBlockId} belongs to, must not be null
    * @param segments
    *           The list of segments, must not be null
    */
   public DataBlockId(ContainerDataFormat dataFormat, List<String> segments) {
      Reject.ifNull(dataFormat, "dataFormat");
      Reject.ifNull(segments, "segments");
      Reject.ifTrue(segments.isEmpty(), "segments.isEmpty()");

      idSegments.addAll(segments);
      globalId = computeGlobalId(idSegments);
      this.dataFormat = dataFormat;
      parentId = parentIdFromSegments(idSegments);
   }

   /**
    * Creates a new {@link DataBlockId} from a global id string.
    *
    * @param dataFormat
    *           The {@link ContainerDataFormat} this {@link DataBlockId} belongs to, must not be null
    * @param globalId
    *           The global id, must not be null
    */
   public DataBlockId(ContainerDataFormat dataFormat, String globalId) {
      Reject.ifNull(dataFormat, "dataFormat");
      Reject.ifNull(globalId, "globalId");

      this.globalId = globalId;
      this.dataFormat = dataFormat;
      idSegments.addAll(Arrays.asList(globalId.split(Pattern.quote(SEGMENT_SEPARATOR))));
      parentId = parentIdFromSegments(idSegments);
   }

   /**
    * @return the global id
    */
   public String getGlobalId() {
      return globalId;
   }

   /**
    * @return the local id
    */
   public String getLocalId() {
      return idSegments.get(idSegments.size() - 1);
   }

   /**
    * @return the id segments, ordered hierarchically from top-level parent down to the leaf child
    */
   public List<String> getIdSegments() {
      return Collections.unmodifiableList(idSegments);
   }

   /**
    * @return the {@link ContainerDataFormat} this {@link DataBlockId} belongs to
    */
   public ContainerDataFormat getDataFormat() {
      return dataFormat;
   }

   /**
    * @return the parent {@link DataBlockId} or null if this is a top-level {@link DataBlockId}
    */
   public DataBlockId getParentId() {
      return parentId;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return getClass().getSimpleName() + "[" + "globalId=" + globalId + "]";
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + (dataFormat == null ? 0 : dataFormat.hashCode());
      result = prime * result + (globalId == null ? 0 : globalId.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {

      if (this == obj) {
         return true;
      }
      if (obj == null) {
         return false;
      }
      if (getClass() != obj.getClass()) {
         return false;
      }
      DataBlockId other = (DataBlockId) obj;
      if (dataFormat == null) {
         if (other.dataFormat != null) {
            return false;
         }
      } else if (!dataFormat.equals(other.dataFormat)) {
         return false;
      }
      if (globalId == null) {
         if (other.globalId != null) {
            return false;
         }
      } else if (!globalId.equals(other.globalId)) {
         return false;
      }
      return true;
   }

   private DataBlockId parentIdFromSegments(List<String> segments) {
      if (segments.size() == 1) {
         return null;
      }

      return new DataBlockId(dataFormat, segments.subList(0, segments.size() - 1));
   }

   /**
    * Computes the global id from a list of segments.
    *
    * @param segmentList
    *           The list of segments.
    * @return The global id.
    */
   private static String computeGlobalId(List<String> segmentList) {
      return segmentList.stream().collect(Collectors.joining(SEGMENT_SEPARATOR));
   }
}
