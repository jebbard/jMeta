/**
 *
 * {@link ID3v24TailTagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.impl.id3v2;

/**
 * {@link ID3v24TailTagSearcher}
 *
 */
public class ID3v24TailTagSearcher extends ID3v24TagSearcher {

	/**
	 * Creates a new {@link ID3v24TailTagSearcher}.
	 */
	public ID3v24TailTagSearcher() {
		super(new byte[] { '3', 'D', 'I', 4, 0 }, new long[] { -10, -138 }, "ID3v2.4 (Back of File)");
	}
}
