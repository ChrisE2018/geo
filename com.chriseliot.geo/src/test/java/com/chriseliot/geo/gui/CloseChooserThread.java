
package com.chriseliot.geo.gui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JFileChooser;

import com.chriseliot.geo.TestSupport;
import com.chriseliot.util.FileUtils;

public class CloseChooserThread extends Thread
{
    private boolean isRunning = true;

    private boolean isDialogSeen = false;

    private final int limit = 50;

    private final FileUtils fu;

    public CloseChooserThread (FileUtils fu)
    {
        this.fu = fu;
        setDaemon (true);
    }

    public boolean isDialogSeen ()
    {
        return isDialogSeen;
    }

    public void closeDialog ()
    {
        final JFileChooser chooser = fu.chooser;
        if (chooser != null)
        {
            System.out.printf ("Found JFileChooser %s%n", chooser);
            chooser.cancelSelection ();
            isDialogSeen = true;
            isRunning = false;
        }
    }

    @Override
    public void run ()
    {
        isRunning = true;
        isDialogSeen = false;
        System.out.println ("Thread starts");
        for (int i = 0; i < limit && isRunning; i++)
        {
            TestSupport.dream (100); // Sleep with no exceptions
            closeDialog ();
        }
        assertTrue (isDialogSeen);
        System.out.println ("Thread exits");
    }
}
