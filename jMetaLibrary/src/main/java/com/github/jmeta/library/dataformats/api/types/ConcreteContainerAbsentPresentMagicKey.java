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
 * {@link ConcreteContainerAbsentPresentMagicKey} is a magic key that indicates <i>absence</i> of a concrete container.
 * Depending on the data format, this either also proves the <i>presence of a generic container type</i> or it proves
 * that the end of payload is reached. So this kind of magic key is used for two cases:
 * <ul>
 * <li>For data formats where the payload size is known in advance: It is used as magic key of a generic container; if
 * the magic key bytes <i>are not</i> found at the current offset, this indicates the presence of the generic container
 * type</li>
 * <li>For data formats where the payload size is NOT known in advance but must be determined by reading all nested
 * containers: It is used as magic key the follow-up datablock or footer; if the magic key bytes <i>are</i> found at the
 * current offset, this indicates that the end of payload is reached and the absence of yet another generic
 * container</li>
 * </ul>
 * 
 * The sole difference to {@link ConcreteContainerPresentMagicKey} is: A generic container is identified to be present
 * iff a given byte sequence <i>is not</i> found at the current byte offset.
 */
public class ConcreteContainerAbsentPresentMagicKey extends AbstractMagicKey {

   /**
    * Creates a new {@link ConcreteContainerAbsentPresentMagicKey}.
    * 
    * @see AbstractMagicKey#AbstractMagicKey(byte[], int, DataBlockId, long, long)
    */
   public ConcreteContainerAbsentPresentMagicKey(byte[] magicKeyBytes, int bitLength, DataBlockId headerBlockId,
      long offsetForBackwardReading, long offsetFromStartOfHeaderOrFooter) {
      super(magicKeyBytes, bitLength, headerBlockId, offsetForBackwardReading, offsetFromStartOfHeaderOrFooter);
   }

   /**
    * Creates a new {@link ConcreteContainerAbsentPresentMagicKey}.
    * 
    * @see AbstractMagicKey#AbstractMagicKey(byte[], DataBlockId, long, long)
    */
   public ConcreteContainerAbsentPresentMagicKey(byte[] magicKeyBytes, DataBlockId headerBlockId,
      long offsetForBackwardReading, long offsetFromStartOfHeaderOrFooter) {
      super(magicKeyBytes, headerBlockId, offsetForBackwardReading, offsetFromStartOfHeaderOrFooter);
   }

   /**
    * Creates a new {@link ConcreteContainerAbsentPresentMagicKey}.
    * 
    * @see AbstractMagicKey#AbstractMagicKey(String, DataBlockId, long, long)
    */
   public ConcreteContainerAbsentPresentMagicKey(String asciiKey, DataBlockId headerBlockId,
      long offsetForBackwardReading, long offsetFromStartOfHeaderOrFooter) {
      super(asciiKey, headerBlockId, offsetForBackwardReading, offsetFromStartOfHeaderOrFooter);
   }

   /**
    * @see com.github.jmeta.library.dataformats.api.types.AbstractMagicKey#isContainerPresent(java.nio.ByteBuffer)
    */
   @Override
   public boolean isContainerPresent(ByteBuffer readBytes) {
      return !equalsBytes(readBytes);
   }
}
