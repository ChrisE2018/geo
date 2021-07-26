
package com.chriseliot.geo.gui;

import java.awt.*;

import org.apache.logging.log4j.*;

public class CloseDialogThread extends Thread
{
    private final Logger logger = LogManager.getFormatterLogger (getClass ());
    private boolean isRunning = true;
    private boolean isDialogSeen = false;
    private boolean isTrace = false;
    private int limit = 50;

    public boolean isRunning ()
    {
        return isRunning;
    }

    public void halt ()
    {
        isRunning = false;
    }

    public boolean isDialogSeen ()
    {
        return isDialogSeen;
    }

    public void setTrace (boolean isTrace)
    {
        this.isTrace = isTrace;
    }

    public void setLimit (int limit)
    {
        this.limit = limit;
    }

    public int countVisibleDialogs ()
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
                    count++;
                }
            }
        }
        return count;
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
                    dream (50);
                    d.setVisible (false);
                    count++;
                }
            }
        }
        return count;
    }

    public int closeDialogs ()
    {
        int count = 0;
        final Window[] windows = Window.getWindows ();
        for (final Window w : windows)
        {
            if (w instanceof Dialog)
            {
                if (isTrace)
                {
                    logger.info ("Closing dialog %s", w);
                }
                final Dialog d = (Dialog)w;
                d.setVisible (false);
                dream (50);
                count++;
            }
        }
        return count;
    }

    @Override
    public void run ()
    {
        isRunning = true;
        if (isTrace)
        {
            logger.info ("Monitoring for dialog with %d windows", Window.getWindows ().length);
        }
        for (int i = 0; i < limit && isRunning; i++)
        {
            dream (100);
            if (closeVisibleDialogs () > 0)
            {
                isDialogSeen = true;
                isRunning = false;
            }
        }
        if (!isDialogSeen)
        {
            if (isTrace)
            {
                logger.info ("No visible dialog seen in %d iterations with %d windows", limit, Window.getWindows ().length);
            }
        }
        isRunning = false;
    }

    public void dream (long ms)
    {
        try
        {
            Thread.sleep (ms);
        }
        catch (final InterruptedException e)
        {
        }
    }

    public void dream (long ms, int n)
    {
        for (int i = 0; i < n && isRunning; i++)
        {
            dream (ms);
        }
    }
}
