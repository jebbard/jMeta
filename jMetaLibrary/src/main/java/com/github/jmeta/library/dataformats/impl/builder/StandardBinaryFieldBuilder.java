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

import com.github.jmeta.library.dataformats.api.services.builder.BinaryFieldBuilder;
import com.github.jmeta.library.dataformats.api.services.builder.DataBlockDescriptionBuilder;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link StandardBinaryFieldBuilder}
 *
 */
public class StandardBinaryFieldBuilder<P extends DataBlockDescriptionBuilder<P>>
   extends AbstractFieldBuilder<P, byte[], BinaryFieldBuilder<P>> implements BinaryFieldBuilder<P> {

   /**
    * Creates a new {@link StandardBinaryFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param isGeneric
    *           TODO
    * @param fieldType
    */
   public StandardBinaryFieldBuilder(P parentBuilder, String localId, String name, String description,
      boolean isGeneric) {
      super(parentBuilder, localId, name, description, FieldType.BINARY, isGeneric);
   }
}
