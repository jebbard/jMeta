/**
 * {@link FieldProperties}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_BINARY_ENUMERATED_VALUE_NOT_UNIQUE;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_BINARY_ENUMERATED_VALUE_TOO_LONG;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_DEFAULT_VALUE_CONVERSION_FAILED;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_DEFAULT_VALUE_EXCEEDS_LENGTH;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_DEFAULT_VALUE_NOT_ENUMERATED;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_FLAG_PROPERTIES_UNNECESSARY;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_NON_FLAGS;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_NON_NUMERIC;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_NON_STRING;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_PRESENCE_OF_MISSING_FIELDS;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIELD_FUNC_PRESENCE_OF_UNSPECIFIED_FLAG_NAME;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIXED_BYTE_ORDER_NON_NUMERIC;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_FIXED_CHARSET_NON_STRING;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_MAGIC_KEY_BIT_LENGTH_BIGGER_THAN_FIXED_SIZE;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_MAGIC_KEY_BIT_LENGTH_TOO_BIG;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_MAGIC_KEY_BIT_LENGTH_TOO_SMALL;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_MAGIC_KEY_INVALID_FIELD_LENGTH;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_MAGIC_KEY_INVALID_FIELD_TYPE;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_MAGIC_KEY_INVALID_FIELD_VALUE;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_NUMERIC_FIELD_TOO_LONG;
import static com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException.VLD_TERMINATION_CHAR_NON_STRING;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.dataformats.api.exceptions.InvalidSpecificationException;
import com.github.jmeta.library.dataformats.api.types.converter.BinaryFieldConverter;
import com.github.jmeta.library.dataformats.api.types.converter.FieldConverter;
import com.github.jmeta.library.dataformats.api.types.converter.FlagsFieldConverter;
import com.github.jmeta.library.dataformats.api.types.converter.StringFieldConverter;
import com.github.jmeta.library.dataformats.api.types.converter.UnsignedNumericFieldConverter;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link FieldProperties}
 *
 * @param <T>
 */
public class FieldProperties<T> {

   private final FieldType<T> fieldType;

   private final T defaultValue;

   private final FieldConverter<T> converter;

   private final Map<T, byte[]> enumeratedValues = new HashMap<>();

   private final List<FieldFunction> functions = new ArrayList<>();

   private final boolean isMagicKey;

   private final long magicKeyBitLength;

   private final Character terminationCharacter;

   private final Charset fixedCharacterEncoding;

   private final FlagSpecification flagSpecification;

   private final ByteOrder fixedByteOrder;

   private final static Map<FieldType<?>, FieldConverter<?>> FIELD_CONVERTERS = new HashMap<>();
   static {
      FIELD_CONVERTERS.put(FieldType.BINARY, new BinaryFieldConverter());
      FIELD_CONVERTERS.put(FieldType.FLAGS, new FlagsFieldConverter());
      FIELD_CONVERTERS.put(FieldType.UNSIGNED_WHOLE_NUMBER, new UnsignedNumericFieldConverter());
      FIELD_CONVERTERS.put(FieldType.STRING, new StringFieldConverter());
   }

   public FieldProperties(FieldType<T> fieldType, T defaultValue, Map<T, byte[]> enumeratedValues,
      Character terminationCharacter, FlagSpecification flagSpecification, Charset fixedCharset,
      ByteOrder fixedByteOrder, List<FieldFunction> functions, boolean isMagicKey, long magicKeyBitLength,
      FieldConverter<T> customConverter) {
      Reject.ifNull(fieldType, "fieldType");
      Reject.ifNull(enumeratedValues, "enumeratedValues");
      Reject.ifNull(functions, "functions");

      this.fieldType = fieldType;
      this.defaultValue = defaultValue;
      this.converter = customConverter == null ? getDefaultFieldConverter(fieldType) : customConverter;
      this.enumeratedValues.putAll(enumeratedValues);
      this.functions.addAll(functions);
      this.isMagicKey = isMagicKey;
      this.magicKeyBitLength = magicKeyBitLength;
      this.terminationCharacter = terminationCharacter;
      this.fixedCharacterEncoding = fixedCharset;
      this.flagSpecification = flagSpecification;
      this.fixedByteOrder = fixedByteOrder;
   }

