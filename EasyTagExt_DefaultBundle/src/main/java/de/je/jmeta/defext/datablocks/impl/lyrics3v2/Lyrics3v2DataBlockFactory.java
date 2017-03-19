/**
 *
 * {@link ID3v23DataBlockFactory}.java
 *
 * @author Jens Ebert
 *
 * @date 07.05.2011
 */
package de.je.jmeta.defext.datablocks.impl.lyrics3v2;

import de.je.jmeta.datablocks.impl.IFieldConverter;
import de.je.jmeta.datablocks.impl.StandardDataBlockFactory;
import de.je.jmeta.dataformats.DataBlockId;

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
   protected <T> IFieldConverter<T> getFieldConverter(DataBlockId fieldId) {

      if (fieldId.getGlobalId().endsWith(".footer.size")
         || fieldId.getGlobalId().endsWith(".header.size"))
         return (IFieldConverter<T>) STRING_SIZE_INTEGER_CONVERTER;

      return super.getFieldConverter(fieldId);
   }
}
