/**
 *
 * {@link AbstractMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.api.services;

import static com.github.jmeta.library.media.api.helper.MediaTestUtility.at;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.After;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import com.github.jmeta.library.media.api.exceptions.EndOfMediumException;
import com.github.jmeta.library.media.api.exceptions.MediumStoreClosedException;
import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumAction;
import com.github.jmeta.library.media.api.types.MediumActionType;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.library.media.impl.cache.MediumCache;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManager;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.reference.MediumReferenceFactory;
import com.github.jmeta.library.media.impl.store.StandardMediumStore;
import com.github.jmeta.utility.charset.api.services.Charsets;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.testsetup.api.exceptions.InvalidTestDataException;

/**
 * {@link AbstractMediumStoreTest} is the base class for testing the {@link MediumStore} interface. In contrast to
 * {@link AbstractReadOnlyMediumStoreTest}, it uses also writable media, and it does not contain the default test cases
 * for {@link MediumStore#open()}, {@link MediumStore#close()}, {@link MediumStore#getMedium()} and
 * {@link MediumStore#createMediumReference(long)}.
 * 
 * Each subclass corresponds to a specific {@link Medium} type, a read-only or writable {@link Medium} instance, as well
 * as a cached or un-cached medium. The sub-class hierarchy is non-trivial, so here is the explanation of the purpose of
 * each abstract subclass:
 * <ul>
 * <li>{@link AbstractMediumStoreTest}: Contains all tests that should run for any medium type and caching combinations,
 * thus e.g. all general negative tests (closed media and wrong reference)</li>
 * <li>{@link AbstractCachedMediumStoreTest}: Tests based on a cached medium; most of the "normal" read test cases go
 * here, no write test cases and no parameter negative tests</li>
 * <li>{@link AbstractCachedAndWritableRandomAccessMediumStoreTest}: Tests based on a cached, writable and thus
 * random-access medium; additional "normal" write test cases go here and no parameter negative tests</li>
 * <li>{@link AbstractUnCachedMediumStoreTest}: Tests based on an un-cached medium; only special read test cases
 * specifically designed for an un-cached medium go here, no write test cases</li>
 * <li>{@link AbstractUnCachedAndWritableRandomAccessMediumStoreTest}: Tests based on an un-cached, writable and thus
 * random-access medium; only special write test cases specifically designed for an un-cached medium go here</li>
 * </ul>
 * 
 * The filled media used for testing all must contain {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}, a String fully
 * containing only human-readable standard ASCII characters. This guarantees that 1 bytes = 1 character. Furthermore,
 * all bytes inserted must also be standard human-readable ASCII characters with this property.
 * 
 * There are specific naming conventions for testing {@link MediumStore#getData(MediumReference, int)} and
 * {@link MediumStore#cache(MediumReference, int)}: [method name]_[medium type]_[parameter values, esp. offset
 * range]_[expected behaviour].
 *
 * @param <T>
 *           The type of {@link Medium} to use
 */
@RunWith(MockitoJUnitRunner.class)
public abstract class AbstractMediumStoreTest<T extends Medium<?>> {

   /**
    * For getting the current test case's name, must be public
    */
   @Rule
   public TestName testName = new TestName();

   protected MediumStore mediumStoreUnderTest;

   protected T currentMedium;

   protected MediumAccessor<T> mediumAccessorSpy;

   protected MediumCache mediumCacheSpy;

   protected MediumReferenceFactory mediumReferenceFactorySpy;

   protected MediumChangeManager mediumChangeManagerSpy;

   /**
    * Validates all test files needed in this test class
    */
   @BeforeClass
   public static void validateTestFiles() {
      MediaTestFiles.validateTestFiles();
   }

