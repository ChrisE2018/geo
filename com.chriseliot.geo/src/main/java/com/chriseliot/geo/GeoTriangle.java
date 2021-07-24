
package com.chriseliot.geo;

import static java.lang.Math.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import javax.swing.SwingConstants;

import org.apache.logging.log4j.*;

import com.chriseliot.util.Labels;

public class GeoTriangle extends GeoItem
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    /** Difference to allow for double float equality comparison. */
    private final double epsilon = 0.000001;

    /**
     * Vertex between line3 and line1. Opposite l1. V1 is the first vertex clockwise starting north
     * of the triangle centroid.
     */
    private final GeoVertex v1;

    /**
     * Vertex between line1 and line2. Opposite l2. V2 is clockwise from v1.
     */
    private final GeoVertex v2;

    /**
     * Vertex between line2 and line3. Opposite l3. V3 is clockwise from v2.
     */
    private final GeoVertex v3;

    /** Length from vertex 2 to vertex 3. This is the length of the side opposite v1. */
    private final CentroidVariable l1;

    /** Length from vertex 3 to vertex 1. This is the length of the side opposite v2. */
    private final CentroidVariable l2;

    /** Length from vertex 1 to vertex 2. This is the length of the side opposite v3. */
    private final CentroidVariable l3;

    /**
     * Angle of vertex 3 (degrees). This is the angle from line3 to line1. Opposite l1. This is at
     * the same corner as vertex v1.
     */
    private final TriangleAngleVariable angle1;

    /**
     * Angle of vertex 3 (degrees). This is the angle from line1 to line2. Opposite l2. This is at
     * the same corner as vertex v2.
     */
    private final TriangleAngleVariable angle2;

    /**
     * Angle of vertex 2 (degrees). This is the angle from line2 to line3. Opposite l3. This is at
     * the same corner as vertex v3.
     */
    private final TriangleAngleVariable angle3;

    private final NamedPoint centroid;

    public GeoTriangle (GeoPlane plane, Color color, GeoVertex iv1, GeoVertex iv2, GeoVertex iv3)
    {
        super (plane, "t", color);
        // @see
        // https://math.stackexchange.com/questions/978642/how-to-sort-vertices-of-a-polygon-in-counter-clockwise-order
        final Point2D.Double c = centroid (iv1, iv2, iv3);
        final Map<Double, GeoVertex> sort = new TreeMap<> ();
        sort.put (iv1.getAngleFrom (c), iv1);
        sort.put (iv2.getAngleFrom (c), iv2);
        sort.put (iv3.getAngleFrom (c), iv3);
        final List<GeoVertex> sorted = new ArrayList<> (sort.values ());
        v1 = sorted.get (0);
        v2 = sorted.get (1);
        v3 = sorted.get (2);
        v1.addTriangle (this);
        v2.addTriangle (this);
        v3.addTriangle (this);
        final String name = getName ();
        // Compute length of sides opposite each vertex
        l1 = new CentroidVariable (this, color, name + SEP + "l1", v2.distance (v3));
        l2 = new CentroidVariable (this, color, name + SEP + "l2", v3.distance (v1));
        l3 = new CentroidVariable (this, color, name + SEP + "l3", v1.distance (v2));
        l1.setLocation (v2.getVertex (), v3.getVertex ());
        l2.setLocation (v3.getVertex (), v1.getVertex ());
        l3.setLocation (v1.getVertex (), v2.getVertex ());

        // Angle opposite vertex 1 is angle between side l3 and l2.
        angle1 = new TriangleAngleVariable (this, color, name + SEP + "angle1",
                theta (l3.getDoubleValue (), l2.getDoubleValue (), l1.getDoubleValue ()));
        // Angle opposite vertex 2 is angle between side l1 and l3.
        angle2 = new TriangleAngleVariable (this, color, name + SEP + "angle2",
                theta (l1.getDoubleValue (), l3.getDoubleValue (), l2.getDoubleValue ()));
        // Angle opposite vertex 3 is angle between side l1 and l2.
        angle3 = new TriangleAngleVariable (this, color, name + SEP + "angle3",
                theta (l1.getDoubleValue (), l2.getDoubleValue (), l3.getDoubleValue ()));
        angle1.setLocation (v1.getVertex ());
        angle2.setLocation (v2.getVertex ());
        angle3.setLocation (v3.getVertex ());

        centroid = new NamedPoint (this, true, color, name + SEP + "C", centroid (), SwingConstants.SOUTH_WEST);
        addCategory ("simple");
        addCategory ("standard");
        l1.addCategory ("standard");
        l2.addCategory ("standard");
        l3.addCategory ("standard");
        angle1.addCategory ("standard");
        angle2.addCategory ("standard");
        angle3.addCategory ("standard");
    }

    /**
     * Compute centroid of triangle formed by three vertices.
     *
     * @see https://math.stackexchange.com/questions/978642/how-to-sort-vertices-of-a-polygon-in-counter-clockwise-order
     */
    public Point2D.Double centroid ()
    {
        return centroid (v1, v2, v3);
    }

    /**
     * Compute centroid of triangle formed by three vertices.
     *
     * @see https://math.stackexchange.com/questions/978642/how-to-sort-vertices-of-a-polygon-in-counter-clockwise-order
     */
    public Point2D.Double centroid (GeoVertex v1, GeoVertex v2, GeoVertex v3)
    {
        return centroid (v1.getPosition (), v2.getPosition (), v3.getPosition ());
    }

    /**
     * Compute centroid of triangle formed by three points.
     *
     * @see https://math.stackexchange.com/questions/978642/how-to-sort-vertices-of-a-polygon-in-counter-clockwise-order
     */
    public Point2D.Double centroid (Point2D.Double v1, Point2D.Double v2, Point2D.Double v3)
    {
        final double tx = v1.x + v2.x + v3.x;
        final double ty = v1.y + v2.y + v3.y;
        return new Point2D.Double (tx / 3.0, ty / 3.0);
    }

    public NamedPoint getCentroid ()
    {
        return centroid;
    }

    /**
     * Vertex between line3 and line1. Opposite l1. V1 is the first vertex clockwise starting north
     * of the triangle centroid.
     */
    public GeoVertex getV1 ()
    {
        return v1;
    }

    /**
     * Vertex between line1 and line2. Opposite l2. V2 is clockwise from v1.
     */
    public GeoVertex getV2 ()
    {
        return v2;
    }

    /**
     * Vertex between line2 and line3. Opposite l3. V3 is clockwise from v2.
     */
    public GeoVertex getV3 ()
    {
        return v3;
    }

    /** Determine if a vertex belongs to this triangle. */
    public boolean hasVertex (GeoVertex v)
    {
        return v == v1 || v == v2 || v == v3;
    }

    /** Length from vertex 2 to vertex 3. This is the length of the side opposite v1. */
    public CentroidVariable getL1 ()
    {
        return l1;
    }

    /** Length from vertex 3 to vertex 1. This is the length of the side opposite v2. */
    public CentroidVariable getL2 ()
    {
        return l2;
    }

    /** Length from vertex 1 to vertex 2. This is the length of the side opposite v3. */
    public CentroidVariable getL3 ()
    {
        return l3;
    }

    /**
     * Angle of vertex 1 (degrees). This is the angle from line3 to line2. Opposite l1. This is at
     * the same corner as vertex v1.
     */
    public TriangleAngleVariable getAngle1 ()
    {
        return angle1;
    }

    /**
     * Angle of vertex 2 (degrees). This is the angle from line3 to line1. Opposite l2. This is at
     * the same corner as vertex v2.
     */
    public TriangleAngleVariable getAngle2 ()
    {
        return angle2;
    }

    /**
     * Angle of vertex 3 (degrees). This is the angle from line1 to line2. Opposite l3. This is at
     * the same corner as vertex v3.
     */
    public TriangleAngleVariable getAngle3 ()
    {
        return angle3;
    }

    /** Get length of all sides. */
    public List<NamedVariable> getSides ()
    {
        final List<NamedVariable> result = new ArrayList<> ();
        result.add (l1);
        result.add (l2);
        result.add (l3);
        return result;
    }

    /** Get size of all angles (degrees). */
    public List<TriangleAngleVariable> getAngles ()
    {
        final List<TriangleAngleVariable> result = new ArrayList<> ();
        result.add (angle1);
        result.add (angle2);
        result.add (angle3);
        return result;
    }

    /** Get length of all sides and size of all angles (degrees). */
    public List<NamedVariable> getVariables ()
    {
        final List<NamedVariable> result = new ArrayList<> ();
        result.add (l1);
        result.add (l2);
        result.add (l3);
        result.add (angle1);
        result.add (angle2);
        result.add (angle3);
        return result;
    }

    /** Get all vertices. */
    @Override
    public List<GeoVertex> getVertices ()
    {
        final List<GeoVertex> result = new ArrayList<> ();
        result.add (v1);
        result.add (v2);
        result.add (v3);
        return result;
    }

    /**
     * Get the length of the first leg next to a vertex. The choice of which leg is first and which
     * is second is arbitrary, but they must be different.
     *
     * The index of the side returned is one less (mod 3) than the index of the vertex.
     *
     * @param v The vertex to define the perspective.
     *
     * @return The leg counter clockwise from the vertex.
     */
    public NamedVariable getLeg1 (GeoVertex v)
    {
        if (v == v2)
        {
            return l1;
        }
        if (v == v3)
        {
            return l2;
        }
        if (v == v1)
        {
            return l3;
        }
        return null;
    }

    /**
     * Get the length of the second leg next to a vertex. The choice of which leg is first and which
     * is second is arbitrary, but they must be different.
     *
     * The index of the side returned is one more (mod 3) than the index of the vertex.
     *
     * @param v The vertex to define the perspective.
     *
     * @return The leg clockwise from the vertex.
     */
    public NamedVariable getLeg2 (GeoVertex v)
    {
        if (v == v2)
        {
            return l3;
        }
        if (v == v3)
        {
            return l1;
        }
        if (v == v1)
        {
            return l2;
        }
        return null;
    }

    /**
     * Get the length of the side opposite to a vertex.
     *
     * @param v The vertex to define the perspective.
     *
     * @return The side opposite the vertex.
     */
    public NamedVariable getOpposite (GeoVertex v)
    {
        if (v == v1)
        {
            return l1;
        }
        if (v == v2)
        {
            return l2;
        }
        if (v == v3)
        {
            return l3;
        }
        return null;
    }

    /**
     * Get the length of the side opposite to an angle.
     *
     * @param angle The angle to define the perspective.
     *
     * @return The side opposite the angle.
     */
    public NamedVariable getOpposite (TriangleAngleVariable angle)
    {
        if (angle == angle1)
        {
            return l1;
        }
        if (angle == angle2)
        {
            return l2;
        }
        if (angle == angle3)
        {
            return l3;
        }
        return null;
    }

    /**
     * Get the vertex opposite a side.
     *
     * @param l The side to determine the perspective.
     *
     * @return The opposite vertex.
     */
    public GeoVertex getOpposite (NamedVariable l)
    {
        if (l == l1)
        {
            return v1;
        }
        if (l == l2)
        {
            return v2;
        }
        if (l == l3)
        {
            return v3;
        }
        return null;
    }

    /**
     * Get the vertex opposite a side.
     *
     * @param l The side to determine the perspective.
     *
     * @return The opposite vertex.
     */
    public TriangleAngleVariable getOppositeAngle (NamedVariable l)
    {
        if (l == l1)
        {
            return angle1;
        }
        if (l == l2)
        {
            return angle2;
        }
        if (l == l3)
        {
            return angle3;
        }
        return null;
    }

    /**
     * Get the angle corresponding with a vertex.
     *
     * @param v The vertex to define the perspective.
     *
     * @return The angle corresponding to the vertex.
     */
    public TriangleAngleVariable getAngle (GeoVertex v)
    {
        if (v == v1)
        {
            return angle1;
        }
        if (v == v2)
        {
            return angle2;
        }
        if (v == v3)
        {
            return angle3;
        }
        return null;
    }

    /**
     * Get the vertex corresponding with a angle.
     *
     * @param angle The angle to define the perspective.
     *
     * @return The vertex corresponding to the angle.
     */
    public GeoVertex getVertex (TriangleAngleVariable angle)
    {
        if (angle == angle1)
        {
            return v1;
        }
        if (angle == angle2)
        {
            return v2;
        }
        if (angle == angle3)
        {
            return v3;
        }
        return null;
    }

    /** The perimeter of a triangle is the sum of the length of the sides. */
    public double getPerimeter ()
    {
        return l1.getDoubleValue () + l2.getDoubleValue () + l3.getDoubleValue ();
    }

    /**
     * Compute area of a triangle using Heron's formula.
     *
     * @return The area.
     *
     * @see https://www.inchcalculator.com/triangle-height-calculator/
     */
    public double getHeronArea ()
    {
        final double s = getPerimeter () / 2;
        final double area2 = s * (s - l1.getDoubleValue ()) * (s - l2.getDoubleValue ()) * (s - l3.getDoubleValue ());
        return sqrt (area2);
    }

    /**
     * Compute area of this triangle, as currently drawn on screen.
     *
     * This formula allows a negative result. Not sure if that is good but right now this is only
     * being used to check if the area is zero to be sure it really is a triangle so it does not
     * matter.
     *
     * This returns double the true area and may be negative.
     */
    public double triangleArea ()
    {
        final Point2D.Double p1 = v2.getPosition ();
        final Point2D.Double p2 = v3.getPosition ();
        final Point2D.Double p3 = v1.getPosition ();
        return p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y);
    }

    /**
     * Recalculate values derived from screen positions after a something moves.
     */
    @Override
    public void recalculate ()
    {
        l1.setDoubleValue (v2.distance (v3));
        l2.setDoubleValue (v3.distance (v1));
        l3.setDoubleValue (v1.distance (v2));
        centroid.setPosition (centroid ());

        l1.setDoubleValue (v2.distance (v3));
        l2.setDoubleValue (v3.distance (v1));
        l3.setDoubleValue (v1.distance (v2));

        // Angle opposite vertex 1 is angle between side l3 and l2.
        angle1.setDoubleValue (theta (l3.getDoubleValue (), l2.getDoubleValue (), l1.getDoubleValue ()));
        // Angle opposite vertex 2 is angle between side l1 and l3.
        angle2.setDoubleValue (theta (l1.getDoubleValue (), l3.getDoubleValue (), l2.getDoubleValue ()));
        // Angle opposite vertex 3 is angle between side l1 and l2.
        angle3.setDoubleValue (theta (l1.getDoubleValue (), l2.getDoubleValue (), l3.getDoubleValue ()));
    }

    /**
     * The triangle altitude (height) from a base side.
     *
     * @param base The perspective. The triangle is sitting with this side on the bottom. We need to
     *            compute the distance of a line perpendicular from this side to the opposite angle.
     *
     * @return The triangle altitude (height).
     *
     * @see https://www.inchcalculator.com/triangle-height-calculator/
     */
    public double getAltitude (NamedVariable base)
    {
        final double area = getHeronArea ();
        return 2 * area / base.getDoubleValue ();
    }

    /**
     * Calculate angle of triangle from length of all sides. This is the angle between side a and
     * side b. It is the angle opposite side c.
     *
     * c^2 = a^2 + b^2 - 2*a*b*cos(theta)
     *
     * @param a Length of side adjacent to angle theta.
     * @param b Length of side adjacent to angle theta.
     * @param c Length of side opposite angle theta.
     *
     * @return theta in degrees.
     */
    public double theta (double a, double b, double c)
    {
        final double numerator = a * a + b * b - c * c;
        final double denominator = 2 * a * b;
        final double costheta = numerator / denominator;
        return toDegrees (acos (costheta));
    }

    // /** Calculate length of base of triangle from opposite angle and length of two legs. */
    // public double base (double a, double b, double theta)
    // {
    // return sqrt (a * a + b * b - (2 * a * b * cos (toRadians (theta))));
    // }
    //
    // /** Distance between two points. */
    // public double distance (Point2D.Double p, Point2D.Double q)
    // {
    // return sqrt (distance2 (p, q));
    // }
    //
    // /** Square of the distance between two points. */
    // public double distance2 (Point2D.Double p, Point2D.Double q)
    // {
    // final double dx = q.x - p.x;
    // final double dy = q.y - p.y;
    // return dx * dx + dy * dy;
    // }

    /**
     * Derive inferences from this rectangle.
     *
     * @see https://www.mathsisfun.com/algebra/trig-cosine-law.html
     */
    @Override
    public void solve ()
    {
        // If a vertex angle is known, the corresponding triangle angle is known
        vertexFromAngle (angle1, v1);
        vertexFromAngle (angle2, v2);
        vertexFromAngle (angle3, v3);
        angleFromVertex (angle1, v1);
        angleFromVertex (angle2, v2);
        angleFromVertex (angle3, v3);

        // If two angles are known, the third can be derived
        deriveThirdAngle ();

        // If two vertices are at known locations, the length of the side is determined
        twoVertices (v2, v3, l1);
        twoVertices (v3, v1, l2);
        twoVertices (v1, v2, l3);

        // Implement variations of the law of cosines
        lawOfCosines ();
        // Law of sines:
        // If side a is opposite angle A and similarly b, B, c, C
        // then: (a / sin A) == (b / sin B) == (c / sin C)
        lawOfSines ();
        // If two angles are known the third is derived from 180 = a + b + c.

        // Location of centroid
        if (!centroid.getX ().isDetermined ())
        {
            final NamedVariable x1 = v1.getVertex ().getX ();
            final NamedVariable x2 = v2.getVertex ().getX ();
            final NamedVariable x3 = v3.getVertex ().getX ();
            if (x1.isDetermined () && x2.isDetermined () && x3.isDetermined ())
            {
                centroid.getX ().setFormula ("centroid", "%s == (%s + %s + %s) / 3", centroid.getX (), x1, x2, x3);
            }
        }
        if (!centroid.getY ().isDetermined ())
        {
            final NamedVariable y1 = v1.getVertex ().getY ();
            final NamedVariable y2 = v2.getVertex ().getY ();
            final NamedVariable y3 = v3.getVertex ().getY ();
            if (y1.isDetermined () && y2.isDetermined () && y3.isDetermined ())
            {
                centroid.getY ().setFormula ("centroid", "%s == (%s + %s + %s) / 3", centroid.getY (), y1, y2, y3);
            }
        }
        // If all angles and sides are known, the triangle is fully known
        if (!isDetermined ())
        {
            if (l1.isDetermined () && l2.isDetermined () && l3.isDetermined ())
            {
                setStatus (GeoStatus.derived, "triangle sides");
            }
        }
        if (!isDetermined ())
        {
            if (v1.isDetermined () && v2.isDetermined () && v3.isDetermined ())
            {
                setStatus (GeoStatus.derived, "triangle vertices");
            }
        }
        if (isDetermined ())
        {
            if (!l1.isDetermined ())
            {
                l1.setStatus (GeoStatus.derived, "determined triangle");
            }
            if (!l2.isDetermined ())
            {
                l2.setStatus (GeoStatus.derived, "determined triangle");
            }
            if (!l3.isDetermined ())
            {
                l3.setStatus (GeoStatus.derived, "determined triangle");
            }
            if (!angle1.isDetermined ())
            {
                angle1.setStatus (GeoStatus.derived, "determined triangle");
            }
            if (!angle2.isDetermined ())
            {
                angle2.setStatus (GeoStatus.derived, "determined triangle");
            }
            if (!angle3.isDetermined ())
            {
                angle3.setStatus (GeoStatus.derived, "determined triangle");
            }
        }
    }

    /**
     * Compute length of one side from position of the endpoint vertices. This is just a computation
     * of distance, not an instance of the law of cosines so there is no trigonometry involved.
     */
    private void twoVertices (GeoVertex a, GeoVertex b, NamedVariable l)
    {
        final NamedPoint av = a.getVertex ();
        final NamedPoint bv = b.getVertex ();
        if (!l.isDetermined ())
        {
            if (av.isDetermined ())
            {
                if (bv.isDetermined ())
                {
                    l.setFormula ("triangle vertices", "%s == sqrt((%s - %s) ^ 2 + (%s - %s) ^ 2)", l, bv.getX (), av.getX (),
                            bv.getY (), av.getY ());
                    logger.info ("Length %s derived from vertices %s, %s", l.getName (), a.getName (), b.getName ());
                }
            }
        }
    }

    private void deriveThirdAngle ()
    {
        if (!angle1.isDetermined ())
        {
            if (angle2.isDetermined () && angle3.isDetermined ())
            {
                angle1.setFormula ("triangle angles sum 180", "%s == 180 - (%s + %s)", angle1, angle2, angle3);
            }
        }
        if (!angle2.isDetermined ())
        {
            if (angle3.isDetermined () && angle1.isDetermined ())
            {
                angle2.setFormula ("triangle angles sum 180", "%s == 180 - (%s + %s)", angle2, angle3, angle1);
            }
        }
        if (!angle3.isDetermined ())
        {
            if (angle2.isDetermined () && angle1.isDetermined ())
            {
                angle3.setFormula ("triangle angles sum 180", "%s == 180 - (%s + %s)", angle3, angle2, angle1);
            }
        }
    }

    /** Compute a triangle angle from a vertex angle. */
    private void angleFromVertex (TriangleAngleVariable angle, GeoVertex v)
    {
        if (!angle.isDetermined ())
        {
            if (v.isDetermined ())
            {
                // Value is the correct angle
                final double angleValue = angle.getDoubleValue ();
                final String angleName = angle.getName ();
                // Vertex angle may be a reflection
                final NamedVariable vertexAngle = v.getAngle ();
                final double vertexValue = vertexAngle.getDoubleValue ();
                final String vertexName = vertexAngle.getName ();
                if (abs (vertexValue - angleValue) < epsilon)
                {
                    angle.setFormula ("Angle from vertex", "%s == %s", angle, vertexAngle);
                    logger.info ("Angle %s == vertex %s", angleName, vertexName);
                }
                else if (vertexValue < 0 && abs (angleValue + vertexValue) < epsilon)
                {
                    angle.setFormula ("Angle from -vertex", "%s == -%s", angle, vertexAngle);
                    logger.info ("Angle %s == -vertex %s", angleName, vertexName);
                }
                else if (vertexValue >= 0 && abs (180 - angleValue - vertexValue) < epsilon)
                {
                    angle.setFormula ("Angle from 180-vertex", "%s == 180-%s", angle, vertexAngle);
                    logger.info ("Angle %s == 180-vertex %s", angleName, vertexName);
                }
                else if (vertexValue < 0 && abs (180 - angleValue + vertexValue) < epsilon)
                {
                    angle.setFormula ("Angle from vertex+180", "%s == %s+180", angle, vertexAngle);
                    logger.info ("Angle %s == vertex %s + 180", angleName, vertexName);
                }
                else
                {
                    logger.warn ("Angle %s %.2f does not match vertex %s %.2f", angleName, angleValue, vertexName, vertexValue);
                }
            }
        }
    }

    /**
     * Compute a vertex angle from a triangle angle. This implements a cheat. It uses the numerical
     * values to figure out which formula applies in this case.
     *
     * @param angle The triangle angle to derive the vertex angle from.
     * @param v The vertex to derive.
     */
    private void vertexFromAngle (TriangleAngleVariable angle, GeoVertex v)
    {
        if (!v.isDetermined ())
        {
            if (angle.isDetermined ())
            {
                // Value is the correct angle
                final double angleValue = angle.getDoubleValue ();
                final String angleName = angle.getName ();
                // Vertex angle may be a reflection
                final NamedVariable vertexAngle = v.getAngle ();
                final double vertexValue = vertexAngle.getDoubleValue ();
                final String vertexName = vertexAngle.getName ();
                if (vertexValue == angleValue)
                {
                    vertexAngle.setFormula ("Vertex from angle", "%s == %s", vertexAngle, angle);
                    logger.info ("Vertex %s == angle %s", vertexName, angleName);
                }
                else if (vertexValue < 0 && vertexValue == -angleValue)
                {
                    vertexAngle.setFormula ("Vertex from -angle", "%s == -%s", vertexAngle, angle);
                    logger.info ("Vertex %s == -angle %s", vertexName, angleName);
                }
                else if (vertexValue >= 0 && vertexValue == 180 - angleValue)
                {
                    vertexAngle.setFormula ("Vertex from 180-angle", "%s == 180-%s", vertexAngle, angle);
                    logger.info ("Vertex %s == 180-angle %s", vertexName, angleName);
                }
                else if (vertexValue < 0 && vertexValue == angleValue - 180)
                {
                    vertexAngle.setFormula ("Vertex from angle-180", "%s == %s-180", vertexAngle, angle);
                    logger.info ("Vertex %s == angle %s - 180", vertexName, angleName);
                }
                else
                {
                    logger.warn ("Vertex %s %.2f does not match angle %s %.2f", vertexName, vertexValue, angleName, angleValue);
                }
            }
        }
    }

    /**
     * Implement the law of cosines.
     *
     * The law of cosines relates the length of the sides of a triangle.
     *
     * c^2 = a^2 + b^2 − 2ab cos(theta) If theta = 90 degrees, this simplifies to the Pythagorean
     * theorem.
     */
    private void lawOfCosines ()
    {
        final int count = countSidesDetermined ();
        if (count == 3)
        {
            // Determine all angles
            deriveVertexByLawOfCosines (v1);
            deriveVertexByLawOfCosines (v2);
            deriveVertexByLawOfCosines (v3);
        }
        applyLawOfCosines (v1);
        applyLawOfCosines (v2);
        applyLawOfCosines (v3);
    }

    /**
     * Implement the law of cosines.
     *
     * The law of cosines relates the length of the sides of a triangle.
     *
     * c^2 = a^2 + b^2 − 2ab cos(theta) If theta = 90 degrees, this simplifies to the Pythagorean
     * theorem.
     *
     * @param v The vertex angle to derive.
     */
    private void deriveVertexByLawOfCosines (GeoVertex v)
    {
        final TriangleAngleVariable theta = getAngle (v);
        if (!theta.isDetermined ())
        {
            final NamedVariable a = getLeg1 (v);
            final NamedVariable b = getLeg2 (v);
            final NamedVariable c = getOpposite (v);
            theta.setFormula ("law of cosines",
                    "%s == Block({$a=%s, $b=%s, $c=%s}, Return(ArcCos(($a^2 + $b^2 - $c^2) / (2 * $a * $b)) / Degree))", theta, a,
                    b, c);

            // logger.info ("Theta %s: %s", theta.getName (), theta.getFormula ());
        }
    }

    /**
     * Apply law of cosines when a vertex is known.
     *
     * @param v A determined vertex
     */
    private void applyLawOfCosines (GeoVertex v)
    {
        final TriangleAngleVariable theta = getAngle (v);
        final NamedVariable c = getOpposite (v);
        if (!c.isDetermined ())
        {
            if (theta.isDetermined ())
            {
                final NamedVariable a = getLeg1 (v);
                final NamedVariable b = getLeg2 (v);
                if (a.isDetermined ())
                {
                    if (b.isDetermined ())
                    {
                        c.setFormula ("law of cosines",
                                "%s == Block({$a=%s, $b=%s, $theta=%s}, Return(sqrt($a^2 + $b^2 - 2*$a*$b*cos($theta * Degree))))",
                                c, a, b, theta);

                        logger.info ("Calculate %s: %s", c.getName (), c.getFormulaInstance ());
                    }
                }
            }
        }
    }

    /**
     * Law of sines.
     *
     * If side a is opposite angle A and similarly b, B, c, C then:
     *
     * (a / sin A) == (b / sin B) == (c / sin C)
     *
     * @see https://www.mathsisfun.com/algebra/trig-sine-law.html
     */
    private void lawOfSines ()
    {
        final NamedVariable a = l1;
        final NamedVariable b = l2;
        final NamedVariable c = l3;
        final TriangleAngleVariable A = getOppositeAngle (a);
        final TriangleAngleVariable B = getOppositeAngle (b);
        final TriangleAngleVariable C = getOppositeAngle (c);
        if (!a.isDetermined () && A.isDetermined ())
        {
            if (b.isDetermined () && B.isDetermined ())
            {
                applyLawOfSines (a, A, b, B);
            }
            if (c.isDetermined () && C.isDetermined ())
            {
                applyLawOfSines (a, A, c, C);
            }
        }
        if (!b.isDetermined () && B.isDetermined ())
        {
            if (a.isDetermined () && A.isDetermined ())
            {
                applyLawOfSines (b, B, a, A);
            }
            if (c.isDetermined () && C.isDetermined ())
            {
                applyLawOfSines (b, B, c, C);
            }
        }
        if (!c.isDetermined () && C.isDetermined ())
        {
            if (a.isDetermined () && A.isDetermined ())
            {
                applyLawOfSines (c, C, a, A);
            }
            if (b.isDetermined () && B.isDetermined ())
            {
                applyLawOfSines (c, C, b, B);
            }
        }
        // We can apply the law of sines to calculate angles also, but sometimes the answer is
        // ambiguous
        // @see https://www.mathsisfun.com/algebra/trig-sine-law.html
    }

    /**
     * Law of sines.
     *
     * If side a is opposite angle A and similarly b, B, c, C then:
     *
     * (a / sin A) == (b / sin B)
     *
     * a == (b * sin (A)) / sin (B)
     */
    private void applyLawOfSines (NamedVariable a, TriangleAngleVariable A, NamedVariable b, TriangleAngleVariable B)
    {
        a.setFormula ("law of size",
                "%s == Block({angleA=%s, sideB=%s, angleB=%s}, Return((sideB * Sin (angleA * Degree)) / Sin(angleB * Degree)))",
                a, A, b, B);
        logger.info ("Calculate %s: %s", a.getName (), a.getFormulaInstance ());
    }

    public int countSidesDetermined ()
    {
        int count = 0;
        if (l1.isDetermined ())
        {
            count++;
        }
        if (l2.isDetermined ())
        {
            count++;
        }
        if (l3.isDetermined ())
        {
            count++;
        }
        return count;
    }

    public NamedVariable getUndeterminedSide ()
    {
        if (!l1.isDetermined ())
        {
            return l1;
        }
        if (!l2.isDetermined ())
        {
            return l2;
        }
        if (!l3.isDetermined ())
        {
            return l3;
        }
        return null;
    }

    /**
     * Paint this item in its current state. The Graphics object is normally the window component,
     * but could be a printer or BufferedImage.
     *
     * @param g The paint destination.
     * @param labels The label container.
     */
    @Override
    public void paint (Graphics g, Labels labels)
    {
        final Point2D.Double p1 = v2.getPosition ();
        final Point2D.Double p2 = v3.getPosition ();
        final Point2D.Double p3 = v1.getPosition ();
        if (p1 != null && p2 != null && p3 != null)
        {
            g.setColor (getStatus ().getColor ());
            // If moving a line removes a vertex, this rectangle should be removed too
            final int[] x = {(int)round (p1.x), (int)round (p2.x), (int)round (p3.x)};
            final int[] y = {(int)round (p1.y), (int)round (p2.y), (int)round (p3.y)};
            final Graphics2D gg = (Graphics2D)g;
            final Stroke stroke = gg.getStroke ();
            final float size = isSelected () ? 5.0f : 3.0f;
            gg.setStroke (new BasicStroke (size));
            g.drawPolygon (x, y, 3);
            gg.setStroke (stroke);
            markRightAngle (g, angle1, v1, v2, v3);
            markRightAngle (g, angle2, v2, v1, v3);
            markRightAngle (g, angle3, v3, v1, v2);
            labels.add (this, getStatus ().getColor (), centroid.getIntPosition (), SwingConstants.SOUTH_WEST, getName ());
        }
    }

    private void markRightAngle (Graphics g, NamedVariable angle, GeoVertex v, GeoVertex va, GeoVertex vb)
    {
        if (abs (angle.getDoubleValue () - 90.0) < 1.0)
        {
            // logger.info ("Right angle %s", angle);
            markRightAngle (g, v.getVertex ().getPosition (), va.getVertex ().getPosition (), vb.getVertex ().getPosition ());
        }
    }

    /**
     * @see https://math.stackexchange.com/questions/175896/finding-a-point-along-a-line-a-certain-distance-away-from-another-point
     *
     * @param g
     * @param v
     * @param va
     * @param vb
     */
    private void markRightAngle (Graphics g, Point2D.Double v, Point2D.Double va, Point2D.Double vb)
    {
        // logger.info ("v = %.2f %.2f", v.x, v.y);
        // logger.info ("va = %.2f %.2f", va.x, va.y);
        // logger.info ("vb = %.2f %.2f", vb.x, vb.y);
        // Length of square
        final double len = 20;

        // We need the point 20 pixels in the direction of va
        final double dx1 = va.x - v.x;
        final double dy1 = va.y - v.y;
        // logger.info ("d1 = %.2f %.2f", dx1, dy1);
        final double l1 = sqrt (dx1 * dx1 + dy1 * dy1);
        // logger.info ("length to p1 %.2f", l1);
        final double dx1n = len * dx1 / l1;
        final double dy1n = len * dy1 / l1;
        // logger.info ("d1n = %.2f %.2f", dx1n, dy1n);
        // logger.info ("normal1 length = %.2f", sqrt ((dx1n - v.x) * (dx1n - v.x) + (dy1n - v.y) *
        // (dy1n - v.y)));
        // We need the point 20 pixels in the direction of vb
        final double dx2 = vb.x - v.x;
        final double dy2 = vb.y - v.y;
        final double l2 = sqrt (dx2 * dx2 + dy2 * dy2);
        final double dx2n = len * dx2 / l2;
        final double dy2n = len * dy2 / l2;
        // end point1
        final double p1x = v.x + dx1n;
        final double p1y = v.y + dy1n;
        // end point2
        final int p2x = (int)round (v.x + dx2n);
        final int p2y = (int)round (v.y + dy2n);
        // Corner point
        final int cx = (int)round (p1x + dx2n);
        final int cy = (int)round (p1y + dy2n);
        // g.drawRect ((int)v.x - 25, (int)v.y - 25, 50, 50);
        g.setColor (Color.red);
        g.drawLine (cx, cy, (int)round (p1x), (int)round (p1y));
        g.drawLine (cx, cy, p2x, p2y);
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
        result.put ("Delete", item -> item.remove ());
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        buffer.append (" ");
        buffer.append (v2);
        buffer.append (" ");
        buffer.append (v3);
        buffer.append (" ");
        buffer.append (v1);
        buffer.append (">");
        return buffer.toString ();
    }
}
