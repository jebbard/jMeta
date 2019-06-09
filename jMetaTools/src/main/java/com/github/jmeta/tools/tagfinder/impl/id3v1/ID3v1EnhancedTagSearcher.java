/**
 *
 * {@link ID3v1EnhancedTagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.impl.id3v1;

import java.io.RandomAccessFile;

import com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher;

/**
 * {@link ID3v1EnhancedTagSearcher}
 *
 */
public class ID3v1EnhancedTagSearcher extends AbstractMagicKeyTagSearcher {

	private static final int ID3V1_ENHANCED_TAG_SIZE = 227;

	/**
	 * Creates a new {@link ID3v1EnhancedTagSearcher}.
	 */
	public ID3v1EnhancedTagSearcher() {
		super(new byte[] { 'T', 'A', 'G', '+' },
			new long[] { -AbstractID3v1TagSearcher.ID3V1_TAG_SIZE - ID3v1EnhancedTagSearcher.ID3V1_ENHANCED_TAG_SIZE },
			"Enhanced ID3v1");
	}

	/**
	 * @see com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher#getTotalTagSize(java.io.RandomAccessFile,
	 *      long)
	 */
	@Override
	protected int getTotalTagSize(RandomAccessFile file, long possibleOffset) {

		return ID3v1EnhancedTagSearcher.ID3V1_ENHANCED_TAG_SIZE;
	}
}
