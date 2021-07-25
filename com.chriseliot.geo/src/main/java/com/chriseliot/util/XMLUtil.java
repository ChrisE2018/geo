
package com.chriseliot.util;

import java.io.*;
import java.net.URL;
import java.util.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.*;
import org.xml.sax.SAXException;

public class XMLUtil
{
    public DocumentBuilder getDocumentBuilder () throws ParserConfigurationException
    {
        final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance ();
        factory.setNamespaceAware (true);
        return factory.newDocumentBuilder ();
    }

    public Document getDocument (URL url) throws ParserConfigurationException, SAXException, IOException
    {
        final InputStream stream = url.openStream ();
        final Document result = getDocument (stream);
        stream.close ();
        return result;
    }

    public Document getDocument (InputStream stream) throws ParserConfigurationException, SAXException, IOException
    {
        final DocumentBuilder builder = getDocumentBuilder ();
        return builder.parse (stream);
    }

    public Document getDocument (File file) throws ParserConfigurationException, IOException, SAXException
    {
        final DocumentBuilder builder = getDocumentBuilder ();
        return builder.parse (file);
    }

    public String getValue (Element element, String attribute)
    {
        if (element.hasAttribute (attribute))
        {
            // Short form
            return element.getAttribute (attribute);
        }
        else
        {
            return null;
        }
    }

    public String getValue (Element element, String attribute, String defaultValue)
    {
        if (element.hasAttribute (attribute))
        {
            // Short form
            return element.getAttribute (attribute);
        }
        else
        {
            return defaultValue;
        }
    }

    public String getText (Element element)
    {
        final NodeList children = element.getChildNodes ();
        for (int i = 0; i < children.getLength (); i++)
        {
            final Node node = children.item (i);
            if (node instanceof Text)
            {
                final Text child = (Text)node;
                return child.getData ();
            }
        }
        return null;
    }

    public String get (Element element, String attribute, String defaultValue)
    {
        if (element.hasAttribute (attribute))
        {
            return element.getAttribute (attribute);
        }
        else
        {
            return defaultValue;
        }
    }

    public Boolean getBoolean (Element element, String attribute, Boolean defaultValue)
    {
        if (element.hasAttribute (attribute))
        {
            return Boolean.valueOf (element.getAttribute (attribute));
        }
        else
        {
            return defaultValue;
        }
    }

    public Integer getInteger (Element element, String attribute, Integer defaultValue)
    {
        if (element.hasAttribute (attribute))
        {
            return Integer.valueOf (element.getAttribute (attribute));
        }
        else
        {
            return defaultValue;
        }
    }

    public Double getDouble (Element element, String attribute, Double defaultValue)
    {
        if (element.hasAttribute (attribute))
        {
            return Double.valueOf (element.getAttribute (attribute));
        }
        else
        {
            return defaultValue;
        }
    }

    /**
     * Convert an XML element into a readable string.
     *
     * @param element Any XML element to convert.
     *
     * @return The XML string, indented but without the xml declaration line.
     */
    public String getXMLString (Element element)
    {
        try
        {
            return getXMLString (element, false, true);
        }
        catch (final TransformerException e)
        {
            e.printStackTrace ();
            return null;
        }
    }

    public String getXMLString (Element element, boolean declaration, boolean indent) throws TransformerException
    {
        final TransformerFactory factory = TransformerFactory.newInstance ();
        final Transformer transformer = factory.newTransformer ();

        transformer.setOutputProperty (OutputKeys.OMIT_XML_DECLARATION, !declaration ? "yes" : "no");
        transformer.setOutputProperty (OutputKeys.INDENT, indent ? "yes" : "no");

        final StringWriter sw = new StringWriter ();
        final StreamResult result = new StreamResult (sw);
        final DOMSource source = new DOMSource (element);
        transformer.transform (source, result);
        return sw.toString ();
    }

    public String getXMLString (Document doc, boolean declaration, boolean indent) throws TransformerException
    {
        final TransformerFactory factory = TransformerFactory.newInstance ();
        final Transformer transformer = factory.newTransformer ();

        transformer.setOutputProperty (OutputKeys.OMIT_XML_DECLARATION, !declaration ? "yes" : "no");
        transformer.setOutputProperty (OutputKeys.INDENT, indent ? "yes" : "no");

        final StringWriter sw = new StringWriter ();
        final StreamResult result = new StreamResult (sw);
        final DOMSource source = new DOMSource (doc);
        transformer.transform (source, result);
        return sw.toString ();
    }

    public List<Element> getChildren (Element root)
    {
        final List<Element> result = new ArrayList<> ();
        final NodeList children = root.getChildNodes ();
        for (int i = 0; i < children.getLength (); i++)
        {
            final Node node = children.item (i);
            if (node.getNodeType () == Node.ELEMENT_NODE)
            {
                final Element e = Element.class.cast (node);
                result.add (e);
            }
        }
        return result;
    }

    public List<Element> getChildren (Element root, String tagname)
    {
        final List<Element> result = new ArrayList<> ();
        final NodeList children = root.getChildNodes ();
        for (int i = 0; i < children.getLength (); i++)
        {
            final Node node = children.item (i);
            if (node.getNodeType () == Node.ELEMENT_NODE)
            {
                final Element e = Element.class.cast (node);
                if (e.getTagName ().equals (tagname))
                {
                    result.add (e);
                }
            }
        }
        return result;
    }

    public Element getNthChild (Element root, String tagname, int n)
    {
        int count = 0;
        final NodeList children = root.getChildNodes ();
        for (int i = 0; i < children.getLength (); i++)
        {
            final Node node = children.item (i);
            if (node.getNodeType () == Node.ELEMENT_NODE)
            {
                final Element e = Element.class.cast (node);
                if (e.getTagName ().equals (tagname))
                {
                    if (n <= count)
                    {
                        return e;
                    }
                    count++;
                }
            }
        }
        return null;
    }

    /**
     * Return the nth element that has a given attribute.
     *
     * @param root The parent element to search.
     * @param attribute The attribute to check.
     * @param value The value of the attribute to select.
     * @param n The nth matching element to select.
     *
     * @return The selected element, or null if none found.
     */
    public Element getNthChild (Element root, String attribute, String value, int n)
    {
        int count = 0;
        final NodeList children = root.getChildNodes ();
        for (int i = 0; i < children.getLength (); i++)
        {
            final Node node = children.item (i);
            if (node.getNodeType () == Node.ELEMENT_NODE)
            {
                final Element e = Element.class.cast (node);
                if (e.getAttribute (attribute).equals (value))
                {
                    if (n <= count)
                    {
                        return e;
                    }
                    count++;
                }
            }
        }
        return null;
    }

    public void writeXml (Document doc, File file) throws IOException, TransformerException
    {
        final TransformerFactory transformerFactory = TransformerFactory.newInstance ();
        final Transformer transformer = transformerFactory.newTransformer ();
        transformer.setOutputProperty (OutputKeys.INDENT, "yes");
        transformer.setOutputProperty ("{http://xml.apache.org/xslt}indent-amount", "4");
        final DOMSource source = new DOMSource (doc);
        final FileWriter writer = new FileWriter (file);
        final StreamResult result = new StreamResult (writer);
        transformer.transform (source, result);
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        buffer.append (" ");
        buffer.append (System.identityHashCode (this));
        buffer.append (">");
        return buffer.toString ();
    }
}
