/**
 *
 * {@link ID3v23DataBlockFactory}.java
 *
 * @author Jens Ebert
 *
 * @date 07.05.2011
 */
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.util.HashSet;
import java.util.Set;

import com.github.jmeta.library.datablocks.impl.FieldConverter;
import com.github.jmeta.library.datablocks.impl.StandardDataBlockFactory;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;

/**
 * {@link ID3v23DataBlockFactory}
 *
 */
public class ID3v23DataBlockFactory extends StandardDataBlockFactory {

   private static final SyncSafeIntegerConverter SYNC_SAFE_INTEGER_CONVERTER = new SyncSafeIntegerConverter();

   @SuppressWarnings("unchecked")
   @Override
   protected <T> FieldConverter<T> getFieldConverter(DataBlockId fieldId) {

      if (SYNC_SAFE_INTEGER_FIELD_IDS.contains(fieldId.getGlobalId()))
         return (FieldConverter<T>) SYNC_SAFE_INTEGER_CONVERTER;

      return super.getFieldConverter(fieldId);
   }

   private static final Set<String> SYNC_SAFE_INTEGER_FIELD_IDS = new HashSet<>();

   static {
      SYNC_SAFE_INTEGER_FIELD_IDS.add("id3v23.header.size");
      SYNC_SAFE_INTEGER_FIELD_IDS.add("id3v23.payload.GENERIC.header.size");
   }
}
