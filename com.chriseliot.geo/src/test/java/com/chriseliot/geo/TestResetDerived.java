
package com.chriseliot.geo;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.*;

import com.chriseliot.util.Namer;

class TestResetDerived
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    private GeoPlane plane;
    private GeoLine line1;
    private GeoLine line2;
    private GeoLine line3;

    // Vertices should be named for the opposite side, but here they are not
    private GeoVertex v1;
    private GeoVertex v2;
    private GeoVertex v3;
    private GeoTriangle t;

    private NamedPoint p1;
    private NamedPoint p2;
    private NamedPoint p3;

    @BeforeEach
    public void init ()
    {
        logger.info ("Setup test scenario");
        Namer.reset ();
        plane = new GeoPlane ();
        line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));

        // Vertices should be named for the opposite side, but here they are not
        v1 = line1.getVertex (line2);
        v2 = line2.getVertex (line3);
        v3 = line3.getVertex (line1);
        plane.findTriangles ();
        t = plane.getTriangle (v1, v2, v3);
        assertNotNull (t);
        t.addStatusChangeListener (new TraceStatusChangeListener ());
        assertFalse (line1.isDetermined ());
        assertFalse (line2.isDetermined ());
        assertFalse (line3.isDetermined ());
        line1.addStatusChangeListener (new TraceStatusChangeListener ());
        assertFalse (t.isDetermined ());
        p1 = v1.getVertex ();
        p2 = v2.getVertex ();
        p3 = v3.getVertex ();

        // Make sure points are as expected
        assertEquals (new Point2D.Double (10, 20), p1.getPosition ());
        assertEquals (new Point2D.Double (50, 55), p2.getPosition ());
        assertEquals (new Point2D.Double (30, 40), p3.getPosition ());

        // Two known vertices are enough to solve the triangle
        // But not if it is just their positions
        assertFalse (line1.isDetermined ());
        assertFalse (line2.isDetermined ());
        assertFalse (line3.isDetermined ());
    }

    @AfterEach
    public void teardown ()
    {
        logger.info ("Teardown test scenario");
        plane.clear ();

        plane = null;
        line1 = null;
        line2 = null;
        line3 = null;

        // Vertices should be named for the opposite side, but here they are not
        v1 = null;
        v2 = null;
        v3 = null;
        t = null;
        p1 = null;
        p2 = null;
        p3 = null;
    }

    @Tag ("Triangle")
    @Test
    public void testResetDerived1 ()
    {
        logger.info ("Changing %s.status to known", p1.getName ());
        p1.setGivenStatus (GeoStatus.known);
        logger.info ("Changed %s.status to known", p1.getName ());
        assertFalse (line1.isDetermined ());
        assertFalse (line2.isDetermined ());
        assertFalse (line3.isDetermined ());
        assertTrue (p1.isDetermined ());
        assertEquals (line2.getFrom ().getPosition (), p1.getPosition ());
        assertTrue (line2.getFrom ().isDetermined ());

        assertFalse (t.isDetermined ());

        p2.setGivenStatus (GeoStatus.known);
        assertTrue (p2.isDetermined ());
        assertEquals (line2.getTo ().getPosition (), p2.getPosition ());
        assertTrue (line2.getTo ().isDetermined ());

        assertFalse (t.isDetermined ());

        assertEquals (GeoStatus.known, p1.getStatus ());
        assertEquals (GeoStatus.known, p2.getStatus ());
        assertEquals (GeoStatus.unknown, p3.getStatus ());
        assertEquals (GeoStatus.unknown, t.getStatus ());
        assertEquals (GeoStatus.unknown, line1.getStatus ());
        line2.solve ();
        assertTrue (line2.isDetermined ());
        assertEquals (GeoStatus.derived, line2.getStatus ());
        assertEquals (GeoStatus.unknown, line3.getStatus ());
        assertEquals (GeoStatus.unknown, t.getStatus ());

        // Make triangle be known or derived
        p3.setGivenStatus (GeoStatus.known);

        assertEquals (GeoStatus.known, p1.getStatus ());
        assertEquals (GeoStatus.known, p2.getStatus ());
        assertEquals (GeoStatus.known, p3.getStatus ());
        assertEquals (GeoStatus.derived, line1.getStatus ());
        assertEquals (GeoStatus.derived, line2.getStatus ());
        assertEquals (GeoStatus.derived, line3.getStatus ());
        assertEquals (GeoStatus.derived, t.getStatus ());

        // Make everything be known or derived
        v1.setGivenStatus (GeoStatus.known);
        v2.setGivenStatus (GeoStatus.known);
        v3.setGivenStatus (GeoStatus.known);

        for (final GeoItem item : plane.getItems ())
        {
            if (item == p1 || item == p2 || item == p3 || item == v1 || item == v2 || item == v3)
            {
                assertEquals (GeoStatus.known, item.getStatus ());
            }
        }
    }

    @Tag ("Triangle")
    @Test
    public void testResetDerived2 ()
    {
        logger.info ("Changing %s.status to known", p1.getName ());
        p1.setGivenStatus (GeoStatus.known);
        logger.info ("Changed %s.status to known", p1.getName ());

        p2.setGivenStatus (GeoStatus.known);

        line2.solve ();

        // Make triangle be known or derived
        p3.setGivenStatus (GeoStatus.known);

        // Make everything be known or derived
        v1.setGivenStatus (GeoStatus.known);
        v2.setGivenStatus (GeoStatus.known);
        v3.setGivenStatus (GeoStatus.known);
        // Part1 ends here
        for (final GeoItem item : plane.getItems ())
        {
            if (!(item == p1 || item == p2 || item == p3 || item == v1 || item == v2 || item == v3))
            {
                assertTrue (item.whyDetermined ());
                // assertEquals (GeoStatus.derived, item.getStatus ());
            }
        }

        // Retract assertions
        v1.setStatusUnknown ();
        v2.setStatusUnknown ();
        v3.setStatusUnknown ();
        p1.setStatusUnknown ();
        p2.setStatusUnknown ();
        plane.resetDerived ();

        assertEquals (GeoStatus.unknown, p1.getStatus ());
        assertEquals (GeoStatus.unknown, p2.getStatus ());
        assertEquals (GeoStatus.known, p3.getStatus ());
        assertEquals (GeoStatus.unknown, line1.getStatus ());
        assertEquals (GeoStatus.unknown, line2.getStatus ());
        assertEquals (GeoStatus.unknown, line3.getStatus ());
        assertEquals (GeoStatus.unknown, t.getStatus ());
    }
}
