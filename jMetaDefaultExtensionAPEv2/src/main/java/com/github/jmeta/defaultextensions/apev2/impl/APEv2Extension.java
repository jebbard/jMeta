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
 * {@link APEv2Extension}
 *
 */
public class APEv2Extension implements Extension {

   private final DataFormatSpecificationBuilderFactory specFactory = ComponentRegistry
      .lookupService(DataFormatSpecificationBuilderFactory.class);

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
      final DataBlockId apeV2HeaderId = new DataBlockId(APEv2Extension.APEv2, "apev2.header");
      final DataBlockId apeV2PayloadId = new DataBlockId(APEv2Extension.APEv2, "apev2.payload");
      final DataBlockId apeV2FooterId = new DataBlockId(APEv2Extension.APEv2, "apev2.footer");

      final DataBlockId apeV2GenericItemId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}");
      final DataBlockId apeV2GenericItemPayloadId = new DataBlockId(APEv2, "apev2.payload.${ITEM_ID}.payload");

      FlagDescription readOnlyFlag = new FlagDescription("Read-only", new BitAddress(0, 0), "", 1, null);
      FlagDescription itemTypeFlag = new FlagDescription("Item type", new BitAddress(0, 1), "", 2, null);
      FlagDescription thisIsTheHeaderFlag = new FlagDescription("This is the header", new BitAddress(3, 5), "", 1,
         null);
      FlagDescription tagContainsNoFooterFlag = new FlagDescription("Tag contains no footer", new BitAddress(3, 6), "",
         1, null);
      FlagDescription tagContainsHeaderFlag = new FlagDescription("Tag contains header", new BitAddress(3, 7), "", 1,
         null);

      DataFormatSpecificationBuilder builder = specFactory.createDataFormatSpecificationBuilder(APEv2Extension.APEv2);

      // @formatter:off
      return builder.addContainerWithContainerBasedPayload("apev2", "APEv2 Tag", "The APEv2 Tag")
         .withLengthOf(4, DataBlockDescription.UNDEFINED)
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
               .asSizeOf(apeV2PayloadId, apeV2FooterId)
            .finishField()
            .addNumericField("itemCount", "APEv2 header item count", "APEv2 header item count")
               .withStaticLengthOf(4)
               .asCountOf(apeV2GenericItemId)
            .finishField()
            .addFlagsField("tagFlags", "APEv2 header tag flags", "APEv2 header tag flags")
               .withStaticLengthOf(4)
               .withFlagSpecification(4, ByteOrder.LITTLE_ENDIAN)
                  .withDefaultFlagBytes(new byte[4])
                  .addFlagDescription(readOnlyFlag)
                  .addFlagDescription(itemTypeFlag)
                  .addFlagDescription(thisIsTheHeaderFlag)
                  .addFlagDescription(tagContainsNoFooterFlag)
                  .addFlagDescription(tagContainsHeaderFlag)
               .finishFlagSpecification()
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
               .asSizeOf(apeV2PayloadId, apeV2HeaderId)
            .finishField()
            .addNumericField("itemCount", "APEv2 footer item count", "APEv2 footer item count")
               .withStaticLengthOf(4)
               .asCountOf(apeV2GenericItemId)
            .finishField()
            .addFlagsField("tagFlags", "APEv2 footer tag flags", "APEv2 footer tag flags")
               .withStaticLengthOf(4)
               .withFlagSpecification(4, ByteOrder.LITTLE_ENDIAN)
               .withDefaultFlagBytes(new byte[4])
                  .addFlagDescription(readOnlyFlag)
                  .addFlagDescription(itemTypeFlag)
                  .addFlagDescription(thisIsTheHeaderFlag)
                  .addFlagDescription(tagContainsNoFooterFlag)
                  .addFlagDescription(tagContainsHeaderFlag)
               .finishFlagSpecification()
            .finishField()
            .addBinaryField("reserved", "APEv2 footer reserved", "APEv2 footer reserved")
               .withStaticLengthOf(8)
               .withDefaultValue(new byte[] { 0, 0, 0, 0, 0, 0, 0, 0 })
            .finishField()
         .finishFooter()
         .getPayload()
            .withDescription("APEv2 payload", "The APEv2 payload")
            .withLengthOf(0, DataBlockDescription.UNDEFINED)
            .addGenericContainerWithFieldBasedPayload("ITEM_ID", "APEv2 item", "The APEv2 item")
               .withLengthOf(1, DataBlockDescription.UNDEFINED)
               .asDefaultNestedContainer()
               .addHeader("header", "APEv2 item header", "The APEv2 item header")
                  .withLengthOf(APEv2_MIN_ITEM_HEADER_LENGTH, DataBlockDescription.UNDEFINED)
                  .addNumericField("size", "APEv2 item value size", "APEv2 item value size")
                     .withStaticLengthOf(4)
                     .asSizeOf(apeV2GenericItemPayloadId)
                  .finishField()
                  .addFlagsField("flags", "APEv2 item flags", "APEv2 item flags")
                     .withStaticLengthOf(4)
                     .withFlagSpecification(4, ByteOrder.LITTLE_ENDIAN)
                        .withDefaultFlagBytes(new byte[4])
                        .addFlagDescription(readOnlyFlag)
                        .addFlagDescription(itemTypeFlag)
                     .finishFlagSpecification()
                  .finishField()
                  .addStringField("key", "APEv2 item key", "APEv2 item key")
                     .withLengthOf(2, 255)
                     .asIdOf(apeV2GenericItemId)
                     .withTerminationCharacter('\u0000')
                  .finishField()
               .finishHeader()
               .getPayload()
                  .withDescription("APEv2 item payload", "The APEv2 item payload")
                  .withLengthOf(0, DataBlockDescription.UNDEFINED)
                  .addStringField("value", "APEv2 item value", "APEv2 item value")
                     .withLengthOf(0, DataBlockDescription.UNDEFINED)
                  .finishField()
               .finishFieldBasedPayload()
            .finishContainer()
         .finishContainerBasedPayload()
      .finishContainer()
      .withByteOrders(ByteOrder.LITTLE_ENDIAN)
      .withCharsets(Charsets.CHARSET_ISO)
      .build();
      // @formatter:on
   }

}
