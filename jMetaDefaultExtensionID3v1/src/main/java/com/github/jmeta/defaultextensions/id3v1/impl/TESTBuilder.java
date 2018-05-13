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

import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.impl.builder.TopLevelContainerBuilder;

/**
 * {@link TESTBuilder}
 *
 */
public class TESTBuilder {

   /**
    * Creates a new {@link TESTBuilder}.
    */
   public TESTBuilder() {
      this.createBuilder()
          .addContainerWithFieldBasedPayload("id3v1", "ID3v1 tag", "The ID3v1 tag")
             .withStaticLengthOf(128)
             .addHeader("header", "ID3v1 tag header", "The ID3v1 tag header")
                .addStringField("id", "ID3v1 tag header id", "The ID3v1 tag header id")
                   .asMagicKey()
                   .withDefaultValue("TAG")
                   .withStaticLengthOf(3)
                .finishField()
             .finishHeader()
             .getPayload();
   }

   private ContainerSequenceBuilder<List<DataBlockDescription>> createBuilder() {
      return new TopLevelContainerBuilder(ID3v1Extension.ID3v1);
   }
}
