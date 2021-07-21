
package com.chriseliot.geo.gui;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.*;
import javax.swing.table.*;

import org.apache.logging.log4j.*;

import com.chriseliot.geo.*;
import com.chriseliot.util.*;
import com.opencsv.*;

/**
 * Toplevel Geometry calculator.
 *
 * @author cre
 */
public class Geo extends JPanel implements MouseListener, MouseMotionListener
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    public static void main (String[] args)
    {
        final Geo geo = new Geo ();
        geo.frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        geo.setup ();
        geo.open ();
    }

    private final TextUtils tu = new TextUtils ();
    /** Storage for all geometry items. */
    private final GeoPlane plane = new GeoPlane ();
    private final GeoSolution solution = new GeoSolution (plane);

    private final JFrame frame = new JFrame ("Geometry Solver");
    private final GeoControls controls = new GeoControls (this, false);
    private final JTable solutionTable = new JTable (solution);
    private final JScrollPane solutionScroll =
        new JScrollPane (solutionTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    /**
     * The nearby shape handle if the mouse is near something. This is what will be selected if the
     * mouse button is pressed.
     */
    private Point2D.Double mousePoint = null;

    /** The point that was clicked on. This may be snapped to a shape. */
    private Point2D.Double clickPoint = null;

    /** The current mouse drag point. This only has a value when the mouse is pressed. */
    private Point2D.Double dragPoint = null;

    /** The item the mouse is over. The tooltip should be painted from this. */
    private LabelItem hoverItem = null;

    /** Square of the maximum distance to snap a point. */
    private final int snapLimit = 30 * 30;

    /** Color to create new items with. */
    private Color createColor = Color.orange;

    /** Construct the window and initialize the frame. */
    public Geo ()
    {
        final JSplitPane splitPane = new JSplitPane (JSplitPane.HORIZONTAL_SPLIT, solutionScroll, this);
        splitPane.setOneTouchExpandable (true);
        splitPane.setDividerLocation (150);
        splitPane.setPreferredSize (new Dimension (1000, 1000));
        frame.setDefaultCloseOperation (JFrame.DISPOSE_ON_CLOSE);
        frame.setLayout (new BorderLayout ());
        frame.add (controls, BorderLayout.NORTH);
        frame.add (splitPane, BorderLayout.CENTER);
        frame.pack ();
    }

    /** Setup this panel. */
    public void setup ()
    {
        setPreferredSize (new Dimension (1000, 1000));
        setBackground (Color.black);
        addListeners ();
        solutionScroll.setPreferredSize (new Dimension (700, 1500));
        solutionTable.setPreferredSize (new Dimension (700, 1500));
        solutionTable.setAutoResizeMode (JTable.AUTO_RESIZE_OFF);
        solutionTable.setAutoCreateRowSorter (false);
        final TableColumn statusColumn = solutionTable.getColumnModel ().getColumn (GeoSolution.STATUS_COLUMN);
        final JComboBox<GeoStatus> comboBox = new JComboBox<> ();
        for (final GeoStatus status : GeoStatus.values ())
        {
            comboBox.addItem (status);
        }
        statusColumn.setCellEditor (new DefaultCellEditor (comboBox));
        solutionTable.setDefaultRenderer (Object.class, new DefaultTableCellRenderer ()
        {
            @Override
            public Component getTableCellRendererComponent (JTable table, Object value, boolean isSelected, boolean hasFocus,
                    int row, int column)
            {
                final Component c = super.getTableCellRendererComponent (table, value, isSelected, hasFocus, row, column);
                c.setForeground (solution.getCellColor (row, column));
                return c;
            }
        });
        solutionTable.setAutoCreateRowSorter (false);
        final TableRowSorter<GeoSolution> sorter = new TableRowSorter<> (solution);
        solutionTable.setRowSorter (sorter);
        sorter.setSortKeys (null);
        sorter.setRowFilter (solution.getRowFilter ());
        solutionTable.addMouseListener (new MouseAdapter ()
        {
            @Override
            public void mousePressed (MouseEvent mouseEvent)
            {
                final JTable table = (JTable)mouseEvent.getSource ();
                final Point point = mouseEvent.getPoint ();
                final int row = table.rowAtPoint (point);
                final int column = table.columnAtPoint (point);
                final GeoItem item = solution.getRowItem (row);
                logger.info ("Click on %s", item);
                if (item instanceof NamedVariable)
                {
                    final NamedVariable var = (NamedVariable)item;
                    final String[] terms = var.getTerms ();
                    if (terms != null)
                    {
                        final StringBuilder builder = new StringBuilder ();
                        for (int i = 0; i < terms.length; i++)
                        {
                            if (i > 0)
                            {
                                builder.append (", ");
                            }
                            builder.append (terms[i]);
                        }
                        logger.info ("Terms: %s", builder);
                    }
                }
                if (mouseEvent.getClickCount () == 2 && row != -1)
                {
                    final Object name = table.getValueAt (row, column);
                    logger.info ("Double click on %s", name);
                }
                frame.repaint ();
            }
        });
        // Use lambda to repaint when plane changes
        plane.addChangeListener (e -> frame.repaint (10));
    }

    public void open ()
    {
        frame.setVisible (true);
    }

    /** Clear the geometry plane. */
    public void clear ()
    {
        plane.clear ();
        solution.clear ();
        repaint ();
    }

    /** Storage for all geometry items. */
    public GeoPlane getPlane ()
    {
        return plane;
    }

    public GeoSolution getSolution ()
    {
        return solution;
    }

    public Color getCreateColor ()
    {
        return createColor;
    }

    public void setCreateColor (Color color)
    {
        createColor = color;
    }

    @Override
    public void paintComponent (Graphics g)
    {
        final int width = getWidth ();
        final int height = getHeight ();
        g.setColor (getBackground ());
        g.fillRect (0, 0, width, height);
        plane.paintItems (g);
        final Point2D.Double c = clickPoint;
        final Point2D.Double d = dragPoint;
        if (c != null && d != null)
        {
            final int cx = (int)round (c.x);
            final int cy = (int)round (c.y);
            final int dx = (int)round (d.x);
            final int dy = (int)round (d.y);
            g.setColor (Color.yellow);
            switch (controls.getSelected ())
            {
                case select:
                {
                    g.drawRect (dx - 2, dy - 2, 5, 5);
                    g.setColor (Color.LIGHT_GRAY);
                    g.drawLine (cx, cy, dx, dy);
                    break;
                }
                case line:
                {
                    g.drawLine (cx, cy, dx, dy);
                    break;
                }
                case rectangle:
                {
                    final int x = (int)round (min (c.x, d.x));
                    final int y = (int)round (min (c.y, d.y));
                    final int w = (int)round (abs (d.x - c.x));
                    final int h = (int)round (abs (d.y - c.y));
                    g.drawRect (x, y, w, h);
                    break;
                }
                case oval:
                {
                    final int w = (int)round (d.x - c.x);
                    final int h = (int)round (d.y - c.y);
                    g.drawOval (cx, cy, w, h);
                    break;
                }
                default:
                {
                    break;
                }
            }
        }
        g.setColor (Color.yellow);
        if (mousePoint != null)
        {
            final int mx = (int)round (mousePoint.x);
            final int my = (int)round (mousePoint.y);
            g.drawRect (mx - 2, my - 2, 5, 5);
        }
        if (hoverItem != null)
        {
            final String tooltip = hoverItem.getTooltip ();
            if (tooltip != null)
            {
                final FontMetrics metrics = g.getFontMetrics ();
                final int b = metrics.getDescent ();
                final int h = metrics.getHeight ();
                final int w = metrics.stringWidth (tooltip);
                final Rectangle bounds = hoverItem.getBounds ();
                final int x = bounds.x;
                final int y = bounds.y;
                g.setColor (Color.black);
                g.fillRect (x - 2, y - 2, w + 4, h + 4);
                g.setColor (Color.yellow);
                g.drawRect (x - 2, y - 2, w + 4, h + 4);
                g.drawString (tooltip, x, y + h - b);
            }
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

    private void addListeners ()
    {
        addMouseListener (this);
        addMouseMotionListener (this);
    }

    @Override
    public void mouseClicked (MouseEvent e)
    {
    }

    /**
     * Handle mouse pressed events. Determine the shape we are going to draw and prepare to drag
     * selection points. Implement snap points.
     */
    @Override
    public void mousePressed (MouseEvent e)
    {
        hoverItem = null;
        mousePoint = null;
        plane.deselectAll ();
        final Point p = e.getPoint ();
        final LabelItem label = plane.getLabels ().find (p);
        if (label != null)
        {
            final Object source = label.getSource ();
            logger.info ("Click on label %s: %s", label, source);
            if (source instanceof GeoItem)
            {
                final GeoItem item = (GeoItem)source;
                if (handleClick (p, item))
                {
                    return;
                }
            }
        }
        clickPoint = new Point2D.Double (p.x, p.y);
        final NamedPoint click = plane.getClickObject (clickPoint, snapLimit);
        if (click != null)
        {
            logger.info ("Click on %s", click);
            clickPoint = click.getPosition ();
        }
        plane.selectAll (plane.getDragPoints (clickPoint));
    }

    /**
     * Handle mouse released events. Remove the mouse motion listener and create the new geometry
     * item.
     */
    @Override
    public void mouseReleased (MouseEvent e)
    {
        final Point2D.Double c = clickPoint;
        final Point2D.Double d = dragPoint;
        clickPoint = null;
        dragPoint = null;
        final GeoShape geoShape = controls.getSelected ();
        if (c != null && d != null)
        {
            // This could be a switch but it is more compact as an if-then-else chain.
            if (geoShape == GeoShape.select)
            {
                plane.drag (c, d);
            }
            else if (geoShape == GeoShape.line)
            {
                plane.addItem (new GeoLine (plane, createColor, c, d));
            }
            else if (geoShape == GeoShape.rectangle)
            {
                plane.addItem (new GeoRectangle (plane, createColor, c, d));
            }
            else if (geoShape == GeoShape.oval)
            {
                plane.addItem (new GeoOval (plane, createColor, c, d));
            }
        }
        solution.update ();
        solutionTable.doLayout ();
        solutionTable.invalidate ();
        solutionTable.setShowGrid (true);
        repaint ();
    }

    @Override
    public void mouseEntered (MouseEvent e)
    {
    }

    @Override
    public void mouseExited (MouseEvent e)
    {
        hoverItem = null;
        repaint ();
    }

    @Override
    public void mouseDragged (MouseEvent e)
    {
        final Point p = e.getPoint ();
        dragPoint = new Point2D.Double (p.x, p.y);
        final Point2D.Double s = plane.getSnapPoint (dragPoint, snapLimit);
        if (s != null)
        {
            dragPoint = s;
        }
        repaint ();
    }

    @Override
    public void mouseMoved (MouseEvent e)
    {
        hoverItem = null;
        if (clickPoint == null)
        {
            Point2D.Double result = null;
            boolean refresh = (mousePoint != null);
            final Point p = e.getPoint ();
            final Point2D.Double s = plane.getSnapPoint (p, snapLimit);
            if (s != null)
            {
                result = s;
                refresh = true;
            }
            mousePoint = result;
            final Labels labels = plane.getLabels ();
            final LabelItem item = labels.find (p);
            if (item != null)
            {
                hoverItem = item;
                refresh = true;
            }
            if (refresh)
            {
                repaint ();
            }
        }
    }

    public boolean handleClick (Point p, GeoItem item)
    {
        final JPopupMenu popup = new JPopupMenu ();
        final JMenuItem known = new JMenuItem ("Known");
        final JMenuItem fixed = new JMenuItem ("Fixed");
        final JMenuItem unknown = new JMenuItem ("Unknown");
        final JMenuItem setValue = new JMenuItem ("Set Value");
        final JMenuItem showDerivation = new JMenuItem ("Show Derivation");
        known.addActionListener (evt -> item.setKnownAction ());
        unknown.addActionListener (evt -> item.setUnknownAction ());
        fixed.addActionListener (evt -> item.setFixedAction ());
        setValue.addActionListener (evt -> item.setValueAction ());
        showDerivation.addActionListener (evt -> item.showDerivationAction ());
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
        popup.show (this, p.x, p.y);
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

        final int userSelection = fileChooser.showSaveDialog (frame);

        if (userSelection == JFileChooser.APPROVE_OPTION)
        {
            final File file = fileChooser.getSelectedFile ();
            logger.info ("Saving to %s", file.getAbsolutePath ());
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
                for (final GeoItem item : plane.getItems ())
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
        }
    }

    /**
     * @see https://www.codejava.net/java-se/swing/show-simple-open-file-dialog-using-jfilechooser
     */
    public void read ()
    {
        final File currentDir = new File ("").getAbsoluteFile ();
        final JFileChooser fileChooser = new JFileChooser (currentDir);
        final int result = fileChooser.showOpenDialog (this);
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
            }
            catch (final Exception e)
            {
                e.printStackTrace ();
                // Don't do anything more if there is an error
                return;
            }
            clear ();
            // All objects have been read. Now we need to rebuild them.
            logger.info ("Restoring %d objects", objects.size ());
            for (final String name : objects.keySet ())
            {
                final Map<String, String> attributes = objects.get (name);
                logger.info ("%s: %s", name, attributes);
            }
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
        solution.update ();
        solutionTable.doLayout ();
        solutionTable.invalidate ();
        solutionTable.setShowGrid (true);
        repaint ();
    }
}
