/**
 * {@link MagicKey}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package de.je.jmeta.dataformats;

import java.nio.ByteBuffer;

import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

/**
 *
 */
public class MagicKey {

   /**
    * @return offset from start of header or footer
    */
   public long getOffsetFromStartOfHeaderOrFooter() {

      return m_offsetFromStartOfHeaderOrFooter;
   }

   /**
    *
    */
   public final static long NO_BACKWARD_READING = 0;

   /**
    * Creates a new {@link MagicKey}.
    * 
    * @param magicKeyBytes
    * @param bitLength
    * @param stringRepresentation
    * @param headerBlockId
    * @param offsetForBackwardReading
    * @param offsetFromStartOfHeaderOrFooter
    */
   public MagicKey(byte[] magicKeyBytes, int bitLength,
      String stringRepresentation, DataBlockId headerBlockId,
      long offsetForBackwardReading, long offsetFromStartOfHeaderOrFooter) {
      Reject.ifNull(magicKeyBytes, "magicKeyBytes");
      Reject.ifNull(stringRepresentation, "stringRepresentation");
      Reject.ifNull(headerBlockId, "headerBlockId");
      Reject.ifTrue(offsetFromStartOfHeaderOrFooter < 0,
         "The offset from start of header or footer must be >= 0.");
      Contract.checkPrecondition(bitLength > 0,
         "The bit length must be bigger than 0.");
      Contract.checkPrecondition(bitLength <= magicKeyBytes.length * Byte.SIZE,
         "The bit length must not be bigger than length of the magic key bytes * sizeof(Byte)");
      Contract.checkPrecondition(offsetForBackwardReading <= 0,
         "The backward reading offset must be <= 0.");

      m_magicKeyBytes = magicKeyBytes.clone();
      m_bitLength = bitLength;
      m_stringRepresentation = stringRepresentation;
      m_exclusionBytes = null;
      m_headerOrFooterBlockId = headerBlockId;
      m_headerOrFooterOffsetForBackwardReading = offsetForBackwardReading;
      m_offsetFromStartOfHeaderOrFooter = offsetFromStartOfHeaderOrFooter;
   }

   /**
    * @return header or footer offset for backward reading
    */
   public long getHeaderOrFooterOffsetForBackwardReading() {

      return m_headerOrFooterOffsetForBackwardReading;
   }

   /**
    * Creates a new {@link MagicKey}.
    * 
    * @param exclusionBytes
    * @param bitLength
    * @param headerOfFooterBlockId
    * @param offsetFromStartOfHeaderOrFooter
    */
   public MagicKey(byte[] exclusionBytes, int bitLength,
      DataBlockId headerOfFooterBlockId, long offsetFromStartOfHeaderOrFooter) {
      Reject.ifNull(exclusionBytes, "exclusionBytes");
      Reject.ifNull(headerOfFooterBlockId, "headerBlockId");
      Reject.ifTrue(bitLength < 1, "The bit length must be bigger than 0.");
      Reject.ifTrue(offsetFromStartOfHeaderOrFooter < 0,
         "The offset from start of header or footer must be >= 0.");
      Contract.checkPrecondition(bitLength <= exclusionBytes.length * Byte.SIZE,
         "The bit length must not be bigger than length of the exclusion bytes * sizeof(Byte)");

      m_magicKeyBytes = null;
      m_bitLength = bitLength;
      m_stringRepresentation = "";
      m_exclusionBytes = exclusionBytes.clone();
      m_headerOrFooterBlockId = headerOfFooterBlockId;
      m_headerOrFooterOffsetForBackwardReading = NO_BACKWARD_READING;
      m_offsetFromStartOfHeaderOrFooter = offsetFromStartOfHeaderOrFooter;
   }

   /**
    * @return the magic key bytes
    */
   public byte[] getMagicKeyBytes() {

      Contract.checkPrecondition(!isExclusionKey(),
         "isExclusionKey() was true");

      return m_magicKeyBytes.clone();
   }

   /**
    * Returns exclusionBytes
    *
    * @return exclusionBytes
    */
   public byte[] getExclusionBytes() {

      Contract.checkPrecondition(isExclusionKey(),
         "isExclusionKey() was false");

      return m_exclusionBytes.clone();
   }

   /**
    * @return true if exclusion key, false otherwise
    */
   public boolean isExclusionKey() {

      return m_exclusionBytes != null;
   }

   /**
    * @return the bit length
    */
   public int getBitLength() {

      return m_bitLength;
   }

   /**
    * @return the string representation
    */
   public String getStringRepresentation() {

      return m_stringRepresentation;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[" + "magicKeyBytes=" + m_magicKeyBytes
         + ", bitLength=" + m_bitLength + ", stringRepresentation="
         + m_stringRepresentation + "]";
   }

   /**
    * @return the header or footer id
    */
   public DataBlockId getHeaderOrFooterId() {

      return m_headerOrFooterBlockId;
   }

   /**
    * @param readBytes
    * @return true if equal, false otherwise
    */
   public boolean equalsBytes(ByteBuffer readBytes) {

      Reject.ifNull(readBytes, "readBytes");

      int comparedBits = 0;

      final byte[] magicKeyBytes = (isExclusionKey() ? getExclusionBytes()
         : getMagicKeyBytes());

      if (readBytes.remaining() < magicKeyBytes.length)
         return false;

      for (int i = 0; i < magicKeyBytes.length; i++) {
         final byte magicKeyByte = magicKeyBytes[i];
         final byte readByte = readBytes.get();

         if (getBitLength() - comparedBits < Byte.SIZE) {
            byte bitMask = 0;

            for (int j = 1; j <= getBitLength() % Byte.SIZE; ++j)
               bitMask |= (1 << Byte.SIZE - j);

            if ((bitMask & readByte) != magicKeyByte)
               return false;
         }

         else if (magicKeyByte != readByte)
            return false;

         comparedBits += Byte.SIZE;
      }

      return true;
   }

   private final byte[] m_magicKeyBytes;

   private final byte[] m_exclusionBytes;

   private final int m_bitLength;

   private final long m_headerOrFooterOffsetForBackwardReading;

   private final long m_offsetFromStartOfHeaderOrFooter;

   private final DataBlockId m_headerOrFooterBlockId;

   private final String m_stringRepresentation;
}
