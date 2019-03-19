/**
 *
 * {@link ContainerContext}.java
 *
 * @author Jens Ebert
 *
 * @date 20.02.2019
 *
 */
package com.github.jmeta.library.datablocks.api.types;

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
 * {@link ContainerContext} contains contextual bits of metadata for the current container like sizes, counts, byte
 * orders etc. of fields or other data blocks within this container. Saying this, during parsing e.g. header or footer
 * fields, this context of metadata is built up, and {@link ContainerContext} allows parsing code to later access this
 * information.
 *
 * {@link ContainerContext}s are hierarchical in a way that they contain a reference to the {@link ContainerContext} of
 * the parent container (if any). If a size could not be determined, the delegate to the parent
 * {@link ContainerContext}.
 */
public class ContainerContext {

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
               Set<DataBlockCrossReference> refBlocks = fieldFunction.getReferencedBlocks();

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
    * Creates a new {@link ContainerContext}.
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
   public ContainerContext(DataFormatSpecification spec, ContainerContext parentContainerContext,
      SizeProvider customSizeProvider, CountProvider customCountProvider) {
      Reject.ifNull(spec, "spec");

      this.spec = spec;
      this.parentContainerContext = parentContainerContext;
      this.customSizeProvider = customSizeProvider;
      this.customCountProvider = customCountProvider;
   }

   /**
    * @return the {@link DataFormatSpecification} the container of this context belongs to
    */
   public DataFormatSpecification getDataFormatSpecification() {
      return spec;
   }

   /**
    * @return the parent {@link ContainerContext} or null if this is a top-level {@link ContainerContext}
    */
   public ContainerContext getParentContainerContext() {
      return parentContainerContext;
   }

   /**
    * @return the {@link Container} this {@link ContainerContext} belongs to
    */
   public Container getContainer() {
      return container;
   }

   /**
    * Initializes the {@link Container} this {@link ContainerContext} belongs to. This method must only be called once
    * after creating the {@link ContainerContext}.
    *
    * @param container
    *           The {@link Container} this context belongs to, must not be null
    */
   public void initContainer(Container container) {
      Reject.ifNull(container, "container");
      Reject.ifFalse(this.container == null, "initContainer must only be called once");

      this.container = container;
   }

