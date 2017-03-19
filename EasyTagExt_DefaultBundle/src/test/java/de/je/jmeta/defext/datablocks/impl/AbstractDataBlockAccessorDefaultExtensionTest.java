package de.je.jmeta.defext.datablocks.impl;

import java.io.File;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

import de.je.jmeta.datablocks.iface.AbstractDataBlockAccessorTest;
import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.datatype.FileMedium;
import de.je.jmeta.media.api.datatype.InMemoryMedium;
import de.je.util.javautil.testUtil.setup.TestDataException;

/**
 * {@link AbstractDataBlockAccessorDefaultExtensionTest} is an abstract base class for testing daata block access
 */
public abstract class AbstractDataBlockAccessorDefaultExtensionTest
   extends AbstractDataBlockAccessorTest {

   /**
    * @see de.je.jmeta.datablocks.iface.AbstractDataBlockAccessorTest#AbstractDataBlockAccessorTest(File, File,
    *      Integer[])
    */
   public AbstractDataBlockAccessorDefaultExtensionTest(File testFile,
      File csvFile, Integer[] fieldSizes) {
      super(testFile, csvFile, fieldSizes);
   }

   /**
    * @see de.je.jmeta.datablocks.iface.IDataBlockAccessorTest#createMediaToCheck(java.io.File)
    */
   @Override
   protected List<IMedium<?>> createMediaToCheck(File baseFile)
      throws Exception {

      List<IMedium<?>> media = new ArrayList<>();

      if (baseFile.length() > Integer.MAX_VALUE)
         throw new TestDataException(
            "Specified file " + baseFile + " is too big: " + baseFile.length(),
            null);

      byte[] readMediumBytes = new byte[(int) baseFile.length()];

      try (RandomAccessFile raf = new RandomAccessFile(baseFile, "r")) {
         int bytesRead = 0;

         while (bytesRead < readMediumBytes.length) {
            int readReturn = raf.read(readMediumBytes, bytesRead,
               readMediumBytes.length - bytesRead);

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
