
package com.chriseliot.geo.gui;

import static java.lang.Math.abs;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;
import java.util.function.Consumer;

import javax.swing.*;

import org.apache.logging.log4j.*;

import com.chriseliot.geo.*;
import com.chriseliot.util.*;

public class GeoMouse implements MouseListener, MouseMotionListener
{
    /** Length of shortest line to create by click and drag. */
    private static final double MINIMUM_LINE_DISTANCE = 10;

    /** Length squared of shortest line to create by click and drag. */
    private static final double MINIMUM_LINE_DISTANCE2 = MINIMUM_LINE_DISTANCE * MINIMUM_LINE_DISTANCE;

    /** If a line endpoint is this close to an axis, make it align with the axis. */
    private final double LINE_AXIS_SNAP = 5;

    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    private final Geo geo;

    /** Square of the maximum distance to snap a point. */
    private final int snapLimit = 30 * 30;

    /** Color to create new items with. */
    private Color createColor = Color.orange;

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

    public GeoMouse (Geo geo)
    {
        this.geo = geo;
    }

    public Color getCreateColor ()
    {
        return createColor;
    }

    public void setCreateColor (Color color)
    {
        createColor = color;
    }

    public GeoPlane getPlane ()
    {
        return geo.getPlane ();
    }

    public Point2D.Double getMousePoint ()
    {
        return mousePoint;
    }

    public Point2D.Double getClickPoint ()
    {
        return clickPoint;
    }

    public Point2D.Double getDragPoint ()
    {
        return dragPoint;
    }

    public LabelItem getHoverItem ()
    {
        return hoverItem;
    }

    @Override
    public void mouseClicked (MouseEvent e)
    {
        final Point p = e.getPoint ();
        final GeoPlane plane = getPlane ();
        final LabelItem label = plane.getLabels ().find (p);
        if (label != null)
        {
            final Object source = label.getSource ();
            if (source instanceof GeoItem)
            {
                logger.info ("Click on label %s: %s", label, source);
                final GeoItem item = (GeoItem)source;
                if (handleClick (p, item))
                {
                    return;
                }
            }
        }
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
        final GeoPlane plane = getPlane ();
        plane.deselectAll ();
        final Point p = e.getPoint ();
        clickPoint = new Point2D.Double (p.x, p.y);
        final NamedPoint click = plane.getClickObject (clickPoint, snapLimit);
        if (click != null)
        {
            logger.info ("Click on %s", click);
            final Point2D.Double position = click.getPosition ();
            final List<NamedPoint> dragPoints = plane.getDragPoints (position);
            for (final NamedPoint np : dragPoints)
            {
                if (np.getX ().getStatus () == GeoStatus.fixed || np.getY ().getStatus () == GeoStatus.fixed)
                {
                    // Don't allow dragging of fixed positions
                    clickPoint = null;
                    return;
                }
            }
            clickPoint = position;
            plane.selectAll (dragPoints);
        }
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
        final GeoShape geoShape = geo.getControls ().getSelected ();
        if (c != null && d != null)
        {
            final GeoPlane plane = getPlane ();
            // This could be a switch but it is more compact as an if-then-else chain.
            if (geoShape == GeoShape.select)
            {
                plane.drag (c, d);
            }
            else if (geoShape == GeoShape.line)
            {
                if (getPlane ().distance2 (c, d) > MINIMUM_LINE_DISTANCE2)
                {
                    plane.addItem (new GeoLine (plane, createColor, c, d));
                }
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
        final GeoSolution solution = geo.getSolution ();
        solution.update ();
        final JTable solutionTable = geo.getSolutionTable ();
        solutionTable.doLayout ();
        solutionTable.invalidate ();
        solutionTable.setShowGrid (true);
        geo.repaint ();
    }

    @Override
    public void mouseEntered (MouseEvent e)
    {
    }

    @Override
    public void mouseExited (MouseEvent e)
    {
        hoverItem = null;
        geo.repaint ();
    }

    @Override
    public void mouseDragged (MouseEvent e)
    {

        final Point p = e.getPoint ();
        dragPoint = new Point2D.Double (p.x, p.y);
        final Point2D.Double s = getPlane ().getSnapPoint (dragPoint, snapLimit);
        if (s != null)
        {
            dragPoint = s;
        }
        else
        {
            final GeoShape geoShape = geo.getControls ().getSelected ();
            if (geoShape == GeoShape.line)
            {
                final double dx = abs (clickPoint.x - dragPoint.x);
                if (dx < LINE_AXIS_SNAP)
                {
                    dragPoint.x = clickPoint.x;
                }
                final double dy = abs (clickPoint.y - dragPoint.y);
                if (dy < LINE_AXIS_SNAP)
                {
                    dragPoint.y = clickPoint.y;
                }
            }
        }
        geo.repaint ();
    }

    @Override
    public void mouseMoved (MouseEvent e)
    {
        hoverItem = null;
        if (clickPoint == null)
        {
            final GeoPlane plane = getPlane ();
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
                geo.repaint ();
            }
        }
    }

    /** Handler for popup menu driven by click on an item. */
    public boolean handleClick (Point p, GeoItem item)
    {
        final Map<String, Consumer<GeoItem>> menuItems = new LinkedHashMap<> ();
        item.popup (menuItems);
        final JPopupMenu popup = new JPopupMenu ();
        for (final Entry<String, Consumer<GeoItem>> entry : menuItems.entrySet ())
        {
            final String key = entry.getKey ();
            final Consumer<GeoItem> action = entry.getValue ();
            final JMenuItem menuItem = new JMenuItem (key);
            if (action == null)
            {
                menuItem.setEnabled (false);
            }
            else
            {
                menuItem.addActionListener (evt -> action.accept (item));
            }
            popup.add (menuItem);
        }
        popup.show (geo, p.x, p.y);
        geo.repaint (10);
        return true;
    }
}
