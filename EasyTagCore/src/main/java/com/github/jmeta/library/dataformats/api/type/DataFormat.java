/**
 * {@link DataFormat}.java
 *
 * @author Jens Ebert
 * @date 31.12.10 19:47:06 (December 31, 2010)
 */

package com.github.jmeta.library.dataformats.api.type;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.jmeta.utility.dbc.api.services.Reject;

import de.je.util.javautil.common.extenum.AbstractExtensibleEnum;

/**
 *
 */
public class DataFormat extends AbstractExtensibleEnum<DataFormat> {

   /**
    * Creates a new {@link DataFormat}.
    * 
    * @param id
    * @param fileExtensions
    * @param mimeTypes
    * @param specificationLinks
    * @param author
    * @param revisionDate
    */
   protected DataFormat(String id, Set<String> fileExtensions,
      Set<String> mimeTypes, List<String> specificationLinks, String author,
      Date revisionDate) {
      super(id);

      Reject.ifNull(author, "author");
      Reject.ifNull(specificationLinks, "specificationLinks");
      Reject.ifNull(mimeTypes, "mimeTypes");
      Reject.ifNull(fileExtensions, "fileExtensions");
      Reject.ifNull(revisionDate, "revisionDate");

      m_fileExtensions.addAll(fileExtensions);
      m_mimeTypes.addAll(mimeTypes);
      m_specificationLinks.addAll(specificationLinks);
      m_name = id;
      m_author = author;
      m_revisionDate = revisionDate;
   }

   /**
    * @return the name of the data format
    */
   public String getName() {

      return m_name;
   }

   /**
    * @return the author of the data format
    */
   public String getAuthor() {

      return m_author;
   }

   /**
    * @return the specification links
    */
   public List<String> getSpecificationLinks() {

      return Collections.unmodifiableList(m_specificationLinks);
   }

   /**
    * @return the mime types associated with the data format
    */
   public Set<String> getMimeTypes() {

      return Collections.unmodifiableSet(m_mimeTypes);
   }

   /**
    * @return the file extensions associated with the data format
    */
   public Set<String> getFileExtensions() {

      return Collections.unmodifiableSet(m_fileExtensions);
   }

   /**
    * Returns revisionDate
    * 
    * @return revisionDate
    */
   public Date getRevisionDate() {

      return m_revisionDate;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[" + "name=" + m_name + ", author="
         + m_author + ", specificationLinks=" + m_specificationLinks
         + ", mimeTypes=" + m_mimeTypes + ", fileExtensions=" + m_fileExtensions
         + "]";
   }

   private final Set<String> m_fileExtensions = new HashSet<>();

   private final Set<String> m_mimeTypes = new HashSet<>();

   private final List<String> m_specificationLinks = new ArrayList<>();

   private final String m_name;

   private final String m_author;

   private final Date m_revisionDate;
}
