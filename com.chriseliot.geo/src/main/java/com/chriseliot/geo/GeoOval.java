
package com.chriseliot.geo;

import static java.lang.Math.round;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingConstants;

import com.chriseliot.util.Labels;

public class GeoOval extends GeoItem
{
    private final NamedPoint from;
    private final NamedPoint to;
    private final NamedPoint center;

    public GeoOval (GeoPlane plane, Color color, Point2D.Double from, Point2D.Double to)
    {
        super (plane, "O", color);
        final String name = getName ();
        this.from = new NamedPoint (this, true, color, name + SEP + "A", from, SwingConstants.SOUTH_WEST);
        this.to = new NamedPoint (this, true, color, name + SEP + "B", to, SwingConstants.NORTH_EAST);
        center = new TransposePoint (this, true, color, name + SEP + "C", (from.x + to.x) * 0.5, (from.y + to.y) * 0.5,
                SwingConstants.CENTER);
        addCategory ("simple");
        addCategory ("standard");
        center.addCategory ("standard");
    }

    /** Recalculate values derived from screen positions after a something moves. */
    @Override
    public void recalculate ()
    {
        final Point2D.Double a = from.getPosition ();
        final Point2D.Double b = to.getPosition ();
        final double cx = (a.x + b.x) / 2;
        final double cy = (a.y + b.y) / 2;
        final Point2D.Double c = new Point2D.Double (cx, cy);
        center.setPosition (c);
    }

    @Override
    public void paint (Graphics g, Labels labels)
    {
        g.setColor (getStatus ().getColor ());
        final Point2D.Double a = from.getPosition ();
        final Point2D.Double b = to.getPosition ();
        final int w = (int)round (b.x - a.x);
        final int h = (int)round (b.y - a.y);
        final float size = isSelected () ? 3.0f : 1.0f;

        final Graphics2D gg = (Graphics2D)g;
        final Stroke stroke = gg.getStroke ();
        gg.setStroke (new BasicStroke (size));
        g.drawOval ((int)round (a.x), (int)round (a.y), w, h);
        gg.setStroke (stroke);
        labels.add (this, getStatus ().getColor (), center.getIntPosition (), SwingConstants.SOUTH_WEST, getName ());
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
        result.put ("Delete", item -> item.remove ());
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
