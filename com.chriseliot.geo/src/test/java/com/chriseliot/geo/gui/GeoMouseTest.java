
package com.chriseliot.geo.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.event.MouseEvent;

import org.junit.jupiter.api.Test;

class GeoMouseTest
{
    @Test
    void testCreate ()
    {
        final Geo geo = new Geo ();
        final GeoMouse test = new GeoMouse (geo);
        assertNotNull (test.toString ());
    }

    @Test
    void testGetters ()
    {
        final Geo geo = new Geo ();
        final GeoMouse test = new GeoMouse (geo);
        assertNotNull (test.getCreateColor ());
        test.setCreateColor (Color.blue);
        assertEquals (Color.blue, test.getCreateColor ());
        assertNotNull (test.getPlane ());
        assertNull (test.getMousePoint ());
        assertNull (test.getClickPoint ());
        assertNull (test.getDragPoint ());
        assertNull (test.getHoverItem ());
    }

    @Test
    void mouseClicked1 ()
    {
        final Geo geo = new Geo ();
        final GeoMouse test = new GeoMouse (geo);
        final Component source = geo;
        final int id = 0;
        final long when = 0;
        final int modifiers = 0;
        final int x = 0;
        final int y = 0;
        final int clickCount = 0;
        final boolean popupTrigger = false;
        final MouseEvent e = new MouseEvent (source, id, when, modifiers, x, y, clickCount, popupTrigger);

        test.mouseEntered (e);
        test.mouseExited (e);
        test.mousePressed (e);
        test.mouseReleased (e);
        test.mouseClicked (e);
        test.mouseMoved (e);
        // Need more setup before calling mouseDragged
    }
}
