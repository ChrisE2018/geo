
package com.chriseliot.geo.gui;

import javax.swing.RowFilter;

import com.chriseliot.geo.*;

public class SolutionRowFilter extends RowFilter<GeoSolution, Integer>
{
    private final GeoSolution geoSolution;

    public SolutionRowFilter (GeoSolution geoSolution)
    {
        this.geoSolution = geoSolution;
    }

    @Override
    public boolean include (Entry<? extends GeoSolution, ? extends Integer> entry)
    {
        final Integer id = entry.getIdentifier ();
        final GeoItem item = geoSolution.getItem (id);
        if (item instanceof GeoVertex)
        {
            final GeoVertex v = (GeoVertex)item;
            final GeoItem line1 = v.getLine1 ();
            if (line1.isOpen () && geoSolution.getVisibleItems ().contains (line1))
            {
                return true;
            }
            final GeoItem line2 = v.getLine2 ();
            if (line2.isOpen () && geoSolution.getVisibleItems ().contains (line2))
            {
                return true;
            }
            return false;
        }
        return geoSolution.getVisibleItems ().contains (item);
    }
}
