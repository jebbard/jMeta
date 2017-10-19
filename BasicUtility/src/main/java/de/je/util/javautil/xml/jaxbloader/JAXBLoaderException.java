/**
 *
 * {@link JAXBLoaderException}.java
 *
 * @author Jens Ebert
 *
 * @date 29.04.2011
 */
package de.je.util.javautil.xml.jaxbloader;

/**
 * {@link JAXBLoaderException} is thrown whenever loading of an XML file with the {@link AbstractJAXBLoader} failed.
 */
public class JAXBLoaderException extends RuntimeException {

   /**
    * Creates a new {@link JAXBLoaderException}.
    * 
    * @param message
    *           The message.
    * @param cause
    *           The causing exception or null, if none.
    */
   public JAXBLoaderException(String message, Throwable cause) {
      super(message, cause);
   }

   private static final long serialVersionUID = 1L;
}
