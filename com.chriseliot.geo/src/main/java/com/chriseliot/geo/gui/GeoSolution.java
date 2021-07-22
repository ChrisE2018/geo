
package com.chriseliot.geo.gui;

import java.awt.Color;
import java.util.*;

import javax.swing.RowFilter;
import javax.swing.table.AbstractTableModel;

import org.apache.logging.log4j.*;

import com.chriseliot.geo.*;

/** Table of items in the geometry plane. */
public class GeoSolution extends AbstractTableModel
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());
    public static int OPEN_COLUMN = 0;
    public static int NAME_COLUMN = 1;
    public static int CLASS_COLUMN = 2;
    public static int PARENT_COLUMN = 3;
    public static int CHILDREN_COLUMN = 4;
    public static int STATUS_COLUMN = 5;
    public static int VALUE_COLUMN = 6;
    public static int FORMULA_COLUMN = 7;
    private static String[] COLUMNS = {"Open", "Name", "Class", "Parent", "Children", "Status", "Value", "Formula"};
    private static Class<?>[] COLUMN_CLASS =
        {Boolean.class, String.class, String.class, String.class, String.class, GeoStatus.class, String.class, String.class};
    private static boolean[] COLUMN_EDITABLE = {true, true, false, false, false, true, false, false};

    private final GeoPlane plane;

    private final List<GeoItem> items = new ArrayList<> ();
    private final List<GeoItem> visibleItems = new ArrayList<> ();

    public GeoSolution (GeoPlane plane)
    {
        this.plane = plane;
    }

    public class SolutionRowFilter extends RowFilter<GeoSolution, Integer>
    {
        @Override
        public boolean include (Entry<? extends GeoSolution, ? extends Integer> entry)
        {
            final Integer id = entry.getIdentifier ();
            final GeoItem item = items.get (id);
            if (item instanceof GeoVertex)
            {
                final GeoVertex v = (GeoVertex)item;
                final GeoItem line1 = v.getLine1 ();
                if (line1.isOpen () && visibleItems.contains (line1))
                {
                    return true;
                }
                final GeoItem line2 = v.getLine2 ();
                if (line2.isOpen () && visibleItems.contains (line2))
                {
                    return true;
                }
                return false;
            }
            return visibleItems.contains (item);
        }
    }

    public SolutionRowFilter getRowFilter ()
    {
        return new SolutionRowFilter ();
    }

    public void update ()
    {
        boolean change = false;
        for (final GeoItem item : plane.getItems ())
        {
            if (!items.contains (item))
            {
                change = true;
                addItem (item);
                setVisible (item, true);
            }
        }
        if (change)
        {
            fireTableDataChanged ();
        }
    }

    /** Items in the geometry plane. */
    private void addItem (GeoItem item)
    {
        if (!items.contains (item))
        {
            items.add (item);
            for (final GeoItem child : item.getChildren ())
            {
                addItem (child);
            }
        }
    }

    /** Items in the geometry plane. */
    public void remove (GeoItem item)
    {
        for (final GeoItem child : item.getChildren ())
        {
            remove (child);
        }
        items.remove (item);
        visibleItems.remove (item);
    }

    public void clear ()
    {
        visibleItems.clear ();
        items.clear ();
        fireTableDataChanged ();
    }

    public void setVisible (GeoItem item, boolean isVisible)
    {
        if (isVisible)
        {
            visibleItems.add (item);
            if (item.isOpen ())
            {
                for (final GeoItem child : item.getChildren ())
                {
                    setVisible (child, true);
                }
            }
        }
        else
        {
            visibleItems.remove (item);
        }
        fireTableDataChanged ();
    }

    public void setVisible (NamedPoint item, boolean isVisible)
    {
        if (isVisible)
        {
            visibleItems.add (item);
        }
        else
        {
            visibleItems.remove (item);
        }
        fireTableDataChanged ();
    }

    public void setVisible (NamedVariable item, boolean isVisible)
    {
        if (isVisible)
        {
            visibleItems.add (item);
        }
        else
        {
            visibleItems.remove (item);
        }
        fireTableDataChanged ();
    }

    public void expandAll ()
    {
        for (final GeoItem item : items)
        {
            if (item.hasChildren ())
            {
                item.setOpen (true);
            }
            setVisible (item, true);
        }
        fireTableDataChanged ();
    }

    @Override
    public int getRowCount ()
    {
        return items.size ();
    }

    public GeoItem getRowItem (int rowIndex)
    {
        return items.get (rowIndex);
    }

    @Override
    public int getColumnCount ()
    {
        return COLUMNS.length;
    }

    @Override
    public String getColumnName (int column)
    {
        if (column < COLUMNS.length)
        {
            return COLUMNS[column];
        }
        return "?";
    }

    @Override
    public Class<?> getColumnClass (int column)
    {
        if (column < COLUMN_CLASS.length)
        {
            return COLUMN_CLASS[column];
        }
        return Object.class;
    }

    @Override
    public boolean isCellEditable (int row, int column)
    {
        if (column < COLUMN_EDITABLE.length)
        {
            return COLUMN_EDITABLE[column];
        }
        return false;
    }

    public Color getCellColor (int rowIndex, int columnIndex)
    {
        final GeoItem row = items.get (rowIndex);
        final GeoStatus status = row.getStatus ();
        if (status != null)
        {
            return status.getColor ();
        }
        if (row instanceof NamedPoint)

        {
            return getCellColor ((NamedPoint)row, columnIndex);
        }
        else if (row instanceof NamedVariable)
        {
            return getCellColor ((NamedVariable)row, columnIndex);
        }
        return getCellColor (row, columnIndex);
    }

    /**
     *
     * @param row
     * @param columnIndex
     *
     * @return
     */
    private Color getCellColor (GeoItem row, int columnIndex)
    {
        final GeoStatus status = row.getStatus ();
        return status.getColor ();
    }

    private Color getCellColor (NamedPoint row, int columnIndex)
    {
        if (columnIndex == STATUS_COLUMN)
        {
            return row.getStatus ().getColor ();
        }
        return row.getColor ();
    }

    private Color getCellColor (NamedVariable row, int columnIndex)
    {
        if (columnIndex == STATUS_COLUMN)
        {
            return row.getStatus ().getColor ();
        }
        return row.getColor ();
    }

    @Override
    public Object getValueAt (int rowIndex, int columnIndex)
    {
        if (rowIndex < items.size ())
        {
            final Object row = items.get (rowIndex);
            if (row instanceof NamedPoint)
            {
                return getValueAt ((NamedPoint)row, columnIndex);
            }
            else if (row instanceof NamedVariable)
            {
                return getValueAt ((NamedVariable)row, columnIndex);
            }
            else if (row instanceof GeoItem)
            {
                return getValueAt ((GeoItem)row, columnIndex);
            }
        }
        return null;
    }

    private Object getValueAt (GeoItem item, int columnIndex)
    {
        if (columnIndex == OPEN_COLUMN)
        {
            return item.isOpen ();
        }
        if (columnIndex == NAME_COLUMN)
        {
            return item.getName ();
        }
        if (columnIndex == CLASS_COLUMN)
        {
            return item.getClass ().getSimpleName ();
        }
        if (columnIndex == PARENT_COLUMN)
        {
            if (item instanceof GeoVertex)
            {
                final GeoVertex v = (GeoVertex)item;
                return v.getLine1 ().getName () + ", " + v.getLine2 ().getName ();
            }
            final GeoItem parent = item.getParent ();
            if (parent != null)
            {
                return parent.getName ();
            }
            return "-";
        }
        if (columnIndex == CHILDREN_COLUMN)
        {
            final StringBuilder builder = new StringBuilder ();
            for (final GeoItem child : item.getChildren ())
            {
                if (builder.length () > 0)
                {
                    builder.append (", ");
                }
                builder.append (child.getName ());
            }
            return builder.toString ();
        }
        if (columnIndex == STATUS_COLUMN)
        {
            return item.getStatus ();
        }
        if (columnIndex == VALUE_COLUMN)
        {
            if (item instanceof NamedPoint)
            {
                final NamedPoint named = (NamedPoint)item;
                final java.awt.geom.Point2D.Double value = named.getPosition ();
                if (value != null)
                {
                    return String.format ("<%.2f, %.2f>", value.x, value.y);
                }
            }
            else if (item instanceof NamedVariable)
            {
                final NamedVariable variable = (NamedVariable)item;
                return variable.getDoubleValue ();
            }
            else if (item instanceof GeoVertex)
            {
                final GeoVertex v = (GeoVertex)item;
                return v.getAngle ().getDoubleValue ();
            }
            return "-";
        }
        if (columnIndex == FORMULA_COLUMN)
        {
            if (item instanceof NamedVariable)
            {
                final NamedVariable variable = (NamedVariable)item;
                return variable.getFormulaInstance ();
            }
        }
        return null;
    }

    @Override
    public void setValueAt (Object value, int rowIndex, int columnIndex)
    {
        final Object row = items.get (rowIndex);

        if (row instanceof NamedPoint)
        {
            setValueAt ((NamedPoint)row, columnIndex, value);
        }
        else if (row instanceof NamedVariable)
        {
            setValueAt ((NamedVariable)row, columnIndex, value);
        }
        else if (row instanceof GeoItem)
        {
            setValueAt ((GeoItem)row, columnIndex, value);
        }
    }

    /**
     *
     * @param row
     * @param columnIndex
     * @param value
     */
    private void setValueAt (GeoItem row, int columnIndex, Object value)
    {
        if (columnIndex == OPEN_COLUMN)
        {
            row.setOpen ((Boolean)value);
            for (final GeoItem child : row.getChildren ())
            {
                setVisible (child, (Boolean)value);
            }
        }
        else if (columnIndex == NAME_COLUMN)
        {
            row.setName ((String)value);
        }
        else if (columnIndex == STATUS_COLUMN)
        {
            logger.info ("Set %s status = %s", row, value);
            final GeoStatus status = (GeoStatus)value;
            row.setGivenStatus (status);
        }
    }

    @Override
    public String toString ()
    {
        final StringBuilder buffer = new StringBuilder ();
        buffer.append ("#<");
        buffer.append (getClass ().getSimpleName ());
        buffer.append (" ");
        buffer.append (System.identityHashCode (this));
        buffer.append (">");
        return buffer.toString ();
    }
}
