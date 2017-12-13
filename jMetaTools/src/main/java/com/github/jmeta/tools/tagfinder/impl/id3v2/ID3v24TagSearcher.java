/**
 *
 * {@link ID3v24TailTagSearcher}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.impl.id3v2;

import java.nio.ByteBuffer;

/**
 * {@link ID3v24TagSearcher}
 *
 */
public class ID3v24TagSearcher extends ID3v23TagSearcher {

   /**
    * Creates a new {@link ID3v22TagSearcher}.
    */
   public ID3v24TagSearcher() {
      super(new byte[] { 'I', 'D', '3', 4, 0 }, new long[] { 0 }, "ID3v2.4");
   }

   public ID3v24TagSearcher(byte[] magicKey, long[] possibleOffsets, String tagName) {
      super(magicKey, possibleOffsets, tagName);
   }

   /**
    * @see com.github.jmeta.tools.tagfinder.api.services.AbstractMagicKeyTagSearcher#getAdditionalInfo(java.nio.ByteBuffer)
    */
   @Override
   protected String[] getAdditionalInfo(ByteBuffer tagBytes) {

      ID3v2TagHeaderInfo info = getTagHeaderInfo(tagBytes);

      String[] additionalInfo = super.getAdditionalInfo(tagBytes);

      String footer = "Has footer: " + ((info.getFlags() & MASK_FOURTH_BIT) != 0);

      String[] returnedInfo = new String[1 + additionalInfo.length];

      returnedInfo[0] = footer;

      for (int i = 0; i < additionalInfo.length; i++)
         returnedInfo[i + 1] = additionalInfo[i];

      return returnedInfo;
   }

   protected String getFlagSummary(ID3v2FrameInfo frameInfo) {

      final boolean isTagAlterPreservation = (frameInfo.getFlags()[0] & MASK_FIRST_BIT) != 0;
      final boolean isFileAlterPreservation = (frameInfo.getFlags()[0] & MASK_SECOND_BIT) != 0;
      final boolean isReadOnly = (frameInfo.getFlags()[0] & MASK_THIRD_BIT) != 0;
      final boolean isCompressed = (frameInfo.getFlags()[1] & MASK_SECOND_BIT) != 0;
      final boolean isGrouped = (frameInfo.getFlags()[1] & 8) != 0;
      final boolean isEncrypted = (frameInfo.getFlags()[1] & 4) != 0;
      final boolean isUnsynchronised = (frameInfo.getFlags()[1] & 2) != 0;
      final boolean hasDataLengthIndicator = (frameInfo.getFlags()[1] & 1) != 0;

      return ", Flags: (tag alter preservation = " + isTagAlterPreservation + "; file alter preservation = "
         + isFileAlterPreservation + "; read-only = " + isReadOnly + "; compressed = " + isCompressed + "; encrypted = "
         + isEncrypted + "; grouped = " + isGrouped + "; unsynchronised = " + isUnsynchronised
         + "; data length indicator = " + hasDataLengthIndicator + ")";
   }
}
