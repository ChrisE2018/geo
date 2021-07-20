
package com.chriseliot.util;

import java.awt.*;

public class LabelItem
{
    /** The object that created this label and is responsible. */
    private final Object source;
    private final Color color;
    private final Point position;
    private final String text;
    private final String tooltip;
    private Rectangle bounds = null;
    private final int anchor;

    public LabelItem (Object source, Color color, Point position, int anchor, String text)
    {
        this.source = source;
        this.color = color;
        this.position = position;
        this.anchor = anchor;
        this.text = text;
        tooltip = null;
    }

    public LabelItem (Object source, Color color, Point position, int anchor, String text, String tooltip)
    {
        this.source = source;
        this.color = color;
        this.position = position;
        this.anchor = anchor;
        this.text = text;
        this.tooltip = tooltip;
    }

    /** The object that created this label and is responsible. */
    public Object getSource ()
    {
        return source;
    }

    /**
     * @return the color
     */
    public Color getColor ()
    {
        return color;
    }

    /**
     * @return the position
     */
    public Point getPosition ()
    {
        return position;
    }

    public int getAnchor ()
    {
        return anchor;
    }

    /**
     * @return the text
     */
    public String getText ()
    {
        return text;
    }

    public String getTooltip ()
    {
        return tooltip;
    }

    /**
     * @return the bounds
     */
    public Rectangle getBounds ()
    {
        return bounds;
    }

    /**
     * @param bounds the bounds to set
     */
    public void setBounds (Rectangle bounds)
    {
        this.bounds = bounds;
    }

    public void setBounds (int x, int y, int w, int h)
    {
        bounds = new Rectangle (x, y, w, h);
    }

    public boolean intersects (Rectangle bounds)
    {
        if (this.bounds != null)
        {
            return this.bounds.intersects (bounds);
        }
        return false;
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        if (bounds != null)
        {
            buffer.append (" ");
            buffer.append ("<");
            buffer.append (bounds.x);
            buffer.append (" ");
            buffer.append (bounds.y);
            buffer.append (" ");
            buffer.append (bounds.width);
            buffer.append (" ");
            buffer.append (bounds.height);
            buffer.append (">");
        }
        buffer.append (" ");
        buffer.append (text);
        if (tooltip != null)
        {
            buffer.append (" ");
            buffer.append (tooltip);
        }
        buffer.append (">");
        return buffer.toString ();
    }
}
