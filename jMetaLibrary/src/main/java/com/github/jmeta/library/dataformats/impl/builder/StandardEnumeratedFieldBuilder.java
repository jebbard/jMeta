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
public class StandardEnumeratedFieldBuilder<P extends DataBlockDescriptionBuilder<P>, FIT>
   extends AbstractFieldBuilder<P, FIT, EnumeratedFieldBuilder<P, FIT>>
   implements EnumeratedFieldBuilder<P, FIT> {

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
      super(parentBuilder, localId, name, description, (FieldType<FIT>) FieldType.ENUMERATED,
         isGeneric);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.services.builder.EnumeratedFieldBuilder#addEnumeratedValue(byte[],
    *      java.lang.Object)
    */
   @Override
   public EnumeratedFieldBuilder<P, FIT> addEnumeratedValue(byte[] binaryValue,
      FIT interpretedValue) {
      getEnumeratedValues().put(interpretedValue, binaryValue);
      return this;
   }

}
