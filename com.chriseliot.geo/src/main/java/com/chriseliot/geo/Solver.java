package com.chriseliot.geo;
//
// package com.empiremaster.geo;
//
// import java.util.*;
//
// import org.apache.logging.log4j.*;
// import org.matheclipse.core.eval.ExprEvaluator;
// import org.matheclipse.core.interfaces.IExpr;
//
/// **
// * Geometry solver.
// *
// * Rules to implement
// * <ul>
// * <it>By definition of point, the x, y values of a known point are set to the values of the
// * point.</it>
// *
// * <it>By definition of point, the value of a point is set to its x, y values if they are
// * known.</it>
// *
// * <it>The angles of a vertex must add to 180 degrees. </it>
// *
// * <it>The angles of a triangle must add to 180 degrees. </it>
// *
// * <it>The law of cosines relates the length of the sides of a triangle.
// *
// * c^2 = a^2 + b^2 − 2ab cos(theta) If theta = 90 degrees, this simplifies to the pythagorean
// * theorem.</it>
// *
// * <it>[Done] Various points that the same thing should propagate to each other.</it>
// *
// * <it>Length and slope of a line should be computed from the endpoints.</it> <it>The midpoint of
// a
// * line can be computed from the endpoints.</it>
// *
// * <it>Second line endpoint should be computed from one endpoint and the slope and length.</it>
// *
// * <it>Length of a triangle side may match the length of a line.</it>
// *
// * <it>Angle of a triangle may match the angle of a vertex.</it>
// *
// * <it>Two points determine a line</it> <it>A vertex is defined by its endpoint</it>
// * </ul>
// *
// */
// public class Solver
// {
// private final Logger logger = LogManager.getFormatterLogger (this.getClass ());
//
// private final GeoPlane plane;
// private final List<GeoItem> known = new ArrayList<> ();
// private final List<GeoItem> unknown = new ArrayList<> ();
// private final List<GeoItem> derived = new ArrayList<> ();
//
// /**
// * Create a solver for this geometry. This collects known and unknown facts. The known facts are
// * added to the derived list, so the solver will recognize that there is more work to do. The
// * known list is left empty and it will be populated by the solver.
// *
// * TODO: The solver should become independent, meaning all lines and points should be added
// * directly here. It should be possible to run the solver without the gui.
// */
// public Solver (GeoPlane plane)
// {
// this.plane = plane;
// }
//
// /**
// * Look for derived facts. Keep deriving new facts until nothing more can be found. At the start
// * of each cycle the derived list is added to the known facts. During a cycle new facts are
// * added to the derived list. If the derived list does not change, the solver is done.
// */
// public void solve ()
// {
// setup ();
// while (!derived.isEmpty ())
// {
// known.addAll (derived);
// unknown.removeAll (derived);
// derived.clear ();
// cycle ();
// }
// logger.info ("Known has %d items", known.size ());
// logger.info ("Unknown has %d items", unknown.size ());
// logger.info ("Derived has %d items", derived.size ());
// logger.info ("Mathematical setup");
// final List<String> e = new ArrayList<> ();
// final List<String> k = new ArrayList<> ();
// final List<String> u = new ArrayList<> ();
// final List<String> d = new ArrayList<> ();
// for (final GeoItem item : plane.getItems ())
// {
// for (final String equation : item.getMathEquations ())
// {
// final String eq = equation.replace ("" + GeoItem.SEP, "");
// e.add (eq);
// logger.info (": %s", eq);
// }
// }
// for (final GeoItem item : plane.getItems ())
// {
// if (item instanceof NamedVariable)
// {
// final NamedVariable var = (NamedVariable)item;
// final String v = var.getName ().replace ("" + GeoItem.SEP, "");
// final GeoStatus status = var.getStatus ();
// if (status == GeoStatus.known || status == GeoStatus.fixed)
// {
// k.add (v);
// }
// else if (status == GeoStatus.derived)
// {
// d.add (v);
// }
// else
// {
// u.add (v);
// }
// }
// }
// logger.info ("Solving");
// final StringBuilder formula = new StringBuilder ();
// formula.append ("solve");
// formula.append ("(");
// formula.append ("{");
// for (int i = 0; i < e.size (); i++)
// {
// if (i > 0)
// {
// formula.append (", ");
// }
// formula.append (e.get (i));
// }
// formula.append ("}");
// formula.append (", ");
// formula.append ("{");
// // All variables, unknown first
// final List<String> vv = new ArrayList<> ();
// vv.addAll (u);
// vv.addAll (k);
// // vv.addAll (d);
// for (int i = 0; i < vv.size (); i++)
// {
// if (i > 0)
// {
// formula.append (", ");
// }
// formula.append (vv.get (i));
// }
// formula.append ("}");
// formula.append (")");
// logger.info ("Symbolic: %s", formula);
// final ExprEvaluator eval = new ExprEvaluator ();
// final IExpr expr = eval.parse (formula.toString ());
// logger.info ("Expression: %s", expr);
// final IExpr value = eval.eval (expr);
// logger.info ("Value: %s", value);
// logger.info ("===");
// }
//
// private void setup ()
// {
// final List<GeoItem> items = plane.getItems ();
// for (final GeoItem item : items)
// {
// final GeoStatus status = item.getStatus ();
// if (status == GeoStatus.known || status == GeoStatus.fixed)
// {
// derived.add (item);
// }
// else
// {
// item.setStatus (GeoStatus.unknown, "solver");
// unknown.add (item);
// }
// }
// }
//
// /**
// * Look for new derived facts. Only facts listed in the unknown list can be derived. Nothing is
// * created during problem solving. Since there are a finite number of facts to consider, and
// * something must change during each cycle, this will always terminate.
// */
// public void cycle ()
// {
// for (final GeoItem item : known)
// {
// checkKnownItem (item);
// }
// for (final GeoItem item : unknown)
// {
// checkUnknownItem (item);
// }
// for (final GeoItem item : plane.getItems ())
// {
// if (item instanceof GeoVertex)
// {
// checkVertex ((GeoVertex)item);
// }
// if (item instanceof GeoLine)
// {
// checkLine ((GeoLine)item);
// }
// }
// for (final GeoTriangle t : plane.getTriangles ())
// {
// checkMatchingLines (t);
// checkAngles (t);
// lawOfCosines (t);
// checkTriangle (t);
// }
// }
//
// /** Record an inference about an item. */
// public void derive (GeoItem item, String reason, String format, Object... args)
// {
// if (unknown.contains (item))
// {
// if (!derived.contains (item))
// {
// derived.add (item);
// item.setStatus (GeoStatus.derived, reason);
// logger.info (format, args);
// }
// }
// }
//
// private int countKnown (List<NamedVariable> variables)
// {
// int result = 0;
// for (final NamedVariable v : variables)
// {
// if (known.contains (v))
// {
// result++;
// }
// }
// return result;
// }
//
// private boolean hasKnown (List<NamedVariable> variables)
// {
// for (final NamedVariable v : variables)
// {
// if (known.contains (v))
// {
// return true;
// }
// }
// return false;
// }
//
// /**
// * Check to see if a known item is a NamedPoint. If so, its children are also known since they
// * are the x, y values.
// *
// * @param item
// */
// private void checkKnownItem (GeoItem item)
// {
// if (item instanceof NamedPoint)
// {
// for (final GeoItem child : item.getChildren ())
// {
// derive (child, "definition of point", "Derived %s by definition of point", child);
// }
// }
// final GeoItem parent = item.getParent ();
// if (parent instanceof GeoVertex)
// {
// final GeoVertex v = (GeoVertex)parent;
// final NamedVariable angle1 = v.getAngle1 ();
// final NamedVariable angle2 = v.getAngle2 ();
// if (item == angle1)
// {
// derive (angle2, "vertex angles", "Derived angle %s because vertex angles add to 180 degrees",
// angle2);
// }
// else if (item == angle2)
// {
// derive (angle1, "vertex angles", "Derived angle %s because vertex angles add to 180 degrees",
// angle1);
// }
// }
// }
//
// private void checkVertex (GeoVertex v)
// {
// final NamedPoint vertex = v.getVertex ();
// // logger.info ("Checking vertex %s at %s %s", v, vertex.getStatus (), vertex);
// final GeoLine l1 = v.getLine1 ();
// final GeoLine l2 = v.getLine2 ();
// checkPairedPoints (vertex, l1.getFrom ());
// checkPairedPoints (vertex, l1.getTo ());
// checkPairedPoints (vertex, l2.getFrom ());
// checkPairedPoints (vertex, l2.getTo ());
// checkPairedPoints (l1.getFrom (), l2.getFrom ());
// checkPairedPoints (l1.getFrom (), l2.getTo ());
// checkPairedPoints (l1.getTo (), l2.getFrom ());
// checkPairedPoints (l1.getTo (), l2.getTo ());
// if (known.contains (vertex) && unknown.contains (v))
// {
// derive (v, "vertex position", "Vertex %s is determined by its position %s", v, vertex);
// }
// else if (known.contains (v))
// {
// derive (vertex, "vertex position", "Vertex position %s is defined by the vertex %s", vertex, v);
// }
// if (known.contains (v))
// {
// final NamedVariable angle1 = v.getAngle1 ();
// derive (angle1, "vertex angle", "Vertex angle %s is defined by the vertex %s", angle1, v);
// final NamedVariable angle2 = v.getAngle2 ();
// derive (angle2, "vertex angle", "Vertex angle %s is defined by the vertex %s", angle2, v);
// }
// }
//
// private void checkPairedPoints (NamedPoint a, NamedPoint b)
// {
// if (a.at (b))
// {
// deriveSamePosition (a, b);
// }
// }
//
// /**
// *
// * <ul>
// * <it>Two points determine a line</it> <it>Length and slope of a line should be computed from
// * the endpoints.</it>
// *
// * <it>The midpoint of a line can be computed from the endpoints.</it>
// *
// * <it>Second line endpoint should be computed from one endpoint and the slope and length.</it>
// * </ul>
// *
// * @param line
// */
// private void checkLine (GeoLine line)
// {
// final NamedPoint a = line.getFrom ();
// final NamedPoint b = line.getTo ();
// final NamedVariable length = line.getLength ();
// final NamedVariable slope = line.getSlope ();
// // [TODO] Can also determine the line from midpoint and either endpoint
// if (known.contains (a) && known.contains (b))
// {
// derive (line, "line endpoints", "Line %s is determined by endpoints %s %s", line, a, b);
// final NamedPoint c = line.getMidpoint ();
// derive (c, "line endpoints", "Midpoint %s can be computed from endpoints %s %s", c, a, b);
// derive (length, "line endpoints", "Line length %s can be computed from endpoints %s %s", length,
// a, b);
// derive (slope, "line endpoints", "Line slope %s can be computed from endpoints %s %s", slope, a,
// b);
// }
// if (known.contains (length) && known.contains (slope))
// {
// if (known.contains (a))
// {
// derive (b, "line endpoints", "Line endpoint %s can be computed from endpoint %s and slope %s and
// length %s", b, a,
// slope, length);
// }
// if (known.contains (b))
// {
// derive (a, "line endpoints", "Line endpoint %s can be computed from endpoint %s and slope %s and
// length %s", a, b,
// slope, length);
// }
// }
// if (known.contains (line))
// {
// derive (a, "endpoints determined by line", "Line %s determines endpoints %s %s", line, a, b);
// derive (b, "endpoints determined by line", "Line %s determines endpoints %s %s", line, a, b);
// }
// }
//
// private void deriveSamePosition (NamedPoint a, NamedPoint b)
// {
// // logger.info ("Checking %s %s and %s %s", a.getStatus (), a, b.getStatus (), b);
// if (known.contains (a))
// {
// derive (b, "Equal position", "Derived position %s because it is the same as %s", b, a);
// }
// else if (known.contains (b))
// {
// derive (a, "Equal position", "Derived position %s because it is the same as %s", a, b);
// }
// }
//
// /**
// * Check to see if both children of an unknown NamedPoint are known values. If so, the named
// * point can be derived.
// *
// * @param item
// */
// private void checkUnknownItem (GeoItem item)
// {
// if (item instanceof NamedPoint)
// {
// for (final GeoItem child : item.getChildren ())
// {
// if (!known.contains (child))
// {
// return;
// }
// }
// derive (item, "Definition of point", "Derived %s by definition of point", item);
// }
// }
//
// /** If the vertex of a triangle match lines, then make them equal. */
// private void checkMatchingLines (GeoTriangle t)
// {
// final NamedPoint v1 = t.getV1 ().getVertex ();
// final NamedPoint v2 = t.getV2 ().getVertex ();
// final NamedPoint v3 = t.getV3 ().getVertex ();
// deriveTriangleSide (v1, v2, t.getL1 ());
// deriveTriangleSide (v2, v3, t.getL2 ());
// deriveTriangleSide (v3, v1, t.getL3 ());
// }
//
// private void deriveTriangleSide (NamedPoint v1, NamedPoint v2, NamedVariable side)
// {
// if (known.contains (v1) && known.contains (v2))
// {
// derive (side, "Length endpoints", "Derived length of triangle side %s from endpoints %s and %s",
// side, v1, v2);
// }
// }
//
// /** The angles of a triangle must add to 180 degrees. */
// private void checkAngles (GeoTriangle t)
// {
// final List<GeoVertex> vertices = t.getVertices ();
// int count = 0;
// GeoVertex computed = null;
// for (final GeoVertex v : vertices)
// {
// if (known.contains (v.getAngle1 ()))
// {
// count++;
// }
// else if (unknown.contains (v))
// {
// computed = v;
// }
// }
// if (count == 2 && computed != null)
// {
// computed.setStatus (GeoStatus.derived, "triangle interior angles");
// derived.add (computed);
// logger.info ("Derived angle %s because angles of a triangle add to 180 degrees", computed);
// }
// }
//
// /**
// * The law of cosines relates the length of the sides of a triangle.
// *
// * c^2 = a^2 + b^2 − 2ab cos(theta)
// *
// * If theta = 90 degrees, this simplifies to the pythagorean theorem.
// *
// * @param t The triangle to check.
// *
// * TODO: The segments specifically connecting the triangle vertices must be created.
// * Currently, the lines may be part of longer lines.
// */
// private void lawOfCosines (GeoTriangle t)
// {
// final List<NamedVariable> sides = t.getSides ();
// final List<NamedVariable> angles = t.getAngles ();
// final int knownSides = countKnown (sides);
// boolean deriveAll = false;
// if (knownSides >= 3)
// {
// deriveAll = true;
// }
// else if (knownSides == 2 && hasKnown (angles))
// {
// deriveAll = true;
// }
// if (deriveAll)
// {
// final List<NamedVariable> variables = t.getVariables ();
// for (final NamedVariable v : variables)
// {
// if (unknown.contains (v))
// {
// deriveByLawOfCosine (v);
// }
// }
// }
// }
//
// private void deriveByLawOfCosine (NamedVariable v)
// {
// derive (v, "Law of cosines", "Derived %s by law of cosines", v);
// }
//
// private void checkTriangle (GeoTriangle t)
// {
// final NamedPoint v1 = t.getV1 ().getVertex ();
// final NamedPoint v2 = t.getV2 ().getVertex ();
// final NamedPoint v3 = t.getV3 ().getVertex ();
// if (known.contains (v1) && known.contains (v2) && known.contains (v3))
// {
// derive (t, "Triangle endpoints", "Triangle %s is determined by its endpoints %s %s %s", t, v1,
// v2, v3);
// }
// else if (known.contains (t))
// {
// derive (v1, "Known triangle", "Triangle point %s is determined by %s", v1, t);
// derive (v2, "Known triangle", "Triangle point %s is determined by %s", v2, t);
// derive (v3, "Known triangle", "Triangle point %s is determined by %s", v3, t);
// }
// }
//
// @Override
// public String toString ()
// {
// final StringBuilder buffer = new StringBuilder ();
// buffer.append ("#<");
// buffer.append (getClass ().getSimpleName ());
// buffer.append (" ");
// buffer.append (System.identityHashCode (this));
// buffer.append (">");
// return buffer.toString ();
// }
// }
