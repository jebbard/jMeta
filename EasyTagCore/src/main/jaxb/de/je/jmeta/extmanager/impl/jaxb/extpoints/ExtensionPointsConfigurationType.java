//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.09.09 um 07:00:16 PM CEST 
//


package de.je.jmeta.extmanager.impl.jaxb.extpoints;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ExtensionPointsConfigurationType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ExtensionPointsConfigurationType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="ExtensionBundleSearchPath" type="{www.easytag.de/XMLSchema_v1_0}ExtensionBundleSearchPathType"/>
 *         &lt;element name="ExtensionPoints" type="{www.easytag.de/XMLSchema_v1_0}ExtensionPointsType"/>
 *       &lt;/all>
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtensionPointsConfigurationType", namespace = "www.easytag.de/XMLSchema_v1_0", propOrder = {

})
public class ExtensionPointsConfigurationType {

    @XmlElement(name = "ExtensionBundleSearchPath", required = true)
    protected ExtensionBundleSearchPathType extensionBundleSearchPath;
    @XmlElement(name = "ExtensionPoints", required = true)
    protected ExtensionPointsType extensionPoints;
    @XmlAttribute(name = "version", required = true)
    protected String version;

    /**
     * Ruft den Wert der extensionBundleSearchPath-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ExtensionBundleSearchPathType }
     *     
     */
    public ExtensionBundleSearchPathType getExtensionBundleSearchPath() {
        return extensionBundleSearchPath;
    }

    /**
     * Legt den Wert der extensionBundleSearchPath-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtensionBundleSearchPathType }
     *     
     */
    public void setExtensionBundleSearchPath(ExtensionBundleSearchPathType value) {
        this.extensionBundleSearchPath = value;
    }

    /**
     * Ruft den Wert der extensionPoints-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link ExtensionPointsType }
     *     
     */
    public ExtensionPointsType getExtensionPoints() {
        return extensionPoints;
    }

    /**
     * Legt den Wert der extensionPoints-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ExtensionPointsType }
     *     
     */
    public void setExtensionPoints(ExtensionPointsType value) {
        this.extensionPoints = value;
    }

    /**
     * Ruft den Wert der version-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getVersion() {
        return version;
    }

    /**
     * Legt den Wert der version-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setVersion(String value) {
        this.version = value;
    }

}
