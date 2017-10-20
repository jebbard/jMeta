/**
 *
 * {@link AbstractSingleBlockDataBlockAccessorTest}.java
 *
 * @author Jens Ebert
 *
 * @date 03.06.2011
 */
package com.github.jmeta.library.datablocks.api.service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractDataBlockAccessorTest} is a convenience class for derived classes of {@link IDataBlockAccessorTest}.
 */
public abstract class AbstractDataBlockAccessorTest extends IDataBlockAccessorTest {

   private final Path testFile;

   private final Path csvFile;

   private final List<Integer> fieldSizes = new ArrayList<>();

   /**
    * Creates a new {@AbstractSingleBlockDataBlockAccessorTest}.
    * 
    * @param testFile
    *           The test {@link Path}.
    * @param csvFile
    *           The csv {@link Path}.
    * @param fieldSizes
    *           The sizes of some selected fields in bytes. This is for testing the lazy field facility. The values
    *           should equal the size of a single field in the data and should be smaller than the size of one or
    *           several other fields in the data.
    */
   public AbstractDataBlockAccessorTest(Path testFile, Path csvFile, Integer[] fieldSizes) {

      Reject.ifNull(fieldSizes, "fieldSizes");
      checkFile(testFile);
      checkFile(csvFile);

      this.testFile = testFile;
      this.csvFile = csvFile;
      this.fieldSizes.addAll(Arrays.asList(fieldSizes));
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.service.IDataBlockAccessorTest#getFieldSizesForTestingLazyFields()
    */
   @Override
   protected List<Integer> getFieldSizesForTestingLazyFields() {

      return fieldSizes;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.service.IDataBlockAccessorTest#getFileForMediaContents()
    */
   @Override
   protected Path getFileForMediaContents() {

      return testFile;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.service.IDataBlockAccessorTest#createExpectationProvider()
    */
   @Override
   protected AbstractMediumExpectationProvider createExpectationProvider() throws InvalidTestDataCsvFormatException {

      return new CsvFileDataFormatExpectationProvider(getDataFormatRepository(), testFile, csvFile);
   }

   /**
    * Checks the given {@link Path} for plausibility.
    * 
    * @param file
    *           The file to check
    */
   private static void checkFile(Path file) {

      Reject.ifNull(file, "file");
      Reject.ifFalse(Files.exists(file), "Files.exists(");
      Reject.ifFalse(Files.isRegularFile(file), "Files.isRegularFile(file)");
   }
}
