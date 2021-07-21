
package com.chriseliot.geo;

import static java.lang.Math.*;

import java.awt.Color;

import javax.swing.*;

public class TriangleAngleVariable extends NamedVariable
{
    public TriangleAngleVariable (GeoItem parent, Color color, String name, Double value)
    {
        super (parent, color, name, value);
    }

    public TriangleAngleVariable (GeoItem parent, Color color, String name)
    {
        super (parent, color, name);
    }

    /** Should a popup menu on this item include a set value item. */
    @Override
    public boolean canSetValue ()
    {
        return true;
    }

    /** Action to perform for a set value action. */
    @Override
    public void setValueAction ()
    {
        final JTextField angleField = new JTextField (String.format ("%.3f", getDoubleValue ()));
        final JCheckBox cornerField = new JCheckBox ("Right Angle", getDoubleValue () == 90);
        final Object[] message = {"Angle:", angleField, cornerField};
        final String result = JOptionPane.showInputDialog (null, message, "Set angle", JOptionPane.QUESTION_MESSAGE);
        if (result != null)
        {
            if (cornerField.isSelected ())
            {
                setValueAction (90);
            }
            else
            {
                setValueAction (Double.parseDouble (result));
            }
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
        final GeoItem grandParent = parent.getParent ();
        final GeoTriangle p = (GeoTriangle)grandParent;
        // Compute rotation around midpoint to make the new angle
        final double radians = toRadians (result);
        final double theta1 = getDoubleValue ();
        final double delta = result - theta1;
        final NamedVariable c = p.getOpposite (this);
        // For law of sines
        final double constant = c.getDoubleValue () / sin (radians);
        final GeoVertex v = p.getVertex (this);
        final NamedVariable legA = p.getLeg1 (v);
        final NamedVariable legB = p.getLeg2 (v);
        final TriangleAngleVariable angleA = p.getOppositeAngle (legA);
        final TriangleAngleVariable angleB = p.getOppositeAngle (legB);
        final double A2 = angleA.getDoubleValue () + delta * 0.5;
        final double B2 = angleB.getDoubleValue () + delta * 0.5;
        final double a2 = constant * sin (A2);
        final double b2 = constant * sin (B2);

        // final double length = p.getLength ().getDoubleValue ();
        // final double l2 = length / 2;
        // final double bx = l2 * sin (radians);
        // final double by = l2 * cos (radians);
        // final NamedPoint m = p.getMidpoint ();
        // final double mx = m.getPosition ().x;
        // final double my = m.getPosition ().y;
        // p.getFrom ().setValueAction (new Point2D.Double (mx - bx, my - by));
        // p.getTo ().setValueAction (new Point2D.Double (mx + bx, my + by));
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
