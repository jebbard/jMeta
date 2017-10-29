/**
 *
 * {@link ID3v22TagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.impl.id3v2;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ID3v23TagSearcher}
 *
 */
public class ID3v23TagSearcher extends AbstractID3v2TagSearcher {

   private static final int ID3V23_FLAGS_LENGTH = 2;

   private static final int ID3V23_ID_LENGTH = 4;

   protected static final int MASK_THIRD_BIT = 32;

   protected static final int MASK_SECOND_BIT = 64;

   protected static final int MASK_FIRST_BIT = 128;

   private static final int ID3V23_FRAME_HEADER_SIZE = 10;

   protected static final int MASK_FOURTH_BIT = 16;

   public ID3v23TagSearcher(byte[] magicKey, long[] possibleOffsets,
      String tagName) {
      super(magicKey, possibleOffsets, tagName);
   }

   /**
    * Creates a new {@ID3v22TagSearcher}.
    * 
    * @param magicKey
    * @param possibleOffsets
    * @param tagName
    */
   public ID3v23TagSearcher() {
      super(new byte[] { 'I', 'D', '3', 3, 0 }, new long[] { 0 }, "ID3v2.3");
   }

   /**
    * @see com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher#getAdditionalInfo(java.nio.ByteBuffer)
    */
   @Override
   protected String[] getAdditionalInfo(ByteBuffer tagBytes) {

      ID3v2TagHeaderInfo info = getTagHeaderInfo(tagBytes);

      final boolean isUnsynchronised = (info.getFlags() & MASK_FIRST_BIT) != 0;
      final boolean hasExtendedHeader = (info.getFlags()
         & MASK_SECOND_BIT) != 0;
      final boolean isExperimental = (info.getFlags() & MASK_THIRD_BIT) != 0;

      List<String> additionInfoList = new ArrayList<String>(4);

      additionInfoList.add("Is unsynchronized: " + isUnsynchronised);
      additionInfoList.add("Has extended header: " + hasExtendedHeader);
      additionInfoList.add("Is experimental: " + isExperimental);

      // Read each frame of the tag, if thats easy (no ext header and not unsynchr.)
      if (!isUnsynchronised && !hasExtendedHeader) {
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

            additionInfoList
               .add(frameInfo.toString() + getFlagSummary(frameInfo));
         }
      }

      String[] returnedArray = new String[additionInfoList.size()];

      return additionInfoList.toArray(returnedArray);
   }

   protected String getFlagSummary(ID3v2FrameInfo frameInfo) {

      final boolean isTagAlterPreservation = (frameInfo.getFlags()[0]
         & MASK_FIRST_BIT) != 0;
      final boolean isFileAlterPreservation = (frameInfo.getFlags()[0]
         & MASK_SECOND_BIT) != 0;
      final boolean isReadOnly = (frameInfo.getFlags()[0]
         & MASK_THIRD_BIT) != 0;
      final boolean isCompressed = (frameInfo.getFlags()[1]
         & MASK_FIRST_BIT) != 0;
      final boolean isEncrypted = (frameInfo.getFlags()[1]
         & MASK_SECOND_BIT) != 0;
      final boolean isGrouped = (frameInfo.getFlags()[1] & MASK_THIRD_BIT) != 0;

      return ", Flags: (tag alter preservation = " + isTagAlterPreservation
         + "; file alter preservation = " + isFileAlterPreservation
         + "; read-only = " + isReadOnly + "; compressed = " + isCompressed
         + "; encrypted = " + isEncrypted + "; grouped = " + isGrouped + ")";
   }

   /**
    * @param tagBytes
    */
   private ID3v2FrameInfo getFrameInfo(ByteBuffer tagBytes) {

      byte[] idBytes = new byte[ID3V23_ID_LENGTH];
      byte[] flags = new byte[ID3V23_FLAGS_LENGTH];

      tagBytes.get(idBytes);
      int size = tagBytes.getInt();
      tagBytes.get(flags);

      return new ID3v2FrameInfo(idBytes, size, flags);
   }

}
