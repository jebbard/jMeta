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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractDataFormatSpecificationBuilder}
 *
 */
public abstract class AbstractDataFormatSpecificationBuilder<P extends DataFormatSpecificationBuilder>
   implements DataFormatSpecificationBuilder {

   private final List<DataBlockDescription> childDescriptions = new ArrayList<>();
   private final P parentBuilder;

   private final ContainerDataFormat dataFormat;
   private final String globalId;
   private final String name;
   private final String description;
   private final PhysicalDataBlockType type;
   private FieldProperties<?> fieldProperties;
   private DataBlockId overriddenId;
   private long maximumByteLength;
   private long minimumByteLength;
   private int minimumOccurrences;
   private int maximumOccurrences;

   public AbstractDataFormatSpecificationBuilder(ContainerDataFormat dataFormat, P parentBuilder, String localId,
      String name, String description, PhysicalDataBlockType type) {
      Reject.ifNull(parentBuilder, "parentBuilder");
      Reject.ifNull(localId, "localId");
      Reject.ifNull(type, "type");
      Reject.ifNull(dataFormat, "dataFormat");

      // TODO check local Id for validity

      this.parentBuilder = parentBuilder;
      this.dataFormat = dataFormat;
      this.name = name;
      this.description = description;
      this.type = type;
      this.globalId = parentBuilder.getGlobalId() + "." + localId;
   }

   public AbstractDataFormatSpecificationBuilder(P parentBuilder, String localId, String name, String description,
      PhysicalDataBlockType type) {
      this(parentBuilder.getDataFormat(), parentBuilder, localId, name, description, type);
   }

   @Override
   public ContainerDataFormat getDataFormat() {
      return dataFormat;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder#getGlobalId()
    */
   @Override
   public String getGlobalId() {
      return globalId;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder#addChildDescription(com.github.jmeta.library.dataformats.api.types.DataBlockDescription)
    */
   @Override
   public void addChildDescription(DataBlockDescription childDesc) {
      Reject.ifNull(childDesc, "childDesc");

      childDescriptions.add(childDesc);
   }

   protected P finish() {
      DataBlockDescription myDescription = new DataBlockDescription(new DataBlockId(dataFormat, globalId), name,
         description, type, childDescriptions.stream().map(desc -> desc.getId()).collect(Collectors.toList()),
         fieldProperties, minimumOccurrences, maximumOccurrences, minimumByteLength, maximumByteLength, overriddenId);

      parentBuilder.addChildDescription(myDescription);

      return parentBuilder;
   }
}
