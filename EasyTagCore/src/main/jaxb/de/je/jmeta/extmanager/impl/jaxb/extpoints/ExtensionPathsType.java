//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation,
// vhudson-jaxb-ri-2.2-147
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2011.04.28 at 05:00:14 PM CEST
//

package de.je.jmeta.extmanager.impl.jaxb.extpoints;

import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * <p>
 * Java class for ExtensionPathsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="ExtensionPathsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="Path" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtensionPathsType", namespace = "www.easytag.de/XMLSchema_v1_0", propOrder = {
   "path" })
public class ExtensionPathsType {

   @XmlElement(name = "Path", required = true)
   protected List<String> path;

   /**
    * Gets the value of the path property.
    * 
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the path property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * 
    * <pre>
    * getPath().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link String }
    * 
    * 
    */
   public List<String> getPath() {

      if (path == null) {
         path = new ArrayList<String>();
      }
      return this.path;
   }

}
