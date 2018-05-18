/**
 *
 * {@link TESTBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.types.BitAddress;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.library.dataformats.impl.builder.TopLevelContainerBuilder;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link TESTBuilder}
 *
 */
public class TESTBuilder {
   private static final byte[] ID3V23_TAG_VERSION_BYTES = new byte[] { 3, 0 };
   private static final int ID3V23_TAG_FLAG_SIZE = 1;
   public static final DataBlockId ID3V23_TAG_ID = new DataBlockId(ID3v23Extension.ID3v23, "id3v23");
   private static final byte[] ID3V23_TAG_ID_BYTES = new byte[] { 'I', 'D', '3' };
   private static final String ID3V23_TAG_ID_STRING = "ID3";
   private static final byte[] ID3V23_TAG_MAGIC_KEY_BYTES = new byte[ID3V23_TAG_ID_BYTES.length
      + ID3V23_TAG_VERSION_BYTES.length];
   private static final DataBlockId PADDING_BYTES_FIELD_ID = new DataBlockId(ID3v23Extension.ID3v23,
      "id3v23.payload.padding.payload.bytes");
   private static final String TAG_FLAGS_EXPERIMENTAL_INDICATOR = "Experimental Indicator";
   private static final String TAG_FLAGS_EXTENDED_HEADER = "Extended Header";
   public static final String TAG_FLAGS_UNSYNCHRONIZATION = "Unsynchronization";
private static final DataBlockId ID3V23_EXTENDED_HEADER_ID = new DataBlockId(ID3v23Extension.ID3v23, "id3v23.extHeader");
private static final String ID3V23_GENERIC_CONTAINER_ID = "id3v23.payload.${FRAME_ID}";
public static final DataBlockId GENERIC_FRAME_HEADER_FRAME_FLAGS_FIELD_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".header.flags");
private static final DataBlockId GENERIC_FRAME_HEADER_FRAME_ID_FIELD_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".header.id");
private static final DataBlockId GENERIC_FRAME_HEADER_FRAME_SIZE_FIELD_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".header.size");
private static final DataBlockId GENERIC_FRAME_HEADER_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".header");
private static final DataBlockId GENERIC_FRAME_ID = new DataBlockId(ID3v23Extension.ID3v23, ID3V23_GENERIC_CONTAINER_ID);
private static final DataBlockId GENERIC_FRAME_PAYLOAD_DATA_FIELD_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".payload.data");
private static final DataBlockId GENERIC_FRAME_PAYLOAD_DECOMPRESSED_SIZE_FIELD_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".payload.decompressedSize");
private static final DataBlockId GENERIC_FRAME_PAYLOAD_ENCRYPTION_METHOD_FIELD_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".payload.encryptionMethod");
private static final DataBlockId GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".payload.groupId");
private static final DataBlockId GENERIC_FRAME_PAYLOAD_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".payload");
private static final DataBlockId GENERIC_INFORMATION_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".payload.information");
private static final DataBlockId GENERIC_TEXT_ENCODING_ID = new DataBlockId(ID3v23Extension.ID3v23,
   ID3V23_GENERIC_CONTAINER_ID + ".payload.textEncoding");
private static final DataBlockId ID3V23_PAYLOAD_ID = new DataBlockId(ID3v23Extension.ID3v23, "id3v23.payload");
private static final String EXT_HEADER_FLAG_CRC_DATA_PRESENT = "CRC data present";
private static final DataBlockId ID3V23_EXTENDED_HEADER_FIELD_CRC_ID = new DataBlockId(ID3v23Extension.ID3v23,
   "id3v23.extHeader.crc");
private static final DataBlockId ID3V23_EXTENDED_HEADER_FIELD_FLAGS_ID = new DataBlockId(ID3v23Extension.ID3v23,
   "id3v23.extHeader.flags");
private static final DataBlockId ID3V23_EXTENDED_HEADER_FIELD_PADDINGSIZE_ID = new DataBlockId(ID3v23Extension.ID3v23,
   "id3v23.extHeader.paddingSize");
