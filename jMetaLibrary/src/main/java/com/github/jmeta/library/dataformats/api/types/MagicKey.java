/**
 * {@link MagicKey}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.types;

import java.nio.ByteBuffer;
import java.util.Arrays;

import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * This class represents a magic key used for identifying the data format of a given container.
 */
// TODO add testcase class for class
public class MagicKey {

   private final byte[] magicKeyBytes;

   private final int bitLength;

   private final long deltaOffset;

   private final DataBlockId headerOrFooterBlockId;

   private final String stringRepresentation;

   /**
    * Creates a new {@link MagicKey}. Use this constructor if the magic key has no human-readable string representation
    * and an odd length, i.e. only covers bytes partially.
    * 
    * @param magicKeyBytes
    *           The magic key's bytes indicating presence of a container if found or not found, depending on the
    *           concrete subclass; the string representation of the magic key is just containing the raw bytes as array,
    *           e.g. "[1, 2, 144]"
    * @param bitLength
    *           The length of the magic key in bits
    * @param headerBlockId
    *           The id of the header or footer this magic key is occurring in
    * @param deltaOffset
    *           The delta offset of the first byte of the magic key from start of header (positive offset) or from the
    *           end of the footer (negative offset) it is contained in
    */
   public MagicKey(byte[] magicKeyBytes, int bitLength, DataBlockId headerBlockId, long deltaOffset) {
      Reject.ifNull(magicKeyBytes, "magicKeyBytes");
      Reject.ifNull(headerBlockId, "headerBlockId");
      Reject.ifNegativeOrZero(bitLength, "bitLength");
      Reject.ifFalse(bitLength <= magicKeyBytes.length * Byte.SIZE, "bitLength <= magicKeyBytes.length * Byte.SIZE");

      this.magicKeyBytes = magicKeyBytes.clone();
      this.bitLength = bitLength;
      this.stringRepresentation = Arrays.toString(magicKeyBytes);
      this.headerOrFooterBlockId = headerBlockId;
      this.deltaOffset = deltaOffset;
   }

   /**
    * Creates a new {@link MagicKey}. Use this constructor if the magic key has no human-readable string representation
    * but only covers full bytes.
    * 
    * @param magicKeyBytes
    *           The magic key's bytes indicating presence of a container if found or not found, depending on the
    *           concrete subclass; the string representation of the magic key is just containing the raw bytes as array,
    *           e.g. "[1, 2, 144]"
    * @param headerBlockId
    *           The id of the header or footer this magic key is occurring in
    * @param deltaOffset
    *           The delta offset of the first byte of the magic key from start of header (positive offset) or from the
    *           end of the footer (negative offset) it is contained in
    */
   public MagicKey(byte[] magicKeyBytes, DataBlockId headerBlockId, long deltaOffset) {
      this(magicKeyBytes, magicKeyBytes != null ? magicKeyBytes.length * Byte.SIZE : 0, headerBlockId, deltaOffset);
   }

   /**
    * Creates a new {@link MagicKey}. Use this constructor if the magic key has a human-readable ASCII string
    * representation and only covers full bytes.
    * 
    * @param asciiKey
    *           A string containing only 7 bit standard ASCII characters which is the human-readable magic key
    * @param headerBlockId
    *           The id of the header or footer this magic key is occurring in
    * @param deltaOffset
    *           The delta offset of the first byte of the magic key from start of header (positive offset) or from the
    *           end of the footer (negative offset) it is contained in
    */
   public MagicKey(String asciiKey, DataBlockId headerBlockId, long deltaOffset) {
      this(asciiKey != null ? asciiKey.getBytes(Charsets.CHARSET_ASCII) : null, headerBlockId, deltaOffset);
   }

   /**
    * @return a clone of the magic key bytes
    */
   public byte[] getMagicKeyBytes() {
      return magicKeyBytes.clone();
   }

   /**
    * @return the string representation of the magic key bytes
    */
   public String getStringRepresentation() {
      return stringRepresentation;
   }

   /**
    * @return the bit length
    */
   public int getBitLength() {
      return bitLength;
   }

   /**
    * @return the byte length, i.e. the number of fully or partly covered bytes
    */
   public int getByteLength() {
      return getBitLength() / Byte.SIZE + (getBitLength() % Byte.SIZE != 0 ? 1 : 0);
   }

   /**
    * @return the header or footer id this magic key can be found in
    */
   public DataBlockId getHeaderOrFooterId() {
      return headerOrFooterBlockId;
   }

   /**
    * @return delta offset from start of header or end of footer
    */
   public long getDeltaOffset() {
      return deltaOffset;
   }

   /**
    * Indicates whether the container whose presence is determined by this magic key is present at the beginning of the
    * given bytes or not. The bytes are NOT scanned for the magic key. Either the magic key is found at the beginning of
    * the bytes, or not.
    * 
    * <b>NOTE:</b> A concrete implementation might detect presence (i.e. return true) if the magic key bytes are found,
    * or it might also detect presence if the magic key is not found.
    * 
    * @param bytesToCheckForMagicKey
    *           The bytes to look for the magic key
    * @return true if the container's presence is indicated by the given bytes, false otherwise
    */
   public boolean isContainerPresent(ByteBuffer readBytes) {
      Reject.ifNull(readBytes, "readBytes");

      int comparedBits = 0;

      if (readBytes.remaining() < magicKeyBytes.length) {
         return false;
      }

      for (int i = 0; i < magicKeyBytes.length; i++) {
         final byte magicKeyByte = magicKeyBytes[i];
         final byte readByte = readBytes.get();

         if (getBitLength() - comparedBits < Byte.SIZE) {
            byte bitMask = 0;

            for (int j = 1; j <= getBitLength() % Byte.SIZE; ++j) {
               bitMask |= (1 << Byte.SIZE - j);
            }

            if ((bitMask & readByte) != magicKeyByte) {
               return false;
            }
         }

         else if (magicKeyByte != readByte) {
            return false;
         }

         comparedBits += Byte.SIZE;
      }

      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return getClass().getName() + "[" + "magicKeyBytes=" + magicKeyBytes + ", bitLength=" + bitLength
         + ", stringRepresentation=" + stringRepresentation + "]";
   }
}
