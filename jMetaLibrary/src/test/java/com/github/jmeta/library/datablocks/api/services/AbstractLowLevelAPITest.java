package com.github.jmeta.library.datablocks.api.services;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.github.jmeta.library.datablocks.api.exceptions.BinaryValueConversionException;
import com.github.jmeta.library.datablocks.api.exceptions.InterpretedValueConversionException;
import com.github.jmeta.library.datablocks.api.types.Container;
import com.github.jmeta.library.datablocks.api.types.ContainerBasedPayload;
import com.github.jmeta.library.datablocks.api.types.DataBlock;
import com.github.jmeta.library.datablocks.api.types.DataBlockState;
import com.github.jmeta.library.datablocks.api.types.Field;
import com.github.jmeta.library.datablocks.api.types.FieldBasedPayload;
import com.github.jmeta.library.datablocks.api.types.FieldSequence;
import com.github.jmeta.library.datablocks.api.types.Header;
import com.github.jmeta.library.datablocks.api.types.Payload;
import com.github.jmeta.library.dataformats.api.services.DataFormatRepository;
import com.github.jmeta.library.dataformats.api.types.PhysicalDataBlockType;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.services.MediaAPI;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.AbstractMedium;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAccessType;
import com.github.jmeta.library.media.impl.StandardMediaAPI;
import com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils;
import com.github.jmeta.utility.compregistry.api.services.ComponentRegistry;
import com.github.jmeta.utility.dbc.api.services.Reject;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;
import com.github.jmeta.utility.testsetup.api.services.JMetaTestBasics;

/**
 * {@link AbstractLowLevelAPITest} tests the {@link LowLevelAPI} interface. Basically, a file medium contains the data
 * to read. Using the file contents, the three media types file, memory and stream are tested.
 */
public abstract class AbstractLowLevelAPITest {

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

   private AbstractMediumExpectationProvider expectationProvider;

   private DataFormatRepository dataFormatRepository;

   private LowLevelAPI testling;

   private final Path testFile;

   private final Path csvFile;

   private final List<Integer> fieldSizes = new ArrayList<>();

   /**
    * Creates a new {@link AbstractLowLevelAPITest}.
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
   public AbstractLowLevelAPITest(Path testFile, Path csvFile, Integer[] fieldSizes) {

      Reject.ifNull(fieldSizes, "fieldSizes");
      AbstractLowLevelAPITest.checkFile(testFile);
      AbstractLowLevelAPITest.checkFile(csvFile);

      this.testFile = testFile;
      this.csvFile = csvFile;
      this.fieldSizes.addAll(Arrays.asList(fieldSizes));
   }

   /**
    * Tests {@link LowLevelAPI#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forInputStreamMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(createStreamMedium(getFileForMediaContents(),
         Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forInputStreamMediumWithDefaultCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(createStreamMedium(getFileForMediaContents(),
         Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, MediumStore.MINIMUM_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forReadOnlyFileMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), MediumAccessType.READ_ONLY, Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES,
            Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forReadOnlyInMemoryMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createInMemoryMedium(getFileForMediaContents(), MediumAccessType.READ_ONLY,
            Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableFileMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), MediumAccessType.READ_WRITE,
            Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableFileMediumWithDefaultCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), MediumAccessType.READ_WRITE,
            Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, MediumStore.MINIMUM_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableFileMediumWithSmallCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {

      int maxCacheSizeToUse = 101;
      int maxReadWriteBlockSizeToUse = 7;
      resetMaxSizes(maxCacheSizeToUse, 7);

      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(createFileMedium(getFileForMediaContents(),
         MediumAccessType.READ_WRITE, maxCacheSizeToUse, maxReadWriteBlockSizeToUse));
   }

   /**
    * Tests {@link LowLevelAPI#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableInMemoryMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createInMemoryMedium(getFileForMediaContents(), MediumAccessType.READ_WRITE,
            Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getContainerIterator}.
    */
   @Test
   public void getContainerIterator_forWritableInMemoryMediumWithSmallCacheAndRWBSize_returnsExpectedContainersAndFields() {
      int maxCacheSizeToUse = 101;
      int maxReadWriteBlockSizeToUse = 10;
      resetMaxSizes(maxCacheSizeToUse, maxReadWriteBlockSizeToUse);

      assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(createInMemoryMedium(
         getFileForMediaContents(), MediumAccessType.READ_WRITE, maxCacheSizeToUse, maxReadWriteBlockSizeToUse));
   }

