/**
 *
 * {@link ID3v23DataBlockFactory}.java
 *
 * @author Jens Ebert
 *
 * @date 07.05.2011
 */
package com.github.jmeta.defaultextensions.lyrics3v2.impl;

import com.github.jmeta.library.datablocks.impl.FieldConverter;
import com.github.jmeta.library.datablocks.impl.StandardDataBlockFactory;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

// TODO primeRefactor009: There must be a better approach to implement setting other field converters (the same for
// ID3v23)
/**
 * {@link Lyrics3v2DataBlockFactory}
 *
 */
public class Lyrics3v2DataBlockFactory extends StandardDataBlockFactory {

   private static final Lyrics3v2StringSizeIntegerConverter STRING_SIZE_INTEGER_CONVERTER = new Lyrics3v2StringSizeIntegerConverter();

   @SuppressWarnings("unchecked")
   @Override
   protected <T> FieldConverter<T> getFieldConverter(DataBlockId fieldId) {

      if (fieldId.getGlobalId().endsWith(".footer.size")
         || fieldId.getGlobalId().endsWith(".header.size"))
         return (FieldConverter<T>) STRING_SIZE_INTEGER_CONVERTER;

      return super.getFieldConverter(fieldId);
   }
}
