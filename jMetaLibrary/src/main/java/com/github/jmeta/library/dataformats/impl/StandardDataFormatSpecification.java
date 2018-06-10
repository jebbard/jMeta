/**
 *
 * {@link StandardDataFormatSpecification}.java
 *
 * @author Jens Ebert
 *
 * @date 31.12.2010
 */

package com.github.jmeta.library.dataformats.impl;

import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_DEFAULT_NESTED_CONTAINER_MISSING;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_REFERENCING_WRONG_TYPE;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_INVALID_BYTE_ORDER;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_INVALID_CHARACTER_ENCODING;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_MAGIC_KEY_MISSING;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardDataFormatSpecification}
 *
 */
public class StandardDataFormatSpecification implements DataFormatSpecification {

   private static final String GENERIC_ID_REPLACE_PATTERN = "([^\\.]+)";

   private static final Pattern GENERIC_PLACEHOLDER_PATTERN = Pattern.compile("(\\$\\{.+?\\})");

   /**
    * Creates a new {@link StandardDataFormatSpecification}.
    * 
    * @param dataFormat
    * @param dataBlockDescriptions
    * @param topLevelDataBlockDescriptions
    * @param genericDataBlocks
    * @param supportedByteOrders
    * @param supportedCharacterEncodings
    * @param defaultNestedContainerId
    *           TODO
    */
   public StandardDataFormatSpecification(ContainerDataFormat dataFormat,
      Map<DataBlockId, DataBlockDescription> dataBlockDescriptions,
      List<DataBlockDescription> topLevelDataBlockDescriptions, Set<DataBlockId> genericDataBlocks,
      List<ByteOrder> supportedByteOrders, List<Charset> supportedCharacterEncodings,
      DataBlockId defaultNestedContainerId) {
      Reject.ifNull(dataBlockDescriptions, "dataBlockDescriptions");
      Reject.ifNull(topLevelDataBlockDescriptions, "topLevelDataBlockDescriptions");
      Reject.ifNull(dataFormat, "dataFormat");
      Reject.ifNull(supportedCharacterEncodings, "supportedCharacterEncodings");
      Reject.ifNull(supportedByteOrders, "supportedByteOrders");
      Reject.ifNull(genericDataBlocks, "genericDataBlocks");

      initGenericIdPatterns(genericDataBlocks);

      m_supportedByteOrders.addAll(supportedByteOrders);
      m_supportedCharacterEncodings.addAll(supportedCharacterEncodings);
      m_dataFormat = dataFormat;
      m_topLevelDataBlocks.addAll(topLevelDataBlockDescriptions);
      m_dataBlockDescriptions.putAll(dataBlockDescriptions);
      this.defaultNestedContainerId = defaultNestedContainerId;

      validateSpecification();
   }

   /**
    * @param defaultNestedContainerId
    */
   private void validateSpecification() {
      validateDefaultNestedContainerDefined();
      validateTopLevelMagicKeys();
      validateCharacterEncodingsAndByteOrders();
      validateFieldFunctions();
   }

   /**
    * 
    */
   private void validateFieldFunctions() {

      Stream<DataBlockDescription> descriptionStream = m_dataBlockDescriptions.values().stream();

      List<FieldFunction> fieldFunctions = descriptionStream
         .filter(desc -> desc.getPhysicalType() == PhysicalDataBlockType.FIELD)
         .map(desc -> desc.getFieldProperties().getFieldFunctions()).flatMap(Collection::stream)
         .collect(Collectors.toList());

      for (FieldFunction fieldFunction : fieldFunctions) {
         FieldFunctionType<?> type = fieldFunction.getFieldFunctionType();
         Set<DataBlockId> affectedIds = fieldFunction.getAffectedBlockIds();

         if (type == FieldFunctionType.ID_OF) {
            affectedIds.forEach(id -> {
               Set<PhysicalDataBlockType> allowedTypes = Set.of(PhysicalDataBlockType.CONTAINER);

               checkFieldFunctionTargetType(type, id, allowedTypes);
            });
         } else if (type == FieldFunctionType.SIZE_OF) {
            // TODO prüfen, ob alle referenzierten Blöcke zusammenhängen?
            affectedIds.forEach(id -> {

            });
         } else if (type == FieldFunctionType.COUNT_OF) {
            affectedIds.forEach(id -> {
               Set<PhysicalDataBlockType> allowedTypes = Set.of(PhysicalDataBlockType.FIELD,
                  PhysicalDataBlockType.HEADER, PhysicalDataBlockType.FOOTER, PhysicalDataBlockType.CONTAINER);

               checkFieldFunctionTargetType(type, id, allowedTypes);
            });
         }
      }
   }

