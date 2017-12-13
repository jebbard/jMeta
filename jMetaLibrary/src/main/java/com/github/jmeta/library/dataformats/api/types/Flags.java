/**
 *
 * {@link Flags}.java
 *
 * @author Jens Ebert
 *
 * @date 09.01.2009
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import java.nio.ByteBuffer;
import java.util.Iterator;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link Flags} provides support for reading and writing flag values. A flag is either a single bit that can be set
 * (=1) or not set (=0) within a number of bytes, or a series of multiple bits that can be assigned more than two values
 * (so called multi-bits). Each flag has a meaning which is expressed by its name in the {@link FlagSpecification}
 * belonging to the flags. The {@link FlagSpecification} defines the byte length, order, position and bit size of flags.
 * It does not change over the whole lifetime of a {@link Flags} object.
 *
 * Flag values can be written by using {@link #setFlag(String, boolean)} or by specifying a byte value that e.g. comes
 * from a file.
 */
public class Flags {

   /** The length of a {@link Short} in bytes. */
   public static final int SHORT_BYTE_LENGTH = 2;
   /** The length of an {@link Integer} in bytes. */
   public static final int INT_BYTE_LENGTH = 4;
   /** The length of a {@link Long} in bytes. */
   public static final int LONG_BYTE_LENGTH = 8;

   private final FlagSpecification spec;
   private final ByteBuffer flagBytes;

   /**
    * Creates this {@link Flags} object.
    *
    * @param spec
    *           The {@link FlagSpecification} defining names, length and positions of the flags. Must not be null
    */
   public Flags(FlagSpecification spec) {
      Reject.ifNull(spec, "spec");

      this.spec = spec;

      flagBytes = ByteBuffer.wrap(spec.getDefaultFlagBytes());
      flagBytes.order(spec.getByteOrdering());
   }

   /**
    * Determines if a specific flag is set in the current byte value of this {@link Flags}.
    *
    * @param flagName
    *           The name of the flag. Must be defined in the specification. Must be defined in the flag specification
    *
    * @return true if the flag is set, false if it is not set. For multi-bit flags, this method returns true if the at
    *         least one bit is set in the multi-bit flag.
    */
   public boolean getFlag(String flagName) {
      Reject.ifNull(flagName, "flagName");
      Reject.ifFalse(getSpecification().hasFlag(flagName), "getSpecification().hasFlag(flagName)");

      BitAddress address = spec.getFlagAddress(flagName);

      byte flagByte = flagBytes.get(address.getByteAddress());

      byte mask = (byte) (1 << address.getBitPosition());

      return (mask & flagByte) != 0;
   }

   /**
    * Returns the flag value string
    * 
    * @param flagName
    *           The name of the flag. Must be defined in the flag specification
    * @return the flag value string
    */
   public String getFlagValueString(String flagName) {
      int flagValue = getFlagIntegerValue(flagName);

      return spec.getFlagDescriptions().get(flagName).getValueNames().get(flagValue);
   }

   /**
    * Returns the signed integer representation of the flag value. For single-bit flags, the method returns 1 if the
    * single-bit is set and 0 otherwise.
    *
    * @param flagName
    *           The name of the flag. Must be defined in the specification.
    * @return a signed integer corresponding to the bits in the flag.
    */
   public int getFlagIntegerValue(String flagName) {
      Reject.ifNull(flagName, "flagName");
      Reject.ifFalse(getSpecification().hasFlag(flagName), "getSpecification().hasFlag(flagName)");

      int flagValue = 0;

      FlagDescription description = spec.getFlagDescriptions().get(flagName);

      BitAddress flagStartAddress = description.getStartBitAddress();

      // The remaining bit count of the flag, starting with the next byte
      int bitCountInFurtherBytes = description.getBitSize() > (Byte.SIZE - flagStartAddress.getBitPosition())
         ? description.getBitSize() - Byte.SIZE + flagStartAddress.getBitPosition()
         : 0;

      // The number of bytes the flag spans over
      int spannedBytes = 1 + bitCountInFurtherBytes / Byte.SIZE + (bitCountInFurtherBytes % Byte.SIZE == 0 ? 0 : 1);

      int currentBitCount = 0;

      // The indices of the start and end bit belonging to the flag in the current byte
      int startBitIndex = flagStartAddress.getBitPosition();
      int endBitIndex = bitCountInFurtherBytes == 0 ? flagStartAddress.getBitPosition() + description.getBitSize() - 1
         : Byte.SIZE - 1;

      for (int i = flagStartAddress.getByteAddress(); i < flagStartAddress.getByteAddress() + spannedBytes; i++) {
         byte flagByte = flagBytes.get(i);

         // Build the bit mask
         byte bitMask = 0;

         for (int j = startBitIndex; j <= endBitIndex; j++) {
            bitMask |= (1 << j);
         }

         // Mask the flags (extracting the relevant bits only) and prevent signed one-bits
         // in the int when casting a negative flag byte by eliminating leading 1s.
         int maskedFlagByte = (0xFF & (flagByte & bitMask)) >> startBitIndex;

         flagValue |= (maskedFlagByte << currentBitCount);

         currentBitCount += endBitIndex - startBitIndex + 1;

         startBitIndex = 0;

         // In case of the last byte the flag spans over: The end bit index might be
         // smaller thann 7
         endBitIndex = (i == flagStartAddress.getByteAddress() + spannedBytes - 2
            ? (bitCountInFurtherBytes % Byte.SIZE) - 1
            : Byte.SIZE - 1);
      }

      return flagValue;
   }

