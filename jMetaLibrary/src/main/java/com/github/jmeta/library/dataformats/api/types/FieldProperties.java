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
   public FieldProperties(FieldType<T> fieldType, T defaultValue, Map<T, byte[]> enumeratedValues,
      Character terminationCharacter, FlagSpecification flagSpecification, Charset fixedCharset,
      ByteOrder fixedByteOrder, List<FieldFunction> functions, boolean isMagicKey) {
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
   public FieldType<T> getFieldType() {

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

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + (isMagicKey ? 1231 : 1237);
      result = prime * result + ((m_defaultValue == null) ? 0 : m_defaultValue.hashCode());
      result = prime * result + ((m_enumeratedValues == null) ? 0 : m_enumeratedValues.hashCode());
      result = prime * result + ((m_fieldType == null) ? 0 : m_fieldType.hashCode());
      result = prime * result + ((m_fixedByteOrder == null) ? 0 : m_fixedByteOrder.hashCode());
      result = prime * result + ((m_fixedCharset == null) ? 0 : m_fixedCharset.hashCode());
      result = prime * result + ((m_flagSpecification == null) ? 0 : m_flagSpecification.hashCode());
      result = prime * result + ((m_functions == null) ? 0 : m_functions.hashCode());
      result = prime * result + ((m_terminationCharacter == null) ? 0 : m_terminationCharacter.hashCode());
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
      FieldProperties<?> other = (FieldProperties<?>) obj;
      if (isMagicKey != other.isMagicKey)
         return false;
      if (m_defaultValue == null) {
         if (other.m_defaultValue != null)
            return false;
      } else if (!m_defaultValue.equals(other.m_defaultValue))
         return false;
      if (m_enumeratedValues == null) {
         if (other.m_enumeratedValues != null)
            return false;
      } else if (!m_enumeratedValues.equals(other.m_enumeratedValues))
         return false;
      if (m_fieldType == null) {
         if (other.m_fieldType != null)
            return false;
      } else if (!m_fieldType.equals(other.m_fieldType))
         return false;
      if (m_fixedByteOrder == null) {
         if (other.m_fixedByteOrder != null)
            return false;
      } else if (!m_fixedByteOrder.equals(other.m_fixedByteOrder))
         return false;
      if (m_fixedCharset == null) {
         if (other.m_fixedCharset != null)
            return false;
      } else if (!m_fixedCharset.equals(other.m_fixedCharset))
         return false;
      if (m_flagSpecification == null) {
         if (other.m_flagSpecification != null)
            return false;
      } else if (!m_flagSpecification.equals(other.m_flagSpecification))
         return false;
      if (m_functions == null) {
         if (other.m_functions != null)
            return false;
      } else if (!m_functions.equals(other.m_functions))
         return false;
      if (m_terminationCharacter == null) {
         if (other.m_terminationCharacter != null)
            return false;
      } else if (!m_terminationCharacter.equals(other.m_terminationCharacter))
         return false;
      return true;
   }

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

   private final FieldType<T> m_fieldType;

   private final Charset m_fixedCharset;

   private final ByteOrder m_fixedByteOrder;

   private final List<FieldFunction> m_functions = new ArrayList<>();

   private final boolean isMagicKey;
}