   @SuppressWarnings("unchecked")
   private static <F> FieldConverter<F> getDefaultFieldConverter(FieldType<F> fieldType) {
      return (FieldConverter<F>) FIELD_CONVERTERS.get(fieldType);
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
    * Returns the attribute {@link #converter}.
    * 
    * @return the attribute {@link #converter}
    */
   public FieldConverter<T> getConverter() {
      return converter;
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

   public long getMagicKeyBitLength() {
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

      return fixedCharacterEncoding;
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
      result = prime * result + ((converter == null) ? 0 : converter.hashCode());
      result = prime * result + ((defaultValue == null) ? 0 : defaultValue.hashCode());
      result = prime * result + ((enumeratedValues == null) ? 0 : enumeratedValues.hashCode());
      result = prime * result + ((fieldType == null) ? 0 : fieldType.hashCode());
      result = prime * result + ((fixedByteOrder == null) ? 0 : fixedByteOrder.hashCode());
      result = prime * result + ((fixedCharacterEncoding == null) ? 0 : fixedCharacterEncoding.hashCode());
      result = prime * result + ((flagSpecification == null) ? 0 : flagSpecification.hashCode());
      result = prime * result + ((functions == null) ? 0 : functions.hashCode());
      result = prime * result + (isMagicKey ? 1231 : 1237);
      result = prime * result + (int) (magicKeyBitLength ^ (magicKeyBitLength >>> 32));
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
      if (converter == null) {
         if (other.converter != null)
            return false;
      } else if (!converter.equals(other.converter))
         return false;
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
      if (fixedCharacterEncoding == null) {
         if (other.fixedCharacterEncoding != null)
            return false;
      } else if (!fixedCharacterEncoding.equals(other.fixedCharacterEncoding))
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
      if (magicKeyBitLength != other.magicKeyBitLength)
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
         + ", converter=" + converter + ", enumeratedValues=" + enumValuesToString() + ", functions=" + functions
         + ", isMagicKey=" + isMagicKey + ", magicKeyBitLength=" + magicKeyBitLength + ", terminationCharacter="
         + terminationCharacter + ", fixedCharacterEncoding=" + fixedCharacterEncoding + ", flagSpecification="
         + flagSpecification + ", fixedByteOrder=" + fixedByteOrder + "]";
   }

   void validateFieldProperties(DataBlockDescription desc) {
      long minimumByteLength = desc.getMinimumByteLength();
      long maximumByteLength = desc.getMaximumByteLength();

      boolean hasFixedSize = minimumByteLength == maximumByteLength;

      String messagePrefix = "Error validating field properties for <" + desc.getId() + ">: ";

      // Validate magic key
      if (isMagicKey()) {
         if (!hasFixedSize || maximumByteLength == DataBlockDescription.UNDEFINED) {
            throw new InvalidSpecificationException(VLD_MAGIC_KEY_INVALID_FIELD_LENGTH, desc, minimumByteLength,
               maximumByteLength);
         }

         if (getEnumeratedValues().isEmpty() && getDefaultValue() == null) {
            throw new InvalidSpecificationException(VLD_MAGIC_KEY_INVALID_FIELD_VALUE, desc);
         }

         if (getFieldType() == FieldType.UNSIGNED_WHOLE_NUMBER) {
            throw new InvalidSpecificationException(VLD_MAGIC_KEY_INVALID_FIELD_TYPE, desc);
         }

         if (getMagicKeyBitLength() != DataBlockDescription.UNDEFINED && getMagicKeyBitLength() <= 0) {
            throw new InvalidSpecificationException(VLD_MAGIC_KEY_BIT_LENGTH_TOO_SMALL, desc, getMagicKeyBitLength());
         }

         if (getMagicKeyBitLength() != DataBlockDescription.UNDEFINED
            && getMagicKeyBitLength() > maximumByteLength * Byte.SIZE) {
            throw new InvalidSpecificationException(VLD_MAGIC_KEY_BIT_LENGTH_BIGGER_THAN_FIXED_SIZE, desc,
               maximumByteLength * Byte.SIZE, getMagicKeyBitLength());
         }
      }

      // Validate enumerated fields
      Map<byte[], Object> binaryValues = new HashMap<>();

      for (Iterator<?> enumValueIterator = getEnumeratedValues().keySet().iterator(); enumValueIterator.hasNext();) {
         Object nextKey = enumValueIterator.next();
         byte[] nextValue = getEnumeratedValues().get(nextKey);

         if (binaryValues.containsKey(nextValue)) {
            throw new InvalidSpecificationException(VLD_BINARY_ENUMERATED_VALUE_NOT_UNIQUE, desc,
               Arrays.toString(nextValue), nextKey, binaryValues.get(nextValue));
         }

         if (hasFixedSize && nextValue.length > minimumByteLength) {
            throw new InvalidSpecificationException(VLD_BINARY_ENUMERATED_VALUE_TOO_LONG, desc, nextKey,
               nextValue.length, minimumByteLength);
         }
      }

      if (!getEnumeratedValues().isEmpty() && getDefaultValue() != null) {
         if (!getEnumeratedValues().containsKey(getDefaultValue())) {
            throw new InvalidSpecificationException(VLD_DEFAULT_VALUE_NOT_ENUMERATED, desc, getDefaultValue());
         }
      }

      // Validate numeric fields
      if (getFieldType() == FieldType.UNSIGNED_WHOLE_NUMBER) {
         if (minimumByteLength > Long.BYTES || maximumByteLength > Long.BYTES) {
            throw new InvalidSpecificationException(VLD_NUMERIC_FIELD_TOO_LONG, desc, Long.BYTES, minimumByteLength,
               maximumByteLength);
         }
      } else {
         if (getFixedByteOrder() != null) {
            throw new InvalidSpecificationException(VLD_FIXED_BYTE_ORDER_NON_NUMERIC, desc);
         }
      }

      // Validate string fields
      if (getFieldType() != FieldType.STRING) {
         if (getFixedCharacterEncoding() != null) {
            throw new InvalidSpecificationException(VLD_FIXED_CHARSET_NON_STRING, desc);
         }

         if (getTerminationCharacter() != null) {
            throw new InvalidSpecificationException(VLD_TERMINATION_CHAR_NON_STRING, desc);
         }
      }

      // Validate field functions stand-alone
      List<FieldFunction> fieldFunctions = getFieldFunctions();

      for (FieldFunction fieldFunction : fieldFunctions) {
         FieldFunctionType<?> ffType = fieldFunction.getFieldFunctionType();
         if (ffType == FieldFunctionType.ID_OF || ffType == FieldFunctionType.CHARACTER_ENCODING_OF
            || ffType == FieldFunctionType.BYTE_ORDER_OF) {
            if (getFieldType() != FieldType.STRING) {
               throw new InvalidSpecificationException(VLD_FIELD_FUNC_NON_STRING, desc);
            }
         } else if (ffType == FieldFunctionType.SIZE_OF || ffType == FieldFunctionType.COUNT_OF) {
            if (getFieldType() != FieldType.UNSIGNED_WHOLE_NUMBER) {
               throw new InvalidSpecificationException(VLD_FIELD_FUNC_NON_NUMERIC, desc);
            }
         } else if (ffType == FieldFunctionType.PRESENCE_OF) {
            if (getFieldType() != FieldType.FLAGS) {
               throw new InvalidSpecificationException(VLD_FIELD_FUNC_NON_FLAGS, desc);
            }

            if (fieldFunction.getFlagName() == null || fieldFunction.getFlagValue() == null) {
               throw new InvalidSpecificationException(VLD_FIELD_FUNC_PRESENCE_OF_MISSING_FIELDS, desc,
                  fieldFunction.getFlagName(), fieldFunction.getFlagValue());
            }

            if (!getFlagSpecification().hasFlag(fieldFunction.getFlagName())) {
               throw new InvalidSpecificationException(VLD_FIELD_FUNC_PRESENCE_OF_UNSPECIFIED_FLAG_NAME, desc,
                  fieldFunction.getFlagName());
            }
         }

         if (ffType != FieldFunctionType.PRESENCE_OF) {
            if (fieldFunction.getFlagName() != null || fieldFunction.getFlagValue() != null) {
               throw new InvalidSpecificationException(VLD_FIELD_FUNC_FLAG_PROPERTIES_UNNECESSARY, desc,
                  fieldFunction.getFlagName(), fieldFunction.getFlagValue());
            }
         }
      }

      validateDefaultValue(messagePrefix, desc);
   }

   List<MagicKey> determineFieldMagicKeys(DataBlockDescription fieldDesc, long magicKeyOffset) {
      List<MagicKey> fieldMagicKeys = new ArrayList<>();

      if (!getEnumeratedValues().isEmpty()) {

         getEnumeratedValues().forEach((Object interpretedValue, byte[] binaryValue) -> {
            fieldMagicKeys.addAll(getFieldMagicKeys(fieldDesc, magicKeyOffset, binaryValue));
         });
      } else if (getDefaultValue() != null) {
         byte[] magicKeyBytes = null;

         if (getFieldType() == FieldType.STRING) {
            magicKeyBytes = ((String) getDefaultValue()).getBytes(Charsets.CHARSET_ASCII);
         } else if (getFieldType() == FieldType.BINARY) {
            magicKeyBytes = (byte[]) getDefaultValue();
         } else if (getFieldType() == FieldType.FLAGS) {
            magicKeyBytes = getFlagSpecification().getDefaultFlagBytes();
         } // Note that the else case cannot happen due to validation already done

         fieldMagicKeys.addAll(getFieldMagicKeys(fieldDesc, magicKeyOffset, magicKeyBytes));
      } // Note that the else case cannot happen due to validation already done

      return fieldMagicKeys;
   }

   /**
    * @param fieldDesc
    * @param magicKeyOffset
    * @param fieldMagicKeys
    * @param magicKeyBytes
    */
   private List<MagicKey> getFieldMagicKeys(DataBlockDescription fieldDesc, long magicKeyOffset, byte[] magicKeyBytes) {
      List<MagicKey> fieldMagicKeys = new ArrayList<>();

      if (getMagicKeyBitLength() != DataBlockDescription.UNDEFINED) {
         int maxMagicKeyBitLength = magicKeyBytes.length * Byte.SIZE;

         if (getMagicKeyBitLength() > maxMagicKeyBitLength) {
            throw new InvalidSpecificationException(VLD_MAGIC_KEY_BIT_LENGTH_TOO_BIG, fieldDesc, getMagicKeyBitLength(),
               maxMagicKeyBitLength);
         }

         int actualMagicKeyByteLength = (int) getMagicKeyBitLength() / Byte.SIZE
            + (getMagicKeyBitLength() % Byte.SIZE > 0 ? 1 : 0);

         byte[] adaptedMagicKeyBytes = magicKeyBytes;

         if (actualMagicKeyByteLength < magicKeyBytes.length) {
            adaptedMagicKeyBytes = new byte[actualMagicKeyByteLength];

            System.arraycopy(magicKeyBytes, 0, adaptedMagicKeyBytes, 0, actualMagicKeyByteLength);
         }

         fieldMagicKeys
            .add(new MagicKey(adaptedMagicKeyBytes, (int) getMagicKeyBitLength(), fieldDesc.getId(), magicKeyOffset));
      } else {
         fieldMagicKeys.add(new MagicKey(magicKeyBytes, fieldDesc.getId(), magicKeyOffset));
      }

      return fieldMagicKeys;
   }

   private void validateDefaultValue(String messagePrefix, DataBlockDescription fieldDesc) {
      long maximumByteLength = fieldDesc.getMaximumByteLength();

      if (getDefaultValue() != null) {
         ByteOrder byteOrderToUse = getFixedByteOrder() != null ? getFixedByteOrder() : ByteOrder.LITTLE_ENDIAN;
         Charset charsetToUse = getFixedCharacterEncoding() != null ? getFixedCharacterEncoding()
            : Charsets.CHARSET_ASCII;

         ByteBuffer defaultBinaryValue = null;
         try {
            defaultBinaryValue = getConverter().toBinary(getDefaultValue(), fieldDesc, byteOrderToUse, charsetToUse);
         } catch (InterpretedValueConversionException e) {
            throw new InvalidSpecificationException(VLD_DEFAULT_VALUE_CONVERSION_FAILED, fieldDesc, e,
               getDefaultValue());
         }

         if (maximumByteLength != DataBlockDescription.UNDEFINED
            && defaultBinaryValue.remaining() > maximumByteLength) {
            throw new InvalidSpecificationException(VLD_DEFAULT_VALUE_EXCEEDS_LENGTH, fieldDesc,
               Arrays.toString(defaultBinaryValue.array()), maximumByteLength);
         }
      }
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
