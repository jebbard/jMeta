package com.github.jmeta.library.media.impl.cache;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;

import com.github.jmeta.library.media.api.helper.TestMedia;
import com.github.jmeta.library.media.api.types.MediumOffset;
import com.github.jmeta.library.media.api.types.MediumRegion;
import com.github.jmeta.utility.dbc.api.exceptions.PreconditionUnfullfilledException;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * This class tests the {@link MediumCache} class. It uses special naming
 * conventions for the test methods of
 * {@link MediumCache#addRegion(MediumRegion)}: addRegion__[cache fill
 * level]_[location / covering of add]_[size properties of the add]__[expected
 * result], with the following examples:
 * <ul>
 * <li>[cache fill level]: "toFilledCache", "toEmptyCache"</li>
 * <li>[location / covering of add]: "beforeExistRegs", "overlExistRegsAtFront"
 * ...</li>
 * <li>[size properties of the add]: "exceedMaxCSize",
 * "twiceMaxRSizeAndExceedMaxCSize"</li>
 * <li>[expected result]: "addsNewReg_leaveExistRegUnchanged",
 * "addsNewReg_shrinksExistRegAtFront"</li>
 * </ul>
 *
 * Due to the long method names to be expected we had to abbreviate some terms,
 * e.g. "exist" = existing, "reg" = region, "CSize" = cache size, "RSize" =
 * region size etc.
 */
public class MediumCacheTest {

	/**
	 * This is a list index into the list of {@link MediumRegion}s built by the
	 * {@link TestCacheBuilder} which is created from
	 * {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions()}. It refers
	 * to the first {@link MediumRegion} in the default {@link TestCacheBuilder}
	 * having direct consecutive follow-up {@link MediumRegion}s in the cache.
	 */
	private final static int DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS = 0;

	/**
	 * Size in bytes of the first range with multiple consecutive cached regions in
	 * the default cache
	 */
	private final static int DEFAULT_CACHE_FIRST_CONSECUTIVE_REGIONS_SIZE = 160;

	/**
	 * Size in bytes of the first gap between cached regions in the default cache
	 */
	private final static int DEFAULT_CACHE_FIRST_GAP_SIZE = 200;

	/**
	 * This is a medium offset before the first cached {@link MediumRegion} in the
	 * default {@link TestCacheBuilder}, which is created from
	 * {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions()}.
	 */
	private final static long DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION = 2L;
	/**
	 * This is a medium offset within the first gap in the default
	 * {@link TestCacheBuilder}, which is created from
	 * {@link #createDefaultLayoutHavingSubsequentAndScatteredRegions()}.
	 */
	private final static long DEFAULT_CACHE_OFFSET_IN_FIRST_GAP = 165L;

	private static final byte DEFAULT_FILL_BYTE = -13;

	/**
	 * Creates a cached {@link MediumRegion} filled with a default fill byte
	 *
	 * @param offset The start offset to use
	 * @param size   The size to use
	 * @return The created {@link MediumRegion}
	 */
	private static MediumRegion createCachedRegionWithDefaultFillByte(long offset, int size) {
		return new MediumRegion(TestMedia.at(offset),
			ByteBuffer.wrap(TestCacheBuilder.regionBytesFromDistinctOffsetSequence(offset, size)));
	}

	/**
	 * Creates an uncached {@link MediumRegion} of the given size
	 *
	 * @param offset The start offset to use
	 * @param size   The size to use
	 * @return The created {@link MediumRegion}
	 */
	private static MediumRegion createUnCachedRegion(long offset, int size) {
		return new MediumRegion(TestMedia.at(offset), size);
	}

