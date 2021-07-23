
package com.chriseliot.geo;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.geom.Point2D;

import javax.swing.SwingConstants;

import com.chriseliot.util.Labels;

public class GeoRectangle extends GeoItem
{
    private final Point2D.Double from;
    private final Point2D.Double to;
    private final NamedPoint tl;
    private final NamedPoint tr;
    private final NamedPoint bl;
    private final NamedPoint br;
    private final NamedPoint center;
    private final NamedVariable width;
    private final NamedVariable height;

    public GeoRectangle (GeoPlane plane, Color color, Point2D.Double from, Point2D.Double to)
    {
        super (plane, "R", color);
        final double minx = min (from.x, to.x);
        final double miny = min (from.y, to.y);
        this.from = new Point2D.Double (minx, miny);
        final double dx = abs (to.x - from.x);
        final double dy = abs (to.y - from.y);
        this.to = new Point2D.Double (this.from.x + dx, this.from.y + dy);
        final String name = getName ();
        // Top left
        tl = new NamedPoint (this, true, color, name + SEP + "tl", from, SwingConstants.SOUTH_WEST);
        // Top right
        tr = new NamedPoint (this, true, color, name + SEP + "tr", to.x, from.y, SwingConstants.SOUTH_EAST);
        // Bottom left
        bl = new NamedPoint (this, true, color, name + SEP + "bl", from.x, to.y, SwingConstants.NORTH_WEST);
        // Bottom right
        br = new NamedPoint (this, true, color, name + SEP + "br", to, SwingConstants.NORTH_EAST);

        // = abs(bottom_right.x - top_left.x)
        width = new NamedVariable (this, color, name + SEP + "w");
        // = abs(bottom_right.y - top_left.y)
        height = new NamedVariable (this, color, name + SEP + "h");
        center = new TransposePoint (this, true, color, name + SEP + "C", (from.x + to.x) * 0.5, (from.y + to.y) * 0.5,
                SwingConstants.CENTER);
    }

    public NamedPoint getTopLeft ()
    {
        return tl;
    }

    public NamedPoint getTopRight ()
    {
        return tr;
    }

    public NamedPoint getBottomLeft ()
    {
        return bl;
    }

    public NamedPoint getBottomRight ()
    {
        return br;
    }

    public NamedVariable getWidth ()
    {
        return width;
    }

    public NamedVariable getHeight ()
    {
        return height;
    }

    @Override
    public void paint (Graphics g, Labels labels)
    {
        g.setColor (getStatus ().getColor ());
        final int w = (int)round (to.x - from.x);
        final int h = (int)round (to.y - from.y);
        final float size = isSelected () ? 3.0f : 1.0f;

        final Graphics2D gg = (Graphics2D)g;
        final Stroke stroke = gg.getStroke ();
        gg.setStroke (new BasicStroke (size));
        g.drawRect ((int)round (from.x), (int)round (from.y), w, h);
        gg.setStroke (stroke);
        labels.add (this, getStatus ().getColor (), center.getIntPosition (), SwingConstants.SOUTH_WEST, getName ());
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
