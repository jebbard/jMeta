/**
 *
 * {@link TESTBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.defaultextensions.lyrics3v2.impl;

import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.types.BitAddress;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.FieldFunctionType;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.FlagSpecification;
import com.github.jmeta.library.dataformats.impl.builder.TopLevelContainerBuilder;

/**
 * {@link TESTBuilder}
 *
 */
public class TESTBuilder {

   private static final String OGG_MAGIC_KEY_STRING = "OggS";

   /**
    * Creates a new {@link TESTBuilder}.
    */
   public static Map<DataBlockId, DataBlockDescription> build() {
      final DataBlockId oggPageHeaderSegmentTableEntryId = new DataBlockId(Lyrics3v2Extension.LYRICS3v2, "ogg.header.segmentTableEntry");
      final DataBlockId oggSegmentId = new DataBlockId(Lyrics3v2Extension.LYRICS3v2, "ogg.payload.packetPartContainer.payload.segment");

      Set<DataBlockId> affectedBlocks = new HashSet<>();

      affectedBlocks.add(oggPageHeaderSegmentTableEntryId);
      
      Set<DataBlockId> affectedBlocks2 = new HashSet<>();

      affectedBlocks2.add(oggSegmentId);


      ContainerSequenceBuilder<List<DataBlockDescription>> builder = new TopLevelContainerBuilder(Lyrics3v2Extension.LYRICS3v2);
      
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
                   .withFieldFunction(new FieldFunction(FieldFunctionType.COUNT_OF, affectedBlocks, null, 0))
                .finishField()
                .addNumericField("segmentTableEntry", "Ogg page segment table entry", "Ogg segment table entry")
                   .withStaticLengthOf(1)
                   .withOccurrences(0, 99999)
                   .withFieldFunction(new FieldFunction(FieldFunctionType.SIZE_OF, affectedBlocks2, null, 0))
                .finishField()
             .finishHeader()
             .getPayload()
                .withLengthOf(0, DataBlockDescription.UNLIMITED)
                .addContainerWithFieldBasedPayload("packetPartContainer", "Ogg packet", "Ogg packet")
                   .withOccurrences(1, 999999)
                   .withLengthOf(1, DataBlockDescription.UNLIMITED)
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
      
      List<DataBlockDescription> topLevelContainers = builder.finishContainerSequence();

      Map<DataBlockId, DataBlockDescription> topLevelContainerMap = topLevelContainers.stream()
         .collect(Collectors.toMap(b -> b.getId(), b -> b));

      return topLevelContainerMap;
   }
   
   @Test
   public void testBuilders() {

      final DataBlockId lyrics3V2GenericFieldId = new DataBlockId(Lyrics3v2Extension.LYRICS3v2, "lyrics3v2.payload.${FIELD_ID}");
      final DataBlockId lyrics3V2GenericFieldHeaderId = new DataBlockId(Lyrics3v2Extension.LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.header");
      final DataBlockId lyrics3V2GenericFieldPayloadId = new DataBlockId(Lyrics3v2Extension.LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.payload");
      final DataBlockId lyrics3V2GenericFieldHeaderSizeId = new DataBlockId(Lyrics3v2Extension.LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.header.size");
      final DataBlockId lyrics3V2GenericFieldHeaderIdId = new DataBlockId(Lyrics3v2Extension.LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.header.id");
      final DataBlockId lyrics3V2GenericFieldPayloadDataId = new DataBlockId(Lyrics3v2Extension.LYRICS3v2,
         "lyrics3v2.payload.${FIELD_ID}.payload.value");

      Map<DataBlockId, DataBlockDescription> blockMap = TESTBuilder.build();

      Map<DataBlockId, DataBlockDescription> descMap = new Lyrics3v2Extension().getDescMap(lyrics3V2GenericFieldId, lyrics3V2GenericFieldHeaderId, lyrics3V2GenericFieldPayloadId, lyrics3V2GenericFieldHeaderSizeId, lyrics3V2GenericFieldHeaderIdId, lyrics3V2GenericFieldPayloadDataId, lyrics3V2GenericFieldHeaderId);

      Assert.assertEquals(descMap.keySet(), blockMap.keySet());

      List<DataBlockDescription> valuesLeft = new ArrayList<>(descMap.values());
      List<DataBlockDescription> valuesRight = new ArrayList<>(blockMap.values());

      valuesLeft.sort((d1, d2) -> d1.getId().getGlobalId().compareTo(d2.getId().getGlobalId()));
      valuesRight.sort((d1, d2) -> d1.getId().getGlobalId().compareTo(d2.getId().getGlobalId()));

      Assert.assertEquals(valuesLeft, valuesRight);

      Assert.assertEquals(valuesLeft.toString(), valuesRight.toString());

      Assert.assertEquals(descMap, blockMap);

      System.out.println(blockMap.equals(descMap));
   }
}
