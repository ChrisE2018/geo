
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.chriseliot.util.Labels;

public class TestGeoRectangle
{
    @Test
    public void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoRectangle test = new GeoRectangle (plane, color, from, to);
        assertNotNull (test.toString ());
    }

    @Test
    public void testGetters ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoRectangle test = new GeoRectangle (plane, color, from, to);
        assertNotNull (test.getTopLeft ());
        assertNotNull (test.getTopRight ());
        assertNotNull (test.getBottomLeft ());
        assertNotNull (test.getBottomRight ());
        assertNotNull (test.getWidth ());
        assertNotNull (test.getHeight ());
    }

    @Test
    public void testPaint ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoRectangle test = new GeoRectangle (plane, color, from, to);
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Labels labels = new Labels ();
        test.setSelected (false);
        test.paint (g, labels);
        test.setSelected (true);
        test.paint (g, labels);
    }

    @Test
    public void popupTest ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoRectangle test = new GeoRectangle (plane, color, from, to);
        final Map<String, Consumer<GeoItem>> menu = new HashMap<> ();
        test.popup (menu);
        assertFalse (menu.isEmpty ());
        assertTrue (menu.containsKey ("Delete"));
        menu.get ("Delete").accept (test);
    }
}
