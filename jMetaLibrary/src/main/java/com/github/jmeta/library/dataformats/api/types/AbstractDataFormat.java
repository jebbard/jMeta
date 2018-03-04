/**
 *
 * {@link AbstractDataFormat}.java
 *
 * @author Jens Ebert
 *
 * @date 04.03.2018
 *
 */
package com.github.jmeta.library.dataformats.api.types;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.github.jmeta.utility.dbc.api.services.Reject;

/**
 * {@link AbstractDataFormat} is the base class of all data formats supported by jMeta.
 */
public abstract class AbstractDataFormat {

   private final Set<String> fileExtensions = new HashSet<>();
   private final Set<String> mimeTypes = new HashSet<>();
   private final List<String> specificationLinks = new ArrayList<>();
   private final String name;
   private final String author;
   private final Date revisionDate;

   /**
    * Creates a new {@link AbstractDataFormat}.
    * 
    * @param id
    *           Unique identifier of the data format
    * @param fileExtensions
    *           File extensions supported by the data format
    * @param mimeTypes
    *           Mime types corresponding to the data format
    * @param specificationLinks
    *           Links to specification documents describing the data format
    * @param author
    *           Name of the author of the data format
    * @param revisionDate
    *           Date of when the data format specification revision was created
    */
   public AbstractDataFormat(String id, Set<String> fileExtensions, Set<String> mimeTypes,
      List<String> specificationLinks, String author, Date revisionDate) {
      Reject.ifNull(author, "author");
      Reject.ifNull(specificationLinks, "specificationLinks");
      Reject.ifNull(mimeTypes, "mimeTypes");
      Reject.ifNull(fileExtensions, "fileExtensions");
      Reject.ifNull(revisionDate, "revisionDate");

      this.fileExtensions.addAll(fileExtensions);
      this.mimeTypes.addAll(mimeTypes);
      this.specificationLinks.addAll(specificationLinks);
      this.name = id;
      this.author = author;
      this.revisionDate = revisionDate;
   }

   /**
    * @return the name of the data format
    */
   public String getName() {
      return name;
   }

   /**
    * @return the author of the data format
    */
   public String getAuthor() {
      return author;
   }

   /**
    * @return the specification links
    */
   public List<String> getSpecificationLinks() {
      return Collections.unmodifiableList(specificationLinks);
   }

   /**
    * @return the mime types associated with the data format
    */
   public Set<String> getMimeTypes() {
      return Collections.unmodifiableSet(mimeTypes);
   }

   /**
    * @return the file extensions associated with the data format
    */
   public Set<String> getFileExtensions() {
      return Collections.unmodifiableSet(fileExtensions);
   }

   /**
    * @return revisionDate of the data format
    */
   public Date getRevisionDate() {
      return revisionDate;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[" + "name=" + name + ", author=" + author + ", specificationLinks="
         + specificationLinks + ", mimeTypes=" + mimeTypes + ", fileExtensions=" + fileExtensions + "]";
   }
}