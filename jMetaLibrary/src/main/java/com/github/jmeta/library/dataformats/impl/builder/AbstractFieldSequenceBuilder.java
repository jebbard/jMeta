/**
 *
 * {@link StandardHeaderBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.StringFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;

/**
 * {@link AbstractFieldSequenceBuilder} is the base class of all builders that allow to create sequences of child
 * fields.
 *
 * @param <P>
 *           The parent type of this builder
 * @param <C>
 *           The concrete derived interface of the class implementing this
 *           {@link AbstractDataFormatSpecificationBuilder}
 */
public abstract class AbstractFieldSequenceBuilder<P extends DataBlockDescriptionBuilder<P>, C extends DataBlockDescriptionBuilder<C>>
   extends AbstractDataFormatSpecificationBuilder<P, C> implements FieldSequenceBuilder<C> {

   /**
    * Creates a new {@link AbstractFieldSequenceBuilder}.
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
   public AbstractFieldSequenceBuilder(P parentBuilder, String localId, String name, String description,
      PhysicalDataBlockType type, boolean isGeneric) {
      super(parentBuilder, localId, name, description, type, isGeneric);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder#addStringField(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   @Override
   public StringFieldBuilder<C> addStringField(String localId, String name, String description) {
      return new StandardStringFieldBuilder<>((C) this, localId, name, description, isGeneric());
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder#addNumericField(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   @Override
   public NumericFieldBuilder<C> addNumericField(String localId, String name, String description) {
      return new StandardNumericFieldBuilder<>((C) this, localId, name, description, isGeneric());
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder#addBinaryField(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   @Override
   public BinaryFieldBuilder<C> addBinaryField(String localId, String name, String description) {
      return new StandardBinaryFieldBuilder<>((C) this, localId, name, description, isGeneric());
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder#addFlagsField(java.lang.String,
    *      java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   @Override
   public FlagsFieldBuilder<C> addFlagsField(String localId, String name, String description) {
      return new StandardFlagsFieldBuilder<>((C) this, localId, name, description, isGeneric());
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder#addEnumeratedField(java.lang.Class,
    *      java.lang.String, java.lang.String, java.lang.String)
    */
   @SuppressWarnings("unchecked")
   @Override
   public <FIT> EnumeratedFieldBuilder<C, FIT> addEnumeratedField(Class<FIT> type, String localId, String name,
      String description) {
      return new StandardEnumeratedFieldBuilder<>((C) this, localId, name, description, isGeneric());
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FieldSequenceBuilder#removeField(java.lang.String)
    */
   @Override
   public FieldSequenceBuilder<C> removeField(String localId) {

      DataBlockId fieldSequenceId = new DataBlockId(getDataFormat(), getGlobalId());
      DataBlockDescription fieldSequenceDescription = getRootBuilder().getDataBlockDescription(fieldSequenceId);

      if (!fieldSequenceDescription.hasChildWithLocalId(localId)) {
         throw new IllegalArgumentException(
            "Field local id <" + localId + "> - not found as a a child id of <" + getGlobalId() + ">");
      }

      DataBlockId existingChildId = new DataBlockId(getDataFormat(), fieldSequenceId, localId);

      getRootBuilder().removeDataBlockDescription(existingChildId);

      removeChildDescription(existingChildId);

      return this;
   }
}
