/**
 *
 * {@link StringFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 12.05.2018
 *
 */
package com.github.jmeta.library.dataformats.api.services.builder;

/**
 * {@link EnumeratedFieldBuilder}
 *
 */
public interface EnumeratedFieldBuilder<ParentBuilder, FieldInterpretedType>
   extends FieldBuilder<ParentBuilder, FieldInterpretedType> {

   void addEnumeratedValue(byte[] binaryValue, FieldInterpretedType interpretedValue);

}