   /**
    * @param fieldFunctionType
    * @param targetId
    * @param allowedTargetTypes
    */
   private void checkFieldFunctionTargetType(FieldFunctionType<?> fieldFunctionType, DataBlockId targetId,
      Set<PhysicalDataBlockType> allowedTargetTypes) {
      PhysicalDataBlockType affectedIdType = getDataBlockDescription(targetId).getPhysicalType();

      if (!allowedTargetTypes.contains(affectedIdType)) {
         throw new InvalidSpecificationException(VLD_FIELD_FUNC_REFERENCING_WRONG_TYPE,
            getDefaultNestedContainerDescription(), fieldFunctionType, allowedTargetTypes, targetId, affectedIdType);
      }
   }

   /**
    * 
    */
   private void validateCharacterEncodingsAndByteOrders() {
      List<DataBlockDescription> allFields = m_dataBlockDescriptions.values().stream()
         .filter(desc -> desc.getPhysicalType() == PhysicalDataBlockType.FIELD).collect(Collectors.toList());

      for (DataBlockDescription fieldDesc : allFields) {
         Charset fixedCharacterEncoding = fieldDesc.getFieldProperties().getFixedCharacterEncoding();
         if (fixedCharacterEncoding != null) {
            if (!getSupportedCharacterEncodings().contains(fixedCharacterEncoding)) {
               throw new InvalidSpecificationException(VLD_INVALID_CHARACTER_ENCODING, fieldDesc,
                  fixedCharacterEncoding, getSupportedCharacterEncodings());
            }
         }

         ByteOrder fixedByteOrder = fieldDesc.getFieldProperties().getFixedByteOrder();
         if (fixedByteOrder != null) {
            if (!getSupportedByteOrders().contains(fixedByteOrder)) {
               throw new InvalidSpecificationException(VLD_INVALID_BYTE_ORDER, fieldDesc, fixedByteOrder,
                  getSupportedByteOrders());
            }
         }
      }
   }

