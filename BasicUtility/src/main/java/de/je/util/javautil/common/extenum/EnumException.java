/**
 *
 * {@link EnumException}.java
 *
 * @author Jens Ebert
 *
 * @date 18.06.2009
 *
 */
package de.je.util.javautil.common.extenum;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link EnumException} is thrown whenever an error occurs with {@link AbstractExtensibleEnum} handling.
 */
public class EnumException extends RuntimeException {

   /**
    * Creates this {@link EnumException}.
    * 
    * @param message
    *           A human readable error message in English.
    * @param id
    *           The duplicate id that caused this exception, may be null.
    * @param enumClass
    *           The enum class causing the exception.
    */
   public EnumException(String message, String id, Class<? extends AbstractExtensibleEnum<?>> enumClass) {
      super(message);

      Reject.ifNull(enumClass, "enumClass");
      Reject.ifNull(message, "message");

      m_enumClass = enumClass;
      m_duplicateId = id;
   }

   /**
    * Returns the enum class causing the exception.
    *
    * @return the enum class causing the exception.
    */
   public Class<? extends AbstractExtensibleEnum<?>> getEnumClass() {
      return m_enumClass;
   }

   /**
    * Returns the duplicate enum id that caused this exception.
    *
    * @return the duplicate enum id that caused this exception.
    */
   public String getDuplicateId() {
      return m_duplicateId;
   }

   private final String m_duplicateId;
   private final Class<? extends AbstractExtensibleEnum<?>> m_enumClass;

   private static final long serialVersionUID = 1L;
}
