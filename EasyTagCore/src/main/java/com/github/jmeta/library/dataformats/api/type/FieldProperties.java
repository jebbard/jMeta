/**
 * {@link FieldProperties}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.type;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.common.flags.FlagSpecification;

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
    * @param terminationBytes
    * @param minimumCharacterLength
    * @param maximumCharacterLength
    * @param terminationCharacter
    * @param patterns
    * @param minimumValue
    * @param maximumValue
    * @param flagSpecification
    * @param fixedCharset
    * @param fixedByteOrder
    * @param functions
    */
   public FieldProperties(FieldType<?> fieldType, T defaultValue,
      Map<T, byte[]> enumeratedValues, byte[] terminationBytes,
      long minimumCharacterLength, long maximumCharacterLength,
      Character terminationCharacter, List<String> patterns, T minimumValue,
      T maximumValue, FlagSpecification flagSpecification, Charset fixedCharset,
      ByteOrder fixedByteOrder, List<FieldFunction> functions) {
      // TODO writeConcept001: Is default value a mandatory property for a FieldProperties<> instance?
      // Reject.ifNull(defaultValue, "defaultValue");

	  Reject.ifFalse(
         minimumCharacterLength <= maximumCharacterLength,
         "minimumCharacterLength <= maximumCharacterLength");

      m_fieldType = fieldType;
      m_defaultValue = defaultValue;
      m_terminationBytes = terminationBytes;
      m_minimumCharacterLength = minimumCharacterLength;
      m_maximumCharacterLength = maximumCharacterLength;
      m_terminationCharacter = terminationCharacter;
      m_patterns = patterns;
      m_minimumValue = minimumValue;
      m_maximumValue = maximumValue;
      m_flagSpecification = flagSpecification;
      m_fixedCharset = fixedCharset;
      m_fixedByteOrder = fixedByteOrder;

      if (functions != null)
         m_functions.addAll(functions);

      if (enumeratedValues != null)
         m_enumeratedValues.putAll(enumeratedValues);
   }

   /**
    * Returns terminationBytes
    *
    * @return terminationBytes
    */
   public byte[] getTerminationBytes() {

      return m_terminationBytes;
   }

   /**
    * Returns maximumCharacterLength
    *
    * @return maximumCharacterLength
    */
   public long getMaximumCharacterLength() {

      return m_maximumCharacterLength;
   }

   /**
    * Returns minimumCharacterLength
    *
    * @return minimumCharacterLength
    */
   public long getMinimumCharacterLength() {

      return m_minimumCharacterLength;
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
    * @return the patterns
    */
   public List<String> getPatterns() {

      if (m_patterns == null)
         return null;

      return Collections.unmodifiableList(m_patterns);
   }

   /**
    * @return the minimum value
    */
   public T getMinimumValue() {

      return m_minimumValue;
   }

   /**
    * @return the maximum value
    */
   public T getMaximumValue() {

      return m_maximumValue;
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
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[" + ", termination=" + m_terminationBytes
         + ", defaultValue=" + m_defaultValue + ", enumeratedValues="
         + m_enumeratedValues + ", blockFunction=" + m_functions + "]";
   }

   private final byte[] m_terminationBytes;

   private final long m_maximumCharacterLength;

   private final long m_minimumCharacterLength;

   private final Character m_terminationCharacter;

   private final T m_defaultValue;

   private final Map<T, byte[]> m_enumeratedValues = new HashMap<>();

   private final List<String> m_patterns;

   private final T m_minimumValue;

   private final T m_maximumValue;

   private final FlagSpecification m_flagSpecification;

   private final FieldType<?> m_fieldType;

   private final Charset m_fixedCharset;

   private final ByteOrder m_fixedByteOrder;

   private final List<FieldFunction> m_functions = new ArrayList<>();
}
