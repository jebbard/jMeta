/**
 *
 * {@link ID3v22TagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package de.je.jmeta.testtools.tagfinder.id3v2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ID3v22TagSearcher}
 *
 */
public class ID3v22TagSearcher extends AbstractID3v2TagSearcher {

   /**
    * Creates a new {@ID3v22TagSearcher}.
    * 
    * @param magicKey
    * @param possibleOffsets
    * @param tagName
    */
   public ID3v22TagSearcher() {
      super(new byte[] { 'I', 'D', '3', 2, 0 }, new long[] { 0 }, "ID3v2.2");
   }

   /**
    * @see de.je.jmeta.testtools.tagfinder.AbstractMagicKeyTagSearcher#getAdditionalInfo(java.nio.ByteBuffer)
    */
   @Override
   protected String[] getAdditionalInfo(ByteBuffer tagBytes) {

      ID3v2TagHeaderInfo info = getTagHeaderInfo(tagBytes);

      final boolean isUnsynchronised = (info.getFlags() & 128) != 0;
      final boolean isCompressed = (info.getFlags() & 64) != 0;

      List<String> additionInfoList = new ArrayList<String>(4);

      additionInfoList.add("Is unsynchronized: " + isUnsynchronised);
      additionInfoList.add("Is compressed: " + isCompressed);

      // Read each frame of the tag, if thats easy (no ext header and not unsynchr.)
      if (!isUnsynchronised && !isCompressed) {
         tagBytes.position(ID3V2_TAG_HEADER_SIZE);

         while (tagBytes.hasRemaining()) {
            tagBytes.mark();

            byte nextByte = tagBytes.get();

            // Padding is encountered
            if (nextByte == 0)
               break;

            // "unread" the previously read byte
            tagBytes.reset();

            ID3v2FrameInfo frameInfo = getFrameInfo(tagBytes);

            tagBytes.position(tagBytes.position() + frameInfo.getSize());

            additionInfoList.add(frameInfo.toString());
         }
      }

      String[] returnedArray = new String[additionInfoList.size()];

      return additionInfoList.toArray(returnedArray);
   }

   private ID3v2FrameInfo getFrameInfo(ByteBuffer tagBytes) {

      byte[] idBytes = new byte[3];
      byte[] size = new byte[4];

      tagBytes.get(idBytes);

      for (int i = 0; i < size.length - 1; i++) {
         size[i + 1] = tagBytes.get();
      }

      return new ID3v2FrameInfo(idBytes, ByteBuffer.wrap(size).getInt(),
         new byte[] {});
   }
}
