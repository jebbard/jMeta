/**
 *
 * {@link Charsets}.java
 *
 * @author Jens Ebert
 *
 * @date 22.02.2009
 *
 */
package com.github.jmeta.utility.charset.api.services;

import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.errors.api.services.JMetaIllegalStateException;
import com.github.jmeta.utility.errors.api.services.JMetaRuntimeException;

/**
 * {@link Charsets} is a helper class defining constants for all {@link Charset}s Java defines as always available.
 *
 * Additionally it provides helper methods to deal with BOMs and figure out character byte lengths.
 */
public final class Charsets {

   /**
    * Returns true if the given charset is a BOMMED charset, false otherwise.
    *
    * @param cs
    *           The {@link Charset} to check
    * @return true if the given charset is a BOMMED charset, false otherwise.
    */
   public static boolean hasBOM(Charset cs) {
      Reject.ifNull(cs, "cs");

      return BOMMED_CHARSETS.containsKey(cs);
   }

   /**
    * Returns a byte representation of a {@link String} encoded in the given {@link Charset} without BOM (byte order
    * management) bytes which are usually contained when using {@link String#getBytes(String)}. For instance, all UTF-16
    * encoded strings contain the bytes -2 and -1 at the beginning (FEFF or FFEF, depending on byte order).
    *
    * This is necessary to identify the byte ordering. However, in some cases a String needs to be converter without BOM
    * e.g. when appending raw bytes to an already BOMed byte representation of a string.
    *
    * @param string
    *           The string to get the bytes for.
    * @param cs
    *           The {@link Charset} the string is encoded in.
    *
    * @return A byte representation of the string according to the given {@link Charset} without BOM (byte order
    *         management) bytes.
    */
   public static byte[] getBytesWithoutBOM(String string, Charset cs) {
      Reject.ifNull(cs, "cs");
      Reject.ifNull(string, "string");

      byte[] returnedBytes;
      try {
         returnedBytes = string.getBytes(cs.name());
      } catch (UnsupportedEncodingException e) {
         throw new JMetaRuntimeException("Unsupported encoding", e);
      }

      if (hasBOM(cs)) {
         byte[] bomBytes = BOMMED_CHARSETS.get(cs);

         for (int i = 0; i < bomBytes.length; i++) {
            if (returnedBytes.length <= i || returnedBytes[i] != bomBytes[i]) {
               throw new JMetaIllegalStateException(
                  "Byte representation: <" + Arrays.toString(returnedBytes) + "> of String: <" + string
                     + "> with charset: <" + cs + "> does not start with BOM bytes: <" + bomBytes + ">",
                  null);
            }
         }

         byte[] tempBytes = new byte[returnedBytes.length - bomBytes.length];

         System.arraycopy(returnedBytes, bomBytes.length, tempBytes, 0, tempBytes.length);

         returnedBytes = tempBytes;
      }

      return returnedBytes;
   }

   /**
    * Seven-bit ASCII, a.k.a. ISO646-US, a.k.a. the Basic Latin block of the Unicode character set
    */
   public static Charset CHARSET_ASCII = Charset.forName("US-ASCII");
   /**
    * ISO Latin Alphabet No. 1, a.k.a. ISO-LATIN-1
    */
   public static Charset CHARSET_ISO = Charset.forName("ISO-8859-1");
   /**
    * Sixteen-bit UCS Transformation Format, byte order identified by an optional byte order mark
    */
   public static Charset CHARSET_UTF16 = Charset.forName("UTF-16");
   /**
    * Sixteen-bit UCS Transformation Format, big-endian byte order
    */
   public static Charset CHARSET_UTF16BE = Charset.forName("UTF-16BE");
   /**
    * Sixteen-bit UCS Transformation Format, little-endian byte order
    */
   public static Charset CHARSET_UTF16LE = Charset.forName("UTF-16LE");
   /**
    * Eight-bit UCS Transformation Format
    */
   public static Charset CHARSET_UTF8 = Charset.forName("UTF-8");

   private static final Map<Charset, byte[]> BOMMED_CHARSETS = new HashMap<>();

   static {
      BOMMED_CHARSETS.put(CHARSET_UTF16, new byte[] { (byte) 0xfe, (byte) 0xff });
   }

   /**
    * Cannot instantiate this class.
    */
   private Charsets() {
   }
}
