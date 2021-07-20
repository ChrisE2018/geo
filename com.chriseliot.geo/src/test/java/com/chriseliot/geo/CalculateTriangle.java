
package com.chriseliot.geo;

import static java.lang.Math.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.awt.geom.Point2D;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.Test;

public class CalculateTriangle
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    @Test
    public void testCalculate ()
    {
        final Point2D.Double p1 = new Point2D.Double (10, 20);
        final Point2D.Double p2 = new Point2D.Double (30, 40);
        final Point2D.Double p3 = new Point2D.Double (50, 55);
        logger.info ("P1 %s", p1);
        logger.info ("P2 %s", p2);
        logger.info ("P3 %s", p3);
        final double a = distance (p1, p2);
        final double b = distance (p2, p3);
        final double c = distance (p3, p1);
        logger.info ("distance(p1, p2) %.3f", a);
        logger.info ("distance(p2, p3) %.3f", b);
        logger.info ("distance(p3, p1) %.3f", c);
        final double theta1 = theta (a, b, c); // angle a,b
        final double theta2 = theta (b, c, a); // angle b, c
        final double theta3 = theta (c, a, b); // angle c, a
        logger.info ("angle a,b: %.3f degrees", theta1);
        logger.info ("angle b,c: %.3f degrees", theta2);
        logger.info ("angle c,a: %.3f degrees", theta3);
        final double sum = theta1 + theta2 + theta3;
        logger.info ("sum of angles %.3f degrees", sum);
        assertEquals (180, sum, TestSupport.epsilon);
        final double aa = base (c, b, theta2);
        logger.info ("distance(p1, p2) %.3f = %.3f", a, aa);
        final double bb = base (c, a, theta3);
        logger.info ("distance(p2, p3) %.3f = %.3f", b, bb);
        final double cc = base (b, a, theta1);
        logger.info ("distance(p3, p1) %.3f = %.3f", c, cc);
        assertEquals (a, aa);
        assertEquals (b, bb);
        assertEquals (c, cc);
    }

    public double theta (double a, double b, double c)
    {
        final double numerator = a * a + b * b - c * c;
        final double denominator = 2 * a * b;
        final double costheta = numerator / denominator;
        return toDegrees (acos (costheta));
    }

    public double base (double a, double b, double theta)
    {
        return sqrt (a * a + b * b - (2 * a * b * cos (toRadians (theta))));
    }

    public double distance (Point2D.Double p, Point2D.Double q)
    {
        return sqrt (distance2 (p, q));
    }

    public double distance2 (Point2D.Double p, Point2D.Double q)
    {
        final double dx = q.x - p.x;
        final double dy = q.y - p.y;
        return dx * dx + dy * dy;
    }
}
