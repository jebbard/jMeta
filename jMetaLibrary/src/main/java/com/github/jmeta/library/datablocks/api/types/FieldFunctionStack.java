/**
 *
 * {@link AbstractFieldFunctionStack}.java
 *
 * @author Jens Ebert
 *
 * @date 17.01.2011
 */

package com.github.jmeta.library.datablocks.api.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction;
import com.github.jmeta.library.dataformats.api.types.CountOf;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.dataformats.api.types.PresenceOf;
import com.github.jmeta.library.dataformats.api.types.SizeOf;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractFieldFunctionStack}
 *
 */
// TODO doItFirst001: consider using another data structure instead of list
public class FieldFunctionStack {

   private static final Logger LOGGER = LoggerFactory.getLogger(FieldFunctionStack.class);

   /**
    * @param desc
    * @param field
    */
   public void pushFieldFunctions(DataBlockDescription desc, Field<?> field) {

      Reject.ifNull(field, "field");
      Reject.ifNull(desc, "desc");
      Reject.ifFalse(desc.getId().equals(field.getId()), "desc.getId().equals(field.getId())");

      // CONFIG_CHECK felder müssen immer typ feld haben
      if (!desc.getPhysicalType().equals(PhysicalDataBlockType.FIELD)) {
         throw new IllegalStateException("Field is not of type field!");
      }

      final FieldProperties<?> fieldProperties = desc.getFieldProperties();

      List<AbstractFieldFunction<?>> fieldFunctions = fieldProperties.getFieldFunctions();

      for (int i = 0; i < fieldFunctions.size(); ++i) {
         AbstractFieldFunction<?> function = fieldFunctions.get(i);

         DataBlockId id = function.getReferencedBlock().getReferencedId();

         Class<? extends AbstractFieldFunction<?>> type = (Class<? extends AbstractFieldFunction<?>>) function
            .getClass();

         updateCollections(id, type);

         try {
            // Special treatment for flags field functions
            if (type == PresenceOf.class) {
               Flags flags = (Flags) field.getInterpretedValue();

               addValue(id, type,
                  flags.getFlagIntegerValue(((PresenceOf) function).getFlagName()) == ((PresenceOf) function)
                     .getFlagValue());
            } else {
               addValue(id, type, field.getInterpretedValue());
            }
         } catch (BinaryValueConversionException e) {
            // Silently ignore: The local id remains by its previous value
            LOGGER.warn(LOGGING_BINARY_TO_INTERPRETED_FAILED, type, field.getId());
            LOGGER.error("pushAbstractFieldFunctions", e);
         }
      }
   }

   /**
    * @param affectedBlockId
    * @param type
    * @param value
    */
   public <T> void pushFieldFunction(DataBlockId affectedBlockId, Class<? extends AbstractFieldFunction<?>> type,
      T value) {

      Reject.ifNull(value, "field");
      Reject.ifNull(type, "type");
      Reject.ifNull(affectedBlockId, "affectedBlockId");

      updateCollections(affectedBlockId, type);

      addValue(affectedBlockId, type, value);
   }

   private void addValue(DataBlockId affectedBlockId, Class<? extends AbstractFieldFunction<?>> type, Object value) {

      // DO NOT add count or size of zero to stack, as they would cause exceptions when parsing
      if (type == CountOf.class || type == SizeOf.class) {
         if ((Long) value == 0) {
            return;
         }
      }

      m_fieldFunctionStack.get(affectedBlockId).get(type).add(value);
   }

   /**
    * @param id
    * @param functionType
    * @return true if it has the field function, false otherwise
    */
   public boolean hasFieldFunction(DataBlockId id, Class<? extends AbstractFieldFunction<?>> type) {

      Reject.ifNull(id, "id");
      Reject.ifNull(type, "type");

      return m_fieldFunctionStack.containsKey(id) && m_fieldFunctionStack.get(id).containsKey(type);
   }

   /**
    * @param id
    * @param functionType
    * @return the field function
    */
   public <T> T popFieldFunction(DataBlockId id, Class<? extends AbstractFieldFunction<?>> functionType) {

      Reject.ifNull(id, "id");
      Reject.ifNull(functionType, "functionType");
      Reject.ifFalse(hasFieldFunction(id, functionType), "hasAbstractFieldFunction(id, functionType)");

      // Might throw ClassCastException!!
      List<Object> fieldValues = m_fieldFunctionStack.get(id).get(functionType);

      @SuppressWarnings("unchecked")
      T returnedValue = (T) fieldValues.remove(0);

      // pop from stack
      if (fieldValues.isEmpty()) {
         m_fieldFunctionStack.get(id).remove(functionType);

         if (m_fieldFunctionStack.get(id).isEmpty()) {
            m_fieldFunctionStack.remove(id);
         }
      }

      return returnedValue;
   }

   @Override
   public String toString() {

      StringBuffer stringRepresentation = new StringBuffer();

      for (Iterator<DataBlockId> iterator = m_fieldFunctionStack.keySet().iterator(); iterator.hasNext();) {
         DataBlockId affectedBlockId = iterator.next();
         Map<Class<? extends AbstractFieldFunction<?>>, List<Object>> fieldFunctionValues = m_fieldFunctionStack
            .get(affectedBlockId);

         stringRepresentation.append(affectedBlockId.getGlobalId());
         stringRepresentation.append("\n");

         for (Iterator<Class<? extends AbstractFieldFunction<?>>> fieldFuncIter = fieldFunctionValues.keySet()
            .iterator(); fieldFuncIter.hasNext();) {
            Class<? extends AbstractFieldFunction<?>> fieldFuncType = fieldFuncIter.next();
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

   private void updateCollections(DataBlockId id, final Class<? extends AbstractFieldFunction<?>> type) {

      if (!m_fieldFunctionStack.containsKey(id)) {
         m_fieldFunctionStack.put(id, new LinkedHashMap<Class<? extends AbstractFieldFunction<?>>, List<Object>>());
      }

      Map<Class<? extends AbstractFieldFunction<?>>, List<Object>> functionTypeMap = m_fieldFunctionStack.get(id);

      if (functionTypeMap.get(type) == null) {
         functionTypeMap.put(type, new ArrayList<>());
      }
   }

   private static final String LOGGING_BINARY_TO_INTERPRETED_FAILED = "Could not add field function of type <%1$s> for field id <%2$s> to field function stack because the conversion from binary to interpreted value failed. Exception see below.";

   private final Map<DataBlockId, Map<Class<? extends AbstractFieldFunction<?>>, List<Object>>> m_fieldFunctionStack = new HashMap<>();
}
