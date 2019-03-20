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
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_OPTIONAL_FIELD_PRESENCE_OF_MISSING;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_INVALID_BYTE_ORDER;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_INVALID_CHARACTER_ENCODING;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_MAGIC_KEY_MISSING;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.dataformats.api.types.PresenceOf;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardDataFormatSpecification}
 *
 */
public class StandardDataFormatSpecification implements DataFormatSpecification {

   private static final String GENERIC_ID_REPLACE_PATTERN = "([^\\.]+)";

   private static final Pattern GENERIC_PLACEHOLDER_PATTERN = Pattern.compile("(\\$\\{.+?\\})");

   private final Map<DataBlockId, String> m_genericDataBlocks = new HashMap<>();

   private final List<ByteOrder> m_supportedByteOrders = new ArrayList<>();

   private final List<Charset> m_supportedCharacterEncodings = new ArrayList<>();

   private final ContainerDataFormat m_dataFormat;

   private final List<DataBlockDescription> m_topLevelDataBlocks = new ArrayList<>();

   private final Map<DataBlockId, DataBlockDescription> m_dataBlockDescriptions = new HashMap<>();

   private final DataBlockId defaultNestedContainerId;

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
    * Validates all field functions and ensures the dynamic occurrence / size data blocks have a fitting field function
    */
   @SuppressWarnings("unchecked")
   private void validateFieldFunctions() {

      Map<DataBlockId, Object> fieldFunctions = getFieldFunctionMap();

      fieldFunctions.forEach((fieldIdWithFunctions, functionsForField) -> {
         for (AbstractFieldFunction<?> fieldFunction : (List<AbstractFieldFunction<?>>) functionsForField) {
            fieldFunction.validate(getDataBlockDescription(fieldIdWithFunctions), this);
         }
      });

      Map<DataBlockId, List<AbstractFieldFunction<?>>> fieldFunctionsByTargetId = getAllFieldFunctionsByTargetId();

      List<DataBlockDescription> dataBlocksWithDynamicOccurrences = m_dataBlockDescriptions.values().stream()
         .filter(desc -> desc.getMaximumOccurrences() != desc.getMinimumOccurrences()).collect(Collectors.toList());

      for (DataBlockDescription dataBlockWithDynamicOccurrences : dataBlocksWithDynamicOccurrences) {
         DataBlockId id = dataBlockWithDynamicOccurrences.getId();

         boolean hasFieldFunctionForId = fieldFunctionsByTargetId.containsKey(id);

         if (dataBlockWithDynamicOccurrences.isOptional()) {
            if (!hasFieldFunctionForId || !hasFieldFunctionOfType(fieldFunctionsByTargetId.get(id), PresenceOf.class)) {
               throw new InvalidSpecificationException(VLD_FIELD_FUNC_OPTIONAL_FIELD_PRESENCE_OF_MISSING,
                  dataBlockWithDynamicOccurrences);
            }
         } else {
            // TODO This does not work for the Ogg special case...
            // if (!hasFieldFunctionForId
            // || !hasFieldFunctionOfType(fieldFunctionsByTargetId.get(id), FieldFunctionType.COUNT_OF)) {
            // throw new InvalidSpecificationException(VLD_FIELD_FUNC_DYN_OCCUR_FIELD_COUNT_OF_MISSING,
            // dataBlockWithDynamicOccurrences);
            // }
         }

      }

      // TODO verify SIZE_OF exists
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecification#getAllFieldFunctionsByTargetId()
    */
   @SuppressWarnings("unchecked")
   @Override
   public Map<DataBlockId, List<AbstractFieldFunction<?>>> getAllFieldFunctionsByTargetId() {
      Map<DataBlockId, Object> fieldFunctions = getFieldFunctionMap();

      Map<DataBlockId, List<AbstractFieldFunction<?>>> fieldFunctionsByTargetId = new HashMap<>();

      fieldFunctions.forEach((fieldIdWithFunctions, functionsForField) -> {
         for (AbstractFieldFunction<?> fieldFunction : (List<AbstractFieldFunction<?>>) functionsForField) {
            List<DataBlockCrossReference> targetRefs = fieldFunction.getReferencedBlocks();

            for (DataBlockCrossReference targetRef : targetRefs) {
               if (!fieldFunctionsByTargetId.containsKey(targetRef.getId())) {
                  fieldFunctionsByTargetId.put(targetRef.getId(), new ArrayList<>());
               }

               fieldFunctionsByTargetId.get(targetRef.getId()).add(fieldFunction);
            }
         }
      });
      return fieldFunctionsByTargetId;
   }

   private Map<DataBlockId, Object> getFieldFunctionMap() {
      Map<DataBlockId, Object> fieldFunctions = m_dataBlockDescriptions.values().stream()
         .filter(desc -> desc.getPhysicalType() == PhysicalDataBlockType.FIELD)
         .collect(Collectors.toMap(DataBlockDescription::getId, desc -> desc.getFieldProperties().getFieldFunctions()));
      return fieldFunctions;
   }

   private boolean hasFieldFunctionOfType(List<AbstractFieldFunction<?>> functions,
      Class<? extends AbstractFieldFunction<?>> type) {
      return functions.stream().anyMatch(function -> function.getClass().equals(type));
   }

   /**
    *
    */
   private void validateCharacterEncodingsAndByteOrders() {
      List<DataBlockDescription> allFields = m_dataBlockDescriptions.values().stream()
         .filter(desc -> desc.getPhysicalType() == PhysicalDataBlockType.FIELD).collect(Collectors.toList());

      for (DataBlockDescription fieldDesc : allFields) {
         Charset fixedCharacterEncoding = fieldDesc.getFieldProperties().getFixedCharacterEncoding();
         if (fixedCharacterEncoding != null && !getSupportedCharacterEncodings().contains(fixedCharacterEncoding)) {
            throw new InvalidSpecificationException(VLD_INVALID_CHARACTER_ENCODING, fieldDesc, fixedCharacterEncoding,
               getSupportedCharacterEncodings());
         }

         ByteOrder fixedByteOrder = fieldDesc.getFieldProperties().getFixedByteOrder();
         if (fixedByteOrder != null && !getSupportedByteOrders().contains(fixedByteOrder)) {
            throw new InvalidSpecificationException(VLD_INVALID_BYTE_ORDER, fieldDesc, fixedByteOrder,
               getSupportedByteOrders());
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
         for (int i = 1; i < matcher.groupCount() + 1; i++) {
            matchingStrings.add(matcher.group(i));
         }
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
         genericDescription.getMinimumByteLength(), genericDescription.getMaximumByteLength(), false,
         genericDescription.getIdField());
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

      if (!m_dataBlockDescriptions.containsKey(id)) {
         return getMatchingGenericId(id) != null;
      }

      return m_dataBlockDescriptions.containsKey(id);
   }

   @Override
   public DataBlockId getMatchingGenericId(DataBlockId id) {

      if (m_genericDataBlocks.containsKey(id)) {
         return id;
      }

      for (Iterator<DataBlockId> iterator = m_genericDataBlocks.keySet().iterator(); iterator.hasNext();) {
         DataBlockId nextId = iterator.next();
         String nextPattern = m_genericDataBlocks.get(nextId);

         if (id.getGlobalId().matches(nextPattern)) {
            return nextId;
         }
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

         if (!descs.isEmpty() && defaultNestedContainerId == null) {
            throw new InvalidSpecificationException(VLD_DEFAULT_NESTED_CONTAINER_MISSING, topLevelDesc);
         }
      }
   }

   private void initGenericIdPatterns(Set<DataBlockId> genericDataBlocks) {

      for (Iterator<DataBlockId> iterator = genericDataBlocks.iterator(); iterator.hasNext();) {
         DataBlockId genericBlockId = iterator.next();

         final String idString = genericBlockId.getGlobalId();
         Matcher matcher = GENERIC_PLACEHOLDER_PATTERN.matcher(idString);

         StringBuilder idPattern = new StringBuilder();

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
}
