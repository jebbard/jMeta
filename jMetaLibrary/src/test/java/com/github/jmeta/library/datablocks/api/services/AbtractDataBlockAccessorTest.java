package com.github.jmeta.library.datablocks.api.services;

import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldSequence;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatRepository;
import com.github.jmeta.library.dataformats.api.types.BinaryValue;
import com.github.jmeta.library.dataformats.api.types.DataFormat;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.testsetup.api.exceptions.TestDataException;
import com.github.jmeta.utility.testsetup.api.services.JMetaTestBasics;

// TODO doItFirst005: write test case for "out of order" reading
// TODO doItFirst004: make ogg test case
// TODO doItFirst006: field function stack won't currently work when reading "out of order" in depth -
// notion of data block instance ids OR saving parent IDataBlock reference...

/**
 * {@link AbtractDataBlockAccessorTest} tests the {@link DataBlockAccessor} interface. Basically, a file medium contains
 * the data to read. Using the file contents, the three media types file, memory and stream are tested.
 */
public abstract class AbtractDataBlockAccessorTest {

   private final List<Medium<?>> testedMedia = new ArrayList<>();

   private AbstractMediumExpectationProvider expectationProvider;

   private DataFormatRepository dataFormatRepository;

   private DataBlockAccessor testling;

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {

      JMetaTestBasics.emptyLogFile(JMetaTestBasics.DEFAULT_LOG_FILE);

      JMetaTestBasics.setupExtensions();

      dataFormatRepository = ComponentRegistry.lookupService(DataFormatRepository.class);

      prepareTestedMedia();

      try {
         expectationProvider = createExpectationProvider();
      } catch (InvalidTestDataCsvFormatException e) {
         throw new TestDataException("Could not read test data.", e);
      }

      testling = ComponentRegistry.lookupService(DataBlockAccessor.class);

      if (testling == null)
         throw new TestDataException("Testdata must not be null", null);
   }

   /**
    * Tears down the test fixtures.
    */
   @After
   public void tearDown() {

      expectationProvider.cleanUp();

      for (int i = 0; i < testedMedia.size(); ++i) {
         Medium<?> medium = testedMedia.get(i);

         // testling.closeMedium(medium);
      }

      // Check log files
      JMetaTestBasics.performGeneralLogCheck(JMetaTestBasics.DEFAULT_LOG_FILE);

      ComponentRegistry.clearServiceCache();
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void test_getContainerIterator() {

      for (int i = 0; i < testedMedia.size(); ++i) {
         Medium<?> medium = testedMedia.get(i);

         AbstractDataBlockIterator<Container> topLevelContainerIterator = getTestling().getContainerIterator(medium,
            new ArrayList<DataFormat>(), false);

         Assert.assertNotNull(topLevelContainerIterator);

         // Check the whole data block hierarchy returned for correctness
         checkContainers(topLevelContainerIterator, null, false);

         getTestling().closeMedium(medium);
      }
   }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator(Medium, List, boolean)}.
    */
   @Test
   public void test_getReverseContainerIterator() {

      for (int i = 0; i < testedMedia.size(); ++i) {
         Medium<?> medium = testedMedia.get(i);

         // Reverse reading is supported for random access media only
         if (medium.isRandomAccess()) {
            AbstractDataBlockIterator<Container> topLevelContainerIterator = getTestling()
               .getReverseContainerIterator(medium, new ArrayList<DataFormat>(), false);

            Assert.assertNotNull(topLevelContainerIterator);

            // Check the whole data block hierarchy returned for correctness
            checkContainers(topLevelContainerIterator, null, true);

            getTestling().closeMedium(medium);
         }
      }
   }

   /**
    * Returns the {@link DataFormatRepository} instance.
    * 
    * @return the {@link DataFormatRepository} instance.
    */
   protected DataFormatRepository getDataFormatRepository() {

      return dataFormatRepository;
   }

   /**
    * Returns the {@link DataBlockAccessor} for testing.
    *
    * @return the {@link DataBlockAccessor} for testing.
    */
   protected DataBlockAccessor getTestling() {

      return testling;
   }

