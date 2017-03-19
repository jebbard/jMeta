/**
 *
 * {@link BnaryConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package de.je.jmeta.defext.datablocks.impl.id3v23;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import de.je.jmeta.datablocks.BinaryValueConversionException;
import de.je.jmeta.datablocks.InterpretedValueConversionException;
import de.je.jmeta.datablocks.impl.SignedNumericFieldConverter;
import de.je.jmeta.dataformats.BinaryValue;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link SyncSafeIntegerConverter}
 *
 */
public class SyncSafeIntegerConverter extends SignedNumericFieldConverter {

   private static final int FROM_CONVERSION_MASK = 0x7F000000;

   private static final int TO_CONVERSION_MASK = 0x7F;

   private static final int MAX_SYNC_SAFE_INTEGER = 2 >> 28;

   @Override
   public Long toInterpreted(BinaryValue binaryValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding)
         throws BinaryValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(binaryValue, "binaryValue");
      if (binaryValue.getTotalSize() != Integer.SIZE / Byte.SIZE)
         throw new BinaryValueConversionException(
            "ID3v23 size fields must have integer (" + Integer.SIZE / Byte.SIZE
               + " bytes) size",
            null, desc, binaryValue, byteOrder, characterEncoding);

      int size = ByteBuffer.wrap(binaryValue.getFragment(0)).getInt();

      return new Long(synchSafeToInt(size));
   }

   @Override
   public BinaryValue toBinary(Long interpretedValue, DataBlockDescription desc,
      ByteOrder byteOrder, Charset characterEncoding)
         throws InterpretedValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(interpretedValue, "interpretedValue");

      if (interpretedValue > MAX_SYNC_SAFE_INTEGER)
         throw new InterpretedValueConversionException(
            "ID3v23 sync-safe integers may not be larger than "
               + MAX_SYNC_SAFE_INTEGER,
            null, desc, interpretedValue, byteOrder, characterEncoding);

      long converted = intToSynchSafe(interpretedValue.intValue());

      return super.toBinary(converted, desc, byteOrder, characterEncoding);
   }

   /**
    * Converts a usual integer to a synchsafe integer.
    *
    * @param usualInteger
    *           The usual integer to convert.
    * @return A synchsafe representation of the given integer.
    */
   private int intToSynchSafe(int usualInteger) {

      int synchSafeInteger = 0;
      int shiftedInteger = usualInteger;
      int mask = TO_CONVERSION_MASK;

      for (int i = 0; i < Integer.SIZE / Byte.SIZE; ++i) {
         synchSafeInteger += shiftedInteger & mask;
         shiftedInteger <<= 1;
         mask <<= Byte.SIZE;
      }

      return synchSafeInteger;
   }

   /**
    * Converts a synchsafe integer to a usual integer.
    *
    * @param synchSafeInteger
    *           The synchsafe integer to convert.
    * @return The usual integer.
    */
   private int synchSafeToInt(int synchSafeInteger) {

      int usualInteger = 0;
      int mask = FROM_CONVERSION_MASK;

      for (int i = 0; i < Integer.SIZE / Byte.SIZE; ++i) {
         usualInteger >>= 1;
         usualInteger |= synchSafeInteger & mask;
         mask >>= Byte.SIZE;
      }

      return usualInteger;
   }

}
