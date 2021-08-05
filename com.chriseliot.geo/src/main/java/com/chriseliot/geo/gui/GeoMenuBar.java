
package com.chriseliot.geo.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.print.*;
import java.io.*;
import java.util.*;

import javax.swing.*;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.logging.log4j.*;
import org.xml.sax.SAXException;

public class GeoMenuBar extends JMenuBar implements ActionListener
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    // File menu items
    private final JMenuItem newItem = new JMenuItem ("New");
    private final JMenuItem openItem = new JMenuItem ("Open");
    private final JMenuItem closeItem = new JMenuItem ("Close");
    private final JMenuItem saveItem = new JMenuItem ("Save");
    private final JMenuItem printItem = new JMenuItem ("Print...");
    private final JMenuItem quitItem = new JMenuItem ("Quit");

    // Edit menu items
    private final JMenuItem undoItem = new JMenuItem ("Undo");
    private final JMenuItem cutItem = new JMenuItem ("Cut");
    private final JMenuItem copyItem = new JMenuItem ("Copy");
    private final JMenuItem clearItem = new JMenuItem ("Clear");
    private final JMenuItem pasteItem = new JMenuItem ("Paste");

    // View menu items
    private final JRadioButtonMenuItem simpleItem = new JRadioButtonMenuItem ("Simple");
    private final JRadioButtonMenuItem standardItem = new JRadioButtonMenuItem ("Standard");
    private final JRadioButtonMenuItem detailItem = new JRadioButtonMenuItem ("Detail");

    // Select menu items
    private final JRadioButtonMenuItem selectItem = new JRadioButtonMenuItem ("Select");
    private final JRadioButtonMenuItem lineItem = new JRadioButtonMenuItem ("Create Line");
    private final JRadioButtonMenuItem circleItem = new JRadioButtonMenuItem ("Create Circle");
    private final JRadioButtonMenuItem rectangleItem = new JRadioButtonMenuItem ("Create Rectangle");

    private final Geo geo;

    public GeoMenuBar (Geo geo)
    {
        this.geo = geo;
        add (getFileMenu ());
        add (getEditMenu ());
        add (getViewMenu ());
        add (getSelectMenu ());
    }

    public JMenu getFileMenu ()
    {
        final JMenu result = new JMenu ("File");

        addMenuItem (result, newItem, false);
        addMenuItem (result, openItem);
        addMenuItem (result, closeItem, false);
        addMenuItem (result, saveItem);
        addMenuItem (result, printItem);
        addMenuItem (result, quitItem);

        return result;
    }

    public JMenu getEditMenu ()
    {
        final JMenu result = new JMenu ("Edit");
        addMenuItem (result, undoItem, false);
        addMenuItem (result, cutItem, false);
        addMenuItem (result, copyItem, false);
        addMenuItem (result, clearItem);
        addMenuItem (result, pasteItem, false);
        return result;
    }

    public JMenu getViewMenu ()
    {
        final ButtonGroup group = new ButtonGroup ();
        group.add (simpleItem);
        group.add (standardItem);
        group.add (detailItem);
        detailItem.setSelected (true);
        final JMenu result = new JMenu ("View");
        addMenuItem (result, simpleItem);
        addMenuItem (result, standardItem);
        addMenuItem (result, detailItem);
        return result;
    }

    public Set<String> getCategories ()
    {
        final Set<String> result = new TreeSet<> ();
        if (simpleItem.isSelected ())
        {
            result.add ("simple");
        }
        if (standardItem.isSelected ())
        {
            result.add ("standard");
        }
        if (detailItem.isSelected ())
        {
            result.add ("detail");
        }
        return result;
    }

    public JMenu getSelectMenu ()
    {
        final ButtonGroup group = new ButtonGroup ();
        group.add (selectItem);
        group.add (lineItem);
        group.add (circleItem);
        group.add (rectangleItem);
        lineItem.setSelected (true);
        final JMenu result = new JMenu ("Select");
        addMenuItem (result, selectItem);
        addMenuItem (result, lineItem);
        addMenuItem (result, circleItem);
        addMenuItem (result, rectangleItem);
        return result;
    }

    private void addMenuItem (JMenu menu, JMenuItem item, boolean enable)
    {
        item.setEnabled (enable);
        menu.add (item);
        item.addActionListener (this);
    }

    private void addMenuItem (JMenu menu, JMenuItem item)
    {
        menu.add (item);
        item.addActionListener (this);
    }

    @Override
    public void actionPerformed (ActionEvent e)
    {
        try
        {
            doActionEvent (e);
        }
        catch (final Exception ex)
        {
            ex.printStackTrace ();
        }
        geo.repaint ();
    }

    private void doActionEvent (ActionEvent e) throws UnsupportedEncodingException, FileNotFoundException, IOException,
            ParserConfigurationException, TransformerException, SAXException
    {
        final Object source = e.getSource ();
        logger.info ("Menu item %s", source);
        final GeoControls controls = geo.getControls ();
        // File menu
        if (source == newItem)
        {
            logger.info ("New item");
        }
        else if (source == openItem)
        {
            controls.read ();
        }
        else if (source == closeItem)
        {
            logger.info ("Close item");
        }
        else if (source == saveItem)
        {
            controls.save ();
        }
        else if (source == printItem)
        {
            logger.info ("Print item");
            print ();
        }
        else if (source == quitItem)
        {
            controls.quit ();
        }
        // Edit menu
        else if (source == undoItem)
        {
            logger.info ("Undo item");
        }
        else if (source == copyItem)
        {
            logger.info ("Copy item");
        }
        else if (source == clearItem)
        {
            logger.info ("Clear item");
            geo.clear ();
        }
        else if (source == pasteItem)
        {
            logger.info ("Paste item");
        }
        // View menu
        // Select menu
        else if (source == selectItem)
        {
            controls.setSelectSelected (true);
        }
        else if (source == lineItem)
        {
            controls.setLineSelected (true);
        }
        else if (source == circleItem)
        {
            controls.setOvalSelected (true);
        }
        else if (source == rectangleItem)
        {
            controls.setRectangleSelected (true);
        }
    }

    public void print ()
    {
        final PrinterJob job = PrinterJob.getPrinterJob ();
        job.setPrintable (new Printable ()
        {
            @Override
            public int print (Graphics g, PageFormat pageFormat, int pageIndex) throws PrinterException
            {
                // We have only one page, and 'page' is zero-based
                if (pageIndex > 0)
                {
                    return NO_SUCH_PAGE;
                }
                // User (0,0) is typically outside the imageable area, so we must translate by the X
                // and Y values in the PageFormat to avoid clipping.
                final Graphics2D g2d = (Graphics2D)g;
                g2d.translate (pageFormat.getImageableX (), pageFormat.getImageableY ());
                geo.print (g);
                return PAGE_EXISTS;
            }
        });
        final boolean doPrint = job.printDialog ();
        if (doPrint)
        {
            try
            {
                job.print ();
            }
            catch (final PrinterException e)
            {
                // The job did not successfully complete
                logger.error ("Printing failed");
            }
        }
        else
        {
            logger.warn ("Printing cancelled");
        }
    }
}
