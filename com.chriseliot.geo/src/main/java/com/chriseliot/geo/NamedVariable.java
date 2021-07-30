
package com.chriseliot.geo;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.logging.log4j.*;
import org.matheclipse.core.eval.ExprEvaluator;
import org.w3c.dom.Element;

import com.chriseliot.geo.gui.NamedVariableActions;
import com.chriseliot.util.Labels;

/** A variable representing a single real value. */
public class NamedVariable extends GeoItem
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    /**
     * Current value determined from the screen positions. This may be more specific than can be
     * determined from the geometry. In other words, this will be a specific number while the
     * geometry allows for some range of values.
     */
    private Double value;

    /** Location on screen to draw label for this variable. */
    private NamedPoint location;

    /** A variable representing a single real value. */
    public NamedVariable (GeoItem parent, Color color, String name, Double value)
    {
        super (parent, name, color);
        this.value = value;
        if (parent instanceof NamedPoint)
        {
            location = (NamedPoint)parent;
        }
        else
        {
            location = null;
        }
        setStatusUnknown ();
    }

    /** A variable representing a single real value. */
    public NamedVariable (GeoItem parent, Color color, String name)
    {
        super (parent, name, color);
        value = null;
        if (parent instanceof NamedPoint)
        {
            location = (NamedPoint)parent;
        }
        else
        {
            location = null;
        }
        setStatusUnknown ();
    }

    /**
     * Current value determined from the screen positions. This may be more specific than can be
     * determined from the geometry. In other words, this will be a specific number while the
     * geometry allows for some range of values.
     */
    public Double getDoubleValue ()
    {
        return value;
    }

    /**
     * Current value determined from the screen positions. This may be more specific than can be
     * determined from the geometry. In other words, this will be a specific number while the
     * geometry allows for some range of values.
     */
    public void setDoubleValue (Double value)
    {
        this.value = value;
    }

    /** Set the status to unknown. Reset the formula and terms to the correct default state. */
    @Override
    public void setStatusUnknown ()
    {
        super.setStatusUnknown ();
    }

    /**
     * Define the value of this item for math eclipse. This should be overridden.
     *
     * @param eval The symbolic math context. Use the defineVariable method to define a boolean,
     *            double or IExpr binding the name of this item to some value.
     */
    @Override
    public void defineVariable (ExprEvaluator eval)
    {
        eval.defineVariable (getName (), value);
    }

    /**
     * Get a math eclipse expression for the value of this item. This should be overridden.
     *
     * @return The item value.
     */
    @Override
    public String getStringValue ()
    {
        return String.format ("%.4f", value);
    }

    /** Derive consequential formulas around this variable. */
    @Override
    public void solve ()
    {
        if (getParent () instanceof NamedPoint)
        {
            final NamedPoint parent = (NamedPoint)getParent ();
            for (final GeoItem item : getPlane ().getItems ())
            {
                if (item instanceof NamedPoint && item != parent)
                {
                    final NamedPoint p = (NamedPoint)item;
                    if (parent.at (p))
                    {
                        parent.equivalent (p);
                    }
                }
            }
        }
        setFormula ("fixed value", "%s == " + getDoubleValue (), this, this);
    }

    /** Location on screen to draw label for this variable. */
    public NamedPoint getLocation ()
    {
        return location;
    }

    /** Location on screen to draw label for this variable. */
    public void setLocation (NamedPoint location)
    {
        this.location = location;
    }

    /**
     * Paint this item in its current state. The Graphics object is normally the window component,
     * but could be a printer or BufferedImage.
     *
     * @param g The paint destination.
     */
    @Override
    public void paint (Graphics g, Labels labels)
    {
        final NamedPoint location = getLocation ();
        if (location != null)
        {
            final Point position = location.getIntPosition ();
            final Color color = getStatus ().getColor ();
            final String name = getName ();
            String text = name;
            if (value != null)
            {
                text = String.format ("%s = %.1f", getName (), value);
            }
            final String tooltip;
            final Inference inference = getInference ();
            if (inference == null)
            {
                tooltip = text;
            }
            else
            {
                tooltip = String.format ("%.1f = %s", value, inference.getInstantiation ());
            }
            final int anchor = location.getAnchor ();
            labels.add (this, color, position, anchor, text, tooltip);
        }
    }

    /**
     * Populate a popup menu with required items. This should be overridden by subclasses. Be sure
     * to call the super method.
     *
     * @param result
     */
    @Override
    public void popup (Map<String, Consumer<GeoItem>> result)
    {
        super.popup (result);
        final NamedVariableActions actions = new NamedVariableActions ();
        final GeoItem parent = getParent ();
        if (parent instanceof NamedPoint)
        {
            final NamedPoint p = (NamedPoint)parent;
            if (this == p.getX () || this == p.getY ())
            {
                result.put ("Set Value", item -> actions.setValueAction (this));
            }
        }
        result.put ("Rename Variable", item -> actions.renameVariableAction (this));
        if (getStatus () == GeoStatus.derived)
        {
            result.put ("Show Solution", item -> actions.showSolutionAction (this));
            result.put ("Show Derivation", item -> actions.showDerivationAction (this));
        }
    }

    /**
     * Action to take when the dialog returns. This only works for variables that are x or y of a
     * named point. If calls the point to make the adjustment.
     */
    public void setValueAction (double result)
    {
        final GeoItem parent = getParent ();
        if (parent instanceof NamedPoint)
        {
            final NamedPoint p = (NamedPoint)parent;
            if (this == p.getX ())
            {
                p.setValueAction (new Point2D.Double (result, p.getY ().getDoubleValue ()));
                setGivenStatus (GeoStatus.fixed);
            }
            else
            {
                p.setValueAction (new Point2D.Double (p.getX ().getDoubleValue (), result));
                setGivenStatus (GeoStatus.fixed);
            }
        }
    }

    /** Implement the rename option. */
    public void renameVariableAction (String name)
    {
        logger.info ("Rename from %s to %s", getName (), name);
        final GeoPlane plane = getPlane ();
        if (plane.get (name) == null)
        {
            plane.remove (this);
            setName (name);
            plane.addItem (this);
            plane.fireChangeListeners (this);
        }
    }

    @Override
    public void getAttributes (Element element)
    {
        super.getAttributes (element);
        if (value != null)
        {
            element.setAttribute ("value", String.valueOf (value));
        }
        if (location != null)
        {
            element.setAttribute ("location", String.valueOf (location.getName ()));
        }
    }

    @Override
    public void marshall (Element element)
    {
        super.marshall (element);
        final GeoPlane plane = getPlane ();
        value = xu.getDouble (element, "value", null);

        final String locationName = xu.get (element, "location", null);
        if (locationName != null)
        {
            if (location == null)
            {
                // This might have to be deferred until the end of the read
                location = (NamedPoint)plane.get (locationName);
            }
        }
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        buffer.append (" ");
        buffer.append (getName ());
        if (value != null)
        {
            buffer.append (" ");
            buffer.append (String.format ("%.3f", value));
        }
        buffer.append (" ");
        buffer.append (getStatus ());
        final Inference inference = getInference ();
        if (inference != null)
        {
            buffer.append (" ");
            buffer.append ("{");
            buffer.append (inference.getInstantiation ());
            buffer.append ("}");
        }
        buffer.append (">");
        return buffer.toString ();
    }
}
