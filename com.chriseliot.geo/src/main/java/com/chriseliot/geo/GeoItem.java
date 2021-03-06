
package com.chriseliot.geo;

import java.awt.*;
import java.awt.geom.Point2D;
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

    /** The current status of this item */
    private GeoStatus status = GeoStatus.unknown;

    /** The reason this item was given this status. */
    private String reason = null;

    /**
     * Represents one derivation of the value of this item.
     */
    private final List<Inference> inferences = new ArrayList<> ();

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

    public static String[] getNames (GeoItem[] items)
    {
        final String[] result = new String[items.length];
        if (items != null)
        {
            for (int i = 0; i < items.length; i++)
            {
                result[i] = items[i].getName ();
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
        setStatus (GeoStatus.unknown, "given");
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

    /** Represents one derivation step. */
    public Inference getInference ()
    {
        for (final Inference inference : inferences)
        {
            if (inference.isDetermined ())
            {
                return inference;
            }
        }
        return null;
    }

    /**
     * Shorthand function to set the formula and save the names of the variables involved.
     *
     * @param reason A string giving the reason for this derivation.
     * @param formulaExpression A Java format template to create the derivation formula.
     * @param variables Variables to substitute into the template string to create the derivation
     *            formula.
     */
    public void setFormula (String reason, String formulaExpression, GeoItem... variables)
    {
        final GeoItem[] terms = new GeoItem[variables.length];
        System.arraycopy (variables, 0, terms, 0, variables.length);
        Inference.verifyInference (reason, formulaExpression, terms);
        final GeoItem owner = terms[0];
        if (!owner.isDetermined ())
        {
            for (int i = 1; i < terms.length; i++)
            {
                if (!terms[i].isDetermined ())
                {
                    // Can't make this inference at this time.
                    return;
                }
            }
            inferences.add (new Inference (this, reason, formulaExpression, terms));
            setStatus (GeoStatus.derived, reason);
        }
    }

    /**
     * Solve the formula in terms of given knowns to determine the value of this variable.
     *
     * 1. Perform a recursive search to find the root source of the derivation.
     *
     * 2. Use the Symja solver to eliminate all of the intermediate variables from the derivation.
     *
     * This is shown by the "Show Solution" popup menu item.
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

    /**
     * Build a derivation string for a show derivation action.
     *
     * @param builder The result is built here.
     */
    public void getDerivation (StringBuilder builder)
    {
        final Set<GeoItem> closed = new HashSet<> ();
        getDerivation (builder, 0, closed);
    }

    /**
     * Build a derivation string for a show derivation action. The derivation of the nested terms is
     * shown in a post-order traversal, so the most primitive terms appear first.
     *
     * @param builder The result is built here.
     * @param level Current indentation level.
     * @param closed Items already visited that can terminate search.
     */
    private void getDerivation (StringBuilder builder, int level, Set<GeoItem> closed)
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

    public void getFormulaLine (StringBuilder builder)
    {
        getFormulaLine (builder, 0);
    }

    /**
     * Display one line of an overall derivation.
     *
     * @param builder The result is built here.
     * @param level Current indentation level.
     */
    private void getFormulaLine (StringBuilder builder, int level)
    {
        builder.append (getFormulaLine (level));
        builder.append ("\n");
    }

    private String getFormulaLine (int level)
    {
        final StringBuilder builder = new StringBuilder ();
        for (int i = 0; i < level; i++)
        {
            builder.append ("|  ");
        }
        final Inference inference = getInference ();
        if (inference == null)
        {
            builder.append (String.format ("%s == %s", getName (), getStringValue ()));

            if (getStatus () == GeoStatus.known)
            {
                tab (builder, 45);
                builder.append (" given");
            }
            else if (getStatus () == GeoStatus.fixed)
            {
                tab (builder, 45);
                builder.append (" fixed");
            }
        }
        else
        {
            final String formula = inference.getInstantiation ();
            builder.append (formula);
            tab (builder, 45);
            builder.append (" ");
            builder.append (inference.getReason ());
        }
        return builder.toString ();
    }

    private void tab (StringBuilder builder, int column)
    {
        while (builder.length () < column)
        {
            builder.append (' ');
        }
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
