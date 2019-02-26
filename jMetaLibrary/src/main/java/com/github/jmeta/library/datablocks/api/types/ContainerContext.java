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

/**
 * {@link ContainerContext}
 *
 */
public class ContainerContext {

   // private final Map<FieldFunctionType<?>, Map<DataBlockId, List<Field<?>>>> fields = new HashMap<>();
   //
   // private final DataFormatSpecification spec;
   //
   // /**
   // * Creates a new {@link ContainerContext}.
   // *
   // * @param spec
   // */
   // public ContainerContext(DataFormatSpecification spec) {
   // this.spec = spec;
   // }
   //
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
