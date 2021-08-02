
package com.chriseliot.geo;

import static java.lang.Math.sqrt;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.xml.parsers.*;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import com.chriseliot.util.*;

public class TestGeoVertex
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());
    private final TestSupport ts = new TestSupport ();

    @Test
    public void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 60));
        final GeoVertex test = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));
        assertNotNull (test.toString ());
    }

    @Test
    public void testGetters ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 30));
        final GeoVertex test = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));
        assertEquals (line1, test.getLine1 ());
        assertEquals (line2, test.getLine2 ());
        assertEquals (new Point2D.Double (10, 20), test.getPosition ());
        final NamedPoint vertex = test.getVertex ();
        assertEquals (new Point2D.Double (10, 20), vertex.getPosition ());
        final NamedVariable angle = test.getAngle ();
        assertNotNull (angle);
        assertFalse (test.isSelected ());
        test.setSelected (true);
        assertTrue (test.isSelected ());
        test.setSelected (false);
        assertFalse (test.isSelected ());
        assertTrue (test.getTriangles ().isEmpty ());
    }

    @Test
    public void testSimple ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (15, 5), new Point2D.Double (50, 55));
        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));
        final GeoVertex v2 = new GeoVertex (plane, Color.green, line2, line3, new Point2D.Double (50, 55));
        assertTrue (v1.at (new Point2D.Double (10, 20)));
        assertTrue (v1.hasLine (line1));
        assertTrue (v1.hasLine (line2));
        assertFalse (v1.hasLine (line3));
        v1.recalculate ();
        v1.remove ();
        assertNotNull (v1.getPosition ());
        assertNotNull (v2.getPosition ());
        assertEquals (2825, v1.distance2 (v2));
        assertEquals (sqrt (2825), v1.distance (v2));
        v1.setPosition (null);
        assertNull (v1.getPosition ());
    }

    @Test
    public void testRightAngles ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (0, 100));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (100, 0));
        final GeoVertex test = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (0, 0));

        final NamedVariable angle = test.getAngle ();
        assertNotNull (angle);
        assertEquals (-90.0, angle.getDoubleValue ());
    }

    @Test
    public void testAngles ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (0, 100));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (100, 0));
        final GeoVertex test = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (0, 0));
        assertEquals (0, test.getAngleTo (new Point2D.Double (0, 100)));
        assertEquals (45, test.getAngleTo (new Point2D.Double (100, 100)));
        assertEquals (90, test.getAngleTo (new Point2D.Double (100, 0)));
        assertEquals (180, test.getAngleFrom (new Point2D.Double (0, 100)));
        assertEquals (225, test.getAngleFrom (new Point2D.Double (100, 100)));
        assertEquals (270, test.getAngleFrom (new Point2D.Double (100, 0)));
    }

    @Test
    public void testPaint ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 60));
        final GeoVertex test = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Labels labels = new Labels ();
        test.setPosition (new Point2D.Double (10, 20));
        assertNotNull (test.getPosition ());
        assertNotNull (test.getIntPosition ());
        test.paint (g, labels);
        test.setSelected (true);
        test.paint (g, labels);
        test.recalculate ();
        test.setSelected (true);
        test.paint (g, labels);
    }

    @Test
    public void testConnects ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));
        final GeoLine line4 = new GeoLine (plane, Color.blue, new Point2D.Double (50, 55), new Point2D.Double (100, 100));
        final GeoLine line5 = new GeoLine (plane, Color.blue, new Point2D.Double (100, 100), new Point2D.Double (110, 110));
        assertFalse (line1.isParallel (10, 20, 30, 40, 10, 20, 50, 55));
        assertFalse (line1.isParallel (10, 20, 50, 55, 30, 40, 50, 55));
        assertFalse (line1.isParallel (30, 40, 50, 55, 10, 20, 30, 40));
        assertNotNull (line1.intersection (line2));
        assertNotNull (line2.intersection (line3));
        assertNotNull (line3.intersection (line1));
        assertEquals (new Point2D.Double (10, 20), line1.intersection (line2));
        assertEquals (new Point2D.Double (50, 55), line2.intersection (line3));
        assertEquals (new Point2D.Double (30, 40), line3.intersection (line1));
        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));
        final GeoVertex v2 = new GeoVertex (plane, Color.green, line2, line3, new Point2D.Double (50, 55));
        final GeoVertex v3 = new GeoVertex (plane, Color.green, line3, line1, new Point2D.Double (30, 40));
        final GeoVertex v4 = new GeoVertex (plane, Color.green, line4, line5, new Point2D.Double (100, 100));
        assertTrue (v1.connects (v2));
        assertTrue (v2.connects (v3));
        assertTrue (v3.connects (v1));
        assertTrue (v2.connects (v1));

        assertFalse (v1.connects (v4));
        assertFalse (v2.connects (v4));
        assertFalse (v4.connects (v1));

        assertEquals (line1, v1.connector (v3));
        assertEquals (line2, v2.connector (v1));
        assertEquals (line3, v3.connector (v2));

        assertEquals (line1, v3.connector (v1));
        assertEquals (line2, v1.connector (v2));
        assertEquals (line3, v2.connector (v3));
        assertNull (v4.connector (v1));
    }

    @Test
    public void testTriangles ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));
        final GeoVertex v2 = new GeoVertex (plane, Color.green, line2, line3, new Point2D.Double (50, 55));
        final GeoVertex v3 = new GeoVertex (plane, Color.green, line3, line1, new Point2D.Double (30, 40));

        // Force vertices into the lines. Normally they are added by findVertices
        line1.getVertices ().add (v1);
        line2.getVertices ().add (v1);
        line2.getVertices ().add (v2);
        line3.getVertices ().add (v2);
        line3.getVertices ().add (v3);
        line1.getVertices ().add (v3);

        final GeoTriangle t = new GeoTriangle (plane, Color.red, v1, v2, v3);
        assertTrue (v1.getTriangles ().contains (t));
        assertTrue (v2.getTriangles ().contains (t));
        assertTrue (v3.getTriangles ().contains (t));
        assertTrue (v1.getLine1 ().getVertices ().contains (v1));
        assertTrue (v2.getLine1 ().getVertices ().contains (v2));
        assertTrue (v3.getLine1 ().getVertices ().contains (v3));
        v1.remove ();
        v2.remove ();
        v3.remove ();
        assertFalse (v1.getLine1 ().getVertices ().contains (v1));
        assertFalse (v2.getLine1 ().getVertices ().contains (v2));
        assertFalse (v3.getLine1 ().getVertices ().contains (v3));
        assertFalse (t.getPlane ().contains (t));
    }

    @Test
    public void testXmlAttributes () throws ParserConfigurationException
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));

        final XMLUtil xu = new XMLUtil ();
        final DocumentBuilder builder = xu.getDocumentBuilder ();
        final Document doc = builder.newDocument ();
        final Element element = doc.createElement ("test");
        assertNotNull (element);
        v1.getAttributes (element);
        assertEquals (v1.getName (), xu.get (element, "name", "missing"));
        assertEquals ("false", xu.get (element, "open", "missing"));

        final Element root = doc.createElement ("root");
        final String name = GeoItem.class.getSimpleName ();
        assertNull (xu.getNthChild (root, name, 0));
        v1.getElement (root);
    }

    @Test
    public void testSolve1 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (0, 10));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (10, 0));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (0, 0));
        logger.info ("V1 %s angle %.2f", v1.getName (), v1.getAngle ().getDoubleValue ());
        v1.getVertex ().setGivenStatus (GeoStatus.known);
        ts.checkExpression (v1.getVertex ().getX (), 0);
        ts.checkExpression (v1.getVertex ().getY (), 0);
        // Lines are at right angles
        ts.checkExpression (v1.getAngle (), 90);
    }

    @Test
    public void testSolve2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (0, 10));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (10, 0));

        // Reverse lines
        final GeoVertex v1 = new GeoVertex (plane, Color.green, line2, line1, new Point2D.Double (0, 0));
        v1.getVertex ().setGivenStatus (GeoStatus.known);
        final String trace = null;
        ts.checkExpression (v1.getVertex ().getX (), 0, trace);
        ts.checkExpression (v1.getVertex ().getY (), 0, trace);
        // Counterclockwise rotation
        ts.checkExpression (v1.getAngle (), -90, trace);
    }

    @Test
    public void testSolve3 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (0, 10));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (10, 10));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (0, 0));
        v1.getVertex ().setGivenStatus (GeoStatus.known);
        ts.checkExpression (v1.getVertex ().getX (), 0);
        ts.checkExpression (v1.getVertex ().getY (), 0);
        // 45 degree angle
        ts.checkExpression (v1.getAngle (), 45);
    }

    @Test
    public void testSolve4 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (5, 10));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (10, 5));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (0, 0));
        v1.getVertex ().setGivenStatus (GeoStatus.known);
        ts.checkExpression (v1.getVertex ().getX (), 0);
        ts.checkExpression (v1.getVertex ().getY (), 0);
        ts.checkExpression (v1.getAngle (), 36.869897);
    }

    @Test
    public void testSolve5 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));
        v1.getVertex ().setGivenStatus (GeoStatus.known);
        final String trace = null;// "testSolve5";
        ts.checkExpression (v1.getVertex ().getX (), 10, trace);
        ts.checkExpression (v1.getVertex ().getY (), 20, trace);
        ts.checkExpression (v1.getAngle (), 3.81407, trace);
    }

    @Test
    public void testSolve6 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (0, 0), new Point2D.Double (20, 20));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (0, 0), new Point2D.Double (40, 35));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (0, 0));
        v1.getVertex ().setGivenStatus (GeoStatus.known);
        line1.getAngle ().setGivenStatus (GeoStatus.known);
        line2.getAngle ().setGivenStatus (GeoStatus.known);
        final String trace = null;// "testSolve6";
        ts.checkExpression (v1.getVertex ().getX (), 0, trace);
        ts.checkExpression (v1.getVertex ().getY (), 0, trace);
        ts.checkExpression (v1.getAngle (), -3.81407, trace);
    }

    @Test
    public void testSolve7 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));
        v1.getVertex ().setGivenStatus (GeoStatus.known);
        line1.getAngle ().setGivenStatus (GeoStatus.known);
        line2.getAngle ().setGivenStatus (GeoStatus.known);
        final String trace = null;// "testSolve7";
        ts.checkExpression (v1.getVertex ().getX (), 10, trace);
        ts.checkExpression (v1.getVertex ().getY (), 20, trace);
        ts.checkExpression (v1.getAngle (), -3.81407, trace);
    }

    @Test
    public void testSolve8 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));

        line1.getAngle ().setGivenStatus (GeoStatus.known);
        line2.getAngle ().setGivenStatus (GeoStatus.known);
        final String trace = null;// "testSolve8";
        ts.checkExpression (v1.getVertex ().getX (), 10, trace);
        ts.checkExpression (v1.getVertex ().getY (), 20, trace);
        ts.checkExpression (v1.getAngle (), -3.81407, trace);
    }

    @Test
    public void testSolve9 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (10, 20));

        v1.setGivenStatus (GeoStatus.known);
        line2.getAngle ().setGivenStatus (GeoStatus.known);
        assertEquals (GeoStatus.derived, v1.getVertex ().getStatus ());
    }
}
