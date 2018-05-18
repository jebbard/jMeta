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
import java.util.stream.Collectors;

import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.StandardDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.dataformats.impl.builder.TopLevelContainerBuilder;
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

      // Data blocks
      final DataBlockId lyrics3V2HeaderId = new DataBlockId(LYRICS3v2, "lyrics3v2.header");
      Map<DataBlockId, DataBlockDescription> descMap = getDescMap(lyrics3V2GenericFieldId,
         lyrics3V2GenericFieldHeaderId, lyrics3V2GenericFieldPayloadId, lyrics3V2GenericFieldHeaderSizeId,
         lyrics3V2GenericFieldHeaderIdId, lyrics3V2GenericFieldPayloadDataId, lyrics3V2HeaderId);

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

   /**
    * @param lyrics3V2GenericFieldId
    * @param lyrics3V2GenericFieldHeaderId
    * @param lyrics3V2GenericFieldPayloadId
    * @param lyrics3V2GenericFieldHeaderSizeId
    * @param lyrics3V2GenericFieldHeaderIdId
    * @param lyrics3V2GenericFieldPayloadDataId
    * @param lyrics3V2HeaderId
    * @return
    */
   public Map<DataBlockId, DataBlockDescription> getDescMap(final DataBlockId lyrics3V2GenericFieldId,
      final DataBlockId lyrics3V2GenericFieldHeaderId, final DataBlockId lyrics3V2GenericFieldPayloadId,
      final DataBlockId lyrics3V2GenericFieldHeaderSizeId, final DataBlockId lyrics3V2GenericFieldHeaderIdId,
      final DataBlockId lyrics3V2GenericFieldPayloadDataId, final DataBlockId lyrics3V2HeaderId) {
      
      final DataBlockId lyrics3V2PayloadId = new DataBlockId(Lyrics3v2Extension.LYRICS3v2, "lyrics3v2.payload");

      Set<DataBlockId> affectedBlocks = new HashSet<>();

      affectedBlocks.add(lyrics3V2HeaderId);
      affectedBlocks.add(lyrics3V2PayloadId);

      Set<DataBlockId> fieldIdAffectedBlocks = new HashSet<>();

      fieldIdAffectedBlocks.add(lyrics3V2GenericFieldId);

      Set<DataBlockId> fieldSizeAffectedBlocks = new HashSet<>();

      fieldSizeAffectedBlocks.add(lyrics3V2GenericFieldPayloadId);

      ContainerSequenceBuilder<List<DataBlockDescription>> builder = new TopLevelContainerBuilder(
         Lyrics3v2Extension.LYRICS3v2);

      builder
      .addContainerWithContainerBasedPayload("lyrics3v2", "Lyrics3v2 Tag", "The Lyrics3v2 Tag")
         .withLengthOf(4, DataBlockDescription.UNLIMITED)
         .addHeader("header", "Lyrics3v2 header", "The Lyrics3v2 header")
            .withStaticLengthOf(HEADER_BYTE_LENGTH)
            .addStringField("id", "Lyrics3v2 header id", "Lyrics3v2 header id")
               .withStaticLengthOf(HEADER_BYTE_LENGTH)
               .withDefaultValue(LYRICS3v2_MAGIC_HEADER_STRING)
               .asMagicKey()
            .finishField()
         .finishHeader()
         .addFooter("footer", "Lyrics3v2 footer", "The Lyrics3v2 footer")
            .withStaticLengthOf(FOOTER_BYTE_LENGTH)
            .addNumericField("size", "Lyrics3v2 footer tag size", "Lyrics3v2 footer tag size")
               .withStaticLengthOf(FOOTER_SIZE_FIELD_LENGTH)
               .withFieldFunction(new FieldFunction(FieldFunctionType.SIZE_OF, affectedBlocks, null, 0))
            .finishField()
            .addStringField("id", "Lyrics3v2 footer id", "Lyrics3v2 footer id")
               .withStaticLengthOf(LYRICS3v2_MAGIC_FOOTER_STRING.length())
               .withDefaultValue(LYRICS3v2_MAGIC_FOOTER_STRING)
               .asMagicKey()
            .finishField()
         .finishFooter()
         .getPayload()
            .withDescription("Lyrics3v2 payload", "The Lyrics3v2 payload")
            .withLengthOf(0, DataBlockDescription.UNLIMITED)
            .addGenericContainerWithFieldBasedPayload("FIELD_ID", "Lyrics3v2 field", "The Lyrics3v2 field")
               .withLengthOf(1, DataBlockDescription.UNLIMITED)
               .addHeader("header", "Lyrics3v2 field header", "The Lyrics3v2 field header")
                  .withStaticLengthOf(LYRICS3v2_FIELD_SIZE_LENGTH + LYRICS3v2_FIELD_ID_SIZE)
                  .addStringField("id", "Lyrics3v2 field id", "Lyrics3v2 field id")
                     .withStaticLengthOf(LYRICS3v2_FIELD_ID_SIZE)
                     .withFieldFunction(new FieldFunction(FieldFunctionType.ID_OF, fieldIdAffectedBlocks, null, 0))
                  .finishField()
                  .addNumericField("size", "Lyrics3v2 item value size", "Lyrics3v2 item value size")
                     .withStaticLengthOf(LYRICS3v2_FIELD_SIZE_LENGTH)
                     .withFieldFunction(new FieldFunction(FieldFunctionType.SIZE_OF, fieldSizeAffectedBlocks, null, 0))
                  .finishField()
               .finishHeader()
               .getPayload()
                  .withDescription("Lyrics3v2 field payload", "The Lyrics3v2 field payload")
                  .withLengthOf(0, DataBlockDescription.UNLIMITED)
                  .addStringField("value", "Lyrics3v2 field data", "Lyrics3v2 field data")
                     .withLengthOf(0, DataBlockDescription.UNLIMITED)
                  .finishField()
               .finishFieldBasedPayload()
            .finishContainer()
         .finishContainerBasedPayload()
      .finishContainer();

      List<DataBlockDescription> topLevelContainers = builder.finishContainerSequence();

      Map<DataBlockId, DataBlockDescription> topLevelContainerMap = topLevelContainers.stream()
         .collect(Collectors.toMap(b -> b.getId(), b -> b));

      return topLevelContainerMap;
   }

}
