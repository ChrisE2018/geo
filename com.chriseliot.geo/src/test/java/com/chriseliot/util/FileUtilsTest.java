
package com.chriseliot.util;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;

import org.junit.jupiter.api.*;

public class FileUtilsTest
{
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
        assertEquals ("foo", test.removeDot (".foo"));
        assertEquals ("foo", test.removeDot ("foo"));
    }
}
