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

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataBlockCrossReference;
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FooterBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.HeaderBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldFunction;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link StandardFieldBasedPayloadContainerBuilder} allows to build containers with field-based payload.
 *
 * @param <P>
 *           The parent type of this builder
 */
public class StandardFieldBasedPayloadContainerBuilder<P extends ContainerSequenceBuilder<P> & DataFormatBuilder>
   extends AbstractDataFormatSpecificationBuilder<P, ContainerBuilder<P, FieldBasedPayloadBuilder<P>>>
   implements ContainerBuilder<P, FieldBasedPayloadBuilder<P>> {

   private final FieldBasedPayloadBuilder<P> payloadBuilder = new StandardFieldBasedPayloadBuilder<>(this,
      DataBlockId.DEFAULT_PAYLOAD_ID, "payload", "The payload", isGeneric());

   /**
    * Creates a new {@link StandardFieldBasedPayloadContainerBuilder}.
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
   public StandardFieldBasedPayloadContainerBuilder(P parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, PhysicalDataBlockType.CONTAINER, isGeneric);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#getPayload()
    */
   @Override
   public FieldBasedPayloadBuilder<P> getPayload() {
      return payloadBuilder;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#addHeader(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public HeaderBuilder<ContainerBuilder<P, FieldBasedPayloadBuilder<P>>> addHeader(String localId, String name,
      String description) {
      return new StandardHeaderBuilder<>(this, localId, name, description, isGeneric());
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#addFooter(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public FooterBuilder<ContainerBuilder<P, FieldBasedPayloadBuilder<P>>> addFooter(String localId, String name,
      String description) {
      return new StandardFooterBuilder<>(this, localId, name, description, isGeneric());
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#asDefaultNestedContainer()
    */
   @Override
   public ContainerBuilder<P, FieldBasedPayloadBuilder<P>> asDefaultNestedContainer() {
      setDefaultNestedContainer(true);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#finishContainer()
    */
   @Override
   public P finishContainer() {
      P parentBuilder = super.finish();

      List<DataBlockDescription> overallDescriptions = getRootBuilder().getDataBlockDescription(createId())
         .getTransitiveChildDescriptionsOfType(PhysicalDataBlockType.FIELD);
      overallDescriptions.forEach(desc -> {
         for (FieldFunction fieldFunction : desc.getFieldProperties().getFieldFunctions()) {
            Set<DataBlockId> resolvedAffectedBlocks = fieldFunction.getAffectedBlockIds().stream().map(id -> {
               DataBlockId referencedId = getRootBuilder()
                  .getReferencedId(new DataBlockCrossReference(id.getGlobalId()));

               if (referencedId == null) {
                  return id;
               }

               return referencedId;
            }).collect(Collectors.toSet());

            fieldFunction.setAffectedBlockIds(resolvedAffectedBlocks);
         }

         desc.getFieldProperties().validateFieldProperties(desc);
      });

      return parentBuilder;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder#cloneFrom(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public ContainerBuilder<P, FieldBasedPayloadBuilder<P>> cloneFrom(DataBlockId existingContainerId) {

      ContainerBuilderCloner.cloneContainerIntoBuilder(this, existingContainerId,
         PhysicalDataBlockType.FIELD_BASED_PAYLOAD);

      return this;
   }
}
