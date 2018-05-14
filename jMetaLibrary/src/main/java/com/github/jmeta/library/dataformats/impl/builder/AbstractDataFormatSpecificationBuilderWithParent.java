/**
 *
 * {@link AbstractDataFormatSpecificationBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link AbstractDataFormatSpecificationBuilderWithParent}
 *
 */
public abstract class AbstractDataFormatSpecificationBuilderWithParent<P extends DataFormatSpecificationBuilder>
   extends AbstractDataFormatSpecificationBuilder<P> {

   private final P parentBuilder;

   public AbstractDataFormatSpecificationBuilderWithParent(P parentBuilder, String localId, String name,
      String description, PhysicalDataBlockType type) {
      super(parentBuilder.getDescriptionCollector(), parentBuilder.getDataFormat(), localId, name, description, type);

      this.parentBuilder = parentBuilder;
   }

   public AbstractDataFormatSpecificationBuilderWithParent(DescriptionCollector descriptionCollector,
      ContainerDataFormat dataFormat, String localId, String name, String description, PhysicalDataBlockType type) {
      super(descriptionCollector, dataFormat, localId, name, description, type);

      this.parentBuilder = null;
   }

   protected P finish() {
      DataBlockDescription myDescription = createDescriptionFromProperties();

      if (parentBuilder != null) {
         parentBuilder.addChildDescription(myDescription);
      }
      getDescriptionCollector().addDataBlockDescription(myDescription);

      return parentBuilder;
   }
}