   /**
    * Tests {@link LowLevelAPI#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forReadOnlyFileMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), MediumAccessType.READ_ONLY, Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES,
            Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forReadOnlyInMemoryMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createInMemoryMedium(getFileForMediaContents(), MediumAccessType.READ_ONLY,
            Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableFileMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), MediumAccessType.READ_WRITE,
            Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableFileMediumWithDefaultCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createFileMedium(getFileForMediaContents(), MediumAccessType.READ_WRITE,
            Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, MediumStore.MINIMUM_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableFileMediumWithSmallCacheAndSmallRWBSize_returnsExpectedContainersAndFields() {

      int maxCacheSizeToUse = 101;
      int maxReadWriteBlockSizeToUse = 7;
      resetMaxSizes(maxCacheSizeToUse, maxReadWriteBlockSizeToUse);

      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(createFileMedium(
         getFileForMediaContents(), MediumAccessType.READ_WRITE, maxCacheSizeToUse, maxReadWriteBlockSizeToUse));
   }

   /**
    * Tests {@link LowLevelAPI#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableInMemoryMediumWithDefaultCacheAndRWBSize_returnsExpectedContainersAndFields() {
      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(
         createInMemoryMedium(getFileForMediaContents(), MediumAccessType.READ_WRITE,
            Medium.DEFAULT_MAX_CACHE_SIZE_IN_BYTES, Medium.DEFAULT_MAX_READ_WRITE_BLOCK_SIZE_IN_BYTES));
   }

   /**
    * Tests {@link LowLevelAPI#getReverseContainerIterator}.
    */
   @Test
   public void getReverseContainerIterator_forWritableInMemoryMediumWithSmallCacheAndRWBSize_returnsExpectedContainersAndFields() {
      int maxCacheSizeToUse = 101;
      int maxReadWriteBlockSizeToUse = 10;
      resetMaxSizes(maxCacheSizeToUse, maxReadWriteBlockSizeToUse);

      assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(createInMemoryMedium(
         getFileForMediaContents(), MediumAccessType.READ_WRITE, maxCacheSizeToUse, maxReadWriteBlockSizeToUse));
   }

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {

      JMetaTestBasics.emptyLogFile(JMetaTestBasics.DEFAULT_UNITTEST_LOG_FILE);

      JMetaTestBasics.setupExtensions();

      dataFormatRepository = ComponentRegistry.lookupService(DataFormatRepository.class);

      checkTestFile();

      try {
         expectationProvider = createExpectationProvider();
      } catch (InvalidTestDataCsvFormatException e) {
         throw new InvalidTestDataException("Could not read test data.", e);
      }

      testling = ComponentRegistry.lookupService(LowLevelAPI.class);

      if (testling == null) {
         throw new InvalidTestDataException("Testdata must not be null", null);
      }
   }

   /**
    * Tears down the test fixtures.
    */
   @After
   public void tearDown() {

      expectationProvider.cleanUp();

      // Check log files
      JMetaTestBasics.performGeneralLogCheck(JMetaTestBasics.DEFAULT_UNITTEST_LOG_FILE);

      ComponentRegistry.clearServiceCache();
   }

   /**
    * Checks {@link LowLevelAPI#getContainerIterator(Medium)} for a given medium.
    *
    * @param medium
    *           The medium to check
    */
   private void assertGetContainerIteratorReturnsContainersAndFieldsInExpectedOrder(Medium<?> medium) {
      try (MediumContainerIterator topLevelContainerIterator = getTestling().getContainerIterator(medium)) {

         Assert.assertNotNull(topLevelContainerIterator);

         // Check the whole data block hierarchy returned for correctness
         checkContainers(topLevelContainerIterator, null, false);

      } catch (IOException e) {
         Assert.fail("Unexpected exception during close" + e);
      }
   }

