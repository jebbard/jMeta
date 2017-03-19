/**
 * {@link PayloadContainerIterator}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package de.je.jmeta.datablocks.impl;

import java.util.List;

import de.je.jmeta.datablocks.AbstractDataBlockIterator;
import de.je.jmeta.datablocks.IContainer;
import de.je.jmeta.datablocks.IPayload;
import de.je.jmeta.datablocks.export.FieldFunctionStack;
import de.je.jmeta.datablocks.export.IDataBlockReader;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.jmeta.dataformats.PhysicalDataBlockType;
import de.je.jmeta.media.api.IMediumReference;
import de.je.jmeta.media.api.exception.EndOfMediumException;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

/**
 *
 */
public class PayloadContainerIterator
   extends AbstractDataBlockIterator<IContainer> {

   /**
    * Creates a new instance of {@link PayloadContainerIterator}.
    *
    * @param parent
    * @param reader
    * @param reference
    * @param context
    * @param previousFieldSize
    */
   public PayloadContainerIterator(IPayload parent, IDataBlockReader reader,
      IMediumReference reference, FieldFunctionStack context,
      long previousFieldSize) {
      Reject.ifNull(parent, "parent");
      Reject.ifNull(reader, "reader");
      Reject.ifNull(reference, "reference");
      Reject.ifNull(context, "context");

      m_parent = parent;
      m_nextContainerReference = reference;
      m_reader = reader;
      m_context = context;
      m_remainingParentSize = m_parent.getTotalSize() - previousFieldSize;

      m_containerDescs = DataBlockDescription.getChildDescriptionsOfType(
         m_reader.getSpecification(), m_parent.getId(),
         PhysicalDataBlockType.CONTAINER);
   }

   /**
    * @see java.util.Iterator#hasNext()
    */
   @Override
   public boolean hasNext() {

      // If the parent size is known: The already read containers reach to the end of the
      // payload => no further children available
      long remainingParentByteCount = DataBlockDescription.UNKNOWN_SIZE;

      if (m_parent.getId().getGlobalId().equals("ogg.payload")) {
         System.out.println("OGG payload");
      }

      if (m_parent.getTotalSize() != DataBlockDescription.UNKNOWN_SIZE) {
         remainingParentByteCount = m_parent.getTotalSize()
            - (m_nextContainerReference.getAbsoluteMediumOffset()
               - m_parent.getMediumReference().getAbsoluteMediumOffset());

         if (remainingParentByteCount <= 0)
            return false;
      }

      long minHeaderSize = m_reader
         .getShortestMinimumContainerHeaderSize(m_parent.getId());

      if (minHeaderSize != DataBlockDescription.UNKNOWN_SIZE)
         try {
            // The whole header size is cached intentionally due to the premise of small headers
            m_reader.cache(m_nextContainerReference, minHeaderSize);
         } catch (@SuppressWarnings("unused") EndOfMediumException e) {
            return false;
         }

      // We need to evaluate whether there still is a container child
      for (int i = 0; i < m_containerDescs.size(); ++i) {
         DataBlockDescription containerDesc = m_containerDescs.get(i);

         if (m_reader.hasContainerWithId(m_nextContainerReference,
            containerDesc.getId(), m_parent, remainingParentByteCount))
            return true;
      }

      // The payload has no container children at all OR no containers have been
      // identified
      return false;
   }

   /**
    * @see java.util.Iterator#next()
    */
   @Override
   public IContainer next() {

      Contract.checkPrecondition(hasNext(), "hasNext() was false");

      if (m_parent.getId().getGlobalId().equals("ogg.payload")) {
         System.out.println("OGG payload");
      }

      for (int i = 0; i < m_containerDescs.size(); ++i) {
         DataBlockDescription containerDesc = m_containerDescs.get(i);

         if (m_reader.hasContainerWithId(m_nextContainerReference,
            containerDesc.getId(), m_parent, m_remainingParentSize)) {
            IContainer container = m_reader.readContainerWithId(
               m_nextContainerReference, containerDesc.getId(), m_parent,
               m_context, m_remainingParentSize);

            if (container != null) {
               m_nextContainerReference = m_nextContainerReference
                  .advance(container.getTotalSize());

               if (m_remainingParentSize != DataBlockDescription.UNKNOWN_SIZE)
                  m_remainingParentSize -= container.getTotalSize();

               return container;
            }
         }
      }

      throw new IllegalStateException("No child container found for payload "
         + m_parent + " at " + m_nextContainerReference);
   }

   private final FieldFunctionStack m_context;

   private final IPayload m_parent;

   private long m_remainingParentSize;

   private IMediumReference m_nextContainerReference;

   private final IDataBlockReader m_reader;

   private List<DataBlockDescription> m_containerDescs;
}
