
package com.chriseliot.geo.gui;

import javax.swing.*;

import com.chriseliot.geo.*;

public class NamedVariableActions
{
    /** Action to perform for a show derivation action. */
    public void showSolutionAction (NamedVariable item)
    {
        final Inference inference = item.getInference ();
        if (inference != null)
        {
            final String rawFormula = inference.getInstantiation ();
            final String derivedFormula = item.getDerivedFormula ();
            final String message = String.format ("Raw: %s\nDerived: %s", rawFormula, derivedFormula);
            JOptionPane.showMessageDialog (null, message, "Derivation", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Action to perform for a show derivation action. */
    public void showDerivationAction (NamedVariable item)
    {
        final Inference inference = item.getInference ();
        if (inference != null)
        {
            final StringBuilder builder = new StringBuilder ();
            final String formula = inference.getInstantiation ();
            builder.append (String.format ("Derivation: %s\n\n", formula));
            item.getDerivation (builder);
            item.getFormulaLine (builder);
            JOptionPane.showMessageDialog (null, builder.toString (), "Derivation", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /** Action to perform for a set value action. */
    public void setValueAction (NamedVariable item)
    {
        final Double value = item.getDoubleValue ();
        final String name = item.getName ();
        final String message = (value == null) ? String.format ("Enter new value for %s", name)
                                               : String.format ("Enter new value for %s (%s)", name, value);
        final String result = JOptionPane.showInputDialog (null, message, "Input Value", JOptionPane.QUESTION_MESSAGE);
        if (result != null)
        {
            item.setValueAction (Double.parseDouble (result));
        }
    }

    /** Action to perform for a set value action. */
    public void setValueAction (LineAngleVariable item)
    {
        final String name = item.getName ();
        final Double value = item.getDoubleValue ();
        final String message = (value == null) ? String.format ("Enter new angle %s degrees", name)
                                               : String.format ("Enter new angle %s (%s) degrees", name, value);
        final String result = JOptionPane.showInputDialog (null, message, "Input Value", JOptionPane.QUESTION_MESSAGE);
        if (result != null)
        {
            item.setValueAction (Double.parseDouble (result));
        }
    }

    /** Action to perform for a set value action. */
    public void setValueAction (TriangleAngleVariable item)
    {
        final String name = item.getName ();
        final Double value = item.getDoubleValue ();
        final JTextField angleField = new JTextField (String.format ("%.3f", value));
        final JCheckBox cornerField = new JCheckBox ("Right Angle", value == 90);
        final Object[] message = {"Angle:", angleField, cornerField};
        final String result = JOptionPane.showInputDialog (null, message, "Set angle " + name, JOptionPane.QUESTION_MESSAGE);
        if (result != null)
        {
            if (cornerField.isSelected ())
            {
                item.setValueAction (90);
            }
            else
            {
                item.setValueAction (Double.parseDouble (result));
            }
        }
    }

    /** Implement the rename option. */
    public void renameVariableAction (NamedVariable item)
    {
        final String name = item.getName ();
        final String message = String.format ("Enter new name for %s", name);
        final String result = JOptionPane.showInputDialog (null, message, "Input Name", JOptionPane.QUESTION_MESSAGE);
        if (result != null)
        {
            item.renameVariableAction (result);
        }
    }
}