   /**
    * Checks {@link LowLevelAPI#getReverseContainerIterator(Medium)} for a given medium.
    *
    * @param medium
    *           The medium to check
    */
   private void assertGetReverseContainerIteratorReturnsContainersAndFieldsInExpectedOrder(Medium<?> medium) {
      try (
         MediumContainerIterator topLevelReverseContainerIterator = getTestling().getReverseContainerIterator(medium)) {

         Assert.assertNotNull(topLevelReverseContainerIterator);

         // Check the whole data block hierarchy returned for correctness
         checkContainers(topLevelReverseContainerIterator, null, true);
      } catch (IOException e) {
         Assert.fail("Unexpected exception during close" + e);
      }
   }

   /**
    * Recursively checks all {@link Container}s starting with the given iterator.
    *
    * @param containerIterator
    *           The {@link ContainerIterator}
    * @param parentInstanceId
    *           The parent of the iterated children or null if it is a top-level container iterator.
    * @param reverseReading
    *           true if the containers are currently read in reverse mode, false otherwise.
    */
   private void checkContainers(ContainerIterator containerIterator, DataBlockInstanceId parentInstanceId,
      boolean reverseReading) {

      List<DataBlockInstanceId> expectedIds = null;

      if (parentInstanceId == null) {
         if (reverseReading) {
            expectedIds = expectationProvider.getExpectedTopLevelContainersReverse();
         } else {
            expectedIds = expectationProvider.getExpectedTopLevelContainers();
         }
      } else {
         expectedIds = expectationProvider.getExpectedChildBlocksOfType(parentInstanceId,
            PhysicalDataBlockType.CONTAINER);
      }

      if (expectedIds.size() == 0 && !reverseReading) {
         Assert.assertFalse(containerIterator.hasNext());
      }

      for (int j = 0; j < expectedIds.size(); ++j) {
         DataBlockInstanceId expectedContainerId = expectedIds.get(j);

         Assert.assertTrue("Expected next container with id <" + expectedContainerId + "> within parent of id <"
            + parentInstanceId + ">, but containerIterator.hasNext() returned false.", containerIterator.hasNext());

         // Check container itself
         Container nextContainer = containerIterator.next();

         Assert.assertNotNull(nextContainer);

         Assert.assertEquals(DataBlockState.PERSISTED, nextContainer.getState());

         Assert.assertEquals(expectedContainerId.getId(), nextContainer.getId());
         // Check size of the container
         checkDataBlockSize(nextContainer, expectedContainerId);

         // Check the bytes of the container
         checkDataBlockBytes(nextContainer);

         // Check headers and footers of the container
         checkHeadersFooters(nextContainer, expectedContainerId, true);
         checkHeadersFooters(nextContainer, expectedContainerId, false);

         // Check the payload of the container
         checkPayload(nextContainer, expectedContainerId);
      }

      // There must not be any further containers
      if (!reverseReading) {
         Assert.assertFalse(containerIterator.hasNext());
      }
   }

