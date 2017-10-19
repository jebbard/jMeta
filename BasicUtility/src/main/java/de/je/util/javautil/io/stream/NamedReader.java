/**
 *
 * {@link NamedReader}.java
 *
 * @author Jens
 *
 * @date 11.05.2014
 *
 */
package de.je.util.javautil.io.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link NamedReader} class is a {@link BufferedReader} that adds some meta data to a given arbitrary, wrapped
 * {@link Reader}. This is mainly useful for identification purposes, because you sometimes want to know the original
 * source of a given {@link Reader} (file, URL etc.).
 * 
 * @author jebert
 */
public class NamedReader extends BufferedReader {

   /**
    * Creates a new {@link NamedReader}.
    * 
    * @param in
    *           The wrapped {@link Reader}.
    * @param name
    *           Name of the wrapped {@link Reader}, arbitrary, but non-null. Should be used for identification purposes.
    */
   public NamedReader(Reader in, String name) {
      super(in);

      Reject.ifNull(name, "name");

      this.name = name;
   }

   /**
    * Creates a new {@link NamedReader} instance using a given existing file. The wrapped {@link Reader} is created as a
    * {@link InputStreamReader} with the given charset.
    * 
    * @param file
    *           The given {@link File}. Must exist and be a file.
    * @param charset
    *           The {@link Charset} to use.
    * @return The {@link NamedReader}.
    * @throws IOException
    *            if creation of the wrapped {@link Reader} fails.
    * 
    * @pre file.exists()
    * @pre file.isFile()
    */
   @SuppressWarnings("resource")
   public static NamedReader createFromFile(File file, Charset charset) throws IOException {
      Reject.ifNull(file, "file");
      Reject.ifNull(charset, "charset");
      Reject.ifFalse(file.exists(), "file.exists()");
      Reject.ifFalse(file.isFile(), "file.isFile()");

      return new NamedReader(new InputStreamReader(new FileInputStream(file), charset), file.getCanonicalPath());
   }

   /**
    * Creates a new {@link NamedReader} instance using a given resource. It creates a new wrapped {@link Reader} using
    * the {@link Class#getResourceAsStream(String)} method. The method appends the canonical class name to the resource
    * name and uses this as the name of the {@link NamedReader}.
    * 
    * @param clazz
    *           The given {@link Class}. The resource must be located relative to the class as described in the
    *           {@link Class#getResourceAsStream(String)} documentation.
    * @param resource
    *           The resource name.
    * @param charset
    *           The {@link Charset} to use.
    * @return The {@link NamedReader}.
    * @throws IOException
    *            if creation of the wrapped {@link Reader} fails.
    */
   @SuppressWarnings("resource")
   public static NamedReader createFromResource(Class<?> clazz, String resource, Charset charset) throws IOException {
      Reject.ifNull(clazz, "clazz");
      Reject.ifNull(resource, "resource");

      InputStream resourceAsStream = clazz.getResourceAsStream(resource);

      if (resourceAsStream == null)
         throw new IOException(
            "Resource with name <" + resource + "> not found for class <" + clazz.getCanonicalName() + ">.");

      return new NamedReader(new InputStreamReader(resourceAsStream, charset),
         clazz.getCanonicalName() + "." + resource);
   }

   /**
    * @return the name of the wrapped {@link Reader}.
    */
   public String getName() {
      return name;
   }

   private final String name;
}
