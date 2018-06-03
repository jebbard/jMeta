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

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FieldProperties}
 *
 * @param <T>
 */
public class FieldProperties<T> {

   private final FieldType<T> fieldType;

   private final T defaultValue;

   private final Map<T, byte[]> enumeratedValues = new HashMap<>();

   private final List<FieldFunction> functions = new ArrayList<>();

   private final boolean isMagicKey;

   private final Integer magicKeyBitLength;

   private final Character terminationCharacter;

   private final Charset fixedCharset;

   private final FlagSpecification flagSpecification;

   private final ByteOrder fixedByteOrder;

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
    * @param magicKeyBitLength
    *           TODO
    */
   public FieldProperties(FieldType<T> fieldType, T defaultValue, Map<T, byte[]> enumeratedValues,
      Character terminationCharacter, FlagSpecification flagSpecification, Charset fixedCharset,
      ByteOrder fixedByteOrder, List<FieldFunction> functions, boolean isMagicKey, Integer magicKeyBitLength) {
      Reject.ifNull(fieldType, "fieldType");
      Reject.ifNull(enumeratedValues, "enumeratedValues");
      Reject.ifNull(functions, "functions");

      this.fieldType = fieldType;
      this.defaultValue = defaultValue;
      this.terminationCharacter = terminationCharacter;
      this.flagSpecification = flagSpecification;
      this.fixedCharset = fixedCharset;
      this.fixedByteOrder = fixedByteOrder;
      this.isMagicKey = isMagicKey;
      this.magicKeyBitLength = magicKeyBitLength;
      this.functions.addAll(functions);
      this.enumeratedValues.putAll(enumeratedValues);
   }

   /**
    * Returns fieldType
    *
    * @return fieldType
    */
   public FieldType<T> getFieldType() {

      return fieldType;
   }

   /**
    * @return the default value
    */
   public T getDefaultValue() {

      return defaultValue;
   }

   /**
    * @return the enumerated values
    */
   public Map<T, byte[]> getEnumeratedValues() {

      return Collections.unmodifiableMap(enumeratedValues);
   }

   /**
    * @return the {@link FieldFunction}
    */
   public List<FieldFunction> getFieldFunctions() {

      return Collections.unmodifiableList(functions);
   }

   /**
    * Returns the attribute {@link #isMagicKey}.
    * 
    * @return the attribute {@link #isMagicKey}
    */
   public boolean isMagicKey() {
      return isMagicKey;
   }

   public Integer getMagicKeyBitLength() {
      return magicKeyBitLength;
   }

   /**
    * Returns terminationCharacter
    *
    * @return terminationCharacter
    */
   public Character getTerminationCharacter() {

      return terminationCharacter;
   }

   /**
    * Returns fixedCharset
    *
    * @return fixedCharset
    */
   public Charset getFixedCharacterEncoding() {

      return fixedCharset;
   }

   /**
    * @return the {@link FlagSpecification}
    */
   public FlagSpecification getFlagSpecification() {

      return flagSpecification;
   }

