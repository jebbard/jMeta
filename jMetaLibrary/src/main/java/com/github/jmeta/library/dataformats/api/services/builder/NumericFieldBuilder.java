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
public interface NumericFieldBuilder<P>
   extends FieldBuilder<P, Long, NumericFieldBuilder<P>> {

   NumericFieldBuilder<P> withFixedByteOrder(ByteOrder byteOrder);
}
