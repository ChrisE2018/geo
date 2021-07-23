
package com.chriseliot.geo.gui;

import java.awt.geom.Point2D;

import javax.swing.*;

import com.chriseliot.geo.*;

public class NamedPointActions
{
    /**
     * Action to perform for a set value action.
     *
     * @see https://stackoverflow.com/questions/6555040/multiple-input-in-joptionpane-showinputdialog/6555051
     */
    public void setValueAction (NamedPoint item)
    {
        final NamedVariable x = item.getX ();
        final NamedVariable y = item.getY ();
        final JTextField xField = new JTextField (String.format ("%.3f", x.getDoubleValue ()));
        final JTextField yField = new JTextField (String.format ("%.3f", y.getDoubleValue ()));
        final Object[] message = {"X:", xField, "Y:", yField};

        final int option = JOptionPane.showConfirmDialog (null, message, "Set Position", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION)
        {
            final double resultX = Double.parseDouble (xField.getText ());
            final double resultY = Double.parseDouble (yField.getText ());
            item.setValueAction (new Point2D.Double (resultX, resultY));
        }
    }
}
