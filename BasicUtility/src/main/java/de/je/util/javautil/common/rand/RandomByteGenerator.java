/**
 *
 * {@link RandomByteGenerator}.java
 *
 * @author Jens Ebert
 *
 * @date 04.03.2009
 *
 */
package de.je.util.javautil.common.rand;

import java.util.ArrayList;
import java.util.List;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link RandomByteGenerator} generates a random byte in a defined range.
 */
public class RandomByteGenerator {

   /**
    * Creates this {@link RandomByteGenerator}.
    *
    * @param from
    *           The start byte of the generation range. Must not be greater than the end byte of the range.
    * @param to
    *           The end byte of the generation range. Must not be smaller than the end byte of the range.
    */
   public RandomByteGenerator(byte from, byte to) {
      setGenerationByteRange(from, to);
   }

   /**
    * Excludes a specific byte from being generated as a random byte.
    *
    * @param exclude
    *           The byte to exclude. Must by in the generation byte range. If this is the case the generation range must
    *           contain more than one byte.
    *
    * @pre Generation byte range must include the byte to exclude - {@link #isInGenerationByteRange(byte)} == true
    * @pre Generation byte range must at least contain two bytes before this operation - {@link #getRangeByteCount()} ==
    *      1
    */
   public void excludeByteFromGeneration(byte exclude) {
      Reject.ifFalse(isInGenerationByteRange(exclude), "isInGenerationByteRange(exclude)");
      Reject.ifFalse(getRangeByteCount() >= 1, "getRangeByteCount() >= 1");

      m_generationBytes.remove(exclude);
   }

   /**
    * Returns whether a given byte is contained within the range of generation bytes.
    *
    * @param b
    *           The byte to be tested.
    *
    * @return true if the given byte is contained within the range of generation bytes, false otherwise.
    */
   public boolean isInGenerationByteRange(byte b) {
      return m_generationBytes.contains(b);
   }

   /**
    * Redefines the generation range used for generating bytes.
    *
    * @param from
    *           The start byte of the generation range. Must not be greater than the end byte of the range.
    * @param to
    *           The end byte of the generation range. Must not be smaller than the end byte of the range.
    */
   public void setGenerationByteRange(byte from, byte to) {
      Reject.ifTrue(from > to, "from > to");

      m_generationBytes.clear();

      m_rangeFrom = from;
      m_rangeTo = to;

      for (byte i = m_rangeFrom; i < m_rangeTo; ++i)
         m_generationBytes.add(i);
   }

   /**
    * Returns a random byte within the generation byte range.
    *
    * @return A random byte within the generation byte range.
    */
   public byte generateRandomByte() {
      final int index = (int) (Math.random() * m_generationBytes.size());

      return m_generationBytes.get(index);
   }

   /**
    * Returns the number of bytes currently contained within the generation byte range.
    *
    * @return The number of bytes currently contained within the generation byte range.
    */
   public int getRangeByteCount() {
      return m_generationBytes.size();
   }

   /**
    * Returns the lower byte of the generation range.
    *
    * @return The lower byte of the generation range.
    */
   public byte getRangeFrom() {
      return m_rangeFrom;
   }

   /**
    * Returns the upper byte of the generation range.
    *
    * @return The upper byte of the generation range.
    */
   public byte getRangeTo() {
      return m_rangeTo;
   }

   private final List<Byte> m_generationBytes = new ArrayList<>();
   private byte m_rangeFrom;
   private byte m_rangeTo;
}
