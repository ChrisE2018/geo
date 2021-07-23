
package com.chriseliot.geo;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.SwingConstants;

import org.junit.jupiter.api.Test;

import com.chriseliot.util.*;

public class TestGeoItem
{
    @Test
    public void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        assertNotNull (item.toString ());
        final GeoItem child = new GeoItem (item, "tt", Color.blue);
        assertNotNull (child.toString ());
    }

    @Test
    public void testGetters ()
    {
        Namer.reset ();
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        assertEquals (plane, item.getPlane ());
        assertEquals (item.getChildren ().size (), 0);
        assertFalse (item.hasChildren ());
        final GeoItem child = new GeoItem (item, "tt", Color.blue);

        assertEquals (plane, item.getPlane ());
        assertEquals ("t001", item.getName ());
        assertNull (item.getParent ());
        assertEquals (item, child.getParent ());
        final List<GeoItem> children = item.getChildren ();
        assertEquals (1, children.size ());
        assertTrue (item.hasChildren ());
        assertTrue (children.contains (child));
        assertEquals (Color.black, item.getColor ());
        assertEquals (Color.blue, child.getColor ());
        assertFalse (item.isSelected ());
        assertFalse (child.isSelected ());
        assertFalse (item.isOpen ());
        assertFalse (child.isOpen ());
        assertEquals (GeoStatus.unknown, item.getStatus ());
        assertEquals (GeoStatus.unknown, child.getStatus ());
    }

    @Test
    public void testDefaultMethods ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);

        item.recalculate ();
        item.findVertices (item);
        item.move (10, 20);
        item.remove ();
        assertNull (item.getVertices ());
    }

    @Test
    public void testSimple ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        item.setName ("tt");
        assertEquals ("tt", item.getName ());
        assertEquals (Color.black, item.getColor ());
        item.setColor (Color.blue);
        assertEquals (Color.blue, item.getColor ());
        assertFalse (item.isSelected ());
        item.setSelected (true);
        assertTrue (item.isSelected ());
        item.setSelected (false);
        assertFalse (item.isSelected ());
        assertFalse (item.isOpen ());
        item.setOpen (true);
        assertTrue (item.isOpen ());
        item.setOpen (false);
        assertFalse (item.isOpen ());
        assertEquals (GeoStatus.unknown, item.getStatus ());
        item.setStatus (GeoStatus.derived, "test");
        assertEquals (GeoStatus.derived, item.getStatus ());
        item.setStatus (GeoStatus.unknown, "test");
        assertEquals (GeoStatus.unknown, item.getStatus ());
        item.setStatus (GeoStatus.unknown, "test");
        assertEquals (GeoStatus.unknown, item.getStatus ());

        assertEquals (1, item.getCategories ().size ());
        final Set<String> filter = new HashSet<> ();
        assertFalse (item.among (filter));
        filter.add ("test");
        assertFalse (item.among (filter));
        item.addCategory ("test");
        assertTrue (item.among (filter));
        assertEquals (2, item.getCategories ().size ());

        // Should do better testing of snap points
        assertTrue (item.getSnapPoints ().isEmpty ());
        // Should do better testing of drag points
        assertTrue (item.getDragPoints ().isEmpty ());
    }

    @Test
    public void testPaint ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final BufferedImage image = new BufferedImage (500, 500, BufferedImage.TYPE_INT_RGB);
        final Graphics g = image.getGraphics ();
        final Labels labels = new Labels ();
        item.paint (g, labels);
    }

    @Test
    public void testSnapPoints ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (parent, true, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        final List<Point2D.Double> snaps = parent.getSnapPoints ();
        assertEquals (1, snaps.size ());
        assertEquals (new Point2D.Double (10, 20), snaps.get (0));
        assertEquals (parent, child.getParent ());

        final GeoItem child2 = new GeoItem (parent, "t2", Color.black);
        final List<Point2D.Double> snaps2 = parent.getSnapPoints ();
        assertEquals (1, snaps2.size ());
        assertEquals (new Point2D.Double (10, 20), snaps2.get (0));
        assertEquals (parent, child2.getParent ());
    }

    @Test
    public void testDragPoints ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (parent, true, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        final GeoItem cousin = new GeoItem (parent, "t2", Color.black);
        final List<Point2D.Double> points = parent.getDragPoints ();
        assertEquals (1, points.size ());
        assertTrue (points.contains (new Point2D.Double (10, 20)));
        assertEquals (parent, child.getParent ());
        assertEquals (parent, cousin.getParent ());
    }

    @Test
    public void testDragPoints2 ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (parent, true, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        final List<Point2D.Double> points = parent.getDragPoints ();
        assertEquals (1, points.size ());
        assertEquals (new Point2D.Double (10, 20), points.get (0));
        assertEquals (parent, child.getParent ());

        final NamedPoint child2 = new NamedPoint (parent, true, Color.green, "test", 30, 40, SwingConstants.NORTH_WEST);
        final List<Point2D.Double> points2 = parent.getDragPoints ();
        assertEquals (2, points2.size ());
        assertTrue (points2.contains (new Point2D.Double (10, 20)));
        assertTrue (points2.contains (new Point2D.Double (30, 40)));
        assertEquals (parent, child2.getParent ());

        // Third child is not draggable, so not included in drag points.
        final NamedPoint child3 = new NamedPoint (parent, false, Color.green, "test", 30, 40, SwingConstants.NORTH_WEST);
        final List<Point2D.Double> points3 = parent.getDragPoints ();
        assertEquals (2, points3.size ());
        assertTrue (points3.contains (new Point2D.Double (10, 20)));
        assertTrue (points3.contains (new Point2D.Double (30, 40)));
        assertEquals (parent, child3.getParent ());
    }

    @Test
    public void testSolve ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (parent, true, Color.green, "test", 10, 20, SwingConstants.NORTH_WEST);
        assertEquals (parent.getName (), child.getAttributes ().get ("parent"));
        parent.setStatus (GeoStatus.fixed, "test");
        assertEquals ("test", parent.getReason ());
        child.setGivenStatus (GeoStatus.known);
        assertTrue (child.isDetermined ());
        assertNotNull (parent.toString ());
    }

    @Test
    public void testPopupSupport ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        assertTrue (item.canSetKnown ());
        assertTrue (item.canSetUnknown ());
        assertFalse (item.canSetFixed ());
        assertFalse (item.canSetValue ());
        assertFalse (item.canShowDerivation ());
        assertFalse (item.canRenameVariable ());
        assertFalse (item.canShowSolution ());
        assertEquals (GeoStatus.unknown, item.getStatus ());
        item.setKnownAction ();
        assertEquals (GeoStatus.known, item.getStatus ());
        item.setFixedAction ();
        assertEquals (GeoStatus.fixed, item.getStatus ());
        item.setUnknownAction ();
        assertEquals (GeoStatus.unknown, item.getStatus ());
        item.setValueAction ();
        assertEquals (GeoStatus.fixed, item.getStatus ());
        item.showDerivationAction ();
        item.renameVariableAction ();
        item.showSolutionAction ();
    }

    @Test
    public void testAttributes ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final Map<String, Object> attributes = item.getAttributes ();
        assertEquals (item.getName (), attributes.get ("name"));
        assertNull (attributes.get ("parent"));
        assertEquals (Boolean.FALSE, attributes.get ("selected"));
        assertEquals (Boolean.FALSE, attributes.get ("open"));
        assertEquals (GeoStatus.unknown, attributes.get ("status"));
        assertNull (attributes.get ("reason"));
        final Map<String, String> attributes2 = new HashMap<> ();
        for (final Entry<String, Object> entry : attributes.entrySet ())
        {
            final String key = entry.getKey ();
            final Object value = entry.getValue ();
            if (value == null)
            {
                attributes2.put (key, null);
            }
            else
            {
                attributes2.put (key, value.toString ());
            }
        }
        item.readAttributes (attributes2);
    }
}
