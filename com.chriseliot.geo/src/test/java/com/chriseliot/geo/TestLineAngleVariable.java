
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.function.Consumer;

import org.junit.jupiter.api.Test;

import com.chriseliot.geo.gui.CloseDialogThread;

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

    @Test
    public void testPopup ()
    {
        final GeoPlane plane = new GeoPlane ();
        final Point2D.Double from = new Point2D.Double (0, 0);
        final Point2D.Double to = new Point2D.Double (100, 100);
        final GeoLine grandParent = new GeoLine (plane, Color.black, from, to);
        final LineAngleVariable test = grandParent.getAngle ();

        final Map<String, Consumer<GeoItem>> result = new HashMap<> ();
        test.popup (result);
        assertEquals (4, result.size ());
        assertTrue (result.containsKey ("known"));
        assertTrue (result.containsKey ("unknown"));
        assertTrue (result.containsKey ("Set Value"));
        assertTrue (result.containsKey ("Rename Variable"));

        final CloseDialogThread thread = new CloseDialogThread ();
        thread.start ();
        result.get ("Set Value").accept (test);
        thread.halt ();
        TestSupport.dream (10);
        System.out.printf ("Set Value dialog returns\n");
        assertTrue (thread.isDialogSeen ());
    }
}
