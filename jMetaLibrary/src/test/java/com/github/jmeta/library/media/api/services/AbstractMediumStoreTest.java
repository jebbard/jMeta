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
import com.github.jmeta.library.media.api.helper.MediaTestFiles;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumReference;
import com.github.jmeta.library.media.impl.cache.MediumCache;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.reference.MediumReferenceFactory;
import com.github.jmeta.library.media.impl.store.StandardMediumStore;
import com.github.jmeta.utility.charset.api.services.Charsets;
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
 * <li>{@link AbstractCachedMediumStoreTest}: Tests based on a cached medium; most of the "normal" read test cases go
 * here, no write test cases and no parameter negative tests</li>
 * <li>{@link AbstractCachedAndWritableRandomAccessMediumStoreTest}: Tests based on a cached, writable and thus
 * random-access medium; additional "normal" write test cases go here (these are NOT repeated in
 * {@link AbstractUnCachedAndWritableRandomAccessMediumStoreTest}!), no negative write test cases and no parameter
 * negative tests</li>
 * <li>{@link AbstractUnCachedMediumStoreTest}: Tests based on an un-cached medium; only special read test cases
 * specifically designed for an un-cached medium go here, no write test cases; in addition, all parameter negative tests
 * for read methods go here</li>
 * <li>{@link AbstractUnCachedAndWritableRandomAccessMediumStoreTest}: Tests based on an un-cached, writable and thus
 * random-access medium; only special write test cases specifically designed for an un-cached medium go here; in
 * addition, all parameter negative tests for write methods go here</li>
 * </ul>
 * 
 * The reason for keeping negative tests only in the "Un-cached" versions of the class hierarchy is that thus, we also
 * test the {@link InMemoryMedium} that is by definition un-cached. Furthermore, there is no benefit in repeating tests
 * for the cached variants, as the behaviour is the same.
 * 
 * The filled media used for testing all must contain {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT}, a String fully
 * containing only human-readable standard ASCII characters. This guarantees that 1 bytes = 1 character. Furthermore,
 * all bytes inserted must also be standard human-readable ASCII characters with this property.
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
    * Tests {@link MediumStore#getData(MediumReference, int)}.
    */
   @Test
   public void getData_forEmptyMedium_untilEOM_throwsEndOfMediumException() {
      mediumStoreUnderTest = createEmptyMediumStore();

      mediumStoreUnderTest.open();

      long getDataStartOffset = 0;
      int getDataSize = 1;

      testGetData_throwsEndOfMediumException(getDataStartOffset, getDataSize);
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
    * @param enableCaching
    *           true to enable, false to disable caching
    * @param maxCacheSize
    *           the maximum cache size in bytes
    * @param maxCacheRegionSize
    *           the maximum cache region size in bytes
    * @param maxReadWriteBlockSize
    *           the maximum read write block size in bytes
    * 
    * @return a {@link Medium} containing {@link MediaTestFiles#FIRST_TEST_FILE_CONTENT} as content with the given
    *         configuration parameters
    * 
    * @throws IOException
    *            In case of any errors creating the {@link Medium}
    */
   protected abstract T createFilledMedium(String testMethodName, boolean enableCaching, long maxCacheSize,
      int maxCacheRegionSize, int maxReadWriteBlockSize) throws IOException;

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
      mediumCacheSpy = Mockito.spy(new MediumCache(mediumToUse, mediumToUse.getMaxCacheSizeInBytes(),
         mediumToUse.getMaxCacheRegionSizeInBytes()));
      mediumReferenceFactorySpy = Mockito.spy(new MediumReferenceFactory(mediumToUse));

      return new StandardMediumStore<>(mediumAccessorSpy, mediumCacheSpy, mediumReferenceFactorySpy);
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
    * Tests {@link MediumStore#getData(MediumReference, int)} by comparing its result with the expected medium content.
    * 
    * @param offset
    *           The offset to use for the method call
    * @param readDataSize
    *           The size to use for the method call
    * @param currentMediumContent
    *           The current medium content used to get the expected data
    */
   protected void testGetData_returnsExpectedData(MediumReference offset, int readDataSize,
      String currentMediumContent) {
      ByteBuffer returnedData = getDataNoEOMExpected(offset, readDataSize);

      Assert.assertEquals(readDataSize, returnedData.remaining());
      byte[] byteBufferData = new byte[readDataSize];
      returnedData.get(byteBufferData);

      String asString = new String(byteBufferData, Charsets.CHARSET_ASCII);

      Assert.assertEquals(currentMediumContent.substring((int) offset.getAbsoluteMediumOffset(),
         (int) (offset.getAbsoluteMediumOffset() + readDataSize)), asString);
   }

   /**
    * Tests {@link MediumStore#getData(MediumReference, int)} to throw an end of medium exception when reaching it.
    * 
    * @param offset
    *           The offset to use for the method call
    * @param readDataSize
    *           The size to use for the method call
    */
   protected void testGetData_throwsEndOfMediumException(long getDataStartOffset, int getDataSize) {
      MediumReference getDataOffset = at(currentMedium, getDataStartOffset);

      try {
         mediumStoreUnderTest.getData(getDataOffset, getDataSize);
         Assert.fail(EndOfMediumException.class + "expected");
      } catch (EndOfMediumException e) {
         Assert.assertEquals(getDataOffset, e.getMediumReference());
         Assert.assertEquals(getDataSize, e.getByteCountTriedToRead());
         Assert.assertEquals((int) (getDataSize - getDataStartOffset), e.getBytesReallyRead());
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

}
