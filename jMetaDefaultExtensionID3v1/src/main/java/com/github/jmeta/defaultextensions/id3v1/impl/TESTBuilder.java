/**
 *
 * {@link TESTBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.defaultextensions.id3v1.impl;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.impl.builder.TopLevelContainerBuilder;

/**
 * {@link TESTBuilder}
 *
 */
public class TESTBuilder {

   /**
    * Creates a new {@link TESTBuilder}.
    */
   public static List<DataBlockDescription> build() {
      ContainerSequenceBuilder<List<DataBlockDescription>> builder = createBuilder();
      
      builder
          .addContainerWithFieldBasedPayload("id3v1", "ID3v1 tag", "The ID3v1 tag")
             .withStaticLengthOf(128)
             .addHeader("header", "ID3v1 tag header", "The ID3v1 tag header")
                .addStringField("id", "ID3v1 tag header id", "The ID3v1 tag header id")
                   .asMagicKey().withDefaultValue("TAG").withStaticLengthOf(3)
                .finishField()
             .finishHeader()
             .getPayload()
                .addStringField("title", "title", "The ID3v1 title")
                   .withTerminationCharacter('\0').withDefaultValue("\0").withStaticLengthOf(30)
                .finishField()
                .addStringField("artist", "artist", "The ID3v1 artist")
                   .withTerminationCharacter('\0').withDefaultValue("\0").withStaticLengthOf(30)
                .finishField()
                .addStringField("album", "album", "The ID3v1 album")
                   .withTerminationCharacter('\0').withDefaultValue("\0").withStaticLengthOf(30)
                .finishField()
                .addStringField("year", "year", "The ID3v1 year")
                   .withTerminationCharacter('\0').withDefaultValue("\0").withStaticLengthOf(4)
                .finishField()
                .addStringField("comment", "comment", "The ID3v1 comment")
                   .withTerminationCharacter('\0').withDefaultValue("\0").withStaticLengthOf(28)
                .finishField()
                .addNumericField("trackIndicator", "track indicator", "The ID3v1 track indicator")
                   .withDefaultValue(0L).withStaticLengthOf(1)
                .finishField()
                .addNumericField("track", "track", "The ID3v1 track")
                   .withDefaultValue(0L).withStaticLengthOf(1)
                .finishField()
                .addEnumeratedField(String.class, "genre", "genre", "The ID3v1 genre")
                   .withDefaultValue("\0").withStaticLengthOf(1)
                   .addEnumeratedValue(new byte[]{-1}, "Unknown")
                   .addEnumeratedValue(new byte[]{99}, "Rock")
                   .addEnumeratedValue(new byte[]{2}, "Jazz")
                .finishField()
             .finishFieldBasedPayload()
          .finishContainer();
      
      List<DataBlockDescription> topLevelContainers = builder.finishContainerSequence();
      
      return topLevelContainers;
   }
   
   @Test
   public void testBuilders() {
      final DataBlockId id3v1TagId = new DataBlockId(ID3v1Extension.ID3v1, "id3v1");

      List<DataBlockDescription> blocks = TESTBuilder.build();

      Map<DataBlockId, DataBlockDescription> blockMap = blocks.stream()
         .collect(Collectors.toMap(b -> b.getId(), b -> b));
      
      Map<DataBlockId, DataBlockDescription> descMap = new ID3v1Extension().getDescMap(id3v1TagId);

      Assert.assertEquals(descMap.toString(), blockMap.toString());

      Assert.assertEquals(descMap, blockMap);

      System.out.println(blockMap.equals(descMap));

   }
   

   private static ContainerSequenceBuilder<List<DataBlockDescription>> createBuilder() {
      return new TopLevelContainerBuilder(ID3v1Extension.ID3v1);
   }
}
