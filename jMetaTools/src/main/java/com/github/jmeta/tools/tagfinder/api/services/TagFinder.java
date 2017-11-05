/**
 *
 * {@link TagFinder}.java
 *
 * @author jebert
 *
 * @date 05.02.2011
 */
package com.github.jmeta.tools.tagfinder.api.services;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

import com.github.jmeta.tools.tagfinder.api.types.TagFinderFormatter;
import com.github.jmeta.tools.tagfinder.api.types.TagInfo;
import com.github.jmeta.tools.tagfinder.impl.ape.APEv1TagSearcher;
import com.github.jmeta.tools.tagfinder.impl.ape.APEv2TagSearcher;
import com.github.jmeta.tools.tagfinder.impl.id3v1.ID3v11TagSearcher;
import com.github.jmeta.tools.tagfinder.impl.id3v1.ID3v1EnhancedTagSearcher;
import com.github.jmeta.tools.tagfinder.impl.id3v1.ID3v1TagSearcher;
import com.github.jmeta.tools.tagfinder.impl.id3v2.ID3v22TagSearcher;
import com.github.jmeta.tools.tagfinder.impl.id3v2.ID3v23TagSearcher;
import com.github.jmeta.tools.tagfinder.impl.id3v2.ID3v24TagSearcher;
import com.github.jmeta.tools.tagfinder.impl.id3v2.ID3v24TailTagSearcher;
import com.github.jmeta.tools.tagfinder.impl.lyrics3.Lyrics3v1TagSearcher;
import com.github.jmeta.tools.tagfinder.impl.lyrics3.Lyrics3v2TagSearcher;
import com.github.jmeta.utility.dbc.api.services.Reject;

// TODO tagFind002: Document TagFinder
// TODO tagFind003: Implement TagFinder with multithreading

/**
 * {@link TagFinder}
 *
 */
public class TagFinder {

   /**
    * @param args
    */
   public static void main(String[] args) {

      if (args.length != EXPECTED_ARG_COUNT)
         throw new IllegalArgumentException(
            "Usage: TagFinder <list of semicolon separated root folders> <target tag folder> <list of semicolon separated file extensions>");

      String[] semicolonSeparatedFiles = args[0].split(ARGUMENT_SEPARATOR);
      String targetTagFolder = args[1];
      String[] semicolonSeparatedExtensions = args[2].split(ARGUMENT_SEPARATOR);

      TagFinder finder = new TagFinder(semicolonSeparatedFiles, semicolonSeparatedExtensions,
         new File(targetTagFolder));

      finder.runTagFinding();
   }

   public TagFinder(String[] rootFolders, String[] fileExtensions, File targetTagFolder) {
      Reject.ifNull(fileExtensions, "fileExtensions");
      Reject.ifNull(rootFolders, "rootFolder");
      Reject.ifNull(targetTagFolder, "targetTagFolder");
      Reject.ifFalse(targetTagFolder.exists(), "The given target folder " + targetTagFolder + " must exist!");
      Reject.ifFalse(targetTagFolder.isDirectory(),
         "The given target folder file " + targetTagFolder + " must be a directory!");

      m_logger = setupLogger(targetTagFolder);

      Set<File> rootFolderSet = new HashSet<File>(rootFolders.length);

      for (int i = 0; i < rootFolders.length; i++) {
         File nextRootFolder = new File(rootFolders[i]);
         Reject.ifFalse(nextRootFolder.exists(), "The given root folder " + nextRootFolder + " must exist!");
         Reject.ifFalse(nextRootFolder.isDirectory(),
            "The given root folder file " + nextRootFolder + " must be a directory!");

         rootFolderSet.add(nextRootFolder);
      }

      m_targetTagFolder = targetTagFolder;
      m_rootFolders = rootFolderSet;
      m_fileExtensions = fileExtensions;

      for (int i = 0; i < TAG_SEARCH_LIST.length; i++) {
         m_foundTagCounts.put(TAG_SEARCH_LIST[i].getTagName(), Integer.valueOf(0));
      }
   }

