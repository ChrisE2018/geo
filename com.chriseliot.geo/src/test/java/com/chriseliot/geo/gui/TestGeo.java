
package com.chriseliot.geo.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.image.BufferedImage;

import org.junit.jupiter.api.Test;

class TestGeo
{
    @Test
    void testCreate ()
    {
        final Geo test = new Geo ();
        assertNotNull (test.toString ());
    }

    @Test
    void testGetters ()
    {
        final Geo test = new Geo ();
        assertNotNull (test.getPlane ());
        assertNotNull (test.getSolution ());
        test.getCreateColor ();
    }

    @Test
    void testSetup ()
    {
        final Geo test = new Geo ();
        test.setup ();
    }

    @Test
    void testSimple ()
    {
        final Geo test = new Geo ();
        test.clear ();
        test.mouseClicked (null);
        test.mouseEntered (null);
        test.setCreateColor (Color.blue);
        assertEquals (Color.blue, test.getCreateColor ());
        test.setCreateColor (Color.red);
        assertEquals (Color.red, test.getCreateColor ());
    }

    @Test
    public void testPaint ()
    {
        final Geo test = new Geo ();
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        test.paintComponent (g);
    }
}
