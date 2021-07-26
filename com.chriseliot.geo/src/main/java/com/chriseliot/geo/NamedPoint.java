
package com.chriseliot.geo;

import static java.lang.Math.round;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.function.Consumer;

import org.w3c.dom.Element;

import com.chriseliot.geo.gui.NamedPointActions;
import com.chriseliot.util.Labels;

public class NamedPoint extends GeoItem
{
    /**
     * What part of the label is anchored to the position. A SOUTH_WEST anchor will have the south
     * west point of the label anchored to the position, so the label will be above and to the right
     * of the position. In general, use SOUTH anchor for labels above the position, NORTH anchor for
     * labels below the position. Use WEST anchor for labels to the right of the position and EAST
     * anchor for labels to the left.
     */
    private final int anchor;

    /** Can the user drag this point with the mouse. */
    private final boolean draggable;

    private Point2D.Double position;

    /** The x coordinate. */
    private final NamedVariable x;

    /** The y coordinate. */
    private final NamedVariable y;

    /**
     * Construct a named point.
     *
     * @param parent The GeoItem containing this point.
     * @param draggable Should dragging be allowed.
     * @param color The color to use for this point.
     * @param name Name of this NamedPoint.
     * @param position Position on screen for this point.
     * @param anchor What part of the label is anchored to the position.
     */
    public NamedPoint (GeoItem parent, boolean draggable, Color color, String name, Point2D.Double position, int anchor)
    {
        super (parent, name, color);
        this.draggable = draggable;
        this.anchor = anchor;
        this.position = position;
        x = new NamedVariable (this, color, name + SEP + "x", position.x);
        y = new NamedVariable (this, color, name + SEP + "y", position.y);
    }

    /**
     * Construct a named point.
     *
     * @param parent The GeoItem containing this point.
     * @param draggable Should dragging be allowed.
     * @param color The color to use when drawing this item.
     * @param name Name of this NamedPoint.
     * @param x Position on screen for this point.
     * @param y Position on screen for this point.
     * @param anchor What part of the label is anchored to the position.
     */
    public NamedPoint (GeoItem parent, boolean draggable, Color color, String name, double x, double y, int anchor)
    {
        super (parent, name, color);
        position = new Point2D.Double (x, y);
        this.draggable = draggable;
        this.anchor = anchor;
        this.x = new NamedVariable (this, color, name + SEP + "x", x);
        this.y = new NamedVariable (this, color, name + SEP + "y", y);
    }

    /** Can the user drag this point with the mouse. */
    public boolean isDraggable ()
    {
        return draggable;
    }

    /**
     * Set the position of this point.
     *
     * @param position The new position, cannot be null.
     */
    public void setPosition (Point2D.Double position)
    {
        this.position = position;
    }

    public void setPosition (double x, double y)
    {
        position = new Point2D.Double (x, y);
    }

    /** The x coordinate. */
    public NamedVariable getX ()
    {
        return x;
    }

    /** The y coordinate. */
    public NamedVariable getY ()
    {
        return y;
    }

    /** Drag to a new location. */
    public void drag (Point2D.Double d)
    {
        position = d;
        recalculate ();
    }

    /**
     * @return the position
     */
    public Point2D.Double getPosition ()
    {
        return position;
    }

    /** The closest integer Point to this position. */
    public Point getIntPosition ()
    {
        return new Point ((int)round (position.x), (int)round (position.y));
    }

    @Override
    public void recalculate ()
    {
        x.setDoubleValue (position.x);
        y.setDoubleValue (position.y);
    }

    /**
     * What part of the label is anchored to the position. A SOUTH_WEST anchor will have the south
     * west point of the label anchored to the position, so the label will be above and to the right
     * of the position. In general, use SOUTH anchor for labels above the position, NORTH anchor for
     * labels below the position. Use WEST anchor for labels to the right of the position and EAST
     * anchor for labels to the left.
     */
    public int getAnchor ()
    {
        return anchor;
    }

    public double distance2 (Point p)
    {
        return distance2 (getPosition (), p.x, p.y);
    }

    public double distance2 (Point2D.Double p)
    {
        return distance2 (getPosition (), p);
    }

    public double distance2 (NamedPoint p)
    {
        return distance2 (getPosition (), p.getPosition ());
    }

