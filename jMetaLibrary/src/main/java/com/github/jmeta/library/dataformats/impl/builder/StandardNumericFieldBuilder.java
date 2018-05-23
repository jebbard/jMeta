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

import java.nio.ByteOrder;

import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link StandardNumericFieldBuilder}
 *
 */
public class StandardNumericFieldBuilder<P extends DataBlockDescriptionBuilder<P>>
   extends AbstractFieldBuilder<P, Long, NumericFieldBuilder<P>> implements NumericFieldBuilder<P> {

   /**
    * Creates a new {@link StandardNumericFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param isGeneric
    *           TODO
    * @param fieldType
    */
   public StandardNumericFieldBuilder(P parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, FieldType.UNSIGNED_WHOLE_NUMBER, isGeneric);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.NumericFieldBuilder#withFixedByteOrder(java.nio.ByteOrder)
    */
   @Override
   public NumericFieldBuilder<P> withFixedByteOrder(ByteOrder byteOrder) {
      setFixedByteOrder(byteOrder);
      return this;
   }

}
