
package com.chriseliot.geo;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.function.Consumer;

import javax.swing.SwingConstants;

import org.w3c.dom.Element;

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
        addCategory ("simple");
        addCategory ("standard");
        center.addCategory ("standard");
        tl.addCategory ("standard");
        tr.addCategory ("standard");
        bl.addCategory ("standard");
        br.addCategory ("standard");
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
    public void getAttributes (Element element)
    {
        super.getAttributes (element);
        element.setAttribute ("tl", tl.getName ());
        element.setAttribute ("tr", tr.getName ());
        element.setAttribute ("bl", bl.getName ());
        element.setAttribute ("br", br.getName ());
        element.setAttribute ("center", center.getName ());
        element.setAttribute ("width", width.getName ());
        element.setAttribute ("height", height.getName ());
    }

    @Override
    public void marshall (Element element)
    {
        super.marshall (element);
        tl.marshall (xu.getNthChild (element, "name", xu.get (element, "tl", null), 0));
        tr.marshall (xu.getNthChild (element, "name", xu.get (element, "tr", null), 0));
        bl.marshall (xu.getNthChild (element, "name", xu.get (element, "bl", null), 0));
        br.marshall (xu.getNthChild (element, "name", xu.get (element, "br", null), 0));
        center.marshall (xu.getNthChild (element, "name", xu.get (element, "center", null), 0));
        width.marshall (xu.getNthChild (element, "name", xu.get (element, "width", null), 0));
        height.marshall (xu.getNthChild (element, "name", xu.get (element, "height", null), 0));
        from.x = tl.getPosition ().x;
        from.y = tl.getPosition ().y;
        to.x = br.getPosition ().x;
        to.y = br.getPosition ().y;
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
