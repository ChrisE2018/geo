
package com.chriseliot.geo;

public class Inference
{
    /** The variable that will be derived by this inference. */
    private final NamedVariable owner;

    /**
     * Formula for the true value. This relates the variables in terms to the value of the owner
     * variable. To make the actual formula requires instantiating this expression with the names of
     * the terms.
     */
    private final String formula;

    /**
     * A list of variables used in the formula. The set of free variables of the formulaExpression
     * are saved here as terms. To compute a numerical value for this variable requires knowing the
     * exact values of all terms. These values can be plugged into the formula to compute a value of
     * this variable.
     */
    private final NamedVariable[] terms;

    public Inference (NamedVariable owner, String formula, NamedVariable[] terms)
    {
        this.owner = owner;
        this.formula = formula;
        this.terms = terms;
        assert (owner.equals (terms[0]));
    }

    /** The variable that will be derived by this inference. */
    public NamedVariable getOwner ()
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
    public NamedVariable[] getTerms ()
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

    /**
     * The instantiated formula with variable names replacing the corresponding format variables.
     */
    public String getInstantiation ()
    {
        final Object[] names = getTermNames ();
        return String.format (formula, names);
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
