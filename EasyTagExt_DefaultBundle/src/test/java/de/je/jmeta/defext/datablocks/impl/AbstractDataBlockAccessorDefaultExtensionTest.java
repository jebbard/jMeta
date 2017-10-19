package de.je.jmeta.defext.datablocks.impl;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.datablocks.api.AbstractDataBlockAccessorTest;
import com.github.jmeta.library.media.api.type.FileMedium;
import com.github.jmeta.library.media.api.type.IMedium;
import com.github.jmeta.library.media.api.type.InMemoryMedium;

import de.je.util.javautil.testUtil.setup.TestDataException;

/**
 * {@link AbstractDataBlockAccessorDefaultExtensionTest} is an abstract base class for testing daata block access
 */
public abstract class AbstractDataBlockAccessorDefaultExtensionTest extends AbstractDataBlockAccessorTest {

   /**
    * @see com.github.jmeta.library.datablocks.api.AbstractDataBlockAccessorTest#AbstractDataBlockAccessorTest(Path, Path,
    *      Integer[])
    */
   public AbstractDataBlockAccessorDefaultExtensionTest(Path testFile, Path csvFile, Integer[] fieldSizes) {
      super(testFile, csvFile, fieldSizes);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.IDataBlockAccessorTest#createMediaToCheck(java.nio.file.Path)
    */
   @Override
   protected List<IMedium<?>> createMediaToCheck(Path baseFile) throws Exception {

      List<IMedium<?>> media = new ArrayList<>();

      long size = Files.size(baseFile);

      if (size > Integer.MAX_VALUE)
         throw new TestDataException("Specified file " + baseFile + " is too big: " + size, null);

      byte[] readMediumBytes = new byte[(int) size];

      try (RandomAccessFile raf = new RandomAccessFile(baseFile.toFile(), "r")) {
         int bytesRead = 0;

         while (bytesRead < readMediumBytes.length) {
            int readReturn = raf.read(readMediumBytes, bytesRead, readMediumBytes.length - bytesRead);

            if (readReturn == -1)
               throw new RuntimeException("Unexpected end of file");

            bytesRead += readReturn;
         }

         media.add(new FileMedium(baseFile, true));
         // TODO stage2_003: stream medium currently does not work
         // media.add(new StreamMedium(new FileInputStream(theFile), null));
         media.add(new InMemoryMedium(readMediumBytes, null, true));
      }

      return media;
   }

}
