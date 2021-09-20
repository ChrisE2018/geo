
package com.chriseliot.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import com.chriseliot.geo.gui.CloseChooserThread;

public class GetSaveFileTest
{
    private final Logger logger = LogManager.getFormatterLogger (getClass ());

    // Can't run many times
    @DisabledIfSystemProperty (named = "java.awt.headless", matches = "true")
    @Test
    @Timeout (value = 5)
    @Disabled
    void getSaveFile (TestInfo testInfo)
    {
        logger.info ("Test %s", testInfo);
        final FileUtils test = new FileUtils ();
        assertNull (test.chooser);
        final CloseChooserThread thread = new CloseChooserThread (test);
        thread.start ();
        final File currentDir = new File ("data/").getAbsoluteFile ();
        logger.info ("Creating getSaveFile dialog");
        test.getSaveFile (null, "getSaveFile test", currentDir, ".xml");
        logger.info ("getSaveFile dialog returns");
        assertTrue (thread.isDialogSeen ());
        assertNull (test.chooser);
    }
}
