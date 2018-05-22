/**
 *
 * {@link StandardStringFieldBuilder}.java
 *
 * @author Jens Ebert
 *
 * @date 13.05.2018
 *
 */
package com.github.jmeta.library.dataformats.impl.builder;

import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link StandardEnumeratedFieldBuilder}
 *
 */
public class StandardEnumeratedFieldBuilder<P extends DataBlockDescriptionBuilder<P>, FieldInterpretedType>
   extends AbstractFieldBuilder<P, EnumeratedFieldBuilder<P, FieldInterpretedType>, FieldInterpretedType>
   implements EnumeratedFieldBuilder<P, FieldInterpretedType> {

   /**
    * Creates a new {@link StandardEnumeratedFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param isGeneric
    *           TODO
    * @param fieldType
    */
   public StandardEnumeratedFieldBuilder(P parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, (FieldType<FieldInterpretedType>) FieldType.ENUMERATED,
         isGeneric);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder#addEnumeratedValue(byte[],
    *      java.lang.Object)
    */
   @Override
   public EnumeratedFieldBuilder<P, FieldInterpretedType> addEnumeratedValue(byte[] binaryValue,
      FieldInterpretedType interpretedValue) {
      getEnumeratedValues().put(interpretedValue, binaryValue);
      return this;
   }

}
