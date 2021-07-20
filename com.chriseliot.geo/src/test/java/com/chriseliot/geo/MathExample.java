
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.Test;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.*;

public class MathExample
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    // /**
    // * Demo of the symja library for symbolic math in Java.
    // *
    // * This package:
    // *
    // * @see https://github.com/axkr/symja_android_library
    // *
    // * There is another package. It requires a specialized JVM installation.
    // * @see https://github.com/yuemingl/SymJava
    // *
    // * @param args
    // */
    // public static void main (String[] args)
    // {
    // final MathExample test = new MathExample ();
    // test.demo (false);
    // }

    @Test
    public void testCreate ()
    {
        final ExprEvaluator util = new ExprEvaluator (true, (short)100);
        assertNotNull (util);
    }

    /** That multiplication by zero can be simplified */
    @Test
    public void testSimplify1 ()
    {
        final ExprEvaluator util = new ExprEvaluator (true, (short)100);
        final IExpr expr = util.parse ("(x + 5) * 0");
        logger.info ("Eval: %s => %s", expr.toString (), util.eval (expr));
    }

    /** That multiplication by zero can be simplified */
    @Test
    public void testSimplify2 ()
    {
        final ExprEvaluator util = new ExprEvaluator (true, (short)100);
        final IExpr expr = util.parse ("(x + 5) * (a - a)");
        logger.info ("Eval: %s => %s", expr.toString (), util.eval (expr));
    }

    /** That multiplication by one can be simplified */
    @Test
    public void testSimplify3 ()
    {
        final ExprEvaluator util = new ExprEvaluator (true, (short)100);
        final IExpr expr = util.parse ("(x + 5) * cos(0)");
        logger.info ("Eval: %s => %s", expr.toString (), util.eval (expr));
    }

    /** That multiplication by zero can be simplified */
    @Test
    public void testSimplify4 ()
    {
        final ExprEvaluator util = new ExprEvaluator (true, (short)100);
        final IExpr expr = util.parse ("(x + 5) * sin(0)");
        logger.info ("Eval: %s => %s", expr.toString (), util.eval (expr));
    }

    @Test
    public void testDemo ()
    {
        final MathExample test = new MathExample ();
        test.demo (true);
    }

    public void demo (boolean output)
    {
        final ExprEvaluator util = new ExprEvaluator (output, (short)100);

        // Convert an expression to the internal Java form:
        // Note: single character identifiers are case sensitive
        // (the "D()" function identifier must be written as upper case
        // character)
        final String javaForm = util.toJavaForm ("D(sin(x)*cos(x),x)");
        // prints: D(Times(Sin(x),Cos(x)),x)
        System.out.println ("Out[1]: " + javaForm.toString ());

        // Use the Java form to create an expression with F.* static
        // methods:
        final ISymbol x = F.Dummy ("x");
        IAST function = F.D (F.Times (F.Sin (x), F.Cos (x)), x);
        IExpr result = util.eval (function);
        // print: Cos(x)^2-Sin(x)^2
        System.out.println ("Out[2]: " + result.toString ());

        // Note "diff" is an alias for the "D" function
        result = util.eval ("diff(sin(x)*cos(x),x)");
        // print: Cos(x)^2-Sin(x)^2
        System.out.println ("Out[3]: " + result.toString ());

        // evaluate the last result (% contains "last answer")
        result = util.eval ("%+cos(x)^2");
        // print: 2*Cos(x)^2-Sin(x)^2
        System.out.println ("Out[4]: " + result.toString ());

        // evaluate an Integrate[] expression
        result = util.eval ("integrate(sin(x)^5,x)");
        // print: 2/3*Cos(x)^3-1/5*Cos(x)^5-Cos(x)
        System.out.println ("Out[5]: " + result.toString ());

        // set the value of a variable "a" to 10
        result = util.eval ("a=10");
        // print: 10
        System.out.println ("Out[6]: " + result.toString ());

        // do a calculation with variable "a"
        result = util.eval ("a*3+b");
        // print: 30+b
        System.out.println ("Out[7]: " + result.toString ());

        // Do a calculation in "numeric mode" with the N() function
        // Note: single character identifiers are case sensistive
        // (the "N()" function identifier must be written as upper case
        // character)
        result = util.eval ("N(sinh(5))");
        // print: 74.20321057778875
        System.out.println ("Out[8]: " + result.toString ());

        // define a function with a recursive factorial function definition.
        // Note: fac(0) is the stop condition.
        result = util.eval ("fac(x_Integer):=x*fac(x-1);fac(0)=1");
        // now calculate factorial of 10:
        result = util.eval ("fac(10)");
        // print: 3628800
        System.out.println ("Out[9]: " + result.toString ());

        function = F.Function (F.Divide (F.Gamma (F.Plus (F.C1, F.Slot1)), F.Gamma (F.Plus (F.C1, F.Slot2))));
        // eval function ( Gamma(1+#1)/Gamma(1+#2) ) & [23,20]
        result = util.evalFunction (function, "23", "20");
        // print: 10626
        System.out.println ("Out[10]: " + result.toString ());
    }
}
