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

import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link TopLevelContainerSequenceBuilder}
 *
 */
public class TopLevelContainerSequenceBuilder
   extends AbstractDataFormatSpecificationBuilder<Map<DataBlockId, DataBlockDescription>>
   implements ContainerSequenceBuilder<Map<DataBlockId, DataBlockDescription>> {

   private static class TopLevelDescriptionCollector implements DescriptionCollector {

      /**
       * @see com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector#getAllDescriptions()
       */
      @Override
      public Map<DataBlockId, DataBlockDescription> getAllDescriptions() {
         return overallDescriptions;
      }

      final Map<DataBlockId, DataBlockDescription> overallDescriptions = new HashMap<>();

      @Override
      public void addDataBlockDescription(DataBlockDescription newDescription) {
         Reject.ifNull(newDescription, "newDescription");

         overallDescriptions.put(newDescription.getId(), newDescription);
      }
   };

   @Override
   public ContainerBuilder<FieldBasedPayloadBuilder> addContainerWithFieldBasedPayload(String localId, String name,
      String description) {
      return new StandardFieldBasedPayloadContainerBuilder(getDescriptionCollector(), getDataFormat(), localId, name,
         description);
   }

   @Override
   public ContainerBuilder<ContainerBasedPayloadBuilder> addContainerWithContainerBasedPayload(String localId,
      String name, String description) {
      return new StandardContainerBasedPayloadContainerBuilder(getDescriptionCollector(), getDataFormat(), localId,
         name, description);
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
      return finish();
   }

   /**
    * Creates a new {@link TopLevelContainerSequenceBuilder}.
    * 
    * @param dataFormat
    * @param type
    */
   public TopLevelContainerSequenceBuilder(ContainerDataFormat dataFormat) {
      super(new TopLevelDescriptionCollector(), dataFormat, "dummy", "dummy", "dummy", PhysicalDataBlockType.CONTAINER);
   }

   /**
    * @see com.github.jmeta.library.dataformats.impl.builder.AbstractDataFormatSpecificationBuilder#finish()
    */
   @Override
   protected Map<DataBlockId, DataBlockDescription> finish() {
      return getDescriptionCollector().getAllDescriptions();
   }
}
