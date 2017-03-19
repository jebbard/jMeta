/**
 *
 * {@link DefaultExtensionsDataFormat}.java
 *
 * @author Jens Ebert
 *
 * @date 28.04.2011
 */
package de.je.jmeta.defext.dataformats;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.je.jmeta.dataformats.DataFormat;

/**
 * {@link DefaultExtensionsDataFormat}
 *
 */
public class DefaultExtensionsDataFormat extends DataFormat {

   private DefaultExtensionsDataFormat(String id, Set<String> fileExtensions,
      Set<String> mimeTypes, List<String> specificationLinks, String author,
      Date revisionDate) {
      super(id, fileExtensions, mimeTypes, specificationLinks, author,
         revisionDate);
   }

   /**
    *
    */
   public static final DataFormat ID3v23 = new DefaultExtensionsDataFormat(
      "ID3v2.3", new HashSet<String>(), new HashSet<String>(),
      new ArrayList<String>(), "M. Nilsson", new Date());

   /**
    *
    */
   public static final DataFormat MP3 = new DefaultExtensionsDataFormat("MP3",
      new HashSet<String>(), new HashSet<String>(), new ArrayList<String>(), "",
      new Date());

   /**
    *
    */
   public static final DataFormat APEv2 = new DefaultExtensionsDataFormat(
      "APEv2", new HashSet<String>(), new HashSet<String>(),
      new ArrayList<String>(), "", new Date());

   /**
    *
    */
   public static final DataFormat LYRICS3v2 = new DefaultExtensionsDataFormat(
      "Lyrics3v2", new HashSet<String>(), new HashSet<String>(),
      new ArrayList<String>(), "", new Date());

   /**
    *
    */
   public static final DataFormat ID3v1 = new DefaultExtensionsDataFormat(
      "ID3v1", new HashSet<String>(), new HashSet<String>(),
      new ArrayList<String>(), "M. Nilsson", new Date());

   /**
    *
    */
   public static final DataFormat OGG = new DefaultExtensionsDataFormat("Ogg",
      new HashSet<String>(), new HashSet<String>(), new ArrayList<String>(), "",
      new Date());
}
