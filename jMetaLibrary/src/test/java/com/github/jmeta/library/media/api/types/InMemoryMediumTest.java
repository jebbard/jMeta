/**
 *
 * {@link InMemoryMediumTest}.java
 *
 * @author Jens
 *
 * @date 27.05.2015
 *
 */
package com.github.jmeta.library.media.api.types;

import java.nio.ByteBuffer;

/**
 * {@link InMemoryMediumTest} tests the {@link InMemoryMedium} class.
 */
public class InMemoryMediumTest extends AbstractMediumTest<ByteBuffer> {

	private static final byte[] WRAPPED_MEDIUM = new byte[] { 1, 2, 3, 4, 5, 6 };

	private static final String EXTERNAL_NAME = "my name";

	private static final boolean READ_ONLY = true;

	/**
	 * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getExpectedExternalName()
	 */
	@Override
	protected String getExpectedExternalName() {

		return InMemoryMediumTest.EXTERNAL_NAME;
	}

	/**
	 * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getExpectedLength()
	 */
	@Override
	protected long getExpectedLength() {

		return InMemoryMediumTest.WRAPPED_MEDIUM.length;
	}

	/**
	 * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getExpectedWrappedMedium()
	 */
	@Override
	protected ByteBuffer getExpectedWrappedMedium() {

		return ByteBuffer.wrap(InMemoryMediumTest.WRAPPED_MEDIUM);
	}

	/**
	 * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#getMediumToTest()
	 */
	@Override
	protected Medium<ByteBuffer> getMediumToTest() {

		return new InMemoryMedium(InMemoryMediumTest.WRAPPED_MEDIUM, InMemoryMediumTest.EXTERNAL_NAME,
			InMemoryMediumTest.READ_ONLY);
	}

	/**
	 * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#isExpectedAsExisting()
	 */
	@Override
	protected boolean isExpectedAsExisting() {

		return true;
	}

	/**
	 * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#isExpectedAsRandomAccess()
	 */
	@Override
	protected boolean isExpectedAsRandomAccess() {

		return true;
	}

	/**
	 * @see com.github.jmeta.library.media.api.types.AbstractMediumTest#isExpectedAsReadOnly()
	 */
	@Override
	protected boolean isExpectedAsReadOnly() {

		return InMemoryMediumTest.READ_ONLY;
	}

}
