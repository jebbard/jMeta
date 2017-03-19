/**
 *
 * {@link ITagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package de.je.jmeta.testtools.tagfinder;

import java.io.RandomAccessFile;

/**
 * {@link ITagSearcher}
 *
 */
public interface ITagSearcher {

   public String getTagName();

   public TagInfo getTagInfo(RandomAccessFile file);
}
