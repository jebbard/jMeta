/**
 *
 * {@link ReadOnlyInMemoryMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.impl.store;

import java.io.IOException;

import com.github.jmeta.library.media.api.helper.MediaTestUtility;
import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.services.AbstractWritableRandomAccessMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.impl.mediumAccessor.InMemoryMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.utility.charset.api.services.Charsets;

/**
 * {@link WritableInMemoryMediumStoreTest} tests a {@link MediumStore} backed by
 * {@link InMemoryMedium} instances, which is - by definition - without an
 * additional cache.
 */
public class WritableInMemoryMediumStoreTest extends AbstractWritableRandomAccessMediumStoreTest<InMemoryMedium> {

	/**
	 * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
	 */
	@Override
	protected InMemoryMedium createEmptyMedium(String testMethodName) throws IOException {
		return new InMemoryMedium(new byte[0], "Stream based empty medium", false);
	}

	/**
	 * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createFilledMedium(java.lang.String,
	 *      long, int)
	 */
	@Override
	protected InMemoryMedium createFilledMedium(String testMethodName, long maxCacheSize, int maxReadWriteBlockSize)
		throws IOException {
		return new InMemoryMedium(MediaTestUtility.readFileContent(TestMedia.FIRST_TEST_FILE_PATH),
			"Stream based filled medium", false, maxCacheSize, maxReadWriteBlockSize);
	}

	/**
	 * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createMediumAccessor(com.github.jmeta.library.media.api.types.Medium)
	 */
	@Override
	protected MediumAccessor<InMemoryMedium> createMediumAccessor(InMemoryMedium mediumToUse) {
		return new InMemoryMediumAccessor(mediumToUse);
	}

	/**
	 * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#getMediumContentAsString(com.github.jmeta.library.media.api.types.Medium)
	 */
	@Override
	protected String getMediumContentAsString(InMemoryMedium medium) {
		return new String(medium.getWrappedMedium().array(), Charsets.CHARSET_UTF8);
	}
}
