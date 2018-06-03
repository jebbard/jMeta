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
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link ID3v23Extension}
 *
 */
public class ID3v23Extension implements Extension {

   private final DataFormatSpecificationBuilderFactory specFactory = ComponentRegistry
      .lookupService(DataFormatSpecificationBuilderFactory.class);

   private static final String EXT_HEADER_FLAG_CRC_DATA_PRESENT = "CRC data present";
   public static final String FRAME_FLAGS_COMPRESSION = "Compression";
   private static final String FRAME_FLAGS_ENCRYPTION = "Encryption";
   private static final String FRAME_FLAGS_FILE_ALTER_PRESERVATION = "File Alter Preservation";
   private static final String FRAME_FLAGS_GROUP_IDENTITY = "Group Identity";
   private static final String FRAME_FLAGS_READ_ONLY = "Read Only";
   private static final String FRAME_FLAGS_TAG_ALTER_PRESERVATION = "Tag Alter Preservation";
   private static final int FRAME_ID_SIZE = 4;
   private static final String ID3V23_GENERIC_CONTAINER_ID = "id3v23.payload.${FRAME_ID}";
   private static final String ID3V23_TEXT_FRAME_ID = "id3v23.payload.TEXT_FRAME_ID";
   private static final byte[] ID3V23_TAG_VERSION_BYTES = new byte[] { 3, 0 };
   /**
    *
    */
   public static final ContainerDataFormat ID3v23 = new ContainerDataFormat("ID3v2.3", new HashSet<String>(),
      new HashSet<String>(), new ArrayList<String>(), "M. Nilsson", new Date());
   public static final DataBlockId GENERIC_FRAME_HEADER_FRAME_FLAGS_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".header.flags");
   private static final DataBlockId GENERIC_FRAME_ID = new DataBlockId(ID3v23, ID3V23_GENERIC_CONTAINER_ID);
   private static final DataBlockId GENERIC_FRAME_PAYLOAD_DECOMPRESSED_SIZE_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload.decompressedSize");
   private static final DataBlockId GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload.groupId");
   private static final DataBlockId GENERIC_FRAME_PAYLOAD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload");

   private static final DataBlockId GENERIC_TEXT_FRAME_ID = new DataBlockId(ID3v23, ID3V23_TEXT_FRAME_ID);

   private static final DataBlockId GENERIC_INFORMATION_ID = new DataBlockId(ID3v23,
      ID3V23_TEXT_FRAME_ID + ".payload.information");
   private static final DataBlockId GENERIC_FRAME_PAYLOAD_DATA_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload.data");
   private static final DataBlockId ID3V23_EXTENDED_HEADER_FIELD_CRC_ID = new DataBlockId(ID3v23,
      "id3v23.extHeader.crc");
   private static final DataBlockId ID3V23_EXTENDED_HEADER_ID = new DataBlockId(ID3v23, "id3v23.extHeader");
   private static final int ID3V23_FRAME_FLAG_SIZE = 2;
   public static final DataBlockId ID3V23_HEADER_FLAGS_FIELD_ID = new DataBlockId(ID3v23, "id3v23.header.flags");
   private static final DataBlockId ID3V23_PAYLOAD_ID = new DataBlockId(ID3v23, "id3v23.payload");
   private static final int ID3V23_TAG_FLAG_SIZE = 1;
   public static final DataBlockId ID3V23_TAG_ID = new DataBlockId(ID3v23, "id3v23");
   private static final byte[] ID3V23_TAG_ID_BYTES = new byte[] { 'I', 'D', '3' };
   private static final String ID3V23_TAG_ID_STRING = "ID3";
   private static final byte[] ID3V23_TAG_MAGIC_KEY_BYTES = new byte[ID3V23_TAG_ID_BYTES.length
      + ID3V23_TAG_VERSION_BYTES.length];
   private static final String TAG_FLAGS_EXPERIMENTAL_INDICATOR = "Experimental Indicator";
   private static final String TAG_FLAGS_EXTENDED_HEADER = "Extended Header";
   public static final String TAG_FLAGS_UNSYNCHRONIZATION = "Unsynchronization";

   static {
      for (int i = 0; i < ID3V23_TAG_ID_BYTES.length; i++) {
         ID3V23_TAG_MAGIC_KEY_BYTES[i] = ID3V23_TAG_ID_BYTES[i];
      }

      for (int i = 0; i < ID3V23_TAG_VERSION_BYTES.length; i++) {
         ID3V23_TAG_MAGIC_KEY_BYTES[ID3V23_TAG_ID_BYTES.length + i] = ID3V23_TAG_VERSION_BYTES[i];
      }
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionId()
    */
   @Override
   public String getExtensionId() {
      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.ID3v23DataBlocksExtension";
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionDescription()
    */
   @Override
   public ExtensionDescription getExtensionDescription() {
      return new ExtensionDescription("ID3v23", "jMeta", "1.0", null, "ID3v23 extension", null, null);
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
         serviceProviders.add((T) new ID3v23DataBlocksService());
      }
      return serviceProviders;
   }

   private DataFormatSpecification createSpecification() {

      DataFormatSpecificationBuilder builder = specFactory.createDataFormatSpecificationBuilder(ID3v23Extension.ID3v23);

      // @formatter:off
      return builder.addContainerWithContainerBasedPayload("id3v23", "id3v23 tag", "The id3v23 tag")
         .withLengthOf(21, DataBlockDescription.UNLIMITED)
         .addHeader("header", "id3v23 tag header", "The id3v23 tag header")
            .withStaticLengthOf(10)
            .addStringField("id", "id3v23 tag header id", "The id3v23 tag header id")
               .withStaticLengthOf(3)
               .withDefaultValue(ID3V23_TAG_ID_STRING)
               .withFixedCharset(Charsets.CHARSET_ISO)
               .asMagicKey()
            .finishField()
            .addBinaryField("version", "id3v23 tag header version", "The id3v23 tag header version")
               .withStaticLengthOf(2)
               .withDefaultValue(ID3V23_TAG_VERSION_BYTES)
            .finishField()
            .addFlagsField("flags", "id3v23 tag header flags", "The id3v23 tag header flags")
               .withStaticLengthOf(ID3V23_TAG_FLAG_SIZE)
               .withFlagSpecification(1,ByteOrder.BIG_ENDIAN)
                  .withDefaultFlagBytes(new byte[] { 0 })
                  .addFlagDescription(new FlagDescription(TAG_FLAGS_UNSYNCHRONIZATION, new BitAddress(0, 0), "", 1, null))
                  .addFlagDescription(new FlagDescription(TAG_FLAGS_EXTENDED_HEADER, new BitAddress(0, 1), "", 1, null))
                  .addFlagDescription(new FlagDescription(TAG_FLAGS_EXPERIMENTAL_INDICATOR, new BitAddress(0, 2), "", 1, null))
               .finishFlagSpecification()
               .indicatesPresenceOf(TAG_FLAGS_EXTENDED_HEADER, 1, ID3V23_EXTENDED_HEADER_ID)
            .finishField()
            .addNumericField("size", "id3v23 tag size", "The id3v23 tag size")
               .withStaticLengthOf(4)
               .asSizeOf(ID3V23_PAYLOAD_ID)
            .finishField()
         .finishHeader()
         .addHeader("extHeader", "id3v23 extended header", "The id3v23 extended header")
            .withLengthOf(10, 14)
            .withOccurrences(0, 1)
            .addNumericField("size", "id3v23 extended header size", "The id3v23 extended header size")
               .withStaticLengthOf(4)
               .withDefaultValue(Long.valueOf(0x2000))
            .finishField()
            .addFlagsField("flags", "id3v23 extended header flags", "The id3v23 extended header flags")
               .withStaticLengthOf(ID3V23_TAG_FLAG_SIZE)
               .withFlagSpecification(1, ByteOrder.BIG_ENDIAN)
                  .withDefaultFlagBytes(new byte[] { 0 })
                  .addFlagDescription(new FlagDescription(EXT_HEADER_FLAG_CRC_DATA_PRESENT, new BitAddress(0, 0), "", 1, null))
               .finishFlagSpecification()
               .indicatesPresenceOf(EXT_HEADER_FLAG_CRC_DATA_PRESENT, 1, ID3V23_EXTENDED_HEADER_FIELD_CRC_ID)
            .finishField()
            .addNumericField("paddingSize", "id3v23 extended header padding size", "The id3v23 extended header padding size")
               .withStaticLengthOf(4)
            .finishField()
            .addNumericField("crc", "id3v23 extended header CRC", "The id3v23 extended header CRC")
               .withStaticLengthOf(4)
            .finishField()
         .finishHeader()
         .getPayload()
            .withDescription("payload", "The id3v23 payload")
            .withLengthOf(11, DataBlockDescription.UNLIMITED)
            .addGenericContainerWithFieldBasedPayload("FRAME_ID", "GENERIC_ID3v23_FRAME", "The id3v23 GENERIC_FRAME")
               .withLengthOf(11, DataBlockDescription.UNLIMITED)
               .asDefaultNestedContainer()
               .withOccurrences(0, 999999)
               .addHeader("header", "Generic frame header", "The generic frame header")
                  .withLengthOf(10, 10)
                  .addStringField("id", "Generic frame id field", "The generic frame id field")
                     .withStaticLengthOf(FRAME_ID_SIZE)
                     .asIdOf(GENERIC_FRAME_ID)
                     .withFixedCharset(Charsets.CHARSET_ISO)
                  .finishField()
                  .addNumericField("size", "Generic frame size field", "The generic frame size field")
                     .withStaticLengthOf(4)
                     .asSizeOf(GENERIC_FRAME_PAYLOAD_ID)
                  .finishField()
                  .addFlagsField("flags", "Generic frame flags field", "The generic frame flags field")
                     .withStaticLengthOf(2)
                     .withFlagSpecification(ID3V23_FRAME_FLAG_SIZE, ByteOrder.BIG_ENDIAN)
                        .withDefaultFlagBytes(new byte[] { 0, 0 })
                        .addFlagDescription(new FlagDescription(FRAME_FLAGS_TAG_ALTER_PRESERVATION, new BitAddress(0, 0), "", 1, null))
                        .addFlagDescription(new FlagDescription(FRAME_FLAGS_FILE_ALTER_PRESERVATION, new BitAddress(0, 1), "", 1, null))
                        .addFlagDescription(new FlagDescription(FRAME_FLAGS_READ_ONLY, new BitAddress(0, 2), "", 1, null))
                        .addFlagDescription(new FlagDescription(FRAME_FLAGS_COMPRESSION, new BitAddress(1, 0), "", 1, null))
                        .addFlagDescription(new FlagDescription(FRAME_FLAGS_ENCRYPTION, new BitAddress(1, 1), "", 1, null))
                        .addFlagDescription(new FlagDescription(FRAME_FLAGS_GROUP_IDENTITY, new BitAddress(1, 2), "", 1, null))
                     .finishFlagSpecification()
                     .indicatesPresenceOf(FRAME_FLAGS_COMPRESSION, 1, GENERIC_FRAME_PAYLOAD_DECOMPRESSED_SIZE_FIELD_ID)
                     .indicatesPresenceOf(FRAME_FLAGS_GROUP_IDENTITY, 1, GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID)
                     .indicatesPresenceOf(FRAME_FLAGS_ENCRYPTION, 1, GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID)
                  .finishField()
               .finishHeader()
               .getPayload()
                  .withDescription("Generic frame payload", "The generic frame payload")
                  .withLengthOf(1, DataBlockDescription.UNLIMITED)
                  .addNumericField("decompressedSize", "Decompressed size field", "The decompressed size field")
                     .withStaticLengthOf(4)
                     .withOccurrences(0, 1)
                  .finishField()
                  .addNumericField("encryptionMethod", "Encryption method field", "The encryption method field")
                     .withStaticLengthOf(1)
                     .withOccurrences(0, 1)
                  .finishField()
                  .addNumericField("groupId", "Decompressed size field", "The decompressed size field")
                     .withStaticLengthOf(1)
                     .withOccurrences(0, 1)
                  .finishField()
                  .addBinaryField("data", "Payload data field", "The payload data field")
                     .withLengthOf(1, DataBlockDescription.UNLIMITED)
                  .finishField()
               .finishFieldBasedPayload()
            .finishContainer()
            .addContainerWithFieldBasedPayload("TEXT_FRAME_ID", "GENERIC_ID3v23_TEXT_FRAME", "The id3v23 GENERIC_TEXT_FRAME")
               .cloneFrom(GENERIC_FRAME_ID)
               .getPayload()
                  .withoutField(GENERIC_FRAME_PAYLOAD_DATA_FIELD_ID.getLocalId())
                  .addStringField("textEncoding", "Text encoding", "Text encoding")
                     .withStaticLengthOf(1)
                     .withDefaultValue(Charsets.CHARSET_ISO.name())
                     .asCharacterEncodingOf(GENERIC_INFORMATION_ID)
                     .addEnumeratedValue(new byte[] { 0 }, Charsets.CHARSET_ISO.name())
                     .addEnumeratedValue(new byte[] { 1 }, Charsets.CHARSET_UTF16.name())
                  .finishField()
                  .addStringField("information", "Information", "Information")
                     .withTerminationCharacter('\u0000')
                     .withLengthOf(1, DataBlockDescription.UNLIMITED)
                  .finishField()
               .finishFieldBasedPayload()
            .finishContainer()
            .addContainerWithFieldBasedPayload("TPE1", "Lead performer/soloist", "The ID3v23 Lead performer/soloist")
               .cloneFrom(GENERIC_TEXT_FRAME_ID)
            .finishContainer()
            .addContainerWithFieldBasedPayload("TRCK", "Track number/Position in set", "The ID3v23 Track number/Position in set")
               .cloneFrom(GENERIC_TEXT_FRAME_ID)
            .finishContainer()
            .addContainerWithFieldBasedPayload("TIT2", "Title/songname/content description", "The ID3v23 Title/songname/content description")
               .cloneFrom(GENERIC_TEXT_FRAME_ID)
            .finishContainer()
            .addContainerWithFieldBasedPayload("padding", "Padding", "Padding")
               .withOccurrences(0, 1)
               .withLengthOf(1, DataBlockDescription.UNLIMITED)
               .addHeader("header", "Padding header", "Padding header")
                  .withStaticLengthOf(1)
                  .addBinaryField("key", "id3v23 padding header key", "The id3v23 padding header key")
                     .withDefaultValue(new byte[] { 0 })
                     .withStaticLengthOf(1)
                     .asMagicKey()
                  .finishField()
               .finishHeader()
               .getPayload()
                  .withDescription("Padding payload", "Padding payload")
                  .withLengthOf(0, DataBlockDescription.UNLIMITED)
                  .addBinaryField("bytes", "Padding bytes", "Padding bytes")
                     .withDefaultValue(new byte[] { 0 })
                     .withLengthOf(0, DataBlockDescription.UNLIMITED)
                  .finishField()
               .finishFieldBasedPayload()
            .finishContainer()
        .finishContainerBasedPayload()
     .finishContainer()
     .withByteOrders(ByteOrder.BIG_ENDIAN)
     .withCharsets(Charsets.CHARSET_ISO, Charsets.CHARSET_UTF16)
     .build();
      // @formatter:on
   }
}
