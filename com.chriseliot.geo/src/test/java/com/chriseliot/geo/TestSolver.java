package com.chriseliot.geo;
//
// package com.empiremaster.geo;
//
// import static org.junit.jupiter.api.Assertions.*;
//
// import java.awt.Color;
// import java.awt.geom.Point2D;
//
// import javax.swing.SwingConstants;
//
// import org.apache.logging.log4j.*;
// import org.junit.jupiter.api.Test;
//
// public class TestSolver
// {
// private final Logger logger = LogManager.getFormatterLogger (this.getClass ());
//
// @Test
// public void testCreate ()
// {
// final GeoPlane plane = new GeoPlane ();
// final GeoItem item = new GeoItem (plane, "t", Color.black);
// final Solver test = new Solver (plane);
// assertNotNull (test.toString ());
// assertNotNull (item);
// logger.debug ("Initial solver test complete");
// }
//
// /** Get full coverage of the Solver constructor. */
// @Test
// public void testCreateDerived ()
// {
// final GeoPlane plane = new GeoPlane ();
// final GeoItem item = new GeoItem (plane, "t", Color.black);
// item.setStatus (GeoStatus.known, "test");
// assertNotNull (new Solver (plane));
// final GeoItem item2 = new GeoItem (plane, "t2", Color.black);
// item2.setStatus (GeoStatus.fixed, "test");
// assertNotNull (new Solver (plane));
// }
//
// /** Run the solver but don't check anything. */
// @Test
// public void testSolve ()
// {
// final GeoPlane plane = new GeoPlane ();
// final GeoItem item = new GeoItem (plane, "t", Color.black);
// item.setStatus (GeoStatus.known, "test");
// final GeoItem item2 = new GeoItem (plane, "t2", Color.black);
// item2.setStatus (GeoStatus.fixed, "test");
// final Solver test = new Solver (plane);
// test.solve ();
// }
//
// /** Test propagation from point to x, y of the point. */
// @Test
// public void testPoint1 ()
// {
// final GeoPlane plane = new GeoPlane ();
// final GeoItem item = new GeoItem (plane, "t", Color.black);
// final NamedPoint child = new NamedPoint (item, false, Color.green, "test", 0, 0,
// SwingConstants.NORTH_WEST);
// child.setStatus (GeoStatus.known, "test");
// final Solver test = new Solver (plane);
// test.solve ();
// for (final GeoItem p : child.getChildren ())
// {
// assertEquals (GeoStatus.derived, p.getStatus ());
// }
// assertFalse (child.getChildren ().isEmpty ());
// }
//
// /** Test propagation from x, y of the point to point. */
// @Test
// public void testPoint2 ()
// {
// final GeoPlane plane = new GeoPlane ();
// final GeoItem item = new GeoItem (plane, "t", Color.black);
// final NamedPoint child = new NamedPoint (item, false, Color.green, "test", 0, 0,
// SwingConstants.NORTH_WEST);
// for (final GeoItem p : child.getChildren ())
// {
// p.setStatus (GeoStatus.known, "test");
// }
// final Solver test = new Solver (plane);
// test.solve ();
// assertEquals (GeoStatus.derived, child.getStatus ());
// assertFalse (child.getChildren ().isEmpty ());
// }
//
// /** Test that a known line allows all parts to be derived. */
// @Test
// public void testLine ()
// {
// final GeoPlane plane = new GeoPlane ();
// final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new
// Point2D.Double (30, 40));
// line1.setStatus (GeoStatus.known, "test");
// final Solver test = new Solver (plane);
// test.solve ();
// for (final GeoItem item : plane.getItems ())
// {
// if (item == line1)
// {
// assertEquals (GeoStatus.known, item.getStatus ());
// }
// else
// {
// // assertEquals (GeoStatus.derived, item.getStatus ());
// }
// }
// }
//
// @Test
// public void testVertex ()
// {
// logger.info ("Test Vertex Starting");
// final GeoPlane plane = new GeoPlane ();
// final GeoLine line1 = new GeoLine (plane, Color.red, new Point2D.Double (10, 20), new
// Point2D.Double (30, 40));
// final GeoLine line2 = new GeoLine (plane, Color.blue, new Point2D.Double (10, 20), new
// Point2D.Double (50, 55));
// final GeoLine line3 = new GeoLine (plane, Color.blue, new Point2D.Double (30, 40), new
// Point2D.Double (50, 55));
//
// final GeoVertex v1 = line1.getVertex (line2);
// final GeoVertex v2 = line2.getVertex (line3);
// final GeoVertex v3 = line3.getVertex (line1);
// final Solver test = new Solver (plane);
// test.solve ();
// assertEquals (GeoStatus.unknown, v1.getStatus ());
// assertEquals (GeoStatus.unknown, v2.getStatus ());
// assertEquals (GeoStatus.unknown, v3.getStatus ());
// line1.setStatus (GeoStatus.known, "test");
// line2.setStatus (GeoStatus.known, "test");
// line3.setStatus (GeoStatus.known, "test");
// test.solve ();
// for (final GeoItem item : plane.getItems ())
// {
// if (item == line1 || item == line2 || item == line3)
// {
// assertEquals (GeoStatus.known, item.getStatus ());
// }
// else
// {
// // assertEquals (GeoStatus.derived, item.getStatus ());
// }
// }
// }
// }
