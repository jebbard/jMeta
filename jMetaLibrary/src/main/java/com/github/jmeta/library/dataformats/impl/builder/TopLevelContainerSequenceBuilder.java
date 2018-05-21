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
public class TopLevelContainerSequenceBuilder
   implements ContainerSequenceBuilder<Map<DataBlockId, DataBlockDescription>> {

   private static class TopLevelDescriptionCollector implements DescriptionCollector {

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

      final Map<DataBlockId, DataBlockDescription> overallDescriptions = new HashMap<>();

      final Map<DataBlockId, DataBlockDescription> genericDescriptions = new HashMap<>();

      final Map<DataBlockId, DataBlockDescription> topLevelDescriptions = new HashMap<>();

      @Override
      public Map<DataBlockId, DataBlockDescription> getTopLevelDescriptions() {
         return topLevelDescriptions;
      }

      @Override
      public void addDataBlockDescription(DataBlockDescription newDescription, boolean isGeneric, boolean isTopLevel) {
         Reject.ifNull(newDescription, "newDescription");

         overallDescriptions.put(newDescription.getId(), newDescription);

         if (isGeneric) {
            genericDescriptions.put(newDescription.getId(), newDescription);
         }

         if (isTopLevel) {
            topLevelDescriptions.put(newDescription.getId(), newDescription);
         }
      }
   };

   @Override
   public ContainerBuilder<FieldBasedPayloadBuilder> addContainerWithFieldBasedPayload(String localId, String name,
      String description) {
      return new StandardFieldBasedPayloadContainerBuilder(getDescriptionCollector(), getDataFormat(), localId, name,
         description, false);
   }

   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder> addContainerWithContainerBasedPayload(String localId,
      String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder(getDescriptionCollector(), getDataFormat(), localId,
         name, description, false);
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

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder#finishContainerSequence()
    */
   @Override
   public Map<DataBlockId, DataBlockDescription> finishContainerSequence() {
      return collector.getAllDescriptions();
   }

   public Set<DataBlockId> getGenericDataBlocks() {
      return getDescriptionCollector().getGenericDescriptions().keySet();
   }

   public Set<DataBlockId> getTopLevelDataBlocks() {
      return getDescriptionCollector().getTopLevelDescriptions().keySet();
   }

   private final DescriptionCollector collector;
   private final ContainerDataFormat dataFormat;

   /**
    * Creates a new {@link TopLevelContainerSequenceBuilder}.
    * 
    * @param dataFormat
    * @param type
    */
   public TopLevelContainerSequenceBuilder(ContainerDataFormat dataFormat) {
      this.collector = new TopLevelDescriptionCollector();
      this.dataFormat = dataFormat;
   }

   @Override
   public String getGlobalId() {
      return null;
   }

   @Override
   public void addChildDescription(DataBlockDescription childDesc) {
   }

   @Override
   public ContainerDataFormat getDataFormat() {
      return dataFormat;
   }

   @Override
   public DescriptionCollector getDescriptionCollector() {
      return collector;
   }
}
