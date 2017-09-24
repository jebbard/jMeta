//
// Diese Datei wurde mit der JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.7 generiert 
// Siehe <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2017.09.24 um 08:51:10 AM CEST 
//


package de.je.jmeta.extmanager.impl.jaxb.extbundles;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the de.je.jmeta.extmanager.impl.jaxb.extbundles package. 
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

    private final static QName _ExtensionBundle_QNAME = new QName("www.easytag.de/XMLSchema_v1_0", "ExtensionBundle");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: de.je.jmeta.extmanager.impl.jaxb.extbundles
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link ExtensionBundleType }
     * 
     */
    public ExtensionBundleType createExtensionBundleType() {
        return new ExtensionBundleType();
    }

    /**
     * Create an instance of {@link BundlePropertiesType }
     * 
     */
    public BundlePropertiesType createBundlePropertiesType() {
        return new BundlePropertiesType();
    }

    /**
     * Create an instance of {@link BundleJarType }
     * 
     */
    public BundleJarType createBundleJarType() {
        return new BundleJarType();
    }

    /**
     * Create an instance of {@link ExtensionType }
     * 
     */
    public ExtensionType createExtensionType() {
        return new ExtensionType();
    }

    /**
     * Create an instance of {@link BundledExtensionsType }
     * 
     */
    public BundledExtensionsType createBundledExtensionsType() {
        return new BundledExtensionsType();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ExtensionBundleType }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "www.easytag.de/XMLSchema_v1_0", name = "ExtensionBundle")
    public JAXBElement<ExtensionBundleType> createExtensionBundle(ExtensionBundleType value) {
        return new JAXBElement<ExtensionBundleType>(_ExtensionBundle_QNAME, ExtensionBundleType.class, null, value);
    }

}
