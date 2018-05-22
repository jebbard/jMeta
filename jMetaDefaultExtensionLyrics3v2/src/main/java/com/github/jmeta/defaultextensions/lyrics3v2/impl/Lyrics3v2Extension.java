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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.StandardDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.impl.builder.TopLevelContainerSequenceBuilder;
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
      TopLevelContainerSequenceBuilder builder = getDescMap();

      return new StandardDataFormatSpecification(LYRICS3v2, builder.finishContainerSequence(),
         builder.getTopLevelDataBlocks(), builder.getGenericDataBlocks(), List.of(ByteOrder.LITTLE_ENDIAN),
         List.of(Charsets.CHARSET_ISO), builder.getDefaultNestedContainer());
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
   public TopLevelContainerSequenceBuilder getDescMap() {

      final DataBlockId lyrics3V2PayloadId = new DataBlockId(Lyrics3v2Extension.LYRICS3v2, "lyrics3v2.payload");
      final DataBlockId lyrics3V2GenericFieldId = new DataBlockId(LYRICS3v2, "lyrics3v2.payload.${FIELD_ID}");
      final DataBlockId lyrics3V2GenericFieldPayloadId = new DataBlockId(LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.payload");
      final DataBlockId lyrics3V2HeaderId = new DataBlockId(LYRICS3v2, "lyrics3v2.header");

      TopLevelContainerSequenceBuilder builder = new TopLevelContainerSequenceBuilder(Lyrics3v2Extension.LYRICS3v2);

      // @formatter:off

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
               .asSizeOf(lyrics3V2HeaderId, lyrics3V2PayloadId)
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
               .asDefaultNestedContainer()
               .addHeader("header", "Lyrics3v2 field header", "The Lyrics3v2 field header")
                  .withStaticLengthOf(LYRICS3v2_FIELD_SIZE_LENGTH + LYRICS3v2_FIELD_ID_SIZE)
                  .addStringField("id", "Lyrics3v2 field id", "Lyrics3v2 field id")
                     .withStaticLengthOf(LYRICS3v2_FIELD_ID_SIZE)
                     .asIdOf(lyrics3V2GenericFieldId)
                  .finishField()
                  .addNumericField("size", "Lyrics3v2 item value size", "Lyrics3v2 item value size")
                     .withStaticLengthOf(LYRICS3v2_FIELD_SIZE_LENGTH)
                     .asSizeOf(lyrics3V2GenericFieldPayloadId)
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

      // @formatter:on

      return builder;
   }

}
