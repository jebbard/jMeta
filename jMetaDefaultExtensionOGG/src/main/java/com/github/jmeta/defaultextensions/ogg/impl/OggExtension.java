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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilderFactory;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.impl.builder.TopLevelContainerSequenceBuilder;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link OggExtension}
 *
 */
public class OggExtension implements Extension {

   private final DataFormatSpecificationBuilderFactory specFactory = ComponentRegistry
      .lookupService(DataFormatSpecificationBuilderFactory.class);

   private static final String OGG_MAGIC_KEY_STRING = "OggS";
   /**
    *
    */
   public static final ContainerDataFormat OGG = new ContainerDataFormat("Ogg", new HashSet<String>(),
      new HashSet<String>(), new ArrayList<String>(), "", new Date());

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
      return new ExtensionDescription("Ogg", "jMeta", "1.0", null, "Ogg extension", null, null);
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
      final DataBlockId oggPageHeaderSegmentTableEntryId = new DataBlockId(OggExtension.OGG,
         "ogg.header.segmentTableEntry");
      final DataBlockId oggSegmentId = new DataBlockId(OggExtension.OGG,
         "ogg.payload.packetPartContainer.payload.segment");

      DataFormatSpecificationBuilder builder = specFactory.createDataFormatSpecificationBuilder(OggExtension.OGG);

      // @formatter:off
      builder.addContainerWithContainerBasedPayload("ogg", "Ogg page", "The ogg page")
          .withLengthOf(20, DataBlockDescription.UNLIMITED)
          .addHeader("header", "Ogg page header", "Ogg page header")
             .withLengthOf(20, DataBlockDescription.UNLIMITED)
             .addStringField("capturePattern", "Ogg page header capture pattern", "Ogg page header capture pattern")
                .withStaticLengthOf(4)
                .withDefaultValue(OGG_MAGIC_KEY_STRING)
                .asMagicKey()
             .finishField()
             .addBinaryField("streamStructureVersion", "Ogg page header stream structure version", "Ogg page header structure version")
                .withStaticLengthOf(1)
                .withDefaultValue(new byte[] { 0 })
             .finishField()
             .addBinaryField("headerTypeFlag", "Ogg page header type flag", "Ogg page header type flag")
                .withStaticLengthOf(1)
             .finishField()
             .addNumericField("absoluteGranulePos", "Ogg page absolute granule position", "Ogg page absolute granule position")
                .withStaticLengthOf(8)
             .finishField()
             .addNumericField("streamSerialNumber", "Ogg page stream serial number", "Ogg page stream serial number")
                .withStaticLengthOf(4)
             .finishField()
             .addNumericField("pageSequenceNumber", "Ogg page sequence number", "Ogg page sequence number")
                .withStaticLengthOf(4)
             .finishField()
             .addBinaryField("pageChecksum", "Ogg page checksum", "Ogg page checksum")
                .withStaticLengthOf(4)
             .finishField()
             .addNumericField("pageSegments", "Ogg page segments", "Ogg page segments")
                .withStaticLengthOf(1)
                .asCountOf(oggPageHeaderSegmentTableEntryId)
             .finishField()
             .addNumericField("segmentTableEntry", "Ogg page segment table entry", "Ogg segment table entry")
                .withStaticLengthOf(1)
                .withOccurrences(0, 99999)
                .asSizeOf(oggSegmentId)
             .finishField()
          .finishHeader()
          .getPayload()
             .withLengthOf(0, DataBlockDescription.UNLIMITED)
             .addContainerWithFieldBasedPayload("packetPartContainer", "Ogg packet", "Ogg packet")
                .withOccurrences(1, 999999)
                .withLengthOf(1, DataBlockDescription.UNLIMITED)
                .asDefaultNestedContainer()
                .getPayload()
                   .withDescription("Ogg packet", "Ogg packet")
                   .withLengthOf(1, DataBlockDescription.UNLIMITED)
                   .addBinaryField("segment", "Ogg segment", "Ogg segment")
                      .withLengthOf(0, DataBlockDescription.UNLIMITED)
                      .withOccurrences(1, 999999)
                   .finishField()
                .finishFieldBasedPayload()
             .finishContainer()
          .finishContainerBasedPayload()
       .finishContainer();
      // @formatter:on

