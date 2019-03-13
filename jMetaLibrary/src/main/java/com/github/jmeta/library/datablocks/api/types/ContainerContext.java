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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import com.github.jmeta.library.dataformats.api.types.IdOf;
import com.github.jmeta.library.dataformats.api.types.PresenceOf;
import com.github.jmeta.library.dataformats.api.types.SizeOf;
import com.github.jmeta.library.dataformats.api.types.SummedSizeOf;
import com.github.jmeta.utility.byteutils.api.services.ByteOrders;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ContainerContext}
 *
 */
public class ContainerContext {

   private final SizeProvider customSizeProvider;
   private final CountProvider customCountProvider;

   /**
    * Returns the attribute {@link #spec}.
    *
    * @return the attribute {@link #spec}
    */
   public DataFormatSpecification getDataFormatSpecification() {
      return spec;
   }

   /**
    * Returns the attribute {@link #parentContainerContext}.
    *
    * @return the attribute {@link #parentContainerContext}
    */
   public ContainerContext getParentContainerContext() {
      return parentContainerContext;
   }

   public static class FieldCrossReference<T, F extends AbstractFieldFunction<T>> {

      private final DataBlockId referencedBlock;
      private final Field<T> referencingField;
      private final F referencingFieldFunction;

      /**
       * Creates a new {@link FieldCrossReference}.
       *
       * @param referencedBlock
       * @param referencedBlockSequenceNumber
       * @param referencingField
       * @param referencingFieldFunction
       */
      public FieldCrossReference(DataBlockId referencedBlock, Field<T> referencingField, F referencingFieldFunction) {
         this.referencedBlock = referencedBlock;
         this.referencingField = referencingField;
         this.referencingFieldFunction = referencingFieldFunction;
      }

      /**
       * Returns the attribute {@link #referencedBlock}.
       *
       * @return the attribute {@link #referencedBlock}
       */
      public DataBlockId getReferencedBlock() {
         return referencedBlock;
      }

      /**
       * Returns the attribute {@link #referencingField}.
       *
       * @return the attribute {@link #referencingField}
       */
      public Field<T> getReferencingField() {
         return referencingField;
      }

      /**
       * Returns the attribute {@link #referencingFieldFunction}.
       *
       * @return the attribute {@link #referencingFieldFunction}
       */
      public F getReferencingFieldFunction() {
         return referencingFieldFunction;
      }

      public T getValue() {
         try {
            return referencingField.getInterpretedValue();
         } catch (BinaryValueConversionException e) {
            throw new RuntimeException("Unexpected exception during context field conversion", e);
         }
      }
   }

   private class FieldFunctionStore<T, F extends AbstractFieldFunction<T>> {

      private final Class<F> fieldFunctionClass;

      private final Map<DataBlockId, Map<Integer, FieldCrossReference<T, F>>> fieldCrossRefs = new HashMap<>();

      private final Map<Field<T>, Set<FieldCrossReference<T, F>>> fieldCrossRefsBySourceField = new HashMap<>();

      /**
       * Creates a new {@link FieldFunctionStore}.
       *
       * @param fieldFunctionClass
       */
      public FieldFunctionStore(Class<F> fieldFunctionClass) {
         Reject.ifNull(fieldFunctionClass, "fieldFunctionClass");
         this.fieldFunctionClass = fieldFunctionClass;
      }

      @SuppressWarnings("unchecked")
      public void addField(Field<?> field) {
         Reject.ifNull(field, "field");

         DataBlockDescription fieldDesc = spec.getDataBlockDescription(field.getId());

         List<?> fieldFunctionList = fieldDesc.getFieldProperties().getFieldFunctions();

         for (AbstractFieldFunction<?> fieldFunction : (List<AbstractFieldFunction<?>>) fieldFunctionList) {
            if (fieldFunction.getClass().equals(fieldFunctionClass)) {
               Set<DataBlockId> targetIds = new HashSet<>();

               if (fieldFunction.getClass().equals(SummedSizeOf.class)) {
                  DataBlockCrossReference[] refBlocks = ((SummedSizeOf) fieldFunction).getReferencedBlocks();

                  for (DataBlockCrossReference refBlock : refBlocks) {
                     targetIds.add(refBlock.getId());
                  }

               } else {
                  targetIds.add(fieldFunction.getReferencedBlock().getId());
               }

               for (DataBlockId targetId : targetIds) {
                  Map<Integer, FieldCrossReference<T, F>> fieldCrossRefsPerSequenceNumber = null;

                  if (fieldCrossRefs.containsKey(targetId)) {
                     fieldCrossRefsPerSequenceNumber = fieldCrossRefs.get(targetId);
                  } else {
                     fieldCrossRefsPerSequenceNumber = new HashMap<>();
                     fieldCrossRefs.put(targetId, fieldCrossRefsPerSequenceNumber);
                  }

                  FieldCrossReference<T, F> crossRef = new FieldCrossReference<>(targetId, (Field<T>) field,
                     (F) fieldFunction);
                  fieldCrossRefsPerSequenceNumber.put(field.getSequenceNumber(), crossRef);

                  Set<FieldCrossReference<T, F>> crossRefsForField = null;

                  if (fieldCrossRefsBySourceField.containsKey(field)) {
                     crossRefsForField = fieldCrossRefsBySourceField.get(field);
                  } else {
                     crossRefsForField = new HashSet<>();
                     fieldCrossRefsBySourceField.put((Field<T>) field, crossRefsForField);
                  }

                  crossRefsForField.add(crossRef);
               }
            }
         }
      }

