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

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.AbstractFieldFunction;
import com.github.jmeta.library.dataformats.api.types.ByteOrderOf;
import com.github.jmeta.library.dataformats.api.types.CharacterEncodingOf;
import com.github.jmeta.library.dataformats.api.types.CountOf;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.Flags;
import com.github.jmeta.library.dataformats.api.types.IdOf;
import com.github.jmeta.library.dataformats.api.types.PresenceOf;
import com.github.jmeta.library.dataformats.api.types.SizeOf;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link ContainerContext}
 *
 */
public class ContainerContext {

   private class FieldFunctionStore<T> {

      private final Class<? extends AbstractFieldFunction<T>> fieldFunctionClass;

      private final Map<DataBlockId, Map<Integer, Field<T>>> fields = new HashMap<>();

      /**
       * Creates a new {@link FieldFunctionStore}.
       *
       * @param fieldFunctionClass
       */
      public FieldFunctionStore(Class<? extends AbstractFieldFunction<T>> fieldFunctionClass) {
         Reject.ifNull(fieldFunctionClass, "fieldFunctionClass");
         this.fieldFunctionClass = fieldFunctionClass;
      }

      public void addField(Field<?> field) {
         Reject.ifNull(field, "field");

         DataBlockDescription fieldDesc = spec.getDataBlockDescription(field.getId());

         List<?> fieldFunctions = fieldDesc.getFieldProperties().getFieldFunctions();

         for (AbstractFieldFunction<?> fieldFunction : (List<AbstractFieldFunction<?>>) fieldFunctions) {
            if (fieldFunction.getClass().equals(fieldFunctionClass)) {
               DataBlockId targetId = fieldFunction.getReferencedBlock().getReferencedId();

               Map<Integer, Field<T>> fieldsPerSequenceNumber = null;

               if (fields.containsKey(targetId)) {
                  fieldsPerSequenceNumber = fields.get(targetId);
               } else {
                  fieldsPerSequenceNumber = new HashMap<>();
                  fields.put(targetId, fieldsPerSequenceNumber);
               }

               fieldsPerSequenceNumber.put(field.getSequenceNumber(), (Field<T>) field);
            }
         }
      }

      public T getValue(DataBlockId targetId, int sequenceNumber) {
         if (!fields.containsKey(targetId)) {
            return null;
         }

         if (!fields.get(targetId).containsKey(sequenceNumber)) {
            return null;
         }

         Field<?> field = fields.get(targetId).get(sequenceNumber);

         try {
            return (T) field.getInterpretedValue();
         } catch (BinaryValueConversionException e) {
            throw new RuntimeException("Unexpected exception during context field conversion", e);
         }
      }
   }

   private final DataFormatSpecification spec;
   private Container container;
   private final ContainerContext parentContainerContext;

   private final FieldFunctionStore<Long> sizes = new FieldFunctionStore<>(SizeOf.class);
   private final FieldFunctionStore<Long> counts = new FieldFunctionStore<>(CountOf.class);
   private final FieldFunctionStore<Flags> presences = new FieldFunctionStore<>(PresenceOf.class);
   private final FieldFunctionStore<String> ids = new FieldFunctionStore<>(IdOf.class);
   private final FieldFunctionStore<String> byteOrders = new FieldFunctionStore<>(ByteOrderOf.class);
   private final FieldFunctionStore<String> characterEncodings = new FieldFunctionStore<>(CharacterEncodingOf.class);

