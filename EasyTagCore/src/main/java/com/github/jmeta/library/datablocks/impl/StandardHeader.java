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

import com.github.jmeta.library.datablocks.api.services.IDataBlockReader;
import com.github.jmeta.library.datablocks.api.type.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.type.IField;
import com.github.jmeta.library.datablocks.api.type.IHeader;
import com.github.jmeta.library.dataformats.api.type.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.type.DataBlockId;
import com.github.jmeta.library.media.api.type.IMediumReference;

import de.je.util.javautil.common.err.Reject;

/**
 *
 */
public class StandardHeader extends AbstractDataBlock implements IHeader {

   /**
    * Creates a new {@link StandardHeader}.
    * 
    * @param id
    * @param reference
    * @param fields
    * @param isFooter
    * @param dataBlockReader
    */
   public StandardHeader(DataBlockId id, IMediumReference reference,
      List<IField<?>> fields, boolean isFooter,
      IDataBlockReader dataBlockReader) {
      super(id, null, reference, dataBlockReader);

      Reject.ifNull(fields, "fields");

      m_isFooter = isFooter;

      setFields(fields);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IHeader#isFooter()
    */
   @Override
   public boolean isFooter() {

      return m_isFooter;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IHeader#getFields()
    */
   @Override
   public List<IField<?>> getFields() {

      return Collections.unmodifiableList(m_fields);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IDataBlock#getTotalSize()
    */
   @Override
   public long getTotalSize() {

      if (m_fields.size() == 0)
         return DataBlockDescription.UNKNOWN_SIZE;

      long returnedSize = 0;

      for (Iterator<IField<?>> fieldIterator = m_fields
         .iterator(); fieldIterator.hasNext();) {
         IField<?> field = fieldIterator.next();

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
   public void setFields(List<IField<?>> fields) {

      Reject.ifNull(fields, "fields");

      m_fields.clear();

      for (int i = 0; i < fields.size(); ++i)
         addField(fields.get(i));
   }

   private void addField(IField<?> field) {

      Reject.ifNull(field, "field");

      if (field.getParent() == null)
         field.initParent(this);

      m_fields.add(field);
   }

   private final List<IField<?>> m_fields = new ArrayList<>();

   private final boolean m_isFooter;
}
