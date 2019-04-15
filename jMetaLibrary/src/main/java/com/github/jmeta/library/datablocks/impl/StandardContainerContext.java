/**
 *
 * {@link StandardContainerContext}.java
 *
 * @author Jens Ebert
 *
 * @date 20.02.2019
 *
 */
package com.github.jmeta.library.datablocks.impl;

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.services.CountProvider;
import com.github.jmeta.library.datablocks.api.services.SizeProvider;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction;
import com.github.jmeta.library.dataformats.api.types.ByteOrderOf;
import com.github.jmeta.library.dataformats.api.types.CharacterEncodingOf;
import com.github.jmeta.library.dataformats.api.types.CountOf;
import com.github.jmeta.library.dataformats.api.types.DataBlockCrossReference;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.library.dataformats.api.types.PresenceOf;
import com.github.jmeta.library.dataformats.api.types.SizeOf;
import com.github.jmeta.library.dataformats.api.types.SummedSizeOf;
import com.github.jmeta.utility.byteutils.api.services.ByteOrders;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardContainerContext} contains contextual bits of metadata for the current container like sizes, counts,
 * byte orders etc. of fields or other data blocks within this container. Saying this, during parsing e.g. header or
 * footer fields, this context of metadata is built up, and {@link StandardContainerContext} allows parsing code to
 * later access this information.
 *
 * {@link StandardContainerContext}s are hierarchical in a way that they contain a reference to the
 * {@link StandardContainerContext} of the parent container (if any). If a size could not be determined, the delegate to
 * the parent {@link StandardContainerContext}.
 */
public class StandardContainerContext implements ContainerContext {

   private Container container;
   private final SizeProvider customSizeProvider;
   private final CountProvider customCountProvider;

   private final DataFormatSpecification spec;
   private final ContainerContext parentContainerContext;

   private final FieldFunctionStore<Long, SizeOf> sizes = new FieldFunctionStore<>(SizeOf.class);
   private final FieldFunctionStore<Long, SummedSizeOf> summedSizes = new FieldFunctionStore<>(SummedSizeOf.class);
   private final FieldFunctionStore<Long, CountOf> counts = new FieldFunctionStore<>(CountOf.class);
   private final FieldFunctionStore<Flags, PresenceOf> presences = new FieldFunctionStore<>(PresenceOf.class);
   private final FieldFunctionStore<String, ByteOrderOf> byteOrders = new FieldFunctionStore<>(ByteOrderOf.class);
   private final FieldFunctionStore<String, CharacterEncodingOf> characterEncodings = new FieldFunctionStore<>(
      CharacterEncodingOf.class);

   /**
    * {@link FieldCrossReference} combines a {@link Field} containing a field function with the {@link DataBlockId}
    * referenced by it for easier access by the {@link FieldFunctionStore} and {@link ContainerContext}.
    *
    * @param <T>
    *           The interpreted field type
    * @param <F>
    *           The concrete type of {@link AbstractFieldFunction}
    */
   public static class FieldCrossReference<T, F extends AbstractFieldFunction<T>> {

      private final DataBlockId referencedBlock;
      private final Field<T> referencingField;
      private final F referencingFieldFunction;

      /**
       * Creates a new {@link FieldCrossReference}.
       *
       * @param referencedBlock
       *           The referenced {@link DataBlockId}, must not be null
       * @param referencingField
       *           The {@link Field} referencing the referenced {@link DataBlockId}, must not be null
       * @param referencingFieldFunction
       *           The {@link AbstractFieldFunction} the field uses to reference the data block, must not be null
       */
      public FieldCrossReference(DataBlockId referencedBlock, Field<T> referencingField, F referencingFieldFunction) {
         Reject.ifNull(referencingField, "referencingField");
         Reject.ifNull(referencedBlock, "referencedBlock");
         Reject.ifNull(referencingField, "referencingField");

         this.referencedBlock = referencedBlock;
         this.referencingField = referencingField;
         this.referencingFieldFunction = referencingFieldFunction;
      }