   /**
    * Sets the flag with the given name to the given state.
    *
    * @param flagName
    *           The flag to set. Must be defined in the flag specification
    * @param set
    *           true to set the flag, false to unset the flag.
    */
   public void setFlag(String flagName, boolean set) {
      Reject.ifNull(flagName, "flagName");
      Reject.ifFalse(getSpecification().hasFlag(flagName), "getSpecification().hasFlag(flagName)");

      BitAddress address = spec.getFlagAddress(flagName);

      byte flagByte = flagBytes.get(address.getByteAddress());

      byte mask = (byte) (1 << address.getBitPosition());

      if (set) {
         flagByte |= mask;
      }

      else {
         flagByte &= ~mask;
      }

      flagBytes.put(address.getByteAddress(), flagByte);
   }

   /**
    * Sets the flag with the given name to the given integer value. As a precondition, the given value must not be
    * bigger than 2^(bit size of the flag). For single-bit flags, only 0 (=cleared) and 1 (=set) may be passed.
    *
    * @param flagName
    *           The name of the flag. Must be defined in the specification.
    * @param value
    *           The new value of the flag in a signed integer representation. Each bit in the integer corresponds to one
    *           bit in the flag. Therefore the given value must not be bigger than 2^(bit size of the flag).
    */
   public void setFlagIntegerValue(String flagName, int value) {
      Reject.ifNull(flagName, "flagName");
      Reject.ifFalse(getSpecification().hasFlag(flagName), "getSpecification().hasFlag(flagName)");
      final FlagDescription flagDescription = getSpecification().getFlagDescriptions().get(flagName);
      Reject.ifFalse(value < (1 << flagDescription.getBitSize()), "value < (1 << flagDescription.getBitSize())");

      // TODO implement
   }

   /**
    * Returns a byte representation of the {@link Flags}. If the flags consist of multiple bytes, this only returns the
    * first byte.
    *
    * @return A byte representation of the {@link Flags}.
    */
   public byte asByte() {
      return flagBytes.get(0);
   }

   /**
    * Writes the flags from a byte value. Flag positions that are not actually occupied by a named flag are ignored.
    * Flags are set or unset according to the state of the bit at the specified position. If the flags consist of
    * multiple bytes, this only writes the first byte.
    *
    * @param byteValue
    *           The byte value to set.
    */
   public void fromByte(byte byteValue) {
      flagBytes.put(0, byteValue);
   }

   /**
    * Returns a short representation of the {@link Flags}. If the flags consist of more than {@link #SHORT_BYTE_LENGTH}
    * bytes, this only returns the first two bytes.
    * 
    * The specified byte length must be greater or equal to required byte length -
    * {@link FlagSpecification#getByteLength()}{@literal >=} {@link #SHORT_BYTE_LENGTH} for {@link #getSpecification()}
    *
    * @return A short representation of the {@link Flags}.
    */
   public short asShort() {
      Reject.ifFalse(getSpecification().getByteLength() >= SHORT_BYTE_LENGTH,
         "getSpecification().getByteLength() >= SHORT_BYTE_LENGTH");

      return flagBytes.getShort(0);
   }

   /**
    * Writes the flags from a short value. Flag positions that are not actually occupied by a named flag are ignored.
    * Flags are set or unset according to the state of the bit at the specified position. If the flags consist of more
    * than {@link #SHORT_BYTE_LENGTH} bytes, this only writes the first two byte.
    *
    * The specified byte length must be greater or equal to required byte length -
    * {@link FlagSpecification#getByteLength()}{@literal >=} {@link #SHORT_BYTE_LENGTH} for {@link #getSpecification()}
    *
    * @param shortValue
    *           The short value to set.
    */
   public void fromShort(short shortValue) {
      Reject.ifFalse(getSpecification().getByteLength() >= SHORT_BYTE_LENGTH,
         "getSpecification().getByteLength() >= SHORT_BYTE_LENGTH");

      flagBytes.putShort(0, shortValue);
   }

