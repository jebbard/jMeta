/**
 *
 * {@link BnaryConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.lyrics3v2;

import java.nio.ByteOrder;
import java.nio.charset.Charset;

import de.je.jmeta.datablocks.BinaryValueConversionException;
import de.je.jmeta.datablocks.InterpretedValueConversionException;
import de.je.jmeta.datablocks.impl.SignedNumericFieldConverter;
import de.je.jmeta.dataformats.BinaryValue;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link Lyrics3v2StringSizeIntegerConverter}
 *
 */
public class Lyrics3v2StringSizeIntegerConverter
   extends SignedNumericFieldConverter {

   private static final int DECIMAL_RADIX = 10;

   private static final int MAX_LYRICS3v2_SIZE_FIELD_LENGTH = 6;

   private static final int MAX_SIZE = DECIMAL_RADIX * DECIMAL_RADIX
      * DECIMAL_RADIX * DECIMAL_RADIX * DECIMAL_RADIX * DECIMAL_RADIX - 1;

   // TODO primeRefactor005: Finalize and document this method
   @Override
   public Long toInterpreted(BinaryValue binaryValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding)
         throws BinaryValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(binaryValue, "binaryValue");

      if (binaryValue.getTotalSize() > MAX_LYRICS3v2_SIZE_FIELD_LENGTH)
         throw new BinaryValueConversionException(
            "Total size of binary value containing Lyrics3v2 size information must not be bigger than 6 bytes",
            null, desc, binaryValue, byteOrder, characterEncoding);

      byte[] lengthFieldBytes = binaryValue.getBytes(0,
         (int) binaryValue.getTotalSize());

      int totalSize = 0;
      int digitMultiplier = 1;

      for (int i = lengthFieldBytes.length - 1; i >= 0; --i) {
         int nextDigit = Character.digit(lengthFieldBytes[i], DECIMAL_RADIX);

         if (nextDigit == -1)
            throw new BinaryValueConversionException(
               "Size field's value <" + binaryValue
                  + "> may contain digits only",
               null, desc, binaryValue, byteOrder, characterEncoding);

         totalSize += nextDigit * digitMultiplier;

         digitMultiplier *= DECIMAL_RADIX;
      }

      return new Long(totalSize);
   }

   @Override
   public BinaryValue toBinary(Long interpretedValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding)
         throws InterpretedValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(interpretedValue, "interpretedValue");

      if (interpretedValue > MAX_SIZE)
         throw new InterpretedValueConversionException(
            "Lyrics3v2 size fields may not be larger than " + MAX_SIZE, null,
            desc, interpretedValue, byteOrder, characterEncoding);

      // TODO primeRefactor004: implement

      return super.toBinary(interpretedValue, desc, byteOrder,
         characterEncoding);
   }

}