      public DataBlockId getReferencedBlock() {
         return referencedBlock;
      }

      public Field<T> getReferencingField() {
         return referencingField;
      }

      public F getReferencingFieldFunction() {
         return referencingFieldFunction;
      }

      /**
       * @return The interpreted value of the referencing field
       */
      public T getValue() {
         try {
            return referencingField.getInterpretedValue();
         } catch (BinaryValueConversionException e) {
            throw new RuntimeException("Unexpected exception during context field conversion", e);
         }
      }
   }

   /**
    * {@link FieldFunctionStore} stores all {@link FieldCrossReference}s for a concrete type of
    * {@link AbstractFieldFunction}.
    *
    * @param <T>
    *           The interpreted field type
    * @param <F>
    *           The concrete type of {@link AbstractFieldFunction}
    */
   private class FieldFunctionStore<T, F extends AbstractFieldFunction<T>> {

      private final Class<F> fieldFunctionClass;

      private final Map<DataBlockId, Map<Integer, FieldCrossReference<T, F>>> fieldCrossRefsByTargetId = new HashMap<>();

      /**
       * Creates a new {@link FieldFunctionStore}.
       *
       * @param fieldFunctionClass
       *           The concrete class of {@link AbstractFieldFunction}, must not be null
       */
      public FieldFunctionStore(Class<F> fieldFunctionClass) {
         Reject.ifNull(fieldFunctionClass, "fieldFunctionClass");
         this.fieldFunctionClass = fieldFunctionClass;
      }

      /**
       * Adds the field function of the given field and the type represented by this {@link FieldFunctionStore}.
       *
       * @param field
       *           The field, must not be null
       */
      @SuppressWarnings("unchecked")
      public void addField(Field<?> field) {
         Reject.ifNull(field, "field");

         DataBlockDescription fieldDesc = spec.getDataBlockDescription(field.getId());

         List<?> fieldFunctionList = fieldDesc.getFieldProperties().getFieldFunctions();

         for (AbstractFieldFunction<?> fieldFunction : (List<AbstractFieldFunction<?>>) fieldFunctionList) {
            if (fieldFunction.getClass().equals(fieldFunctionClass)) {
               List<DataBlockCrossReference> refBlocks = fieldFunction.getReferencedBlocks();

               Set<DataBlockId> targetIds = refBlocks.stream().map(DataBlockCrossReference::getId)
                  .collect(Collectors.toSet());

               for (DataBlockId targetId : targetIds) {
                  if (!fieldCrossRefsByTargetId.containsKey(targetId)) {
                     fieldCrossRefsByTargetId.put(targetId, new HashMap<>());
                  }

                  fieldCrossRefsByTargetId.get(targetId).put(field.getSequenceNumber(),
                     new FieldCrossReference<>(targetId, (Field<T>) field, (F) fieldFunction));
               }
            }
         }
      }

      /**
       * Returns the {@link FieldCrossReference} for the given target id and sequence number or null if there is none.
       *
       * @param targetId
       *           The target {@link DataBlockId}, must not be null
       * @param sequenceNumber
       *           The sequence number, must not be negative
       * @return the {@link FieldCrossReference} for the given target id and sequence number or null if there is none
       */
      public FieldCrossReference<T, F> getCrossReference(DataBlockId targetId, int sequenceNumber) {
         Reject.ifNull(targetId, "targetId");
         Reject.ifNegative(sequenceNumber, "sequenceNumber");

         if (!fieldCrossRefsByTargetId.containsKey(targetId)) {
            return null;
         }

         if (!fieldCrossRefsByTargetId.get(targetId).containsKey(sequenceNumber)) {
            return null;
         }

         return fieldCrossRefsByTargetId.get(targetId).get(sequenceNumber);
      }
   }

