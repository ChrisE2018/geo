
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import javax.swing.SwingConstants;

import org.junit.jupiter.api.Test;

import com.chriseliot.util.Labels;

class TestCentroidVariable
{
    @Test
    void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final Color color = Color.red;
        final String name = "test1";
        assertNotNull (new CentroidVariable (parent, color, name, 43.0).toString ());
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
        final CentroidVariable v1 = new CentroidVariable (parent, Color.red, "test1", 22.0);
        final CentroidVariable v2 = new CentroidVariable (parent, Color.red, "test1", 43.0);
        assertEquals (parent, v1.getLocation ());
        assertEquals (parent, v2.getLocation ());
        v1.setLocation (parent);
        v1.setLocation (parent, p2);
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
        final CentroidVariable v = new CentroidVariable (parent, Color.red, "test1", 11.0);
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Labels labels = new Labels ();
        v.paint (g, labels);
        v.setLocation (parent, p2);
        v.paint (g, labels);
        v.setFormula ("test", "2 == 1 + 1", v);
        final Inference inference = v.getInference ();
        assertNotNull (inference);
        assertNotNull (inference.getInstantiation ());
        v.paint (g, labels);
        v.setDoubleValue (null);
        v.paint (g, labels);

        // Set location null to get other branch
        v.setLocation ();
        v.paint (g, labels);
    }
}
