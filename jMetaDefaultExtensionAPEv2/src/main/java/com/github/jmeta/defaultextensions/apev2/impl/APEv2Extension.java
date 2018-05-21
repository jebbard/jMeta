/**
 *
 * {@link APEv2Extension}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.defaultextensions.apev2.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.StandardDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.types.BitAddress;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.library.dataformats.impl.builder.TopLevelContainerSequenceBuilder;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link APEv2Extension}
 *
 */
public class APEv2Extension implements Extension {

   private static final String APE_MAGIC_KEY_STRING = "APETAGEX";
   private static final int APEv2_HEADER_FOOTER_BYTE_LENGTH = 32;
   private static final int APEv2_MIN_ITEM_HEADER_LENGTH = 9;
   private static final int PREAMPLE_BYTE_LENGTH = 8;
   /**
    *
    */
   public static final ContainerDataFormat APEv2 = new ContainerDataFormat("APEv2", new HashSet<String>(),
      new HashSet<String>(), new ArrayList<String>(), "", new Date());

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionId()
    */
   @Override
   public String getExtensionId() {
      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.APEv2DataBlocksExtension";
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionDescription()
    */
   @Override
   public ExtensionDescription getExtensionDescription() {
      return new ExtensionDescription("APEv2", "jMeta", "1.0", null, "APEv2 extension", null, null);
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
         serviceProviders.add((T) new APEv2DataBlocksService());
      }
      return serviceProviders;
   }

   private DataFormatSpecification createSpecification() {

      // Data blocks
      final DataBlockId apeV2TagId = new DataBlockId(APEv2, "apev2");

      final DataBlockId apeV2GenericItemId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}");
      final DataBlockId apeV2GenericItemHeaderId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.header");
      final DataBlockId apeV2GenericItemPayloadId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.payload");
      final DataBlockId apeV2GenericItemValueSizeId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.header.size");
      final DataBlockId apeV2GenericItemFlagsId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.header.flags");
      final DataBlockId apeV2GenericItemKeyId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.header.key");
      final DataBlockId apeV2GenericItemValueId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.payload.value");

      Map<DataBlockId, DataBlockDescription> descMap = getDescMap(apeV2TagId, apeV2GenericItemId,
         apeV2GenericItemHeaderId, apeV2GenericItemPayloadId, apeV2GenericItemValueSizeId, apeV2GenericItemFlagsId,
         apeV2GenericItemKeyId, apeV2GenericItemValueId);

      Set<DataBlockId> topLevelIds = new HashSet<>();
      topLevelIds.add(apeV2TagId);

      // Byte orders and charsets
      List<ByteOrder> supportedByteOrders = new ArrayList<>();
      List<Charset> supportedCharsets = new ArrayList<>();

      supportedByteOrders.add(ByteOrder.LITTLE_ENDIAN);

      supportedCharsets.add(Charsets.CHARSET_ISO);

      final Set<DataBlockId> genericDataBlocks = new HashSet<>();

      genericDataBlocks.add(apeV2GenericItemId);
      genericDataBlocks.add(apeV2GenericItemFlagsId);
      genericDataBlocks.add(apeV2GenericItemKeyId);
      genericDataBlocks.add(apeV2GenericItemValueId);
      genericDataBlocks.add(apeV2GenericItemValueSizeId);
      genericDataBlocks.add(apeV2GenericItemHeaderId);
      genericDataBlocks.add(apeV2GenericItemPayloadId);

      DataFormatSpecification dummyAPEv2Spec = new StandardDataFormatSpecification(APEv2, descMap, topLevelIds,
         genericDataBlocks, new HashSet<>(), supportedByteOrders, supportedCharsets, apeV2GenericItemId);

