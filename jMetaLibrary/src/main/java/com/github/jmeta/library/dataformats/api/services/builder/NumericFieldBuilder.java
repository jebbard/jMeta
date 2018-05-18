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

import java.nio.ByteOrder;

/**
 * {@link NumericFieldBuilder}
 *
 */
public interface NumericFieldBuilder<ParentBuilder> extends FieldBuilder<ParentBuilder, Long>,
   FieldDescriptionModifier<ParentBuilder, Long, NumericFieldBuilder<ParentBuilder>>,
   DataBlockDescriptionModifier<NumericFieldBuilder<ParentBuilder>> {

   NumericFieldBuilder<ParentBuilder> withFixedByteOrder(ByteOrder byteOrder);
}