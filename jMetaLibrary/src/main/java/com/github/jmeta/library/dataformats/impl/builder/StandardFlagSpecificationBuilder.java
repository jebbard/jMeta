/**
 *
 * {@link StandardFlagSpecificationBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 21.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardFlagSpecificationBuilder} allows to build flag specifications.
 *
 * @param <P> The parent type of the parent field builder
 */
public class StandardFlagSpecificationBuilder<P extends DataBlockDescriptionBuilder<P>>
	implements FlagSpecificationBuilder<P> {

	private final AbstractFieldBuilder<P, Flags, FlagsFieldBuilder<P>> parentBuilder;

	private final int flagByteLength;
	private final ByteOrder flagByteOrder;
	private final List<FlagDescription> flagDescriptions = new ArrayList<>();
	private byte[] defaultFlagBytes;

	/**
	 * Creates a new {@link StandardFlagSpecificationBuilder}.
	 *
	 * @param parentBuilder  The parent {@link DataFormatBuilder}
	 * @param flagByteLength The byte length of the flags
	 * @param flagByteOrder  The byte order of the flag bytes
	 */
	public StandardFlagSpecificationBuilder(AbstractFieldBuilder<P, Flags, FlagsFieldBuilder<P>> parentBuilder,
		int flagByteLength, ByteOrder flagByteOrder) {
		Reject.ifNull(parentBuilder, "parentBuilder");

		this.parentBuilder = parentBuilder;
		this.flagByteLength = flagByteLength;
		this.flagByteOrder = flagByteOrder;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder#addFlagDescription(com.github.jmeta.library.dataformats.api.types.FlagDescription)
	 */
	@Override
	public FlagSpecificationBuilder<P> addFlagDescription(FlagDescription flagDesc) {
		Reject.ifNull(flagDesc, "flagDesc");
		flagDescriptions.add(flagDesc);
		return this;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder#finishFlagSpecification()
	 */
	@SuppressWarnings("unchecked")
	@Override
	public FlagsFieldBuilder<P> finishFlagSpecification() {
		FlagSpecification flagSpecification = new FlagSpecification(flagDescriptions, flagByteLength, flagByteOrder,
			this.defaultFlagBytes);
		parentBuilder.setFlagSpecification(flagSpecification);
		parentBuilder.withDefaultValue(new Flags(flagSpecification));
		return (FlagsFieldBuilder<P>) parentBuilder;
	}

	/**
	 * @see com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder#withDefaultFlagBytes(byte[])
	 */
	@Override
	public FlagSpecificationBuilder<P> withDefaultFlagBytes(byte[] defaultFlagBytes) {
		this.defaultFlagBytes = defaultFlagBytes;
		return this;
	}
}
