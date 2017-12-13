/**
 * {@link StandardHeader}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class StandardHeader extends AbstractDataBlock implements Header {

   /**
    * Creates a new {@link StandardHeader}.
    * 
    * @param id
    * @param reference
    * @param fields
    * @param isFooter
    * @param dataBlockReader
    */
   public StandardHeader(DataBlockId id, MediumOffset reference,
      List<Field<?>> fields, boolean isFooter,
      DataBlockReader dataBlockReader) {
      super(id, null, reference, dataBlockReader);

      Reject.ifNull(fields, "fields");

      m_isFooter = isFooter;

      setFields(fields);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Header#isFooter()
    */
   @Override
   public boolean isFooter() {

      return m_isFooter;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Header#getFields()
    */
   @Override
   public List<Field<?>> getFields() {

      return Collections.unmodifiableList(m_fields);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getTotalSize()
    */
   @Override
   public long getTotalSize() {

      if (m_fields.size() == 0)
         return DataBlockDescription.UNKNOWN_SIZE;

      long returnedSize = 0;

      for (Iterator<Field<?>> fieldIterator = m_fields
         .iterator(); fieldIterator.hasNext();) {
         Field<?> field = fieldIterator.next();

         // As soon as one child has unknown size, the whole header has unknown size
         if (field.getTotalSize() == DataBlockDescription.UNKNOWN_SIZE)
            return DataBlockDescription.UNKNOWN_SIZE;

         returnedSize += field.getTotalSize();
      }

      return returnedSize;
   }

   /**
    * @param fields
    */
   public void setFields(List<Field<?>> fields) {

      Reject.ifNull(fields, "fields");

      m_fields.clear();

      for (int i = 0; i < fields.size(); ++i)
         addField(fields.get(i));
   }

   private void addField(Field<?> field) {

      Reject.ifNull(field, "field");

      if (field.getParent() == null)
         field.initParent(this);

      m_fields.add(field);
   }

   private final List<Field<?>> m_fields = new ArrayList<>();

   private final boolean m_isFooter;
}
