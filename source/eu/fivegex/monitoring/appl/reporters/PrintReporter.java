// PrintReporter.java
// Author: Stuart Clayman
// Email: sclayman@ee.ucl.ac.uk
// Date: Sept 2009

package eu.fivegex.monitoring.appl.reporters;

import eu.reservoir.monitoring.core.AbstractReporter;
import eu.reservoir.monitoring.core.Measurement;


/**
 * A PrintReporter just prints a Measurement.
 */
public class PrintReporter extends AbstractReporter {
    /**
     * In a PrintReporter, report() just prints the Measurement.
     */
    
    public PrintReporter() {
        super("print-reporter");
    }
    
    @Override
    public void report(Measurement m) {
	System.out.println(m);
    }
}