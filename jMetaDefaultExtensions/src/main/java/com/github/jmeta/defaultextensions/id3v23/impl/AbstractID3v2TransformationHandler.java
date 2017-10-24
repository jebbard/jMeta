
package com.github.jmeta.defaultextensions.id3v23.impl;

import com.github.jmeta.library.datablocks.api.services.AbstractTransformationHandler;
import com.github.jmeta.library.datablocks.api.services.DataBlockFactory;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.types.DataTransformationType;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractID3v2TransformationHandler}
 *
 */
public abstract class AbstractID3v2TransformationHandler extends AbstractTransformationHandler {

   private static final int MAX_ID3V2_PAYLOAD_SIZE = (1 << 28) - 1;

   /**
    * Creates a new {@link AbstractID3v2TransformationHandler}.
    * 
    * @param dtt
    * @param handlerId
    * @param dbFactory
    * @param logging
    */
   public AbstractID3v2TransformationHandler(DataTransformationType dtt, int handlerId, DataBlockFactory dbFactory) {
      super(dtt, dbFactory);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.TransformationHandler#transform(com.github.jmeta.library.datablocks.api.types.Container)
    */
   @Override
   public Container transform(Container container) {

      Reject.ifNull(container, "container");
      Reject.ifFalse(requiresTransform(container), "requiresTransform(container)");

      Payload payload = container.getPayload();

      if (payload.getTotalSize() > MAX_ID3V2_PAYLOAD_SIZE)
         throw new IllegalStateException("The size of an ID3v2 container must not exceed 2^28-1 bytes");

      // Intentional cast to int due to size limitation of ID3v2 containers to 2^28-1
      int size = (int) payload.getTotalSize();

      byte[] payloadBytes = payload.getBytes(0, size);
      byte[][] transformedPayloadBytes = transformRawBytes(payloadBytes);

      payload.setBytes(transformedPayloadBytes);

      return getDataBlockFactory().createContainer(container.getId(), container.getParent(),
         container.getMediumReference(), container.getHeaders(), payload, container.getFooters());
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.TransformationHandler#untransform(com.github.jmeta.library.datablocks.api.types.Container)
    */
   @Override
   public Container untransform(Container container) {

      Reject.ifNull(container, "container");
      Reject.ifFalse(requiresUntransform(container), "requiresUntransform(container)");

      Payload payload = container.getPayload();

      if (payload.getTotalSize() > MAX_ID3V2_PAYLOAD_SIZE)
         throw new IllegalStateException("The size of an ID3v2 container must not exceed 2^28-1 bytes");

      // Intentional cast to int due to size limitation of ID3v2 containers to 2^28-1
      int size = (int) payload.getTotalSize();

      byte[] payloadBytes = payload.getBytes(0, size);
      byte[][] untransformedPayloadBytes = untransformRawBytes(payloadBytes);

      payload.setBytes(untransformedPayloadBytes);

      return getDataBlockFactory().createContainer(container.getId(), container.getParent(),
         container.getMediumReference(), container.getHeaders(), payload, container.getFooters());
   }

   /**
    * @param payloadBytes
    * @return the transformed bytes
    */
   protected abstract byte[][] transformRawBytes(byte[] payloadBytes);

   /**
    * @param payloadBytes
    * @return the untransformed bytes
    */
   protected abstract byte[][] untransformRawBytes(byte[] payloadBytes);
}