/**
 *
 * {@link StandardDataFormatSpecification}.java
 *
 * @author Jens Ebert
 *
 * @date 31.12.2010
 */

package com.github.jmeta.library.dataformats.api.service;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.jmeta.library.dataformats.api.type.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.type.DataBlockId;
import com.github.jmeta.library.dataformats.api.type.DataFormat;
import com.github.jmeta.library.dataformats.api.type.DataTransformationType;
import com.github.jmeta.library.dataformats.api.type.LocationProperties;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardDataFormatSpecification}
 *
 */
public class StandardDataFormatSpecification implements IDataFormatSpecification {

   private static final String GENERIC_ID_REPLACE_PATTERN = "([^\\.]+)";

   private static final Pattern GENERIC_PLACEHOLDER_PATTERN = Pattern.compile("(\\$\\{.+?\\})");

   /**
    * Creates a new {@link StandardDataFormatSpecification}.
    * 
    * @param dataFormat
    * @param dataBlockDescriptions
    * @param topLevelDataBlockIds
    * @param genericDataBlocks
    * @param paddingDataBlocks
    * @param supportedByteOrders
    * @param supportedCharacterEncodings
    * @param transformations
    */
   public StandardDataFormatSpecification(DataFormat dataFormat,
      Map<DataBlockId, DataBlockDescription> dataBlockDescriptions, Set<DataBlockId> topLevelDataBlockIds,
      Set<DataBlockId> genericDataBlocks, Set<DataBlockId> paddingDataBlocks, List<ByteOrder> supportedByteOrders,
      List<Charset> supportedCharacterEncodings, List<DataTransformationType> transformations) {
      Reject.ifNull(transformations, "transformations");
      Reject.ifNull(dataBlockDescriptions, "dataBlockDescriptions");
      Reject.ifNull(topLevelDataBlockIds, "topLevelDataBlockIds");
      Reject.ifNull(dataFormat, "dataFormat");
      Reject.ifNull(supportedCharacterEncodings, "supportedCharacterEncodings");
      Reject.ifNull(supportedByteOrders, "supportedByteOrders");
      Reject.ifNull(paddingDataBlocks, "paddingDataBlocks");
      Reject.ifNull(genericDataBlocks, "genericDataBlocks");

      initGenericIdPatterns(genericDataBlocks);

      m_paddingDataBlocks.addAll(paddingDataBlocks);
      m_supportedByteOrders.addAll(supportedByteOrders);
      m_supportedCharacterEncodings.addAll(supportedCharacterEncodings);
      m_dataFormat = dataFormat;
      m_topLevelDataBlockIds.addAll(topLevelDataBlockIds);
      m_dataBlockDescriptions.putAll(dataBlockDescriptions);
      m_transformations.addAll(transformations);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#getDataBlockDescription(com.github.jmeta.library.dataformats.api.type.DataBlockId)
    */
   @Override
   public DataBlockDescription getDataBlockDescription(DataBlockId id) {

      Reject.ifNull(id, "id");
      Reject.ifFalse(specifiesBlockWithId(id), "specifiesBlockWithId(id)");

      // The requested id is an unspecified id that must match a generic id
      if (!m_dataBlockDescriptions.containsKey(id)) {
         DataBlockId matchingGenericId = getMatchingGenericId(id);

         DataBlockDescription genericDescription = getDataBlockDescription(matchingGenericId);

         Matcher matcher = Pattern.compile(m_genericDataBlocks.get(matchingGenericId)).matcher(id.getGlobalId());

         List<String> matchingStrings = new ArrayList<>();

         if (matcher.find()) {
            // Ignore group zero (= the whole match) which is also NOT included in Matcher.groupCount()
            for (int i = 1; i < matcher.groupCount() + 1; i++)
               matchingStrings.add(matcher.group(i));
         }

         // Replace parent ids in location properties
         Map<DataBlockId, LocationProperties> locationProps = new HashMap<>();

         for (Iterator<DataBlockId> iterator = genericDescription.getAllParentsForLocationProperties()
            .iterator(); iterator.hasNext();) {
            DataBlockId parentId = iterator.next();
            String replacedParentId = parentId.getGlobalId();

            for (int j = 0; j < matchingStrings.size(); ++j) {
               String matchingString = matchingStrings.get(j);

               replacedParentId = replacedParentId.replaceFirst(GENERIC_PLACEHOLDER_PATTERN.pattern(), matchingString);
            }

            locationProps.put(new DataBlockId(m_dataFormat, replacedParentId),
               genericDescription.getLocationPropertiesForParent(parentId));
         }

         // Replace child ids
         List<DataBlockId> realChildIds = new ArrayList<>();

         for (int i = 0; i < genericDescription.getOrderedChildIds().size(); ++i) {
            DataBlockId childId = genericDescription.getOrderedChildIds().get(i);

            String replacedChildId = childId.getGlobalId();

            for (int j = 0; j < matchingStrings.size(); ++j) {
               String matchingString = matchingStrings.get(j);

               replacedChildId = replacedChildId.replaceFirst(GENERIC_PLACEHOLDER_PATTERN.pattern(), matchingString);
            }

            realChildIds.add(new DataBlockId(m_dataFormat, replacedChildId));
         }
         return new DataBlockDescription(id, genericDescription.getName(), "Unspecified data block",
            genericDescription.getPhysicalType(), realChildIds, genericDescription.getChildOrder(),
            genericDescription.getFieldProperties(), locationProps, genericDescription.getMinimumByteLength(),
            genericDescription.getMaximumByteLength(), genericDescription.getMagicKeys(), null);
      }

      return m_dataBlockDescriptions.get(id);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#getDataFormat()
    */
   @Override
   public DataFormat getDataFormat() {

      return m_dataFormat;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#getDefaultByteOrder()
    */
   @Override
   public ByteOrder getDefaultByteOrder() {

      return m_supportedByteOrders.get(0);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#getDefaultCharacterEncoding()
    */
   @Override
   public Charset getDefaultCharacterEncoding() {

      return m_supportedCharacterEncodings.get(0);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#getDataTransformations()
    */
   @Override
   public List<DataTransformationType> getDataTransformations() {

      return Collections.unmodifiableList(m_transformations);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#getSupportedByteOrders()
    */
   @Override
   public List<ByteOrder> getSupportedByteOrders() {

      return Collections.unmodifiableList(m_supportedByteOrders);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#getSupportedCharacterEncodings()
    */
   @Override
   public List<Charset> getSupportedCharacterEncodings() {

      return Collections.unmodifiableList(m_supportedCharacterEncodings);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#getTopLevelDataBlockIds()
    */
   @Override
   public Set<DataBlockId> getTopLevelDataBlockIds() {

      return Collections.unmodifiableSet(m_topLevelDataBlockIds);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#isGeneric(DataBlockId)
    */
   @Override
   public boolean isGeneric(DataBlockId id) {

      Reject.ifFalse(specifiesBlockWithId(id), "specifiesBlockWithId(id)");

      return m_genericDataBlocks.containsKey(id);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#getPaddingBlockIds()
    */
   @Override
   public Set<DataBlockId> getPaddingBlockIds() {

      return Collections.unmodifiableSet(m_paddingDataBlocks);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification#specifiesBlockWithId(com.github.jmeta.library.dataformats.api.type.DataBlockId)
    */
   @Override
   public boolean specifiesBlockWithId(DataBlockId id) {

      Reject.ifNull(id, "id");

      if (!m_dataBlockDescriptions.containsKey(id))
         return getMatchingGenericId(id) != null;

      return m_dataBlockDescriptions.containsKey(id);
   }

   @Override
   public DataBlockId getMatchingGenericId(DataBlockId id) {

      if (m_genericDataBlocks.containsKey(id))
         return id;

      for (Iterator<DataBlockId> iterator = m_genericDataBlocks.keySet().iterator(); iterator.hasNext();) {
         DataBlockId nextId = iterator.next();
         String nextPattern = m_genericDataBlocks.get(nextId);

         if (id.getGlobalId().matches(nextPattern))
            return nextId;
      }

      return null;
   }

   private void initGenericIdPatterns(Set<DataBlockId> genericDataBlocks) {

      for (Iterator<DataBlockId> iterator = genericDataBlocks.iterator(); iterator.hasNext();) {
         DataBlockId genericBlockId = iterator.next();

         final String idString = genericBlockId.getGlobalId();
         Matcher matcher = GENERIC_PLACEHOLDER_PATTERN.matcher(idString);

         StringBuffer idPattern = new StringBuffer();

         int matchEndIndex = 0;

         while (matcher.find()) {
            int matchStartIndex = matcher.start(0);
            matchEndIndex = matcher.end(0);

            idPattern.append(idString.substring(0, matchStartIndex));
            idPattern.append(GENERIC_ID_REPLACE_PATTERN);
         }

         idPattern.append(idString.substring(matchEndIndex, idString.length()));

         m_genericDataBlocks.put(genericBlockId, idPattern.toString());
      }
   }

   private final Map<DataBlockId, String> m_genericDataBlocks = new HashMap<>();

   private final Set<DataBlockId> m_paddingDataBlocks = new HashSet<>();

   private final List<ByteOrder> m_supportedByteOrders = new ArrayList<>();

   private final List<Charset> m_supportedCharacterEncodings = new ArrayList<>();

   private final DataFormat m_dataFormat;

   private final Set<DataBlockId> m_topLevelDataBlockIds = new HashSet<>();

   private final Map<DataBlockId, DataBlockDescription> m_dataBlockDescriptions = new HashMap<>();

   private final List<DataTransformationType> m_transformations = new ArrayList<>();
}
