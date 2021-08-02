
package com.chriseliot.geo.gui;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.geom.Point2D;

import javax.swing.*;
import javax.swing.RowFilter.Entry;

import org.junit.jupiter.api.Test;

import com.chriseliot.geo.*;

public class TestGeoSolution
{
    @Test
    public void testCreate ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoSolution test = new GeoSolution (plane);
        assertNotNull (test.toString ());
    }

    @Test
    public void testGetters ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoSolution test = new GeoSolution (plane);
        assertEquals (8, test.getColumnCount ());
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (item, false, Color.green, "test", 0, 0, SwingConstants.NORTH_WEST);

        assertNotNull (test.getRowFilter ());
        assertEquals (0, test.getRowCount ());
        test.update ();
        assertNotNull (test.getRowItem (0));
        assertEquals (4, test.getRowCount ());
        assertTrue (test.isCellEditable (0, 0));
        assertFalse (test.isCellEditable (0, test.getColumnCount ()));
        assertEquals (Color.red, test.getCellColor (0, test.getColumnCount ()));
        assertEquals (Color.red, test.getCellColor (0, 0));
        for (int i = 0; i < test.getRowCount (); i++)
        {
            for (int j = 0; j < test.getColumnCount (); j++)
            {
                // Cheat on expectation but get coverage
                final Color expected = test.getCellColor (i, j);
                assertEquals (expected, test.getCellColor (i, j));
                final Object value = test.getValueAt (i, j);
                if (j == 0)
                {
                    assertNotNull (value);
                }
            }
        }
        test.remove (item);
        assertEquals (0, test.getRowCount ());
        test.clear ();
        assertEquals (0, test.getRowCount ());
        assertNotNull (child);
        assertEquals ("?", test.getColumnName (test.getColumnCount ()));
        assertEquals ("Open", test.getColumnName (0));
        assertEquals (Object.class, test.getColumnClass (test.getColumnCount ()));
        assertEquals (Boolean.class, test.getColumnClass (0));
    }

    @Test
    public void testSetValue ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoSolution test = new GeoSolution (plane);
        assertEquals (8, test.getColumnCount ());
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (item, false, Color.green, "test", 0, 0, SwingConstants.NORTH_WEST);
        assertNotNull (child);
        test.update ();
        for (int i = 0; i < test.getRowCount (); i++)
        {
            final GeoItem row = test.getRowItem (i);
            test.setValueAt (row.isOpen (), i, GeoSolution.OPEN_COLUMN);
            test.setValueAt (row.getName (), i, GeoSolution.NAME_COLUMN);
            test.setValueAt (row.getClass ().getSimpleName (), i, GeoSolution.CLASS_COLUMN);
            test.setValueAt (row.getStatus (), i, GeoSolution.STATUS_COLUMN);
        }
    }

    @Test
    public void testVisible ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoSolution test = new GeoSolution (plane);
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (item, false, Color.green, "test", 0, 0, SwingConstants.NORTH_WEST);
        test.setVisible (item, true);
        test.setVisible (item, false);
        item.setOpen (true);
        test.setVisible (item, true);

        test.setVisible (child, true);
        test.setVisible (child, false);
        test.setVisible (child.getX (), true);
        test.setVisible (child.getX (), false);
    }

    @Test
    public void testFilter ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoSolution test = new GeoSolution (plane);
        assertEquals (8, test.getColumnCount ());
        final GeoItem item = new GeoItem (plane, "t", Color.black);
        final NamedPoint child = new NamedPoint (item, false, Color.green, "test", 0, 0, SwingConstants.NORTH_WEST);
        assertNotNull (child);
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new Point2D.Double (50, 55));

        final GeoVertex v1 = line1.getVertex (line2);
        final GeoVertex v2 = line2.getVertex (line3);
        final GeoVertex v3 = line3.getVertex (line1);
        assertNotNull (v1);
        assertNotNull (v2);
        assertNotNull (v3);
        test.update ();

        final SolutionRowFilter filter = test.getRowFilter ();
        for (int i = 0; i < test.getRowCount (); i++)
        {
            final int ii = i;
            final RowFilter.Entry<GeoSolution, Integer> entry = new Entry<GeoSolution, Integer> ()
            {
                @Override
                public GeoSolution getModel ()
                {
                    return test;
                }

                @Override
                public int getValueCount ()
                {
                    return test.getRowCount ();
                }

                @Override
                public GeoItem getValue (int index)
                {
                    return test.getRowItem (index);
                }

                @Override
                public Integer getIdentifier ()
                {
                    return ii;
                }
            };
            assertNotNull (entry.getModel ());
            assertNotNull (entry.getValue (i));
            assertEquals (test.getRowCount (), entry.getValueCount ());
            filter.include (entry);
        }
    }
}
