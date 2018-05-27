/**
 *
 * {@link TopLevelContainerSequenceBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.impl.StandardDataFormatSpecification;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link TopLevelContainerSequenceBuilder}
 *
 */
public class TopLevelContainerSequenceBuilder implements DataFormatSpecificationBuilder {

   @Override
   public String getGlobalId() {
      return null;
   }

   @Override
   public void addChildDescription(DataBlockDescription childDesc) {
   }

   @Override
   public ContainerDataFormat getDataFormat() {
      return this.dataFormat;
   }

   @Override
   public DescriptionCollector getDescriptionCollector() {
      return this;
   }

   @Override
   public DataFormatSpecification build(List<ByteOrder> supportedByteOrders,
      List<Charset> supportedCharacterEncodings) {
      Reject.ifNull(supportedCharacterEncodings, "supportedCharacterEncodings");
      Reject.ifNull(supportedByteOrders, "supportedByteOrders");

      return new StandardDataFormatSpecification(dataFormat, getAllDescriptions(), topLevelDescriptions.keySet(),
         genericDescriptions.keySet(), supportedByteOrders, supportedCharacterEncodings,
         defaultNestedContainerDesc == null ? null : defaultNestedContainerDesc.getId());
   }

   private final Map<DataBlockId, DataBlockDescription> overallDescriptions = new HashMap<>();
   private final Map<DataBlockId, DataBlockDescription> genericDescriptions = new HashMap<>();
   private final Map<DataBlockId, DataBlockDescription> topLevelDescriptions = new HashMap<>();
   private final ContainerDataFormat dataFormat;

   private DataBlockDescription defaultNestedContainerDesc;

   /**
    * Creates a new {@link TopLevelContainerSequenceBuilder}.
    * 
    * @param dataFormat
    * @param type
    */
   public TopLevelContainerSequenceBuilder(ContainerDataFormat dataFormat) {
      this.dataFormat = dataFormat;
   }

   @Override
   public Map<DataBlockId, DataBlockDescription> getAllDescriptions() {
      return overallDescriptions;
   }

   @Override
   public void addDataBlockDescription(DataBlockDescription newDescription, boolean isTopLevel,
      boolean isDefaultNestedContainer) {
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
   }

   @Override
   public ContainerBuilder<DataFormatSpecificationBuilder, FieldBasedPayloadBuilder<DataFormatSpecificationBuilder>> addContainerWithFieldBasedPayload(
      String localId, String name, String description) {
      return new StandardFieldBasedPayloadContainerBuilder<>(this, localId, name, description, false);
   }

   @Override
   public ContainerBuilder<DataFormatSpecificationBuilder, ContainerBasedPayloadBuilder<DataFormatSpecificationBuilder>> addContainerWithContainerBasedPayload(
      String localId, String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder<>(this, localId, name, description, false);
   }

   @Override
   public ContainerBuilder<DataFormatSpecificationBuilder, FieldBasedPayloadBuilder<DataFormatSpecificationBuilder>> addGenericContainerWithFieldBasedPayload(
      String localId, String name, String description) {
      return new StandardFieldBasedPayloadContainerBuilder<>(this, localId, name, description, true);
   }

   @Override
   public ContainerBuilder<DataFormatSpecificationBuilder, ContainerBasedPayloadBuilder<DataFormatSpecificationBuilder>> addGenericContainerWithContainerBasedPayload(
      String localId, String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder<>(this, localId, name, description, true);
   }
}
