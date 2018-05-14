/**
 *
 * {@link TopLevelContainerBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.dataformats.api.services.builder.ContainerBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.ContainerSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector;
import com.github.jmeta.library.dataformats.api.services.builder.FieldBasedPayloadBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link TopLevelContainerBuilder}
 *
 */
public class TopLevelContainerBuilder extends AbstractDataFormatSpecificationBuilder<List<DataBlockDescription>>
   implements ContainerSequenceBuilder<List<DataBlockDescription>> {

   private static class TopLevelDescriptionCollector implements DescriptionCollector {

      /**
       * @see com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector#getAllDescriptions()
       */
      @Override
      public List<DataBlockDescription> getAllDescriptions() {
         return overallDescriptions;
      }

      final List<DataBlockDescription> overallDescriptions = new ArrayList<>();

      @Override
      public void addDataBlockDescription(DataBlockDescription newDescription) {
         Reject.ifNull(newDescription, "newDescription");

         overallDescriptions.add(newDescription);
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
   public List<DataBlockDescription> finishContainerSequence() {
      return finish();
   }

   /**
    * Creates a new {@link TopLevelContainerBuilder}.
    * 
    * @param dataFormat
    * @param type
    */
   public TopLevelContainerBuilder(ContainerDataFormat dataFormat) {
      super(new TopLevelDescriptionCollector(), dataFormat, "dummy", "dummy", "dummy", PhysicalDataBlockType.CONTAINER);
   }

   /**
    * @see com.github.jmeta.library.dataformats.impl.builder.AbstractDataFormatSpecificationBuilder#finish()
    */
   @Override
   protected List<DataBlockDescription> finish() {
      return getDescriptionCollector().getAllDescriptions();
   }
}
