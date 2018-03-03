/**
 *
 * {@link APEv2Extension}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.defaultextensions.mp3.impl;

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
 * {@link MP3Extension}
 *
 */
public class MP3Extension implements Extension {

   private static final int FRAME_SYNC_BIT_COUNT = 11;
   private static final byte[] MP3_FRAME_SYNC = new byte[] { -1, -32 }; // 11 one bits
   private static final int MP3_HEADER_BYTE_LENGTH = 4;
   /**
    *
    */
   public static final DataFormat MP3 = new DataFormat("MP3", new HashSet<String>(), new HashSet<String>(),
      new ArrayList<String>(), "", new Date());

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionId()
    */
   @Override
   public String getExtensionId() {
      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.MP3DataBlocksExtension";
   }

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionDescription()
    */
   @Override
   public ExtensionDescription getExtensionDescription() {
      return new ExtensionDescription("MP3", "jMeta", "1.0", null, "MP3 extension", null, null);
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
         serviceProviders.add((T) new MP3DataBlocksService());
      }
      return serviceProviders;
   }

   private DataFormatSpecification createSpecification() {

      // Data blocks
      final DataBlockId mp3FrameId = new DataBlockId(MP3, "mp3");
      final DataBlockId mp3HeaderId = new DataBlockId(MP3, "mp3.header");
      final DataBlockId mp3CRCId = new DataBlockId(MP3, "mp3.crc");
      final DataBlockId mp3CRCFieldId = new DataBlockId(MP3, "mp3.crc.data");
      final DataBlockId mp3PayloadId = new DataBlockId(MP3, "mp3.payload");
      final DataBlockId mp3PayloadDataId = new DataBlockId(MP3, "mp3.payload.data");
      final DataBlockId mp3HeaderContentId = new DataBlockId(MP3, "mp3.header.content");

      Map<DataBlockId, DataBlockDescription> descMap = new HashMap<>();

      // 1. MP3 header content
      final Map<DataBlockId, LocationProperties> headerContentLocationProps = new HashMap<>();

      headerContentLocationProps.put(mp3HeaderId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerContentChildIds = new ArrayList<>();

      List<FlagDescription> flagDescriptions = new ArrayList<>();

      /*
       * Bit layout of the MPEG-1 Audio header Byte: -- 00 -- -- 01 -- -- 02 -- -- 03 -- Bit index in byte: 76543210
       * 76543210 76543210 76543210 Bit contents: AAAAAAAA AAABBCCD EEEEFFGH IIJJKLMM Description: A: Frame sync B: MPEG
       * audio version ID C: Layer description D: Protection bit E: Bitrate index F: Sampling rate frequency index G:
       * Padding bit H: Private bit I: Channel mode J: Mode extension K: Copyright L: Original M: Emphasis
       */

      flagDescriptions.add(new FlagDescription("Frame sync", new BitAddress(0, 0), "", FRAME_SYNC_BIT_COUNT, null));

      flagDescriptions.add(new FlagDescription("No protection bit", new BitAddress(1, 0), "", 1, null));

      List<String> layerValueNames = new ArrayList<>();
      layerValueNames.add("reserved");
      layerValueNames.add("Layer III");
      layerValueNames.add("Layer II");
      layerValueNames.add("Layer I");

      flagDescriptions.add(new FlagDescription("Layer", new BitAddress(1, 1), "", 2, layerValueNames));

      List<String> idValueNames = new ArrayList<>();
      idValueNames.add("MPEG Version 2.5");
      idValueNames.add("reserved");
      idValueNames.add("MPEG Version 2 (ISO/IEC 13818-3)");
      idValueNames.add("MPEG Version 1 (ISO/IEC 11172-3)");

      flagDescriptions.add(new FlagDescription("Id", new BitAddress(1, 3), "", 2, idValueNames));

      flagDescriptions.add(new FlagDescription("Private bit", new BitAddress(2, 0), "", 1, null));
      flagDescriptions.add(new FlagDescription("Padding bit", new BitAddress(2, 1), "", 1, null));
      flagDescriptions.add(new FlagDescription("Sampling frequency", new BitAddress(2, 2), "", 2, null));
      flagDescriptions.add(new FlagDescription("Bitrate index", new BitAddress(2, 4), "", 4, null));
      flagDescriptions.add(new FlagDescription("Emphasis bit", new BitAddress(3, 0), "", 2, null));
      flagDescriptions.add(new FlagDescription("Original or copy", new BitAddress(3, 2), "", 1, null));
      flagDescriptions.add(new FlagDescription("Copyright bit", new BitAddress(3, 3), "", 1, null));
      flagDescriptions.add(new FlagDescription("Mode extension bit", new BitAddress(3, 4), "", 2, null));
      flagDescriptions.add(new FlagDescription("Mode bit", new BitAddress(3, 6), "", 2, null));

      FlagSpecification mp3HeaderFlagSpec = new FlagSpecification(flagDescriptions, MP3_HEADER_BYTE_LENGTH,
         ByteOrder.BIG_ENDIAN, new byte[MP3_HEADER_BYTE_LENGTH]);

      Flags defaultFlags = new Flags(mp3HeaderFlagSpec);

      List<FieldFunction> functions = new ArrayList<>();

      Set<DataBlockId> affectedBlocks = new HashSet<>();

      affectedBlocks.add(mp3CRCId);

      functions.add(new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedBlocks, "No protection bit", 0));

      descMap.put(mp3HeaderContentId,
         new DataBlockDescription(mp3HeaderContentId, "MP3 header contents", "The MP3 header contents",
            PhysicalDataBlockType.FIELD, headerContentChildIds, ChildOrder.SEQUENTIAL,
            new FieldProperties<>(FieldType.FLAGS, defaultFlags, null, null, DataBlockDescription.UNKNOWN_SIZE,
               DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, mp3HeaderFlagSpec, null, null, null),
            headerContentLocationProps, MP3_HEADER_BYTE_LENGTH, MP3_HEADER_BYTE_LENGTH, null, null));

      // 02. MP3 header
      final Map<DataBlockId, LocationProperties> headerLocationProps = new HashMap<>();

      headerLocationProps.put(mp3FrameId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerChildIds = new ArrayList<>();
      headerChildIds.add(mp3HeaderContentId);

      descMap.put(mp3HeaderId,
         new DataBlockDescription(mp3HeaderId, "MP3 header", "The MP3 header", PhysicalDataBlockType.HEADER,
            headerChildIds, ChildOrder.SEQUENTIAL, null, headerLocationProps, MP3_HEADER_BYTE_LENGTH,
            MP3_HEADER_BYTE_LENGTH, null, null));

      // 03. CRC field
      final Map<DataBlockId, LocationProperties> crcFieldLocationProps = new HashMap<>();

      crcFieldLocationProps.put(mp3FrameId,
         new LocationProperties(4, 0, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> crcFieldChildIds = new ArrayList<>();
      crcFieldChildIds.add(mp3CRCFieldId);

      descMap.put(mp3CRCId, new DataBlockDescription(mp3CRCId, "MP3 CRC data", "The MP3 CRC data",
         PhysicalDataBlockType.FIELD, crcFieldChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<>(FieldType.BINARY,
            null, null, null, 2, 2, null, null, null, null, null, null, null, null),
         crcFieldLocationProps, 2, 2, null, null));

      // 04. CRC
      final Map<DataBlockId, LocationProperties> crcLocationProps = new HashMap<>();

      crcLocationProps.put(mp3FrameId,
         new LocationProperties(4, 0, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> crcChildIds = new ArrayList<>();
      crcChildIds.add(mp3CRCFieldId);

      descMap.put(mp3CRCId, new DataBlockDescription(mp3CRCId, "MP3 CRC", "The MP3 CRC", PhysicalDataBlockType.HEADER,
         crcChildIds, ChildOrder.SEQUENTIAL, null, crcLocationProps, 2, 2, null, null));

      // 05. MP3 frame payload dataa
      final List<DataBlockId> payloadDataChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> payloadDataLocationProps = new HashMap<>();
      payloadDataLocationProps.put(mp3PayloadId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(mp3PayloadDataId,
         new DataBlockDescription(mp3PayloadDataId, "payloadData", "The MP3 payload data", PhysicalDataBlockType.FIELD,
            payloadDataChildIds, ChildOrder.SEQUENTIAL,
            new FieldProperties<>(FieldType.BINARY, null, null, null, DataBlockDescription.UNKNOWN_SIZE,
               DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null, null, null),
            payloadDataLocationProps, 1, 998, null, null));

      // 06. MP3 frame payload
      final List<DataBlockId> payloadChildIds = new ArrayList<>();

      payloadChildIds.add(mp3PayloadDataId);

      final Map<DataBlockId, LocationProperties> payloadLocationProps = new HashMap<>();
      payloadLocationProps.put(mp3FrameId,
         new LocationProperties(4, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(mp3PayloadId,
         new DataBlockDescription(mp3PayloadId, "payload", "The MP3 payload", PhysicalDataBlockType.FIELD_BASED_PAYLOAD,
            payloadChildIds, ChildOrder.SEQUENTIAL, null, payloadLocationProps, 1, 998, null, null));

      // 08. MP3 tag

      final List<DataBlockId> frameChildIds = new ArrayList<>();
      frameChildIds.add(mp3HeaderId);
      frameChildIds.add(mp3PayloadId);

      final Map<DataBlockId, LocationProperties> frameLocationProps = new HashMap<>();
      frameLocationProps.put(null, new LocationProperties(DataBlockDescription.UNKNOWN_SIZE, 1, 1,
         DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      // Magic Keys
      MagicKey mp3MagicKey = new MagicKey(MP3_FRAME_SYNC, FRAME_SYNC_BIT_COUNT, "", mp3HeaderId,
         MagicKey.NO_BACKWARD_READING, 0);

      List<MagicKey> mp3FrameMagicKeys = new ArrayList<>();
      mp3FrameMagicKeys.add(mp3MagicKey);

      descMap.put(mp3FrameId,
         new DataBlockDescription(mp3FrameId, "MP3 Frame", "The MP3 Frame", PhysicalDataBlockType.CONTAINER,
            frameChildIds, ChildOrder.SEQUENTIAL, null, frameLocationProps, 33, 1024, mp3FrameMagicKeys, null));

      Set<DataBlockId> topLevelIds = new HashSet<>();
      topLevelIds.add(mp3FrameId);

      // Byte orders and charsets
      List<ByteOrder> supportedByteOrders = new ArrayList<>();
      List<Charset> supportedCharsets = new ArrayList<>();

      // There is no ByteOrder relevant for MP3
      supportedByteOrders.add(ByteOrder.BIG_ENDIAN);

      supportedCharsets.add(Charsets.CHARSET_ISO);

      DataFormatSpecification dummyMP3Spec = new StandardDataFormatSpecification(MP3, descMap, topLevelIds,
         new HashSet<>(), new HashSet<>(), supportedByteOrders, supportedCharsets,
         new ArrayList<DataTransformationType>());

      return dummyMP3Spec;
   }

}
