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
public abstract class AbstractDataFormatSpecificationBuilder<P extends DataFormatSpecificationBuilder, C extends DataFormatSpecificationBuilder>
   implements DataFormatSpecificationBuilder {

   private final P parentBuilder;

   public AbstractDataFormatSpecificationBuilder(P parentBuilder, String localId, String name, String description,
      PhysicalDataBlockType type, boolean isGeneric) {
      this(parentBuilder, parentBuilder.getDescriptionCollector(), parentBuilder.getDataFormat(), localId, name,
         description, type, isGeneric);

      setGlobalId(parentBuilder.getGlobalId() + "." + localId);
   }

   public AbstractDataFormatSpecificationBuilder(P parentBuilder, DescriptionCollector descriptionCollector,
      ContainerDataFormat dataFormat, String localId, String name, String description, PhysicalDataBlockType type,
      boolean isGeneric) {
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
      this.isGeneric = isGeneric;

      this.parentBuilder = parentBuilder;
   }

   protected P finish() {
      DataBlockDescription myDescription = createDescriptionFromProperties();

      if (parentBuilder != null) {
         parentBuilder.addChildDescription(myDescription);
      }
      getDescriptionCollector().addDataBlockDescription(myDescription, isGeneric(), parentBuilder == null);

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

   public DescriptionCollector getDescriptionCollector() {
      return descriptionCollector;
   }

   private final DescriptionCollector descriptionCollector;
   private final List<DataBlockDescription> childDescriptions = new ArrayList<>();

   private final ContainerDataFormat dataFormat;
   private String globalId;
   private String name;
   private String description;
   private final PhysicalDataBlockType type;
   private FieldProperties<?> fieldProperties;
   private DataBlockId overriddenId;
   private long minimumByteLength = 0;
   private long maximumByteLength = DataBlockDescription.UNLIMITED;
   private int minimumOccurrences = 1;
   private int maximumOccurrences = 1;

   private final boolean isGeneric;

   /**
    * Sets the attribute {@link #name}.
    *
    * @param new
    *           vakue for attribute {@link #name name}.
    */
   protected void setName(String name) {
      this.name = name;
   }

   protected boolean isGeneric() {
      return isGeneric;
   }

   /**
    * Sets the attribute {@link #description}.
    *
    * @param new
    *           vakue for attribute {@link #description description}.
    */
   protected void setDescription(String description) {
      this.description = description;
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

   protected void setGlobalId(String globalId) {
      this.globalId = globalId;
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
}
