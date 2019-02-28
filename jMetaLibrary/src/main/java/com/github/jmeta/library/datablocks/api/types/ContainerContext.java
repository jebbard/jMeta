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

import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;

/**
 * {@link ContainerContext}
 *
 */
public class ContainerContext {

   // private final Map<FieldFunctionType<?>, Map<DataBlockId, List<Field<?>>>> fields = new HashMap<>();

   private final DataFormatSpecification spec;
   private final Container container;
   private final ContainerContext parentContainerContext = null;

   /**
    * Creates a new {@link ContainerContext}.
    *
    * @param spec
    *           The {@link DataFormatSpecification} of this context, must not be null
    * @param container
    *           The {@link Container} this context belongs to, must not be null
    */
   public ContainerContext(DataFormatSpecification spec, Container container) {
      this.spec = spec;
      this.container = container;
      // TODO set parent container context to context of the provided container's parent
      // this.parentContainerContext = parentContainerContext;
   }

   // /**
   // * Adds all {@link FieldFunction}s of the given field to this {@link ContainerContext}. The concrete values can be
   // * later retrieved using the getters.
   // *
   // * @param field
   // * The {@link Field} to add, must not be null
   // */
   // public void addFieldFunctions(Field<?> field) {
   // Reject.ifNull(field, "field");
   //
   // DataBlockDescription fieldDesc = spec.getDataBlockDescription(field.getId());
   //
   // fieldDesc.getFieldProperties().getFieldFunctions().forEach(fieldFunction -> {
   // fields.get(fieldFunction.getFieldFunctionType()).put(null, null);
   // });
   // }
   //
   // public void updateReferencingFields(DataBlock datablock) {
   // // TODO implement
   // }
   //
   // public long getSizeOf(DataBlockId id, int sequenceIndexInParent) {
   // // TODO implement
   // return 0;
   // }
   //
   // public long getCountOf(DataBlockId id) {
   // // TODO implement
   // return 0;
   // }
   //
   // public String getIdOf(DataBlockId id, int sequenceIndexInParent) {
   // // TODO implement
   // return "";
   // }
   //
   // public boolean isPresent(DataBlockId id, int sequenceIndexInParent) {
   // // TODO implement
   // return false;
   // }
   //
   // public ByteOrder getByteOrderOf(DataBlockId id, int sequenceIndexInParent) {
   // // TODO implement
   // return null;
   // }
   //
   // public Charset getCharacterEncodingOf(DataBlockId id, int sequenceIndexInParent) {
   // // TODO implement
   // return null;
   // }
}
