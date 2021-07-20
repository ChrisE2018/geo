
package com.chriseliot.util;

import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.Test;

public class TestTextUtils
{
    @Test
    public void testCreate ()
    {
        final TextUtils test = new TextUtils ();
        assertNotNull (test.toString ());
    }

    @Test
    public void testJoin ()
    {
        final TextUtils test = new TextUtils ();
        final String punctuation = "-";
        final List<String> haystack = new ArrayList<> ();
        assertEquals ("", test.join (punctuation, haystack));
        haystack.add ("alpha");
        assertEquals ("alpha", test.join (punctuation, haystack));
        haystack.add ("beta");
        assertEquals ("alpha-beta", test.join (punctuation, haystack));
        haystack.add ("gamma");
        assertEquals ("alpha-beta-gamma", test.join (punctuation, haystack));

        assertEquals ("alpha-beta-gamma", test.join (punctuation, new LinkedHashSet<> (haystack)));
        assertEquals ("", test.join (punctuation, new LinkedHashSet<> ()));
        final StringBuilder builder = new StringBuilder ();
        test.join (builder, punctuation, new LinkedHashSet<> ());
        assertEquals ("", builder.toString ());
        test.join (builder, punctuation, new LinkedHashSet<> (haystack));
        assertEquals ("alpha-beta-gamma", builder.toString ());
    }

    @Test
    public void testEndsWith ()
    {
        final TextUtils test = new TextUtils ();
        assertTrue (test.endsWith ("-", new String[] {"foo", "bar-"}));
        assertTrue (test.endsWith ("-", new String[] {"foo-", "bar-"}));
        assertFalse (test.endsWith ("-", new String[] {"foo", "bar"}));
        assertFalse (test.endsWith ("-", new String[] {"-foo", "bar"}));
        assertFalse (test.endsWith ("-", new String[] {"fo-o", "bar"}));
        assertTrue (test.endsWith ("", new String[] {"foo", "bar-"}));
        assertFalse (test.endsWith ("", new String[] {}));
    }

    @Test
    public void testMember ()
    {

        final TextUtils test = new TextUtils ();
        assertTrue (test.member ("foo", new String[] {"foo", "bar-"}));
        assertFalse (test.member ("foo", new String[] {"foo-", "bar-"}));
        assertFalse (test.member ("bar", new String[] {"foo", "bar-"}));
        assertTrue (test.member ("bar", new String[] {"foo", "bar"}));
        assertFalse (test.member ("bar", new String[] {}));
    }
}
