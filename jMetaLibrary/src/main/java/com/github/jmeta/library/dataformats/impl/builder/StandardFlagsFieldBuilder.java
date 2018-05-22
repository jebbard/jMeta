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
import com.github.jmeta.library.dataformats.api.services.builder.FlagSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder;
import com.github.jmeta.library.dataformats.api.types.FieldType;
import com.github.jmeta.library.dataformats.api.types.Flags;

/**
 * {@link StandardFlagsFieldBuilder}
 *
 */
public class StandardFlagsFieldBuilder<P extends DataBlockDescriptionBuilder<P>>
   extends AbstractFieldBuilder<P, FlagsFieldBuilder<P>, Flags> implements FlagsFieldBuilder<P> {

   /**
    * Creates a new {@link StandardFlagsFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param isGeneric
    *           TODO
    * @param fieldType
    */
   public StandardFlagsFieldBuilder(P parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, FieldType.FLAGS, isGeneric);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.FlagsFieldBuilder#withFlagSpecification(com.github.jmeta.library.dataformats.api.types.FlagSpecification)
    */
   @Override
   public FlagSpecificationBuilder<P> withFlagSpecification(int byteLength, ByteOrder byteOrder) {
      return new StandardFlagSpecificationBuilder<>(this, byteLength, byteOrder);
   }

}