   /**
    * Creates a new {@link ContainerContext}.
    *
    * @param spec
    *           The {@link DataFormatSpecification} of this context, must not be null
    * @param parentContainerContext
    *           The parent {@link ContainerContext}, might be null if this {@link ContainerContext} belongs to a
    *           top-level container
    */
   public ContainerContext(DataFormatSpecification spec, ContainerContext parentContainerContext) {
      Reject.ifNull(spec, "spec");

      this.spec = spec;
      this.parentContainerContext = parentContainerContext;
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
    * @return The size of the data block or {@link DataBlockDescription#UNDEFINED} if none is available
    */
   public long getSizeOf(DataBlockId id, int sequenceNumber) {
      Reject.ifNull(id, "id");
      Reject.ifNegative(sequenceNumber, "sequenceNumber");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      if (desc.hasFixedSize()) {
         return desc.getMaximumByteLength();
      }

      Long sizeIndicatedByFieldFunction = sizes.getValue(id, sequenceNumber);

      if (sizeIndicatedByFieldFunction == null) {
         if (parentContainerContext == null) {
            return DataBlockDescription.UNDEFINED;
         }

         return parentContainerContext.getSizeOf(id, sequenceNumber);
      }

      return sizeIndicatedByFieldFunction;
   }

   /**
    * Determines the number of occurrences of the given {@link DataBlockId} with the given sequence number within the
    * current {@link Container}. The approach is as follows:
    * <ul>
    * <li>If the data block has fixed number of occurrences according to its specification, this number is returned</li>
    * <li>Otherwise the field functions are searched for a field that contains the count of the data block within this
    * {@link ContainerContext}</li>
    * <li>If there is none, the same is done hierarchically for the parent {@link ContainerContext}</li>
    * <li>If there is none in the parent container context, {@link DataBlockDescription#UNDEFINED} is returned</li>
    * </ul>
    *
    * @param id
    *           The {@link DataBlockId} of the data block, must not be null
    * @param sequenceNumber
    *           The sequence number of the data block, must not be negative
    * @return The count of the data block or {@link DataBlockDescription#UNDEFINED} if none is available
    */
   public long getCountOf(DataBlockId id, int sequenceNumber) {
      Reject.ifNull(id, "id");
      Reject.ifNegative(sequenceNumber, "sequenceNumber");

      DataBlockDescription desc = spec.getDataBlockDescription(id);

      if (desc.getMaximumOccurrences() == desc.getMaximumOccurrences()) {
         return desc.getMaximumOccurrences();
      }

      Long countIndicatedByFieldFunction = counts.getValue(id, sequenceNumber);

      if (countIndicatedByFieldFunction == null) {
         if (parentContainerContext == null) {
            return DataBlockDescription.UNDEFINED;
         }

         return parentContainerContext.getCountOf(id, sequenceNumber);
      }

      return countIndicatedByFieldFunction;
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

      String idIndicatedByFieldFunction = ids.getValue(id, sequenceNumber);

      if (idIndicatedByFieldFunction == null) {
         if (parentContainerContext != null) {
            return parentContainerContext.getIdOf(id, sequenceNumber);
         }
      }

      return idIndicatedByFieldFunction;
   }

   // public boolean isPresent(DataBlockId id, int sequenceIndexInParent) {
   // // TODO implement
   // return false;
   // }

   /**
    * Determines the {@link ByteOrder} of the given {@link DataBlockId} with the given sequence number within the
    * current {@link Container}. The approach is as follows:
    * <ul>
    * <li>The field functions are searched for a field that contains the {@link ByteOrder} of the data block within this
    * {@link ContainerContext}</li>
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

      return null;
      // ByteOrder byteOrderIndicatedByFieldFunction = byteOrders.getValue(id, sequenceNumber);
      //
      // if (byteOrderIndicatedByFieldFunction == null) {
      // if (parentContainerContext != null) {
      // return parentContainerContext.getByteOrderOf(id, sequenceNumber);
      // } else {
      // return spec.getDefaultByteOrder();
      // }
      // }
      //
      // return byteOrderIndicatedByFieldFunction;
   }

   /**
    * Determines the {@link Charset} of the given {@link DataBlockId} with the given sequence number within the current
    * {@link Container}. The approach is as follows:
    * <ul>
    * <li>The field functions are searched for a field that contains the {@link Charset} of the data block within this
    * {@link ContainerContext}</li>
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

      return null;
      // Charset characterEncodingIndicatedByFieldFunction = characterEncodings.getValue(id, sequenceNumber);
      //
      // if (characterEncodingIndicatedByFieldFunction == null) {
      // if (parentContainerContext != null) {
      // return parentContainerContext.getCharacterEncodingOf(id, sequenceNumber);
      // } else {
      // return spec.getDefaultCharacterEncoding();
      // }
      // }
      //
      // return characterEncodingIndicatedByFieldFunction;
   }
}
