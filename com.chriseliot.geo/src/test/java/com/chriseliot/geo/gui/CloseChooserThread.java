
package com.chriseliot.geo.gui;

import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.swing.*;

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

    public void validateAndClose ()
    {
        final JFileChooser chooser = fu.chooser;
        logger.info ("Validate %s", chooser);
        SwingUtilities.invokeLater (new Runnable ()
        {
            @Override
            public void run ()
            {
                TestSupport.dream (100);
                if (chooser != null)
                {
                    chooser.validate ();
                    chooser.cancelSelection ();
                    isDialogSeen = true;
                }
            }
        });
    }

    public void closeDialog ()
    {
        final JFileChooser chooser = fu.chooser;
        if (chooser != null)
        {
            logger.info ("Found %s", chooser);
            chooser.cancelSelection ();
            isDialogSeen = true;
        }
    }

    @Override
    public void run ()
    {
        isRunning = true;
        isDialogSeen = false;
        validateAndClose ();
        for (int i = 0; i < limit && isRunning && !isDialogSeen; i++)
        {
            TestSupport.dream (100);
            closeDialog ();
        }
        isRunning = false;
        assertTrue (isDialogSeen);
    }
}
