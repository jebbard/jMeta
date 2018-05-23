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
   public DataFormatSpecification createDataFormatSpecification(List<ByteOrder> supportedByteOrders,
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
   public ContainerBuilder<FieldBasedPayloadBuilder> addContainerWithFieldBasedPayload(String localId, String name,
      String description) {
      return new StandardFieldBasedPayloadContainerBuilder(this, dataFormat, localId, name, description, false);
   }

   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder> addContainerWithContainerBasedPayload(String localId,
      String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder(this, dataFormat, localId, name, description, false);
   }

   @Override
   public ContainerBuilder<FieldBasedPayloadBuilder> addGenericContainerWithFieldBasedPayload(String localId,
      String name, String description) {
      return new StandardFieldBasedPayloadContainerBuilder(this, dataFormat, localId, name, description, true);
   }

   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder> addGenericContainerWithContainerBasedPayload(String localId,
      String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder(this, dataFormat, localId, name, description, true);
   }
}
