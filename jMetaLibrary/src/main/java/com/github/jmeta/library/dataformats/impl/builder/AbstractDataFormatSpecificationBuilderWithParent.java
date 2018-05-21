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
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link AbstractDataFormatSpecificationBuilderWithParent}
 *
 */
public abstract class AbstractDataFormatSpecificationBuilderWithParent<P extends DataFormatSpecificationBuilder, C extends DataFormatSpecificationBuilder>
   extends AbstractDataFormatSpecificationBuilder<P> {

   private final P parentBuilder;

   public AbstractDataFormatSpecificationBuilderWithParent(P parentBuilder, String localId, String name,
      String description, PhysicalDataBlockType type) {
      super(parentBuilder.getDescriptionCollector(), parentBuilder.getDataFormat(), localId, name, description, type);

      setGlobalId(parentBuilder.getGlobalId() + "." + localId);

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

   public C withStaticLengthOf(long staticByteLength) {
      setStaticLength(staticByteLength);
      return (C) this;
   }

   public C withLengthOf(long minimumByteLength, long maximumByteLength) {
      setLength(minimumByteLength, maximumByteLength);
      return (C) this;
   }

   public C withOccurrences(int minimumOccurrences, int maximumOccurrences) {
      setOccurrences(minimumOccurrences, maximumOccurrences);
      return (C) this;
   }

   public C withOverriddenId(DataBlockId overriddenId) {
      setOverriddenId(overriddenId);
      return (C) this;
   }

   public C withDescription(String name, String description) {
      setName(name);
      setDescription(description);
      return (C) this;
   }

}
