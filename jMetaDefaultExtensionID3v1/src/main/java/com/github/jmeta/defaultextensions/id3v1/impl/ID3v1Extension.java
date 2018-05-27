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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockService;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilderFactory;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
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

   private final DataFormatSpecificationBuilderFactory specFactory = ComponentRegistry
      .lookupService(DataFormatSpecificationBuilderFactory.class);

   private static final String ID3V1_TAG_ID_STRING = "TAG";

   /**
    *
    */
   public static final ContainerDataFormat ID3v1 = new ContainerDataFormat("ID3v1", new HashSet<String>(),
      new HashSet<String>(), new ArrayList<String>(), "M. Nilsson", new Date());

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
      return new ExtensionDescription("ID3v1", "jMeta", "1.0", null, "ID3v1 extension", null, null);
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

   public final static int id3v1TagLength = 128;

   private DataFormatSpecification createSpecification() {

      DataFormatSpecificationBuilder builder = specFactory.createDataFormatSpecificationBuilder(ID3v1Extension.ID3v1);

      final char nullCharacter = '\0';

      // @formatter:off
      return builder.addContainerWithFieldBasedPayload("id3v1", "ID3v1 tag", "The ID3v1 tag")
          .withStaticLengthOf(id3v1TagLength)
          .addHeader("header", "ID3v1 tag header", "The ID3v1 tag header")
             .withStaticLengthOf(3)
             .addStringField("id", "ID3v1 tag header id", "The ID3v1 tag header id")
                .asMagicKey().withDefaultValue(ID3V1_TAG_ID_STRING).withStaticLengthOf(3)
             .finishField()
          .finishHeader()
          .getPayload()
             .withStaticLengthOf(125)
             .addStringField("title", "title", "The ID3v1 title")
                .withTerminationCharacter(nullCharacter).withDefaultValue(""+nullCharacter).withStaticLengthOf(30)
             .finishField()
             .addStringField("artist", "artist", "The ID3v1 artist")
                .withTerminationCharacter(nullCharacter).withDefaultValue(""+nullCharacter).withStaticLengthOf(30)
             .finishField()
             .addStringField("album", "album", "The ID3v1 album")
                .withTerminationCharacter(nullCharacter).withDefaultValue(""+nullCharacter).withStaticLengthOf(30)
             .finishField()
             .addStringField("year", "year", "The ID3v1 year")
                .withTerminationCharacter(nullCharacter).withDefaultValue(""+nullCharacter).withStaticLengthOf(4)
             .finishField()
             .addStringField("comment", "comment", "The ID3v1 comment")
                .withTerminationCharacter(nullCharacter).withDefaultValue(""+nullCharacter).withStaticLengthOf(28)
             .finishField()
             .addNumericField("trackIndicator", "track indicator", "The ID3v1 track indicator")
                .withDefaultValue(0L).withStaticLengthOf(1)
             .finishField()
             .addNumericField("track", "track", "The ID3v1 track")
                .withDefaultValue(0L).withStaticLengthOf(1)
             .finishField()
             .addEnumeratedField(String.class, "genre", "genre", "The ID3v1 genre")
                .withDefaultValue(""+nullCharacter).withStaticLengthOf(1)
                .addEnumeratedValue(new byte[]{-1}, "Unknown")
                .addEnumeratedValue(new byte[]{99}, "Rock")
                .addEnumeratedValue(new byte[]{2}, "Jazz")
             .finishField()
          .finishFieldBasedPayload()
      .finishContainer()
      .withByteOrders(ByteOrder.BIG_ENDIAN)
      .withCharsets(Charsets.CHARSET_ISO, Charsets.CHARSET_ASCII, Charsets.CHARSET_UTF8)
      .build();
      // @formatter:on
   }

}
