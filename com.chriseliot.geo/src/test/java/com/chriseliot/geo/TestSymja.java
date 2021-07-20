
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.matheclipse.core.convert.Lists;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.expression.F;
import org.matheclipse.core.interfaces.*;

class TestSymja
{
    @Test
    void testCreate ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse ("solve({a==b,b==5}, {a, b})");
        final IExpr value = eval.eval (expr);
        assertNotNull (value);
    }

    @Test
    void testSolve ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse ("solve({a==b,b==5}, {a, b})");
        final IExpr value = eval.eval (expr);
        assertEquals ("{{a->5,b->5}}", value.toString ());
    }

    @Test
    void testEliminate ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse ("eliminate({a==b,b==5}, b)");
        final IExpr value = eval.eval (expr);
        assertEquals ("a==5", value.toString ());
    }

    @Test
    void testLookup ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse ("Lookup(solve({a==b,b==5}, {a, b}), a)[[1]]");
        final IExpr value = eval.eval (expr);
        assertEquals ("5", value.toString ());
    }

    @Test
    void testLookup2 ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse ("Extract(Lookup(solve({a==b,b==5}, {a, b}), a), 1)");
        final IExpr value = eval.eval (expr);
        assertEquals ("5", value.toString ());
    }

    @Test
    void testPart ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse ("Part(Lookup(solve({a==b,b==5}, {a, b}), a), 1)");
        final IExpr value = eval.eval (expr);
        assertEquals ("5", value.toString ());
    }

    @Test
    void testListGet ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse ("Lookup(solve({a==b,b==5}, {a, b}), a)");
        final IExpr value = eval.eval (expr);
        final IAST list = (IAST)value;
        final IExpr v = list.get (1);
        assertEquals ("5", v.toString ());
    }

    @Test
    void testListGet2 ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse ("solve({a==b,b==5}, {a, b})");
        final IExpr value = eval.eval (expr);
        final IAST result = (IAST)value;
        System.out.printf ("result: %s %s\n", result, result.size ());
        for (int i = 1; i < result.size (); i++)
        {
            final IExpr rule = result.get (i);
            System.out.printf ("elt(%d): %s \n", i, rule);
            // if (rule.isRuleAST ())
            {
                System.out.printf ("rule(%d): %s \n", i, rule);
                System.out.printf ("rule.get(1): %s \n", ((IAST)rule).get (1));
                System.out.printf ("rule.first(): %s \n", rule.first ());
                final IExpr item = rule.first ();
                System.out.printf ("item.first(): %s \n", item.first ());
                if (item.first ().toString ().equals ("a"))
                {
                    System.out.printf ("a == %s \n", item.second ());
                    assertEquals ("5", item.second ().toString ());
                }
            }
        }
        // final IExpr v = result.get (1);
        // assertEquals ("5", v.toString ());
    }

    @Test
    void testListGet3 ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse ("solve({a==b,b==5}, {a, b})");
        final IExpr value = eval.eval (expr);
        final IExpr lookup = eval.parse ("Lookup");
        final IExpr form = lookup.apply (value, eval.parse ("a"));
        final IExpr v = eval.eval (form);
        assertEquals ("5", v.first ().toString ());
    }

    @Test
    void testListGet4 ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final ISymbol a = eval.defineVariable ("a");
        final ISymbol b = eval.defineVariable ("b");
        final IExpr e1 = eval.parse ("a==b");
        final IExpr e2 = eval.parse ("b==5");
        final IAST expr = F.Solve (Lists.asList (e1, e2), Lists.asList (a, b));
        System.out.printf ("expr: %s \n", expr);
        final IExpr value = eval.eval (expr);
        System.out.printf ("value: %s \n", value);
        // final IExpr lookup = eval.parse ("Lookup");
        // final IExpr lookup = F.Lookup;
        // final IExpr form = lookup.apply (value, a);
        final IExpr form = F.Lookup.apply (value, a);
        final IExpr v = eval.eval (form);
        assertEquals ("5", v.first ().toString ());
    }

    @Test
    void testSolveLength ()
    {
        final ExprEvaluator eval = new ExprEvaluator ();
        final IExpr expr = eval.parse ("solve({l==sqrt(dx^2+dy^2),dx==5,dy==6}, {l, dx, dy})");
        final IExpr value = eval.eval (expr);
        final IAST result = (IAST)value;
        System.out.printf ("result: %s %s\n", result, result.size ());
    }
}
