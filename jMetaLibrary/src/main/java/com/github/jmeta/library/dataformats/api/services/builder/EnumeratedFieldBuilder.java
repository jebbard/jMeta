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
public interface EnumeratedFieldBuilder<P, FIT> extends
   FieldBuilder<P, FIT, EnumeratedFieldBuilder<P, FIT>> {

   EnumeratedFieldBuilder<P, FIT> addEnumeratedValue(byte[] binaryValue,
      FIT interpretedValue);

}
