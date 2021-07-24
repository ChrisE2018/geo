
package com.chriseliot.geo.gui;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.apache.logging.log4j.*;

import com.chriseliot.geo.*;
import com.chriseliot.util.*;
import com.opencsv.*;

public class GeoControls extends JPanel implements ActionListener
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());
    private final FileUtils fu = new FileUtils ();
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
        catch (final IOException ex)
        {
            ex.printStackTrace ();
        }
        geo.repaint ();
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
     *
     * @see https://www.codejava.net/java-se/swing/show-save-file-dialog-using-jfilechooser
     * @see https://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it
     * @see https://stackoverflow.com/questions/3651494/jfilechooser-with-confirmation-dialog
     */
    public void save () throws UnsupportedEncodingException, FileNotFoundException, IOException
    {
        final File currentDir = new File ("data/").getAbsoluteFile ();
        logger.info ("Current dir %s", currentDir);
        final JFileChooser fileChooser = new JFileChooser (currentDir)
        {
            @Override
            public void approveSelection ()
            {
                final File f = getSelectedFile ();
                if (f.exists () && getDialogType () == SAVE_DIALOG)
                {
                    final int result = JOptionPane.showConfirmDialog (this, "The file exists, overwrite?", "Existing file",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result)
                    {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection ();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection ();
                            return;
                    }
                }
                super.approveSelection ();
            }
        };
        fileChooser.setDialogTitle ("Save geometry to file");

        final int userSelection = fileChooser.showSaveDialog (geo);

        if (userSelection == JFileChooser.APPROVE_OPTION)
        {
            final File file = fu.setExtension (fileChooser.getSelectedFile (), ".csv");
            logger.info ("Saving to %s", file.getAbsolutePath ());
            final GeoPlane plane = geo.getPlane ();
            try (CSVWriter stream = new CSVWriter (new OutputStreamWriter (new FileOutputStream (file), "utf-8")))
            {
                final Set<String> keys = new TreeSet<> ();
                for (final GeoItem item : plane.getItems ())
                {
                    final Map<String, Object> attributes = item.getAttributes ();
                    keys.addAll (attributes.keySet ());
                }
                final String[] header = new String[keys.size ()];
                keys.toArray (header);
                stream.writeNext (header);
                for (final GeoItem item : geo.getPlane ().getItems ())
                {
                    final Map<String, Object> attributes = item.getAttributes ();
                    // We need to match the order of the keys which are in a sorted TreeSet.
                    final List<String> values = new ArrayList<> ();
                    for (final String key : keys)
                    {
                        final Object value = attributes.get (key);
                        if (value == null)
                        {
                            values.add ("");
                        }
                        else
                        {
                            values.add (value.toString ());
                        }
                    }
                    final String[] data = new String[keys.size ()];
                    values.toArray (data);
                    stream.writeNext (data);
                }
            }
            geo.getFrame ().setTitle (file.getAbsolutePath ());
        }
    }

    /**
     * @see https://www.codejava.net/java-se/swing/show-simple-open-file-dialog-using-jfilechooser
     */
    public void read ()
    {
        final File currentDir = new File ("data/").getAbsoluteFile ();
        final JFileChooser fileChooser = new JFileChooser (currentDir);
        final int result = fileChooser.showOpenDialog (geo);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File file = fileChooser.getSelectedFile ();
            System.out.println ("Selected file: " + file.getAbsolutePath ());
            final Map<String, Map<String, String>> objects = new HashMap<> ();
            // parsing a CSV file into CSVReader class constructor
            try (final CSVReader reader = new CSVReader (new FileReader (file)))
            {
                String[] header = null;
                String[] data;
                // reads one line at a time
                while ((data = reader.readNext ()) != null)
                {
                    if (header == null)
                    {
                        header = data;
                    }
                    else
                    {
                        final Map<String, String> attributes = new LinkedHashMap<> ();
                        for (int i = 0; i < header.length; i++)
                        {
                            final String key = header[i];
                            final String value = data[i];
                            attributes.put (key, value);
                        }
                        final String name = attributes.get ("name");
                        objects.put (name, attributes);
                    }
                }
                geo.getFrame ().setTitle (file.getAbsolutePath ());
            }
            catch (final Exception e)
            {
                e.printStackTrace ();
                // Don't do anything more if there is an error
                return;
            }
            geo.clear ();
            // All objects have been read. Now we need to rebuild them.
            logger.info ("Restoring %d objects", objects.size ());
            for (final String name : objects.keySet ())
            {
                final Map<String, String> attributes = objects.get (name);
                logger.info ("%s: %s", name, attributes);
            }
            final GeoPlane plane = geo.getPlane ();
            boolean building = true;
            while (building)
            {
                building = false;
                logger.info ("Building objects in plane with %d objects", plane.getItems ().size ());
                for (final String name : objects.keySet ())
                {
                    final Map<String, String> attributes = objects.get (name);
                    GeoItem item = plane.get (name);
                    final String parent = attributes.get ("parent");

                    if (item == null)
                    {
                        if (parent.isEmpty ())
                        {
                            // Create a toplevel item
                            logger.info ("Ready to build toplevel %s: %s", name, attributes);
                            final String classname = attributes.get ("classname");
                            if (classname.equals (GeoLine.class.getCanonicalName ()))
                            {
                                // The from and to values will be read later
                                final Point2D.Double from = new Point2D.Double (0, 0);
                                final Point2D.Double to = new Point2D.Double (0, 0);
                                item = new GeoLine (plane, Color.black, from, to);
                                building = true;
                                logger.info ("Created %s", item);
                            }
                            // TODO: Handle GeoVertex
                            // TODO: Handle GeoOval
                            // TODO: Handle GeoRectangle
                            else
                            {
                                logger.info ("Can't create toplevel %s", classname);
                            }
                        }
                        else
                        {
                            logger.info ("Trying to build %s child of %s: %s", name, parent, attributes);
                            final GeoItem parentItem = plane.get (parent);
                            logger.info ("Parent %s: %s = %s", name, parent, parentItem);
                            if (parentItem != null)
                            {
                                // Ready to build this item
                                logger.info ("Ready to build %s child of %s: %s", name, parentItem, attributes);
                            }
                        }
                    }
                    if (item != null)
                    {
                        // Item already exits or was just created. Change it to match attributes.
                        logger.info ("Update %s: %s", name, attributes);
                        item.readAttributes (attributes);
                        // logger.info ("Item %s: %s", name, item);
                    }
                }
            }
        }
        // plane.solve ();
        final GeoSolution solution = geo.getSolution ();
        final JTable solutionTable = geo.getSolutionTable ();
        solution.update ();
        solutionTable.doLayout ();
        solutionTable.invalidate ();
        solutionTable.setShowGrid (true);
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
