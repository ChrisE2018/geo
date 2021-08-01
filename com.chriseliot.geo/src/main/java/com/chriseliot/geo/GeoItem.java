
package com.chriseliot.geo;

import java.awt.*;
import java.awt.geom.Point2D;
import java.beans.*;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.*;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.w3c.dom.*;

import com.chriseliot.util.*;

public class GeoItem
{
    private static Logger logger = LogManager.getFormatterLogger (GeoItem.class);

    public static final XMLUtil xu = new XMLUtil ();

    public static final TextUtils tu = new TextUtils ();

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

    /** Have inferences been derived since the last GeoPlane change. */
    private boolean isSolved = false;

    /** The current status of this item */
    private GeoStatus status = GeoStatus.unknown;

    /**
     * Represents one derivation of the value of this item.
     */
    private final List<Inference> inferences = new ArrayList<> ();

    /** Listeners called when status of this item changes. Useful for debugging and unit testing. */
    private final List<PropertyChangeListener> statusChangeListeners = new ArrayList<> ();

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

    public static List<String> getNames (Collection<GeoItem> items)
    {
        final List<String> result = new ArrayList<> ();
        if (items != null)
        {
            for (final GeoItem item : items)
            {
                result.add (item.getName ());
            }
        }
        return result;
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
            logger.info ("[%s] Changing %s status from %s to %s", reason, name, this.status, status);
            fireStatusChangeListeners (this.status, status);
            this.status = status;
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
        setStatus (GeoStatus.unknown, "given");
    }

    public void addStatusChangeListener (PropertyChangeListener listener)
    {
        statusChangeListeners.add (listener);
    }

    private void fireStatusChangeListeners (GeoStatus oldValue, GeoStatus newValue)
    {
        if (!statusChangeListeners.isEmpty ())
        {
            final PropertyChangeEvent e = new PropertyChangeEvent (this, "status", oldValue, newValue);
            for (final PropertyChangeListener listener : statusChangeListeners)
            {
                listener.propertyChange (e);
            }
        }
    }

    /** Is this value known directly or indirectly. */
    public boolean isDetermined ()
    {
        if (status.isDetermined ())
        {
            return true;
        }
        if (isDetermined (false))
        {
            status = GeoStatus.derived;
            return true;
        }
        return false;
    }

    /** Is this value known directly or indirectly. */
    public boolean isDetermined (boolean why)
    {
        if (status.isDetermined ())
        {
            if (why)
            {
                logger.info ("%s is determined by status %s", this, status);
            }
            return true;
        }
        final Set<GeoItem> known = new HashSet<> ();
        final Set<GeoItem> closed = new HashSet<> ();
        closed.add (this);
        for (final Inference inference : inferences)
        {
            if (inference.isDetermined (known, closed, why, 0))
            {
                return true;
            }
        }
        return false;
    }

    /** Is this value known directly or indirectly. */
    public boolean isDetermined (Set<GeoItem> known, Set<GeoItem> closed, boolean why, int level)
    {
        if (known.contains (this))
        {
            return true;
        }
        if (status.isDetermined ())
        {
            known.add (this);
            return true;
        }
        if (!closed.contains (this))
        {
            closed.add (this);
            for (final Inference inference : inferences)
            {
                if (inference.isDetermined (known, closed, why, level + 1))
                {
                    if (why)
                    {
                        logger.info ("%s %s is determined by inference %s with %d open terms", indent (level), this, inference,
                                inference.getTerms ().length - 1);
                    }
                    known.add (this);
                    return true;
                }
            }
        }
        if (why)
        {
            logger.info ("%s %s is not determined because none of the %d inferences hold", indent (level), this,
                    inferences.size ());
        }
        return false;
    }

    public String indent (int level)
    {
        final StringBuilder builder = new StringBuilder ();
        for (int i = 0; i < level; i++)
        {
            builder.append ("|  ");
        }
        return builder.toString ();
    }

    public boolean whyDetermined ()
    {
        return isDetermined (true);
    }

    /** Determine the list of items that form the basis for inferring this one is determined. */
    public Set<GeoItem> getSupport ()
    {
        final Set<GeoItem> known = new HashSet<> ();
        final Set<GeoItem> closed = new HashSet<> ();
        if (isDetermined (known, closed, false, 0))
        {
            return known;
        }
        return null;
    }

    /** Represents one derivation step. */
    public Inference getInference ()
    {
        for (final Inference inference : inferences)
        {
            final Set<GeoItem> known = new HashSet<> ();
            final Set<GeoItem> closed = new HashSet<> ();
            closed.add (this);
            if (inference.isDetermined (known, closed, false, 0))
            {
                return inference;
            }
        }
        return null;
    }

    /** Get all inferences attached to this item. */
    public List<Inference> getInferences ()
    {
        return inferences;
    }

    /**
     * Shorthand function to set the formula and save the names of the variables involved.
     *
     * @param reason A string giving the reason for this derivation.
     * @param formula A Java format template to create the derivation formula.
     * @param variables Variables to substitute into the template string to create the derivation
     *            formula.
     */
    public void setFormula (String reason, String formula, GeoItem... variables)
    {
        final GeoItem[] terms = new GeoItem[variables.length];
        System.arraycopy (variables, 0, terms, 0, variables.length);
        if (!hasInference (reason, formula, terms))
        {
            inferences.add (new Inference (this, reason, formula, terms));
        }
    }

