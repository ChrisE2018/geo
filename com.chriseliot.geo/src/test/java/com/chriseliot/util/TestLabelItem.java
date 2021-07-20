
package com.chriseliot.util;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;

import org.junit.jupiter.api.Test;

public class TestLabelItem
{
    @Test
    public void testCreate ()
    {
        final Color color = Color.blue;
        final Point position = new Point (17, 93);
        final int anchor = 43;
        final String text = "foobar";
        final LabelItem test = new LabelItem (this, color, position, anchor, text);
        assertNotNull (test.toString ());
    }

    @Test
    public void testGetters ()
    {
        final Color color = Color.blue;
        final Point position = new Point (17, 93);
        final int anchor = 43;
        final String text = "foobar";
        final LabelItem test = new LabelItem (this, color, position, anchor, text);
        assertEquals (this, test.getSource ());
        assertEquals (Color.blue, test.getColor ());
        assertEquals (new Point (17, 93), test.getPosition ());
        assertEquals (43, test.getAnchor ());
        assertEquals ("foobar", test.getText ());
    }

    @Test
    public void testToString ()
    {
        final Color color = Color.blue;
        final Point position = new Point (17, 93);
        final int anchor = 43;
        final String text = "foobar";
        final LabelItem test = new LabelItem (this, color, position, anchor, text, "tip");
        assertNotNull (test.toString ());
        final Rectangle bounds = new Rectangle (10, 20, 30, 40);
        test.setBounds (bounds);
        assertNotNull (test.toString ());
        assertEquals ("tip", test.getTooltip ());
    }

    @Test
    public void testBounds ()
    {
        final Color color = Color.blue;
        final Point position = new Point (17, 93);
        final int anchor = 43;
        final String text = "foobar";
        final LabelItem test = new LabelItem (this, color, position, anchor, text);
        assertNull (test.getBounds ());
        final Rectangle bounds = new Rectangle (10, 20, 30, 40);
        assertFalse (test.intersects (bounds));
        test.setBounds (bounds);
        assertNotNull (test.getBounds ());
        test.setBounds (1, 2, 3, 4);
        assertNotNull (test.getBounds ());
        assertEquals (new Rectangle (1, 2, 3, 4), test.getBounds ());
        assertFalse (test.intersects (bounds));
        test.setBounds (bounds);
        assertTrue (test.intersects (bounds));
    }
}
