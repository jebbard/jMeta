/**
 *
 * {@link DataFormatExpectationProvider}.java
 *
 * @author Jens Ebert
 *
 * @date 28.05.2011
 */
package de.je.jmeta.datablocks.iface;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.Set;

import de.je.jmeta.datablocks.IDataBlockAccessor;
import de.je.jmeta.dataformats.DataFormat;
import de.je.jmeta.dataformats.IDataFormatRepository;
import de.je.jmeta.dataformats.IDataFormatSpecification;
import de.je.jmeta.dataformats.PhysicalDataBlockType;
import de.je.util.javautil.common.err.Reject;
import de.je.util.javautil.io.file.FileUtility;
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
   public AbstractMediumExpectationProvider(
      IDataFormatRepository dataFormatRepository, File testFile) {

      Reject.ifNull(testFile, "testFile");
      Reject.ifNull(dataFormatRepository, "dataFormatRepository");
      Reject.ifFalse(testFile.exists(),
         "testFile.exists()");
      Reject.ifFalse(testFile.isFile(),
         "testFile.isFile()");

      this.dataFormatRepository = dataFormatRepository;

      File tempCopyFile = new File(testFile.getParentFile(),
         testFile.getName() + "_TEMP");

      if (tempCopyFile.exists())
         tempCopyFile.delete();

      try {
         boolean copySuccessful = FileUtility.copyFile(testFile, tempCopyFile);

         if (!copySuccessful)
            throw new IOException("Copy returned false");

         m_raf = new RandomAccessFile(tempCopyFile, "r");
         m_file = tempCopyFile;
      } catch (IOException e) {
         throw new TestDataException(
            "Could not create or open copy of test file <"
               + testFile.getAbsolutePath() + "> in destination file <"
               + tempCopyFile + ">.",
            e);
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
   public abstract Object getExpectedFieldInterpretedValue(
      DataBlockInstanceId fieldInstanceId);

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
   public abstract List<DataBlockInstanceId> getExpectedChildBlocksOfType(
      DataBlockInstanceId parentInstanceId, PhysicalDataBlockType blockType);

   /**
    * Returns the expected block size for the given {@link DataBlockInstanceId}.
    *
    * @param instanceId
    *           The {@link DataBlockInstanceId}.
    * @return the expected block size for the given {@link DataBlockInstanceId}.
    */
   public abstract long getExpectedDataBlockSize(
      DataBlockInstanceId instanceId);

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
         m_raf.getChannel().read(result, absoluteOffset);
      } catch (IOException e) {
         throw new TestDataException(
            "Could not read <" + size + "> expected block "
               + "bytes from file <" + m_file.getAbsolutePath()
               + "> at offset <" + absoluteOffset + ">.",
            e);
      }

      return returnedExpectedBytes;
   }

   /**
    * Cleans this {@link AbstractMediumExpectationProvider} up. Should be called whenever the user is finished with this
    * instance, e.g. in tear down of a jUnit test case.
    */
   public void cleanUp() {

      if (m_raf != null)
         try {
            m_raf.close();
         } catch (IOException e) {
            throw new TestDataException(
               "Could not close file <" + m_file.getAbsolutePath() + ">.", e);
         }

      if (!m_file.delete())
         throw new TestDataException(
            "Could not delete file <" + m_file.getAbsolutePath() + ">.", null);
   }

   /**
    * Returns the {@link IDataFormatSpecification} corresponding to the given {@link DataFormat}.
    * 
    * @param format
    *           The {@link DataFormat}
    *
    * @return the {@link IDataFormatSpecification} corresponding to the given {@link DataFormat}.
    */
   protected IDataFormatSpecification getDataFormatSpecification(
      DataFormat format) {

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

   private final File m_file;

   private final RandomAccessFile m_raf;
}
