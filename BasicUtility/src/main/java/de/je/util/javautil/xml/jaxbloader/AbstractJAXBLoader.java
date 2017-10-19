/**
 *
 * {@link AbstractJAXBLoader}.java
 *
 * @date 10.03.2010
 *
 */
package de.je.util.javautil.xml.jaxbloader;

import java.io.File;
import java.io.InputStream;
import java.net.URL;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import de.je.util.javautil.common.err.Reject;

/**
 * {@link AbstractJAXBLoader} provides functionality to load and unmarshal an XML file into a root object using the JAXB
 * library.
 *
 * @author Jens Ebert
 * @version 1.0
 *
 * @param <T>
 *           The type of root object for loading.
 */
public abstract class AbstractJAXBLoader<T> {

   /**
    * Creates a new {@link AbstractJAXBLoader}.
    * 
    * @param schemaFileRelativePath
    *           The relative path to the schema file. The path must be given relative to the location of the
    *           implementing class of this {@link AbstractJAXBLoader}.
    * @param rootClassToLoad
    *           The class of the root object that is loaded.
    */
   public AbstractJAXBLoader(String schemaFileRelativePath, Class<T> rootClassToLoad) {
      Reject.ifNull(schemaFileRelativePath, "schemaFileName");

      m_schemaFileRelativePath = schemaFileRelativePath;
      m_marshallClassesPackage = rootClassToLoad.getPackage().getName();

      URL schemaURL = loadSchema();

      try {
         JAXBContext context = JAXBContext.newInstance(m_marshallClassesPackage);
         m_configFileSchema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(schemaURL);
         m_unmarshaller = context.createUnmarshaller();

         m_unmarshaller.setSchema(m_configFileSchema);
      }

      catch (JAXBException e) {
         throw new JAXBLoaderException("Could not instantiate JAXBContext", e);
      }

      catch (SAXException e) {
         throw new JAXBLoaderException("Could not load schema URL: " + schemaURL.getFile(), e);
      }
   }

   /**
    * Returns whether this {@link AbstractJAXBLoader} has already loaded.
    *
    * @return whether this {@link AbstractJAXBLoader} has already loaded.
    */
   public boolean isLoaded() {
      return m_rootObject != null;
   }

   /**
    * Loads the given XML {@link InputStream}. Must not have been loaded before.
    *
    * @param inputStream
    *           The XML {@link InputStream} to load.
    *
    * @throws JAXBLoaderException
    *            if an exception occurred during loading the {@link InputStream}.
    *
    * @pre {@link #isLoaded()} == false
    * @post {@link #isLoaded()} == true
    */
   public void load(InputStream inputStream) {
      Reject.ifNull(inputStream, "inputStream");
      Reject.ifTrue(isLoaded(), "isLoaded()");

      m_rootObject = unmarshal(inputStream);
   }

   /**
    * Returns the root object that has been unmarshalled.
    *
    * @return the root object that has been unmarshalled.
    *
    * @pre {@link #isLoaded()} == true.
    */
   public T getRootObject() {
      Reject.ifFalse(isLoaded(), "isLoaded()");

      return m_rootObject;
   }

   /**
    * Unmarshals a piece of XML to concrete objects.
    *
    * @param inputStream
    *           The XML {@link InputStream} to unmarshal.
    * @throws JAXBLoaderException
    *            if an exception occurred during loading the file.
    * @return The unmarshalled object.
    */
   @SuppressWarnings("unchecked")
   private T unmarshal(InputStream inputStream) {
      try {
         JAXBElement<T> element = (JAXBElement<T>) m_unmarshaller.unmarshal(inputStream);

         return element.getValue();
      }

      catch (JAXBException e) {
         throw new JAXBLoaderException("Could not unmarshal input stream.", e);
      }
   }

   /**
    * Loads the XML schema of the component configuration files.
    *
    * @return The {@link File} corresponding to the XML schema.
    */
   private URL loadSchema() {
      final URL schemaURL = getClass().getResource(m_schemaFileRelativePath);

      if (schemaURL == null)
         throw new JAXBLoaderException("Could not find the schema resource: " + m_schemaFileRelativePath, null);

      return schemaURL;
   }

   private final String m_schemaFileRelativePath;

   private final String m_marshallClassesPackage;

   private Schema m_configFileSchema;

   private Unmarshaller m_unmarshaller;

   private T m_rootObject;
}