   /**
    * Returns the sizes of some selected fields in bytes. This is for testing the lazy field facility. The values should
    * equal the size of a single field in the data and should be smaller than the size of one or several other fields in
    * the data.
    *
    * This way, the cases of "field size equal to lazy field size" and "field size bigger than lazy field size" are
    * tested in {@link #test_getContainerIterator_withLazyField()}. The "equal to" case is clearly the same as the
    * "smaller" case, but is nevertheless tested separately. Note that the case "lazy field size smaller than lazy field
    * size" which leads to the creation of normal fields is already covered by {@link #test_getContainerIterator()}, as
    * usually the field sizes of the tested data in this method is smaller than the default lazy field size.
    *
    * @return The field sizes to set as lazy field size.
    */
   protected abstract List<Integer> getFieldSizesForTestingLazyFields();

   /**
    * Creates and returns the {@link AbstractMedium} instances used for all tests.
    * 
    * @param baseFile
    *           A base file whose contents can be used to create the media.
    * @return The {@link AbstractMedium} instances used for all tests.
    * @throws Exception
    *            If an exception occurs during medium creation.
    */
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

   /**
    * Returns a single {@link Path} object that contains the relevant base data from which the data blocks are read. The
    * data from the {@link Path} is taken by this class and converted to several {@link AbstractMedium} instances
    * finally tested.
    *
    * @return a single {@link Path} object that contains the relevant base data from which the data blocks are read.
    */
   protected abstract Path getFileForMediaContents();

   /**
    * Creates an {@link AbstractMediumExpectationProvider} instance for the given {@link DataFormat}.
    *
    * @return an {@link AbstractMediumExpectationProvider} instance for the given {@link DataFormat}.
    * @throws InvalidTestDataCsvFormatException
    *            if the test data could not be read.
    */
   protected abstract AbstractMediumExpectationProvider createExpectationProvider()
      throws InvalidTestDataCsvFormatException;

   /**
    * Recursively checks all {@link Container}s starting with the given iterator.
    *
    * @param containerIterator
    *           The {@link AbstractDataBlockIterator}
    * @param parentInstanceId
    *           The parent of the iterated children or null if it is a top-level container iterator.
    * @param reverseReading
    *           true if the containers are currently read in reverse mode, false otherwise.
    */
   private void checkContainers(AbstractDataBlockIterator<Container> containerIterator,
      DataBlockInstanceId parentInstanceId, boolean reverseReading) {

      List<DataBlockInstanceId> expectedIds = null;

      if (parentInstanceId == null)
         if (reverseReading)
            expectedIds = expectationProvider.getExpectedTopLevelContainersReverse();

         else
            expectedIds = expectationProvider.getExpectedTopLevelContainers();

      else {
         expectedIds = expectationProvider.getExpectedChildBlocksOfType(parentInstanceId,
            PhysicalDataBlockType.CONTAINER);
      }

      if (expectedIds.size() == 0)
         Assert.assertFalse(containerIterator.hasNext());

      for (int j = 0; j < expectedIds.size(); ++j) {
         DataBlockInstanceId expectedContainerId = expectedIds.get(j);

         Assert.assertTrue(containerIterator.hasNext());

         // Check container itself
         Container nextContainer = containerIterator.next();

         Assert.assertNotNull(nextContainer);
         Assert.assertEquals(expectedContainerId.getId(), nextContainer.getId());
         // Check size of the container
         checkDataBlockSize(nextContainer, expectedContainerId);

         // Check the bytes of the container
         checkDataBlockBytes(nextContainer);

         // Check headers and footers of the container
         checkHeadersFooters(nextContainer, expectedContainerId, true);
         checkHeadersFooters(nextContainer, expectedContainerId, false);

         // Check the payload of the container
         checkPayload(nextContainer, expectedContainerId, j % 2 == 0);
      }

      // There must not be any further containers
      Assert.assertFalse(containerIterator.hasNext());
   }

