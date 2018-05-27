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
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardContainerBasedPayloadContainerBuilder} allows to build containers with container-based payload.
 *
 * @param <P>
 *           The parent type of this builder
 */
public class StandardContainerBasedPayloadContainerBuilder<P extends ContainerSequenceBuilder<P> & DataFormatBuilder>
   extends AbstractDataFormatSpecificationBuilder<P, ContainerBuilder<P, ContainerBasedPayloadBuilder<P>>>
   implements ContainerBuilder<P, ContainerBasedPayloadBuilder<P>> {

   private final ContainerBasedPayloadBuilder<P> payloadBuilder = new StandardContainerBasedPayloadBuilder<>(this,
      "payload", "payload", "The payload");

   /**
    * Creates a new {@link StandardContainerBasedPayloadContainerBuilder}.
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
    * @param isGeneric
    *           true if it is a generic data block, false otherwise
    */
   public StandardContainerBasedPayloadContainerBuilder(P parentBuilder, String localId, String name,
      String description, boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER, isGeneric);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#getPayload()
    */
   @Override
   public ContainerBasedPayloadBuilder<P> getPayload() {
      return payloadBuilder;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#addHeader(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public HeaderBuilder<ContainerBuilder<P, ContainerBasedPayloadBuilder<P>>> addHeader(String localId, String name,
      String description) {
      return new StandardHeaderBuilder<>(this, localId, name, description, isGeneric());
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#addFooter(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public FooterBuilder<ContainerBuilder<P, ContainerBasedPayloadBuilder<P>>> addFooter(String localId, String name,
      String description) {
      return new StandardFooterBuilder<>(this, localId, name, description, isGeneric());
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#asDefaultNestedContainer()
    */
   @Override
   public ContainerBuilder<P, ContainerBasedPayloadBuilder<P>> asDefaultNestedContainer() {
      setDefaultNestedContainer(true);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#finishContainer()
    */
   @Override
   public P finishContainer() {
      return super.finish();
   }
}
