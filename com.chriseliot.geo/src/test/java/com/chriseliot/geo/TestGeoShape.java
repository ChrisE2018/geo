
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

public class TestGeoShape
{
    @Test
    public void testCreate ()
    {
        assertNotNull (GeoShape.select.toString ());
        assertNotNull (GeoShape.line.toString ());
    }
}