	/**
	 * Returns the total size of a list of {@link MediumRegion}s.
	 *
	 * @param regionList The region list
	 * @return The summed size of all regions in the list.
	 */
	private static int getTotalRegionSize(List<MediumRegion> regionList) {
		return regionList.stream().collect(Collectors.summingInt(region -> region.getSize()));
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toEmptyCache_belowMaxRAndCSize__addsNewReg() {
		MediumCache cache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(0L, 20);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(TestMedia.DEFAULT_TEST_MEDIUM);

		expectedCacheContent.appendRegionInfo(regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toEmptyCache_exceedMaxCSize__addsLastSplitNewRegPartsUpToMaxCSize() {
		int maxRegionSize = 350;
		long maxCacheSize = 1000L;

		MediumCache cache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM, maxCacheSize, maxRegionSize);

		long startOffset = 10L;
		int regionToAddSize = (int) (maxCacheSize + 100);

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(TestMedia.DEFAULT_TEST_MEDIUM);

		expectedCacheContent.appendMultipleRegionInfos(startOffset + maxRegionSize,
			regionToAdd.getSize() - maxRegionSize, maxRegionSize, MediumCacheTest.DEFAULT_FILL_BYTE);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toEmptyCache_exceedMaxRSize__addsMultiRegs() {
		int maxRegionSize = 50;
		long maxCacheSize = Long.MAX_VALUE;

		MediumCache cache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM, maxCacheSize, maxRegionSize);

		long startOffset = 10L;
		int regionToAddSize = (7 * maxRegionSize) + 5;

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(TestMedia.DEFAULT_TEST_MEDIUM);

		expectedCacheContent.appendMultipleRegionInfos(startOffset, regionToAdd.getSize(), maxRegionSize,
			MediumCacheTest.DEFAULT_FILL_BYTE);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_belowMaxRAndCSize_beforeExistRegsNoOverl__addsNewReg_leavesExistRegsUnchanged() {
		int maxRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;
		long maxCacheSize = MediumCache.UNLIMITED_CACHE_SIZE;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		int regionToModifyIndex = 0;

		int regionToAddSize = 2;
		long startOffset = defaultCacheBuilder.getStartOffset() - regionToAddSize;

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(defaultCacheBuilder);

		expectedCacheContent.insertRegionInfoBefore(regionToModifyIndex, regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_belowMaxRAndCSize_behindExistRegsNoOverl__addsNewReg_leavesExistRegsUnchanged() {
		int maxRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;
		long maxCacheSize = MediumCache.UNLIMITED_CACHE_SIZE;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		long startOffset = defaultCacheBuilder.getEndOffset() + 5;
		int regionToAddSize = 20;

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(defaultCacheBuilder);

		expectedCacheContent.appendRegionInfo(regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_belowMaxRAndCSize_enclosedByExistReg__dividesExistgRegWithNewReg() {
		int maxRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;
		long maxCacheSize = MediumCache.UNLIMITED_CACHE_SIZE;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		List<MediumRegion> allCachedRegions = defaultCacheBuilder.getAllCachedRegions();

		int regionToModifyIndex = 0;

		MediumRegion existingRegion = allCachedRegions.get(regionToModifyIndex);

		int newRegionStartDistanceToExistingRegionStart = 4;
		int newRegionEndDistanceToExistingRegionEnd = 7;

		long startOffset = allCachedRegions.get(regionToModifyIndex).getStartOffset().getAbsoluteMediumOffset()
			+ newRegionStartDistanceToExistingRegionStart;
		int regionToAddSize = (int) (existingRegion.calculateEndOffset().getAbsoluteMediumOffset()
			- newRegionEndDistanceToExistingRegionEnd - startOffset);

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(defaultCacheBuilder);

		expectedCacheContent.clipAllRegionsAgainstMediumRegion(regionToAdd);
		expectedCacheContent.insertRegionInfoBefore(regionToModifyIndex + 1, regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_belowMaxRAndCSize_exactlyCoverExistReg__replacesExistWithNewReg() {
		int maxRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;
		long maxCacheSize = MediumCache.UNLIMITED_CACHE_SIZE;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		int regionToModifyIndex = 0;

		MediumRegion existingRegion = defaultCacheBuilder.getAllCachedRegions().get(regionToModifyIndex);

		long startOffset = existingRegion.getStartOffset().getAbsoluteMediumOffset();
		int regionToAddSize = existingRegion.getSize();

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(defaultCacheBuilder);

		expectedCacheContent.clipAllRegionsAgainstMediumRegion(regionToAdd);
		expectedCacheContent.insertRegionInfoBefore(regionToModifyIndex, regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_belowMaxRAndCSize_overlExistRegAtBack__addsNewReg_shrinksExistRegAtBack() {
		int maxRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;
		long maxCacheSize = MediumCache.UNLIMITED_CACHE_SIZE;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		int regionToModifyIndex = 2;

		MediumRegion existingRegion = defaultCacheBuilder.getAllCachedRegions().get(regionToModifyIndex);

		int newRegionStartDistanceToOriginalRegionStart = 5;

		long startOffset = existingRegion.getStartOffset().getAbsoluteMediumOffset()
			+ newRegionStartDistanceToOriginalRegionStart;
		int regionToAddSize = existingRegion.getSize();

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(defaultCacheBuilder);

		expectedCacheContent.clipAllRegionsAgainstMediumRegion(regionToAdd);
		expectedCacheContent.insertRegionInfoBefore(regionToModifyIndex + 1, regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_belowMaxRAndCSize_overlExistRegAtFront__addsNewReg_shrinksExistRegAtFront() {
		int maxRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;
		long maxCacheSize = MediumCache.UNLIMITED_CACHE_SIZE;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		int regionToModifyIndex = 0;

		MediumRegion existingRegion = defaultCacheBuilder.getAllCachedRegions().get(regionToModifyIndex);

		int newRegionStartDistanceToOriginalRegionStart = -2;

		long startOffset = existingRegion.getStartOffset().getAbsoluteMediumOffset()
			+ newRegionStartDistanceToOriginalRegionStart;
		int regionToAddSize = existingRegion.getSize();

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(defaultCacheBuilder);

		expectedCacheContent.clipAllRegionsAgainstMediumRegion(regionToAdd);
		expectedCacheContent.insertRegionInfoBefore(regionToModifyIndex, regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_belowMaxRAndCSize_overlMultiRegsAndGaps__replacesFullyCovRegsWithNewRegAndSplitsFirstAndLastExistReg() {
		int maxRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;
		long maxCacheSize = MediumCache.UNLIMITED_CACHE_SIZE;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		List<MediumRegion> allCachedRegions = defaultCacheBuilder.getAllCachedRegions();

		int firstRegionToModifyIndex = 1;
		int secondRegionToModifyIndex = 5;

		MediumRegion firstOverlappedExistingRegion = allCachedRegions.get(firstRegionToModifyIndex);
		MediumRegion lastOverlappedExistingRegion = allCachedRegions.get(secondRegionToModifyIndex);

		int newRegionStartDistanceToFirstExistingRegionStart = 5;
		int newRegionEndDistanceToLastExistingRegionEnd = 3;

		long startOffset = firstOverlappedExistingRegion.getStartOffset().getAbsoluteMediumOffset()
			+ newRegionStartDistanceToFirstExistingRegionStart;
		int regionToAddSize = (int) (lastOverlappedExistingRegion.calculateEndOffset().getAbsoluteMediumOffset()
			- newRegionEndDistanceToLastExistingRegionEnd - startOffset);

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(defaultCacheBuilder);

		expectedCacheContent.clipAllRegionsAgainstMediumRegion(regionToAdd);
		expectedCacheContent.insertRegionInfoBefore(firstRegionToModifyIndex + 1, regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_belowMaxRAndCSize_overlMultiRegsAndGapsStartAtExistRegStart__replacesFullyCovRegsWithNewRegAndSplitsLastExistReg() {
		int maxRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;
		long maxCacheSize = MediumCache.UNLIMITED_CACHE_SIZE;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		List<MediumRegion> allCachedRegions = defaultCacheBuilder.getAllCachedRegions();

		int firstRegionToModifyIndex = 1;
		int secondRegionToModifyIndex = 5;

		MediumRegion existingRegion = allCachedRegions.get(secondRegionToModifyIndex);

		int newRegionEndDistanceToLastExistingRegionEnd = 3;

		long startOffset = allCachedRegions.get(firstRegionToModifyIndex).getStartOffset().getAbsoluteMediumOffset();
		int regionToAddSize = (int) (existingRegion.calculateEndOffset().getAbsoluteMediumOffset()
			- newRegionEndDistanceToLastExistingRegionEnd - startOffset);

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(defaultCacheBuilder);

		expectedCacheContent.clipAllRegionsAgainstMediumRegion(regionToAdd);
		expectedCacheContent.insertRegionInfoBefore(firstRegionToModifyIndex, regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_exceedMaxCSizeNewRegBiggerThanAllExist_noOverl__removesAllExistAndAddsNewReg() {
		int maxRegionSize = 1000;
		long maxCacheSize = 1000L;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		long startOffset = 9999L;
		int regionToAddSize = (int) (maxCacheSize - 1);

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(TestMedia.DEFAULT_TEST_MEDIUM);

		expectedCacheContent.appendRegionInfo(regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_exceedMaxCSizeNewRegBiggerThanMaxCSize_coveringSomeExist_removesAllExistAndAddsLastSplitNewRegParts() {
		int maxRegionSize = 150;
		long maxCacheSize = 500L;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		// Why choosing 46L as start offset?
		// - New region spans from [46, 548)
		// - It does not overlap the first default region, but the second, starting at
		// the 2nd byte
		// - It covers all other regions and gaps in the default cache, and ends exactly
		// where the last region in the
		// default cache ends
		long startOffset = 46L;
		int regionToAddSize = (int) (maxCacheSize + 2);

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		// Expectation: The total size of the cache after adding the new region would be
		// 500 + 2 (=size of new region) + 1 (=size of remainder of second region) + 40
		// (=size of first region) = 543
		// As this exceeds the max cache size by 43, expectation is:
		// (1) The first region spanning 40 bytes and the one byte remainder of the
		// second region are removed
		// (2) The first part of the new region having a size of 150 bytes is removed
		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(defaultCacheBuilder);

		expectedCacheContent.clipAllRegionsAgainstMediumRegion(regionToAdd);
		expectedCacheContent.removeRegionInfo(0);
		expectedCacheContent.removeRegionInfo(0);
		expectedCacheContent.appendMultipleRegionInfos(startOffset + maxRegionSize,
			regionToAdd.getSize() - maxRegionSize, maxRegionSize, MediumCacheTest.DEFAULT_FILL_BYTE);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_exceedMaxCSizeWithLessBytesThanFirstAddedReg_noOverl__removesFirstAddedAndAddsNewReg() {
		int maxRegionSize = 200;
		long maxCacheSize = 250L;

		MediumCache cache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM, maxCacheSize, maxRegionSize);

		MediumRegion existingRegion = MediumCacheTest.createCachedRegionWithDefaultFillByte(0L, 200);

		cache.addRegion(existingRegion);

		long startOffset = 300L;
		int regionToAddSize = 100;

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(TestMedia.DEFAULT_TEST_MEDIUM);

		expectedCacheContent.appendRegionInfo(regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test
	public void addRegion__toFilledCache_sumSizeAboveButNewSizeBelowMaxCSize_overlExistRegAtFront__addsNewReg_shrinksExistRegAtFront() {
		int maxRegionSize = 200;
		long maxCacheSize = 250L;

		MediumCache cache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM, maxCacheSize, maxRegionSize);

		MediumRegion firstRegion = MediumCacheTest.createCachedRegionWithDefaultFillByte(0L, maxRegionSize);
		cache.addRegion(firstRegion);

		long startOffset = 150L;
		int regionToAddSize = 100;

		MediumRegion regionToAdd = MediumCacheTest.createCachedRegionWithDefaultFillByte(startOffset, regionToAddSize);

		TestCacheBuilder expectedCacheContent = new TestCacheBuilder(TestMedia.DEFAULT_TEST_MEDIUM);

		expectedCacheContent.appendRegionInfo(firstRegion);
		expectedCacheContent.clipAllRegionsAgainstMediumRegion(regionToAdd);
		expectedCacheContent.appendRegionInfo(regionToAdd);

		testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(cache, regionToAdd,
			expectedCacheContent);
	}

	/**
	 * Tests {@link MediumCache#addRegion(MediumRegion)}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void addRegion_forInvalidReference_throwsException() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);
		emptyCache.addRegion(new MediumRegion(TestMedia.at(TestMedia.OTHER_MEDIUM, 0L), 10));
	}

	/**
	 * Verifies that all invariants of the {@link MediumCache} class are fulfilled.
	 *
	 * @param cache The {@link MediumCache} instance to check.
	 */
	private void assertCacheInvariantsAreFulfilled(MediumCache cache) {
		List<MediumRegion> actualRegions = cache.getAllCachedRegions();

		long actualCacheSize = 0;

		for (MediumRegion mediumRegion : actualRegions) {
			actualCacheSize += mediumRegion.getSize();

			Assert.assertTrue(
				"The current medium regions size <" + mediumRegion.getSize()
					+ "> must be smaller or equal to the configured maximum cache region size of <"
					+ cache.getMaximumCacheRegionSizeInBytes() + ">",
				mediumRegion.getSize() <= cache.getMaximumCacheRegionSizeInBytes());
		}

		Assert.assertTrue("The current total medium region size of <" + actualCacheSize
			+ "> must be smaller or equal to the configured maximum cache size of <"
			+ cache.getMaximumCacheSizeInBytes() + ">", actualCacheSize <= cache.getMaximumCacheSizeInBytes());
	}

	/**
	 * Verifies that the given {@link MediumCache} instance is indeed empty.
	 *
	 * @param cache The cache instance to check.
	 */
	private void assertCacheIsEmpty(MediumCache cache) {

		Assert.assertEquals(0L, cache.calculateCurrentCacheSizeInBytes());
		Assert.assertEquals(0, cache.getAllCachedRegions().size());
	}

	/**
	 * Tests {@link MediumCache#clear()}.
	 */
	@Test
	public void clear_forEmptyCache_changesNothing() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);

		emptyCache.clear();

		assertCacheIsEmpty(emptyCache);
	}

	/**
	 * Tests {@link MediumCache#clear()}.
	 */
	@Test
	public void clear_forFilledCache_cacheIsEmpty() {
		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache(MediumCache.UNLIMITED_CACHE_SIZE,
			MediumCache.UNLIMITED_CACHE_REGION_SIZE);

		cache.clear();

		assertCacheIsEmpty(cache);
	}

	/**
	 * Tests {@link MediumCache#clear()}.
	 */
	@Test
	public void clearTwice_forFilledCache_cacheIsStillEmpty() {
		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache(MediumCache.UNLIMITED_CACHE_SIZE,
			MediumCache.UNLIMITED_CACHE_REGION_SIZE);

		cache.clear();

		assertCacheIsEmpty(cache);

		cache.clear();

		assertCacheIsEmpty(cache);
	}

	/**
	 * Tests
	 * {@link MediumCache#MediumCache(com.github.jmeta.library.media.api.types.Medium, long, int)},
	 * {@link MediumCache#getMedium()} ,
	 * {@link MediumCache#getMaximumCacheSizeInBytes()} and
	 * {@link MediumCache#getMaximumCacheRegionSizeInBytes()}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void constructor_forMaxCacheSizeSmallerThanMaxRegionSize_throwsException() {
		long maximumCacheSizeInBytes = 10L;
		int maximumCacheRegionSizeInBytes = 20;
		new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM, maximumCacheSizeInBytes, maximumCacheRegionSizeInBytes);
	}

	/**
	 * Tests
	 * {@link MediumCache#MediumCache(com.github.jmeta.library.media.api.types.Medium)},
	 * {@link MediumCache#getMedium()} ,
	 * {@link MediumCache#getMaximumCacheSizeInBytes()} and
	 * {@link MediumCache#getMaximumCacheRegionSizeInBytes()}.
	 */
	@Test
	public void constructor_initializesMediumAndDefaultSizesCorrectly() {
		MediumCache newCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);

		Assert.assertEquals(TestMedia.DEFAULT_TEST_MEDIUM, newCache.getMedium());
		Assert.assertEquals(MediumCache.UNLIMITED_CACHE_SIZE, newCache.getMaximumCacheSizeInBytes());
		Assert.assertEquals(MediumCache.UNLIMITED_CACHE_REGION_SIZE, newCache.getMaximumCacheRegionSizeInBytes());
	}

	/**
	 * Tests
	 * {@link MediumCache#MediumCache(com.github.jmeta.library.media.api.types.Medium, long, int)},
	 * {@link MediumCache#getMedium()} ,
	 * {@link MediumCache#getMaximumCacheSizeInBytes()} and
	 * {@link MediumCache#getMaximumCacheRegionSizeInBytes()}.
	 */
	@Test
	public void constructor_initializesMediumAndSizesCorrectly() {
		long maximumCacheSizeInBytes = 20L;
		int maximumCacheRegionSizeInBytes = 10;
		MediumCache newCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM, maximumCacheSizeInBytes,
			maximumCacheRegionSizeInBytes);

		Assert.assertEquals(TestMedia.DEFAULT_TEST_MEDIUM, newCache.getMedium());
		Assert.assertEquals(maximumCacheSizeInBytes, newCache.getMaximumCacheSizeInBytes());
		Assert.assertEquals(maximumCacheRegionSizeInBytes, newCache.getMaximumCacheRegionSizeInBytes());
	}

	/**
	 * Creates a {@link TestCacheBuilder} with unlimited sizes and some pre-existing
	 * content: (1) Three consecutive regions of different sizes starting at an
	 * offset &gt; 0, (2) A gap of {@value #DEFAULT_CACHE_FIRST_GAP_SIZE} bytes, (3)
	 * Two consecutive regions, (4) A gap of 1 byte, (5) A last region
	 *
	 * @return The default {@link MediumCache} pre-filled and with unlimited sizes
	 */
	private TestCacheBuilder createDefaultLayoutHavingSubsequentAndScatteredRegions() {
		TestCacheBuilder cacheLayout = new TestCacheBuilder(TestMedia.DEFAULT_TEST_MEDIUM);

		cacheLayout.appendRegionInfo(5, 45);
		cacheLayout.appendRegionInfo(45, 65);
		cacheLayout.appendRegionInfo(65, 165);
		cacheLayout.appendRegionInfo(165 + MediumCacheTest.DEFAULT_CACHE_FIRST_GAP_SIZE, 370);
		cacheLayout.appendRegionInfo(397, 398);
		cacheLayout.appendRegionInfo(398, 548);

		return cacheLayout;
	}

	/**
	 * Tests {@link MediumCache#getAllCachedRegions()}.
	 */
	@Test
	public void getAllCachedRegions_forEmptyCache_returnsEmptyList() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);

		Assert.assertEquals(0, emptyCache.getAllCachedRegions().size());
	}

	/**
	 * Tests {@link MediumCache#getAllCachedRegions()}.
	 */
	@Test
	public void getAllCachedRegions_forFilledCache_returnsExpectedRegionsInCorrectOrder() {
		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache(MediumCache.UNLIMITED_CACHE_SIZE,
			MediumCache.UNLIMITED_CACHE_REGION_SIZE);

		assertCacheInvariantsAreFulfilled(cache);

		Assert.assertEquals(cacheLayout.getAllCachedRegions().size(), cache.getAllCachedRegions().size());
		Assert.assertEquals(cacheLayout.getAllCachedRegions(), cache.getAllCachedRegions());
	}

	/**
	 * Tests {@link MediumCache#getCachedByteCountAt(MediumOffset)}.
	 */
	@Test
	public void getCachedByteCountAt_endOfLastConsecutiveRegion_returnsZero() {
		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache(MediumCache.UNLIMITED_CACHE_SIZE,
			MediumCache.UNLIMITED_CACHE_REGION_SIZE);

		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

		MediumRegion lastConsecutiveRegion = firstConsecutiveRegions.get(firstConsecutiveRegions.size() - 1);
		Assert.assertEquals(0, cache.getCachedByteCountAt(lastConsecutiveRegion.calculateEndOffset()));
	}

	/**
	 * Tests {@link MediumCache#getCachedByteCountAt(MediumOffset)}.
	 */
	@Test
	public void getCachedByteCountAt_endOfRegionWithConsecutiveRegions_returnsTotalSizeOfSubsequentRegions() {
		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache(MediumCache.UNLIMITED_CACHE_SIZE,
			MediumCache.UNLIMITED_CACHE_REGION_SIZE);

		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

		MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);
		Assert.assertEquals(
			MediumCacheTest.DEFAULT_CACHE_FIRST_CONSECUTIVE_REGIONS_SIZE - firstConsecutiveRegion.getSize(),
			cache.getCachedByteCountAt(firstConsecutiveRegion.calculateEndOffset()));
	}

	/**
	 * Tests {@link MediumCache#getCachedByteCountAt(MediumOffset)}.
	 */
	@Test
	public void getCachedByteCountAt_forEmptyCache_returnsZero() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);

		Assert.assertEquals(0,
			emptyCache.getCachedByteCountAt(TestMedia.at(MediumCacheTest.DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION)));
		Assert.assertEquals(0,
			emptyCache.getCachedByteCountAt(TestMedia.at(MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP)));
	}

	/**
	 * Tests {@link MediumCache#getCachedByteCountAt(MediumOffset)}.
	 */
	@Test
	public void getCachedByteCountAt_forFilledCacheButWithOffsetOutsideRegions_returnsZero() {
		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache(MediumCache.UNLIMITED_CACHE_SIZE,
			MediumCache.UNLIMITED_CACHE_REGION_SIZE);

		Assert.assertEquals(0,
			cache.getCachedByteCountAt(TestMedia.at(MediumCacheTest.DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION)));
		Assert.assertEquals(0,
			cache.getCachedByteCountAt(TestMedia.at(MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP)));
	}

	/**
	 * Tests {@link MediumCache#getCachedByteCountAt(MediumOffset)}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void getCachedByteCountAt_forInvalidReference_throwsException() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);
		emptyCache.getCachedByteCountAt(TestMedia.at(TestMedia.OTHER_MEDIUM, 0L));
	}

	/**
	 * Tests {@link MediumCache#getCachedByteCountAt(MediumOffset)}.
	 */
	@Test
	public void getCachedByteCountAt_middleOfLastConsecutiveRegion_returnsRemainingLastRegionSize() {
		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache(MediumCache.UNLIMITED_CACHE_SIZE,
			MediumCache.UNLIMITED_CACHE_REGION_SIZE);

		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

		MediumRegion lastConsecutiveRegion = firstConsecutiveRegions.get(firstConsecutiveRegions.size() - 1);
		int middleOffset = lastConsecutiveRegion.getSize() / 2;
		Assert.assertEquals(lastConsecutiveRegion.getSize() - middleOffset,
			cache.getCachedByteCountAt(lastConsecutiveRegion.getStartOffset().advance(middleOffset)));
	}

	/**
	 * Tests {@link MediumCache#getCachedByteCountAt(MediumOffset)}.
	 */
	@Test
	public void getCachedByteCountAt_middleOfRegionWithConsecutiveRegions_returnsCorrectTotalConsecutiveByteCount() {
		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache(MediumCache.UNLIMITED_CACHE_SIZE,
			MediumCache.UNLIMITED_CACHE_REGION_SIZE);

		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

		MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);
		int middleOffset = firstConsecutiveRegion.getSize() / 2;
		Assert.assertEquals(MediumCacheTest.DEFAULT_CACHE_FIRST_CONSECUTIVE_REGIONS_SIZE - middleOffset,
			cache.getCachedByteCountAt(firstConsecutiveRegion.getStartOffset().advance(middleOffset)));
	}

	/**
	 * Tests {@link MediumCache#getCachedByteCountAt(MediumOffset)}.
	 */
	@Test
	public void getCachedByteCountAt_startOfRegionWithConsecutiveRegions_returnsTotalSizeOfAllConsecutiveRegions() {
		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache(MediumCache.UNLIMITED_CACHE_SIZE,
			MediumCache.UNLIMITED_CACHE_REGION_SIZE);

		List<MediumRegion> firstConsecutiveRegions = getFirstConsecutiveRegionsFromDefaultCache(cacheLayout);

		MediumRegion firstConsecutiveRegion = firstConsecutiveRegions.get(0);
		Assert.assertEquals(MediumCacheTest.DEFAULT_CACHE_FIRST_CONSECUTIVE_REGIONS_SIZE,
			cache.getCachedByteCountAt(firstConsecutiveRegion.getStartOffset()));
	}

	/**
	 * Tests {@link MediumCache#calculateCurrentCacheSizeInBytes()}.
	 */
	@Test
	public void getCurrentCacheSizeInBytes_forEmptyCache_returnsZero() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);

		Assert.assertEquals(0, emptyCache.calculateCurrentCacheSizeInBytes());
	}

	/**
	 * Tests {@link MediumCache#calculateCurrentCacheSizeInBytes()}.
	 */
	@Test
	public void getCurrentCacheSizeInBytes_forFilledCache_returnsExpectedTotalSize() {
		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = cacheLayout.buildCache(MediumCache.UNLIMITED_CACHE_SIZE,
			MediumCache.UNLIMITED_CACHE_REGION_SIZE);

		Assert.assertEquals(cacheLayout.getTotalRegionSizeInBytes(), cache.calculateCurrentCacheSizeInBytes());
	}

	/**
	 * Returns the first consecutive sequence of {@link MediumRegion}s in the
	 * default {@link TestCacheBuilder}.
	 *
	 * @param cacheLayout The {@link TestCacheBuilder} to use containing details
	 *                    about the cache structure
	 * @return the first consecutive sequence of {@link MediumRegion}s in the
	 *         default {@link TestCacheBuilder}.
	 */
	private List<MediumRegion> getFirstConsecutiveRegionsFromDefaultCache(TestCacheBuilder cacheLayout) {
		List<MediumRegion> allRegions = cacheLayout.getAllCachedRegions();

		List<MediumRegion> firstConsecutiveRegions = new ArrayList<>();

		firstConsecutiveRegions
			.add(allRegions.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS));
		firstConsecutiveRegions
			.add(allRegions.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 1));
		firstConsecutiveRegions
			.add(allRegions.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2));
		return firstConsecutiveRegions;
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_forEmptyCache_returnsSingleUncachedRegion() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);
		MediumOffset startOffset = TestMedia.at(0L);
		int rangeSizeInBytes = 10;
		List<MediumRegion> regionsInRange = emptyCache.getRegionsInRange(startOffset, rangeSizeInBytes);

		Assert.assertEquals(1, regionsInRange.size());

		Assert.assertEquals(new MediumRegion(startOffset, rangeSizeInBytes), regionsInRange.get(0));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void getRegionsInRange_forInvalidRangeSize_throwsException() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);
		emptyCache.getRegionsInRange(TestMedia.at(0L), -10);
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void getRegionsInRange_forInvalidReference_throwsException() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);
		emptyCache.getRegionsInRange(TestMedia.at(TestMedia.OTHER_MEDIUM, 0L), 10);
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeCoveringAllCachedRegions_returnsCachedRegionsAndGapsBetweenThem() {

		int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		int uncachedRegionBeforeSize = 3;
		int uncachedRegionAfterSize = 20;

		List<MediumRegion> allCachedRegionsWithGaps = cacheLayout.getAllCachedRegionsWithGaps();
		List<MediumRegion> expectedRegions = new ArrayList<>();

		MediumRegion lastRegion = allCachedRegionsWithGaps.get(allCachedRegionsWithGaps.size() - 1);

		expectedRegions.add(MediumCacheTest
			.createUnCachedRegion(cacheLayout.getStartOffset() - uncachedRegionBeforeSize, uncachedRegionBeforeSize));
		expectedRegions.addAll(allCachedRegionsWithGaps);
		expectedRegions.add(new MediumRegion(lastRegion.calculateEndOffset(), uncachedRegionAfterSize));

		MediumRegion[] expectedRegionsArray = new MediumRegion[expectedRegions.size()];
		testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout, maxCacheRegionSize,
			expectedRegions.toArray(expectedRegionsArray));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeCoveringFullCachedRegion_returnsSingleRegion() {

		int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout, maxCacheRegionSize, cacheLayout
			.getAllCachedRegions().get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeFullyCoveringMultipleCachedRegions_returnsAllCoveredRegions() {

		int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout, maxCacheRegionSize,
			cacheLayout.getAllCachedRegions()
				.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS),
			cacheLayout.getAllCachedRegions()
				.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 1),
			cacheLayout.getAllCachedRegions()
				.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeOutsideCachedRegionAndSizeLessThanMaxRegionSize_returnsSingleUncachedRegion() {

		int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout, maxCacheRegionSize,
			MediumCacheTest.createUnCachedRegion(MediumCacheTest.DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION,
				(int) (cacheLayout.getStartOffset() - MediumCacheTest.DEFAULT_CACHE_OFFSET_BEFORE_FIRST_REGION)));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeOutsideCachedRegionExactlyTripleSizeOfMaxRegionSize_returnsThreeUncachedRegions() {

		int maxCacheRegionSize = 60;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout, maxCacheRegionSize,
			MediumCacheTest.createUnCachedRegion(MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP, maxCacheRegionSize),
			MediumCacheTest.createUnCachedRegion(MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP + maxCacheRegionSize,
				maxCacheRegionSize),
			MediumCacheTest.createUnCachedRegion(
				MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP + (2 * maxCacheRegionSize), maxCacheRegionSize));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeOutsideCachedRegionSizeBiggerThanButNoMultipleOfMaxRegionSize_returnsTwoUncachedRegions() {

		int maxCacheRegionSize = 180;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		int remainderRegionSize = 5;

		testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout, maxCacheRegionSize,
			MediumCacheTest.createUnCachedRegion(MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP, maxCacheRegionSize),
			MediumCacheTest.createUnCachedRegion(MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP + maxCacheRegionSize,
				remainderRegionSize));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeOverlappingCachedRegionsAtBack_returnsTwoCachedAndSingleGapRegion() {

		int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		int uncachedRegionSize = 10;

		MediumRegion firstRegion = cacheLayout.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2);
		testGetRegionsInRange_fullyCoveringExpectedRegions(cacheLayout, maxCacheRegionSize, firstRegion,
			new MediumRegion(firstRegion.calculateEndOffset(), uncachedRegionSize));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeOverlappingCachedRegionsAtFront_returnsSingleGapAndSingleCachedRegion() {

		int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		int uncachedRegionSize = 3;

		testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 0, 1, maxCacheRegionSize,
			MediumCacheTest.createUnCachedRegion(cacheLayout.getStartOffset() - uncachedRegionSize, uncachedRegionSize),
			cacheLayout.getAllCachedRegions()
				.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeStartingAndEndingWithinDifferentCachedRegionsWithoutGaps_returnsAllCoveredRegions() {

		int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 1, 2, maxCacheRegionSize,
			cacheLayout.getAllCachedRegions()
				.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS),
			cacheLayout.getAllCachedRegions()
				.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 1),
			cacheLayout.getAllCachedRegions()
				.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeWithGapBetweenTwoCachedRegionsAndExactlyDoubleSizeOfMaxRegionSize_returnsCachedRegionsAndGap() {

		int maxCacheRegionSize = MediumCacheTest.DEFAULT_CACHE_FIRST_GAP_SIZE / 2;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 5, 7, maxCacheRegionSize,
			cacheLayout.getAllCachedRegions()
				.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2),
			MediumCacheTest.createUnCachedRegion(MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP, maxCacheRegionSize),
			MediumCacheTest.createUnCachedRegion(MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP + maxCacheRegionSize,
				maxCacheRegionSize),
			cacheLayout.getAllCachedRegions()
				.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 3));
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeWithGapBetweenTwoCachedRegionsAndSizeLessThanMaxRegionSize_returnsCachedRegionsAndGap() {

		int maxCacheRegionSize = MediumCacheTest.DEFAULT_CACHE_FIRST_GAP_SIZE + 33;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumRegion firstRegion = cacheLayout.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2);
		MediumRegion lastRegion = cacheLayout.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 3);

		testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 5, 7, maxCacheRegionSize, firstRegion,
			MediumCacheTest.createUnCachedRegion(MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP,
				(int) lastRegion.getStartOffset().distanceTo(firstRegion.calculateEndOffset())),
			lastRegion);
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void getRegionsInRange_rangeWithinCachedRegion_returnsEnclosingRegion() {

		int maxCacheRegionSize = MediumCache.UNLIMITED_CACHE_REGION_SIZE;

		TestCacheBuilder cacheLayout = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 1, 2, maxCacheRegionSize, cacheLayout
			.getAllCachedRegions().get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS));
	}

	/**
	 * Tests {@link MediumCache#removeRegionsInRange(MediumOffset, int)}.
	 */
	@Test(expected = PreconditionUnfullfilledException.class)
	public void removeRegionsInRange_forInvalidReference_throwsException() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);
		emptyCache.removeRegionsInRange(TestMedia.at(TestMedia.OTHER_MEDIUM, 0L), 10);
	}

	/**
	 * Tests {@link MediumCache#removeRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void removeRegionsInRange_fromEmptyCache_anyRange_doesNotChangeCacheSize() {
		MediumCache emptyCache = new MediumCache(TestMedia.DEFAULT_TEST_MEDIUM);
		emptyCache.removeRegionsInRange(TestMedia.at(TestMedia.DEFAULT_TEST_MEDIUM, 0L), 10);

		Assert.assertEquals(emptyCache.calculateCurrentCacheSizeInBytes(), 0);
	}

	/**
	 * Tests {@link MediumCache#removeRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void removeRegionsInRange_fromFilledCache_coveringAndPartlyOverlappingMultipleCachedRegionsWithGaps_removesEntireRegionsAndPartlyCoveredOnes() {
		int maxCacheRegionSize = 150;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumRegion firstExistingRegion = defaultCacheBuilder.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS);
		MediumRegion lastExistingRegion = defaultCacheBuilder.getAllCachedRegions()
			.get(defaultCacheBuilder.getAllCachedRegions().size() - 1);

		int keepAtEnd = 4;

		MediumOffset rangeToRemoveStartOffset = firstExistingRegion.getStartOffset();
		int rangeToRemoveSize = (int) lastExistingRegion.calculateEndOffset().distanceTo(rangeToRemoveStartOffset)
			- keepAtEnd;

		int bytesActuallyRemoved = (int) defaultCacheBuilder.getTotalRegionSizeInBytes() - keepAtEnd;

		MediumRegion[] splitRegions = lastExistingRegion.split(rangeToRemoveStartOffset.advance(rangeToRemoveSize));

		testRemoveRegionsInRange(defaultCacheBuilder, 0, keepAtEnd, maxCacheRegionSize, bytesActuallyRemoved,
			new MediumRegion(rangeToRemoveStartOffset, maxCacheRegionSize),
			new MediumRegion(rangeToRemoveStartOffset.advance(maxCacheRegionSize), maxCacheRegionSize),
			new MediumRegion(rangeToRemoveStartOffset.advance(2 * maxCacheRegionSize), maxCacheRegionSize),
			new MediumRegion(rangeToRemoveStartOffset.advance(3 * maxCacheRegionSize), 89), splitRegions[1]);
	}

	/**
	 * Tests {@link MediumCache#removeRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void removeRegionsInRange_fromFilledCache_exactlyCoveringMultipleCachedRegions_removesEntireRegions() {
		int maxCacheRegionSize = 150;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumRegion existingRegion1 = defaultCacheBuilder.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS);
		MediumRegion existingRegion2 = defaultCacheBuilder.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 1);
		MediumRegion existingRegion3 = defaultCacheBuilder.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2);

		MediumOffset rangeToRemoveStartOffset = existingRegion1.getStartOffset();
		int rangeToRemoveSize = existingRegion1.getSize() + existingRegion2.getSize() + existingRegion3.getSize();

		int bytesActuallyRemoved = rangeToRemoveSize;

		testRemoveRegionsInRange(defaultCacheBuilder, 0, 0, maxCacheRegionSize, bytesActuallyRemoved,
			new MediumRegion(rangeToRemoveStartOffset, maxCacheRegionSize), new MediumRegion(
				rangeToRemoveStartOffset.advance(maxCacheRegionSize), rangeToRemoveSize - maxCacheRegionSize));
	}

	/**
	 * Tests {@link MediumCache#removeRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void removeRegionsInRange_fromFilledCache_exactlyCoveringSingleCachedRegion_removesEntireRegion() {
		int maxCacheRegionSize = 150;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumRegion existingRegion1 = defaultCacheBuilder.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS);

		MediumOffset rangeToRemoveStartOffset = existingRegion1.getStartOffset();
		int rangeToRemoveSize = existingRegion1.getSize();

		int bytesActuallyRemoved = existingRegion1.getSize();

		testRemoveRegionsInRange(defaultCacheBuilder, 0, 0, maxCacheRegionSize, bytesActuallyRemoved,
			new MediumRegion(rangeToRemoveStartOffset, rangeToRemoveSize));
	}

	/**
	 * Tests {@link MediumCache#removeRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void removeRegionsInRange_fromFilledCache_insideSingleCachedRegion_splitsRegionAndOnlyRemovesInnerPart() {
		int maxCacheRegionSize = 150;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumRegion existingRegion1 = defaultCacheBuilder.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS);

		int keepAtStart = 4;
		int keepAtEnd = 8;

		MediumOffset rangeToRemoveStartOffset = existingRegion1.getStartOffset().advance(keepAtStart);
		int rangeToRemoveSize = existingRegion1.getSize() - keepAtStart - keepAtEnd;

		int bytesActuallyRemoved = rangeToRemoveSize;

		MediumRegion[] splitRegions = existingRegion1.split(rangeToRemoveStartOffset);

		testRemoveRegionsInRange(defaultCacheBuilder, keepAtStart, keepAtEnd, maxCacheRegionSize, bytesActuallyRemoved,
			splitRegions[0], new MediumRegion(rangeToRemoveStartOffset, rangeToRemoveSize),
			splitRegions[1].split(rangeToRemoveStartOffset.advance(rangeToRemoveSize))[1]);
	}

	/**
	 * Tests {@link MediumCache#removeRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void removeRegionsInRange_fromFilledCache_inUncachedRange_doesNotChangeCacheSize() {
		int maxRegionSize = 150;
		long maxCacheSize = 500L;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumCache cache = defaultCacheBuilder.buildCache(maxCacheSize, maxRegionSize);

		long cacheSizeBefore = cache.calculateCurrentCacheSizeInBytes();

		cache.removeRegionsInRange(
			TestMedia.at(TestMedia.DEFAULT_TEST_MEDIUM, MediumCacheTest.DEFAULT_CACHE_OFFSET_IN_FIRST_GAP),
			MediumCacheTest.DEFAULT_CACHE_FIRST_GAP_SIZE / 2);

		Assert.assertEquals(cache.calculateCurrentCacheSizeInBytes(), cacheSizeBefore);
	}

	/**
	 * Tests {@link MediumCache#removeRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void removeRegionsInRange_fromFilledCache_overlappingSingleCachedRegionAtBack_splitsRegionAndOnlyRemovesBackPart() {
		int maxCacheRegionSize = 150;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumRegion existingRegion1 = defaultCacheBuilder.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS + 2);

		int keepAtStart = 4;

		MediumOffset rangeToRemoveStartOffset = existingRegion1.getStartOffset().advance(keepAtStart);
		int rangeToRemoveSize = existingRegion1.getSize() + 10;

		int bytesActuallyRemoved = existingRegion1.getSize() - keepAtStart;

		MediumRegion[] splitRegions = existingRegion1.split(rangeToRemoveStartOffset);

		testRemoveRegionsInRange(defaultCacheBuilder, keepAtStart, 0, maxCacheRegionSize, bytesActuallyRemoved,
			splitRegions[0], new MediumRegion(rangeToRemoveStartOffset, rangeToRemoveSize));
	}

	/**
	 * Tests {@link MediumCache#removeRegionsInRange(MediumOffset, int)}.
	 */
	@Test
	public void removeRegionsInRange_fromFilledCache_overlappingSingleCachedRegionAtFront_splitsRegionAndOnlyRemovesFrontPart() {
		int maxCacheRegionSize = 150;

		TestCacheBuilder defaultCacheBuilder = createDefaultLayoutHavingSubsequentAndScatteredRegions();

		MediumRegion existingRegion1 = defaultCacheBuilder.getAllCachedRegions()
			.get(MediumCacheTest.DEFAULT_CACHE_INDEX_FIRST_REGION_WITH_CONSECUTIVE_REGIONS);

		MediumOffset rangeToRemoveStartOffset = existingRegion1.getStartOffset().advance(-3);
		int rangeToRemoveSize = 10;

		int keepAtBack = (existingRegion1.getSize() + 3) - 10;

		int bytesActuallyRemoved = existingRegion1.getSize() - keepAtBack;

		MediumRegion[] splitRegions = existingRegion1.split(rangeToRemoveStartOffset.advance(rangeToRemoveSize));

		testRemoveRegionsInRange(defaultCacheBuilder, 0, keepAtBack, maxCacheRegionSize, bytesActuallyRemoved,
			new MediumRegion(rangeToRemoveStartOffset, rangeToRemoveSize), splitRegions[1]);
	}

	/**
	 * Calls {@link MediumCache#addRegion(MediumRegion)} and checks its outcomes.
	 * Firstly, after the call, the cache invariants must still be in place. Second,
	 * the expected regions and nothing else must be present, and third, the cache
	 * size must have changed correspondingly.
	 *
	 * @param cache                The test object
	 * @param regionToAdd          The region to add to the cache
	 * @param expectedCacheBuilder A {@link TestCacheBuilder} containing the cache
	 *                             expectations.
	 */
	private void testAddRegion_expectedRegionsArePresentSizeChangedAndInvariantsAreFullfilled(MediumCache cache,
		MediumRegion regionToAdd, TestCacheBuilder expectedCacheBuilder) {

		cache.addRegion(regionToAdd);

		assertCacheInvariantsAreFulfilled(cache);

		long actualNewSize = cache.calculateCurrentCacheSizeInBytes();

		List<MediumRegion> expectedRegions = expectedCacheBuilder.getAllCachedRegions();

		Assert.assertEquals(MediumCacheTest.getTotalRegionSize(expectedRegions), actualNewSize);

		List<MediumRegion> allRegions = cache.getAllCachedRegions();

		Assert.assertEquals(expectedRegions, allRegions);
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)} for a range
	 * that exactly fully covers a list of expected {@link MediumRegion}s.
	 *
	 * @param cacheLayout        The {@link TestCacheBuilder} to use containing
	 *                           details about the cache structure
	 * @param maxCacheRegionSize Allows to pass in a custom maximum cache region
	 *                           size to use in the test.
	 * @param expectedRegions    The {@link MediumRegion}s expected to be returned
	 *                           by
	 *                           {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 *                           The input range when calling this method is derived
	 *                           from this list, its start reference is the
	 *                           {@link MediumOffset} of the first region in the
	 *                           list, and its size is the total size of all regions
	 *                           in the list.
	 */
	private void testGetRegionsInRange_fullyCoveringExpectedRegions(TestCacheBuilder cacheLayout,
		int maxCacheRegionSize, MediumRegion... expectedRegions) {
		testGetRegionsInRange_notFullyCoveringExpectedRegions(cacheLayout, 0, 0, maxCacheRegionSize, expectedRegions);
	}

	/**
	 * Tests {@link MediumCache#getRegionsInRange(MediumOffset, int)} for a range
	 * that exactly fully covers a list of expected {@link MediumRegion}s.
	 *
	 * @param cacheBuilder                   The {@link TestCacheBuilder} to use
	 *                                       containing details about the cache
	 *                                       structure
	 * @param firstRegionAddSizeToStart      This is added to the start offset for
	 *                                       calling
	 *                                       {@link MediumCache#getRegionsInRange(MediumOffset, int)}
	 * @param lastRegionSubstractSizeFromEnd This is substracted from the size given
	 *                                       to
	 *                                       {@link MediumCache#getRegionsInRange(MediumOffset, int)}
	 * @param maxCacheRegionSize             Allows to pass in a custom maximum
	 *                                       cache region size to use in the test.
	 * @param expectedRegions                The {@link MediumRegion}s expected to
	 *                                       be returned by
	 *                                       {@link MediumCache#getRegionsInRange(MediumOffset, int)}.
	 *                                       The input range when calling this
	 *                                       method is derived from this list, its
	 *                                       start reference is the
	 *                                       {@link MediumOffset} of the first
	 *                                       region in the list advanced by
	 *                                       "firstRegionAddSizeToStart" bytes, and
	 *                                       its size is the total size of all
	 *                                       regions in the list minus
	 *                                       "firstRegionAddSizeToStart" minus
	 *                                       "lastRegionSubstractSizeFromEnd".
	 */
	private void testGetRegionsInRange_notFullyCoveringExpectedRegions(TestCacheBuilder cacheBuilder,
		int firstRegionAddSizeToStart, int lastRegionSubstractSizeFromEnd, int maxCacheRegionSize,
		MediumRegion... expectedRegions) {
		Reject.ifNegative(firstRegionAddSizeToStart, "firstRegionAddSizeToStart");
		Reject.ifNegative(lastRegionSubstractSizeFromEnd, "lastRegionSubstractSizeFromEnd");

		List<MediumRegion> expectedRegionList = Arrays.asList(expectedRegions);

		MediumOffset rangeStartReference = expectedRegionList.get(0).getStartOffset();

		int totalRegionSize = MediumCacheTest.getTotalRegionSize(expectedRegionList);

		MediumCache cache = cacheBuilder.buildCache(MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

		List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(
			rangeStartReference.advance(firstRegionAddSizeToStart), totalRegionSize - lastRegionSubstractSizeFromEnd);

		Assert.assertEquals(expectedRegionList, actualRegionsInRange);
	}

	/**
	 * @param cacheBuilder                   The {@link TestCacheBuilder} to use
	 *                                       containing details about the cache
	 *                                       structure
	 * @param firstRegionAddSizeToStart      This is added to the start offset for
	 *                                       calling
	 *                                       {@link MediumCache#removeRegionsInRange(MediumOffset, int)}
	 * @param lastRegionSubstractSizeFromEnd This is subtracted from the size given
	 *                                       to
	 *                                       {@link MediumCache#removeRegionsInRange(MediumOffset, int)}
	 * @param maxCacheRegionSize             Allows to pass in a custom maximum
	 *                                       cache region size to use in the test.
	 * @param totalByteCountRemoved          Total number of bytes actually removed
	 * @param expectedRegions                The {@link MediumRegion}s expected to
	 *                                       be returned by
	 *                                       {@link MediumCache#getRegionsInRange(MediumOffset, int)}
	 *                                       after the removal. The input range when
	 *                                       calling this method is derived from
	 *                                       this list, its start reference is the
	 *                                       {@link MediumOffset} of the first
	 *                                       region in the list advanced by
	 *                                       "firstRegionAddSizeToStart" bytes, and
	 *                                       its size is the total size of all
	 *                                       regions in the list minus
	 *                                       "firstRegionAddSizeToStart" minus
	 *                                       "lastRegionSubstractSizeFromEnd".
	 */
	private void testRemoveRegionsInRange(TestCacheBuilder cacheBuilder, int firstRegionAddSizeToStart,
		int lastRegionSubstractSizeFromEnd, int maxCacheRegionSize, long totalByteCountRemoved,
		MediumRegion... expectedRegions) {

		List<MediumRegion> expectedRegionList = Arrays.asList(expectedRegions);

		MediumOffset rangeStartReference = expectedRegionList.get(0).getStartOffset();

		int totalRegionSize = MediumCacheTest.getTotalRegionSize(expectedRegionList);

		MediumCache cache = cacheBuilder.buildCache(MediumCache.UNLIMITED_CACHE_SIZE, maxCacheRegionSize);

		long cacheSizeBeforeRemove = cache.calculateCurrentCacheSizeInBytes();

		cache.removeRegionsInRange(rangeStartReference.advance(firstRegionAddSizeToStart),
			totalRegionSize - firstRegionAddSizeToStart - lastRegionSubstractSizeFromEnd);

		Assert.assertEquals(cache.calculateCurrentCacheSizeInBytes(), cacheSizeBeforeRemove - totalByteCountRemoved);

		List<MediumRegion> actualRegionsInRange = cache.getRegionsInRange(rangeStartReference, totalRegionSize);

		Assert.assertEquals(expectedRegionList, actualRegionsInRange);
	}
}
