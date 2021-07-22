
package com.chriseliot.util;

import java.io.File;

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
}
