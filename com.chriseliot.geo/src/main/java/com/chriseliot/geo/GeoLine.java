
package com.chriseliot.geo;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;

import org.apache.logging.log4j.*;

import com.chriseliot.util.Labels;

public class GeoLine extends GeoItem
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    /** Starting point of this line segment. */
    private final NamedPoint from;

    /** End point of this line segment. */
    private final NamedPoint to;

    /** Midpoint of this line segment. */
    private final NamedPoint midpoint;

    /**
     * Angle in degrees from the y axis. The angle of this line relative to the Y axis. A line from
     * <0, 0> to <0, 100> will have a y-angle of 0. This line is straight up on the mathematical
     * plane, but straight down using screen coordinates. As it rotates clockwise the angle
     * increases.
     */
    private final NamedVariable angle;

    /** Delta x from the endpoint to the start of this line segment. The value may be negative. */
    private final NamedVariable dx;

    /** Delta y from the endpoint to the start of this line segment. The value may be negative. */
    private final NamedVariable dy;

    /** Length of this line segment. Never negative. */
    private final NamedVariable length;

    private final List<GeoVertex> vertices = new ArrayList<> ();

    public GeoLine (GeoPlane plane, Color color, Point2D.Double from, Point2D.Double to)
    {
        super (plane, "l", color);
        final String name = getName ();
        this.from = new NamedPoint (this, true, color, name + SEP + "A", from, SwingConstants.SOUTH_WEST);
        this.to = new NamedPoint (this, true, color, name + SEP + "B", to, SwingConstants.SOUTH_WEST);
        // Midpoint should be draggable, but should move the line to a parallel location.
        midpoint = new TransposePoint (this, true, Color.blue, name + SEP + "M", (from.x + to.x) / 2, (from.y + to.y) / 2,
                SwingConstants.SOUTH_WEST);
        angle = new NamedVariable (midpoint, color, name + SEP + "angle");
        dx = new NamedVariable (midpoint, color, name + SEP + "dx");
        dy = new NamedVariable (midpoint, color, name + SEP + "dy");
        length = new NamedVariable (midpoint, color, name + SEP + "length");
        recalculate ();
    }

    /** Starting point of this line segment. */
    public NamedPoint getFrom ()
    {
        return from;
    }

    /** End point of this line segment. */
    public NamedPoint getTo ()
    {
        return to;
    }

    /** Midpoint of this line segment. */
    public NamedPoint getMidpoint ()
    {
        return midpoint;
    }

    /**
     * Angle in degrees from the y axis. The angle of this line relative to the Y axis. A line from
     * <0, 0> to <0, 100> will have a y-angle of 0. This line is straight up on the mathematical
     * plane, but straight down using screen coordinates. As it rotates clockwise the angle
     * increases.
     */
    public NamedVariable getAngle ()
    {
        return angle;
    }

    /** Delta x from the endpoint to the start of this line segment. The value may be negative. */
    public NamedVariable getDx ()
    {
        return dx;
    }

    /** Delta y from the endpoint to the start of this line segment. The value may be negative. */
    public NamedVariable getDy ()
    {
        return dy;
    }

    /** Length of this line segment. Never negative. */
    public NamedVariable getLength ()
    {
        return length;
    }

    /** Recalculate values derived from screen positions after a something moves. */
    @Override
    public void recalculate ()
    {
        // logger.debug ("Recalulate %s", this);
        final Point2D.Double a = from.getPosition ();
        final Point2D.Double b = to.getPosition ();
        final double midX = (a.x + b.x) / 2;
        final double midY = (a.y + b.y) / 2;
        midpoint.setPosition (new Point2D.Double (midX, midY));
        angle.setDoubleValue (axisYangle ());
        dx.setDoubleValue (b.x - a.x);
        dy.setDoubleValue (b.y - a.y);
        length.setDoubleValue (calculateLength ());
        // Lots of extra work here to avoid ConcurrentModificationException
        final List<GeoVertex> open = new ArrayList<> ();
        open.addAll (vertices);
        for (final GeoVertex v : open)
        {
            // This needs to iterate over a copy of the vertices list because the vertex might
            // delete itself and cause a ConcurrentModificationException
            v.recalculate ();
        }
        // Now look for newly created vertices
        final GeoPlane plane = getPlane ();
        final List<GeoItem> items = new ArrayList<> ();
        items.addAll (plane.getItems ());
        for (final GeoItem item : items)
        {
            if (item != this)
            {
                findVertices (item);
            }
        }
        plane.findTriangles ();
    }

    /**
     * Create vertices if this item crosses the other item.
     *
     * @param i An existing item to check.
     */
    @Override
    public void findVertices (GeoItem i)
    {
        if (i instanceof GeoLine)
        {
            final GeoLine l = (GeoLine)i;
            if (!hasVertex (l))
            {
                if (intersects (l))
                {
                    final Point2D.Double position = intersection (l);
                    if (position == null)
                    {
                        logger.warn ("Suppressing bad vertex %s X %s", this, l);
                    }
                    else
                    {
                        final GeoVertex v = new GeoVertex (getPlane (), Color.blue, this, l, position);
                        vertices.add (v);
                        l.vertices.add (v);
                    }
                }
            }
        }
    }

    /** Does this line have a vertex connection to the other line. */
    public boolean hasVertex (GeoLine l)
    {
        for (final GeoVertex v : vertices)
        {
            if (v.hasLine (l))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Return the vertex that connects to another line.
     *
     * @param l The line to connect by a vertex.
     *
     * @return The vertex connecting this line to the other line.
     */
    public GeoVertex getVertex (GeoLine l)
    {
        for (final GeoVertex v : vertices)
        {
            if (v.hasLine (l))
            {
                return v;
            }
        }
        return null;
    }

    @Override
    public List<GeoVertex> getVertices ()
    {
        return vertices;
    }

    public void remove (GeoVertex v)
    {
        vertices.remove (v);
    }

    /**
     * Remove the children of this item. This is called by the GeoPlane so this method should not
     * try to remove itself from the GeoPlane.
     *
     */
    @Override
    public void remove ()
    {
        for (final GeoVertex v : vertices)
        {
            v.remove ();
        }
    }

    private boolean intersects (GeoLine l)
    {
        final Line2D.Double l1 = getLine2D ();
        final Line2D.Double l2 = l.getLine2D ();
        if (l1.intersectsLine (l2))
        {
            return true;
        }
        return false;
    }

    public Point2D.Double intersection (double x1, double y1, double x2, double y2)
    {
        final Point2D.Double a = from.getPosition ();
        final Point2D.Double b = to.getPosition ();
        final double lx1 = a.x;
        final double ly1 = a.y;
        final double lx2 = b.x;
        final double ly2 = b.y;
        return intersection (lx1, ly1, lx2, ly2, x1, y1, x2, y2);
    }

    public Point2D.Double intersection (GeoLine l)
    {
        final Point2D.Double a = from.getPosition ();
        final Point2D.Double b = to.getPosition ();
        final double x1 = a.x;
        final double y1 = a.y;
        final double x2 = b.x;
        final double y2 = b.y;
        final Point2D.Double la = l.from.getPosition ();
        final Point2D.Double lb = l.to.getPosition ();
        final double x3 = la.x;
        final double y3 = la.y;
        final double x4 = lb.x;
        final double y4 = lb.y;
        return intersection (x1, y1, x2, y2, x3, y3, x4, y4);
    }

    /**
     *
     * @param x1
     * @param y1
     * @param x2
     * @param y2
     * @param x3
     * @param y3
     * @param x4
     * @param y4
     *
     * @return
     *
     * @see https://stackoverflow.com/questions/16314069/calculation-of-intersections-between-line-segments
     */
    public Point2D.Double intersection (double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
    {
        final double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        if (denom == 0.0)
        {
            // Lines are parallel.
            return null;
        }
        final double ua = ((x4 - x3) * (y1 - y3) - (y4 - y3) * (x1 - x3)) / denom;
        final double ub = ((x2 - x1) * (y1 - y3) - (y2 - y1) * (x1 - x3)) / denom;
        // Verify that the intersection is within the bounds of the segments
        if (ua >= 0.0 && ua <= 1.0 && ub >= 0.0f && ub <= 1.0)
        {
            // Get the intersection point.
            return new Point2D.Double (x1 + ua * (x2 - x1), y1 + ua * (y2 - y1));
        }

        return null;
    }

    public boolean isParallel (double x1, double y1, double x2, double y2, double x3, double y3, double x4, double y4)
    {
        final double denom = (y4 - y3) * (x2 - x1) - (x4 - x3) * (y2 - y1);
        return denom == 0;
    }

    /** Horizontal span of this line segment. */
    public double deltaX ()
    {
        final Point2D.Double a = from.getPosition ();
        final Point2D.Double b = to.getPosition ();
        return b.x - a.x;
    }

    /** Vertical span of this line segment. */
    public double deltaY ()
    {
        final Point2D.Double a = from.getPosition ();
        final Point2D.Double b = to.getPosition ();
        return b.y - a.y;
    }

    /**
     * Calculate slope of this line.
     *
     * @return The slope (dy / dx) or null if dx is zero.
     */
    public Double slope ()
    {
        final double dx = deltaX ();
        if (dx == 0)
        {
            return null;
        }
        return deltaY () / dx;
    }

    /**
     * The angle of this line relative to the X axis. A flat line from <0, 0> to <100, 0> will have
     * x-angle 0. As it rotates clockwise the angle increases.
     */
    public double axisXangle ()
    {
        final double dx = deltaX ();
        final double dy = deltaY ();
        return toDegrees (atan2 (dy, dx));
    }

    /**
     * The angle of this line relative to the Y axis. A line from <0, 0> to <0, 100> will have a
     * y-angle of 0. This line is straight up on the mathematical plane, but straight down using
     * screen coordinates. As it rotates clockwise the angle increases.
     */
    public double axisYangle ()
    {
        final double dx = deltaX ();
        final double dy = deltaY ();
        return toDegrees (atan2 (dx, dy));
    }

    /**
     * Angle between this line and another line.
     *
     * @param l
     *
     * @return Angle in degrees required to rotate the this line clockwise to the same angle as the
     *         other one.
     */
    public double angle (GeoLine l)
    {
        return axisXangle () - l.axisXangle ();
    }

    public Line2D.Double getLine2D ()
    {
        final Point2D.Double a = from.getPosition ();
        final Point2D.Double b = to.getPosition ();
        return new Line2D.Double (a.x, a.y, b.x, b.y);
    }

    /** Calculate square of the length of this line segment. */
    public double calculateLength2 ()
    {
        final double dx = deltaX ();
        final double dy = deltaY ();
        return dx * dx + dy * dy;
    }

    /** Calculate the length of this line segment. */
    public double calculateLength ()
    {
        return sqrt (calculateLength2 ());
    }

    /** Transpose the position of this item. */
    @Override
    public void move (double dx, double dy)
    {
        logger.info ("Move %s by %.2f %.2f", this, dx, dy);
        final Point2D.Double a = from.getPosition ();
        from.setPosition (a.x + dx, a.y + dy);
        final Point2D.Double b = to.getPosition ();
        to.setPosition (b.x + dx, b.y + dy);

        recalculate ();
    }

    @Override
    public void solve ()
    {
        if (getStatus ().isDetermined ())
        {
            // The from and to point values can be used when the line is derived
            // because the only way to derive the status of a line is when both from and to are
            // known
            if (!midpoint.getX ().isDetermined ())
            {
                final NamedVariable x = midpoint.getX ();
                x.setFormula ("midpoint x of line", "%s == (%s + %s) / 2", x, from.getX (), to.getX ());
            }
            if (!midpoint.getY ().isDetermined ())
            {
                final NamedVariable y = midpoint.getY ();
                y.setFormula ("midpoint y of line", "%s == (%s + %s) / 2", y, from.getY (), to.getY ());
            }
            if (!dx.isDetermined ())
            {
                dx.setFormula ("dx of line", "%s == (%s - %s)", dx, to.getX (), from.getX ());
            }
            if (!dy.isDetermined ())
            {
                dy.setFormula ("dy of line", "%s == (%s - %s)", dy, to.getY (), from.getY ());
            }
            if (!length.isDetermined ())
            {
                if (dx.isDetermined () && dy.isDetermined ())
                {
                    length.setFormula ("length of line", "%s == sqrt(%s ^ 2 + %s ^ 2)", length, dx, dy);
                }
            }
            if (!angle.isDetermined ())
            {
                angle.setFormula ("angle of line", "%s == arctan(%s, %s) / degree", angle, dx, dy);
            }
        }
        else if (from.isDetermined () && to.isDetermined ())
        {
            // This is the only way to derive the status of a line.
            // Hence, the from and to point values can be used when the line is derived.
            setStatus (GeoStatus.derived, "known endpoints");
        }
        else
        {
            if (angle.isDetermined () && length.isDetermined ())
            {
                if (!dx.isDetermined ())
                {
                    dx.setFormula ("dx from angle of line", "%s == %s * sin((90 -  %s) * Degree)", dx, length, angle);
                }
                if (!dy.isDetermined ())
                {
                    dy.setFormula ("dy from angle of line", "%s == %s * cos((90 - %s) * Degree)", dy, length, angle);
                }
            }
            if (from.getX ().isDetermined () && dx.isDetermined () && !to.getX ().isDetermined ())
            {
                to.getX ().setFormula ("to x from dx of line", "%s == %s + %s", to.getX (), from.getX (), dx);
            }
            if (from.getY ().isDetermined () && dy.isDetermined () && !to.getY ().isDetermined ())
            {
                to.getY ().setFormula ("to y from dy of line", "%s == %s + %s", to.getY (), from.getY (), dy);
            }
            if (to.getX ().isDetermined () && dx.isDetermined () && !from.getX ().isDetermined ())
            {
                from.getX ().setFormula ("from x from dx of line", "%s == %s - %s", from.getX (), to.getX (), dx);
            }
            if (to.getY ().isDetermined () && dy.isDetermined () && !from.getY ().isDetermined ())
            {
                from.getY ().setFormula ("from y from dy of line", "%s == %s - %s", from.getY (), to.getY (), dy);
            }
            if (from.getX ().isDetermined ())
            {
                if (midpoint.getX ().isDetermined ())
                {
                    if (!dx.isDetermined ())
                    {
                        dx.setFormula ("midpoint x of line", "%s == abs(%s - %s) * 2", dx, midpoint.getX (), from.getX ());
                    }
                }
                if (dx.isDetermined () && !to.getX ().isDetermined ())
                {
                    to.getX ().setFormula ("endpoint and dx", "%s == %s + %s", to.getX (), from.getX (), dx);

                }
            }
            if (from.getY ().isDetermined ())
            {
                if (midpoint.getY ().isDetermined ())
                {
                    if (!dy.isDetermined ())
                    {
                        dy.setFormula ("midpoint y of line", "%s == abs(%s - %s) * 2", dy, midpoint.getY (), from.getY ());
                    }
                }
                if (dy.isDetermined () && !to.getY ().isDetermined ())
                {
                    to.getY ().setFormula ("endpoint and dy", "%s == %s + %s", to.getY (), from.getY (), dy);
                }
            }

            if (to.getX ().isDetermined ())
            {
                if (midpoint.getX ().isDetermined ())
                {
                    if (!dx.isDetermined ())
                    {
                        dx.setFormula ("midpoint x of line", "%s == abs(%s - %s) * 2", dx, midpoint.getX (), to.getX ());
                    }
                }
                if (dx.isDetermined () && !from.getX ().isDetermined ())
                {
                    from.getX ().setFormula ("endpoint and dx", "%s == %s - %s", from.getX (), to.getX (), dx);

                }
            }
            if (to.getY ().isDetermined ())
            {
                if (midpoint.getY ().isDetermined ())
                {
                    if (!dy.isDetermined ())
                    {
                        dy.setFormula ("midpoint y of line", "%s == abs(%s - %s) * 2", dy, midpoint.getY (), from.getY ());
                    }
                }
                if (dy.isDetermined () && !from.getY ().isDetermined ())
                {
                    from.getY ().setFormula ("endpoint and dy", "%s == %s - %s", from.getY (), to.getY (), dy);
                }
            }
        }
    }

    @Override
    public void paint (Graphics g, Labels labels)
    {
        g.setColor (getStatus ().getColor ());
        final Point a = from.getIntPosition ();
        final Point b = to.getIntPosition ();
        final float size = isSelected () ? 3.0f : 1.0f;
        final Graphics2D gg = (Graphics2D)g;
        final Stroke stroke = gg.getStroke ();
        gg.setStroke (new BasicStroke (size));
        g.drawLine (a.x, a.y, b.x, b.y);
        gg.setStroke (stroke);
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
        final Point2D.Double a = from.getPosition ();
        buffer.append (String.format ("<%.2f %.2f>", a.x, a.y));
        buffer.append (" ");
        final Point2D.Double b = to.getPosition ();
        buffer.append (String.format ("<%.2f %.2f>", b.x, b.y));
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
