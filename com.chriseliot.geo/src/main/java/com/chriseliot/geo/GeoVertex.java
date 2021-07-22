
package com.chriseliot.geo;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import javax.swing.SwingConstants;

import org.apache.logging.log4j.*;

import com.chriseliot.util.Labels;

public class GeoVertex extends GeoItem
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    /** The first intersecting line. Which is first and which is second is arbitrary. */
    private final GeoLine line1;

    /** The second intersecting line. Which is first and which is second is arbitrary. */
    private final GeoLine line2;

    /** Screen position of this vertex. */
    private Point2D.Double position;

    /**
     * Screen position of this vertex. This should be correctly computed to correspond with the
     * intersection point.
     */
    private final NamedPoint vertex;

    /**
     * Value of vertex angle. Degrees required to rotate line1 clockwise to line2. Fully
     * understanding this value is difficult because the angle of the lines depends on which
     * endpoint is first (the line direction).
     */
    private final NamedVariable angle;

    /** List of triangles using this vertex as a corner. A vertex can be part of many triangles. */
    private final List<GeoTriangle> triangles = new ArrayList<> ();

    /**
     * Construct a vertex from two intersecting lines. The lines may intersect only at endpoints or
     * somewhere in the middle.
     *
     * @param plane The geometric plane.
     * @param color The color to display this vertex. This is obsolete, since the color is generally
     *            taken from the status.
     * @param line1 The first intersecting line. Which is first and which is second is arbitrary.
     * @param line2 The second intersecting line. Which is first and which is second is arbitrary.
     * @param position The position of this vertex. This should be correctly computed to correspond
     *            with the intersection point.
     */
    public GeoVertex (GeoPlane plane, Color color, GeoLine line1, GeoLine line2, Point2D.Double position)
    {
        super (plane, "v", color);
        this.line1 = line1;
        this.line2 = line2;
        this.position = position;
        final String name = getName ();
        vertex = new NamedPoint (this, true, color, name + SEP + "p", position, SwingConstants.SOUTH_WEST);
        plane.addItem (vertex);
        final double theta1 = line1.angle (line2);
        // Make the vertex point be the parent of the angle so the NamedVariable has a location.
        angle = new NamedVariable (vertex, color, name + SEP + "v", theta1);
    }

    /** The first intersecting line. Which is first and which is second is arbitrary. */
    public GeoLine getLine1 ()
    {
        return line1;
    }

    /** The second intersecting line. Which is first and which is second is arbitrary. */
    public GeoLine getLine2 ()
    {
        return line2;
    }

    /** Screen position of this vertex. */
    public Point2D.Double getPosition ()
    {
        return position;
    }

    public Point getIntPosition ()
    {
        if (position != null)
        {
            return new Point ((int)round (position.x), (int)round (position.y));
        }
        return null;
    }

    /** Screen position of this vertex. */
    public void setPosition (Point2D.Double position)
    {
        this.position = position;
    }

    /** Screen position of this vertex. */
    public NamedPoint getVertex ()
    {
        return vertex;
    }

    /** Compare with screen position of this vertex. */
    public boolean at (Point2D.Double p)
    {
        return position.equals (p);
    }

    /**
     * Value of vertex angle. Degrees required to rotate line1 clockwise to line2. Fully
     * understanding this value is difficult because the angle of the lines depends on which
     * endpoint is first (the line direction).
     */
    public NamedVariable getAngle ()
    {
        return angle;
    }

    /** Angle in degrees to a point from this vertex. */
    public double getAngleTo (Point2D.Double p)
    {
        return (getAngleFrom (p) + 180) % 360;
    }

    /**
     * Angle in degrees from a point to this vertex. Generally, this is getAngleTo(p) + 180 mod 360.
     * This is used to sort the vertices of a triangle into clockwise order by sorting the angle
     * from the triangle centroid to each vertex.
     */
    public double getAngleFrom (Point2D.Double p)
    {
        return (Math.toDegrees (Math.atan2 (position.x - p.x, position.y - p.y)) + 360) % 360;
    }

    // /**
    // * The smaller positive angle. This is probably obsolete. It was an attempt to determine the
    // * inner angle of a triangle.
    // */
    // public double getMinAbsAngle ()
    // {
    // return min (abs (angle1.getDoubleValue ()), abs (angle2.getDoubleValue ()));
    // }
    //
    // /**
    // * The larger positive angle. This is probably obsolete. It was an attempt to determine the
    // * inner angle of a triangle.
    // */
    // public double getMaxAbsAngle ()
    // {
    // return max (abs (angle1.getDoubleValue ()), abs (angle2.getDoubleValue ()));
    // }

    // /**
    // * Is a line through this vertex pointing toward another vertex or not.
    // *
    // * @param line The line to check.
    // * @param v The other vertex to check.
    // *
    // * @return True if the second endpoint of the line is at or beyond vertex v.
    // */
    // public boolean isDirectedTo (GeoLine line, GeoVertex v)
    // {
    // final Point2D.Double pos = getPosition ();
    // final Point2D.Double vpos = v.getPosition ();
    // final NamedPoint a = line.getFrom ();
    // final NamedPoint b = line.getTo ();
    // final Point2D.Double ap = a.getPosition ();
    // final Point2D.Double bp = b.getPosition ();
    // if (ap.x < bp.x)
    // {
    // return pos.x < vpos.x;
    // }
    // if (ap.y < bp.y)
    // {
    // return pos.y < vpos.y;
    // }
    // // Not allowed for the vertex points to match.
    // return false;
    // }

    /** Does this vertex connect to the line. */
    public boolean hasLine (GeoLine l)
    {
        return line1 == l || line2 == l;
    }

    /** Does this vertex have a line connecting to the other vertex. */
    public boolean connects (GeoVertex v)
    {
        return v.hasLine (line1) || v.hasLine (line2);
    }

    /** Get the line connecting to the other vertex. */
    public GeoLine connector (GeoVertex v)
    {
        if (v.hasLine (line1))
        {
            return line1;
        }
        if (v.hasLine (line2))
        {
            return line2;
        }
        return null;
    }

    /** Square of the distance to another vertex. */
    public double distance2 (GeoVertex v)
    {
        final double dx = position.x - v.position.x;
        final double dy = position.y - v.position.y;
        return dx * dx + dy * dy;
    }

    /** Distance to another vertex. */
    public double distance (GeoVertex v)
    {
        return sqrt (distance2 (v));
    }

    /** Record that this vertex is part of a triangle. A vertex can be part of many triangles. */
    public void addTriangle (GeoTriangle t)
    {
        triangles.add (t);
    }

    /** List of triangles using this vertex as a corner. A vertex can be part of many triangles. */
    public List<GeoTriangle> getTriangles ()
    {
        return triangles;
    }

    /** Recalculate values derived from screen positions after a something moves. */
    @Override
    public void recalculate ()
    {
        position = line1.intersection (line2);
        if (position == null)
        {
            logger.info ("Removing %s", this);
            getPlane ().remove (this);
            line1.remove (this);
            line2.remove (this);
        }
        else
        {
            vertex.setPosition (position);
            final double theta1 = line1.angle (line2);
            angle.setDoubleValue (theta1);
        }
    }

    @Override
    public void solve ()
    {
        if (line1.getAngle ().isDetermined () && line2.getAngle ().isDetermined ())
        {
            if (!angle.isDetermined ())
            {
                angle.setFormula ("vertex angles", "%s == %s - %s", angle, line2.getAngle (), line1.getAngle ());
            }
        }
        if (isDetermined ())
        {
            if (!vertex.isDetermined ())
            {
                vertex.setStatus (GeoStatus.derived, "vertex known");
            }
            if (!angle.isDetermined ())
            {
                angle.setStatus (GeoStatus.derived, "vertex known");
            }
        }
        if (!isDetermined ())
        {
            if (angle.isDetermined ())
            {
                if (vertex.isDetermined ())
                {
                    setStatus (GeoStatus.derived, "vertex determined");
                }
            }
        }
    }

    /**
     * Remove the children of this item. This is called by the GeoPlane so this method should not
     * try to remove itself from the GeoPlane.
     */
    @Override
    public void remove ()
    {
        line1.remove (this);
        line2.remove (this);
        for (final GeoTriangle t : triangles)
        {
            logger.info ("Removing triangle %s", t);
            getPlane ().remove (t);
        }
    }

    @Override
    public void paint (Graphics g, Labels labels)
    {
        final Point p = getIntPosition ();
        if (p != null)
        {
            final int x = p.x - 7;
            final int y = p.y - 7;
            final float size = isSelected () ? 3.0f : 1.0f;
            g.setColor (getStatus ().getColor ());
            final Graphics2D gg = (Graphics2D)g;
            final Stroke stroke = gg.getStroke ();
            gg.setStroke (new BasicStroke (size));
            g.drawOval (x, y, 15, 15);
            gg.setStroke (stroke);
            labels.add (this, getStatus ().getColor (), p, SwingConstants.SOUTH_WEST, getName (), toString ());
        }
    }

    /**
     * Get named attributes. Used for saving to a csv file. This method should be overriden by
     * subclasses.
     *
     * @param result Map to store attributes.
     */
    @Override
    public void getAttributes (Map<String, Object> result)
    {
        result.put ("positionx", position.x);
        result.put ("positionx", position.y);
        result.put ("angle", angle.getDoubleValue ());
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
        if (position == null)
        {
            buffer.append ("[MISSING]");
        }
        else
        {
            buffer.append (String.format ("<%.3f, %.3f>", position.x, position.y));
        }
        buffer.append (" ");
        buffer.append (line1.getName ());
        buffer.append (" ");
        buffer.append (line2.getName ());
        buffer.append (" ");
        buffer.append (String.format ("[%.3f]", angle.getDoubleValue ()));
        buffer.append (">");
        return buffer.toString ();
    }
}
