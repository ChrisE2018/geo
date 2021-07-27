
package com.chriseliot.geo;

import java.util.List;

import org.w3c.dom.Element;

import com.chriseliot.util.*;

public class Inference
{
    private static XMLUtil xu = new XMLUtil ();
    private static TextUtils tu = new TextUtils ();

    /** The variable that will be derived by this inference. */
    private final GeoItem owner;

    /**
     * Formula for the true value. This relates the variables in terms to the value of the owner
     * variable. To make the actual formula requires instantiating this expression with the names of
     * the terms.
     */
    private String formula;

    /**
     * A list of variables used in the formula. The set of free variables of the formulaExpression
     * are saved here as terms. To compute a numerical value for this variable requires knowing the
     * exact values of all terms. These values can be plugged into the formula to compute a value of
     * this variable.
     */
    private GeoItem[] terms;

    public Inference (GeoItem owner, String formula, GeoItem[] terms)
    {
        this.owner = owner;
        this.formula = formula;
        this.terms = terms;
        assert (owner.equals (terms[0]));
    }

    /** The variable that will be derived by this inference. */
    public GeoItem getOwner ()
    {
        return owner;
    }

    /**
     * Formula for the true value. This relates the variables in terms to the value of the owner
     * variable. To make the actual formula requires instantiating this expression with the names of
     * the terms.
     */
    public String getFormula ()
    {
        return formula;
    }

    /**
     * A list of variables used in the formula. The set of free variables of the formulaExpression
     * are saved here as terms. To compute a numerical value for this variable requires knowing the
     * exact values of all terms. These values can be plugged into the formula to compute a value of
     * this variable.
     */
    public GeoItem[] getTerms ()
    {
        return terms;
    }

    /** The names of the free variables. */
    public String[] getTermNames ()
    {
        final String[] names = new String[terms.length];
        for (int i = 0; i < terms.length; i++)
        {
            names[i] = terms[i].getName ();
        }
        return names;
    }

    public boolean isDetermined ()
    {
        for (final GeoItem term : terms)
        {
            if (!term.isDetermined ())
            {
                return false;
            }
        }
        return true;
    }

    /**
     * The instantiated formula with variable names replacing the corresponding format variables.
     */
    public String getInstantiation ()
    {
        final Object[] names = getTermNames ();
        return String.format (formula, names);
    }

    public void getAttributes (Element element)
    {
        element.setAttribute ("formula", String.valueOf (getFormula ()));
        element.setAttribute ("terms", tu.join ("+", getTermNames ()));
    }

    public void marshall (Element element)
    {
        final GeoPlane plane = owner.getPlane ();
        formula = xu.get (element, "formula", null);

        final List<String> termList = tu.split (xu.get (element, "terms", ""), "+");
        terms = new NamedVariable[termList.size ()];
        for (int i = 0; i < terms.length; i++)
        {
            final String name = termList.get (i);
            terms[i] = plane.get (name);
        }
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        buffer.append (" ");
        buffer.append (getInstantiation ());
        buffer.append (">");
        return buffer.toString ();
    }
}
