
package com.chriseliot.geo.gui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.JFileChooser;

import org.apache.logging.log4j.*;

import com.chriseliot.geo.TestSupport;
import com.chriseliot.util.FileUtils;

public class CloseChooserThread extends Thread
{
    private final Logger logger = LogManager.getFormatterLogger (getClass ());
    private boolean isRunning = true;
    private boolean isDialogSeen = false;
    private final int limit = 50;
    private final FileUtils fu;

    public CloseChooserThread (FileUtils fu)
    {
        this.fu = fu;
    }

    public boolean isDialogSeen ()
    {
        return isDialogSeen;
    }

    public int closeDialogs ()
    {
        int count = 0;
        final JFileChooser chooser = fu.chooser;

        if (chooser != null)
        {
            logger.info ("Found %s", chooser);
            if (!chooser.isValid ())
            {
                logger.info ("Validate %s", chooser);
                chooser.validate ();

                // Give the dialog time to get in a good state
                TestSupport.dream (10);
            }
            logger.info ("Closing %s", chooser);
            chooser.cancelSelection ();
            isDialogSeen = true;
            count++;
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
            if (closeDialogs () > 0)
            {
                isRunning = false;
            }
        }
        isRunning = false;
        assertTrue (isDialogSeen);
        TestSupport.dream (50);
    }
}
