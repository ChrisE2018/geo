
package com.chriseliot.geo.gui;

import java.awt.Window;

import javax.swing.JDialog;

public class CloseDialogThread extends Thread
{
    private boolean isRunning = false;
    private boolean isDialogSeen = false;

    public boolean isRunning ()
    {
        return isRunning;
    }

    public boolean isDialogSeen ()
    {
        return isDialogSeen;
    }

    public void halt ()
    {
        isRunning = false;
    }

    @Override
    public void run ()
    {
        isRunning = true;
        for (int i = 0; i < 25; i++)
        {
            final Window[] windows = Window.getWindows ();
            for (final Window w : windows)
            {
                if (w instanceof JDialog)
                {
                    isDialogSeen = true;
                    final JDialog d = (JDialog)w;
                    d.setVisible (false);
                }
            }
            dream (100);
        }
    }

    public void dream (long ms)
    {
        try
        {
            if (isRunning)
            {
                Thread.sleep (ms);
            }
            else
            {
                throw new InterruptedException ();
            }
        }
        catch (final InterruptedException e)
        {
        }
    }
}
