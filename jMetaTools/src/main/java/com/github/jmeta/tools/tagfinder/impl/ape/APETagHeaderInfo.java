package com.github.jmeta.tools.tagfinder.impl.ape;

public class APETagHeaderInfo {

	private final long m_preample;

	private final int m_version;

	private final int m_tagSize;

	private final int m_itemCount;

	private final byte[] m_flags;

	public APETagHeaderInfo(long preample, int version, int tagSize, int itemCount, byte[] flags) {
		m_preample = preample;
		m_version = version;
		m_tagSize = tagSize;
		m_itemCount = itemCount;
		m_flags = flags;
	}

	/**
	 * Returns flags
	 *
	 * @return flags
	 */
	public byte[] getFlags() {

		return m_flags;
	}

	/**
	 * Returns itemCount
	 *
	 * @return itemCount
	 */
	public int getItemCount() {

		return m_itemCount;
	}

	/**
	 * Returns preample
	 *
	 * @return preample
	 */
	public long getPreample() {

		return m_preample;
	}

	/**
	 * Returns tagSize
	 *
	 * @return tagSize
	 */
	public int getTagSize() {

		return m_tagSize;
	}

	/**
	 * Returns version
	 *
	 * @return version
	 */
	public int getVersion() {

		return m_version;
	}
}
