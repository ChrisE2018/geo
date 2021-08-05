
package com.chriseliot.geo;

import static org.junit.jupiter.api.Assertions.*;

import java.awt.Color;
import java.util.List;

import org.junit.jupiter.api.Test;

class InferenceTest
{
    @Test
    void createTest ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem owner = new GeoItem (plane, "t", Color.black);
        final String reason = "test";
        final String formula = "2 + 2 == 4";
        final GeoItem[] terms = {owner};
        final Inference test = new Inference (owner, reason, formula, terms);
        assertNotNull (test.toString ());
    }

    @Test
    void getterTest ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem owner = new GeoItem (plane, "t", Color.black);
        final GeoItem other = new GeoItem (plane, "t", Color.red);
        final String reason = "test";
        final String formula = "2 + 2 == 4";
        final GeoItem[] terms = {owner};
        final Inference test = new Inference (owner, reason, formula, terms);
        assertEquals (owner, test.getOwner ());
        assertEquals (reason, test.getReason ());
        assertEquals (formula, test.getFormula ());
        assertEquals (terms, test.getTerms ());
        final List<GeoItem> termList = test.getTermList ();
        assertEquals (terms.length, termList.size ());
        for (final GeoItem item : terms)
        {
            assertTrue (termList.contains (item));
            assertTrue (test.hasTerm (item));
        }
        assertFalse (test.hasTerm (other));
    }

    @Test
    void formulaTest ()
    {
        final GeoPlane plane = new GeoPlane ();
        final GeoItem owner = new GeoItem (plane, "t", Color.black);
        final String reason = "test";
        final String formula = "2 + 2 == 4";
        final GeoItem[] terms = {owner};
        final Inference test = new Inference (owner, reason, formula, terms);

        assertFalse (test.isDetermined ());
        owner.setGivenStatus (GeoStatus.known);
        assertTrue (test.isDetermined ());
        assertEquals (formula, test.getInstantiation ());
        assertEquals (formula, Inference.getInstantiation (formula, terms));
    }
}
