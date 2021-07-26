
package com.chriseliot.util;

import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.time.Duration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;

import com.chriseliot.geo.gui.CloseDialogThread;

public class FileUtilsTest
{
    private final CloseDialogThread thread = new CloseDialogThread ();

    @Test
    void testCreate ()
    {
        final FileUtils test = new FileUtils ();
        assertNotNull (test.toString ());
    }

    @Test
    void testHasExtension ()
    {
        final FileUtils test = new FileUtils ();
        assertTrue (test.hasExtension ("foo.txt", ".txt"));
        assertTrue (test.hasExtension ("foo.txt", "txt"));
        assertFalse (test.hasExtension ("footxt", ".txt"));
        assertFalse (test.hasExtension ("foo.png", "txt"));
    }

    @Test
    void testHasExtension2 ()
    {
        final FileUtils test = new FileUtils ();
        assertTrue (test.hasExtension (new File ("foo.txt"), ".txt"));
        assertTrue (test.hasExtension (new File ("foo.txt"), "txt"));
        assertFalse (test.hasExtension (new File ("footxt"), ".txt"));
        assertFalse (test.hasExtension (new File ("foo.png"), "txt"));
    }

    @Test
    void testGetExtension ()
    {
        final FileUtils test = new FileUtils ();
        assertEquals (".txt", test.getExtension ("foo.txt"));
        assertEquals (".txt", test.getExtension ("foo.txt"));
        assertNull (test.getExtension ("footxt"));
        assertEquals (".png", test.getExtension ("foo.png"));
    }

    @Tag ("Fast")
    @Test
    void testSetExtension ()
    {
        final FileUtils test = new FileUtils ();
        assertEquals ("foo.txt", test.setExtension ("foo.txt", ".txt"));
        assertEquals ("foo.txt", test.setExtension ("foo.txt", "txt"));
        assertEquals ("footxt.txt", test.setExtension ("footxt", ".txt"));
        assertEquals ("foo.txt", test.setExtension ("foo.png", "txt"));
    }

    @Tag ("Fast")
    @Test
    void testSetExtension2 ()
    {
        final FileUtils test = new FileUtils ();
        assertEquals (new File ("foo.txt"), test.setExtension (new File ("foo.txt"), ".txt"));
        assertEquals (new File ("foo.txt"), test.setExtension (new File ("foo.txt"), "txt"));
        assertEquals (new File ("footxt.txt"), test.setExtension (new File ("footxt"), ".txt"));
        assertEquals (new File ("foo.txt"), test.setExtension (new File ("foo.png"), "txt"));
    }

    @Tag ("gui")
    @Test
    void getSaveFile (TestInfo testInfo)
    {
        System.out.printf ("Test %s\n", testInfo);
        final FileUtils test = new FileUtils ();
        thread.setTrace (true);
        thread.start ();
        assertTrue (thread.isRunning ());
        assertTimeout (Duration.ofSeconds (5), new Executable ()
        {
            @Override
            public void execute () throws Throwable
            {
                final File currentDir = new File ("data/").getAbsoluteFile ();
                System.out.printf ("Creating getSaveFile dialog\n");
                test.getSaveFile (null, "getSaveFile test", currentDir, ".xml");
                System.out.printf ("getSaveFile dialog returns\n");
            }
        });
        assertTrue (thread.isDialogSeen ());
        assertFalse (thread.isRunning ());
    }

    // This does not seem to be reliable.
    // @Test
    @Tag ("gui")
    void getReadFile ()
    {
        final FileUtils test = new FileUtils ();
        thread.setTrace (true);
        thread.closeDialogs ();
        thread.start ();

        assertTimeout (Duration.ofSeconds (5), new Executable ()
        {
            @Override
            public void execute () throws Throwable
            {
                final File currentDir = new File ("data/").getAbsoluteFile ();
                System.out.printf ("Creating getReadFile dialog\n");
                test.getReadFile (null, "getReadFile test", currentDir, "Only xml", ".xml");
                System.out.printf ("getReadFile dialog returns\n");
            }
        });

        assertTrue (thread.isDialogSeen ());
        assertFalse (thread.isRunning ());
    }
}