   /**
    * Returns fixedByteOrder
    *
    * @return fixedByteOrder
    */
   public ByteOrder getFixedByteOrder() {

      return fixedByteOrder;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
      result = prime * result + ((enumeratedValues == null) ? 0 : enumeratedValues.hashCode());
      result = prime * result + ((fieldType == null) ? 0 : fieldType.hashCode());
      result = prime * result + ((fixedByteOrder == null) ? 0 : fixedByteOrder.hashCode());
      result = prime * result + ((fixedCharset == null) ? 0 : fixedCharset.hashCode());
      result = prime * result + ((flagSpecification == null) ? 0 : flagSpecification.hashCode());
      result = prime * result + ((functions == null) ? 0 : functions.hashCode());
      result = prime * result + (isMagicKey ? 1231 : 1237);
      result = prime * result + ((magicKeyBitLength == null) ? 0 : magicKeyBitLength.hashCode());
      result = prime * result + ((terminationCharacter == null) ? 0 : terminationCharacter.hashCode());
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
      if (defaultValue == null) {
         if (other.defaultValue != null)
            return false;
      } else if (!defaultValue.equals(other.defaultValue))
         return false;
      if (enumeratedValues == null) {
         if (other.enumeratedValues != null)
            return false;
      } else if (!enumeratedValues.equals(other.enumeratedValues))
         return false;
      if (fieldType == null) {
         if (other.fieldType != null)
            return false;
      } else if (!fieldType.equals(other.fieldType))
         return false;
      if (fixedByteOrder == null) {
         if (other.fixedByteOrder != null)
            return false;
      } else if (!fixedByteOrder.equals(other.fixedByteOrder))
         return false;
      if (fixedCharset == null) {
         if (other.fixedCharset != null)
            return false;
      } else if (!fixedCharset.equals(other.fixedCharset))
         return false;
      if (flagSpecification == null) {
         if (other.flagSpecification != null)
            return false;
      } else if (!flagSpecification.equals(other.flagSpecification))
         return false;
      if (functions == null) {
         if (other.functions != null)
            return false;
      } else if (!functions.equals(other.functions))
         return false;
      if (isMagicKey != other.isMagicKey)
         return false;
      if (magicKeyBitLength == null) {
         if (other.magicKeyBitLength != null)
            return false;
      } else if (!magicKeyBitLength.equals(other.magicKeyBitLength))
         return false;
      if (terminationCharacter == null) {
         if (other.terminationCharacter != null)
            return false;
      } else if (!terminationCharacter.equals(other.terminationCharacter))
         return false;
      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "FieldProperties [fieldType=" + fieldType + ", defaultValue=" + interpretedValueToString(defaultValue)
         + ", enumeratedValues=" + enumValuesToString() + ", functions=" + functions + ", isMagicKey=" + isMagicKey
         + ", magicKeyBitLength=" + magicKeyBitLength + ", terminationCharacter=" + terminationCharacter
         + ", fixedCharset=" + fixedCharset + ", flagSpecification=" + flagSpecification + ", fixedByteOrder="
         + fixedByteOrder + "]";
   }

