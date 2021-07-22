
package com.chriseliot.geo.gui;

import java.awt.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;

import org.apache.logging.log4j.*;

import com.chriseliot.geo.*;
import com.chriseliot.util.FileUtils;
import com.opencsv.*;

public class GeoClick
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());
    private final FileUtils fu = new FileUtils ();

    private final Geo geo;

    public GeoClick (Geo geo)
    {
        this.geo = geo;
    }

    public boolean handleClick (Point p, GeoItem item)
    {
        final JPopupMenu popup = new JPopupMenu ();
        final JMenuItem known = new JMenuItem ("Known");
        final JMenuItem fixed = new JMenuItem ("Fixed");
        final JMenuItem unknown = new JMenuItem ("Unknown");
        final JMenuItem setValue = new JMenuItem ("Set Value");
        final JMenuItem renameVariable = new JMenuItem ("Rename Variable");
        final JMenuItem showDerivation = new JMenuItem ("Show Derivation");
        final JMenuItem showSolution = new JMenuItem ("Show Solution");
        known.addActionListener (evt -> item.setKnownAction ());
        unknown.addActionListener (evt -> item.setUnknownAction ());
        fixed.addActionListener (evt -> item.setFixedAction ());
        setValue.addActionListener (evt -> item.setValueAction ());
        setValue.addActionListener (evt -> item.setValueAction ());
        renameVariable.addActionListener (evt -> item.renameVariableAction ());
        showDerivation.addActionListener (evt -> item.showDerivationAction ());
        showSolution.addActionListener (evt -> item.showSolutionAction ());
        if (item.canSetKnown ())
        {
            popup.add (known);
        }
        if (item.canSetFixed ())
        {
            popup.add (fixed);
        }
        if (item.canSetUnknown ())
        {
            popup.add (unknown);
        }
        if (item.canSetValue ())
        {
            popup.add (setValue);
        }
        if (item.canShowDerivation ())
        {
            popup.add (showDerivation);
        }
        if (item.canShowSolution ())
        {
            popup.add (showSolution);
        }
        if (item.canRenameVariable ())
        {
            popup.add (renameVariable);
        }
        popup.show (geo, p.x, p.y);
        return true;
    }

    /**
     * @throws IOException
     * @throws FileNotFoundException
     * @throws UnsupportedEncodingException
     *
     * @see https://www.codejava.net/java-se/swing/show-save-file-dialog-using-jfilechooser
     * @see https://stackoverflow.com/questions/2885173/how-do-i-create-a-file-and-write-to-it
     */
    public void save () throws UnsupportedEncodingException, FileNotFoundException, IOException
    {
        final File currentDir = new File ("").getAbsoluteFile ();
        logger.info ("Current dir %s", currentDir);
        final JFileChooser fileChooser = new JFileChooser (currentDir);
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
        final File currentDir = new File ("").getAbsoluteFile ();
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
}
