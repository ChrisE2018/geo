
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.junit.jupiter.api.Test;

class TestLineAngleVariable
{
    @Test
    void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final LineAngleVariable test = new LineAngleVariable (parent, Color.black, "tt");
        assertNotNull (test.toString ());
    }

    @Test
    void testCreate2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final LineAngleVariable test = new LineAngleVariable (parent, Color.black, "tt", 5.1);
        assertNotNull (test.toString ());
    }

    @Test
    void testSetValueActionDouble ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Point2D.Double from = new Point2D.Double (0, 0);
        final Point2D.Double to = new Point2D.Double (100, 100);
        final GeoLine grandParent = new GeoLine (plane, Color.black, from, to);
        final LineAngleVariable test = grandParent.getAngle ();
        test.setValueAction (45);
    }
}
