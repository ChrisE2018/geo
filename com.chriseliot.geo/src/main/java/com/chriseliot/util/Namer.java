
package com.chriseliot.util;

import java.util.*;

public class Namer
{
    private static Map<String, Integer> counters = new HashMap<> ();

    /** Reset all name sequences. */
    public static void reset ()
    {
        counters.clear ();
    }

    /** Generate a new name from the given root. */
    public String getname (String root)
    {
        Integer index = counters.get (root);
        if (index == null)
        {
            index = 0;
        }
        index++;
        counters.put (root, index);
        return String.format ("%s%03d", root, index);
    }
}
