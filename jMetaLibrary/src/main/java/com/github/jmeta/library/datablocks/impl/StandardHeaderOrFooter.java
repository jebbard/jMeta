/**
 * {@link StandardHeaderOrFooter}.java
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
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.Footer;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class StandardHeaderOrFooter extends AbstractDataBlock implements Header, Footer {

   /**
    * Creates a new {@link StandardHeaderOrFooter}.
    *
    * @param id
    * @param reference
    * @param fields
    * @param isFooter
    * @param dataBlockReader
    * @param sequenceNumber
    *           TODO
    */
   public StandardHeaderOrFooter(DataBlockId id, MediumOffset reference, List<Field<?>> fields, boolean isFooter,
      DataBlockReader dataBlockReader, int sequenceNumber, ContainerContext containerContext) {
      super(id, null, reference, dataBlockReader, 0, containerContext);

      Reject.ifNull(fields, "fields");

      m_isFooter = isFooter;

      setFields(fields);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Header#getFields()
    */
   @Override
   public List<Field<?>> getFields() {

      return Collections.unmodifiableList(m_fields);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getSize()
    */
   @Override
   public long getSize() {

      if (m_fields.size() == 0) {
         return DataBlockDescription.UNDEFINED;
      }

      long returnedSize = 0;

      for (Iterator<Field<?>> fieldIterator = m_fields.iterator(); fieldIterator.hasNext();) {
         Field<?> field = fieldIterator.next();

         // As soon as one child has unknown size, the whole header has unknown size
         if (field.getSize() == DataBlockDescription.UNDEFINED) {
            return DataBlockDescription.UNDEFINED;
         }

         returnedSize += field.getSize();
      }

      return returnedSize;
   }

   /**
    * @param fields
    */
   public void setFields(List<Field<?>> fields) {

      Reject.ifNull(fields, "fields");

      m_fields.clear();

      for (int i = 0; i < fields.size(); ++i) {
         addField(fields.get(i));
      }
   }

   private void addField(Field<?> field) {

      Reject.ifNull(field, "field");

      if (field.getParent() == null) {
         field.initParent(this);
      }

      m_fields.add(field);
   }

   private final List<Field<?>> m_fields = new ArrayList<>();

   private final boolean m_isFooter;
}
