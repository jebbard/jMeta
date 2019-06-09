/**
 *
 * {@link ByteBufferUtilsTest}.java
 *
 * @author Jens Ebert
 *
 * @date 05.03.2018
 *
 */
package com.github.jmeta.utility.byteutils.api.services;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

/**
 * {@link ByteBufferUtilsTest} tests the {@link ByteBufferUtils} class.
 */
public class ByteBufferUtilsTest {

	private final static byte[] TEST_BYTES = { 0, 1, 2, 3, 4, 5, 7, 8, 9 };

	/**
	 * Tests
	 * {@link com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils#asByteArrayCopy(java.nio.ByteBuffer)}.
	 */
	@Test
	public void asByteArrayCopy_forBufferRemainingEqualToCapacity_returnsAllBytes() {
		ByteBuffer testBB = ByteBuffer.wrap(ByteBufferUtilsTest.TEST_BYTES);

		byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(testBB);

		Assert.assertArrayEquals(ByteBufferUtilsTest.TEST_BYTES, copiedBytes);
	}

	/**
	 * Tests
	 * {@link com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils#asByteArrayCopy(java.nio.ByteBuffer)}.
	 */
	@Test
	public void asByteArrayCopy_forBufferWithNonZeroPosition_returnsBytesBetweenPositionAndLimit() {
		ByteBuffer testBB = ByteBuffer.wrap(ByteBufferUtilsTest.TEST_BYTES);
		testBB.position(3);
		testBB.limit(5);

		byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(testBB);

		Assert.assertArrayEquals(new byte[] { 3, 4 }, copiedBytes);
	}

	/**
	 * Tests
	 * {@link com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils#asByteArrayCopy(java.nio.ByteBuffer)}.
	 */
	@Test
	public void asByteArrayCopy_forBufferWithNonZeroPositionNonZeroOffset_returnsSizeBytesAtPositionPlusOffset() {
		ByteBuffer testBB = ByteBuffer.wrap(ByteBufferUtilsTest.TEST_BYTES);
		testBB.position(1);
		testBB.limit(7);

		byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(testBB, 2, 3);

		Assert.assertArrayEquals(new byte[] { 3, 4, 5 }, copiedBytes);
	}

	/**
	 * Tests
	 * {@link com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils#asByteArrayCopy(java.nio.ByteBuffer)}.
	 */
	@Test
	public void asByteArrayCopy_forReadOnlyByteBuffer_returnsCopyOnly() {

		ByteBuffer readOnlyBB = ByteBuffer.wrap(ByteBufferUtilsTest.TEST_BYTES).asReadOnlyBuffer();

		byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(readOnlyBB);

		Assert.assertFalse(ByteBufferUtilsTest.TEST_BYTES == copiedBytes);
	}

	/**
	 * Tests
	 * {@link com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils#asByteArrayCopy(java.nio.ByteBuffer)}.
	 */
	@Test
	public void asByteArrayCopy_forWritableByteBuffer_returnsCopyOnly() {

		ByteBuffer testBB = ByteBuffer.wrap(ByteBufferUtilsTest.TEST_BYTES);

		byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(testBB);

		Assert.assertFalse(ByteBufferUtilsTest.TEST_BYTES == copiedBytes);
	}

	/**
	 * Tests
	 * {@link com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils#asByteArrayCopy(java.nio.ByteBuffer)}.
	 */
	@Test
	public void asByteArrayCopy_forZeroRemainingBytes_returnsEmptyArray() {
		ByteBuffer testBB = ByteBuffer.allocate(0);

		byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(testBB);

		Assert.assertArrayEquals(new byte[] {}, copiedBytes);

		testBB = ByteBuffer.wrap(ByteBufferUtilsTest.TEST_BYTES);
		testBB.position(3);
		testBB.limit(3);

		copiedBytes = ByteBufferUtils.asByteArrayCopy(testBB);

		Assert.assertArrayEquals(new byte[] {}, copiedBytes);
	}

	/**
	 * Tests
	 * {@link com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils#asByteArrayCopy(ByteBuffer, int, int)}.
	 */
	@Test
	public void asByteArrayCopySlice_forReadOnlyByteBuffer_returnsCopyOnly() {

		ByteBuffer readOnlyBB = ByteBuffer.wrap(ByteBufferUtilsTest.TEST_BYTES).asReadOnlyBuffer();

		byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(readOnlyBB, 0, 1);

		Assert.assertFalse(ByteBufferUtilsTest.TEST_BYTES == copiedBytes);
	}

	/**
	 * Tests
	 * {@link com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils#asByteArrayCopy(ByteBuffer, int, int)}.
	 */
	@Test
	public void asByteArrayCopySlice_forWritableByteBuffer_returnsCopyOnly() {

		ByteBuffer testBB = ByteBuffer.wrap(ByteBufferUtilsTest.TEST_BYTES);

		byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(testBB, 0, 4);

		Assert.assertFalse(ByteBufferUtilsTest.TEST_BYTES == copiedBytes);
	}

	/**
	 * Tests
	 * {@link com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils#asByteArrayCopy(ByteBuffer, int, int)}.
	 */
	@Test
	public void asByteArrayCopySlice_forZeroRemainingBytes_returnsEmptyArray() {
		ByteBuffer testBB = ByteBuffer.allocate(0);

		byte[] copiedBytes = ByteBufferUtils.asByteArrayCopy(testBB, 0, 0);

		Assert.assertArrayEquals(new byte[] {}, copiedBytes);

		testBB = ByteBuffer.wrap(ByteBufferUtilsTest.TEST_BYTES);
		testBB.position(3);
		testBB.limit(3);

		copiedBytes = ByteBufferUtils.asByteArrayCopy(testBB, 0, 0);

		Assert.assertArrayEquals(new byte[] {}, copiedBytes);
	}
}