   /**
    * Checks the payload of the given {@link Container}.
    *
    * @param parentContainer
    *           The parent {@link Container}.
    * @param parentContainerInstanceId
    *           The {@link DataBlockInstanceId} of the parent.
    * @param fieldsFirst
    *           true to read child fields of the payload before child containers, false to read child containers of the
    *           payload before child fields.
    */
   private void checkPayload(Container parentContainer, DataBlockInstanceId parentContainerInstanceId,
      boolean fieldsFirst) {

      Payload payload = parentContainer.getPayload();

      Assert.assertNotNull(payload);

      DataBlockInstanceId expectedPayloadId = expectationProvider
         .getExpectedChildBlocksOfType(parentContainerInstanceId, PhysicalDataBlockType.PAYLOAD).get(0);

      Assert.assertEquals(expectedPayloadId.getId(), payload.getId());
      // Check size of the payload
      checkDataBlockSize(payload, expectedPayloadId);

      // Check the bytes of the payload
      checkDataBlockBytes(payload);

      // The check for fields and child containers is tested in different
      // order in each loop pass!
      if (fieldsFirst) {
         // FIRST: Check all fields of the payload
         checkFields(payload, expectedPayloadId);

         // SECOND: Recursively check all child containers
         checkContainers(payload.getContainerIterator(), expectedPayloadId, false);
      }

      else {
         // FIRST: Recursively check all child containers
         checkContainers(payload.getContainerIterator(), expectedPayloadId, false);

         // SECOND: Check all fields of the payload
         checkFields(payload, expectedPayloadId);
      }
   }

   /**
    * Checks {@link Header}s of the given {@link Container} for correctness.
    *
    * Should be called twice, once for headers, once for footers.
    *
    * @param parentContainer
    *           The parent {@link Container} for which to check the {@link Header}s.
    * @param parentContainerInstanceId
    *           {@link DataBlockInstanceId} of the parent container.
    * @param checkHeaders
    *           true if to check headers, false for checking footers.
    */
   private void checkHeadersFooters(Container parentContainer, DataBlockInstanceId parentContainerInstanceId,
      boolean checkHeaders) {

      List<Header> headerList = null;

      if (checkHeaders)
         headerList = parentContainer.getHeaders();

      else
         headerList = parentContainer.getFooters();

      Assert.assertNotNull(headerList);

      List<DataBlockInstanceId> expectedHeaderIds = null;

      if (checkHeaders)
         expectedHeaderIds = expectationProvider.getExpectedChildBlocksOfType(parentContainerInstanceId,
            PhysicalDataBlockType.HEADER);

      else
         expectedHeaderIds = expectationProvider.getExpectedChildBlocksOfType(parentContainerInstanceId,
            PhysicalDataBlockType.FOOTER);

      Assert.assertEquals(expectedHeaderIds.size(), headerList.size());

      for (int i = 0; i < expectedHeaderIds.size(); ++i) {
         DataBlockInstanceId expectedHeaderId = expectedHeaderIds.get(i);

         Header header = headerList.get(i);

         Assert.assertEquals(expectedHeaderId.getId(), header.getId());
         // Check size of the header or footer
         checkDataBlockSize(header, expectedHeaderId);

         // Check the bytes of header or footer
         checkDataBlockBytes(header);

         checkFields(header, expectedHeaderId);
      }
   }

   /**
    * Checks {@link Field}s of the given {@link DataBlock} parent for correctness.
    *
    * @param parentSequence
    *           The parent {@link FieldSequence} for which to check the {@link Field}s.
    * @param parentInstanceId
    *           The {@link DataBlockInstanceId} of the parent.
    */
   private void checkFields(FieldSequence parentSequence, DataBlockInstanceId parentInstanceId) {

      List<Field<?>> fieldList = parentSequence.getFields();

      Assert.assertNotNull(fieldList);

      List<DataBlockInstanceId> expectedFieldIds = expectationProvider.getExpectedChildBlocksOfType(parentInstanceId,
         PhysicalDataBlockType.FIELD);

      Assert.assertEquals(expectedFieldIds.size(), fieldList.size());

      for (int i = 0; i < expectedFieldIds.size(); ++i) {
         DataBlockInstanceId expectedFieldId = expectedFieldIds.get(i);

         Field<?> field = fieldList.get(i);

         Assert.assertEquals(expectedFieldId.getId(), field.getId());
         // Check size of the field
         checkDataBlockSize(field, expectedFieldId);

         // Check the bytes of field
         checkDataBlockBytes(field);
         checkFieldBinaryValue(field);

         Object expectedFieldValue = expectationProvider.getExpectedFieldInterpretedValue(expectedFieldId);

         // Do not check the field value, as it is allowed to have any value
         if (expectedFieldValue.equals(AbstractMediumExpectationProvider.ANY_WILDCARD))
            continue;

         try {
            Object actualFieldValue = field.getInterpretedValue();

            Assert.assertEquals(expectedFieldValue, actualFieldValue);

            String stringRepresentation = field.getStringRepresentation();

            Assert.assertNotNull(stringRepresentation);
         } catch (BinaryValueConversionException e) {
            ExpectedFailedFieldConversionData convData = expectationProvider
               .getExpectedFailingFieldConversions(expectedFieldId);

            if (convData == null)
               Assert.fail("Unexpected conversion exception" + e);

            else {
               Assert.assertEquals(expectedFieldId.getId(), e.getFieldDescription().getId());
               try {
                  Assert.assertEquals(field.getBinaryValue(), e.getBinaryValue());
               } catch (InterpretedValueConversionException e1) {
                  Assert.fail("Unexpected conversion exception" + e1);
               }
               Assert.assertEquals(convData.getByteOrder(), e.getByteOrder());
               Assert.assertEquals(convData.getCharacterEncoding(), e.getCharacterEncoding());
            }
         }
      }
   }

