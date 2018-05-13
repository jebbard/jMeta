/**
 *
 * {@link TESTBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

import java.util.List;

import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;

/**
 * {@link TESTBuilder}
 *
 */
public class TESTBuilder {

   /**
    * Creates a new {@link TESTBuilder}.
    */
   public TESTBuilder() {
      this.createBuilder().addContainerWithContainerBasedPayload("test", "test", "test")
         .addFooter("test", "test", "test").addStringField("test", "test", "test").finishField()
         .addStringField("test2", "test", "test").finishField().finishFooter().addHeader("test", "test", "test")
         .finishHeader().getPayload().addContainerWithContainerBasedPayload("test", "test", "test").finishContainer()
         .finishContainerSequence().finishContainer().addContainerWithFieldBasedPayload("test", "test", "test")
         .getPayload().addStringField("test", "test", "test");
   }

   private ContainerSequenceBuilder<List<DataBlockDescription>> createBuilder() {
      return null;
   }
}
