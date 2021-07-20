
package com.chriseliot.geo;

import static java.lang.Math.*;

import java.awt.Color;
import java.awt.geom.Point2D;

import javax.swing.JOptionPane;

public class AngleVariable extends NamedVariable
{
    public AngleVariable (GeoItem parent, Color color, String name, Double value)
    {
        super (parent, color, name, value);
    }

    public AngleVariable (GeoItem parent, Color color, String name)
    {
        super (parent, color, name);
    }

    /** Should a popup menu on this item include a set value item. */
    @Override
    public boolean canSetValue ()
    {

        final GeoItem parent = getParent ();
        if (parent instanceof NamedPoint)
        {
            final GeoItem grandParent = parent.getParent ();
            if (grandParent instanceof GeoLine)
            {
                return ((GeoLine)grandParent).getAngle () == this;
            }
        }
        return false;
    }

    /** Action to perform for a set value action. */
    @Override
    public void setValueAction ()
    {
        final Double value = getDoubleValue ();
        final String message = (value == null) ? String.format ("Enter new angle %s degrees", getName ())
                                               : String.format ("Enter new angle %s (%s) degrees", getName (), value);
        final String result = JOptionPane.showInputDialog (null, message, "Input Value", JOptionPane.QUESTION_MESSAGE);
        if (result != null)
        {
            setValueAction (Double.parseDouble (result));
        }
    }

    /**
     * Action to take when the dialog returns. This only works for variables that are x or y of a
     * named point. If calls the point to make the adjustment.
     */
    @Override
    public void setValueAction (double result)
    {
        final GeoItem parent = getParent ();
        if (parent instanceof NamedPoint)
        {
            final GeoItem grandParent = parent.getParent ();
            if (grandParent instanceof GeoLine && this == ((GeoLine)grandParent).getAngle ())
            {
                final double radians = toRadians (result);
                // Compute rotation around midpoint to make the new angle
                final GeoLine p = (GeoLine)grandParent;
                final double length = p.getLength ().getDoubleValue ();
                final double l2 = length / 2;
                final double bx = l2 * sin (radians);
                final double by = l2 * cos (radians);
                final NamedPoint m = p.getMidpoint ();
                final double mx = m.getPosition ().x;
                final double my = m.getPosition ().y;
                p.getFrom ().setValueAction (new Point2D.Double (mx - bx, my - by));
                p.getTo ().setValueAction (new Point2D.Double (mx + bx, my + by));
            }
        }
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        buffer.append (" ");
        buffer.append (getDoubleValue ());
        buffer.append (">");
        return buffer.toString ();
    }
}
