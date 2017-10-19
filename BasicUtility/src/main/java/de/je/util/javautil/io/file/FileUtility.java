package de.je.util.javautil.io.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link FileUtility} contains static utility functionality for handling {@link File}s.
 */
public class FileUtility {

   /**
    * Copies a {@link File} to another place.
    *
    * @param src
    *           The source {@link File} to copy. Must exist and must point to a {@link File} rather than a directory.
    * @param dst
    *           The destination {@link File} to copy to. Must not exist.
    *
    * @throws IOException
    *            if any file system operation fails.
    *
    * @return true if the copy action succeeded, false if it didn't.
    */
   public static boolean copyFile(File src, File dst) throws IOException {
      Reject.ifNull(dst, "dst");
      Reject.ifNull(src, "src");
      Reject.ifTrue(!src.exists(), "!src.exists()");
      Reject.ifTrue(dst.exists(), "dst.exists()");

      if (!dst.createNewFile())
         return false;

      try (FileInputStream srcStream = new FileInputStream(src);
         FileChannel srcChannel = srcStream.getChannel();
         FileOutputStream dstStream = new FileOutputStream(dst);
         FileChannel dstChannel = dstStream.getChannel()) {
         dstChannel.transferFrom(srcChannel, 0, srcChannel.size());
      }

      return true;
   }
}