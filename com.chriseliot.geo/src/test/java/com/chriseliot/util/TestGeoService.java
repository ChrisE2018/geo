
package com.chriseliot.util;

import org.apache.logging.log4j.*;

public class TestGeoService implements GeoService
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    public TestGeoService ()
    {
        logger.info ("Created %s", this);
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        buffer.append (" ");
        buffer.append (System.identityHashCode (this));
        buffer.append (">");
        return buffer.toString ();
    }
}
