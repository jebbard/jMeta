package com.github.jmeta.library.datablocks.api.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;
import com.github.jmeta.utility.testsetup.api.services.JMetaTestBasics;

// TODO [OLD]: field function stack won't currently work when reading "out of order" in depth -
// notion of data block instance ids OR saving parent IDataBlock reference...

/**
 * {@link AbtractDataBlockAccessorTest} tests the {@link DataBlockAccessor} interface. Basically, a file medium contains
 * the data to read. Using the file contents, the three media types file, memory and stream are tested.
 */
public abstract class AbtractDataBlockAccessorTest {

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

      checkTestFile();

      try {
         expectationProvider = createExpectationProvider();
      } catch (InvalidTestDataCsvFormatException e) {
         throw new InvalidTestDataException("Could not read test data.", e);
      }

      testling = ComponentRegistry.lookupService(DataBlockAccessor.class);

      if (testling == null)
         throw new InvalidTestDataException("Testdata must not be null", null);
   }

   /**
    * Tears down the test fixtures.
    */
   @After
   public void tearDown() {

      expectationProvider.cleanUp();

      // Check log files
      JMetaTestBasics.performGeneralLogCheck(JMetaTestBasics.DEFAULT_LOG_FILE);

      ComponentRegistry.clearServiceCache();
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forReadOnlyFileMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(createFileMedium(getFileForMediaContents(),
         true, Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableFileMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(createFileMedium(getFileForMediaContents(),
         false, Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableFileMediumWithDefaultCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, 100));
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableFileMediumWithSmallCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, 101, 7));
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableFileMediumWithRidiculouslySmallCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, 1, 1));
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableUncachedFileMediumWithDefaultRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, 0, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableUncachedFileMediumWithSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, 0, 19));
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forReadOnlyUncachedInMemoryMediumWithDefaultRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createInMemoryMedium(getFileForMediaContents(), true, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableUncachedInMemoryMediumWithDefaultRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createInMemoryMedium(getFileForMediaContents(), false, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link DataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableUncachedInMemoryMediumWithSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createInMemoryMedium(getFileForMediaContents(), false, 10));
   }

   // TODO: Currently assertion fails for Multi MP3 file (expects Lyrics Tag as next container, but EOM is wrongly
   // detected); reason: Current position of the stream medium is advanced way more than the current read offset, thus
   // isAtEOM detects medium end - too much cache calls in between that advance the streams current position -
   // Reading style must be adapted
   // /**
   // * Tests {@link DataBlockAccessor#getContainerIterator}.
   // */
   // @Test
   // public void
   // getContainerIterator_forInputStreamMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
   // assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(createStreamMedium(getFileForMediaContents(),
   // Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   // }

   // TODO: Currently fails for most of the formats: Redesign of reading process necessary
   // /**
   // * Tests {@link DataBlockAccessor#getContainerIterator}.
   // */
   // @Test
   // public void
   // getContainerIterator_forInputStreamMediumWithDefaultCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {
   // assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
   // createStreamMedium(getFileForMediaContents(), Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, 99));
   // }

   // TODO: Currently fails for most of the formats: Redesign of reading process necessary
   // /**
   // * Tests {@link DataBlockAccessor#getContainerIterator}.
   // */
   // @Test
   // public void
   // getContainerIterator_forInputStreamMediumWithSmallCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {
   // assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
   // createStreamMedium(getFileForMediaContents(), 35, 34));
   // }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forReadOnlyFileMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), true, Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES,
            Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableFileMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES,
            Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableFileMediumWithDefaultCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, 100));
   }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableFileMediumWithSmallCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, 101, 7));
   }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableFileMediumWithRidiculouslySmallCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, 1, 1));
   }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableUncachedFileMediumWithDefaultRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, 0, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableUncachedFileMediumWithSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), false, 0, 19));
   }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forReadOnlyUncachedInMemoryMediumWithDefaultRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createInMemoryMedium(getFileForMediaContents(), true, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableUncachedInMemoryMediumWithDefaultRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createInMemoryMedium(getFileForMediaContents(), false, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link DataBlockAccessor#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableUncachedInMemoryMediumWithSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createInMemoryMedium(getFileForMediaContents(), false, 10));
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
    * tested. The "equal to" case is clearly the same as the "smaller" case, but is nevertheless tested separately. Note
    * that the case "lazy field size smaller than lazy field size" which leads to the creation of normal fields is
    * already covered.
    *
    * @return The field sizes to set as lazy field size.
    */
   protected abstract List<Integer> getFieldSizesForTestingLazyFields();

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
    * Creates a {@link FileMedium}.
    */
   private Medium<?> createFileMedium(Path baseFile, boolean isReadOnly, long maxCacheSize, int maxReadWriteBlockSize) {
      return new FileMedium(baseFile, isReadOnly, maxCacheSize, maxReadWriteBlockSize);
   }

   /**
    * Creates an {@link InMemoryMedium}.
    */
   private Medium<?> createInMemoryMedium(Path baseFile, boolean isReadOnly, int maxReadWriteBlockSize) {

      byte[] byteContent = MediaTestUtility.readFileContent(baseFile);

      return new InMemoryMedium(byteContent, "TestMemMedium", isReadOnly, maxReadWriteBlockSize);
   }

   /**
    * Creates an {@link InputStreamMedium}.
    */
   private Medium<?> createStreamMedium(Path baseFile, long maxCacheSize, int maxReadWriteBlockSize) {
      try {
         return new InputStreamMedium(new FileInputStream(baseFile.toFile()), "TestStreamMedium", maxCacheSize,
            maxReadWriteBlockSize);
      } catch (FileNotFoundException e) {
         throw new RuntimeException("Could not create FileInputStream for path <" + baseFile + ">", e);
      }
   }

   /**
    * Checks {@link DataBlockAccessor#getContainerIterator(Medium, List, boolean)} for a given medium.
    * 
    * @param medium
    *           The medium to check
    */
   private void assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(Medium<?> medium) {
      AbstractDataBlockIterator<Container> topLevelContainerIterator = getTestling().getContainerIterator(medium,
         new ArrayList<DataFormat>(), false);

      Assert.assertNotNull(topLevelContainerIterator);

      // Check the whole data block hierarchy returned for correctness
      checkContainers(topLevelContainerIterator, null, false);

      getTestling().closeMedium(medium);
   }

   /**
    * Checks {@link DataBlockAccessor#getReverseContainerIterator(Medium, List, boolean)} for a given medium.
    * 
    * @param medium
    *           The medium to check
    */
   private void assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(Medium<?> medium) {
      AbstractDataBlockIterator<Container> topLevelReverseContainerIterator = getTestling()
         .getReverseContainerIterator(medium, new ArrayList<DataFormat>(), false);

      Assert.assertNotNull(topLevelReverseContainerIterator);

      // Check the whole data block hierarchy returned for correctness
      checkContainers(topLevelReverseContainerIterator, null, true);

      getTestling().closeMedium(medium);
   }

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

         Assert.assertTrue("Expected next container with id <" + expectedContainerId + "> within parent of id <"
            + parentInstanceId + ">, but containerIterator.hasNext() returned false.", containerIterator.hasNext());

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

         if (readSizes[i] != 0) {
            byte[] expectedBytes = expectationProvider.getExpectedBytes(absOffset + readOffsets[i], readSizes[i]);

            byte[] actualBytes = dataBlock.getBytes(readOffsets[i], readSizes[i]);

            org.junit.Assert.assertArrayEquals(expectedBytes, actualBytes);
         }
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
   private void checkTestFile() {

      Path theFile = getFileForMediaContents();

      if (theFile == null || !Files.exists(theFile))
         throw new InvalidTestDataException("Invalid file returned by method getFileForMediaContents", null);
   }
}
