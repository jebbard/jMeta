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

import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class StandardContainer extends AbstractDataBlock implements Container {

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
   public StandardContainer(DataBlockId id, DataBlock parent,
      MediumReference reference, List<Header> headers, Payload payload,
      List<Header> footers, DataBlockReader dataBlockReader) {
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
   private void addFooter(Header footer) {

      Reject.ifNull(footer, "footer");

      footer.initParent(this);

      m_footers.add(footer);
   }

   /**
    * @param header
    */
   private void addHeader(Header header) {

      Reject.ifNull(header, "header");

      header.initParent(this);

      m_headers.add(header);
   }

   private void setPayload(Payload payload) {

      Reject.ifNull(payload, "payload");

      m_payload = payload;
      m_payload.initParent(this);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Container#getHeaders()
    */
   @Override
   public List<Header> getHeaders() {

      return Collections.unmodifiableList(m_headers);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Container#getFooters()
    */
   @Override
   public List<Header> getFooters() {

      return Collections.unmodifiableList(m_footers);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Container#getPayload()
    */
   @Override
   public Payload getPayload() {

      return m_payload;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getTotalSize()
    */
   @Override
   public long getTotalSize() {

      long totalSize = 0;

      for (Iterator<Header> fieldIterator = m_headers.iterator(); fieldIterator
         .hasNext();) {
         Header header = fieldIterator.next();

         totalSize += header.getTotalSize();
      }

      for (Iterator<Header> fieldIterator = m_footers.iterator(); fieldIterator
         .hasNext();) {
         Header footer = fieldIterator.next();

         totalSize += footer.getTotalSize();
      }

      totalSize += m_payload.getTotalSize();

      return totalSize;
   }

   private final List<Header> m_headers = new ArrayList<>();

   private final List<Header> m_footers = new ArrayList<>();

   private Payload m_payload;
}
