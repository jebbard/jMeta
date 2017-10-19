/**
 * {@link StandardHeader}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package de.je.jmeta.datablocks.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import de.je.jmeta.datablocks.IField;
import de.je.jmeta.datablocks.IHeader;
import de.je.jmeta.datablocks.export.AbstractDataBlock;
import de.je.jmeta.datablocks.export.IDataBlockReader;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.jmeta.dataformats.DataBlockId;
import de.je.jmeta.media.api.IMediumReference;
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
    * @see de.je.jmeta.datablocks.IHeader#isFooter()
    */
   @Override
   public boolean isFooter() {

      return m_isFooter;
   }

   /**
    * @see de.je.jmeta.datablocks.IHeader#getFields()
    */
   @Override
   public List<IField<?>> getFields() {

      return Collections.unmodifiableList(m_fields);
   }

   /**
    * @see de.je.jmeta.datablocks.IDataBlock#getTotalSize()
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
