/**
 *
 * {@link NamedWriter}.java
 *
 * @author Jens Ebert
 *
 * @date 11.05.2014
 *
 */
package com.github.jmeta.utility.namedio.api.services;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link NamedWriter} class is a {@link BufferedWriter} that adds some meta data to a given arbitrary, wrapped
 * {@link Writer}. This is mainly useful for identification purposes, because you sometimes want to know the destination
 * of a given {@link Writer} (file, URL etc.).
 */
public class NamedWriter extends BufferedWriter {

   private final String name;

   /**
    * Creates a new {@link NamedWriter}.
    * 
    * @param in
    *           The wrapped {@link Writer}.
    * @param name
    *           Name of the wrapped {@link Writer}, arbitrary, but non-null. Should be used for identification purposes.
    */
   public NamedWriter(Writer in, String name) {
      super(in);

      Reject.ifNull(name, "name");

      this.name = name;
   }

   /**
    * Creates a new {@link NamedWriter} instance using a given existing file. The wrapped {@link Writer} is created as a
    * {@link OutputStreamWriter} with the given charset.
    * 
    * @param file
    *           The given {@link File}. Must be a file.
    * @param charset
    *           The {@link Charset} to use.
    * @param append
    *           true to append to the {@link Writer}, false otherwise.
    * @return The {@link NamedWriter}.
    * @throws IOException
    *            if creation of the wrapped {@link Writer} fails.
    */
   @SuppressWarnings("resource")
   public static NamedWriter createFromFile(File file, Charset charset, boolean append) throws IOException {
      Reject.ifNull(file, "file");
      Reject.ifNull(charset, "charset");
      Reject.ifFalse(file.isFile(), "file.isFile()");

      return new NamedWriter(new OutputStreamWriter(new FileOutputStream(file, append), charset),
         file.getCanonicalPath());
   }

   /**
    * @return the name of the wrapped {@link Writer}.
    */
   public String getName() {
      return name;
   }
}
