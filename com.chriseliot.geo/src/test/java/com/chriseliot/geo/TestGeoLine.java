
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

    @Test
    public void testAttributes ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final Map<String, Object> attributes = line.getAttributes ();
        assertFalse (attributes.isEmpty ());
        assertTrue (attributes.containsKey ("angle"));
        assertTrue (attributes.containsKey ("length"));
        assertTrue (attributes.containsKey ("dx"));
        assertTrue (attributes.containsKey ("dy"));
    }

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
        final String formula = mx.getFormulaInstance ();
        final String[] terms = mx.getTermNames ();

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

    @Test
    public void testCombinations ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine test = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        int count = 0;
        int total = 0;
        final NamedPoint from = test.getFrom ();
        final NamedPoint to = test.getTo ();
        final NamedPoint midpoint = test.getMidpoint ();
        final NamedVariable[] vars = {from.getX (), from.getY (), to.getX (), to.getY (), midpoint.getX (), midpoint.getY (),
                                      test.getLength (), test.getAngle ()};
        for (int i = 0; i < 256; i++)
        {
            total++;
            for (final NamedVariable v : vars)
            {
                v.setDefaultFormula ();
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
            if (test.isDetermined ())
            {
                count++;
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

        assertEquals (256, total);
        assertEquals (116, count);
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
                    v.setDefaultFormula ();
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
