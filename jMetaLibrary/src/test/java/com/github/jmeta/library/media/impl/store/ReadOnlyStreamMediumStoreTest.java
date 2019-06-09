/**
 *
 * {@link ReadOnlyStreamMediumStoreTest}.java
 *
 * @author Jens Ebert
 *
 * @date 31.10.2017
 *
 */
package com.github.jmeta.library.media.impl.store;

import java.io.FileInputStream;
import java.io.IOException;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.services.AbstractReadOnlyMediumStoreTest;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.impl.mediumAccessor.InputStreamMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;

/**
 * {@link ReadOnlyStreamMediumStoreTest} tests a {@link MediumStore} backed by
 * {@link InputStreamMedium} instances.
 */
public class ReadOnlyStreamMediumStoreTest extends AbstractReadOnlyMediumStoreTest<InputStreamMedium> {

	private static final String STREAM_BASED_MEDIUM_NAME = "Stream based medium";

	/**
	 * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createEmptyMedium(java.lang.String)
	 */
	@Override
	protected InputStreamMedium createMedium() throws IOException {
		return new InputStreamMedium(new FileInputStream(TestMedia.FIRST_TEST_FILE_PATH.toFile()),
			ReadOnlyStreamMediumStoreTest.STREAM_BASED_MEDIUM_NAME);
	}

	/**
	 * @see com.github.jmeta.library.media.api.services.AbstractMediumStoreTest#createMediumAccessor(com.github.jmeta.library.media.api.types.Medium)
	 */
	@Override
	protected MediumAccessor<InputStreamMedium> createMediumAccessor(InputStreamMedium mediumToUse) {
		return new InputStreamMediumAccessor(mediumToUse);
	}

}
