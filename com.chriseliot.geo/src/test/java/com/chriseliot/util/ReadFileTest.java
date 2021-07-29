
package com.chriseliot.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfSystemProperty;

import com.chriseliot.geo.gui.CloseChooserThread;

public class ReadFileTest
{
    private final Logger logger = LogManager.getFormatterLogger (getClass ());

    // This test does not seem to be reliable.
    // The test name is modified to move it away from GetSaveFileTest.
    @DisabledIfSystemProperty (named = "java.awt.headless", matches = "true")
    @Test
    void getReadFile ()
    {
        final FileUtils test = new FileUtils ();
        assertNull (test.chooser);
        final CloseChooserThread thread = new CloseChooserThread (test);
        thread.start ();

        final File currentDir = new File ("data/").getAbsoluteFile ();
        logger.info ("Creating getReadFile dialog");
        test.getReadFile (null, "getReadFile test", currentDir, "Only xml", ".xml");
        logger.info ("getReadFile dialog returns");

        assertTrue (thread.isDialogSeen ());
        assertNull (test.chooser);
    }
}
