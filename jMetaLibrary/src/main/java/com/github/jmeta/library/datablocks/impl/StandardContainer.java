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

import com.github.jmeta.library.datablocks.api.types.AbstractDataBlock;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerContext;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.DataBlockState;
import com.github.jmeta.library.datablocks.api.types.Footer;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.datablocks.impl.events.DataBlockEventBus;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.media.api.types.MediumOffset;
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
    * @param mediumDataProvider
    * @param containerContext
    *           TODO
    * @param sequenceNumber
    *           TODO
    */
   public StandardContainer(DataBlockId id, DataBlock parent, MediumOffset reference, List<Header> headers,
      Payload payload, List<Footer> footers, MediumDataProvider mediumDataProvider, ContainerContext containerContext,
      int sequenceNumber, DataBlockEventBus eventBus) {
      this(id, parent, reference, mediumDataProvider, containerContext, sequenceNumber, eventBus);

      Reject.ifNull(footers, "footers");
      Reject.ifNull(payload, "payload");
      Reject.ifNull(headers, "headers");

      for (int i = 0; i < headers.size(); ++i) {
         insertHeader(i, headers.get(i));
      }

      for (int i = 0; i < footers.size(); ++i) {
         insertFooter(i, footers.get(i));
      }

      setPayload(payload);
   }

   /**
    * Creates a new {@link StandardContainer}.
    *
    * @param id
    * @param parent
    * @param reference
    * @param mediumDataProvider
    * @param sequenceNumber
    *           TODO
    * @param eventBus
    *           TODO
    * @param parentContainerContext
    */
   public StandardContainer(DataBlockId id, DataBlock parent, MediumOffset reference,
      MediumDataProvider mediumDataProvider, ContainerContext containerContext, int sequenceNumber,
      DataBlockEventBus eventBus) {
      super(id, sequenceNumber, reference, parent, mediumDataProvider, containerContext, DataBlockState.PERSISTED,
         eventBus);
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

   private final List<Header> m_headers = new ArrayList<>();

   private final List<Footer> m_footers = new ArrayList<>();

   private Payload m_payload;
}
