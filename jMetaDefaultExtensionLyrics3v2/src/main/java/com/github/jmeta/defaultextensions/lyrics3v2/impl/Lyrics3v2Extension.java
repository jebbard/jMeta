/**
 *
 * {@link APEv2Extension}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.defaultextensions.lyrics3v2.impl;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilderFactory;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.SizeOf;
import com.github.jmeta.library.dataformats.api.types.SummedSizeOf;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link Lyrics3v2Extension}
 *
 */
public class Lyrics3v2Extension implements Extension {

	private static final Lyrics3v2StringSizeIntegerConverter STRING_SIZE_INTEGER_CONVERTER = new Lyrics3v2StringSizeIntegerConverter();

	/**
	*
	*/
	public static final ContainerDataFormat LYRICS3v2 = new ContainerDataFormat("Lyrics3v2", new HashSet<String>(),
		new HashSet<String>(), new ArrayList<String>(), "", new Date());

	static final DataBlockCrossReference REF_TAG = new DataBlockCrossReference("Tag");

	private final DataFormatSpecificationBuilderFactory specFactory = ComponentRegistry
		.lookupService(DataFormatSpecificationBuilderFactory.class);

	private DataFormatSpecification createSpecification() {
		final String lyrics3v2MagicFooterString = "LYRICS200";
		final String lyrics3v2MagicHeaderString = "LYRICSBEGIN";

		DataFormatSpecificationBuilder builder = specFactory
			.createDataFormatSpecificationBuilder(Lyrics3v2Extension.LYRICS3v2);

		DataBlockCrossReference fieldReference = new DataBlockCrossReference("Field");
		DataBlockCrossReference headerReference = new DataBlockCrossReference("Header");
		DataBlockCrossReference payloadReference = new DataBlockCrossReference("Payload");
		DataBlockCrossReference fieldPayloadReference = new DataBlockCrossReference("Field Payload");

		DataBlockCrossReference idFieldReference = new DataBlockCrossReference("Field id");
		// @formatter:off
		return builder.addContainerWithContainerBasedPayload("lyrics3v2", "Lyrics3v2 Tag", "The Lyrics3v2 Tag")
			.referencedAs(Lyrics3v2Extension.REF_TAG).addHeader("header", "Lyrics3v2 header", "The Lyrics3v2 header")
			.referencedAs(headerReference).addStringField("id", "Lyrics3v2 header id", "Lyrics3v2 header id")
			.withStaticLengthOf(lyrics3v2MagicHeaderString.length()).withDefaultValue(lyrics3v2MagicHeaderString)
			.asMagicKey().finishField().finishHeader().getPayload().referencedAs(payloadReference)
			.withDescription("Lyrics3v2 payload", "The Lyrics3v2 payload")
			.addGenericContainerWithFieldBasedPayload("FIELD_ID", "Lyrics3v2 field", "The Lyrics3v2 field")
			.referencedAs(fieldReference).asDefaultNestedContainer()
			.withIdField(idFieldReference = new DataBlockCrossReference("Field id"))
			.addHeader("header", "Lyrics3v2 field header", "The Lyrics3v2 field header")
			.addStringField("id", "Lyrics3v2 field id", "Lyrics3v2 field id").withStaticLengthOf(3)
			.referencedAs(idFieldReference).finishField()
			.addNumericField("size", "Lyrics3v2 item value size", "Lyrics3v2 item value size")
			.withCustomConverter(Lyrics3v2Extension.STRING_SIZE_INTEGER_CONVERTER).withStaticLengthOf(5)
			.withFieldFunction(new SizeOf(fieldPayloadReference)).finishField().finishHeader().getPayload()
			.referencedAs(fieldPayloadReference)
			.withDescription("Lyrics3v2 field payload", "The Lyrics3v2 field payload")
			.addStringField("value", "Lyrics3v2 field data", "Lyrics3v2 field data")
			.withLengthOf(0, DataBlockDescription.UNDEFINED).finishField().finishFieldBasedPayload().finishContainer()
			.finishContainerBasedPayload().addFooter("footer", "Lyrics3v2 footer", "The Lyrics3v2 footer")
			.addNumericField("size", "Lyrics3v2 footer tag size", "Lyrics3v2 footer tag size")
			.withCustomConverter(Lyrics3v2Extension.STRING_SIZE_INTEGER_CONVERTER).withStaticLengthOf(6)
			.withFieldFunction(new SummedSizeOf(headerReference, payloadReference)).finishField()
			.addStringField("id", "Lyrics3v2 footer id", "Lyrics3v2 footer id")
			.withStaticLengthOf(lyrics3v2MagicFooterString.length()).withDefaultValue(lyrics3v2MagicFooterString)
			.asMagicKey().finishField().finishFooter().finishContainer().withByteOrders(ByteOrder.LITTLE_ENDIAN)
			.withCharsets(Charsets.CHARSET_ISO).build();
		// @formatter:on
	}

	/**
	 * @see com.github.jmeta.utility.extmanager.api.services.Extension#getAllServiceProviders(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> getAllServiceProviders(Class<T> serviceInterface) {
		List<T> serviceProviders = new ArrayList<>();

		if (serviceInterface == DataFormatSpecification.class) {
			serviceProviders.add((T) createSpecification());
		} else if (serviceInterface == DataBlockService.class) {
			serviceProviders.add((T) new Lyrics3v2DataBlocksService());
		}
		return serviceProviders;
	}

	/**
	 * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionDescription()
	 */
	@Override
	public ExtensionDescription getExtensionDescription() {
		return new ExtensionDescription("Lyrics3v2", "jMeta", "1.0", null, "Lyrics3v2 extension", null, null);
	}

	/**
	 * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionId()
	 */
	@Override
	public String getExtensionId() {
		return "DEFAULT_de.je.jmeta.defext.datablocks.impl.Lyrics3v2DataBlocksExtension";
	}

}
