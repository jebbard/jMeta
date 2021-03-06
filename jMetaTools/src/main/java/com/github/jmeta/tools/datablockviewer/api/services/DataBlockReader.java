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
import java.util.Iterator;

import com.github.jmeta.library.datablocks.api.services.LowLevelAPI;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.MediumAccessType;
import com.github.jmeta.library.startup.api.services.LibraryJMeta;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link DataBlockReader} tests the usability of the Audio Composition API when reading all attributes from an audio
 * file.
 */
public class DataBlockReader {

   private final LibraryJMeta m_context = LibraryJMeta.getLibrary();

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

      LowLevelAPI accessor = m_context.getLowLevelAPI();

      return accessor.getContainerIterator(new FileMedium(file.toPath(), MediumAccessType.READ_ONLY));
   }
}