   @Override
   public DataBlockDescription getDefaultNestedContainerDescription() {

      if (defaultNestedContainerId == null) {
         return null;
      }

      return getDataBlockDescription(defaultNestedContainerId);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getDataBlockDescription(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public DataBlockDescription getDataBlockDescription(DataBlockId id) {

      Reject.ifNull(id, "id");
      Reject.ifFalse(specifiesBlockWithId(id), "specifiesBlockWithId(id)");

      // The requested id is an unspecified id that must match a generic id
      if (!m_dataBlockDescriptions.containsKey(id)) {
         return createConcreteDescription(id);
      }

      return m_dataBlockDescriptions.get(id);
   }

   /**
    * @param id
    * @return
    */
   private DataBlockDescription createConcreteDescription(DataBlockId id) {
      DataBlockId matchingGenericId = getMatchingGenericId(id);

      DataBlockDescription genericDescription = getDataBlockDescription(matchingGenericId);

      Matcher matcher = Pattern.compile(m_genericDataBlocks.get(matchingGenericId)).matcher(id.getGlobalId());

      List<String> matchingStrings = new ArrayList<>();

      if (matcher.find()) {
         // Ignore group zero (= the whole match) which is also NOT included in Matcher.groupCount()
         for (int i = 1; i < matcher.groupCount() + 1; i++)
            matchingStrings.add(matcher.group(i));
      }

      // Replace child ids
      List<DataBlockDescription> realChildren = new ArrayList<>();

      for (int i = 0; i < genericDescription.getOrderedChildren().size(); ++i) {
         DataBlockId childId = genericDescription.getOrderedChildren().get(i).getId();

         String replacedChildId = childId.getGlobalId();

         for (int j = 0; j < matchingStrings.size(); ++j) {
            String matchingString = matchingStrings.get(j);

            replacedChildId = replacedChildId.replaceFirst(GENERIC_PLACEHOLDER_PATTERN.pattern(), matchingString);
         }

         DataBlockId replacedChildDataBlockId = new DataBlockId(m_dataFormat, replacedChildId);

         realChildren.add(createConcreteDescription(replacedChildDataBlockId));
      }
      return new DataBlockDescription(id, genericDescription.getName(), "Unspecified data block",
         genericDescription.getPhysicalType(), realChildren, genericDescription.getFieldProperties(),
         genericDescription.getMinimumOccurrences(), genericDescription.getMaximumOccurrences(),
         genericDescription.getMinimumByteLength(), genericDescription.getMaximumByteLength(), false);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getDataFormat()
    */
   @Override
   public ContainerDataFormat getDataFormat() {

      return m_dataFormat;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getDefaultByteOrder()
    */
   @Override
   public ByteOrder getDefaultByteOrder() {

      return m_supportedByteOrders.get(0);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getDefaultCharacterEncoding()
    */
   @Override
   public Charset getDefaultCharacterEncoding() {

      return m_supportedCharacterEncodings.get(0);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getSupportedByteOrders()
    */
   @Override
   public List<ByteOrder> getSupportedByteOrders() {

      return Collections.unmodifiableList(m_supportedByteOrders);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getSupportedCharacterEncodings()
    */
   @Override
   public List<Charset> getSupportedCharacterEncodings() {

      return Collections.unmodifiableList(m_supportedCharacterEncodings);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getTopLevelDataBlockDescriptions()
    */
   @Override
   public List<DataBlockDescription> getTopLevelDataBlockDescriptions() {

      return Collections.unmodifiableList(m_topLevelDataBlocks);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#specifiesBlockWithId(com.github.jmeta.library.dataformats.api.types.DataBlockId)
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

   private void validateTopLevelMagicKeys() {
      for (Iterator<DataBlockDescription> iterator = m_topLevelDataBlocks.iterator(); iterator.hasNext();) {
         DataBlockDescription dataBlockDesc = iterator.next();

         if (dataBlockDesc.getHeaderMagicKeys().isEmpty()) {
            throw new InvalidSpecificationException(VLD_MAGIC_KEY_MISSING, dataBlockDesc);
         }
      }
   }

   private void validateDefaultNestedContainerDefined() {
      for (Iterator<DataBlockDescription> iterator = m_topLevelDataBlocks.iterator(); iterator.hasNext();) {
         DataBlockDescription topLevelDesc = iterator.next();

         List<DataBlockDescription> descs = topLevelDesc
            .getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD);

         if (descs.size() > 0) {
            if (defaultNestedContainerId == null) {
               throw new InvalidSpecificationException(VLD_DEFAULT_NESTED_CONTAINER_MISSING, topLevelDesc);
            }
         }
      }
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

   private final DataBlockId defaultNestedContainerId;

   private final Map<DataBlockId, String> m_genericDataBlocks = new HashMap<>();

   private final List<ByteOrder> m_supportedByteOrders = new ArrayList<>();

   private final List<Charset> m_supportedCharacterEncodings = new ArrayList<>();

   private final ContainerDataFormat m_dataFormat;

   private final List<DataBlockDescription> m_topLevelDataBlocks = new ArrayList<>();

   private final Map<DataBlockId, DataBlockDescription> m_dataBlockDescriptions = new HashMap<>();
}
