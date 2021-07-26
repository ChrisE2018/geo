
package com.chriseliot.geo.gui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.*;

import com.chriseliot.geo.TestSupport;

public class CloseDialogThread extends Thread
{
    private boolean isRunning = true;
    private boolean isDialogSeen = false;
    private final int limit = 25;

    public void halt ()
    {
        isRunning = false;
    }

    public boolean isDialogSeen ()
    {
        return isDialogSeen;
    }

    public int closeVisibleDialogs ()
    {
        int count = 0;
        final Window[] windows = Window.getWindows ();
        for (final Window w : windows)
        {
            if (w instanceof Dialog)
            {
                final Dialog d = (Dialog)w;
                if (d.isVisible ())
                {
                    // Give the dialog time to get in a good state
                    TestSupport.dream (50);
                    d.setVisible (false);
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    public void run ()
    {
        isRunning = true;
        for (int i = 0; i < limit && isRunning; i++)
        {
            TestSupport.dream (100);
            if (closeVisibleDialogs () > 0)
            {
                isDialogSeen = true;
                isRunning = false;
            }
        }
        assertTrue (isDialogSeen);
        isRunning = false;
    }
}
