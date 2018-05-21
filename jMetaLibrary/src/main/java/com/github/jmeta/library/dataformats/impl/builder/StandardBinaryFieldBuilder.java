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
import com.github.jmeta.library.dataformats.api.services.builder.DataFormatSpecificationBuilder;
import com.github.jmeta.library.dataformats.api.types.FieldType;

/**
 * {@link StandardBinaryFieldBuilder}
 *
 */
public class StandardBinaryFieldBuilder<ParentBuilder extends DataFormatSpecificationBuilder>
   extends AbstractFieldBuilder<ParentBuilder, BinaryFieldBuilder<ParentBuilder>, byte[]>
   implements BinaryFieldBuilder<ParentBuilder> {

   /**
    * Creates a new {@link StandardBinaryFieldBuilder}.
    * 
    * @param parentBuilder
    * @param localId
    * @param name
    * @param description
    * @param isGeneric TODO
    * @param fieldType
    */
   public StandardBinaryFieldBuilder(ParentBuilder parentBuilder, String localId, String name, String description, boolean isGeneric) {
      super(parentBuilder, localId, name, description, FieldType.BINARY, isGeneric);
   }
}
