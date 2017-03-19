/**
 *
 * {@link ID3v23DataBlockFactory}.java
 *
 * @author Jens Ebert
 *
 * @date 07.05.2011
 */
package de.je.jmeta.defext.datablocks.impl.id3v23;

import java.util.HashSet;
import java.util.Set;

import de.je.jmeta.datablocks.impl.IFieldConverter;
import de.je.jmeta.datablocks.impl.StandardDataBlockFactory;
import de.je.jmeta.dataformats.DataBlockId;

/**
 * {@link ID3v23DataBlockFactory}
 *
 */
public class ID3v23DataBlockFactory extends StandardDataBlockFactory {

   private static final SyncSafeIntegerConverter SYNC_SAFE_INTEGER_CONVERTER = new SyncSafeIntegerConverter();

   @SuppressWarnings("unchecked")
   @Override
   protected <T> IFieldConverter<T> getFieldConverter(DataBlockId fieldId) {

      if (SYNC_SAFE_INTEGER_FIELD_IDS.contains(fieldId.getGlobalId()))
         return (IFieldConverter<T>) SYNC_SAFE_INTEGER_CONVERTER;

      return super.getFieldConverter(fieldId);
   }

   private static final Set<String> SYNC_SAFE_INTEGER_FIELD_IDS = new HashSet<>();

   static {
      SYNC_SAFE_INTEGER_FIELD_IDS.add("id3v23.header.size");
      SYNC_SAFE_INTEGER_FIELD_IDS.add("id3v23.payload.GENERIC.header.size");
   }
}
