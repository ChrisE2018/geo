
package com.chriseliot.geo.gui;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.List;

import javax.swing.JTable;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.chriseliot.geo.*;
import com.chriseliot.util.*;

public class FileSave
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    private final FileUtils fu = new FileUtils ();
    private final XMLUtil xu = new XMLUtil ();

    private final Geo geo;

    public FileSave (Geo geo)
    {
        this.geo = geo;
    }

    /**
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     * @throws TransformerException
     * @throws ParserConfigurationException
     *
     * @see https://www.codejava.net/java-se/swing/show-save-file-dialog-using-jfilechooser
     * @see https://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it
     * @see https://stackoverflow.com/questions/3651494/jfilechooser-with-confirmation-dialog
     */
    public void save () throws UnsupportedEncodingException, FileNotFoundException, IOException, ParserConfigurationException,
            TransformerException
    {
        final File currentDir = new File ("data/").getAbsoluteFile ();
        logger.info ("Current dir %s", currentDir);
        final File file = fu.getSaveFile (geo, "Save geometry to file", currentDir, ".xml");
        if (file != null)
        {
            logger.info ("Saving to %s", file.getAbsolutePath ());
            saveXml (file);
        }
    }

    public void saveXml (File file) throws ParserConfigurationException, IOException, TransformerException
    {
        final DocumentBuilder builder = xu.getDocumentBuilder ();
        final Document doc = builder.newDocument ();
        final Element root = doc.createElement ("geometry");
        doc.appendChild (root);
        // Save namer state
        // Save window state
        final GeoPlane plane = geo.getPlane ();
        for (final GeoItem item : plane.getRoots ())
        {
            item.getElement (root);
        }
        xu.writeXml (doc, file);
    }

    /**
     * @throws SAXException
     * @throws IOException
     * @throws ParserConfigurationException
     *
     * @see https://www.codejava.net/java-se/swing/show-simple-open-file-dialog-using-jfilechooser
     */
    public void read () throws ParserConfigurationException, IOException, SAXException
    {
        final File currentDir = new File ("data/").getAbsoluteFile ();
        final File file = fu.getReadFile (geo, "Select Geometry File", currentDir, "Select geometry xml", ".xml");
        if (file != null)
        {
            readXml (file);
        }
    }

    public void readXml (File file) throws ParserConfigurationException, IOException, SAXException
    {
        final GeoPlane plane = geo.getPlane ();
        plane.clear ();
        final Document doc = xu.getDocument (file);
        final Element root = doc.getDocumentElement ();
        final List<Element> toplevel = xu.getChildren (root);
        for (final Element element : toplevel)
        {
            marshall (plane, element);
        }
        final GeoSolution solution = geo.getSolution ();
        solution.update ();
        final JTable solutionTable = geo.getSolutionTable ();
        solutionTable.doLayout ();
        solutionTable.invalidate ();
        solutionTable.setShowGrid (true);
        geo.repaint ();
        logger.info ("Plane %s", plane);
    }

    private void marshall (GeoPlane plane, Element element)
    {
        final String tag = element.getTagName ();
        if (tag.equals ("GeoLine"))
        {
            final Color color = new Color (xu.getInteger (element, "color", 0));
            final Point2D.Double from = new Point2D.Double (0, 0);
            final Point2D.Double to = new Point2D.Double (0, 0);
            final GeoLine item = new GeoLine (plane, color, from, to);
            item.marshall (element);
        }
        else if (tag.equals ("GeoVertex"))
        {
            final Color color = new Color (xu.getInteger (element, "color", 0));
            final GeoLine line1 = (GeoLine)plane.get (xu.get (element, "line1", null));
            final GeoLine line2 = (GeoLine)plane.get (xu.get (element, "line2", null));
            final GeoVertex item = new GeoVertex (plane, color, line1, line2, new Point2D.Double (0, 0));
            plane.addItem (item);
            item.marshall (element);
        }
        else if (tag.equals ("GeoTriangle"))
        {
            final Color color = new Color (xu.getInteger (element, "color", 0));
            final GeoVertex v1 = (GeoVertex)plane.get (xu.get (element, "v1", null));
            final GeoVertex v2 = (GeoVertex)plane.get (xu.get (element, "v2", null));
            final GeoVertex v3 = (GeoVertex)plane.get (xu.get (element, "v3", null));
            final GeoTriangle item = new GeoTriangle (plane, color, v1, v2, v3);
            item.marshall (element);
        }
        else if (tag.equals ("GeoRectangle"))
        {
            final Color color = new Color (xu.getInteger (element, "color", 0));
            final GeoRectangle item = new GeoRectangle (plane, color, new Point2D.Double (0, 0), new Point2D.Double (0, 0));
            item.marshall (element);
        }
        else if (tag.equals ("GeoOval"))
        {
            final Color color = new Color (xu.getInteger (element, "color", 0));
            final GeoOval item = new GeoOval (plane, color, new Point2D.Double (0, 0), new Point2D.Double (0, 0));
            item.marshall (element);
        }
        else
        {
            logger.warn ("Can't marshall %s element", tag);
        }
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
