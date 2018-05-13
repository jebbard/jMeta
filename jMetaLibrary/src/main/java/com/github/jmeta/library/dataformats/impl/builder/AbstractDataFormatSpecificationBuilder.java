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
import com.github.jmeta.library.dataformats.api.services.builder.DescriptionCollector;
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
public abstract class AbstractDataFormatSpecificationBuilder<Result> implements DataFormatSpecificationBuilder {

   /**
    * Returns the attribute {@link #descriptionCollector}.
    * 
    * @return the attribute {@link #descriptionCollector}
    */
   public DescriptionCollector getDescriptionCollector() {
      return descriptionCollector;
   }

   /**
    * Returns the attribute {@link #childDescriptions}.
    * 
    * @return the attribute {@link #childDescriptions}
    */
   public List<DataBlockDescription> getChildDescriptions() {
      return childDescriptions;
   }

   private final DescriptionCollector descriptionCollector;
   private final List<DataBlockDescription> childDescriptions = new ArrayList<>();

   private final ContainerDataFormat dataFormat;
   private final String globalId;
   private final String name;
   private final String description;
   private final PhysicalDataBlockType type;
   private FieldProperties<?> fieldProperties;
   private DataBlockId overriddenId;
   private long minimumByteLength = 0;
   private long maximumByteLength = DataBlockDescription.UNLIMITED;
   private int minimumOccurrences = 1;
   private int maximumOccurrences = 1;

   public AbstractDataFormatSpecificationBuilder(DescriptionCollector descriptionCollector,
      ContainerDataFormat dataFormat, String localId, String name, String description, PhysicalDataBlockType type) {
      Reject.ifNull(localId, "localId");
      Reject.ifNull(type, "type");
      Reject.ifNull(dataFormat, "dataFormat");
      Reject.ifNull(descriptionCollector, "descriptionCollector");

      // TODO check local Id for validity

      this.descriptionCollector = descriptionCollector;
      this.dataFormat = dataFormat;
      this.name = name;
      this.description = description;
      this.type = type;
      this.globalId = localId;
   }

   @Override
   public ContainerDataFormat getDataFormat() {
      return dataFormat;
   }

   protected void setStaticLength(long staticByteLength) {
      setLength(staticByteLength, staticByteLength);
   }

   protected void setLength(long minimumByteLength, long maximumByteLength) {
      this.minimumByteLength = minimumByteLength;
      this.maximumByteLength = maximumByteLength;
   }

   protected void setOccurrences(int minimumOccurrences, int maximumOccurrences) {
      this.minimumOccurrences = minimumOccurrences;
      this.maximumOccurrences = maximumOccurrences;
   }

   protected void setOverriddenId(DataBlockId overriddenId) {
      this.overriddenId = overriddenId;
   }

   protected void setFieldProperties(FieldProperties<?> fieldProperties) {
      this.fieldProperties = fieldProperties;
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

   protected DataBlockDescription createDescriptionFromProperties() {
      return new DataBlockDescription(new DataBlockId(dataFormat, globalId), name, description, type,
         childDescriptions.stream().map(desc -> desc.getId()).collect(Collectors.toList()), fieldProperties,
         minimumOccurrences, maximumOccurrences, minimumByteLength, maximumByteLength, overriddenId);
   }

   protected abstract Result finish();
}
