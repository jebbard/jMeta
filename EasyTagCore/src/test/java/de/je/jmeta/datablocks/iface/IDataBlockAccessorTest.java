package de.je.jmeta.datablocks.iface;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import de.je.jmeta.datablocks.AbstractDataBlockIterator;
import de.je.jmeta.datablocks.BinaryValueConversionException;
import de.je.jmeta.datablocks.IContainer;
import de.je.jmeta.datablocks.IDataBlock;
import de.je.jmeta.datablocks.IDataBlockAccessor;
import de.je.jmeta.datablocks.IField;
import de.je.jmeta.datablocks.IFieldSequence;
import de.je.jmeta.datablocks.IHeader;
import de.je.jmeta.datablocks.IPayload;
import de.je.jmeta.datablocks.InterpretedValueConversionException;
import de.je.jmeta.datablocks.impl.InvalidTestDataCsvFormatException;
import de.je.jmeta.dataformats.BinaryValue;
import de.je.jmeta.dataformats.DataFormat;
import de.je.jmeta.dataformats.IDataFormatRepository;
import de.je.jmeta.dataformats.PhysicalDataBlockType;
import de.je.jmeta.media.api.IMedium;
import de.je.jmeta.media.api.datatype.AbstractMedium;
import de.je.jmeta.testHelpers.basics.JMetaTestBasics;
import de.je.util.javautil.simpleregistry.ISimpleComponentRegistry;
import de.je.util.javautil.testUtil.setup.TestDataException;
import junit.framework.Assert;

// TODO doItFirst005: write test case for "out of order" reading
// TODO doItFirst004: make ogg test case
// TODO doItFirst006: field function stack won't currently work when reading "out of order" in depth -
// notion of data block instance ids OR saving parent IDataBlock reference...

/**
 * {@link IDataBlockAccessorTest} tests the {@link IDataBlockAccessor} interface. Basically, a file medium contains the
 * data to read. Using the file contents, the three media types file, memory and stream are tested.
 */
public abstract class IDataBlockAccessorTest {

   private ISimpleComponentRegistry registryToUse;

   private final List<IMedium<?>> testedMedia = new ArrayList<>();

   private AbstractMediumExpectationProvider expectationProvider;

   private IDataFormatRepository dataFormatRepository;

   private IDataBlockAccessor testling;

   /**
    * Sets up the test fixtures.
    */
   @Before
   public void setUp() {

      JMetaTestBasics.emptyLogFile(JMetaTestBasics.DEFAULT_LOG_FILE);

      registryToUse = JMetaTestBasics.setupComponents();

      dataFormatRepository = registryToUse.getComponentImplementation(IDataFormatRepository.class);

      prepareTestedMedia();

      try {
         expectationProvider = createExpectationProvider();
      } catch (InvalidTestDataCsvFormatException e) {
         throw new TestDataException("Could not read test data.", e);
      }

      testling = registryToUse.getComponentImplementation(IDataBlockAccessor.class);

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
         IMedium<?> medium = testedMedia.get(i);

         testling.closeMedium(medium);
      }

