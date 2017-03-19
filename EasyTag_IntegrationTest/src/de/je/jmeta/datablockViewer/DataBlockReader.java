/**
 *
 * {@link ReadSingleAttribute}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2008
 *
 */

package de.je.jmeta.datablockViewer;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import de.je.jmeta.context.iface.IJMetaContext;
import de.je.jmeta.context.iface.JMetaContext;
import de.je.jmeta.datablocks.iface.IContainer;
import de.je.jmeta.datablocks.iface.IDataBlockAccessor;
import de.je.jmeta.dataformats.iface.DataFormat;
import de.je.jmeta.defext.media.iface.FileMedium;
import de.je.util.common.err.Reject;

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
   public Iterator<IContainer> getAllContainersFromFile(File file) {

      Reject.ifNull(file, "file");
      Reject.ifTrue(!file.exists(), "!file.exists()");

      IDataBlockAccessor accessor = m_context.getDataBlockAccessor();

      return accessor.getContainerIterator(
         new FileMedium(file, file.getAbsolutePath()),
         new ArrayList<DataFormat>(), false);
   }

   private final IJMetaContext m_context = JMetaContext.getInstance().get();
}
