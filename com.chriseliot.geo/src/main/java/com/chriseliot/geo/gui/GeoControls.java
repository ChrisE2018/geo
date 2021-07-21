
package com.chriseliot.geo.gui;

import java.awt.*;
import java.awt.event.*;
import java.io.*;

import javax.swing.*;

import org.apache.logging.log4j.*;

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
        final ButtonGroup group = new ButtonGroup ();
        group.add (select);
        group.add (line);
        group.add (rectangle);
        group.add (oval);
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

        add (clear);
        add (expandAll);
        add (unknownAll);
        add (save);
        add (read);
        add (quit);
        color.addActionListener (this);
        backColor.addActionListener (this);
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
        catch (final IOException ex)
        {
            ex.printStackTrace ();
        }
    }

    private void doAction (Object source) throws UnsupportedEncodingException, FileNotFoundException, IOException
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
                item.setDefaultFormula ();
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
            geo.save ();
        }
        else if (source == read)
        {
            geo.read ();
        }
        else if (source == quit)
        {
            System.exit (0);
        }
        geo.repaint ();
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
