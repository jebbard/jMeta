//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.09.24 um 08:51:10 AM CEST 
//


package de.je.jmeta.extmanager.impl.jaxb.extpoints;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.je.jmeta.extmanager.impl.jaxb.extpoints package. 
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _ExtensionPointsConfiguration_QNAME = new QName("www.easytag.de/XMLSchema_v1_0", "ExtensionPointsConfiguration");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.je.jmeta.extmanager.impl.jaxb.extpoints
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExtensionPointsConfigurationType }
     * 
     */
    public ExtensionPointsConfigurationType createExtensionPointsConfigurationType() {
        return new ExtensionPointsConfigurationType();
    }

    /**
     * Create an instance of {@link ExtensionPointsType }
     * 
     */
    public ExtensionPointsType createExtensionPointsType() {
        return new ExtensionPointsType();
    }

    /**
     * Create an instance of {@link ExtensionBundleSearchPathType }
     * 
     */
    public ExtensionBundleSearchPathType createExtensionBundleSearchPathType() {
        return new ExtensionBundleSearchPathType();
    }

    /**
     * Create an instance of {@link ExtensionPointType }
     * 
     */
    public ExtensionPointType createExtensionPointType() {
        return new ExtensionPointType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExtensionPointsConfigurationType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "www.easytag.de/XMLSchema_v1_0", name = "ExtensionPointsConfiguration")
    public JAXBElement<ExtensionPointsConfigurationType> createExtensionPointsConfiguration(ExtensionPointsConfigurationType value) {
        return new JAXBElement<ExtensionPointsConfigurationType>(_ExtensionPointsConfiguration_QNAME, ExtensionPointsConfigurationType.class, null, value);
    }

}
