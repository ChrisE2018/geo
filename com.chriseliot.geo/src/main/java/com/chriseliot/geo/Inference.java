
package com.chriseliot.geo;

import java.util.*;

import org.apache.logging.log4j.*;
import org.w3c.dom.Element;

import com.chriseliot.util.*;

public class Inference
{
    private static Logger logger = LogManager.getFormatterLogger (Inference.class);

    private static XMLUtil xu = new XMLUtil ();
    private static TextUtils tu = new TextUtils ();

    /** The variable that will be derived by this inference. */
    private final GeoItem owner;
    /**
     * The reason this item was given this status. This should be associated with an Inference
     */
    private String reason;

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

    public Inference (GeoItem owner, String reason, String formula, GeoItem[] terms)
    {
        this.owner = owner;
        this.reason = reason;
        this.formula = formula;
        this.terms = terms;
        assert (owner.equals (terms[0]));
    }

    /** The variable that will be derived by this inference. */
    public GeoItem getOwner ()
    {
        return owner;
    }

    /** The reason this item was given this status. */
    public String getReason ()
    {
        return reason;
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

    public boolean hasTerm (GeoItem term)
    {
        for (final GeoItem t : terms)
        {
            if (t.equals (term))
            {
                return true;
            }
        }
        return false;
    }

    public boolean isDetermined (Set<GeoItem> known, Set<GeoItem> closed)
    {
        for (int i = 1; i < terms.length; i++)
        {
            final GeoItem term = terms[i];
            if (!term.isDetermined (known, closed))
            {
                return false;
            }
        }
        return true;
    }

    public boolean whyDetermined (Set<GeoItem> known, Set<GeoItem> closed)
    {
        final Set<GeoItem> savedKnown = new HashSet<> (known);
        final Set<GeoItem> savedClosed = new HashSet<> (closed);
        for (int i = 1; i < terms.length; i++)
        {
            final GeoItem term = terms[i];
            if (!term.isDetermined (known, closed))
            {
                final Set<GeoItem> missing = new HashSet<> (closed);
                missing.removeAll (known);
                logger.info ("%s is not determined because %s it not determined without %s", this, term.getName (),
                        GeoItem.getNames (missing));
                final Set<GeoItem> supportRequired = term.getSupport ();
                final Set<GeoItem> supportMissing = new HashSet<> (supportRequired);
                supportMissing.removeAll (known);
                logger.info ("%s needs support %s which is missing %s", term.getName (), GeoItem.getNames (supportRequired),
                        GeoItem.getNames (supportMissing));

                return false;
            }
        }
        logger.info ("%s is determined because %s terms are determined", this, terms.length - 1);
        for (int i = 1; i < terms.length; i++)
        {
            final GeoItem term = terms[i];
            term.whyDetermined (savedKnown, savedClosed);
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
        element.setAttribute ("reason", reason);
        element.setAttribute ("formula", formula);
        element.setAttribute ("terms", tu.join ("+", getTermNames ()));
    }

    public void marshall (Element element)
    {
        final GeoPlane plane = owner.getPlane ();
        reason = xu.get (element, "reason", null);
        formula = xu.get (element, "formula", null);

        final List<String> termList = tu.split (xu.get (element, "terms", ""), "+");
        terms = new NamedVariable[termList.size ()];
        for (int i = 0; i < terms.length; i++)
        {
            final String name = termList.get (i);
            terms[i] = plane.get (name);
        }
    }

    public boolean matches (String reason, String formula, GeoItem[] terms)
    {
        if (getReason ().equals (reason))
        {
            if (this.formula.equals (formula))
            {
                if (this.terms.length == terms.length)
                {
                    for (final GeoItem item : terms)
                    {
                        if (!hasTerm (item))
                        {
                            return false;
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        buffer.append (" ");
        buffer.append (reason);
        buffer.append (" ");
        buffer.append (getInstantiation ());
        buffer.append (">");
        return buffer.toString ();
    }
}
