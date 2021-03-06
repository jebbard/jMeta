/**
 *
 * {@link AbstractFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder;
import com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.dataformats.api.types.converter.FieldConverter;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractFieldBuilder} is the base class for all
 * {@link DataBlockDescriptionBuilder}s that build fields. As such, it holds all
 * attributes necessary for creating {@link FieldProperties}.
 *
 * @param <P> The parent type of this builder
 * @param <F> The interpreted type of the concrete field built by this class
 * @param <C> The concrete derived interface of the class implementing this
 *            {@link AbstractDataFormatSpecificationBuilder}
 */
public abstract class AbstractFieldBuilder<P extends DataBlockDescriptionBuilder<P>, F, C extends FieldBuilder<P, F, C>>
	extends AbstractDataFormatSpecificationBuilder<P, C> implements FieldBuilder<P, F, C> {

	private Character terminationCharacter;
	private F defaultValue;
	private FlagSpecification flagSpecification;
	private Charset fixedCharset;
	private ByteOrder fixedByteOrder;
	private boolean isMagicKey = false;
	private final Map<F, byte[]> enumeratedValues = new HashMap<>();
	private final FieldType<F> fieldType;
	private final List<AbstractFieldFunction<F>> functions = new ArrayList<>();
	private long magicKeyBitLength = DataBlockDescription.UNDEFINED;
	private FieldConverter<F> customConverter;

	/**
	 * Creates a new {@link AbstractFieldBuilder}.
	 *
	 * @param parentBuilder The parent {@link DataFormatBuilder}. Required for
	 *                      allowing a fluent API, as it is returned by the
	 *                      {@link #finish()} method. Must not be null.
	 * @param localId       The local id of the data block. Must not be null and
	 *                      must not contain the
	 *                      {@link DataBlockId#SEGMENT_SEPARATOR}.
	 * @param name          The human-readable name of the data block in its
	 *                      specification
	 * @param description   The description of the data block from its specification
	 * @param fieldType     The {@link FieldType} of the field
	 * @param isGeneric     true if it is a generic data block, false otherwise
	 */
	public AbstractFieldBuilder(P parentBuilder, String localId, String name, String description,
		FieldType<F> fieldType, boolean isGeneric) {
		super(parentBuilder, localId, name, description, PhysicalDataBlockType.FIELD, isGeneric);
		Reject.ifNull(fieldType, "fieldType");

		this.fieldType = fieldType;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#addEnumeratedValue(byte[],
	 *      java.lang.Object)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public C addEnumeratedValue(byte[] binaryValue, F interpretedValue) {
		enumeratedValues.put(interpretedValue, binaryValue);
		return (C) this;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.impl.builder.AbstractFieldBuilder#asMagicKey()
	 */
	@Override
	@SuppressWarnings("unchecked")
	public C asMagicKey() {
		this.isMagicKey = true;
		return (C) this;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#asMagicKeyWithOddBitLength(long)
	 */
	@Override
	public C asMagicKeyWithOddBitLength(long bitLength) {
		this.magicKeyBitLength = bitLength;
		return asMagicKey();
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#finishField()
	 */
	@Override
	public P finishField() {
		FieldProperties<F> fieldProperties = new FieldProperties<>(fieldType, defaultValue, enumeratedValues,
			terminationCharacter, flagSpecification, fixedCharset, fixedByteOrder, functions, isMagicKey,
			magicKeyBitLength, customConverter);

		setFieldProperties(fieldProperties);

		return finish();
	}

	protected void setFixedByteOrder(ByteOrder fixedByteOrder) {
		this.fixedByteOrder = fixedByteOrder;
	}

	protected void setFixedCharset(Charset fixedCharset) {
		this.fixedCharset = fixedCharset;
	}

	protected void setFlagSpecification(FlagSpecification flagSpecification) {
		this.flagSpecification = flagSpecification;
	}

	protected void setTerminationCharacter(Character terminationCharacter) {
		this.terminationCharacter = terminationCharacter;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#withCustomConverter(com.github.jmeta.library.dataformats.api.types.converter.FieldConverter)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public C withCustomConverter(FieldConverter<F> customConverter) {
		Reject.ifNull(customConverter, "customConverter");
		this.customConverter = customConverter;
		return (C) this;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#withDefaultValue(java.lang.Object)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public C withDefaultValue(F value) {
		this.defaultValue = value;
		return (C) this;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#withFieldFunction(com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction)
	 */
	@Override
	@SuppressWarnings("unchecked")
	public C withFieldFunction(AbstractFieldFunction<F> fieldFunction) {
		Reject.ifNull(fieldFunction, "fieldFunction");

		functions.add(fieldFunction);

		return (C) this;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#withLengthOf(long,
	 *      long)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public C withLengthOf(long minimumByteLength, long maximumByteLength) {
		setLengths(minimumByteLength, maximumByteLength);
		return (C) this;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FieldBuilder#withStaticLengthOf(long)
	 */
	@Override
	public C withStaticLengthOf(long staticByteLength) {
		return withLengthOf(staticByteLength, staticByteLength);
	}

}