   /**
    * Returns an int representation of the {@link Flags}. If the flags consist of more than {@link #INT_BYTE_LENGTH}
    * bytes, this only returns the first four bytes.
    *
    * The specified byte length must be greater or equal to required byte length -
    * {@link FlagSpecification#getByteLength()}{@literal >=}{@link #LONG_BYTE_LENGTH} for {@link #getSpecification()}
    * 
    * @return An int representation of the {@link Flags}.
    */
   public int asInt() {
      Reject.ifFalse(getSpecification().getByteLength() >= INT_BYTE_LENGTH,
         "getSpecification().getByteLength() >= INT_BYTE_LENGTH");

      return flagBytes.getInt(0);
   }

   /**
    * Writes the flags from an int value. Flag positions that are not actually occupied by a named flag are ignored.
    * Flags are set or unset according to the state of the bit at the specified position. If the flags consist of more
    * than {@link #INT_BYTE_LENGTH} bytes, this only writes the first four bytes.
    *
    * The specified byte length must be greater or equal to required byte length -
    * {@link FlagSpecification#getByteLength()}{@literal >=}{@link #LONG_BYTE_LENGTH} for {@link #getSpecification()}
    *
    * @param intValue
    *           The int value to set.
    */
   public void fromInt(int intValue) {
      Reject.ifFalse(getSpecification().getByteLength() >= INT_BYTE_LENGTH,
         "getSpecification().getByteLength() >= INT_BYTE_LENGTH");

      flagBytes.putInt(0, intValue);
   }

   /**
    * Returns a long representation of the {@link Flags}.
    *
    * The specified byte length must be greater or equal to required byte length -
    * {@link FlagSpecification#getByteLength()}{@literal >=}{@link #LONG_BYTE_LENGTH} for {@link #getSpecification()}
    *
    * @return A long representation of the {@link Flags}.
    */
   public long asLong() {
      Reject.ifFalse(getSpecification().getByteLength() >= LONG_BYTE_LENGTH,
         "getSpecification().getByteLength() >= LONG_BYTE_LENGTH");

      return flagBytes.getLong(0);
   }

   /**
    * Writes the flags from a long value. Flag positions that are not actually occupied by a named flag are ignored.
    * Flags are set or unset according to the state of the bit at the specified position.
    *
    * The specified byte length must be greater or equal to required byte length -
    * {@link FlagSpecification#getByteLength()}{@literal >=}{@link #LONG_BYTE_LENGTH} for {@link #getSpecification()}
    *
    * @param longValue
    *           The long value to set.
    */
   public void fromLong(long longValue) {
      Reject.ifFalse(getSpecification().getByteLength() >= LONG_BYTE_LENGTH,
         "getSpecification().getByteLength() >= LONG_BYTE_LENGTH");

      flagBytes.putLong(0, longValue);
   }

   /**
    * Returns a byte array representation of the {@link Flags}.
    *
    * @return A byte array representation of the {@link Flags}.
    */
   public byte[] asArray() {
      return flagBytes.array();
   }

   /**
    * Writes the flags from a byte array. Flag positions that are not actually occupied by a named flag are ignored.
    * Flags are set or unset according to the state of the bit at the specified position. If the flags consist of more
    * than the arrays byte length bytes, this only writes the array bytes.
    *
    * The specified byte length must be greater or equal to required byte length -
    * {@link FlagSpecification#getByteLength()}{@literal >= array.length} for {@link #getSpecification()}
    *
    * @param array
    *           The array to set.
    */
   public void fromArray(byte[] array) {
      Reject.ifFalse(spec.getByteLength() >= array.length, "getSpecification().getByteLength() >= array.length");

      flagBytes.put(array, 0, array.length);
   }

   /**
    * Returns the {@link FlagSpecification} associated with this {@link Flags} object.
    *
    * @return The {@link FlagSpecification} associated with this {@link Flags} object.
    */
   public FlagSpecification getSpecification() {
      return spec;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      String stringValue = getClass().getName() + "[flagSpec=";

      stringValue += getSpecification() + "; ";

      for (Iterator<String> flagIterator = spec.getFlagDescriptions().keySet().iterator(); flagIterator.hasNext();) {
         String flagName = flagIterator.next();

         if (getFlag(flagName)) {
            stringValue += flagName + "=1 ";
         }

         else {
            stringValue += flagName + "=0 ";
         }
      }

      stringValue += "]";

      return stringValue;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + ((flagBytes == null) ? 0 : flagBytes.hashCode());
      result = prime * result + ((spec == null) ? 0 : spec.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj) {
         return true;
      }

      if (obj == null) {
         return false;
      }

      if (getClass() != obj.getClass()) {
         return false;
      }

      Flags other = (Flags) obj;

      if (spec == null) {
         if (other.spec != null) {
            return false;
         }
      } else if (!spec.equals(other.spec)) {
         return false;
      }

      for (Iterator<String> iterator = spec.getFlagDescriptions().keySet().iterator(); iterator.hasNext();) {
         String flagName = iterator.next();

         if (!getFlag(flagName) == other.getFlag(flagName))
            return false;
      }

      return true;
   }
}
