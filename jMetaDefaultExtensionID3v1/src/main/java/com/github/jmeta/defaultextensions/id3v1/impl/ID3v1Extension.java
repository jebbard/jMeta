/**
 *
 * {@link APEv2Extension}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.defaultextensions.id3v1.impl;

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
import com.github.jmeta.library.dataformats.api.types.ChildOrder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.DataFormat;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.LocationProperties;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.extmanager.api.services.Extension;
import com.github.jmeta.utility.extmanager.api.types.ExtensionDescription;

// REMINDER: ID3v1 and ID3v1.1 are ONE SINGLE data format in the components
// DataBlocks and DataFormats. In the component Metadata, these formats can
// be treated as different.
/**
 * {@link ID3v1Extension}
 *
 */
public class ID3v1Extension implements Extension {

   private static final byte[] ID3V1_TAG_ID = new byte[] { 'T', 'A', 'G' };
   private static final String ID3V1_TAG_ID_STRING = "TAG";
   /**
    *
    */
   public static final DataFormat ID3v1 = new DataFormat("ID3v1", new HashSet<String>(), new HashSet<String>(),
      new ArrayList<String>(), "M. Nilsson", new Date());

   /**
    * @see com.github.jmeta.utility.extmanager.api.services.Extension#getExtensionId()
    */
   @Override
   public String getExtensionId() {
      return "DEFAULT_de.je.jmeta.defext.datablocks.impl.ID3v1DataBlocksExtension";
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
         serviceProviders.add((T) new ID3v1DataBlocksService());
      }
      return serviceProviders;
   }

   private DataFormatSpecification createSpecification() {

      final int id3v1TagLength = 128;

      final char nullCharacter = '\0';

      // Data blocks
      final DataBlockId id3v1TagId = new DataBlockId(ID3v1, "id3v1");
      final DataBlockId id3v1HeaderId = new DataBlockId(ID3v1, "id3v1.header");
      final DataBlockId id3v1PayloadId = new DataBlockId(ID3v1, "id3v1.payload");
      final DataBlockId id3v1TagHeaderId = new DataBlockId(ID3v1, "id3v1.header.id");
      final DataBlockId titleId = new DataBlockId(ID3v1, "id3v1.payload.title");
      final DataBlockId artistId = new DataBlockId(ID3v1, "id3v1.payload.artist");
      final DataBlockId albumId = new DataBlockId(ID3v1, "id3v1.payload.album");
      final DataBlockId yearId = new DataBlockId(ID3v1, "id3v1.payload.year");
      final DataBlockId commentId = new DataBlockId(ID3v1, "id3v1.payload.comment");
      final DataBlockId trackIndicatorId = new DataBlockId(ID3v1, "id3v1.payload.trackIndicator");
      final DataBlockId trackId = new DataBlockId(ID3v1, "id3v1.payload.track");
      final DataBlockId genreId = new DataBlockId(ID3v1, "id3v1.payload.genre");

      Map<DataBlockId, DataBlockDescription> descMap = new HashMap<>();

      // 1. title
      final List<DataBlockId> titleChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> titleLocationProps = new HashMap<>();
      titleLocationProps.put(id3v1PayloadId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));
      descMap.put(titleId,
         new DataBlockDescription(titleId, "title", "The ID3v1 title", PhysicalDataBlockType.FIELD, titleChildIds,
            ChildOrder.SEQUENTIAL,
            new FieldProperties<>(FieldType.STRING, "" + nullCharacter, null, new byte[] { nullCharacter }, 0, 30,
               nullCharacter, null, null, null, null, null, null, new ArrayList<>()),
            titleLocationProps, 30, 30, null, null));

      // 2. artist
      final List<DataBlockId> artistChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> artistLocationProps = new HashMap<>();
      artistLocationProps.put(id3v1PayloadId,
         new LocationProperties(30, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(artistId,
         new DataBlockDescription(artistId, "artist", "The ID3v1 artist", PhysicalDataBlockType.FIELD, artistChildIds,
            ChildOrder.SEQUENTIAL,
            new FieldProperties<>(FieldType.STRING, "" + nullCharacter, null, new byte[] { nullCharacter }, 0, 30,
               nullCharacter, null, null, null, null, null, null, new ArrayList<>()),
            artistLocationProps, 30, 30, null, null));

      // 3. album
      final List<DataBlockId> albumChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> albumLocationProps = new HashMap<>();
      albumLocationProps.put(id3v1PayloadId,
         new LocationProperties(60, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(albumId,
         new DataBlockDescription(albumId, "album", "The ID3v1 album", PhysicalDataBlockType.FIELD, albumChildIds,
            ChildOrder.SEQUENTIAL,
            new FieldProperties<>(FieldType.STRING, "" + nullCharacter, null, new byte[] { nullCharacter }, 0, 30,
               nullCharacter, null, null, null, null, null, null, new ArrayList<>()),
            albumLocationProps, 30, 30, null, null));

      // 4. year
      final List<DataBlockId> yearChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> yearLocationProps = new HashMap<>();
      yearLocationProps.put(id3v1PayloadId,
         new LocationProperties(90, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(yearId,
         new DataBlockDescription(yearId, "year", "The ID3v1 year", PhysicalDataBlockType.FIELD, yearChildIds,
            ChildOrder.SEQUENTIAL,
            new FieldProperties<>(FieldType.STRING, "" + nullCharacter, null, new byte[] { nullCharacter }, 0, 4,
               nullCharacter, null, null, null, null, null, null, new ArrayList<>()),
            yearLocationProps, 4, 4, null, null));

      // 5. comment
      final List<DataBlockId> commentChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> commentLocationProps = new HashMap<>();
      commentLocationProps.put(id3v1PayloadId,
         new LocationProperties(94, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(commentId,
         new DataBlockDescription(commentId, "comment", "The ID3v1 comment", PhysicalDataBlockType.FIELD,
            commentChildIds, ChildOrder.SEQUENTIAL,
            new FieldProperties<>(FieldType.STRING, "" + nullCharacter, null, new byte[] { nullCharacter }, 0, 28,
               nullCharacter, null, null, null, null, null, null, new ArrayList<>()),
            commentLocationProps, 28, 28, null, null));

      // 6. track indicator
      final List<DataBlockId> trackIndicatorChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> trackIndicatorLocationProps = new HashMap<>();
      trackIndicatorLocationProps.put(id3v1PayloadId,
         new LocationProperties(122, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(trackIndicatorId,
         new DataBlockDescription(trackIndicatorId, "track indicator", "The ID3v1 track indicator",
            PhysicalDataBlockType.FIELD, trackIndicatorChildIds, ChildOrder.SEQUENTIAL,
            new FieldProperties<>(FieldType.UNSIGNED_WHOLE_NUMBER, (long) 0, null, new byte[] { nullCharacter }, 1, 1,
               null, null, null, null, null, null, null, new ArrayList<>()),
            trackIndicatorLocationProps, 1, 1, null, null));

      // 7. track
      final List<DataBlockId> trackChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> trackLocationProps = new HashMap<>();
      trackLocationProps.put(id3v1PayloadId,
         new LocationProperties(123, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(trackId,
         new DataBlockDescription(trackId, "track indicator", "The ID3v1 track", PhysicalDataBlockType.FIELD,
            trackChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<>(FieldType.UNSIGNED_WHOLE_NUMBER, (long) 0, null,
               new byte[] { nullCharacter }, 1, 1, null, null, null, null, null, null, null, new ArrayList<>()),
            trackLocationProps, 1, 1, null, null));

      // 8. genre
      final List<DataBlockId> genreChildIds = new ArrayList<>();

      final Map<DataBlockId, LocationProperties> genreLocationProps = new HashMap<>();
      genreLocationProps.put(id3v1PayloadId,
         new LocationProperties(124, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final Map<String, byte[]> enumeratedGenres = new HashMap<>();

      enumeratedGenres.put("Unknown", new byte[] { -1 });
      enumeratedGenres.put("Gabber", new byte[] { 99 });
      enumeratedGenres.put("Jazz", new byte[] { 2 });

      descMap.put(genreId, new DataBlockDescription(genreId, "genre", "The ID3v1 genre", PhysicalDataBlockType.FIELD,
         genreChildIds, ChildOrder.SEQUENTIAL,
         new FieldProperties<>(FieldType.ENUMERATED, "" + nullCharacter, enumeratedGenres, new byte[] { nullCharacter },
            0, 1, nullCharacter, null, null, null, null, null, null, new ArrayList<>()),
         genreLocationProps, 1, 1, null, null));

      // 9. tag id
      final List<DataBlockId> headerIdChildIds = new ArrayList<>();

      Map<String, byte[]> tagIdEnumerated = new HashMap<>();

      tagIdEnumerated.put(ID3V1_TAG_ID_STRING, ID3V1_TAG_ID);

      final Map<DataBlockId, LocationProperties> idLocationProps = new HashMap<>();
      idLocationProps.put(id3v1HeaderId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(id3v1TagHeaderId,
         new DataBlockDescription(
            id3v1TagHeaderId, "ID3v1 tag header id", "The ID3v1 tag header id", PhysicalDataBlockType.FIELD,
            headerIdChildIds, ChildOrder.SEQUENTIAL, new FieldProperties<>(FieldType.STRING, ID3V1_TAG_ID_STRING,
               tagIdEnumerated, null, 3, 3, null, null, null, null, null, null, null, new ArrayList<>()),
            idLocationProps, 3, 3, null, null));

      // 10. ID3v1 header
      final Map<DataBlockId, LocationProperties> headerLocationProps = new HashMap<>();

      headerLocationProps.put(id3v1TagId,
         new LocationProperties(0, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      final List<DataBlockId> headerChildIds = new ArrayList<>();
      headerChildIds.add(id3v1TagHeaderId);

      descMap.put(id3v1HeaderId,
         new DataBlockDescription(id3v1HeaderId, "ID3v1 tag header", "The ID3v1 tag header",
            PhysicalDataBlockType.HEADER, headerChildIds, ChildOrder.SEQUENTIAL, null, headerLocationProps, 3, 3, null,
            null));

      // 11. ID3v1 payload
      final List<DataBlockId> payloadChildIds = new ArrayList<>();

      payloadChildIds.add(titleId);
      payloadChildIds.add(artistId);
      payloadChildIds.add(albumId);
      payloadChildIds.add(yearId);
      payloadChildIds.add(commentId);
      payloadChildIds.add(trackIndicatorId);
      payloadChildIds.add(trackId);
      payloadChildIds.add(genreId);

      final Map<DataBlockId, LocationProperties> payloadLocationProps = new HashMap<>();
      payloadLocationProps.put(id3v1TagId,
         new LocationProperties(3, 1, 1, DataBlockDescription.UNKNOWN_SIZE, new ArrayList<>(), new ArrayList<>()));

      descMap.put(id3v1PayloadId,
         new DataBlockDescription(id3v1PayloadId, "payload", "The ID3v1 payload", PhysicalDataBlockType.PAYLOAD,
            payloadChildIds, ChildOrder.SEQUENTIAL, null, payloadLocationProps, 125, 125, null, null));

      // 12. ID3v1 tag

      final List<DataBlockId> tagChildIds = new ArrayList<>();
      tagChildIds.add(id3v1HeaderId);
      tagChildIds.add(id3v1PayloadId);

      final Map<DataBlockId, LocationProperties> tagLocationProps = new HashMap<>();
      tagLocationProps.put(null, new LocationProperties(-id3v1TagLength, 1, 1, DataBlockDescription.UNKNOWN_SIZE,
         new ArrayList<>(), new ArrayList<>()));

      // Magic Keys
      MagicKey id3v1MagicKey = new MagicKey(ID3V1_TAG_ID, 24, ID3V1_TAG_ID_STRING, id3v1HeaderId, -id3v1TagLength, 0);

      List<MagicKey> id3v1TagMagicKeys = new ArrayList<>();
      id3v1TagMagicKeys.add(id3v1MagicKey);

      descMap.put(id3v1TagId,
         new DataBlockDescription(id3v1TagId, "ID3v1 tag", "The ID3v1 tag", PhysicalDataBlockType.CONTAINER,
            tagChildIds, ChildOrder.SEQUENTIAL, null, tagLocationProps, 128, 128, id3v1TagMagicKeys, null));

      Set<DataBlockId> topLevelIds = new HashSet<>();
      topLevelIds.add(id3v1TagId);

      // Byte orders and charsets
      List<ByteOrder> supportedByteOrders = new ArrayList<>();
      List<Charset> supportedCharsets = new ArrayList<>();

      // There is no ByteOrder relevant for ID3v1
      supportedByteOrders.add(ByteOrder.BIG_ENDIAN);

      supportedCharsets.add(Charsets.CHARSET_ISO);
      supportedCharsets.add(Charsets.CHARSET_ASCII);
      supportedCharsets.add(Charsets.CHARSET_UTF8);

      DataFormatSpecification dummyID3v1Spec = new StandardDataFormatSpecification(ID3v1, descMap, topLevelIds,
         new HashSet<>(), new HashSet<>(), supportedByteOrders, supportedCharsets,
         new ArrayList<DataTransformationType>());

      return dummyID3v1Spec;
   }

}
