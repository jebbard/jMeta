/**
 * {@link StandardContainer}.java
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
import com.github.jmeta.library.datablocks.api.type.IContainer;
import com.github.jmeta.library.datablocks.api.type.IDataBlock;
import com.github.jmeta.library.datablocks.api.type.IHeader;
import com.github.jmeta.library.datablocks.api.type.IPayload;
import com.github.jmeta.library.dataformats.api.type.DataBlockId;
import com.github.jmeta.library.media.api.type.IMediumReference;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class StandardContainer extends AbstractDataBlock implements IContainer {

   /**
    * Creates a new {@link StandardContainer}.
    * 
    * @param id
    * @param parent
    * @param reference
    * @param headers
    * @param payload
    * @param footers
    * @param dataBlockReader
    */
   public StandardContainer(DataBlockId id, IDataBlock parent,
      IMediumReference reference, List<IHeader> headers, IPayload payload,
      List<IHeader> footers, IDataBlockReader dataBlockReader) {
      super(id, parent, reference, dataBlockReader);

      Reject.ifNull(footers, "footers");
      Reject.ifNull(payload, "payload");
      Reject.ifNull(headers, "headers");

      for (int i = 0; i < headers.size(); ++i)
         addHeader(headers.get(i));

      for (int i = 0; i < footers.size(); ++i)
         addFooter(footers.get(i));

      setPayload(payload);
   }

   /**
    * @param footer
    */
   private void addFooter(IHeader footer) {

      Reject.ifNull(footer, "footer");

      footer.initParent(this);

      m_footers.add(footer);
   }

   /**
    * @param header
    */
   private void addHeader(IHeader header) {

      Reject.ifNull(header, "header");

      header.initParent(this);

      m_headers.add(header);
   }

   private void setPayload(IPayload payload) {

      Reject.ifNull(payload, "payload");

      m_payload = payload;
      m_payload.initParent(this);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IContainer#getHeaders()
    */
   @Override
   public List<IHeader> getHeaders() {

      return Collections.unmodifiableList(m_headers);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IContainer#getFooters()
    */
   @Override
   public List<IHeader> getFooters() {

      return Collections.unmodifiableList(m_footers);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IContainer#getPayload()
    */
   @Override
   public IPayload getPayload() {

      return m_payload;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.type.IDataBlock#getTotalSize()
    */
   @Override
   public long getTotalSize() {

      long totalSize = 0;

      for (Iterator<IHeader> fieldIterator = m_headers.iterator(); fieldIterator
         .hasNext();) {
         IHeader header = fieldIterator.next();

         totalSize += header.getTotalSize();
      }

      for (Iterator<IHeader> fieldIterator = m_footers.iterator(); fieldIterator
         .hasNext();) {
         IHeader footer = fieldIterator.next();

         totalSize += footer.getTotalSize();
      }

      totalSize += m_payload.getTotalSize();

      return totalSize;
   }

   private final List<IHeader> m_headers = new ArrayList<>();

   private final List<IHeader> m_footers = new ArrayList<>();

   private IPayload m_payload;
}
