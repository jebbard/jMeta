/**
 * {@link PayloadContainerIterator}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:08 (December 31, 2010)
 */

package com.github.jmeta.library.datablocks.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.github.jmeta.library.datablocks.api.services.ContainerIterator;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.types.DataBlockDescription;
import com.github.jmeta.library.dataformats.api.types.DataBlockId;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 *
 */
public class PayloadContainerIterator implements ContainerIterator {

   /**
    * @see com.github.jmeta.library.datablocks.api.services.ContainerIterator#remove()
    */
   @Override
   public void remove() {
   }

   private final Map<DataBlockId, Integer> nextSequenceNumber = new HashMap<>();

   /**
    * Creates a new instance of {@link PayloadContainerIterator}.
    *
    * @param parent
    * @param reader
    * @param reference
    * @param context
    */
   public PayloadContainerIterator(Payload parent, DataBlockReader reader, MediumOffset reference) {
      Reject.ifNull(parent, "parent");
      Reject.ifNull(reader, "reader");
      Reject.ifNull(reference, "reference");

      m_parent = parent;
      m_nextContainerReference = reference;
      m_reader = reader;
      m_remainingParentSize = m_parent.getSize();

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

      if (m_parent.getSize() != DataBlockDescription.UNDEFINED) {
         remainingParentByteCount = m_parent.getSize()
            - (m_nextContainerReference.getAbsoluteMediumOffset() - m_parent.getOffset().getAbsoluteMediumOffset());

         if (remainingParentByteCount <= 0) {
            return false;
         }
      }

      List<DataBlockDescription> nestedContainerDescsWithMagicKeys = getNestedContainerDescsWithMagicKeys();

      // We need to evaluate whether there still is a container child
      for (int i = 0; i < nestedContainerDescsWithMagicKeys.size(); ++i) {
         DataBlockDescription containerDesc = nestedContainerDescsWithMagicKeys.get(i);

         if (m_reader.hasContainerWithId(m_nextContainerReference, containerDesc.getId(), m_parent,
            remainingParentByteCount)) {
            return true;
         }
      }

      // If no nested container with magic key was found, we assume the default nested container
      DataBlockDescription containerDesc = m_reader.getSpecification().getDefaultNestedContainerDescription();
      if (containerDesc != null) {
         return m_reader.hasContainerWithId(m_nextContainerReference, containerDesc.getId(), m_parent,
            remainingParentByteCount);
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

         DataBlockId containerId = containerDesc.getId();

         if (m_reader.hasContainerWithId(m_nextContainerReference, containerId, m_parent, m_remainingParentSize)) {

            Container nextContainer = readNextContainer(containerId);

            if (nextContainer != null) {
               return nextContainer;
            }
         }
      }

      // If no nested container with magic key was found, we assume the default nested container
      DataBlockDescription containerDesc = m_reader.getSpecification().getDefaultNestedContainerDescription();

      if (containerDesc != null) {
         Container nextContainer = readNextContainer(containerDesc.getId());

         if (nextContainer != null) {
            return nextContainer;
         }
      }

      throw new IllegalStateException(
         "No child container found for payload " + m_parent + " at " + m_nextContainerReference);
   }

   /**
    * @param containerId
    */
   private Container readNextContainer(DataBlockId containerId) {
      int sequenceNumber = 0;

      if (nextSequenceNumber.containsKey(containerId)) {
         sequenceNumber = nextSequenceNumber.get(containerId);
      }

      Container container = m_reader.readContainerWithId(m_nextContainerReference, containerId, m_parent,
         m_remainingParentSize, sequenceNumber, m_parent.getContainerContext());

      nextSequenceNumber.put(containerId, sequenceNumber + 1);

      if (container != null) {
         updateProgress(container);
      }

      return container;
   }

   private void updateProgress(Container container) {
      m_nextContainerReference = m_nextContainerReference.advance(container.getSize());

      if (m_remainingParentSize != DataBlockDescription.UNDEFINED) {
         m_remainingParentSize -= container.getSize();
      }
   }

   private final Payload m_parent;

   private long m_remainingParentSize;

   private MediumOffset m_nextContainerReference;

   private final DataBlockReader m_reader;

   private List<DataBlockDescription> m_containerDescs;
}
