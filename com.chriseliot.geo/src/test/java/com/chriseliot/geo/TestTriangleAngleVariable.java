
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.junit.jupiter.api.Test;

class TestTriangleAngleVariable
{
    @Test
    void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final TriangleAngleVariable test = new TriangleAngleVariable (item, Color.black, "a");
        assertNotNull (test.toString ());
    }

    @Test
    void testSimple ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final TriangleAngleVariable test = new TriangleAngleVariable (item, Color.black, "a");
        assertTrue (test.canSetValue ());
    }

    @Test
    void testSetValueAction ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (10, 20));
        final GeoVertex v2 = plane.getVertex (new Point2D.Double (50, 55));
        final GeoVertex v3 = plane.getVertex (new Point2D.Double (30, 40));

        final GeoTriangle t = plane.getTriangle (v1, v2, v3);
        final TriangleAngleVariable test = t.getAngle1 ();
        final CloseDialogThread thread = new CloseDialogThread ();
        thread.start ();
        test.setValueAction ();
        thread.halt ();
        thread.dream (10);
        assertTrue (thread.isDialogSeen ());
        assertFalse (thread.isRunning ());
    }

    @Test
    void testSetValueResult ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));
        assertNotNull (line1);
        assertNotNull (line2);
        assertNotNull (line3);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (10, 20));
        final GeoVertex v2 = plane.getVertex (new Point2D.Double (50, 55));
        final GeoVertex v3 = plane.getVertex (new Point2D.Double (30, 40));

        final GeoTriangle t = plane.getTriangle (v1, v2, v3);
        final TriangleAngleVariable test = t.getAngle1 ();
        // test.setValueAction (45);
        assertNotNull (test);
    }
}
