/**
 *
 * {@link ReadSingleAttribute}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2008
 *
 */

package com.github.jmeta.tools.datablockviewer.api.services;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import com.github.jmeta.library.datablocks.api.services.DataBlockAccessor;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.dataformats.api.types.DataFormat;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.startup.api.services.LibraryJMeta;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link DataBlockReader} tests the usability of the Audio Composition API when reading all attributes from an audio
 * file.
 */
public class DataBlockReader {

   /**
    * Reads all attributes from a given file.
    *
    * @param file
    *           The file to read all attributes from.
    *
    * @return A mapping of all tags and attributes belonging to each tag.
    */
   public Iterator<Container> getAllContainersFromFile(File file) {

      Reject.ifNull(file, "file");
      Reject.ifTrue(!file.exists(), "!file.exists()");

      DataBlockAccessor accessor = m_context.getDataBlockAccessor();

      return accessor.getContainerIterator(new FileMedium(file.toPath(), true), new ArrayList<DataFormat>(), false);
   }

   private final LibraryJMeta m_context = LibraryJMeta.getLibrary();
}