    private double distance2 (Point2D.Double p, Point2D.Double q)
    {
        final double dx = q.x - p.x;
        final double dy = q.y - p.y;
        return dx * dx + dy * dy;
    }

    private double distance2 (Point2D.Double p, double qx, double qy)
    {
        final double dx = qx - p.x;
        final double dy = qy - p.y;
        return dx * dx + dy * dy;
    }

    public boolean at (NamedPoint p)
    {
        return at (p.getPosition ());
    }

    public boolean at (Point2D.Double p)
    {
        if (p != null)
        {
            final Point2D.Double pos = getPosition ();
            return pos.x == p.x && pos.y == p.y;
        }
        return false;
    }

    /** Derive inferences from this item. */
    @Override
    public void solve ()
    {
        if (isDetermined ())
        {
            if (!x.isDetermined ())
            {
                x.setFormula ("definition of point", "%s == " + x.getDoubleValue (), x);
            }
            if (!y.isDetermined ())
            {
                y.setFormula ("definition of point", "%s == " + y.getDoubleValue (), y);
            }
        }
        else if (x.isDetermined () && y.isDetermined ())
        {
            setStatus (GeoStatus.derived, "definition of point");
        }
    }

    /** If two points are at the same screen position, make them equivalent. */
    public void equivalent (NamedPoint p)
    {
        if (p.getX ().isDetermined () && !getX ().isDetermined ())
        {
            getX ().setFormula ("equivalent pont x", "%s == %s", getX (), p.getX ());
        }
        if (p.getY ().isDetermined () && !getY ().isDetermined ())
        {
            getY ().setFormula ("equivalent pont y", "%s == %s", getY (), p.getY ());
        }

        if (!p.getX ().isDetermined () && getX ().isDetermined ())
        {
            p.getX ().setFormula ("equivalent pont x", "%s == %s", p.getX (), getX ());
        }
        if (!p.getY ().isDetermined () && getY ().isDetermined ())
        {
            p.getY ().setFormula ("equivalent pont y", "%s == %s", p.getY (), getY ());
        }
    }

    /**
     * Paint this item in its current state. The Graphics object is normally the window component,
     * but could be a printer or BufferedImage.
     *
     * @param g The paint destination.
     */
    @Override
    public void paint (Graphics g, Labels labels)
    {
        final Color color = getStatus ().getColor ();
        final Point2D.Double pos = getPosition ();
        final Point position = getIntPosition ();
        final int anchor = getAnchor ();
        final String text = (getStatus () != GeoStatus.derived) ? getName () : getName () + "*";
        final String tooltip = String.format ("%s <%.1f %.1f>", getName (), pos.x, pos.y);
        labels.add (this, color, position, anchor, text, tooltip);
    }

    /**
     * Populate a popup menu with required items. This should be overridden by subclasses. Be sure
     * to call the super method.
     *
     * @param result
     */
    @Override
    public void popup (Map<String, Consumer<GeoItem>> result)
    {
        super.popup (result);
        final NamedPointActions actions = new NamedPointActions ();
        result.put ("Set Value", evt -> actions.setValueAction (this));
    }

    public void setValueAction (Point2D.Double result)
    {
        getPlane ().drag (position, result);
    }

    @Override
    public void getAttributes (Element element)
    {
        super.getAttributes (element);
        element.setAttribute ("x", x.getName ());
        element.setAttribute ("y", y.getName ());
    }

    @Override
    public void marshall (Element element)
    {
        super.marshall (element);
        x.marshall (xu.getNthChild (element, "name", xu.get (element, "x", null), 0));
        y.marshall (xu.getNthChild (element, "name", xu.get (element, "y", null), 0));
        position.x = x.getDoubleValue ();
        position.y = y.getDoubleValue ();
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        buffer.append (" ");
        buffer.append (getName ());
        buffer.append (" ");
        buffer.append (getParent ().getClass ().getSimpleName ());
        final Point2D.Double pos = getPosition ();
        buffer.append (" ");
        buffer.append (String.format ("<%.2f, %.2f>", pos.x, pos.y));
        buffer.append (" ");
        buffer.append (getStatus ());
        final String reason = getReason ();
        if (reason != null)
        {
            buffer.append (" ");
            buffer.append ("'");
            buffer.append (getReason ());
            buffer.append ("'");
        }
        buffer.append (">");
        return buffer.toString ();
    }
}
