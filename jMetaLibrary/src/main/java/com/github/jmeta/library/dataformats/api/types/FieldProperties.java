/**
 * {@link FieldProperties}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

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
 * {@link FieldProperties} represent all properties of a field.
 *
 * @param <F> The interpreted type of the field
 */
public class FieldProperties<F> {

	/**
	 * The maximum size of a field
	 */
	public static final long MAX_FIELD_SIZE = Integer.MAX_VALUE;

	private static final Map<FieldType<?>, FieldConverter<?>> FIELD_CONVERTERS = new HashMap<>();
	static {
		FieldProperties.FIELD_CONVERTERS.put(FieldType.BINARY, new BinaryFieldConverter());
		FieldProperties.FIELD_CONVERTERS.put(FieldType.FLAGS, new FlagsFieldConverter());
		FieldProperties.FIELD_CONVERTERS.put(FieldType.UNSIGNED_WHOLE_NUMBER, new UnsignedNumericFieldConverter());
		FieldProperties.FIELD_CONVERTERS.put(FieldType.STRING, new StringFieldConverter());
	}

	@SuppressWarnings("unchecked")
	private static <F> FieldConverter<F> getDefaultFieldConverter(FieldType<F> fieldType) {
		return (FieldConverter<F>) FieldProperties.FIELD_CONVERTERS.get(fieldType);
	}

	private final FieldConverter<F> converter;
	private final F defaultValue;
	private final Map<F, byte[]> enumeratedValues = new HashMap<>();
	private final FieldType<F> fieldType;
	private final ByteOrder fixedByteOrder;
	private final Charset fixedCharacterEncoding;
	private final FlagSpecification flagSpecification;
	private final List<AbstractFieldFunction<F>> functions = new ArrayList<>();

	private final boolean isMagicKey;
	private final long magicKeyBitLength;

	private final Character terminationCharacter;

	/**
	 * Creates a new {@link FieldProperties}.
	 *
	 * @param fieldType            The {@link FieldType}
	 * @param defaultValue         The default value of the field, might be null
	 * @param enumeratedValues     The enumerated values that might occur for the
	 *                             field, might be empty
	 * @param terminationCharacter The termination character for string fields or
	 *                             null
	 * @param flagSpecification    The {@link FlagSpecification} for flags fields or
	 *                             null
	 * @param fixedCharset         The fixed {@link Charset} for string fields or
	 *                             null
	 * @param fixedByteOrder       The fixed {@link ByteOrder} for numeric fields or
	 *                             null
	 * @param functions            The {@link AbstractFieldFunction}s of the field,
	 *                             must not be null
	 * @param isMagicKey           true if the field acts as a magic key, false
	 *                             otherwise
	 * @param magicKeyBitLength    The magic key's bit length
	 * @param customConverter      A custom {@link FieldConverter} to use, pass null
	 *                             to use a default {@link FieldConverter}
	 */
	public FieldProperties(FieldType<F> fieldType, F defaultValue, Map<F, byte[]> enumeratedValues,
		Character terminationCharacter, FlagSpecification flagSpecification, Charset fixedCharset,
		ByteOrder fixedByteOrder, List<AbstractFieldFunction<F>> functions, boolean isMagicKey, long magicKeyBitLength,
		FieldConverter<F> customConverter) {
		Reject.ifNull(fieldType, "fieldType");
		Reject.ifNull(enumeratedValues, "enumeratedValues");
		Reject.ifNull(functions, "functions");

		this.fieldType = fieldType;
		this.defaultValue = defaultValue;
		this.converter = customConverter == null ? FieldProperties.getDefaultFieldConverter(fieldType)
			: customConverter;
		this.enumeratedValues.putAll(enumeratedValues);
		this.functions.addAll(functions);
		this.isMagicKey = isMagicKey;
		this.magicKeyBitLength = magicKeyBitLength;
		this.terminationCharacter = terminationCharacter;
		this.fixedCharacterEncoding = fixedCharset;
		this.flagSpecification = flagSpecification;
		this.fixedByteOrder = fixedByteOrder;
	}

