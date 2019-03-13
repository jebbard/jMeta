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
import com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.PresenceOf;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link MP3Extension}
 *
 */
public class MP3Extension implements Extension {

   static final String HEADER_FLAGS_MODE_BIT = "Mode bit";
   static final String HEADER_FLAGS_MODE_EXTENSION_BIT = "Mode extension bit";
   static final String HEADER_FLAGS_COPYRIGHT_BIT = "Copyright bit";
   static final String HEADER_FLAGS_ORIGINAL_OR_COPY = "Original or copy";
   static final String HEADER_FLAGS_EMPHASIS_BIT = "Emphasis bit";
   static final String HEADER_FLAGS_BITRATE_INDEX = "Bitrate index";
   static final String HEADER_FLAGS_SAMPLING_FREQUENCY = "Sampling frequency";
   static final String HEADER_FLAGS_PADDING_BIT = "Padding bit";
   static final String HEADER_FLAGS_PRIVATE_BIT = "Private bit";
   static final String HEADER_FLAGS_ID = "Id";
   static final String HEADER_FLAGS_LAYER = "Layer";
   static final String HEADER_FLAGS_NO_PROTECTION_BIT = "No protection bit";
   static final String HEADER_FLAGS_FRAME_SYNC = "Frame sync";

   private final DataFormatSpecificationBuilderFactory specFactory = ComponentRegistry
      .lookupService(DataFormatSpecificationBuilderFactory.class);

   static final DataBlockCrossReference REF_PAYLOAD = new DataBlockCrossReference("Payload");

   /**
    *
    */
   public static final ContainerDataFormat MP3 = new ContainerDataFormat("MP3", new HashSet<String>(),
      new HashSet<String>(), new ArrayList<String>(), "", new Date());

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
      final int mp3HeaderByteLength = 4;

      // Data blocks
      /*
       * Bit layout of the MPEG-1 Audio header Byte: -- 00 -- -- 01 -- -- 02 -- -- 03 -- Bit index in byte: 76543210
       * 76543210 76543210 76543210 Bit contents: AAAAAAAA AAABBCCD EEEEFFGH IIJJKLMM Description: A: Frame sync B: MPEG
       * audio version ID C: Layer description D: Protection bit E: Bitrate index F: Sampling rate frequency index G:
       * Padding bit H: Private bit I: Channel mode J: Mode extension K: Copyright L: Original M: Emphasis
       */
      DataFormatSpecificationBuilder builder = specFactory.createDataFormatSpecificationBuilder(MP3Extension.MP3);

      // @formatter:off
      DataBlockCrossReference crcReference = new DataBlockCrossReference("CRC");
      builder.addContainerWithFieldBasedPayload("mp3", "MP3 Frame", "The MP3 Frame")
         .addHeader("header", "MP3 header", "The MP3 header")
            .addFlagsField("content", "MP3 header contents", "The MP3 header contents")
               .withStaticLengthOf(mp3HeaderByteLength)
               .withFlagSpecification(mp3HeaderByteLength,
                  ByteOrder.BIG_ENDIAN)
                  .withDefaultFlagBytes(new byte[] { -1, -32, 0, 0 }) // 11 one bits
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_FRAME_SYNC, new BitAddress(0, 0), "", 11, null))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_NO_PROTECTION_BIT, new BitAddress(1, 0), "", 1, null))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_LAYER, new BitAddress(1, 1), "", 2, List.of("reserved", "Layer III", "Layer II", "Layer I")))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_ID, new BitAddress(1, 3), "", 2, List.of("MPEG Version 2.5", "reserved", "MPEG Version 2 (ISO/IEC 13818-3)", "MPEG Version 1 (ISO/IEC 11172-3)")))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_PRIVATE_BIT, new BitAddress(2, 0), "", 1, null))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_PADDING_BIT, new BitAddress(2, 1), "", 1, null))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_SAMPLING_FREQUENCY, new BitAddress(2, 2), "", 2, null))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_BITRATE_INDEX, new BitAddress(2, 4), "", 4, null))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_EMPHASIS_BIT, new BitAddress(3, 0), "", 2, null))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_ORIGINAL_OR_COPY, new BitAddress(3, 2), "", 1, null))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_COPYRIGHT_BIT, new BitAddress(3, 3), "", 1, null))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_MODE_EXTENSION_BIT, new BitAddress(3, 4), "", 2, null))
                  .addFlagDescription(new FlagDescription(HEADER_FLAGS_MODE_BIT, new BitAddress(3, 6), "", 2, null))
               .finishFlagSpecification()
               .withFieldFunction(new PresenceOf(crcReference, HEADER_FLAGS_NO_PROTECTION_BIT, 0))
               .asMagicKeyWithOddBitLength(11)
            .finishField()
         .finishHeader()
         .addHeader("crc", "MP3 CRC", "The MP3 CRC")
            .referencedAs(crcReference)
            .withOccurrences(0, 1)
            .addBinaryField("data", "MP3 CRC data", "The MP3 CRC data")
               .withStaticLengthOf(2)
            .finishField()
         .finishHeader()
         .getPayload()
            .referencedAs(REF_PAYLOAD)
            .addBinaryField("data", "payloadData", "The MP3 payload data")
               .withLengthOf(1, 998)
            .finishField()
         .finishFieldBasedPayload()
      .finishContainer()
      .withByteOrders(ByteOrder.BIG_ENDIAN)
      .withCharsets(Charsets.CHARSET_ISO);
      // @formatter:on

      return builder.build();
   }

}
