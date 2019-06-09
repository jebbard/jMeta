/**
 *
 * {@link AbstractReadOnlyMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.api.services;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.exceptions.ReadOnlyMediumException;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.cache.MediumCache;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManager;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.offset.MediumOffsetFactory;
import com.github.jmeta.library.media.impl.store.StandardMediumStore;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractReadOnlyMediumStoreTest} contains negative tests for
 * {@link MediumStore} write methods on read-only {@link Medium} instances.
 *
 * In addition it contains all general method tests for
 * {@link MediumStore#open()}, {@link MediumStore#close()},
 * {@link MediumStore#getMedium()} and
 * {@link MediumStore#createMediumOffset(long)}.
 *
 * The reason for separating this class from others:
 * <ul>
 * <li>Read-only media only behave different than writable media for the write
 * methods</li>
 * <li>In addition, putting the general test cases at the top of the class
 * hierarchy the other test class(es) would lead to a lot of duplicate test
 * cases executed, here they are just run once per medium type</li>
 * </ul>
 *
 * The media used for testing all must contain
 * {@link TestMedia#FIRST_TEST_FILE_CONTENT}, a String fully containing only
 * human-readable standard ASCII characters. This guarantees that 1 bytes = 1
 * character. Furthermore, all bytes inserted must also be standard
 * human-readable ASCII characters with this property.
 *
 * @param <T> The type of {@link Medium} to test
 */
public abstract class AbstractReadOnlyMediumStoreTest<T extends Medium<?>> {

	/**
	 * Validates all test files needed in this test class
	 */
	@BeforeClass
	public static void validateTestFiles() {
		TestMedia.validateTestFiles();
	}

	protected MediumStore mediumStoreUnderTest;

	protected T currentMedium;

