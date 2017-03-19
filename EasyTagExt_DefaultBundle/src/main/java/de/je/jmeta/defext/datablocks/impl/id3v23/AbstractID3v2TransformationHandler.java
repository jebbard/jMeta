
package de.je.jmeta.defext.datablocks.impl.id3v23;

import de.je.jmeta.datablocks.IContainer;
import de.je.jmeta.datablocks.IDataBlockFactory;
import de.je.jmeta.datablocks.IPayload;
import de.je.jmeta.datablocks.export.AbstractTransformationHandler;
import de.je.jmeta.dataformats.DataTransformationType;
import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

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
   public AbstractID3v2TransformationHandler(DataTransformationType dtt, int handlerId, IDataBlockFactory dbFactory) {
      super(dtt, dbFactory);
   }

   /**
    * @see de.je.jmeta.datablocks.ITransformationHandler#transform(de.je.jmeta.datablocks.IContainer)
    */
   @Override
   public IContainer transform(IContainer container) {

      Reject.ifNull(container, "container");
      Contract.checkPrecondition(requiresTransform(container), "requiresTransform(container)");

      IPayload payload = container.getPayload();

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
    * @see de.je.jmeta.datablocks.ITransformationHandler#untransform(de.je.jmeta.datablocks.IContainer)
    */
   @Override
   public IContainer untransform(IContainer container) {

      Reject.ifNull(container, "container");
      Contract.checkPrecondition(requiresUntransform(container), "requiresUntransform(container)");

      IPayload payload = container.getPayload();

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