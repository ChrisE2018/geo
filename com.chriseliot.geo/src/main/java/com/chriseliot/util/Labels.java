
package com.chriseliot.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import javax.swing.SwingConstants;

public class Labels
{
    private final List<LabelItem> labels = new ArrayList<> ();

    public void add (LabelItem label)
    {
        labels.add (label);
    }

    public void add (Object source, Color color, Point position, int anchor, String text)
    {
        labels.add (new LabelItem (source, color, position, anchor, text));
    }

    public void add (Object source, Color color, Point position, int anchor, String text, String tooltip)
    {
        labels.add (new LabelItem (source, color, position, anchor, text, tooltip));
    }

    public void add (Object source, Point position, int anchor, String text)
    {
        labels.add (new LabelItem (source, Color.black, position, anchor, text));
    }

    public boolean blocked (int x, int y, int w, int h)
    {
        return blocked (new Rectangle (x, y, w, h));
    }

    public boolean blocked (Point p, int w, int h)
    {
        return blocked (new Rectangle (p.x, p.y, w, h));
    }

    public boolean blocked (Rectangle bounds)
    {
        for (final LabelItem item : labels)
        {
            if (item.intersects (bounds))
            {
                return true;
            }
        }
        return false;
    }

    public LabelItem find (Point p)
    {
        for (final LabelItem item : labels)
        {
            final Rectangle bounds = item.getBounds ();
            if (bounds != null)
            {
                if (bounds.contains (p))
                {
                    return item;
                }
            }
        }
        return null;
    }

    public void paint (Graphics g)
    {
        final FontMetrics m = g.getFontMetrics ();
        final int h = m.getHeight ();
        final int b = m.getDescent ();
        for (final LabelItem item : labels)
        {
            g.setColor (item.getColor ());
            final String text = item.getText ();
            final int w = m.stringWidth (text);
            final int anchor = item.getAnchor ();
            final Point p = item.getPosition ();
            int x = p.x;
            int y = p.y - h;
            int delta = h + 2;
            if (anchor == SwingConstants.NORTH_WEST)
            {
                y += h;
            }
            else if (anchor == SwingConstants.NORTH_EAST)
            {
                y += h;
                x -= w;
            }
            else if (anchor == SwingConstants.SOUTH_WEST)
            {
                delta = -delta;
            }
            else if (anchor == SwingConstants.SOUTH_EAST)
            {
                x -= w;
                delta = -delta;
            }
            else if (anchor == SwingConstants.CENTER)
            {
                x -= w / 2;
                y += h / 2;
                delta = -delta;
            }
            while (blocked (x, y, w, h))
            {
                y += delta;
            }
            item.setBounds (x, y, w, h);
            g.drawString (text, x, y + h - b);
        }
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
