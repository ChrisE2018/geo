
package com.chriseliot.geo.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.swing.event.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.*;
import org.xml.sax.SAXException;

import com.chriseliot.geo.*;
import com.chriseliot.util.Namer;

public class GeoControls extends JPanel implements ActionListener
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

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

    /** Set true when changes need to be saved to a file. */
    private boolean dirty = false;

    private static boolean ENABLE_SAVE_CHANGES_ALERTS = true;

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

        /**
         * If it is made possible to change the GeoPlane then this listener will have to be updated.
         */
        geo.getPlane ().addChangeListener (new ChangeListener ()
        {
            @Override
            public void stateChanged (ChangeEvent e)
            {
                dirty = true;
            }
        });
    }

    /** Set true when changes need to be saved to a file. */
    public boolean isDirty ()
    {
        return dirty;
    }

    /** Set true when changes need to be saved to a file. */
    public void setDirty (boolean dirty)
    {
        this.dirty = dirty;
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
            final FileSave fileSave = new FileSave (geo);
            fileSave.save ();
            dirty = false;
        }
        else if (source == read)
        {
            if (ENABLE_SAVE_CHANGES_ALERTS)
            {
                if (dirty)
                {
                    // Ask if the user wants to save.
                    final int result = JOptionPane.showConfirmDialog (geo, "Save changes before read?");
                    if (result == JOptionPane.CANCEL_OPTION)
                    {
                        return;
                    }
                    if (result == JOptionPane.YES_OPTION)
                    {
                        final FileSave fileSave = new FileSave (geo);
                        fileSave.save ();
                        dirty = false;
                    }
                }
            }
            final FileSave fileSave = new FileSave (geo);
            fileSave.read ();
            dirty = false;
        }
        else if (source == quit)
        {
            if (ENABLE_SAVE_CHANGES_ALERTS)
            {
                if (dirty)
                {
                    // Ask if the user wants to save.
                    final int result = JOptionPane.showConfirmDialog (geo, "Save changes before quit?");
                    if (result == JOptionPane.CANCEL_OPTION)
                    {
                        return;
                    }
                    if (result == JOptionPane.YES_OPTION)
                    {
                        final FileSave fileSave = new FileSave (geo);
                        fileSave.save ();
                        dirty = false;
                    }
                }
            }
            System.exit (0);
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
