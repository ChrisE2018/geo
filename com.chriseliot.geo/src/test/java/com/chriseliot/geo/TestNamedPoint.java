
package com.chriseliot.geo;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Consumer;

import javax.swing.SwingConstants;
import javax.xml.parsers.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;
import org.w3c.dom.*;

import com.chriseliot.geo.gui.CloseDialogThread;
import com.chriseliot.util.*;

public class TestNamedPoint
{
    private final TestSupport ts = new TestSupport ();

    @Test
    public void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        assertNotNull (test.toString ());
    }

    @Test
    public void testCreate2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        assertNotNull (test.toString ());
    }

    @Test
    public void testGetters ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        assertFalse (test.isDraggable ());
        assertFalse (test.isOpen ());
        final Point2D.Double position = new Point2D.Double (10, 20);
        assertEquals (position, test.getPosition ());
        assertEquals (new Point (10, 20), test.getIntPosition ());
        assertEquals (SwingConstants.NORTH_WEST, test.getAnchor ());
        assertEquals (10, test.getX ().getDoubleValue ());
        assertEquals (20, test.getY ().getDoubleValue ());
    }

    @Test
    public void testSetters ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);

        assertFalse (test.isOpen ());
        test.setOpen (true);
        assertTrue (test.isOpen ());
        test.setOpen (false);
        assertFalse (test.isOpen ());
        test.drag (new Point2D.Double (30, 50));
        assertEquals (new Point2D.Double (30, 50), test.getPosition ());
        assertTrue (test.at (new Point2D.Double (30, 50)));
        assertFalse (test.at (new Point2D.Double (33, 50)));
        assertFalse (test.at (new Point2D.Double (30, 55)));
        // test.drag (null);
        // assertFalse (test.at (new Point2D.Double (30, 50)));
        test.setPosition (113, 57);
        assertTrue (test.at (new Point2D.Double (113, 57)));
        test.setPosition (new Point2D.Double (112, 17));
        assertTrue (test.at (new Point2D.Double (112, 17)));
    }

    @Test
    public void testDistance ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);

        final Point2D.Double position = new Point2D.Double (10, 20);
        assertEquals (position, test.getPosition ());
        assertEquals (0, test.distance2 (position));
        assertEquals (0, test.distance2 (new Point (10, 20)));
        assertEquals (0, test.distance2 (test));
        assertEquals (100, test.distance2 (new Point2D.Double (20, 20)));
        assertEquals (100, test.distance2 (new Point (20, 20)));
        assertTrue (test.at (position));
        assertFalse (test.at (new Point2D.Double (30, 10)));
        final Point2D.Double p = null;
        assertFalse (test.at (p));
        assertTrue (test.at (test));
    }

    @Test
    public void testPaint ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Labels labels = new Labels ();
        test.paint (g, labels);
        test.setStatus (GeoStatus.derived, "test");
        test.paint (g, labels);
    }

    @Test
    public void testToStringNull ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        assertNotNull (test.toString ());
        test.setPosition (null);
        assertNotNull (test.toString ());
    }

    /** Check variables for a standard point at <10, 20> */
    private void checkVariables (NamedPoint test)
    {
        final String trace = null;
        ts.checkExpression (test.getX (), 10, trace);
        ts.checkExpression (test.getY (), 20, trace);
    }

    @Test
    public void testSolve1 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        final NamedPoint p = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        test.getX ().setGivenStatus (GeoStatus.known);
        test.getY ().setGivenStatus (GeoStatus.known);
        checkVariables (test);
        checkVariables (p);
    }

    @Test
    public void testSolve2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        final NamedPoint p = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        // Reverse for more coverage
        test.getY ().setGivenStatus (GeoStatus.known);
        test.getX ().setGivenStatus (GeoStatus.known);
        checkVariables (test);
        checkVariables (p);
    }

    @Test
    public void testSolve3 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        final NamedPoint p = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        p.getX ().setGivenStatus (GeoStatus.known);
        p.getY ().setGivenStatus (GeoStatus.known);
        checkVariables (test);
        checkVariables (p);
    }

    @Test
    public void testSolve4 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);

        test.setGivenStatus (GeoStatus.known);
        checkVariables (test);
        assertNotNull (test.toString ());
    }

    @Test
    public void testEquivalent ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "parent", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        final NamedPoint alpha = new NamedPoint (parent, false, Color.red, "alpha", 10, 20, SwingConstants.NORTH_WEST);
        final NamedPoint beta = new NamedPoint (parent, false, Color.red, "beta", 15, 30, SwingConstants.NORTH_WEST);
        test.setGivenStatus (GeoStatus.known);
        assertTrue (test.getX ().isDetermined ());
        assertTrue (test.getY ().isDetermined ());
        assertTrue (alpha.getX ().isDetermined ());
        assertTrue (alpha.getY ().isDetermined ());
        // Beta is not in the same place so propagation should not occur
        beta.getX ().whyDetermined ();
        assertFalse (beta.getX ().isDetermined ());
        assertFalse (beta.getY ().isDetermined ());
    }

    @DisabledIfSystemProperty (named = "java.awt.headless", matches = "true")
    @Test
    public void testPopup ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);

        final Map<String, Consumer<GeoItem>> result = new HashMap<> ();
        test.popup (result);
        assertEquals (3, result.size ());
        assertTrue (result.containsKey ("known"));
        assertTrue (result.containsKey ("unknown"));
        assertTrue (result.containsKey ("Set Value"));

        final CloseDialogThread thread = new CloseDialogThread ();
        thread.start ();
        result.get ("Set Value").accept (test);
        thread.halt ();
        TestSupport.dream (10);
        System.out.printf ("Set Value dialog returns\n");
        assertTrue (thread.isDialogSeen ());
    }

    @Test
    public void testCanSetValue ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        test.setValueAction (new Point2D.Double (20, 30));
    }

    @Test
    public void testXmlAttributes () throws ParserConfigurationException
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);

        final XMLUtil xu = new XMLUtil ();
        final DocumentBuilder builder = xu.getDocumentBuilder ();
        final Document doc = builder.newDocument ();
        final Element element = doc.createElement ("test");
        assertNotNull (element);
        test.getAttributes (element);
        assertEquals (test.getX ().getName (), xu.get (element, "x", "missing"));
        assertEquals (test.getY ().getName (), xu.get (element, "y", "missing"));
    }
}
