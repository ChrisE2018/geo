
package com.chriseliot.geo;

import java.beans.*;

import org.apache.logging.log4j.*;

public class TraceStatusChangeListener implements PropertyChangeListener
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    @Override
    public void propertyChange (PropertyChangeEvent e)
    {
        logger.info ("%s status %s new value %s", e.getSource (), e.getOldValue (), e.getNewValue ());
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