      public FieldCrossReference<T, F> getCrossReference(DataBlockId targetId, int sequenceNumber) {
         if (!fieldCrossRefs.containsKey(targetId)) {
            return null;
         }

         if (!fieldCrossRefs.get(targetId).containsKey(sequenceNumber)) {
            return null;
         }

         return fieldCrossRefs.get(targetId).get(sequenceNumber);
      }
   }

   private final DataFormatSpecification spec;
   private Container container;
   private final ContainerContext parentContainerContext;

   private final FieldFunctionStore<Long, SizeOf> sizes = new FieldFunctionStore<>(SizeOf.class);
   private final FieldFunctionStore<Long, SummedSizeOf> summedSizes = new FieldFunctionStore<>(SummedSizeOf.class);
   private final FieldFunctionStore<Long, CountOf> counts = new FieldFunctionStore<>(CountOf.class);
   private final FieldFunctionStore<Flags, PresenceOf> presences = new FieldFunctionStore<>(PresenceOf.class);
   private final FieldFunctionStore<String, IdOf> ids = new FieldFunctionStore<>(IdOf.class);
   private final FieldFunctionStore<String, ByteOrderOf> byteOrders = new FieldFunctionStore<>(ByteOrderOf.class);
   private final FieldFunctionStore<String, CharacterEncodingOf> characterEncodings = new FieldFunctionStore<>(
      CharacterEncodingOf.class);

   /**
    * Returns the attribute {@link #container}.
    *
    * @return the attribute {@link #container}
    */
   public Container getContainer() {
      return container;
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
    *           TODO
    * @param customCountProvider
    *           TODO
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
      ids.addField(field);
   }

   /**
    * Determines the size of the given {@link DataBlockId} with the given sequence number within the current
    * {@link Container}. The approach is as follows:
    * <ul>
    * <li>If the data block has fixed size according to its specification, this size is returned</li>
    * <li>Otherwise the field functions are searched for a field that contains the size of the data block within this
    * {@link ContainerContext}</li>
    * <li>If there is none, the same is done hierarchically for the parent {@link ContainerContext}</li>
    * <li>If there is none in the parent container context, {@link DataBlockDescription#UNDEFINED} is returned</li>
    * </ul>
    *
    * @param id
    *           The {@link DataBlockId} of the data block, must not be null
    * @param sequenceNumber
    *           The sequence number of the data block, must not be negative
    * @param remainingDirectParentByteCount
    *           TODO
    * @return The size of the data block or {@link DataBlockDescription#UNDEFINED} if none is available
    */
   public long getSizeOf(DataBlockId id, int sequenceNumber, long remainingDirectParentByteCount) {
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
            DataBlockCrossReference[] allReferences = summedSizeCrossReference.getReferencingFieldFunction()
               .getReferencedBlocks();

            long partialSize = summedSizeCrossReference.getValue();

            for (DataBlockCrossReference dataBlockCrossReference : allReferences) {
               DataBlockId siblingId = dataBlockCrossReference.getId();
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
    * <li>If the data block has fixed number of occurrences according to its specification, this number is returned</li>
    * <li>Otherwise if the data block is optional, the field functions are searched for a field that contains the
    * presence of the data block within this {@link ContainerContext}</li>
    * <li>Otherwise the field functions are searched for a field that contains the count of the data block within this
    * {@link ContainerContext}</li>
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

      if (desc.getMaximumOccurrences() == desc.getMinimumOccurrences()) {
         return desc.getMaximumOccurrences();
      }

      if (customCountProvider != null) {
         long actualOccurrences = customCountProvider.getCountOf(desc.getId(), 0, this);

         if (actualOccurrences != DataBlockDescription.UNDEFINED) {
            return actualOccurrences;
         }
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
    * Determines the actual id of the given (generic) {@link DataBlockId} with the given sequence number within the
    * current {@link Container}. The approach is as follows:
    * <ul>
    * <li>The field functions are searched for a field that contains the id of the data block within this
    * {@link ContainerContext}</li>
    * <li>If there is none, the same is done hierarchically for the parent {@link ContainerContext}</li>
    * <li>If there is none in the parent container context, null is returned</li>
    * </ul>
    *
    * @param id
    *           The {@link DataBlockId} of the data block, must not be null
    * @param sequenceNumber
    *           The sequence number of the data block, must not be negative
    * @return The id of the data block or null if none is available
    */
   public String getIdOf(DataBlockId id, int sequenceNumber) {
      Reject.ifNull(id, "id");
      Reject.ifNegative(sequenceNumber, "sequenceNumber");

      FieldCrossReference<String, IdOf> crossReference = ids.getCrossReference(id, sequenceNumber);

      if (crossReference == null) {
         if (parentContainerContext != null) {
            return parentContainerContext.getIdOf(id, sequenceNumber);
         }
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
