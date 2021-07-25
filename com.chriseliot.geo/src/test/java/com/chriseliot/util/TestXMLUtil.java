
package com.chriseliot.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.net.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.chriseliot.geo.TestSupport;

class TestXMLUtil
{
    private final TestSupport ts = new TestSupport ();

    @Test
    void testGreate ()
    {
        final XMLUtil xu = new XMLUtil ();
        assertNotNull (xu.toString ());
    }

    @Test
    void testGetDocumentBuilder () throws ParserConfigurationException
    {
        final XMLUtil xu = new XMLUtil ();
        assertNotNull (xu.getDocumentBuilder ());
    }

    @Test
    void testGetDocumentURL () throws ParserConfigurationException, SAXException, IOException
    {
        final XMLUtil xu = new XMLUtil ();
        final URL url = getClass ().getResource ("test.xml");
        assertNotNull (url);
        assertNotNull (xu.getDocument (url));
    }

    @Test
    void testGetDocumentInputStream () throws IOException, ParserConfigurationException, SAXException
    {
        final XMLUtil xu = new XMLUtil ();
        final URL url = getClass ().getResource ("test.xml");
        assertNotNull (xu.getDocument (url.openStream ()));
    }

    @Test
    void testGetDocumentFile () throws URISyntaxException, ParserConfigurationException, IOException, SAXException
    {
        final XMLUtil xu = new XMLUtil ();
        final URL url = getClass ().getResource ("test.xml");
        final URI uri = url.toURI ();
        final File file = new File (uri);
        assertNotNull (xu.getDocument (file));
    }

    @Test
    void testGetValue () throws ParserConfigurationException, SAXException, IOException
    {
        final XMLUtil xu = new XMLUtil ();
        final URL url = getClass ().getResource ("test.xml");
        final Document doc = xu.getDocument (url);
        final Element element = doc.getDocumentElement ();
        assertEquals ("101", xu.getValue (element, "alpha"));
        assertNull (xu.getValue (element, "missing"));
        assertEquals ("101", xu.getValue (element, "alpha", "missing"));
        assertEquals ("default", xu.getValue (element, "missing", "default"));
    }

    @Test
    void testGetText () throws ParserConfigurationException, SAXException, IOException
    {
        final XMLUtil xu = new XMLUtil ();
        final URL url = getClass ().getResource ("test.xml");
        final Document doc = xu.getDocument (url);
        final Element root = doc.getDocumentElement ();
        final Element child = xu.getNthChild (root, "test", 0);
        assertNotNull (child);
        assertEquals ("text inside", xu.getText (child));
        final Element child2 = xu.getNthChild (root, "notext", 0);
        assertNotNull (child2);
        assertNull (xu.getText (child2));
        xu.getText (root);
    }

    @Test
    void testGet () throws ParserConfigurationException, SAXException, IOException
    {
        final XMLUtil xu = new XMLUtil ();
        final URL url = getClass ().getResource ("test.xml");
        final Document doc = xu.getDocument (url);
        final Element root = doc.getDocumentElement ();
        assertEquals ("101", xu.get (root, "alpha", "beta"));
        assertEquals ("beta", xu.get (root, "missing", "beta"));

        assertTrue (xu.getBoolean (root, "beta", false));
        assertTrue (xu.getBoolean (root, "missing", true));
        assertFalse (xu.getBoolean (root, "missing", false));

        assertEquals (12, xu.getInteger (root, "count", 6));
        assertEquals (6, xu.getInteger (root, "missing", 6));
        assertEquals (8, xu.getInteger (root, "missing", 8));

        assertEquals (123.456, xu.getDouble (root, "x", 17.5));
        assertEquals (17.5, xu.getDouble (root, "missing", 17.5));
        assertEquals (11.5, xu.getDouble (root, "missing", 11.5));
    }

    @Test
    void testGetXMLString () throws ParserConfigurationException, SAXException, IOException, TransformerException
    {
        final XMLUtil xu = new XMLUtil ();
        final URL url = getClass ().getResource ("test.xml");
        final Document doc = xu.getDocument (url);
        final Element root = doc.getDocumentElement ();
        final Element child = xu.getNthChild (root, "test", 0);
        assertEquals ("<test beta=\"101\">text inside</test>", xu.getXMLString (child).trim ());
        assertEquals ("<test beta=\"101\">text inside</test>", xu.getXMLString (child, false, false));
        assertEquals ("<?xml version=\"1.0\" encoding=\"UTF-8\"?><test beta=\"101\">text inside</test>",
                xu.getXMLString (child, true, true).trim ());
        assertNotNull (xu.getXMLString (doc, true, true));
        assertNotNull (xu.getXMLString (doc, false, false));
    }

    @Test
    void testGetChildren () throws ParserConfigurationException, SAXException, IOException
    {
        final XMLUtil xu = new XMLUtil ();
        final URL url = getClass ().getResource ("test.xml");
        final Document doc = xu.getDocument (url);
        final Element root = doc.getDocumentElement ();
        assertEquals (4, xu.getChildren (root).size ());
        assertEquals (2, xu.getChildren (root, "GeoLine").size ());
    }

    @Test
    void testGetNthChild () throws ParserConfigurationException, SAXException, IOException
    {
        final XMLUtil xu = new XMLUtil ();
        final URL url = getClass ().getResource ("test.xml");
        final Document doc = xu.getDocument (url);
        final Element root = doc.getDocumentElement ();
        assertNotNull (xu.getNthChild (root, "GeoLine", 0));
        assertNotNull (xu.getNthChild (root, "GeoLine", 1));
        assertNull (xu.getNthChild (root, "GeoLine", 2));
        assertNotNull (xu.getNthChild (root, "angle", "l001$angle", 0));
        assertNull (xu.getNthChild (root, "angle", "l001$angle", 1));
    }

    @Test
    void testWriteXml () throws ParserConfigurationException, SAXException, IOException, TransformerException
    {
        final XMLUtil xu = new XMLUtil ();
        final URL url = getClass ().getResource ("test.xml");
        final Document doc = xu.getDocument (url);
        final File file = ts.getTestDataFile (this, "test", ".xml");
        xu.writeXml (doc, file);
        assertTrue (file.exists ());
        // TODO: Compare the two files
        file.delete ();
    }
}
