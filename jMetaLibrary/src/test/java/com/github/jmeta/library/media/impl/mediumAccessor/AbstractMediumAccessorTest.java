/**
 * {@link AbstractMediumAccessorTest}.java
 *
 * @author Jens Ebert
 * @date 09.12.10 21:22:54 (December 9, 2010)
 */

package com.github.jmeta.library.media.impl.mediumAccessor;

import java.nio.ByteBuffer;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * Tests the interface {@link MediumAccessor}. Basic idea is to work on the
 * {@link TestMedia#FIRST_TEST_FILE_PATH}. Its contents is just ASCII bytes that
 * are read once at the beginning of test execution and determined as expected
 * content. Then reading and writing is tested based on this.
 */
public abstract class AbstractMediumAccessorTest {

	/**
	 * {@link ReadTestData} summarizes offset and size of test data for tests of
	 * reading.
	 */
	protected static class ReadTestData {

		private int offsetToRead;

		private int sizeToRead;

		private int expectedBytesOffset;

		/**
		 * This constructor is used for testing random-access implementations, where the
		 * offset to read is actually relevant and the bytes read are expected at the
		 * same offset.
		 *
		 * @param offsetToRead The offset to read bytes from or null to indicate stream
		 *                     medium.
		 * @param sizeToRead   The number of bytes to read.
		 */
		public ReadTestData(int offsetToRead, int sizeToRead) {
			this(offsetToRead, sizeToRead, offsetToRead);
		}

		/**
		 * This constructor is used for testing stream implementations, where the offset
		 * to read is ignored and no matter what its value is, the bytes are read just
		 * sequentially. Thus, the offsetToRead might be arbitrary, while the expected
		 * byte offsets are different.
		 *
		 * @param offsetToRead        The offset to read from
		 * @param sizeToRead          The size to read
		 * @param expectedBytesOffset The expected read offset
		 */
		public ReadTestData(int offsetToRead, int sizeToRead, Integer expectedBytesOffset) {
			this.offsetToRead = offsetToRead;
			this.sizeToRead = sizeToRead;
			this.expectedBytesOffset = expectedBytesOffset;
		}
	}

	private static byte[] EXPECTED_FILE_CONTENTS;

	/**
	 * Reads the contents of the {@link TestMedia#FIRST_TEST_FILE_PATH} into memory
	 * to make it available for expectation testing.
	 */
	@BeforeClass
	public static void determineExpectedFileContents() {
		TestMedia.validateTestFiles();

		AbstractMediumAccessorTest.EXPECTED_FILE_CONTENTS = TestMedia.FIRST_TEST_FILE_CONTENT.getBytes();
	}

	/**
	 * Returns the bytes expected in the {@link Medium} used for testing.
	 *
	 * @return the bytes expected in the {@link Medium} used for testing
	 */
	protected static byte[] getExpectedMediumContent() {
		return AbstractMediumAccessorTest.EXPECTED_FILE_CONTENTS;
	}

	/**
	 * Encapsulates test calls to {@link MediumAccessor#read(int)}, without
	 * expecting an end of medium during read, i.e. if it occurs, a test failure is
	 * generated.
	 *
	 * @param mediumAccessor The {@link MediumAccessor} to use.
	 * @param readTestData   The {@link ReadTestData} to use.
	 * @return The {@link ByteBuffer} of data read, returned by
	 *         {@link MediumAccessor#read(int)}
	 */
	protected static ByteBuffer performReadNoEOMExpected(MediumAccessor<?> mediumAccessor, ReadTestData readTestData) {
		mediumAccessor.setCurrentPosition(TestMedia.at(mediumAccessor.getMedium(), readTestData.offsetToRead));

		try {
			ByteBuffer readContent = mediumAccessor.read(readTestData.sizeToRead);

			return readContent;
		}

		catch (EndOfMediumException e) {
			Assert.fail("Unexpected end of medium detected! Exception: " + e);
		}

		return null;
	}

	private MediumAccessor<?> mediumAccessor;

	/**
	 * Tests {@link MediumAccessor#close()}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void close_onClosedMediumAccessor_throwsException() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		mediumAccessor.close();
		// Close twice
		mediumAccessor.close();
	}

	/**
	 * Tests {@link MediumAccessor#isOpened()} and {@link MediumAccessor#close()}.
	 */
	@Test
	public void close_onOpenedMediumAccessor_isOpenedReturnsFalse() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		mediumAccessor.close();

		Assert.assertFalse(mediumAccessor.isOpened());
	}

	/**
	 * Tests {@link MediumAccessor#getCurrentPosition()}.
	 */
	@Test
	public void getCurrentPosition_afterReadUntilEOM_changedByNumberOfReadBytesUntilEOM() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		ReadTestData readTestData = getReadTestDataUntilEndOfMedium();

		int sizeToRead = readTestData.sizeToRead + 10;

		MediumOffset initialPosition = TestMedia.at(mediumAccessor.getMedium(), readTestData.offsetToRead);

		mediumAccessor.setCurrentPosition(initialPosition);

		try {
			mediumAccessor.read(sizeToRead);

			Assert.fail("Expected end of medium exception, but it did not occur!");
		}

		catch (EndOfMediumException e) {
			Assert.assertEquals(mediumAccessor.getMedium(), mediumAccessor.getCurrentPosition().getMedium());
			Assert.assertEquals(initialPosition.advance(e.getByteCountActuallyRead()),
				mediumAccessor.getCurrentPosition());
		}
	}

	/**
	 * Tests {@link MediumAccessor#getCurrentPosition()}.
	 */
	@Test
	public void getCurrentPosition_afterReadWithoutEOM_changedByNumberOfReadBytes() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		int sizeToRead = 10;
		AbstractMediumAccessorTest.performReadNoEOMExpected(mediumAccessor, new ReadTestData(0, sizeToRead));

		Assert.assertEquals(mediumAccessor.getMedium(), mediumAccessor.getCurrentPosition().getMedium());
		Assert.assertEquals(sizeToRead, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
	}

	/**
	 * Tests {@link MediumAccessor#getCurrentPosition()}.
	 */
	@Test
	public void getCurrentPosition_forNewlyOpenedMediumAccessor_returnsZero() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		Assert.assertEquals(mediumAccessor.getMedium(), mediumAccessor.getCurrentPosition().getMedium());
		Assert.assertEquals(0, mediumAccessor.getCurrentPosition().getAbsoluteMediumOffset());
	}

	/**
	 * Tests {@link MediumAccessor#getMedium()}.
	 */
	@Test
	public void getMedium_onClosedMediumAccessor_returnsExpectedMedium() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		mediumAccessor.close();

		Assert.assertEquals(getExpectedMedium(), mediumAccessor.getMedium());
	}

	/**
	 * Tests {@link MediumAccessor#getMedium()}.
	 */
	@Test
	public void getMedium_onOpenedMediumAccessor_returnsExpectedMedium() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		Assert.assertEquals(getExpectedMedium(), mediumAccessor.getMedium());
	}

	/**
	 * Tests {@link MediumAccessor#isAtEndOfMedium()}.
	 */
	@Test
	public void isAtEndOfMedium_ifAtEndOfMedium_doesNotAdvanceCurrentPosition() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		ReadTestData readOverEndOfMedium = getReadTestDataUntilEndOfMedium();

		// The explicit read is only really necessary for stream media, see a similar
		// test case without read for
		// random-access media
		AbstractMediumAccessorTest.performReadNoEOMExpected(mediumAccessor, readOverEndOfMedium);

		MediumOffset offsetBeforeIsAtEOM = mediumAccessor.getCurrentPosition();

		mediumAccessor.isAtEndOfMedium();
		Assert.assertEquals(offsetBeforeIsAtEOM, mediumAccessor.getCurrentPosition());
	}

	/**
	 * Tests {@link MediumAccessor#isAtEndOfMedium()}.
	 */
	@Test
	public void isAtEndOfMedium_ifAtEndOfMedium_returnsTrue() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		ReadTestData readOverEndOfMedium = getReadTestDataUntilEndOfMedium();

		// The explicit read is only really necessary for stream media, see a similar
		// test case without read for
		// random-access media
		AbstractMediumAccessorTest.performReadNoEOMExpected(mediumAccessor, readOverEndOfMedium);

		// Each call is checked twice to ensure it is repeatable (especially for
		// streams!)
		Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium());
		Assert.assertEquals(true, mediumAccessor.isAtEndOfMedium());
	}

	/**
	 * Tests {@link MediumAccessor#isAtEndOfMedium()}.
	 */
	@Test
	public void isAtEndOfMedium_ifNotAtEndOfMedium_doesNotAdanceCurrentPosition() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		MediumOffset offsetBeforeIsAtEOM = mediumAccessor.getCurrentPosition();

		mediumAccessor.isAtEndOfMedium();
		Assert.assertEquals(offsetBeforeIsAtEOM, mediumAccessor.getCurrentPosition());
	}

	/**
	 * Tests {@link MediumAccessor#isAtEndOfMedium()}.
	 */
	@Test
	public void isAtEndOfMedium_ifNotAtEndOfMedium_returnsFalse() {

		ReadTestData readOverEndOfMedium = getReadTestDataUntilEndOfMedium();

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		Medium<?> medium = mediumAccessor.getMedium();

		int readOffset = readOverEndOfMedium.offsetToRead;

		MediumOffset readReferenceOne = TestMedia.at(medium, 0);
		MediumOffset readReferenceTwo = TestMedia.at(medium, readOffset);

		// Each call is checked twice to ensure it is repeatable (especially for
		// streams!)
		mediumAccessor.setCurrentPosition(readReferenceOne);
		Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium());
		mediumAccessor.setCurrentPosition(readReferenceTwo);
		Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium());
		mediumAccessor.setCurrentPosition(readReferenceOne);
		Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium());
		mediumAccessor.setCurrentPosition(readReferenceTwo);
		Assert.assertEquals(false, mediumAccessor.isAtEndOfMedium());
	}

	/**
	 * Tests {@link MediumAccessor#isAtEndOfMedium()}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void isAtEndOfMedium_onClosedMediumAccessor_throwsException() {
		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		mediumAccessor.close();

		mediumAccessor.isAtEndOfMedium();
	}

	/**
	 * Tests {@link MediumAccessor#isOpened()}.
	 */
	@Test
	public void isOpened_forNewMediumAccessor_returnsFalse() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		Assert.assertFalse(mediumAccessor.isOpened());
	}

	/**
	 * Tests {@link MediumAccessor#isOpened()} and {@link MediumAccessor#open()}.
	 */
	@Test
	public void isOpened_forOpenedMediumAccessor_returnsTrue() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		Assert.assertTrue(mediumAccessor.isOpened());
	}

	/**
	 * Tests {@link MediumAccessor#close()}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void open_onOpenedMediumAccessor_throwsException() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();
		mediumAccessor.open();
	}

	/**
	 * Tests {@link MediumAccessor#read(int)}.
	 */
	@Test
	public void read_forAnyOffsetAndSize_returnsExpectedBytesAndLeavesLimitPositionOfBufferUnchanged() {

		final List<ReadTestData> readTestData = getReadTestDataToUse();

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		Medium<?> medium = mediumAccessor.getMedium();

		long mediumSizeBeforeRead = medium.getCurrentLength();

		for (ReadTestData readTestDataRecord : readTestData) {
			ByteBuffer readContent = AbstractMediumAccessorTest.performReadNoEOMExpected(mediumAccessor,
				readTestDataRecord);

			Assert.assertEquals(readTestDataRecord.sizeToRead, readContent.remaining());

			// Reads the correct contents
			assertEqualsFileContent(readContent, readTestDataRecord.expectedBytesOffset);
		}

		// Size did not change after read operations
		Assert.assertEquals(mediumSizeBeforeRead, medium.getCurrentLength());
	}

	/**
	 * Tests {@link MediumAccessor#read(int)}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void read_onClosedMediumAccessor_throwsException() {
		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		mediumAccessor.close();

		AbstractMediumAccessorTest.performReadNoEOMExpected(mediumAccessor, new ReadTestData(0, 5));
	}

	/**
	 * Tests {@link MediumAccessor#read(int)}.
	 */
	@Test
	public void read_untilEndOfMedium_throwsEndOfMediumException() {

		ReadTestData readOverEndOfMedium = getReadTestDataUntilEndOfMedium();

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		mediumAccessor.open();

		Medium<?> medium = mediumAccessor.getMedium();

		Integer readOffset = readOverEndOfMedium.offsetToRead;
		int readSize = readOverEndOfMedium.sizeToRead + 20;

		MediumOffset readReference = TestMedia.at(medium, readOffset);

		mediumAccessor.setCurrentPosition(readReference);

		try {
			mediumAccessor.read(readSize);

			Assert.fail("Expected end of medium exception, but it did not occur!");
		}

		catch (EndOfMediumException e) {
			Assert.assertEquals(readReference, e.getReadStartReference());
			Assert.assertEquals(readSize, e.getByteCountTriedToRead());
			Assert.assertEquals(AbstractMediumAccessorTest.getExpectedMediumContent().length - readOffset,
				e.getByteCountActuallyRead());
		}
	}

	/**
	 * Sets up the test case.
	 */
	@Before
	public void setUp() {
		prepareMediumData(AbstractMediumAccessorTest.getExpectedMediumContent());

		mediumAccessor = createImplementationToTest();

		if (mediumAccessor == null) {
			throw new InvalidTestDataException("The tested MediumAccessor must not be null", null);
		}

		validateReadTestData(getReadTestDataUntilEndOfMedium());
		getReadTestDataToUse().forEach((readTestData) -> validateReadTestData(readTestData));

		validateTestMedium(mediumAccessor.getMedium());
	}

	/**
	 * Tears the test case down.
	 */
	@After
	public void tearDown() {

		MediumAccessor<?> mediumAccessor = getImplementationToTest();

		if (mediumAccessor.isOpened()) {
			mediumAccessor.close();
		}
	}

	private void validateReadTestData(ReadTestData readTestData) {
		if (readTestData == null) {
			throw new InvalidTestDataException("read test data must not be null", null);
		}

		if (readTestData.offsetToRead > AbstractMediumAccessorTest.getExpectedMediumContent().length) {
			throw new InvalidTestDataException(
				"Read offset " + readTestData.offsetToRead + " exceeds the actual length of the test medium "
					+ AbstractMediumAccessorTest.getExpectedMediumContent().length,
				null);
		}
	}

	/**
	 * Asserts whether the given bytes read previously in a test case at the given
	 * zero based test file offset equal the test file contents at that offset.
	 *
	 * @param bytesRead  The bytes previously read
	 * @param fileOffset The zero-based file offset expected to contain the bytes
	 *                   matching the given bytes read
	 */
	protected void assertEqualsFileContent(ByteBuffer bytesRead, int fileOffset) {

		bytesRead.mark();

		int index = 0;

		Assert.assertTrue(
			bytesRead.remaining() + fileOffset <= AbstractMediumAccessorTest.getExpectedMediumContent().length);

		while (bytesRead.hasRemaining()) {
			Assert.assertEquals(AbstractMediumAccessorTest.getExpectedMediumContent()[fileOffset + index],
				bytesRead.get());

			index++;
		}

		bytesRead.reset();
	}

	/**
	 * Returns the concrete implementation of {@link MediumAccessor } to test. It is
	 * used for reading and writing from a {@link MediumAccessor}.
	 *
	 * @return the concrete implementation of {@link MediumAccessor } to test.
	 */
	protected abstract MediumAccessor<?> createImplementationToTest();

	/**
	 * @return the {@link Medium} of the current {@link MediumAccessor} tested
	 */
	protected abstract Medium<?> getExpectedMedium();

	/**
	 * @return The concrete {@link MediumAccessor} currently tested.
	 */
	protected MediumAccessor<?> getImplementationToTest() {
		return mediumAccessor;
	}

	/**
	 * Returns a Map of offsets in the {@link TestMedia#FIRST_TEST_FILE_PATH} that
	 * are checked using {@link MediumAccessor#read}. It is checked that the bytes
	 * read from that offset match the expected bytes from the
	 * {@link TestMedia#FIRST_TEST_FILE_PATH}. The given size to read is mapped to
	 * the offset.
	 *
	 * @return a Map of offsets in the {@link TestMedia#FIRST_TEST_FILE_PATH} that
	 *         are checked using {@link MediumAccessor#read}.
	 */
	protected abstract List<ReadTestData> getReadTestDataToUse();

	/**
	 * Returns a {@link ReadTestData} instance ranging from a specific offset until
	 * exactly the last byte of the medium.
	 *
	 * @return a {@link ReadTestData} instance ranging from a specific offset until
	 *         exactly the last byte of the medium.
	 */
	protected abstract ReadTestData getReadTestDataUntilEndOfMedium();

	/**
	 * This method is called during {@link #setUp()} to prepare the medium data to
	 * be tested in a sufficient way. E.g. in case of a file a prototypical test
	 * file might first be copied before doing the tests.
	 *
	 * @param testFileContents The contents of the test file
	 */
	protected abstract void prepareMediumData(byte[] testFileContents);

	/**
	 * Checks the test {@link Medium} to fulfill any preconditions for the tests.
	 *
	 * @param theMedium The {@link Medium} to test.
	 */
	protected abstract void validateTestMedium(Medium<?> theMedium);
}