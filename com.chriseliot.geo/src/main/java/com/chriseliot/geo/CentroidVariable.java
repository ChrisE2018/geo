
package com.chriseliot.geo;

import static java.lang.Math.round;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

import com.chriseliot.util.Labels;

public class CentroidVariable extends NamedVariable
{
    /**
     * Location on screen to draw label for this variable. The location used will be the average
     * (centroid) of all locations given.
     */
    private final List<NamedPoint> locations = new ArrayList<> ();

    public CentroidVariable (GeoItem parent, Color color, String name, Double value)
    {
        super (parent, color, name, value);
        if (parent instanceof NamedPoint)
        {
            setLocation ((NamedPoint)parent);
        }
    }

    @Override
    public void setLocation (NamedPoint p)
    {
        locations.clear ();
        locations.add (p);
    }

    public void setLocation (NamedPoint... points)
    {
        locations.clear ();
        for (final NamedPoint p : points)
        {
            locations.add (p);
        }
    }

    @Override
    public NamedPoint getLocation ()
    {
        if (!locations.isEmpty ())
        {
            return locations.get (0);
        }
        return null;
    }

    public Point2D.Double getPosition ()
    {
        double totalX = 0;
        double totalY = 0;
        int count = 0;
        for (final NamedPoint p : locations)
        {
            final Point2D.Double position = p.getPosition ();
            totalX += position.x;
            totalY += position.y;
            count++;
        }
        if (count > 0)
        {
            return new Point2D.Double (totalX / count, totalY / count);
        }
        return null;
    }

    /**
     * Paint this item in its current state. The Graphics object is normally the window component,
     * but could be a printer or BufferedImage.
     *
     * @param g The paint destination.
     */
    @Override
    public void paint (Graphics g, Labels labels)
    {
        final Point2D.Double position = getPosition ();
        final NamedPoint location = getLocation ();
        if (position != null && location != null)
        {
            final Color color = getStatus ().getColor ();
            final Double value = getDoubleValue ();
            String text = getName ();
            if (value != null)
            {
                text = String.format ("%s = %.1f", getName (), value);
            }
            final String tooltip;
            if (getInference () == null)
            {
                tooltip = text;
            }
            else
            {
                tooltip = String.format ("%.1f = %s", value, getInference ().getInstantiation ());
            }

            final Point p = new Point ((int)round (position.x), (int)round (position.y));
            final int anchor = location.getAnchor ();
            labels.add (this, color, p, anchor, text, tooltip);
        }
    }
}