private static final DataBlockId ID3V23_EXTENDED_HEADER_FIELD_SIZE_ID = new DataBlockId(ID3v23Extension.ID3v23,
   "id3v23.extHeader.size");
private static final int FRAME_ID_SIZE = 4;
public static final String FRAME_FLAGS_COMPRESSION = "Compression";
private static final String FRAME_FLAGS_ENCRYPTION = "Encryption";
private static final String FRAME_FLAGS_FILE_ALTER_PRESERVATION = "File Alter Preservation";
private static final String FRAME_FLAGS_GROUP_IDENTITY = "Group Identity";
private static final String FRAME_FLAGS_READ_ONLY = "Read Only";
private static final String FRAME_FLAGS_TAG_ALTER_PRESERVATION = "Tag Alter Preservation";
private static final int ID3V23_FRAME_FLAG_SIZE = 2;

   /**
    * Creates a new {@link TESTBuilder}.
    */
   public static Map<DataBlockId, DataBlockDescription> build() {
      // 3. tag flags
      List<FlagDescription> tagFlagDescriptions = new ArrayList<>();

      tagFlagDescriptions.add(new FlagDescription(TAG_FLAGS_UNSYNCHRONIZATION, new BitAddress(0, 0), "", 1, null));
      tagFlagDescriptions.add(new FlagDescription(TAG_FLAGS_EXTENDED_HEADER, new BitAddress(0, 1), "", 1, null));
      tagFlagDescriptions.add(new FlagDescription(TAG_FLAGS_EXPERIMENTAL_INDICATOR, new BitAddress(0, 2), "", 1, null));

      final byte[] defaultTagFlagBytes = new byte[] { 0 };

      FlagSpecification id3v23TagFlagSpec = new FlagSpecification(tagFlagDescriptions, ID3V23_TAG_FLAG_SIZE,
         ByteOrder.BIG_ENDIAN, defaultTagFlagBytes);

      Flags defaultTagFlags = new Flags(id3v23TagFlagSpec);

      defaultTagFlags.fromArray(defaultTagFlagBytes);

      Set<DataBlockId> affectedDataBlockIds = new HashSet<>();

      affectedDataBlockIds.add(ID3V23_EXTENDED_HEADER_ID);

      Set<DataBlockId> affectedDataBlockIdsUnsync = new HashSet<>();

      affectedDataBlockIdsUnsync.add(GENERIC_FRAME_HEADER_ID);
      affectedDataBlockIdsUnsync.add(GENERIC_FRAME_PAYLOAD_ID);
      affectedDataBlockIdsUnsync.add(ID3V23_EXTENDED_HEADER_ID);

      // 4. tag size
      Set<DataBlockId> affectedTagSizeBlocks = new HashSet<>();

      affectedTagSizeBlocks.add(ID3V23_PAYLOAD_ID);

      ContainerSequenceBuilder<List<DataBlockDescription>> builder = new TopLevelContainerBuilder(ID3v23Extension.ID3v23);

      // 2. Extended header flags
      List<FlagDescription> extendedHeaderFlagDescriptions = new ArrayList<>();

      extendedHeaderFlagDescriptions
         .add(new FlagDescription(EXT_HEADER_FLAG_CRC_DATA_PRESENT, new BitAddress(0, 0), "", 1, null));

      final byte[] defaultExtHeaderFlagBytes = new byte[] { 0 };

      FlagSpecification id3v23ExtHeaderFlagSpec = new FlagSpecification(extendedHeaderFlagDescriptions, 1,
         ByteOrder.BIG_ENDIAN, defaultExtHeaderFlagBytes);

      Flags defaultExtHeaderFlags = new Flags(id3v23TagFlagSpec);

      defaultExtHeaderFlags.fromArray(defaultTagFlagBytes);

      Set<DataBlockId> affectedDataBlockIdsExtHead = new HashSet<>();

      affectedDataBlockIdsExtHead.add(ID3V23_EXTENDED_HEADER_FIELD_CRC_ID);

      // 1 Frame id
      Set<DataBlockId> affectedBlocks = new HashSet<>();

      affectedBlocks.add(GENERIC_FRAME_ID);
      
      // 2 Frame size
      Set<DataBlockId> affectedFrameSizeBlocks = new HashSet<>();

      affectedFrameSizeBlocks.add(GENERIC_FRAME_PAYLOAD_ID);

      // 3 Frame flags
      List<FlagDescription> frameFlagDescriptions = new ArrayList<>();

      frameFlagDescriptions
         .add(new FlagDescription(FRAME_FLAGS_TAG_ALTER_PRESERVATION, new BitAddress(0, 0), "", 1, null));
      frameFlagDescriptions
         .add(new FlagDescription(FRAME_FLAGS_FILE_ALTER_PRESERVATION, new BitAddress(0, 1), "", 1, null));
      frameFlagDescriptions.add(new FlagDescription(FRAME_FLAGS_READ_ONLY, new BitAddress(0, 2), "", 1, null));
      frameFlagDescriptions.add(new FlagDescription(FRAME_FLAGS_COMPRESSION, new BitAddress(1, 0), "", 1, null));
      frameFlagDescriptions.add(new FlagDescription(FRAME_FLAGS_ENCRYPTION, new BitAddress(1, 1), "", 1, null));
      frameFlagDescriptions.add(new FlagDescription(FRAME_FLAGS_GROUP_IDENTITY, new BitAddress(1, 2), "", 1, null));

      final byte[] defaultFrameFlagBytes = new byte[] { 0, 0 };

      FlagSpecification id3v23FrameFlagSpec = new FlagSpecification(frameFlagDescriptions, ID3V23_FRAME_FLAG_SIZE,
         ByteOrder.BIG_ENDIAN, defaultFrameFlagBytes);

      Flags defaultFrameFlags = new Flags(id3v23FrameFlagSpec);

      defaultFrameFlags.fromArray(defaultFrameFlagBytes);

      Set<DataBlockId> affectedDataBlockIdsCompression = new HashSet<>();

      affectedDataBlockIdsCompression.add(GENERIC_FRAME_PAYLOAD_DECOMPRESSED_SIZE_FIELD_ID);

      Set<DataBlockId> affectedDataBlockIdsCompressionTrafo = new HashSet<>();

      affectedDataBlockIdsCompressionTrafo.add(GENERIC_FRAME_PAYLOAD_ID);

      Set<DataBlockId> affectedDataBlockIdsGroup = new HashSet<>();

      affectedDataBlockIdsGroup.add(GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID);

      Set<DataBlockId> affectedDataBlockIdsEncryption = new HashSet<>();

      affectedDataBlockIdsEncryption.add(GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID);

      Set<DataBlockId> affectedDataBlockIdsEncryptionTrafo = new HashSet<>();

      affectedDataBlockIdsEncryptionTrafo.add(GENERIC_FRAME_PAYLOAD_ID);

      builder
      .addContainerWithContainerBasedPayload("id3v23", "id3v23 tag", "The id3v23 tag")
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
               .withDefaultValue(defaultTagFlags)
               .withFlagSpecification(id3v23TagFlagSpec)
               .withFieldFunction(new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedDataBlockIds, TAG_FLAGS_EXTENDED_HEADER, 1))
            .finishField()
            .addNumericField("size", "id3v23 tag size", "The id3v23 tag size")
               .withStaticLengthOf(4)
               .withFieldFunction(new FieldFunction(FieldFunctionType.SIZE_OF, affectedTagSizeBlocks, null, null))
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
               .withFlagSpecification(id3v23ExtHeaderFlagSpec)
               .withDefaultValue(defaultExtHeaderFlags)
               .withFieldFunction(
         new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedDataBlockIdsExtHead, EXT_HEADER_FLAG_CRC_DATA_PRESENT, 1))
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
               .withOccurrences(0, 999999)
               .addHeader("header", "Generic frame header", "The generic frame header")
                  .withLengthOf(10, 10)
                  .addStringField("id", "Generic frame id field", "The generic frame id field")
                     .withStaticLengthOf(FRAME_ID_SIZE)
                     .withFieldFunction(new FieldFunction(FieldFunctionType.ID_OF, affectedBlocks, null, null))
                     .withFixedCharset(Charsets.CHARSET_ISO)
                  .finishField()
                  .addNumericField("size", "Generic frame size field", "The generic frame size field")
                     .withStaticLengthOf(4)
                     .withFieldFunction(new FieldFunction(FieldFunctionType.SIZE_OF, affectedFrameSizeBlocks, null, null))
                  .finishField()
                  .addFlagsField("flags", "Generic frame flags field", "The generic frame flags field")
                     .withStaticLengthOf(2)
                     .withFlagSpecification(id3v23FrameFlagSpec)
                     .withDefaultValue(defaultFrameFlags)
                     .withFieldFunction(
         new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedDataBlockIdsCompression, FRAME_FLAGS_COMPRESSION, 1))
                     .withFieldFunction(
                        new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedDataBlockIdsGroup, FRAME_FLAGS_GROUP_IDENTITY, 1))
                     .withFieldFunction(
                        new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedDataBlockIdsEncryption, FRAME_FLAGS_ENCRYPTION, 1))
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
            .finishContainer();

      List<DataBlockDescription> topLevelContainers = builder.finishContainerSequence();

      Map<DataBlockId, DataBlockDescription> topLevelContainerMap = topLevelContainers.stream()
         .collect(Collectors.toMap(b -> b.getId(), b -> b));

      return topLevelContainerMap;
   }

   @Test
   public void testBuilders() {
      final DataBlockId apeV2TagId = new DataBlockId(ID3v23Extension.ID3v23, "apev2");

      final DataBlockId apeV2GenericItemId = new DataBlockId(ID3v23Extension.ID3v23, "apev2.payload.${ITEM_ID}");
      final DataBlockId apeV2GenericItemHeaderId = new DataBlockId(ID3v23Extension.ID3v23,
         "apev2.payload.${ITEM_ID}.header");
      final DataBlockId apeV2GenericItemPayloadId = new DataBlockId(ID3v23Extension.ID3v23,
         "apev2.payload.${ITEM_ID}.payload");
      final DataBlockId apeV2GenericItemValueSizeId = new DataBlockId(ID3v23Extension.ID3v23,
         "apev2.payload.${ITEM_ID}.header.size");
      final DataBlockId apeV2GenericItemFlagsId = new DataBlockId(ID3v23Extension.ID3v23,
         "apev2.payload.${ITEM_ID}.header.flags");
      final DataBlockId apeV2GenericItemKeyId = new DataBlockId(ID3v23Extension.ID3v23,
         "apev2.payload.${ITEM_ID}.header.key");
      final DataBlockId apeV2GenericItemValueId = new DataBlockId(ID3v23Extension.ID3v23,
         "apev2.payload.${ITEM_ID}.payload.value");

      Map<DataBlockId, DataBlockDescription> blockMap = TESTBuilder.build();

      Map<DataBlockId, DataBlockDescription> descMap = new ID3v23Extension().getDescMap();

      ArrayList<DataBlockId> arrayList = new ArrayList<>(descMap.keySet());
      Collections.sort(arrayList, (s1, s2) -> s1.getGlobalId().compareTo(s2.getGlobalId()));
      ArrayList<DataBlockId> arrayList2 = new ArrayList<>(blockMap.keySet());
      Collections.sort(arrayList2, (s1, s2) -> s1.getGlobalId().compareTo(s2.getGlobalId()));

      Assert.assertEquals(arrayList.toString(), arrayList2.toString());

      Assert.assertEquals(descMap.keySet(), blockMap.keySet());

      List<DataBlockDescription> valuesLeft = new ArrayList<>(descMap.values());
      List<DataBlockDescription> valuesRight = new ArrayList<>(blockMap.values());

      valuesLeft.sort((d1, d2) -> d1.getId().getGlobalId().compareTo(d2.getId().getGlobalId()));
      valuesRight.sort((d1, d2) -> d1.getId().getGlobalId().compareTo(d2.getId().getGlobalId()));

      Assert.assertEquals(valuesLeft, valuesRight);

      Assert.assertEquals(valuesLeft.toString(), valuesRight.toString());

      Assert.assertEquals(descMap, blockMap);

      System.out.println(blockMap.equals(descMap));
   }
}
