
package com.chriseliot.geo;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.*;
import org.w3c.dom.*;

import com.chriseliot.util.*;

public class GeoItem
{
    public static final XMLUtil xu = new XMLUtil ();

    private static Logger logger = LogManager.getFormatterLogger (GeoItem.class);

    /** Separator between parts of a name. */
    public static char SEP = '$';

    /** Support for generating unique names. */
    private static Namer namer = new Namer ();

    /** The geometry plane. */
    private final GeoPlane plane;

    /** Display name. */
    private String name;

    /** Parent item of this item. */
    private final GeoItem parent;

    /** Children of this item. */
    private final List<GeoItem> children = new ArrayList<> ();

    /** Display color. */
    private Color color;

    /** Categories for display filtering. */
    private final Set<String> categories = new TreeSet<> ();

    /** Has this item been selected by a mouse click. */
    private boolean isSelected;

    /** Is this item expanded in the solution table. */
    private boolean isOpen;

    /** The current status of this item */
    private GeoStatus status = GeoStatus.unknown;

    /** The reason this item was given this status. */
    private String reason = null;

    /** Make a toplevel item (with no parent). */
    public GeoItem (GeoPlane plane, String nameRoot, Color color)
    {
        this.plane = plane;
        parent = null;
        name = namer.getname (nameRoot);
        this.color = color;
        plane.addItem (this);
        addCategory ("detail");
    }

    /** Make a child item with the given parent. */
    public GeoItem (GeoItem parent, String name, Color color)
    {
        plane = parent.getPlane ();
        this.parent = parent;
        this.parent.addChild (this);
        this.name = name;
        this.color = color;
        plane.addItem (this);
        addCategory ("detail");
    }

    /** The geometry plane. */
    public GeoPlane getPlane ()
    {
        return plane;
    }

    /** Display name. */
    public String getName ()
    {
        return name;
    }

    /** Display name. */
    public void setName (String name)
    {
        this.name = name;
    }

    /** The GeoItem parent of this item. */
    public GeoItem getParent ()
    {
        return parent;
    }

    /** Children of this item. */
    public List<GeoItem> getChildren ()
    {
        return children;
    }

    /** Children of this item. */
    public void addChild (GeoItem child)
    {
        children.add (child);
    }

    /** Are there any children of this item. */
    public boolean hasChildren ()
    {
        return !children.isEmpty ();
    }

    /** Display color. */
    public Color getColor ()
    {
        return color;
    }

    /** Display color. */
    public void setColor (Color color)
    {
        this.color = color;
    }

    /** Categories for display filtering. */
    public Set<String> getCategories ()
    {
        return categories;
    }

    /** Categories for display filtering. */
    public void addCategory (String category)
    {
        categories.add (category);
    }

    public boolean among (Set<String> filter)
    {
        for (final String c : filter)
        {
            if (categories.contains (c))
            {
                return true;
            }
        }
        return false;
    }

    /** Has this item been selected by a mouse click. */
    public boolean isSelected ()
    {
        return isSelected;
    }

    /** Has this item been selected by a mouse click. */
    public void setSelected (boolean isSelected)
    {
        this.isSelected = isSelected;
    }

    /** Is this item expanded in the solution table. */
    public boolean isOpen ()
    {
        return isOpen;
    }

    /** Is this item expanded in the solution table. */
    public void setOpen (boolean isOpen)
    {
        this.isOpen = isOpen;
    }

    /** Return the current status of this item. */
    public GeoStatus getStatus ()
    {
        return status;
    }

    /** Set the current status of this item. */
    public void setStatus (GeoStatus status, String reason)
    {
        if (this.status != status)
        {
            this.status = status;
            this.reason = reason;
            plane.setDirty ();
        }
    }

    /** Set the current status of this item. */
    public void setGivenStatus (GeoStatus status)
    {
        setStatus (status, "given");
        plane.solve ();
        plane.fireChangeListeners (this);
    }

    /** Set the status to unknown. */
    public void setStatusUnknown ()
    {
        setStatus (GeoStatus.unknown, null);
    }

    /** Is this value known. */
    public boolean isDetermined ()
    {
        return status.isDetermined ();
    }

    /** The reason this item was given this status. */
    public String getReason ()
    {
        return reason;
    }

    /**
     * Recalculate values derived from screen positions after a something moves.
     */
    public void recalculate ()
    {

    }

