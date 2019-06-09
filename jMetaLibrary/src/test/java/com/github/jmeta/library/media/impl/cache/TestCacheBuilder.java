package com.github.jmeta.library.media.impl.cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.byteutils.api.services.ByteBufferUtils;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * This helper class helps to easily define the structure and contents of a
 * {@link MediumCache} instance declaratively. It removes the burden of creating
 * {@link MediumRegion}s for the correct medium with the correct references from
 * the test automater by just working with plain offsets. It furthermore
 * accumulates the (expected) total cache size and provides the helper method
 * {@link #getAllCachedRegionsWithGaps()} for testing
 * {@link MediumCache#getRegionsInRange(com.github.jmeta.library.media.api.types.MediumOffset, int)}.
 * You can build the {@link MediumCache} instance for testing from the
 * {@link TestCacheBuilder} using the method {@link #buildCache(long, int)}.
 *
 * Instances of this class are also used to defined the <i>expected</i>
 * structure of a {@link MediumCache} when testing
 * {@link MediumCache#addRegion(MediumRegion)}. For this, it provides copy
 * mechanisms from an existing {@link TestCacheBuilder} and incremental
 * manipulation methods.
 *
 */
class TestCacheBuilder {

	/**
	 * Represents all necessary about a potential cached {@link MediumRegion} to
	 * create by the {@link TestCacheBuilder}.
	 */
	public static class TestCacheRegionInfo {

		/**
		 * Converts a {@link MediumRegion} to a new {@link TestCacheRegionInfo}.
		 * 
		 * @param region The {@link MediumRegion} to convert
		 * @return the {@link TestCacheRegionInfo} converted from the
		 *         {@link MediumRegion}
		 */
		public static TestCacheRegionInfo fromMediumRegion(MediumRegion region) {
			byte[] regionBytes = ByteBufferUtils.asByteArrayCopy(region.getBytes());

			TestCacheRegionInfo testCacheRegionInfoToAdd = new TestCacheRegionInfo(
				region.getStartOffset().getAbsoluteMediumOffset(), regionBytes);
			return testCacheRegionInfoToAdd;
		}

		private final long regionStartOffset;

		private final byte[] regionBytes;

		/**
		 * Creates a new instance using the given region bytes.
		 * 
		 * @param regionStartOffset The start offset to use. Must be positive or zero.
		 * @param regionBytes       The region's bytes to use
		 */
		public TestCacheRegionInfo(long regionStartOffset, byte[] regionBytes) {
			Reject.ifNegative(regionStartOffset, "regionStartOffset");
			Reject.ifNull(regionBytes, "regionBytes");

			this.regionStartOffset = regionStartOffset;
			this.regionBytes = regionBytes;
		}

		/**
		 * Converts this {@link TestCacheRegionInfo} to a cached {@link MediumRegion}.
		 * 
		 * @param medium The {@link Medium} to use.
		 * @return this {@link TestCacheRegionInfo} converted to a cached
		 *         {@link MediumRegion}
		 */
		public MediumRegion asMediumRegion(Medium<?> medium) {
			long offset = getRegionStartOffset();
			return new MediumRegion(TestMedia.at(medium, offset),
				ByteBuffer.wrap(TestCacheBuilder.regionBytesFromDistinctOffsetSequence(offset, getRegionSize())));
		}

		/**
		 * @return the region's bytes
		 */
		public byte[] getRegionBytes() {
			return regionBytes;
		}

		/**
		 * @return the region's end offset
		 */
		public long getRegionEndOffset() {
			return regionStartOffset + getRegionSize();
		}

		/**
		 * @return the region's size
		 */
		public int getRegionSize() {
			return regionBytes.length;
		}

		/**
		 * @return the region's start offset
		 */
		public long getRegionStartOffset() {
			return regionStartOffset;
		}
	}

	/**
	 * Creates a byte array with distinct bytes, starting at offset %
	 * Byte.MAX_VALUE, always increasing by 1 until wrapped around at Byte.MAX_VALUE
	 * again and so on, until the given size is reached.
	 * 
	 * Is used to ensure a byte array with distinctive content is created, and the
	 * content is also depending on the start offset.
	 * 
	 * @param offset The offset to use
	 * @param size   The size of the returned array
	 * @return a byte array with distinct bytes
	 */
	static byte[] regionBytesFromDistinctOffsetSequence(long offset, int size) {
		byte[] content = new byte[size];

		for (int i = 0; i < content.length; i++) {
			content[i] = (byte) ((offset + i) % Byte.MAX_VALUE);
		}

		return content;
	}

	/**
	 * Creates bytes to be used as content of a {@link MediumRegion} of the given
	 * size filled all over with the same fill byte.
	 * 
	 * @param fillByte The fill byte to use
	 * @param size     The size of the returned array
	 * @return byte array filled all over with the same fill byte
	 */
	private static byte[] regionBytesFromFillByte(byte fillByte, int size) {
		byte[] content = new byte[size];

		Arrays.fill(content, fillByte);

		return content;
	}

	private final List<TestCacheRegionInfo> regionInfos;

	private final Medium<?> regionMedium;

	/**
	 * Creates a new empty {@link TestCacheBuilder} for the given {@link Medium}.
	 * After this, it can be populated using {@link #appendRegionInfo(long, long)}.
	 * 
	 * @param regionMedium The {@link Medium} to use.
	 */
	public TestCacheBuilder(Medium<?> regionMedium) {
		Reject.ifNull(regionMedium, "regionMedium");

		regionInfos = new ArrayList<>();
		this.regionMedium = regionMedium;
	}

	/**
	 * Creates a new {@link TestCacheBuilder} containing the copied region info data
	 * of the given other {@link TestCacheBuilder}.
	 * 
	 * @param builderToCopy The {@link TestCacheBuilder} to copy.
	 */
	public TestCacheBuilder(TestCacheBuilder builderToCopy) {
		Reject.ifNull(builderToCopy, "builderToCopy");

		regionMedium = builderToCopy.regionMedium;
		regionInfos = new ArrayList<>(builderToCopy.regionInfos);
	}

	/**
	 * Appends multiple new {@link TestCacheRegionInfo}s filled with the given
	 * default fill byte starting at the given start offset. The number and size of
	 * {@link TestCacheRegionInfo}s to add is determined by totalSize / maxSize. If
	 * there is a remainder, also a {@link TestCacheRegionInfo} for the remainder is
	 * added.
	 * 
	 * This method is incremental in a sense that you have to build the cache step
	 * by step from lower offset regions to higher offset regions. You cannot add a
	 * region with offset 10 after you have already added a region with offset 1000.
	 * 
	 * @param startOffset The start offset to use, must be bigger or equal to the
	 *                    last added region end offset.
	 * @param totalSize   The total size of the {@link TestCacheRegionInfo}s to add,
	 *                    must be bigger than the maximum size
	 * @param maxSize     The maximum size of each {@link TestCacheRegionInfo} to
	 *                    add
	 * @param fillByte    The fill byte to use
	 */
	public void appendMultipleRegionInfos(long startOffset, int totalSize, int maxSize, byte fillByte) {
		Reject.ifNegativeOrZero(totalSize, "totalSize");
		Reject.ifFalse(maxSize <= totalSize, "maxSize <= totalSize");

		int times = totalSize / maxSize;
		int remainder = totalSize % maxSize;

		for (int i = 0; i < times; i++) {
			appendRegionInfo(
				new TestCacheRegionInfo(startOffset, TestCacheBuilder.regionBytesFromFillByte(fillByte, maxSize)));

			startOffset += maxSize;
		}

		if (remainder > 0) {
			appendRegionInfo(
				new TestCacheRegionInfo(startOffset, TestCacheBuilder.regionBytesFromFillByte(fillByte, remainder)));
		}
	}

	/**
	 * Appends the necessary information for a new cached {@link MediumRegion} to be
	 * contained in the {@link MediumCache} to build. You specify the region based
	 * on its start and end offset. Where are the region bytes? You cannot pass them
	 * here, but they are automatically generated in a reproducible way. The byte at
	 * offset x has the value "x MOD Byte.MAX_VALUE". This way, on the one hand you
	 * can detect any problems with wrong bytes in the cache (as most bytes should
	 * differ from each other), on the other hand, you are not surprised which byte
	 * is where.
	 * 
	 * This method is incremental in a sense that you have to build the cache step
	 * by step from lower offset regions to higher offset regions. You cannot add a
	 * region with offset 10 after you have already added a region with offset 1000.
	 * 
	 * @param regionStartOffset The start offset to use, must be smaller than the
	 *                          end offset, must be bigger or equal to the last
	 *                          added region end offset.
	 * @param regionEndOffset   The end offset to use, must be bigger than the start
	 *                          offset
	 */
	public void appendRegionInfo(long regionStartOffset, long regionEndOffset) {
		int regionSize = (int) (regionEndOffset - regionStartOffset);

		appendRegionInfo(new TestCacheRegionInfo(regionStartOffset,
			TestCacheBuilder.regionBytesFromDistinctOffsetSequence(regionEndOffset, regionSize)));
	}

	/**
	 * Appends a new {@link TestCacheRegionInfo} based on the given
	 * {@link MediumRegion}.
	 * 
	 * This method is incremental in a sense that you have to build the cache step
	 * by step from lower offset regions to higher offset regions. You cannot add a
	 * region with offset 10 after you have already added a region with offset 1000.
	 * 
	 * @param region The {@link MediumRegion} to add.
	 */
	public void appendRegionInfo(MediumRegion region) {
		Reject.ifNull(region, "region");

		appendRegionInfo(new TestCacheRegionInfo(region.getStartOffset().getAbsoluteMediumOffset(),
			ByteBufferUtils.asByteArrayCopy(region.getBytes())));
	}

	/**
	 * Appends the given {@link TestCacheRegionInfo} to the internal data structures
	 * after validating it.
	 * 
	 * @param testCacheRegionInfoToAdd the {@link TestCacheRegionInfo} to add. Must
	 *                                 not overlap the last existing
	 *                                 {@link TestCacheRegionInfo}.
	 */
	private void appendRegionInfo(TestCacheRegionInfo testCacheRegionInfoToAdd) {
		if (!regionInfos.isEmpty()) {
			long lastAddedRegionInfoEndOffset = regionInfos.get(regionInfos.size() - 1).getRegionEndOffset();

			Reject.ifFalse(testCacheRegionInfoToAdd.getRegionStartOffset() >= lastAddedRegionInfoEndOffset,
				"testCacheRegionInfoToAdd.getRegionStartOffset() >= lastAddedRegionEndOffset");
		}

		regionInfos.add(testCacheRegionInfoToAdd);
	}

	/**
	 * Builds a {@link MediumCache} instance from this {@link TestCacheBuilder} for
	 * testing, using all {@link MediumRegion} information currently contained in
	 * it. It does not do anything special to ensure that the regions are below the
	 * given maximum region or maximum cache size. This has to be ensured manually
	 * by the test automater.
	 * 
	 * @param maxCacheSize       The maximum cache size to use.
	 * @param maxCacheRegionSize The maximum region size to use.
	 * @return The built {@link MediumCache} for testing.
	 */
	public MediumCache buildCache(long maxCacheSize, int maxCacheRegionSize) {
		List<MediumRegion> regionList = getAllCachedRegions();

		MediumCache resultingCache = new MediumCache(regionMedium, maxCacheSize, maxCacheRegionSize);

		for (MediumRegion mediumRegion : regionList) {
			resultingCache.addRegion(mediumRegion);
		}

		return resultingCache;
	}

	/**
	 * Builds the list of all {@link MediumRegion}s in this {@link TestCacheBuilder}
	 * with or without uncached gap {@link MediumRegion}s.
	 * 
	 * @param withGaps true if uncached gap {@link MediumRegion}s must also be
	 *                 contained, false for only cached {@link MediumRegion}s.
	 * @return the list of all {@link MediumRegion}s in this
	 *         {@link TestCacheBuilder} with or without uncached gap
	 *         {@link MediumRegion}s, in offset order.
	 */
	private List<MediumRegion> buildRegionList(boolean withGaps) {
		List<MediumRegion> resultList = new ArrayList<>(regionInfos.size());

		Long lastRegionEndOffset = null;

		for (TestCacheRegionInfo testCacheRegionInfo : regionInfos) {
			Long currentRegionStartOffset = testCacheRegionInfo.getRegionStartOffset();
			Long currentRegionEndOffset = testCacheRegionInfo.getRegionEndOffset();

			// Also build a non-cached gap region
			if (withGaps && (lastRegionEndOffset != null) && (currentRegionStartOffset > lastRegionEndOffset)) {
				resultList.add(new MediumRegion(TestMedia.at(regionMedium, lastRegionEndOffset),
					(int) (currentRegionStartOffset - lastRegionEndOffset)));
			}

			resultList.add(testCacheRegionInfo.asMediumRegion(regionMedium));

			lastRegionEndOffset = currentRegionEndOffset;
		}

		return resultList;
	}

	/**
	 * Performs a clipping of all currently contained {@link TestCacheRegionInfo}s
	 * against the given clip {@link MediumRegion}. Clipping means the following:
	 * <ul>
	 * <li>If an existing {@link TestCacheRegionInfo} is fully contained inside the
	 * clip {@link MediumRegion}, it is removed. This includes the case where the
	 * clip {@link MediumRegion} covers the same range as the
	 * {@link TestCacheRegionInfo}.</li>
	 * <li>If an existing {@link TestCacheRegionInfo} is overlapped at its front by
	 * the clip {@link MediumRegion}, it is clipped at its front
	 * correspondingly.</li>
	 * <li>If an existing {@link TestCacheRegionInfo} is overlapped at its back by
	 * the clip {@link MediumRegion}, it is clipped at its back
	 * correspondingly.</li>
	 * <li>If an existing {@link TestCacheRegionInfo} fully contains the
	 * {@link MediumRegion}, it is broken apart into two parts and its middle part
	 * covered by the clip {@link MediumRegion} is removed.</li>
	 * <li>If there is no overlap, the existing {@link TestCacheRegionInfo} is left
	 * untouched.</li>
	 * </ul>
	 * 
	 * Note that this method does not add any new {@link TestCacheRegionInfo} for
	 * the clip {@link MediumRegion}. This must be done after the clipping by the
	 * test code itself, e.g. by using {@link #appendRegionInfo(MediumRegion)} or
	 * {@link #appendMultipleRegionInfos(long, int, int, byte)}.
	 * 
	 * @param clipRegion The {@link MediumRegion} to use for clipping
	 */
	public void clipAllRegionsAgainstMediumRegion(MediumRegion clipRegion) {
		Reject.ifNull(clipRegion, "clipRegion");

		List<TestCacheRegionInfo> survivedClippingList = new ArrayList<>();

		regionInfos.stream()
			.forEach(regionInfo -> clipRegionInfoAgainstMediumRegion(regionInfo, clipRegion, survivedClippingList));

		regionInfos.clear();
		regionInfos.addAll(survivedClippingList);
	}

	/**
	 * Performs the clipping of the given {@link TestCacheRegionInfo} against the
	 * given clip {@link MediumRegion}. See
	 * {@link #clipAllRegionsAgainstMediumRegion(MediumRegion)} for more details.
	 * 
	 * @param testCacheRegionInfo  The {@link TestCacheRegionInfo} to clip.
	 * @param clipRegion           The clip {@link MediumRegion}.
	 * @param survivedClippingList This list is updated wit all
	 *                             {@link TestCacheRegionInfo} that survived the
	 *                             clipping, this includes also
	 *                             {@link TestCacheRegionInfo}s broken into two
	 *                             parts. Note that no new
	 *                             {@link TestCacheRegionInfo} is added for the clip
	 *                             {@link MediumRegion}.
	 */
	private void clipRegionInfoAgainstMediumRegion(TestCacheRegionInfo testCacheRegionInfo, MediumRegion clipRegion,
		List<TestCacheRegionInfo> survivedClippingList) {

		MediumRegion regionInfoAsRegion = testCacheRegionInfo.asMediumRegion(regionMedium);

		int overlappingByteCount = regionInfoAsRegion.getOverlappingByteCount(clipRegion);

		if (overlappingByteCount == 0) {
			survivedClippingList.add(testCacheRegionInfo);
		} else if (overlappingByteCount == regionInfoAsRegion.getSize()) {
			// Ignore the region, it does not survive as it is fully covered by the clip
			// region
		} else if (overlappingByteCount == clipRegion.getSize()) {
			// Need to divide the region info as the clip region is fully enclosed in it
			int keepBytesAtStart = (int) clipRegion.getStartOffset().distanceTo(regionInfoAsRegion.getStartOffset());
			int keepBytesAtEnd = (int) regionInfoAsRegion.calculateEndOffset()
				.distanceTo(clipRegion.calculateEndOffset());

			TestCacheRegionInfo trimmedRegionInfo = trimRegionInfo(testCacheRegionInfo,
				testCacheRegionInfo.getRegionSize() - keepBytesAtStart, 0);
			survivedClippingList.add(trimmedRegionInfo);
			int bytesToRemoveFromStart = testCacheRegionInfo.getRegionSize() - keepBytesAtEnd;
			trimmedRegionInfo = trimRegionInfo(testCacheRegionInfo, bytesToRemoveFromStart, bytesToRemoveFromStart);
			survivedClippingList.add(trimmedRegionInfo);
		} else if (clipRegion.overlapsOtherRegionAtBack(regionInfoAsRegion)) {
			TestCacheRegionInfo trimmedRegionInfo = trimRegionInfo(testCacheRegionInfo, overlappingByteCount, 0);
			survivedClippingList.add(trimmedRegionInfo);
		} else if (clipRegion.overlapsOtherRegionAtFront(regionInfoAsRegion)) {
			TestCacheRegionInfo trimmedRegionInfo = trimRegionInfo(testCacheRegionInfo, overlappingByteCount,
				overlappingByteCount);
			survivedClippingList.add(trimmedRegionInfo);
		}
	}

	/**
	 * @return all cached {@link MediumRegion}s currently defined in this
	 *         {@link TestCacheBuilder} in offset order.
	 */
	public List<MediumRegion> getAllCachedRegions() {
		return buildRegionList(false);
	}

	/**
	 * @return all cached {@link MediumRegion}s currently defined in this
	 *         {@link TestCacheBuilder} plus any uncached gap {@link MediumRegion}s
	 *         between them, in offset order.
	 */
	public List<MediumRegion> getAllCachedRegionsWithGaps() {
		return buildRegionList(true);
	}

	/**
	 * @return 0L if this {@link TestCacheBuilder} is still empty, otherwise the end
	 *         offset of the last region in the layout.
	 */
	public long getEndOffset() {
		return regionInfos.isEmpty() ? 0L : regionInfos.get(regionInfos.size() - 1).getRegionEndOffset();
	}

	/**
	 * @return 0L if this {@link TestCacheBuilder} is still empty, otherwise the
	 *         start offset of the first region in the layout.
	 */
	public long getStartOffset() {
		return regionInfos.isEmpty() ? 0L : regionInfos.get(0).getRegionStartOffset();
	}

	/**
	 * @return the accumulated total region size of this {@link TestCacheBuilder}.
	 */
	public long getTotalRegionSizeInBytes() {
		return regionInfos.stream().collect(Collectors.summingLong(regionInfo -> regionInfo.getRegionSize()));
	}

	/**
	 * Inserts a {@link TestCacheRegionInfo} based on an existing
	 * {@link MediumRegion} to this {@link TestCacheBuilder} before the given index.
	 * Basically it takes the offset, size and byte information from the existing
	 * {@link MediumRegion} to create the new {@link TestCacheRegionInfo}. This will
	 * lead to the equal {@link MediumRegion} created later by
	 * {@link #buildCache(long, int)}.
	 * 
	 * @param regionIndex Identifies the region before which to insert the hew
	 *                    region information. Must be a valid index
	 * @param region      The {@link MediumRegion} to for which to add a
	 *                    {@link TestCacheRegionInfo}. The {@link MediumRegion} must
	 *                    neither overlap the previous {@link TestCacheRegionInfo}
	 *                    before the given index nor the one after it (if any).
	 */
	public void insertRegionInfoBefore(int regionIndex, MediumRegion region) {
		Reject.ifNull(region, "region");
		validateRegionIndex(regionIndex);

		TestCacheRegionInfo testCacheRegionInfoToInsert = TestCacheRegionInfo.fromMediumRegion(region);

		if (!regionInfos.isEmpty()) {
			long regionAtIndexStartOffset = regionInfos.get(regionIndex).getRegionStartOffset();

			Reject.ifFalse(testCacheRegionInfoToInsert.getRegionEndOffset() <= regionAtIndexStartOffset,
				"testCacheRegionInfoToInsert.getRegionEndOffset() <= regionAtIndexStartOffset");
		}

		regionInfos.add(regionIndex, testCacheRegionInfoToInsert);
	}

	/**
	 * Removes the {@link TestCacheRegionInfo} at the given index
	 * 
	 * @param regionIndex Identifies the {@link TestCacheRegionInfo} to remove. Must
	 *                    be a valid index
	 */
	public void removeRegionInfo(int regionIndex) {
		validateRegionIndex(regionIndex);

		regionInfos.remove(regionIndex);
	}

	/**
	 * Performs the actual trimming of a region info.
	 * 
	 * @param originalRegionInfo         The {@link TestCacheRegionInfo} to trim.
	 * 
	 * @param bytesToRemove              Number of bytes to trim away at the front
	 *                                   or end of the original existing region,
	 *                                   must not be negative or zero. Must not be
	 *                                   bigger than the original region info's size
	 *                                   minus 1.
	 * @param originalByteCopyStartIndex The byte array index in the original region
	 *                                   info bytes to start copying. This
	 *                                   influences if trimming happens at front or
	 *                                   back of the region.
	 * @return The trimmed {@link TestCacheRegionInfo}
	 */
	private TestCacheRegionInfo trimRegionInfo(TestCacheRegionInfo originalRegionInfo, int bytesToRemove,
		int originalByteCopyStartIndex) {
		Reject.ifNotInInterval(bytesToRemove, 1, originalRegionInfo.getRegionSize() - 1, "bytesToRemove");

		byte[] remainingRegionBytes = new byte[originalRegionInfo.getRegionSize() - bytesToRemove];

		System.arraycopy(originalRegionInfo.getRegionBytes(), originalByteCopyStartIndex, remainingRegionBytes, 0,
			remainingRegionBytes.length);

		return new TestCacheRegionInfo(originalRegionInfo.getRegionStartOffset() + originalByteCopyStartIndex,
			remainingRegionBytes);
	}

	/**
	 * Validates the given index against the internal region data structure. Throws
	 * a {@link PreconditionUnfullfilledException} in case it is invalid. Otherwise
	 * it does nothing.
	 * 
	 * @param regionIndex The region index to verify
	 */
	private void validateRegionIndex(int regionIndex) {
		Reject.ifNegative(regionIndex, "regionIndex");
		Reject.ifTrue((regionInfos.size() == 0) && (regionIndex > 0), "regionInfos.size() == 0 && regionIndex > 0");
		Reject.ifTrue((regionInfos.size() > 0) && (regionIndex >= regionInfos.size()),
			"regionInfos.size() > 0 && regionIndex >= regionInfos.size()");
	}
}