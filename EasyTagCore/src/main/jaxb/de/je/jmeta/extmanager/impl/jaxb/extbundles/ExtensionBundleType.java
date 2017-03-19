//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2016.11.26 um 08:13:54 AM CET 
//


package de.je.jmeta.extmanager.impl.jaxb.extbundles;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java-Klasse für ExtensionBundleType complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="ExtensionBundleType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;all>
 *         &lt;element name="BundleProperties" type="{www.easytag.de/XMLSchema_v1_0}BundlePropertiesType"/>
 *         &lt;element name="BundleJar" type="{www.easytag.de/XMLSchema_v1_0}BundleJarType" minOccurs="0"/>
 *         &lt;element name="BundledExtensions" type="{www.easytag.de/XMLSchema_v1_0}BundledExtensionsType"/>
 *       &lt;/all>
 *       &lt;attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="version" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ExtensionBundleType", namespace = "www.easytag.de/XMLSchema_v1_0", propOrder = {

})
public class ExtensionBundleType {

    @XmlElement(name = "BundleProperties", required = true)
    protected BundlePropertiesType bundleProperties;
    @XmlElement(name = "BundleJar")
    protected BundleJarType bundleJar;
    @XmlElement(name = "BundledExtensions", required = true)
    protected BundledExtensionsType bundledExtensions;
    @XmlAttribute(name = "name", required = true)
    protected String name;
    @XmlAttribute(name = "version", required = true)
    protected String version;

    /**
     * Ruft den Wert der bundleProperties-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BundlePropertiesType }
     *     
     */
    public BundlePropertiesType getBundleProperties() {
        return bundleProperties;
    }

    /**
     * Legt den Wert der bundleProperties-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BundlePropertiesType }
     *     
     */
    public void setBundleProperties(BundlePropertiesType value) {
        this.bundleProperties = value;
    }

    /**
     * Ruft den Wert der bundleJar-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BundleJarType }
     *     
     */
    public BundleJarType getBundleJar() {
        return bundleJar;
    }

    /**
     * Legt den Wert der bundleJar-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BundleJarType }
     *     
     */
    public void setBundleJar(BundleJarType value) {
        this.bundleJar = value;
    }

    /**
     * Ruft den Wert der bundledExtensions-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link BundledExtensionsType }
     *     
     */
    public BundledExtensionsType getBundledExtensions() {
        return bundledExtensions;
    }

    /**
     * Legt den Wert der bundledExtensions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link BundledExtensionsType }
     *     
     */
    public void setBundledExtensions(BundledExtensionsType value) {
        this.bundledExtensions = value;
    }

    /**
     * Ruft den Wert der name-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Legt den Wert der name-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setName(String value) {
        this.name = value;
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
