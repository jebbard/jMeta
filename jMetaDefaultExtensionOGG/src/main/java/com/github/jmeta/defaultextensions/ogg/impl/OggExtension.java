/**
 *
 * {@link APEv2Extension}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.defaultextensions.ogg.impl;

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
import com.github.jmeta.library.dataformats.api.types.ChildOrder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.DataFormat;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.LocationProperties;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link OggExtension}
 *
 */
public class OggExtension implements Extension {

   private static final byte[] OGG_MAGIC_KEY_BYTES = new byte[] { 'O', 'g', 'g', 'S' };
   private static final String OGG_MAGIC_KEY_STRING = "OggS";
   /**
    *
    */
   public static final DataFormat OGG = new DataFormat("Ogg", new HashSet<String>(), new HashSet<String>(),
      new ArrayList<String>(), "", new Date());

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionId()
    */
   @Override
   public String getExtensionId() {
      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.OggDataBlocksExtension";
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionDescription()
    */
   @Override
   public ExtensionDescription getExtensionDescription() {
      return null;
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
         serviceProviders.add((T) new OggDataBlocksService());
      }
      return serviceProviders;
   }

   private DataFormatSpecification createSpecification() {

      // Data blocks
      final DataBlockId oggPageId = new DataBlockId(OGG, "ogg");
      final DataBlockId oggPageHeaderId = new DataBlockId(OGG, "ogg.header");
      final DataBlockId oggPayloadId = new DataBlockId(OGG, "ogg.payload");

      final DataBlockId oggPageHeaderCaptureId = new DataBlockId(OGG, "ogg.header.capturePattern");
      final DataBlockId oggPageHeaderStreamStructVersionId = new DataBlockId(OGG, "ogg.header.streamStructureVersion");
      final DataBlockId oggPageHeaderHeaderTypeFlagId = new DataBlockId(OGG, "ogg.header.headerTypeFlag");
      final DataBlockId oggPageHeaderAbsoluteGranulePosId = new DataBlockId(OGG, "ogg.header.absoluteGranulePos");
      final DataBlockId oggPageHeaderStreamSerialNoId = new DataBlockId(OGG, "ogg.header.streamSerialNumber");
      final DataBlockId oggPageHeaderPageSequNoId = new DataBlockId(OGG, "ogg.header.pageSequenceNumber");
      final DataBlockId oggPageHeaderPageChecksumId = new DataBlockId(OGG, "ogg.header.pageChecksum");
      final DataBlockId oggPageHeaderSegmentsId = new DataBlockId(OGG, "ogg.header.pageSegments");
      final DataBlockId oggPageHeaderSegmentTableEntryId = new DataBlockId(OGG, "ogg.header.segmentTableEntry");

      final DataBlockId oggPacketPartContainerId = new DataBlockId(OGG, "ogg.payload.packetPartContainer");
      final DataBlockId oggPacketPartId = new DataBlockId(OGG, "ogg.payload.packetPartContainer.payload");
      final DataBlockId oggSegmentId = new DataBlockId(OGG, "ogg.payload.packetPartContainer.payload.segment");

      Map<DataBlockId, DataBlockDescription> descMap = new HashMap<>();

      // 1. Ogg capture pattern
      final Map<DataBlockId, LocationProperties> capturePatternLocationProps = new HashMap<>();
      capturePatternLocationProps.put(oggPageHeaderId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> capturePatternChildIds = new ArrayList<>();

      descMap.put(oggPageHeaderCaptureId, new DataBlockDescription(oggPageHeaderCaptureId,
         "Ogg page header capture pattern", "Ogg page header capture pattern", PhysicalDataBlockType.FIELD,
         capturePatternChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<>(FieldType.STRING, OGG_MAGIC_KEY_STRING,
            null, null, 4, 4, null, null, null, null, null, null, null, null),
         capturePatternLocationProps, 4, 4, null, null));

      // 2. Ogg stream structure version
      final Map<DataBlockId, LocationProperties> streamStructVersionLocationProps = new HashMap<>();
      streamStructVersionLocationProps.put(oggPageHeaderId,
         new LocationProperties(4, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> streamStructVersionChildIds = new ArrayList<>();

      descMap.put(oggPageHeaderStreamStructVersionId,
         new DataBlockDescription(oggPageHeaderStreamStructVersionId, "Ogg page header stream structure version",
            "Ogg page header structure version", PhysicalDataBlockType.FIELD, streamStructVersionChildIds,
            ChildOrder.SEQUENTIAL, new FieldProperties<>(FieldType.BINARY, new BinaryValue(new byte[] { 0 }), null,
               null, 1, 1, null, null, null, null, null, null, null, null),
            streamStructVersionLocationProps, 1, 1, null, null));

      // 3. Ogg header type flag
      // TODO doItFirst002: define flags
      final Map<DataBlockId, LocationProperties> headerTypeFlagLocationProps = new HashMap<>();
      headerTypeFlagLocationProps.put(oggPageHeaderId,
         new LocationProperties(5, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerTypeFlagChildIds = new ArrayList<>();

      descMap
         .put(oggPageHeaderHeaderTypeFlagId, new DataBlockDescription(oggPageHeaderHeaderTypeFlagId,
            "Ogg page header type flag", "Ogg page header type flag", PhysicalDataBlockType.FIELD,
            headerTypeFlagChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<BinaryValue>(FieldType.BINARY, null,
               null, null, 1, 1, null, null, null, null, null, null, null, null),
            headerTypeFlagLocationProps, 1, 1, null, null));

      // 4. Ogg absolute granule position
      final Map<DataBlockId, LocationProperties> absGranulePosLocationProps = new HashMap<>();
      absGranulePosLocationProps.put(oggPageHeaderId,
         new LocationProperties(6, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> absGranulePosChildIds = new ArrayList<>();

      descMap.put(oggPageHeaderAbsoluteGranulePosId, new DataBlockDescription(oggPageHeaderAbsoluteGranulePosId,
         "Ogg page absolute granule position", "Ogg page absolute granule position", PhysicalDataBlockType.FIELD,
         absGranulePosChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<Long>(FieldType.UNSIGNED_WHOLE_NUMBER, null,
            null, null, 8, 8, null, null, null, null, null, null, null, null),
         absGranulePosLocationProps, 8, 8, null, null));

      // 5. Ogg stream serial number
      final Map<DataBlockId, LocationProperties> streamSerialNoLocationProps = new HashMap<>();
      streamSerialNoLocationProps.put(oggPageHeaderId,
         new LocationProperties(14, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> streamSerialNoChildIds = new ArrayList<>();

      descMap.put(oggPageHeaderStreamSerialNoId, new DataBlockDescription(oggPageHeaderStreamSerialNoId,
         "Ogg page stream serial number", "Ogg page stream serial number", PhysicalDataBlockType.FIELD,
         streamSerialNoChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<Long>(FieldType.UNSIGNED_WHOLE_NUMBER, null,
            null, null, 4, 4, null, null, null, null, null, null, null, null),
         streamSerialNoLocationProps, 4, 4, null, null));

      // 6. Ogg page sequence number
      final Map<DataBlockId, LocationProperties> pageSequNoLocationProps = new HashMap<>();
      pageSequNoLocationProps.put(oggPageHeaderId,
         new LocationProperties(18, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> pageSequNoChildIds = new ArrayList<>();

      descMap.put(oggPageHeaderPageSequNoId, new DataBlockDescription(
         oggPageHeaderPageSequNoId, "Ogg page sequence number", "Ogg page sequence number", PhysicalDataBlockType.FIELD,
         pageSequNoChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<Long>(FieldType.UNSIGNED_WHOLE_NUMBER, null,
            null, null, 4, 4, null, null, null, null, null, null, null, null),
         pageSequNoLocationProps, 4, 4, null, null));

      // 7. Ogg page checksum
      final Map<DataBlockId, LocationProperties> pageChecksumLocationProps = new HashMap<>();
      pageChecksumLocationProps.put(oggPageHeaderId,
         new LocationProperties(22, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> pageChecksumChildIds = new ArrayList<>();

      descMap.put(oggPageHeaderPageChecksumId,
         new DataBlockDescription(
            oggPageHeaderPageChecksumId, "Ogg page checksum", "Ogg page checksum", PhysicalDataBlockType.FIELD,
            pageChecksumChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<BinaryValue>(FieldType.BINARY, null, null,
               null, 4, 4, null, null, null, null, null, null, null, null),
            pageChecksumLocationProps, 4, 4, null, null));

      // 8. Ogg page segments
      final Map<DataBlockId, LocationProperties> pageSegmentsLocationProps = new HashMap<>();
      pageSegmentsLocationProps.put(oggPageHeaderId,
         new LocationProperties(26, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      List<FieldFunction> pageSegmentsFunctions = new ArrayList<>();

      Set<DataBlockId> affectedBlocks = new HashSet<>();

      affectedBlocks.add(oggPageHeaderSegmentTableEntryId);

      pageSegmentsFunctions.add(new FieldFunction(FieldFunctionType.COUNT_OF, affectedBlocks, null, 0));

      final List<DataBlockId> pageSegmentsChildIds = new ArrayList<>();

      descMap.put(oggPageHeaderSegmentsId,
         new DataBlockDescription(
            oggPageHeaderSegmentsId, "Ogg page segments", "Ogg page segments", PhysicalDataBlockType.FIELD,
            pageSegmentsChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<Long>(FieldType.UNSIGNED_WHOLE_NUMBER,
               null, null, null, 1, 1, null, null, null, null, null, null, null, pageSegmentsFunctions),
            pageSegmentsLocationProps, 1, 1, null, null));

      // 9. Ogg page segment table entry
      final Map<DataBlockId, LocationProperties> segmentTableLocationProps = new HashMap<>();
      segmentTableLocationProps.put(oggPageHeaderId,
         new LocationProperties(26, 0, 99999, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      List<FieldFunction> segmentTableEntryFunctions = new ArrayList<>();

      Set<DataBlockId> affectedBlocks2 = new HashSet<>();

      affectedBlocks2.add(oggSegmentId);

      segmentTableEntryFunctions.add(new FieldFunction(FieldFunctionType.SIZE_OF, affectedBlocks2, null, 0));

      final List<DataBlockId> segmentTableChildIds = new ArrayList<>();

      descMap.put(oggPageHeaderSegmentTableEntryId, new DataBlockDescription(oggPageHeaderSegmentTableEntryId,
         "Ogg page segment table entry", "Ogg segment table entry", PhysicalDataBlockType.FIELD, segmentTableChildIds,
         ChildOrder.SEQUENTIAL, new FieldProperties<Long>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null, 1, 1, null,
            null, null, null, null, null, null, segmentTableEntryFunctions),
         segmentTableLocationProps, 1, 1, null, null));

      // 10. The Ogg page header
      final Map<DataBlockId, LocationProperties> pageHeaderLocationProps = new HashMap<>();

      pageHeaderLocationProps.put(oggPageId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> pageHeaderChildIds = new ArrayList<>();

      pageHeaderChildIds.add(oggPageHeaderCaptureId);
      pageHeaderChildIds.add(oggPageHeaderStreamStructVersionId);
      pageHeaderChildIds.add(oggPageHeaderHeaderTypeFlagId);
      pageHeaderChildIds.add(oggPageHeaderAbsoluteGranulePosId);
      pageHeaderChildIds.add(oggPageHeaderStreamSerialNoId);
      pageHeaderChildIds.add(oggPageHeaderPageSequNoId);
      pageHeaderChildIds.add(oggPageHeaderPageChecksumId);
      pageHeaderChildIds.add(oggPageHeaderSegmentsId);
      pageHeaderChildIds.add(oggPageHeaderSegmentTableEntryId);

      descMap.put(oggPageHeaderId,
         new DataBlockDescription(oggPageHeaderId, "Ogg page header", "Ogg page header", PhysicalDataBlockType.HEADER,
            pageHeaderChildIds, ChildOrder.SEQUENTIAL, null, pageHeaderLocationProps, DataBlockDescription.UNKNOWN_SIZE,
            DataBlockDescription.UNKNOWN_SIZE, null, null));

      // 11a. Ogg packet container
      final Map<DataBlockId, LocationProperties> packetContainerLocationProps = new HashMap<>();
      packetContainerLocationProps.put(oggPayloadId,
         new LocationProperties(0, 1, 999999, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> packetContainerChildIds = new ArrayList<>();
      packetContainerChildIds.add(oggPacketPartId);

      descMap.put(oggPacketPartContainerId,
         new DataBlockDescription(oggPacketPartContainerId, "Ogg packet", "Ogg packet", PhysicalDataBlockType.CONTAINER,
            packetContainerChildIds, ChildOrder.SEQUENTIAL, null, packetContainerLocationProps, 1,
            DataBlockDescription.UNKNOWN_SIZE, null, null));

      // 11b. Ogg packet
      final Map<DataBlockId, LocationProperties> packetLocationProps = new HashMap<>();
      packetLocationProps.put(oggPacketPartContainerId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> packetChildIds = new ArrayList<>();
      packetChildIds.add(oggSegmentId);

      // FIXME: Change to FIELD_BASED_PAYLOAD
      descMap.put(oggPacketPartId,
         new DataBlockDescription(oggPacketPartId, "Ogg packet", "Ogg packet", PhysicalDataBlockType.PAYLOAD,
            packetChildIds, ChildOrder.SEQUENTIAL, null, packetLocationProps, 1, DataBlockDescription.UNKNOWN_SIZE,
            null, null));

      // 12. Ogg segment
      final Map<DataBlockId, LocationProperties> segmentLocationProps = new HashMap<>();
      segmentLocationProps.put(oggPacketPartId,
         new LocationProperties(0, 1, 999999, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> segmentChildIds = new ArrayList<>();

      descMap.put(oggSegmentId,
         new DataBlockDescription(oggSegmentId, "Ogg segment", "Ogg segment", PhysicalDataBlockType.FIELD,
            segmentChildIds, ChildOrder.SEQUENTIAL,
            new FieldProperties<BinaryValue>(FieldType.BINARY, null, null, null, DataBlockDescription.UNKNOWN_SIZE,
               DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null, null, null),
            segmentLocationProps, 0, 999999, null, null));

      // 13. The Ogg page payload
      final Map<DataBlockId, LocationProperties> pagePayloadLocationProps = new HashMap<>();

      pagePayloadLocationProps.put(oggPageId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> pagePayloadChildIds = new ArrayList<>();

      pagePayloadChildIds.add(oggPacketPartContainerId);

      // FIXME: Change to FIELD_BASED_PAYLOAD
      descMap.put(oggPayloadId,
         new DataBlockDescription(oggPayloadId, "Ogg page payload", "Ogg page payload", PhysicalDataBlockType.PAYLOAD,
            pagePayloadChildIds, ChildOrder.SEQUENTIAL, null, pagePayloadLocationProps, 0, 9999999, null, null));

      // 14. Ogg page

      final List<DataBlockId> pageChildIds = new ArrayList<>();
      pageChildIds.add(oggPageHeaderId);
      pageChildIds.add(oggPayloadId);

      final Map<DataBlockId, LocationProperties> pageLocationProps = new HashMap<>();
      pageLocationProps.put(null, new LocationProperties(DataBlockDescription.UNKNOWN_SIZE, 1, 1,
         DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      // Magic Keys
      MagicKey oggMagicKey = new MagicKey(OGG_MAGIC_KEY_BYTES, OGG_MAGIC_KEY_BYTES.length * Byte.SIZE,
         OGG_MAGIC_KEY_STRING, oggPageHeaderId, MagicKey.NO_BACKWARD_READING, 0);

      List<MagicKey> oggMagicKeys = new ArrayList<>();
      oggMagicKeys.add(oggMagicKey);

      descMap.put(oggPageId,
         new DataBlockDescription(oggPageId, "Ogg page", "The ogg page", PhysicalDataBlockType.CONTAINER, pageChildIds,
            ChildOrder.SEQUENTIAL, null, pageLocationProps, DataBlockDescription.UNKNOWN_SIZE,
            DataBlockDescription.UNKNOWN_SIZE, oggMagicKeys, null));

      Set<DataBlockId> topLevelIds = new HashSet<>();
      topLevelIds.add(oggPageId);

      // Byte orders and charsets
      List<ByteOrder> supportedByteOrders = new ArrayList<>();
      List<Charset> supportedCharsets = new ArrayList<>();

      supportedByteOrders.add(ByteOrder.LITTLE_ENDIAN);

      supportedCharsets.add(Charsets.CHARSET_ISO);

      final Set<DataBlockId> genericDataBlocks = new HashSet<>();

      DataFormatSpecification dummyOggSpec = new StandardDataFormatSpecification(OGG, descMap, topLevelIds,
         genericDataBlocks, new HashSet<>(), supportedByteOrders, supportedCharsets,
         new ArrayList<DataTransformationType>());

      return dummyOggSpec;
   }

}