      return builder.createDataFormatSpecification(List.of(ByteOrder.LITTLE_ENDIAN), List.of(Charsets.CHARSET_ISO));
   }

   /**
    * @param oggPageId
    * @param oggPacketPartContainerId
    * @return
    */
   public TopLevelContainerSequenceBuilder getDescMap() {
      final DataBlockId oggPageHeaderSegmentTableEntryId = new DataBlockId(OggExtension.OGG,
         "ogg.header.segmentTableEntry");
      final DataBlockId oggSegmentId = new DataBlockId(OggExtension.OGG,
         "ogg.payload.packetPartContainer.payload.segment");

      TopLevelContainerSequenceBuilder builder = new TopLevelContainerSequenceBuilder(OggExtension.OGG);

      // @formatter:off

      builder
          .addContainerWithContainerBasedPayload("ogg", "Ogg page", "The ogg page")
             .withLengthOf(20, DataBlockDescription.UNLIMITED)
             .addHeader("header", "Ogg page header", "Ogg page header")
                .withLengthOf(20, DataBlockDescription.UNLIMITED)
                .addStringField("capturePattern", "Ogg page header capture pattern", "Ogg page header capture pattern")
                   .withStaticLengthOf(4)
                   .withDefaultValue(OGG_MAGIC_KEY_STRING)
                   .asMagicKey()
                .finishField()
                .addBinaryField("streamStructureVersion", "Ogg page header stream structure version", "Ogg page header structure version")
                   .withStaticLengthOf(1)
                   .withDefaultValue(new byte[] { 0 })
                .finishField()
                .addBinaryField("headerTypeFlag", "Ogg page header type flag", "Ogg page header type flag")
                   .withStaticLengthOf(1)
                .finishField()
                .addNumericField("absoluteGranulePos", "Ogg page absolute granule position", "Ogg page absolute granule position")
                   .withStaticLengthOf(8)
                .finishField()
                .addNumericField("streamSerialNumber", "Ogg page stream serial number", "Ogg page stream serial number")
                   .withStaticLengthOf(4)
                .finishField()
                .addNumericField("pageSequenceNumber", "Ogg page sequence number", "Ogg page sequence number")
                   .withStaticLengthOf(4)
                .finishField()
                .addBinaryField("pageChecksum", "Ogg page checksum", "Ogg page checksum")
                   .withStaticLengthOf(4)
                .finishField()
                .addNumericField("pageSegments", "Ogg page segments", "Ogg page segments")
                   .withStaticLengthOf(1)
                   .asCountOf(oggPageHeaderSegmentTableEntryId)
                .finishField()
                .addNumericField("segmentTableEntry", "Ogg page segment table entry", "Ogg segment table entry")
                   .withStaticLengthOf(1)
                   .withOccurrences(0, 99999)
                   .asSizeOf(oggSegmentId)
                .finishField()
             .finishHeader()
             .getPayload()
                .withLengthOf(0, DataBlockDescription.UNLIMITED)
                .addContainerWithFieldBasedPayload("packetPartContainer", "Ogg packet", "Ogg packet")
                   .withOccurrences(1, 999999)
                   .withLengthOf(1, DataBlockDescription.UNLIMITED)
                   .asDefaultNestedContainer()
                   .getPayload()
                      .withDescription("Ogg packet", "Ogg packet")
                      .withLengthOf(1, DataBlockDescription.UNLIMITED)
                      .addBinaryField("segment", "Ogg segment", "Ogg segment")
                         .withLengthOf(0, DataBlockDescription.UNLIMITED)
                         .withOccurrences(1, 999999)
                      .finishField()
                   .finishFieldBasedPayload()
                .finishContainer()
             .finishContainerBasedPayload()
          .finishContainer();

      // @formatter:on

      return builder;
   }

}
