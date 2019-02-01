/**
 * {@link PayloadContainerIterator}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockIterator;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.FieldFunctionStack;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class PayloadContainerIterator extends AbstractDataBlockIterator<Container> {

   /**
    * @see java.io.Closeable#close()
    */
   @Override
   public void close() throws IOException {
      // Closing a PayloadContainerIterator has no effect
   }

   /**
    * Creates a new instance of {@link PayloadContainerIterator}.
    *
    * @param parent
    * @param reader
    * @param reference
    * @param context
    */
   public PayloadContainerIterator(Payload parent, DataBlockReader reader, MediumOffset reference,
      FieldFunctionStack context) {
      Reject.ifNull(parent, "parent");
      Reject.ifNull(reader, "reader");
      Reject.ifNull(reference, "reference");
      Reject.ifNull(context, "context");

      m_parent = parent;
      m_nextContainerReference = reference;
      m_reader = reader;
      m_context = context;
      m_remainingParentSize = m_parent.getTotalSize();

      DataBlockDescription parentDescription = m_reader.getSpecification().getDataBlockDescription(m_parent.getId());

      m_containerDescs = parentDescription.getChildDescriptionsOfType(PhysicalDataBlockType.CONTAINER);
   }

   /**
    * @see java.util.Iterator#hasNext()
    */
   @Override
   public boolean hasNext() {

      // If the parent size is known: The already read containers reach to the end of the
      // payload => no further children available
      long remainingParentByteCount = DataBlockDescription.UNDEFINED;

      if (m_parent.getTotalSize() != DataBlockDescription.UNDEFINED) {
         remainingParentByteCount = m_parent.getTotalSize() - (m_nextContainerReference.getAbsoluteMediumOffset()
            - m_parent.getMediumReference().getAbsoluteMediumOffset());

         if (remainingParentByteCount <= 0)
            return false;
      }

      List<DataBlockDescription> nestedContainerDescsWithMagicKeys = getNestedContainerDescsWithMagicKeys();

      // We need to evaluate whether there still is a container child
      for (int i = 0; i < nestedContainerDescsWithMagicKeys.size(); ++i) {
         DataBlockDescription containerDesc = nestedContainerDescsWithMagicKeys.get(i);

         if (m_reader.hasContainerWithId(m_nextContainerReference, containerDesc.getId(), m_parent,
            remainingParentByteCount, true))
            return true;
      }

      // If no nested container with magic key was found, we assume the default nested container
      DataBlockDescription containerDesc = m_reader.getSpecification().getDefaultNestedContainerDescription();
      if (containerDesc != null) {
         return m_reader.hasContainerWithId(m_nextContainerReference, containerDesc.getId(), m_parent,
            remainingParentByteCount, true);
      }

      // The payload has no container children at all OR no containers have been
      // identified
      return false;
   }

   private List<DataBlockDescription> getNestedContainerDescsWithMagicKeys() {
      return m_containerDescs.stream().filter((desc) -> !desc.getHeaderMagicKeys().isEmpty())
         .collect(Collectors.toList());
   }

   /**
    * @see java.util.Iterator#next()
    */
   @Override
   public Container next() {

      Reject.ifFalse(hasNext(), "hasNext()");

      List<DataBlockDescription> nestedContainerDescsWithMagicKeys = getNestedContainerDescsWithMagicKeys();

      for (int i = 0; i < nestedContainerDescsWithMagicKeys.size(); ++i) {
         DataBlockDescription containerDesc = nestedContainerDescsWithMagicKeys.get(i);

         if (m_reader.hasContainerWithId(m_nextContainerReference, containerDesc.getId(), m_parent,
            m_remainingParentSize, true)) {
            Container container = m_reader.readContainerWithId(m_nextContainerReference, containerDesc.getId(),
               m_parent, m_context, m_remainingParentSize);

            if (container != null) {
               updateProgress(container);

               return container;
            }
         }
      }

      // If no nested container with magic key was found, we assume the default nested container
      DataBlockDescription containerDesc = m_reader.getSpecification().getDefaultNestedContainerDescription();

      if (containerDesc != null) {
         Container container = m_reader.readContainerWithId(m_nextContainerReference, containerDesc.getId(), m_parent,
            m_context, m_remainingParentSize);

         if (container != null) {
            updateProgress(container);

            return container;
         }
      }

      throw new IllegalStateException(
         "No child container found for payload " + m_parent + " at " + m_nextContainerReference);
   }

   private void updateProgress(Container container) {
      m_nextContainerReference = m_nextContainerReference.advance(container.getTotalSize());

      if (m_remainingParentSize != DataBlockDescription.UNDEFINED)
         m_remainingParentSize -= container.getTotalSize();
   }

   private final FieldFunctionStack m_context;

   private final Payload m_parent;

   private long m_remainingParentSize;

   private MediumOffset m_nextContainerReference;

   private final DataBlockReader m_reader;

   private List<DataBlockDescription> m_containerDescs;
}