	/**
	 * Tests {@link MediumStore#close()}.
	 */
	@Test(expected = MediumStoreClosedException.class)
	public void close_onClosedMediumStore_throwsException() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		mediumStoreUnderTest.close();
		mediumStoreUnderTest.close();
	}

	/**
	 * Tests {@link MediumStore#close()}.
	 */
	@Test
	public void close_onOpenedMediumStore_closesStore() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		mediumStoreUnderTest.close();

		Assert.assertFalse(mediumStoreUnderTest.isOpened());
	}

	/**
	 * Creates a {@link Medium} for testing.
	 * 
	 * @return a {@link Medium} for testing
	 * 
	 * @throws IOException In case of any errors creating the {@link Medium}
	 */
	protected abstract T createMedium() throws IOException;

	/**
	 * Creates a test class implementation specific {@link MediumAccessor} to use
	 * for testing.
	 * 
	 * @param mediumToUse The {@link Medium} to use for the {@link MediumStore}.
	 * @return a {@link MediumAccessor} to use based on a given {@link Medium}.
	 */
	protected abstract MediumAccessor<T> createMediumAccessor(T mediumToUse);

	/**
	 * Tests {@link MediumStore#createMediumOffset(long)}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void createMediumReference_forInvalidOffset_throwsException() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		mediumStoreUnderTest.createMediumOffset(-10);
	}

	/**
	 * Tests {@link MediumStore#createMediumOffset(long)}.
	 */
	@Test(expected = MediumStoreClosedException.class)
	public void createMediumReference_onClosedMediumStore_throwsException() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.createMediumOffset(10);
	}

	/**
	 * Tests {@link MediumStore#createMediumOffset(long)}.
	 */
	@Test
	public void createMediumReference_onOpenedMediumStore_returnsExpectedReference() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		int offset = 10;
		MediumOffset actualReference = mediumStoreUnderTest.createMediumOffset(offset);

		Assert.assertEquals(TestMedia.at(currentMedium, offset), actualReference);
	}

	/**
	 * Creates a {@link MediumStore} based on a default {@link Medium}.
	 * 
	 * @return a {@link MediumStore} based on a default {@link Medium}
	 */
	protected MediumStore createMediumStore() {
		try {
			currentMedium = createMedium();

			return createMediumStoreToTest(currentMedium);
		} catch (IOException e) {
			throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
		}
	}

	/**
	 * Creates a {@link MediumStore} to test based on a given {@link Medium}.
	 * 
	 * @param mediumToUse The {@link Medium} to use for the {@link MediumStore}.
	 * @return a {@link MediumStore} to test based on a given {@link Medium}.
	 */
	protected MediumStore createMediumStoreToTest(T mediumToUse) {
		MediumOffsetFactory mediumReferenceFactory = new MediumOffsetFactory(mediumToUse);
		return new StandardMediumStore<>(createMediumAccessor(mediumToUse), new MediumCache(mediumToUse),
			mediumReferenceFactory, new MediumChangeManager(mediumReferenceFactory));
	}

	/**
	 * Tests {@link MediumStore#flush()}.
	 */
	@Test(expected = ReadOnlyMediumException.class)
	public void flush_onReadOnlyMedium_throwsException() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		mediumStoreUnderTest.flush();
	}

	/**
	 * Tests {@link MediumStore#getMedium()}.
	 */
	@Test
	public void getMedium_onClosedMediumStore_returnsExpectedMedium() {
		mediumStoreUnderTest = createMediumStore();

		Assert.assertEquals(currentMedium, mediumStoreUnderTest.getMedium());
	}

	/**
	 * Tests {@link MediumStore#getMedium()}.
	 */
	@Test
	public void getMedium_onOpenedMediumStore_returnsExpectedMedium() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		Assert.assertEquals(currentMedium, mediumStoreUnderTest.getMedium());
	}

	/**
	 * Tests {@link MediumStore#insertData(MediumOffset, ByteBuffer)}.
	 */
	@Test(expected = ReadOnlyMediumException.class)
	public void insertData_onReadOnlyMedium_throwsException() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		mediumStoreUnderTest.insertData(TestMedia.at(currentMedium, 10), ByteBuffer.allocate(10));
	}

	/**
	 * Tests {@link MediumStore#isOpened()}.
	 */
	@Test
	public void isOpened_onNewMediumStore_returnsFalse() {
		mediumStoreUnderTest = createMediumStore();

		Assert.assertFalse(mediumStoreUnderTest.isOpened());
	}

	/**
	 * Tests {@link MediumStore#isOpened()} and {@link MediumStore#open()}.
	 */
	@Test
	public void isOpened_onOpenedMediumStore_returnsTrue() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		Assert.assertTrue(mediumStoreUnderTest.isOpened());
	}

	/**
	 * Tests {@link MediumStore#close()}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void open_onOpenedMediumStore_throwsException() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();
		mediumStoreUnderTest.open();
	}

	/**
	 * Tests {@link MediumStore#removeData(MediumOffset, int)}.
	 */
	@Test(expected = ReadOnlyMediumException.class)
	public void removeData_onReadOnlyMedium_throwsException() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		mediumStoreUnderTest.removeData(TestMedia.at(currentMedium, 10), 20);
	}

	/**
	 * Tests
	 * {@link MediumStore#replaceData(MediumOffset, int, java.nio.ByteBuffer)}.
	 */
	@Test(expected = ReadOnlyMediumException.class)
	public void replaceData_onReadOnlyMedium_throwsException() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		mediumStoreUnderTest.replaceData(TestMedia.at(currentMedium, 10), 20, ByteBuffer.allocate(10));
	}

	/**
	 * Closes the {@link MediumStore} under test, if necessary.
	 */
	@After
	public void tearDown() {
		if ((mediumStoreUnderTest != null) && mediumStoreUnderTest.isOpened()) {
			mediumStoreUnderTest.close();
		}
	}

	/**
	 * Tests
	 * {@link MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)}.
	 */
	@Test(expected = ReadOnlyMediumException.class)
	public void undo_onReadOnlyMedium_throwsException() {
		mediumStoreUnderTest = createMediumStore();

		mediumStoreUnderTest.open();

		mediumStoreUnderTest.undo(
			new MediumAction(MediumActionType.REMOVE, new MediumRegion(TestMedia.at(currentMedium, 10), 20), 0, null));
	}
}
