
package com.chriseliot.geo;

import static java.lang.Math.abs;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.Map.Entry;

import javax.swing.event.*;

import org.apache.logging.log4j.*;

import com.chriseliot.util.Labels;

/**
 * Container for geometry items. Nothing accessible from this class should be a GUI element so a
 * geometry plane can be used completely off screen. Some of these methods are designed in support
 * of GUI elements, which is OK, because they do not use GUI elements as parameters.
 */
public class GeoPlane
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    /** All geometry items. */
    private final List<GeoItem> items = new ArrayList<> ();

    /** Variables defined. */
    private final Map<String, GeoItem> bindings = new HashMap<> ();

    /** All geometry vertices. */
    private final List<GeoVertex> vertices = new ArrayList<> ();

    /** All geometry triangles. */
    private final List<GeoTriangle> triangles = new ArrayList<> ();

    private final List<ChangeListener> changeListeners = new ArrayList<> ();

    /** Set when any status changes to indicate that deduction should continue. */
    private boolean dirty = false;

    /** Labels that have been painted. */
    private Labels labels = new Labels ();

    /**
     * Container for geometry items. Nothing accessible from this class should be a GUI element so a
     * geometry plane can be used completely off screen. Some of these methods are designed in
     * support of GUI elements, which is OK, because they do not use GUI elements as parameters.
     */
    public GeoPlane ()
    {
    }

    /** All geometry items. */
    public List<GeoItem> getItems ()
    {
        return items;
    }

    /** Variables defined. */
    public Map<String, GeoItem> getBindings ()
    {
        return bindings;
    }

    /** Lookup a value by name. */
    public GeoItem get (String name)
    {
        return bindings.get (name);
    }

    /**
     * All geometry vertices. This may be obsolete.
     */
    public List<GeoVertex> getVertices ()
    {
        return vertices;
    }

    /**
     * All geometry triangles. This may be obsolete.
     */
    public List<GeoTriangle> getTriangles ()
    {
        return triangles;
    }

    /** Clear the geometry plane. */
    public void clear ()
    {
        items.clear ();
        bindings.clear ();
        vertices.clear ();
        triangles.clear ();
    }

    /**
     * Add an item to this geometry. The item is checked against all previously known items to find
     * and create new vertices.
     *
     * @param item The new item to add, along with implied vertices.
     */
    public void addItem (GeoItem item)
    {
        if (!items.contains (item))
        {
            items.add (item);
        }
        if (bindings.containsValue (item))
        {
            final String key = getBindingKey (item);
            bindings.remove (key);
        }
        bindings.put (item.getName (), item);
        if (item instanceof GeoVertex)
        {
            if (!vertices.contains (item))
            {
                vertices.add ((GeoVertex)item);
            }
        }
        if (item instanceof GeoTriangle)
        {
            if (!triangles.contains (item))
            {
                triangles.add ((GeoTriangle)item);
            }
        }
    }

    /** Add many items all at once. */
    public void addAll (List<GeoItem> items)
    {
        for (final GeoItem item : items)
        {
            addItem (item);
        }
    }

    /** Determine if this plane contains the named item. */
    public boolean contains (GeoItem item)
    {
        return items.contains (item);
    }

    /**
     * Remove an item from the plane. Give the item a chance to remove its children too. This does
     * not assume the bindings use the correct name for this item.
     */
    public void remove (GeoItem item)
    {
        item.remove ();
        items.remove (item);
        vertices.remove (item);
        triangles.remove (item);
        final String key = getBindingKey (item);
        if (key != null)
        {
            bindings.remove (key);
        }
    }

    /**
     * Find the key used to save this item.
     *
     * @param item The item to find.
     *
     * @return The key used in the bindings map.
     */
    public String getBindingKey (GeoItem item)
    {
        for (final Entry<String, GeoItem> entry : bindings.entrySet ())
        {
            if (entry.getValue () == item)
            {
                return entry.getKey ();
            }
        }
        return null;
    }

    /** Make all items deselected. */
    public void deselectAll ()
    {
        for (final GeoItem item : items)
        {
            item.setSelected (false);
        }
    }

    /** Select all items associated with the NamedPoint list. */
    public void selectAll (List<NamedPoint> dragged)
    {
        for (final NamedPoint p : dragged)
        {
            final GeoItem item = p.getParent ();
            item.setSelected (true);
            item.setOpen (true);
        }
    }

    /**
     * Search all items and reset items with derived status to unknown. Then re-apply known status
     * to known items to derive consequences again.
     */
    public void resetDerived ()
    {
        for (final GeoItem item : items)
        {
            if (item.getStatus () == GeoStatus.derived)
            {
                item.setDefaultFormula ();
            }
        }
        solve ();
        fireChangeListeners (this);
    }

    /**
     * Look for triangles in the data. The line segments involved should be made into first class
     * lines, so they can have derived values added. This should be done in the gui too, so their
     * properties can be marked as known or unknown.
     */
    public void findTriangles ()
    {
        final int count = vertices.size ();
        for (int i1 = 0; i1 < count; i1++)
        {
            final GeoVertex v1 = vertices.get (i1);
            for (int i2 = i1 + 1; i2 < count; i2++)
            {
                final GeoVertex v2 = vertices.get (i2);
                for (int i3 = i2 + 1; i3 < count; i3++)
                {
                    final GeoVertex v3 = vertices.get (i3);
                    // final double area = triangleArea (v1, v2, v3);
                    // logger.info ("Considering %.3f area T %s %s %s", area, v1.getName (),
                    // v2.getName (), v3.getName ());
                    if (isTriangle (v1, v2, v3))
                    {
                        if (!hasTriangle (v1, v2, v3))
                        {
                            logger.debug ("Found triangle %s %s %s", v1, v2, v3);
                            final GeoTriangle t = new GeoTriangle (this, Color.red, v1, v2, v3);
                            triangles.add (t);
                            addItem (t);
                        }
                    }
                }
            }
        }
    }

    /**
     * Determine if the three given vertices define a known triangle. This must use the hasVertex
     * method because the triangle will re-arrange the vertices in a preferred order.
     *
     * @return true if the triangle is known, false otherwise.
     */
    public boolean hasTriangle (GeoVertex v1, GeoVertex v2, GeoVertex v3)
    {
        for (final GeoTriangle t : triangles)
        {
            if (t.hasVertex (v1))
            {
                if (t.hasVertex (v2))
                {
                    if (t.hasVertex (v3))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Determine if the three given vertices define a known triangle. This must use the hasVertex
     * method because the triangle will re-arrange the vertices in a preferred order.
     *
     * @return The triangle defined by the vertices if there is one , null otherwise.
     */
    public GeoTriangle getTriangle (GeoVertex v1, GeoVertex v2, GeoVertex v3)
    {
        for (final GeoTriangle t : triangles)
        {
            if (t.hasVertex (v1))
            {
                if (t.hasVertex (v2))
                {
                    if (t.hasVertex (v3))
                    {
                        return t;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Determine if three vertices form a triangle. The vertices must be connected by lines and not
     * be collinear. This implementation verifies that the points are not collinear, by checking
     * that the area is positive.
     *
     * @see https://www.geeksforgeeks.org/program-check-three-points-collinear/
     */
    public boolean isTriangle (GeoVertex v1, GeoVertex v2, GeoVertex v3)
    {
        final GeoLine l1 = v1.connector (v2);
        if (l1 != null)
        {
            final GeoLine l2 = v2.connector (v3);
            if (l2 != null && l1 != l2)
            {
                final GeoLine l3 = v3.connector (v1);
                if (l3 != null && l1 != l3 && l2 != l3)
                {
                    // Area might be positive or negative
                    final double area = triangleArea (v1, v2, v3);
                    return abs (area) > 0;
                }
            }
        }
        return false;
    }

    /** Compute the area of a triangle defined by three vertices. */
    private double triangleArea (GeoVertex v1, GeoVertex v2, GeoVertex v3)
    {
        final Point2D.Double p1 = v1.getPosition ();
        final Point2D.Double p2 = v2.getPosition ();
        final Point2D.Double p3 = v3.getPosition ();
        return p1.x * (p2.y - p3.y) + p2.x * (p3.y - p1.y) + p3.x * (p1.y - p2.y);
    }

    /**
     * Search for a vertex among items known by this geometry.
     *
     * @param position The position to check for the vertex.
     *
     * @return null If no vertex is found at the position checked.
     */
    public GeoVertex getVertex (Point2D.Double position)
    {
        for (final GeoItem item : items)
        {
            if (item instanceof GeoVertex)
            {
                final GeoVertex v = (GeoVertex)item;
                if (v.at (position))
                {
                    return v;
                }
            }
        }
        return null;
    }

    /**
     * Get the closest snap point.
     *
     * @param p The search point.
     * @param limit2 The square of the maximum distance.
     *
     * @return The closest snap point.
     */
    public Point2D.Double getSnapPoint (Point p, double limit2)
    {
        Point2D.Double result = null;
        for (final GeoItem item : items)
        {
            final List<Point2D.Double> snaps = item.getSnapPoints ();
            result = getClosest (p, result, snaps, limit2);
        }
        return result;
    }

    /**
     * Get the closest snap point.
     *
     * @param p The search point.
     * @param limit2 The square of the maximum distance.
     *
     * @return The closest snap point.
     */
    public Point2D.Double getSnapPoint (Point2D.Double p, double limit2)
    {
        Point2D.Double result = null;
        for (final GeoItem item : items)
        {
            final List<Point2D.Double> snaps = item.getSnapPoints ();
            result = getClosest (p, result, snaps, limit2);
        }
        return result;
    }

    /**
     * Get a list of all things that are dragged when the mouse is clicked on the given point.
     *
     * @param p The point where the mouse is clicked.
     *
     * @return The list of items that should move with the mouse.
     */
    public List<NamedPoint> getDragPoints (Point2D.Double p)
    {
        final List<NamedPoint> result = new ArrayList<> ();
        for (final GeoItem item : items)
        {
            if (item instanceof NamedPoint)
            {
                final NamedPoint s = (NamedPoint)item;
                if (s.at (p) && s.isDraggable ())
                {
                    result.add (s);
                }
            }
        }
        return result;
    }

    public void drag (Point2D.Double from, Point2D.Double to)
    {
        final List<NamedPoint> dragged = getDragPoints (from);
        logger.info ("Drag %d items from %s to %s", dragged.size (), from, to);
        for (final NamedPoint p : dragged)
        {
            /*
             * Need to find the associated shape and move the whole object. For a line, this means
             * moving the midpoint. For a rectangle it means moving the connected sides. For a
             * circle it means adjusting the radius and diameter.
             *
             * This needs to also adjust the x, y of the point
             */
            logger.info ("Drag %s to %s", p, to);
            p.drag (to);
        }
        for (final NamedPoint p : dragged)
        {
            p.getParent ().recalculate ();
        }
        fireChangeListeners ();
    }

    /**
     * Determine the object clicked on and return the part that was clicked. The part will be a
     * NamedPoint. Each item must create its parts and add them to the internal list. If the mouse
     * click is not near any object, this will return null.
     *
     * @param target The search point.
     * @param limit2 The square of the maximum distance.
     *
     * @return The part of the object clicked on.
     */
    public NamedPoint getClickObject (Point2D.Double target, double limit2)
    {
        NamedPoint result = null;
        double distance = limit2;
        for (final GeoItem item : items)
        {
            for (final GeoItem child : item.getChildren ())
            {
                if (child instanceof NamedPoint)
                {
                    final NamedPoint part = (NamedPoint)child;
                    final double d = part.distance2 (target);
                    if (d < distance)
                    {
                        result = part;
                        distance = d;
                    }
                    // Pick a vertex in preference to other things
                    else if (d == distance && item instanceof GeoVertex)
                    {
                        result = part;
                        distance = d;
                    }
                }
            }
        }
        return result;
    }

    /**
     * Search a list of points to find the closest point.
     *
     * @param target The position we want to be close to.
     * @param defaultValue The value if no better point is found in the points.
     * @param points The points to search.
     * @param limit2 Square of the maximum distance allowed.
     *
     * @return The defaultValue or best point found. The return value will never be null if a
     *         defaultValue is supplied.
     */
    private Point2D.Double getClosest (Point target, Point2D.Double defaultValue, List<Point2D.Double> points, double limit2)
    {
        Point2D.Double result = defaultValue;
        double distance = defaultValue == null ? limit2 : distance2 (target, defaultValue);
        for (final Point2D.Double s : points)
        {
            final double d = distance2 (target, s);
            if (d < distance)
            {
                result = s;
                distance = d;
            }
        }
        return result;
    }

    /**
     * Search a list of points to find the closest point.
     *
     * @param target The position we want to be close to.
     * @param defaultValue The value if no better point is found in the points.
     * @param points The points to search.
     * @param limit2 Square of the maximum distance allowed.
     *
     * @return The defaultValue or best point found. The return value will never be null if a
     *         defaultValue is supplied.
     */
    private Point2D.Double getClosest (Point2D.Double target, Point2D.Double defaultValue, List<Point2D.Double> points,
            double limit2)
    {
        Point2D.Double result = defaultValue;
        double distance = defaultValue == null ? limit2 : distance2 (target, defaultValue);
        for (final Point2D.Double s : points)
        {
            final double d = distance2 (target, s);
            if (d < distance)
            {
                result = s;
                distance = d;
            }
        }
        return result;
    }

    /**
     * Return distance squared between two points.
     *
     * @param p
     * @param q
     *
     * @return
     */
    public double distance2 (Point p, NamedPoint q)
    {
        return q.distance2 (p);
    }

    /**
     * Return distance squared between two points.
     *
     * @param p
     * @param q
     *
     * @return
     */
    public int distance2 (Point p, Point q)
    {
        final int dx = q.x - p.x;
        final int dy = q.y - p.y;
        return dx * dx + dy * dy;
    }

    /**
     * Return distance squared between two points.
     *
     * @param p
     * @param q
     *
     * @return
     */
    public double distance2 (Point p, Point2D.Double q)
    {
        final double dx = q.x - p.x;
        final double dy = q.y - p.y;
        return dx * dx + dy * dy;
    }

    /**
     * Return distance squared between two points.
     *
     * @param p
     * @param q
     *
     * @return
     */
    public double distance2 (Point2D.Double p, Point2D.Double q)
    {
        final double dx = q.x - p.x;
        final double dy = q.y - p.y;
        return dx * dx + dy * dy;
    }

    public void addChangeListener (ChangeListener listener)
    {
        changeListeners.add (listener);
    }

    public void removeChangeListener (ChangeListener listener)
    {
        changeListeners.remove (listener);
    }

    public void fireChangeListeners (Object source)
    {
        if (!changeListeners.isEmpty ())
        {
            final ChangeEvent e = new ChangeEvent (source);
            for (final ChangeListener listener : changeListeners)
            {
                listener.stateChanged (e);
            }
        }
    }

    public void fireChangeListeners ()
    {
        fireChangeListeners (this);
    }

    /** Set when any status changes to indicate that deduction should continue. */
    public boolean isDirty ()
    {
        return dirty;
    }

    /** Set when any status changes to indicate that deduction should continue. */
    public void setDirty ()
    {
        dirty = true;
    }

    public void solve ()
    {
        while (dirty)
        {
            dirty = false;
            for (final GeoItem item : items)
            {
                item.solve ();
            }
        }
    }

    /** Paint and label all items in this geometry. */
    public void paintItems (Graphics g, Set<String> categories)
    {
        final Labels labels = new Labels ();
        for (final GeoItem item : items)
        {
            if (item.among (categories))
            {
                item.paint (g, labels);
            }
        }
        labels.paint (g);
        this.labels = labels;
    }

    /** Labels that have been painted. */
    public Labels getLabels ()
    {
        return labels;
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
