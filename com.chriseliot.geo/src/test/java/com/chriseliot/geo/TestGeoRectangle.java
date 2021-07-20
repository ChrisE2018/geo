
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

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
}
