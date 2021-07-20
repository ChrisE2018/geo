
package com.chriseliot.util;

import java.util.*;

public class TextUtils
{
    /**
     * Join a list of strings with punctuation in the middle. Inspired by Python List.join method.
     *
     * @param punctuation The punctuation to put between words.
     * @param haystack A list of words. May be empty.
     *
     * @return All words joined with punctuation in between.
     */
    public String join (String punctuation, List<String> haystack)
    {
        final StringBuilder builder = new StringBuilder ();
        if (!haystack.isEmpty ())
        {
            builder.append (haystack.get (0));
            for (int i = 1; i < haystack.size (); i++)
            {
                builder.append (punctuation);
                builder.append (haystack.get (i));
            }
        }
        return builder.toString ();
    }

    /**
     * Join a list of strings with punctuation in the middle. Inspired by Python List.join method.
     *
     * @param punctuation The punctuation to put between words.
     * @param haystack A list of words. May be empty.
     *
     * @return All words joined with punctuation in between.
     */
    public String join (String punctuation, Collection<String> haystack)
    {
        final StringBuilder builder = new StringBuilder ();
        if (!haystack.isEmpty ())
        {
            for (final String item : haystack)
            {
                if (builder.length () > 0)
                {
                    builder.append (punctuation);
                }
                builder.append (item);
            }
        }
        return builder.toString ();
    }

    /**
     * Join a list of strings with punctuation in the middle. Inspired by Python List.join method.
     * This is more efficient than constructing a stream and appending it to a StringBuilder.
     *
     * @param builder The StringBuilder to append the result into.
     * @param punctuation The punctuation to put between words.
     * @param haystack A list of words. May be empty.
     */
    public void join (StringBuilder builder, String punctuation, Collection<String> haystack)
    {
        if (!haystack.isEmpty ())
        {
            final int initialLength = builder.length ();
            for (final String item : haystack)
            {
                if (builder.length () > initialLength)
                {
                    builder.append (punctuation);
                }
                builder.append (item);
            }
        }
    }

    /**
     * Determine if some word ends with a search string.
     *
     * @param needle The string to search for. If this is the empty string it matches any choice.
     * @param haystack The set of choices. May be empty in which case the result is always false.
     *
     * @return True if any choice ends with the needle. Always false if there are no choices.
     */
    public boolean endsWith (String needle, String[] haystack)
    {
        for (final String choice : haystack)
        {
            if (choice.endsWith (needle))
            {
                return true;
            }
        }
        return false;
    }

    /** Determine if a search string is among the choices. */
    public boolean member (String needle, String[] haystack)
    {
        for (final String choice : haystack)
        {
            if (needle.equals (choice))
            {
                return true;
            }
        }
        return false;
    }
}
