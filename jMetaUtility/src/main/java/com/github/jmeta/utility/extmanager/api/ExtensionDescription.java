/**
 *
 * {@link ExtensionDescription}.java
 *
 * @author Jens Ebert
 *
 * @date 15.10.2017
 *
 */
package com.github.jmeta.utility.extmanager.api;

import java.time.LocalDateTime;

/**
 * {@link ExtensionDescription} is just a set of additional descriptions of an {@link IExtension}. These values just
 * have information purpose and are not used in any functional way.
 */
public class ExtensionDescription {

   private final String name;
   private final String authors;
   private final String version;
   private final LocalDateTime publishTime;
   private final String description;
   private final String copyrightNotice;
   private final String licenseTerms;

   /**
    * Creates a new {@link ExtensionDescription}.
    * 
    * @param name
    *           the name of the extension
    * @param authors
    *           a string representing the author(s) of the extension
    * @param version
    *           a version string identifying the version of this extension
    * @param publishTime
    *           a time indicating when this extension was published
    * @param description
    *           a human-readable, preferably English description of this extension
    * @param copyrightNotice
    *           a copyright notice for this extension
    * @param licenseTerms
    *           the license terms for this extension
    */
   public ExtensionDescription(String name, String authors, String version, LocalDateTime publishTime,
      String description, String copyrightNotice, String licenseTerms) {
      this.name = name;
      this.authors = authors;
      this.version = version;
      this.publishTime = publishTime;
      this.description = description;
      this.copyrightNotice = copyrightNotice;
      this.licenseTerms = licenseTerms;
   }

   /**
    * @return the name of the extension
    */
   public String getName() {
      return name;
   }

   /**
    * @return a string representing the author(s) of the extension
    */
   public String getAuthors() {
      return authors;
   }

   /**
    * @return a version string identifying the version of this extension
    */
   public String getVersion() {
      return version;
   }

   /**
    * @return a time indicating when this extension was published
    */
   public LocalDateTime getPublishTime() {
      return publishTime;
   }

   /**
    * @return a human-readable, preferably English description of this extension
    */
   public String getDescription() {
      return description;
   }

   /**
    * @return a copyright notice for this extension
    */
   public String getCopyrightNotice() {
      return copyrightNotice;
   }

   /**
    * @return the license terms for this extension
    */
   public String getLicenseTerms() {
      return licenseTerms;
   }
}
