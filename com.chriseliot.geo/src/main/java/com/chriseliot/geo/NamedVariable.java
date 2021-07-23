
package com.chriseliot.geo;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.logging.log4j.*;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

import com.chriseliot.util.*;

/** A variable representing a single real value. */
public class NamedVariable extends GeoItem
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());
    private final TextUtils tu = new TextUtils ();

    /**
     * Current value determined from the screen positions. This may be more specific than can be
     * determined from the geometry. In other words, this will be a specific number while the
     * geometry allows for some range of values.
     */
    private Double value;

    /**
     * Formula for the true value. If this term has not been constrained, this will be null.
     * Otherwise it relates the variables in terms to the value of this variable. To make the actual
     * formula requires instantiating this expression with the names of the terms.
     */
    private String formulaExpression;

    /**
     * A list of variables used in the formula. Will be null if the formula has not been set. When
     * the formula is set, the free variables are determined. The set of free variables are saved
     * here as terms. To compute a numerical value for this variable requires knowing the exact
     * values of all terms. These values can be plugged into the formula to compute a value of this
     * variable.
     */
    private NamedVariable[] terms = null;

    /** Location on screen to draw label for this variable. */
    private NamedPoint location;

    /**
     * Location on screen to draw label for this variable. If location2 is not null, draw the label
     * half way between location and location2.
     */
    private NamedPoint location2;

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

    /**
     * Formula for the true value. If this term has not been constrained, this will be null.
     * Otherwise it relates the variables in terms to the value of this variable.
     */
    public String getFormulaExpression ()
    {
        return formulaExpression;
    }

    /**
     * A list of variables used in the formula. Will be null if the formula has not been set. When
     * the formula is set, the free variables are determined. The set of free variables are saved
     * here as terms. To compute a numerical value for this variable requires knowing the exact
     * values of all terms. These values can be plugged into the formula to compute a value of this
     * variable.
     */
    public NamedVariable[] getTerms ()
    {
        return terms;
    }

    public String[] getTermNames ()
    {
        final String[] names = new String[terms.length];
        for (int i = 0; i < terms.length; i++)
        {
            names[i] = terms[i].getName ();
        }
        return names;
    }

    public String getFormulaInstance ()
    {
        if (formulaExpression == null)
        {
            return null;
        }
        final Object[] names = getTermNames ();
        return String.format (formulaExpression, names);
    }

    /** Set the status to unknown. Reset the formula and terms to the corrct default state. */
    @Override
    public void setDefaultFormula ()
    {
        super.setDefaultFormula ();
        formulaExpression = null;
        terms = new NamedVariable[] {};
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
        this.formulaExpression = formulaExpression;
        this.terms = terms;
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
    private void getDerivationChain (Set<String> chain, Set<String> roots)
    {
        for (final NamedVariable ti : terms)
        {
            final String f = ti.getFormulaInstance ();
            roots.add (ti.getName ());
            if (f != null)
            {
                chain.add (f);
                if (ti != this)
                {
                    ti.getDerivationChain (chain, roots);
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
        location2 = null;
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
            if (formulaExpression == null)
            {
                tooltip = text;
            }
            else
            {
                tooltip = String.format ("%.1f = %s", value, getFormulaInstance ());
            }
            final int anchor = location.getAnchor ();
            labels.add (this, color, position, anchor, text, tooltip);
        }
    }

    /** Should a popup menu on this item include a show solution item. */
    @Override
    public boolean canShowSolution ()
    {
        return getStatus () == GeoStatus.derived;
    }

    /** Action to perform for a show derivation action. */
    @Override
    public void showSolutionAction ()
    {
        final String rawFormula = getFormulaInstance ();
        final String derivedFormula = getDerivedFormula ();
        final String message = String.format ("Raw: %s\nDerived: %s", rawFormula, derivedFormula);
        JOptionPane.showMessageDialog (null, message, "Derivation", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Should a popup menu on this item include a show derivation item. */
    @Override
    public boolean canShowDerivation ()
    {
        return getStatus () == GeoStatus.derived;
    }

    /** Action to perform for a show derivation action. */
    @Override
    public void showDerivationAction ()
    {
        final StringBuilder builder = new StringBuilder ();
        final String formula = getFormulaInstance ();
        builder.append (String.format ("Derivation: %s\n\n", formula));
        getDerivation (builder, 0);
        getFormulaLine (builder, 0);
        JOptionPane.showMessageDialog (null, builder.toString (), "Derivation", JOptionPane.INFORMATION_MESSAGE);
    }

    public void getDerivation (StringBuilder builder, int level)
    {
        for (int i = 1; i < terms.length; i++)
        {
            final NamedVariable ti = terms[i];
            ti.getDerivation (builder, level + 1);
            ti.getFormulaLine (builder, level);
        }
    }

    private void getFormulaLine (StringBuilder builder, int level)
    {
        for (int i = 0; i < level; i++)
        {
            builder.append ("|  ");
        }
        final String formula = getFormulaInstance ();
        if (formula == null)
        {
            builder.append (String.format ("%s == %.3f", getName (), getDoubleValue ()));
        }
        else
        {
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

    /** Should a popup menu on this item include a set value item. */
    @Override
    public boolean canSetValue ()
    {
        final GeoItem parent = getParent ();
        if (parent instanceof NamedPoint)
        {
            final NamedPoint p = (NamedPoint)parent;
            return this == p.getX () || this == p.getY ();
        }
        return false;
    }

    /** Action to perform for a set value action. */
    @Override
    public void setValueAction ()
    {
        final String message = (value == null) ? String.format ("Enter new value for %s", getName ())
                                               : String.format ("Enter new value for %s (%s)", getName (), value);
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

    /** Can this item support a rename option. */
    @Override
    public boolean canRenameVariable ()
    {
        return true;
    }

    /** Implement the rename option. */
    @Override
    public void renameVariableAction ()
    {
        final String message = String.format ("Enter new name for %s", getName ());
        final String result = JOptionPane.showInputDialog (null, message, "Input Name", JOptionPane.QUESTION_MESSAGE);
        if (result != null)
        {
            renameVariableAction (result);
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

    /**
     * Get named attributes. Used for saving to a csv file. This method should be overriden by
     * subclasses.
     *
     * @param result Map to store attributes.
     */
    @Override
    public void getAttributes (Map<String, Object> result)
    {
        result.put ("value", value);
        result.put ("formula", formulaExpression);
        result.put ("terms", tu.join ("+", getTermNames ()));
        result.put ("location", location == null ? "" : location.getName ());
        result.put ("location2", location2 == null ? "" : location2.getName ());
    }

    @Override
    public void readAttributes (Map<String, String> attributes)
    {
        super.readAttributes (attributes);
        value = Double.parseDouble (attributes.get ("value"));
        formulaExpression = attributes.get ("formula");
        final String termsAttribute = attributes.get ("terms");
        final GeoPlane plane = getPlane ();
        final List<String> termList = tu.split (termsAttribute, "+");
        terms = new NamedVariable[termList.size ()];
        for (int i = 0; i < terms.length; i++)
        {
            final String name = termList.get (i);
            terms[i] = (NamedVariable)plane.get (name);
        }
        final String locationName = attributes.get ("location");
        final String location2Name = attributes.get ("location2");
        if (!locationName.isEmpty ())
        {
            location = (NamedPoint)getPlane ().get (locationName);
        }
        if (!location2Name.isEmpty ())
        {
            location2 = (NamedPoint)getPlane ().get (location2Name);
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
        final String formula = getFormulaInstance ();
        if (formula != null)
        {
            buffer.append (" ");
            buffer.append ("{");
            buffer.append (formula);
            buffer.append ("}");
        }
        buffer.append (">");
        return buffer.toString ();
    }
}
