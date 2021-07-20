
package com.chriseliot.geo;

import java.awt.Color;

public enum GeoStatus
{
    known, unknown, derived, fixed;

    private final boolean determined;
    private Color color;
    private String colorName;

    private GeoStatus ()
    {
        if (name ().equals ("known"))
        {
            determined = true;
            color = Color.cyan;
            colorName = "cyan";
        }
        else if (name ().equals ("derived"))
        {
            determined = true;
            color = Color.green;
            colorName = "green";
        }
        else if (name ().equals ("fixed"))
        {
            determined = true;
            color = Color.green;
            colorName = "green";
        }
        else
        {
            determined = false;
            color = Color.red;
            colorName = "red";
        }
    }

    public boolean isDetermined ()
    {
        return determined;
    }

    public Color getColor ()
    {
        return color;
    }

    public String getColorName ()
    {
        return colorName;
    }
}
