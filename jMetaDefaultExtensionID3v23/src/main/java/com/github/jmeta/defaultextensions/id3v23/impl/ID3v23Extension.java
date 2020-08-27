/**
 *
 * {@link APEv2Extension}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilderFactory;
import com.github.jmeta.library.dataformats.api.types.BitAddress;
import com.github.jmeta.library.dataformats.api.types.CharacterEncodingOf;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.PresenceOf;
import com.github.jmeta.library.dataformats.api.types.SizeOf;
import com.github.jmeta.library.dataformats.api.types.SummedSizeOf;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link ID3v23Extension}
 *
 */
public class ID3v23Extension implements Extension {

	public static final ContainerDataFormat ID3v23 = new ContainerDataFormat("ID3v2.3", new HashSet<String>(),
		new HashSet<String>(), new ArrayList<String>(), "M. Nilsson", new Date());

	static final String FRAME_FLAGS_COMPRESSION = "Compression";
	static final String FRAME_FLAGS_ENCRYPTION = "Encryption";
	static final String FRAME_FLAGS_FILE_ALTER_PRESERVATION = "File Alter Preservation";
	static final String FRAME_FLAGS_GROUP_IDENTITY = "Group Identity";
	static final String FRAME_FLAGS_READ_ONLY = "Read Only";
	static final String FRAME_FLAGS_TAG_ALTER_PRESERVATION = "Tag Alter Preservation";

	static final String EXT_HEADER_FLAG_CRC_DATA_PRESENT = "CRC data present";

	static final String TAG_FLAGS_EXPERIMENTAL_INDICATOR = "Experimental Indicator";
	static final String TAG_FLAGS_EXTENDED_HEADER = "Extended Header";
	static final String TAG_FLAGS_UNSYNCHRONIZATION = "Unsynchronization";

	static final DataBlockCrossReference REF_EXT_HEADER = new DataBlockCrossReference("Extended header");
	static final DataBlockCrossReference REF_TAG_HEADER_FLAGS = new DataBlockCrossReference("Header flags");
	static final DataBlockCrossReference REF_GENERIC_FRAME_HEADER_FLAGS = new DataBlockCrossReference(
		"Generic frame header flags");

	private static final SyncSafeIntegerConverter SYNC_SAFE_INTEGER_CONVERTER = new SyncSafeIntegerConverter();

