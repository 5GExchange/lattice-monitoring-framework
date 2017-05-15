package eu.fivegex.monitoring.appl.reporters;

import eu.reservoir.monitoring.core.AbstractReporter;
import eu.reservoir.monitoring.core.Measurement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A PrintReporter just prints a Measurement.
 */
public final class LoggerReporter extends AbstractReporter {
    /**
     * In a LoggerReporter, report() just logs the Measurement to the log file.
     */
    
    private static Logger LOGGER = LoggerFactory.getLogger(LoggerReporter.class);
    
    
    public LoggerReporter(String reporterName) {
        super(reporterName); 
    }
    
    
    @Override
    public void report(Measurement m) {
	LOGGER.info(m.toString());
    }
}