   /**
    * Checks whether the bytes returned for the given data block equal the expected bytes.
    *
    * @param dataBlock
    *           The {@link DataBlock}.
    */
   private void checkDataBlockBytes(DataBlock dataBlock) {

      long totalSize = dataBlock.getTotalSize();

      long[] readOffsets = new long[] { 0, totalSize / 2, totalSize - 1, };
      int[] readSizes = new int[] { totalSize > 30 ? 31 : (int) totalSize,
         totalSize / 2 < 10 ? (int) totalSize / 2 : (int) totalSize / 10, 1, };

      long absOffset = dataBlock.getMediumReference().getAbsoluteMediumOffset();

      for (int i = 0; i < readSizes.length; i++) {
         byte[] expectedBytes = expectationProvider.getExpectedBytes(absOffset + readOffsets[i], readSizes[i]);

         byte[] actualBytes = dataBlock.getBytes(readOffsets[i], readSizes[i]);

         org.junit.Assert.assertArrayEquals(expectedBytes, actualBytes);
      }
   }

   /**
    * Checks whether the binary value returned for the given field equals the expected bytes.
    * 
    * @param field
    *           The field to check.
    */
   private void checkFieldBinaryValue(Field<?> field) {

      long totalSize = field.getTotalSize();

      long[] readOffsets = new long[] { 0, totalSize / 2, totalSize - 1, };
      int[] readSizes = new int[] { totalSize > 30 ? 31 : (int) totalSize,
         totalSize / 2 < 10 ? (int) totalSize / 2 : (int) totalSize / 10, 1, };

      long absOffset = field.getMediumReference().getAbsoluteMediumOffset();

      for (int i = 0; i < readSizes.length; i++) {
         byte[] expectedBytes = expectationProvider.getExpectedBytes(absOffset + readOffsets[i], readSizes[i]);

         try {
            BinaryValue binaryValue = field.getBinaryValue();

            byte[] actualBytes = binaryValue.getBytes(readOffsets[i], readSizes[i]);

            org.junit.Assert.assertArrayEquals(expectedBytes, actualBytes);
         } catch (InterpretedValueConversionException e) {
            Assert.fail("Unexpected conversion exception" + e);
         }
      }
   }

   /**
    * Checks whether the size of the given data block equals its expected size.
    *
    * @param dataBlock
    *           The {@link DataBlock}.
    * @param instanceId
    *           The {@link DataBlockInstanceId} of the {@link DataBlock}.
    */
   private void checkDataBlockSize(DataBlock dataBlock, DataBlockInstanceId instanceId) {

      long expectedSize = expectationProvider.getExpectedDataBlockSize(instanceId);

      Assert.assertEquals(expectedSize, dataBlock.getTotalSize());
   }

   /**
    * Creates the {@link AbstractMedium} instances to be tested by the super class.
    */
   private void prepareTestedMedia() {

      Path theFile = getFileForMediaContents();

      if (theFile == null || !Files.exists(theFile))
         throw new TestDataException("Invalid file returned by method getFileForMediaContents", null);

      try {
         List<Medium<?>> media = createMediaToCheck(theFile);

         testedMedia.addAll(media);
      } catch (Exception e) {
         throw new RuntimeException("Unexpected exception during medium preparation", e);
      }
   }
}
