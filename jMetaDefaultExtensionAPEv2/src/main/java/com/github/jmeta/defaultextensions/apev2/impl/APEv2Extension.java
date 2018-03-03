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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.StandardDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.BinaryValue;
import com.github.jmeta.library.dataformats.api.types.BitAddress;
import com.github.jmeta.library.dataformats.api.types.ChildOrder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.DataFormat;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.library.dataformats.api.types.LocationProperties;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link APEv2Extension}
 *
 */
public class APEv2Extension implements Extension {

   private static final String APE_MAGIC_KEY_STRING = "APETAGX";
   private static final int APEv2_HEADER_FOOTER_BYTE_LENGTH = 32;
   private static final byte[] APEv2_MAGIC_KEY_BYTES = new byte[] { 'A', 'P', 'E', 'T', 'A', 'G', 'E', 'X', -48, 0x07,
      0, 0 };
   private static final int APEv2_MIN_ITEM_HEADER_LENGTH = 9;
   private static final int PREAMPLE_BYTE_LENGTH = 8;
   /**
    *
    */
   public static final DataFormat APEv2 = new DataFormat("APEv2", new HashSet<String>(), new HashSet<String>(),
      new ArrayList<String>(), "", new Date());

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

   private List<DataBlockId> addHeaderOrFooterIds(Map<DataBlockId, DataBlockDescription> descMap,
      DataBlockId headerOrFooterId, DataBlockId otherId, DataBlockId payloadId, DataBlockId itemId,
      List<FlagDescription> itemFlagDescriptions) {

      final DataBlockId apeV2PreampleId = new DataBlockId(APEv2, headerOrFooterId.getGlobalId() + ".preample");
      final DataBlockId apeV2VersionNumberId = new DataBlockId(APEv2,
         headerOrFooterId.getGlobalId() + ".versionNumber");
      final DataBlockId apeV2TagSizeId = new DataBlockId(APEv2, headerOrFooterId.getGlobalId() + ".tagSize");
      final DataBlockId apeV2ItemCountId = new DataBlockId(APEv2, headerOrFooterId.getGlobalId() + ".itemCount");
      final DataBlockId apeV2TagFlagsId = new DataBlockId(APEv2, headerOrFooterId.getGlobalId() + ".tagFlags");
      final DataBlockId apeV2ReservedId = new DataBlockId(APEv2, headerOrFooterId.getGlobalId() + ".reserved");

      List<DataBlockId> returnedList = new ArrayList<>();

      returnedList.add(apeV2PreampleId);
      returnedList.add(apeV2VersionNumberId);
      returnedList.add(apeV2TagSizeId);
      returnedList.add(apeV2ItemCountId);
      returnedList.add(apeV2TagFlagsId);
      returnedList.add(apeV2ReservedId);

      // 1. Header preample
      final Map<DataBlockId, LocationProperties> headerPreampleLocationProps = new HashMap<>();

      headerPreampleLocationProps.put(headerOrFooterId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerPreampleChildIds = new ArrayList<>();

      descMap.put(apeV2PreampleId,
         new DataBlockDescription(apeV2PreampleId, "APEv2 header preample", "APEv2 header preample",
            PhysicalDataBlockType.FIELD, headerPreampleChildIds, ChildOrder.SEQUENTIAL,
            new FieldProperties<>(FieldType.STRING, APE_MAGIC_KEY_STRING, null, null, PREAMPLE_BYTE_LENGTH,
               PREAMPLE_BYTE_LENGTH, null, null, null, null, null, null, null, null),
            headerPreampleLocationProps, PREAMPLE_BYTE_LENGTH, PREAMPLE_BYTE_LENGTH, null, null));

      // 2. Header version number
      final Map<DataBlockId, LocationProperties> headerVersionNumberLocationProps = new HashMap<>();

      headerVersionNumberLocationProps.put(headerOrFooterId,
         new LocationProperties(1, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerVersionNumberChildIds = new ArrayList<>();

      descMap
         .put(apeV2VersionNumberId, new DataBlockDescription(apeV2VersionNumberId, "APEv2 header version number",
            "APEv2 header version number", PhysicalDataBlockType.FIELD, headerVersionNumberChildIds,
            ChildOrder.SEQUENTIAL, new FieldProperties<>(FieldType.UNSIGNED_WHOLE_NUMBER, Long.valueOf(0x2000), null,
               null, 4, 4, null, null, null, null, null, null, null, null),
            headerVersionNumberLocationProps, 4, 4, null, null));

      // 3. Header tag size
      final Map<DataBlockId, LocationProperties> headerTagSizeLocationProps = new HashMap<>();

      headerTagSizeLocationProps.put(headerOrFooterId,
         new LocationProperties(1, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerTagSizeChildIds = new ArrayList<>();

      List<FieldFunction> tagSizeFunctions = new ArrayList<>();

      Set<DataBlockId> affectedBlocks = new HashSet<>();

      affectedBlocks.add(payloadId);
      affectedBlocks.add(otherId);

      tagSizeFunctions.add(new FieldFunction(FieldFunctionType.SIZE_OF, affectedBlocks, null, 0));

      descMap.put(apeV2TagSizeId,
         new DataBlockDescription(
            apeV2TagSizeId, "APEv2 header tag size", "APEv2 header tag size", PhysicalDataBlockType.FIELD,
            headerTagSizeChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<Long>(FieldType.UNSIGNED_WHOLE_NUMBER,
               null, null, null, 4, 4, null, null, null, null, null, null, null, tagSizeFunctions),
            headerTagSizeLocationProps, 4, 4, null, null));

      // 4. Header item count
      final Map<DataBlockId, LocationProperties> headerItemCountLocationProps = new HashMap<>();

      headerItemCountLocationProps.put(headerOrFooterId,
         new LocationProperties(1, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerItemCountChildIds = new ArrayList<>();

      List<FieldFunction> itemCountFunctions = new ArrayList<>();

      Set<DataBlockId> itemCountAffectedBlocks = new HashSet<>();

      itemCountAffectedBlocks.add(itemId);

      itemCountFunctions.add(new FieldFunction(FieldFunctionType.COUNT_OF, itemCountAffectedBlocks, null, 0));

      descMap.put(apeV2ItemCountId,
         new DataBlockDescription(
            apeV2ItemCountId, "APEv2 header item count", "APEv2 header item count", PhysicalDataBlockType.FIELD,
            headerItemCountChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<Long>(FieldType.UNSIGNED_WHOLE_NUMBER,
               null, null, null, 4, 4, null, null, null, null, null, null, null, itemCountFunctions),
            headerItemCountLocationProps, 4, 4, null, null));

      // 5. Tag flags
      final Map<DataBlockId, LocationProperties> headerFlagsLocationProps = new HashMap<>();

      headerFlagsLocationProps.put(headerOrFooterId,
         new LocationProperties(1, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerFlagsChildIds = new ArrayList<>();

      List<FlagDescription> tagFlagDescriptions = new ArrayList<>();

      tagFlagDescriptions.addAll(itemFlagDescriptions);
      tagFlagDescriptions.add(new FlagDescription("This is the header", new BitAddress(3, 5), "", 1, null));
      tagFlagDescriptions.add(new FlagDescription("Tag contains no footer", new BitAddress(3, 6), "", 1, null));
      tagFlagDescriptions.add(new FlagDescription("Tag contains header", new BitAddress(3, 7), "", 1, null));

      FlagSpecification apev2HeaderFlagSpec = new FlagSpecification(tagFlagDescriptions, 4, ByteOrder.LITTLE_ENDIAN,
         new byte[4]);

      Flags defaultTagFlags = new Flags(apev2HeaderFlagSpec);

      List<FieldFunction> tagFlagsFunctions = new ArrayList<>();

      descMap.put(apeV2TagFlagsId,
         new DataBlockDescription(
            apeV2TagFlagsId, "APEv2 header tag flags", "APEv2 header tag flags", PhysicalDataBlockType.FIELD,
            headerFlagsChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<>(FieldType.FLAGS, defaultTagFlags, null,
               null, 4, 4, null, null, null, null, apev2HeaderFlagSpec, null, null, tagFlagsFunctions),
            headerFlagsLocationProps, 4, 4, null, null));

      // 6. Header reserved
      final Map<DataBlockId, LocationProperties> headerReservedLocationProps = new HashMap<>();

      headerReservedLocationProps.put(headerOrFooterId,
         new LocationProperties(1, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerReservedChildIds = new ArrayList<>();

      descMap
         .put(apeV2ReservedId,
            new DataBlockDescription(apeV2ReservedId, "APEv2 header reserved", "APEv2 header reserved",
               PhysicalDataBlockType.FIELD, headerReservedChildIds, ChildOrder.SEQUENTIAL,
               new FieldProperties<>(FieldType.BINARY, new BinaryValue(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 }), null,
                  null, 8, 8, null, null, null, null, null, null, null, null),
               headerReservedLocationProps, 8, 8, null, null));

      return returnedList;
   }

   private DataFormatSpecification createSpecification() {

      // Data blocks
      final DataBlockId apeV2TagId = new DataBlockId(APEv2, "apev2");
      final DataBlockId apeV2HeaderId = new DataBlockId(APEv2, "apev2.header");
      final DataBlockId apeV2PayloadId = new DataBlockId(APEv2, "apev2.payload");
      final DataBlockId apeV2FooterId = new DataBlockId(APEv2, "apev2.footer");

      final DataBlockId apeV2GenericItemId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}");
      final DataBlockId apeV2GenericItemHeaderId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.header");
      final DataBlockId apeV2GenericItemPayloadId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.payload");
      final DataBlockId apeV2GenericItemValueSizeId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.header.size");
      final DataBlockId apeV2GenericItemFlagsId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.header.flags");
      final DataBlockId apeV2GenericItemKeyId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.header.key");
      final DataBlockId apeV2GenericItemValueId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.payload.value");

      Map<DataBlockId, DataBlockDescription> descMap = new HashMap<>();

      // The item (and part of the tag) flags
      List<FlagDescription> itemFlagDescriptions = new ArrayList<>();

      itemFlagDescriptions.add(new FlagDescription("Read-only", new BitAddress(0, 0), "", 1, null));

      itemFlagDescriptions.add(new FlagDescription("Item type", new BitAddress(0, 1), "", 2, null));

      // 1. The APEv2 header
      final Map<DataBlockId, LocationProperties> headerLocationProps = new HashMap<>();

      headerLocationProps.put(apeV2TagId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerChildIds = addHeaderOrFooterIds(descMap, apeV2HeaderId, apeV2FooterId,
         apeV2PayloadId, apeV2GenericItemId, itemFlagDescriptions);

      descMap.put(apeV2HeaderId,
         new DataBlockDescription(apeV2HeaderId, "APEv2 header", "The APEv2 header", PhysicalDataBlockType.HEADER,
            headerChildIds, ChildOrder.SEQUENTIAL, null, headerLocationProps, APEv2_HEADER_FOOTER_BYTE_LENGTH,
            APEv2_HEADER_FOOTER_BYTE_LENGTH, null, null));

      // 2. The APEv2 footer
      final Map<DataBlockId, LocationProperties> footerLocationProps = new HashMap<>();

      footerLocationProps.put(apeV2TagId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> footerChildIds = addHeaderOrFooterIds(descMap, apeV2FooterId, apeV2HeaderId,
         apeV2PayloadId, apeV2GenericItemId, itemFlagDescriptions);

      descMap.put(apeV2FooterId,
         new DataBlockDescription(apeV2FooterId, "APEv2 footer", "The APEv2 footer", PhysicalDataBlockType.FOOTER,
            footerChildIds, ChildOrder.SEQUENTIAL, null, footerLocationProps, APEv2_HEADER_FOOTER_BYTE_LENGTH,
            APEv2_HEADER_FOOTER_BYTE_LENGTH, null, null));

      // 3. Item (value) size
      final Map<DataBlockId, LocationProperties> itemSizeLocationProps = new HashMap<>();

      itemSizeLocationProps.put(apeV2GenericItemHeaderId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> itemSizeChildIds = new ArrayList<>();

      List<FieldFunction> itemValueSizeFunctions = new ArrayList<>();

      Set<DataBlockId> itemValueSizeAffectedBlocks = new HashSet<>();

      itemValueSizeAffectedBlocks.add(apeV2GenericItemPayloadId);

      itemValueSizeFunctions.add(new FieldFunction(FieldFunctionType.SIZE_OF, itemValueSizeAffectedBlocks, null, 0));

      descMap.put(apeV2GenericItemValueSizeId,
         new DataBlockDescription(
            apeV2GenericItemValueSizeId, "APEv2 item value size", "APEv2 item value size", PhysicalDataBlockType.FIELD,
            itemSizeChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<Long>(FieldType.UNSIGNED_WHOLE_NUMBER, null,
               null, null, 4, 4, null, null, null, null, null, null, null, itemValueSizeFunctions),
            itemSizeLocationProps, 4, 4, null, null));

      // 4. Item flags
      final Map<DataBlockId, LocationProperties> itemFlagsLocationProps = new HashMap<>();

      itemFlagsLocationProps.put(apeV2GenericItemHeaderId,
         new LocationProperties(1, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> itemFlagsChildIds = new ArrayList<>();

      List<FieldFunction> itemFlagsFunctions = new ArrayList<>();

      FlagSpecification apev2ItemFlagSpec = new FlagSpecification(itemFlagDescriptions, 4, ByteOrder.LITTLE_ENDIAN,
         new byte[4]);

      Flags defaultItemFlags = new Flags(apev2ItemFlagSpec);

      descMap.put(apeV2GenericItemFlagsId,
         new DataBlockDescription(
            apeV2GenericItemFlagsId, "APEv2 item flags", "APEv2 item flags", PhysicalDataBlockType.FIELD,
            itemFlagsChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<>(FieldType.FLAGS, defaultItemFlags, null,
               null, 4, 4, null, null, null, null, apev2ItemFlagSpec, null, null, itemFlagsFunctions),
            itemFlagsLocationProps, 4, 4, null, null));

      // 5. Item key
      final Map<DataBlockId, LocationProperties> itemKeyLocationProps = new HashMap<>();

      final LocationProperties itemKeyLocProps = new LocationProperties(8, 1, 1, DataBlockDescription.UNKNOWN_SIZE,
         new ArrayList<>(), new ArrayList<>());
      itemKeyLocationProps.put(apeV2GenericItemHeaderId, itemKeyLocProps);
      itemKeyLocationProps.put(apeV2GenericItemId, itemKeyLocProps);

      final List<DataBlockId> itemKeyChildIds = new ArrayList<>();

      List<FieldFunction> itemKeyFunctions = new ArrayList<>();

      Set<DataBlockId> itemKeyAffectedBlocks = new HashSet<>();

      itemKeyAffectedBlocks.add(apeV2GenericItemId);

      itemKeyFunctions.add(new FieldFunction(FieldFunctionType.ID_OF, itemKeyAffectedBlocks, null, 0));

      descMap.put(apeV2GenericItemKeyId,
         new DataBlockDescription(
            apeV2GenericItemKeyId, "APEv2 item key", "APEv2 item key", PhysicalDataBlockType.FIELD, itemKeyChildIds,
            ChildOrder.SEQUENTIAL, new FieldProperties<String>(FieldType.STRING, null, null, /* new byte[] {0} */null,
               2, 255, '\u0000', null, null, null, null, null, null, itemKeyFunctions),
            itemKeyLocationProps, 2, 255, null, null));

      // 6. Item value
      final Map<DataBlockId, LocationProperties> itemValueLocationProps = new HashMap<>();

      itemValueLocationProps.put(apeV2GenericItemPayloadId,
         new LocationProperties(1, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> itemValueChildIds = new ArrayList<>();

      descMap.put(apeV2GenericItemValueId,
         new DataBlockDescription(apeV2GenericItemValueId, "APEv2 item value", "APEv2 item value",
            PhysicalDataBlockType.FIELD, itemValueChildIds, ChildOrder.SEQUENTIAL,
            new FieldProperties<String>(FieldType.STRING, null, null, null, DataBlockDescription.UNKNOWN_SIZE,
               DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null, null, null),
            itemValueLocationProps, 0, DataBlockDescription.UNKNOWN_SIZE, null, null));

      // 7. APEv2 item header
      final Map<DataBlockId, LocationProperties> itemHeaderLocationProps = new HashMap<>();

      itemHeaderLocationProps.put(apeV2GenericItemId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> itemHeaderChildIds = new ArrayList<>();
      itemHeaderChildIds.add(apeV2GenericItemValueSizeId);
      itemHeaderChildIds.add(apeV2GenericItemFlagsId);
      itemHeaderChildIds.add(apeV2GenericItemKeyId);

      descMap.put(apeV2GenericItemHeaderId,
         new DataBlockDescription(apeV2GenericItemHeaderId, "APEv2 item header", "The APEv2 item header",
            PhysicalDataBlockType.HEADER, itemHeaderChildIds, ChildOrder.SEQUENTIAL, null, itemHeaderLocationProps,
            APEv2_MIN_ITEM_HEADER_LENGTH, DataBlockDescription.UNKNOWN_SIZE, null, null));

      // 8. APEv2 item payload
      final Map<DataBlockId, LocationProperties> itemPayloadLocationProps = new HashMap<>();

      itemPayloadLocationProps.put(apeV2GenericItemId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> itemPayloadChildIds = new ArrayList<>();
      itemPayloadChildIds.add(apeV2GenericItemValueId);

      descMap.put(apeV2GenericItemPayloadId,
         new DataBlockDescription(apeV2GenericItemPayloadId, "APEv2 item payload", "The APEv2 item payload",
            PhysicalDataBlockType.FIELD_BASED_PAYLOAD, itemPayloadChildIds, ChildOrder.SEQUENTIAL, null,
            itemPayloadLocationProps, 0, DataBlockDescription.UNKNOWN_SIZE, null, null));

      // 9. APEv2 item
      final List<DataBlockId> itemChildIds = new ArrayList<>();
      itemChildIds.add(apeV2GenericItemHeaderId);
      itemChildIds.add(apeV2GenericItemPayloadId);

      final Map<DataBlockId, LocationProperties> itemLocationProps = new HashMap<>();
      itemLocationProps.put(apeV2PayloadId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      // Magic Keys
      MagicKey genericItemMagicKey = new MagicKey(new byte[] { 0 }, Byte.SIZE, apeV2GenericItemId, 0);

      List<MagicKey> apev2ItemMagicKeys = new ArrayList<>();
      apev2ItemMagicKeys.add(genericItemMagicKey);

      descMap.put(apeV2GenericItemId,
         new DataBlockDescription(apeV2GenericItemId, "APEv2 item", "The APEv2 item", PhysicalDataBlockType.CONTAINER,
            itemChildIds, ChildOrder.SEQUENTIAL, null, itemLocationProps, 1, DataBlockDescription.UNKNOWN_SIZE,
            apev2ItemMagicKeys, null));

      // 10. APEv2 payload
      final List<DataBlockId> payloadChildIds = new ArrayList<>();

      payloadChildIds.add(apeV2GenericItemId);

      final Map<DataBlockId, LocationProperties> payloadLocationProps = new HashMap<>();
      payloadLocationProps.put(apeV2TagId,
         new LocationProperties(4, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(apeV2PayloadId,
         new DataBlockDescription(apeV2PayloadId, "APEv2 payload", "The APEv2 payload",
            PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD, payloadChildIds, ChildOrder.SEQUENTIAL, null,
            payloadLocationProps, 0, DataBlockDescription.UNKNOWN_SIZE, null, null));

      // 11. APEv2 tag

      final List<DataBlockId> tagChildIds = new ArrayList<>();
      tagChildIds.add(apeV2HeaderId);
      tagChildIds.add(apeV2PayloadId);
      tagChildIds.add(apeV2FooterId);

      final Map<DataBlockId, LocationProperties> tagLocationProps = new HashMap<>();
      tagLocationProps.put(null, new LocationProperties(DataBlockDescription.UNKNOWN_SIZE, 1, 1,
         DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      // Magic Keys
      MagicKey apev2FooterMagicKey = new MagicKey(APEv2_MAGIC_KEY_BYTES, APEv2_MAGIC_KEY_BYTES.length * Byte.SIZE, "",
         apeV2FooterId, -APEv2_HEADER_FOOTER_BYTE_LENGTH, 0);
      MagicKey apev2HeaderMagicKey = new MagicKey(APEv2_MAGIC_KEY_BYTES, APEv2_MAGIC_KEY_BYTES.length * Byte.SIZE, "",
         apeV2HeaderId, MagicKey.NO_BACKWARD_READING, 0);

      List<MagicKey> apev2TagMagicKeys = new ArrayList<>();
      apev2TagMagicKeys.add(apev2HeaderMagicKey);
      apev2TagMagicKeys.add(apev2FooterMagicKey);

      descMap.put(apeV2TagId,
         new DataBlockDescription(apeV2TagId, "APEv2 Tag", "The APEv2 Tag", PhysicalDataBlockType.CONTAINER,
            tagChildIds, ChildOrder.SEQUENTIAL, null, tagLocationProps, 4, DataBlockDescription.UNKNOWN_SIZE,
            apev2TagMagicKeys, null));

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
         genericDataBlocks, new HashSet<>(), supportedByteOrders, supportedCharsets,
         new ArrayList<DataTransformationType>());

      return dummyAPEv2Spec;
   }

}