      return dummyAPEv2Spec;
   }

   /**
    * @param apeV2TagId
    * @param apeV2GenericItemId
    * @param apeV2GenericItemHeaderId
    * @param apeV2GenericItemPayloadId
    * @param apeV2GenericItemValueSizeId
    * @param apeV2GenericItemFlagsId
    * @param apeV2GenericItemKeyId
    * @param apeV2GenericItemValueId
    * @return
    */
   public Map<DataBlockId, DataBlockDescription> getDescMap(final DataBlockId apeV2TagId,
      final DataBlockId apeV2GenericItemId, final DataBlockId apeV2GenericItemHeaderId,
      final DataBlockId apeV2GenericItemPayloadId, final DataBlockId apeV2GenericItemValueSizeId,
      final DataBlockId apeV2GenericItemFlagsId, final DataBlockId apeV2GenericItemKeyId,
      final DataBlockId apeV2GenericItemValueId) {
      final DataBlockId apeV2HeaderId = new DataBlockId(APEv2Extension.APEv2, "apev2.header");
      final DataBlockId apeV2PayloadId = new DataBlockId(APEv2Extension.APEv2, "apev2.payload");
      final DataBlockId apeV2FooterId = new DataBlockId(APEv2Extension.APEv2, "apev2.footer");

      Set<DataBlockId> affectedBlocksHeader = new HashSet<>();

      affectedBlocksHeader.add(apeV2PayloadId);
      affectedBlocksHeader.add(apeV2FooterId);
      Set<DataBlockId> affectedBlocksFooter = new HashSet<>();

      affectedBlocksFooter.add(apeV2PayloadId);
      affectedBlocksFooter.add(apeV2HeaderId);

      Set<DataBlockId> itemCountAffectedBlocks = new HashSet<>();

      itemCountAffectedBlocks.add(apeV2GenericItemId);

      List<FlagDescription> itemFlagDescriptions = new ArrayList<>();

      itemFlagDescriptions.add(new FlagDescription("Read-only", new BitAddress(0, 0), "", 1, null));

      itemFlagDescriptions.add(new FlagDescription("Item type", new BitAddress(0, 1), "", 2, null));

      List<FlagDescription> tagFlagDescriptions = new ArrayList<>();

      tagFlagDescriptions.addAll(itemFlagDescriptions);
      tagFlagDescriptions.add(new FlagDescription("This is the header", new BitAddress(3, 5), "", 1, null));
      tagFlagDescriptions.add(new FlagDescription("Tag contains no footer", new BitAddress(3, 6), "", 1, null));
      tagFlagDescriptions.add(new FlagDescription("Tag contains header", new BitAddress(3, 7), "", 1, null));

      FlagSpecification apev2HeaderFlagSpec = new FlagSpecification(tagFlagDescriptions, 4, ByteOrder.LITTLE_ENDIAN,
         new byte[4]);

      Flags defaultTagFlags = new Flags(apev2HeaderFlagSpec);

      Set<DataBlockId> itemValueSizeAffectedBlocks = new HashSet<>();

      itemValueSizeAffectedBlocks.add(apeV2GenericItemPayloadId);

      Set<DataBlockId> itemKeyAffectedBlocks = new HashSet<>();

      itemKeyAffectedBlocks.add(apeV2GenericItemId);

      FlagSpecification apev2ItemFlagSpec = new FlagSpecification(itemFlagDescriptions, 4, ByteOrder.LITTLE_ENDIAN,
         new byte[4]);

      Flags defaultItemFlags = new Flags(apev2ItemFlagSpec);

      ContainerSequenceBuilder<Map<DataBlockId, DataBlockDescription>> builder = new TopLevelContainerSequenceBuilder(
         APEv2Extension.APEv2);
   // @formatter:off
      builder
      .addContainerWithContainerBasedPayload("apev2", "APEv2 Tag", "The APEv2 Tag")
         .withLengthOf(4, DataBlockDescription.UNLIMITED)
         .addHeader("header", "APEv2 header", "The APEv2 header")
            .withStaticLengthOf(APEv2_HEADER_FOOTER_BYTE_LENGTH)
            .addStringField("preample", "APEv2 header preample", "APEv2 header preample")
               .withStaticLengthOf(PREAMPLE_BYTE_LENGTH)
               .withDefaultValue(APE_MAGIC_KEY_STRING)
               .asMagicKey()
            .finishField()
            .addNumericField("versionNumber", "APEv2 header version number", "APEv2 header version number")
               .withStaticLengthOf(4)
               .withDefaultValue(Long.valueOf(0x2000))
            .finishField()
            .addNumericField("tagSize", "APEv2 header tag size", "APEv2 header tag size")
               .withStaticLengthOf(4)
               .withFieldFunction(new FieldFunction(FieldFunctionType.SIZE_OF, affectedBlocksHeader, null, 0))
            .finishField()
            .addNumericField("itemCount", "APEv2 header item count", "APEv2 header item count")
               .withStaticLengthOf(4)
               .withFieldFunction(new FieldFunction(FieldFunctionType.COUNT_OF, itemCountAffectedBlocks, null, 0))
            .finishField()
            .addFlagsField("tagFlags", "APEv2 header tag flags", "APEv2 header tag flags")
               .withStaticLengthOf(4)
               .withFlagSpecification(apev2HeaderFlagSpec)
               .withDefaultValue(defaultTagFlags)
            .finishField()
            .addBinaryField("reserved", "APEv2 header reserved", "APEv2 header reserved")
               .withStaticLengthOf(8)
               .withDefaultValue(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 })
            .finishField()
         .finishHeader()
         .addFooter("footer", "APEv2 footer", "The APEv2 footer")
            .withStaticLengthOf(APEv2_HEADER_FOOTER_BYTE_LENGTH)
            .addStringField("preample", "APEv2 footer preample", "APEv2 footer preample")
               .withStaticLengthOf(PREAMPLE_BYTE_LENGTH)
               .withDefaultValue(APE_MAGIC_KEY_STRING)
               .asMagicKey()
            .finishField()
            .addNumericField("versionNumber", "APEv2 footer version number", "APEv2 footer version number")
               .withStaticLengthOf(4)
               .withDefaultValue(Long.valueOf(0x2000))
            .finishField()
            .addNumericField("tagSize", "APEv2 footer tag size", "APEv2 footer tag size")
               .withStaticLengthOf(4)
               .withFieldFunction(new FieldFunction(FieldFunctionType.SIZE_OF, affectedBlocksFooter, null, 0))
            .finishField()
            .addNumericField("itemCount", "APEv2 footer item count", "APEv2 footer item count")
               .withStaticLengthOf(4)
               .withFieldFunction(new FieldFunction(FieldFunctionType.COUNT_OF, itemCountAffectedBlocks, null, 0))
            .finishField()
            .addFlagsField("tagFlags", "APEv2 footer tag flags", "APEv2 footer tag flags")
               .withStaticLengthOf(4)
               .withFlagSpecification(apev2HeaderFlagSpec)
               .withDefaultValue(defaultTagFlags)
            .finishField()
            .addBinaryField("reserved", "APEv2 footer reserved", "APEv2 footer reserved")
               .withStaticLengthOf(8)
               .withDefaultValue(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 })
            .finishField()
         .finishFooter()
         .getPayload()
            .withDescription("APEv2 payload", "The APEv2 payload")
            .withLengthOf(0, DataBlockDescription.UNLIMITED)
            .addGenericContainerWithFieldBasedPayload("ITEM_ID", "APEv2 item", "The APEv2 item")
               .withLengthOf(1, DataBlockDescription.UNLIMITED)
               .addHeader("header", "APEv2 item header", "The APEv2 item header")
                  .withLengthOf(APEv2_MIN_ITEM_HEADER_LENGTH, DataBlockDescription.UNLIMITED)
                  .addNumericField("size", "APEv2 item value size", "APEv2 item value size")
                     .withStaticLengthOf(4)
                     .withFieldFunction(new FieldFunction(FieldFunctionType.SIZE_OF, itemValueSizeAffectedBlocks, null, 0))
                  .finishField()
                  .addFlagsField("flags", "APEv2 item flags", "APEv2 item flags")
                     .withStaticLengthOf(4)
                     .withFlagSpecification(apev2ItemFlagSpec)
                     .withDefaultValue(defaultItemFlags)
                  .finishField()
                  .addStringField("key", "APEv2 item key", "APEv2 item key")
                     .withLengthOf(2, 255)
                     .withFieldFunction(new FieldFunction(FieldFunctionType.ID_OF, itemKeyAffectedBlocks, null, 0))
                     .withTerminationCharacter('\u0000')
                  .finishField()
               .finishHeader()
               .getPayload()
                  .withDescription("APEv2 item payload", "The APEv2 item payload")
                  .withLengthOf(0, DataBlockDescription.UNLIMITED)
                  .addStringField("value", "APEv2 item value", "APEv2 item value")
                     .withLengthOf(0, DataBlockDescription.UNLIMITED)
                  .finishField()
               .finishFieldBasedPayload()
            .finishContainer()
         .finishContainerBasedPayload()
      .finishContainer();
   // @formatter:on

      return builder.finishContainerSequence();
   }

}
