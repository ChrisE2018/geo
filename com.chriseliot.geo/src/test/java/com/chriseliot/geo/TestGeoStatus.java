
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;

import org.junit.jupiter.api.Test;

public class TestGeoStatus
{
    @Test
    public void testCreate ()
    {
        final GeoStatus s = GeoStatus.known;
        assertNotNull (s.toString ());
    }

    @Test
    public void testComputable ()
    {
        assertTrue (GeoStatus.known.isDetermined ());
        assertTrue (GeoStatus.fixed.isDetermined ());
        assertTrue (GeoStatus.derived.isDetermined ());
        assertFalse (GeoStatus.unknown.isDetermined ());
    }

    @Test
    public void testColor ()
    {
        assertEquals (Color.cyan, GeoStatus.known.getColor ());
        assertEquals (Color.green, GeoStatus.fixed.getColor ());
        assertEquals (Color.green, GeoStatus.derived.getColor ());
        assertEquals (Color.red, GeoStatus.unknown.getColor ());
    }

    @Test
    public void testColorName ()
    {
        assertEquals ("cyan", GeoStatus.known.getColorName ());
        assertEquals ("green", GeoStatus.fixed.getColorName ());
        assertEquals ("green", GeoStatus.derived.getColorName ());
        assertEquals ("red", GeoStatus.unknown.getColorName ());
    }
}
