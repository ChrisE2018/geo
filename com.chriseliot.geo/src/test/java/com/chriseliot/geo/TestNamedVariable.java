
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.Map.Entry;

import javax.swing.SwingConstants;

import org.junit.jupiter.api.Test;

import com.chriseliot.util.Labels;

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
        assertEquals (0, test.getTerms ().length);
    }

    @Test
    public void testLocation ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint parent = new NamedPoint (item, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedPoint p2 =
            new NamedPoint (item, false, Color.green, "test", new Point2D.Double (20, 40), SwingConstants.NORTH_WEST);
        final NamedVariable v1 = new NamedVariable (parent, Color.red, "test1");
        final NamedVariable v2 = new NamedVariable (parent, Color.red, "test1", 43.0);
        assertEquals (parent, v1.getLocation ());
        assertEquals (parent, v2.getLocation ());
        v1.setLocation (parent);
        assertNull (v1.getLocation2 ());
        v1.setLocation (parent, p2);
        assertNotNull (v1.getLocation2 ());
        assertEquals (p2, v1.getLocation2 ());
    }

    @Test
    public void testFormula ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final Color color = Color.red;
        final String name = "test1";
        final NamedVariable test = new NamedVariable (parent, color, name, 43.0);
        test.setFormula ("test", "1 + 1 = 2");
        assertEquals ("1 + 1 = 2", test.getFormula ());
        assertEquals ("test", test.getReason ());
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
        assertEquals ("test", test.getReason ());
        assertEquals ("c==43.0", test.getDerivedFormula ());
        plane.solve ();
        x.setDefaultFormula ();
        y.setFormula ("test", "y == 55");
        test.setFormula ("test", "test1 == x + y", x, y);
        assertEquals ("y==55", test.getDerivedFormula ());
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
        test.getX ().setFormula ("1 + 1 = 2", "test");
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
        final NamedPoint p2 =
            new NamedPoint (item, false, Color.green, "test", new Point2D.Double (20, 40), SwingConstants.NORTH_WEST);
        final NamedVariable v = new NamedVariable (parent, Color.red, "test1");
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Labels labels = new Labels ();
        v.paint (g, labels);
        v.setLocation (parent, p2);
        v.paint (g, labels);
        v.setFormula ("test", "2 == 1 + 1");
        assertNotNull (v.getFormula ());
        v.paint (g, labels);
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
        assertFalse (item.canShowDerivation ());
        assertFalse (parent.canShowDerivation ());
        assertEquals (v.getStatus () == GeoStatus.derived, v.canShowDerivation ());
        assertEquals (v == parent.getX () || v == parent.getY (), v.canSetValue ());
    }

    @Test
    public void testSetValue ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final NamedVariable v = new NamedVariable (item, Color.red, "test1");
        assertFalse (v.canSetValue ());
        final NamedPoint parent =
            new NamedPoint (item, false, Color.green, "test", new Point2D.Double (10, 10), SwingConstants.NORTH_WEST);
        final NamedVariable v2 = new NamedVariable (parent, Color.red, "test1");
        assertFalse (v2.canSetValue ());
        assertTrue (parent.getX ().canSetValue ());
        assertTrue (parent.getY ().canSetValue ());
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
        final CloseDialogThread thread = new CloseDialogThread ();
        thread.start ();
        v.showDerivationAction ();
        thread.halt ();
        thread.dream (10);
        System.out.printf ("Derivation dialog returns\n");
        assertTrue (thread.isDialogSeen ());
    }

    /**
     * This needs to dismiss the dialog during a unit test
     *
     * @see https://www.eclipse.org/lists/swtbot-dev/msg00817.html
     */
    @Test
    public void testSetValueAction ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint parent = new NamedPoint (item, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedVariable v = new NamedVariable (parent, Color.red, "test1");
        final CloseDialogThread thread = new CloseDialogThread ();
        thread.start ();
        v.setValueAction ();
        v.setValueAction (45);
        thread.halt ();
        thread.dream (10);
        System.out.printf ("Derivation dialog returns\n");
        assertTrue (thread.isDialogSeen ());
    }

    @Test
    public void testAttributes ()
    {

        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double position = new Point2D.Double (10, 20);
        final NamedPoint parent = new NamedPoint (item, false, Color.green, "test", position, SwingConstants.NORTH_WEST);
        final NamedVariable test = new NamedVariable (parent, Color.red, "test1");
        test.setDoubleValue (9.5);
        final Map<String, Object> attributes = test.getAttributes ();
        assertEquals (9.5, attributes.get ("value"));
        final Map<String, String> attributes2 = new HashMap<> ();
        for (final Entry<String, Object> entry : attributes.entrySet ())
        {
            final String key = entry.getKey ();
            final Object value = entry.getValue ();
            if (value == null)
            {
                attributes2.put (key, (String)value);
            }
            else
            {
                attributes2.put (key, value.toString ());
            }
        }
        test.readAttributes (attributes2);
        test.setLocation (parent, parent);
        assertNotNull (test.getAttributes ());
        test.setLocation (null);
        assertNotNull (test.getAttributes ());
    }
}
