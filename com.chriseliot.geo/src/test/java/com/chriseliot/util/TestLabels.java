
package com.chriseliot.util;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.SwingConstants;

import org.junit.jupiter.api.Test;

public class TestLabels
{
    @Test
    public void testCreate ()
    {
        final Labels test = new Labels ();
        assertNotNull (test.toString ());
    }

    @Test
    public void testAdd1 ()
    {
        final Labels test = new Labels ();
        test.add (Color.red, new Point (10, 20), 43, "foobar");
        test.add (this, new Point (10, 20), 43, "foobar");
        test.add (this, Color.green, new Point (10, 20), SwingConstants.SOUTH_WEST, "foobar");
        test.add (this, Color.green, new Point (10, 20), SwingConstants.SOUTH_WEST, "foobar", "tip");
    }

    @Test
    public void testAdd2 ()
    {
        final Color color = Color.blue;
        final Point position = new Point (17, 93);
        final int anchor = 43;
        final String text = "foobar";
        final LabelItem item = new LabelItem (this, color, position, anchor, text);
        final Labels test = new Labels ();
        test.add (item);
    }

    @Test
    public void testPaint ()
    {
        final Labels test = new Labels ();
        test.add (Color.red, new Point (10, 20), SwingConstants.NORTH_WEST, "foobar");
        test.add (Color.red, new Point (10, 20), SwingConstants.NORTH_EAST, "foobar");
        test.add (this, new Point (10, 20), SwingConstants.SOUTH_WEST, "foobar");
        test.add (this, new Point (10, 20), SwingConstants.SOUTH_EAST, "foobar");
        test.add (this, new Point (10, 20), SwingConstants.CENTER, "foobar");
        test.add (this, new Point (10, 20), 999, "foobar");

        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        test.paint (g);

        assertTrue (test.blocked (new Point (0, 0), 10, 20));
        assertFalse (test.blocked (new Point (1000, 0), 10, 20));
    }

    @Test
    public void testFind ()
    {
        final Labels test = new Labels ();
        test.add (Color.red, new Point (10, 20), SwingConstants.SOUTH_WEST, "foobar");

        test.find (new Point (0, 0));
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        test.paint (g);
        test.find (new Point (0, 0));
        test.find (new Point (12, 18));
        test.find (new Point (12, 22));
    }
}
