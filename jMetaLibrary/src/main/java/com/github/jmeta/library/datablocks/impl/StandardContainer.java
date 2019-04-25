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

import com.github.jmeta.library.datablocks.api.services.CountProvider;
import com.github.jmeta.library.datablocks.api.services.SizeProvider;
import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Footer;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatSpecification;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class StandardContainer extends AbstractDataBlock implements Container {

   /**
    * Creates a new {@link StandardContainer}.
    *
    * @param id
    * @param spec
    */
   public StandardContainer(DataBlockId id, DataFormatSpecification spec) {
      super(id, spec);
   }

   /**
    * @param footer
    */
   @Override
   public void insertFooter(int index, Footer footer) {

      Reject.ifNull(footer, "footer");
      Reject.ifNotInInterval(index, 0, m_footers.size(), "index");

      footer.initParent(this);

      m_footers.add(index, footer);
   }

   /**
    * @param index
    *           TODO
    * @param header
    */
   @Override
   public void insertHeader(int index, Header header) {
      Reject.ifNull(header, "header");
      Reject.ifNotInInterval(index, 0, m_headers.size(), "index");

      header.initParent(this);

      m_headers.add(index, header);
   }

   @Override
   public void setPayload(Payload payload) {

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
   public List<Footer> getFooters() {

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
    * @see com.github.jmeta.library.datablocks.api.types.DataBlock#getSize()
    */
   @Override
   public long getSize() {

      long totalSize = 0;

      for (Iterator<Header> fieldIterator = m_headers.iterator(); fieldIterator.hasNext();) {
         Header header = fieldIterator.next();

         totalSize += header.getSize();
      }

      for (Iterator<Footer> fieldIterator = m_footers.iterator(); fieldIterator.hasNext();) {
         Footer footer = fieldIterator.next();

         totalSize += footer.getSize();
      }

      totalSize += m_payload.getSize();

      return totalSize;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.Container#initTopLevelContainerContext(com.github.jmeta.library.datablocks.api.services.SizeProvider,
    *      com.github.jmeta.library.datablocks.api.services.CountProvider)
    */
   @Override
   public void initTopLevelContainerContext(SizeProvider sizeProvider, CountProvider countProvider) {
      initContainerContext(new StandardContainerContext(getSpec(), null, this, sizeProvider, countProvider));
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.types.AbstractDataBlock#initParent(com.github.jmeta.library.datablocks.api.types.DataBlock)
    */
   @Override
   public void initParent(DataBlock parent) {
      super.initParent(parent);
      initContainerContext(parent.getContainerContext().createChildContainerContext(this));
   }

   private final List<Header> m_headers = new ArrayList<>();

   private final List<Footer> m_footers = new ArrayList<>();

   private Payload m_payload;
}
