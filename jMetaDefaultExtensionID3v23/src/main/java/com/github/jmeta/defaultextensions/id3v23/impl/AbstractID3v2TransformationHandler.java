
package com.github.jmeta.defaultextensions.id3v23.impl;

import java.nio.ByteBuffer;

import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.services.DataBlockReader;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractID3v2TransformationHandler} is the base class for all possible ID3v2 tag or frame transformations.
 */
// TODO: Proper testing required
public abstract class AbstractID3v2TransformationHandler {

   private static final int MAX_ID3V2_PAYLOAD_SIZE = (1 << 28) - 1;

   private final ID3v2TransformationType transformationType;

   private final DataBlockFactory dataBlockFactory;

   /**
    * Creates a new {@link AbstractID3v2TransformationHandler}.
    *
    * @param transformationType
    *           The {@link ID3v2TransformationType} identifying the type of transformation
    * @param dataBlockFactory
    *           The {@link DataBlockFactory} for creating transformed containers
    */
   public AbstractID3v2TransformationHandler(ID3v2TransformationType transformationType,
      DataBlockFactory dataBlockFactory) {
      Reject.ifNull(transformationType, "transformationType");
      Reject.ifNull(dataBlockFactory, "dataBlockFactory");

      this.transformationType = transformationType;
      this.dataBlockFactory = dataBlockFactory;
   }

   public ID3v2TransformationType getTransformationType() {

      return transformationType;
   }

   public abstract boolean requiresTransform(Container container);

   public abstract boolean requiresUntransform(Container container);

   public Container transform(Container container, DataBlockReader reader) {
      Reject.ifNull(container, "container");
      Reject.ifFalse(requiresTransform(container), "requiresTransform(container)");

      Payload payload = container.getPayload();

      if (payload.getSize() > MAX_ID3V2_PAYLOAD_SIZE) {
         throw new IllegalStateException("The size of an ID3v2 container must not exceed 2^28-1 bytes");
      }

      // Intentional cast to int due to size limitation of ID3v2 containers to 2^28-1
      int size = (int) payload.getSize();

      ByteBuffer payloadBytes = payload.getBytes(container.getOffset(), size);
      byte[][] transformedPayloadBytes = transformRawBytes(payloadBytes);

      payload.setBytes(transformedPayloadBytes);

      return getDataBlockFactory().createPersistedContainer(container.getId(), container.getSequenceNumber(), container.getParent(),
         container.getOffset(), container.getHeaders(), payload, container.getFooters(), reader,
         container.getContainerContext());
   }

   public Container untransform(Container container, DataBlockReader reader) {

      Reject.ifNull(container, "container");
      Reject.ifFalse(requiresUntransform(container), "requiresUntransform(container)");

      Payload payload = container.getPayload();

      if (payload.getSize() > MAX_ID3V2_PAYLOAD_SIZE) {
         throw new IllegalStateException("The size of an ID3v2 container must not exceed 2^28-1 bytes");
      }

      // Intentional cast to int due to size limitation of ID3v2 containers to 2^28-1
      int size = (int) payload.getSize();

      ByteBuffer payloadBytes = payload.getBytes(container.getOffset(), size);
      byte[][] untransformedPayloadBytes = untransformRawBytes(payloadBytes);

      payload.setBytes(untransformedPayloadBytes);

      return getDataBlockFactory().createPersistedContainer(container.getId(), container.getSequenceNumber(), container.getParent(),
         container.getOffset(), container.getHeaders(), payload, container.getFooters(), reader,
         container.getContainerContext());
   }

   protected abstract byte[][] transformRawBytes(ByteBuffer payloadBytes);

   protected abstract byte[][] untransformRawBytes(ByteBuffer payloadBytes);

   /**
    * Returns the {@link DataBlockFactory} for creating transformed containers
    *
    * @return the {@link DataBlockFactory} for creating transformed containers
    */
   protected DataBlockFactory getDataBlockFactory() {

      return dataBlockFactory;
   }
}