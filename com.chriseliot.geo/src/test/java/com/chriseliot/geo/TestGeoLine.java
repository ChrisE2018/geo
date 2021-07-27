
package com.chriseliot.geo;

import static java.lang.Math.sqrt;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.function.Consumer;

import javax.xml.parsers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.w3c.dom.*;

import com.chriseliot.util.*;

public class TestGeoLine
{
    private final TestSupport ts = new TestSupport ();

    @Test
    public void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoLine test = new GeoLine (plane, color, from, to);
        assertNotNull (test.toString ());
    }

    @Test
    public void testGetters ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoLine test = new GeoLine (plane, color, from, to);
        assertNotNull (test.getFrom ());
        assertEquals (from, test.getFrom ().getPosition ());
        assertNotNull (test.getTo ());
        assertEquals (to, test.getTo ().getPosition ());
        assertNotNull (test.getMidpoint ());
        assertNotNull (test.getDx ());
        assertNotNull (test.getDy ());
        final Point2D.Double middle = new Point2D.Double (20, 30);
        assertEquals (middle, test.getMidpoint ().getPosition ());

        assertEquals (45.0, test.getAngle ().getDoubleValue ());
        assertEquals (1.0, test.slope ());
        assertEquals (28.284271247, test.getLength ().getDoubleValue (), TestSupport.epsilon);

        assertEquals (20, test.deltaX ());
        assertEquals (20, test.deltaY ());
    }

    @Test
    public void testPaint ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoLine test = new GeoLine (plane, color, from, to);
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Labels labels = new Labels ();
        test.setSelected (false);
        test.paint (g, labels);
        test.setSelected (true);
        test.paint (g, labels);
    }

    @Test
    public void testToString ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoLine test = new GeoLine (plane, color, from, to);
        test.setGivenStatus (GeoStatus.fixed);
        assertNotNull (test.toString ());
        test.setStatus (GeoStatus.derived, "test");
        assertNotNull (test.toString ());
    }

    @Test
    public void testGetLine ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoLine test = new GeoLine (plane, color, from, to);
        final Line2D.Double line = test.getLine2D ();
        assertEquals (10, line.getX1 ());
        assertEquals (20, line.getY1 ());
        assertEquals (30, line.getX2 ());
        assertEquals (40, line.getY2 ());
    }

    @Test
    public void testMove ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoLine test = new GeoLine (plane, color, from, to);
        test.move (50, 60);
        final Point2D.Double a = test.getFrom ().getPosition ();
        final Point2D.Double b = test.getTo ().getPosition ();
        assertEquals (60, a.x);
        assertEquals (80, a.y);
        assertEquals (80, b.x);
        assertEquals (100, b.y);
    }

    @Test
    public void testIntersection ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (-10, -10);
        final Point2D.Double to = new Point2D.Double (10, 10);
        final GeoLine test = new GeoLine (plane, color, from, to);

        // Intersect at 0, 0
        assertEquals (new Point2D.Double (0, 0), test.intersection (-10, -10, 10, 10, -10, 10, 10, -10));
        assertEquals (new Point2D.Double (0, 0), test.intersection (-10, 10, 10, -10));

        // Parallel lines do not intersect
        assertNull (test.intersection (-10, 10, 10, 10, -10, -10, 10, -10));

        // Collinear lines do not intersect
        assertNull (test.intersection (-10, 10, 10, 10, -10, 10, 10, 10));

        // Intersection is not inside segment
        assertNull (test.intersection (-10, -10, 10, 10, -10, 20, 10, 20));
        assertNull (test.intersection (-10, 20, 10, 20, -10, -10, 10, 10));
    }

    @Test
    public void testRecalculate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (0, 0);
        final Point2D.Double to = new Point2D.Double (10, 10);
        final GeoLine test = new GeoLine (plane, color, from, to);
        test.recalculate ();
        assertEquals (new Point2D.Double (5, 5), test.getMidpoint ().getPosition ());
        assertEquals (45, test.getAngle ().getDoubleValue ());
        assertEquals (sqrt (200), test.getLength ().getDoubleValue ());
        assertTrue (test.getVertices ().isEmpty ());
    }

    @Test
    public void testVerticalSlope ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        // Create a vertical line with no slope
        final Point2D.Double from = new Point2D.Double (0, 0);
        final Point2D.Double to = new Point2D.Double (0, 10);
        final GeoLine test = new GeoLine (plane, color, from, to);
        assertEquals (0, test.deltaX ());
        assertNull (test.slope ());
    }

    /**
     * The angle of this line relative to the X axis. A flat line from <0, 0> to <100, 0> will have
     * x-angle 0. As it rotates clockwise the angle increases.
     */
    @Test
    public void testAngleX ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final double[][] cases = {// Flat
                                  {0, 0, 30, 0, 0},
                                  // Up angle
                                  {0, 0, 30, 30, 45},
                                  // Straight up
                                  {0, 0, 0, 30, 90},
                                  // Up to the left
                                  {0, 0, -30, 30, 135},
                                  {0, 0, -30, 0, 180},
                                  {0, 0, -30, -30, -135},
                                  {0, 0, 0, -30, -90},
                                  {0, 0, 30, -30, -45}};
        for (final double[] testCase : cases)
        {
            final double x1 = testCase[0];
            final double y1 = testCase[1];
            final double x2 = testCase[2];
            final double y2 = testCase[3];
            final double expected = testCase[4];
            final GeoLine test1 = new GeoLine (plane, color, new Point2D.Double (x1, y1), new Point2D.Double (x2, y2));
            assertEquals (expected, test1.axisXangle ());
        }
    }

    /**
     * Angle in degrees from the y axis. The angle of this line relative to the Y axis. A line from
     * <0, 0> to <0, 100> will have a y-angle of 0. This line is straight up on the mathematical
     * plane, but straight down using screen coordinates. As it rotates clockwise the angle
     * increases.
     */
    @Test
    public void testAngleY ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final double[][] cases = {// Flat
                                  {0, 0, 30, 0, 90},
                                  // Up angle
                                  {0, 0, 30, 30, 45},
                                  // Straight up
                                  {0, 0, 0, 30, 0},
                                  // Up to the left
                                  {0, 0, -30, 30, -45},
                                  {0, 0, -30, 0, -90},
                                  {0, 0, -30, -30, -135},
                                  {0, 0, 0, -30, 180},
                                  {0, 0, 30, -30, 135}};
        for (final double[] testCase : cases)
        {
            final double x1 = testCase[0];
            final double y1 = testCase[1];
            final double x2 = testCase[2];
            final double y2 = testCase[3];
            final double expected = testCase[4];
            final GeoLine test1 = new GeoLine (plane, color, new Point2D.Double (x1, y1), new Point2D.Double (x2, y2));
            assertEquals (expected, test1.axisYangle ());
        }
    }

    @Test
    public void testAngle1 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final GeoLine test = new GeoLine (plane, color, new Point2D.Double (0, 0), new Point2D.Double (30, 0));
        assertNotNull (test.slope ());
        final GeoLine test2 = new GeoLine (plane, color, new Point2D.Double (0, 0), new Point2D.Double (0, 30));
        assertNull (test2.slope ());
        assertEquals (0, test.angle (test));
        assertEquals (0, test2.angle (test2));
        assertEquals (-90, test.angle (test2));
        assertEquals (90, test2.angle (test));
    }

    /**
     * Angle in degrees from the y axis. The angle of this line relative to the Y axis. A line from
     * <0, 0> to <0, 100> will have a y-angle of 0. This line is straight up on the mathematical
     * plane, but straight down using screen coordinates. As it rotates clockwise the angle
     * increases.
     */
    @Test
    public void testAngle2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final GeoLine flat = new GeoLine (plane, color, new Point2D.Double (0, 0), new Point2D.Double (30, 0));

        final double[][] cases = {// Flat
                                  {0, 0, 30, 0, 0},
                                  // Up angle
                                  {0, 0, 30, 30, 45},
                                  // Straight up
                                  {0, 0, 0, 30, 90},
                                  // Up to the left
                                  {0, 0, -30, 30, 135},
                                  {0, 0, -30, 0, 180},
                                  {0, 0, -30, -30, -135},
                                  {0, 0, 0, -30, -90},
                                  {0, 0, 30, -30, -45}};
        for (final double[] testCase : cases)
        {
            final double x1 = testCase[0];
            final double y1 = testCase[1];
            final double x2 = testCase[2];
            final double y2 = testCase[3];
            final double expected = testCase[4];
            final GeoLine test = new GeoLine (plane, color, new Point2D.Double (x1, y1), new Point2D.Double (x2, y2));
            System.out.printf ("p <%.2f, %.2f> angle %.2f (%.2f)\n", x2, y2, test.angle (flat), expected);
            assertEquals (expected, test.angle (flat));
        }
    }

    @Test
    public void testRecalculateVertex ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));

        // Not intersecting
        final GeoLine line4 = new GeoLine (plane, Color.blue, new Point2D.Double (300, 400), new Point2D.Double (500, 550));

        final GeoVertex v1 = line1.getVertex (line2);
        final GeoVertex v2 = line2.getVertex (line3);
        final GeoVertex v3 = line3.getVertex (line1);
        assertEquals (v1, plane.getVertex (v1.getPosition ()));
        assertEquals (v2, plane.getVertex (v2.getPosition ()));
        assertEquals (v3, plane.getVertex (v3.getPosition ()));
        assertNull (plane.getVertex (new Point2D.Double (-99, -99)));
        line1.recalculate ();
        line4.recalculate ();
        assertNull (line1.getVertex (line4));
    }

    @Test
    public void testRemoveVertex1 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));

        final GeoVertex v1 = line1.getVertex (line2);
        final GeoVertex v2 = line2.getVertex (line3);
        final GeoVertex v3 = line3.getVertex (line1);
        line1.remove (v1);
        line1.remove (v2);
        line1.remove (v3);
        assertTrue (line1.getVertices ().isEmpty ());
        assertFalse (line2.getVertices ().isEmpty ());
        assertFalse (line3.getVertices ().isEmpty ());
    }

    @Test
    public void testRemoveVertex2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));

        line1.remove ();
        assertTrue (line1.getVertices ().isEmpty ());
        assertFalse (line2.getVertices ().isEmpty ());
        assertFalse (line3.getVertices ().isEmpty ());
    }

    @Test
    public void testParallel ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        assertTrue (line1.isParallel (0, 0, 30, 0, 0, 20, 30, 20));
        assertFalse (line1.isParallel (0, 0, 30, 0, 0, 50, 30, 20));
    }

    @Test
    public void testParallel2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        assertFalse (line1.isParallel (10, 20, 30, 40, 10, 20, 50, 55));
        assertFalse (line1.isParallel (10, 20, 50, 60, 30, 40, 50, 65));
        assertFalse (line1.isParallel (30, 40, 50, 60, 10, 20, 30, 45));
    }

    /** Check variables for a standard line from <10, 20> to <30, 40> */
    private void checkLineVariables (GeoLine test)
    {
        checkLineVariables (test, null);
    }

    /** Check variables for a standard line from <10, 20> to <30, 40> */
    private void checkLineVariables (GeoLine test, String trace)
    {
        ts.checkExpression (test.getFrom ().getX (), 10, trace);
        ts.checkExpression (test.getFrom ().getY (), 20, trace);
        ts.checkExpression (test.getTo ().getX (), 30, trace);
        ts.checkExpression (test.getTo ().getY (), 40, trace);
        ts.checkExpression (test.getDx (), 20, trace);
        ts.checkExpression (test.getDy (), 20, trace);
        ts.checkExpression (test.getMidpoint ().getX (), 20, trace);
        ts.checkExpression (test.getMidpoint ().getY (), 30, trace);
        ts.checkExpression (test.getAngle (), 45, trace);
        ts.checkExpression (test.getLength (), sqrt (800), trace);
    }

    // @Test
    // public void testAttributes ()
    // {
    // final GeoPlane plane = new GeoPlane ();
    // final GeoLine line = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new
    // Point2D.Double (30, 40));
    // final Map<String, Object> attributes = line.getAttributes ();
    // assertFalse (attributes.isEmpty ());
    // assertTrue (attributes.containsKey ("angle"));
    // assertTrue (attributes.containsKey ("length"));
    // assertTrue (attributes.containsKey ("dx"));
    // assertTrue (attributes.containsKey ("dy"));
    // }

    @Test
    public void testXmlAttributes () throws ParserConfigurationException
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));

        final XMLUtil xu = new XMLUtil ();
        final DocumentBuilder builder = xu.getDocumentBuilder ();
        final Document doc = builder.newDocument ();
        final Element element = doc.createElement ("test");
        assertNotNull (element);
        line.getAttributes (element);
        assertEquals (line.getName (), xu.get (element, "name", "missing"));
        assertEquals ("false", xu.get (element, "open", "missing"));

        final Element root = doc.createElement ("root");
        final String name = GeoItem.class.getSimpleName ();
        assertNull (xu.getNthChild (root, name, 0));
        line.getElement (root);
    }

    @Test
    public void testPopup ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));

        final Map<String, Consumer<GeoItem>> popup = new HashMap<> ();
        line.popup (popup);
        assertNotNull (popup.get ("Delete"));
        popup.get ("Delete").accept (line);
    }

    /** Check the computation of midpoint. */
    @Test
    public void testSolve1 ()
    {
        Namer.reset ();
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        test.getFrom ().getX ().setGivenStatus (GeoStatus.known);
        test.getTo ().getX ().setGivenStatus (GeoStatus.known);
        final NamedVariable mx = test.getMidpoint ().getX ();
        final Inference inference = mx.getInference ();
        final String formula = inference.getInstantiation ();
        final String[] terms = inference.getTermNames ();

        final ExprEvaluator eval = new ExprEvaluator ();
        for (int i = 1; i < terms.length; i++)
        {
            final String var = terms[i];
            System.out.printf ("%s = %s\n", var, i);
            eval.defineVariable (var, i);
        }
        assertEquals ("l01$M$x == l01$A$x + (l01$dx / 2)", formula);
        final IExpr expr = eval.parse (formula);
        final IExpr f = expr.getAt (2);
        assertEquals (2.0, eval.evalf (f), TestSupport.epsilon);
    }

    /** Check all child values. */
    @Test
    public void testSolve2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        test.getFrom ().setGivenStatus (GeoStatus.known);
        test.getTo ().setGivenStatus (GeoStatus.known);
        checkLineVariables (test);
    }

    @Test
    public void testSolve3 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        // Other order for more coverage
        test.getTo ().setGivenStatus (GeoStatus.known);
        test.getFrom ().setGivenStatus (GeoStatus.known);
    }

    @Test
    public void testSolve4 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        test.getAngle ().setGivenStatus (GeoStatus.known);
        test.getLength ().setGivenStatus (GeoStatus.known);
        test.getFrom ().getX ().setGivenStatus (GeoStatus.known);
        test.getFrom ().getY ().setGivenStatus (GeoStatus.known);
        assertTrue (test.isDetermined ());
        checkLineVariables (test);
    }

    @Test
    public void testSolve5 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        test.getFrom ().getY ().setGivenStatus (GeoStatus.known);
        test.getFrom ().getX ().setGivenStatus (GeoStatus.known);
        // Reverse order to get more coverage
        test.getDy ().setGivenStatus (GeoStatus.known);
        test.getDx ().setGivenStatus (GeoStatus.known);
        assertTrue (test.isDetermined ());
        checkLineVariables (test);
    }

    @Test
    public void testSolve6 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        test.getTo ().getY ().setGivenStatus (GeoStatus.known);
        test.getTo ().getX ().setGivenStatus (GeoStatus.known);
        test.getDy ().setGivenStatus (GeoStatus.known);
        test.getDx ().setGivenStatus (GeoStatus.known);
        assertTrue (test.isDetermined ());
        checkLineVariables (test);
    }

    @Test
    public void testSolve7 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        test.getFrom ().getY ().setGivenStatus (GeoStatus.known);
        test.getFrom ().getX ().setGivenStatus (GeoStatus.known);
        test.getMidpoint ().getX ().setGivenStatus (GeoStatus.known);
        test.getMidpoint ().getY ().setGivenStatus (GeoStatus.known);
        assertTrue (test.isDetermined ());
        final String trace = null; // "testSolve7"
        checkLineVariables (test, trace);
    }

    @Test
    public void testSolve8 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        test.getTo ().getY ().setGivenStatus (GeoStatus.known);
        test.getTo ().getX ().setGivenStatus (GeoStatus.known);
        test.getMidpoint ().getX ().setGivenStatus (GeoStatus.known);
        test.getMidpoint ().getY ().setGivenStatus (GeoStatus.known);
        assertTrue (test.isDetermined ());
        final String trace = null; // "testSolve8"
        checkLineVariables (test, trace);
    }

    @Test
    public void testSolve9 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        test.getFrom ().getY ().setGivenStatus (GeoStatus.known);
        test.getFrom ().getX ().setGivenStatus (GeoStatus.known);
        // Reverse order to get more coverage
        test.getMidpoint ().getY ().setGivenStatus (GeoStatus.known);
        test.getMidpoint ().getX ().setGivenStatus (GeoStatus.known);
        assertTrue (test.isDetermined ());
        checkLineVariables (test);
    }

    // @Test
    // public void createParams ()
    // {
    // for (int i = 0; i < 256; i++)
    // {
    // if (i % 32 == 0)
    // {
    // System.out.printf ("\n");
    // }
    // System.out.printf ("%d, ", i);
    // }
    // System.out.printf ("\n");
    // }

    // private boolean expectDetermined (int i)
    // {
    // // One based numbering
    // final int[] expected =
    // {16, 28, 31, 32, 40, 46, 48, 52, 55, 56, 58, 60, 61, 62, 63, 64, 72, 76, 78, 79, 80, 84, 87,
    // 88, 90, 92, 93, 94, 95,
    // 96, 100, 103, 104, 106, 108, 109, 110, 111, 112, 116, 119, 120, 122, 124, 125, 126, 127, 128,
    // 136, 140, 142, 143,
    // 144, 148, 151, 152, 154, 156, 157, 158, 159, 160, 164, 167, 168, 170, 172, 173, 174, 175,
    // 176, 180, 183, 184, 186,
    // 188, 189, 190, 191, 192, 196, 199, 200, 202, 204, 205, 206, 207, 208, 212, 215, 216, 218,
    // 220, 221, 222, 223, 224,
    // 228, 231, 232, 234, 236, 237, 238, 239, 240, 244, 247, 248, 250, 252, 253, 254, 255, 256};
    // for (final int v : expected)
    // {
    // if (v == i)
    // {
    // return true;
    // }
    // }
    // return false;
    // }

    // @ParameterizedTest
    // @ValueSource (ints =
    // {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25,
    // 26, 27, 28, 29, 30, 31, 32, 33,
    // 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56,
    // 57, 58, 59, 60, 61, 62, 63, 64,
    // 65, 66, 67, 68, 69, 70, 71, 72, 73, 74, 75, 76, 77, 78, 79, 80, 81, 82, 83, 84, 85, 86, 87,
    // 88, 89, 90, 91, 92, 93, 94, 95,
    // 96, 97, 98, 99, 100, 101, 102, 103, 104, 105, 106, 107, 108, 109, 110, 111, 112, 113, 114,
    // 115, 116, 117, 118, 119, 120, 121,
    // 122, 123, 124, 125, 126, 127, 128, 129, 130, 131, 132, 133, 134, 135, 136, 137, 138, 139,
    // 140, 141, 142, 143, 144, 145, 146,
    // 147, 148, 149, 150, 151, 152, 153, 154, 155, 156, 157, 158, 159, 160, 161, 162, 163, 164,
    // 165, 166, 167, 168, 169, 170, 171,
    // 172, 173, 174, 175, 176, 177, 178, 179, 180, 181, 182, 183, 184, 185, 186, 187, 188, 189,
    // 190, 191, 192, 193, 194, 195, 196,
    // 197, 198, 199, 200, 201, 202, 203, 204, 205, 206, 207, 208, 209, 210, 211, 212, 213, 214,
    // 215, 216, 217, 218, 219, 220, 221,
    // 222, 223, 224, 225, 226, 227, 228, 229, 230, 231, 232, 233, 234, 235, 236, 237, 238, 239,
    // 240, 241, 242, 243, 244, 245, 246,
    // 247, 248, 249, 250, 251, 252, 253, 254, 255, 256})
    // public void testCombination (int i)
    // { // One based numbering
    // testCombination (i - 1, expectDetermined (i));
    // }

    // @Test
    // public void createParams ()
    // {
    // for (int i = 0; i < 256; i++)
    // {
    // if (i % 32 == 0)
    // {
    // System.out.printf ("\n");
    // }
    // final boolean expect = expectDetermined (i + 1);
    // System.out.printf ("\"%d,%s\", ", i, expect);
    // }
    // System.out.printf ("\n");
    // }

    @ParameterizedTest
    @CsvSource (
    {"0,false", "1,false", "2,false", "3,false", "4,false", "5,false", "6,false", "7,false", "8,false", "9,false", "10,false",
     "11,false", "12,false", "13,false", "14,false", "15,true", "16,false", "17,false", "18,false", "19,false", "20,false",
     "21,false", "22,false", "23,false", "24,false", "25,false", "26,false", "27,true", "28,false", "29,false", "30,true",
     "31,true", "32,false", "33,false", "34,false", "35,false", "36,false", "37,false", "38,false", "39,true", "40,false",
     "41,false", "42,false", "43,false", "44,false", "45,true", "46,false", "47,true", "48,false", "49,false", "50,false",
     "51,true", "52,false", "53,false", "54,true", "55,true", "56,false", "57,true", "58,false", "59,true", "60,true", "61,true",
     "62,true", "63,true", "64,false", "65,false", "66,false", "67,false", "68,false", "69,false", "70,false", "71,true",
     "72,false", "73,false", "74,false", "75,true", "76,false", "77,true", "78,true", "79,true", "80,false", "81,false",
     "82,false", "83,true", "84,false", "85,false", "86,true", "87,true", "88,false", "89,true", "90,false", "91,true", "92,true",
     "93,true", "94,true", "95,true", "96,false", "97,false", "98,false", "99,true", "100,false", "101,false", "102,true",
     "103,true", "104,false", "105,true", "106,false", "107,true", "108,true", "109,true", "110,true", "111,true", "112,false",
     "113,false", "114,false", "115,true", "116,false", "117,false", "118,true", "119,true", "120,false", "121,true", "122,false",
     "123,true", "124,true", "125,true", "126,true", "127,true", "128,false", "129,false", "130,false", "131,false", "132,false",
     "133,false", "134,false", "135,true", "136,false", "137,false", "138,false", "139,true", "140,false", "141,true", "142,true",
     "143,true", "144,false", "145,false", "146,false", "147,true", "148,false", "149,false", "150,true", "151,true", "152,false",
     "153,true", "154,false", "155,true", "156,true", "157,true", "158,true", "159,true", "160,false", "161,false", "162,false",
     "163,true", "164,false", "165,false", "166,true", "167,true", "168,false", "169,true", "170,false", "171,true", "172,true",
     "173,true", "174,true", "175,true", "176,false", "177,false", "178,false", "179,true", "180,false", "181,false", "182,true",
     "183,true", "184,false", "185,true", "186,false", "187,true", "188,true", "189,true", "190,true", "191,true", "192,false",
     "193,false", "194,false", "195,true", "196,false", "197,false", "198,true", "199,true", "200,false", "201,true", "202,false",
     "203,true", "204,true", "205,true", "206,true", "207,true", "208,false", "209,false", "210,false", "211,true", "212,false",
     "213,false", "214,true", "215,true", "216,false", "217,true", "218,false", "219,true", "220,true", "221,true", "222,true",
     "223,true", "224,false", "225,false", "226,false", "227,true", "228,false", "229,false", "230,true", "231,true", "232,false",
     "233,true", "234,false", "235,true", "236,true", "237,true", "238,true", "239,true", "240,false", "241,false", "242,false",
     "243,true", "244,false", "245,false", "246,true", "247,true", "248,false", "249,true", "250,false", "251,true", "252,true",
     "253,true", "254,true", "255,true",})
    public void testCsvCombination (String input, String expected)
    {
        // Zero based numbering
        testCombination (Integer.parseInt (input), Boolean.parseBoolean (expected));
    }

    /**
     *
     * @param i Zero based numbering
     * @param expected
     */
    public void testCombination (int i, boolean expected)
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final NamedPoint from = test.getFrom ();
        final NamedPoint to = test.getTo ();
        final NamedPoint midpoint = test.getMidpoint ();
        final NamedVariable[] vars = {from.getX (), from.getY (), to.getX (), to.getY (), midpoint.getX (), midpoint.getY (),
                                      test.getLength (), test.getAngle ()};

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
        assertEquals (expected, test.isDetermined ());
        if (test.isDetermined ())
        {
            checkLineVariables (test);
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

    @Test
    public void testReportCombinations () throws IOException
    {
        final File file = ts.getTestDataFile (this, "Line Combinations", "html");
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        int count = 0;
        int total = 0;
        final boolean showVariablesRow = false;
        final NamedPoint from = test.getFrom ();
        final NamedPoint to = test.getTo ();
        final NamedPoint midpoint = test.getMidpoint ();
        final NamedVariable[] vars = {from.getX (), from.getY (), to.getX (), to.getY (), midpoint.getX (), midpoint.getY (),
                                      test.getLength (), test.getAngle ()};

        try (final BufferedWriter stream = new BufferedWriter (new FileWriter (file)))
        {
            stream.write ("<html><head><title>Triangle Test Data</title></head>\n");
            stream.write ("<body>\n");
            stream.write ("<h1>Triangle Test Data</h1>\n");
            stream.write ("<h2>\n");
            stream.write (new Date ().toString ());
            stream.write ("</h2>\n");
            final int[] buckets = new int[vars.length + 1];
            final int[] bucketTotal = new int[vars.length + 1];
            stream.write ("<table>\n");
            for (int i = 0; i < 256; i++)
            {
                if (i % 32 == 0)
                {
                    stream.write (
                            "<tr><th>Case</th><th>Line</th><th>Ax</th><th>Ay</th><th>Bx</th><th>By</th><th>Mx</th><th>My</th>\n");
                    stream.write ("<th>Length</th><th>Angle</th><th>Knowns</th></tr>\n");
                    if (showVariablesRow)
                    {
                        stream.write ("<tr><td>&nbsp;</td>\n");
                        for (int j = 0; j < vars.length; j++)
                        {
                            final NamedVariable v = vars[j];
                            stream.write ("<th>" + v.getName () + "</th>\n");
                        }
                        stream.write ("</tr>\n");
                    }
                }
                stream.write ("<tr>\n");
                stream.write ("<td>" + i + "</td>\n");
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
                    // @see
                    // https://stackoverflow.com/questions/14145733/how-can-one-read-an-integer-bit-by-bit-in-java/14145767
                    final int bit = (i >> j) & 1;
                    if (bit != 0)
                    {
                        v.setGivenStatus (GeoStatus.known);
                        knownCount++;
                    }
                }
                if (test.isDetermined ())
                {
                    count++;
                    buckets[knownCount]++;
                }
                bucketTotal[knownCount]++;
                stream.write (String.format ("<td bgcolor=\"%s\">&nbsp;</td>\n", test.getStatus ().getColorName ()));
                for (int j = 0; j < vars.length; j++)
                {
                    final NamedVariable v = vars[j];
                    stream.write (String.format ("<td bgcolor=\"%s\">&nbsp;</td>\n", v.getStatus ().getColorName ()));
                }
                stream.write (String.format ("<td bgcolor=\"%s\">%d</td>\n", test.getStatus ().getColorName (), knownCount));
                stream.write ("</tr>\n");
            }

            stream.write ("</table>\n");
            stream.write ("<table>\n");
            stream.write (String.format (
                    "<tr><td bgcolor=\"%s\">Known</td><td bgcolor=\"%s\">Derived</td><td bgcolor=\"%s\">Unknown</td></tr>\n",
                    GeoStatus.known.getColorName (), GeoStatus.derived.getColorName (), GeoStatus.unknown.getColorName ()));
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