   /**
    * @param messagePrefix
    */
   void validateFieldProperties(String messagePrefix, long minimumByteLength, long maximumByteLength) {
      boolean hasFixedSize = minimumByteLength == maximumByteLength;

      // Validate magic key
      if (isMagicKey()) {
         if (!hasFixedSize || maximumByteLength == DataBlockDescription.UNKNOWN_SIZE) {
            throw new IllegalArgumentException(
               messagePrefix + "Field is tagged as magic key, but it has a variable size: min length = <"
                  + minimumByteLength + ">, max length = <" + maximumByteLength + ">");
         }

         if (getEnumeratedValues().isEmpty() && getDefaultValue() == null) {
            throw new IllegalArgumentException(messagePrefix
               + "Data block is tagged as magic key, but it has neither enumerated values nor a default value set");
         }

         if (getFieldType() == FieldType.UNSIGNED_WHOLE_NUMBER) {
            throw new IllegalArgumentException(messagePrefix
               + "Data block is tagged as magic key, but it has type NUMERIC. Magic key fields must have one of the types STRING, BINARY or FLAGS");
         }

         if (getMagicKeyBitLength() != null && getMagicKeyBitLength() <= 0) {
            throw new IllegalArgumentException(
               messagePrefix + "Data block is tagged as magic key but its bit length set is zero or negative: "
                  + getMagicKeyBitLength());
         }

         // TODO fixed length == key length
      }

      // Validate enumerated fields
      Map<byte[], Object> binaryValues = new HashMap<>();

      for (Iterator<?> enumValueIterator = getEnumeratedValues().keySet().iterator(); enumValueIterator.hasNext();) {
         Object nextKey = enumValueIterator.next();
         byte[] nextValue = getEnumeratedValues().get(nextKey);

         if (binaryValues.containsKey(nextValue)) {
            throw new IllegalArgumentException(messagePrefix + "Binary representation <" + Arrays.toString(nextValue)
               + "> of enumerated interpreted value <" + nextKey
               + "> is not unique, it is already used for interpreted value <" + binaryValues.get(nextValue) + ">.");
         }

         if (hasFixedSize && nextValue.length > minimumByteLength) {
            throw new IllegalArgumentException(
               messagePrefix + "Binary representation of enmuerated value <" + nextKey + "> with length <"
                  + nextValue.length + "> is longer than the field's fixed size which is <" + minimumByteLength + ">");
         }
      }

      if (!getEnumeratedValues().isEmpty() && getDefaultValue() != null) {
         if (!getEnumeratedValues().containsKey(getDefaultValue())) {
            throw new IllegalArgumentException(messagePrefix + "Default field value <" + getDefaultValue()
               + "> must be contained in list of enumerated values, but it is not");
         }
      }

      // Validate numeric fields
      if (getFieldType() == FieldType.UNSIGNED_WHOLE_NUMBER) {
         if (minimumByteLength > Long.BYTES || maximumByteLength > Long.BYTES) {
            throw new IllegalArgumentException(
               messagePrefix + "Field has Numeric type, but its minimum or maximum length is bigger than " + Long.BYTES
                  + ": min length = <" + minimumByteLength + ">, max length = <" + maximumByteLength + ">");
         }
      } else {
         if (getFixedByteOrder() != null) {
            throw new IllegalArgumentException(
               messagePrefix + "Field has not Numeric type, but a fixed byte order is defined for it");
         }
      }

      // Validate string fields
      if (getFieldType() != FieldType.STRING) {
         if (getFixedCharacterEncoding() != null) {
            throw new IllegalArgumentException(
               messagePrefix + "Field has not String type, but a fixed character encoding is defined for it");
         }

         if (getTerminationCharacter() != null) {
            throw new IllegalArgumentException(
               messagePrefix + "Field has not String type, but a termincation character is defined for it");
         }
      }

      // Validate field functions standalone
      List<FieldFunction> fieldFunctions = getFieldFunctions();

      for (FieldFunction fieldFunction : fieldFunctions) {
         FieldFunctionType<?> ffType = fieldFunction.getFieldFunctionType();
         if (ffType == FieldFunctionType.ID_OF || ffType == FieldFunctionType.CHARACTER_ENCODING_OF
            || ffType == FieldFunctionType.BYTE_ORDER_OF) {
            if (getFieldType() != FieldType.STRING) {
               throw new IllegalArgumentException(messagePrefix
                  + "Field is the id of, character encoding of or byte order of another field, but it is not of type String");
            }
         } else if (ffType == FieldFunctionType.SIZE_OF || ffType == FieldFunctionType.COUNT_OF) {
            if (getFieldType() != FieldType.UNSIGNED_WHOLE_NUMBER) {
               throw new IllegalArgumentException(messagePrefix
                  + "Field is the id of, character encoding of or byte order of another field, but it is not of type Numeric");
            }
         } else if (ffType == FieldFunctionType.PRESENCE_OF) {
            if (getFieldType() != FieldType.FLAGS) {
               throw new IllegalArgumentException(messagePrefix
                  + "Field is the id of, character encoding of or byte order of another field, but it is not of type Flags");
            }
         }

      }

      // Validate default value
      // TODO default value length == fixed length
   }

   private String enumValuesToString() {
      String toString = "{";

      for (Iterator<T> iterator = enumeratedValues.keySet().iterator(); iterator.hasNext();) {
         T nextKey = iterator.next();
         byte[] nextValue = enumeratedValues.get(nextKey);
         toString += interpretedValueToString(nextKey) + "=" + Arrays.toString(nextValue);
      }

      return toString + "}";
   }

   private String interpretedValueToString(T interpretedValue) {
      return (interpretedValue instanceof byte[] ? Arrays.toString((byte[]) interpretedValue)
         : interpretedValue.toString());
   }
}
