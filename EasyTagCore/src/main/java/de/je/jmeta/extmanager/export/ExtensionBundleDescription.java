/**
 * {@link ExtensionBundleDescription}.java
 *
 * @author Jens Ebert
 * @date 28.04.11 12:50:18 (April 28, 2011)
 */

package de.je.jmeta.extmanager.export;

import java.util.Date;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link ExtensionBundleDescription} describes several informational properties of a single {@link IExtensionBundle}.
 */
public class ExtensionBundleDescription {

   /**
    * Creates a new instance of {@link ExtensionBundleDescription}.
    *
    * @param name
    *           The informal name of the {@link IExtensionBundle}.
    * @param author
    *           The list of authors of the {@link IExtensionBundle}.
    * @param version
    *           The version of the {@link IExtensionBundle}.
    * @param date
    *           The date of creation of this version of the {@link IExtensionBundle}.
    * @param description
    *           An informal description for the {@link IExtensionBundle}.
    * @param type
    *           The {@link BundleType} of this {@link IExtensionBundle}.
    */
   public ExtensionBundleDescription(String name, String author, String version,
      Date date, String description, BundleType type) {
      Reject.ifNull(type, "type");
      Reject.ifNull(description, "description");
      Reject.ifNull(date, "date");
      Reject.ifNull(version, "version");
      Reject.ifNull(author, "authors");
      Reject.ifNull(name, "name");

      m_name = name;
      m_author = author;
      m_version = version;
      m_date = date;
      m_description = description;
      m_type = type;
   }

   /**
    * Returns the name of the {@link IExtensionBundle}.
    *
    * @return the name of the {@link IExtensionBundle}
    */
   public String getName() {

      return m_name;
   }

   /**
    * Return the author of the {@link IExtensionBundle}.
    *
    * @return the author of the {@link IExtensionBundle}.
    */
   public String getAuthor() {

      return m_author;
   }

   /**
    * Return the version of the {@link IExtensionBundle}.
    *
    * @return the version of the {@link IExtensionBundle}.
    */
   public String getVersion() {

      return m_version;
   }

   /**
    * Return the {@link Date} of the {@link IExtensionBundle}.
    *
    * @return the {@link Date} of the {@link IExtensionBundle}
    */
   public Date getDate() {

      return m_date;
   }

   /**
    * Returns the description of the {@link IExtensionBundle}.
    *
    * @return the description of the {@link IExtensionBundle}.
    */
   public String getDescription() {

      return m_description;
   }

   /**
    * Returns the {@link BundleType} of the {@link IExtensionBundle}.
    *
    * @return the {@link BundleType} of the {@link IExtensionBundle}.
    */
   public BundleType getType() {

      return m_type;
   }

   /**
    * @see java.lang.Object#hashCode()
    */
   @Override
   public int hashCode() {

      final int prime = 31;
      int result = 1;
      result = prime * result + ((m_author == null) ? 0 : m_author.hashCode());
      result = prime * result + ((m_date == null) ? 0 : m_date.hashCode());
      result = prime * result
         + ((m_description == null) ? 0 : m_description.hashCode());
      result = prime * result + ((m_name == null) ? 0 : m_name.hashCode());
      result = prime * result + ((m_type == null) ? 0 : m_type.hashCode());
      result = prime * result
         + ((m_version == null) ? 0 : m_version.hashCode());
      return result;
   }

   /**
    * @see java.lang.Object#equals(java.lang.Object)
    */
   @Override
   public boolean equals(Object obj) {

      if (this == obj)
         return true;
      if (obj == null)
         return false;
      if (getClass() != obj.getClass())
         return false;
      ExtensionBundleDescription other = (ExtensionBundleDescription) obj;
      if (m_author == null) {
         if (other.m_author != null)
            return false;
      } else if (!m_author.equals(other.m_author))
         return false;
      if (m_date == null) {
         if (other.m_date != null)
            return false;
      } else if (!m_date.equals(other.m_date))
         return false;
      if (m_description == null) {
         if (other.m_description != null)
            return false;
      } else if (!m_description.equals(other.m_description))
         return false;
      if (m_name == null) {
         if (other.m_name != null)
            return false;
      } else if (!m_name.equals(other.m_name))
         return false;
      if (m_type != other.m_type)
         return false;
      if (m_version == null) {
         if (other.m_version != null)
            return false;
      } else if (!m_version.equals(other.m_version))
         return false;
      return true;
   }

   /**
    * @see java.lang.Object#toString()
    */
   @Override
   public String toString() {

      return getClass().getName() + "[" + "name=" + m_name + ", author="
         + m_author + ", version=" + m_version + ", date=" + m_date + ", type="
         + m_type + ", description=" + m_description + "]";
   }

   private final String m_name;

   private final String m_author;

   private final String m_version;

   private final Date m_date;

   private final String m_description;

   private final BundleType m_type;
}
