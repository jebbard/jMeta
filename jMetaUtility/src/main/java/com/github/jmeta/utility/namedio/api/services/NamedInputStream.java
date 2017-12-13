/**
 *
 * {@link NamedInputStream}.java
 *
 * @author Jens Ebert
 *
 * @date 11.05.2014
 *
 */
package com.github.jmeta.utility.namedio.api.services;

import java.io.File;
import java.io.FileInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * The {@link NamedInputStream} class is a {@link FilterInputStream} that adds some meta data to a given arbitrary,
 * wrapped {@link InputStream}. This is mainly useful for identification purposes, because you sometimes want to know
 * the original source of a given input stream (file, URL etc.).
 */
public class NamedInputStream extends FilterInputStream {

   private final String name;

   /**
    * Creates a new {@link NamedInputStream}.
    * 
    * @param in
    *           The wrapped {@link InputStream}.
    * @param name
    *           Name of the wrapped {@link InputStream}, arbitrary, but non-null. Should be used for identification
    *           purposes.
    */
   public NamedInputStream(InputStream in, String name) {
      super(in);

      Reject.ifNull(name, "name");

      this.name = name;
   }

   /**
    * @return the name of the wrapped {@link InputStream}.
    */
   public String getName() {
      return name;
   }

   /**
    * Creates a new {@link NamedInputStream} instance using a given existing file. The wrapped {@link InputStream} is
    * created as a {@link FileInputStream}.
    * 
    * @param file
    *           The given {@link File}. Must exist and be a file.
    * @return The {@link NamedInputStream}.
    * @throws IOException
    *            if creation of the wrapped {@link InputStream} fails.
    */
   @SuppressWarnings("resource")
   public static NamedInputStream createFromFile(File file) throws IOException {
      Reject.ifNull(file, "file");
      Reject.ifFalse(file.exists(), "file.exists()");
      Reject.ifFalse(file.isFile(), "file.isFile()");

      return new NamedInputStream(new FileInputStream(file), file.getCanonicalPath());
   }

   /**
    * Creates a new {@link NamedInputStream} instance using a given url. It creates a new wrapped {@link InputStream}
    * using the {@link URL#openStream()} method.
    * 
    * @param url
    *           The given {@link URL}.
    * @return The {@link NamedInputStream}.
    * @throws IOException
    *            if creation of the wrapped {@link InputStream} fails.
    */
   public static NamedInputStream createFromURL(URL url) throws IOException {
      Reject.ifNull(url, "url");

      return new NamedInputStream(url.openStream(), url.toString());
   }

   /**
    * Creates a new {@link NamedInputStream} instance using a given resource. It creates a new wrapped
    * {@link InputStream} using the {@link Class#getResourceAsStream(String)} method. The method appends the canonical
    * class name to the resource name and uses this as the name of the {@link NamedInputStream}.
    * 
    * @param clazz
    *           The given {@link Class}. The resource must be located relative to the class as described in the
    *           {@link Class#getResourceAsStream(String)} documentation.
    * @param resource
    *           The resource name.
    * @return The {@link NamedInputStream}.
    * @throws IOException
    *            if creation of the wrapped {@link InputStream} fails.
    */
   @SuppressWarnings("resource")
   public static NamedInputStream createFromResource(Class<?> clazz, String resource) throws IOException {
      Reject.ifNull(clazz, "clazz");
      Reject.ifNull(resource, "resource");

      InputStream resourceAsStream = clazz.getResourceAsStream(resource);

      if (resourceAsStream == null) {
         throw new IOException(
            "Resource with name <" + resource + "> not found for class <" + clazz.getCanonicalName() + ">.");
      }

      return new NamedInputStream(resourceAsStream, clazz.getCanonicalName() + "." + resource);
   }
}
