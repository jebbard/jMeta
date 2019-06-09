/**
 *
 * {@link ITagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.api.services;

import java.io.RandomAccessFile;

import com.github.jmeta.tools.tagfinder.api.types.TagInfo;

/**
 * {@link ITagSearcher}
 *
 */
public interface ITagSearcher {

	TagInfo getTagInfo(RandomAccessFile file);

	String getTagName();
}
