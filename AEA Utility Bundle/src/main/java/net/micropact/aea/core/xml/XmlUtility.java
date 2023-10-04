package net.micropact.aea.core.xml;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import net.entellitrak.aea.gl.api.java.GeneralRuntimeException;

/**
 * This class contains utility functionality related to dealing with XML files.
 *
 * @author zmiller
 */
public final class XmlUtility {

    /**
     * Utility classes do not need constructors.
     */
    private XmlUtility(){}


    /**
     * Converts an XML document to a String representation. The representation is not pretty printed.
     *
     * @param document the document
     * @return the xml string
     * @throws TransformerException If there was an underlying transformer exception
     */
    public static String convertDocumentToString(final Document document) throws TransformerException {
        final Transformer transformer = XmlUtility.getTransformer();

        final DOMSource source = new DOMSource(document);

        try(StringWriter stringWriter = new StringWriter()){
            final StreamResult result = new StreamResult(stringWriter);
            transformer.transform(source, result);
            return stringWriter.toString();
        } catch (final IOException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get an XML Document from an XML String without validation checks.
     *
     * @param xmlString the xml string
     * @return the xml document
     */
    public static Document convertStringToDocumentWithoutValidation(final String xmlString) {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            documentBuilderFactory.setValidating(false);
            documentBuilderFactory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);

            return documentBuilderFactory
                    .newDocumentBuilder()
                    .parse(new InputSource(new StringReader(xmlString)));
        } catch (final RuntimeException | SAXException | IOException | ParserConfigurationException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get a {@link DocumentBuilder} with security flags enabled..
     *
     * @return the document builder
     */
    public static DocumentBuilder getSecureDocumentBuilder() {
        try {
            final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            documentBuilderFactory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            documentBuilderFactory.setFeature("http://javax.xml.XMLConstants/feature/secure-processing", true);

            return documentBuilderFactory.newDocumentBuilder();
        } catch (final ParserConfigurationException e) {
            throw new GeneralRuntimeException(e);
        }
    }

    /**
     * Get an XML Transformer.
     *
     * <p>
     *  The transformer will have security flags enabled.
     * </p>
     *
     * @return the transformer
     */
    private static Transformer getTransformer() {
        try {
            //Fortify SCA Fix 05172018
            /* Suppress XML transformers should be secured
             * because this is how Fortify wants the transformer to be secured. */
            @SuppressWarnings("squid:S4435")
            final TransformerFactory transformerFactory =
                    TransformerFactory.newInstance("com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl",
                            XmlUtility.class.getClassLoader());
            //End Fortify SCA Fix 05172018

            transformerFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            /* TODO: Uncomment this to make fortify happy if core ever stops overriding java's built-in XML libraries */
            //transformerFactory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            return transformerFactory.newTransformer();
        } catch (final TransformerConfigurationException e) {
            throw new GeneralRuntimeException(e);
        }
    }
}
