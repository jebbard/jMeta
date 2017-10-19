package de.je.util.javautil.common.flags;

import java.util.ArrayList;
import java.util.List;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link FlagDescription} contains the description of a single flag. Each flag has a name, a bit start position, a bit
 * size and a description.
 */
public class FlagDescription {

   /**
    * Creates a new {@link FlagDescription}.
    * 
    * @param flagName
    *           The name of the flag.
    * @param startBitAddress
    *           The starting {@link BitAddress} of the flag.
    * @param flagDescription
    *           A U.S. English description of the flag.
    * @param bitSize
    *           The length of the flag in bits.
    * @param valueNames
    *           The names of the values this flag can obtain. May be null if there are no specific names for the values.
    *           If non-null, the number of names must match the number of bits, i.e. 2^bitSize. The value names are
    *           ordered from lowest bit to highest.
    */
   public FlagDescription(String flagName, BitAddress startBitAddress, String flagDescription, int bitSize,
      List<String> valueNames) {
      Reject.ifNull(flagName, "flagName");
      Reject.ifNull(flagDescription, "flagDescription");
      Reject.ifNull(startBitAddress, "startBitAddress");
      Reject.ifNotInInterval(bitSize, 1, Integer.SIZE, "Bit size must be in interval 1, " + Integer.SIZE);

      m_startBitAddress = startBitAddress;
      m_flagName = flagName;
      m_flagDescription = flagDescription;
      m_bitSize = bitSize;

      if (valueNames != null) {
         Reject.ifTrue(valueNames.size() != (1 << bitSize), "Number of value names must equal " + (1 << bitSize));

         m_valueNames.addAll(valueNames);
      }
   }

   /**
    * Returns the name of the flag.
    *
    * @return The name of the flag.
    */
   public String getFlagName() {
      return m_flagName;
   }

   /**
    * Returns a U.S. English description of the flag.
    *
    * @return A U.S. English description of the flag.
    */
   public String getFlagDescription() {
      return m_flagDescription;
   }

   /**
    * Returns the length of the flag in bits.
    *
    * @return The length of the flag in bits.
    */
   public int getBitSize() {
      return m_bitSize;
   }

   /**
    * Returns the start {@link BitAddress} of the flag within its flag bytes.
    *
    * @return The start {@link BitAddress} of the flag within its flag bytes.
    */
   public BitAddress getStartBitAddress() {
      return m_startBitAddress;
   }

   /**
    * Returns the names of the values this flag may obtain. The number of names matches the bit size of the flag, i.e.
    * 2^bitSize. The value names are ordered from lowest bit to highest.
    *
    * @return The names of the values this flag may obtain. The number of names matches the bit size of the flag, i.e.
    *         2^bitSize. The value names are ordered from lowest bit to highest.
    */
   public List<String> getValueNames() {
      return m_valueNames;
   }

   /**
    * Returns true if this flag is a multi-bit flag, false if it consists only of a single bit.
    *
    * @return true if this flag is a multi-bit flag, false if it consists only of a single bit.
    */
   public boolean isMultibitFlag() {
      return m_bitSize > 1;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {
      final int prime = 31;
      int result = 1;
      result = prime * result + m_bitSize;
      result = prime * result + ((m_flagDescription == null) ? 0 : m_flagDescription.hashCode());
      result = prime * result + ((m_flagName == null) ? 0 : m_flagName.hashCode());
      result = prime * result + ((m_startBitAddress == null) ? 0 : m_startBitAddress.hashCode());
      result = prime * result + ((m_valueNames == null) ? 0 : m_valueNames.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {
      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      FlagDescription other = (FlagDescription) obj;
      if (m_bitSize != other.m_bitSize)
         return false;
      if (m_flagDescription == null) {
         if (other.m_flagDescription != null)
            return false;
      } else if (!m_flagDescription.equals(other.m_flagDescription))
         return false;
      if (m_flagName == null) {
         if (other.m_flagName != null)
            return false;
      } else if (!m_flagName.equals(other.m_flagName))
         return false;
      if (m_startBitAddress == null) {
         if (other.m_startBitAddress != null)
            return false;
      } else if (!m_startBitAddress.equals(other.m_startBitAddress))
         return false;
      if (m_valueNames == null) {
         if (other.m_valueNames != null)
            return false;
      } else if (!m_valueNames.equals(other.m_valueNames))
         return false;
      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {
      return "FlagDescription [m_startBitAddress=" + m_startBitAddress + ", m_flagName=" + m_flagName
         + ", m_flagDescription=" + m_flagDescription + ", m_bitSize=" + m_bitSize + ", m_valueNames=" + m_valueNames
         + "]";
   }

   private final BitAddress m_startBitAddress;
   private final String m_flagName;
   private final String m_flagDescription;
   private final int m_bitSize;
   private final List<String> m_valueNames = new ArrayList<>();
}
