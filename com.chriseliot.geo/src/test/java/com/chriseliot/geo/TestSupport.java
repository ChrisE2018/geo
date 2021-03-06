
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.imageio.ImageIO;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.Test;
import org.matheclipse.core.eval.ExprEvaluator;
import org.matheclipse.core.interfaces.IExpr;

public class TestSupport
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    public static final double epsilon = 0.0001;

    @Test
    public void testCreate ()
    {
        assertNotNull (toString ());
    }

    @Test
    public void testGetPngFile ()
    {
        assertNotNull (getTestPngFile (this, "test"));
    }

    public void checkExpression (NamedVariable named, double expected)
    {
        checkExpression (named, expected, null);
    }

    public void checkExpression (NamedVariable named, double expected, String trace)
    {
        final Inference inference = named.getInference ();
        if (inference != null)
        {
            final String formula = inference.getInstantiation ();
            if (trace != null)
            {
                logger.info ("[%s] checkExpression testing if %s == %.2f", trace, named.getName (), expected);
            }
            final GeoItem[] terms = inference.getTerms ();

            final ExprEvaluator eval = new ExprEvaluator ();
            for (int i = 1; i < terms.length; i++)
            {
                final GeoItem var = terms[i];
                assertNotNull (var);
                var.defineVariable (eval);
                if (trace != null)
                {
                    logger.info ("[%s] Define %s == %s", trace, var.getName (), eval.eval (var.getName ()));
                }
            }
            final IExpr expr = eval.parse (formula);
            assertNotEquals ("true", expr.toString ());
            final IExpr rhs = expr.getAt (2);
            final double value = eval.evalf (rhs);
            if (trace != null)
            {
                logger.info ("[%s] Formula: %s", trace, formula);
                logger.info ("[%s] RHS '%s'", trace, rhs);
                logger.info ("[%s] expected %.2f = %.2f actual", trace, expected, value);
                // Can't do this and get full coverage.
                // final String grade = abs (value - expected) < epsilon ? "PASS" : "FAIL";
                // logger.info ("[%s] expected %.2f = %.2f actual %s", trace, expected, value,
                // grade);
                logger.info ("");
            }
            assertEquals (expected, value, epsilon);
        }
    }

    /** Get full coverage of this file. */
    @Test
    public void testCheckExpression ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem parent = new GeoItem (plane, "parent", Color.black);
        final NamedVariable test = new NamedVariable (parent, Color.red, "test", 5.0);
        final NamedVariable alpha = new NamedVariable (parent, Color.red, "alpha", 5.0);
        assertNotNull (plane.get (parent.getName ()));

        alpha.setFormula ("test", "alpha == 5", alpha);
        checkExpression (alpha, 5);
        test.setFormula ("test", "test == alpha", test, alpha);
        final Inference inference = test.getInference ();
        assertNotNull (inference);
        final String[] terms = inference.getTermNames ();
        assertEquals (2, terms.length);
        assertEquals ("alpha", terms[1]);
        checkExpression (test, 5, "testCheckExpression");
        checkExpression (test, 5);
    }

    /**
     * Create a file for test data.
     *
     * @param source The class running the unit test. The classname is used as part of the test data
     *            filename.
     * @param suffix A suffix to distinguish from all other test data files used from the same unit
     *            test source file.
     *
     * @return A File in ${user.home}/Documents/foe/data.
     */
    public File getTestPngFile (Object source, String suffix)
    {
        return getTestDataFile (source, suffix, "png");
    }

    /**
     * Create a file for test data.
     *
     * @param source The class running the unit test. The classname is used as part of the test data
     *            filename.
     * @param suffix A suffix to distinguish from all other test data files used from the same unit
     *            test source file.
     * @param extension The file extension, with no dot.
     *
     * @return A File in ${user.home}/Documents/foe/data.
     */
    public File getTestDataFile (Object source, String suffix, String extension)
    {
        final String filename = source.getClass ().getSimpleName () + "_" + suffix + "." + extension;
        final String userHome = System.getProperty ("user.home");
        final File data = new File (userHome, "Documents/foe/data");
        data.mkdirs ();
        final File file = new File (data, filename);
        return file;
    }

    /**
     * Textual comparison of two files. Asserts failure if files are not the same.
     *
     * @throws IOException
     */
    public void compare (File expected, File actual) throws IOException
    {
        try (final BufferedReader expectedStream = new BufferedReader (new FileReader (expected)))
        {
            try (final BufferedReader actualStream = new BufferedReader (new FileReader (actual)))
            {
                while (true)
                {
                    final String expectedLine = expectedStream.readLine ();
                    final String actualLine = actualStream.readLine ();
                    if (expectedLine == null && actualLine == null)
                    {
                        return;
                    }
                    assertNotNull (expectedLine);
                    assertNotNull (actualLine);
                    assertEquals (expectedLine, actualLine);
                }
            }
        }
    }

    /**
     * Compare a newly generated image to saved data from a test data file. If the file is missing,
     * the data will be saved instead of compared.
     *
     * @param image The newly generated image.
     * @param file The file to contain the test data.
     *
     * @throws IOException
     */
    public void compare (BufferedImage image, File file) throws IOException
    {
        if (!file.exists ())
        {
            logger.info ("Creating image file %s", file);
            ImageIO.write (image, "png", file);
        }
        else
        {
            logger.info ("Loading expected image file %s", file);
            final BufferedImage expected = ImageIO.read (file);
            final int width = expected.getWidth ();
            final int height = expected.getHeight ();
            assertEquals (width, image.getWidth ());
            assertEquals (height, image.getHeight ());
            assertTrue (equalBufferedImage (expected, image));
        }
    }

    /**
     * Compare every pixel in a pair of images and return true if they are all the same.
     *
     * @param b1 Expected image data.
     * @param b2 Actual image data.
     *
     * @return File if the width, height or image content differs.
     */
    public boolean equalBufferedImage (BufferedImage b1, BufferedImage b2)
    {
        if (b1 != null && b2 != null)
        {
            final int width = b1.getWidth ();
            if (width == b2.getWidth ())
            {
                final int height = b1.getHeight ();
                if (height == b2.getHeight ())
                {
                    for (int i = 0; i < width; i++)
                    {
                        for (int j = 0; j < height; j++)
                        {
                            if (b1.getRGB (i, j) != b2.getRGB (i, j))
                            {
                                return false;
                            }
                        }
                    }
                    return true;
                }
            }
        }
        return false;
    }

    public static void dream (int ms)
    {
        try
        {
            Thread.sleep (ms);
        }
        catch (final InterruptedException e)
        {
        }
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        buffer.append (" ");
        buffer.append (System.identityHashCode (this));
        buffer.append (">");
        return buffer.toString ();
    }
}
