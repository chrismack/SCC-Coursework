//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.5-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2018.01.23 at 08:09:31 PM GMT 
//


package com.brokering;

import javax.xml.bind.annotation.XmlRegistry;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the com.brokering package. 
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


    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: com.brokering
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Shares }
     * 
     */
    public Shares createShares() {
        return new Shares();
    }

    /**
     * Create an instance of {@link Share }
     * 
     */
    public Share createShare() {
        return new Share();
    }

    /**
     * Create an instance of {@link SharePrice }
     * 
     */
    public SharePrice createSharePrice() {
        return new SharePrice();
    }

    /**
     * Create an instance of {@link ShareNews }
     * 
     */
    public ShareNews createShareNews() {
        return new ShareNews();
    }

    /**
     * Create an instance of {@link CompanyInfo }
     * 
     */
    public CompanyInfo createCompanyInfo() {
        return new CompanyInfo();
    }

    /**
     * Create an instance of {@link ShareHistory }
     * 
     */
    public ShareHistory createShareHistory() {
        return new ShareHistory();
    }

}