   private Logger setupLogger(File targetTagFolder) {

      Logger logger = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);

      try {
         final Handler fileHandler = new FileHandler(
            targetTagFolder.getAbsolutePath() + System.getProperty("file.separator") + "tagSearch_%g.log", false);
         final Formatter formatter = new TagFinderFormatter();
         final Handler sysOutHandler = new StreamHandler(System.out, formatter);

         fileHandler.setFormatter(formatter);

         logger.setUseParentHandlers(false);
         logger.addHandler(sysOutHandler);
         logger.addHandler(fileHandler);
      } catch (Exception e) {
         throw new RuntimeException("Could not initialize logger", e);
      }

      return logger;
   }

   /**
    */
   public void runTagFinding() {

      m_previousRunStartTime = System.currentTimeMillis();

      printIntroMessage(m_fileExtensions);

      Set<String> extensionList = new HashSet<String>(Arrays.asList(m_fileExtensions));

      List<File> fileList = getFileList(m_rootFolders, extensionList);

      if (fileList.size() == 0)
         m_logger.info("0 files found in root folder <" + m_rootFolders + ">. TagFinder stops without action.");

      int totalTagCount = printTagSearching(fileList);

      printOutroMessage(m_fileExtensions, fileList, totalTagCount);
   }

   private List<File> getFileList(Set<File> rootFolders, Set<String> extensionList) {

      List<File> fileList = new ArrayList<File>();

      for (Iterator<File> iterator = rootFolders.iterator(); iterator.hasNext();) {
         File nextRootFolder = iterator.next();

         addFilesInDirectory(nextRootFolder, fileList, extensionList);
      }

      return fileList;
   }

   private void addFilesInDirectory(File dir, List<File> fileList, Set<String> extensionList) {

      Reject.ifFalse(dir.isDirectory(), "File " + dir + " must be a directory.");

      File[] files = dir.listFiles();

      for (int i = 0; i < files.length; i++) {
         File nextFile = files[i];

         if (nextFile.isDirectory() && !nextFile.isHidden()) {
            addFilesInDirectory(nextFile, fileList, extensionList);
         }

         else {
            if (extensionList.contains(ANY_WILDCARD))
               fileList.add(nextFile);

            else {
               String[] splittedBySeparator = nextFile.getName().split(FILE_EXTENSION_SEPARATOR);

               if (splittedBySeparator.length == 0)
                  continue;

               String extension = splittedBySeparator[splittedBySeparator.length - 1];

               if (extensionList.contains(extension))
                  fileList.add(nextFile);
            }
         }
      }
   }

   private int performTagSearch(File nextFile, String fileId) {

      int totalTagCount = 0;

      try {
         RandomAccessFile raf = new RandomAccessFile(nextFile, "r");

         for (int i = 0; i < TAG_SEARCH_LIST.length; ++i) {
            ITagSearcher tagSearcher = TAG_SEARCH_LIST[i];

            final TagInfo tagInfo = tagSearcher.getTagInfo(raf);

            if (tagInfo != null) {
               final String[] additionalTagProperties = tagInfo.getAdditionalTagProperties();

               m_logger.info(COLUMN_SPACE + "----------------- TAG found!!! -----------------");
               m_logger.info(COLUMN_SPACE + COLUMN_SPACE + "Tag type: <" + tagSearcher.getTagName() + ">");
               m_logger.info(COLUMN_SPACE + COLUMN_SPACE + "Tag absolute byte offset in file: <"
                  + tagInfo.getAbsoluteOffset() + ">");
               m_logger
                  .info(COLUMN_SPACE + COLUMN_SPACE + "Tag declared byte size: <" + tagInfo.getDeclaredTagSize() + ">");

               if (additionalTagProperties.length > 0) {
                  m_logger.info(COLUMN_SPACE + COLUMN_SPACE + "Tag additional information list: ");

                  for (int j = 0; j < additionalTagProperties.length; j++) {
                     m_logger.info(COLUMN_SPACE + COLUMN_SPACE + COLUMN_SPACE + "" + additionalTagProperties[j]);
                  }
               }

               int tagCount = m_foundTagCounts.get(tagSearcher.getTagName());

               m_foundTagCounts.put(tagSearcher.getTagName(), ++tagCount);

               totalTagCount++;

               addTagContentToFile(tagSearcher.getTagName(), tagInfo, fileId, nextFile.getAbsolutePath());
            }
         }

         raf.close();
      }

      catch (IOException e) {
         throw new IllegalStateException("IO Exception: ", e);
      }

      return totalTagCount;
   }

   /**
    * @param fileExtensions
    */
   private void printIntroMessage(String[] fileExtensions) {

      m_logger.info(TAG_FINDER_LOG_MARK);
      m_logger.info(TAG_FINDER_LOG_MARK);
      m_logger.info(LINE_SEPARATOR);
      m_logger.info(COLUMN_SPACE + "Starting TagFinder with root folder:");
      m_logger.info(COLUMN_SPACE + COLUMN_SPACE + "<" + m_rootFolders + ">");
      m_logger.info(COLUMN_SPACE + "and target tag folder:");
      m_logger.info(COLUMN_SPACE + COLUMN_SPACE + "<" + m_targetTagFolder + ">");
      m_logger.info(COLUMN_SPACE + "and file extensions: ");
      m_logger.info(COLUMN_SPACE + COLUMN_SPACE + "<" + Arrays.toString(fileExtensions) + ">.");
      m_logger.info(LINE_SEPARATOR);
      m_logger.info(TAG_FINDER_LOG_MARK);
      m_logger.info(TAG_FINDER_LOG_MARK);
   }

   /**
    * @param fileList
    * @return
    */
   private int printTagSearching(List<File> fileList) {

      int totalTagCount = 0;

      for (int i = 0; i < fileList.size(); ++i) {
         File nextFile = fileList.get(i);

         DecimalFormat fileIdFormat = new DecimalFormat("000000000");

         String fileId = fileIdFormat.format(i);

         m_logger.info(LINE_SEPARATOR);
         m_logger.info("****************************************************");
         m_logger.info("Scanning file...");
         m_logger.info(nextFile.getAbsolutePath());
         m_logger.info("File Id: " + fileId);
         m_logger.info(LINE_SEPARATOR);

         totalTagCount += performTagSearch(nextFile, fileId);
      }

      return totalTagCount;
   }

   /**
    * @param fileExtensions
    * @param fileList
    * @param totalTagCount
    */
   private void printOutroMessage(String[] fileExtensions, List<File> fileList, int totalTagCount) {

      m_logger.info(LINE_SEPARATOR);
      m_logger.info(TAG_FINDER_LOG_MARK);
      m_logger.info(TAG_FINDER_LOG_MARK);
      m_logger.info(COLUMN_SPACE + "Finished TagFinder with root folder:");
      m_logger.info(COLUMN_SPACE + COLUMN_SPACE + "<" + m_rootFolders + ">");
      m_logger.info(COLUMN_SPACE + "and target tag folder:");
      m_logger.info(COLUMN_SPACE + COLUMN_SPACE + "<" + m_targetTagFolder + ">");
      m_logger.info(COLUMN_SPACE + "and file extensions: ");
      m_logger.info(COLUMN_SPACE + COLUMN_SPACE + "<" + Arrays.toString(fileExtensions) + ">.");
      m_logger.info(LINE_SEPARATOR);
      m_logger.info("Total number of tags found: " + totalTagCount);
      m_logger.info("Total number of files scanned: " + fileList.size());
      m_logger.info(LINE_SEPARATOR);

      for (Iterator<String> iterator = m_foundTagCounts.keySet().iterator(); iterator.hasNext();) {
         String nextKey = iterator.next();
         Integer nextValue = m_foundTagCounts.get(nextKey);

         m_logger.info(COLUMN_SPACE + "[TAG: " + nextKey + "] - " + nextValue);
      }

      long totalDuration = System.currentTimeMillis() - m_previousRunStartTime;

      m_logger.info(LINE_SEPARATOR);
      m_logger.info("Tag search took " + totalDuration + " milliseconds.");

      long hours = totalDuration / MILLIS_TO_HOURS;
      totalDuration = totalDuration % MILLIS_TO_HOURS;
      long minutes = totalDuration / MILLIS_TO_MINUTES;
      totalDuration = totalDuration % MILLIS_TO_MINUTES;
      long seconds = totalDuration / MILLIS_TO_SECONDS;
      totalDuration = totalDuration % MILLIS_TO_SECONDS;
      long milliseconds = totalDuration;

      m_logger.info("Tag search took " + hours + " hours, " + minutes + " minutes, " + seconds + " seconds and "
         + milliseconds + " milliseconds.");
      m_logger.info(LINE_SEPARATOR);

      m_logger.info(TAG_FINDER_LOG_MARK);
      m_logger.info(TAG_FINDER_LOG_MARK);
   }

   private void addTagContentToFile(String tagName, TagInfo tagInfo, String originalFileId, String originalFileName) {

      final File tagFolder = new File(m_targetTagFolder + PATH_SEPARATOR + tagName);
      File targetFilePath = new File(tagFolder, tagName + ".txt");

      try {
         if (!tagFolder.exists()) {
            if (!tagFolder.mkdir())
               throw new IOException("Folder creation of folder " + tagFolder.getAbsolutePath() + " failed");
         }

         boolean tagFileAlreadyExists = targetFilePath.exists();
         if (!tagFileAlreadyExists) {
            if (!targetFilePath.createNewFile())
               throw new IOException("File creation of file " + targetFilePath.getAbsolutePath() + " failed");
         }

         final FileOutputStream fileOutputStream = new FileOutputStream(targetFilePath, tagFileAlreadyExists);
         BufferedOutputStream targetStream = new BufferedOutputStream(fileOutputStream);

         String fileString = "\n\n<<<<Copied from <" + originalFileId + " - " + originalFileName + ">>>>:\n";
         targetStream.write(fileString.getBytes());
         targetStream.write(tagInfo.getTagBytes());
         targetStream.close();

         m_logger.info(COLUMN_SPACE + "Successfully created copy of tag <" + tagName + "> in file: "
            + targetFilePath.getAbsolutePath());
      } catch (IOException e) {
         throw new IllegalStateException("Unexpected IO exception: " + e, e);
      }
   }

   private final Map<String, Integer> m_foundTagCounts = new LinkedHashMap<String, Integer>();

   private final File m_targetTagFolder;

   private long m_previousRunStartTime;

   private final Set<File> m_rootFolders;

   private String[] m_fileExtensions;

   private static final String COLUMN_SPACE = "    ";

   private static final ITagSearcher[] TAG_SEARCH_LIST = new ITagSearcher[] { new ID3v1TagSearcher(),
      new ID3v11TagSearcher(), new ID3v1EnhancedTagSearcher(), new APEv1TagSearcher(), new APEv2TagSearcher(),
      new ID3v23TagSearcher(), new ID3v22TagSearcher(), new ID3v24TagSearcher(), new ID3v24TailTagSearcher(),
      new Lyrics3v1TagSearcher(), new Lyrics3v2TagSearcher(), };

   private static final String LINE_SEPARATOR = System.getProperty("line.separator");

   private static final String FILE_EXTENSION_SEPARATOR = "\\.";

   private static final String TAG_FINDER_LOG_MARK = "#############################################";

   private static final String ARGUMENT_SEPARATOR = ";";

   private static final String ANY_WILDCARD = "?";

   private static final int EXPECTED_ARG_COUNT = 3;

   private final Logger m_logger;

   private static final String PATH_SEPARATOR = System.getProperty("file.separator");

   private static final int MILLIS_TO_HOURS = 3600000;

   private static final int MILLIS_TO_MINUTES = 60000;

   private static final int MILLIS_TO_SECONDS = 1000;
}
