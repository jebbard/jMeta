//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a>
// Any modifications to this file will be lost upon recompilation of the source schema.
// Generated on: 2014.05.19 at 07:35:14 PM CEST
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
 * Java class for AvailableExtensionPointsType complex type.
 * 
 * <p>
 * The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="AvailableExtensionPointsType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="ExtensionPoint" type="{www.easytag.de/XMLSchema_v1_0}ExtensionPointType" maxOccurs="unbounded"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AvailableExtensionPointsType", namespace = "www.easytag.de/XMLSchema_v1_0", propOrder = {
   "extensionPoint" })
public class AvailableExtensionPointsType {

   @XmlElement(name = "ExtensionPoint", required = true)
   protected List<ExtensionPointType> extensionPoint;

   /**
    * Gets the value of the extensionPoint property.
    * 
    * <p>
    * This accessor method returns a reference to the live list, not a snapshot. Therefore any modification you make to
    * the returned list will be present inside the JAXB object. This is why there is not a <CODE>set</CODE> method for
    * the extensionPoint property.
    * 
    * <p>
    * For example, to add a new item, do as follows:
    * 
    * <pre>
    * getExtensionPoint().add(newItem);
    * </pre>
    * 
    * 
    * <p>
    * Objects of the following type(s) are allowed in the list {@link ExtensionPointType }
    * 
    * 
    */
   public List<ExtensionPointType> getExtensionPoint() {

      if (extensionPoint == null) {
         extensionPoint = new ArrayList<ExtensionPointType>();
      }
      return this.extensionPoint;
   }

}