    /**
     * Create vertices if this item crosses the other item.
     *
     * @param i An existing item to check.
     */
    public void findVertices (GeoItem i)
    {

    }

    public List<GeoVertex> getVertices ()
    {
        return null;
    }

    /** Position of all named points on this item. */
    public List<Point2D.Double> getSnapPoints ()
    {
        final List<Point2D.Double> result = new ArrayList<> ();
        for (final GeoItem item : getChildren ())
        {
            if (item instanceof NamedPoint)
            {
                final NamedPoint p = (NamedPoint)item;
                result.add (p.getPosition ());
            }
        }
        return result;
    }

    /** List of points on this item that can be dragged. */
    public List<Point2D.Double> getDragPoints ()
    {
        final List<Point2D.Double> result = new ArrayList<> ();
        for (final GeoItem item : getChildren ())
        {
            if (item instanceof NamedPoint)
            {
                final NamedPoint p = (NamedPoint)item;
                if (p.isDraggable ())
                {
                    result.add (p.getPosition ());
                }
            }
        }
        return result;
    }

    /**
     * Transpose the position of this item.
     *
     * @param dx Distance to move.
     * @param dy Distance to move.
     */
    public void move (double dx, double dy)
    {

    }

    /**
     * Remove the children of this item. This is called by the GeoPlane so this method should not
     * try to remove itself from the GeoPlane.
     */
    public void remove ()
    {
        final GeoPlane plane = getPlane ();
        plane.remove (this);
        for (final GeoItem child : new ArrayList<> (children))
        {
            child.remove ();
        }
    }

    /** Derive inferences from this item. */
    public void solve ()
    {
    }

    /**
     * Paint this item in its current state. The Graphics object is normally the window component,
     * but could be a printer or BufferedImage.
     *
     * @param g The paint destination.
     * @param labels The label container.
     */
    public void paint (Graphics g, Labels labels)
    {

    }

    /**
     * Populate a popup menu with required items. This should be overridden by subclasses. Be sure
     * to call the super method.
     *
     * @param result
     */
    public void popup (Map<String, Consumer<GeoItem>> result)
    {
        result.put ("known", item -> item.setGivenStatus (GeoStatus.known));
        result.put ("unknown", item -> item.setGivenStatus (GeoStatus.unknown));
    }

    /** Convert to an element for saving to a file. */
    public void getElement (Element root)
    {
        final Document doc = root.getOwnerDocument ();
        final Element result = doc.createElement (getClass ().getSimpleName ());
        root.appendChild (result);
        for (final GeoItem child : children)
        {
            child.getElement (result);
        }
        getAttributes (result);
    }

    /** Convert to an element for saving to a file. */
    public void getAttributes (Element element)
    {
        element.setAttribute ("name", name);
        element.setAttribute ("color", String.valueOf (color.getRGB ()));
        element.setAttribute ("selected", String.valueOf (isSelected));
        element.setAttribute ("open", String.valueOf (isOpen));
        element.setAttribute ("status", String.valueOf (status));
        element.setAttribute ("reason", reason);
    }

    /** Restore from the xml. */
    public void marshall (Element element)
    {
        setName (xu.get (element, "name", name));
        color = new Color (xu.getInteger (element, "color", 0));
        isSelected = xu.getBoolean (element, "selected", false);
        isOpen = xu.getBoolean (element, "open", false);
        status = GeoStatus.valueOf (xu.get (element, "status", "unknown"));
        reason = xu.get (element, "reason", null);
    }

    /** Find an item in the plane and restore it from the xml. */
    public void marshallReference (Element parentXml, String name)
    {
        final GeoItem item = getPlane ().get (name);
        if (item == null)
        {
            logger.warn ("Can't locate item %s", name);
        }
        else
        {
            final Element child = xu.getNthChild (parentXml, "name", name, 0);
            if (child == null)
            {
                logger.warn ("Can't locate xml name=%s in %s", name, xu.getXMLString (parentXml));
            }
            else
            {
                item.marshall (child);
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
        buffer.append (name);
        buffer.append (" ");
        buffer.append (status);
        if (reason != null)
        {
            buffer.append (" ");
            buffer.append ("'");
            buffer.append (reason);
            buffer.append ("'");
        }
        buffer.append (">");
        return buffer.toString ();
    }
}
