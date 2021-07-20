
package com.chriseliot.geo;

import java.awt.Color;
import java.awt.geom.Point2D;

import org.apache.logging.log4j.*;

public class TransposePoint extends NamedPoint
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    public TransposePoint (GeoItem parent, boolean draggable, Color color, String name, double x, double y, int anchor)
    {
        super (parent, draggable, color, name, x, y, anchor);
    }

    /** Drag to a new location. */
    @Override
    public void drag (Point2D.Double drag)
    {
        final Point2D.Double from = getPosition ();
        final double dx = drag.x - from.x;
        final double dy = drag.y - from.y;
        logger.info ("Drag %s %.2f %.2f to %s", this, dx, dy, drag);
        final GeoItem parent = getParent ();
        parent.move (dx, dy);
        setPosition (drag);
    }
}