    private boolean hasInference (String reason, String formula, GeoItem[] terms)
    {
        for (final Inference inference : inferences)
        {
            if (inference.matches (reason, formula, terms))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Solve the formula in terms of given knowns to determine the value of this variable.
     *
     * 1. Perform a recursive search to find the root source of the derivation.
     *
     * 2. Use the Symja solver to eliminate all of the intermediate variables from the derivation.
     *
     * @return an expression for this variable in terms of the given known values only, with no
     *         intermediate terms involved.
     */
    public String getDerivedFormula ()
    {
        final StringBuilder builder = new StringBuilder ();
        final Set<String> chain = new HashSet<> ();
        final Set<String> roots = new HashSet<> ();
        getDerivationChain (chain, roots);
        builder.append ("eliminate");
        builder.append ("(");
        builder.append ("{");
        tu.join (builder, ", ", chain);
        builder.append ("}");
        builder.append (", ");
        builder.append ("{");
        roots.remove (getName ());
        tu.join (builder, ", ", roots);
        builder.append ("}");
        builder.append (")");
        logger.info ("Symbolic %s: %s", getName (), builder.toString ());
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse (builder.toString ());
        logger.info ("Expression: %s", expr);
        final IExpr value = eval.eval (expr);
        logger.info ("Value: %s", value);
        return value.toString ();
    }

    /**
     * Get the formulas required to derive this value.
     *
     * @param chain All formulas involved in deriving this value are added to this set. This is done
     *            by searching the terms for GeoItems that contribute to the derivation of this
     *            value. The formula for this variable is included in the set.
     * @param roots The variable names of the intermediate terms that are derived along the way
     *            while deriving this value. These are the names that should be eliminated to create
     *            the overall formula. The name of this variable is included in the set, but needs
     *            to be removed before the elimination step because we want it to be part of the
     *            overall formula.
     */
    private void getDerivationChain (Set<String> chain, Set<String> roots)
    {
        final Inference inference = getInference ();
        if (inference != null)
        {
            for (final GeoItem term : inference.getTerms ())
            {
                roots.add (term.getName ());
                final Inference termInference = term.getInference ();
                if (termInference != null)
                {
                    final String termFormula = termInference.getInstantiation ();
                    chain.add (termFormula);
                    if (term != this)
                    {
                        term.getDerivationChain (chain, roots);
                    }
                }
            }
        }
    }

    public void getDerivation (StringBuilder builder, int level, Set<GeoItem> closed)
    {
        if (!closed.contains (this))
        {
            final Inference inference = getInference ();
            if (inference != null)
            {
                closed.add (this);
                final GeoItem[] terms = inference.getTerms ();
                for (int i = 1; i < terms.length; i++)
                {
                    final GeoItem ti = terms[i];
                    ti.getDerivation (builder, level + 1, closed);
                    ti.getFormulaLine (builder, level);
                }
            }
        }
    }

    public void getFormulaLine (StringBuilder builder, int level)
    {
        for (int i = 0; i < level; i++)
        {
            builder.append ("|  ");
        }
        final Inference inference = getInference ();
        if (inference == null)
        {
            builder.append (String.format ("%s == %s", getName (), getStringValue ()));
        }
        else
        {
            final String formula = inference.getInstantiation ();
            builder.append (formula);
        }
        if (getStatus () == GeoStatus.known)
        {
            builder.append (" given");
        }
        else if (getStatus () == GeoStatus.fixed)
        {
            builder.append (" fixed");
        }
        builder.append ("\n");
    }

    /**
     * Define the value of this item for math eclipse. This should be overridden.
     *
     * @param eval The symbolic math context. Use the defineVariable method to define a boolean,
     *            double or IExpr binding the name of this item to some value.
     */
    public void defineVariable (ExprEvaluator eval)
    {
        eval.defineVariable (name);
    }

    /**
     * Get a math eclipse expression for the value of this item. This should be overridden.
     *
     * @return The item value.
     */
    public String getStringValue ()
    {
        return name;
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

    public void resetInferences ()
    {
        // inferences.clear ();
        isSolved = false;
    }

    public boolean isSolved ()
    {
        return isSolved;
    }

    public void deriveInferences ()
    {
        if (!isSolved)
        {
            isSolved = true;
            solve ();
            // Copy deep status to the item itself
            isDetermined ();
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
        result.put ("unknown", new Consumer<GeoItem> ()
        {
            @Override
            public void accept (GeoItem item)
            {
                setGivenStatus (GeoStatus.unknown);
                plane.setDirty ();
                plane.solve ();
                plane.fireChangeListeners (this);
            }
        });
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
        for (final Inference inference : inferences)
        {
            final Element element = doc.createElement (Inference.class.getSimpleName ());
            root.appendChild (element);
            inference.getAttributes (element);
        }
    }

    /** Convert to an element for saving to a file. */
    public void getAttributes (Element element)
    {
        element.setAttribute ("name", name);
        element.setAttribute ("color", String.valueOf (color.getRGB ()));
        element.setAttribute ("selected", String.valueOf (isSelected));
        element.setAttribute ("open", String.valueOf (isOpen));
        element.setAttribute ("status", String.valueOf (status));
    }

    /** Restore from the xml. */
    public void marshall (Element element)
    {
        setName (xu.get (element, "name", name));
        color = new Color (xu.getInteger (element, "color", 0));
        isSelected = xu.getBoolean (element, "selected", false);
        isOpen = xu.getBoolean (element, "open", false);
        status = GeoStatus.valueOf (xu.get (element, "status", "unknown"));
    }

    /** Find an item in the plane and restore it from the xml. */
    public void marshallReference (Element parentXml, String name)
    {
        final GeoItem item = getPlane ().get (name);
        if (item == null)
        {
            logger.warn ("Can't locate %s in %s", name, GeoItem.getNames (getPlane ().getItems ()));
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

        buffer.append (" inferences:");
        buffer.append (inferences.size ());
        buffer.append (">");
        return buffer.toString ();
    }
}
