
package com.chriseliot.util;

import java.util.Optional;

import org.apache.logging.log4j.*;
import org.junit.jupiter.api.extension.*;

public class Watcher implements TestWatcher
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    @Override
    public void testDisabled (ExtensionContext context, Optional<String> reason)
    {
        logger.info ("testDisabled %s", context);
    }

    @Override
    public void testSuccessful (ExtensionContext context)
    {
        logger.info ("testSuccessful %s", context.getDisplayName ());
    }

    @Override
    public void testAborted (ExtensionContext context, Throwable cause)
    {
        logger.info ("testAborted %s", context);
    }

    @Override
    public void testFailed (ExtensionContext context, Throwable cause)
    {
        logger.info ("testFailed %s", context);
    }
}