   /**
    * Closes the {@link MediumStore} under test, if necessary.
    */
   @After
   public void tearDown() {
      if (mediumStoreUnderTest != null && mediumStoreUnderTest.isOpened()) {
         mediumStoreUnderTest.close();
      }
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void isAtEndOfMedium_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.isAtEndOfMedium(at(currentMedium, 10));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void isAtEndOfMedium_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.isAtEndOfMedium(at(MediaTestUtility.OTHER_MEDIUM, 10));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumReference)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void getCachedByteCountAt_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      Assert.assertEquals(0, mediumStoreUnderTest.getCachedByteCountAt(at(currentMedium, 0)));
   }

   /**
    * Tests {@link MediumStore#getCachedByteCountAt(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getCachedByteCountAt_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.getCachedByteCountAt(at(MediaTestUtility.OTHER_MEDIUM, 10));
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void cache_forClosedMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
   }

   /**
    * Tests {@link MediumStore#cache(MediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void cache_forReferencePointingToWrongMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(MediaTestUtility.OTHER_MEDIUM, 10), 10);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMedium_noCacheBefore_returnsExpectedData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      long getDataStartOffset = 5;
      int getDataSize = 400;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMedium_partlyCacheBeforeWithGaps_returnsExpectedData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 10), 10);
      cacheNoEOMExpected(at(currentMedium, 30), 100);
      cacheNoEOMExpected(at(currentMedium, 135), 200);

      long getDataStartOffset = 5;
      int getDataSize = 400;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMedium_fullMediumCacheBefore_returnsExpectedData() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      cacheNoEOMExpected(at(currentMedium, 0), currentMediumContent.length());

      long getDataStartOffset = 5;
      int getDataSize = 400;

      testGetData_returnsExpectedData(at(currentMedium, getDataStartOffset), getDataSize, currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forEmptyMedium_fromStart_throwsEOMException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      testGetData_throwsEndOfMediumException(at(currentMedium, 0), 1, currentMedium.getMaxReadWriteBlockSizeInBytes(),
         currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forFilledMedium_fromMiddleToBeyondEOMAndNoCacheBefore_throwsEOMException() {
      mediumStoreUnderTest = createDefaultFilledMediumStore();

      String currentMediumContent = getMediumContentAsString(currentMedium);

      mediumStoreUnderTest.open();

      testGetData_throwsEndOfMediumException(at(currentMedium, 15), currentMediumContent.length(),
         currentMedium.getMaxReadWriteBlockSizeInBytes(), currentMediumContent);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void getData_forClosedMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      getDataNoEOMExpected(at(currentMedium, 10), 10);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void getData_forReferencePointingToWrongMedium_anyRange_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      getDataNoEOMExpected(at(MediaTestUtility.OTHER_MEDIUM, 10), 10);
   }

   /**
    * Tests {@link MediumStore#replaceData(MediumReference, int, java.nio.ByteBuffer)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void replaceData_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.replaceData(at(currentMedium, 10), 20, ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#isAtEndOfMedium(MediumReference)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void replaceData_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.replaceData(at(MediaTestUtility.OTHER_MEDIUM, 10), 20, ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#removeData(MediumReference, int)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void removeData_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.removeData(at(currentMedium, 10), 20);
   }

   /**
    * Tests {@link MediumStore#removeData(MediumReference, int)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void removeData_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.removeData(at(MediaTestUtility.OTHER_MEDIUM, 10), 20);
   }

   /**
    * Tests {@link MediumStore#insertData(MediumReference, ByteBuffer)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void insertData_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.insertData(at(currentMedium, 10), ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#insertData(MediumReference, ByteBuffer)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void insertData_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.insertData(at(MediaTestUtility.OTHER_MEDIUM, 10), ByteBuffer.allocate(10));
   }

   /**
    * Tests {@link MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void undo_forNonPendingAction_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      MediumAction mediumAction = new MediumAction(MediumActionType.REMOVE, new MediumRegion(at(currentMedium, 10), 20),
         0, null);

      mediumAction.setDone();

      mediumStoreUnderTest.undo(mediumAction);
   }

   /**
    * Tests {@link MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void undo_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest
         .undo(new MediumAction(MediumActionType.REMOVE, new MediumRegion(at(currentMedium, 10), 20), 0, null));
   }

   /**
    * Tests {@link MediumStore#undo(com.github.jmeta.library.media.api.types.MediumAction)}.
    */
   @Test(expected = PreconditionUnfullfilledException.class)
   public void undo_forReferencePointingToWrongMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      mediumStoreUnderTest.undo(new MediumAction(MediumActionType.REMOVE,
         new MediumRegion(at(MediaTestUtility.OTHER_MEDIUM, 10), 20), 0, null));
   }

   /**
    * Tests {@link MediumStore#flush()}.
    */
   @Test(expected = MediumStoreClosedException.class)
   public void flush_forClosedMedium_throwsException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.flush();
   }

   /**
    * Creates an empty {@link Medium} for testing, i.e. with zero bytes.
    * 
    * For writable media, be sure to only return a copy of the original medium such that the original medium is not
    * modified by writing tests and all tests remain repeatable.
    * 
    * @param testMethodName
    *           the name of the current test method, can be used to create a copy of the original medium with the given
    *           name
    * 
    * @return an empty {@link Medium} for testing
    * 
    * @throws IOException
    *            In case of any errors creating the {@link Medium}
    */
   protected abstract T createEmptyMedium(String testMethodName) throws IOException;

   /**
    * Creates a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT} as content, backed or not
    * backed by a cache with the given maximum cache and cache region size as well as the given maximum read write block
    * size. Implementing test classes who's medium type does not support caching must return null.
    * 
    * For writable media, be sure to only return a copy of the original medium such that the original medium is not
    * modified by writing tests and all tests remain repeatable.
    * 
    * @param testMethodName
    *           the name of the current test method, can be used to create a copy of the original medium with the given
    *           name
    * @param maxCacheSize
    *           the maximum cache size in bytes
    * @param maxReadWriteBlockSize
    *           the maximum read write block size in bytes
    * @return a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT} as content with the given
    *         configuration parameters
    * 
    * @throws IOException
    *            In case of any errors creating the {@link Medium}
    */
   protected abstract T createFilledMedium(String testMethodName, long maxCacheSize, int maxReadWriteBlockSize)
      throws IOException;

   /**
    * Returns the current content of the given {@link Medium} as string representation. Implementations should implement
    * a strategy to get this content that is independent of the classes under test. They can distinguish the media by
    * medium name to identify them. This method is used to check the current medium content against the expected medium
    * content.
    * 
    * In test cases, you should only call this method either BEFORE opening the medium (to get the initial content) or
    * AFTER the closing of the medium (to get the changed content), otherwise you might run into exception because the
    * medium is still locked by the {@link MediumStore} and it cannot be accessed.
    * 
    * @return the current content of the filled {@link Medium}
    */
   protected abstract String getMediumContentAsString(T medium);

   /**
    * Creates a test class implementation specific {@link MediumAccessor} to use for testing.
    * 
    * @param mediumToUse
    *           The {@link Medium} to use for the {@link MediumStore}.
    * @return a {@link MediumAccessor} to use based on a given {@link Medium}.
    */
   protected abstract MediumAccessor<T> createMediumAccessor(T mediumToUse);

   /**
    * Creates a {@link MediumStore} to test based on a given {@link Medium}.
    * 
    * @param mediumToUse
    *           The {@link Medium} to use for the {@link MediumStore}.
    * @return a {@link MediumStore} to test based on a given {@link Medium}.
    */
   protected MediumStore createMediumStoreToTest(T mediumToUse) {
      mediumAccessorSpy = Mockito.spy(createMediumAccessor(mediumToUse));

      int maxCacheRegionSize = 0;

      if (mediumToUse.getMaxCacheSizeInBytes() > 0) {
         maxCacheRegionSize = mediumToUse.getMaxReadWriteBlockSizeInBytes();
      }

      mediumCacheSpy = Mockito
         .spy(new MediumCache(mediumToUse, mediumToUse.getMaxCacheSizeInBytes(), maxCacheRegionSize));
      mediumReferenceFactorySpy = Mockito.spy(new MediumReferenceFactory(mediumToUse));
      mediumChangeManagerSpy = Mockito.spy(new MediumChangeManager(mediumReferenceFactorySpy));

      return new StandardMediumStore<>(mediumAccessorSpy, mediumCacheSpy, mediumReferenceFactorySpy,
         mediumChangeManagerSpy);
   }

   /**
    * Creates a {@link MediumStore} based on an empty {@link Medium}.
    * 
    * @return a {@link MediumStore} based on an empty {@link Medium}
    */
   protected MediumStore createEmptyMediumStore() {
      try {
         currentMedium = createEmptyMedium(testName.getMethodName());

         return createMediumStoreToTest(currentMedium);
      } catch (IOException e) {
         throw new InvalidTestDataException("Could not create filled medium due to IO Exception", e);
      }
   }

   /**
    * Creates a default {@link MediumStore} based on a filled {@link Medium}.
    * 
    * @return a {@link MediumStore} based on a filled {@link Medium}
    */
   protected abstract MediumStore createDefaultFilledMediumStore();

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)} by comparing its result with the expected medium content.
    * 
    * @param getDataOffset
    *           The offset to use for the method call
    * @param getDataSize
    *           The size to use for the method call
    * @param currentMediumContent
    *           The current medium content used to get the expected data
    */
   protected void testGetData_returnsExpectedData(MediumReference getDataOffset, int getDataSize,
      String currentMediumContent) {
      ByteBuffer returnedData = getDataNoEOMExpected(getDataOffset, getDataSize);

      assertByteBufferMatchesMediumRange(returnedData, getDataOffset, getDataSize, currentMediumContent);
   }

   /**
    * Checks whether the given {@link ByteBuffer} matches the medium content in the specified range
    * 
    * @param returnedData
    *           The {@link ByteBuffer} to check
    * @param rangeStartOffset
    *           The start offset of the compared range
    * @param rangeSize
    *           The size of the compared range
    * @param currentMediumContent
    *           The current content of the {@link Medium}
    */
   protected void assertByteBufferMatchesMediumRange(ByteBuffer returnedData, MediumReference rangeStartOffset,
      int rangeSize, String currentMediumContent) {
      Assert.assertEquals(rangeSize, returnedData.remaining());
      byte[] byteBufferData = new byte[rangeSize];
      returnedData.asReadOnlyBuffer().get(byteBufferData);

      String asString = new String(byteBufferData, Charsets.CHARSET_ASCII);

      Assert.assertEquals(currentMediumContent.substring((int) rangeStartOffset.getAbsoluteMediumOffset(),
         (int) (rangeStartOffset.getAbsoluteMediumOffset() + rangeSize)), asString);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)} to throw an end of medium exception when reaching it.
    * 
    * @param getDataSize
    *           The size to use for the method call
    * @param chunkSizeToUse
    *           The size of the read-write block chunks
    * @param currentMediumContent
    *           The current content of the medium
    * @param getDataStartOffset
    *           The offset to use for the method call
    */
   protected void testGetData_throwsEndOfMediumException(MediumReference getDataOffset, int getDataSize,
      int chunkSizeToUse, String currentMediumContent) {

      try {
         mediumStoreUnderTest.getData(getDataOffset, getDataSize);
         Assert.fail(EndOfMediumException.class + " expected, but was not thrown");
      } catch (EndOfMediumException e) {
         long getDataStartOffset = getDataOffset.getAbsoluteMediumOffset();

         MediumReference expectedReadOffset = at(currentMedium, getDataStartOffset
            + chunkSizeToUse * (int) ((currentMediumContent.length() - getDataStartOffset) / chunkSizeToUse));

         Assert.assertEquals(expectedReadOffset, e.getReadStartReference());
         Assert.assertEquals(getDataSize < chunkSizeToUse ? getDataSize : chunkSizeToUse, e.getByteCountTriedToRead());
         Assert.assertEquals((int) (currentMediumContent.length() - getDataStartOffset) % chunkSizeToUse,
            e.getByteCountActuallyRead());

         assertByteBufferMatchesMediumRange(e.getBytesReadSoFar(), expectedReadOffset, e.getByteCountActuallyRead(),
            currentMediumContent);
      }
   }

   /**
    * Calls {@link MediumStore#getData(MediumReference, int)} and expects no end of medium.
    * 
    * @param offset
    *           The offset to use
    * @param byteCount
    *           The number of bytes to cache
    */
   protected ByteBuffer getDataNoEOMExpected(MediumReference offset, int byteCount) {
      try {
         return mediumStoreUnderTest.getData(offset, byteCount);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Calls {@link MediumStore#cache(MediumReference, int)} and expects no end of medium.
    * 
    * @param offset
    *           The offset to use
    * @param byteCount
    *           The number of bytes to cache
    */
   protected void cacheNoEOMExpected(MediumReference offset, int byteCount) {
      try {
         mediumStoreUnderTest.cache(offset, byteCount);
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Verifies that there were exactly N calls to {@link MediumAccessor#read(ByteBuffer)} without
    * {@link EndOfMediumException}, no matter which parameters used.
    * 
    * @param N
    *           The number of expected calls
    */
   protected void verifyExactlyNReads(int N) {
      try {
         Mockito.verify(mediumAccessorSpy, Mockito.times(N)).read(Mockito.any());
      } catch (EndOfMediumException e) {
         throw new RuntimeException("Unexpected end of medium", e);
      }
   }

   /**
    * Verifies that the medium cache is currently empty.
    */
   protected void assertCacheIsEmpty() {
      Assert.assertEquals(0, mediumCacheSpy.getAllCachedRegions().size());
      Assert.assertEquals(0, mediumCacheSpy.calculateCurrentCacheSizeInBytes());
   }

}
