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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link TopLevelContainerSequenceBuilder}
 *
 */
public class TopLevelContainerSequenceBuilder implements ContainerSequenceBuilder, DescriptionCollector {

   private final Map<DataBlockId, DataBlockDescription> overallDescriptions = new HashMap<>();
   private final Map<DataBlockId, DataBlockDescription> genericDescriptions = new HashMap<>();
   private final Map<DataBlockId, DataBlockDescription> topLevelDescriptions = new HashMap<>();
   private final ContainerDataFormat dataFormat;

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
   public Map<DataBlockId, DataBlockDescription> getGenericDescriptions() {
      return genericDescriptions;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector#getAllDescriptions()
    */
   @Override
   public Map<DataBlockId, DataBlockDescription> getAllDescriptions() {
      return overallDescriptions;
   }

   @Override
   public Map<DataBlockId, DataBlockDescription> getTopLevelDescriptions() {
      return topLevelDescriptions;
   }

   @Override
   public void addDataBlockDescription(DataBlockDescription newDescription, boolean isGeneric, boolean isTopLevel,
      boolean isDefaultNestedContainer) {
      Reject.ifNull(newDescription, "newDescription");

      overallDescriptions.put(newDescription.getId(), newDescription);

      if (isGeneric) {
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
      // TODO implement
      return null;
   }

   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder> addGenericContainerWithContainerBasedPayload(String localId,
      String name, String description) {
      // TODO implement
      return null;
   }

   public Map<DataBlockId, DataBlockDescription> finishContainerSequence() {
      return getAllDescriptions();
   }

   public Set<DataBlockId> getGenericDataBlocks() {
      return getGenericDescriptions().keySet();
   }

   public Set<DataBlockId> getTopLevelDataBlocks() {
      return getTopLevelDescriptions().keySet();
   }

   private DataBlockDescription defaultNestedContainerDesc;

   public DataBlockId getDefaultNestedContainer() {
      return defaultNestedContainerDesc.getId();
   }
}
