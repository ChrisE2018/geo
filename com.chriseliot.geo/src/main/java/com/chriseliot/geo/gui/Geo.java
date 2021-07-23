
package com.chriseliot.geo.gui;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;

import javax.swing.*;
import javax.swing.table.*;

import org.apache.logging.log4j.*;

import com.chriseliot.geo.*;
import com.chriseliot.util.LabelItem;

/**
 * Toplevel Geometry calculator.
 *
 * @author cre
 */
public class Geo extends JPanel
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    public static void main (String[] args)
    {
        final Geo geo = new Geo ();
        geo.frame.setDefaultCloseOperation (JFrame.EXIT_ON_CLOSE);
        geo.setup ();
        geo.open ();
    }

    /** Storage for all geometry items. */
    private final GeoPlane plane = new GeoPlane ();
    private final GeoSolution solution = new GeoSolution (plane);

    private final JFrame frame = new JFrame ("Geometry Solver");

    /** Panel for user controls. */
    private final GeoControls controls = new GeoControls (this, false);
    private final JTable solutionTable = new JTable (solution);
    private final JScrollPane solutionScroll =
        new JScrollPane (solutionTable, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

    private final GeoMouse geoMouse = new GeoMouse (this);

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
                    final NamedVariable[] terms = var.getTerms ();
                    if (terms != null)
                    {
                        final StringBuilder builder = new StringBuilder ();
                        for (int i = 0; i < terms.length; i++)
                        {
                            if (i > 0)
                            {
                                builder.append (", ");
                            }
                            builder.append (terms[i].getName ());
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

    /** Panel for user controls. */
    public GeoControls getControls ()
    {
        return controls;
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

    public JTable getSolutionTable ()
    {
        return solutionTable;
    }

    public JFrame getFrame ()
    {
        return frame;
    }

    public Color getCreateColor ()
    {
        return geoMouse.getCreateColor ();
    }

    public void setCreateColor (Color color)
    {
        geoMouse.setCreateColor (color);
    }

    @Override
    public void paintComponent (Graphics g)
    {
        final int width = getWidth ();
        final int height = getHeight ();
        g.setColor (getBackground ());
        g.fillRect (0, 0, width, height);
        plane.paintItems (g, controls.getCategories ());
        final Point2D.Double c = geoMouse.getClickPoint ();
        final Point2D.Double d = geoMouse.getDragPoint ();
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
        final Point2D.Double mousePoint = geoMouse.getMousePoint ();
        if (mousePoint != null)
        {
            final int mx = (int)round (mousePoint.x);
            final int my = (int)round (mousePoint.y);
            g.drawRect (mx - 2, my - 2, 5, 5);
        }
        final LabelItem hoverItem = geoMouse.getHoverItem ();
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
        addMouseListener (geoMouse);
        addMouseMotionListener (geoMouse);
    }
}
