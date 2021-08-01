
package com.chriseliot.geo.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.xml.parsers.*;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.*;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import com.chriseliot.geo.*;
import com.chriseliot.util.*;

public class GeoControls extends JPanel implements ActionListener
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());
    private final FileUtils fu = new FileUtils ();
    private final XMLUtil xu = new XMLUtil ();
    private final Geo geo;
    private final JCheckBox select = new JCheckBox ("Select", false);
    private final JCheckBox line = new JCheckBox ("Line", true);
    private final JCheckBox rectangle = new JCheckBox ("Rectangle", false);
    private final JCheckBox oval = new JCheckBox ("Oval", false);
    private final JCheckBox simpleCategory = new JCheckBox ("Simple", false);
    private final JCheckBox standardCategory = new JCheckBox ("Standard", true);
    private final JCheckBox detailCategory = new JCheckBox ("Detail", false);
    private final JButton color = new JButton ("Color");
    private final JButton backColor = new JButton ("Background");
    private final JButton clear = new JButton ("Clear");
    private final JButton expandAll = new JButton ("Expand All");
    private final JButton unknownAll = new JButton ("Unknown All");
    private final JButton debug = new JButton ("Debug");
    private final JButton save = new JButton ("Save");
    private final JButton read = new JButton ("Read");
    private final JButton quit = new JButton ("Quit");

    /**
     * Create the control panel.
     *
     * @param geo The application window.
     * @param preview Control showing "preview" features that are not ready for release.
     */
    public GeoControls (Geo geo, boolean preview)
    {
        this.geo = geo;
        setPreferredSize (new Dimension (700, 75));
        final ButtonGroup toolGroup = new ButtonGroup ();
        toolGroup.add (select);
        toolGroup.add (line);
        toolGroup.add (rectangle);
        toolGroup.add (oval);
        add (select);
        add (line);
        if (preview)
        {
            add (rectangle);
            add (oval);
            add (color);
            add (backColor);
            add (debug);
        }

        final ButtonGroup categoryGroup = new ButtonGroup ();
        categoryGroup.add (simpleCategory);
        categoryGroup.add (standardCategory);
        categoryGroup.add (detailCategory);

        add (simpleCategory);
        add (standardCategory);
        add (detailCategory);
        add (expandAll);
        add (unknownAll);
        add (clear);
        add (save);
        add (read);
        add (quit);
        color.addActionListener (this);
        backColor.addActionListener (this);
        simpleCategory.addActionListener (this);
        standardCategory.addActionListener (this);
        detailCategory.addActionListener (this);

        clear.addActionListener (this);
        expandAll.addActionListener (this);
        unknownAll.addActionListener (this);
        debug.addActionListener (this);
        save.addActionListener (this);
        read.addActionListener (this);
        quit.addActionListener (this);
    }

    public GeoShape getSelected ()
    {
        if (isSelectSelected ())
        {
            return GeoShape.select;
        }
        if (isLineSelected ())
        {
            return GeoShape.line;
        }
        if (isRectangleSelected ())
        {
            return GeoShape.rectangle;
        }
        if (isOvalSelected ())
        {
            return GeoShape.oval;
        }
        return GeoShape.none;
    }

    public boolean isSelectSelected ()
    {
        return select.isSelected ();
    }

    public boolean isLineSelected ()
    {
        return line.isSelected ();
    }

    public boolean isRectangleSelected ()
    {
        return rectangle.isSelected ();
    }

    public boolean isOvalSelected ()
    {
        return oval.isSelected ();
    }

    public Set<String> getCategories ()
    {
        final Set<String> result = new TreeSet<> ();
        if (simpleCategory.isSelected ())
        {
            result.add ("simple");
        }
        if (standardCategory.isSelected ())
        {
            result.add ("standard");
        }
        if (detailCategory.isSelected ())
        {
            result.add ("detail");
        }
        return result;
    }

    @Override
    public void paintComponent (Graphics g)
    {
        final int width = getWidth ();
        final int height = getHeight ();
        g.setColor (getBackground ());
        g.fillRect (0, 0, width, height);
        g.setColor (getForeground ());
    }

    @Override
    public void actionPerformed (ActionEvent e)
    {
        final Object source = e.getSource ();
        try
        {
            doAction (source);
        }
        catch (final Exception ex)
        {
            ex.printStackTrace ();
        }
        geo.repaint ();
    }

    private void doAction (Object source) throws UnsupportedEncodingException, FileNotFoundException, IOException,
            ParserConfigurationException, TransformerException, SAXException
    {
        if (source == color)
        {
            logger.info ("Color");
            final Color color = JColorChooser.showDialog (this, "Color to create items", geo.getCreateColor ());
            geo.setCreateColor (color);
        }
        else if (source == backColor)
        {
            logger.info ("Background Color");
            final Color color = JColorChooser.showDialog (this, "Background color", geo.getBackground ());
            geo.setBackground (color);
            geo.repaint ();
        }
        else if (source == clear)
        {
            logger.info ("Clear");
            geo.clear ();
            Namer.reset ();
        }
        else if (source == expandAll)
        {
            geo.getSolution ().expandAll ();
        }
        else if (source == unknownAll)
        {
            for (final GeoItem item : geo.getPlane ().getItems ())
            {
                item.setStatusUnknown ();
            }
            geo.getPlane ().resetDerived ();
        }
        else if (source == debug)
        {
            for (final GeoItem item : geo.getPlane ().getItems ())
            {
                logger.info ("Item: %s", item);
            }
            for (final GeoItem item : geo.getPlane ().getItems ())
            {
                if (item instanceof GeoVertex)
                {
                    logger.info ("Vertex: %s", item);
                }
            }
        }
        else if (source == save)
        {
            save ();
        }
        else if (source == read)
        {
            read ();
        }
        else if (source == quit)
        {
            System.exit (0);
        }
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
            plane.addItem (item);
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
            plane.addItem (item);
            item.marshall (element);
        }
        else if (tag.equals ("GeoRectangle"))
        {
            final Color color = new Color (xu.getInteger (element, "color", 0));
            final GeoRectangle item = new GeoRectangle (plane, color, new Point2D.Double (0, 0), new Point2D.Double (0, 0));
            plane.addItem (item);
            item.marshall (element);
        }
        else if (tag.equals ("GeoOval"))
        {
            final Color color = new Color (xu.getInteger (element, "color", 0));
            final GeoOval item = new GeoOval (plane, color, new Point2D.Double (0, 0), new Point2D.Double (0, 0));
            plane.addItem (item);
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
