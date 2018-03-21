/**
 *
 * {@link ConcreteContainerAbsentPresentMagicKey}.java
 *
 * @author Jens Ebert
 *
 * @date 21.03.2018
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import java.nio.ByteBuffer;

/**
 * {@link ConcreteContainerPresentMagicKey} is a magic key that indicates presence of a concrete container. If the magic
 * key bytes are found at a given location, the concrete container is detected to be present and can be parsed. This is
 * e.g. used during identification of a data format of a top-level container or for identification of a concrete nested
 * container type.
 * 
 * For the special case of
 */
public class ConcreteContainerPresentMagicKey extends AbstractMagicKey {

   /**
    * Creates a new {@link ConcreteContainerPresentMagicKey}.
    * 
    * @see AbstractMagicKey#AbstractMagicKey(byte[], int, DataBlockId, long, long)
    */
   public ConcreteContainerPresentMagicKey(byte[] magicKeyBytes, int bitLength, DataBlockId headerBlockId,
      long offsetForBackwardReading, long offsetFromStartOfHeaderOrFooter) {
      super(magicKeyBytes, bitLength, headerBlockId, offsetForBackwardReading, offsetFromStartOfHeaderOrFooter);
   }

   /**
    * Creates a new {@link ConcreteContainerPresentMagicKey}.
    * 
    * @see AbstractMagicKey#AbstractMagicKey(byte[], DataBlockId, long, long)
    */
   public ConcreteContainerPresentMagicKey(byte[] magicKeyBytes, DataBlockId headerBlockId,
      long offsetForBackwardReading, long offsetFromStartOfHeaderOrFooter) {
      super(magicKeyBytes, headerBlockId, offsetForBackwardReading, offsetFromStartOfHeaderOrFooter);
   }

   /**
    * Creates a new {@link ConcreteContainerPresentMagicKey}.
    * 
    * @see AbstractMagicKey#AbstractMagicKey(String, DataBlockId, long, long)
    */
   public ConcreteContainerPresentMagicKey(String asciiKey, DataBlockId headerBlockId, long offsetForBackwardReading,
      long offsetFromStartOfHeaderOrFooter) {
      super(asciiKey, headerBlockId, offsetForBackwardReading, offsetFromStartOfHeaderOrFooter);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractMagicKey#isContainerPresent(java.nio.ByteBuffer)
    */
   @Override
   public boolean isContainerPresent(ByteBuffer readBytes) {
      return equalsBytes(readBytes);
   }
}