      // Check log files
      JMetaTestBasics.performGeneralLogCheck(JMetaTestBasics.DEFAULT_LOG_FILE);
   }

   /**
    * Tests {@link IDataBlockAccessor#getContainerIterator}.
    */
   @Test
   public void test_getContainerIterator() {

      for (int i = 0; i < testedMedia.size(); ++i) {
         IMedium<?> medium = testedMedia.get(i);

         AbstractDataBlockIterator<IContainer> topLevelContainerIterator = getTestling().getContainerIterator(medium,
            new ArrayList<DataFormat>(), false);

         Assert.assertNotNull(topLevelContainerIterator);

         // Check the whole data block hierarchy returned for correctness
         checkContainers(topLevelContainerIterator, null, false);

         getTestling().closeMedium(medium);
      }
   }

   /**
    * Tests {@link IDataBlockAccessor#getContainerIterator} with the prior specification of a small lazy field size that
    * is equal to the size of one field and smaller than several other fields, therefore provoking the creation of lazy
    * fields.
    *
    * See {@link #getFieldSizesForTestingLazyFields()} for more details.
    */
   @Test
   public void test_getContainerIterator_withLazyField() {

      List<Integer> fieldSizesToTest = getFieldSizesForTestingLazyFields();

      for (int i = 0; i < fieldSizesToTest.size(); ++i) {
         Integer fieldSize = fieldSizesToTest.get(i);

         getTestling().setLazyFieldSize(fieldSize);

         // Call the usual test method
         test_getContainerIterator();
      }
   }

   /**
    * Tests {@link IDataBlockAccessor#getReverseContainerIterator(IMedium, List, boolean)}.
    */
   @Test
   public void test_getReverseContainerIterator() {

      for (int i = 0; i < testedMedia.size(); ++i) {
         IMedium<?> medium = testedMedia.get(i);

         // Reverse reading is supported for random access media only
         if (medium.isRandomAccess()) {
            AbstractDataBlockIterator<IContainer> topLevelContainerIterator = getTestling()
               .getReverseContainerIterator(medium, new ArrayList<DataFormat>(), false);

            Assert.assertNotNull(topLevelContainerIterator);

            // Check the whole data block hierarchy returned for correctness
            checkContainers(topLevelContainerIterator, null, true);

            getTestling().closeMedium(medium);
         }
      }
   }

   /**
    * Returns the {@link IDataFormatRepository} instance.
    * 
    * @return the {@link IDataFormatRepository} instance.
    */
   protected IDataFormatRepository getDataFormatRepository() {

      return dataFormatRepository;
   }

   /**
    * Returns the {@link IDataBlockAccessor} for testing.
    *
    * @return the {@link IDataBlockAccessor} for testing.
    */
   protected IDataBlockAccessor getTestling() {

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
   protected abstract List<IMedium<?>> createMediaToCheck(File baseFile) throws Exception;

   /**
    * Returns a single {@link File} object that contains the relevant base data from which the data blocks are read. The
    * data from the {@link File} is taken by this class and converted to several {@link AbstractMedium} instances
    * finally tested.
    *
    * @return a single {@link File} object that contains the relevant base data from which the data blocks are read.
    */
   protected abstract File getFileForMediaContents();

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
    * Recursively checks all {@link IContainer}s starting with the given iterator.
    *
    * @param containerIterator
    *           The {@link AbstractDataBlockIterator}
    * @param parentInstanceId
    *           The parent of the iterated children or null if it is a top-level container iterator.
    * @param reverseReading
    *           true if the containers are currently read in reverse mode, false otherwise.
    */
   private void checkContainers(AbstractDataBlockIterator<IContainer> containerIterator,
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
         IContainer nextContainer = containerIterator.next();

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
    * Checks the payload of the given {@link IContainer}.
    *
    * @param parentContainer
    *           The parent {@link IContainer}.
    * @param parentContainerInstanceId
    *           The {@link DataBlockInstanceId} of the parent.
    * @param fieldsFirst
    *           true to read child fields of the payload before child containers, false to read child containers of the
    *           payload before child fields.
    */
   private void checkPayload(IContainer parentContainer, DataBlockInstanceId parentContainerInstanceId,
      boolean fieldsFirst) {

      IPayload payload = parentContainer.getPayload();

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
    * Checks {@link IHeader}s of the given {@link IContainer} for correctness.
    *
    * Should be called twice, once for headers, once for footers.
    *
    * @param parentContainer
    *           The parent {@link IContainer} for which to check the {@link IHeader}s.
    * @param parentContainerInstanceId
    *           {@link DataBlockInstanceId} of the parent container.
    * @param checkHeaders
    *           true if to check headers, false for checking footers.
    */
   private void checkHeadersFooters(IContainer parentContainer, DataBlockInstanceId parentContainerInstanceId,
      boolean checkHeaders) {

      List<IHeader> headerList = null;

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

         IHeader header = headerList.get(i);

         Assert.assertEquals(expectedHeaderId.getId(), header.getId());
         // Check size of the header or footer
         checkDataBlockSize(header, expectedHeaderId);

         // Check the bytes of header or footer
         checkDataBlockBytes(header);

         checkFields(header, expectedHeaderId);
      }
   }

   /**
    * Checks {@link IField}s of the given {@link IDataBlock} parent for correctness.
    *
    * @param parentSequence
    *           The parent {@link IFieldSequence} for which to check the {@link IField}s.
    * @param parentInstanceId
    *           The {@link DataBlockInstanceId} of the parent.
    */
   private void checkFields(IFieldSequence parentSequence, DataBlockInstanceId parentInstanceId) {

      List<IField<?>> fieldList = parentSequence.getFields();

      Assert.assertNotNull(fieldList);

      List<DataBlockInstanceId> expectedFieldIds = expectationProvider.getExpectedChildBlocksOfType(parentInstanceId,
         PhysicalDataBlockType.FIELD);

      Assert.assertEquals(expectedFieldIds.size(), fieldList.size());

      for (int i = 0; i < expectedFieldIds.size(); ++i) {
         DataBlockInstanceId expectedFieldId = expectedFieldIds.get(i);

         IField<?> field = fieldList.get(i);

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
    *           The {@link IDataBlock}.
    */
   private void checkDataBlockBytes(IDataBlock dataBlock) {

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
   private void checkFieldBinaryValue(IField<?> field) {

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
    *           The {@link IDataBlock}.
    * @param instanceId
    *           The {@link DataBlockInstanceId} of the {@link IDataBlock}.
    */
   private void checkDataBlockSize(IDataBlock dataBlock, DataBlockInstanceId instanceId) {

      long expectedSize = expectationProvider.getExpectedDataBlockSize(instanceId);

      Assert.assertEquals(expectedSize, dataBlock.getTotalSize());
   }

   /**
    * Creates the {@link AbstractMedium} instances to be tested by the super class.
    */
   private void prepareTestedMedia() {

      File theFile = getFileForMediaContents();

      if (theFile == null || !theFile.exists())
         throw new TestDataException("Invalid file returned by method getFileForMediaContents", null);

      try {
         List<IMedium<?>> media = createMediaToCheck(theFile);

         testedMedia.addAll(media);
      } catch (Exception e) {
         throw new RuntimeException("Unexpected exception during medium preparation", e);
      }
   }
}
