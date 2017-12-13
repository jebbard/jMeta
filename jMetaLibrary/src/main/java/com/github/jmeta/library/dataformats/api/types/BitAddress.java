/**
 *
 * {@link BitAddress}.java
 *
 * @author Jens Ebert
 *
 * @date 09.01.2009
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link BitAddress} defines a position of bits within a series of bytes. This enables to define global bit positions
 * without respect to byte ordering. Bits in one byte are always arranged left (highest) to right (lowest) bit - where
 * lowest bit has index 0 and highest bit has index {@link #BITS_PER_BYTE} - 1. Therefore a byte within this class
 * always has {@link #BITS_PER_BYTE} bits.
 */
public class BitAddress {

   /** The number of bits within one byte. */
   public static final int BITS_PER_BYTE = Byte.SIZE;

   private final int bitPosition;

   private final int byteAddress;

   /**
    * Creates a {@link BitAddress}.
    *
    * @param byteAddress
    *           The address of the byte. Byte addresses can be arbitrary integers but must be positive incl. zero.
    * @param bitPosition
    *           The position of the bit within the addressed byte. Must be in the range 0 to {@link #BITS_PER_BYTE} - 1.
    */
   public BitAddress(int byteAddress, int bitPosition) {
      Reject.ifNotInInterval(bitPosition, 0, BITS_PER_BYTE - 1, "bitPosition");
      Reject.ifNegative(byteAddress, "byteAddress");

      this.bitPosition = bitPosition;
      this.byteAddress = byteAddress;
   }

   /**
    * Returns the bit position. The bit position is an int between 0 and {@link #BITS_PER_BYTE} - 1.
    *
    * @return The bit position. The bit position is an int between 0 and {@link #BITS_PER_BYTE} - 1.
    */
   public int getBitPosition() {
      return bitPosition;
   }

   /**
    * Returns the byte address. The byte address is an arbitrary positive integer.
    *
    * @return The byte address. The byte address is an arbitrary positive integer.
    */
   public int getByteAddress() {
      return byteAddress;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object otherObject) {
      if (this == otherObject) {
         return true;
      }

      if (otherObject == null) {
         return false;
      }

      if (getClass() != otherObject.getClass()) {
         return false;
      }

      final BitAddress address = (BitAddress) otherObject;

      return byteAddress == address.getByteAddress() && bitPosition == address.getBitPosition();
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      return 13 * ((Integer) bitPosition).hashCode() + 47 * ((Integer) byteAddress).hashCode();
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return getClass().getName() + "[byteAddress=" + getByteAddress() + ", bitPosition=" + getBitPosition() + "]";
   }

   /**
    * Compares two {@link BitAddress} as to whether this {@link BitAddress} is smaller than the given other
    * {@link BitAddress}.
    *
    * @param other
    *           The other {@link BitAddress} to compare with.
    * @return true if this {@link BitAddress} is smaller, i.e. has a smaller byte address or - if byte addresses are
    *         equal - a smaller bit address, false otherwise.
    */
   public boolean smallerThan(BitAddress other) {
      Reject.ifNull(other, "other");

      return byteAddress < other.getByteAddress()
         || (byteAddress == other.getByteAddress() && bitPosition < other.getBitPosition());
   }

   /**
    * Advances this {@link BitAddress} for the given number of bits and returns a new {@link BitAddress} corresponding
    * to this advanced position.
    *
    * @param bitCount
    *           The number of bits to advance, must be positive.
    * @return A {@link BitAddress} advanced by the given number of bits.
    */
   public BitAddress advance(int bitCount) {
      Reject.ifNegative(bitCount, "bitCount");

      return new BitAddress((byteAddress + (bitPosition + bitCount) / BITS_PER_BYTE),
         (bitPosition + bitCount) % BITS_PER_BYTE);
   }
}
