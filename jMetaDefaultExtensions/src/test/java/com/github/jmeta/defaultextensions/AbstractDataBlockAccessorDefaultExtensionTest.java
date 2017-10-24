package com.github.jmeta.defaultextensions;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.jmeta.library.datablocks.api.services.AbstractDataBlockAccessorTest;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.utility.testsetup.api.exceptions.TestDataException;

/**
 * {@link AbstractDataBlockAccessorDefaultExtensionTest} is an abstract base class for testing daata block access
 */
public abstract class AbstractDataBlockAccessorDefaultExtensionTest extends AbstractDataBlockAccessorTest {

   /**
    * @see com.github.jmeta.library.datablocks.api.services.AbstractDataBlockAccessorTest#AbstractDataBlockAccessorTest(Path,
    *      Path, Integer[])
    */
   public AbstractDataBlockAccessorDefaultExtensionTest(Path testFile, Path csvFile, Integer[] fieldSizes) {
      super(testFile, csvFile, fieldSizes);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.AbtractDataBlockAccessorTest#createMediaToCheck(java.nio.file.Path)
    */
   @Override
   protected List<Medium<?>> createMediaToCheck(Path baseFile) throws Exception {

      List<Medium<?>> media = new ArrayList<>();

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
