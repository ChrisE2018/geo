
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.io.*;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.junit.jupiter.api.Test;
import org.xml.sax.SAXException;

import com.chriseliot.geo.gui.*;
import com.chriseliot.util.Namer;

class TestFileSave
{
    private final TestSupport ts = new TestSupport ();

    @Test
    public void testCreate ()
    {
        final Geo geo = new Geo ();
        final FileSave fileSave = new FileSave (geo);
        assertNotNull (fileSave.toString ());

    }

    @Test
    public void testSimple () throws ParserConfigurationException, IOException, TransformerException
    {
        Namer.reset ();
        final Geo geo = new Geo ();
        final FileSave fileSave = new FileSave (geo);
        final GeoPlane plane = geo.getPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoLine test = new GeoLine (plane, color, from, to);
        assertNotNull (test);
        final File file = ts.getTestDataFile (this, "line1", "xml");
        fileSave.saveXml (file);
        assertTrue (file.exists ());
        file.delete ();
    }

    @Test
    public void testLineRead () throws ParserConfigurationException, IOException, TransformerException, SAXException
    {
        Namer.reset ();
        final Geo geo = new Geo ();
        final FileSave fileSave = new FileSave (geo);
        final GeoPlane plane = geo.getPlane ();
        final Color color = Color.red;
        final Point2D.Double from = new Point2D.Double (10, 20);
        final Point2D.Double to = new Point2D.Double (30, 40);
        final GeoLine test = new GeoLine (plane, color, from, to);
        assertNotNull (test);
        final File file = ts.getTestDataFile (this, "line2", "xml");
        fileSave.saveXml (file);
        fileSave.readXml (file);
        assertTrue (file.exists ());
        file.delete ();
    }

    @Test
    public void testVertexWrite () throws ParserConfigurationException, IOException, TransformerException
    {
        Namer.reset ();
        final Geo geo = new Geo ();
        final FileSave fileSave = new FileSave (geo);
        final GeoPlane plane = geo.getPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        assertNotNull (line1);
        assertNotNull (line2);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (10, 20));
        assertNotNull (v1);

        final File file = ts.getTestDataFile (this, "vertex1", "xml");
        fileSave.saveXml (file);
        assertTrue (file.exists ());
        file.delete ();
    }

    @Test
    public void testVertexRead () throws ParserConfigurationException, IOException, TransformerException, SAXException
    {
        Namer.reset ();
        final Geo geo = new Geo ();
        final FileSave fileSave = new FileSave (geo);
        final GeoPlane plane = geo.getPlane ();
        final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new Point2D.Double (30, 40));
        final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new Point2D.Double (50, 55));
        assertNotNull (line1);
        assertNotNull (line2);
        final GeoVertex v1 = plane.getVertex (new Point2D.Double (10, 20));
        assertNotNull (v1);

        final File file = ts.getTestDataFile (this, "vertex2", "xml");
        fileSave.saveXml (file);
        fileSave.readXml (file);
        assertTrue (file.exists ());
        file.delete ();
    }

    @Test
    public void testTriangleWrite () throws ParserConfigurationException, IOException, TransformerException
    {
        Namer.reset ();
        final Geo geo = new Geo ();
        final FileSave fileSave = new FileSave (geo);
        final GeoPlane plane = geo.getPlane ();
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
        assertNotNull (t);
        final File file = ts.getTestDataFile (this, "triangle1", "xml");
        fileSave.saveXml (file);
        assertTrue (file.exists ());
        file.delete ();
    }

    @Test
    public void testTriangleRound () throws ParserConfigurationException, IOException, TransformerException, SAXException
    {
        Namer.reset ();
        final Geo geo = new Geo ();
        final FileSave fileSave = new FileSave (geo);
        final GeoPlane plane = geo.getPlane ();
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
        assertNotNull (t);

        final File file = ts.getTestDataFile (this, "triangle2", "xml");
        final File file2 = ts.getTestDataFile (this, "triangle2rt", "xml");
        try
        {
            fileSave.saveXml (file);
            assertTrue (file.exists ());

            // Now read back into a fresh instance
            final Geo geo2 = new Geo ();
            final FileSave fileSave2 = new FileSave (geo2);
            fileSave2.readXml (file);
            // Write out again
            fileSave2.saveXml (file2);
            // Now compare the files
            ts.compare (file, file2);
        }
        finally
        {
            if (file.exists ())
            {
                file.delete ();
            }
            if (file2.exists ())
            {
                file2.delete ();
            }
        }
    }

    @Test
    public void testComplexRound () throws ParserConfigurationException, IOException, TransformerException, SAXException
    {
        Namer.reset ();
        final Geo geo = new Geo ();
        final FileSave fileSave = new FileSave (geo);
        final GeoPlane plane = geo.getPlane ();
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
        assertNotNull (t);

        final GeoRectangle r1 = new GeoRectangle (plane, Color.red, new Point2D.Double (10, 10), new Point2D.Double (150, 300));
        assertNotNull (r1);
        final GeoOval o1 = new GeoOval (plane, Color.red, new Point2D.Double (100, 100), new Point2D.Double (50, 225));
        assertNotNull (o1);
        final File file = ts.getTestDataFile (this, "complex", "xml");
        final File file2 = ts.getTestDataFile (this, "complexrt", "xml");
        try
        {
            fileSave.saveXml (file);
            assertTrue (file.exists ());

            // Now read back into a fresh instance
            final Geo geo2 = new Geo ();
            final FileSave fileSave2 = new FileSave (geo2);
            fileSave2.readXml (file);
            // Write out again
            fileSave2.saveXml (file2);
            // Now compare the files
            ts.compare (file, file2);
        }
        finally
        {
            if (file.exists ())
            {
                file.delete ();
            }
            if (file2.exists ())
            {
                file2.delete ();
            }
        }
    }
}
