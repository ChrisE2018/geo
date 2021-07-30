
package com.chriseliot.geo;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.*;

import com.chriseliot.util.*;

@Tag ("Triangle")
public class TestGeoTriangle
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());
    private final TestSupport ts = new TestSupport ();

    @Test
    public void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (10, 20));
        final GeoVertex v2 = plane.getVertex (new Point2D.Double (50, 55));
        final GeoVertex v3 = plane.getVertex (new Point2D.Double (30, 40));

        final GeoTriangle t = plane.getTriangle (v1, v2, v3);
        assertNotNull (t.toString ());
    }

    @Test
    public void testGetters ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        // These are sorted into the order that the triangle wants them.
        // They are sorted into clockwise order.
        // Note: this is counter-clockwise?
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex v2 = plane.getVertex (new Point2D.Double (50, 55));
        final GeoVertex v3 = plane.getVertex (new Point2D.Double (10, 20));

        final GeoTriangle t = plane.getTriangle (v1, v2, v3);
        assertEquals (v1, t.getV1 ());
        assertEquals (v2, t.getV2 ());
        assertEquals (v3, t.getV3 ());

        assertEquals (53.1507, t.getL1 ().getDoubleValue (), TestSupport.epsilon);
        assertEquals (28.2842, t.getL2 ().getDoubleValue (), TestSupport.epsilon);
        assertEquals (25.0, t.getL3 ().getDoubleValue (), TestSupport.epsilon);

        assertEquals (171.86989, t.getAngle1 ().getDoubleValue (), TestSupport.epsilon);
        assertEquals (4.31602, t.getAngle2 ().getDoubleValue (), TestSupport.epsilon);
        assertEquals (3.81407, t.getAngle3 ().getDoubleValue (), TestSupport.epsilon);

        final List<NamedVariable> sides = t.getSides ();
        assertTrue (sides.contains (t.getL1 ()));
        assertTrue (sides.contains (t.getL2 ()));
        assertTrue (sides.contains (t.getL3 ()));
        assertEquals (3, sides.size ());

        final List<TriangleAngleVariable> angles = t.getAngles ();
        assertTrue (angles.contains (t.getAngle1 ()));
        assertTrue (angles.contains (t.getAngle2 ()));
        assertTrue (angles.contains (t.getAngle3 ()));
        assertEquals (3, angles.size ());

        final List<NamedVariable> variables = t.getVariables ();
        assertTrue (variables.contains (t.getL1 ()));
        assertTrue (variables.contains (t.getL2 ()));
        assertTrue (variables.contains (t.getL3 ()));
        assertTrue (variables.contains (t.getAngle1 ()));
        assertTrue (variables.contains (t.getAngle2 ()));
        assertTrue (variables.contains (t.getAngle3 ()));
        assertEquals (6, variables.size ());

        final List<GeoVertex> vertices = t.getVertices ();
        assertTrue (vertices.contains (t.getV2 ()));
        assertTrue (vertices.contains (t.getV3 ()));
        assertTrue (vertices.contains (t.getV1 ()));
        assertEquals (3, vertices.size ());

        assertTrue (t.hasVertex (v1));
        assertTrue (t.hasVertex (v2));
        assertTrue (t.hasVertex (v3));

        final GeoLine line4 = new GeoLine (plane, Color.blue, new Point2D.Double (100, 100), new Point2D.Double (500, 100));
        final GeoLine line5 = new GeoLine (plane, Color.blue, new Point2D.Double (100, 100), new Point2D.Double (600, 200));
        final GeoVertex v4 = line4.getVertex (line5);
        assertFalse (t.hasVertex (v4));
    }

    @Test
    public void testArea ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoVertex v2 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex v3 = plane.getVertex (new Point2D.Double (30, 0));

        final GeoTriangle t = plane.getTriangle (v1, v2, v3);

        assertEquals (-1200, t.triangleArea ());

        // Also check legs which we are here
        assertEquals (30, t.getL1 ().getDoubleValue ());
        assertEquals (50, t.getL2 ().getDoubleValue ());
        assertEquals (40, t.getL3 ().getDoubleValue ());

        t.recalculate ();
        assertEquals (-1200, t.triangleArea ());
        assertEquals (30, t.getL1 ().getDoubleValue ());
        assertEquals (50, t.getL2 ().getDoubleValue ());
        assertEquals (40, t.getL3 ().getDoubleValue ());
    }

    @Test
    public void testCentroid ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoVertex v2 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex v3 = plane.getVertex (new Point2D.Double (30, 0));

        final GeoTriangle t = plane.getTriangle (v1, v2, v3);

        final double cx = 20.0;
        final double cy = 40.0 / 3.0;
        final Point2D.Double c = new Point2D.Double (cx, cy);
        assertEquals (c, t.centroid ());
        assertEquals (c, t.centroid (new Point2D.Double (30, 0), new Point2D.Double (30, 40), new Point2D.Double (0, 0)));
        assertEquals (c, t.centroid (v1, v2, v3));
        assertEquals (c, t.centroid (v2, v1, v3));
        assertEquals (c, t.centroid (v3, v1, v2));
    }

    @Test
    public void testPaint ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoVertex v2 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex v3 = plane.getVertex (new Point2D.Double (30, 0));

        final GeoTriangle t = plane.getTriangle (v1, v2, v3);
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Labels labels = new Labels ();
        t.setSelected (true);
        t.paint (g, labels);
        t.setSelected (false);
        t.paint (g, labels);
        t.getV1 ().setPosition (null);
        t.paint (g, labels);
        t.getV3 ().setPosition (null);
        t.paint (g, labels);
        t.getV2 ().setPosition (null);
        t.paint (g, labels);
    }

    @Test
    public void testCountSidesDetermined ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (30, 0));

        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t.toString ());

        // Make L3 the undetermined side
        assertEquals (0, t.countSidesDetermined ());
        t.getL1 ().setGivenStatus (GeoStatus.known);
        assertEquals (1, t.countSidesDetermined ());
        t.getL2 ().setGivenStatus (GeoStatus.known);
        assertEquals (2, t.countSidesDetermined ());
        assertEquals (t.getL3 (), t.getUndeterminedSide ());

        // Make L1 the undetermined side
        t.getL1 ().setStatusUnknown ();
        t.getL3 ().setGivenStatus (GeoStatus.known);
        assertEquals (2, t.countSidesDetermined ());
        assertEquals (t.getL1 (), t.getUndeterminedSide ());

        // Make L2 the undetermined side
        t.getL2 ().setStatusUnknown ();
        t.getL1 ().setGivenStatus (GeoStatus.known);
        assertEquals (2, t.countSidesDetermined ());
        assertEquals (t.getL2 (), t.getUndeterminedSide ());

        // Make all sides determined
        t.getL2 ().setGivenStatus (GeoStatus.known);
        assertNull (t.getUndeterminedSide ());
    }

    @Test
    public void testGetLeg1 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (30, 0));

        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t.toString ());

        assertEquals (t.getL1 (), t.getLeg1 (t.getV2 ()));
        assertEquals (t.getL2 (), t.getLeg1 (t.getV3 ()));
        assertEquals (t.getL3 (), t.getLeg1 (t.getV1 ()));
        assertNull (t.getLeg1 (null));
    }

    @Test
    public void testGetLeg2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (30, 0));

        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t.toString ());

        assertEquals (t.getL3 (), t.getLeg2 (t.getV2 ()));
        assertEquals (t.getL1 (), t.getLeg2 (t.getV3 ()));
        assertEquals (t.getL2 (), t.getLeg2 (t.getV1 ()));
        assertNull (t.getLeg2 (null));
    }

    @Test
    public void testGetOppositeVertex ()
    {
        Namer.reset ();
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (30, 0));

        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t.toString ());

        assertEquals (t.getL2 (), t.getOpposite (t.getV2 ()));
        assertEquals (t.getL3 (), t.getOpposite (t.getV3 ()));
        assertEquals (t.getL1 (), t.getOpposite (t.getV1 ()));
        assertNull (t.getOpposite ((GeoVertex)null));
    }

    @Test
    public void testGetOppositeLeg ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (30, 0));

        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t.toString ());

        assertEquals (t.getV1 (), t.getOpposite (t.getL1 ()));
        assertEquals (t.getV2 (), t.getOpposite (t.getL2 ()));
        assertEquals (t.getV3 (), t.getOpposite (t.getL3 ()));
        assertNull (t.getOpposite ((NamedVariable)null));

        assertEquals (t.getL1 (), t.getOpposite (t.getAngle1 ()));
        assertEquals (t.getL2 (), t.getOpposite (t.getAngle2 ()));
        assertEquals (t.getL3 (), t.getOpposite (t.getAngle3 ()));
        assertNull (t.getOpposite ((TriangleAngleVariable)null));
    }

    @Test
    public void testGetVertex ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (30, 0));

        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t.toString ());

        assertEquals (t.getV1 (), t.getVertex (t.getAngle1 ()));
        assertEquals (t.getV2 (), t.getVertex (t.getAngle2 ()));
        assertEquals (t.getV3 (), t.getVertex (t.getAngle3 ()));
        assertNull (t.getVertex ((TriangleAngleVariable)null));

        assertEquals (30.0 + 40.0 + 50.0, t.getPerimeter ());
        assertEquals ((30.0 * 40.0) / 2, t.getHeronArea ());
        assertEquals (t.getHeronArea () * 2, abs (t.triangleArea ()));
        assertEquals (40.0, t.getAltitude (t.getL1 ()));
    }

    @Test
    public void testAngle ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (30, 0));

        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t.toString ());

        assertEquals (t.getAngle2 (), t.getAngle (t.getV2 ()));
        assertEquals (t.getAngle3 (), t.getAngle (t.getV3 ()));
        assertEquals (t.getAngle1 (), t.getAngle (t.getV1 ()));
        assertNull (t.getAngle (null));
    }

    @Test
    public void testSolve1 ()
    {
        Namer.reset ();
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 0));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (0, 0));

        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t.toString ());
        final NamedVariable angle1 = t.getAngle1 ();
        final NamedVariable angle2 = t.getAngle2 ();
        final NamedVariable angle3 = t.getAngle3 ();
        // System.out.printf ("Line1 angle %s \n", line1.getAngle ().getDoubleValue ());
        // System.out.printf ("Line2 angle %s \n", line2.getAngle ().getDoubleValue ());
        // System.out.printf ("Line3 angle %s \n", line3.getAngle ().getDoubleValue ());
        System.out.printf ("T %s %s %s\n", angle1.getDoubleValue (), angle2.getDoubleValue (), angle3.getDoubleValue ());
        assertEquals (90, line1.getAngle ().getDoubleValue ());
        assertEquals (36.869897, line2.getAngle ().getDoubleValue (), TestSupport.epsilon);
        assertEquals (180, line3.getAngle ().getDoubleValue ());
        final GeoVertex v1 = t.getV1 ();
        final GeoVertex v2 = t.getV2 ();
        final GeoVertex v3 = t.getV3 ();
        v1.getVertex ().setGivenStatus (GeoStatus.known);
        v2.getVertex ().setGivenStatus (GeoStatus.known);
        v3.getVertex ().setGivenStatus (GeoStatus.known);
        assertEquals (-143.130102, v1.getAngle ().getDoubleValue (), TestSupport.epsilon);
        assertEquals (-90, v2.getAngle ().getDoubleValue (), TestSupport.epsilon);
        assertEquals (53.130101, v3.getAngle ().getDoubleValue (), TestSupport.epsilon);
        final String trace = null; // "testSolve1";
        ts.checkExpression (t.getL1 (), 30, trace);
        ts.checkExpression (t.getL2 (), 50, trace);
        ts.checkExpression (t.getL3 (), 40, trace);

        ts.checkExpression (t.getAngle1 (), 36.8699, trace);
        ts.checkExpression (t.getAngle2 (), 90, trace);
        ts.checkExpression (t.getAngle3 (), 53.130102, trace);
    }

    @Test
    public void testSolve2 ()
    {
        Namer.reset ();
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 0));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t.toString ());
        final NamedVariable l1 = t.getL1 ();
        final NamedVariable l2 = t.getL2 ();
        final NamedVariable l3 = t.getL3 ();
        final NamedVariable angle1 = t.getAngle2 ();
        final NamedVariable angle2 = t.getAngle3 ();
        final NamedVariable angle3 = t.getAngle1 ();
        l1.setGivenStatus (GeoStatus.known);
        l2.setGivenStatus (GeoStatus.known);
        l3.setGivenStatus (GeoStatus.known);
        assertEquals (GeoStatus.derived, t.getStatus ());
        l3.setStatusUnknown ();
        plane.resetDerived ();
        assertEquals (GeoStatus.unknown, t.getStatus ());
        final NamedVariable neededSide = t.getUndeterminedSide ();
        assertEquals (l3, neededSide);
        assertEquals (angle3, t.getOppositeAngle (l1));
        assertEquals (angle1, t.getOppositeAngle (l2));
        assertEquals (angle2, t.getOppositeAngle (l3));
        assertNull (t.getOppositeAngle (null));
        final NamedVariable opposite = t.getOppositeAngle (neededSide);
        assertEquals (angle2, opposite);
        angle2.setGivenStatus (GeoStatus.known);
    }

    @Test
    public void testAllDetermined1 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (10, 20));
        final GeoVertex v2 = plane.getVertex (new Point2D.Double (50, 55));
        final GeoVertex v3 = plane.getVertex (new Point2D.Double (30, 40));

        final GeoTriangle t = plane.getTriangle (v1, v2, v3);
        assertNotNull (t.toString ());
        v1.getVertex ().getX ().setGivenStatus (GeoStatus.known);
        v1.getVertex ().getY ().setGivenStatus (GeoStatus.known);
        v2.getVertex ().getX ().setGivenStatus (GeoStatus.known);
        v2.getVertex ().getY ().setGivenStatus (GeoStatus.known);
        v3.getVertex ().getX ().setGivenStatus (GeoStatus.known);
        v3.getVertex ().getY ().setGivenStatus (GeoStatus.known);
        assertTrue (t.isDetermined ());

        for (final GeoItem item : plane.getItems ())
        {
            assertTrue (item.isDetermined ());
        }
    }

    @Test
    public void testAllDetermined2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (10, 20));
        final GeoVertex v2 = plane.getVertex (new Point2D.Double (50, 55));
        final GeoVertex v3 = plane.getVertex (new Point2D.Double (30, 40));

        final GeoTriangle t = plane.getTriangle (v1, v2, v3);
        assertNotNull (t.toString ());
        t.getL1 ().setGivenStatus (GeoStatus.known);
        t.getL2 ().setGivenStatus (GeoStatus.known);
        t.getL3 ().setGivenStatus (GeoStatus.known);
        t.getV3 ().getVertex ().getX ().setGivenStatus (GeoStatus.known);
        t.getV3 ().getVertex ().getY ().setGivenStatus (GeoStatus.known);
        assertTrue (t.isDetermined ());

        for (final GeoItem item : plane.getItems ())
        {
            assertTrue (item.isDetermined ());
        }
    }

    @Test
    public void testAllDeterminedExplain ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (10, 20));
        final GeoVertex v2 = plane.getVertex (new Point2D.Double (50, 55));
        final GeoVertex v3 = plane.getVertex (new Point2D.Double (30, 40));

        final GeoTriangle t = plane.getTriangle (v1, v2, v3);
        assertNotNull (t.toString ());
        v1.getVertex ().getX ().setGivenStatus (GeoStatus.known);
        v1.getVertex ().getY ().setGivenStatus (GeoStatus.known);
        v2.getVertex ().getX ().setGivenStatus (GeoStatus.known);
        v2.getVertex ().getY ().setGivenStatus (GeoStatus.known);
        v3.getVertex ().getX ().setGivenStatus (GeoStatus.known);
        v3.getVertex ().getY ().setGivenStatus (GeoStatus.known);
        assertTrue (t.isDetermined ());
        logger.info ("**************************************");
        logger.info ("Checking that all items are determined");
        final List<GeoItem> problems = new ArrayList<> ();
        for (final GeoItem item : plane.getItems ())
        {
            if (!item.isDetermined ())
            {
                problems.add (item);
                logger.info ("%s is not determined as expected", item.getName ());
            }
        }
        if (!problems.isEmpty ())
        {
            logger.info ("**************************************");
            for (final GeoItem item : problems)
            {
                logger.info ("Explaining why %s is not determined as expected", item.getName ());
                logger.info ("%s support %s", item.getName (), GeoItem.getNames (item.getSupport ()));
                assertTrue (item.whyDetermined ());
            }
        }
    }

    @Test
    public void testCombinations ()
    {
        Namer.reset ();
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 0));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t);
        final NamedVariable l1 = t.getL1 ();
        final NamedVariable l2 = t.getL2 ();
        final NamedVariable l3 = t.getL3 ();
        final NamedVariable angle1 = t.getAngle1 ();
        final NamedVariable angle2 = t.getAngle2 ();
        final NamedVariable angle3 = t.getAngle3 ();

        int count = 0;
        int total = 0;
        final NamedVariable[] vars = {l1, l2, l3, angle1, angle2, angle3};
        for (int i = 0; i < 64; i++)
        {
            total++;
            for (final NamedVariable v : vars)
            {
                v.setStatusUnknown ();
            }
            plane.resetDerived ();
            for (int j = 0; j < vars.length; j++)
            {
                final NamedVariable v = vars[j];
                // @see
                // https://stackoverflow.com/questions/14145733/how-can-one-read-an-integer-bit-by-bit-in-java/14145767
                final int bit = (i >> j) & 1;
                if (bit != 0)
                {
                    v.setGivenStatus (GeoStatus.known);
                }
            }
            if (t.isDetermined ())
            {
                count++;
                for (final GeoItem item : plane.getItems ())
                {
                    if (item instanceof NamedVariable)
                    {
                        final NamedVariable v = (NamedVariable)item;
                        ts.checkExpression (v, v.getDoubleValue (), null);
                    }
                }
            }
        }
        assertEquals (64, total);
        assertEquals (35, count);
    }

    /**
     * Make a table of all combinations of known sides and angles. This does not include
     * combinations where the vertices are known.
     *
     * This shows some unsolved cases that should be solvable. If 2 angles and 1 side are known, the
     * triangle is solvable.
     *
     * @throws IOException
     */
    @Test
    public void testReportCombinations () throws IOException
    {
        final File file = ts.getTestDataFile (this, "Triangle Combinations", "html");

        Namer.reset ();
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (30, 40));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (30, 0));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex inputAngle1 = plane.getVertex (new Point2D.Double (30, 40));
        final GeoVertex inputAngle2 = plane.getVertex (new Point2D.Double (30, 0));
        final GeoVertex inputAngle3 = plane.getVertex (new Point2D.Double (0, 0));
        final GeoTriangle t = plane.getTriangle (inputAngle1, inputAngle2, inputAngle3);
        assertNotNull (t);
        final NamedVariable l1 = t.getL1 ();
        final NamedVariable l2 = t.getL2 ();
        final NamedVariable l3 = t.getL3 ();
        final NamedVariable angle1 = t.getAngle2 ();
        final NamedVariable angle2 = t.getAngle3 ();
        final NamedVariable angle3 = t.getAngle1 ();
        int count = 0;
        int total = 0;
        final boolean showVariablesRow = false;

        try (final BufferedWriter stream = new BufferedWriter (new FileWriter (file)))
        {
            stream.write ("<html><head><title>Triangle Test Data</title></head>\n");
            stream.write ("<body>\n");
            stream.write ("<h1>Triangle Test Data</h1>\n");
            stream.write ("<h2>\n");
            stream.write (new Date ().toString ());
            stream.write ("</h2>\n");
            final NamedVariable[] vars = {l1, l2, l3, angle1, angle2, angle3};
            stream.write ("<table>\n");
            stream.write ("<tr><th>&nbsp;T&nbsp;</th><th>&nbsp;a&nbsp;</th><th>&nbsp;b&nbsp;</th><th>&nbsp;c&nbsp;</th>\n");
            stream.write ("<th>&nbsp;A&nbsp;</th><th>&nbsp;B&nbsp;</th><th>&nbsp;C&nbsp;</th><th>&nbsp;N&nbsp;</th></tr>\n");
            if (showVariablesRow)
            {
                stream.write ("<tr>\n");
                for (int j = 0; j < vars.length; j++)
                {
                    final NamedVariable v = vars[j];
                    stream.write ("<th>" + v.getName () + "</th>\n");
                }
                stream.write ("</tr>\n");
            }
            final int[] buckets = new int[vars.length + 1];
            final int[] bucketTotal = new int[vars.length + 1];
            for (int i = 0; i < 64; i++)
            {
                stream.write ("<tr>\n");
                total++;
                for (final NamedVariable v : vars)
                {
                    v.setStatusUnknown ();
                }
                plane.resetDerived ();
                int knownCount = 0;
                for (int j = 0; j < vars.length; j++)
                {
                    final NamedVariable v = vars[j];
                    final int bit = (i >> j) & 1;
                    if (bit != 0)
                    {
                        v.setGivenStatus (GeoStatus.known);
                        knownCount++;
                    }
                }
                if (t.isDetermined ())
                {
                    count++;
                    buckets[knownCount]++;
                }
                bucketTotal[knownCount]++;
                stream.write (String.format ("<td bgcolor=\"%s\">&nbsp;</td>\n", t.getStatus ().getColorName ()));
                for (int j = 0; j < vars.length; j++)
                {
                    final NamedVariable v = vars[j];
                    stream.write (String.format ("<td bgcolor=\"%s\">&nbsp;</td>\n", v.getStatus ().getColorName ()));
                }
                stream.write (String.format ("<td bgcolor=\"%s\">%d</td>\n", t.getStatus ().getColorName (), knownCount));
                stream.write ("</tr>\n");
            }
            stream.write ("</table>\n");
            stream.write ("<table>\n");
            stream.write (String.format (
                    "<tr><td bgcolor=\"%s\">Known</td><td bgcolor=\"%s\">Derived</td><td bgcolor=\"%s\">Unknown</td></tr>\n",

                    GeoStatus.known.getColorName (), GeoStatus.derived.getColorName (), GeoStatus.unknown.getColorName ()));
            stream.write ("</table>\n");
            stream.write ("<table>\n");
            stream.write ("<tr><th>Known</th><th>Solved</th><th>Unsolved</th><th>Total</th></tr>\n");
            for (int i = 0; i < buckets.length; i++)
            {
                stream.write (String.format ("<tr><td>%d</td><td>%d</td><td>%d</td><td>%d</td></tr>\n", i, buckets[i],
                        (bucketTotal[i] - buckets[i]), bucketTotal[i]));
            }
            stream.write ("</table>\n");
            stream.write ("<p>Solved cases: " + count + "</p>");
            stream.write ("<p>Total cases: " + total + "</p>");
            stream.write ("</body>\n");
            stream.write ("</html>\n");
        }
    }
}
