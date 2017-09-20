/**
 *
 * {@link AbstractSingleBlockDataBlockAccessorTest}.java
 *
 * @author Jens Ebert
 *
 * @date 03.06.2011
 */
package de.je.jmeta.datablocks.iface;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import de.je.jmeta.datablocks.impl.CsvFileDataFormatExpectationProvider;
import de.je.jmeta.datablocks.impl.InvalidTestDataCsvFormatException;
import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractDataBlockAccessorTest} is a convenience class for derived classes of {@link IDataBlockAccessorTest}.
 */
public abstract class AbstractDataBlockAccessorTest
   extends IDataBlockAccessorTest {

   private final File testFile;

   private final File csvFile;

   private final List<Integer> fieldSizes = new ArrayList<>();

   /**
    * Creates a new {@AbstractSingleBlockDataBlockAccessorTest}.
    * 
    * @param testFile
    *           The test {@link File}.
    * @param csvFile
    *           The csv {@link File}.
    * @param fieldSizes
    *           The sizes of some selected fields in bytes. This is for testing the lazy field facility. The values
    *           should equal the size of a single field in the data and should be smaller than the size of one or
    *           several other fields in the data.
    */
   public AbstractDataBlockAccessorTest(File testFile, File csvFile,
      Integer[] fieldSizes) {

      Reject.ifNull(fieldSizes, "fieldSizes");
      checkFile(testFile);
      checkFile(csvFile);

      this.testFile = testFile;
      this.csvFile = csvFile;
      this.fieldSizes.addAll(Arrays.asList(fieldSizes));
   }

   /**
    * @see de.je.jmeta.datablocks.iface.IDataBlockAccessorTest#getFieldSizesForTestingLazyFields()
    */
   @Override
   protected List<Integer> getFieldSizesForTestingLazyFields() {

      return fieldSizes;
   }

   /**
    * @see de.je.jmeta.datablocks.iface.IDataBlockAccessorTest#getFileForMediaContents()
    */
   @Override
   protected File getFileForMediaContents() {

      return testFile;
   }

   /**
    * @see de.je.jmeta.datablocks.iface.IDataBlockAccessorTest#createExpectationProvider()
    */
   @Override
   protected AbstractMediumExpectationProvider createExpectationProvider()
      throws InvalidTestDataCsvFormatException {

      return new CsvFileDataFormatExpectationProvider(getDataFormatRepository(), testFile,
         csvFile);
   }

   /**
    * Checks the given {@link File} for plausibility.
    * 
    * @param file
    *           The file to check
    */
   private static void checkFile(File file) {

      Reject.ifNull(file, "file");
      Reject.ifFalse(file.exists(), "file.exists()");
      Reject.ifFalse(file.isFile(), "file.isFile()");
   }
}
