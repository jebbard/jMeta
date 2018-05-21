/**
 * {@link FieldProperties}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * {@link FieldProperties}
 *
 * @param <T>
 */
public class FieldProperties<T> {

   /**
    * Creates a new {@link FieldProperties}.
    * 
    * @param fieldType
    * @param defaultValue
    * @param enumeratedValues
    * @param terminationCharacter
    * @param flagSpecification
    * @param fixedCharset
    * @param fixedByteOrder
    * @param functions
    * @param isMagicKey
    *           TODO
    */
   public FieldProperties(FieldType<?> fieldType, T defaultValue, Map<T, byte[]> enumeratedValues,
      Character terminationCharacter, FlagSpecification flagSpecification, Charset fixedCharset,
      ByteOrder fixedByteOrder, List<FieldFunction> functions, boolean isMagicKey) {
      // TODO writeConcept001: Is default value a mandatory property for a FieldProperties<> instance?
      // Reject.ifNull(defaultValue, "defaultValue");

      m_fieldType = fieldType;
      m_defaultValue = defaultValue;
      m_terminationCharacter = terminationCharacter;
      m_flagSpecification = flagSpecification;
      m_fixedCharset = fixedCharset;
      m_fixedByteOrder = fixedByteOrder;
      this.isMagicKey = isMagicKey;

      if (functions != null)
         m_functions.addAll(functions);

      if (enumeratedValues != null)
         m_enumeratedValues.putAll(enumeratedValues);
   }

   /**
    * Returns terminationCharacter
    *
    * @return terminationCharacter
    */
   public Character getTerminationCharacter() {

      return m_terminationCharacter;
   }

   /**
    * Returns fieldType
    *
    * @return fieldType
    */
   public FieldType<?> getFieldType() {

      return m_fieldType;
   }

   /**
    * @return the default value
    */
   public T getDefaultValue() {

      return m_defaultValue;
   }

   /**
    * @return the enumerated values
    */
   public Map<T, byte[]> getEnumeratedValues() {

      return Collections.unmodifiableMap(m_enumeratedValues);
   }

   /**
    * @return the {@link FlagSpecification}
    */
   public FlagSpecification getFlagSpecification() {

      return m_flagSpecification;
   }

   /**
    * Returns fixedCharset
    *
    * @return fixedCharset
    */
   public Charset getFixedCharacterEncoding() {

      return m_fixedCharset;
   }

   /**
    * Returns fixedByteOrder
    *
    * @return fixedByteOrder
    */
   public ByteOrder getFixedByteOrder() {

      return m_fixedByteOrder;
   }

   /**
    * @return the {@link FieldFunction}
    */
   public List<FieldFunction> getFieldFunctions() {

      return Collections.unmodifiableList(m_functions);
   }

   /**
    * Returns the attribute {@link #isMagicKey}.
    * 
    * @return the attribute {@link #isMagicKey}
    */
   public boolean isMagicKey() {
      return isMagicKey;
   }
   //
   // /**
   // * @see java.lang.Object#toString()
   // */
   // @Override
   // public String toString() {
   //
   // return getClass().getName() + "[" + ", termination=" + m_terminationCharacter + ", defaultValue="
   // + (m_defaultValue instanceof byte[] ? Arrays.toString((byte[]) m_defaultValue) : m_defaultValue)
   // + ", enumeratedValues=" + enumValuesToString() + ", blockFunction=" + m_functions + "]";
   // }

   @Override
   public String toString() {
      return "FieldProperties [m_terminationCharacter=" + m_terminationCharacter + ", m_defaultValue="
         + (m_defaultValue instanceof byte[] ? Arrays.toString((byte[]) m_defaultValue) : m_defaultValue)
         + ", m_enumeratedValues=" + enumValuesToString() + ", m_flagSpecification=" + m_flagSpecification
         + ", m_fieldType=" + m_fieldType + ", m_fixedCharset=" + m_fixedCharset + ", m_fixedByteOrder="
         + m_fixedByteOrder + ", m_functions=" + m_functions + ", isMagicKey=" + isMagicKey + "]";
   }

   private String enumValuesToString() {
      String toString = "{";

      for (Iterator<T> iterator = m_enumeratedValues.keySet().iterator(); iterator.hasNext();) {
         T nextKey = iterator.next();
         byte[] nextValue = m_enumeratedValues.get(nextKey);
         toString += nextKey + "=" + Arrays.toString(nextValue);
      }

      return toString + "}";
   }

   private final Character m_terminationCharacter;

   private final T m_defaultValue;

   private final Map<T, byte[]> m_enumeratedValues = new HashMap<>();

   private final FlagSpecification m_flagSpecification;

   private final FieldType<?> m_fieldType;

   private final Charset m_fixedCharset;

   private final ByteOrder m_fixedByteOrder;

   private final List<FieldFunction> m_functions = new ArrayList<>();

   private final boolean isMagicKey;
}
