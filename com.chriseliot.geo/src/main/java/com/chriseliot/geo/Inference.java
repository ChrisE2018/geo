
package com.chriseliot.geo;

import static java.lang.Math.abs;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.apache.logging.log4j.*;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;
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
        assertEquals (owner, terms[0]);
    }

    public static boolean verifyInference (String reason, String formula, GeoItem[] terms)
    {
        final GeoItem owner = terms[0];
        if (owner instanceof NamedVariable)
        {
            final NamedVariable variable = (NamedVariable)owner;
            if (variable.getDoubleValue () != null)
            {
                // Check the value
                final String expression = getInstantiation (formula, terms);
                assertNotNull (expression);
                final ExprEvaluator eval = new ExprEvaluator ();
                final IExpr expr = eval.parse (expression);
                final IExpr rhs = expr.getAt (2);
                final StringBuilder builder = new StringBuilder ();
                builder.append (String.format ("'%s' Inference: %s; rhs: %s", reason, expr, rhs));
                for (final GeoItem term : terms)
                {
                    builder.append ("; ");
                    builder.append (term.getName ());
                    builder.append (" = ");
                    builder.append (term.getStringValue ());
                }
                logger.debug (builder.toString ());
                for (final GeoItem term : terms)
                {
                    term.defineVariable (eval);
                }
                final double expected = ((NamedVariable)owner).getDoubleValue ();
                final double actual = eval.evalf (rhs);
                final double epsilon = 0.0001;
                if (abs (actual - expected) > epsilon)
                {
                    logger.error ("Var %s expected %.2f != %.2f FAIL", owner.getName (), expected, actual);
                }
                return (expected - actual) < epsilon;
            }
        }
        return true;
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

    public List<GeoItem> getTermList ()
    {
        final List<GeoItem> result = new ArrayList<> ();
        for (final GeoItem item : terms)
        {
            result.add (item);
        }
        return result;
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

    public static String getInstantiation (String formula, GeoItem[] terms)
    {
        final Object[] names = GeoItem.getNames (terms);
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
        buffer.append (" '");
        buffer.append (reason);
        buffer.append ("' {");
        buffer.append (getInstantiation ());
        buffer.append ("}>");
        return buffer.toString ();
    }
}
