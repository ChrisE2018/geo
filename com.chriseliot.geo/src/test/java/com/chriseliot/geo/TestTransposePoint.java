
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.Point2D;

import javax.swing.SwingConstants;

import org.junit.jupiter.api.Test;

public class TestTransposePoint
{
    @Test
    public void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final TransposePoint test = new TransposePoint (parent, true, Color.blue, "tp", 0, 0, SwingConstants.NORTH_WEST);
        assertNotNull (test.toString ());
    }

    @Test
    public void testDrag ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final TransposePoint test = new TransposePoint (parent, true, Color.blue, "tp", 0, 0, SwingConstants.NORTH_WEST);
        test.drag (new Point2D.Double (10, 20));
        assertEquals (new Point (10, 20), test.getIntPosition ());
        assertEquals (10, test.getIntPosition ().x);
        assertEquals (20, test.getIntPosition ().y);
    }

    @Test
    public void testDragParent ()
    {
        final GeoPlane plane = new GeoPlane ();

        final Point2D.Double from = new Point2D.Double (0, 0);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoLine parent = new GeoLine (plane, Color.red, from, to);

        final TransposePoint test = new TransposePoint (parent, true, Color.blue, "tp", 0, 0, SwingConstants.NORTH_WEST);
        test.drag (new Point2D.Double (10, 20));
        assertEquals (new Point (10, 20), test.getIntPosition ());
        assertEquals (10, test.getIntPosition ().x);
        assertEquals (20, test.getIntPosition ().y);

        assertEquals (new Point (10, 20), parent.getFrom ().getIntPosition ());
        assertEquals (10, parent.getFrom ().getIntPosition ().x);
        assertEquals (20, parent.getFrom ().getIntPosition ().y);

        assertEquals (new Point (40, 60), parent.getTo ().getIntPosition ());
        assertEquals (40, parent.getTo ().getIntPosition ().x);
        assertEquals (60, parent.getTo ().getIntPosition ().y);
    }
}