   /**
    * Creates a new {@link StandardContainerContext}.
    *
    * @param spec
    *           The {@link DataFormatSpecification} of this context, must not be null
    * @param parentContainerContext
    *           The parent {@link ContainerContext}, might be null if this {@link ContainerContext} belongs to a
    *           top-level container
    * @param customSizeProvider
    *           A custom {@link SizeProvider} implementation to be used or null if none
    * @param customCountProvider
    *           A custom {@link CountProvider} implementation to be used or null if none
    */
   public StandardContainerContext(DataFormatSpecification spec, ContainerContext parentContainerContext,
      SizeProvider customSizeProvider, CountProvider customCountProvider) {
      Reject.ifNull(spec, "spec");

      this.spec = spec;
      this.parentContainerContext = parentContainerContext;
      this.customSizeProvider = customSizeProvider;
      this.customCountProvider = customCountProvider;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.ContainerContext#getDataFormatSpecification()
    */
   @Override
   public DataFormatSpecification getDataFormatSpecification() {
      return spec;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.ContainerContext#getParentContainerContext()
    */
   @Override
   public ContainerContext getParentContainerContext() {
      return parentContainerContext;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.ContainerContext#getContainer()
    */
   @Override
   public Container getContainer() {
      return container;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.ContainerContext#initContainer(com.github.jmeta.library.datablocks.api.types.Container)
    */
   @Override
   public void initContainer(Container container) {
      Reject.ifNull(container, "container");
      Reject.ifFalse(this.container == null, "initContainer must only be called once");

      this.container = container;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.ContainerContext#addFieldFunctions(com.github.jmeta.library.datablocks.api.types.Field)
    */
   @Override
   public void addFieldFunctions(Field<?> field) {
      Reject.ifNull(field, "field");

      sizes.addField(field);
      summedSizes.addField(field);
      counts.addField(field);
      presences.addField(field);
      byteOrders.addField(field);
      characterEncodings.addField(field);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.ContainerContext#getSizeOf(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      int)
    */
   @Override
   public long getSizeOf(DataBlockId id, int sequenceNumber) {
      Reject.ifNull(id, "id");
      Reject.ifNegative(sequenceNumber, "sequenceNumber");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      if (customSizeProvider != null) {
         long actualSize = customSizeProvider.getSizeOf(id, sequenceNumber, this);

         if (actualSize != DataBlockDescription.UNDEFINED) {
            return actualSize;
         }
      }

      if (desc.hasFixedSize()) {
         return desc.getMaximumByteLength();
      }

      FieldCrossReference<Long, SizeOf> sizeCrossRef = sizes.getCrossReference(id, sequenceNumber);

      if (sizeCrossRef == null) {
         FieldCrossReference<Long, SummedSizeOf> summedSizeCrossReference = summedSizes.getCrossReference(id,
            sequenceNumber);

         if (summedSizeCrossReference != null) {
            Set<DataBlockId> allTargetIds = summedSizeCrossReference.getReferencingFieldFunction().getReferencedBlocks()
               .stream().map(DataBlockCrossReference::getId).collect(Collectors.toSet());

            long partialSize = summedSizeCrossReference.getValue();

            for (DataBlockId siblingId : allTargetIds) {
               if (!siblingId.equals(id)) {
                  long occurrencesOf = getOccurrencesOf(siblingId);
                  if (occurrencesOf >= 1) {
                     for (int i = 0; i < occurrencesOf; i++) {
                        partialSize -= getSizeOf(siblingId, i);
                     }
                  }
               }
            }

            if (partialSize < 0) {
               partialSize = DataBlockDescription.UNDEFINED;
            }

            return partialSize;
         }

         DataBlockId matchingGenericId = spec.getMatchingGenericId(id);

         if (matchingGenericId != null && !matchingGenericId.equals(id)) {
            return getSizeOf(matchingGenericId, sequenceNumber);
         }

         if (parentContainerContext == null) {
            return DataBlockDescription.UNDEFINED;
         }

         return parentContainerContext.getSizeOf(id, sequenceNumber);
      }

      return sizeCrossRef.getValue();
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.ContainerContext#getOccurrencesOf(com.github.jmeta.library.dataformats.api.types.DataBlockId)
    */
   @Override
   public long getOccurrencesOf(DataBlockId id) {
      Reject.ifNull(id, "id");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      if (customCountProvider != null) {
         long actualOccurrences = customCountProvider.getCountOf(desc.getId(), this);

         if (actualOccurrences != DataBlockDescription.UNDEFINED) {
            return actualOccurrences;
         }
      }

      if (desc.getMaximumOccurrences() == desc.getMinimumOccurrences()) {
         return desc.getMaximumOccurrences();
      }

      if (desc.isOptional()) {
         FieldCrossReference<Flags, PresenceOf> crossReference = presences.getCrossReference(id, 0);

         if (crossReference != null) {
            PresenceOf flagFunction = crossReference.getReferencingFieldFunction();

            Flags flags = crossReference.getValue();
            if (flags.getFlagIntegerValue(flagFunction.getFlagName()) == flagFunction.getFlagValue()) {
               return 1;
            } else {
               return 0;
            }
         }
      }

      FieldCrossReference<Long, CountOf> crossReference = counts.getCrossReference(id, 0);

      if (crossReference == null) {
         if (parentContainerContext == null) {
            return DataBlockDescription.UNDEFINED;
         }

         return parentContainerContext.getOccurrencesOf(id);
      }

      return crossReference.getValue();
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.ContainerContext#getByteOrderOf(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      int)
    */
   @Override
   public ByteOrder getByteOrderOf(DataBlockId id, int sequenceNumber) {
      Reject.ifNull(id, "id");
      Reject.ifNegative(sequenceNumber, "sequenceNumber");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      if (desc.getFieldProperties().getFixedByteOrder() != null) {
         return desc.getFieldProperties().getFixedByteOrder();
      }

      FieldCrossReference<String, ByteOrderOf> crossReference = byteOrders.getCrossReference(id, sequenceNumber);

      if (crossReference == null) {

         DataBlockId currentId = id.getParentId();

         while (currentId != null) {
            FieldCrossReference<String, ByteOrderOf> parentCrossReference = byteOrders.getCrossReference(currentId, 0);

            if (parentCrossReference != null) {
               return ByteOrders.fromString(parentCrossReference.getValue());
            }

            currentId = currentId.getParentId();
         }

         if (parentContainerContext != null) {
            return parentContainerContext.getByteOrderOf(id, sequenceNumber);
         } else {
            return spec.getDefaultByteOrder();
         }
      }

      return ByteOrders.fromString(crossReference.getValue());
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.ContainerContext#getCharacterEncodingOf(com.github.jmeta.library.dataformats.api.types.DataBlockId,
    *      int)
    */
   @Override
   public Charset getCharacterEncodingOf(DataBlockId id, int sequenceNumber) {
      Reject.ifNull(id, "id");
      Reject.ifNegative(sequenceNumber, "sequenceNumber");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      if (desc.getFieldProperties().getFixedCharacterEncoding() != null) {
         return desc.getFieldProperties().getFixedCharacterEncoding();
      }

      FieldCrossReference<String, CharacterEncodingOf> crossReference = characterEncodings.getCrossReference(id,
         sequenceNumber);

      if (crossReference == null) {

         DataBlockId currentId = id.getParentId();

         while (currentId != null) {
            FieldCrossReference<String, CharacterEncodingOf> parentCrossReference = characterEncodings
               .getCrossReference(currentId, 0);

            if (parentCrossReference != null) {
               return Charset.forName(parentCrossReference.getValue());
            }

            currentId = currentId.getParentId();
         }

         if (parentContainerContext != null) {
            return parentContainerContext.getCharacterEncodingOf(id, sequenceNumber);
         } else {
            return spec.getDefaultCharacterEncoding();
         }
      }

      return Charset.forName(crossReference.getValue());
   }
}
