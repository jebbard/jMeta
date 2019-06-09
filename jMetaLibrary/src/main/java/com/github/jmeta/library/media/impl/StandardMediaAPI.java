/**
 *
 * {@link StandardMediaAPI}.java
 *
 * @author Jens
 *
 * @date 18.05.2015
 *
 */
package com.github.jmeta.library.media.impl;

import java.util.HashSet;
import java.util.Set;

import com.github.jmeta.library.media.api.services.MediaAPI;
import com.github.jmeta.library.media.api.services.MediumStore;
import com.github.jmeta.library.media.api.types.FileMedium;
import com.github.jmeta.library.media.api.types.InMemoryMedium;
import com.github.jmeta.library.media.api.types.InputStreamMedium;
import com.github.jmeta.library.media.api.types.Medium;
import com.github.jmeta.library.media.impl.cache.MediumCache;
import com.github.jmeta.library.media.impl.changeManager.MediumChangeManager;
import com.github.jmeta.library.media.impl.mediumAccessor.FileMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.InMemoryMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.InputStreamMediumAccessor;
import com.github.jmeta.library.media.impl.mediumAccessor.MediumAccessor;
import com.github.jmeta.library.media.impl.offset.MediumOffsetFactory;
import com.github.jmeta.library.media.impl.store.StandardMediumStore;
import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link StandardMediaAPI} is the default implementation of the
 * {@link MediaAPI} interface.
 */
public class StandardMediaAPI implements MediaAPI {

	private static final Set<Class<? extends Medium<?>>> SUPPORTED_MEDIA_CLASSES = new HashSet<>();

	static {
		StandardMediaAPI.SUPPORTED_MEDIA_CLASSES.add(FileMedium.class);
		StandardMediaAPI.SUPPORTED_MEDIA_CLASSES.add(InMemoryMedium.class);
		StandardMediaAPI.SUPPORTED_MEDIA_CLASSES.add(InputStreamMedium.class);
	}

	private long minimumCacheSize = MediumStore.MINIMUM_CACHE_SIZE_IN_BYTES;
	private long minimumReadWriteBlockSize = MediumStore.MINIMUM_READ_WRITE_BLOCK_SIZE_IN_BYTES;

	/**
	 * @see com.github.jmeta.library.media.api.services.MediaAPI#createMediumStore(com.github.jmeta.library.media.api.types.Medium)
	 */
	@SuppressWarnings("unlikely-arg-type")
	@Override
	public MediumStore createMediumStore(Medium<?> medium) {
		Reject.ifNull(medium, "medium");

		Reject.ifFalse(StandardMediaAPI.SUPPORTED_MEDIA_CLASSES.contains(medium.getClass()),
			"SUPORTED_MEDIA_CLASSES.contains(medium.getClass())");

		Reject.ifTrue(medium.getMaxReadWriteBlockSizeInBytes() < minimumReadWriteBlockSize,
			"The maximum read-write block size must be at least " + minimumReadWriteBlockSize);
		Reject.ifTrue(medium.getMaxCacheSizeInBytes() < minimumCacheSize,
			"The maximum cache size must be at least " + minimumCacheSize);
		Reject.ifTrue(medium.getMaxCacheSizeInBytes() < (2 * medium.getMaxReadWriteBlockSizeInBytes()),
			"The maximum cache size must at least be twice the maximum read-write block size");

		MediumAccessor<?> mediumAccessor = null;

		if (medium.getClass() == FileMedium.class) {
			mediumAccessor = new FileMediumAccessor((FileMedium) medium);
		} else if (medium.getClass() == InMemoryMedium.class) {
			mediumAccessor = new InMemoryMediumAccessor((InMemoryMedium) medium);
		} else if (medium.getClass() == InputStreamMedium.class) {
			mediumAccessor = new InputStreamMediumAccessor((InputStreamMedium) medium);
		}

		MediumOffsetFactory offsetFactory = new MediumOffsetFactory(medium);

		long maxCacheSizeToUse = medium.getMaxCacheSizeInBytes();
		if (maxCacheSizeToUse < minimumCacheSize) {
			throw new IllegalArgumentException("Invalid maximum cache size <" + maxCacheSizeToUse
				+ ">given, it must be at least <" + minimumCacheSize + "> bytes.");
		}

		return new StandardMediumStore<>(mediumAccessor,
			new MediumCache(medium, maxCacheSizeToUse, medium.getMaxReadWriteBlockSizeInBytes()), offsetFactory,
			new MediumChangeManager(offsetFactory));
	}

	/**
	 * Allows test cases to manipulate the minimum cache size to even smaller values
	 * than {@link MediumStore#MINIMUM_CACHE_SIZE_IN_BYTES} (the default).
	 *
	 * @param minimumCacheSize The new minimum cache size to set.
	 */
	public void setMinimumCacheSize(long minimumCacheSize) {
		this.minimumCacheSize = minimumCacheSize;
	}

	/**
	 * Allows test cases to manipulate the minimum cache size to even smaller values
	 * than {@link MediumStore#MINIMUM_READ_WRITE_BLOCK_SIZE_IN_BYTES} (the
	 * default).
	 *
	 * @param minimumReadWriteBlockSize The new minimum read-write block size.
	 */
	public void setMinimumReadWriteBlockSize(long minimumReadWriteBlockSize) {
		this.minimumReadWriteBlockSize = minimumReadWriteBlockSize;
	}
}
