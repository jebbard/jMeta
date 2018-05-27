/**
 *
 * {@link StandardFieldBasedPayloadContainerBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardContainerBasedPayloadBuilder} allows to build container based payload descriptions.
 *
 * @param <P>
 *           The parent type of this builder
 */
public class StandardContainerBasedPayloadBuilder<P extends ContainerSequenceBuilder<P>> extends
   AbstractDataFormatSpecificationBuilder<ContainerBuilder<P, ContainerBasedPayloadBuilder<P>>, ContainerBasedPayloadBuilder<P>>
   implements ContainerBasedPayloadBuilder<P> {

   /**
    * Creates a new {@link StandardContainerBasedPayloadBuilder}.
    * 
    * @param parentBuilder
    *           The parent {@link DataFormatBuilder}. Required for allowing a fluent API, as it is returned by the
    *           {@link #finish()} method. Must not be null.
    * @param localId
    *           The local id of the data block. Must not be null and must not contain the
    *           {@link DataBlockId#SEGMENT_SEPARATOR}.
    * @param name
    *           The human-readable name of the data block in its specification
    * @param description
    *           The description of the data block from its specification
    */
   public StandardContainerBasedPayloadBuilder(ContainerBuilder<P, ContainerBasedPayloadBuilder<P>> parentBuilder,
      String localId, String name, String description) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD, false);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder#addContainerWithFieldBasedPayload(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder<P>, FieldBasedPayloadBuilder<ContainerBasedPayloadBuilder<P>>> addContainerWithFieldBasedPayload(
      String localId, String name, String description) {
      return new StandardFieldBasedPayloadContainerBuilder<>(this, localId, name, description, false);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder#addContainerWithContainerBasedPayload(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder<P>, ContainerBasedPayloadBuilder<ContainerBasedPayloadBuilder<P>>> addContainerWithContainerBasedPayload(
      String localId, String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder<>(this, localId, name, description, false);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder#addGenericContainerWithFieldBasedPayload(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder<P>, FieldBasedPayloadBuilder<ContainerBasedPayloadBuilder<P>>> addGenericContainerWithFieldBasedPayload(
      String localId, String name, String description) {
      return new StandardFieldBasedPayloadContainerBuilder<>(this, "${" + localId + "}", name, description, true);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder#addGenericContainerWithContainerBasedPayload(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder<P>, ContainerBasedPayloadBuilder<ContainerBasedPayloadBuilder<P>>> addGenericContainerWithContainerBasedPayload(
      String localId, String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder<>(this, "${" + localId + "}", name, description, true);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder#finishContainerBasedPayload()
    */
   @Override
   public ContainerBuilder<P, ContainerBasedPayloadBuilder<P>> finishContainerBasedPayload() {
      return finish();
   }
}
