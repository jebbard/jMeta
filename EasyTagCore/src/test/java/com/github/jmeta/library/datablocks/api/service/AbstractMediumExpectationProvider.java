/**
 *
 * {@link DataFormatExpectationProvider}.java
 *
 * @author Jens Ebert
 *
 * @date 28.05.2011
 */
package com.github.jmeta.library.datablocks.api.service;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Set;

import com.github.jmeta.library.datablocks.api.services.IDataBlockAccessor;
import com.github.jmeta.library.dataformats.api.service.IDataFormatRepository;
import com.github.jmeta.library.dataformats.api.service.IDataFormatSpecification;
import com.github.jmeta.library.dataformats.api.type.DataFormat;
import com.github.jmeta.library.dataformats.api.type.PhysicalDataBlockType;

import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.testUtil.setup.TestDataException;

/**
 * {@link AbstractMediumExpectationProvider} provides expected test data for a single top-level data block of a single
 * {@link DataFormat} for testing {@link IDataBlockAccessor} instances.
 */
public abstract class AbstractMediumExpectationProvider {

   /**
    * This wild card expression in an expected value should be interpreted as "any value".
    */
   public static String ANY_WILDCARD = "*";

   private IDataFormatRepository dataFormatRepository;

   /**
    * Creates a new {@AbstractDataFormatExpectationProvider}.
    * 
    * @param dataFormatRepository
    *           The {@link IDataFormatRepository}.
    * @param testFile
    *           The test data file.
    */
   public AbstractMediumExpectationProvider(IDataFormatRepository dataFormatRepository, Path testFile) {

      Reject.ifNull(testFile, "testFile");
      Reject.ifNull(dataFormatRepository, "dataFormatRepository");
      Reject.ifFalse(Files.isRegularFile(testFile), "Files.isRegularFile(testFile)");

      this.dataFormatRepository = dataFormatRepository;

      Path tempCopyFile = testFile.getParent().resolve(testFile.getFileName() + "_TEMP");

      try {
         Files.copy(testFile, tempCopyFile, StandardCopyOption.REPLACE_EXISTING);

         raf = new RandomAccessFile(tempCopyFile.toFile(), "r");
         file = tempCopyFile;
      } catch (IOException e) {
         throw new TestDataException("Could not create or open copy of test file <"
            + testFile.toAbsolutePath().toString() + "> in destination file <" + tempCopyFile + ">.", e);
      }
   }

   /**
    * Returns the expected interpreted field value for the given field {@link DataBlockInstanceId}. If there is no data
    * for the given {@link DataBlockInstanceId}, null is returned.
    *
    * @param fieldInstanceId
    *           The field {@link DataBlockInstanceId}.
    * @return The field's expected interpreted value or null if there is no entry for the given
    *         {@link DataBlockInstanceId}.
    */
   public abstract Object getExpectedFieldInterpretedValue(DataBlockInstanceId fieldInstanceId);

   /**
    * Returns a {@link List} of {@link DataBlockInstanceId} that represents the expected children of the given parent
    * {@link DataBlockInstanceId} of the given {@link PhysicalDataBlockType} in their expected order.
    *
    * @param parentInstanceId
    *           The parent {@link DataBlockInstanceId}.
    * @param blockType
    *           The {@link PhysicalDataBlockType} of the children to query.
    * @return a {@link List} of {@link DataBlockInstanceId} that represents the expected children of the given parent
    *         {@link DataBlockInstanceId} of the given {@link PhysicalDataBlockType} in their expected order.
    */
   public abstract List<DataBlockInstanceId> getExpectedChildBlocksOfType(DataBlockInstanceId parentInstanceId,
      PhysicalDataBlockType blockType);

   /**
    * Returns the expected block size for the given {@link DataBlockInstanceId}.
    *
    * @param instanceId
    *           The {@link DataBlockInstanceId}.
    * @return the expected block size for the given {@link DataBlockInstanceId}.
    */
   public abstract long getExpectedDataBlockSize(DataBlockInstanceId instanceId);

   /**
    * Returns the field {@link DataBlockInstanceId}s, for which a failing conversion from binary to interpreted value is
    * expected.
    * 
    * @param fieldInstance
    *           The field's {@link DataBlockInstanceId}.
    *
    * @return the field {@link DataBlockInstanceId}s, for which a failing conversion from binary to interpreted value is
    *         expected.
    */
   public abstract ExpectedFailedFieldConversionData getExpectedFailingFieldConversions(
      DataBlockInstanceId fieldInstance);

   /**
    * Returns a list of expected top-level container {@link DataBlockInstanceId}s.
    *
    * @return a list of expected top-level container {@link DataBlockInstanceId}s.
    */
   public abstract List<DataBlockInstanceId> getExpectedTopLevelContainers();

   /**
    * Returns a list of expected top-level container {@link DataBlockInstanceId}s when reading from back to front.
    *
    * @return a list of expected top-level container {@link DataBlockInstanceId}s when reading from back to front.
    */
   public abstract List<DataBlockInstanceId> getExpectedTopLevelContainersReverse();

   /**
    * Returns the expected bytes stored at the given absolute offset with given size.
    * 
    * @param absoluteOffset
    *           The absolute medium offset.
    * @param size
    *           The number of expected bytes to return.
    *
    * @return the expected data block bytes for the given {@link DataBlockInstanceId} at the given absolute offset with
    *         given size.
    */
   public byte[] getExpectedBytes(long absoluteOffset, int size) {

      byte[] returnedExpectedBytes = new byte[size];
      ByteBuffer result = ByteBuffer.wrap(returnedExpectedBytes);

      try {
         raf.getChannel().read(result, absoluteOffset);
      } catch (IOException e) {
         throw new TestDataException("Could not read <" + size + "> expected block " + "bytes from file <"
            + file.toAbsolutePath().toString() + "> at offset <" + absoluteOffset + ">.", e);
      }

      return returnedExpectedBytes;
   }

   /**
    * Cleans this {@link AbstractMediumExpectationProvider} up. Should be called whenever the user is finished with this
    * instance, e.g. in tear down of a jUnit test case.
    */
   public void cleanUp() {

      if (raf != null)
         try {
            raf.close();
         } catch (IOException e) {
            throw new TestDataException("Could not close file <" + file.toAbsolutePath().toString() + ">.", e);
         }

      String message = "Could not delete file <" + file.toAbsolutePath().toString() + ">.";
      try {
         if (!Files.deleteIfExists(file))
            throw new TestDataException(message, null);
      } catch (IOException e) {
         throw new TestDataException(message, null);
      }
   }

   /**
    * Returns the {@link IDataFormatSpecification} corresponding to the given {@link DataFormat}.
    * 
    * @param format
    *           The {@link DataFormat}
    *
    * @return the {@link IDataFormatSpecification} corresponding to the given {@link DataFormat}.
    */
   protected IDataFormatSpecification getDataFormatSpecification(DataFormat format) {

      return dataFormatRepository.getDataFormatSpecification(format);
   }

   /**
    * Returns the overall supported data formats.
    * 
    * @return the overall supported data formats.
    */
   protected Set<DataFormat> getSupportedDataFormats() {

      return dataFormatRepository.getSupportedDataFormats();
   }

   private final Path file;

   private final RandomAccessFile raf;
}