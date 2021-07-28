
package com.chriseliot.util;

import org.apache.logging.log4j.*;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.launcher.*;

/**
 * Call Launcher.registerTestExecutionListeners​(TestExecutionListener... listeners)
 *
 * @author cre
 *
 */

public class TestListener implements TestExecutionListener
{
    private final Logger logger = LogManager.getFormatterLogger (this.getClass ());

    public void dynamicTestRegistered​ (TestIdentifier testIdentifier)
    {
        logger.info ("dynamicTestRegistered​ %s", testIdentifier.getDisplayName ());
    }

    public void executionFinished​ (TestIdentifier testIdentifier, TestExecutionResult testExecutionResult)
    {
        logger.info ("executionFinished​​ %s#%s", testIdentifier.getDisplayName (), testExecutionResult.getStatus ());
    }

    public void executionSkipped​ (TestIdentifier testIdentifier, String reason)
    {
        logger.info ("executionSkipped​​ %s (%s)", testIdentifier.getDisplayName (), reason);
    }

    public void executionStarted​ (TestIdentifier testIdentifier)
    {
        logger.info ("executionStarted​​ %s", testIdentifier.getDisplayName ());
    }

    public void reportingEntryPublished​ (TestIdentifier testIdentifier, ReportEntry entry)
    {
        logger.info ("reportingEntryPublished​​ %s: %s", testIdentifier.getDisplayName (), entry);
    }

    public void testPlanExecutionFinished​ (TestPlan testPlan)
    {
        logger.info ("testPlanExecutionFinished​​ %s", testPlan);
    }

    public void testPlanExecutionStarted​ (TestPlan testPlan)
    {
        logger.info ("testPlanExecutionStarted​​ %s", testPlan);
    }
}
