
package com.chriseliot.geo;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.function.Consumer;

import org.apache.logging.log4j.*;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
import org.w3c.dom.Element;

import com.chriseliot.geo.gui.NamedVariableActions;
import com.chriseliot.util.*;

/** A variable representing a single real value. */
public class NamedVariable extends GeoItem
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());
    private static TextUtils tu = new TextUtils ();

    /**
     * Current value determined from the screen positions. This may be more specific than can be
     * determined from the geometry. In other words, this will be a specific number while the
     * geometry allows for some range of values.
     */
    private Double value;

    /** Represents one derivation step. */
    private Inference inference = null;

    /** Location on screen to draw label for this variable. */
    private NamedPoint location;

    /** A variable representing a single real value. */
    public NamedVariable (GeoItem parent, Color color, String name, Double value)
    {
        super (parent, name, color);
        this.value = value;
        if (parent instanceof NamedPoint)
        {
            location = (NamedPoint)parent;
        }
        else
        {
            location = null;
        }
        setDefaultFormula ();
    }

    /** A variable representing a single real value. */
    public NamedVariable (GeoItem parent, Color color, String name)
    {
        super (parent, name, color);
        value = null;
        if (parent instanceof NamedPoint)
        {
            location = (NamedPoint)parent;
        }
        else
        {
            location = null;
        }
        setDefaultFormula ();
    }

    /**
     * Current value determined from the screen positions. This may be more specific than can be
     * determined from the geometry. In other words, this will be a specific number while the
     * geometry allows for some range of values.
     */
    public Double getDoubleValue ()
    {
        return value;
    }

    /**
     * Current value determined from the screen positions. This may be more specific than can be
     * determined from the geometry. In other words, this will be a specific number while the
     * geometry allows for some range of values.
     */
    public void setDoubleValue (Double value)
    {
        this.value = value;
    }

    /** Represents one derivation step. */
    public Inference getInference ()
    {
        return inference;
    }

    /** Set the status to unknown. Reset the formula and terms to the correct default state. */
    @Override
    public void setDefaultFormula ()
    {
        super.setDefaultFormula ();
        inference = null;
    }

    /**
     * Shorthand function to set the formula and save the names of the variables involved.
     *
     * @param reason A string giving the reason for this derivation.
     * @param formulaExpression A Java format template to create the derivation formula.
     * @param variables Variables to substitute into the template string to create the derivation
     *            formula.
     */
    public void setFormula (String reason, String formulaExpression, NamedVariable... variables)
    {
        final NamedVariable[] terms = new NamedVariable[variables.length];
        System.arraycopy (variables, 0, terms, 0, variables.length);
        inference = new Inference (this, formulaExpression, terms);
        setStatus (GeoStatus.derived, reason);
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
    public void getDerivationChain (Set<String> chain, Set<String> roots)
    {
        for (final NamedVariable term : inference.getTerms ())
        {
            roots.add (term.getName ());
            if (term.inference != null)
            {
                final String termFormula = term.inference.getInstantiation ();
                chain.add (termFormula);
                if (term != this)
                {
                    term.getDerivationChain (chain, roots);
                }
            }
        }
    }

    /** Derive consequential formulas around this variable. */
    @Override
    public void solve ()
    {
        if (getParent () instanceof NamedPoint)
        {
            final NamedPoint parent = (NamedPoint)getParent ();
            for (final GeoItem item : getPlane ().getItems ())
            {
                if (item instanceof NamedPoint && item != parent)
                {
                    final NamedPoint p = (NamedPoint)item;
                    if (parent.at (p))
                    {
                        parent.equivalent (p);
                    }
                }
            }
        }
    }

    /** Location on screen to draw label for this variable. */
    public NamedPoint getLocation ()
    {
        return location;
    }

    /** Location on screen to draw label for this variable. */
    public void setLocation (NamedPoint location)
    {
        this.location = location;
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
        final NamedPoint location = getLocation ();
        if (location != null)
        {
            final Point position = location.getIntPosition ();
            final Color color = getStatus ().getColor ();
            final String name = getName ();
            String text = name;
            if (value != null)
            {
                text = String.format ("%s = %.1f", getName (), value);
            }
            final String tooltip;
            if (inference == null)
            {
                tooltip = text;
            }
            else
            {
                tooltip = String.format ("%.1f = %s", value, inference.getInstantiation ());
            }
            final int anchor = location.getAnchor ();
            labels.add (this, color, position, anchor, text, tooltip);
        }
    }

    public void getDerivation (StringBuilder builder, int level)
    {
        if (inference != null)
        {
            final NamedVariable[] terms = inference.getTerms ();
            for (int i = 1; i < terms.length; i++)
            {
                final NamedVariable ti = terms[i];
                ti.getDerivation (builder, level + 1);
                ti.getFormulaLine (builder, level);
            }
        }
    }

    public void getFormulaLine (StringBuilder builder, int level)
    {
        for (int i = 0; i < level; i++)
        {
            builder.append ("|  ");
        }
        if (inference == null)
        {
            builder.append (String.format ("%s == %.3f", getName (), getDoubleValue ()));
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
     * Populate a popup menu with required items. This should be overridden by subclasses. Be sure
     * to call the super method.
     *
     * @param result
     */
    @Override
    public void popup (Map<String, Consumer<GeoItem>> result)
    {
        super.popup (result);
        final NamedVariableActions actions = new NamedVariableActions ();
        final GeoItem parent = getParent ();
        if (parent instanceof NamedPoint)
        {
            final NamedPoint p = (NamedPoint)parent;
            if (this == p.getX () || this == p.getY ())
            {
                result.put ("Set Value", item -> actions.setValueAction (this));
            }
        }
        result.put ("Rename Variable", item -> actions.renameVariableAction (this));
        if (getStatus () == GeoStatus.derived)
        {
            result.put ("Show Solution", item -> actions.showSolutionAction (this));
            result.put ("Show Derivation", item -> actions.showDerivationAction (this));
        }
    }

    /**
     * Action to take when the dialog returns. This only works for variables that are x or y of a
     * named point. If calls the point to make the adjustment.
     */
    public void setValueAction (double result)
    {
        final GeoItem parent = getParent ();
        if (parent instanceof NamedPoint)
        {
            final NamedPoint p = (NamedPoint)parent;
            if (this == p.getX ())
            {
                p.setValueAction (new Point2D.Double (result, p.getY ().getDoubleValue ()));
                setGivenStatus (GeoStatus.fixed);
            }
            else
            {
                p.setValueAction (new Point2D.Double (p.getX ().getDoubleValue (), result));
                setGivenStatus (GeoStatus.fixed);
            }
        }
    }

    /** Implement the rename option. */
    public void renameVariableAction (String name)
    {
        logger.info ("Rename from %s to %s", getName (), name);
        final GeoPlane plane = getPlane ();
        if (plane.get (name) == null)
        {
            plane.remove (this);
            setName (name);
            plane.addItem (this);
            plane.fireChangeListeners (this);
        }
    }

    @Override
    public void getAttributes (Element element)
    {
        super.getAttributes (element);
        if (value != null)
        {
            element.setAttribute ("value", String.valueOf (value));
        }
        if (inference != null)
        {
            element.setAttribute ("formula", String.valueOf (inference.getFormula ()));
        }
        if (inference == null)
        {
            element.setAttribute ("terms", "");
        }
        else
        {
            element.setAttribute ("terms", tu.join ("+", inference.getTermNames ()));
        }
        if (location != null)
        {
            element.setAttribute ("location", String.valueOf (location.getName ()));
        }
    }

    @Override
    public void marshall (Element element)
    {
        super.marshall (element);
        final GeoPlane plane = getPlane ();
        value = xu.getDouble (element, "value", null);
        final String formulaExpression = xu.get (element, "formula", null);
        if (formulaExpression != null)
        {
            final List<String> termList = tu.split (xu.get (element, "terms", ""), "+");
            final NamedVariable[] terms = new NamedVariable[termList.size ()];
            for (int i = 0; i < terms.length; i++)
            {
                final String name = termList.get (i);
                // This might have to be deferred until the end of the read
                terms[i] = (NamedVariable)plane.get (name);
            }
            inference = new Inference (this, formulaExpression, terms);
        }

        final String locationName = xu.get (element, "location", null);
        if (locationName != null)
        {
            if (location == null)
            {
                // This might have to be deferred until the end of the read
                location = (NamedPoint)plane.get (locationName);
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
        buffer.append (getName ());
        if (value != null)
        {
            buffer.append (" ");
            buffer.append (String.format ("%.3f", value));
        }
        buffer.append (" ");
        buffer.append (getStatus ());
        final String reason = getReason ();
        if (reason != null)
        {
            buffer.append (" ");
            buffer.append ("'");
            buffer.append (getReason ());
            buffer.append ("'");
        }
        if (inference != null)
        {
            buffer.append (" ");
            buffer.append ("{");
            buffer.append (inference.getInstantiation ());
            buffer.append ("}");
        }
        buffer.append (">");
        return buffer.toString ();
    }
}
