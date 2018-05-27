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
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FlagDescription;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

/**
 * {@link MP3Extension}
 *
 */
public class MP3Extension implements Extension {

   private final DataFormatSpecificationBuilderFactory specFactory = ComponentRegistry
      .lookupService(DataFormatSpecificationBuilderFactory.class);

   private static final int FRAME_SYNC_BIT_COUNT = 11;
   private static final byte[] MP3_FRAME_SYNC = new byte[] { -1, -32 }; // 11 one bits
   private static final int MP3_HEADER_BYTE_LENGTH = 4;

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

      // Data blocks
      final DataBlockId mp3FrameId = new DataBlockId(MP3, "mp3");
      /*
       * Bit layout of the MPEG-1 Audio header Byte: -- 00 -- -- 01 -- -- 02 -- -- 03 -- Bit index in byte: 76543210
       * 76543210 76543210 76543210 Bit contents: AAAAAAAA AAABBCCD EEEEFFGH IIJJKLMM Description: A: Frame sync B: MPEG
       * audio version ID C: Layer description D: Protection bit E: Bitrate index F: Sampling rate frequency index G:
       * Padding bit H: Private bit I: Channel mode J: Mode extension K: Copyright L: Original M: Emphasis
       */
      DataBlockId crcId = new DataBlockId(MP3Extension.MP3, "mp3.crc");

      DataFormatSpecificationBuilder builder = specFactory.createDataFormatSpecificationBuilder(MP3Extension.MP3);
      final DataBlockId mp3HeaderContentId = new DataBlockId(MP3, "mp3.header.content");

      // @formatter:off
      builder.addContainerWithFieldBasedPayload("mp3", "MP3 Frame", "The MP3 Frame")
         .withLengthOf(33, 1024)
         .addHeader("header", "MP3 header", "The MP3 header").withStaticLengthOf(MP3_HEADER_BYTE_LENGTH)
            .addFlagsField("content", "MP3 header contents", "The MP3 header contents")
               .withStaticLengthOf(MP3_HEADER_BYTE_LENGTH)
               .withFlagSpecification(MP3_HEADER_BYTE_LENGTH,
                  ByteOrder.BIG_ENDIAN)
                  .withDefaultFlagBytes(new byte[MP3_HEADER_BYTE_LENGTH])
                  .addFlagDescription(new FlagDescription("Frame sync", new BitAddress(0, 0), "", FRAME_SYNC_BIT_COUNT, null))
                  .addFlagDescription(new FlagDescription("No protection bit", new BitAddress(1, 0), "", 1, null))
                  .addFlagDescription(new FlagDescription("Layer", new BitAddress(1, 1), "", 2, List.of("reserved", "Layer III", "Layer II", "Layer I")))
                  .addFlagDescription(new FlagDescription("Id", new BitAddress(1, 3), "", 2, List.of("MPEG Version 2.5", "reserved", "MPEG Version 2 (ISO/IEC 13818-3)", "MPEG Version 1 (ISO/IEC 11172-3)")))
                  .addFlagDescription(new FlagDescription("Private bit", new BitAddress(2, 0), "", 1, null))
                  .addFlagDescription(new FlagDescription("Padding bit", new BitAddress(2, 1), "", 1, null))
                  .addFlagDescription(new FlagDescription("Sampling frequency", new BitAddress(2, 2), "", 2, null))
                  .addFlagDescription(new FlagDescription("Bitrate index", new BitAddress(2, 4), "", 4, null))
                  .addFlagDescription(new FlagDescription("Emphasis bit", new BitAddress(3, 0), "", 2, null))
                  .addFlagDescription(new FlagDescription("Original or copy", new BitAddress(3, 2), "", 1, null))
                  .addFlagDescription(new FlagDescription("Copyright bit", new BitAddress(3, 3), "", 1, null))
                  .addFlagDescription(new FlagDescription("Mode extension bit", new BitAddress(3, 4), "", 2, null))
                  .addFlagDescription(new FlagDescription("Mode bit", new BitAddress(3, 6), "", 2, null))
               .finishFlagSpecification()
               .indicatesPresenceOf("No protection bit", 0, crcId)
            .finishField()
         .finishHeader()
         .addHeader("crc", "MP3 CRC", "The MP3 CRC")
            .withStaticLengthOf(2)
            .withOccurrences(0, 1)
            .addBinaryField("data", "MP3 CRC data", "The MP3 CRC data")
               .withStaticLengthOf(2)
            .finishField()
         .finishHeader()
         .getPayload()
            .withLengthOf(1, 998)
            .addBinaryField("data", "payloadData", "The MP3 payload data")
               .withLengthOf(1, 998)
            .finishField()
         .finishFieldBasedPayload()
      .finishContainer()
      .addCustomHeaderMagicKey(mp3FrameId, new MagicKey(MP3_FRAME_SYNC, FRAME_SYNC_BIT_COUNT, mp3HeaderContentId, 0))
      .withByteOrders(ByteOrder.BIG_ENDIAN)
      .withCharsets(Charsets.CHARSET_ISO);
      // @formatter:on

      return builder.build();
   }

}
