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
import java.util.LinkedHashMap;
import java.util.Map;

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder;
import com.github.jmeta.library.dataformats.api.types.ContainerDataFormat;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.FieldProperties;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractDataFormatSpecificationBuilder} is the base class of all {@link DataBlockDescriptionBuilder}s. It
 * allows to create {@link DataBlockDescription}s and offers a generic {@link #finish()} method. On top of this, it
 * provides default implementations for all Methods of {@link DataBlockDescriptionBuilder} and
 * {@link DataFormatBuilder}.
 *
 * @param <P>
 *           The parent type of this builder
 * @param <C>
 *           The concrete derived interface of the class implementing this
 *           {@link AbstractDataFormatSpecificationBuilder}
 */
public abstract class AbstractDataFormatSpecificationBuilder<P extends DataFormatBuilder, C extends DataBlockDescriptionBuilder<C>>
   implements DataBlockDescriptionBuilder<C>, DataFormatBuilder {

   private final P parentBuilder;
   private final DataFormatSpecificationBuilder rootBuilder;
   private boolean isDefaultNestedContainer = false;

   private final ContainerDataFormat dataFormat;
   // We use a map here to ensure we cannot add children with the same id multiple times; this is especially important
   // for the cloning to work
   private final Map<DataBlockId, DataBlockDescription> childDescriptions = new LinkedHashMap<>();
   private String globalId;
   private String name;
   private String description;
   private final PhysicalDataBlockType type;
   private FieldProperties<?> fieldProperties;
   private long minimumByteLength = 0;
   private long maximumByteLength = DataBlockDescription.UNLIMITED;
   private int minimumOccurrences = 1;
   private int maximumOccurrences = 1;
   private final boolean isGeneric;

   /**
    * Creates a new {@link AbstractDataFormatSpecificationBuilder}.
    * 
    * @param parentBuilder
    *           The parent {@link DataFormatBuilder}. Required for allowing a fluent API, as it is returned by the
    *           {@link #finish()} method. Must not be null.
    * @param localId
    *           The local id of the data block. Must not be null and must not contain the
    *           {@link DataBlockId#SEGMENT_SEPARATOR}.
    * @param name
    *           The human-readable name of the data block in its specification
    * @param description
    *           The description of the data block from its specification
    * @param type
    *           The {@link PhysicalDataBlockType} of the data block
    * @param isGeneric
    *           true if it is a generic data block, false otherwise
    */
   public AbstractDataFormatSpecificationBuilder(P parentBuilder, String localId, String name, String description,
      PhysicalDataBlockType type, boolean isGeneric) {
      Reject.ifNull(parentBuilder, "parentBuilder");
      Reject.ifNull(localId, "localId");
      Reject.ifTrue(localId.contains(DataBlockId.SEGMENT_SEPARATOR), "localId.contains(DataBlockId.SEGMENT_SEPARATOR)");
      Reject.ifNull(type, "type");

      this.rootBuilder = parentBuilder.getRootBuilder();
      this.dataFormat = parentBuilder.getDataFormat();
      this.name = name;
      this.description = description;
      this.type = type;
      this.globalId = localId;
      this.isGeneric = isGeneric;

      this.parentBuilder = parentBuilder;

      if (parentBuilder.getGlobalId() != null) {
         this.globalId = parentBuilder.getGlobalId() + DataBlockId.SEGMENT_SEPARATOR + localId;
      } else {
         this.globalId = localId;
      }
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder#getRootBuilder()
    */
   @Override
   public DataFormatSpecificationBuilder getRootBuilder() {
      return rootBuilder;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder#getDataFormat()
    */
   @Override
   public ContainerDataFormat getDataFormat() {
      return dataFormat;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder#getGlobalId()
    */
   @Override
   public String getGlobalId() {
      return globalId;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder#addChildDescription(com.github.jmeta.library.dataformats.api.types.DataBlockDescription)
    */
   @Override
   public void addChildDescription(DataBlockDescription childDesc) {
      Reject.ifNull(childDesc, "childDesc");

      childDescriptions.put(childDesc.getId(), childDesc);
   }

   public void removeChildDescription(DataBlockId childId) {
      Reject.ifNull(childId, "childId");

      childDescriptions.remove(childId);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder#withStaticLengthOf(long)
    */
   @SuppressWarnings("unchecked")
   @Override
   public C withStaticLengthOf(long staticByteLength) {
      this.minimumByteLength = staticByteLength;
      this.maximumByteLength = staticByteLength;
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder#withLengthOf(long,
    *      long)
    */
   @SuppressWarnings("unchecked")
   @Override
   public C withLengthOf(long minimumByteLength, long maximumByteLength) {
      this.minimumByteLength = minimumByteLength;
      this.maximumByteLength = maximumByteLength;
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder#withOccurrences(int,
    *      int)
    */
   @SuppressWarnings("unchecked")
   @Override
   public C withOccurrences(int minimumOccurrences, int maximumOccurrences) {
      this.minimumOccurrences = minimumOccurrences;
      this.maximumOccurrences = maximumOccurrences;
      return (C) this;
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder#withDescription(java.lang.String,
    *      java.lang.String)
    */
   @SuppressWarnings("unchecked")
   @Override
   public C withDescription(String name, String description) {
      this.name = name;
      this.description = description;
      return (C) this;
   }

   /**
    * Finishes building of this {@link AbstractDataFormatSpecificationBuilder} by creating its
    * {@link DataBlockDescription}, and adding it as child description to its parent {@link DataFormatBuilder} as well
    * as to the root builder.
    * 
    * @return The parent {@link DataFormatBuilder}.
    */
   protected P finish() {
      DataBlockDescription myDescription = new DataBlockDescription(new DataBlockId(dataFormat, globalId), name,
         description, type, new ArrayList<>(childDescriptions.values()), fieldProperties, minimumOccurrences,
         maximumOccurrences, minimumByteLength, maximumByteLength, isGeneric);

      parentBuilder.addChildDescription(myDescription);

      getRootBuilder().addDataBlockDescription(myDescription, parentBuilder.getGlobalId() == null,
         this.isDefaultNestedContainer);

      return parentBuilder;
   }

   /**
    * @return whether the data block built is generic or not
    */
   protected boolean isGeneric() {
      return isGeneric;
   }

   /**
    * Allows to set or unset the container {@link DataBlockDescription} built by this class as default nested container.
    * 
    * @param isDefaultNestedContainer
    *           true for setting it as default nested container, false otherwise
    */
   protected void setDefaultNestedContainer(boolean isDefaultNestedContainer) {
      this.isDefaultNestedContainer = isDefaultNestedContainer;
   }

   /**
    * Allows to set {@link FieldProperties} for the field {@link DataBlockDescription} built by this class.
    * 
    * @param fieldProperties
    */
   protected void setFieldProperties(FieldProperties<?> fieldProperties) {
      this.fieldProperties = fieldProperties;
   }
}
