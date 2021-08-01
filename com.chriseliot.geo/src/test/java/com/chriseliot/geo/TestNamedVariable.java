
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.SwingConstants;
import javax.xml.parsers.*;

import org.junit.jupiter.api.Test;
import org.w3c.dom.*;

import com.chriseliot.util.*;

public class TestNamedVariable
{
    @Test
    public void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final Color color = Color.red;
        final String name = "test1";
        assertNotNull (new NamedVariable (parent, color, name).toString ());
        assertNotNull (new NamedVariable (parent, color, name, 43.0).toString ());
    }

    @Test
    public void testCreate2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint parent = new NamedPoint (item, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedVariable v1 = new NamedVariable (parent, Color.red, "test1");
        final NamedVariable v2 = new NamedVariable (parent, Color.red, "test1", 43.0);
        assertNotNull (v1.toString ());
        assertNotNull (v2.toString ());
    }

    @Test
    public void testGetters ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final Color color = Color.red;
        final String name = "test1";
        final NamedVariable test = new NamedVariable (parent, color, name, 43.0);
        assertEquals (43.0, test.getDoubleValue ());
        test.setDoubleValue (17.0);
        assertEquals (17.0, test.getDoubleValue ());
        final Inference inference = test.getInference ();
        assertNull (inference);
    }

    @Test
    public void testLocation ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint parent = new NamedPoint (item, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedVariable v1 = new NamedVariable (parent, Color.red, "test1");
        final NamedVariable v2 = new NamedVariable (parent, Color.red, "test1", 43.0);
        assertEquals (parent, v1.getLocation ());
        assertEquals (parent, v2.getLocation ());
        v1.setLocation (parent);
    }

    @Test
    public void testFormula ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final Color color = Color.red;
        final String name = "test1";
        final NamedVariable test = new NamedVariable (parent, color, name, 43.0);
        test.setFormula ("test", "%s == 43", test);
        final Inference inference = test.getInference ();
        assertNotNull (inference);
        assertEquals ("test1 == 43", inference.getInstantiation ());
        plane.solve ();
    }

    @Test
    public void testDerivedFormula ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final Color color = Color.red;
        final String name = "test1";
        final NamedVariable test = new NamedVariable (parent, color, name, 43.0);
        final NamedVariable x = new NamedVariable (parent, color, "x", 44.0);
        final NamedVariable y = new NamedVariable (parent, color, "y", 45.0);
        test.setFormula ("test", "c == 43.0", test);
        assertEquals ("c==43.0", test.getDerivedFormula ());
        plane.solve ();
        x.setStatusUnknown ();
        y.setFormula ("test", "%s == 45", y);
        test.setFormula ("test", "%s == %s - 2", test, y);
        assertEquals ("c==43.0", test.getDerivedFormula ());
    }

    @Test
    public void testSolve ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final Point2D.Double position2 = new Point2D.Double (12, 22);
        final NamedPoint test = new NamedPoint (parent, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedPoint test2 = new NamedPoint (parent, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedPoint test3 = new NamedPoint (parent, false, Color.green, "test", position2, SwingConstants.NORTH_WEST);
        test.getX ().setFormula ("test", "%s == 10", test.getX ());
        plane.solve ();
        assertNotNull (test2);
        assertNotNull (test3);
        assertNotNull (test.getX ().toString ());
    }

    @Test
    public void testPaint ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint parent = new NamedPoint (item, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedVariable v = new NamedVariable (parent, Color.red, "test1");
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Labels labels = new Labels ();
        v.paint (g, labels);
        v.setFormula ("test", "2 == 1 + 1", v);
        final Inference inference = v.getInference ();
        assertNotNull (inference);
        assertEquals ("2 == 1 + 1", inference.getInstantiation ());
        v.paint (g, labels);
        v.setDoubleValue (12.0);
        v.paint (g, labels);

        // Set location to null to get other branch
        v.setLocation (null);
        v.paint (g, labels);
    }

    @Test
    public void testPopupSupport ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint parent = new NamedPoint (item, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedVariable v = new NamedVariable (parent, Color.red, "test1");
        assertNotEquals (GeoStatus.derived, v.getStatus ());
        assertNotEquals (v, parent.getX ());
        assertNotEquals (v, parent.getY ());

        v.setFormula ("test", "%s == 55", v);
        assertTrue (v.isDetermined ());

        // Try to change to a conflicting name
        final String oldName = v.getName ();
        v.renameVariableAction ("test");
        assertEquals (oldName, v.getName ());

        // Now pick a new name that won't conflict
        v.renameVariableAction ("change");
        assertEquals ("change", v.getName ());
        assertEquals (v, plane.get ("change"));
    }

    /**
     * This needs to dismiss the dialog during a unit test
     *
     * @see https://www.eclipse.org/lists/swtbot-dev/msg00817.html
     */
    @Test
    public void testShowDerivationAction ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint parent = new NamedPoint (item, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedVariable v = new NamedVariable (parent, Color.red, "test1");
        final NamedVariable x = parent.getX ();
        final NamedVariable y = parent.getY ();
        v.setGivenStatus (GeoStatus.known);
        y.setGivenStatus (GeoStatus.fixed);
        x.setDoubleValue (28.0);
        y.setDoubleValue (17.0);
        v.setDoubleValue (11.0);
        x.setFormula ("test", "%s == %s + %s", x, y, v);
        final StringBuilder builder = new StringBuilder ();
        x.getDerivation (builder, 3);
    }

    @Test
    public void testSetValueAction ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint parent = new NamedPoint (item, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedVariable x = parent.getX ();
        x.setValueAction (45.5);
        assertEquals (GeoStatus.fixed, x.getStatus ());
        final NamedVariable test = new NamedVariable (parent, Color.green, "testing");
        test.setValueAction (17.0);
        parent.getX ().setValueAction (22.6);
        final NamedVariable child = new NamedVariable (test, Color.green, "testing");
        child.setValueAction (17.0);
    }

    @Test
    public void testXmlAttributes () throws ParserConfigurationException
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint parent = new NamedPoint (item, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedVariable x = parent.getX ();

        final XMLUtil xu = new XMLUtil ();
        final DocumentBuilder builder = xu.getDocumentBuilder ();
        final Document doc = builder.newDocument ();
        final Element element = doc.createElement ("test");
        assertNotNull (element);
        x.getAttributes (element);
        assertEquals (x.getName (), xu.get (element, "name", "missing"));
        assertEquals ("false", xu.get (element, "open", "missing"));

        final Element root = doc.createElement ("root");
        final String name = GeoItem.class.getSimpleName ();
        assertNull (xu.getNthChild (root, name, 0));
        x.getElement (root);
        x.setFormula ("test", "%s == 10", x);
        x.setLocation (null);
        x.setDoubleValue (null);
        x.getElement (root);
    }
}
