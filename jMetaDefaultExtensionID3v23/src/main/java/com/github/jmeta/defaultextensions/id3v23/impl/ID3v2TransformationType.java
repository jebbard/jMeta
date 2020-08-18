/**
 * {@link ID3v2TransformationType}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:07 (December 31, 2010)
 */

package com.github.jmeta.defaultextensions.id3v23.impl;

/**
 *
 */
public enum ID3v2TransformationType {

	UNSYNCHRONIZATION(0, 0), COMPRESSION(1, 1), ENCRYPTION(2, 2);

	private int m_readOrder;

	private int m_writeOrder;

	private ID3v2TransformationType(int readOrder, int writeOrder) {
		m_readOrder = readOrder;
		m_writeOrder = writeOrder;
	}

	/**
	 * @return the read order
	 */
	public int getReadOrder() {

		return m_readOrder;
	}

	/**
	 * @return the write order
	 */
	public int getWriteOrder() {

		return m_writeOrder;
	}
}
