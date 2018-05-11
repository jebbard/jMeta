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
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link Lyrics3v2Extension}
 *
 */
public class Lyrics3v2Extension implements Extension {

   private static final String LYRICS3v2_MAGIC_FOOTER_STRING = "LYRICS200";
   private static final String LYRICS3v2_MAGIC_HEADER_STRING = "LYRICSBEGIN";
   public static final int FOOTER_SIZE_FIELD_LENGTH = 6;
   private static final int FOOTER_BYTE_LENGTH = LYRICS3v2_MAGIC_FOOTER_STRING.length() + FOOTER_SIZE_FIELD_LENGTH;
   private static final int HEADER_BYTE_LENGTH = LYRICS3v2_MAGIC_HEADER_STRING.length();
   private static final int LYRICS3v2_FIELD_ID_SIZE = 3;
   private static final int LYRICS3v2_FIELD_SIZE_LENGTH = 5;

   /**
    *
    */
   public static final ContainerDataFormat LYRICS3v2 = new ContainerDataFormat("Lyrics3v2", new HashSet<String>(),
      new HashSet<String>(), new ArrayList<String>(), "", new Date());
   public static final DataBlockId lyrics3V2TagId = new DataBlockId(LYRICS3v2, "lyrics3v2");

   private static final DataBlockId lyrics3V2FooterId = new DataBlockId(LYRICS3v2, "lyrics3v2.footer");

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionId()
    */
   @Override
   public String getExtensionId() {
      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.Lyrics3v2DataBlocksExtension";
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionDescription()
    */
   @Override
   public ExtensionDescription getExtensionDescription() {
      return new ExtensionDescription("Lyrics3v2", "jMeta", "1.0", null, "Lyrics3v2 extension", null, null);
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

   private DataFormatSpecification createSpecification() {

      // Data blocks
      final DataBlockId lyrics3V2HeaderId = new DataBlockId(LYRICS3v2, "lyrics3v2.header");
      final DataBlockId lyrics3V2PayloadId = new DataBlockId(LYRICS3v2, "lyrics3v2.payload");

      final DataBlockId lyrics3V2HeaderMagicKeyId = new DataBlockId(LYRICS3v2, "lyrics3v2.header.id");
      final DataBlockId lyrics3V2FooterMagicKeyId = new DataBlockId(LYRICS3v2, "lyrics3v2.footer.id");
      final DataBlockId lyrics3V2FooterSizeId = new DataBlockId(LYRICS3v2, "lyrics3v2.footer.size");

      final DataBlockId lyrics3V2GenericFieldId = new DataBlockId(LYRICS3v2, "lyrics3v2.payload.${FIELD_ID}");
      final DataBlockId lyrics3V2GenericFieldHeaderId = new DataBlockId(LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.header");
      final DataBlockId lyrics3V2GenericFieldPayloadId = new DataBlockId(LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.payload");
      final DataBlockId lyrics3V2GenericFieldHeaderSizeId = new DataBlockId(LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.header.size");
      final DataBlockId lyrics3V2GenericFieldHeaderIdId = new DataBlockId(LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.header.id");
      final DataBlockId lyrics3V2GenericFieldPayloadDataId = new DataBlockId(LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.payload.value");

      Map<DataBlockId, DataBlockDescription> descMap = new HashMap<>();

      // 1. Header id
      final List<DataBlockId> headerIdChildIds = new ArrayList<>();

      descMap.put(lyrics3V2HeaderMagicKeyId,
         new DataBlockDescription(lyrics3V2HeaderMagicKeyId, "Lyrics3v2 header id", "Lyrics3v2 header id",
            PhysicalDataBlockType.FIELD, headerIdChildIds, new FieldProperties<>(FieldType.STRING,
               LYRICS3v2_MAGIC_HEADER_STRING, null, null, null, null, null, null, true),
            0, 1, 1, HEADER_BYTE_LENGTH, HEADER_BYTE_LENGTH, null));

      // 2. The Lyrics3v2 header
      final List<DataBlockId> headerChildIds = new ArrayList<>();

      headerChildIds.add(lyrics3V2HeaderMagicKeyId);

      descMap.put(lyrics3V2HeaderId,
         new DataBlockDescription(lyrics3V2HeaderId, "Lyrics3v2 header", "The Lyrics3v2 header",
            PhysicalDataBlockType.HEADER, headerChildIds, null, 0, 1, 1, HEADER_BYTE_LENGTH, HEADER_BYTE_LENGTH, null));

      // 3. Footer tag size
      final List<DataBlockId> footerTagSizeChildIds = new ArrayList<>();

      List<FieldFunction> tagSizeFunctions = new ArrayList<>();

      Set<DataBlockId> affectedBlocks = new HashSet<>();

      affectedBlocks.add(lyrics3V2PayloadId);
      affectedBlocks.add(lyrics3V2HeaderId);

      tagSizeFunctions.add(new FieldFunction(FieldFunctionType.SIZE_OF, affectedBlocks, null, 0));

      descMap.put(lyrics3V2FooterSizeId,
         new DataBlockDescription(lyrics3V2FooterSizeId, "Lyrics3v2 footer tag size", "Lyrics3v2 footer tag size",
            PhysicalDataBlockType.FIELD, footerTagSizeChildIds,
            new FieldProperties<Long>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null, null, null, null,
               tagSizeFunctions, false),
            0, 1, 1, FOOTER_SIZE_FIELD_LENGTH, FOOTER_SIZE_FIELD_LENGTH, null));

      // 4. Footer id
      final List<DataBlockId> footerIdChildIds = new ArrayList<>();

      descMap.put(lyrics3V2FooterMagicKeyId,
         new DataBlockDescription(lyrics3V2FooterMagicKeyId, "Lyrics3v2 footer id", "Lyrics3v2 footer id",
            PhysicalDataBlockType.FIELD, footerIdChildIds,
            new FieldProperties<>(FieldType.STRING, LYRICS3v2_MAGIC_FOOTER_STRING, null, null, null, null, null, null,
               true),
            FOOTER_SIZE_FIELD_LENGTH, 1, 1, LYRICS3v2_MAGIC_FOOTER_STRING.length(),
            LYRICS3v2_MAGIC_FOOTER_STRING.length(), null));

      // 5. The Lyrics3v2 footer
      final List<DataBlockId> footerChildIds = new ArrayList<>();

      footerChildIds.add(lyrics3V2FooterSizeId);
      footerChildIds.add(lyrics3V2FooterMagicKeyId);

      descMap.put(lyrics3V2FooterId,
         new DataBlockDescription(lyrics3V2FooterId, "Lyrics3v2 footer", "The Lyrics3v2 footer",
            PhysicalDataBlockType.FOOTER, footerChildIds, null, 0, 1, 1, FOOTER_BYTE_LENGTH, FOOTER_BYTE_LENGTH, null));

      // 5. Field id
      final List<DataBlockId> fieldIdChildIds = new ArrayList<>();

      List<FieldFunction> fieldIdFunctions = new ArrayList<>();

      Set<DataBlockId> fieldIdAffectedBlocks = new HashSet<>();

      fieldIdAffectedBlocks.add(lyrics3V2GenericFieldId);

      fieldIdFunctions.add(new FieldFunction(FieldFunctionType.ID_OF, fieldIdAffectedBlocks, null, 0));

      descMap.put(lyrics3V2GenericFieldHeaderIdId,
         new DataBlockDescription(lyrics3V2GenericFieldHeaderIdId, "Lyrics3v2 field id", "Lyrics3v2 field id",
            PhysicalDataBlockType.FIELD, fieldIdChildIds,
            new FieldProperties<>(FieldType.STRING, null, null, null, null, null, null, fieldIdFunctions, false), 0, 1,
            1, LYRICS3v2_FIELD_ID_SIZE, LYRICS3v2_FIELD_ID_SIZE, null));

      // 6. Field size
      final List<DataBlockId> fieldSizeChildIds = new ArrayList<>();

      List<FieldFunction> fieldSizeFunctions = new ArrayList<>();

      Set<DataBlockId> fieldSizeAffectedBlocks = new HashSet<>();

      fieldSizeAffectedBlocks.add(lyrics3V2GenericFieldPayloadId);

      fieldSizeFunctions.add(new FieldFunction(FieldFunctionType.SIZE_OF, fieldSizeAffectedBlocks, null, 0));

      descMap.put(lyrics3V2GenericFieldHeaderSizeId,
         new DataBlockDescription(lyrics3V2GenericFieldHeaderSizeId, "Lyrics3v2 item value size",
            "Lyrics3v2 item value size", PhysicalDataBlockType.FIELD, fieldSizeChildIds,
            new FieldProperties<Long>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null, null, null, null,
               fieldSizeFunctions, false),
            LYRICS3v2_FIELD_ID_SIZE, 1, 1, LYRICS3v2_FIELD_SIZE_LENGTH, LYRICS3v2_FIELD_SIZE_LENGTH, null));

      // 7. Field header
      final List<DataBlockId> fieldHeaderChildIds = new ArrayList<>();
      fieldHeaderChildIds.add(lyrics3V2GenericFieldHeaderIdId);
      fieldHeaderChildIds.add(lyrics3V2GenericFieldHeaderSizeId);

      descMap.put(lyrics3V2GenericFieldHeaderId,
         new DataBlockDescription(lyrics3V2GenericFieldHeaderId, "Lyrics3v2 field header", "The Lyrics3v2 field header",
            PhysicalDataBlockType.HEADER, fieldHeaderChildIds, null, 0, 1, 1,
            LYRICS3v2_FIELD_SIZE_LENGTH + LYRICS3v2_FIELD_ID_SIZE,
            LYRICS3v2_FIELD_SIZE_LENGTH + LYRICS3v2_FIELD_ID_SIZE, null));

      // 8. Field data
      final List<DataBlockId> fieldDataChildIds = new ArrayList<>();

      descMap.put(lyrics3V2GenericFieldPayloadDataId,
         new DataBlockDescription(lyrics3V2GenericFieldPayloadDataId, "Lyrics3v2 field data", "Lyrics3v2 field data",
            PhysicalDataBlockType.FIELD, fieldDataChildIds,
            new FieldProperties<>(FieldType.STRING, null, null, null, null, null, null, null, false),
            LYRICS3v2_FIELD_SIZE_LENGTH + LYRICS3v2_FIELD_ID_SIZE, 1, 1, 0, DataBlockDescription.UNKNOWN_SIZE, null));

      // 9. Lyrics3v2 field payload
      final List<DataBlockId> fieldPayloadChildIds = new ArrayList<>();
      fieldPayloadChildIds.add(lyrics3V2GenericFieldPayloadDataId);

      descMap.put(lyrics3V2GenericFieldPayloadId,
         new DataBlockDescription(lyrics3V2GenericFieldPayloadId, "Lyrics3v2 field payload",
            "The Lyrics3v2 field payload", PhysicalDataBlockType.FIELD_BASED_PAYLOAD, fieldPayloadChildIds, null, 0, 1,
            1, 0, DataBlockDescription.UNKNOWN_SIZE, null));

      // 10. Lyrics3v2 field
      final List<DataBlockId> fieldChildIds = new ArrayList<>();
      fieldChildIds.add(lyrics3V2GenericFieldHeaderId);
      fieldChildIds.add(lyrics3V2GenericFieldPayloadId);

      descMap.put(lyrics3V2GenericFieldId,
         new DataBlockDescription(lyrics3V2GenericFieldId, "Lyrics3v2 field", "The Lyrics3v2 field",
            PhysicalDataBlockType.CONTAINER, fieldChildIds, null, 0, 1, 1, 1, DataBlockDescription.UNKNOWN_SIZE, null));

      // 11. Lyrics3v2 payload
      final List<DataBlockId> payloadChildIds = new ArrayList<>();

      payloadChildIds.add(lyrics3V2GenericFieldId);

      descMap.put(lyrics3V2PayloadId,
         new DataBlockDescription(lyrics3V2PayloadId, "Lyrics3v2 payload", "The Lyrics3v2 payload",
            PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD, payloadChildIds, null, HEADER_BYTE_LENGTH, 1, 1, 0,
            DataBlockDescription.UNKNOWN_SIZE, null));

      // 12. Lyrics3v2 tag
      final List<DataBlockId> tagChildIds = new ArrayList<>();
      tagChildIds.add(lyrics3V2HeaderId);
      tagChildIds.add(lyrics3V2PayloadId);
      tagChildIds.add(lyrics3V2FooterId);

      descMap.put(lyrics3V2TagId,
         new DataBlockDescription(lyrics3V2TagId, "Lyrics3v2 Tag", "The Lyrics3v2 Tag", PhysicalDataBlockType.CONTAINER,
            tagChildIds, null, DataBlockDescription.UNKNOWN_SIZE, 1, 1, 4, DataBlockDescription.UNKNOWN_SIZE, null));

      Set<DataBlockId> topLevelIds = new HashSet<>();
      topLevelIds.add(lyrics3V2TagId);

      // Byte orders and charsets
      List<ByteOrder> supportedByteOrders = new ArrayList<>();
      List<Charset> supportedCharsets = new ArrayList<>();

      supportedByteOrders.add(ByteOrder.LITTLE_ENDIAN);

      supportedCharsets.add(Charsets.CHARSET_ISO);

      final Set<DataBlockId> genericDataBlocks = new HashSet<>();

      genericDataBlocks.add(lyrics3V2GenericFieldId);
      genericDataBlocks.add(lyrics3V2GenericFieldHeaderIdId);
      genericDataBlocks.add(lyrics3V2GenericFieldPayloadDataId);
      genericDataBlocks.add(lyrics3V2GenericFieldHeaderSizeId);
      genericDataBlocks.add(lyrics3V2GenericFieldHeaderId);
      genericDataBlocks.add(lyrics3V2GenericFieldPayloadId);

      DataFormatSpecification dummyLyrics3v2Spec = new StandardDataFormatSpecification(LYRICS3v2, descMap, topLevelIds,
         genericDataBlocks, new HashSet<>(), supportedByteOrders, supportedCharsets, lyrics3V2GenericFieldId);

      return dummyLyrics3v2Spec;
   }

}
