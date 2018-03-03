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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.StandardDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.BinaryValue;
import com.github.jmeta.library.dataformats.api.types.BitAddress;
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
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link ID3v23Extension}
 *
 */
public class ID3v23Extension implements Extension {

   private static final String EXT_HEADER_FLAG_CRC_DATA_PRESENT = "CRC data present";
   private static final String FRAME_FLAGS_COMPRESSION = "Compression";
   private static final String FRAME_FLAGS_ENCRYPTION = "Encryption";
   private static final String FRAME_FLAGS_FILE_ALTER_PRESERVATION = "File Alter Preservation";
   private static final String FRAME_FLAGS_GROUP_IDENTITY = "Group Identity";
   private static final String FRAME_FLAGS_READ_ONLY = "Read Only";
   private static final String FRAME_FLAGS_TAG_ALTER_PRESERVATION = "Tag Alter Preservation";
   private static final int FRAME_ID_SIZE = 4;
   private static final String ID3V23_GENERIC_CONTAINER_ID = "id3v23.payload.${FRAME_ID}";
   private static final byte[] ID3V23_TAG_VERSION_BYTES = new byte[] { 3, 0 };
   /**
    *
    */
   public static final DataFormat ID3v23 = new DataFormat("ID3v2.3", new HashSet<String>(), new HashSet<String>(),
      new ArrayList<String>(), "M. Nilsson", new Date());
   private static final DataBlockId GENERIC_FRAME_HEADER_FRAME_FLAGS_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".header.flags");
   private static final DataBlockId GENERIC_FRAME_HEADER_FRAME_ID_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".header.id");
   private static final DataBlockId GENERIC_FRAME_HEADER_FRAME_SIZE_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".header.size");
   private static final DataBlockId GENERIC_FRAME_HEADER_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".header");
   private static final DataBlockId GENERIC_FRAME_ID = new DataBlockId(ID3v23, ID3V23_GENERIC_CONTAINER_ID);
   private static final DataBlockId GENERIC_FRAME_PAYLOAD_DATA_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload.data");
   private static final DataBlockId GENERIC_FRAME_PAYLOAD_DECOMPRESSED_SIZE_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload.decompressedSize");
   private static final DataBlockId GENERIC_FRAME_PAYLOAD_ENCRYPTION_METHOD_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload.encryptionMethod");
   private static final DataBlockId GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload.groupId");
   private static final DataBlockId GENERIC_FRAME_PAYLOAD_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload");
   private static final DataBlockId GENERIC_INFORMATION_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload.information");
   private static final DataBlockId GENERIC_TEXT_ENCODING_ID = new DataBlockId(ID3v23,
      ID3V23_GENERIC_CONTAINER_ID + ".payload.textEncoding");
   private static final DataBlockId ID3V23_EXTENDED_HEADER_FIELD_CRC_ID = new DataBlockId(ID3v23,
      "id3v23.extHeader.crc");
   private static final DataBlockId ID3V23_EXTENDED_HEADER_FIELD_FLAGS_ID = new DataBlockId(ID3v23,
      "id3v23.extHeader.flags");
   private static final DataBlockId ID3V23_EXTENDED_HEADER_FIELD_PADDINGSIZE_ID = new DataBlockId(ID3v23,
      "id3v23.extHeader.paddingSize");
   private static final DataBlockId ID3V23_EXTENDED_HEADER_FIELD_SIZE_ID = new DataBlockId(ID3v23,
      "id3v23.extHeader.size");
   private static final DataBlockId ID3V23_EXTENDED_HEADER_ID = new DataBlockId(ID3v23, "id3v23.extHeader");
   private static final int ID3V23_FRAME_FLAG_SIZE = 2;
   private static final DataBlockId ID3V23_HEADER_FLAGS_FIELD_ID = new DataBlockId(ID3v23, "id3v23.header.flags");
   private static final DataBlockId ID3V23_HEADER_ID = new DataBlockId(ID3v23, "id3v23.header");
   private static final DataBlockId ID3V23_HEADER_ID_FIELD_ID = new DataBlockId(ID3v23, "id3v23.header.id");
   private static final DataBlockId ID3V23_HEADER_SIZE_FIELD_ID = new DataBlockId(ID3v23, "id3v23.header.size");
   private static final DataBlockId ID3V23_HEADER_VERSION_FIELD_ID = new DataBlockId(ID3v23, "id3v23.header.version");
   private static final DataBlockId ID3V23_PAYLOAD_ID = new DataBlockId(ID3v23, "id3v23.payload");
   private static final int ID3V23_TAG_FLAG_SIZE = 1;
   private static final DataBlockId ID3V23_TAG_ID = new DataBlockId(ID3v23, "id3v23");
   private static final byte[] ID3V23_TAG_ID_BYTES = new byte[] { 'I', 'D', '3' };
   private static final String ID3V23_TAG_ID_STRING = "ID3";
   private static final String ID3V23_TAG_VERSION_STRING = "\u0003\u0000";
   private static final byte[] ID3V23_TAG_MAGIC_KEY_BYTES = new byte[ID3V23_TAG_ID_BYTES.length
      + ID3V23_TAG_VERSION_BYTES.length];
   private static final String ID3V23_TAG_MAGIC_KEY_STRING = ID3V23_TAG_ID_STRING + ID3V23_TAG_VERSION_STRING;
   private static final DataBlockId PADDING_BYTES_FIELD_ID = new DataBlockId(ID3v23,
      "id3v23.payload.padding.payload.bytes");
   private static final DataBlockId PADDING_ID = new DataBlockId(ID3v23, "id3v23.payload.padding");
   private static final DataBlockId PADDING_PAYLOAD_ID = new DataBlockId(ID3v23, "id3v23.payload.padding.payload");
   private static final String TAG_FLAGS_EXPERIMENTAL_INDICATOR = "Experimental Indicator";
   private static final String TAG_FLAGS_EXTENDED_HEADER = "Extended Header";
   private static final String TAG_FLAGS_UNSYNCHRONIZATION = "Unsynchronization";
   private static final String TIT2_LOCAL_ID = "TIT2";
   private static final String TPE1_LOCAL_ID = "TPE1";
   private static final String TRCK_LOCAL_ID = "TRCK";

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

   @SuppressWarnings({ "unchecked", "rawtypes" })
   private void addConcreteContainerDescriptionFromGeneric(Map<DataBlockId, DataBlockDescription> descMap,
      DataBlockId genericId, String genericLocalId, String concreteLocalId, List<DataBlockId> concretePayloadChildren) {

      Reject.ifNull(concreteLocalId, "concreteLocalId");
      Reject.ifNull(genericLocalId, "genericLocalId");
      Reject.ifNull(genericId, "genericId");
      Reject.ifNull(descMap, "descMap");

      Reject.ifFalse(descMap.containsKey(genericId), "descMap.containsKey(genericId)");

      // Get generic block description
      DataBlockDescription genericDesc = descMap.get(genericId);

      // Replace id, name and spec desc
      DataBlockId concreteId = replaceId(genericId, genericLocalId, concreteLocalId);
      String concreteSpecDesc = genericDesc.getSpecDescription().replace(genericLocalId, concreteLocalId);
      String concreteName = genericDesc.getName().replace(genericLocalId, concreteLocalId);

      // Define all other items to replace
      List<DataBlockId> genericChildren = new ArrayList<>(genericDesc.getOrderedChildIds());
      List<DataBlockId> concreteChildren = new ArrayList<>();
      Map<DataBlockId, LocationProperties> concreteLocationProperties = new HashMap<>();
      FieldProperties<?> genericFieldProperties = genericDesc.getFieldProperties();
      FieldProperties<?> concreteFieldProperties = genericDesc.getFieldProperties();

      // Add payload children, if this is a PAYLOAD block
      if (genericDesc.getPhysicalType().equals(PhysicalDataBlockType.FIELD_BASED_PAYLOAD)
         || genericDesc.getPhysicalType().equals(PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD))
         if (concretePayloadChildren != null) {
            for (int i = 0; i < concretePayloadChildren.size(); ++i) {
               DataBlockId concretePayloadChildId = concretePayloadChildren.get(i);

               final DataBlockId overriddenId = descMap.get(concretePayloadChildId).getOverriddenId();

               if (overriddenId != null && genericChildren.contains(overriddenId))
                  genericChildren.remove(overriddenId);
            }

            genericChildren.addAll(concretePayloadChildren);
         }

      // Replace child IDs, create concrete block descriptions for them
      for (int i = 0; i < genericChildren.size(); ++i) {
         DataBlockId genericChildId = genericChildren.get(i);

         // Add a new concrete block description for each child
         addConcreteContainerDescriptionFromGeneric(descMap, genericChildId, genericLocalId, concreteLocalId,
            concretePayloadChildren);

         concreteChildren.add(replaceId(genericChildId, genericLocalId, concreteLocalId));
      }

      // Replace location properties
      for (Iterator<DataBlockId> iterator = genericDesc.getAllParentsForLocationProperties().iterator(); iterator
         .hasNext();) {
         DataBlockId genericLocationParentId = iterator.next();
         LocationProperties locProp = genericDesc.getLocationPropertiesForParent(genericLocationParentId);

         concreteLocationProperties.put(replaceId(genericLocationParentId, genericLocalId, concreteLocalId), locProp);
      }

      // Replace field functions
      if (genericFieldProperties != null) {
         List<FieldFunction> concreteFieldFunctions = new ArrayList<>();

         for (int i = 0; i < genericFieldProperties.getFieldFunctions().size(); ++i) {
            FieldFunction genericFunction = genericFieldProperties.getFieldFunctions().get(i);

            Set<DataBlockId> concreteAffectedIds = new HashSet<>();

            for (Iterator<DataBlockId> affectedBlockIterator = genericFunction.getAffectedBlockIds()
               .iterator(); affectedBlockIterator.hasNext();) {
               DataBlockId genericAffectedBlockId = affectedBlockIterator.next();

               concreteAffectedIds.add(replaceId(genericAffectedBlockId, genericLocalId, concreteLocalId));
            }

            concreteFieldFunctions.add(new FieldFunction(genericFunction.getFieldFunctionType(), concreteAffectedIds,
               genericFunction.getFlagName(), genericFunction.getFlagValue()));
         }

         concreteFieldProperties = new FieldProperties(genericFieldProperties.getFieldType(),
            genericFieldProperties.getDefaultValue(), genericFieldProperties.getEnumeratedValues(),
            genericFieldProperties.getTerminationBytes(), genericFieldProperties.getMinimumCharacterLength(),
            genericFieldProperties.getMaximumCharacterLength(), genericFieldProperties.getTerminationCharacter(),
            genericFieldProperties.getPatterns(), genericFieldProperties.getMinimumValue(),
            genericFieldProperties.getMaximumValue(), genericFieldProperties.getFlagSpecification(),
            genericFieldProperties.getFixedCharacterEncoding(), genericFieldProperties.getFixedByteOrder(),
            concreteFieldFunctions);
      }

      // Create concrete block description
      DataBlockDescription concreteDesc = new DataBlockDescription(concreteId, concreteName, concreteSpecDesc,
         genericDesc.getPhysicalType(), concreteChildren, concreteFieldProperties, concreteLocationProperties,
         genericDesc.getMinimumByteLength(), genericDesc.getMaximumByteLength(), genericDesc.getMagicKeys(),
         null);

      descMap.put(concreteId, concreteDesc);
   }

   private void addExtendedHeader(Map<DataBlockId, DataBlockDescription> descMap) {

      // 1. Extended Header Size
      final List<DataBlockId> extHeaderSizeChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> extHeaderSizeLocationProps = new HashMap<>();
      extHeaderSizeLocationProps.put(ID3V23_EXTENDED_HEADER_ID,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(ID3V23_EXTENDED_HEADER_FIELD_SIZE_ID,
         new DataBlockDescription(ID3V23_EXTENDED_HEADER_FIELD_SIZE_ID, "id3v23 extended header size",
            "The id3v23 extended header size", PhysicalDataBlockType.FIELD, extHeaderSizeChildIds,
            new FieldProperties<Integer>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null, 4, 4,
               null, null, null, null, null, null, null, new ArrayList<>()), extHeaderSizeLocationProps,
            4, 4, null, null));

      // 2. Extended header flags
      final List<DataBlockId> flagsChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> flagsLocationProps = new HashMap<>();
      extHeaderSizeLocationProps.put(ID3V23_EXTENDED_HEADER_ID,
         new LocationProperties(4, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      List<FlagDescription> extendedHeaderFlagDescriptions = new ArrayList<>();

      extendedHeaderFlagDescriptions
         .add(new FlagDescription(EXT_HEADER_FLAG_CRC_DATA_PRESENT, new BitAddress(0, 0), "", 1, null));

      final byte[] defaultTagFlagBytes = new byte[] { 0 };

      FlagSpecification id3v23TagFlagSpec = new FlagSpecification(extendedHeaderFlagDescriptions, 1,
         ByteOrder.BIG_ENDIAN, defaultTagFlagBytes);

      Flags defaultExtHeaderFlags = new Flags(id3v23TagFlagSpec);

      defaultExtHeaderFlags.fromArray(defaultTagFlagBytes);

      final ArrayList<FieldFunction> extHeaderFlagFunctions = new ArrayList<>();

      Set<DataBlockId> affectedDataBlockIds = new HashSet<>();

      affectedDataBlockIds.add(ID3V23_EXTENDED_HEADER_FIELD_CRC_ID);

      extHeaderFlagFunctions.add(
         new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedDataBlockIds, EXT_HEADER_FLAG_CRC_DATA_PRESENT, 1));

      descMap.put(ID3V23_EXTENDED_HEADER_FIELD_FLAGS_ID,
         new DataBlockDescription(ID3V23_EXTENDED_HEADER_FIELD_FLAGS_ID, "id3v23 extended header flags",
            "The id3v23 extended header flags", PhysicalDataBlockType.FIELD, flagsChildIds, new FieldProperties<>(FieldType.FLAGS, defaultExtHeaderFlags, null, null, DataBlockDescription.UNKNOWN_SIZE,
               DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null, null, extHeaderFlagFunctions),
            flagsLocationProps,
            ID3V23_TAG_FLAG_SIZE, ID3V23_TAG_FLAG_SIZE, null, null));

      // 3. Ext header padding size
      final List<DataBlockId> paddingSizeChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> paddingSizeLocationProps = new HashMap<>();
      extHeaderSizeLocationProps.put(ID3V23_EXTENDED_HEADER_ID,
         new LocationProperties(6, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(ID3V23_EXTENDED_HEADER_FIELD_PADDINGSIZE_ID,
         new DataBlockDescription(ID3V23_EXTENDED_HEADER_FIELD_PADDINGSIZE_ID, "id3v23 extended header padding size",
            "The id3v23 extended header padding size", PhysicalDataBlockType.FIELD, paddingSizeChildIds,
            new FieldProperties<Integer>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null,
               DataBlockDescription.UNKNOWN_SIZE, DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null,
               null, new ArrayList<>()),
            paddingSizeLocationProps,
            4, 4, null, null));

      // 4. Ext header CRC
      final List<DataBlockId> crcChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> crcLocationProps = new HashMap<>();
      crcLocationProps.put(ID3V23_EXTENDED_HEADER_ID,
         new LocationProperties(6, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(ID3V23_EXTENDED_HEADER_FIELD_CRC_ID,
         new DataBlockDescription(ID3V23_EXTENDED_HEADER_FIELD_CRC_ID, "id3v23 extended header CRC",
            "The id3v23 extended header CRC", PhysicalDataBlockType.FIELD, crcChildIds, new FieldProperties<Integer>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null,
               DataBlockDescription.UNKNOWN_SIZE, DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null,
               null, new ArrayList<>()),
            crcLocationProps,
            4, 4, null, null));

      // 5. id3v23 extended header
      final Map<DataBlockId, LocationProperties> extHeaderLocationProps = new HashMap<>();

      extHeaderLocationProps.put(ID3V23_TAG_ID,
         new LocationProperties(10, 1, 0, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> extHeaderChildIds = new ArrayList<>();
      extHeaderChildIds.add(ID3V23_EXTENDED_HEADER_FIELD_SIZE_ID);
      extHeaderChildIds.add(ID3V23_EXTENDED_HEADER_FIELD_FLAGS_ID);
      extHeaderChildIds.add(ID3V23_EXTENDED_HEADER_FIELD_PADDINGSIZE_ID);
      extHeaderChildIds.add(ID3V23_EXTENDED_HEADER_FIELD_CRC_ID);

      descMap.put(ID3V23_EXTENDED_HEADER_ID,
         new DataBlockDescription(ID3V23_EXTENDED_HEADER_ID, "id3v23 extended header", "The id3v23 extended header",
            PhysicalDataBlockType.HEADER, extHeaderChildIds, null, extHeaderLocationProps, 10, 14,
            null, null));
   }

   /**
    * @param descMap
    */
   private void addGenericFrameHeader(Map<DataBlockId, DataBlockDescription> descMap) {

      // 1 Frame id
      final List<DataBlockId> frameIdChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> frameIdLocationProps = new HashMap<>();
      final LocationProperties idFieldLocProps = new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE,
         new ArrayList<>(), new ArrayList<>());
      frameIdLocationProps.put(GENERIC_FRAME_HEADER_ID, idFieldLocProps);
      frameIdLocationProps.put(GENERIC_FRAME_ID, idFieldLocProps);

      List<FieldFunction> genericIdFieldFunction = new ArrayList<>();

      Set<DataBlockId> affectedBlocks = new HashSet<>();

      affectedBlocks.add(GENERIC_FRAME_ID);

      genericIdFieldFunction.add(new FieldFunction(FieldFunctionType.ID_OF, affectedBlocks, null, null));

      descMap.put(GENERIC_FRAME_HEADER_FRAME_ID_FIELD_ID,
         new DataBlockDescription(GENERIC_FRAME_HEADER_FRAME_ID_FIELD_ID, "Generic frame id field",
            "The generic frame id field", PhysicalDataBlockType.FIELD, frameIdChildIds, new FieldProperties<>(FieldType.STRING, null, null, null, FRAME_ID_SIZE, FRAME_ID_SIZE, null, null, null,
               null, null, Charsets.CHARSET_ISO, null, genericIdFieldFunction),
            frameIdLocationProps,
            FRAME_ID_SIZE, FRAME_ID_SIZE, null, null));

      // 2 Frame size
      final List<DataBlockId> frameSizeChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> frameSizeLocationProps = new HashMap<>();
      frameSizeLocationProps.put(GENERIC_FRAME_HEADER_ID, idFieldLocProps);

      List<FieldFunction> genericFrameSizeFieldFunction = new ArrayList<>();

      Set<DataBlockId> affectedFrameSizeBlocks = new HashSet<>();

      affectedFrameSizeBlocks.add(GENERIC_FRAME_PAYLOAD_ID);

      genericFrameSizeFieldFunction
         .add(new FieldFunction(FieldFunctionType.SIZE_OF, affectedFrameSizeBlocks, null, null));

      descMap.put(GENERIC_FRAME_HEADER_FRAME_SIZE_FIELD_ID,
         new DataBlockDescription(GENERIC_FRAME_HEADER_FRAME_SIZE_FIELD_ID, "Generic frame size field",
            "The generic frame size field", PhysicalDataBlockType.FIELD, frameSizeChildIds, new FieldProperties<Integer>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null, 4, 4, null, null, null,
               null, null, Charsets.CHARSET_ISO, null, genericFrameSizeFieldFunction),
            frameSizeLocationProps,
            4, 4, null, null));

      // 3 Frame flags
      final List<DataBlockId> frameFlagsChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> frameFlagsLocationProps = new HashMap<>();
      frameFlagsLocationProps.put(GENERIC_FRAME_HEADER_ID, idFieldLocProps);

      List<FlagDescription> frameFlagDescriptions = new ArrayList<>();

      frameFlagDescriptions
         .add(new FlagDescription(FRAME_FLAGS_TAG_ALTER_PRESERVATION, new BitAddress(0, 0), "", 1, null));
      frameFlagDescriptions
         .add(new FlagDescription(FRAME_FLAGS_FILE_ALTER_PRESERVATION, new BitAddress(0, 1), "", 1, null));
      frameFlagDescriptions.add(new FlagDescription(FRAME_FLAGS_READ_ONLY, new BitAddress(0, 2), "", 1, null));
      frameFlagDescriptions.add(new FlagDescription(FRAME_FLAGS_COMPRESSION, new BitAddress(1, 0), "", 1, null));
      frameFlagDescriptions.add(new FlagDescription(FRAME_FLAGS_ENCRYPTION, new BitAddress(1, 1), "", 1, null));
      frameFlagDescriptions.add(new FlagDescription(FRAME_FLAGS_GROUP_IDENTITY, new BitAddress(1, 2), "", 1, null));

      final byte[] defaultTagFlagBytes = new byte[] { 0, 0 };

      FlagSpecification id3v23FrameFlagSpec = new FlagSpecification(frameFlagDescriptions, ID3V23_FRAME_FLAG_SIZE,
         ByteOrder.BIG_ENDIAN, defaultTagFlagBytes);

      Flags defaultFrameFlags = new Flags(id3v23FrameFlagSpec);

      defaultFrameFlags.fromArray(defaultTagFlagBytes);

      final ArrayList<FieldFunction> frameFlagFunctions = new ArrayList<>();

      Set<DataBlockId> affectedDataBlockIdsCompression = new HashSet<>();

      affectedDataBlockIdsCompression.add(GENERIC_FRAME_PAYLOAD_DECOMPRESSED_SIZE_FIELD_ID);

      Set<DataBlockId> affectedDataBlockIdsCompressionTrafo = new HashSet<>();

      affectedDataBlockIdsCompressionTrafo.add(GENERIC_FRAME_PAYLOAD_ID);

      frameFlagFunctions.add(
         new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedDataBlockIdsCompression, FRAME_FLAGS_COMPRESSION, 1));
      frameFlagFunctions.add(new FieldFunction(FieldFunctionType.TRANSFORMATION_OF,
         affectedDataBlockIdsCompressionTrafo, FRAME_FLAGS_COMPRESSION, 1));

      Set<DataBlockId> affectedDataBlockIdsGroup = new HashSet<>();

      affectedDataBlockIdsGroup.add(GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID);

      frameFlagFunctions.add(
         new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedDataBlockIdsGroup, FRAME_FLAGS_GROUP_IDENTITY, 1));

      Set<DataBlockId> affectedDataBlockIdsEncryption = new HashSet<>();

      affectedDataBlockIdsEncryption.add(GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID);

      Set<DataBlockId> affectedDataBlockIdsEncryptionTrafo = new HashSet<>();

      affectedDataBlockIdsEncryptionTrafo.add(GENERIC_FRAME_PAYLOAD_ID);

      frameFlagFunctions.add(
         new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedDataBlockIdsEncryption, FRAME_FLAGS_ENCRYPTION, 1));
      frameFlagFunctions.add(new FieldFunction(FieldFunctionType.TRANSFORMATION_OF, affectedDataBlockIdsEncryptionTrafo,
         FRAME_FLAGS_ENCRYPTION, 1));

      descMap.put(
         GENERIC_FRAME_HEADER_FRAME_FLAGS_FIELD_ID, new DataBlockDescription(GENERIC_FRAME_HEADER_FRAME_FLAGS_FIELD_ID,
            "Generic frame flags field", "The generic frame flags field", PhysicalDataBlockType.FIELD,
            frameFlagsChildIds, new FieldProperties<>(FieldType.FLAGS, defaultFrameFlags, null,
               null, 2, 2, null, null, null, null, id3v23FrameFlagSpec, null, null, frameFlagFunctions), frameFlagsLocationProps,
            2, 2, null, null));

      // 4. Frame header
      final List<DataBlockId> frameHeaderChildIds = new ArrayList<>();
      frameHeaderChildIds.add(GENERIC_FRAME_HEADER_FRAME_ID_FIELD_ID);
      frameHeaderChildIds.add(GENERIC_FRAME_HEADER_FRAME_SIZE_FIELD_ID);
      frameHeaderChildIds.add(GENERIC_FRAME_HEADER_FRAME_FLAGS_FIELD_ID);

      final Map<DataBlockId, LocationProperties> frameHeaderLocationProps = new HashMap<>();
      frameHeaderLocationProps.put(GENERIC_FRAME_ID, idFieldLocProps);

      descMap.put(GENERIC_FRAME_HEADER_ID,
         new DataBlockDescription(GENERIC_FRAME_HEADER_ID, "Generic frame header", "The generic frame header",
            PhysicalDataBlockType.HEADER, frameHeaderChildIds, null, frameHeaderLocationProps, 10,
            10, null, null));
   }

   /**
    * @param descMap
    */
   private void addGenericFramePayload(Map<DataBlockId, DataBlockDescription> descMap) {

      // 1 Decompressed size
      final List<DataBlockId> decompressedSizeChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> decompressedSizeLocationProps = new HashMap<>();
      decompressedSizeLocationProps.put(GENERIC_FRAME_PAYLOAD_ID,
         new LocationProperties(0, 1, 0, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(GENERIC_FRAME_PAYLOAD_DECOMPRESSED_SIZE_FIELD_ID,
         new DataBlockDescription(GENERIC_FRAME_PAYLOAD_DECOMPRESSED_SIZE_FIELD_ID, "Decompressed size field",
            "The decompressed size field", PhysicalDataBlockType.FIELD, decompressedSizeChildIds, new FieldProperties<Integer>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null,
               DataBlockDescription.UNKNOWN_SIZE, DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null,
               null, null),
            decompressedSizeLocationProps,
            4, 4, null, null));

      // 2 Encryption method
      final List<DataBlockId> encryptionMethodChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> encryptionMethodLocationProps = new HashMap<>();
      encryptionMethodLocationProps.put(GENERIC_FRAME_PAYLOAD_ID,
         new LocationProperties(0, 1, 0, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(GENERIC_FRAME_PAYLOAD_ENCRYPTION_METHOD_FIELD_ID,
         new DataBlockDescription(GENERIC_FRAME_PAYLOAD_ENCRYPTION_METHOD_FIELD_ID, "Encryption method field",
            "The encryption method field", PhysicalDataBlockType.FIELD, encryptionMethodChildIds, new FieldProperties<Byte>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null,
               DataBlockDescription.UNKNOWN_SIZE, DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null,
               null, null),
            encryptionMethodLocationProps,
            1, 1, null, null));

      // 3 Group Id
      final List<DataBlockId> groupIdChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> groupIdLocationProps = new HashMap<>();
      groupIdLocationProps.put(GENERIC_FRAME_PAYLOAD_ID,
         new LocationProperties(0, 1, 0, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID,
         new DataBlockDescription(GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID, "Group id field", "The group id field",
            PhysicalDataBlockType.FIELD, groupIdChildIds, new FieldProperties<>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null, DataBlockDescription.UNKNOWN_SIZE,
               DataBlockDescription.UNKNOWN_SIZE, null, null, Byte.valueOf((byte) 0x80), Byte.valueOf((byte) 0xFF),
               null, null, null, null),
            groupIdLocationProps,
            1, 1, null, null));

      // 4 Payload data
      final List<DataBlockId> payloadDataChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> payloadDataLocationProps = new HashMap<>();
      payloadDataLocationProps.put(GENERIC_FRAME_PAYLOAD_ID,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(GENERIC_FRAME_PAYLOAD_DATA_FIELD_ID,
         new DataBlockDescription(GENERIC_FRAME_PAYLOAD_DATA_FIELD_ID, "Payload data field", "The payload data field",
            PhysicalDataBlockType.FIELD, payloadDataChildIds, new FieldProperties<BinaryValue>(FieldType.BINARY, null, null, null, DataBlockDescription.UNKNOWN_SIZE,
               DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null, null, null),
            payloadDataLocationProps,
            1, DataBlockDescription.UNKNOWN_SIZE, null, null));

      // 5 Generic Frame Payload
      final List<DataBlockId> genericFramePayloadChildIds = new ArrayList<>();
      genericFramePayloadChildIds.add(GENERIC_FRAME_PAYLOAD_DECOMPRESSED_SIZE_FIELD_ID);
      genericFramePayloadChildIds.add(GENERIC_FRAME_PAYLOAD_ENCRYPTION_METHOD_FIELD_ID);
      genericFramePayloadChildIds.add(GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID);
      genericFramePayloadChildIds.add(GENERIC_FRAME_PAYLOAD_DATA_FIELD_ID);

      final Map<DataBlockId, LocationProperties> genericFramePayloadLocationProps = new HashMap<>();
      genericFramePayloadLocationProps.put(GENERIC_FRAME_ID,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(GENERIC_FRAME_PAYLOAD_ID,
         new DataBlockDescription(GENERIC_FRAME_PAYLOAD_ID, "Generic frame payload", "The generic frame payload",
            PhysicalDataBlockType.FIELD_BASED_PAYLOAD, genericFramePayloadChildIds, new FieldProperties<>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null, DataBlockDescription.UNKNOWN_SIZE,
               DataBlockDescription.UNKNOWN_SIZE, null, null, Byte.valueOf((byte) 0x80), Byte.valueOf((byte) 0xFF),
               null, null, null, null),
            genericFramePayloadLocationProps,
            1, DataBlockDescription.UNKNOWN_SIZE, null, null));
   }

   /**
    * @param descMap
    */
   private void addHeader(Map<DataBlockId, DataBlockDescription> descMap) {

      // 1. tag id
      final List<DataBlockId> tagIdChildIds = new ArrayList<>();

      Map<String, byte[]> tagIdEnumerated = new HashMap<>();

      tagIdEnumerated.put(ID3V23_TAG_ID_STRING, ID3V23_TAG_ID_BYTES);

      final Map<DataBlockId, LocationProperties> tagIdLocationProps = new HashMap<>();
      tagIdLocationProps.put(ID3V23_HEADER_ID,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap
         .put(ID3V23_HEADER_ID_FIELD_ID,
            new DataBlockDescription(ID3V23_HEADER_ID_FIELD_ID, "id3v23 tag header id", "The id3v23 tag header id",
               PhysicalDataBlockType.FIELD, tagIdChildIds, new FieldProperties<>(FieldType.STRING, ID3V23_TAG_ID_STRING, tagIdEnumerated, null, 3, 3, null, null,
                  null, null, null, Charsets.CHARSET_ISO, null, new ArrayList<>()),
               tagIdLocationProps,
               3, 3, null, null));

      // 2. tag version
      final List<DataBlockId> versionChildIds = new ArrayList<>();

      Map<byte[], byte[]> versionEnumerated = new HashMap<>();

      versionEnumerated.put(ID3V23_TAG_VERSION_BYTES, ID3V23_TAG_VERSION_BYTES);

      final Map<DataBlockId, LocationProperties> versionLocationProps = new HashMap<>();
      versionLocationProps.put(ID3V23_HEADER_ID,
         new LocationProperties(3, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(ID3V23_HEADER_VERSION_FIELD_ID, new DataBlockDescription(ID3V23_HEADER_VERSION_FIELD_ID,
         "id3v23 tag header version", "The id3v23 tag header version", PhysicalDataBlockType.FIELD, versionChildIds,
         new FieldProperties<>(FieldType.BINARY, ID3V23_TAG_VERSION_BYTES, versionEnumerated,
            null, 2, 2, null, null, null, null, null, null, null, new ArrayList<>()), versionLocationProps,
         2, 2, null, null));

      // 3. tag flags
      final List<DataBlockId> flagsChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> flagsLocationProps = new HashMap<>();
      flagsLocationProps.put(ID3V23_HEADER_ID,
         new LocationProperties(5, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      List<FlagDescription> tagFlagDescriptions = new ArrayList<>();

      tagFlagDescriptions.add(new FlagDescription(TAG_FLAGS_UNSYNCHRONIZATION, new BitAddress(0, 0), "", 1, null));
      tagFlagDescriptions.add(new FlagDescription(TAG_FLAGS_EXTENDED_HEADER, new BitAddress(0, 1), "", 1, null));
      tagFlagDescriptions.add(new FlagDescription(TAG_FLAGS_EXPERIMENTAL_INDICATOR, new BitAddress(0, 2), "", 1, null));

      final byte[] defaultTagFlagBytes = new byte[] { 0 };

      FlagSpecification id3v23TagFlagSpec = new FlagSpecification(tagFlagDescriptions, ID3V23_TAG_FLAG_SIZE,
         ByteOrder.BIG_ENDIAN, defaultTagFlagBytes);

      Flags defaultTagFlags = new Flags(id3v23TagFlagSpec);

      defaultTagFlags.fromArray(defaultTagFlagBytes);

      final ArrayList<FieldFunction> tagFlagFunctions = new ArrayList<>();

      Set<DataBlockId> affectedDataBlockIds = new HashSet<>();

      affectedDataBlockIds.add(ID3V23_EXTENDED_HEADER_ID);

      Set<DataBlockId> affectedDataBlockIdsUnsync = new HashSet<>();

      affectedDataBlockIdsUnsync.add(GENERIC_FRAME_HEADER_ID);
      affectedDataBlockIdsUnsync.add(GENERIC_FRAME_PAYLOAD_ID);
      affectedDataBlockIdsUnsync.add(ID3V23_EXTENDED_HEADER_ID);

      tagFlagFunctions
         .add(new FieldFunction(FieldFunctionType.PRESENCE_OF, affectedDataBlockIds, TAG_FLAGS_EXTENDED_HEADER, 1));
      tagFlagFunctions.add(new FieldFunction(FieldFunctionType.TRANSFORMATION_OF, affectedDataBlockIdsUnsync,
         TAG_FLAGS_UNSYNCHRONIZATION, 1));

      descMap.put(ID3V23_HEADER_FLAGS_FIELD_ID, new DataBlockDescription(ID3V23_HEADER_FLAGS_FIELD_ID,
         "id3v23 tag header flags", "The id3v23 tag header flags", PhysicalDataBlockType.FIELD, flagsChildIds,
         new FieldProperties<>(FieldType.FLAGS, defaultTagFlags, null, null, DataBlockDescription.UNKNOWN_SIZE,
            DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, id3v23TagFlagSpec, null, null, tagFlagFunctions),
         flagsLocationProps,
         ID3V23_TAG_FLAG_SIZE, ID3V23_TAG_FLAG_SIZE, null, null));

      // 4. tag size
      final List<DataBlockId> sizeChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> sizeLocationProps = new HashMap<>();
      sizeLocationProps.put(ID3V23_HEADER_ID,
         new LocationProperties(6, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      Set<DataBlockId> affectedTagSizeBlocks = new HashSet<>();

      affectedTagSizeBlocks.add(ID3V23_PAYLOAD_ID);

      List<FieldFunction> tagSizeFunc = new ArrayList<>();

      tagSizeFunc.add(new FieldFunction(FieldFunctionType.SIZE_OF, affectedTagSizeBlocks, null, null));

      descMap.put(ID3V23_HEADER_SIZE_FIELD_ID,
         new DataBlockDescription(ID3V23_HEADER_SIZE_FIELD_ID, "id3v23 tag size", "The id3v23 tag size",
            PhysicalDataBlockType.FIELD, sizeChildIds, new FieldProperties<Integer>(FieldType.UNSIGNED_WHOLE_NUMBER, null, null, null,
               DataBlockDescription.UNKNOWN_SIZE, DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null,
               null, tagSizeFunc),
            sizeLocationProps,
            4, 4, null, null));

      // 5. id3v23 header
      List<byte[]> fixedByteValueBytes = new ArrayList<>();
      List<String> fixedStringValue = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> headerLocationProps = new HashMap<>();

      headerLocationProps.put(ID3V23_TAG_ID,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      fixedByteValueBytes.add(ID3V23_TAG_ID_BYTES);
      fixedStringValue.add(ID3V23_TAG_ID_STRING);

      final List<DataBlockId> headerChildIds = new ArrayList<>();
      headerChildIds.add(ID3V23_HEADER_ID_FIELD_ID);
      headerChildIds.add(ID3V23_HEADER_VERSION_FIELD_ID);
      headerChildIds.add(ID3V23_HEADER_FLAGS_FIELD_ID);
      headerChildIds.add(ID3V23_HEADER_SIZE_FIELD_ID);

      descMap.put(ID3V23_HEADER_ID,
         new DataBlockDescription(ID3V23_HEADER_ID, "id3v23 tag header", "The id3v23 tag header",
            PhysicalDataBlockType.HEADER, headerChildIds, null, headerLocationProps, 10, 10, null,
            null));
   }

   /**
    *
    */
   private void addPaddingContainer(Map<DataBlockId, DataBlockDescription> descMap) {

      // 1. Padding first field
      final Map<DataBlockId, LocationProperties> firstFieldLocationProps = new HashMap<>();
      final LocationProperties paddingfirstFieldLoc = new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE,
         new ArrayList<>(), new ArrayList<>());
      firstFieldLocationProps.put(PADDING_ID, paddingfirstFieldLoc);
      firstFieldLocationProps.put(PADDING_PAYLOAD_ID, paddingfirstFieldLoc);

      final byte[] paddingByte = new byte[] { 0 };

      // 2. Padding bytes
      final List<DataBlockId> paddingBytesChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> paddingBytesLocationProps = new HashMap<>();
      paddingBytesLocationProps.put(PADDING_PAYLOAD_ID,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(PADDING_BYTES_FIELD_ID,
         new DataBlockDescription(PADDING_BYTES_FIELD_ID, "Padding bytes", "Padding bytes", PhysicalDataBlockType.FIELD,
            paddingBytesChildIds, new FieldProperties<>(FieldType.BINARY, paddingByte, null, null, DataBlockDescription.UNKNOWN_SIZE,
               DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null, null, new ArrayList<>()),
            paddingBytesLocationProps,
            1, DataBlockDescription.UNKNOWN_SIZE, null, null));

      // 3. Padding Payload
      final List<DataBlockId> paddingPayloadChildIds = new ArrayList<>();

      // paddingPayloadChildIds.add(PADDING_FIRST_BYTE_FIELD_ID);
      paddingPayloadChildIds.add(PADDING_BYTES_FIELD_ID);

      final Map<DataBlockId, LocationProperties> paddingPayloadLocationProps = new HashMap<>();
      paddingPayloadLocationProps.put(PADDING_ID,
         new LocationProperties(1, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(PADDING_PAYLOAD_ID,
         new DataBlockDescription(PADDING_PAYLOAD_ID, "Padding payload", "Padding payload",
            PhysicalDataBlockType.FIELD_BASED_PAYLOAD, paddingPayloadChildIds, null, paddingPayloadLocationProps,
            1, DataBlockDescription.UNKNOWN_SIZE, null, null));

      // 4. Padding Container
      MagicKey paddingMagicKey = new MagicKey(paddingByte, Byte.SIZE, "\u0000", PADDING_PAYLOAD_ID,
         MagicKey.NO_BACKWARD_READING, 0);

      List<MagicKey> paddingMagicKeys = new ArrayList<>();
      paddingMagicKeys.add(paddingMagicKey);

      final List<DataBlockId> paddingChildIds = new ArrayList<>();

      paddingChildIds.add(PADDING_PAYLOAD_ID);

      final Map<DataBlockId, LocationProperties> paddingLocationProps = new HashMap<>();
      paddingLocationProps.put(ID3V23_PAYLOAD_ID, new LocationProperties(DataBlockDescription.UNKNOWN_SIZE, 0, 1,
         DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(PADDING_ID,
         new DataBlockDescription(PADDING_ID, "Padding", "Padding", PhysicalDataBlockType.CONTAINER, paddingChildIds,
            null, paddingLocationProps, 1, DataBlockDescription.UNKNOWN_SIZE, paddingMagicKeys, null));
   }

   /**
    * @param descMap
    */
   private void addPayload(Map<DataBlockId, DataBlockDescription> descMap) {

      // 10. id3v23 payload
      final List<DataBlockId> payloadChildIds = new ArrayList<>();

      payloadChildIds.add(GENERIC_FRAME_ID);
      payloadChildIds.add(PADDING_ID);

      final Map<DataBlockId, LocationProperties> payloadLocationProps = new HashMap<>();
      payloadLocationProps.put(ID3V23_TAG_ID,
         new LocationProperties(10, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(ID3V23_PAYLOAD_ID,
         new DataBlockDescription(ID3V23_PAYLOAD_ID, "payload", "The id3v23 payload",
            PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD, payloadChildIds, null, payloadLocationProps,
            11, DataBlockDescription.UNKNOWN_SIZE, null, null));
   }

   /**
    * @param descMap
    */
   private void addPayloadFrames(Map<DataBlockId, DataBlockDescription> descMap) {

      // 1. generic frame
      addGenericFramePayload(descMap);
      addGenericFrameHeader(descMap);

      final List<DataBlockId> genericFrameChildIds = new ArrayList<>();

      genericFrameChildIds.add(GENERIC_FRAME_HEADER_ID);
      genericFrameChildIds.add(GENERIC_FRAME_PAYLOAD_ID);

      final Map<DataBlockId, LocationProperties> genericFrameLocationProps = new HashMap<>();
      genericFrameLocationProps.put(ID3V23_PAYLOAD_ID, new LocationProperties(DataBlockDescription.UNKNOWN_SIZE, 999999,
         0, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      // Magic Keys
      MagicKey genericFrameMagicKey = new MagicKey(new byte[] { 0 }, Byte.SIZE, GENERIC_FRAME_HEADER_ID, 0);

      List<MagicKey> id3v23GenericFrameMagicKeys = new ArrayList<>();
      id3v23GenericFrameMagicKeys.add(genericFrameMagicKey);

      final DataBlockDescription genericBlockDesc = new DataBlockDescription(GENERIC_FRAME_ID, "GENERIC_ID3v23_FRAME",
         "The id3v23 GENERIC_FRAME", PhysicalDataBlockType.CONTAINER, genericFrameChildIds, null, genericFrameLocationProps,
         11, DataBlockDescription.UNKNOWN_SIZE, id3v23GenericFrameMagicKeys, null);

      descMap.put(GENERIC_FRAME_ID, genericBlockDesc);

      final List<DataBlockId> textEncChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> textEncLocationProps = new HashMap<>();
      textEncLocationProps.put(GENERIC_FRAME_PAYLOAD_ID,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      Map<Charset, byte[]> charsetsEnumerated = new HashMap<>();

      charsetsEnumerated.put(Charsets.CHARSET_ISO, new byte[] { 00 });
      charsetsEnumerated.put(Charsets.CHARSET_UTF16, new byte[] { 01 });

      List<FieldFunction> textEncFieldFunctions = new ArrayList<>();

      Set<DataBlockId> affectedTextEncIds = new HashSet<>();

      affectedTextEncIds.add(GENERIC_INFORMATION_ID);

      textEncFieldFunctions
         .add(new FieldFunction(FieldFunctionType.CHARACTER_ENCODING_OF, affectedTextEncIds, null, null));

      DataBlockDescription textEncDesc = new DataBlockDescription(GENERIC_TEXT_ENCODING_ID, "Text encoding",
         "Text encoding", PhysicalDataBlockType.FIELD, textEncChildIds, new FieldProperties<>(FieldType.ENUMERATED, Charsets.CHARSET_ISO, charsetsEnumerated, null,
            DataBlockDescription.UNKNOWN_SIZE, DataBlockDescription.UNKNOWN_SIZE, null, null, null, null, null, null,
            null, textEncFieldFunctions),
         textEncLocationProps,
         1, 1, null, GENERIC_FRAME_PAYLOAD_DATA_FIELD_ID);

      descMap.put(GENERIC_TEXT_ENCODING_ID, textEncDesc);

      final List<DataBlockId> informationChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> informationLocationProps = new HashMap<>();
      informationLocationProps.put(GENERIC_FRAME_PAYLOAD_ID,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(GENERIC_INFORMATION_ID,
         new DataBlockDescription(GENERIC_INFORMATION_ID, "Information", "Information", PhysicalDataBlockType.FIELD,
            informationChildIds, new FieldProperties<>(FieldType.STRING, null, null, null, DataBlockDescription.UNKNOWN_SIZE,
               DataBlockDescription.UNKNOWN_SIZE, '\u0000', null, null, null, null, null, null, new ArrayList<>()),
            informationLocationProps,
            1, DataBlockDescription.UNKNOWN_SIZE, null, GENERIC_FRAME_PAYLOAD_DATA_FIELD_ID));

      // 4. Generic (text information frame) infos
      List<DataBlockId> textFrameChildren = new ArrayList<>();

      textFrameChildren.add(GENERIC_TEXT_ENCODING_ID);
      textFrameChildren.add(GENERIC_INFORMATION_ID);

      final DataBlockId genericId = genericBlockDesc.getId();
      final String genericLocalId = genericId.getIdSegments().get(genericId.getIdSegments().size() - 1);

      // 5. tpe1
      addConcreteContainerDescriptionFromGeneric(descMap, genericId, genericLocalId, TPE1_LOCAL_ID, textFrameChildren);

      // 6. tit2
      addConcreteContainerDescriptionFromGeneric(descMap, genericId, genericLocalId, TIT2_LOCAL_ID, textFrameChildren);

      // 7. trck
      addConcreteContainerDescriptionFromGeneric(descMap, genericId, genericLocalId, TRCK_LOCAL_ID, textFrameChildren);

      // 8. Padding
      addPaddingContainer(descMap);
   }

   /**
    * @param descMap
    */
   private void addTag(Map<DataBlockId, DataBlockDescription> descMap) {

      // 11. id3v23 tag
      final List<DataBlockId> tagChildIds = new ArrayList<>();
      tagChildIds.add(ID3V23_HEADER_ID);
      tagChildIds.add(ID3V23_PAYLOAD_ID);

      // Magic Keys
      MagicKey id3v23MagicKey = new MagicKey(ID3V23_TAG_MAGIC_KEY_BYTES, ID3V23_TAG_MAGIC_KEY_BYTES.length * Byte.SIZE,
         ID3V23_TAG_MAGIC_KEY_STRING, ID3V23_HEADER_ID, MagicKey.NO_BACKWARD_READING, 0);

      List<MagicKey> id3v23TagMagicKeys = new ArrayList<>();
      id3v23TagMagicKeys.add(id3v23MagicKey);

      descMap.put(ID3V23_TAG_ID,
         new DataBlockDescription(ID3V23_TAG_ID, "id3v23 tag", "The id3v23 tag", PhysicalDataBlockType.CONTAINER,
            tagChildIds, null, null, 21, DataBlockDescription.UNKNOWN_SIZE, id3v23TagMagicKeys, null));
   }

   private DataFormatSpecification createSpecification(Map<DataBlockId, DataBlockDescription> descMap) {

      Set<DataBlockId> topLevelIds = new HashSet<>();
      topLevelIds.add(ID3V23_TAG_ID);

      // Byte orders and charsets
      List<ByteOrder> supportedByteOrders = new ArrayList<>();
      List<Charset> supportedCharsets = new ArrayList<>();

      // There is no ByteOrder relevant for id3v23
      supportedByteOrders.add(ByteOrder.BIG_ENDIAN);

      supportedCharsets.add(Charsets.CHARSET_ISO);
      supportedCharsets.add(Charsets.CHARSET_UTF16);

      final Set<DataBlockId> genericDataBlocks = new HashSet<>();

      genericDataBlocks.add(GENERIC_FRAME_ID);
      genericDataBlocks.add(GENERIC_FRAME_HEADER_ID);
      genericDataBlocks.add(GENERIC_FRAME_HEADER_FRAME_FLAGS_FIELD_ID);
      genericDataBlocks.add(GENERIC_FRAME_HEADER_FRAME_ID_FIELD_ID);
      genericDataBlocks.add(GENERIC_FRAME_HEADER_FRAME_SIZE_FIELD_ID);
      genericDataBlocks.add(GENERIC_FRAME_PAYLOAD_ID);
      genericDataBlocks.add(GENERIC_FRAME_PAYLOAD_DECOMPRESSED_SIZE_FIELD_ID);
      genericDataBlocks.add(GENERIC_FRAME_PAYLOAD_ENCRYPTION_METHOD_FIELD_ID);
      genericDataBlocks.add(GENERIC_FRAME_PAYLOAD_GROUP_ID_FIELD_ID);
      genericDataBlocks.add(GENERIC_FRAME_PAYLOAD_DATA_FIELD_ID);

      final Set<DataBlockId> paddingDataBlocks = new HashSet<>();

      paddingDataBlocks.add(PADDING_ID);

      final ArrayList<DataTransformationType> transformations = new ArrayList<>();

      List<DataBlockId> unsynchronisationContainers = new ArrayList<>();

      unsynchronisationContainers.add(ID3V23_TAG_ID);

      transformations.add(new DataTransformationType("Unsynchronisation", unsynchronisationContainers, true, 0, 0));

      DataFormatSpecification dummyID3v23Spec = new StandardDataFormatSpecification(ID3v23, descMap, topLevelIds,
         genericDataBlocks, paddingDataBlocks, supportedByteOrders, supportedCharsets, transformations);
      return dummyID3v23Spec;
   }

   private DataFormatSpecification createSpecification() {

      Map<DataBlockId, DataBlockDescription> descMap = new HashMap<>();

      addPayloadFrames(descMap);

      addHeader(descMap);
      addExtendedHeader(descMap);

      addPayload(descMap);

      addTag(descMap);

      return createSpecification(descMap);
   }

   private DataBlockId replaceId(DataBlockId genericId, String genericLocalId, String concreteLocalId) {

      DataBlockId concreteId = genericId;

      if (genericId.getGlobalId().contains(genericLocalId))
         concreteId = new DataBlockId(ID3v23, genericId.getGlobalId().replace(genericLocalId, concreteLocalId));

      return concreteId;
   }

}
