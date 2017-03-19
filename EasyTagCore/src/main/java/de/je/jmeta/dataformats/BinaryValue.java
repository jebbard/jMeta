/**
 *
 * {@link BinaryValue}.java
 *
 * @author Jens Ebert
 *
 * @date 18.06.2011
 */
package de.je.jmeta.dataformats;

import java.nio.ByteBuffer;
import java.util.Arrays;

import de.je.util.javautil.common.err.Contract;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link BinaryValue}
 *
 */
/**
 * {@link BinaryValue}
 *
 */
public class BinaryValue {

   private int m_maxFragmentSize = Integer.MAX_VALUE;

   private static final String CLOSING_ARRAY_BRACKET = "}";

   private static final String OPENING_ARRAY_BRACKET = "{";

   /**
    * @return the fragment count
    */
   public int getFragmentCount() {

      return m_buffer.length;
   }

   /**
    * @param absoluteOffset
    * @return the fragment for the given offset
    */
   public byte[] getFragmentForOffset(long absoluteOffset) {

      return m_buffer[getFragmentIndexForOffset(absoluteOffset)];
   }

   private int getFragmentIndexForOffset(long absoluteOffset) {

      Contract.checkPrecondition(absoluteOffset < getTotalSize(),
         "absoluteOffset < getTotalSize()");

      return (int) absoluteOffset / m_maxFragmentSize;
   }

   /**
    * @param fragmentNr
    * @return the frament bytes for the given fragment nr.
    */
   public byte[] getFragment(int fragmentNr) {

      Contract.checkPrecondition(fragmentNr < getFragmentCount(),
         "fragmentNr < getFragmentCount()");

      return m_buffer[fragmentNr];
   }

   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + Arrays.hashCode(m_buffer);
      return result;
   }

   @Override
   public boolean equals(Object obj) {

      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      BinaryValue other = (BinaryValue) obj;
      if (getTotalSize() != other.getTotalSize())
         return false;

      if (getFragmentCount() != other.getFragmentCount())
         return false;

      for (int i = 0; i < getFragmentCount(); i++) {
         if (!Arrays.equals(getFragment(i), other.getFragment(i)))
            return false;
      }

      return true;
   }

   @Override
   public String toString() {

      StringBuffer stringRepresentation = new StringBuffer();

      if (getFragmentCount() > 1)
         stringRepresentation.append(OPENING_ARRAY_BRACKET);

      for (int i = 0; i < getFragmentCount(); i++) {
         stringRepresentation.append(OPENING_ARRAY_BRACKET);

         byte[] fragment = getFragment(i);

         for (int j = 0; j < Math.min(fragment.length, 4); j++) {
            stringRepresentation.append(fragment[j]);
            stringRepresentation.append(",");
         }

         stringRepresentation.append("...");
         stringRepresentation.append(CLOSING_ARRAY_BRACKET);
      }

      if (getFragmentCount() > 1)
         stringRepresentation.append(CLOSING_ARRAY_BRACKET);

      return stringRepresentation.toString();
   }

   /**
    * @return the total size
    */
   public long getTotalSize() {

      return m_totalSize;
   }

   /**
    * Creates a new {@link BinaryValue}.
    * 
    * @param buffer
    */
   public BinaryValue(byte[][] buffer) {
      Reject.ifNull(buffer, "buffer");

      m_buffer = buffer;

      long totalSize = 0;

      for (int i = 0; i < buffer.length; i++)
         totalSize += buffer[i].length;

      m_totalSize = totalSize;
   }

   /**
    * Creates a new {@link BinaryValue}.
    * 
    * @param singleFragment
    */
   public BinaryValue(ByteBuffer singleFragment) {
      Reject.ifNull(singleFragment, "singleFragment");

      byte[] singleFragmentBytes = new byte[singleFragment.remaining()];

      singleFragment.get(singleFragmentBytes);

      m_buffer = new byte[][] { singleFragmentBytes };
      m_totalSize = singleFragmentBytes.length;
   }

   /**
    * Creates a new {@link BinaryValue}.
    * 
    * @param singleFragment
    */
   public BinaryValue(byte[] singleFragment) {
      Reject.ifNull(singleFragment, "singleFragment");

      m_buffer = new byte[][] { singleFragment };
      m_totalSize = singleFragment.length;
   }

   /**
    * Creates a new {@link BinaryValue}.
    * 
    * @param binaryData
    * @param maxFragmentSize
    */
   public BinaryValue(byte[] binaryData, int maxFragmentSize) {
      Reject.ifNull(binaryData, "singleFragment");

      final int fragmentCount = (binaryData.length / maxFragmentSize)
         + ((binaryData.length % maxFragmentSize) > 0 ? 1 : 0);
      m_buffer = new byte[fragmentCount][];
      int nextByte = 0;

      int currentBufferSize = Math.min(binaryData.length, maxFragmentSize);
      int remainingBufferSize = binaryData.length;

      for (int i = 0; i < fragmentCount; ++i) {
         if (i == fragmentCount - 1)
            currentBufferSize = remainingBufferSize;

         m_buffer[i] = new byte[currentBufferSize];

         for (int j = 0; j < currentBufferSize; j++, nextByte++)
            m_buffer[i][j] = binaryData[nextByte];

         remainingBufferSize -= currentBufferSize;
      }

      m_totalSize = binaryData.length;
      m_maxFragmentSize = maxFragmentSize;
   }

   /**
    * @return the fragment size
    */
   public int getMaxFragmentSize() {

      return m_maxFragmentSize;
   }

   /**
    * @param maxFragmentSize
    */
   public void setMaxFragmentSize(int maxFragmentSize) {

      Reject.ifNull(maxFragmentSize, "maxFragmentSize");

      m_maxFragmentSize = maxFragmentSize;
   }

   /**
    * @param offset
    * @param size
    * @return the bytes
    */
   public byte[] getBytes(long offset, int size) {

      Contract.checkPrecondition(offset + size <= getTotalSize(),
         "offset + size <= getTotalSize() was false");

      byte[] returnedBytes = new byte[size];

      int currentFragmentIndex = getFragmentIndexForOffset(offset);
      int currentByteInFragmentIndex = (int) (offset % m_maxFragmentSize);

      for (int i = 0; i < size; ++i) {
         returnedBytes[i] = getFragment(
            currentFragmentIndex)[currentByteInFragmentIndex];

         if (currentByteInFragmentIndex
            + 1 == getFragment(currentFragmentIndex).length) {
            currentFragmentIndex++;
            currentByteInFragmentIndex = 0;
         }

         else
            currentByteInFragmentIndex++;
      }

      return returnedBytes;
   }

   private final long m_totalSize;

   private final byte[][] m_buffer;
}
