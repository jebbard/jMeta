/**
 *
 * {@link TopLevelContainerSequenceBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.MagicKey;
import com.github.jmeta.library.dataformats.impl.builder.StandardContainerBasedPayloadContainerBuilder;
import com.github.jmeta.library.dataformats.impl.builder.StandardFieldBasedPayloadContainerBuilder;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link TopLevelContainerSequenceBuilder} builds a {@link DataFormatSpecification}.
 */
public class TopLevelContainerSequenceBuilder implements DataFormatSpecificationBuilder {

   private final Map<DataBlockId, DataBlockDescription> overallDescriptions = new HashMap<>();
   private final Map<DataBlockId, DataBlockDescription> genericDescriptions = new HashMap<>();
   private final Map<DataBlockId, DataBlockDescription> topLevelDescriptions = new HashMap<>();
   private final ContainerDataFormat dataFormat;
   private DataBlockDescription defaultNestedContainerDesc;
   private final List<ByteOrder> supportedByteOrders = new ArrayList<>();
   private final List<Charset> supportedCharacterEncodings = new ArrayList<>();

   /**
    * Creates a new {@link TopLevelContainerSequenceBuilder}.
    * 
    * @param dataFormat
    *           The {@link ContainerDataFormat} for which to build the {@link DataFormatSpecification}.
    */
   public TopLevelContainerSequenceBuilder(ContainerDataFormat dataFormat) {
      Reject.ifNull(dataFormat, "dataFormat");

      this.dataFormat = dataFormat;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder#build()
    */
   @Override
   public DataFormatSpecification build() {

      if (topLevelDescriptions.isEmpty()) {
         throw new IllegalStateException(
            "No top-level container defined for this data format - did you remember to call finishContainer?");
      }

      if (overallDescriptions.isEmpty()) {
         throw new IllegalStateException(
            "No data blocks defined for this data format - did you remember to call finish*?");
      }

      return new StandardDataFormatSpecification(dataFormat, overallDescriptions, topLevelDescriptions.keySet(),
         genericDescriptions.keySet(), supportedByteOrders, supportedCharacterEncodings,
         defaultNestedContainerDesc == null ? null : defaultNestedContainerDesc.getId());
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder#addDataBlockDescription(com.github.jmeta.library.dataformats.api.types.DataBlockDescription,
    *      boolean, boolean)
    */
   @Override
   public DataFormatSpecificationBuilder addDataBlockDescription(DataBlockDescription newDescription,
      boolean isTopLevel, boolean isDefaultNestedContainer) {
      Reject.ifNull(newDescription, "newDescription");

      overallDescriptions.put(newDescription.getId(), newDescription);

      if (newDescription.isGeneric()) {
         genericDescriptions.put(newDescription.getId(), newDescription);
      }

      if (isTopLevel) {
         topLevelDescriptions.put(newDescription.getId(), newDescription);
      }

      if (isDefaultNestedContainer) {
         if (this.defaultNestedContainerDesc != null) {
            throw new IllegalArgumentException(
               "Already have a default nested container, you may only define one default nested container. Already set default nested container: "
                  + this.defaultNestedContainerDesc);
         }

         this.defaultNestedContainerDesc = newDescription;
      }

      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder#addCustomHeaderMagicKey(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.dataformats.api.types.MagicKey)
    */
   @Override
   public DataFormatSpecificationBuilder addCustomHeaderMagicKey(DataBlockId containerId, MagicKey magicKey) {
      overallDescriptions.get(containerId).addHeaderMagicKey(magicKey);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder#addCustomFooterMagicKey(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      com.github.jmeta.library.dataformats.api.types.MagicKey)
    */
   @Override
   public DataFormatSpecificationBuilder addCustomFooterMagicKey(DataBlockId containerId, MagicKey magicKey) {
      overallDescriptions.get(containerId).addFooterMagicKey(magicKey);
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder#withByteOrders(java.nio.ByteOrder,
    *      java.nio.ByteOrder[])
    */
   @Override
   public DataFormatSpecificationBuilder withByteOrders(ByteOrder defaultByteOrder,
      ByteOrder... furtherSupportedByteOrders) {
      supportedByteOrders.add(defaultByteOrder);
      supportedByteOrders.addAll(Arrays.asList(furtherSupportedByteOrders));
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder#withCharsets(java.nio.charset.Charset,
    *      java.nio.charset.Charset[])
    */
   @Override
   public DataFormatSpecificationBuilder withCharsets(Charset defaultCharset, Charset... furtherSupportedCharsets) {
      supportedCharacterEncodings.add(defaultCharset);
      supportedCharacterEncodings.addAll(Arrays.asList(furtherSupportedCharsets));
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder#getGlobalId()
    */
   @Override
   public String getGlobalId() {
      return null;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder#addChildDescription(com.github.jmeta.library.dataformats.api.types.DataBlockDescription)
    */
   @Override
   public void addChildDescription(DataBlockDescription childDesc) {
      // Nothing to be done here
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder#getDataFormat()
    */
   @Override
   public ContainerDataFormat getDataFormat() {
      return this.dataFormat;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder#getRootBuilder()
    */
   @Override
   public DataFormatSpecificationBuilder getRootBuilder() {
      return this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder#addContainerWithFieldBasedPayload(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public ContainerBuilder<DataFormatSpecificationBuilder, FieldBasedPayloadBuilder<DataFormatSpecificationBuilder>> addContainerWithFieldBasedPayload(
      String localId, String name, String description) {
      return new StandardFieldBasedPayloadContainerBuilder<>(this, localId, name, description, false);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder#addContainerWithContainerBasedPayload(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public ContainerBuilder<DataFormatSpecificationBuilder, ContainerBasedPayloadBuilder<DataFormatSpecificationBuilder>> addContainerWithContainerBasedPayload(
      String localId, String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder<>(this, localId, name, description, false);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder#addGenericContainerWithFieldBasedPayload(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public ContainerBuilder<DataFormatSpecificationBuilder, FieldBasedPayloadBuilder<DataFormatSpecificationBuilder>> addGenericContainerWithFieldBasedPayload(
      String localId, String name, String description) {
      return new StandardFieldBasedPayloadContainerBuilder<>(this, localId, name, description, true);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder#addGenericContainerWithContainerBasedPayload(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @Override
   public ContainerBuilder<DataFormatSpecificationBuilder, ContainerBasedPayloadBuilder<DataFormatSpecificationBuilder>> addGenericContainerWithContainerBasedPayload(
      String localId, String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder<>(this, localId, name, description, true);
   }
}