   /**
    * Adds all {@link FieldFunction}s of the given field to this {@link ContainerContext}. The concrete values can be
    * later retrieved using the getters.
    *
    * @param field
    *           The {@link Field} to add, must not be null
    */
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
    * Determines the size of the given {@link DataBlockId} with the given sequence number within the current
    * {@link Container}. The approach is as follows:
    * <ul>
    * <li>If there is a custom {@link SizeProvider} returning a size that is not equal to
    * {@link DataBlockDescription#UNDEFINED}, this size is returned</li>
    * <li>Otherwise if the data block has fixed size according to its specification, this size is returned</li>
    * <li>Otherwise the size of field functions (single block size) are searched for a field that contains the size of
    * the data block within this {@link ContainerContext}</li>
    * <li>Otherwise the size of field functions (multiple block size) are searched for a field that contains the size of
    * the data block within this {@link ContainerContext}</li>
    * <li>If there is no single block size function found, it is checked if there is one for the matching generic id of
    * the target data block</li>
    * <li>If there is none, the same is done hierarchically for the parent {@link ContainerContext}</li>
    * <li>If there is none in the parent container context, the given remaining parent byte count is returned</li>
    * </ul>
    *
    * @param id
    *           The {@link DataBlockId} of the data block, must not be null
    * @param sequenceNumber
    *           The sequence number of the data block, must not be negative
    * @param remainingDirectParentByteCount
    *           The number of remaining direct parent bytes, if known; must either be
    *           {@link DataBlockDescription#UNDEFINED} or strictly positive
    * @return The size of the data block or {@link DataBlockDescription#UNDEFINED} if none is available
    */
   public long getSizeOf(DataBlockId id, int sequenceNumber, long remainingDirectParentByteCount) {
      Reject.ifNull(id, "id");
      Reject.ifNegative(sequenceNumber, "sequenceNumber");
      Reject.ifTrue(
         remainingDirectParentByteCount != DataBlockDescription.UNDEFINED && remainingDirectParentByteCount < 0,
         "remainingDirectParentByteCount != DataBlockDescription.UNDEFINED && remainingDirectParentByteCount < 0");

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
                        partialSize -= getSizeOf(siblingId, i, remainingDirectParentByteCount);
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
            return getSizeOf(matchingGenericId, sequenceNumber, remainingDirectParentByteCount);
         }

         if (parentContainerContext == null) {
            return remainingDirectParentByteCount;
         }

         return parentContainerContext.getSizeOf(id, sequenceNumber, remainingDirectParentByteCount);
      }

      return sizeCrossRef.getValue();
   }

   /**
    * Determines the number of occurrences of the given {@link DataBlockId} within the current {@link Container}. The
    * approach is as follows:
    * <ul>
    * <li>If there is a custom {@link CountProvider} returning a size that is not equal to
    * {@link DataBlockDescription#UNDEFINED}, this size is returned</li>
    * <li>Otherwise if the data block has fixed number of occurrences according to its specification, this number is
    * returned</li>
    * <li>Otherwise if the data block is optional, the field functions are searched for a field that contains the
    * presence of the data block within this {@link ContainerContext}</li>
    * <li>Otherwise the {@link CountOf} field functions are searched for a field that contains the count of the data
    * block within this {@link ContainerContext}</li>
    * <li>If there is none, the same is done hierarchically for the parent {@link ContainerContext}</li>
    * <li>If there is none in the parent container context, {@link DataBlockDescription#UNDEFINED} is returned</li>
    * </ul>
    *
    * @param id
    *           The {@link DataBlockId} of the data block, must not be null
    * @return The count of the data block or {@link DataBlockDescription#UNDEFINED} if none is available
    */
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
    * Determines the {@link ByteOrder} of the given {@link DataBlockId} with the given sequence number within the
    * current {@link Container}. The approach is as follows:
    * <ul>
    * <li>If the field has a fixed {@link ByteOrder} according to its specification, the fixed {@link ByteOrder} is
    * taken</li>
    * <li>Otherwise the field functions are searched for a field that contains the {@link ByteOrder} of the data block
    * within this {@link ContainerContext}</li>
    * <li>If there is none, the same is done hierarchically for the parent {@link ContainerContext}</li>
    * <li>If there is none in the parent container context, the default {@link ByteOrder} of the
    * {@link DataFormatSpecification} is returned</li>
    * </ul>
    *
    * @param id
    *           The {@link DataBlockId} of the data block, must not be null
    * @param sequenceNumber
    *           The sequence number of the data block, must not be negative
    * @return The {@link ByteOrder} of the data block or the default {@link ByteOrder} of the
    *         {@link DataFormatSpecification} if none is available
    */
   public ByteOrder getByteOrderOf(DataBlockId id, int sequenceNumber) {
      Reject.ifNull(id, "id");
      Reject.ifNegative(sequenceNumber, "sequenceNumber");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      if (desc.getFieldProperties().getFixedByteOrder() != null) {
         return desc.getFieldProperties().getFixedByteOrder();
      }

      FieldCrossReference<String, ByteOrderOf> crossReference = byteOrders.getCrossReference(id, sequenceNumber);

      if (crossReference == null) {
         if (parentContainerContext != null) {
            return parentContainerContext.getByteOrderOf(id, sequenceNumber);
         } else {
            return spec.getDefaultByteOrder();
         }
      }

      return ByteOrders.fromString(crossReference.getValue());
   }

   /**
    * Determines the {@link Charset} of the given {@link DataBlockId} with the given sequence number within the current
    * {@link Container}. The approach is as follows:
    * <ul>
    * <li>If the field has a fixed {@link Charset} according to its specification, the fixed {@link Charset} is
    * taken</li>
    * <li>Otherwise the field functions are searched for a field that contains the {@link Charset} of the data block
    * within this {@link ContainerContext}</li>
    * <li>If there is none, the same is done hierarchically for the parent {@link ContainerContext}</li>
    * <li>If there is none in the parent container context, the default {@link Charset} of the
    * {@link DataFormatSpecification} is returned</li>
    * </ul>
    *
    * @param id
    *           The {@link DataBlockId} of the data block, must not be null
    * @param sequenceNumber
    *           The sequence number of the data block, must not be negative
    * @return The {@link Charset} of the data block or the default {@link Charset} of the
    *         {@link DataFormatSpecification} if none is available
    */
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
         if (parentContainerContext != null) {
            return parentContainerContext.getCharacterEncodingOf(id, sequenceNumber);
         } else {
            return spec.getDefaultCharacterEncoding();
         }
      }

      return Charset.forName(crossReference.getValue());
   }
}
