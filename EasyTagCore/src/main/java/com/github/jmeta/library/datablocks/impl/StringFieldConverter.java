/**
 *
 * {@link BnaryConverter}.java
 *
 * @author Jens Ebert
 *
 * @date 19.06.2011
 */
package de.je.jmeta.datablocks.impl;

import java.io.UnsupportedEncodingException;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

import de.je.jmeta.datablocks.BinaryValueConversionException;
import de.je.jmeta.datablocks.InterpretedValueConversionException;
import de.je.jmeta.dataformats.BinaryValue;
import de.je.jmeta.dataformats.DataBlockDescription;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link StringFieldConverter}
 *
 */
public class StringFieldConverter implements IFieldConverter<String> {

   @Override
   public String toInterpreted(BinaryValue binaryValue,
      DataBlockDescription desc, ByteOrder byteOrder, Charset characterEncoding)
         throws BinaryValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "spec");
      Reject.ifNull(binaryValue, "byteValue");

      if (binaryValue.getTotalSize() > Integer.MAX_VALUE)
         throw new BinaryValueConversionException(
            "String fields may not be longer than " + Integer.MAX_VALUE
               + " bytes.",
            null, desc, binaryValue, byteOrder, characterEncoding);

      String stringValue;
      try {
         stringValue = new String(binaryValue.getFragment(0),
            characterEncoding.name());

         final Character terminationCharacter = desc.getFieldProperties()
            .getTerminationCharacter();

         if (terminationCharacter != null) {
            int index = stringValue.indexOf(terminationCharacter, 0);

            if (index != -1)
               stringValue = stringValue.substring(0, index);
         }

         // CONFIG_CHECK: Prüfen auf nicht unterstützte Encodings
         return stringValue;
      } catch (UnsupportedEncodingException e) {
         throw new BinaryValueConversionException(
            "String conversion failed due to unsupported character encoding <"
               + characterEncoding + ">.",
            e, desc, binaryValue, byteOrder, characterEncoding);
      }
   }

   @Override
   public BinaryValue toBinary(String interpretedValue,
      DataBlockDescription desc, ByteOrder byteOrder, Charset characterEncoding)
         throws InterpretedValueConversionException {

      Reject.ifNull(characterEncoding, "characterEncoding");
      Reject.ifNull(byteOrder, "byteOrder");
      Reject.ifNull(desc, "desc");
      Reject.ifNull(interpretedValue, "interpretedValue");

      // CONFIG_CHECK: Prüfen auf nicht unterstützte Encodings
      try {
         return new BinaryValue(
            interpretedValue.getBytes(characterEncoding.name()));
      } catch (UnsupportedEncodingException e) {
         throw new InterpretedValueConversionException(
            "String conversion failed due to unsupported character encoding <"
               + characterEncoding + ">.",
            e, desc, interpretedValue, byteOrder, characterEncoding);
      }
   }

}
