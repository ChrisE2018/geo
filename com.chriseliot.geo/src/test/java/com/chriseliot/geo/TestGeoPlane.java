
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;
import java.util.List;

import javax.swing.SwingConstants;
import javax.swing.event.*;

import org.junit.jupiter.api.Test;

import com.chriseliot.util.Namer;

public class TestGeoPlane
{
    private final TestSupport ts = new TestSupport ();

    @Test
    public void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        assertNotNull (plane.toString ());
    }

    @Test
    public void testGetItems ()
    {
        final GeoPlane plane = new GeoPlane ();
        assertTrue (plane.getVertices ().isEmpty ());
        assertTrue (plane.getBindings ().isEmpty ());
        assertEquals (0, plane.getItems ().size ());
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        assertEquals (1, plane.getItems ().size ());
        plane.clear ();
        assertEquals (0, plane.getItems ().size ());
        final List<GeoItem> items = new ArrayList<> ();
        items.add (item);
        plane.addAll (items);
        assertEquals (1, plane.getItems ().size ());
        assertTrue (plane.contains (item));
        assertEquals (item, plane.get (item.getName ()));
        item.remove ();
        assertEquals (0, plane.getItems ().size ());
        assertNull (plane.get (item.getName ()));
        assertNotNull (plane.getLabels ());
    }

    @Test
    public void testSelect ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        assertTrue (plane.contains (item));
        item.setSelected (true);
        plane.deselectAll ();
        assertFalse (item.isSelected ());

        final NamedPoint child = new NamedPoint (item, false, Color.green, "test", 0, 0, SwingConstants.NORTH_WEST);

        final List<NamedPoint> dragged = new ArrayList<> ();
        dragged.add (child);
        assertFalse (item.isOpen ());
        plane.selectAll (dragged);
        assertTrue (item.isSelected ());
        assertTrue (item.isOpen ());
        plane.deselectAll ();
        assertFalse (item.isSelected ());
        assertFalse (child.isSelected ());
    }

    @Test
    public void testVertex ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));

        final GeoVertex v1 = line1.getVertex (line2);
        final GeoVertex v2 = line2.getVertex (line3);
        final GeoVertex v3 = line3.getVertex (line1);
        assertEquals (v1, plane.getVertex (v1.getPosition ()));
        assertEquals (v2, plane.getVertex (v2.getPosition ()));
        assertEquals (v3, plane.getVertex (v3.getPosition ()));
        assertNull (plane.getVertex (new Point2D.Double (-99, -99)));

        for (final GeoItem root : plane.getRoots ())
        {
            assertNull (root.getParent ());
        }
        plane.remove (line3);
        assertNull (plane.getBindingKey (line3));
    }

    @Test
    public void testFindTriangles ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));

        final GeoLine line4 = new GeoLine (plane, Color.blue, new Point2D.Double (100, 100), new Point2D.Double (500, 100));
        final GeoLine line5 = new GeoLine (plane, Color.blue, new Point2D.Double (100, 100), new Point2D.Double (600, 200));

        final GeoVertex v1 = line1.getVertex (line2);
        final GeoVertex v2 = line2.getVertex (line3);
        final GeoVertex v3 = line3.getVertex (line1);
        final GeoVertex v4 = line4.getVertex (line5);

        plane.findTriangles ();
        assertTrue (plane.hasTriangle (v1, v2, v3));
        assertFalse (plane.hasTriangle (v4, v2, v3));
        assertFalse (plane.hasTriangle (v1, v4, v3));
        assertFalse (plane.hasTriangle (v1, v2, v4));

        assertFalse (plane.isTriangle (v4, v2, v3));
        assertFalse (plane.isTriangle (v1, v4, v3));
        assertFalse (plane.isTriangle (v1, v2, v4));

        // Obtain full coverage of the getTriangle method.
        assertNotNull (plane.getTriangle (v1, v2, v3));
        assertNull (plane.getTriangle (v4, v2, v3));
        assertNull (plane.getTriangle (v1, v4, v3));
        assertNull (plane.getTriangle (v1, v2, v4));

        // Test the isTriangle method.
        assertTrue (plane.isTriangle (v1, v2, v3));
        assertFalse (plane.isTriangle (v4, v2, v3));
        assertFalse (plane.isTriangle (v1, v4, v3));
        assertFalse (plane.isTriangle (v1, v2, v4));
    }

    @Test
    public void testDistance ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (item, false, Color.green, "test", 0, 0, SwingConstants.NORTH_WEST);
        assertEquals (0, plane.distance2 (new Point (0, 0), child));
        assertEquals (200, plane.distance2 (new Point (10, 10), child));

        assertEquals (0, plane.distance2 (new Point (0, 0), new Point (0, 0)));
        assertEquals (200, plane.distance2 (new Point (10, 10), new Point (0, 0)));
        assertEquals (0, plane.distance2 (new Point (0, 0), new Point2D.Double (0, 0)));
        assertEquals (200, plane.distance2 (new Point (10, 10), new Point2D.Double (0, 0)));

        assertEquals (0, plane.distance2 (new Point (0, 0), new Point2D.Double (0, 0)));
        assertEquals (200, plane.distance2 (new Point (0, 0), new Point2D.Double (10, 10)));

        assertEquals (0, plane.distance2 (new Point2D.Double (0, 0), new Point2D.Double (0, 0)));
        assertEquals (200, plane.distance2 (new Point2D.Double (0, 0), new Point2D.Double (10, 10)));
    }

    @Test
    public void testGetSnapPoint1 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (item, false, Color.green, "test", 0, 0, SwingConstants.NORTH_WEST);
        assertEquals (new Point2D.Double (0, 0), plane.getSnapPoint (new Point (5, 5), 1000));
        assertNull (plane.getSnapPoint (new Point (50, 50), 10));
        assertTrue (plane.contains (child));
    }

    @Test
    public void testGetSnapPoint2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (item, false, Color.green, "test", 0, 0, SwingConstants.NORTH_WEST);
        assertEquals (new Point2D.Double (0, 0), plane.getSnapPoint (new Point2D.Double (5, 5), 1000));
        assertNull (plane.getSnapPoint (new Point2D.Double (50, 50), 10));
        assertTrue (plane.contains (child));
    }

    @Test
    public void testGetDragPoint ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final NamedPoint child1 = new NamedPoint (item, false, Color.green, "c1", 1, 1, SwingConstants.NORTH_WEST);
        final NamedPoint child2 = new NamedPoint (item, true, Color.green, "c2", 1, 1, SwingConstants.NORTH_WEST);
        final NamedPoint child3 = new NamedPoint (item, true, Color.green, "c2", 1, 2, SwingConstants.NORTH_WEST);
        final List<NamedPoint> drag = plane.getDragPoints (new Point2D.Double (1, 1));
        assertFalse (drag.contains (child1));
        assertTrue (drag.contains (child2));
        assertEquals (1, drag.size ());
        assertTrue (plane.contains (child3));
    }

    @Test
    public void testGetClickObject1 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double p1 = new Point2D.Double (1, 1);
        final Point2D.Double p2 = new Point2D.Double (5, 5);
        final Point2D.Double p3 = new Point2D.Double (10, 20);
        final NamedPoint child1 = new NamedPoint (item, false, Color.green, "c1", p1, SwingConstants.NORTH_WEST);
        final NamedPoint child2 = new NamedPoint (item, true, Color.green, "c2", p2, SwingConstants.NORTH_WEST);
        final NamedPoint child3 = new NamedPoint (item, true, Color.green, "c2", p3, SwingConstants.NORTH_WEST);

        assertEquals (child1, plane.getClickObject (new Point2D.Double (1, 2), 100));
        assertEquals (child2, plane.getClickObject (new Point2D.Double (6, 6), 100));
        assertEquals (child3, plane.getClickObject (new Point2D.Double (9, 15), 100));
        assertEquals (child3, plane.getClickObject (new Point2D.Double (15, 25), 100));
        assertNull (plane.getClickObject (new Point2D.Double (15, 25), 10));
    }

    @Test
    public void testGetClickObject2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Point2D.Double p1 = new Point2D.Double (10, 0);
        final Point2D.Double p2 = new Point2D.Double (20, 0);
        final Point2D.Double p3 = new Point2D.Double (30, 0);
        final NamedPoint child1 = new NamedPoint (item, false, Color.green, "c1", p1, SwingConstants.NORTH_WEST);
        final NamedPoint child2 = new NamedPoint (item, true, Color.green, "c2", p2, SwingConstants.NORTH_WEST);
        final NamedPoint child3 = new NamedPoint (item, true, Color.green, "c2", p3, SwingConstants.NORTH_WEST);

        assertEquals (child1, plane.getClickObject (new Point2D.Double (1, 0), 100));
        assertEquals (child2, plane.getClickObject (new Point2D.Double (22, 0), 100));
        assertEquals (child3, plane.getClickObject (new Point2D.Double (26, 0), 100));
        assertEquals (child3, plane.getClickObject (new Point2D.Double (35, 0), 100));

        final Point2D.Double p4 = new Point2D.Double (40, 0);
        final GeoLine line1 = new GeoLine (plane, Color.red, p4, new Point2D.Double (40, 400));

        assertEquals (child1, plane.getClickObject (new Point2D.Double (1, 0), 100));
        assertEquals (child2, plane.getClickObject (new Point2D.Double (22, 0), 100));
        assertEquals (child3, plane.getClickObject (new Point2D.Double (26, 0), 100));
        assertEquals (line1.getFrom (), plane.getClickObject (new Point2D.Double (39, 0), 100));
    }

    @Test
    public void testGetClickObject3 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));

        final GeoVertex v1 = line1.getVertex (line2);
        assertEquals (v1.getVertex (), plane.getClickObject (v1.getPosition (), 1));
    }

    private boolean fired = false;

    @Test
    public void testListeners ()
    {
        fired = false;
        final GeoPlane plane = new GeoPlane ();
        final ChangeListener listener = new ChangeListener ()
        {
            @Override
            public void stateChanged (ChangeEvent e)
            {
                fired = true;
            }
        };
        plane.addChangeListener (listener);
        assertFalse (fired);
        plane.fireChangeListeners ();
        assertTrue (fired);
        plane.removeChangeListener (listener);
        fired = false;
        plane.fireChangeListeners ();
        assertFalse (fired);
    }

    @Test
    public void testSolve ()
    {
        final GeoPlane plane = new GeoPlane ();
        assertFalse (plane.isDirty ());
        plane.solve ();
        assertFalse (plane.isDirty ());
        plane.setDirty ();
        assertTrue (plane.isDirty ());
        plane.solve ();
        assertFalse (plane.isDirty ());
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        item.setGivenStatus (GeoStatus.known);
        plane.solve ();
    }

    @Test
    public void testResetDerived ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));

        final GeoVertex v1 = line1.getVertex (line2);
        final GeoVertex v2 = line2.getVertex (line3);
        final GeoVertex v3 = line3.getVertex (line1);
        plane.findTriangles ();
        final GeoTriangle t = plane.getTriangle (v1, v2, v3);
        assertNotNull (t);
        final NamedPoint p1 = v1.getVertex ();
        final NamedPoint p2 = v2.getVertex ();
        final NamedPoint p3 = v3.getVertex ();
        assertEquals (GeoStatus.unknown, p1.getStatus ());
        assertEquals (GeoStatus.unknown, p2.getStatus ());
        assertEquals (GeoStatus.unknown, p3.getStatus ());
        assertEquals (GeoStatus.unknown, line1.getStatus ());
        assertEquals (GeoStatus.unknown, line2.getStatus ());
        assertEquals (GeoStatus.unknown, line3.getStatus ());

        p1.setGivenStatus (GeoStatus.known);
        assertEquals (GeoStatus.known, p1.getStatus ());
        assertEquals (GeoStatus.unknown, p2.getStatus ());
        assertEquals (GeoStatus.unknown, p3.getStatus ());
        assertEquals (GeoStatus.unknown, t.getStatus ());

        // Two known vertices are enough to solve the triangle
        // But not if it is just their positions
        p2.setGivenStatus (GeoStatus.known);
        assertEquals (GeoStatus.known, p1.getStatus ());
        assertEquals (GeoStatus.known, p2.getStatus ());
        assertEquals (GeoStatus.unknown, p3.getStatus ());
        assertEquals (GeoStatus.unknown, t.getStatus ());
        assertEquals (GeoStatus.unknown, line1.getStatus ());
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
            else
            {
                assertEquals (GeoStatus.derived, item.getStatus ());
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

    @Test
    public void testPaint1 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final GeoItem child = new GeoItem (item, "t", Color.black);
        assertEquals (item, child.getParent ());
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Set<String> categories = new HashSet<> ();
        plane.paintItems (g, categories);
        categories.add ("simple");
        categories.add ("standard");
        categories.add ("detail");
        plane.paintItems (g, categories);
        assertNotNull (plane.getLabels ());
    }

    @Test
    public void testPaint2 () throws IOException
    {
        Namer.reset ();
        final GeoPlane plane = new GeoPlane ();

        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (50, 50), new Point2D.Double (350, 50));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (350, 50), new Point2D.Double (350, 450));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (350, 450), new Point2D.Double (50, 50));

        final GeoVertex v1 = new GeoVertex (plane, Color.green, line1, line2, new Point2D.Double (350, 50));
        final GeoVertex v2 = new GeoVertex (plane, Color.green, line2, line3, new Point2D.Double (350, 450));
        final GeoVertex v3 = new GeoVertex (plane, Color.green, line3, line1, new Point2D.Double (50, 50));

        final GeoTriangle t = new GeoTriangle (plane, Color.red, v1, v2, v3);
        t.setSelected (false);

        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Set<String> categories = new HashSet<> ();
        categories.add ("simple");
        categories.add ("standard");
        categories.add ("detail");
        plane.paintItems (g, categories);
        // Be sure to call Namer.reset before generating images for compare.
        ts.compare (image, ts.getTestPngFile (this, "master_p1"));
        v1.getVertex ().setGivenStatus (GeoStatus.known);
        v2.getVertex ().setGivenStatus (GeoStatus.known);
        v3.getVertex ().setGivenStatus (GeoStatus.known);
        plane.paintItems (g, categories);
        ts.compare (image, ts.getTestPngFile (this, "master_p2"));

        // Get coverage of missing file case inside TestSupport
        final File temp = ts.getTestDataFile (this, "master_p3", "temp");
        ts.compare (image, temp);
        temp.delete ();
    }
}
