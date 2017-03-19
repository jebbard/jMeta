/**
 *
 * {@link FieldFunctionStack}.java
 *
 * @author Jens Ebert
 *
 * @date 17.01.2011
 */

package de.je.jmeta.datablocks.export;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.je.jmeta.datablocks.BinaryValueConversionException;
import de.je.jmeta.datablocks.IField;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.jmeta.dataformats.DataBlockId;
import de.je.jmeta.dataformats.FieldFunction;
import de.je.jmeta.dataformats.FieldFunctionType;
import de.je.jmeta.dataformats.FieldProperties;
import de.je.jmeta.dataformats.PhysicalDataBlockType;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.common.flags.Flags;

/**
 * {@link FieldFunctionStack}
 *
 */
// TODO doItFirst001: consider using another data structure instead of list
public class FieldFunctionStack {

   private static final Logger LOGGER = LoggerFactory.getLogger(FieldFunctionStack.class);

   /**
    * @param desc
    * @param field
    */
   public void pushFieldFunctions(DataBlockDescription desc, IField<?> field) {

      Reject.ifNull(field, "field");
      Reject.ifNull(desc, "desc");
      Contract.checkPrecondition(desc.getId().equals(field.getId()),
         "The given DataBlockDescription must refer to the given field");

      // CONFIG_CHECK felder müssen immer typ feld haben
      if (!desc.getPhysicalType().equals(PhysicalDataBlockType.FIELD))
         throw new IllegalStateException("Field is not of type field!");

      final FieldProperties<?> fieldProperties = desc.getFieldProperties();

      List<FieldFunction> fieldFunctions = fieldProperties.getFieldFunctions();

      for (int i = 0; i < fieldFunctions.size(); ++i) {
         FieldFunction function = fieldFunctions.get(i);

         for (Iterator<DataBlockId> blockIterator = function.getAffectedBlockIds().iterator(); blockIterator
            .hasNext();) {
            DataBlockId id = blockIterator.next();

            final FieldFunctionType<?> type = function.getFieldFunctionType();

            updateCollections(id, type);

            try {
               // Special treatment for flags field functions
               if (function.getFlagName() != null) {
                  Flags flags = (Flags) field.getInterpretedValue();

                  addValue(id, type, flags.getFlagIntegerValue(function.getFlagName()) == function.getFlagValue());
               }

               else
                  addValue(id, type, field.getInterpretedValue());
            } catch (BinaryValueConversionException e) {
               // Silently ignore: The local id remains by its previous value
               LOGGER.warn(LOGGING_BINARY_TO_INTERPRETED_FAILED, type, field.getId());
               LOGGER.error("pushFieldFunctions", e);
            }
         }
      }
   }

   /**
    * @param affectedBlockId
    * @param type
    * @param value
    */
   public <T> void pushFieldFunction(DataBlockId affectedBlockId, FieldFunctionType<T> type, T value) {

      Reject.ifNull(value, "field");
      Reject.ifNull(type, "type");
      Reject.ifNull(affectedBlockId, "affectedBlockId");

      updateCollections(affectedBlockId, type);

      addValue(affectedBlockId, type, value);
   }

   private void addValue(DataBlockId affectedBlockId, FieldFunctionType<?> type, Object value) {

      // DO NOT add count or size of zero to stack, as they would cause exceptions when parsing
      if (type == FieldFunctionType.COUNT_OF || type == FieldFunctionType.SIZE_OF) {
         if (((Long) value) == 0)
            return;
      }

      m_fieldFunctionStack.get(affectedBlockId).get(type).add(value);
   }

   /**
    * @param id
    * @param functionType
    * @return true if it has the field function, false otherwise
    */
   public boolean hasFieldFunction(DataBlockId id, FieldFunctionType<?> functionType) {

      Reject.ifNull(id, "id");
      Reject.ifNull(functionType, "functionType");

      return m_fieldFunctionStack.containsKey(id) && m_fieldFunctionStack.get(id).containsKey(functionType);
   }

   /**
    * @param id
    * @param functionType
    * @return the field function
    */
   public <T> T popFieldFunction(DataBlockId id, FieldFunctionType<T> functionType) {

      Reject.ifNull(id, "id");
      Reject.ifNull(functionType, "functionType");
      Contract.checkPrecondition(hasFieldFunction(id, functionType), "Stack must contain field function");

      // Might throw ClassCastException!!
      List<Object> fieldValues = m_fieldFunctionStack.get(id).get(functionType);

      @SuppressWarnings("unchecked")
      T returnedValue = (T) fieldValues.remove(0);

      // pop from stack
      if (fieldValues.isEmpty()) {
         m_fieldFunctionStack.get(id).remove(functionType);

         if (m_fieldFunctionStack.get(id).isEmpty())
            m_fieldFunctionStack.remove(id);
      }

      return returnedValue;
   }

   @Override
   public String toString() {

      StringBuffer stringRepresentation = new StringBuffer();

      for (Iterator<DataBlockId> iterator = m_fieldFunctionStack.keySet().iterator(); iterator.hasNext();) {
         DataBlockId affectedBlockId = iterator.next();
         Map<FieldFunctionType<?>, List<Object>> fieldFunctionValues = m_fieldFunctionStack.get(affectedBlockId);

         stringRepresentation.append(affectedBlockId.getGlobalId());
         stringRepresentation.append("\n");

         for (Iterator<FieldFunctionType<?>> fieldFuncIter = fieldFunctionValues.keySet().iterator(); fieldFuncIter
            .hasNext();) {
            FieldFunctionType<?> fieldFuncType = fieldFuncIter.next();
            List<Object> valueList = fieldFunctionValues.get(fieldFuncType);

            stringRepresentation.append("\t→");
            stringRepresentation.append(fieldFuncType);
            stringRepresentation.append("\n");

            for (int i = 0; i < valueList.size(); ++i) {
               Object value = valueList.get(i);
               stringRepresentation.append("\t\t→");
               stringRepresentation.append(i);
               stringRepresentation.append(": ");
               stringRepresentation.append(value);
               stringRepresentation.append("\n");
            }
         }
      }

      return stringRepresentation.toString();
   }

   private void updateCollections(DataBlockId id, final FieldFunctionType<?> type) {

      if (!m_fieldFunctionStack.containsKey(id)) {
         m_fieldFunctionStack.put(id, new LinkedHashMap<FieldFunctionType<?>, List<Object>>());
      }

      Map<FieldFunctionType<?>, List<Object>> functionTypeMap = m_fieldFunctionStack.get(id);

      if (functionTypeMap.get(type) == null)
         functionTypeMap.put(type, new ArrayList<>());
   }

   private static final String LOGGING_BINARY_TO_INTERPRETED_FAILED = "Could not add field function of type <%1$s> for field id <%2$s> to field function stack because the conversion from binary to interpreted value failed. Exception see below.";

   private final Map<DataBlockId, Map<FieldFunctionType<?>, List<Object>>> m_fieldFunctionStack = new HashMap<>();
}