   /**
    * Checks whether the bytes returned for the given data block equal the expected bytes.
    *
    * @param dataBlock
    *           The {@link DataBlock}.
    */
   private void checkDataBlockBytes(DataBlock dataBlock) {

      long totalSize = dataBlock.getSize();

      long[] readOffsets = new long[] { 0, totalSize / 2, totalSize - 1, };
      int[] readSizes = new int[] { totalSize > 30 ? 31 : (int) totalSize,
         totalSize / 2 < 10 ? (int) totalSize / 2 : (int) totalSize / 10, 1, };

      long absOffset = dataBlock.getOffset().getAbsoluteMediumOffset();

      for (int i = 0; i < readSizes.length; i++) {

         if (readSizes[i] != 0) {
            ByteBuffer expectedBytes = expectationProvider.getExpectedBytes(absOffset + readOffsets[i], readSizes[i]);

            ByteBuffer actualBytes = dataBlock.getBytes(dataBlock.getOffset().advance(readOffsets[i]), readSizes[i]);

            org.junit.Assert.assertEquals(expectedBytes, actualBytes);
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

      Assert.assertEquals("Block size for instance: <" + instanceId + "> ", expectedSize, dataBlock.getSize());
   }

   /**
    * Checks whether the binary value returned for the given field equals the expected bytes.
    *
    * @param field
    *           The field to check.
    */
   private void checkFieldBinaryValue(Field<?> field) {

      long totalSize = field.getSize();

      long[] readOffsets = new long[] { 0, totalSize / 2, totalSize - 1, };
      int[] readSizes = new int[] { totalSize > 30 ? 31 : (int) totalSize,
         totalSize / 2 < 10 ? (int) totalSize / 2 : (int) totalSize / 10, 1, };

      long absOffset = field.getOffset().getAbsoluteMediumOffset();

      for (int i = 0; i < readSizes.length; i++) {
         ByteBuffer expectedBytes = expectationProvider.getExpectedBytes(absOffset + readOffsets[i], readSizes[i]);

         try {
            ByteBuffer binaryValue = field.getBinaryValue();

            byte[] actualBytes = ByteBufferUtils.asByteArrayCopy(binaryValue, (int) readOffsets[i], readSizes[i]);

            org.junit.Assert.assertEquals(expectedBytes, ByteBuffer.wrap(actualBytes));
         } catch (InterpretedValueConversionException e) {
            Assert.fail("Unexpected conversion exception" + e);
         }
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

      Assert.assertEquals(parentInstanceId.toString(), expectedFieldIds.size(), fieldList.size());

      for (int i = 0; i < expectedFieldIds.size(); ++i) {
         DataBlockInstanceId expectedFieldId = expectedFieldIds.get(i);

         Field<?> field = fieldList.get(i);

         Assert.assertEquals(DataBlockState.PERSISTED, field.getState());

         Assert.assertEquals(expectedFieldId.getId(), field.getId());
         // Check size of the field
         checkDataBlockSize(field, expectedFieldId);

         // Check the bytes of field
         checkDataBlockBytes(field);
         checkFieldBinaryValue(field);

         Object expectedFieldValue = expectationProvider.getExpectedFieldInterpretedValue(expectedFieldId);

         // Do not check the field value, as it is allowed to have any value
         if (expectedFieldValue.equals(AbstractMediumExpectationProvider.ANY_WILDCARD)) {
            continue;
         }

         try {
            Object actualFieldValue = field.getInterpretedValue();

            if (actualFieldValue instanceof byte[]) {
               byte[] actualFieldValueAsByteArray = (byte[]) actualFieldValue;

               Assert.assertArrayEquals((byte[]) expectedFieldValue, actualFieldValueAsByteArray);
            } else {
               Assert.assertEquals(expectedFieldValue, actualFieldValue);
            }
         } catch (BinaryValueConversionException e) {
            ExpectedFailedFieldConversionData convData = expectationProvider
               .getExpectedFailingFieldConversions(expectedFieldId);

            if (convData == null) {
               Assert.fail("Unexpected conversion exception" + e);
            } else {
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

      List<FieldSequence> headerOrFooterList = new ArrayList<>();

      if (checkHeaders) {
         headerOrFooterList.addAll(parentContainer.getHeaders());
      } else {
         headerOrFooterList.addAll(parentContainer.getFooters());
      }

      Assert.assertNotNull(headerOrFooterList);

      List<DataBlockInstanceId> expectedHeaderIds = null;

      if (checkHeaders) {
         expectedHeaderIds = expectationProvider.getExpectedChildBlocksOfType(parentContainerInstanceId,
            PhysicalDataBlockType.HEADER);
      } else {
         expectedHeaderIds = expectationProvider.getExpectedChildBlocksOfType(parentContainerInstanceId,
            PhysicalDataBlockType.FOOTER);
      }

      Assert.assertEquals(expectedHeaderIds.size(), headerOrFooterList.size());

      for (int i = 0; i < expectedHeaderIds.size(); ++i) {
         DataBlockInstanceId expectedHeaderId = expectedHeaderIds.get(i);

         FieldSequence header = headerOrFooterList.get(i);

         Assert.assertEquals(DataBlockState.PERSISTED, header.getState());

         Assert.assertEquals(expectedHeaderId.getId(), header.getId());
         // Check size of the header or footer
         checkDataBlockSize(header, expectedHeaderId);

         // Check the bytes of header or footer
         checkDataBlockBytes(header);

         checkFields(header, expectedHeaderId);
      }
   }

   /**
    * Checks the payload of the given {@link Container}.
    *
    * @param parentContainer
    *           The parent {@link Container}.
    * @param parentContainerInstanceId
    *           The {@link DataBlockInstanceId} of the parent.
    */
   private void checkPayload(Container parentContainer, DataBlockInstanceId parentContainerInstanceId) {

      Payload payload = parentContainer.getPayload();

      Assert.assertNotNull(payload);

      DataBlockInstanceId expectedPayloadId = null;
      boolean hasFieldBasedPayload = false;

      List<DataBlockInstanceId> payloadChildren = expectationProvider
         .getExpectedChildBlocksOfType(parentContainerInstanceId, PhysicalDataBlockType.FIELD_BASED_PAYLOAD);

      if (payloadChildren.size() > 0) {
         expectedPayloadId = payloadChildren.get(0);
         hasFieldBasedPayload = true;
      } else {
         payloadChildren = expectationProvider.getExpectedChildBlocksOfType(parentContainerInstanceId,
            PhysicalDataBlockType.CONTAINER_BASED_PAYLOAD);

         if (payloadChildren.size() > 0) {
            expectedPayloadId = payloadChildren.get(0);
         }
      }

      if (expectedPayloadId != null) {

         Assert.assertEquals(DataBlockState.PERSISTED, payload.getState());

         Assert.assertEquals(expectedPayloadId.getId(), payload.getId());
         // Check size of the payload
         checkDataBlockSize(payload, expectedPayloadId);

         // Check the bytes of the payload
         checkDataBlockBytes(payload);

         if (hasFieldBasedPayload) {
            checkFields((FieldBasedPayload) payload, expectedPayloadId);
         } else {
            checkContainers(((ContainerBasedPayload) payload).getContainerIterator(), expectedPayloadId, false);
         }
      }
   }

   /**
    * Creates the {@link AbstractMedium} instances to be tested by the super class.
    */
   private void checkTestFile() {

      Path theFile = getFileForMediaContents();

      if (theFile == null || !Files.exists(theFile)) {
         throw new InvalidTestDataException("Invalid file returned by method getFileForMediaContents", null);
      }
   }

   /**
    * Creates a {@link FileMedium}.
    */
   private Medium<?> createFileMedium(Path baseFile, MediumAccessType mediumAccessType, long maxCacheSize,
      int maxReadWriteBlockSize) {
      return new FileMedium(baseFile, mediumAccessType, maxCacheSize, maxReadWriteBlockSize);
   }

   /**
    * Creates an {@link InMemoryMedium}.
    */
   private Medium<?> createInMemoryMedium(Path baseFile, MediumAccessType mediumAccessType, long maxCacheSize,
      int maxReadWriteBlockSize) {

      byte[] byteContent = MediaTestUtility.readFileContent(baseFile);

      return new InMemoryMedium(byteContent, "TestMemMedium", mediumAccessType, maxCacheSize, maxReadWriteBlockSize);
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

   private void resetMaxSizes(long maxCacheSizeToUse, int maxRWBSizeToUse) {
      MediaAPI mediaAPI = ComponentRegistry.lookupService(MediaAPI.class);

      ((StandardMediaAPI) mediaAPI).setMinimumCacheSize(maxCacheSizeToUse);
      ((StandardMediaAPI) mediaAPI).setMinimumReadWriteBlockSize(maxRWBSizeToUse);
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.AbstractLowLevelAPITest#createExpectationProvider()
    */
   protected AbstractMediumExpectationProvider createExpectationProvider() throws InvalidTestDataCsvFormatException {

      return new CsvFileMediumExpectationProvider(getDataFormatRepository(), testFile, csvFile);
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
    * @see com.github.jmeta.library.datablocks.api.services.AbstractLowLevelAPITest#getFieldSizesForTestingLazyFields()
    */
   protected List<Integer> getFieldSizesForTestingLazyFields() {

      return fieldSizes;
   }

   /**
    * @see com.github.jmeta.library.datablocks.api.services.AbstractLowLevelAPITest#getFileForMediaContents()
    */
   protected Path getFileForMediaContents() {

      return testFile;
   }

   /**
    * Returns the {@link LowLevelAPI} for testing.
    *
    * @return the {@link LowLevelAPI} for testing.
    */
   protected LowLevelAPI getTestling() {

      return testling;
   }
}
