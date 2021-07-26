
package com.chriseliot.util;

import java.awt.Component;
import java.io.File;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileUtils
{
    /**
     * Determine if a pathname ends with an extension.
     *
     * @param pathname The pathname.
     * @param extension The extension. This may be given with or without a dot at the start.
     *
     * @return True if the pathname ends with the extension.
     */
    public boolean hasExtension (String pathname, String extension)
    {
        if (extension.startsWith ("."))
        {
            return pathname.endsWith (extension);
        }
        return pathname.endsWith ("." + extension);
    }

    /**
     * Determine if a file name ends with an extension.
     *
     * @param file The file.
     * @param extension The extension. This may be given with or without a dot at the start.
     *
     * @return True if the file name ends with the extension.
     */
    public boolean hasExtension (File file, String extension)
    {
        return hasExtension (file.getName (), extension);
    }

    /**
     * Get the extension from a pathname if it has one.
     *
     * @param pathname The pathname to check.
     *
     * @return The extension, including the dot, or null if there is none.
     */
    public String getExtension (String pathname)
    {
        final int pos = pathname.lastIndexOf (".");
        if (pos >= 0)
        {
            return pathname.substring (pos);
        }
        return null;
    }

    public String removeDot (String extension)
    {
        if (extension.startsWith ("."))
        {
            return extension.substring (1);
        }
        return extension;
    }

    /**
     * Modify a pathname to have the required extension.
     *
     * @param pathname The pathname. If this has a different extension it is removed first.
     * @param extension The required extension.
     *
     * @return The pathname modified to have the required extension.
     */
    public String setExtension (String pathname, String extension)
    {
        if (!hasExtension (pathname, extension))
        {
            String root = pathname;
            final int pos = pathname.lastIndexOf (".");
            if (pos >= 0)
            {
                root = pathname.substring (0, pos);
            }
            if (extension.startsWith ("."))
            {
                return root + extension;
            }
            else
            {
                return root + "." + extension;
            }
        }
        return pathname;
    }

    /**
     * Modify a file to have the required extension.
     *
     * @param file The file. If this has a different extension it is removed first.
     * @param extension The required extension.
     *
     * @return The file modified to have the required extension. This is done by creating a new file
     *         with the same parent directory and a filename modified to have the required
     *         extension.
     */
    public File setExtension (File file, String extension)
    {
        final String filename = file.getName ();
        final File parent = file.getParentFile ();
        return new File (parent, setExtension (filename, extension));
    }

    /** Choose a file to save to. */
    public File getSaveFile (Component parent, String title, File currentDir, String extension)
    {
        final JFileChooser fileChooser = new JFileChooser (currentDir)
        {
            @Override
            public void approveSelection ()
            {
                final File f = setExtension (getSelectedFile (), extension);
                if (f.exists () && getDialogType () == SAVE_DIALOG)
                {
                    final int result = JOptionPane.showConfirmDialog (this, "The file exists, overwrite?", "Existing file",
                            JOptionPane.YES_NO_CANCEL_OPTION);
                    switch (result)
                    {
                        case JOptionPane.YES_OPTION:
                            super.approveSelection ();
                            return;
                        case JOptionPane.NO_OPTION:
                            return;
                        case JOptionPane.CLOSED_OPTION:
                            return;
                        case JOptionPane.CANCEL_OPTION:
                            cancelSelection ();
                            return;
                    }
                }
                super.approveSelection ();
            }
        };
        fileChooser.setDialogTitle (title);

        final int userSelection = fileChooser.showSaveDialog (parent);

        if (userSelection == JFileChooser.APPROVE_OPTION)
        {
            final File file = fileChooser.getSelectedFile ();
            return setExtension (file, extension);
        }
        return null;
    }

    public File getReadFile (Component parent, String title, File currentDir, String extensionDescription, String extension)
    {

        final JFileChooser fileChooser = new JFileChooser (currentDir);
        // Should use a file filter here.
        final FileNameExtensionFilter filter = new FileNameExtensionFilter (extensionDescription, removeDot (extension));
        fileChooser.setFileFilter (filter);
        fileChooser.setDialogTitle (title);
        final int result = fileChooser.showOpenDialog (parent);
        if (result == JFileChooser.APPROVE_OPTION)
        {
            final File file = fileChooser.getSelectedFile ();
            System.out.println ("Selected file: " + file.getAbsolutePath ());
            return setExtension (file, extension);
        }
        return null;
    }
}