	List<MagicKey> determineFieldMagicKeys(DataBlockDescription fieldDesc, long magicKeyOffset) {
		List<MagicKey> fieldMagicKeys = new ArrayList<>();

		if (!getEnumeratedValues().isEmpty()) {

			getEnumeratedValues().forEach((Object interpretedValue, byte[] binaryValue) -> fieldMagicKeys
				.addAll(getFieldMagicKeys(fieldDesc, magicKeyOffset, binaryValue)));
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

	private String enumValuesToString() {
		String toString = "{";

		for (Iterator<F> iterator = enumeratedValues.keySet().iterator(); iterator.hasNext();) {
			F nextKey = iterator.next();
			byte[] nextValue = enumeratedValues.get(nextKey);
			toString += interpretedValueToString(nextKey) + "=" + Arrays.toString(nextValue);
		}

		return toString + "}";
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		FieldProperties<?> other = (FieldProperties<?>) obj;
		if (converter == null) {
			if (other.converter != null) {
				return false;
			}
		} else if (!converter.equals(other.converter)) {
			return false;
		}
		if (defaultValue == null) {
			if (other.defaultValue != null) {
				return false;
			}
		} else if (!defaultValue.equals(other.defaultValue)) {
			return false;
		}
		if (enumeratedValues == null) {
			if (other.enumeratedValues != null) {
				return false;
			}
		} else if (!enumeratedValues.equals(other.enumeratedValues)) {
			return false;
		}
		if (fieldType == null) {
			if (other.fieldType != null) {
				return false;
			}
		} else if (!fieldType.equals(other.fieldType)) {
			return false;
		}
		if (fixedByteOrder == null) {
			if (other.fixedByteOrder != null) {
				return false;
			}
		} else if (!fixedByteOrder.equals(other.fixedByteOrder)) {
			return false;
		}
		if (fixedCharacterEncoding == null) {
			if (other.fixedCharacterEncoding != null) {
				return false;
			}
		} else if (!fixedCharacterEncoding.equals(other.fixedCharacterEncoding)) {
			return false;
		}
		if (flagSpecification == null) {
			if (other.flagSpecification != null) {
				return false;
			}
		} else if (!flagSpecification.equals(other.flagSpecification)) {
			return false;
		}
		if (functions == null) {
			if (other.functions != null) {
				return false;
			}
		} else if (!functions.equals(other.functions)) {
			return false;
		}
		if (isMagicKey != other.isMagicKey) {
			return false;
		}
		if (magicKeyBitLength != other.magicKeyBitLength) {
			return false;
		}
		if (terminationCharacter == null) {
			if (other.terminationCharacter != null) {
				return false;
			}
		} else if (!terminationCharacter.equals(other.terminationCharacter)) {
			return false;
		}
		return true;
	}

	public FieldConverter<F> getConverter() {
		return converter;
	}

	public F getDefaultValue() {

		return defaultValue;
	}

	public Map<F, byte[]> getEnumeratedValues() {

		return Collections.unmodifiableMap(enumeratedValues);
	}

	public List<AbstractFieldFunction<F>> getFieldFunctions() {
		return Collections.unmodifiableList(functions);
	}

	private List<MagicKey> getFieldMagicKeys(DataBlockDescription fieldDesc, long magicKeyOffset,
		byte[] magicKeyBytes) {
		List<MagicKey> fieldMagicKeys = new ArrayList<>();

		if (getMagicKeyBitLength() != DataBlockDescription.UNDEFINED) {
			int maxMagicKeyBitLength = magicKeyBytes.length * Byte.SIZE;

			if (getMagicKeyBitLength() > maxMagicKeyBitLength) {
				throw new InvalidSpecificationException(InvalidSpecificationException.VLD_MAGIC_KEY_BIT_LENGTH_TOO_BIG,
					fieldDesc, getMagicKeyBitLength(), maxMagicKeyBitLength);
			}

			int actualMagicKeyByteLength = ((int) getMagicKeyBitLength() / Byte.SIZE)
				+ ((getMagicKeyBitLength() % Byte.SIZE) > 0 ? 1 : 0);

			byte[] adaptedMagicKeyBytes = magicKeyBytes;

			if (actualMagicKeyByteLength < magicKeyBytes.length) {
				adaptedMagicKeyBytes = new byte[actualMagicKeyByteLength];

				System.arraycopy(magicKeyBytes, 0, adaptedMagicKeyBytes, 0, actualMagicKeyByteLength);
			}

			fieldMagicKeys.add(
				new MagicKey(adaptedMagicKeyBytes, (int) getMagicKeyBitLength(), fieldDesc.getId(), magicKeyOffset));
		} else {
			fieldMagicKeys.add(new MagicKey(magicKeyBytes, fieldDesc.getId(), magicKeyOffset));
		}

		return fieldMagicKeys;
	}

	public FieldType<F> getFieldType() {

		return fieldType;
	}

	public ByteOrder getFixedByteOrder() {

		return fixedByteOrder;
	}

	public Charset getFixedCharacterEncoding() {

		return fixedCharacterEncoding;
	}

	public FlagSpecification getFlagSpecification() {

		return flagSpecification;
	}

	public long getMagicKeyBitLength() {
		return magicKeyBitLength;
	}

	public Character getTerminationCharacter() {

		return terminationCharacter;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = (prime * result) + (converter == null ? 0 : converter.hashCode());
		result = (prime * result) + (defaultValue == null ? 0 : defaultValue.hashCode());
		result = (prime * result) + (enumeratedValues == null ? 0 : enumeratedValues.hashCode());
		result = (prime * result) + (fieldType == null ? 0 : fieldType.hashCode());
		result = (prime * result) + (fixedByteOrder == null ? 0 : fixedByteOrder.hashCode());
		result = (prime * result) + (fixedCharacterEncoding == null ? 0 : fixedCharacterEncoding.hashCode());
		result = (prime * result) + (flagSpecification == null ? 0 : flagSpecification.hashCode());
		result = (prime * result) + (functions == null ? 0 : functions.hashCode());
		result = (prime * result) + (isMagicKey ? 1231 : 1237);
		result = (prime * result) + (int) (magicKeyBitLength ^ (magicKeyBitLength >>> 32));
		result = (prime * result) + (terminationCharacter == null ? 0 : terminationCharacter.hashCode());
		return result;
	}

	private String interpretedValueToString(F interpretedValue) {
		if (interpretedValue == null) {
			return "(null)";
		}

		return interpretedValue instanceof byte[] ? Arrays.toString((byte[]) interpretedValue)
			: interpretedValue.toString();
	}

	public boolean isMagicKey() {
		return isMagicKey;
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

	private void validateDefaultValue(DataBlockDescription fieldDesc) {
		long maximumByteLength = fieldDesc.getMaximumByteLength();

		if (getDefaultValue() != null) {
			ByteOrder byteOrderToUse = getFixedByteOrder() != null ? getFixedByteOrder() : ByteOrder.LITTLE_ENDIAN;
			Charset charsetToUse = getFixedCharacterEncoding() != null ? getFixedCharacterEncoding()
				: Charsets.CHARSET_ASCII;

			ByteBuffer defaultBinaryValue = null;
			try {
				defaultBinaryValue = getConverter().toBinary(getDefaultValue(), fieldDesc, byteOrderToUse,
					charsetToUse);
			} catch (InterpretedValueConversionException e) {
				throw new InvalidSpecificationException(
					InvalidSpecificationException.VLD_DEFAULT_VALUE_CONVERSION_FAILED, fieldDesc, e, getDefaultValue());
			}

			if ((maximumByteLength != DataBlockDescription.UNDEFINED)
				&& (defaultBinaryValue.remaining() > maximumByteLength)) {
				throw new InvalidSpecificationException(InvalidSpecificationException.VLD_DEFAULT_VALUE_EXCEEDS_LENGTH,
					fieldDesc, Arrays.toString(defaultBinaryValue.array()), maximumByteLength);
			}
		}
	}

	void validateFieldProperties(DataBlockDescription desc) {
		long minimumByteLength = desc.getMinimumByteLength();
		long maximumByteLength = desc.getMaximumByteLength();

		boolean hasFixedSize = minimumByteLength == maximumByteLength;

		if ((maximumByteLength != DataBlockDescription.UNDEFINED)
			&& (maximumByteLength > FieldProperties.MAX_FIELD_SIZE)) {
			throw new InvalidSpecificationException(InvalidSpecificationException.VLD_TOO_BIG_FIELD_LENGTH, desc,
				maximumByteLength);
		}

		if ((minimumByteLength != DataBlockDescription.UNDEFINED)
			&& (minimumByteLength > FieldProperties.MAX_FIELD_SIZE)) {
			throw new InvalidSpecificationException(InvalidSpecificationException.VLD_TOO_BIG_FIELD_LENGTH, desc,
				minimumByteLength);
		}

		// Validate magic key
		if (isMagicKey()) {
			if (!hasFixedSize || (maximumByteLength == DataBlockDescription.UNDEFINED)) {
				throw new InvalidSpecificationException(
					InvalidSpecificationException.VLD_MAGIC_KEY_INVALID_FIELD_LENGTH, desc, minimumByteLength,
					maximumByteLength);
			}

			if (getEnumeratedValues().isEmpty() && (getDefaultValue() == null)) {
				throw new InvalidSpecificationException(InvalidSpecificationException.VLD_MAGIC_KEY_INVALID_FIELD_VALUE,
					desc);
			}

			if (getFieldType() == FieldType.UNSIGNED_WHOLE_NUMBER) {
				throw new InvalidSpecificationException(InvalidSpecificationException.VLD_MAGIC_KEY_INVALID_FIELD_TYPE,
					desc);
			}

			if ((getMagicKeyBitLength() != DataBlockDescription.UNDEFINED) && (getMagicKeyBitLength() <= 0)) {
				throw new InvalidSpecificationException(
					InvalidSpecificationException.VLD_MAGIC_KEY_BIT_LENGTH_TOO_SMALL, desc, getMagicKeyBitLength());
			}

			if ((getMagicKeyBitLength() != DataBlockDescription.UNDEFINED)
				&& (getMagicKeyBitLength() > (maximumByteLength * Byte.SIZE))) {
				throw new InvalidSpecificationException(
					InvalidSpecificationException.VLD_MAGIC_KEY_BIT_LENGTH_BIGGER_THAN_FIXED_SIZE, desc,
					maximumByteLength * Byte.SIZE, getMagicKeyBitLength());
			}
		}

		// Validate enumerated fields
		Map<byte[], Object> binaryValues = new HashMap<>();

		for (Iterator<?> enumValueIterator = getEnumeratedValues().keySet().iterator(); enumValueIterator.hasNext();) {
			Object nextKey = enumValueIterator.next();
			@SuppressWarnings("unlikely-arg-type")
			byte[] nextValue = getEnumeratedValues().get(nextKey);

			if (binaryValues.containsKey(nextValue)) {
				throw new InvalidSpecificationException(
					InvalidSpecificationException.VLD_BINARY_ENUMERATED_VALUE_NOT_UNIQUE, desc,
					Arrays.toString(nextValue), nextKey, binaryValues.get(nextValue));
			}

			if (hasFixedSize && (nextValue.length > minimumByteLength)) {
				throw new InvalidSpecificationException(
					InvalidSpecificationException.VLD_BINARY_ENUMERATED_VALUE_TOO_LONG, desc, nextKey, nextValue.length,
					minimumByteLength);
			}
		}

		if (!getEnumeratedValues().isEmpty() && (getDefaultValue() != null)
			&& !getEnumeratedValues().containsKey(getDefaultValue())) {
			throw new InvalidSpecificationException(InvalidSpecificationException.VLD_DEFAULT_VALUE_NOT_ENUMERATED,
				desc, getDefaultValue());
		}

		// Validate numeric fields
		if (getFieldType() == FieldType.UNSIGNED_WHOLE_NUMBER) {
			if ((minimumByteLength > Long.BYTES) || (maximumByteLength > Long.BYTES)) {
				throw new InvalidSpecificationException(InvalidSpecificationException.VLD_NUMERIC_FIELD_TOO_LONG, desc,
					Long.BYTES, minimumByteLength, maximumByteLength);
			}
		} else {
			if (getFixedByteOrder() != null) {
				throw new InvalidSpecificationException(InvalidSpecificationException.VLD_FIXED_BYTE_ORDER_NON_NUMERIC,
					desc);
			}
		}

		// Validate string fields
		if (getFieldType() != FieldType.STRING) {
			if (getFixedCharacterEncoding() != null) {
				throw new InvalidSpecificationException(InvalidSpecificationException.VLD_FIXED_CHARSET_NON_STRING,
					desc);
			}

			if (getTerminationCharacter() != null) {
				throw new InvalidSpecificationException(InvalidSpecificationException.VLD_TERMINATION_CHAR_NON_STRING,
					desc);
			}
		}

		validateDefaultValue(desc);
	}
}