	private final DataFormatSpecificationBuilderFactory specFactory = ComponentRegistry
		.lookupService(DataFormatSpecificationBuilderFactory.class);

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
			serviceProviders.add((T) new ID3v23DataBlocksService());
		}
		return serviceProviders;
	}

	/**
	 * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionDescription()
	 */
	@Override
	public ExtensionDescription getExtensionDescription() {
		return new ExtensionDescription("ID3v23", "jMeta", "1.0", null, "ID3v23 extension", null, null);
	}

	/**
	 * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionId()
	 */
	@Override
	public String getExtensionId() {
		return "DEFAULT_de.je.jmeta.defext.datablocks.impl.ID3v23DataBlocksExtension";
	}

	private DataFormatSpecification createSpecification() {

		final byte[] id3v23TagVersionBytes = new byte[] { 3, 0 };
		final byte[] id3v23TagIdBytes = new byte[] { 'I', 'D', '3' };
		final byte[] id3v23TagMagicKeyBytes = new byte[id3v23TagIdBytes.length + id3v23TagVersionBytes.length];

		for (int i = 0; i < id3v23TagIdBytes.length; i++) {
			id3v23TagMagicKeyBytes[i] = id3v23TagIdBytes[i];
		}

		for (int i = 0; i < id3v23TagVersionBytes.length; i++) {
			id3v23TagMagicKeyBytes[id3v23TagIdBytes.length + i] = id3v23TagVersionBytes[i];
		}

		DataFormatSpecificationBuilder builder = specFactory
			.createDataFormatSpecificationBuilder(ID3v23Extension.ID3v23);

		DataBlockCrossReference frameReference = new DataBlockCrossReference("Frame");
		DataBlockCrossReference textFrameReference = new DataBlockCrossReference("Text Frame");
		DataBlockCrossReference crcReference = new DataBlockCrossReference("CRC");
		DataBlockCrossReference decompressedSizeReference = new DataBlockCrossReference("Decompressed size");
		DataBlockCrossReference groupIdReference = new DataBlockCrossReference("Group id");
		DataBlockCrossReference encryptionMethodReference = new DataBlockCrossReference("Encryption method");
		DataBlockCrossReference informationReference = new DataBlockCrossReference("Information");
		DataBlockCrossReference payloadReference = new DataBlockCrossReference("Tag Payload");
		DataBlockCrossReference framePayloadReference = new DataBlockCrossReference("Frame payload");
		DataBlockCrossReference dataFieldReference = new DataBlockCrossReference("Data");
		DataBlockCrossReference paddingReference = new DataBlockCrossReference("Padding");
		DataBlockCrossReference paddingSizeReference = new DataBlockCrossReference("Padding Size");
		DataBlockCrossReference extHeaderFlagsReference = new DataBlockCrossReference("Ext Header Flags");

		DataBlockCrossReference frameIdReference = new DataBlockCrossReference("Frame Id");
		// @formatter:off
		return builder
			.addContainerWithContainerBasedPayload("id3v23", "id3v23 tag", "The id3v23 tag")
				.addHeader("header", "id3v23 tag header", "The id3v23 tag header")
					.referencedAs(ID3v23Extension.REF_TAG_HEADER_FLAGS)
					.addStringField("id", "id3v23 tag header id", "The id3v23 tag header id")
						.withStaticLengthOf(3).withDefaultValue("ID3").withFixedCharset(Charsets.CHARSET_ISO)
						.asMagicKey()
					.finishField()
					.addBinaryField("version", "id3v23 tag header version", "The id3v23 tag header version")
						.withStaticLengthOf(2).withDefaultValue(id3v23TagVersionBytes)
					.finishField()
					.addFlagsField("flags", "id3v23 tag header flags", "The id3v23 tag header flags")
						.withStaticLengthOf(1)
						.withFlagSpecification(1, ByteOrder.BIG_ENDIAN).withDefaultFlagBytes(new byte[] { 0 })
							.addFlagDescription(
								new FlagDescription(ID3v23Extension.TAG_FLAGS_UNSYNCHRONIZATION, new BitAddress(0, 7), "", 1, null))
							.addFlagDescription(
								new FlagDescription(ID3v23Extension.TAG_FLAGS_EXTENDED_HEADER, new BitAddress(0, 6), "", 1, null))
							.addFlagDescription(new FlagDescription(ID3v23Extension.TAG_FLAGS_EXPERIMENTAL_INDICATOR,
								new BitAddress(0, 5), "", 1, null))
						.finishFlagSpecification()
						.withFieldFunction(
							new PresenceOf(ID3v23Extension.REF_EXT_HEADER, ID3v23Extension.TAG_FLAGS_EXTENDED_HEADER, 1))
					.finishField()
					.addNumericField("size", "id3v23 tag size", "The id3v23 tag size")
						.withStaticLengthOf(4).withFieldFunction(new SummedSizeOf(ID3v23Extension.REF_EXT_HEADER, payloadReference))
						.withCustomConverter(ID3v23Extension.SYNC_SAFE_INTEGER_CONVERTER)
					.finishField()
				.finishHeader()
				.addHeader("extHeader", "id3v23 extended header", "The id3v23 extended header")
					.referencedAs(ID3v23Extension.REF_EXT_HEADER).asOptional()
					.addNumericField("size", "id3v23 extended header size", "The id3v23 extended header size")
						.withStaticLengthOf(4).withDefaultValue(Long.valueOf(0x2000))
						.withFieldFunction(new SummedSizeOf(extHeaderFlagsReference, paddingSizeReference, crcReference))
						// NOTE: This additional SizeOf is necessary to ensure the validation of the
						// SummedSizeOf for id3v23 tag
						// header size is working
						.withFieldFunction(new SizeOf(ID3v23Extension.REF_EXT_HEADER))
					.finishField()
					.addFlagsField("flags", "id3v23 extended header flags", "The id3v23 extended header flags")
						.referencedAs(extHeaderFlagsReference).withStaticLengthOf(2).withFlagSpecification(2, ByteOrder.BIG_ENDIAN)
						.withDefaultFlagBytes(new byte[] { 0, 0 })
						.addFlagDescription(new FlagDescription(ID3v23Extension.EXT_HEADER_FLAG_CRC_DATA_PRESENT,
							new BitAddress(0, 7), "", 1, null))
						.finishFlagSpecification()
						.withFieldFunction(new PresenceOf(crcReference, ID3v23Extension.EXT_HEADER_FLAG_CRC_DATA_PRESENT, 1))
					.finishField()
					.addNumericField("paddingSize", "id3v23 extended header padding size",
						"The id3v23 extended header padding size")
						.referencedAs(paddingSizeReference).withStaticLengthOf(4).withFieldFunction(new SizeOf(paddingReference))
					.finishField()
					.addNumericField("crc", "id3v23 extended header CRC", "The id3v23 extended header CRC")
						.referencedAs(crcReference).asOptional().withStaticLengthOf(4)
					.finishField()
				.finishHeader()
				.getPayload()
					.referencedAs(payloadReference).withDescription("payload", "The id3v23 payload")
					.addGenericContainerWithFieldBasedPayload("FRAME_ID", "GENERIC_ID3v23_FRAME", "The id3v23 GENERIC_FRAME")
						.referencedAs(frameReference)
						.asDefaultNestedContainer()
							.withIdField(frameIdReference)
							.addHeader("header", "Generic frame header", "The generic frame header")
								.referencedAs(ID3v23Extension.REF_GENERIC_FRAME_HEADER_FLAGS)
								.addStringField("id", "Generic frame id field", "The generic frame id field").withStaticLengthOf(4)
									.withFixedCharset(Charsets.CHARSET_ISO).referencedAs(frameIdReference)
								.finishField()
								.addNumericField("size", "Generic frame size field", "The generic frame size field")
									.withStaticLengthOf(4).withFieldFunction(new SizeOf(framePayloadReference))
									.withCustomConverter(ID3v23Extension.SYNC_SAFE_INTEGER_CONVERTER)
								.finishField()
								.addFlagsField("flags", "Generic frame flags field", "The generic frame flags field")
									.withStaticLengthOf(2)
									.withFlagSpecification(2, ByteOrder.BIG_ENDIAN).withDefaultFlagBytes(new byte[] { 0, 0 })
										.addFlagDescription(new FlagDescription(ID3v23Extension.FRAME_FLAGS_TAG_ALTER_PRESERVATION,
											new BitAddress(0, 0), "", 1, null))
										.addFlagDescription(new FlagDescription(ID3v23Extension.FRAME_FLAGS_FILE_ALTER_PRESERVATION,
											new BitAddress(0, 1), "", 1, null))
										.addFlagDescription(new FlagDescription(ID3v23Extension.FRAME_FLAGS_READ_ONLY,
											new BitAddress(0, 2), "", 1, null))
										.addFlagDescription(new FlagDescription(ID3v23Extension.FRAME_FLAGS_COMPRESSION,
											new BitAddress(1, 0), "", 1, null))
										.addFlagDescription(new FlagDescription(ID3v23Extension.FRAME_FLAGS_ENCRYPTION,
											new BitAddress(1, 1), "", 1, null))
										.addFlagDescription(new FlagDescription(ID3v23Extension.FRAME_FLAGS_GROUP_IDENTITY,
											new BitAddress(1, 2), "", 1, null))
									.finishFlagSpecification()
									.withFieldFunction(new PresenceOf(decompressedSizeReference, ID3v23Extension.FRAME_FLAGS_COMPRESSION, 1))
									.withFieldFunction(new PresenceOf(groupIdReference, ID3v23Extension.FRAME_FLAGS_GROUP_IDENTITY, 1))
									.withFieldFunction(new PresenceOf(encryptionMethodReference, ID3v23Extension.FRAME_FLAGS_ENCRYPTION, 1))
								.finishField()
							.finishHeader()
							.getPayload()
								.referencedAs(framePayloadReference)
								.withDescription("Generic frame payload", "The generic frame payload")
								.addNumericField("decompressedSize", "Decompressed size field", "The decompressed size field")
									.referencedAs(decompressedSizeReference).withStaticLengthOf(4).asOptional()
								.finishField()
								.addNumericField("encryptionMethod", "Encryption method field", "The encryption method field")
									.referencedAs(encryptionMethodReference).withStaticLengthOf(1).asOptional()
								.finishField()
								.addNumericField("groupId", "Decompressed size field", "The decompressed size field")
									.referencedAs(groupIdReference).withStaticLengthOf(1).asOptional()
								.finishField()
								.addBinaryField("data", "Payload data field", "The payload data field")
									.referencedAs(dataFieldReference)
									.withLengthOf(1, DataBlockDescription.UNDEFINED)
								.finishField()
							.finishFieldBasedPayload()
						.finishContainer()
						.addContainerWithFieldBasedPayload("TEXT_FRAME_ID", "GENERIC_ID3v23_TEXT_FRAME",
							"The id3v23 GENERIC_TEXT_FRAME")
							.cloneFrom(frameReference).referencedAs(textFrameReference).getPayload()
							.withoutField(dataFieldReference)
							.addStringField("textEncoding", "Text encoding", "Text encoding")
								.withStaticLengthOf(1).withDefaultValue(Charsets.CHARSET_ISO.name())
								.withFieldFunction(new CharacterEncodingOf(informationReference))
								.addEnumeratedValue(new byte[] { 0 }, Charsets.CHARSET_ISO.name())
								.addEnumeratedValue(new byte[] { 1 }, Charsets.CHARSET_UTF16.name())
							.finishField()
							.addStringField("information", "Information", "Information")
								.referencedAs(informationReference)
								.withTerminationCharacter('\u0000').withLengthOf(1, DataBlockDescription.UNDEFINED)
							.finishField()
						.finishFieldBasedPayload()
					.finishContainer()
					.addContainerWithFieldBasedPayload("TPE1", "Lead performer/soloist", "The ID3v23 Lead performer/soloist")
						.cloneFrom(textFrameReference)
					.finishContainer()
					.addContainerWithFieldBasedPayload("TRCK", "Track number/Position in set",
						"The ID3v23 Track number/Position in set")
						.cloneFrom(textFrameReference)
					.finishContainer()
					.addContainerWithFieldBasedPayload("TIT2", "Title/songname/content description",
						"The ID3v23 Title/songname/content description")
						.cloneFrom(textFrameReference)
					.finishContainer()
					.addContainerWithFieldBasedPayload("padding", "Padding", "Padding")
						.referencedAs(paddingReference)
						.addHeader("header", "Padding header", "Padding header")
							.addBinaryField("key", "id3v23 padding header key", "The id3v23 padding header key")
								.withDefaultValue(new byte[] { 0 }).withStaticLengthOf(1).asMagicKey()
							.finishField()
						.finishHeader()
						.getPayload()
							.withDescription("Padding payload", "Padding payload")
							.addBinaryField("bytes", "Padding bytes", "Padding bytes")
								.withDefaultValue(new byte[] { 0 })
								.withLengthOf(0, DataBlockDescription.UNDEFINED)
							.finishField()
						.finishFieldBasedPayload()
					.finishContainer()
				.finishContainerBasedPayload()
			.finishContainer()
			.withByteOrders(ByteOrder.BIG_ENDIAN)
			.withCharsets(Charsets.CHARSET_ISO, Charsets.CHARSET_UTF16).build();
		// @formatter:on
	}
}
